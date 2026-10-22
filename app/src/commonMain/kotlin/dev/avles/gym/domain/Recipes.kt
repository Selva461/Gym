package dev.avles.gym.domain

import dev.avles.gym.data.FoodItem
import dev.avles.gym.data.Micros
import kotlin.math.roundToInt

/**
 * A home recipe as one food: the ingredients summed and divided by the servings it
 * makes, so a pot of sambar is logged as "1 serving" the rest of the week.
 */
object Recipes {

    const val CATEGORY = "My recipes"

    data class Part(val food: FoodItem, val quantity: Double)

    /**
     * One serving of [parts] split [servings] ways. The ingredient list goes into
     * the food's source, so the editor shows what it was made from.
     */
    fun combine(name: String, parts: List<Part>, servings: Int): FoodItem {
        val n = servings.coerceAtLeast(1).toDouble()
        val used = parts.filter { it.quantity > 0 }
        fun sum(of: (FoodItem) -> Double) = used.sumOf { of(it.food) * it.quantity } / n
        val micros = used.fold(Micros()) { acc, p -> acc + p.food.micros * p.quantity } * (1 / n)
        return FoodItem(
            name = name.trim(),
            unitLabel = if (servings <= 1) "1 serving (whole recipe)" else "1 serving (of ${servings})",
            kcal = round1(sum { it.kcal }),
            proteinG = round1(sum { it.proteinG }),
            carbsG = round1(sum { it.carbsG }),
            fatG = round1(sum { it.fatG }),
            category = CATEGORY,
            isCustom = true,
            micros = micros,
            source = "Recipe: " + used.joinToString(", ") { "${trim(it.quantity)} × ${it.food.name}" }
        )
    }

    private fun round1(v: Double) = (v * 10).roundToInt() / 10.0

    private fun trim(v: Double): String =
        if (v == v.toLong().toDouble()) v.toLong().toString() else ((v * 100).roundToInt() / 100.0).toString()
}
