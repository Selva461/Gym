package dev.avles.gym.domain

import kotlin.math.abs

/**
 * Turning numbers into text, identically on every platform and in every locale.
 *
 * This replaces String.format, for two reasons. It does not exist outside the JVM,
 * so it could not follow the app onto iOS. And it formats by the phone's locale
 * unless told otherwise, which has bitten this app before: the weight dialog seeds
 * its field from a formatted number and parses it back, so a locale that writes
 * 62.5 as "62,5" round-tripped it into 625 kg. Here it is always a dot.
 */
object Numbers {

    /**
     * [v] to exactly [decimals] places, rounding half up on the digits as written —
     * the rounding String.format uses, so no figure on screen moves because of this.
     *
     * It rounds the decimal text rather than the binary value on purpose. 0.35 is
     * stored as 0.34999999999999997; arithmetic on that rounds it to 0.3, while
     * anyone reading "0.35" expects 0.4, and so did String.format.
     */
    fun fixed(v: Double, decimals: Int): String {
        require(decimals in 0..6) { "decimals must be 0..6, was $decimals" }
        if (v.isNaN() || v.isInfinite()) return "0"
        val text = abs(v).toString()
        val e = text.indexOfFirst { it == 'E' || it == 'e' }
        val mantissa = if (e < 0) text else text.substring(0, e)
        val exponent = if (e < 0) 0 else text.substring(e + 1).toInt()
        val whole = mantissa.substringBefore('.')
        var digits = whole + mantissa.substringAfter('.', "")
        var point = whole.length + exponent
        if (point < 0) {
            digits = "0".repeat(-point) + digits
            point = 0
        }
        val keep = point + decimals
        if (digits.length < keep) digits = digits.padEnd(keep, '0')
        var kept = digits.substring(0, keep)
        if (digits.length > keep && digits[keep] >= '5') kept = increment(kept)
        val carried = kept.length - keep
        val integer = kept.substring(0, point + carried).trimStart('0').ifEmpty { "0" }
        val body = if (decimals == 0) integer else "$integer.${kept.substring(point + carried)}"
        return if (v < 0 && kept.any { it != '0' }) "-$body" else body
    }

    /** "0999" -> "1000", "99" -> "100", "" -> "1". */
    private fun increment(digits: String): String {
        val chars = digits.toCharArray()
        for (i in chars.indices.reversed()) {
            if (chars[i] != '9') {
                chars[i] = chars[i] + 1
                return chars.concatToString()
            }
            chars[i] = '0'
        }
        return "1" + chars.concatToString()
    }

    /** "60" for a whole number, "62.5" otherwise. */
    fun trimmed(v: Double): String = if (v % 1.0 == 0.0) fixed(v, 0) else fixed(v, 1)

    /** 95 seconds as "1:35". */
    fun clock(seconds: Int): String {
        val s = seconds.coerceAtLeast(0)
        return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
    }
}
