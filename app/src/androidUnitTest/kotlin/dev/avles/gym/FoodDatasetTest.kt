package dev.avles.gym

import dev.avles.gym.data.FoodDataset
import dev.avles.gym.data.SeedData
import dev.avles.gym.domain.FoodChecks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The bundled Indian food dataset, read the way the app reads it on first launch.
 *
 * These run against the real file rather than a fixture, because the decisions
 * worth testing — which dishes are declined and why, which ones the app already
 * has under another name — are facts about that file.
 */
class FoodDatasetTest {

    private val seeded = SeedData.foods()
    private val csv: String = listOf(
        File("src/commonMain/composeResources/files", FoodDataset.ASSET_PATH),
        File("app/src/commonMain/composeResources/files", FoodDataset.ASSET_PATH)
    ).first { it.isFile }.readText()

    private val result = FoodDataset.parse(csv, seeded.map { it.name }.toSet())
    private val byName = result.foods.associateBy { it.name }

    @Test
    fun `every dish is accounted for once`() {
        assertEquals(61, result.foods.size)
        assertEquals(23, result.rejected.size)
        assertEquals(38, result.alreadyPresent.size)
        val all = result.foods.map { it.name } + result.rejected.map { it.dish } + result.alreadyPresent
        assertEquals("a dish was counted twice", all.size, all.map { FoodDataset.key(it) }.toSet().size)
    }

    @Test
    fun `nothing imported passes a gate the built-in foods would fail`() {
        result.foods.forEach { f ->
            assertTrue("${f.name}: ${FoodChecks.problems(f)}", FoodChecks.isValid(f))
            assertTrue("${f.name} has no unit", f.unitLabel.isNotBlank())
            assertTrue("${f.name} carries no source", f.source.isNotBlank())
            assertTrue("\"${f.name}\" is too long for a list row", f.name.length <= 48)
        }
    }

    @Test
    fun `nothing imported duplicates a built-in food`() {
        val builtIn = seeded.map { FoodDataset.key(it.name) }.toSet()
        result.foods.forEach { f ->
            assertTrue("${f.name} is already in the catalogue", FoodDataset.key(f.name) !in builtIn)
        }
    }

    /** Blank protein read as zero protein, in an app whose first number is protein. */
    @Test
    fun `a dish with calories but no macros is declined, not zeroed`() {
        val reasons = result.rejected.associate { it.dish to it.reason }
        assertTrue(reasons.getValue("Tandoori Chicken").contains("without protein"))
        assertTrue(reasons.getValue("Dosa").contains("without protein"))
    }

    @Test
    fun `a dish whose calories contradict its own macros is declined`() {
        val reasons = result.rejected.associate { it.dish to it.reason }
        assertTrue(reasons.getValue("Seekh Kebab").contains("macros make"))
        assertTrue(reasons.getValue("Pilau Rice").contains("macros make"))
    }

    /** The source counts all the frying oil: 485 g of fat on a plate of pakoras. */
    @Test
    fun `a dish that is mostly fat by weight is declined`() {
        val reasons = result.rejected.associate { it.dish to it.reason }
        listOf("Onion pakora", "Potato samosa", "Vegetable cutlet", "Methi poori", "Paneer pulao").forEach {
            assertTrue("$it was not declined for its fat", reasons[it]?.contains("g of fat in") == true)
        }
    }

    @Test
    fun `dishes the app has under another name are left alone`() {
        listOf("Chapati/Roti", "Upma", "Vada", "Lassi (salted)", "Hot tea", "Vegetarian Biryani", "Pulihoora")
            .forEach { assertTrue("$it should be recognised as present", it in result.alreadyPresent) }
    }

    @Test
    fun `a countable thing keeps its own unit, a vessel becomes per 100`() {
        assertEquals("1 paratha (56 g)", byName.getValue("Plain paratha").unitLabel)
        assertEquals("1 triangle (58 g)", byName.getValue("Chicken sandwich").unitLabel)
        assertEquals("100 g", byName.getValue("Rice kheer").unitLabel)
        assertEquals("100 ml", byName.getValue("Lemonade").unitLabel)
        assertEquals("100 g", byName.getValue("Khichdi").unitLabel)
    }

    @Test
    fun `numbers are rescaled, not copied, when the unit changes`() {
        // Khichdi's rows are a weight series from 50 g; per 100 g is twice the 50 g row.
        val khichdi = byName.getValue("Khichdi")
        assertEquals(130.0, khichdi.kcal, 0.01)
        assertEquals(4.0, khichdi.proteinG, 0.01)
        val chapatiLike = byName.getValue("Plain paratha")
        assertEquals(166.3, chapatiLike.kcal, 0.01)
    }

    @Test
    fun `the second language moves from the name to the aliases`() {
        val aloo = byName.getValue("Potato paratha")
        assertEquals("Aloo ka paratha", aloo.aliases)
        assertEquals("Chawal ki kheer", byName.getValue("Rice kheer").aliases)
        // A lower-case parenthetical qualifies the dish and stays in the name.
        assertTrue("Fruit Punch (with squashes)" in byName)
        assertTrue("Fruit Punch (with fresh juices)" in byName)
        assertEquals("", byName.getValue("Fruit Punch (with squashes)").aliases)
    }

    @Test
    fun `source spellings are fixed`() {
        assertTrue("Espresso coffee" in byName)
        assertTrue(result.foods.none { "parantha" in it.name || "parantha" in it.unitLabel })
    }

    @Test
    fun `drinks file under drinks and every category is one the app already uses`() {
        val known = seeded.map { it.category }.toSet()
        result.foods.forEach { assertTrue("${it.name} files under ${it.category}", it.category in known) }
        assertEquals("Drinks", byName.getValue("Lemonade").category)
    }

    /** Seeding runs on every launch; the second run must add nothing. */
    @Test
    fun `importing twice adds nothing the second time`() {
        val names = seeded.map { it.name } + result.foods.map { it.name }
        assertEquals(0, FoodDataset.parse(csv, names.toSet()).foods.size)
    }

    @Test
    fun `the csv reader handles what a spreadsheet writes`() {
        val bom = Char(0xFEFF)
        val text = "${bom}a,b,c\r\n\"x, y\",\"say \"\"hi\"\"\",3\r\n\r\nlast,,\n"
        val rows = FoodDataset.parseCsv(text)
        assertEquals(listOf("a", "b", "c"), rows[0])
        assertEquals(listOf("x, y", "say \"hi\"", "3"), rows[1])
        assertEquals(listOf("last", "", ""), rows[2])
        assertEquals(3, rows.size)
    }

    @Test
    fun `an empty or header-only file imports nothing and does not throw`() {
        assertEquals(0, FoodDataset.parse("", emptySet()).foods.size)
        assertEquals(0, FoodDataset.parse("name,category\n", emptySet()).foods.size)
    }
}
