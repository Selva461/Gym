package dev.avles.gym.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GymDao {

    // ---------- Exercises ----------

    @Query("SELECT * FROM exercise ORDER BY muscleGroup, name")
    fun exercises(): Flow<List<Exercise>>

    @Query("SELECT COUNT(*) FROM exercise")
    suspend fun exerciseCount(): Int

    /** Names only, so the catalogue can be topped up without reading every row. */
    @Query("SELECT name FROM exercise")
    suspend fun exerciseNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercises(items: List<Exercise>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercise(item: Exercise): Long

    // ---------- Sessions and sets ----------

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Query("SELECT * FROM workout_session WHERE endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    fun activeSession(): Flow<WorkoutSession?>

    @Query("SELECT * FROM workout_session WHERE dateEpochDay = :day ORDER BY startedAt DESC")
    fun sessionsOn(day: Long): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_session ORDER BY startedAt DESC LIMIT :limit")
    fun recentSessions(limit: Int): Flow<List<WorkoutSession>>

    @Query("SELECT DISTINCT dateEpochDay FROM workout_session WHERE dateEpochDay BETWEEN :from AND :to")
    fun trainedDaysBetween(from: Long, to: Long): Flow<List<Long>>

    @Query("SELECT DISTINCT dateEpochDay FROM workout_session ORDER BY dateEpochDay DESC")
    fun allTrainedDays(): Flow<List<Long>>

    @Insert
    suspend fun insertSet(entry: SetEntry): Long

    @Delete
    suspend fun deleteSet(entry: SetEntry)

    @Query("SELECT * FROM set_entry WHERE id = :id")
    suspend fun setById(id: Long): SetEntry?

    @Query(
        """
        SELECT s.id AS setId, s.sessionId AS sessionId, s.exerciseId AS exerciseId,
               e.name AS exerciseName, s.setIndex AS setIndex, s.weightKg AS weightKg,
               s.reps AS reps, s.isWarmup AS isWarmup, s.loggedAt AS loggedAt
        FROM set_entry s
        JOIN exercise e ON e.id = s.exerciseId
        WHERE s.sessionId = :sessionId
        ORDER BY s.loggedAt ASC
        """
    )
    fun setsForSession(sessionId: Long): Flow<List<SetRow>>

    @Query(
        """
        SELECT s.id AS setId, s.sessionId AS sessionId, s.exerciseId AS exerciseId,
               e.name AS exerciseName, s.setIndex AS setIndex, s.weightKg AS weightKg,
               s.reps AS reps, s.isWarmup AS isWarmup, s.loggedAt AS loggedAt
        FROM set_entry s
        JOIN exercise e ON e.id = s.exerciseId
        WHERE s.exerciseId = :exerciseId AND s.isWarmup = 0
        ORDER BY s.loggedAt DESC
        LIMIT :limit
        """
    )
    fun historyForExercise(exerciseId: Long, limit: Int): Flow<List<SetRow>>

    @Query(
        """
        SELECT w.dateEpochDay AS dateEpochDay, SUM(s.weightKg * s.reps) AS volume
        FROM set_entry s
        JOIN workout_session w ON w.id = s.sessionId
        WHERE s.isWarmup = 0
        GROUP BY w.dateEpochDay
        ORDER BY w.dateEpochDay ASC
        """
    )
    fun volumeByDay(): Flow<List<DayVolume>>

    // ---------- Food ----------

    @Query("SELECT * FROM food_item ORDER BY name")
    fun foods(): Flow<List<FoodItem>>

    @Query("SELECT COUNT(*) FROM food_item")
    suspend fun foodCount(): Int

    @Query("SELECT name FROM food_item")
    suspend fun foodNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFoods(items: List<FoodItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFood(item: FoodItem): Long

    @Update
    suspend fun updateFood(item: FoodItem)

    @Insert
    suspend fun insertFoodLog(entry: FoodLog): Long

    @Query("DELETE FROM food_log WHERE id = :id")
    suspend fun deleteFoodLog(id: Long)

    @Query(
        """
        SELECT l.id AS logId, l.dateEpochDay AS dateEpochDay, l.meal AS meal,
               l.quantity AS quantity, l.loggedAt AS loggedAt,
               f.id AS foodId, f.name AS name, f.unitLabel AS unitLabel,
               f.kcal AS kcal, f.proteinG AS proteinG, f.carbsG AS carbsG, f.fatG AS fatG,
               f.isSupplement AS isSupplement,
               f.fiberG AS fiberG, f.sugarG AS sugarG, f.satFatG AS satFatG,
               f.sodiumMg AS sodiumMg, f.potassiumMg AS potassiumMg, f.calciumMg AS calciumMg,
               f.ironMg AS ironMg, f.magnesiumMg AS magnesiumMg, f.zincMg AS zincMg,
               f.vitAMcg AS vitAMcg, f.vitCMg AS vitCMg, f.vitDMcg AS vitDMcg,
               f.vitB12Mcg AS vitB12Mcg, f.folateMcg AS folateMcg, f.omega3G AS omega3G
        FROM food_log l
        JOIN food_item f ON f.id = l.foodId
        WHERE l.dateEpochDay = :day
        ORDER BY l.loggedAt ASC
        """
    )
    fun foodLogFor(day: Long): Flow<List<FoodLogRow>>

    // f.* rather than a hand-written column list: FoodItem now carries fifteen embedded
    // micronutrient columns, and spelling them out here would mean this query silently
    // going stale every time one is added.
    @Query(
        """
        SELECT f.* FROM food_log l
        JOIN food_item f ON f.id = l.foodId
        GROUP BY f.id
        ORDER BY COUNT(l.id) DESC, MAX(l.loggedAt) DESC
        LIMIT :limit
        """
    )
    fun mostLoggedFoods(limit: Int): Flow<List<FoodItem>>

    @Query("SELECT DISTINCT dateEpochDay FROM food_log ORDER BY dateEpochDay DESC")
    fun allLoggedFoodDays(): Flow<List<Long>>

    // ---------- Body weight ----------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeight(entry: BodyWeight)

    @Query("SELECT * FROM body_weight ORDER BY dateEpochDay ASC")
    fun weights(): Flow<List<BodyWeight>>

    @Query("SELECT * FROM body_weight ORDER BY dateEpochDay DESC LIMIT 1")
    fun latestWeight(): Flow<BodyWeight?>
}
