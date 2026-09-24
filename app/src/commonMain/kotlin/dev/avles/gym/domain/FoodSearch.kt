package dev.avles.gym.domain

import dev.avles.gym.data.FoodItem

/**
 * Finding a food from what someone types.
 *
 * Search used to be `name.contains(query)`, which had two failures that the Indian
 * dataset made common. A dish has more than one name — "Aloo ka paratha" is the
 * potato paratha — and a substring match ranks "Paneer pulao" and "Chicken paneer
 * tikka" in whatever order the catalogue happens to hold them. So:
 *
 *  - every word typed has to appear, in any order, in the name or in the aliases;
 *  - a name that starts with what was typed ranks first, then one where each word
 *    starts a word of the name, then a name that merely contains them, then a
 *    match on an alias; ties go to the shorter name, which is nearly always the
 *    plainer, commoner food.
 *
 * Pure Kotlin, so it runs on both platforms and is tested without a device.
 */
object FoodSearch {

    const val DEFAULT_LIMIT = 20

    fun search(catalogue: List<FoodItem>, query: String, limit: Int = DEFAULT_LIMIT): List<FoodItem> {
        val q = query.trim().lowercase()
        val tokens = words(q)
        if (tokens.isEmpty()) return emptyList()
        return catalogue
            .mapNotNull { food -> score(food, q, tokens)?.let { food to it } }
            .sortedWith(compareBy<Pair<FoodItem, Int>> { it.second }.thenBy { it.first.name.length }.thenBy { it.first.name })
            .take(limit)
            .map { it.first }
    }

    /** Lower is better; null means no match. */
    internal fun score(food: FoodItem, query: String, tokens: List<String>): Int? {
        val name = food.name.lowercase()
        val nameWords = words(name)
        return when {
            name.startsWith(query) -> 0
            tokens.all { t -> nameWords.any { it.startsWith(t) } } -> 1
            tokens.all { it in name } -> 2
            tokens.all { it in name || it in food.aliases.lowercase() } -> 3
            else -> null
        }
    }

    /**
     * Runs of letters and digits. A loop rather than a Unicode-class regex, because
     * regex engines differ across platforms in exactly that corner and this has to
     * split "Aloo-ka paratha" the same way on both phones.
     */
    private fun words(text: String): List<String> {
        val out = mutableListOf<String>()
        val current = StringBuilder()
        for (c in text) {
            if (c.isLetterOrDigit()) {
                current.append(c)
            } else if (current.isNotEmpty()) {
                out += current.toString()
                current.clear()
            }
        }
        if (current.isNotEmpty()) out += current.toString()
        return out
    }
}
