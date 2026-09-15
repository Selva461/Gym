package dev.avles.gym.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class GymRepository(private val dao: GymDao) {

    /** Fills the catalogue on first launch only. Safe to call every start. */
    suspend fun seedIfEmpty() {
        if (dao.exerciseCount() == 0) dao.insertExercises(SeedData.exercises())
        if (dao.foodCount() == 0) dao.insertFoods(SeedData.foods())
    }

    // ---------- Training ----------

    val exercises: Flow<List<Exercise>> = dao.exercises()
    val activeSession: Flow<WorkoutSession?> = dao.activeSession()
    val volumeByDay: Flow<List<DayVolume>> = dao.volumeByDay()
    val trainedDays: Flow<List<Long>> = dao.allTrainedDays()

    fun recentSessions(limit: Int = 20): Flow<List<WorkoutSession>> = dao.recentSessions(limit)
    fun sessionsOn(day: LocalDate): Flow<List<WorkoutSession>> = dao.sessionsOn(day.toEpochDay())
    fun setsForSession(sessionId: Long): Flow<List<SetRow>> = dao.setsForSession(sessionId)
    fun historyForExercise(exerciseId: Long, limit: Int = 30): Flow<List<SetRow>> =
        dao.historyForExercise(exerciseId, limit)

    suspend fun startSession(label: String, day: LocalDate = LocalDate.now()): Long =
        dao.insertSession(
            WorkoutSession(
                dateEpochDay = day.toEpochDay(),
                label = label,
                startedAt = System.currentTimeMillis()
            )
        )

    suspend fun finishSession(session: WorkoutSession) =
        dao.updateSession(session.copy(endedAt = System.currentTimeMillis()))

    suspend fun logSet(
        sessionId: Long,
        exerciseId: Long,
        setIndex: Int,
        weightKg: Double,
        reps: Int,
        isWarmup: Boolean
    ) = dao.insertSet(
        SetEntry(
            sessionId = sessionId,
            exerciseId = exerciseId,
            setIndex = setIndex,
            weightKg = weightKg,
            reps = reps,
            isWarmup = isWarmup,
            loggedAt = System.currentTimeMillis()
        )
    )

    suspend fun deleteSet(setId: Long) {
        dao.setById(setId)?.let { dao.deleteSet(it) }
    }

    suspend fun addExercise(name: String, muscleGroup: String): Long =
        dao.insertExercise(Exercise(name = name, muscleGroup = muscleGroup, isCustom = true))

    // ---------- Fuel ----------

    val foods: Flow<List<FoodItem>> = dao.foods()
    val loggedFoodDays: Flow<List<Long>> = dao.allLoggedFoodDays()

    fun quickPicks(limit: Int = 8): Flow<List<FoodItem>> = dao.mostLoggedFoods(limit)
    fun foodLogFor(day: LocalDate): Flow<List<FoodLogRow>> = dao.foodLogFor(day.toEpochDay())

    suspend fun logFood(day: LocalDate, foodId: Long, meal: String, quantity: Double) =
        dao.insertFoodLog(
            FoodLog(
                dateEpochDay = day.toEpochDay(),
                foodId = foodId,
                meal = meal,
                quantity = quantity,
                loggedAt = System.currentTimeMillis()
            )
        )

    suspend fun removeFoodLog(logId: Long) = dao.deleteFoodLog(logId)
    suspend fun saveFood(item: FoodItem): Long = dao.upsertFood(item)
    suspend fun updateFood(item: FoodItem) = dao.updateFood(item)

    // ---------- Body ----------

    val weights: Flow<List<BodyWeight>> = dao.weights()
    val latestWeight: Flow<BodyWeight?> = dao.latestWeight()

    suspend fun recordWeight(day: LocalDate, kg: Double, note: String = "") =
        dao.upsertWeight(BodyWeight(dateEpochDay = day.toEpochDay(), weightKg = kg, note = note))
}
