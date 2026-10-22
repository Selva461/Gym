package dev.avles.gym.domain

import kotlin.math.roundToInt

/**
 * Energy spent training, estimated the way the food apps credit exercise: MET ×
 * body weight × time. Weight training averages about 5 MET across a session that
 * includes the rests (the Compendium of Physical Activities puts general resistance
 * training at 3.5 and vigorous at 6). It is an estimate within perhaps a third,
 * which is why the app shows it and does not add it to the calorie target.
 */
object Burn {

    const val STRENGTH_MET = 5.0

    /** kcal for [minutes] at [met] for someone weighing [bodyKg]; zero without a weight. */
    fun kcal(minutes: Int, bodyKg: Double, met: Double = STRENGTH_MET): Int {
        if (minutes <= 0 || bodyKg <= 0) return 0
        return (met * 3.5 * bodyKg / 200.0 * minutes).roundToInt()
    }
}
