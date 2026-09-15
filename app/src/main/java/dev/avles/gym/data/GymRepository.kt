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

        seedSplitsIfEmpty()
    }

    /**
     * Only on a genuinely empty splits table, and never again.
     *
     * Unlike lifts and foods, splits are topped up by nobody: once you have made
     * this list yours — renamed Push to "Push A", dropped the lifts you do not do —
     * re-adding a starter split on every launch would be the app arguing with you.
     */
    private suspend fun seedSplitsIfEmpty() {
        if (dao.splitCount() > 0) return
        val byName = dao.allExercisesOnce().associateBy { it.name }
        SeedData.splits().forEachIndexed { index, (name, lifts) ->
            val splitId = dao.insertSplit(Split(name = name, position = index, isCustom = false))
            val rows = lifts.mapIndexedNotNull { position, liftName ->
                byName[liftName]?.let { SplitExercise(splitId = splitId, exerciseId = it.id, position = position) }
            }
            if (rows.isNotEmpty()) dao.insertSplitExercises(rows)
        }
    }

    // ---------- Splits ----------

    val splits: Flow<List<Split>> = dao.splits()
    val splitExercises: Flow<List<SplitExercise>> = dao.splitExercises()

    /** Creates when [id] is zero, updates in place otherwise. Lifts are replaced wholesale. */
    suspend fun saveSplit(id: Long, name: String, position: Int, exerciseIds: List<Long>): Long {
        val splitId = if (id == 0L) {
            dao.insertSplit(Split(name = name.trim(), position = position, isCustom = true))
        } else {
            dao.updateSplit(Split(id = id, name = name.trim(), position = position, isCustom = true))
            id
        }
        dao.clearSplitExercises(splitId)
        if (exerciseIds.isNotEmpty()) {
            dao.insertSplitExercises(
                exerciseIds.mapIndexed { position, exerciseId ->
                    SplitExercise(splitId = splitId, exerciseId = exerciseId, position = position)
                }
            )
        }
        return splitId
    }

    /**
     * Sessions already run from this split keep their label and their sets; only the
     * link goes. Deleting a plan is not a reason to lose the training done under it.
     */
    suspend fun deleteSplit(splitId: Long) {
        dao.clearSplitExercises(splitId)
        dao.detachSessionsFromSplit(splitId)
        dao.deleteSplitRow(splitId)
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

    val sessionSummaries: Flow<List<SessionSummary>> = dao.sessionSummaries()

    suspend fun startSession(
        label: String,
        day: LocalDate = LocalDate.now(),
        splitId: Long? = null
    ): Long = dao.insertSession(
        WorkoutSession(
            dateEpochDay = day.toEpochDay(),
            label = label,
            startedAt = System.currentTimeMillis(),
            splitId = splitId
        )
    )

    suspend fun renameSession(session: WorkoutSession, label: String) {
        if (label.isNotBlank()) dao.updateSession(session.copy(label = label.trim()))
    }

    /** Re-opens a finished session so more sets can go onto it. */
    suspend fun reopenSession(session: WorkoutSession) =
        dao.updateSession(session.copy(endedAt = null))

    suspend fun deleteSession(sessionId: Long) {
        dao.deleteSetsForSession(sessionId)
        dao.deleteSessionRow(sessionId)
    }

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

    suspend fun editSet(setId: Long, weightKg: Double, reps: Int, isWarmup: Boolean) {
        if (weightKg > 0 && reps > 0) dao.editSet(setId, weightKg, reps, isWarmup)
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

    suspend fun editFoodLog(logId: Long, quantity: Double, meal: String) {
        if (quantity > 0) dao.editFoodLog(logId, quantity, meal)
    }
    suspend fun saveFood(item: FoodItem): Long = dao.upsertFood(item)
    suspend fun updateFood(item: FoodItem) = dao.updateFood(item)

    // ---------- Body ----------

    val weights: Flow<List<BodyWeight>> = dao.weights()
    val latestWeight: Flow<BodyWeight?> = dao.latestWeight()

    suspend fun recordWeight(day: LocalDate, kg: Double, note: String = "") =
        dao.upsertWeight(BodyWeight(dateEpochDay = day.toEpochDay(), weightKg = kg, note = note))
}
