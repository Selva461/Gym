package dev.avles.gym.domain

import dev.avles.gym.data.SavedMealRow

/** Saved meals assembled from their joined rows, with what one serving of each adds up to. */
object SavedMeals {

    data class Meal(val id: Long, val name: String, val items: List<SavedMealRow>) {
        val kcal: Double get() = items.sumOf { it.kcal * it.quantity }
        val proteinG: Double get() = items.sumOf { it.proteinG * it.quantity }
    }

    /** Keeps the rows' order: newest meal first, foods in the order they were saved. */
    fun group(rows: List<SavedMealRow>): List<Meal> =
        rows.groupBy { it.mealId }.map { (id, items) -> Meal(id, items.first().mealName, items) }
}
