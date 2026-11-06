package dev.avles.gym.core.zip

/** A zip or deflate stream this app refuses to read, with a reason a person can act on. */
class ZipException(message: String) : Exception(message)

/**
 * A raw DEFLATE decoder (RFC 1951), the compression inside every .xlsx.
 *
 * Written here rather than borrowed from java.util.zip because commonMain is also
 * compiled for iOS, where the JDK does not exist; it follows the structure of
 * zlib's reference decoder "puff", which favours being obviously correct over
 * being fast. A week's plan is a few hundred kilobytes, so speed does not matter.
 *
 * Every read is bounds checked and the output is capped at [maxOut], so a
 * truncated, corrupt or deliberately huge ("zip bomb") file ends in a
 * [ZipException], never in an index error or an out-of-memory crash.
 */
internal class Inflate private constructor(
    private val src: ByteArray,
    private var pos: Int,
    private val end: Int,
    private val maxOut: Int,
    sizeHint: Int
) {
    private var bitBuf = 0
    private var bitCount = 0
    private var out = ByteArray(sizeHint.coerceIn(256, maxOut.coerceAtLeast(256)))
    private var outLen = 0

    private fun bits(need: Int): Int {
        var value = bitBuf
        while (bitCount < need) {
            if (pos >= end) throw ZipException("The file is cut short.")
            value = value or ((src[pos++].toInt() and 0xFF) shl bitCount)
            bitCount += 8
        }
        bitBuf = value ushr need
        bitCount -= need
        return value and ((1 shl need) - 1)
    }

    private fun ensure(extra: Int) {
        val needed = outLen + extra
        if (needed > maxOut) throw ZipException("A part of the file unpacks larger than the app allows.")
        if (needed > out.size) {
            val grown = maxOf(needed, minOf(maxOut, out.size * 2))
            out = out.copyOf(grown)
        }
    }

    private fun stored() {
        bitBuf = 0
        bitCount = 0
        if (pos + 4 > end) throw ZipException("The file is cut short.")
        val len = (src[pos].toInt() and 0xFF) or ((src[pos + 1].toInt() and 0xFF) shl 8)
        val nlen = (src[pos + 2].toInt() and 0xFF) or ((src[pos + 3].toInt() and 0xFF) shl 8)
        pos += 4
        if (len != (nlen.inv() and 0xFFFF)) throw ZipException("The file is damaged (stored block).")
        if (pos + len > end) throw ZipException("The file is cut short.")
        ensure(len)
        src.copyInto(out, outLen, pos, pos + len)
        outLen += len
        pos += len
    }

    private class Huffman(val count: IntArray = IntArray(MAX_BITS + 1), val symbol: IntArray)

    private fun decode(h: Huffman): Int {
        var code = 0
        var first = 0
        var index = 0
        for (len in 1..MAX_BITS) {
            code = code or bits(1)
            val count = h.count[len]
            if (code - count < first) return h.symbol[index + (code - first)]
            index += count
            first = (first + count) shl 1
            code = code shl 1
        }
        throw ZipException("The file is damaged (bad code).")
    }

    /** Builds a canonical Huffman table; returns 0 when complete, >0 incomplete, <0 over-subscribed. */
    private fun construct(h: Huffman, lengths: IntArray, offset: Int, n: Int): Int {
        h.count.fill(0)
        for (s in 0 until n) h.count[lengths[offset + s]]++
        if (h.count[0] == n) return 0
        var left = 1
        for (len in 1..MAX_BITS) {
            left = (left shl 1) - h.count[len]
            if (left < 0) return left
        }
        val offs = IntArray(MAX_BITS + 1)
        for (len in 1 until MAX_BITS) offs[len + 1] = offs[len] + h.count[len]
        for (s in 0 until n) {
            val l = lengths[offset + s]
            if (l != 0) h.symbol[offs[l]++] = s
        }
        return left
    }

    private fun codes(lencode: Huffman, distcode: Huffman) {
        while (true) {
            var symbol = decode(lencode)
            when {
                symbol < 256 -> {
                    ensure(1)
                    out[outLen++] = symbol.toByte()
                }
                symbol == 256 -> return
                else -> {
                    symbol -= 257
                    if (symbol >= 29) throw ZipException("The file is damaged (length).")
                    val len = LBASE[symbol] + bits(LEXT[symbol])
                    symbol = decode(distcode)
                    if (symbol >= 30) throw ZipException("The file is damaged (distance).")
                    val dist = DBASE[symbol] + bits(DEXT[symbol])
                    if (dist > outLen) throw ZipException("The file is damaged (distance too far).")
                    ensure(len)
                    var from = outLen - dist
                    repeat(len) { out[outLen++] = out[from++] }
                }
            }
        }
    }

    private fun fixed() {
        val lengths = IntArray(FIXED_LCODES)
        for (s in 0 until 144) lengths[s] = 8
        for (s in 144 until 256) lengths[s] = 9
        for (s in 256 until 280) lengths[s] = 7
        for (s in 280 until FIXED_LCODES) lengths[s] = 8
        val lencode = Huffman(symbol = IntArray(FIXED_LCODES))
        construct(lencode, lengths, 0, FIXED_LCODES)
        val dl = IntArray(MAX_DCODES) { 5 }
        val distcode = Huffman(symbol = IntArray(MAX_DCODES))
        construct(distcode, dl, 0, MAX_DCODES)
        codes(lencode, distcode)
    }

    private fun dynamic() {
        val nlen = bits(5) + 257
        val ndist = bits(5) + 1
        val ncode = bits(4) + 4
        if (nlen > MAX_LCODES || ndist > MAX_DCODES) throw ZipException("The file is damaged (table size).")
        val lengths = IntArray(MAX_LCODES + MAX_DCODES)
        for (i in 0 until ncode) lengths[ORDER[i]] = bits(3)
        val lencode = Huffman(symbol = IntArray(MAX_LCODES))
        val distcode = Huffman(symbol = IntArray(MAX_DCODES))
        if (construct(lencode, lengths, 0, 19) != 0) throw ZipException("The file is damaged (code lengths).")

        var index = 0
        while (index < nlen + ndist) {
            var symbol = decode(lencode)
            if (symbol < 16) {
                lengths[index++] = symbol
            } else {
                var len = 0
                when (symbol) {
                    16 -> {
                        if (index == 0) throw ZipException("The file is damaged (repeat).")
                        len = lengths[index - 1]
                        symbol = 3 + bits(2)
                    }
                    17 -> symbol = 3 + bits(3)
                    else -> symbol = 11 + bits(7)
                }
                if (index + symbol > nlen + ndist) throw ZipException("The file is damaged (lengths overflow).")
                repeat(symbol) { lengths[index++] = len }
            }
        }
        if (lengths[256] == 0) throw ZipException("The file is damaged (no end code).")
        var err = construct(lencode, lengths, 0, nlen)
        if (err < 0 || (err > 0 && nlen - lencode.count[0] != 1)) throw ZipException("The file is damaged (literal codes).")
        err = construct(distcode, lengths, nlen, ndist)
        if (err < 0 || (err > 0 && ndist - distcode.count[0] != 1)) throw ZipException("The file is damaged (distance codes).")
        codes(lencode, distcode)
    }

    private fun run(): ByteArray {
        do {
            val last = bits(1)
            when (bits(2)) {
                0 -> stored()
                1 -> fixed()
                2 -> dynamic()
                else -> throw ZipException("The file is damaged (block type).")
            }
        } while (last == 0)
        return out.copyOf(outLen)
    }

    companion object {
        private const val MAX_BITS = 15
        private const val MAX_LCODES = 286
        private const val MAX_DCODES = 30
        private const val FIXED_LCODES = 288

        private val LBASE = intArrayOf(3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31, 35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258)
        private val LEXT = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0)
        private val DBASE = intArrayOf(1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193, 257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577)
        private val DEXT = intArrayOf(0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13)
        private val ORDER = intArrayOf(16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15)

        /** Decodes [length] bytes of raw deflate from [src] at [offset], to at most [maxOut] bytes. */
        fun inflate(src: ByteArray, offset: Int, length: Int, expectedSize: Int, maxOut: Int): ByteArray {
            if (offset < 0 || length < 0 || offset + length > src.size) throw ZipException("The file is cut short.")
            return Inflate(src, offset, offset + length, maxOut, expectedSize).run()
        }
    }
}

/** CRC-32 (the zip polynomial), to check each part unpacked to what was packed. */
internal object Crc32 {
    private val TABLE = IntArray(256) { n ->
        var c = n
        repeat(8) { c = if (c and 1 != 0) (c ushr 1) xor 0xEDB88320.toInt() else c ushr 1 }
        c
    }

    fun of(data: ByteArray): Int {
        var c = -1
        for (b in data) c = TABLE[(c xor b.toInt()) and 0xFF] xor (c ushr 8)
        return c.inv()
    }
}
