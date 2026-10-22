package dev.avles.gym.domain

import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Daily targets worked out from a body, the way every nutrition app's onboarding
 * does it, and the split of a calorie target into protein, carbohydrate and fat.
 *
 * Mifflin-St Jeor for resting energy: of the common equations it is the one that
 * holds up best against measured metabolic rate in adults, and the one MyFitnessPal,
 * HealthifyMe and most coaching calculators use. Everything it returns is a starting
 * point. The weight trend over the next three weeks is the real measurement, and the
 * text on screen says so.
 */
object Energy {

    enum class Sex { MALE, FEMALE }

    /** Activity multipliers on resting energy, with the words people recognise. */
    enum class Activity(val label: String, val factor: Double) {
        DESK("Desk job, little else", 1.2),
        LIGHT("Training 2–3 days", 1.375),
        MODERATE("Training 4–5 days", 1.55),
        HIGH("Training 6+ days or a physical job", 1.725)
    }

    /**
     * What the target is for. A cut is a fifth under maintenance, which loses about
     * half a kilo a week for most people without flattening training; a lean gain
     * is a tenth over, because more than that is mostly fat.
     */
    enum class Goal(val label: String, val kcalFactor: Double, val proteinPerKg: Double) {
        LOSE("Lose fat", 0.80, 2.0),
        MAINTAIN("Maintain", 1.0, 1.8),
        GAIN("Build muscle", 1.10, 1.8)
    }

    data class Targets(
        val kcal: Double,
        val proteinG: Double,
        val carbsG: Double,
        val fatG: Double,
        val maintenanceKcal: Double,
        val waterGlasses: Int
    )

    /** Resting energy, kcal/day. Zero for impossible inputs rather than a nonsense figure. */
    fun restingKcal(weightKg: Double, heightCm: Double, age: Int, sex: Sex): Double {
        if (weightKg <= 0 || heightCm <= 0 || age <= 0) return 0.0
        val base = 10 * weightKg + 6.25 * heightCm - 5 * age
        return base + if (sex == Sex.MALE) 5 else -161
    }

    fun targets(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        sex: Sex,
        activity: Activity,
        goal: Goal
    ): Targets? {
        val resting = restingKcal(weightKg, heightCm, age, sex)
        if (resting <= 0) return null
        val maintenance = resting * activity.factor
        // Never below resting energy, whatever the goal: under that the deficit is
        // coming out of muscle as much as fat.
        val kcal = roundTo(max(maintenance * goal.kcalFactor, resting), 50)
        val protein = roundTo(weightKg * goal.proteinPerKg, 5)
        val split = split(kcal, protein)
        return Targets(
            kcal = kcal,
            proteinG = protein,
            carbsG = split.carbsG,
            fatG = split.fatG,
            maintenanceKcal = roundTo(maintenance, 50),
            waterGlasses = waterGlasses(weightKg)
        )
    }

    /** Fat takes a quarter of the calories, protein is fixed, carbohydrate is the rest. */
    const val FAT_SHARE = 0.25

    data class Split(val proteinG: Double, val carbsG: Double, val fatG: Double)

    /**
     * Protein first, because it is the target this app is built around; fat at a
     * quarter of calories, enough for hormones and a normal diet; carbohydrate takes
     * what is left, which is what fuels training. Rounded to the gram.
     */
    fun split(kcalTarget: Double, proteinTargetG: Double): Split {
        if (kcalTarget <= 0) return Split(proteinTargetG, 0.0, 0.0)
        val fat = (kcalTarget * FAT_SHARE / 9).roundToInt().toDouble()
        val carbs = max(0.0, (kcalTarget - proteinTargetG * 4 - fat * 9) / 4).roundToInt().toDouble()
        return Split(proteinTargetG, carbs, fat)
    }

    /** One glass is 250 ml. */
    const val GLASS_ML = 250

    /**
     * 35 ml per kilo, the usual adult guideline, in whole glasses and never under
     * eight. Training sweat is on top of this and nobody needs an app to tell them
     * to drink when they are thirsty after a session.
     */
    fun waterGlasses(weightKg: Double): Int {
        if (weightKg <= 0) return 8
        return max(8, (weightKg * 35 / GLASS_ML).roundToInt())
    }

    private fun roundTo(v: Double, step: Int): Double = ((v / step).roundToInt() * step).toDouble()
}
