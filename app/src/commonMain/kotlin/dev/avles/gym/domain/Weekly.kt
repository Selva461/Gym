package dev.avles.gym.domain

/**
 * This week against last week, the summary the big apps lead with on a Monday:
 * days trained, volume moved, and how protein and calories averaged on the days
 * food was logged.
 *
 * Weeks are rolling — the last seven days and the seven before — rather than
 * calendar weeks, so the comparison is fair on a Wednesday as well as a Sunday.
 * Averages skip unlogged days, the same rule [Patterns] follows: a day nobody
 * wrote down is a gap, not a zero.
 */
object Weekly {

    data class Week(
        val daysTrained: Int,
        val volumeKg: Double,
        val avgProteinG: Double?,
        val avgKcal: Double?,
        val daysLogged: Int
    )

    data class Comparison(val thisWeek: Week, val lastWeek: Week)

    /**
     * [protein] and [kcal] are daily series ending today, oldest first, null where
     * nothing was logged — the shape [Patterns.series] returns.
     */
    fun compare(
        today: Long,
        trained: Set<Long>,
        volumeByDay: Map<Long, Double>,
        protein: List<Double?>,
        kcal: List<Double?>
    ): Comparison = Comparison(
        thisWeek = week(today - 6, today, trained, volumeByDay, protein.takeLast(7), kcal.takeLast(7)),
        lastWeek = week(
            today - 13, today - 7, trained, volumeByDay,
            protein.dropLast(7).takeLast(7), kcal.dropLast(7).takeLast(7)
        )
    )

    private fun week(
        from: Long,
        to: Long,
        trained: Set<Long>,
        volumeByDay: Map<Long, Double>,
        protein: List<Double?>,
        kcal: List<Double?>
    ): Week {
        val days = from..to
        val loggedProtein = protein.filterNotNull()
        val loggedKcal = kcal.filterNotNull()
        return Week(
            daysTrained = days.count { it in trained },
            volumeKg = days.sumOf { volumeByDay[it] ?: 0.0 },
            avgProteinG = loggedProtein.takeIf { it.isNotEmpty() }?.average(),
            avgKcal = loggedKcal.takeIf { it.isNotEmpty() }?.average(),
            daysLogged = loggedKcal.size
        )
    }

    /** "+12%" or "−8%" against last week, or null when there is nothing to compare with. */
    fun change(now: Double, before: Double): String? {
        if (before <= 0.0) return null
        val pct = ((now - before) / before * 100).let { kotlin.math.round(it).toInt() }
        return when {
            pct > 0 -> "+$pct%"
            pct < 0 -> "−${-pct}%"
            else -> "same"
        }
    }
}
