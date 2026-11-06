package dev.avles.gym

import dev.avles.gym.core.xlsx.Cell
import dev.avles.gym.core.xlsx.MiniXml
import dev.avles.gym.core.xlsx.SheetSpec
import dev.avles.gym.core.xlsx.Xlsx
import dev.avles.gym.core.xlsx.XmlException
import dev.avles.gym.core.zip.ZipArchive
import dev.avles.gym.core.zip.ZipException
import dev.avles.gym.core.zip.ZipLimits
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.random.Random

/**
 * The hand-written zip, deflate and XML code that reads plan spreadsheets, checked
 * against the JDK's own zip implementation and against hostile input. The plan
 * file comes from outside the app, so "it never crashes, it only refuses" is the
 * property that matters most here.
 */
class XlsxTest {

    private fun javaZip(vararg entries: Pair<String, ByteArray>, level: Int = Deflater.DEFAULT_COMPRESSION): ByteArray {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            zip.setLevel(level)
            entries.forEach { (name, data) -> zip.putNextEntry(ZipEntry(name)); zip.write(data); zip.closeEntry() }
        }
        return bytes.toByteArray()
    }

    private val samples: List<ByteArray> = run {
        val r = Random(7)
        listOf(
            ByteArray(0),
            "a".encodeToByteArray(),
            "Idli, sambar and coffee. ".repeat(4000).encodeToByteArray(),
            ByteArray(70_000) { r.nextInt(256).toByte() },
            ByteArray(200_000) { (it % 251).toByte() },
            buildString { repeat(20_000) { append(r.nextInt(1000)).append(',') } }.encodeToByteArray()
        )
    }

    @Test
    fun `inflate matches the JDK at every compression level`() {
        for (level in listOf(0, 1, 6, 9)) {
            samples.forEachIndexed { i, data ->
                val read = ZipArchive.read(javaZip("x$i" to data, level = level))
                assertArrayEquals("sample $i at level $level", data, read.getValue("x$i"))
            }
        }
    }

    @Test
    fun `what the app writes the JDK reads back exactly`() {
        val entries = samples.mapIndexed { i, d -> "dir/part$i.bin" to d }
        val zip = ZipArchive.write(entries)
        val seen = HashMap<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(zip)).use { z ->
            while (true) { val e = z.nextEntry ?: break; seen[e.name] = z.readBytes() }
        }
        entries.forEach { (name, data) -> assertArrayEquals(name, data, seen.getValue(name)) }
        assertEquals(entries.size, ZipArchive.read(zip).size)
    }

    @Test
    fun `damaged or hostile zips are refused, never crash`() {
        val good = javaZip("a.xml" to "<a>hello</a>".repeat(500).encodeToByteArray())
        val cases = mutableListOf(
            "empty" to ByteArray(0),
            "text" to "not a zip at all".encodeToByteArray(),
            "truncated" to good.copyOf(good.size / 2),
            "duplicate names" to ZipArchive.write(listOf("a" to byteArrayOf(1), "a" to byteArrayOf(2)))
        )
        // Flip a byte inside the compressed data (it starts at 30 + the name's 5 bytes):
        // either the deflate stream breaks or the checksum no longer matches.
        for (at in listOf(35, 38, 41)) cases += "corrupt@$at" to good.copyOf().also { it[at] = (it[at].toInt() xor 0x5A).toByte() }
        cases.forEach { (label, bytes) ->
            try {
                ZipArchive.read(bytes)
                fail("$label was accepted")
            } catch (e: ZipException) {
                assertTrue(label, e.message!!.isNotBlank())
            }
        }
        // A tiny file that unpacks huge (a zip bomb) stops at the limit.
        val bomb = javaZip("bomb" to ByteArray(20 * 1024 * 1024), level = 9)
        assertTrue(bomb.size < 100_000)
        try {
            ZipArchive.read(bomb, ZipLimits(maxEntryBytes = 1024 * 1024))
            fail("bomb accepted")
        } catch (e: ZipException) { /* refused */ }
    }

    @Test
    fun `random bytes never escape as anything but a refusal`() {
        val r = Random(42)
        val base = Xlsx.write(listOf(SheetSpec("Week", listOf(listOf(Cell.Text("Date"), Cell.Text("Focus"))))))
        repeat(300) {
            val bytes = base.copyOf()
            repeat(1 + r.nextInt(8)) { val i = r.nextInt(bytes.size); bytes[i] = r.nextInt(256).toByte() }
            try { Xlsx.read(bytes) } catch (e: ZipException) { } catch (e: XmlException) { }
        }
    }

    @Test
    fun `a written workbook reads back cell for cell, formulas stay text`() {
        val rows = listOf(
            listOf(Cell.Text("Date"), Cell.Text("Food"), Cell.Text("Quantity")),
            listOf(Cell.Text("2026-11-09"), Cell.Text("Idli & sambar <hot>"), Cell.Number(3.0)),
            listOf(Cell.Text("2026-11-09"), Cell.Text("=HYPERLINK(\"x\")"), Cell.Number(1.5)),
            listOf(Cell.Text("2026-11-10"), Cell.Text("தயிர் சாதம்"), Cell.Number(1.0))
        )
        val bytes = Xlsx.write(listOf(SheetSpec("Meals", rows, widths = listOf(12.0, 30.0, 10.0), frozenRows = 1)))
        val sheets = Xlsx.read(bytes)
        assertEquals(listOf("Meals"), sheets.map { it.name })
        val read = sheets[0].rows
        assertEquals("Idli & sambar <hot>", read[1][1])
        assertEquals("3", read[1][2])
        assertEquals("=HYPERLINK(\"x\")", read[2][1])
        assertEquals("1.5", read[2][2])
        assertEquals("தயிர் சாதம்", read[3][1])
        assertEquals(listOf(1, 2, 3, 4), read.map { it.number })
        assertTrue("written as inline strings, never formulas", !bytes.decodeToString().contains("<f>"))
    }

    @Test
    fun `xml with a doctype or entity declaration is refused`() {
        val xxe = "<?xml version=\"1.0\"?><!DOCTYPE x [<!ENTITY e SYSTEM \"file:///etc/passwd\">]><x>&e;</x>"
        try { MiniXml.parse(xxe); fail("doctype accepted") } catch (e: XmlException) { }
        try { MiniXml.parse("<x>&e;</x>"); fail("unknown entity accepted") } catch (e: XmlException) { }
        assertEquals("a&b<c>é", MiniXml.parse("<x>a&amp;b&lt;c&gt;&#233;</x>").text)
        assertEquals("v", MiniXml.parse("<x:a xmlns:x=\"n\"><x:b k='v'/></x:a>").child("b")!!.attrs["k"])
        try { MiniXml.parse("<a>" .repeat(100) + "</a>".repeat(100)); fail("deep nesting accepted") } catch (e: XmlException) { }
    }

    @Test
    fun `column letters convert both ways`() {
        assertEquals(0, Xlsx.columnOf("A1"))
        assertEquals(25, Xlsx.columnOf("Z9"))
        assertEquals(26, Xlsx.columnOf("AA10"))
        listOf(0, 1, 25, 26, 51, 52, 701, 702).forEach { assertEquals(it, Xlsx.columnOf(Xlsx.columnName(it) + "1")) }
    }
}
