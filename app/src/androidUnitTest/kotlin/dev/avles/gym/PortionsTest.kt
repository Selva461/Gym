package dev.avles.gym

import dev.avles.gym.data.FoodItem
import dev.avles.gym.data.Micros
import dev.avles.gym.data.SeedData
import dev.avles.gym.domain.Portions
import dev.avles.gym.domain.Recipes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PortionsTest {

    @Test
    fun `a unit that states its weight converts, one that states two does not`() {
        assertEquals(Portions.Measure(100.0, "g"), Portions.measureOf("100 g"))
        assertEquals(Portions.Measure(30.4, "g"), Portions.measureOf("1 scoop (30.4 g)"))
        assertEquals(Portions.Measure(200.0, "ml"), Portions.measureOf("1 glass (200 ml)"))
        assertNull(Portions.measureOf("1 idli"))
        assertNull(Portions.measureOf("60 ml + 200 ml cola"))
        assertNull(Portions.measureOf("1 glass (200 ml + 20 g)"))
        assertEquals(1.8, Portions.unitsFor(180.0, Portions.measureOf("100 g")!!), 1e-9)
    }

    @Test
    fun `a good share of the catalogue can be logged by weight`() {
        val foods = SeedData.foods()
        val weighable = foods.count { Portions.measureOf(it.unitLabel) != null }
        // Dishes are served by the plate or the piece; staples, brands and drinks
        // carry a weight. About a third is right; far less means the parser broke.
        assertTrue("only $weighable of ${foods.size} convert", weighable * 4 > foods.size)
    }

    private fun food(name: String, kcal: Double, p: Double, c: Double, f: Double, fib: Double = 0.0) =
        FoodItem(name = name, unitLabel = "100 g", kcal = kcal, proteinG = p, carbsG = c, fatG = f,
            category = "Test", micros = Micros(fiberG = fib))

    @Test
    fun `a recipe is its ingredients split by servings`() {
        val rice = food("Rice", 130.0, 2.7, 28.0, 0.3, fib = 0.4)
        val dal = food("Dal", 100.0, 6.0, 14.0, 2.0, fib = 3.0)
        val r = Recipes.combine(" Khichdi ", listOf(Recipes.Part(rice, 3.0), Recipes.Part(dal, 1.0)), 2)
        assertEquals("Khichdi", r.name)
        assertEquals(245.0, r.kcal, 1e-9)
        assertEquals(7.1, r.proteinG, 1e-9)
        assertEquals(2.1, r.micros.fiberG, 1e-9)
        assertEquals(Recipes.CATEGORY, r.category)
        assertTrue(r.isCustom)
        assertEquals("1 serving (of 2)", r.unitLabel)
        assertEquals("Recipe: 3 × Rice, 1 × Dal", r.source)
    }
}
