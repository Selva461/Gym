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
    val isStalled: Boolean = false,
    /**
     * True when this target came from the lift's other variations because the one
     * asked about has no history yet. The rationale says so; the flag lets the UI
     * mark it as a starting point rather than a measurement.
     */
    val isFromOtherVariations: Boolean = false
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
        // Zero help on an assisted machine is a real set — the hardest one there is.
        history.filter { !it.isWarmup && it.reps > 0 && (it.weightKg > 0 || (it.isAssisted && it.weightKg == 0.0)) }
            .groupBy { it.sessionId }
            .values
            .sortedByDescending { session -> session.maxOf { it.loggedAt } }

    /**
     * The hardest weight worked in a session, and the best reps achieved at it: the
     * heaviest on a normal lift, the least help on an assisted one.
     */
    private fun topOf(session: List<SetRow>, assisted: Boolean = false): Pair<Double, Int> {
        val topWeight = if (assisted) session.minOf { it.weightKg } else session.maxOf { it.weightKg }
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
    /**
     * [variation] narrows the history to sets performed the same way — the grip,
     * stance or tempo the next set is going to use.
     *
     * This is the whole reason variations are tracked. A close-grip bench and a
     * wide-grip bench at the same weight are different lifts as far as "what next"
     * is concerned, and sharing one history means twelve reps on the easier grip
     * tells you to add weight to the harder one. That is a recommendation that gets
     * someone stapled to a bench.
     *
     * Unspecified ("") is its own bucket, not a wildcard. Sets logged before
     * variations existed are unspecified, and treating them as matching everything
     * would reintroduce exactly the mixing this exists to prevent.
     *
     * The exception is the first session on a new variation, where a strict reading
     * gives nothing at all. Rather than refuse, it falls back to the lift's other
     * variations and says so in the rationale — a starting point you can see the
     * provenance of beats a blank screen, as long as it does not pretend to be
     * history it is not.
     */
    fun recommend(
        exercise: Exercise,
        history: List<SetRow>,
        variation: String = ""
    ): Recommendation? {
        val forLift = history.filter { it.exerciseId == exercise.id }
        val exact = sessionsNewestFirst(forLift.filter { it.variation == variation })
        val fellBack = exact.isEmpty()
        val sessions = if (fellBack) sessionsNewestFirst(forLift) else exact
        if (sessions.isEmpty()) return null
        if (exercise.isAssisted) return recommendAssisted(exercise, sessions, fellBack)

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

        // Said once and appended to whichever rationale is built below, so the two
        // paths cannot drift apart on how they describe the same fallback.
        val borrowedNote = if (fellBack) {
            " This is from your other work on ${exercise.name} — nothing logged on " +
                "this variation yet, so treat it as a starting point."
        } else {
            ""
        }

        if (bestReps >= repHigh) {
            val next = toHalfKg(topWeight + exercise.incrementKg)
            return Recommendation(
                weightKg = next,
                reps = repLow,
                headline = "${Training.formatWeight(next)} kg × $repLow",
                rationale = "You hit $bestReps at ${Training.formatWeight(topWeight)} kg, " +
                    "the top of this lift's range. Add the $weightWord and build back up to $repHigh." +
                    borrowedNote,
                isStepUp = true,
                isFromOtherVariations = fellBack
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
            rationale = base + stallNote + borrowedNote,
            isStepUp = false,
            isStalled = stalled,
            isFromOtherVariations = fellBack
        )
    }

    /**
     * The same double progression run backwards, for a machine where the weight
     * helps. Hit the top of the range and the *help* comes down by the lift's
     * increment; otherwise the help stays and one more rep is the target. At zero
     * help the next step is the lift itself, and the rationale says so.
     */
    private fun recommendAssisted(
        exercise: Exercise,
        sessions: List<List<SetRow>>,
        fellBack: Boolean
    ): Recommendation {
        val (help, bestReps) = topOf(sessions.first(), assisted = true)
        val repHigh = exercise.repHigh.coerceAtLeast(1)
        val repLow = exercise.repLow.coerceIn(1, repHigh)
        val w = Training::formatWeight

        val lastThree = sessions.take(3).map { topOf(it, assisted = true) }
        val stalled = lastThree.size == 3 &&
            lastThree.map { it.first }.distinct().size == 1 &&
            lastThree[0].second <= lastThree[2].second

        val borrowedNote = if (fellBack) {
            " This is from your other work on ${exercise.name} — nothing logged on " +
                "this variation yet, so treat it as a starting point."
        } else {
            ""
        }

        if (bestReps >= repHigh) {
            val next = toHalfKg((help - exercise.incrementKg).coerceAtLeast(0.0))
            val rationale = if (help <= 0.0) {
                "$bestReps reps with no help at all — that is the unassisted lift. " +
                    "Time to log it as the real thing."
            } else {
                "You hit $bestReps with ${w(help)} kg of help, the top of this lift's range. " +
                    "On this machine more weight is easier, so take ${w(help - next)} kg of help " +
                    "off and build back up to $repHigh." +
                    if (next == 0.0) " That is no help at all — the lift itself." else ""
            }
            return Recommendation(
                weightKg = next,
                reps = repLow,
                headline = "${w(next)} kg help × $repLow",
                rationale = rationale + borrowedNote,
                isStepUp = true,
                isFromOtherVariations = fellBack
            )
        }

        val targetReps = (bestReps + 1).coerceAtMost(repHigh)
        val base = "Last time $bestReps with ${w(help)} kg of help. One more rep, same help — " +
            "at $repHigh the help comes down."
        val stallNote = if (stalled) {
            " Third session here without the reps moving; if it holds again, " +
                "${w(toHalfKg(help + exercise.incrementKg))} kg of help and a run back up is the usual way out."
        } else {
            ""
        }
        return Recommendation(
            weightKg = help,
            reps = targetReps,
            headline = "${w(help)} kg help × $targetReps",
            rationale = base + stallNote + borrowedNote,
            isStepUp = false,
            isStalled = stalled,
            isFromOtherVariations = fellBack
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
