package dev.avles.gym.domain

/**
 * What you are training *for*, expressed as weekly sets per muscle.
 *
 * The unit is the weekly hard set, because that is the one training variable with
 * a consistent relationship to growth and the one you can count off a log without
 * guessing. Ten to twenty sets a week per muscle is where most people grow; below
 * about six a muscle holds rather than gains. The numbers below sit in that band
 * and are weighted towards the look each goal describes.
 *
 * A goal is **data**, not code. Adding one is a [Physique] in [Physiques.ALL] — no
 * new logic, no new screen, nothing to wire. That is deliberate: goals are the part
 * of this most likely to change, and the part most likely to want personalising.
 *
 * [notes] is not decoration. Every one of these goals has something true about it
 * that the numbers cannot express, and an app that recommends lateral raises for a
 * six-pack without mentioning body fat is telling a comfortable lie.
 */
data class Physique(
    val id: String,
    val label: String,
    val summary: String,
    /** Weekly working sets per muscle. Anything absent is "maintain, do not chase". */
    val weeklySets: Map<Muscle, Int>,
    /** What the numbers cannot say. Shown with the plan, not buried. */
    val notes: List<String> = emptyList()
) {
    fun targetFor(muscle: Muscle): Int = weeklySets[muscle] ?: 0
}

object Physiques {

    /**
     * Broad shoulders, visible abs, a waist that looks small by comparison.
     *
     * The shape is a ratio, not a size, and the two things that move it most are
     * side delts and lats — width at the top. Upper traps are deliberately held
     * low: heavy shrugging fills the gap between neck and shoulder and makes the
     * same shoulders read *narrower*. That is the least obvious thing in this file
     * and the most useful.
     */
    val V_TAPER = Physique(
        id = "v-taper",
        label = "Six-pack and broad shoulders",
        summary = "Width at the shoulders, a lean middle, and legs that keep up.",
        weeklySets = mapOf(
            Muscle.DELT_SIDE to 18,
            Muscle.LATS to 16,
            Muscle.ABS to 14,
            Muscle.OBLIQUES to 10,
            Muscle.CHEST_UPPER to 12,
            Muscle.CHEST_MID to 8,
            Muscle.DELT_REAR to 10,
            Muscle.DELT_FRONT to 6,
            Muscle.RHOMBOIDS to 8,
            Muscle.TRICEPS to 10,
            Muscle.BICEPS to 10,
            Muscle.QUADS to 10,
            Muscle.HAMSTRINGS to 10,
            Muscle.GLUTES to 10,
            Muscle.CALVES_GASTROC to 8,
            Muscle.ERECTORS to 6,
            // Held low on purpose. See the class note above.
            Muscle.TRAPS_UPPER to 4,
            Muscle.TRAPS_MID to 8
        ),
        notes = listOf(
            "Abs are built in the gym and revealed in the kitchen. Most people " +
                "already have the muscle at 20% body fat and cannot see it. Direct " +
                "ab work thickens the muscle; it does not uncover it. The Fuel tab " +
                "does more for this goal than any exercise here.",
            "Side delts and lats are what make shoulders look broad, because the " +
                "look is a ratio to your waist rather than an absolute width.",
            "Heavy upper-trap work fills the line from neck to shoulder and makes " +
                "the same frame read narrower. That is why the target here is low.",
            "Legs are in the plan at maintenance or better. A wide top on thin legs " +
                "does not read as fit, it reads as a costume."
        )
    )

    /** Nothing neglected. The sensible default when you have no particular look in mind. */
    val BALANCED = Physique(
        id = "balanced",
        label = "All-round fitness",
        summary = "Everything trained enough to grow, nothing chased.",
        weeklySets = Muscle.values()
            .filterNot { it in setOf(Muscle.GRIP, Muscle.SERRATUS, Muscle.HIP_FLEXORS) }
            .associateWith { 10 },
        notes = listOf(
            "Even coverage is the right starting point. Pick a more specific goal " +
                "once you know what you actually want to change."
        )
    )

    /** Squat, bench, deadlift and the muscles that move them. */
    val STRENGTH = Physique(
        id = "strength",
        label = "Strength on the big lifts",
        summary = "The three lifts, and the muscles that carry them.",
        weeklySets = mapOf(
            Muscle.QUADS to 16, Muscle.GLUTES to 14, Muscle.HAMSTRINGS to 12,
            Muscle.ERECTORS to 12, Muscle.CHEST_MID to 14, Muscle.TRICEPS to 12,
            Muscle.DELT_FRONT to 10, Muscle.LATS to 12, Muscle.TRAPS_MID to 10,
            Muscle.RHOMBOIDS to 8, Muscle.ABS to 10, Muscle.BICEPS to 6,
            Muscle.DELT_SIDE to 6, Muscle.GRIP to 6, Muscle.CALVES_GASTROC to 6
        ),
        notes = listOf(
            "Strength comes from heavy sets in low rep ranges. The set counts here " +
                "assume that; twenty easy sets is not the same stimulus.",
            "Progress on these is measured in kilos on the bar, not in the mirror."
        )
    )

    val ALL = listOf(V_TAPER, BALANCED, STRENGTH)

    fun byId(id: String): Physique = ALL.firstOrNull { it.id == id } ?: BALANCED
}
