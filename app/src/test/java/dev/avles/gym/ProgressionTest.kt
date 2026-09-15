package dev.avles.gym

import dev.avles.gym.data.Exercise
import dev.avles.gym.data.SetRow
import dev.avles.gym.domain.Progression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionTest {

    private val squat = Exercise(
        id = 1, name = "Back squat", muscleGroup = "Legs",
        repLow = 4, repHigh = 8, incrementKg = 5.0
    )

    private val curl = Exercise(
        id = 2, name = "Dumbbell curl", muscleGroup = "Pull",
        repLow = 8, repHigh = 12, incrementKg = 1.0
    )

    private fun set(
        id: Long,
        weight: Double,
        reps: Int,
        sessionId: Long = 1,
        exerciseId: Long = 1,
        warmup: Boolean = false,
        loggedAt: Long = id
    ) = SetRow(
        setId = id, sessionId = sessionId, exerciseId = exerciseId,
        exerciseName = "Back squat", setIndex = 1, weightKg = weight,
        reps = reps, isWarmup = warmup, loggedAt = loggedAt
    )

    @Test
    fun `no history means no recommendation, not a guessed starting weight`() {
        assertNull(Progression.recommend(squat, emptyList()))
    }

    @Test
    fun `warm-ups alone are not history`() {
        val history = listOf(set(1, 40.0, 10, warmup = true))
        assertNull(Progression.recommend(squat, history))
    }

    @Test
    fun `short of the top of the range, add a rep at the same weight`() {
        val history = listOf(set(1, 100.0, 5), set(2, 100.0, 5))
        val rec = Progression.recommend(squat, history)!!
        assertEquals(100.0, rec.weightKg, 0.001)
        assertEquals(6, rec.reps)
        assertFalse(rec.isStepUp)
    }

    @Test
    fun `hitting the top of the range steps the weight up and resets the reps`() {
        val history = listOf(set(1, 100.0, 8))
        val rec = Progression.recommend(squat, history)!!
        assertEquals(105.0, rec.weightKg, 0.001)
        assertEquals(4, rec.reps)
        assertTrue(rec.isStepUp)
    }

    @Test
    fun `beating the top of the range still only steps up once`() {
        val history = listOf(set(1, 100.0, 12))
        val rec = Progression.recommend(squat, history)!!
        assertEquals(105.0, rec.weightKg, 0.001)
        assertEquals(4, rec.reps)
    }

    @Test
    fun `the increment is the lift's own, not a global one`() {
        val history = listOf(set(1, 12.0, 12, exerciseId = 2))
        val rec = Progression.recommend(curl, history)!!
        assertEquals(13.0, rec.weightKg, 0.001)
        assertEquals(8, rec.reps)
    }

    @Test
    fun `only the most recent session decides the target`() {
        // An older, heavier session must not override a deliberate deload.
        val history = listOf(
            set(9, 80.0, 5, sessionId = 3, loggedAt = 300),
            set(2, 120.0, 5, sessionId = 1, loggedAt = 100)
        )
        val rec = Progression.recommend(squat, history)!!
        assertEquals(80.0, rec.weightKg, 0.001)
        assertEquals(6, rec.reps)
    }

    @Test
    fun `the heaviest set of the session is the one progressed from`() {
        val history = listOf(
            set(1, 90.0, 6, loggedAt = 100),
            set(2, 100.0, 5, loggedAt = 200),
            set(3, 95.0, 5, loggedAt = 300)
        )
        val rec = Progression.recommend(squat, history)!!
        assertEquals(100.0, rec.weightKg, 0.001)
        assertEquals(6, rec.reps)
    }

    @Test
    fun `sets from other lifts are ignored`() {
        val history = listOf(
            set(1, 200.0, 5, exerciseId = 99, loggedAt = 300),
            set(2, 100.0, 5, exerciseId = 1, loggedAt = 200)
        )
        val rec = Progression.recommend(squat, history)!!
        assertEquals(100.0, rec.weightKg, 0.001)
    }

    @Test
    fun `three sessions at one weight without progress reads as a stall`() {
        val history = listOf(
            set(5, 100.0, 5, sessionId = 3, loggedAt = 300),
            set(3, 100.0, 5, sessionId = 2, loggedAt = 200),
            set(1, 100.0, 5, sessionId = 1, loggedAt = 100)
        )
        val rec = Progression.recommend(squat, history)!!
        assertTrue(rec.isStalled)
        assertTrue("stall should be explained", rec.rationale.contains("Third session"))
    }

    @Test
    fun `reps creeping up across sessions is progress, not a stall`() {
        val history = listOf(
            set(5, 100.0, 7, sessionId = 3, loggedAt = 300),
            set(3, 100.0, 6, sessionId = 2, loggedAt = 200),
            set(1, 100.0, 5, sessionId = 1, loggedAt = 100)
        )
        val rec = Progression.recommend(squat, history)!!
        assertFalse(rec.isStalled)
        assertEquals(8, rec.reps)
    }

    @Test
    fun `recommended weights land on something loadable`() {
        val odd = squat.copy(incrementKg = 2.5)
        val history = listOf(set(1, 81.3, 8))
        val rec = Progression.recommend(odd, history)!!
        assertEquals(0.0, rec.weightKg * 2 % 1.0, 0.0001)
    }

    @Test
    fun `the next milestone is the round number above your estimated max`() {
        assertEquals(95.0, Progression.nextMilestone(92.4), 0.001)
        assertEquals(100.0, Progression.nextMilestone(95.0), 0.001)
        assertEquals(0.0, Progression.nextMilestone(0.0), 0.001)
    }

    @Test
    fun `milestone progress runs from one round number to the next`() {
        assertEquals(0.5f, Progression.milestoneProgress(92.5), 0.001f)
        assertEquals(0.0f, Progression.milestoneProgress(0.0), 0.001f)
        assertTrue(Progression.milestoneProgress(94.9) > 0.9f)
    }
}
