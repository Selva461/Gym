package dev.avles.gym.domain

import dev.avles.gym.data.Micros

/**
 * Which way a nutrient is read. Most are floors — you are trying to reach them.
 * Three are ceilings, where the interesting question is whether you went over.
 */
enum class Direction { FLOOR, CEILING }

/**
 * One row of the nutrient panel: how to pull the number out of a [Micros], what to
 * call it, and the published reference intake to compare it against.
 *
 * Driving the UI off a list rather than fifteen hand-written rows means adding a
 * nutrient is one line here and nothing anywhere else.
 */
data class NutrientRef(
    val label: String,
    val unit: String,
    val reference: Double,
    val direction: Direction = Direction.FLOOR,
    /** Decimal places to show. Vitamin D at 0 dp would read "0" all day. */
    val decimals: Int = 0,
    val note: String = "",
    val read: (Micros) -> Double
)

/**
 * Reference intakes for an adult male, taken from the standard published values
 * (US/EU RDA and AI figures, which agree closely enough at this resolution).
 *
 * These are reference points, not a prescription and not a target the app has
 * calculated for you. The app does not compute a personal requirement from your
 * weight or your training, does not tell you to supplement anything, and treats
 * going over a floor as unremarkable. They are here so that a day's log can be read
 * as "that was low on iron" instead of as a number with nothing to sit against.
 *
 * Anything you actually intend to change about your intake — especially with
 * supplements, where it is easy to reach a dose food never would — is worth putting
 * to a doctor or dietitian rather than to a phone app.
 */
object Nutrients {

    val ALL: List<NutrientRef> = listOf(
        NutrientRef("Fibre", "g", 30.0, decimals = 0) { it.fiberG },
        NutrientRef("Potassium", "mg", 3500.0) { it.potassiumMg },
        NutrientRef("Calcium", "mg", 1000.0) { it.calciumMg },
        NutrientRef("Magnesium", "mg", 400.0) { it.magnesiumMg },
        NutrientRef("Iron", "mg", 8.0, decimals = 1) { it.ironMg },
        NutrientRef("Zinc", "mg", 11.0, decimals = 1) { it.zincMg },
        NutrientRef("Vitamin C", "mg", 90.0) { it.vitCMg },
        NutrientRef(
            "Vitamin D", "mcg", 15.0, decimals = 1,
            note = "600 IU. Food alone rarely gets there."
        ) { it.vitDMcg },
        NutrientRef("Vitamin B12", "mcg", 2.4, decimals = 1) { it.vitB12Mcg },
        NutrientRef("Vitamin A", "mcg", 900.0) { it.vitAMcg },
        NutrientRef("Folate", "mcg", 400.0) { it.folateMcg },
        NutrientRef(
            "Omega-3", "g", 1.6, decimals = 2,
            note = "EPA and DHA from oily fish, or a capsule."
        ) { it.omega3G },
        NutrientRef(
            "Sodium", "mg", 2300.0, direction = Direction.CEILING,
            note = "Easy to pass on a day with pickle, papad and hotel food."
        ) { it.sodiumMg },
        NutrientRef(
            "Saturated fat", "g", 22.0, direction = Direction.CEILING, decimals = 1,
            note = "Coconut oil and ghee are where this comes from."
        ) { it.satFatG },
        NutrientRef("Sugar", "g", 50.0, direction = Direction.CEILING, decimals = 0) { it.sugarG }
    )

    /**
     * How far through the reference a value sits, clamped for drawing. A ceiling is
     * drawn the same way — the bar filling up is the warning.
     */
    fun progress(ref: NutrientRef, micros: Micros): Float =
        MacroMath.ratio(ref.read(micros), ref.reference)

    fun rawProgress(ref: NutrientRef, micros: Micros): Double =
        MacroMath.rawRatio(ref.read(micros), ref.reference)

    /**
     * True when this is worth showing in the error colour: a ceiling that has been
     * passed. Being short of a floor is not an error — it is Tuesday afternoon.
     */
    fun isOver(ref: NutrientRef, micros: Micros): Boolean =
        ref.direction == Direction.CEILING && rawProgress(ref, micros) > 1.0

    /**
     * The floors furthest from being met, worst first, ignoring anything already
     * there. Used to say something specific at the top of the nutrients panel
     * instead of making you scan fifteen bars to find the short one.
     */
    fun shortfalls(micros: Micros, limit: Int = 3): List<NutrientRef> =
        ALL.filter { it.direction == Direction.FLOOR && rawProgress(it, micros) < 0.6 }
            .sortedBy { rawProgress(it, micros) }
            .take(limit)

    fun exceeded(micros: Micros): List<NutrientRef> =
        ALL.filter { it.direction == Direction.CEILING && rawProgress(it, micros) > 1.0 }
            .sortedByDescending { rawProgress(it, micros) }
}
