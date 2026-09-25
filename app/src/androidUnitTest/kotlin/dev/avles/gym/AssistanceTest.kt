package dev.avles.gym

import dev.avles.gym.data.Exercise
import dev.avles.gym.data.SeedData
import dev.avles.gym.data.SetRow
import dev.avles.gym.domain.Assistance
import dev.avles.gym.domain.Progression
import dev.avles.gym.domain.Training
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Assisted machines, where the weight pinned is help and more of it is easier.
 * Every rule that reads weight has to point the other way for them.
 */
class AssistanceTest {

    private val assistedPullUp = Exercise(
        id = 7, name = "Assisted pull-up machine", muscleGroup = "Back",
        repLow = 8, repHigh = 12, incrementKg = 5.0, isAssisted = true
    )

    private fun set(
        id: Long,
        help: Double,
        reps: Int,
        sessionId: Long = 1,
        warmup: Boolean = false,
        assisted: Boolean = true
    ) = SetRow(
        setId = id, sessionId = sessionId, exerciseId = 7,
        exerciseName = "Assisted pull-up machine", setIndex = 1, weightKg = help,
        reps = reps, isWarmup = warmup, loggedAt = id, isAssisted = assisted
    )

    @Test
    fun `the seeded machines are flagged, and nothing else is`() {
        val flagged = SeedData.exercises().filter { it.isAssisted }.map { it.name }.toSet()
        assertEquals(setOf("Assisted pull-up machine", "Assisted dip machine"), flagged)
    }

    @Test
    fun `a lift is recognised by its name whatever the case`() {
        assertTrue(Assistance.looksAssisted("Assisted dip machine"))
        assertTrue(Assistance.looksAssisted("Machine-ASSISTED chin-up"))
        assertFalse(Assistance.looksAssisted("Lat pulldown"))
    }

    @Test
    fun `at the top of the range the help comes down, not up`() {
        val rec = Progression.recommend(assistedPullUp, listOf(set(1, 40.0, 12)))!!
        assertEquals(35.0, rec.weightKg, 0.0)
        assertEquals(8, rec.reps)
        assertTrue(rec.isStepUp)
        assertTrue(rec.headline, rec.headline.contains("help"))
        assertTrue(rec.rationale, rec.rationale.contains("more weight is easier"))
    }

    @Test
    fun `below the top the help stays and one more rep is the target`() {
        val rec = Progression.recommend(assistedPullUp, listOf(set(1, 40.0, 9)))!!
        assertEquals(40.0, rec.weightKg, 0.0)
        assertEquals(10, rec.reps)
        assertFalse(rec.isStepUp)
    }

    @Test
    fun `the hardest set of a session is the one with least help`() {
        // 45 kg of help for 12 and 40 kg for 9 in one session: 40 is the working
        // weight, and 9 reps at it is short of the range.
        val session = listOf(set(1, 45.0, 12), set(2, 40.0, 9))
        val rec = Progression.recommend(assistedPullUp, session)!!
        assertEquals(40.0, rec.weightKg, 0.0)
        assertEquals(10, rec.reps)
    }

    @Test
    fun `help never goes below zero, and zero is the lift itself`() {
        val rec = Progression.recommend(assistedPullUp, listOf(set(1, 5.0, 12)))!!
        assertEquals(0.0, rec.weightKg, 0.0)
        assertTrue(rec.rationale, rec.rationale.contains("no help at all"))
        val done = Progression.recommend(assistedPullUp, listOf(set(2, 0.0, 12)))!!
        assertEquals(0.0, done.weightKg, 0.0)
        assertTrue(done.rationale, done.rationale.contains("unassisted"))
    }

    @Test
    fun `a stall suggests more help, not a lighter plate`() {
        val history = listOf(
            set(1, 40.0, 9, sessionId = 1), set(2, 40.0, 9, sessionId = 2), set(3, 40.0, 9, sessionId = 3)
        )
        val rec = Progression.recommend(assistedPullUp, history)!!
        assertTrue(rec.isStalled)
        assertTrue(rec.rationale, rec.rationale.contains("45 kg of help"))
    }

    @Test
    fun `a record is less help for as many reps`() {
        val earlier = listOf(set(1, 30.0, 10))
        assertTrue("less help, fewer reps", Assistance.isRecord(set(2, 25.0, 8), earlier))
        assertTrue("same help, more reps", Assistance.isRecord(set(3, 30.0, 11), earlier))
        assertFalse("more help, same reps", Assistance.isRecord(set(4, 35.0, 10), earlier))
        assertFalse("same set again", Assistance.isRecord(set(5, 30.0, 10), earlier))
        // Routed through the general record check, so the marker on the Train screen
        // uses it without knowing which kind of lift it is.
        assertFalse(Training.isPersonalRecord(set(6, 40.0, 10), earlier))
        assertTrue(Training.isPersonalRecord(set(7, 20.0, 10), earlier))
    }

    @Test
    fun `help is never counted as volume, and has no estimated max`() {
        val sets = listOf(set(1, 40.0, 10), set(2, 60.0, 5, assisted = false))
        assertEquals(300.0, Training.volume(sets), 0.0)
        assertEquals(0.0, Training.bestEstimatedOneRepMax(listOf(set(3, 40.0, 10))), 0.0)
    }

    @Test
    fun `milestones count down to the unassisted lift`() {
        assertEquals(25.0, Assistance.nextMilestone(27.5), 0.0)
        assertEquals(20.0, Assistance.nextMilestone(25.0), 0.0)
        assertEquals(0.0, Assistance.nextMilestone(3.0), 0.0)
        assertEquals(0.5f, Assistance.milestoneProgress(27.5), 0.001f)
        assertEquals(1f, Assistance.milestoneProgress(0.0), 0.0f)
        assertEquals(25.0, Assistance.leastHelp(listOf(set(1, 30.0, 8), set(2, 25.0, 6)))!!, 0.0)
    }

    @Test
    fun `labels say help, so a number is never read the wrong way round`() {
        assertEquals("30 help × 10", Training.formatSet(set(1, 30.0, 10)))
        assertEquals("60 × 5", Training.formatSet(set(2, 60.0, 5, assisted = false)))
    }
}
