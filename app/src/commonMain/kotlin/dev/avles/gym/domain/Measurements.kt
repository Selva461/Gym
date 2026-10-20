package dev.avles.gym.domain

import dev.avles.gym.data.BodyMeasurement
import kotlin.math.log10

/**
 * Tape-measure tracking: waist, chest, arms and the rest, the numbers that move
 * when the scale does not. The body-fat estimate is the US Navy circumference
 * method, the one the tracking apps use because it needs only a tape: within a few
 * points of a DEXA scan for most people, and consistent with itself, which is what
 * a trend needs.
 */
object Measurements {

    /** The sites people measure, in the order a tape goes round the body. */
    val PARTS = listOf("Neck", "Chest", "Arm", "Waist", "Hips", "Thigh", "Calf")

    data class Trend(val part: String, val latestCm: Double, val firstCm: Double, val points: List<Double>) {
        val changeCm: Double get() = latestCm - firstCm
    }

    /** Per part, oldest first, only the parts measured at least once, in [PARTS] order. */
    fun trends(all: List<BodyMeasurement>): List<Trend> =
        PARTS.mapNotNull { part ->
            val series = all.filter { it.part == part }.sortedBy { it.dateEpochDay }
            if (series.isEmpty()) null
            else Trend(part, series.last().cm, series.first().cm, series.map { it.cm })
        }

    /**
     * US Navy estimate, in percent, from circumferences and height in centimetres.
     * Null when an input is missing or the measurements are impossible (a waist
     * smaller than the neck), rather than a nonsense number.
     */
    fun navyBodyFat(isFemale: Boolean, heightCm: Double, neckCm: Double, waistCm: Double, hipsCm: Double?): Double? {
        if (heightCm <= 0 || neckCm <= 0 || waistCm <= 0) return null
        val result = if (isFemale) {
            val hips = hipsCm ?: return null
            val girth = waistCm + hips - neckCm
            if (girth <= 0) return null
            495 / (1.29579 - 0.35004 * log10(girth) + 0.22100 * log10(heightCm)) - 450
        } else {
            val girth = waistCm - neckCm
            if (girth <= 0) return null
            495 / (1.0324 - 0.19077 * log10(girth) + 0.15456 * log10(heightCm)) - 450
        }
        return result.takeIf { it in 2.0..70.0 }
    }
}
