package dev.avles.gym

import dev.avles.gym.data.FoodItem
import dev.avles.gym.domain.FoodEdits
import dev.avles.gym.domain.FoodSearch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodSearchTest {

    private fun food(name: String, aliases: String = "", source: String = "") = FoodItem(
        name = name, unitLabel = "100 g", kcal = 100.0, proteinG = 5.0, carbsG = 10.0,
        fatG = 3.0, category = "Test", aliases = aliases, source = source
    )

    private val catalogue = listOf(
        food("Chicken paneer tikka"),
        food("Paneer pulao"),
        food("Potato paratha", aliases = "Aloo ka paratha"),
        food("Paneer"),
        food("Rice kheer", aliases = "Chawal ki kheer")
    )

    @Test
    fun `a name starting with the query ranks first, and shorter wins a tie`() {
        val names = FoodSearch.search(catalogue, "paneer").map { it.name }
        assertEquals(listOf("Paneer", "Paneer pulao", "Chicken paneer tikka"), names)
    }

    @Test
    fun `words can come in any order`() {
        assertEquals(listOf("Chicken paneer tikka"), FoodSearch.search(catalogue, "tikka chicken").map { it.name })
    }

    @Test
    fun `a local name finds the dish`() {
        assertEquals(listOf("Potato paratha"), FoodSearch.search(catalogue, "aloo").map { it.name })
        assertEquals(listOf("Rice kheer"), FoodSearch.search(catalogue, "chawal kheer").map { it.name })
    }

    @Test
    fun `a name match beats an alias match`() {
        val list = listOf(food("Kheer"), food("Rice pudding", aliases = "Kheer"))
        assertEquals(listOf("Kheer", "Rice pudding"), FoodSearch.search(list, "kheer").map { it.name })
    }

    @Test
    fun `blank queries and misses return nothing`() {
        assertTrue(FoodSearch.search(catalogue, "   ").isEmpty())
        assertTrue(FoodSearch.search(catalogue, "sushi").isEmpty())
    }

    @Test
    fun `the result is capped`() {
        val many = (1..50).map { food("Dal $it") }
        assertEquals(FoodSearch.DEFAULT_LIMIT, FoodSearch.search(many, "dal").size)
    }

    @Test
    fun `editing a dataset food's numbers stops attributing them to the dataset`() {
        val before = food("Rice kheer", source = "INDB 2024.11 recipe estimate")
        val renamed = FoodEdits.withProvenance(before, before.copy(name = "Kheer", aliases = "Payasam"))
        assertEquals("INDB 2024.11 recipe estimate", renamed.source)
        val changed = FoodEdits.withProvenance(before, before.copy(kcal = 140.0))
        assertEquals("Edited from INDB 2024.11 recipe estimate", changed.source)
        // A second edit does not stack prefixes.
        assertEquals(changed.source, FoodEdits.withProvenance(changed, changed.copy(kcal = 150.0)).source)
        // A food with no source gains none.
        val custom = food("Mine")
        assertEquals("", FoodEdits.withProvenance(custom, custom.copy(kcal = 1.0)).source)
    }
}
