package dev.avles.gym.data

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Reads and replaces the week plan. Separate from [GymRepository] because the plan
 * is its own feature with its own tables: it never writes to the log, and the log
 * never reads it.
 */
class PlanRepository(private val database: GymDatabase) {

    private val dao get() = database.planDao()

    /** Every day from [from] to [to] (epoch days, inclusive), planned or not, in order. */
    fun range(from: Long, to: Long): Flow<List<DayPlan>> =
        combine(dao.daysBetween(from, to), dao.liftsBetween(from, to), dao.foodsBetween(from, to)) { days, lifts, foods ->
            val dayBy = days.associateBy { it.dateEpochDay }
            val liftsBy = lifts.groupBy { it.dateEpochDay }
            val foodsBy = foods.groupBy { it.dateEpochDay }
            (from..to).map { d -> DayPlan(d, dayBy[d], liftsBy[d].orEmpty(), foodsBy[d].orEmpty()) }
        }

    /**
     * Replaces every date in [dates] with the rows given, in one transaction: a week
     * is either the old plan or the new one, never half of each. Dates not named are
     * left exactly as they were.
     */
    suspend fun replace(dates: Set<Long>, days: List<PlanDay>, lifts: List<PlanLift>, foods: List<PlanFood>) {
        if (dates.isEmpty()) return
        val keys = dates.sorted()
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                keys.chunked(CHUNK).forEach {
                    dao.deleteDays(it)
                    dao.deleteLifts(it)
                    dao.deleteFoods(it)
                }
                dao.insertDays(days.filter { it.dateEpochDay in dates }.map { it.copy(id = 0) })
                dao.insertLifts(lifts.filter { it.dateEpochDay in dates }.map { it.copy(id = 0) })
                dao.insertFoods(foods.filter { it.dateEpochDay in dates }.map { it.copy(id = 0) })
            }
        }
    }

    /** Clears the plan for [dates]. */
    suspend fun clear(dates: Set<Long>) = replace(dates, emptyList(), emptyList(), emptyList())

    suspend fun everything(): Triple<List<PlanDay>, List<PlanLift>, List<PlanFood>> =
        Triple(dao.allDays(), dao.allLifts(), dao.allFoods())

    private companion object {
        /** SQLite caps bound parameters per statement; a plan is far smaller, but stay well under. */
        const val CHUNK = 500
    }
}
