package dev.avles.gym.domain

import dev.avles.gym.data.SetRow
import kotlin.math.floor

/**
 * Lifts where the weight on the stack helps rather than resists.
 *
 * On an assisted pull-up or dip machine the plate you pin is a counterweight under
 * your knees: 40 kg takes 40 kg off your bodyweight, so more kilos is *easier*.
 * Every rule elsewhere in the app assumes the opposite — add weight to progress,
 * heavier is a record, weight × reps is work done — and applied here each one
 * points the wrong way: the coach would tell you to add help, a lazy set with the
 * whole stack pinned would be your best ever, and it would count as tonnes lifted.
 *
 * So these lifts are flagged ([dev.avles.gym.data.Exercise.isAssisted]) and the
 * direction flips: progress is taking weight *off*, a record is doing as many reps
 * with less help than ever before, and the help is never counted as volume — the
 * same way a bodyweight lift's bodyweight is not.
 */
object Assistance {

    /**
     * How a lift is recognised as assisted when it is created: by name, the same
     * way [ExerciseGuide] reads equipment from the name. "Assisted pull-up machine",
     * "Assisted dip", "Machine-assisted chin-up" all say so; nothing else does.
     */
    fun looksAssisted(name: String): Boolean = name.contains("assisted", ignoreCase = true)

    /**
     * A record on an assisted lift: no earlier set used as little help for as many
     * reps. Put another way, nothing already logged is at least as hard on both
     * counts. Twelve reps with 30 kg of help beats ten with 30; eight with 25 beats
     * ten with 30, because it took less help; twelve with 35 does not beat ten with
     * 30 unless nothing harder has been done at twelve.
     *
     * Deliberately free of bodyweight: an estimated max needs it, and a record that
     * moves when the scale does is not a record of the lift.
     */
    fun isRecord(candidate: SetRow, history: List<SetRow>): Boolean {
        if (candidate.isWarmup || candidate.reps <= 0 || candidate.weightKg < 0) return false
        return history
            .filter {
                it.setId != candidate.setId &&
                    it.exerciseId == candidate.exerciseId &&
                    it.variation == candidate.variation &&
                    !it.isWarmup && it.reps > 0
            }
            .none { earlier -> earlier.weightKg <= candidate.weightKg && earlier.reps >= candidate.reps }
    }

    /** The least help ever used on a working set, or null with no history. */
    fun leastHelp(sets: List<SetRow>): Double? =
        sets.filter { !it.isWarmup && it.reps > 0 && it.weightKg >= 0 }.minOfOrNull { it.weightKg }

    /**
     * The next round number below the least help used — the assisted lift's
     * equivalent of the next round number above an estimated max. Zero means the
     * next step is the unassisted lift itself.
     */
    fun nextMilestone(leastHelp: Double, step: Double = 5.0): Double {
        if (leastHelp <= 0.0 || step <= 0.0) return 0.0
        val next = floor(leastHelp / step) * step
        return (if (next >= leastHelp) next - step else next).coerceAtLeast(0.0)
    }

    /** How far through the current step down you are, for the same progress bar. */
    fun milestoneProgress(leastHelp: Double, step: Double = 5.0): Float {
        if (step <= 0.0) return 0f
        if (leastHelp <= 0.0) return 1f
        val next = nextMilestone(leastHelp, step)
        val previous = next + step
        return ((previous - leastHelp) / step).coerceIn(0.0, 1.0).toFloat()
    }
}
