package dev.avles.gym.domain

/**
 * Milestones, the badges Hevy and the habit apps hand out: days trained, the
 * longest run, total kilos moved, lifts with a record, days of food logged. Each
 * family has tiers; a badge is earned once its number is reached and never taken
 * back, because the number only grows.
 *
 * Computed from what is already stored, so there is nothing to keep in sync.
 */
object Achievements {

    data class Badge(val family: String, val title: String, val target: Double, val value: Double) {
        val earned: Boolean get() = value >= target
        val progress: Float get() = if (target <= 0) 1f else (value / target).toFloat().coerceIn(0f, 1f)
    }

    data class Totals(
        val daysTrained: Int,
        val longestRun: Int,
        val kilosMoved: Double,
        val liftsWithRecords: Int,
        val daysFoodLogged: Int
    )

    private val TIERS = listOf(
        Triple("Training days", listOf(1.0, 10.0, 50.0, 100.0, 250.0, 500.0)) { t: Totals -> t.daysTrained.toDouble() },
        Triple("Longest run", listOf(3.0, 7.0, 14.0, 30.0, 60.0)) { t: Totals -> t.longestRun.toDouble() },
        Triple("Kilos moved", listOf(10_000.0, 100_000.0, 500_000.0, 1_000_000.0)) { t: Totals -> t.kilosMoved },
        Triple("Lifts with a record", listOf(5.0, 15.0, 30.0, 50.0)) { t: Totals -> t.liftsWithRecords.toDouble() },
        Triple("Days of food logged", listOf(7.0, 30.0, 100.0, 365.0)) { t: Totals -> t.daysFoodLogged.toDouble() }
    )

    /** Every tier of every family, in order. */
    fun all(totals: Totals): List<Badge> = TIERS.flatMap { (family, targets, read) ->
        val value = read(totals)
        targets.map { Badge(family, title(family, it), it, value) }
    }

    /** Per family, the next badge to earn, or null once the family is complete. */
    fun next(totals: Totals): List<Badge> =
        all(totals).groupBy { it.family }.values.mapNotNull { tiers -> tiers.firstOrNull { !it.earned } }

    private fun title(family: String, target: Double): String = when (family) {
        "Training days" -> if (target == 1.0) "First session" else "${target.toInt()} training days"
        "Longest run" -> "${target.toInt()}-day run"
        "Kilos moved" -> if (target >= 1_000_000) "${(target / 1_000_000).toInt()} million kg" else "${(target / 1000).toInt()}k kg"
        "Lifts with a record" -> "Records on ${target.toInt()} lifts"
        else -> "${target.toInt()} days of food"
    }
}
