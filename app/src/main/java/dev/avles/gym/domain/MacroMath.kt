package dev.avles.gym.domain

import dev.avles.gym.data.FoodLogRow
import kotlin.math.roundToInt

data class Totals(
    val kcal: Double = 0.0,
    val proteinG: Double = 0.0,
    val carbsG: Double = 0.0,
    val fatG: Double = 0.0
) {
    operator fun plus(other: Totals) = Totals(
        kcal + other.kcal,
        proteinG + other.proteinG,
        carbsG + other.carbsG,
        fatG + other.fatG
    )
}

object MacroMath {

    fun totalsOf(rows: List<FoodLogRow>): Totals = rows.fold(Totals()) { acc, r ->
        acc + Totals(
            kcal = r.kcal * r.quantity,
            proteinG = r.proteinG * r.quantity,
            carbsG = r.carbsG * r.quantity,
            fatG = r.fatG * r.quantity
        )
    }

    fun totalsByMeal(rows: List<FoodLogRow>): Map<String, Totals> =
        rows.groupBy { it.meal }.mapValues { (_, v) -> totalsOf(v) }

    /** Ratio of value to target, clamped to 0..1 for drawing. Target of zero reads as empty. */
    fun ratio(value: Double, target: Double): Float {
        if (target <= 0.0) return 0f
        return (value / target).coerceIn(0.0, 1.0).toFloat()
    }

    /** Un-clamped, for the "you are over" case. */
    fun rawRatio(value: Double, target: Double): Double =
        if (target <= 0.0) 0.0 else value / target

    /**
     * Calories implied by the macros, using 4/4/9. Compared against the logged
     * kcal it is a cheap sanity check on a food you typed in yourself.
     */
    fun impliedKcal(t: Totals): Double = t.proteinG * 4 + t.carbsG * 4 + t.fatG * 9

    fun macroSplitPercent(t: Totals): Triple<Int, Int, Int> {
        val implied = impliedKcal(t)
        if (implied <= 0.0) return Triple(0, 0, 0)
        val p = (t.proteinG * 4 / implied * 100).roundToInt()
        val c = (t.carbsG * 4 / implied * 100).roundToInt()
        return Triple(p, c, (100 - p - c).coerceAtLeast(0))
    }
}
