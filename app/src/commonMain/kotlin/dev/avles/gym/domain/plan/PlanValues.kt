package dev.avles.gym.domain.plan

import dev.avles.gym.core.Dates
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

/** Planned reps: a range, or a timed hold. AMRAP is a range of 0 to 0 with no hold. */
data class Reps(val low: Int, val high: Int, val holdSeconds: Int = 0)

/**
 * Parsers for the cell values a plan holds, each returning null for anything it
 * does not recognise rather than guessing. They accept the shapes people and
 * spreadsheet apps actually produce: "8-12" or "8–12", "90s" or "1:30", and the
 * serial numbers Excel stores when it decides a cell is a date or a time.
 */
object PlanValues {

    private val EXCEL_EPOCH = LocalDate(1899, 12, 30)
    private val WEEKDAYS = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")

    /** Control characters out, runs of whitespace as one space, trimmed. */
    fun clean(raw: String): String {
        val sb = StringBuilder(raw.length)
        var space = false
        for (ch in raw) {
            if (ch.isWhitespace() || ch.isISOControl()) { space = true; continue }
            if (space && sb.isNotEmpty()) sb.append(' ')
            space = false
            sb.append(ch)
        }
        return sb.toString()
    }

    /** "2026-11-09", "2026/11/09", "2026-11-09T00:00:00", or an Excel date serial. Day-first forms are refused as ambiguous. */
    fun date(raw: String): LocalDate? {
        val t = raw.trim()
        val iso = Regex("""^(\d{4})[-/.](\d{1,2})[-/.](\d{1,2})(?:[T ].*)?$""").matchEntire(t)
        if (iso != null) {
            val (y, m, d) = iso.destructured
            return runCatching { LocalDate(y.toInt(), m.toInt(), d.toInt()) }.getOrNull()
        }
        val serial = t.toDoubleOrNull() ?: return null
        if (serial < 30_000 || serial > 80_000) return null // 1982 to 2119; anything else is not a plan date
        return EXCEL_EPOCH.plus(DatePeriod(days = floor(serial).toInt()))
    }

    /** "18:30", "6:30", "18.30", "6 pm", "6:30 PM", or an Excel time fraction; as "HH:MM", 24-hour. */
    fun time(raw: String): String? {
        val t = raw.trim().lowercase()
        if (t.isEmpty()) return null
        t.toDoubleOrNull()?.let { f ->
            if (f < 0 || f >= 1) return null
            val minutes = (f * 1440).roundToInt().coerceAtMost(1439)
            return hhmm(minutes / 60, minutes % 60)
        }
        val m = Regex("""^(\d{1,2})(?:[:.](\d{2}))?(?::\d{2})?\s*(am|pm|a\.m\.|p\.m\.)?$""").matchEntire(t) ?: return null
        var h = m.groupValues[1].toInt()
        val min = m.groupValues[2].ifEmpty { "0" }.toInt()
        val suffix = m.groupValues[3]
        if (suffix.isEmpty() && m.groupValues[2].isEmpty()) return null // a bare "6" is not clearly a time
        if (suffix.isNotEmpty()) {
            if (h !in 1..12) return null
            if (suffix.startsWith("p") && h != 12) h += 12
            if (suffix.startsWith("a") && h == 12) h = 0
        }
        if (h !in 0..23 || min !in 0..59) return null
        return hhmm(h, min)
    }

    fun hhmm(h: Int, m: Int) = "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"

    /** Minutes after midnight for "HH:MM", or null. */
    fun minutesOf(hhmm: String): Int? {
        val parts = hhmm.split(':')
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        return if (h in 0..23 && m in 0..59) h * 60 + m else null
    }

    /** A whole number in [min]..[max]; "4" and "4.0" both count, "4.5" does not. */
    fun whole(raw: String, min: Int, max: Int): Int? {
        val d = raw.trim().toDoubleOrNull() ?: return null
        if (d != floor(d)) return null
        val n = d.toInt()
        return if (n in min..max) n else null
    }

    /** A number in [min]..[max], rounded to two places. */
    fun decimal(raw: String, min: Double, max: Double): Double? {
        val d = raw.trim().replace(',', '.').toDoubleOrNull() ?: return null
        if (d.isNaN() || d < min || d > max) return null
        return (d * 100).roundToInt() / 100.0
    }

    /** Kilograms: blank, "BW" or "bodyweight" are 0; "80", "80 kg", "80kg" are 80. */
    fun weight(raw: String): Double? {
        val t = raw.trim().lowercase()
        if (t.isEmpty() || t == "-" || t == "bw" || t == "bodyweight" || t == "body weight") return 0.0
        return decimal(t.removeSuffix("kgs").removeSuffix("kg").trim(), 0.0, 1000.0)
    }

    /** "8", "8-12", "8–12", "8 to 12", "45s", "45 sec", "1 min", "1:30", "AMRAP". */
    fun reps(raw: String): Reps? {
        val t = raw.trim().lowercase()
        if (t.isEmpty()) return null
        if (t == "amrap" || t == "max" || t == "max reps" || t == "to failure") return Reps(0, 0)
        whole(t, 1, 100)?.let { return Reps(it, it) }
        Regex("""^(\d{1,3})\s*(?:-|–|—|to)\s*(\d{1,3})$""").matchEntire(t)?.let {
            val a = it.groupValues[1].toInt()
            val b = it.groupValues[2].toInt()
            return if (a in 1..100 && b in a..100) Reps(a, b) else null
        }
        val hold = holdSeconds(t) ?: return null
        return if (hold in 1..3600) Reps(0, 0, hold) else null
    }

    private fun holdSeconds(t: String): Int? {
        Regex("""^(\d{1,4})\s*(s|sec|secs|second|seconds)$""").matchEntire(t)?.let { return it.groupValues[1].toInt() }
        Regex("""^(\d{1,2}(?:\.\d+)?)\s*(m|min|mins|minute|minutes)$""").matchEntire(t)?.let {
            return (it.groupValues[1].toDouble() * 60).roundToInt()
        }
        Regex("""^(\d{1,2}):(\d{2})$""").matchEntire(t)?.let {
            val s = it.groupValues[2].toInt()
            return if (s < 60) it.groupValues[1].toInt() * 60 + s else null
        }
        return null
    }

    /** Seconds in [min]..[max]: "90", "90s", "2 min", "1.5 min", "1:30". Blank is 0. */
    fun seconds(raw: String, min: Int, max: Int): Int? {
        val t = raw.trim().lowercase()
        if (t.isEmpty()) return 0
        val s = whole(t, 0, 100_000) ?: holdSeconds(t) ?: return null
        return if (s in min..max) s else null
    }

    fun iso(epochDay: Long): String = Dates.ofEpochDay(epochDay).toString()

    fun weekday(epochDay: Long): String = Dates.dayName(Dates.ofEpochDay(epochDay))

    /** Whether "Mon", "monday" or "MON." names the weekday of [epochDay]. */
    fun weekdayMatches(name: String, epochDay: Long): Boolean {
        val key = PlanSheets.headerKey(name).take(3)
        val index = WEEKDAYS.indexOf(key)
        if (index < 0) return false
        return Dates.ofEpochDay(epochDay).dayOfWeek.ordinal == index
    }

    fun shortWeekday(date: LocalDate): String = WEEKDAYS[date.dayOfWeek.ordinal].replaceFirstChar { it.uppercaseChar() }

    /** "4 x 8-12 · 60 kg · rest 1:30", the line a planned lift shows. */
    fun liftLine(sets: Int, repsLow: Int, repsHigh: Int, holdSeconds: Int, weightKg: Double, restSeconds: Int): String {
        val reps = when {
            holdSeconds > 0 -> "${holdSeconds}s"
            repsLow == 0 -> "AMRAP"
            repsLow == repsHigh -> "$repsLow"
            else -> "$repsLow-$repsHigh"
        }
        val parts = mutableListOf("$sets x $reps")
        if (weightKg > 0) parts += "${trim(weightKg)} kg"
        if (restSeconds > 0) parts += "rest ${restSeconds / 60}:${(restSeconds % 60).toString().padStart(2, '0')}"
        return parts.joinToString(" · ")
    }

    fun trim(d: Double): String = if (abs(d - d.roundToInt()) < 0.005) d.roundToInt().toString() else ((d * 100).roundToInt() / 100.0).toString()
}
