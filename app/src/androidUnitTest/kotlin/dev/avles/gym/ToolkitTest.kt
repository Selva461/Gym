package dev.avles.gym

import dev.avles.gym.data.SetRow
import dev.avles.gym.domain.Energy
import dev.avles.gym.domain.LiftHistory
import dev.avles.gym.domain.Plates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The calculators the big logging apps ship: plates, rep maxes, targets, the
 * workout card. Each is a number someone will act on without checking it, so each
 * is pinned against a figure worked out by hand.
 */
class ToolkitTest {

    private fun set(
        id: Long, session: Long, kg: Double, reps: Int, at: Long = id,
        warmup: Boolean = false, assisted: Boolean = false, lift: Long = 1, name: String = "Bench press"
    ) = SetRow(
        setId = id, sessionId = session, exerciseId = lift, exerciseName = name, setIndex = 1,
        weightKg = kg, reps = reps, isWarmup = warmup, loggedAt = at, isAssisted = assisted
    )

    // ---- Plates ---------------------------------------------------------------

    @Test
    fun `plates are largest first and exact when the gym stocks them`() {
        val l = Plates.load(87.5)
        assertEquals(listOf(25.0, 5.0, 2.5, 1.25), l.perSide)
        assertEquals(87.5, l.loadedKg, 0.0)
        assertTrue(l.isExact)
        assertEquals("25 + 5 + 2.5 + 1.25", Plates.describe(l.perSide))
    }

    @Test
    fun `repeated plates are counted, and the bar alone is said plainly`() {
        assertEquals("2 × 25 + 20", Plates.describe(Plates.load(160.0).perSide))
        assertEquals("just the bar", Plates.describe(Plates.load(20.0).perSide))
    }

    @Test
    fun `a weight the plates cannot make says how far short it falls`() {
        val l = Plates.load(61.0)
        assertEquals(60.0, l.loadedKg, 0.0)
        assertEquals(1.0, l.shortKg, 1e-9)
        assertFalse(l.isExact)
    }

    @Test
    fun `below the bar is flagged, and other bars subtract their own weight`() {
        assertTrue(Plates.load(15.0).belowBar)
        assertEquals(listOf(10.0), Plates.load(30.0, barKg = 10.0).perSide)
    }

    // ---- Rep maxes and history ------------------------------------------------

    @Test
    fun `the rep max table runs Epley backwards and rounds down to small plates`() {
        val table = LiftHistory.repMaxTable(100.0).toMap()
        assertEquals(100.0, table.getValue(1), 0.0)
        assertEquals(85.0, table.getValue(5), 0.0) // 100 / (1 + 5/30) = 85.7
        assertEquals(75.0, table.getValue(10), 0.0) // 100 / (4/3) = 75
        assertEquals(listOf(100, 90, 80, 70, 60, 50), LiftHistory.percentTable(100.0).map { it.first })
        assertTrue(LiftHistory.repMaxTable(0.0).isEmpty())
    }

    @Test
    fun `the trend has one point per session, oldest first, ignoring warm-ups`() {
        val history = listOf(
            set(5, session = 2, kg = 80.0, reps = 5, at = 500),
            set(4, session = 2, kg = 60.0, reps = 10, at = 400, warmup = true),
            set(3, session = 1, kg = 70.0, reps = 8, at = 300),
            set(2, session = 1, kg = 75.0, reps = 3, at = 200)
        )
        val trend = LiftHistory.trend(history)
        assertEquals(listOf(1L, 2L), trend.map { it.sessionId })
        assertEquals(3L, trend.first().topSet.setId) // 70 × 8 beats 75 × 3
    }

    @Test
    fun `records name the best, the heaviest and the most reps`() {
        val r = LiftHistory.records(
            listOf(
                set(1, 1, 100.0, 1), set(2, 1, 90.0, 6), set(3, 2, 60.0, 15),
                set(4, 2, 110.0, 1, warmup = true)
            )
        )
        assertEquals(2L, r.bestSet!!.setId) // 90 × 6 = 108 e1RM
        assertEquals(1L, r.heaviest!!.setId) // the warm-up does not count
        assertEquals(3L, r.mostReps!!.setId)
        assertEquals(2, r.sessions)
        assertEquals(3, r.workingSets)
        assertEquals(900.0, r.bestSessionVolume, 0.0) // 60 × 15 beats 100 + 90 × 6
        assertNull(LiftHistory.records(emptyList()).bestSet)
    }

    @Test
    fun `the workout card counts a record only against earlier sessions`() {
        val before = listOf(set(1, 1, 80.0, 5))
        val tonight = listOf(
            set(10, 2, 60.0, 10, warmup = true),
            set(11, 2, 82.5, 5),
            set(12, 2, 85.0, 5),
            set(13, 2, 40.0, 10, lift = 2, name = "Curl")
        )
        val card = LiftHistory.report(0, 52 * 60_000L, tonight, before + tonight)
        assertEquals(52, card.minutes)
        assertEquals(3, card.workingSets)
        assertEquals(82.5 * 5 + 85.0 * 5 + 400.0, card.volumeKg, 1e-9)
        // One record for bench (the best of the two), none for a curl never done before.
        assertEquals(listOf(12L), card.records.map { it.setId })
        assertEquals(listOf("Bench press", "Curl"), card.lifts)
    }

    @Test
    fun `less help is the record on an assisted machine`() {
        val before = listOf(set(1, 1, 40.0, 8, assisted = true, name = "Assisted pull-up"))
        val tonight = listOf(set(2, 2, 35.0, 8, assisted = true, name = "Assisted pull-up"))
        assertEquals(listOf(2L), LiftHistory.report(0, 0, tonight, before + tonight).records.map { it.setId })
    }

    // ---- Energy ---------------------------------------------------------------

    @Test
    fun `Mifflin-St Jeor matches the published equation`() {
        // 10 × 75 + 6.25 × 175 − 5 × 28 + 5 = 1708.75
        assertEquals(1708.75, Energy.restingKcal(75.0, 175.0, 28, Energy.Sex.MALE), 1e-9)
        assertEquals(1542.75, Energy.restingKcal(75.0, 175.0, 28, Energy.Sex.FEMALE), 1e-9)
        assertEquals(0.0, Energy.restingKcal(0.0, 175.0, 28, Energy.Sex.MALE), 0.0)
    }

    @Test
    fun `targets scale with goal and never cut below resting energy`() {
        val t = Energy.targets(75.0, 175.0, 28, Energy.Sex.MALE, Energy.Activity.MODERATE, Energy.Goal.LOSE)!!
        // maintenance 1708.75 × 1.55 = 2648.6 -> 2650; cut × 0.8 = 2118.9 -> 2100
        assertEquals(2650.0, t.maintenanceKcal, 0.0)
        assertEquals(2100.0, t.kcal, 0.0)
        assertEquals(150.0, t.proteinG, 0.0)
        val desk = Energy.targets(50.0, 150.0, 60, Energy.Sex.FEMALE, Energy.Activity.DESK, Energy.Goal.LOSE)!!
        assertTrue(desk.kcal >= Energy.restingKcal(50.0, 150.0, 60, Energy.Sex.FEMALE) - 25)
        assertNull(Energy.targets(0.0, 175.0, 28, Energy.Sex.MALE, Energy.Activity.DESK, Energy.Goal.GAIN))
    }

    @Test
    fun `the split gives fat a quarter and carbohydrate the rest`() {
        val s = Energy.split(2100.0, 150.0)
        assertEquals(58.0, s.fatG, 0.0) // 525 / 9 = 58.3
        assertEquals(245.0, s.carbsG, 0.0) // (2100 − 600 − 522) / 4 = 244.5
    }

    @Test
    fun `water is 35 ml a kilo in glasses, at least eight`() {
        assertEquals(11, Energy.waterGlasses(80.0)) // 2800 ml
        assertEquals(8, Energy.waterGlasses(50.0))
        assertEquals(8, Energy.waterGlasses(0.0))
    }
}
