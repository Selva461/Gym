package dev.avles.gym

import dev.avles.gym.data.FoodLogRow
import dev.avles.gym.data.Micros
import dev.avles.gym.domain.Direction
import dev.avles.gym.domain.MacroMath
import dev.avles.gym.domain.Nutrients
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NutrientsTest {

    private fun row(
        qty: Double,
        micros: Micros,
        supplement: Boolean = false,
        kcal: Double = 0.0,
        protein: Double = 0.0
    ) = FoodLogRow(
        logId = 0, dateEpochDay = 0, meal = "Lunch", quantity = qty, loggedAt = 0,
        foodId = 0, name = "x", unitLabel = "1 unit",
        kcal = kcal, proteinG = protein, carbsG = 0.0, fatG = 0.0,
        isSupplement = supplement, micros = micros
    )

    @Test
    fun `micronutrients scale with quantity like macros do`() {
        val t = MacroMath.totalsOf(listOf(row(2.0, Micros(ironMg = 3.0, omega3G = 0.5))))
        assertEquals(6.0, t.micros.ironMg, 0.001)
        assertEquals(1.0, t.micros.omega3G, 0.001)
    }

    @Test
    fun `micronutrients add up across entries`() {
        val t = MacroMath.totalsOf(
            listOf(
                row(1.0, Micros(magnesiumMg = 200.0, calciumMg = 100.0)),
                row(1.0, Micros(magnesiumMg = 50.0, calciumMg = 240.0))
            )
        )
        assertEquals(250.0, t.micros.magnesiumMg, 0.001)
        assertEquals(340.0, t.micros.calciumMg, 0.001)
    }

    @Test
    fun `an empty day is zero across every nutrient, not a crash`() {
        val t = MacroMath.totalsOf(emptyList())
        Nutrients.ALL.forEach { assertEquals(0.0, it.read(t.micros), 0.001) }
    }

    @Test
    fun `supplements can be totalled apart from food`() {
        val rows = listOf(
            row(1.0, Micros(magnesiumMg = 200.0), supplement = true),
            row(1.0, Micros(magnesiumMg = 60.0))
        )
        assertEquals(200.0, MacroMath.totalsFromSupplements(rows).micros.magnesiumMg, 0.001)
        assertEquals(60.0, MacroMath.totalsFromFood(rows).micros.magnesiumMg, 0.001)
        assertEquals(260.0, MacroMath.totalsOf(rows).micros.magnesiumMg, 0.001)
    }

    @Test
    fun `a ceiling that is passed is flagged and a floor that is not is never flagged`() {
        val sodium = Nutrients.ALL.first { it.label == "Sodium" }
        val iron = Nutrients.ALL.first { it.label == "Iron" }
        assertTrue(Nutrients.isOver(sodium, Micros(sodiumMg = 3000.0)))
        assertFalse(Nutrients.isOver(sodium, Micros(sodiumMg = 1000.0)))
        // Being short of iron is a shortfall, never an error.
        assertFalse(Nutrients.isOver(iron, Micros(ironMg = 0.0)))
    }

    @Test
    fun `shortfalls name the floors furthest from being met, worst first`() {
        // Every floor is filled to a comfortable 80% except the two under test, so the
        // ordering is unambiguous. A nutrient left at zero would legitimately outrank
        // both of them — being at none of something is as short as it gets.
        val micros = Micros(
            fiberG = 24.0, potassiumMg = 2800.0, ironMg = 6.4, zincMg = 8.8,
            vitCMg = 72.0, vitDMcg = 12.0, vitB12Mcg = 1.92, vitAMcg = 720.0,
            folateMcg = 320.0, omega3G = 1.28,
            calciumMg = 100.0,      // 10% of 1000
            magnesiumMg = 20.0      // 5% of 400
        )
        val short = Nutrients.shortfalls(micros, limit = 3)
        assertEquals(2, short.size)
        assertEquals("Magnesium", short[0].label)
        assertEquals("Calcium", short[1].label)
        assertTrue(short.all { it.direction == Direction.FLOOR })
    }

    @Test
    fun `a nutrient at zero is the shortest of all`() {
        // 300 of 400 mg is past the 60% mark that counts as short; every other floor
        // here is untouched, so all of them rank ahead of it.
        val short = Nutrients.shortfalls(Micros(magnesiumMg = 300.0), limit = 15)
        assertTrue("three quarters of a reference is not short", short.none { it.label == "Magnesium" })
        assertTrue("every untouched floor is short", short.size >= 10)
        assertTrue("a zero must rank worst", short.first().reference > 0.0)
        assertEquals(0.0, short.first().read(Micros(magnesiumMg = 300.0)), 0.001)
    }

    @Test
    fun `a met floor is not reported short`() {
        val micros = Micros(vitCMg = 200.0)
        assertTrue(Nutrients.shortfalls(micros).none { it.label == "Vitamin C" })
    }

    @Test
    fun `progress clamps but the raw reading does not`() {
        val vitC = Nutrients.ALL.first { it.label == "Vitamin C" }
        assertEquals(1.0f, Nutrients.progress(vitC, Micros(vitCMg = 500.0)), 0.001f)
        assertTrue(Nutrients.rawProgress(vitC, Micros(vitCMg = 500.0)) > 5.0)
    }

    @Test
    fun `every reference is positive and every row reads a distinct nutrient`() {
        Nutrients.ALL.forEach {
            assertTrue("${it.label} has a non-positive reference", it.reference > 0.0)
            assertTrue("${it.label} has no unit", it.unit.isNotBlank())
        }
        assertEquals(Nutrients.ALL.size, Nutrients.ALL.map { it.label }.toSet().size)
        // Each row must pull a different field: a copy-paste slip in the lambdas
        // would otherwise show one nutrient's value under two labels.
        val probe = Micros(
            fiberG = 1.0, sugarG = 2.0, satFatG = 3.0, sodiumMg = 4.0, potassiumMg = 5.0,
            calciumMg = 6.0, ironMg = 7.0, magnesiumMg = 8.0, zincMg = 9.0, vitAMcg = 10.0,
            vitCMg = 11.0, vitDMcg = 12.0, vitB12Mcg = 13.0, folateMcg = 14.0, omega3G = 15.0
        )
        val readings = Nutrients.ALL.map { it.read(probe) }
        assertEquals(readings.size, readings.toSet().size)
    }
}
