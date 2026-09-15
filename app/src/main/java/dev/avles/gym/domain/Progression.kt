package dev.avles.gym.domain

import dev.avles.gym.data.Exercise
import dev.avles.gym.data.SetRow
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * What to try next on a lift, and why.
 *
 * [headline] is the target itself, short enough to read between two sets.
 * [rationale] is the sentence underneath explaining where it came from, because a
 * number you do not believe is a number you will ignore.
 */
data class Recommendation(
    val weightKg: Double,
    val reps: Int,
    val headline: String,
    val rationale: String,
    val isStepUp: Boolean,
    val isStalled: Boolean = false
)

/**
 * Double progression, which is the least clever scheme that works.
 *
 * Every lift has a rep range. You stay at one weight until you can hit the top of
 * the range, then the weight goes up and the reps reset to the bottom. That is the
 * whole rule. It is slow on purpose: it asks for one more rep than last time rather
 * than a number someone's spreadsheet thinks you should be capable of, and the only
 * input is what you actually logged.
 *
 * Nothing here is computed from your age, weight or "level". This is your own
 * history read back to you with one rep or one increment added.
 */
object Progression {

    /**
     * The working sets of the most recent session that included this lift, newest
     * session first. Warm-ups are excluded — they are effort, not stimulus, and
     * progressing off them would suggest a weight you never actually worked at.
     */
    fun sessionsNewestFirst(history: List<SetRow>): List<List<SetRow>> =
        history.filter { !it.isWarmup && it.reps > 0 && it.weightKg > 0 }
            .groupBy { it.sessionId }
            .values
            .sortedByDescending { session -> session.maxOf { it.loggedAt } }

    /** Heaviest weight worked in a session, and the best reps achieved at it. */
    private fun topOf(session: List<SetRow>): Pair<Double, Int> {
        val topWeight = session.maxOf { it.weightKg }
        val bestReps = session.filter { it.weightKg == topWeight }.maxOf { it.reps }
        return topWeight to bestReps
    }

    /** Kilos land on something loadable rather than on 83.7. */
    private fun toHalfKg(kg: Double): Double = (kg * 2).roundToInt() / 2.0

    /**
     * Null when there is no working history for this lift — the UI says so rather
     * than inventing a starting weight, because the app has no business guessing
     * what you can lift before you have shown it once.
     */
    fun recommend(exercise: Exercise, history: List<SetRow>): Recommendation? {
        val sessions = sessionsNewestFirst(history.filter { it.exerciseId == exercise.id })
        if (sessions.isEmpty()) return null

        val (topWeight, bestReps) = topOf(sessions.first())
        val repHigh = exercise.repHigh.coerceAtLeast(1)
        val repLow = exercise.repLow.coerceIn(1, repHigh)
        val weightWord = if (exercise.isBodyweight) "load" else "weight"

        // Three sessions at the same top weight without the reps moving up is a stall.
        // Worth naming, because the fix is a decision only you can make.
        val lastThree = sessions.take(3).map { topOf(it) }
        val stalled = lastThree.size == 3 &&
            lastThree.map { it.first }.distinct().size == 1 &&
            lastThree[0].second <= lastThree[2].second

        if (bestReps >= repHigh) {
            val next = toHalfKg(topWeight + exercise.incrementKg)
            return Recommendation(
                weightKg = next,
                reps = repLow,
                headline = "${Training.formatWeight(next)} kg × $repLow",
                rationale = "You hit $bestReps at ${Training.formatWeight(topWeight)} kg, " +
                    "the top of this lift's range. Add the $weightWord and build back up to $repHigh.",
                isStepUp = true
            )
        }

        val targetReps = (bestReps + 1).coerceAtMost(repHigh)
        val base = "Last time $bestReps at ${Training.formatWeight(topWeight)} kg. " +
            "One more rep, same $weightWord — at $repHigh it goes up."
        val stallNote = if (stalled) {
            " Third session here without the reps moving; if it holds again, " +
                "${Training.formatWeight(toHalfKg(topWeight * 0.9))} kg and a run back up is the usual way out."
        } else {
            ""
        }

        return Recommendation(
            weightKg = topWeight,
            reps = targetReps,
            headline = "${Training.formatWeight(topWeight)} kg × $targetReps",
            rationale = base + stallNote,
            isStepUp = false,
            isStalled = stalled
        )
    }

    /**
     * The next round number above your current estimated max — the thing that is
     * actually motivating, as opposed to "your e1RM is 92.4".
     */
    fun nextMilestone(estimatedOneRepMax: Double, step: Double = 5.0): Double {
        if (estimatedOneRepMax <= 0.0 || step <= 0.0) return 0.0
        val next = ceil(estimatedOneRepMax / step) * step
        return if (next <= estimatedOneRepMax) next + step else next
    }

    /**
     * How far through the gap between the last round number and the next one you
     * are, for a progress bar. Deliberately not a percentage of some target you
     * never set.
     */
    fun milestoneProgress(estimatedOneRepMax: Double, step: Double = 5.0): Float {
        if (estimatedOneRepMax <= 0.0 || step <= 0.0) return 0f
        val next = nextMilestone(estimatedOneRepMax, step)
        val previous = next - step
        return ((estimatedOneRepMax - previous) / step).coerceIn(0.0, 1.0).toFloat()
    }
}
