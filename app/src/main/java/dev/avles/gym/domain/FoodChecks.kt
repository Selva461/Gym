package dev.avles.gym.domain

import dev.avles.gym.data.FoodItem
import kotlin.math.abs

/**
 * What makes a food's numbers believable enough to put in front of someone.
 *
 * One definition, used in two places: the seed catalogue's tests assert that every
 * built-in food passes it, and the dataset importer uses it at runtime to reject
 * rows before they reach the database. Before this existed the rules lived only in
 * a test, which meant a food arriving from a file had no gate at all.
 *
 * None of these checks second-guesses an estimate. They catch numbers that
 * contradict *each other* — a food claiming more fibre than it has carbohydrate, or
 * declaring 120 kcal while its own protein, carbs and fat add up to 86. When a
 * source disagrees with itself there is no way to know which figure is wrong, so
 * the honest response is to decline the row rather than pick one.
 *
 * Pure Kotlin with no JVM-only calls, so it runs unchanged on iOS.
 */
object FoodChecks {

    /** Beyond this, a mismatch between declared calories and macros is a typo, not an estimate. */
    const val ENERGY_TOLERANCE = 0.20

    /**
     * Foods whose energy is not described by the four macros this app tracks.
     * Beer's calories are mostly ethanol at 7 kcal/g.
     */
    val ENERGY_EXEMPT = setOf("Beer")

    /** Every reason this food fails, or an empty list if it is fine. */
    fun problems(food: FoodItem): List<String> {
        val out = mutableListOf<String>()
        val m = food.micros
        if (food.kcal < 0 || food.proteinG < 0 || food.carbsG < 0 || food.fatG < 0) {
            out += "a macro is negative"
        }
        val micros = listOf(
            m.fiberG, m.sugarG, m.satFatG, m.sodiumMg, m.potassiumMg, m.calciumMg, m.ironMg,
            m.magnesiumMg, m.zincMg, m.vitAMcg, m.vitCMg, m.vitDMcg, m.vitB12Mcg, m.folateMcg, m.omega3G
        )
        if (micros.any { it < 0 }) out += "a micronutrient is negative"
        if (m.fiberG > food.carbsG + 0.01) out += "more fibre than carbohydrate"
        if (m.sugarG > food.carbsG + 0.01) out += "more sugar than carbohydrate"
        if (m.satFatG > food.fatG + 0.01) out += "more saturated fat than total fat"
        energyDrift(food)?.let { drift ->
            if (drift > ENERGY_TOLERANCE) {
                out += "declares ${food.kcal.roundToInt()} kcal but its macros make " +
                    "${impliedKcal(food).roundToInt()}"
            }
        }
        return out
    }

    fun isValid(food: FoodItem): Boolean = problems(food).isEmpty()

    /** Beyond this share of its weight as fat, a dish is not a dish. Ghee is 100%, a samosa about 18%. */
    const val MAX_DISH_FAT_SHARE = 0.5

    /**
     * Checks that need the weight of one unit, which only some foods state.
     *
     * [problems] can only ask whether the numbers agree with each other; a samosa
     * whose 141 g carries 83 g of fat agrees with itself perfectly — 814 kcal is
     * exactly what those macros make — and is still impossible to eat. Recipe
     * databases produce this when a deep-fried recipe counts every drop of the
     * frying oil rather than what the food absorbs.
     *
     * Only for dishes. Oils, nuts and butter are legitimately more than half fat.
     */
    fun dishProblems(food: FoodItem, grams: Double): List<String> {
        if (grams <= 0.0) return emptyList()
        val out = mutableListOf<String>()
        if (food.fatG > grams * MAX_DISH_FAT_SHARE) {
            out += "${food.fatG.roundToInt()} g of fat in ${grams.roundToInt()} g of food"
        }
        if (food.proteinG + food.carbsG + food.fatG > grams * 1.02) {
            out += "its protein, carbohydrate and fat weigh more than the food itself"
        }
        return out
    }

    /**
     * Calories the macros account for. Fibre is counted at 2 kcal/g rather than 4,
     * which is why cooked spinach does not read as 29% wrong when its data is right.
     */
    fun impliedKcal(food: FoodItem): Double {
        val fibre = minOf(food.micros.fiberG, food.carbsG)
        return food.proteinG * 4 + (food.carbsG - fibre) * 4 + fibre * 2 + food.fatG * 9
    }

    /** Relative mismatch, or null where the check does not apply. */
    fun energyDrift(food: FoodItem): Double? {
        if (food.kcal < 20 || food.name in ENERGY_EXEMPT) return null
        val implied = impliedKcal(food)
        if (implied <= 0.0) return null
        return abs(food.kcal - implied) / implied
    }

    private fun Double.roundToInt(): Int = kotlin.math.round(this).toInt()
}
