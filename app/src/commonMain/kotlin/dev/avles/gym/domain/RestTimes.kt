package dev.avles.gym.domain

import dev.avles.gym.data.Exercise
import kotlin.math.max
import kotlin.math.min

/**
 * How long to rest after a working set, by what kind of lift it was. Heavy low-rep
 * compounds need about three minutes for the next set to be as good as the last;
 * high-rep isolation work recovers in ninety seconds. Everything between uses your
 * own setting. Read off the lift's rep range, which already says which kind it is.
 */
object RestTimes {

    const val HEAVY_SECONDS = 180
    const val LIGHT_SECONDS = 90

    fun forLift(exercise: Exercise?, defaultSeconds: Int): Int = when {
        exercise == null -> defaultSeconds
        exercise.repHigh <= 6 -> max(defaultSeconds, HEAVY_SECONDS)
        exercise.repLow >= 12 -> min(defaultSeconds, LIGHT_SECONDS)
        else -> defaultSeconds
    }
}
