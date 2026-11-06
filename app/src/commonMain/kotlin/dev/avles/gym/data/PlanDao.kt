package dev.avles.gym.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * The week plan's tables. Bodies-free like [GymDao] (Room's JVM-only default-method
 * handling breaks the iOS build); the replace-a-week logic lives in [PlanRepository].
 */
@Dao
interface PlanDao {

    @Query("SELECT * FROM plan_day WHERE dateEpochDay BETWEEN :from AND :to ORDER BY dateEpochDay")
    fun daysBetween(from: Long, to: Long): Flow<List<PlanDay>>

    @Query("SELECT * FROM plan_lift WHERE dateEpochDay BETWEEN :from AND :to ORDER BY dateEpochDay, position")
    fun liftsBetween(from: Long, to: Long): Flow<List<PlanLift>>

    @Query("SELECT * FROM plan_food WHERE dateEpochDay BETWEEN :from AND :to ORDER BY dateEpochDay, position")
    fun foodsBetween(from: Long, to: Long): Flow<List<PlanFood>>

    @Query("SELECT * FROM plan_day ORDER BY dateEpochDay")
    suspend fun allDays(): List<PlanDay>

    @Query("SELECT * FROM plan_lift ORDER BY dateEpochDay, position")
    suspend fun allLifts(): List<PlanLift>

    @Query("SELECT * FROM plan_food ORDER BY dateEpochDay, position")
    suspend fun allFoods(): List<PlanFood>

    @Query("DELETE FROM plan_day WHERE dateEpochDay IN (:days)")
    suspend fun deleteDays(days: List<Long>)

    @Query("DELETE FROM plan_lift WHERE dateEpochDay IN (:days)")
    suspend fun deleteLifts(days: List<Long>)

    @Query("DELETE FROM plan_food WHERE dateEpochDay IN (:days)")
    suspend fun deleteFoods(days: List<Long>)

    @Insert
    suspend fun insertDays(rows: List<PlanDay>)

    @Insert
    suspend fun insertLifts(rows: List<PlanLift>)

    @Insert
    suspend fun insertFoods(rows: List<PlanFood>)
}
