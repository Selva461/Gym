package dev.avles.gym.domain

/**
 * Two lifts done back to back, then a rest: the superset of every logging app.
 * After a set of the first lift the app moves straight to the second with no rest;
 * after the second it rests and comes back to the first.
 */
object Supersets {

    data class Pair(val first: Long, val second: Long)

    /** What follows a logged set: the lift to show next, and whether to rest first. */
    data class Next(val exerciseId: Long, val rest: Boolean)

    /** Null when [loggedExerciseId] is not part of [pair], so the usual flow applies. */
    fun after(pair: Pair?, loggedExerciseId: Long): Next? = when {
        pair == null -> null
        loggedExerciseId == pair.first -> Next(pair.second, rest = false)
        loggedExerciseId == pair.second -> Next(pair.first, rest = true)
        else -> null
    }
}
