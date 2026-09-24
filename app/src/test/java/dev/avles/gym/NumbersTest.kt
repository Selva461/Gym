package dev.avles.gym

import dev.avles.gym.core.Dates
import dev.avles.gym.core.epochDay
import dev.avles.gym.domain.Numbers
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

/**
 * The portable replacements for String.format and java.time. Each is checked
 * against the JVM call it replaced, so a number or a date on screen cannot move
 * because of the port.
 */
class NumbersTest {

    /**
     * Non-negative only, because that is all the app displays, and because the two
     * differ on purpose below zero: String.format writes -0.04 to no places as "-0".
     */
    @Test
    fun `fixed agrees with String format across the range the app displays`() {
        var v = 0.0
        while (v <= 2000.0) {
            for (d in 0..2) {
                assertEquals("$v to $d places", String.format(Locale.US, "%.${d}f", v), Numbers.fixed(v, d))
            }
            v += 0.05
        }
    }

    @Test
    fun `fixed rounds the way a person reading the digits expects`() {
        assertEquals("0.2", Numbers.fixed(0.15, 1))
        assertEquals("2.3", Numbers.fixed(2.25, 1))
        assertEquals("1.5", Numbers.fixed(1.45, 1))
        assertEquals("-1.3", Numbers.fixed(-1.25, 1))
        assertEquals("0.0", Numbers.fixed(-0.04, 1))
        assertEquals("0", Numbers.fixed(-0.45, 0))
        assertEquals("3.00", Numbers.fixed(3.0, 2))
        assertEquals("1235", Numbers.fixed(1234.5678, 0))
    }

    @Test
    fun `trimmed drops a zero fraction and keeps a real one`() {
        assertEquals("60", Numbers.trimmed(60.0))
        assertEquals("62.5", Numbers.trimmed(62.5))
        assertEquals("2.3", Numbers.trimmed(2.25))
    }

    @Test
    fun `clock pads seconds`() {
        assertEquals("1:35", Numbers.clock(95))
        assertEquals("0:05", Numbers.clock(5))
        assertEquals("0:00", Numbers.clock(-3))
    }

    @Test
    fun `dates agree with java time`() {
        listOf(0L, 1L, 19_000L, 20_720L, 20_721L, 25_000L).forEach { day ->
            val ours = Dates.ofEpochDay(day)
            val theirs = java.time.LocalDate.ofEpochDay(day)
            assertEquals(theirs.toString(), ours.toString())
            assertEquals(day, ours.epochDay)
            assertEquals(
                theirs.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH),
                Dates.dayName(ours)
            )
            assertEquals(
                theirs.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH)),
                Dates.dayAndMonth(ours)
            )
        }
        assertEquals("Thursday", Dates.dayName(LocalDate(2026, 9, 24)))
    }
}
