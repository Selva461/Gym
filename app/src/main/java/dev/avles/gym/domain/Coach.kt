package dev.avles.gym.domain

import dev.avles.gym.data.Exercise
import dev.avles.gym.data.SetRow
import kotlin.math.roundToInt

/**
 * Whether the training you are actually doing serves the look you are after, and
 * what is missing if it does not.
 *
 * The whole thing is arithmetic over the log plus [ExerciseGuide]'s muscle data.
 * It reads no profile, asks no questions about your body, and predicts nothing.
 * It answers one question — "which muscles are under the weekly sets this goal
 * asks for" — and then names lifts that would fix it.
 *
 * That restraint is the design. An app that tells you what you will look like in
 * twelve weeks is guessing; an app that tells you that you did three sets for side
 * delts last week against a target of eighteen is reading your own log back to you,
 * and that is both true and actionable.
 */
object Coach {

    /**
     * A set counts fully for what the lift is *for*, and half for what it also
     * works. A cable row is back volume; the biceps work it gives you is real but
     * is not the same stimulus as a curl, and counting it whole would report arms
     * as covered when they are not.
     */
    private const val SECONDARY_WEIGHT = 0.5

    /** The window. Seven days is the unit the targets are expressed in. */
    const val WINDOW_DAYS = 7

    data class MuscleWeek(
        val muscle: Muscle,
        val sets: Double,
        val target: Int
    ) {
        val shortfall: Double get() = (target - sets).coerceAtLeast(0.0)
        /** 1.0 means on target. Capped so a muscle at triple target does not dominate a sort. */
        val ratio: Float get() = if (target <= 0) 1f else (sets / target).coerceAtMost(2.0).toFloat()
        val onTarget: Boolean get() = target <= 0 || sets >= target * 0.85
    }

    /**
     * A lift worth adding, and the muscle it would fix.
     *
     * [reason] is the muscle's shortfall in plain terms. A recommendation whose
     * reason you cannot see is one you have to take on trust, and this app has not
     * earned that and does not need it — the reason is in your own log.
     */
    data class Suggestion(
        val exercise: Exercise,
        val forMuscle: Muscle,
        val reason: String
    )

    /**
     * Working sets per muscle over the last [WINDOW_DAYS] days.
     *
     * Warm-ups are excluded for the same reason they are excluded everywhere else:
     * they are effort, not stimulus, and counting them would report a week as
     * harder than it was.
     */
    fun weeklySets(
        history: List<SetRow>,
        sessionDayOf: (Long) -> Long?,
        guideFor: (Long) -> ExerciseGuide?,
        today: Long,
        windowDays: Int = WINDOW_DAYS
    ): Map<Muscle, Double> {
        val since = today - (windowDays - 1)
        val counts = mutableMapOf<Muscle, Double>()
        history.asSequence()
            .filter { !it.isWarmup && it.reps > 0 }
            .filter { row -> sessionDayOf(row.sessionId)?.let { it in since..today } == true }
            .forEach { row ->
                val guide = guideFor(row.exerciseId) ?: return@forEach
                guide.primary.forEach { counts.merge(it, 1.0, Double::plus) }
                guide.secondary.forEach { counts.merge(it, SECONDARY_WEIGHT, Double::plus) }
            }
        return counts
    }

    /**
     * Every muscle the goal asks for, with what you actually did. Sorted worst
     * first, because the top of the list is the only part anyone reads.
     */
    fun review(physique: Physique, actual: Map<Muscle, Double>): List<MuscleWeek> =
        physique.weeklySets
            .map { (muscle, target) -> MuscleWeek(muscle, actual[muscle] ?: 0.0, target) }
            .sortedWith(compareBy({ it.ratio }, { -it.target }))

    /**
     * Lifts that would close the biggest gaps.
     *
     * Only the muscles a lift is *for* are considered here, not what it also
     * works — a suggestion has to actually fix the thing it claims to fix, and
     * "do rows, they hit biceps a bit" is how people end up with arms that never
     * grow. Lifts already trained this week are skipped: the answer to three sets
     * of lateral raises is more of them, which the shortfall already says, and
     * repeating the lift you just did is not a suggestion.
     */
    fun suggestions(
        review: List<MuscleWeek>,
        catalogue: List<Exercise>,
        guideFor: (Exercise) -> ExerciseGuide,
        trainedThisWeek: Set<Long> = emptySet(),
        perMuscle: Int = 3,
        muscles: Int = 3
    ): List<Suggestion> {
        val gaps = review.filterNot { it.onTarget }.take(muscles)
        return gaps.flatMap { week ->
            catalogue.asSequence()
                .filterNot { it.id in trainedThisWeek }
                .filter { week.muscle in guideFor(it).primary }
                .take(perMuscle)
                .map {
                    Suggestion(
                        exercise = it,
                        forMuscle = week.muscle,
                        reason = "${week.muscle.label}: ${format(week.sets)} of " +
                            "${week.target} sets this week"
                    )
                }
                .toList()
        }
    }

    /**
     * One line on whether the week served the goal.
     *
     * Deliberately not a score. A number out of a hundred invites optimising the
     * number, and the number would be measuring how closely you matched a set of
     * targets this app made up rather than whether you are getting closer to what
     * you want.
     */
    fun verdict(review: List<MuscleWeek>): String {
        val tracked = review.filter { it.target > 0 }
        if (tracked.isEmpty()) return "No goal set."
        val short = tracked.filterNot { it.onTarget }
        val logged = tracked.sumOf { it.sets }
        return when {
            logged < 1.0 -> "Nothing logged in the last week, so there is nothing to read yet."
            short.isEmpty() -> "Every muscle this goal asks for is at or near its weekly target."
            short.size <= 2 ->
                "Close. ${short.joinToString(" and ") { it.muscle.label.lowercase() }} " +
                    "${if (short.size == 1) "is" else "are"} short this week."
            else ->
                "${short.size} of ${tracked.size} muscles are under target this week, " +
                    "starting with ${short.first().muscle.label.lowercase()}."
        }
    }

    private fun format(sets: Double): String =
        if (sets % 1.0 == 0.0) sets.roundToInt().toString() else String.format("%.1f", sets)
}
