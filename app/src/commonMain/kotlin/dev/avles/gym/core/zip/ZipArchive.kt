package dev.avles.gym.core.zip

/**
 * How much a zip may unpack to before the app stops reading it. The defaults are
 * many times a real plan file (a year of plans is well under a megabyte) and far
 * below what would strain a phone.
 */
data class ZipLimits(
    val maxEntries: Int = 512,
    val maxEntryBytes: Int = 8 * 1024 * 1024,
    val maxTotalBytes: Int = 32 * 1024 * 1024
)

/**
 * Reads and writes zip archives, the container of every .xlsx, in plain Kotlin so
 * the same code runs on Android and iOS.
 *
 * Reading is deliberately narrow: the central directory is the only index trusted,
 * only stored and deflated entries are accepted, encrypted, multi-disk and zip64
 * archives are refused, duplicate names are refused (two entries with one name is
 * how some archive attacks hide a second payload), every offset is bounds checked
 * and every entry's CRC is verified. Nothing is ever written to disk, so a hostile
 * name such as "../../x" has nowhere to go.
 *
 * Writing produces stored (uncompressed) entries, which every spreadsheet app
 * reads; it keeps the writer a few lines long and obviously correct.
 */
object ZipArchive {

    private const val EOCD = 0x06054b50
    private const val CENTRAL = 0x02014b50
    private const val LOCAL = 0x04034b50

    /**
     * The entries of [bytes] whose name passes [wanted], unpacked. Throws
     * [ZipException] for anything that is not a well-formed, plain zip.
     */
    fun read(bytes: ByteArray, limits: ZipLimits = ZipLimits(), wanted: (String) -> Boolean = { true }): Map<String, ByteArray> {
        val eocd = findEndRecord(bytes)
        if (u16(bytes, eocd + 4) != 0 || u16(bytes, eocd + 6) != 0) throw ZipException("Split (multi-part) zips are not supported.")
        val count = u16(bytes, eocd + 10)
        val cdSize = u32(bytes, eocd + 12)
        val cdOffset = u32(bytes, eocd + 16)
        if (count == 0xFFFF || cdSize == 0xFFFFFFFFL || cdOffset == 0xFFFFFFFFL) throw ZipException("Very large (zip64) files are not supported.")
        if (count > limits.maxEntries) throw ZipException("The file holds more parts than a spreadsheet should.")
        if (cdOffset + cdSize > eocd) throw ZipException("The file is damaged (directory).")

        val result = LinkedHashMap<String, ByteArray>()
        val seen = HashSet<String>()
        var total = 0L
        var p = cdOffset.toInt()
        repeat(count) {
            need(bytes, p, 46)
            if (s32(bytes, p) != CENTRAL) throw ZipException("The file is damaged (directory entry).")
            val flags = u16(bytes, p + 8)
            val method = u16(bytes, p + 10)
            val crc = s32(bytes, p + 16)
            val compressed = u32(bytes, p + 20)
            val size = u32(bytes, p + 24)
            val nameLen = u16(bytes, p + 28)
            val extraLen = u16(bytes, p + 30)
            val commentLen = u16(bytes, p + 32)
            val localOffset = u32(bytes, p + 42)
            need(bytes, p + 46, nameLen)
            val name = bytes.decodeToString(p + 46, p + 46 + nameLen)
            p += 46 + nameLen + extraLen + commentLen

            if (!seen.add(name)) throw ZipException("The file has two parts with the same name.")
            if (name.endsWith("/") || !wanted(name)) return@repeat
            if (flags and 1 != 0) throw ZipException("Password-protected files cannot be read.")
            if (size > limits.maxEntryBytes) throw ZipException("A part of the file is larger than the app allows.")
            total += size
            if (total > limits.maxTotalBytes) throw ZipException("The file unpacks larger than the app allows.")

            val lo = localOffset.toInt()
            need(bytes, lo, 30)
            if (s32(bytes, lo) != LOCAL) throw ZipException("The file is damaged (local header).")
            val dataStart = lo + 30 + u16(bytes, lo + 26) + u16(bytes, lo + 28)
            if (compressed > Int.MAX_VALUE) throw ZipException("The file is damaged (size).")
            need(bytes, dataStart, compressed.toInt())

            val data = when (method) {
                0 -> {
                    if (compressed != size) throw ZipException("The file is damaged (stored size).")
                    bytes.copyOfRange(dataStart, dataStart + compressed.toInt())
                }
                8 -> Inflate.inflate(bytes, dataStart, compressed.toInt(), size.toInt(), limits.maxEntryBytes)
                else -> throw ZipException("The file uses a compression this app does not read.")
            }
            if (data.size.toLong() != size) throw ZipException("The file is damaged (size mismatch).")
            if (Crc32.of(data) != crc) throw ZipException("The file is damaged (checksum).")
            result[name] = data
        }
        return result
    }

    /** A zip of [entries] (name to content), stored, in the order given. */
    fun write(entries: List<Pair<String, ByteArray>>): ByteArray {
        val out = ByteSink()
        val central = ByteSink()
        entries.forEach { (name, data) ->
            val nameBytes = name.encodeToByteArray()
            val crc = Crc32.of(data)
            val offset = out.size
            out.i32(LOCAL); out.i16(20); out.i16(UTF8_FLAG); out.i16(0)
            out.i16(DOS_TIME); out.i16(DOS_DATE)
            out.i32(crc); out.i32(data.size); out.i32(data.size)
            out.i16(nameBytes.size); out.i16(0)
            out.bytes(nameBytes); out.bytes(data)

            central.i32(CENTRAL); central.i16(20); central.i16(20); central.i16(UTF8_FLAG); central.i16(0)
            central.i16(DOS_TIME); central.i16(DOS_DATE)
            central.i32(crc); central.i32(data.size); central.i32(data.size)
            central.i16(nameBytes.size); central.i16(0); central.i16(0); central.i16(0); central.i16(0)
            central.i32(0); central.i32(offset)
            central.bytes(nameBytes)
        }
        val cdOffset = out.size
        val cd = central.toByteArray()
        out.bytes(cd)
        out.i32(EOCD); out.i16(0); out.i16(0)
        out.i16(entries.size); out.i16(entries.size)
        out.i32(cd.size); out.i32(cdOffset); out.i16(0)
        return out.toByteArray()
    }

    private const val UTF8_FLAG = 0x0800
    private const val DOS_TIME = 0
    private const val DOS_DATE = (0 shl 9) or (1 shl 5) or 1 // 1980-01-01

    private fun findEndRecord(b: ByteArray): Int {
        if (b.size < 22) throw ZipException("This is not a spreadsheet file (.xlsx).")
        val stop = maxOf(0, b.size - 22 - 0xFFFF)
        var i = b.size - 22
        while (i >= stop) {
            if (s32(b, i) == EOCD) return i
            i--
        }
        throw ZipException("This is not a spreadsheet file (.xlsx).")
    }

    private fun need(b: ByteArray, at: Int, len: Int) {
        if (at < 0 || len < 0 || at.toLong() + len > b.size) throw ZipException("The file is cut short.")
    }

    private fun u16(b: ByteArray, i: Int): Int {
        need(b, i, 2)
        return (b[i].toInt() and 0xFF) or ((b[i + 1].toInt() and 0xFF) shl 8)
    }

    private fun s32(b: ByteArray, i: Int): Int {
        need(b, i, 4)
        return (b[i].toInt() and 0xFF) or ((b[i + 1].toInt() and 0xFF) shl 8) or
            ((b[i + 2].toInt() and 0xFF) shl 16) or ((b[i + 3].toInt() and 0xFF) shl 24)
    }

    private fun u32(b: ByteArray, i: Int): Long = s32(b, i).toLong() and 0xFFFFFFFFL

    private class ByteSink {
        private var buf = ByteArray(4096)
        var size = 0
            private set

        private fun room(n: Int) {
            if (size + n > buf.size) buf = buf.copyOf(maxOf(size + n, buf.size * 2))
        }

        fun i16(v: Int) { room(2); buf[size++] = v.toByte(); buf[size++] = (v shr 8).toByte() }
        fun i32(v: Int) { i16(v and 0xFFFF); i16(v ushr 16) }
        fun bytes(b: ByteArray) { room(b.size); b.copyInto(buf, size); size += b.size }
        fun toByteArray(): ByteArray = buf.copyOf(size)
    }
}
