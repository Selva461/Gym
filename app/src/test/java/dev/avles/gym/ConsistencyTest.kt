package dev.avles.gym

import dev.avles.gym.domain.Consistency
import org.junit.Assert.assertEquals
import org.junit.Test

class ConsistencyTest {

    private val today = 20_000L

    @Test
    fun `no history is a streak of zero`() {
        assertEquals(0, Consistency.currentStreak(emptySet(), today))
    }

    @Test
    fun `consecutive days ending today count`() {
        val days = setOf(today, today - 1, today - 2)
        assertEquals(3, Consistency.currentStreak(days, today))
    }

    @Test
    fun `an empty today does not break a run that ended yesterday`() {
        val days = setOf(today - 1, today - 2, today - 3)
        assertEquals(3, Consistency.currentStreak(days, today))
    }

    @Test
    fun `a two day gap ends the run`() {
        val days = setOf(today - 2, today - 3)
        assertEquals(0, Consistency.currentStreak(days, today))
    }

    @Test
    fun `longest streak survives gaps`() {
        val days = setOf(1L, 2L, 3L, 4L, 7L, 8L, 20L)
        assertEquals(4, Consistency.longestStreak(days))
    }

    @Test
    fun `window count looks back the right distance`() {
        val days = setOf(today, today - 2, today - 6, today - 9)
        assertEquals(3, Consistency.daysHitIn(days, today, window = 7))
    }

    // ---- rest days -------------------------------------------------------

    /**
     * The reason rest days are worth marking at all. Recovery is part of a
     * programme, not an absence from one, and a streak that punishes a scheduled
     * day off teaches you to train through it.
     */
    @Test
    fun `a planned rest day joins the run rather than ending it`() {
        val trained = setOf(10L, 11L, 13L, 14L)
        // Day 12 was a planned rest.
        assertEquals(4, Consistency.currentStreak(trained, today = 14L, bridgedDays = setOf(12L)))
        // Without marking it, the run is only the two days since the gap.
        assertEquals(2, Consistency.currentStreak(trained, today = 14L))
    }

    @Test
    fun `resting today does not end the run`() {
        val trained = setOf(10L, 11L, 12L)
        assertEquals(3, Consistency.currentStreak(trained, today = 13L, bridgedDays = setOf(13L)))
    }

    /**
     * A missed day is recorded but not bridged. A run that survives everything is
     * not measuring anything; the app says a break is a restart rather than a loss,
     * and that is where the kindness belongs rather than in the arithmetic.
     */
    @Test
    fun `a missed day still breaks the run`() {
        val trained = setOf(10L, 11L, 13L, 14L)
        assertEquals(2, Consistency.currentStreak(trained, today = 14L, bridgedDays = emptySet()))
    }

    @Test
    fun `rest days join two runs in the longest streak too`() {
        val trained = setOf(1L, 2L, 4L, 5L, 6L)
        assertEquals(5, Consistency.longestStreak(trained, bridgedDays = setOf(3L)))
        assertEquals(3, Consistency.longestStreak(trained))
    }

    @Test
    fun `a rest day with no training either side is not a streak`() {
        assertEquals(0, Consistency.currentStreak(emptySet(), today = 5L, bridgedDays = setOf(5L)))
    }
}
