package dev.avles.gym.domain

/**
 * Logging by weight. Most catalogue units say what they weigh — "100 g",
 * "1 scoop (30.4 g)", "1 glass (200 ml)" — so a food can be logged as "180 g" as
 * well as "1.5 servings" without anyone doing the division.
 */
object Portions {

    /** What one unit weighs (or holds): [amount] of [unit], which is "g" or "ml". */
    data class Measure(val amount: Double, val unit: String)

    private val AMOUNT = Regex("""(?<![\d.])(\d+(?:\.\d+)?)\s*(g|ml)\b""")

    /**
     * The weight one unit stands for, or null when the label does not say, or says
     * two things ("60 ml + 200 ml cola", "200 ml + 20 g"), where no single number
     * converts honestly.
     */
    fun measureOf(unitLabel: String): Measure? {
        val found = AMOUNT.findAll(unitLabel.lowercase()).toList()
        if (found.size != 1) return null
        val amount = found[0].groupValues[1].toDoubleOrNull() ?: return null
        if (amount <= 0) return null
        return Measure(amount, found[0].groupValues[2])
    }

    /** How many units [amount] grams (or ml) is. */
    fun unitsFor(amount: Double, measure: Measure): Double =
        if (measure.amount <= 0) 0.0 else amount / measure.amount
}
