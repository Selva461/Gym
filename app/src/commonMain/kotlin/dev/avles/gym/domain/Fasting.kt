package dev.avles.gym.domain

/**
 * The intermittent-fasting timer the diet apps carry: when the fast started, how
 * long it is meant to run, how far along it is. Pure arithmetic over two
 * timestamps; the start lives in settings so the timer survives the app closing.
 */
object Fasting {

    /** The common schedules, as hours fasted out of twenty-four. */
    val PLANS = listOf(12, 14, 16, 18, 20)

    data class State(val elapsedSeconds: Long, val targetSeconds: Long) {
        val fraction: Float get() = if (targetSeconds <= 0) 0f else (elapsedSeconds.toFloat() / targetSeconds).coerceIn(0f, 1f)
        val isDone: Boolean get() = elapsedSeconds >= targetSeconds
        val remainingSeconds: Long get() = (targetSeconds - elapsedSeconds).coerceAtLeast(0)
    }

    /** Null when no fast is running. A start in the future (a clock change) counts as zero elapsed. */
    fun state(startedAtMillis: Long, hours: Int, nowMillis: Long): State? {
        if (startedAtMillis <= 0L || hours <= 0) return null
        val elapsed = ((nowMillis - startedAtMillis) / 1000).coerceAtLeast(0)
        return State(elapsed, hours * 3600L)
    }

    /** "15:42" as hours and minutes; fasts run past a day often enough to need more than two digits. */
    fun hoursMinutes(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return "$h:${m.toString().padStart(2, '0')}"
    }
}
