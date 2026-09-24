package dev.avles.gym.domain


/**
 * What your weeks actually look like, as opposed to what you think they look like.
 *
 * Totals and streaks say how much. This says *when* — and when is where the
 * fixable patterns are. "I train four times a week" and "I never train on a
 * Friday" are both true at once, and only the second one tells you what to change.
 *
 * Everything here is arithmetic over days you logged. Nothing is predicted and
 * nothing is scored. A day with no data is absent rather than zero, because a day
 * you forgot to log and a day you ate nothing are different things and averaging
 * them together quietly invents a number.
 */
object Patterns {

    /** Monday-first, which is how a training week is usually planned. */
    private val DAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    /**
     * One weekday across the whole history.
     *
     * [samples] is how many of that weekday have occurred in the window, and it is
     * the number that decides whether the rest is worth reading. Two Fridays is not
     * a pattern, and a screen that presents 0% off two samples as a finding is
     * inviting somebody to reorganise their week around noise.
     */
    data class DayOfWeek(
        val index: Int,
        val label: String,
        val samples: Int,
        val trainedDays: Int,
        val loggedFoodDays: Int,
        val meanKcal: Double,
        val meanProteinG: Double
    ) {
        val trainedRate: Float get() = if (samples == 0) 0f else trainedDays / samples.toFloat()
        val loggedRate: Float get() = if (samples == 0) 0f else loggedFoodDays / samples.toFloat()
        /** Below this, say so rather than drawing a conclusion. */
        val enoughToRead: Boolean get() = samples >= 3
    }

    /** Monday is 0. Epoch day 0, 1970-01-01, was a Thursday. */
    fun weekdayIndex(epochDay: Long): Int = (((epochDay + 3) % 7 + 7) % 7).toInt()

    /**
     * Every weekday over [windowDays] ending at [today], with how often you trained
     * and ate on it.
     *
     * Days are counted from the calendar rather than from the log, so a weekday you
     * never once trained still appears with a rate of zero. That absence is the
     * finding; omitting it would hide the thing worth seeing.
     */
    fun byWeekday(
        today: Long,
        windowDays: Int,
        trained: Set<Long>,
        macrosByDay: Map<Long, DayMacros>
    ): List<DayOfWeek> {
        val days = (0 until windowDays).map { today - it }
        return (0..6).map { index ->
            val matching = days.filter { weekdayIndex(it) == index }
            val fed = matching.mapNotNull { macrosByDay[it] }
            DayOfWeek(
                index = index,
                label = DAY_LABELS[index],
                samples = matching.size,
                trainedDays = matching.count { it in trained },
                loggedFoodDays = fed.size,
                meanKcal = fed.map { it.kcal }.meanOrZero(),
                meanProteinG = fed.map { it.proteinG }.meanOrZero()
            )
        }
    }

    /** A day's eating, as the charts need it. */
    data class DayMacros(val dateEpochDay: Long, val kcal: Double, val proteinG: Double)

    /**
     * A value per day over the window, with days you did not log left as null.
     *
     * Null rather than zero, deliberately. A chart that plots an unlogged day as
     * zero calories draws a cliff that never happened, and the line it produces is
     * a picture of your logging habit rather than your eating.
     */
    fun series(
        today: Long,
        windowDays: Int,
        macrosByDay: Map<Long, DayMacros>,
        pick: (DayMacros) -> Double
    ): List<Double?> =
        (windowDays - 1 downTo 0).map { back -> macrosByDay[today - back]?.let(pick) }

    /**
     * Mean over a trailing window, ignoring days with no entry.
     *
     * Seven days smooths out the difference between a Tuesday and a Sunday, which
     * is the difference this is trying to see past. Null until there is at least one
     * real value in range.
     */
    fun rollingMean(values: List<Double?>, window: Int = 7): List<Double?> =
        values.indices.map { i ->
            val from = (i - window + 1).coerceAtLeast(0)
            values.subList(from, i + 1).filterNotNull().takeIf { it.isNotEmpty() }?.average()
        }

    /**
     * How often a logged day landed within [tolerance] of target, as a fraction of
     * days you actually logged.
     *
     * Of days logged, not of days elapsed. Dividing by the calendar would mix two
     * different failures — eating badly and not writing it down — into one number
     * that cannot tell you which to fix.
     */
    fun adherence(values: List<Double?>, target: Double, tolerance: Double = 0.15): Float {
        if (target <= 0.0) return 0f
        val logged = values.filterNotNull()
        if (logged.isEmpty()) return 0f
        val within = logged.count { kotlin.math.abs(it - target) / target <= tolerance }
        return within / logged.size.toFloat()
    }

    /**
     * The weekday you train least, when there is enough of it to say so.
     *
     * Null when no weekday has three samples or when nothing stands out — the point
     * is to name a gap worth closing, not to always have something to say.
     */
    fun weakestWeekday(stats: List<DayOfWeek>): DayOfWeek? {
        val readable = stats.filter { it.enoughToRead }
        if (readable.size < 4) return null
        val worst = readable.minByOrNull { it.trainedRate } ?: return null
        val best = readable.maxByOrNull { it.trainedRate } ?: return null
        // A flat week is not a finding.
        return if (best.trainedRate - worst.trainedRate >= 0.34f) worst else null
    }

    private fun List<Double>.meanOrZero(): Double = if (isEmpty()) 0.0 else average()
}
