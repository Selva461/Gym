package dev.avles.gym.core.xlsx

import dev.avles.gym.core.zip.ZipArchive
import dev.avles.gym.core.zip.ZipException
import dev.avles.gym.core.zip.ZipLimits

/** A spreadsheet row as read: its 1-based row number in the app it came from, and cell text by column. */
class SheetRow(val number: Int, val cells: List<String>) {
    operator fun get(column: Int): String = cells.getOrNull(column) ?: ""
    val isBlank: Boolean get() = cells.all { it.isBlank() }
}

/** One sheet as read: its tab name and its non-empty rows in order. */
class SheetData(val name: String, val rows: List<SheetRow>)

/** A cell to write: text, a number, or text in a style ([Style]). */
sealed class Cell {
    data class Text(val value: String, val style: Style = Style.PLAIN) : Cell()
    data class Number(val value: Double, val style: Style = Style.PLAIN) : Cell()
}

/**
 * The handful of looks a written sheet uses; their order is the order in styles.xml.
 * [TEXT] is the "@" number format: what is typed there stays text, so Excel does not
 * turn 2026-11-09 or 8-12 into a date.
 */
enum class Style { PLAIN, HEADER, WRAP, TITLE, NOTE, TEXT }

/** A drop-down on a column range, offering a fixed list. */
data class ListRule(val range: String, val options: List<String>)

/** A sheet to write. [widths] are Excel character widths per column; the first [frozenRows] stay on screen. */
class SheetSpec(
    val name: String,
    val rows: List<List<Cell>>,
    val widths: List<Double> = emptyList(),
    val frozenRows: Int = 0,
    val lists: List<ListRule> = emptyList()
)

/**
 * Reads and writes .xlsx files: Office Open XML spreadsheets, as written by Excel,
 * Google Sheets, LibreOffice and Python's openpyxl.
 *
 * Reading takes cell values only. A formula is never evaluated, only the value the
 * spreadsheet app last saved beside it, and macros (.xlsm parts), external links
 * and embedded objects are never opened, so a file cannot make the app run
 * anything. Size is bounded at every layer ([ZipLimits], [MiniXml] caps, and
 * [MAX_ROWS] / [MAX_COLUMNS] here).
 *
 * Writing uses inline strings, so text the person typed, such as a food called
 * "=1+1", lands in the sheet as text and never as a formula.
 */
object Xlsx {

    const val MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    const val MAX_FILE_BYTES = 10 * 1024 * 1024
    const val MAX_ROWS = 5_000
    const val MAX_COLUMNS = 40
    private const val MAX_CELL_CHARS = 2_000

    // ---- reading --------------------------------------------------------------

    /** Every sheet in [bytes], or an exception whose message can be shown as is. */
    fun read(bytes: ByteArray): List<SheetData> {
        if (bytes.size > MAX_FILE_BYTES) throw ZipException("The file is larger than a plan should be (10 MB).")
        val parts = ZipArchive.read(bytes) { it.endsWith(".xml") || it.endsWith(".rels") }
        fun xml(path: String): XmlNode? = parts[path]?.let { MiniXml.parse(it.decodeToString()) }

        val workbookPath = xml("_rels/.rels")?.all("Relationship")
            ?.firstOrNull { it.attrs["Type"].orEmpty().endsWith("/officeDocument") }
            ?.attrs?.get("Target")?.let { resolve("", it) } ?: "xl/workbook.xml"
        val workbook = xml(workbookPath) ?: throw XmlException("This is not an Excel workbook (.xlsx).")
        val dir = workbookPath.substringBeforeLast('/', "")
        val relsPath = (if (dir.isEmpty()) "" else "$dir/") + "_rels/" + workbookPath.substringAfterLast('/') + ".rels"
        val targets = xml(relsPath)?.all("Relationship").orEmpty()
            .associate { it.attrs["Id"].orEmpty() to it.attrs["Target"].orEmpty() }

        val shared = targets.values.firstOrNull { it.endsWith("sharedStrings.xml") }
            ?.let { xml(resolve(dir, it)) }
            ?.all("si")?.map(::richText).orEmpty()

        val sheets = workbook.child("sheets")?.all("sheet").orEmpty()
        return sheets.mapNotNull { sheet ->
            val target = targets[sheet.attrs["id"].orEmpty()] ?: return@mapNotNull null
            val node = xml(resolve(dir, target)) ?: return@mapNotNull null
            SheetData(sheet.attrs["name"].orEmpty(), rowsOf(node, shared))
        }
    }

    /** A zip path for a relationship target, relative to [base] unless it starts with '/'. */
    private fun resolve(base: String, target: String): String {
        val joined = if (target.startsWith("/")) target.drop(1) else if (base.isEmpty()) target else "$base/$target"
        val parts = ArrayList<String>()
        joined.split('/').forEach { seg ->
            when (seg) {
                "", "." -> Unit
                ".." -> parts.removeLastOrNull()
                else -> parts += seg
            }
        }
        return parts.joinToString("/")
    }

    /** Text of a shared or inline string, joining rich-text runs, skipping phonetic hints. */
    private fun richText(si: XmlNode): String {
        si.child("t")?.let { if (si.all("r").isEmpty()) return it.text }
        return buildString { si.all("r").forEach { r -> r.child("t")?.let { append(it.text) } } }
            .ifEmpty { si.child("t")?.text.orEmpty() }
    }

    private fun rowsOf(sheet: XmlNode, shared: List<String>): List<SheetRow> {
        val data = sheet.child("sheetData") ?: return emptyList()
        val rows = ArrayList<SheetRow>()
        var nextRow = 1
        for (row in data.all("row")) {
            val number = row.attrs["r"]?.toIntOrNull() ?: nextRow
            nextRow = number + 1
            if (number > MAX_ROWS) break
            val cells = ArrayList<String>()
            var nextCol = 0
            for (c in row.all("c")) {
                val col = c.attrs["r"]?.let(::columnOf) ?: nextCol
                nextCol = col + 1
                if (col >= MAX_COLUMNS) continue
                val value = cellValue(c, shared).let { if (it.length > MAX_CELL_CHARS) it.take(MAX_CELL_CHARS) else it }
                while (cells.size <= col) cells += ""
                cells[col] = value
            }
            val r = SheetRow(number, cells)
            if (!r.isBlank) rows += r
        }
        return rows
    }

    private fun cellValue(c: XmlNode, shared: List<String>): String {
        val v = c.child("v")?.text.orEmpty()
        return when (c.attrs["t"]) {
            "s" -> v.trim().toIntOrNull()?.let { shared.getOrNull(it) }.orEmpty()
            "inlineStr" -> c.child("is")?.let(::richText).orEmpty()
            "b" -> if (v.trim() == "1") "TRUE" else "FALSE"
            "e" -> ""
            else -> v.trim()
        }
    }

    /** "C12" to 2. Letters only; anything odd gives a column past [MAX_COLUMNS] so it is dropped. */
    internal fun columnOf(ref: String): Int {
        var col = 0
        for (ch in ref) {
            if (ch !in 'A'..'Z' && ch !in 'a'..'z') break
            col = col * 26 + (ch.uppercaseChar() - 'A' + 1)
            if (col > 16_384) return Int.MAX_VALUE
        }
        return if (col == 0) Int.MAX_VALUE else col - 1
    }

    internal fun columnName(index: Int): String {
        var n = index + 1
        val sb = StringBuilder()
        while (n > 0) { val r = (n - 1) % 26; sb.append('A' + r); n = (n - 1) / 26 }
        return sb.reverse().toString()
    }

    // ---- writing --------------------------------------------------------------

    fun write(sheets: List<SheetSpec>): ByteArray {
        require(sheets.isNotEmpty())
        val parts = ArrayList<Pair<String, String>>()
        parts += "[Content_Types].xml" to contentTypes(sheets.size)
        parts += "_rels/.rels" to ROOT_RELS
        parts += "xl/workbook.xml" to workbookXml(sheets)
        parts += "xl/_rels/workbook.xml.rels" to workbookRels(sheets.size)
        parts += "xl/styles.xml" to STYLES
        sheets.forEachIndexed { i, s -> parts += "xl/worksheets/sheet${i + 1}.xml" to sheetXml(s) }
        return ZipArchive.write(parts.map { it.first to it.second.encodeToByteArray() })
    }

    private fun contentTypes(n: Int) = buildString {
        append(XML_HEAD)
        append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">")
        append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>")
        append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>")
        append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>")
        append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>")
        for (i in 1..n) append("<Override PartName=\"/xl/worksheets/sheet$i.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>")
        append("</Types>")
    }

    private fun workbookXml(sheets: List<SheetSpec>) = buildString {
        append(XML_HEAD)
        append("<workbook xmlns=\"$MAIN_NS\" xmlns:r=\"$REL_NS\"><sheets>")
        sheets.forEachIndexed { i, s ->
            append("<sheet name=\"").append(escape(safeSheetName(s.name))).append("\" sheetId=\"${i + 1}\" r:id=\"rId${i + 1}\"/>")
        }
        append("</sheets></workbook>")
    }

    private fun workbookRels(n: Int) = buildString {
        append(XML_HEAD)
        append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">")
        for (i in 1..n) append("<Relationship Id=\"rId$i\" Type=\"$REL_NS/worksheet\" Target=\"worksheets/sheet$i.xml\"/>")
        append("<Relationship Id=\"rId${n + 1}\" Type=\"$REL_NS/styles\" Target=\"styles.xml\"/>")
        append("</Relationships>")
    }

    private fun sheetXml(s: SheetSpec) = buildString {
        append(XML_HEAD)
        append("<worksheet xmlns=\"$MAIN_NS\" xmlns:r=\"$REL_NS\">")
        if (s.frozenRows > 0) {
            append("<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"${s.frozenRows}\" topLeftCell=\"A${s.frozenRows + 1}\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>")
        }
        if (s.widths.isNotEmpty()) {
            append("<cols>")
            s.widths.forEachIndexed { i, w -> append("<col min=\"${i + 1}\" max=\"${i + 1}\" width=\"$w\" customWidth=\"1\"/>") }
            append("</cols>")
        }
        append("<sheetData>")
        s.rows.forEachIndexed { r, row ->
            append("<row r=\"${r + 1}\">")
            row.forEachIndexed { c, cell ->
                val ref = columnName(c) + (r + 1)
                when (cell) {
                    is Cell.Text -> when {
                        cell.value.isNotEmpty() -> {
                            append("<c r=\"$ref\" s=\"${cell.style.ordinal}\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                            append(escape(cell.value)).append("</t></is></c>")
                        }
                        cell.style != Style.PLAIN -> append("<c r=\"$ref\" s=\"${cell.style.ordinal}\"/>")
                    }
                    is Cell.Number -> append("<c r=\"$ref\" s=\"${cell.style.ordinal}\"><v>${number(cell.value)}</v></c>")
                }
            }
            append("</row>")
        }
        append("</sheetData>")
        if (s.lists.isNotEmpty()) {
            append("<dataValidations count=\"${s.lists.size}\">")
            s.lists.forEach { rule ->
                append("<dataValidation type=\"list\" allowBlank=\"1\" showErrorMessage=\"0\" sqref=\"${rule.range}\"><formula1>\"")
                append(escape(rule.options.joinToString(",")))
                append("\"</formula1></dataValidation>")
            }
            append("</dataValidations>")
        }
        append("</worksheet>")
    }

    private fun number(d: Double): String =
        if (d == kotlin.math.floor(d) && kotlin.math.abs(d) < 1e15) d.toLong().toString() else d.toString()

    private fun safeSheetName(name: String): String =
        name.filterNot { it in "[]:*?/\\" }.take(31).ifEmpty { "Sheet" }

    /** XML-escapes text and drops the control characters XML 1.0 cannot hold. */
    internal fun escape(s: String): String = buildString(s.length) {
        for (ch in s) when {
            ch == '&' -> append("&amp;")
            ch == '<' -> append("&lt;")
            ch == '>' -> append("&gt;")
            ch == '"' -> append("&quot;")
            ch < ' ' && ch != '\n' && ch != '\t' && ch != '\r' -> Unit
            ch == '￾' || ch == '￿' -> Unit
            else -> append(ch)
        }
    }

    private const val XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
    private const val MAIN_NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
    private const val REL_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"

    private const val ROOT_RELS = XML_HEAD +
        "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
        "<Relationship Id=\"rId1\" Type=\"$REL_NS/officeDocument\" Target=\"xl/workbook.xml\"/>" +
        "</Relationships>"

    /** Cell formats in [Style] order: plain, header (bold on amber), wrapped, title, note (grey italic wrapped), text. */
    private const val STYLES = XML_HEAD +
        "<styleSheet xmlns=\"$MAIN_NS\">" +
        "<fonts count=\"4\">" +
        "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
        "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
        "<font><b/><sz val=\"16\"/><name val=\"Calibri\"/></font>" +
        "<font><i/><sz val=\"10\"/><color rgb=\"FF666666\"/><name val=\"Calibri\"/></font>" +
        "</fonts>" +
        "<fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill>" +
        "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFF2D49B\"/><bgColor indexed=\"64\"/></patternFill></fill></fills>" +
        "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>" +
        "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
        "<cellXfs count=\"6\">" +
        "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>" +
        "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/>" +
        "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyAlignment=\"1\"><alignment wrapText=\"1\" vertical=\"top\"/></xf>" +
        "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>" +
        "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment wrapText=\"1\" vertical=\"top\"/></xf>" +
        "<xf numFmtId=\"49\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>" +
        "</cellXfs>" +
        "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>" +
        "</styleSheet>"
}
