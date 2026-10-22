package dev.avles.gym

import dev.avles.gym.data.Meals
import dev.avles.gym.data.SeedData
import dev.avles.gym.domain.FoodChecks
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

    private val splits = SeedData.splits()

    @Test
    fun `the catalogue is the size the README claims`() {
        assertEquals(486, exercises.size)
        assertEquals(768, foods.size)
        assertEquals(10, splits.size)
    }

    /**
     * Splits name their lifts as strings, because exercise ids do not exist until the
     * database assigns them. That makes a typo silent: the lift is simply missing from
     * the split and nothing complains. This is the check that complains.
     */
    @Test
    fun `every lift named by a split exists in the catalogue`() {
        val known = exercises.map { it.name }.toSet()
        splits.forEach { (splitName, lifts) ->
            lifts.forEach { lift ->
                assertTrue("$splitName names \"$lift\", which is not a seeded lift", lift in known)
            }
        }
    }

    @Test
    fun `no split is empty or nameless, and none is named twice`() {
        splits.forEach { (name, lifts) ->
            assertTrue("a split has a blank name", name.isNotBlank())
            assertTrue("$name has no lifts", lifts.isNotEmpty())
            assertEquals("$name names a lift twice", lifts.size, lifts.toSet().size)
        }
        assertEquals(splits.size, splits.map { it.first }.toSet().size)
    }

    /**
     * Groups are real muscle groups now, not Push/Pull/Legs — with close to four
     * hundred lifts, three buckets is a scroll rather than a menu. Every lift must
     * land in one the picker and the new-lift dialog actually offer.
     */
    @Test
    fun `every lift falls into a group the app offers`() {
        val declared = SeedData.muscleGroups().toSet()
        val used = exercises.map { it.muscleGroup }.toSet()
        assertEquals(declared, used)
    }

    /**
     * The point of the expansion: you should be able to find a triceps movement for
     * whatever is free. Equipment lives in the lift's name so that typing "cable"
     * into the picker filters to cable work, which only holds if the names say so.
     */
    @Test
    fun `every major muscle has work on free weights and on a machine`() {
        listOf("Chest", "Back", "Shoulders", "Triceps", "Biceps", "Quads").forEach { group ->
            val inGroup = exercises.filter { it.muscleGroup == group }
            assertTrue("$group has under 30 lifts", inGroup.size >= 30)
            listOf("umbbell", "achine").forEach { equipment ->
                assertTrue(
                    "$group has nothing containing \"$equipment\" in its name",
                    inGroup.any { it.name.contains(equipment) }
                )
            }
        }
    }

    /**
     * Cable work is required only where cables are actually how the muscle is
     * trained. Quads are the honest exception — leg extension is a machine, and
     * there is no cable quad movement anyone logs — so demanding one here would
     * have meant inventing an exercise to satisfy a test.
     */
    @Test
    fun `the muscles people train on cables have cable work`() {
        listOf("Chest", "Back", "Shoulders", "Triceps", "Biceps", "Traps", "Glutes").forEach { group ->
            val cable = exercises.count { it.muscleGroup == group && it.name.contains("able") }
            assertTrue("$group has only $cable cable movements", cable >= 4)
        }
    }

    /**
     * The groups the first pass under-served are the ones the coach's weekly
     * targets will send you looking for, so a dozen lifts each is not enough.
     */
    @Test
    fun `the muscles people neglect still have real choice`() {
        mapOf("Traps" to 18, "Forearms" to 20, "Calves" to 18, "Glutes" to 22, "Hamstrings" to 24)
            .forEach { (group, least) ->
                val count = exercises.count { it.muscleGroup == group }
                assertTrue("$group has only $count lifts, expected at least $least", count >= least)
            }
    }

    @Test
    fun `nothing in the catalogue is named twice`() {
        assertEquals(exercises.size, exercises.map { it.name }.toSet().size)
        assertEquals(foods.size, foods.map { it.name }.toSet().size)
    }

    /**
     * A lift whose range is inverted, or whose increment is zero, would make
     * Progression recommend nonsense or loop forever at one weight.
     */
    @Test
    fun `every lift has a usable progression range`() {
        exercises.forEach { e ->
            assertTrue("${e.name}: repLow ${e.repLow} above repHigh ${e.repHigh}", e.repLow <= e.repHigh)
            assertTrue("${e.name}: repLow must be positive", e.repLow >= 1)
            assertTrue("${e.name}: increment must be positive", e.incrementKg > 0.0)
            assertTrue("${e.name}: increment of ${e.incrementKg} kg is implausible", e.incrementKg <= 10.0)
        }
    }

    @Test
    fun `every food is named, has a unit, and carries no negative value`() {
        foods.forEach { f ->
            assertTrue("blank name", f.name.isNotBlank())
            assertTrue("${f.name} has no unit label", f.unitLabel.isNotBlank())
            assertTrue("${f.name} has negative kcal", f.kcal >= 0)
            assertTrue("${f.name} has negative protein", f.proteinG >= 0)
            assertTrue("${f.name} has negative carbs", f.carbsG >= 0)
            assertTrue("${f.name} has negative fat", f.fatG >= 0)
            val m = f.micros
            listOf(
                "fibre" to m.fiberG, "sugar" to m.sugarG, "saturated fat" to m.satFatG,
                "sodium" to m.sodiumMg, "potassium" to m.potassiumMg, "calcium" to m.calciumMg,
                "iron" to m.ironMg, "magnesium" to m.magnesiumMg, "zinc" to m.zincMg,
                "vitamin A" to m.vitAMcg, "vitamin C" to m.vitCMg, "vitamin D" to m.vitDMcg,
                "B12" to m.vitB12Mcg, "folate" to m.folateMcg, "omega-3" to m.omega3G
            ).forEach { (label, value) ->
                assertTrue("${f.name} has negative $label", value >= 0.0)
            }
        }
    }

    @Test
    fun `no food claims more fibre or sugar than it has carbohydrate`() {
        foods.forEach { f ->
            assertTrue(
                "${f.name}: ${f.micros.fiberG} g fibre but only ${f.carbsG} g carbs",
                f.micros.fiberG <= f.carbsG + 0.01
            )
            assertTrue(
                "${f.name}: ${f.micros.sugarG} g sugar but only ${f.carbsG} g carbs",
                f.micros.sugarG <= f.carbsG + 0.01
            )
        }
    }

    @Test
    fun `no food claims more saturated fat than total fat`() {
        foods.forEach { f ->
            assertTrue(
                "${f.name}: ${f.micros.satFatG} g saturated of ${f.fatG} g total fat",
                f.micros.satFatG <= f.fatG + 0.01
            )
        }
    }

    /**
     * Catches a misplaced decimal point rather than a debatable estimate.
     *
     * Fibre is counted at 2 kcal/g, not 4 — a plain 4/4/9 check reads cooked spinach
     * as 29% wrong when the data is right, because most of spinach's carbohydrate is
     * fibre the body does not get 4 kcal/g out of. With that correction every seeded
     * food sits inside 12%; the 20% band leaves room for honest estimation while a
     * 290 typed as 2900 still fails by a factor of ten.
     */
    @Test
    fun `declared calories agree with the macros that make them up`() {
        foods.forEach { f ->
            val drift = FoodChecks.energyDrift(f) ?: return@forEach
            assertTrue(
                "${f.name}: declared ${f.kcal} kcal but its macros imply ${FoodChecks.impliedKcal(f)}",
                drift <= FoodChecks.ENERGY_TOLERANCE
            )
        }
    }

    /**
     * The importer rejects a dataset row on exactly these rules at runtime, so the
     * built-in catalogue is held to the same ones. If a rule tightens, this is where
     * a built-in food that no longer meets it shows up.
     */
    @Test
    fun `every built-in food passes the gate an imported one must pass`() {
        foods.forEach { f ->
            assertTrue("${f.name}: ${FoodChecks.problems(f)}", FoodChecks.isValid(f))
        }
    }

    /**
     * Anything filed under Protein should either carry a real amount of it or be
     * mostly protein by energy. Egg white is only 3.6 g a piece but 85% of its
     * calories, and belongs in the category; a curry at 19% belongs there on the
     * 14 g. Either test alone would reject one of them.
     */
    @Test
    fun `the protein foods actually carry protein`() {
        val protein = foods.filter { it.category == "Protein" }
        assertTrue("no protein category seeded", protein.isNotEmpty())
        protein.forEach { f ->
            val share = if (f.kcal <= 0) 1.0 else f.proteinG * 4 / f.kcal
            assertTrue(
                "${f.name}: ${f.proteinG} g protein, ${(share * 100).toInt()}% of kcal",
                f.proteinG >= 5.0 || share >= 0.25
            )
        }
    }

    @Test
    fun `supplements are flagged and filed consistently`() {
        val flagged = foods.filter { it.isSupplement }
        assertTrue("no supplements seeded", flagged.size >= 15)
        flagged.forEach {
            assertEquals("${it.name} is flagged but not categorised", "Supplement", it.category)
        }
        foods.filter { it.category == "Supplement" }.forEach {
            assertTrue("${it.name} is categorised but not flagged", it.isSupplement)
        }
    }

    @Test
    fun `a supplement files itself under the supplement slot`() {
        assertEquals(Meals.SUPPLEMENT, Meals.slotFor(isSupplement = true, chosen = Meals.DINNER))
        assertEquals(Meals.DINNER, Meals.slotFor(isSupplement = false, chosen = Meals.DINNER))
    }

    /**
     * The variety the catalogue was expanded for, pinned so it cannot quietly shrink.
     * South Indian cooking is the bulk of it because it is the bulk of what gets
     * eaten here; the world cuisines are there so a meal out is still loggable.
     */
    @Test
    fun `the catalogue covers the range it was built for`() {
        val byCategory = foods.groupBy { it.category }.mapValues { it.value.size }
        mapOf(
            "Dosa" to 20, "Idli" to 6, "Tiffin" to 35, "Rice" to 25, "Sides" to 35,
            "Protein" to 55, "Vegetables" to 20, "Fruit" to 20, "Sweets" to 10,
            "Drinks" to 15, "Grains" to 15, "Dairy" to 12, "North Indian" to 25,
            // Filed apart from Drinks on purpose: a glass of sugarcane juice is 180
            // kcal of nearly pure sugar and a tender coconut is 46. Among the teas
            // and coffees the difference disappears; in a list of its own it is the
            // first thing you see.
            "Juices" to 20, "Snacks" to 15
        ).forEach { (category, least) ->
            assertTrue(
                "$category has ${byCategory[category] ?: 0}, expected at least $least",
                (byCategory[category] ?: 0) >= least
            )
        }
        listOf("Italian", "Chinese", "Mexican", "Japanese", "Middle Eastern", "Continental")
            .forEach { assertTrue("no $it food seeded", (byCategory[it] ?: 0) >= 3) }
    }

    /**
     * Names are how you find anything in a catalogue this size — the food picker
     * searches them and nothing else. A duplicate is caught above; this catches the
     * other end, a name so short it matches everything or so long it is truncated
     * to uselessness in a list row.
     */
    @Test
    fun `every food name is a usable length`() {
        foods.forEach { f ->
            assertTrue("\"${f.name}\" is too short to identify", f.name.length >= 3)
            assertTrue("\"${f.name}\" is too long for a list row", f.name.length <= 48)
        }
    }
}
