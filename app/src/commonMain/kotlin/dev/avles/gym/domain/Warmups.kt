package dev.avles.gym.domain

import kotlin.math.floor

/**
 * Warm-up sets worked out from the first working weight, the ramp the lifting
 * apps print above a heavy set: the empty bar for ten, then 40%, 60% and 80% for
 * five, three and two. Reps fall as the weight climbs so the warm-up wakes the
 * movement up without spending what the working sets need.
 *
 * Each weight rounds down to what the plates make (2.5 kg steps, a 1.25 a side),
 * and a step is dropped when it would not be heavier than the one before it, so a
 * light working weight gets a short ramp rather than three sets of the same bar.
 */
object Warmups {

    data class Step(val weightKg: Double, val reps: Int)

    private val RAMP = listOf(0.4 to 5, 0.6 to 3, 0.8 to 2)

    fun plan(workingKg: Double, barKg: Double = Plates.OLYMPIC_BAR_KG): List<Step> {
        if (workingKg <= barKg) return emptyList()
        val steps = mutableListOf(Step(barKg, 10))
        for ((share, reps) in RAMP) {
            val kg = floorTo(workingKg * share, 2.5)
            if (kg > steps.last().weightKg && kg < workingKg) steps += Step(kg, reps)
        }
        return steps
    }

    private fun floorTo(v: Double, step: Double): Double = floor(v / step + 1e-9) * step
}
