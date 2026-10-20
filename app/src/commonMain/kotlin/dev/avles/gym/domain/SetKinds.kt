package dev.avles.gym.domain

import dev.avles.gym.data.SetRow

/**
 * The set types the logging apps offer beyond warm-up and working: a drop set (the
 * weight stripped straight after a set, no rest) and a set taken to failure. Plus
 * RPE, how hard the set felt on the 6 to 10 scale lifters use, where 10 is nothing
 * left and 8 is two reps in the tank.
 */
object SetKinds {
    const val DROP = "drop"
    const val FAILURE = "failure"

    /** What the set-type picker offers, with what each one stores. Warm-up is its own flag. */
    val PICKER = listOf("Working" to "", "Warm-up" to "", "Drop set" to DROP, "To failure" to FAILURE)

    val RPE_CHOICES = listOf(6.0, 7.0, 7.5, 8.0, 8.5, 9.0, 9.5, 10.0)

    fun label(kind: String): String = when (kind) {
        DROP -> "drop set"
        FAILURE -> "to failure"
        else -> ""
    }

    /** "8.5" or "10": RPE is written with a half only when it has one. */
    fun rpeLabel(rpe: Double): String = if (rpe % 1.0 == 0.0) rpe.toInt().toString() else Numbers.fixed(rpe, 1)

    /** The extra words under a set in a list: "drop set · RPE 8". Empty for a plain set. */
    fun describe(row: SetRow): String = listOfNotNull(
        label(row.kind).takeIf { it.isNotEmpty() },
        if (row.rpe > 0) "RPE ${rpeLabel(row.rpe)}" else null
    ).joinToString(" · ")
}
