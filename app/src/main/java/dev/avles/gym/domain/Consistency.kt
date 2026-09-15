package dev.avles.gym.domain

object Consistency {

    /**
     * Days logged in an unbroken run ending today, or ending yesterday if today
     * is still empty. Not logging before bed should not wipe the run.
     */
    fun currentStreak(loggedDays: Set<Long>, today: Long): Int {
        if (loggedDays.isEmpty()) return 0
        var cursor = if (loggedDays.contains(today)) today else today - 1
        if (!loggedDays.contains(cursor)) return 0
        var count = 0
        while (loggedDays.contains(cursor)) {
            count++
            cursor--
        }
        return count
    }

    fun longestStreak(loggedDays: Set<Long>): Int {
        if (loggedDays.isEmpty()) return 0
        val sorted = loggedDays.sorted()
        var best = 1
        var run = 1
        for (i in 1 until sorted.size) {
            if (sorted[i] == sorted[i - 1] + 1) run++ else run = 1
            if (run > best) best = run
        }
        return best
    }

    /** How many of the last [window] days have an entry, today included. */
    fun daysHitIn(loggedDays: Set<Long>, today: Long, window: Int): Int =
        (0 until window).count { loggedDays.contains(today - it) }
}
