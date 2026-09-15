package dev.avles.gym.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class GymRepository(private val dao: GymDao) {

    /**
     * Fills the catalogue on first launch, and tops it up on later ones.
     *
     * Matching on name and inserting only what is missing does two jobs at once. On a
     * fresh install everything is missing, so it seeds. On an install that already has
     * data it adds whatever the catalogue has grown by since — the dosa varieties, the
     * supplements — without touching a single existing row, so a food whose macros you
     * corrected stays corrected. Safe to call on every start.
     */
    suspend fun seedCatalogue() {
        val knownLifts = dao.exerciseNames().toSet()
        SeedData.exercises()
            .filter { it.name !in knownLifts }
            .takeIf { it.isNotEmpty() }
            ?.let { dao.insertExercises(it) }

        val knownFoods = dao.foodNames().toSet()
        SeedData.foods()
            .filter { it.name !in knownFoods }
            .takeIf { it.isNotEmpty() }
            ?.let { dao.insertFoods(it) }
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
