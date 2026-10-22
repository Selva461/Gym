package dev.avles.gym

import dev.avles.gym.data.FoodItem
import dev.avles.gym.data.SeedData
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

    /** The Tamil curries are named in Tamil; English is how many people will look for them. */
    @Test
    fun `an English word finds the Tamil-named curry`() {
        val all = SeedData.foods()
        fun names(q: String) = FoodSearch.search(all, q, limit = 50).map { it.name }
        assertTrue(names("brinjal").containsAll(listOf("Kathirikai kara kuzhambu", "Kathirikai sambar")))
        assertTrue(names("drumstick").containsAll(listOf("Murungakkai sambar", "Murungakkai puli kuzhambu")))
        assertTrue("Vendakkai puli kuzhambu" in names("okra"))
        assertTrue("Kollu rasam" in names("horse gram"))
        assertTrue("Nandu kuzhambu" in names("crab"))
    }

    /** Meals are ordered by name, the egg counter by its Tamil words. */
    @Test
    fun `meals, eggs and mushroom dishes are found by what people call them`() {
        val all = SeedData.foods()
        fun names(q: String) = FoodSearch.search(all, q, limit = 50).map { it.name }
        assertTrue(names("meals").containsAll(listOf("Veg meals", "Chicken meals", "Fish meals", "Mutton meals")))
        assertTrue("Fish meals" in names("meen saapadu"))
        assertTrue("Kalakki" in names("kalakki"))
        assertTrue(names("omelette").containsAll(listOf("Omelette", "Masala omelette", "Cheese omelette")))
        assertTrue("Mushroom gravy" in names("mushroom gravy"))
        assertTrue("Dindigul mutton biryani" in names("thalappakatti"))
    }

    /** Restaurant orders are asked for the way the menu and the counter say them. */
    @Test
    fun `restaurant orders are found the way they are ordered`() {
        val all = SeedData.foods()
        fun names(q: String) = FoodSearch.search(all, q, limit = 50).map { it.name }
        assertTrue("Grill chicken, half" in names("half grill"))
        assertTrue("BBQ chicken, full" in names("full bbq"))
        assertTrue("Tandoori chicken, half" in names("half tandoori"))
        assertTrue("Garlic chicken" in names("garlic chicken"))
        assertTrue(names("chilli chicken").containsAll(listOf("Chilli chicken", "Chilli chicken, gravy")))
        assertTrue("Chicken Schezwan fried rice" in names("szechuan rice"))
    }

    /** Staples are asked for by the Tamil word as often as the English one, and raw or cooked matters. */
    @Test
    fun `a staple is found by its Tamil name, raw apart from cooked`() {
        val all = SeedData.foods()
        fun names(q: String) = FoodSearch.search(all, q, limit = 50).map { it.name }
        assertTrue("Horse gram, boiled" in names("kollu"))
        assertTrue("Roasted gram" in names("pottukadalai"))
        assertTrue("Ragi flour" in names("kezhvaragu"))
        assertTrue(names("chicken breast").containsAll(listOf("Chicken breast, raw", "Chicken breast, cooked weight")))
        assertTrue(names("banana").containsAll(listOf("Nendran banana", "Poovan banana", "Red banana")))
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
