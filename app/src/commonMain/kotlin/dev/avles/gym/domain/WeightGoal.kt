package dev.avles.gym.domain

import dev.avles.gym.data.BodyWeight
import kotlin.math.abs
import kotlin.math.ceil

/**
 * Where the weight is heading and when it gets to the goal: the projection every
 * weight app shows. The rate is a least-squares line through the last four weeks
 * of weigh-ins, not the last two numbers, because day-to-day water swings of a
 * kilo would otherwise make the date jump around every morning.
 */
object WeightGoal {

    const val WINDOW_DAYS = 28

    /** Kilos per week, negative when losing. Null with fewer than three weigh-ins in the window. */
    fun weeklyRate(weights: List<BodyWeight>, today: Long): Double? {
        val recent = weights.filter { it.dateEpochDay > today - WINDOW_DAYS }
        if (recent.size < 3) return null
        val xs = recent.map { it.dateEpochDay.toDouble() }
        val ys = recent.map { it.weightKg }
        val mx = xs.average()
        val my = ys.average()
        val sxx = xs.sumOf { (it - mx) * (it - mx) }
        if (sxx == 0.0) return null
        val slope = xs.indices.sumOf { (xs[it] - mx) * (ys[it] - my) } / sxx
        return slope * 7
    }

    /**
     * Days until [goalKg] at [ratePerWeek] from [latestKg]; null when there is no
     * goal, it is already reached, or the trend is heading the other way (or flat).
     */
    fun daysToGoal(latestKg: Double, goalKg: Double, ratePerWeek: Double?): Int? {
        if (goalKg <= 0 || ratePerWeek == null) return null
        val gap = goalKg - latestKg
        if (abs(gap) < 0.1) return 0
        if (ratePerWeek == 0.0 || gap * ratePerWeek < 0) return null
        val perDay = ratePerWeek / 7
        // The epsilon keeps 6.4 / 0.1 = 64.000000001 from rounding up to 65.
        return ceil(gap / perDay - 1e-6).toInt().takeIf { it in 1..3650 }
    }
}
