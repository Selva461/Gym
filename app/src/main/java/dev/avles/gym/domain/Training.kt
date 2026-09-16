package dev.avles.gym.domain

import dev.avles.gym.data.SetRow
import java.util.Locale
import kotlin.math.roundToInt

object Training {

    /** Working volume only. Warm-ups are effort, not stimulus, so they stay out. */
    fun volume(sets: List<SetRow>): Double =
        sets.filter { !it.isWarmup }.sumOf { it.weightKg * it.reps }

    /** Epley estimate. Loses accuracy above roughly ten reps; that is expected. */
    fun estimatedOneRepMax(weightKg: Double, reps: Int): Double {
        if (reps <= 0 || weightKg <= 0.0) return 0.0
        if (reps == 1) return weightKg
        return weightKg * (1 + reps / 30.0)
    }

    fun bestEstimatedOneRepMax(sets: List<SetRow>): Double =
        sets.filter { !it.isWarmup }
            .maxOfOrNull { estimatedOneRepMax(it.weightKg, it.reps) } ?: 0.0

    /**
     * True when this set beats every earlier working set on the same lift *performed
     * the same way* by estimated 1RM. History is expected newest-first but order does
     * not matter.
     *
     * Matching the variation is the point. A close-grip best and a wide-grip best are
     * separate records, and letting one erase the other means the easier variation
     * silently owns the lift — after which the record marker stops appearing on the
     * harder one no matter how well it goes.
     *
     * Unspecified is its own bucket here too, for the same reason it is in
     * [Progression]: sets logged before variations existed record no grip, and
     * ranking them against a grip they may or may not have used would make the
     * marker mean nothing.
     */
    fun isPersonalRecord(candidate: SetRow, history: List<SetRow>): Boolean {
        if (candidate.isWarmup) return false
        val candidateScore = estimatedOneRepMax(candidate.weightKg, candidate.reps)
        if (candidateScore <= 0.0) return false
        val best = history
            .filter {
                it.setId != candidate.setId &&
                    it.exerciseId == candidate.exerciseId &&
                    it.variation == candidate.variation &&
                    !it.isWarmup
            }
            .maxOfOrNull { estimatedOneRepMax(it.weightKg, it.reps) } ?: 0.0
        return candidateScore > best
    }

    /** Next set number for an exercise inside the session currently open. */
    fun nextSetIndex(sessionSets: List<SetRow>, exerciseId: Long): Int =
        sessionSets.count { it.exerciseId == exerciseId } + 1

    /**
     * Always a dot, never a comma. The weight dialog seeds its text field from this
     * and then parses it back, and its input filter only keeps digits and a dot — so
     * a locale that formats 62.5 as "62,5" would round-trip it into 625 kg.
     */
    fun formatWeight(kg: Double): String =
        if (kg % 1.0 == 0.0) kg.roundToInt().toString() else String.format(Locale.US, "%.1f", kg)
}
