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

    /** One-shot read, for seeding splits against exercise ids that only exist after insert. */
    @Query("SELECT * FROM exercise ORDER BY muscleGroup, name")
    suspend fun allExercisesOnce(): List<Exercise>

    /**
     * Seeded lifts whose muscle group is no longer one the app offers.
     *
     * An install from before the catalogue was reorganised still has its lifts filed
     * under Push/Pull/Legs, which would leave the picker showing groups that no
     * longer exist alongside the new ones. Counting them is how the refresh below
     * knows whether it has anything to do — so it runs once after an upgrade rather
     * than on every launch.
     */
    @Query("SELECT COUNT(*) FROM exercise WHERE isCustom = 0 AND muscleGroup NOT IN (:groups)")
    suspend fun staleSeededExerciseCount(groups: List<String>): Int

    @Query("SELECT * FROM exercise WHERE isCustom = 0")
    suspend fun seededExercises(): List<Exercise>

    /**
     * One call, so one transaction: Room wraps a list @Update in a single one, and a
     * half-applied regrouping is not a state the app can be in.
     *
     * This replaced a @Transaction function with a body on this interface. Room
     * handles an interface function with a body through a JVM-only calling
     * convention, and fails on the iOS targets ("cannot find required type
     * kotlin.coroutines.Continuation"). The DAO stays bodies-free; logic lives in
     * the repository.
     */
    @Update
    suspend fun updateExercises(items: List<Exercise>)

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

    // Targeted UPDATE rather than @Update on the whole row: the caller is editing
    // three fields from a joined row and has no business reconstructing the entity,
    // including its loggedAt, just to change a weight.
    @Query(
        "UPDATE set_entry SET weightKg = :weightKg, reps = :reps, isWarmup = :isWarmup, " +
            "variation = :variation, kind = :kind, rpe = :rpe WHERE id = :id"
    )
    suspend fun editSet(
        id: Long, weightKg: Double, reps: Int, isWarmup: Boolean, variation: String, kind: String, rpe: Double
    )

    @Query("SELECT * FROM workout_session WHERE id = :id")
    suspend fun sessionById(id: Long): WorkoutSession?

    @Query("DELETE FROM workout_session WHERE id = :id")
    suspend fun deleteSessionRow(id: Long)

    /** No foreign keys are declared, so a session's sets are cleared explicitly. */
    @Query("DELETE FROM set_entry WHERE sessionId = :sessionId")
    suspend fun deleteSetsForSession(sessionId: Long)

    @Query(
        """
        SELECT w.id AS sessionId,
               COUNT(s.id) AS setCount,
               COALESCE(SUM(CASE WHEN s.isWarmup = 0 AND COALESCE(e.isAssisted, 0) = 0
                                 THEN s.weightKg * s.reps ELSE 0 END), 0.0) AS volume
        FROM workout_session w
        LEFT JOIN set_entry s ON s.sessionId = w.id
        LEFT JOIN exercise e ON e.id = s.exerciseId
        GROUP BY w.id
        """
    )
    fun sessionSummaries(): Flow<List<SessionSummary>>

    @Query(
        """
        SELECT s.id AS setId, s.sessionId AS sessionId, s.exerciseId AS exerciseId,
               e.name AS exerciseName, s.setIndex AS setIndex, s.weightKg AS weightKg,
               s.reps AS reps, s.isWarmup AS isWarmup, s.loggedAt AS loggedAt,
               s.variation AS variation, e.isAssisted AS isAssisted,
               s.kind AS kind, s.rpe AS rpe
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
               s.reps AS reps, s.isWarmup AS isWarmup, s.loggedAt AS loggedAt,
               s.variation AS variation, e.isAssisted AS isAssisted,
               s.kind AS kind, s.rpe AS rpe
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
        SELECT w.dateEpochDay AS dateEpochDay,
               SUM(CASE WHEN COALESCE(e.isAssisted, 0) = 0 THEN s.weightKg * s.reps ELSE 0 END) AS volume
        FROM set_entry s
        JOIN workout_session w ON w.id = s.sessionId
        LEFT JOIN exercise e ON e.id = s.exerciseId
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

    @Query("UPDATE food_log SET quantity = :quantity, meal = :meal WHERE id = :id")
    suspend fun editFoodLog(id: Long, quantity: Double, meal: String)

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

    /** The whole food log, oldest first, for export. Read once, not observed. */
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
        ORDER BY l.dateEpochDay ASC, l.loggedAt ASC
        """
    )
    suspend fun allFoodLog(): List<FoodLogRow>

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

    /** The foods logged most recently, each once: the "recent" tab of every food diary. */
    @Query(
        """
        SELECT f.* FROM food_log l
        JOIN food_item f ON f.id = l.foodId
        WHERE l.meal != :exceptMeal
        GROUP BY f.id
        ORDER BY MAX(l.loggedAt) DESC
        LIMIT :limit
        """
    )
    fun recentFoods(limit: Int, exceptMeal: String): Flow<List<FoodItem>>

    // Water is logged as food_log rows in its own slot, but a day with only water
    // on it is not a day of food logged: it would add a "fuelled" dot, a zero-calorie
    // day to every average and a day to the food achievements.
    @Query(
        "SELECT DISTINCT dateEpochDay FROM food_log WHERE meal != '" + Meals.WATER + "' " +
            "ORDER BY dateEpochDay DESC"
    )
    fun allLoggedFoodDays(): Flow<List<Long>>

    // ---------- Splits ----------

    @Query("SELECT * FROM split ORDER BY position, name")
    fun splits(): Flow<List<Split>>

    @Query("SELECT COUNT(*) FROM split")
    suspend fun splitCount(): Int

    @Insert
    suspend fun insertSplit(split: Split): Long

    @Update
    suspend fun updateSplit(split: Split)

    @Query("DELETE FROM split WHERE id = :id")
    suspend fun deleteSplitRow(id: Long)

    @Query("SELECT * FROM split_exercise ORDER BY splitId, position")
    fun splitExercises(): Flow<List<SplitExercise>>

    @Insert
    suspend fun insertSplitExercises(rows: List<SplitExercise>)

    @Query("DELETE FROM split_exercise WHERE splitId = :splitId")
    suspend fun clearSplitExercises(splitId: Long)

    /** A deleted split must not leave sessions pointing at it. */
    @Query("UPDATE workout_session SET splitId = NULL WHERE splitId = :splitId")
    suspend fun detachSessionsFromSplit(splitId: Long)

    // ---------- Body weight ----------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeight(entry: BodyWeight)

    @Query("SELECT * FROM body_weight ORDER BY dateEpochDay ASC")
    fun weights(): Flow<List<BodyWeight>>

    @Query("SELECT * FROM body_weight ORDER BY dateEpochDay DESC LIMIT 1")
    fun latestWeight(): Flow<BodyWeight?>

    // ---------- Photos ----------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressPhoto(photo: ProgressPhoto): Long

    @Query("SELECT * FROM progress_photo ORDER BY dateEpochDay DESC, takenAt DESC")
    fun progressPhotos(): Flow<List<ProgressPhoto>>

    @Query("SELECT * FROM progress_photo WHERE id = :id")
    suspend fun progressPhotoById(id: Long): ProgressPhoto?

    @Query("DELETE FROM progress_photo WHERE id = :id")
    suspend fun deleteProgressPhotoRow(id: Long)

    @Query("UPDATE progress_photo SET label = :label, note = :note WHERE id = :id")
    suspend fun editProgressPhoto(id: Long, label: String, note: String)

    /** Every filename the database believes in, for reconciling against the disk. */
    @Query("SELECT fileName FROM progress_photo UNION SELECT fileName FROM exercise_photo")
    suspend fun referencedPhotoFileNames(): List<String>

    /**
     * REPLACE, with the unique index on exerciseId doing the work: attaching a second
     * picture to a lift swaps it rather than accumulating. One picture per lift is the
     * feature; a gallery would be a different one.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercisePhoto(photo: ExercisePhoto): Long

    @Query("SELECT * FROM exercise_photo")
    fun exercisePhotos(): Flow<List<ExercisePhoto>>

    @Query("SELECT * FROM exercise_photo WHERE exerciseId = :exerciseId")
    suspend fun exercisePhotoFor(exerciseId: Long): ExercisePhoto?

    @Query("DELETE FROM exercise_photo WHERE exerciseId = :exerciseId")
    suspend fun deleteExercisePhotoRow(exerciseId: Long)

    // ---------- Day markers ----------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDayMarker(marker: DayMarker): Long

    @Query("SELECT * FROM day_marker ORDER BY dateEpochDay DESC")
    fun dayMarkers(): Flow<List<DayMarker>>

    @Query("SELECT * FROM day_marker WHERE dateEpochDay = :day")
    suspend fun dayMarkerFor(day: Long): DayMarker?

    @Query("DELETE FROM day_marker WHERE dateEpochDay = :day")
    suspend fun clearDayMarker(day: Long)

    /**
     * Every working set, for the coach's weekly volume count. Warm-ups are excluded
     * in SQL rather than in Kotlin because they are never wanted here and a week of
     * them is a lot of rows to carry across the boundary to throw away.
     */
    @Query(
        """
        SELECT s.id AS setId, s.sessionId AS sessionId, s.exerciseId AS exerciseId,
               e.name AS exerciseName, s.setIndex AS setIndex, s.weightKg AS weightKg,
               s.reps AS reps, s.isWarmup AS isWarmup, s.loggedAt AS loggedAt,
               s.variation AS variation, e.isAssisted AS isAssisted,
               s.kind AS kind, s.rpe AS rpe
        FROM set_entry s
        JOIN exercise e ON e.id = s.exerciseId
        WHERE s.isWarmup = 0
        """
    )
    fun allWorkingSets(): Flow<List<SetRow>>

    /**
     * Calories and protein per day, summed in SQL.
     *
     * A day with nothing logged is absent from the result rather than present as a
     * zero. That distinction matters all the way up to the chart: a day you forgot
     * to log and a day you ate nothing are different, and a line that draws the
     * first as the second is a picture of your logging habit.
     */
    @Query(
        """
        SELECT l.dateEpochDay AS dateEpochDay,
               SUM(f.kcal * l.quantity) AS kcal,
               SUM(f.proteinG * l.quantity) AS proteinG
        FROM food_log l
        JOIN food_item f ON f.id = l.foodId
        WHERE l.meal != '""" + Meals.WATER + """'
        GROUP BY l.dateEpochDay
        """
    )
    fun dailyMacros(): Flow<List<DayMacroRow>>

    /** Which day each session belongs to, for counting a training week. */
    @Query("SELECT id, dateEpochDay FROM workout_session")
    fun sessionDays(): Flow<List<SessionDay>>

    // ---- Tape measurements ----

    /** Replaces on the (day, part) unique index, so re-measuring a day overwrites it. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeasurements(rows: List<BodyMeasurement>)

    @Query("SELECT * FROM body_measurement ORDER BY dateEpochDay ASC")
    fun measurements(): Flow<List<BodyMeasurement>>


    @Query("UPDATE exercise SET notes = :notes WHERE id = :id")
    suspend fun setExerciseNotes(id: Long, notes: String)

    // ---- Saved meals ----

    @Insert
    suspend fun insertSavedMeal(meal: SavedMeal): Long

    @Insert
    suspend fun insertSavedMealItems(items: List<SavedMealItem>)

    @Query(
        """
        SELECT m.id AS mealId, m.name AS mealName, i.foodId AS foodId, f.name AS foodName,
               i.quantity AS quantity, f.kcal AS kcal, f.proteinG AS proteinG
        FROM saved_meal m
        JOIN saved_meal_item i ON i.mealId = m.id
        JOIN food_item f ON f.id = i.foodId
        ORDER BY m.createdAt DESC, i.id ASC
        """
    )
    fun savedMealRows(): Flow<List<SavedMealRow>>

    @Query("DELETE FROM saved_meal_item WHERE mealId = :mealId")
    suspend fun deleteSavedMealItems(mealId: Long)

    @Query("DELETE FROM saved_meal WHERE id = :mealId")
    suspend fun deleteSavedMeal(mealId: Long)

    /** Every set, warm-ups included, oldest first: the export is the whole log. */
    @Query(
        """
        SELECT s.id AS setId, s.sessionId AS sessionId, s.exerciseId AS exerciseId,
               e.name AS exerciseName, s.setIndex AS setIndex, s.weightKg AS weightKg,
               s.reps AS reps, s.isWarmup AS isWarmup, s.loggedAt AS loggedAt,
               s.variation AS variation, e.isAssisted AS isAssisted,
               s.kind AS kind, s.rpe AS rpe
        FROM set_entry s
        JOIN exercise e ON e.id = s.exerciseId
        ORDER BY s.loggedAt ASC
        """
    )
    suspend fun everySet(): List<SetRow>
}
