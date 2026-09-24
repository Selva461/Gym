package dev.avles.gym

import dev.avles.gym.data.FoodLogRow
import dev.avles.gym.domain.MacroMath
import dev.avles.gym.domain.Totals
import org.junit.Assert.assertEquals
import org.junit.Test

class MacroMathTest {

    private fun row(
        name: String,
        qty: Double,
        kcal: Double,
        p: Double,
        c: Double,
        f: Double,
        meal: String = "Lunch"
    ) = FoodLogRow(
        logId = 0, dateEpochDay = 0, meal = meal, quantity = qty, loggedAt = 0,
        foodId = 0, name = name, unitLabel = "1 unit",
        kcal = kcal, proteinG = p, carbsG = c, fatG = f
    )

    @Test
    fun `totals scale with quantity`() {
        val rows = listOf(row("Whey", 2.0, 120.0, 24.0, 2.0, 1.5))
        val t = MacroMath.totalsOf(rows)
        assertEquals(240.0, t.kcal, 0.001)
        assertEquals(48.0, t.proteinG, 0.001)
        assertEquals(4.0, t.carbsG, 0.001)
        assertEquals(3.0, t.fatG, 0.001)
    }

    @Test
    fun `totals accept fractional servings`() {
        val rows = listOf(row("Chicken curry", 0.5, 290.0, 27.0, 8.0, 17.0))
        assertEquals(145.0, MacroMath.totalsOf(rows).kcal, 0.001)
        assertEquals(13.5, MacroMath.totalsOf(rows).proteinG, 0.001)
    }

    @Test
    fun `totals of an empty day are zero, not a crash`() {
        val t = MacroMath.totalsOf(emptyList())
        assertEquals(0.0, t.kcal, 0.001)
        assertEquals(0.0, t.proteinG, 0.001)
    }

    @Test
    fun `meals are grouped independently`() {
        val rows = listOf(
            row("Idli", 3.0, 58.0, 2.0, 12.0, 0.2, meal = "Breakfast"),
            row("Chicken curry", 1.0, 290.0, 27.0, 8.0, 17.0, meal = "Lunch")
        )
        val byMeal = MacroMath.totalsByMeal(rows)
        assertEquals(174.0, byMeal.getValue("Breakfast").kcal, 0.001)
        assertEquals(27.0, byMeal.getValue("Lunch").proteinG, 0.001)
    }

    @Test
    fun `ratio clamps and survives a zero target`() {
        assertEquals(0.5f, MacroMath.ratio(75.0, 150.0), 0.0001f)
        assertEquals(1.0f, MacroMath.ratio(200.0, 150.0), 0.0001f)
        assertEquals(0.0f, MacroMath.ratio(75.0, 0.0), 0.0001f)
    }

    @Test
    fun `raw ratio reports going over`() {
        assertEquals(1.25, MacroMath.rawRatio(2500.0, 2000.0), 0.0001)
    }

    @Test
    fun `implied calories use four four nine`() {
        val t = Totals(kcal = 0.0, proteinG = 30.0, carbsG = 40.0, fatG = 10.0)
        assertEquals(30 * 4 + 40 * 4 + 10 * 9.0, MacroMath.impliedKcal(t), 0.001)
    }

    @Test
    fun `macro split of an empty day is all zeroes`() {
        assertEquals(Triple(0, 0, 0), MacroMath.macroSplitPercent(Totals()))
    }
}
