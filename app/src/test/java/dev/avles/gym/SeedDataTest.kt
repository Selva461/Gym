package dev.avles.gym

import dev.avles.gym.data.SeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The seed catalogue is data, not logic, so these are not really unit tests — they
 * are a tripwire. Everything asserted here is a number the README quotes or a shape
 * the app assumes, and a slip while editing a food is otherwise invisible until it
 * quietly skews a day's protein total on the phone.
 */
class SeedDataTest {

    private val exercises = SeedData.exercises()
    private val foods = SeedData.foods()

    @Test
    fun `the catalogue is the size the README claims`() {
        assertEquals(27, exercises.size)
        assertEquals(35, foods.size)
    }

    @Test
    fun `every lift falls into one of the four session labels`() {
        val groups = exercises.map { it.muscleGroup }.toSet()
        assertEquals(setOf("Push", "Pull", "Legs", "Core"), groups)
    }

    @Test
    fun `nothing in the catalogue is named twice`() {
        assertEquals(exercises.size, exercises.map { it.name }.toSet().size)
        assertEquals(foods.size, foods.map { it.name }.toSet().size)
    }

    @Test
    fun `every food is named, has a unit, and carries no negative macro`() {
        foods.forEach { f ->
            assertTrue("blank name", f.name.isNotBlank())
            assertTrue("${f.name} has no unit label", f.unitLabel.isNotBlank())
            assertTrue("${f.name} has negative kcal", f.kcal >= 0)
            assertTrue("${f.name} has negative protein", f.proteinG >= 0)
            assertTrue("${f.name} has negative carbs", f.carbsG >= 0)
            assertTrue("${f.name} has negative fat", f.fatG >= 0)
        }
    }

    /**
     * Catches a misplaced decimal point rather than a debatable estimate. Every seeded
     * food currently sits within about 10% of its own 4/4/9 arithmetic; 15% leaves room
     * for honest rounding while a 290 typed as 2900 fails by a mile. Foods under 20 kcal
     * are skipped — at that size the rounding is larger than the signal.
     */
    @Test
    fun `declared calories agree with the macros that make them up`() {
        foods.filter { it.kcal >= 20 }.forEach { f ->
            val implied = f.proteinG * 4 + f.carbsG * 4 + f.fatG * 9
            val drift = kotlin.math.abs(f.kcal - implied) / implied
            assertTrue(
                "${f.name}: declared ${f.kcal} kcal but its macros imply $implied",
                drift <= 0.15
            )
        }
    }

    @Test
    fun `the protein foods actually carry protein`() {
        val protein = foods.filter { it.category == "Protein" }
        assertTrue("no protein category seeded", protein.isNotEmpty())
        protein.forEach {
            assertTrue("${it.name} is filed under Protein with ${it.proteinG} g", it.proteinG >= 5.0)
        }
    }
}
