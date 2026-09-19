package dev.avles.gym.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * [photos] is optional so that the repository stays constructible without an Android
 * Context — which is what keeps every non-photo test able to build one. When it is
 * absent the photo methods are no-ops rather than crashes: a caller with no store
 * cannot have a picture to lose.
 */
class GymRepository(private val dao: GymDao, private val photos: PhotoStore? = null) {

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
        regroupSeededExercisesIfStale()
    }

    /**
     * Brings seeded lifts onto the current muscle groups and rep ranges.
     *
     * An install from before the catalogue was reorganised has its lifts filed under
     * Push/Pull/Legs/Core, and topping up by name leaves those rows exactly as they
     * were — so the picker would show the old four groups next to the new thirteen.
     * This rewrites the metadata on seeded lifts only; anything you added yourself is
     * marked custom and is never touched.
     *
     * Gated on there being something to fix, so it runs once after an upgrade and
     * not on every launch.
     */
    private suspend fun regroupSeededExercisesIfStale() {
        val groups = SeedData.muscleGroups()
        if (dao.staleSeededExerciseCount(groups) == 0) return
        dao.refreshSeededExercises(SeedData.exercises())
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
        isWarmup: Boolean,
        variation: String = ""
    ) = dao.insertSet(
        SetEntry(
            sessionId = sessionId,
            exerciseId = exerciseId,
            setIndex = setIndex,
            weightKg = weightKg,
            reps = reps,
            isWarmup = isWarmup,
            loggedAt = System.currentTimeMillis(),
            variation = variation.trim()
        )
    )

    suspend fun deleteSet(setId: Long) {
        dao.setById(setId)?.let { dao.deleteSet(it) }
    }

    suspend fun editSet(
        setId: Long,
        weightKg: Double,
        reps: Int,
        isWarmup: Boolean,
        variation: String = ""
    ) {
        if (weightKg > 0 && reps > 0) dao.editSet(setId, weightKg, reps, isWarmup, variation.trim())
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

    // ---------- Photos ----------

    val progressPhotos: Flow<List<ProgressPhoto>> = dao.progressPhotos()
    val exercisePhotos: Flow<List<ExercisePhoto>> = dao.exercisePhotos()

    /**
     * Copies the picked image in, then records it. In that order, deliberately: a
     * file with no row is an orphan the reconciler sweeps up on the next launch,
     * whereas a row with no file is a permanent gap in the timeline that the UI has
     * to keep apologising for.
     */
    suspend fun addProgressPhoto(uri: android.net.Uri, dateEpochDay: Long, label: String = ""): Boolean {
        val store = photos ?: return false
        val fileName = store.importFrom(uri) ?: return false
        dao.insertProgressPhoto(
            ProgressPhoto(
                dateEpochDay = dateEpochDay,
                fileName = fileName,
                label = label.trim(),
                takenAt = System.currentTimeMillis()
            )
        )
        return true
    }

    /** Row first here, because a failed file delete is recoverable and a lost row is not. */
    suspend fun deleteProgressPhoto(id: Long) {
        val row = dao.progressPhotoById(id) ?: return
        dao.deleteProgressPhotoRow(id)
        photos?.delete(row.fileName)
    }

    suspend fun editProgressPhoto(id: Long, label: String, note: String) =
        dao.editProgressPhoto(id, label.trim(), note.trim())

    suspend fun attachExercisePhoto(exerciseId: Long, uri: android.net.Uri): Boolean {
        val store = photos ?: return false
        val fileName = store.importFrom(uri) ?: return false
        // The old picture's file has to go explicitly: the unique index replaces the
        // row, which would otherwise leave its file on disk with nothing pointing at it.
        val previous = dao.exercisePhotoFor(exerciseId)
        dao.insertExercisePhoto(
            ExercisePhoto(exerciseId = exerciseId, fileName = fileName, addedAt = System.currentTimeMillis())
        )
        previous?.let { store.delete(it.fileName) }
        return true
    }

    suspend fun detachExercisePhoto(exerciseId: Long) {
        val row = dao.exercisePhotoFor(exerciseId) ?: return
        dao.deleteExercisePhotoRow(exerciseId)
        photos?.delete(row.fileName)
    }

    /**
     * Deletes photo files nothing points at. Called once on launch alongside seeding.
     *
     * Cheap — a directory listing and a set difference — and it turns "we might leak
     * a few hundred KB every time a delete half-fails" into something that cannot
     * accumulate.
     */
    suspend fun reconcilePhotoFiles(): Int {
        val store = photos ?: return 0
        return store.deleteOrphans(dao.referencedPhotoFileNames().toSet())
    }

    fun photoFile(fileName: String): java.io.File? = photos?.fileFor(fileName)

    fun photoBytesUsed(): Long = photos?.bytesUsed() ?: 0L

    // ---------- Rest and missed days ----------

    val allSets: Flow<List<SetRow>> = dao.allWorkingSets()
    val dailyMacros: Flow<List<DayMacroRow>> = dao.dailyMacros()
    val dayMarkers: Flow<List<DayMarker>> = dao.dayMarkers()
    val sessionDays: Flow<List<SessionDay>> = dao.sessionDays()

    /**
     * Marking a day the same thing twice clears it, so the chip that set it also
     * unsets it. Two taps to undo one tap, with no separate control to find.
     */
    suspend fun markDay(dateEpochDay: Long, kind: String, note: String = "") {
        val existing = dao.dayMarkerFor(dateEpochDay)
        if (existing?.kind == kind && note.isBlank()) {
            dao.clearDayMarker(dateEpochDay)
            return
        }
        dao.upsertDayMarker(
            DayMarker(
                id = existing?.id ?: 0,
                dateEpochDay = dateEpochDay,
                kind = kind,
                note = note.trim(),
                markedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearDayMarker(dateEpochDay: Long) = dao.clearDayMarker(dateEpochDay)
}
