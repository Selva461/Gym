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
}
