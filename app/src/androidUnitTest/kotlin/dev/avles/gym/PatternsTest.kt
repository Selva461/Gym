package dev.avles.gym

import dev.avles.gym.domain.Patterns
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Patterns say *when*, which is where the fixable things are. These tests are
 * mostly about the ways a chart can lie: turning a day you forgot to log into a
 * day you ate nothing, or presenting two samples as a finding.
 */
class PatternsTest {

    private val monday = LocalDate.of(2026, 1, 5).toEpochDay()

    private fun macros(vararg days: Pair<Long, Pair<Double, Double>>) =
        days.associate { (d, v) -> d to Patterns.DayMacros(d, v.first, v.second) }

    @Test
    fun `weekday index is monday-first`() {
        assertEquals(0, Patterns.weekdayIndex(monday))
        assertEquals(6, Patterns.weekdayIndex(monday + 6))
        assertEquals(0, Patterns.weekdayIndex(monday + 7))
    }

    /**
     * A weekday you never trained has to appear with a rate of zero. Its absence is
     * the finding, and building the list from the log rather than the calendar
     * would hide exactly the thing worth seeing.
     */
    @Test
    fun `a weekday never trained still appears, at zero`() {
        val today = monday + 27
        val trained = (0..27L).map { monday + it }.filter { Patterns.weekdayIndex(it) != 4 }.toSet()
        val stats = Patterns.byWeekday(today, 28, trained, emptyMap())

        val friday = stats.first { it.index == 4 }
        assertEquals("Fri", friday.label)
        assertTrue("Friday should have samples", friday.samples >= 3)
        assertEquals(0f, friday.trainedRate, 0.001f)
        assertEquals(1f, stats.first { it.index == 0 }.trainedRate, 0.001f)
    }

    /**
     * An unlogged day is absent, not zero. A chart that plots it as zero calories
     * draws a cliff that never happened and shows your logging habit rather than
     * your eating.
     */
    @Test
    fun `an unlogged day is a gap in the series, not a zero`() {
        val m = macros(monday to (2000.0 to 150.0), monday + 2 to (1800.0 to 140.0))
        val series = Patterns.series(monday + 2, 3, m) { it.kcal }
        assertEquals(listOf(2000.0, null, 1800.0), series)
    }

    @Test
    fun `the rolling mean ignores gaps rather than averaging them as zero`() {
        val values = listOf(2000.0, null, 1000.0)
        val mean = Patterns.rollingMean(values, window = 3)
        assertEquals(2000.0, mean[0]!!, 0.001)
        assertEquals(2000.0, mean[1]!!, 0.001)
        assertEquals(1500.0, mean[2]!!, 0.001)
    }

    @Test
    fun `a rolling mean over nothing is null rather than zero`() {
        assertNull(Patterns.rollingMean(listOf(null, null))[1])
    }

    /**
     * Adherence divides by days logged, not days elapsed. Otherwise eating badly
     * and not writing it down collapse into one number that cannot say which to fix.
     */
    @Test
    fun `adherence is measured over days you logged`() {
        val values = listOf(2000.0, null, null, 2100.0, 1000.0)
        // Three logged, two within 15% of 2000.
        assertEquals(2f / 3f, Patterns.adherence(values, target = 2000.0), 0.001f)
        assertEquals(0f, Patterns.adherence(listOf(null, null), target = 2000.0), 0.001f)
        assertEquals(0f, Patterns.adherence(values, target = 0.0), 0.001f)
    }

    /**
     * Two Fridays is not a pattern. A screen that presents 0% off two samples as a
     * finding invites somebody to reorganise their week around noise.
     */
    @Test
    fun `a weekday with too few samples is not read as a pattern`() {
        val stats = Patterns.byWeekday(monday + 6, 7, emptySet(), emptyMap())
        stats.forEach { assertFalse("one sample should not be readable", it.enoughToRead) }
        assertNull(Patterns.weakestWeekday(stats))
    }

    @Test
    fun `the weakest weekday is named only when it actually stands out`() {
        val today = monday + 27
        val everyDay = (0..27L).map { monday + it }.toSet()
        // Trained every day: nothing stands out, so nothing is claimed.
        assertNull(Patterns.weakestWeekday(Patterns.byWeekday(today, 28, everyDay, emptyMap())))

        val exceptFriday = everyDay.filter { Patterns.weekdayIndex(it) != 4 }.toSet()
        val weakest = Patterns.weakestWeekday(Patterns.byWeekday(today, 28, exceptFriday, emptyMap()))
        assertEquals("Fri", weakest?.label)
    }

    @Test
    fun `mean macros average only the days with food logged`() {
        val today = monday + 13
        val m = macros(monday to (2000.0 to 150.0), monday + 7 to (1000.0 to 50.0))
        val stats = Patterns.byWeekday(today, 14, emptySet(), m)
        val mon = stats.first { it.index == 0 }
        assertEquals(2, mon.samples)
        assertEquals(2, mon.loggedFoodDays)
        assertEquals(1500.0, mon.meanKcal, 0.001)
        // A weekday with no food logged averages nothing rather than dragging to zero.
        assertEquals(0.0, stats.first { it.index == 1 }.meanKcal, 0.001)
        assertEquals(0, stats.first { it.index == 1 }.loggedFoodDays)
    }
}
