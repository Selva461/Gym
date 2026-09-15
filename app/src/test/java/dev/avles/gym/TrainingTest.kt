package dev.avles.gym

import dev.avles.gym.data.SetRow
import dev.avles.gym.domain.Training
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class TrainingTest {

    private fun set(
        id: Long,
        weight: Double,
        reps: Int,
        warmup: Boolean = false,
        exerciseId: Long = 1
    ) = SetRow(
        setId = id, sessionId = 1, exerciseId = exerciseId, exerciseName = "Back squat",
        setIndex = 1, weightKg = weight, reps = reps, isWarmup = warmup, loggedAt = id
    )

    @Test
    fun `volume ignores warm-up sets`() {
        val sets = listOf(
            set(1, 40.0, 10, warmup = true),
            set(2, 80.0, 5),
            set(3, 80.0, 5)
        )
        assertEquals(800.0, Training.volume(sets), 0.001)
    }

    @Test
    fun `one rep max of a single is the weight itself`() {
        assertEquals(100.0, Training.estimatedOneRepMax(100.0, 1), 0.001)
    }

    @Test
    fun `one rep max grows with reps`() {
        val five = Training.estimatedOneRepMax(100.0, 5)
        val eight = Training.estimatedOneRepMax(100.0, 8)
        assertTrue(eight > five)
        assertEquals(116.666, five, 0.01)
    }

    @Test
    fun `nonsense input returns zero rather than throwing`() {
        assertEquals(0.0, Training.estimatedOneRepMax(0.0, 5), 0.001)
        assertEquals(0.0, Training.estimatedOneRepMax(100.0, 0), 0.001)
        assertEquals(0.0, Training.estimatedOneRepMax(100.0, -3), 0.001)
    }

    @Test
    fun `a heavier estimated max is a record`() {
        val history = listOf(set(1, 80.0, 5), set(2, 85.0, 3))
        assertTrue(Training.isPersonalRecord(set(3, 90.0, 3), history))
    }

    @Test
    fun `matching a previous best is not a record`() {
        val history = listOf(set(1, 100.0, 5))
        assertFalse(Training.isPersonalRecord(set(2, 100.0, 5), history))
    }

    @Test
    fun `a warm-up can never be a record`() {
        assertFalse(Training.isPersonalRecord(set(2, 200.0, 5, warmup = true), listOf(set(1, 80.0, 5))))
    }

    @Test
    fun `first ever working set on a lift is a record`() {
        assertTrue(Training.isPersonalRecord(set(1, 60.0, 8), emptyList()))
    }

    @Test
    fun `records are judged per exercise`() {
        val history = listOf(set(1, 200.0, 5, exerciseId = 2))
        assertTrue(Training.isPersonalRecord(set(2, 60.0, 5, exerciseId = 1), history))
    }

    @Test
    fun `set numbering counts only the same lift`() {
        val sessionSets = listOf(set(1, 80.0, 5), set(2, 80.0, 5), set(3, 40.0, 10, exerciseId = 9))
        assertEquals(3, Training.nextSetIndex(sessionSets, exerciseId = 1))
        assertEquals(2, Training.nextSetIndex(sessionSets, exerciseId = 9))
    }

    @Test
    fun `weights drop a pointless decimal`() {
        assertEquals("60", Training.formatWeight(60.0))
        assertEquals("62.5", Training.formatWeight(62.5))
    }

    /**
     * The weight dialog seeds its field from formatWeight and parses the result back
     * through a filter that only keeps digits and a dot. A locale that writes 62.5 as
     * "62,5" therefore used to round-trip it into 625 kg, so the separator is pinned.
     */
    @Test
    fun `weights use a dot whatever the phone's locale is`() {
        val original = Locale.getDefault()
        try {
            listOf(Locale.US, Locale.GERMANY, Locale.FRANCE, Locale.forLanguageTag("en-IN"))
                .forEach { locale ->
                    Locale.setDefault(locale)
                    assertEquals("broken under $locale", "62.5", Training.formatWeight(62.5))
                }
        } finally {
            Locale.setDefault(original)
        }
    }
}
