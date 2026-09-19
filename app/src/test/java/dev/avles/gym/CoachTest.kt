package dev.avles.gym

import dev.avles.gym.data.Exercise
import dev.avles.gym.data.SeedData
import dev.avles.gym.data.SetRow
import dev.avles.gym.domain.Coach
import dev.avles.gym.domain.ExerciseGuide
import dev.avles.gym.domain.Muscle
import dev.avles.gym.domain.Physiques
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The coach reads your own log back to you. These tests are mostly about what it
 * must *not* do: invent volume you did not perform, count a warm-up as stimulus,
 * or suggest a lift that does not fix the thing it claims to fix.
 */
class CoachTest {

    /**
     * Ids are assigned by Room at insert, so everything SeedData hands back has
     * id 0. Keying anything on that collapses all 394 lifts into one entry — which
     * is exactly what happened the first time this file was written, and produced a
     * confusing empty result rather than an error. The fixture numbers them the way
     * the database would.
     */
    private val catalogue = SeedData.exercises().mapIndexed { i, e -> e.copy(id = (i + 1).toLong()) }
    private fun lift(name: String) = catalogue.first { it.name == name }
    private val guides = catalogue.associate { it.id to ExerciseGuide.of(it) }

    private fun set(id: Long, exercise: Exercise, warmup: Boolean = false, sessionId: Long = 1) =
        SetRow(
            setId = id, sessionId = sessionId, exerciseId = exercise.id,
            exerciseName = exercise.name, setIndex = 1, weightKg = 20.0,
            reps = 10, isWarmup = warmup, loggedAt = id, variation = ""
        )

    private fun weekly(history: List<SetRow>, day: Long = 100L, sessionDay: Long = 100L) =
        Coach.weeklySets(
            history = history,
            sessionDayOf = { sessionDay },
            guideFor = { guides[it] },
            today = day
        )

    @Test
    fun `a set counts fully for what the lift is for and half for what it also works`() {
        val row = lift("Barbell bench press")
        val guide = guides[row.id]!!
        val counts = weekly(listOf(set(1, row)))
        guide.primary.forEach { assertEquals(1.0, counts[it]!!, 0.001) }
        guide.secondary.forEach { assertEquals(0.5, counts[it]!!, 0.001) }
    }

    @Test
    fun `warm-ups are not stimulus and are not counted`() {
        val row = lift("Barbell bench press")
        assertTrue(weekly(listOf(set(1, row, warmup = true))).isEmpty())
    }

    /** Anything outside the window is last week's training, not this week's. */
    @Test
    fun `sets older than the window do not count`() {
        val row = lift("Barbell bench press")
        assertTrue(weekly(listOf(set(1, row)), day = 100L, sessionDay = 80L).isEmpty())
        assertFalse(weekly(listOf(set(1, row)), day = 100L, sessionDay = 100L).isEmpty())
        // The window is inclusive of both ends: seven days means today and six before.
        assertFalse(weekly(listOf(set(1, row)), day = 100L, sessionDay = 94L).isEmpty())
        assertTrue(weekly(listOf(set(1, row)), day = 100L, sessionDay = 93L).isEmpty())
    }

    /**
     * The question the whole feature exists to answer: a week of pressing does not
     * serve a goal built on shoulder width, and the app should say so rather than
     * congratulating the effort.
     */
    @Test
    fun `a week of only pressing reports the width muscles as short`() {
        val press = lift("Barbell bench press")
        val history = (1L..12L).map { set(it, press) }
        val review = Coach.review(Physiques.V_TAPER, weekly(history))

        val sideDelt = review.first { it.muscle == Muscle.DELT_SIDE }
        assertFalse("side delts should not read as covered by bench press", sideDelt.onTarget)
        val lats = review.first { it.muscle == Muscle.LATS }
        assertFalse("lats should not read as covered by bench press", lats.onTarget)

        // And the worst gaps sort to the top, because that is the part people read.
        assertTrue(review.first().ratio <= review.last().ratio)
    }

    @Test
    fun `nothing logged is reported as nothing to read, not as failure`() {
        val review = Coach.review(Physiques.V_TAPER, emptyMap())
        assertTrue(Coach.verdict(review), Coach.verdict(review).contains("nothing to read"))
    }

    /**
     * A suggestion has to fix the thing it claims to fix. Rows work biceps a little;
     * suggesting rows for an arm shortfall is how people end up with arms that never
     * grow, so only the muscles a lift is *for* qualify.
     */
    @Test
    fun `every suggestion primarily works the muscle it is offered for`() {
        val press = lift("Barbell bench press")
        val review = Coach.review(Physiques.V_TAPER, weekly((1L..12L).map { set(it, press) }))
        val suggestions = Coach.suggestions(review, catalogue, { guides[it.id]!! })

        assertTrue("no suggestions produced", suggestions.isNotEmpty())
        suggestions.forEach {
            assertTrue(
                "${it.exercise.name} is offered for ${it.forMuscle.label} but does not primarily work it",
                it.forMuscle in guides[it.exercise.id]!!.primary
            )
            assertTrue("a suggestion with no stated reason", it.reason.isNotBlank())
        }
    }

    @Test
    fun `a lift already trained this week is not suggested back`() {
        val press = lift("Barbell bench press")
        val review = Coach.review(Physiques.V_TAPER, weekly((1L..12L).map { set(it, press) }))
        val suggestions = Coach.suggestions(
            review, catalogue, { guides[it.id]!! }, trainedThisWeek = setOf(press.id)
        )
        assertTrue(suggestions.none { it.exercise.id == press.id })
    }

    /**
     * Every goal has to be reachable from the catalogue, or it is a target the app
     * offers and cannot help you hit.
     */
    @Test
    fun `every muscle every goal asks for has lifts that train it`() {
        val primaryIndex = catalogue.flatMap { ex -> ExerciseGuide.of(ex).primary.map { it to ex } }
            .groupBy({ it.first }, { it.second })
        Physiques.ALL.forEach { physique ->
            physique.weeklySets.keys.forEach { muscle ->
                assertTrue(
                    "${physique.label} asks for ${muscle.label} but no lift primarily works it",
                    (primaryIndex[muscle]?.size ?: 0) >= 1
                )
            }
        }
    }

    /**
     * The least obvious claim in Physiques, pinned so it cannot be "tidied" away:
     * heavy upper-trap work fills the neck-to-shoulder line and makes the same
     * frame read narrower, so the width goal holds it below the side delts.
     */
    @Test
    fun `the width goal trains side delts far harder than upper traps`() {
        val v = Physiques.V_TAPER
        assertTrue(v.targetFor(Muscle.DELT_SIDE) >= v.targetFor(Muscle.TRAPS_UPPER) * 3)
        assertTrue("legs are not skipped", v.targetFor(Muscle.QUADS) >= 8)
    }

    /** The honest caveat has to survive, because the numbers cannot say it. */
    @Test
    fun `the six-pack goal says out loud that abs are a body fat story`() {
        val notes = Physiques.V_TAPER.notes.joinToString(" ").lowercase()
        assertTrue("the body-fat caveat is missing", "body fat" in notes)
        assertTrue("it does not point at the food log", "fuel" in notes)
    }
}
