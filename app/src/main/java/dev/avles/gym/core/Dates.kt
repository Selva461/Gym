package dev.avles.gym.core

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

/**
 * The wall clock and the calendar, in one place.
 *
 * Everything in the app that asks "what day is it" or "what time is it" asks here,
 * through kotlinx-datetime rather than java.time, so the same code answers on
 * Android and on iOS. Stored dates stay what they always were — epoch days, a plain
 * integer with no timezone in it — and this is the only place they turn back into
 * calendar dates.
 */
object Dates {

    fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    fun todayEpochDay(): Long = today().epochDay

    fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

    fun hourNow(): Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour

    fun ofEpochDay(day: Long): LocalDate = LocalDate.fromEpochDays(day.toInt())

    /** "Thursday". English on purpose: every other word in the app is. */
    fun dayName(date: LocalDate): String = titleCase(date.dayOfWeek.name)

    /** "24 September". */
    fun dayAndMonth(date: LocalDate): String = "${date.dayOfMonth} ${titleCase(date.month.name)}"

    private fun titleCase(upper: String): String = upper.lowercase().replaceFirstChar { it.uppercaseChar() }
}

/** Days since 1970-01-01, which is how every date in the database is stored. */
val LocalDate.epochDay: Long get() = toEpochDays().toLong()
