package dev.avles.gym.domain

import dev.avles.gym.core.Dates
import dev.avles.gym.core.epochDay
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * A month laid out as the calendar on a wall: weeks as rows, Monday first, blanks
 * before the 1st and after the last day. The training calendar Strong and Hevy
 * show is this grid with a mark on every day something was done.
 */
object MonthGrid {

    /** One row per week; each cell is an epoch day, or null for a day outside the month. */
    fun weeks(anyDayInMonth: LocalDate): List<List<Long?>> {
        val first = LocalDate(anyDayInMonth.year, anyDayInMonth.month, 1)
        val days = daysIn(first)
        val lead = first.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal // 0 for Monday
        val cells = List<Long?>(lead) { null } + (0 until days).map { first.epochDay + it }
        val padded = cells + List<Long?>((7 - cells.size % 7) % 7) { null }
        return padded.chunked(7)
    }

    /** The 1st of the month before or after, for the arrows either side of the title. */
    fun shift(anyDayInMonth: LocalDate, months: Int): LocalDate {
        val index = anyDayInMonth.year * 12 + anyDayInMonth.monthNumber - 1 + months
        return LocalDate(index / 12, index % 12 + 1, 1)
    }

    /** How many of the month's days are in [marked]. */
    fun countIn(anyDayInMonth: LocalDate, marked: Set<Long>): Int =
        weeks(anyDayInMonth).flatten().count { it != null && it in marked }

    private fun daysIn(first: LocalDate): Int {
        val next = shift(first, 1)
        return (next.epochDay - first.epochDay).toInt()
    }

    /** "October 2026". */
    fun title(anyDayInMonth: LocalDate): String =
        Dates.dayAndMonth(anyDayInMonth).substringAfter(' ') + " " + anyDayInMonth.year
}
