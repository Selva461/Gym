package dev.avles.gym.domain

import dev.avles.gym.data.FoodItem

/**
 * What an edit to a food does to the claim about where its numbers came from.
 *
 * A food imported from a dataset says so in the editor. Once someone changes its
 * calories or macros the numbers are theirs, and still attributing them to the
 * dataset would be a small lie in the one place the app makes a claim about data.
 * Renaming the food, or adding what it is also called, leaves the numbers alone
 * and so leaves the attribution alone.
 */
object FoodEdits {

    const val EDITED_PREFIX = "Edited from "

    fun withProvenance(before: FoodItem, after: FoodItem): FoodItem {
        val numbersChanged = before.kcal != after.kcal ||
            before.proteinG != after.proteinG ||
            before.carbsG != after.carbsG ||
            before.fatG != after.fatG ||
            before.unitLabel != after.unitLabel ||
            before.micros != after.micros
        if (!numbersChanged || before.source.isBlank() || before.source.startsWith(EDITED_PREFIX)) {
            return after
        }
        return after.copy(source = EDITED_PREFIX + before.source)
    }
}
