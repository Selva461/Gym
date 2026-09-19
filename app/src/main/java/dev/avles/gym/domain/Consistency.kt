package dev.avles.gym.domain

object Consistency {

    /**
     * Days logged in an unbroken run ending today, or ending yesterday if today
     * is still empty. Not logging before bed should not wipe the run.
     *
     * [bridgedDays] are days that neither count nor break — a planned rest day.
     * That distinction is the whole reason rest days are worth marking: recovery
     * is part of a training programme, not an absence from one, and a streak that
     * punishes you for taking a scheduled day off teaches you to train through it.
     *
     * A missed day is deliberately *not* bridged. It is recorded so the gap in the
     * chart has a reason attached three months later, but a run that survives
     * everything measures nothing. The app states a break as a restart rather than
     * a loss; that is where the kindness belongs, not in the arithmetic.
     */
    fun currentStreak(
        loggedDays: Set<Long>,
        today: Long,
        bridgedDays: Set<Long> = emptySet()
    ): Int {
        if (loggedDays.isEmpty()) return 0
        // Today being a rest day should not end the run, so walk past it too.
        var cursor = today
        while (cursor > today - 2 && !loggedDays.contains(cursor) && bridgedDays.contains(cursor)) {
            cursor--
        }
        if (!loggedDays.contains(cursor)) cursor--
        if (!loggedDays.contains(cursor) && !bridgedDays.contains(cursor)) return 0
        var count = 0
        while (loggedDays.contains(cursor) || bridgedDays.contains(cursor)) {
            if (loggedDays.contains(cursor)) count++
            cursor--
        }
        return count
    }

    /** [bridgedDays] join two runs without adding to either, exactly as above. */
    fun longestStreak(loggedDays: Set<Long>, bridgedDays: Set<Long> = emptySet()): Int {
        if (loggedDays.isEmpty()) return 0
        val sorted = loggedDays.sorted()
        var best = 1
        var run = 1
        for (i in 1 until sorted.size) {
            val gap = (sorted[i - 1] + 1) until sorted[i]
            val joined = gap.all { bridgedDays.contains(it) }
            run = if (sorted[i] == sorted[i - 1] + 1 || joined) run + 1 else 1
            if (run > best) best = run
        }
        return best
    }

    /** How many of the last [window] days have an entry, today included. */
    fun daysHitIn(loggedDays: Set<Long>, today: Long, window: Int): Int =
        (0 until window).count { loggedDays.contains(today - it) }
}
