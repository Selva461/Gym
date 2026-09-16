package dev.avles.gym

import dev.avles.gym.data.Exercise
import dev.avles.gym.data.SeedData
import dev.avles.gym.domain.Equipment
import dev.avles.gym.domain.ExerciseGuide
import dev.avles.gym.domain.Pattern
import dev.avles.gym.domain.Muscle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The guide is derived rather than stored, which is what makes it cover all 394
 * lifts and every lift added later — but it also means a careless keyword can
 * silently mis-describe a hundred exercises at once. These tests are where that
 * gets caught. Most of them exist because an earlier version got exactly the case
 * they name wrong.
 */
class ExerciseGuideTest {

    private val exercises = SeedData.exercises()
    private fun guideFor(name: String) =
        ExerciseGuide.of(exercises.first { it.name == name })

    @Test
    fun `every seeded lift resolves to a complete guide`() {
        exercises.forEach { e ->
            val g = ExerciseGuide.of(e)
            assertTrue("${e.name} has no primary muscle", g.primary.isNotEmpty())
            assertTrue("${e.name} has no setup", g.setup.isNotEmpty())
            assertTrue("${e.name} has no execution", g.execution.isNotEmpty())
            assertTrue("${e.name} has no cues", g.cues.isNotEmpty())
            assertTrue("${e.name} has no mistakes listed", g.mistakes.isNotEmpty())
        }
    }

    /**
     * A muscle cannot be both the point of the lift and a bystander in it. The
     * secondary lists deliberately name the muscle each movement is usually filed
     * under, so that a lift filed somewhere else still shows it; this is the filter
     * that stops that becoming a duplicate.
     */
    @Test
    fun `no muscle is both primary and secondary`() {
        exercises.forEach { e ->
            val g = ExerciseGuide.of(e)
            g.secondary.forEach { m ->
                assertFalse("${e.name}: ${m.label} is listed twice", m in g.primary)
            }
        }
    }

    /**
     * Pattern is what every word of the reference is keyed on, so a lift falling
     * through to OTHER means it is being described in generalities. None should.
     */
    @Test
    fun `no seeded lift falls through to a generic movement pattern`() {
        val vague = exercises.filter { ExerciseGuide.of(it).pattern == Pattern.OTHER }
        assertTrue("described only in generalities: ${vague.map { it.name }}", vague.isEmpty())
    }

    /**
     * Equipment is allowed to be unknown — a wrist roller is a wrist roller and
     * saying "barbell" would be a lie — but it should be rare. If this number
     * climbs, a batch of lifts has stopped being recognised.
     */
    @Test
    fun `almost every lift resolves its equipment`() {
        val unknown = exercises.filter { ExerciseGuide.of(it).equipment == Equipment.OTHER }
        assertTrue("unresolved equipment on ${unknown.size}: ${unknown.map { it.name }}", unknown.size <= 4)
    }

    /**
     * The catalogue puts equipment in the name precisely so this works. Each of
     * these once resolved to something else.
     */
    @Test
    fun `equipment is read off the name, most specific word winning`() {
        assertEquals(Equipment.CABLE, guideFor("Cable rope overhead triceps extension").equipment)
        // No "cable" in the name at all — "pushdown" is the cable word.
        assertEquals(Equipment.CABLE, guideFor("Triceps pushdown").equipment)
        // Contains "dip", but the machine is the thing you are sitting in.
        assertEquals(Equipment.MACHINE, guideFor("Assisted dip machine").equipment)
        assertEquals(Equipment.EZ_BAR, guideFor("EZ-bar skull crusher").equipment)
        assertEquals(Equipment.BODYWEIGHT, guideFor("Diamond push-up").equipment)
        // Names nobody qualifies, because the default is understood.
        assertEquals(Equipment.BARBELL, guideFor("Skull crusher").equipment)
        assertEquals(Equipment.DUMBBELL, guideFor("Hammer curl").equipment)
    }

    /**
     * Close-grip bench, dips and diamond push-ups are filed under Triceps but they
     * are presses. Keying the pattern on the muscle group instead of the movement
     * told people that only their forearm should move during a dip.
     */
    @Test
    fun `a press filed under triceps is still described as a press`() {
        listOf("Close-grip bench press", "Triceps dip", "Diamond push-up").forEach {
            assertEquals(it, Pattern.HORIZONTAL_PRESS, guideFor(it).pattern)
        }
        assertEquals(Pattern.TRICEPS_EXTENSION, guideFor("Triceps pushdown").pattern)
    }

    /** And its muscle map still has to show the chest, which it works. */
    @Test
    fun `a press filed under triceps still shows the chest on the map`() {
        val g = guideFor("Close-grip bench press")
        assertTrue("triceps missing", Muscle.TRICEPS in g.primary)
        assertTrue("chest missing from a bench press", g.secondary.any { it.label.contains("chest", true) })
    }

    /**
     * A triceps kickback and a glute kickback share one word and nothing else.
     * A rack pull is a shortened deadlift, not a row.
     */
    @Test
    fun `lifts that share a word are told apart`() {
        assertEquals(Pattern.TRICEPS_EXTENSION, guideFor("Dumbbell triceps kickback").pattern)
        assertEquals(Pattern.HINGE, guideFor("Rack pull").pattern)
    }

    /** Hip adduction works the adductors. Filing it under Glutes is not a claim about the muscle. */
    @Test
    fun `the adduction machine does not claim to be a glute exercise`() {
        assertEquals(listOf(Muscle.ADDUCTORS), guideFor("Hip adduction").primary)
        assertTrue(Muscle.ABDUCTORS in guideFor("Hip abduction").primary)
    }

    /** An incline press works the upper chest. The word is in the name; use it. */
    @Test
    fun `bench angle in the name changes which part of the chest is shaded`() {
        assertEquals(Muscle.CHEST_UPPER, guideFor("Incline barbell bench press").primary.first())
    }

    /**
     * The whole reason the guide is derived: a lift invented on the phone tomorrow
     * has a reference the moment it is named, with no data entry and no migration.
     */
    @Test
    fun `a lift the user invents gets a reference too`() {
        val invented = Exercise(
            id = 9001, name = "Cable Y-raise", muscleGroup = "Shoulders",
            isCustom = true, repLow = 12, repHigh = 20, incrementKg = 1.0
        )
        val g = ExerciseGuide.of(invented)
        assertEquals(Equipment.CABLE, g.equipment)
        assertEquals(Pattern.LATERAL_RAISE, g.pattern)
        assertTrue(g.setup.isNotEmpty() && g.execution.isNotEmpty() && g.mistakes.isNotEmpty())
        assertTrue("shoulders not identified", g.primary.any { it.label.contains("delt", true) })
    }

    /**
     * Variation axes are offered where the choice is worth its own history and
     * nowhere else. A calf raise does not get a grip chip to look complete, and a
     * pulley height on a dumbbell would be meaningless.
     */
    @Test
    fun `variation axes are offered only where they mean something`() {
        val bench = guideFor("Barbell bench press").variations.map { it.name }
        assertTrue("a bench press should offer grip width", "Grip width" in bench)

        val calf = guideFor("Standing calf raise").variations.map { it.name }
        assertFalse("a calf raise has no grip worth logging", "Grip width" in calf)

        exercises.forEach { e ->
            val g = ExerciseGuide.of(e)
            if (g.equipment != Equipment.CABLE) {
                assertFalse(
                    "${e.name} offers a pulley height and is not on a cable",
                    g.variations.any { it.name == "Pulley height" }
                )
            }
            g.variations.forEach { axis ->
                assertTrue("${e.name}: ${axis.name} has under two options", axis.options.size >= 2)
                assertEquals(
                    "${e.name}: ${axis.name} repeats an option",
                    axis.options.size, axis.options.toSet().size
                )
            }
            assertEquals(
                "${e.name} offers the same axis twice",
                g.variations.size, g.variations.map { it.name }.toSet().size
            )
        }
    }

    @Test
    fun `the one-line summary names the equipment and what it works`() {
        val g = guideFor("Barbell bench press")
        assertTrue(g.summary, g.summary.startsWith("Barbell"))
        assertTrue(g.summary, g.summary.contains("chest"))
    }
}
