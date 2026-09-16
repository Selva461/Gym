package dev.avles.gym

import dev.avles.gym.data.Exercise
import dev.avles.gym.data.SetRow
import dev.avles.gym.domain.Progression
import dev.avles.gym.domain.Training
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A variation is an attribute of the set, not a new exercise — you pick "Barbell
 * bench press" and then a grip, and the catalogue stays 394 rows instead of
 * exploding into every combination of lift and grip.
 *
 * What that costs is these rules, and they are the whole reason the column exists.
 * Progression and records key on the lift *and* the variation; volume does not.
 * Get that wrong in either direction and the app either loses your records or
 * recommends a weight off the wrong grip.
 */
class VariationTest {

    private val bench = Exercise(
        id = 1, name = "Barbell bench press", muscleGroup = "Chest",
        repLow = 5, repHigh = 8, incrementKg = 2.5
    )

    private fun set(
        id: Long,
        weight: Double,
        reps: Int,
        variation: String = "",
        sessionId: Long = id,
        warmup: Boolean = false
    ) = SetRow(
        setId = id, sessionId = sessionId, exerciseId = bench.id,
        exerciseName = bench.name, setIndex = 1, weightKg = weight,
        reps = reps, isWarmup = warmup, loggedAt = id, variation = variation
    )

    /**
     * The failure this whole design exists to prevent. Wide grip is the easier
     * bench for most people; if the two share a history, hitting the top of the
     * range on wide grip tells you to add weight to close grip — a lift you may
     * never have done at that load.
     */
    @Test
    fun `reps on one grip do not raise the target on another`() {
        val history = listOf(
            set(1, 80.0, 8, "Wide"),    // top of the range — would step up
            set(2, 60.0, 5, "Close")    // bottom of the range on the harder grip
        )
        val close = Progression.recommend(bench, history, "Close")!!
        assertEquals("close grip took wide grip's weight", 60.0, close.weightKg, 0.001)
        assertFalse("close grip stepped up off wide grip's reps", close.isStepUp)

        val wide = Progression.recommend(bench, history, "Wide")!!
        assertTrue("wide grip earned its step up and did not get it", wide.isStepUp)
        assertEquals(82.5, wide.weightKg, 0.001)
    }

    /**
     * Unspecified is a bucket, not a wildcard. Every set logged before this column
     * existed is unspecified, and matching it against everything would reintroduce
     * exactly the mixing the previous test forbids.
     */
    @Test
    fun `unspecified sets are their own bucket, not a match for everything`() {
        val history = listOf(set(1, 100.0, 8), set(2, 50.0, 5, "Close"))
        val unspecified = Progression.recommend(bench, history, "")!!
        // 8 reps is the top of this lift's range, so the target is the step up.
        assertTrue(unspecified.isStepUp)
        assertEquals(102.5, unspecified.weightKg, 0.001)
        assertFalse("unspecified history was treated as real history for a grip",
            unspecified.isFromOtherVariations)

        val close = Progression.recommend(bench, history, "Close")!!
        assertEquals("the 100 kg unspecified set leaked into close grip", 50.0, close.weightKg, 0.001)
    }

    /**
     * A strict reading gives nothing at all on the first session of a new grip,
     * which is a blank screen where a starting point would do. So it borrows from
     * the lift's other work — and says so, both in the flag and in words, because a
     * number whose provenance you cannot see is a number you should not trust.
     */
    @Test
    fun `the first session on a new variation borrows a starting point and admits it`() {
        val history = listOf(set(1, 80.0, 6, "Wide"))
        val close = Progression.recommend(bench, history, "Close")!!
        assertTrue("did not flag the borrowed target", close.isFromOtherVariations)
        assertTrue(
            "the rationale does not say where this came from: ${close.rationale}",
            close.rationale.contains("nothing logged on this variation")
        )
        assertEquals(80.0, close.weightKg, 0.001)
    }

    @Test
    fun `a variation with its own history never borrows`() {
        val history = listOf(set(1, 80.0, 6, "Wide"), set(2, 60.0, 6, "Close"))
        val close = Progression.recommend(bench, history, "Close")!!
        assertFalse(close.isFromOtherVariations)
        assertEquals(60.0, close.weightKg, 0.001)
        assertFalse(
            "a rationale that did not borrow should not mention borrowing",
            close.rationale.contains("nothing logged on this variation")
        )
    }

    /** A lift with no history at all still refuses to guess, variation or not. */
    @Test
    fun `borrowing cannot invent a starting weight out of nothing`() {
        assertNull(Progression.recommend(bench, emptyList(), "Close"))
        assertNull(Progression.recommend(bench, listOf(set(1, 60.0, 5, warmup = true)), "Close"))
    }

    /**
     * Records split the same way. Letting the easier grip own the lift means the
     * marker stops appearing on the harder one however well it goes, which makes it
     * worthless exactly when it would mean the most.
     */
    @Test
    fun `a record on one grip does not erase a record on another`() {
        val wideBest = set(1, 100.0, 8, "Wide")
        val closePr = set(2, 70.0, 8, "Close")
        assertTrue(
            "a genuine close-grip best was hidden by a heavier wide-grip set",
            Training.isPersonalRecord(closePr, listOf(wideBest))
        )
        assertFalse(
            "a lighter close-grip set was called a record over a heavier one",
            Training.isPersonalRecord(set(3, 60.0, 5, "Close"), listOf(wideBest, closePr))
        )
    }

    @Test
    fun `an unspecified set is not ranked against a grip it may not have used`() {
        val tagged = set(1, 120.0, 8, "Close")
        assertTrue(Training.isPersonalRecord(set(2, 100.0, 8), listOf(tagged)))
    }

    /**
     * Volume deliberately does *not* split. It is a fatigue number, and fatigue does
     * not care how you held the bar — splitting it would make a session look half as
     * hard as it was the moment you varied your grip.
     */
    @Test
    fun `volume sums across every variation`() {
        val sets = listOf(
            set(1, 100.0, 5, "Wide"),
            set(2, 80.0, 5, "Close"),
            set(3, 60.0, 5)
        )
        assertEquals(500.0 + 400.0 + 300.0, Training.volume(sets), 0.001)
    }

    @Test
    fun `a label is offered only when there is something to say`() {
        assertNull(set(1, 60.0, 5).variationLabel)
        assertNull(set(2, 60.0, 5, "   ").variationLabel)
        assertEquals("Close", set(3, 60.0, 5, "Close").variationLabel)
    }
}
