package dev.avles.gym.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.avles.gym.data.BodyMeasurement
import dev.avles.gym.data.CardioEntry
import dev.avles.gym.data.BodyWeight
import dev.avles.gym.data.DayVolume
import dev.avles.gym.data.Exercise
import dev.avles.gym.data.FoodItem
import dev.avles.gym.data.FoodLogRow
import dev.avles.gym.data.GymRepository
import dev.avles.gym.data.GymSettings
import dev.avles.gym.data.Meals
import dev.avles.gym.data.PickedImage
import dev.avles.gym.data.SessionSummary
import dev.avles.gym.data.ProgressPhoto
import dev.avles.gym.data.SetRow
import dev.avles.gym.data.SettingsStore
import dev.avles.gym.data.Split
import dev.avles.gym.data.SplitPlan
import dev.avles.gym.data.WorkoutSession
import dev.avles.gym.domain.Assistance
import dev.avles.gym.domain.MacroMath
import dev.avles.gym.data.DayKinds
import dev.avles.gym.data.DayMarker
import dev.avles.gym.domain.Burn
import dev.avles.gym.domain.Coach
import dev.avles.gym.domain.Patterns
import dev.avles.gym.domain.Physique
import dev.avles.gym.domain.Physiques
import dev.avles.gym.domain.Energy
import dev.avles.gym.domain.ExerciseGuide
import dev.avles.gym.domain.Export
import dev.avles.gym.domain.LiftHistory
import dev.avles.gym.domain.Photos
import dev.avles.gym.domain.Progression
import dev.avles.gym.domain.Recommendation
import dev.avles.gym.domain.RestTimes
import dev.avles.gym.domain.SavedMeals
import dev.avles.gym.domain.Supersets
import dev.avles.gym.domain.Totals
import dev.avles.gym.domain.Training
import dev.avles.gym.notify.Notifier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import dev.avles.gym.core.Dates
import dev.avles.gym.core.epochDay
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class GymViewModel(
    private val repo: GymRepository,
    private val settingsStore: SettingsStore,
    private val notifier: Notifier
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(Dates.today())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _restRemaining = MutableStateFlow<Int?>(null)
    val restRemaining: StateFlow<Int?> = _restRemaining.asStateFlow()

    private val _lastPrSetId = MutableStateFlow<Long?>(null)
    val lastPrSetId: StateFlow<Long?> = _lastPrSetId.asStateFlow()

    private var restJob: Job? = null

    /**
     * The lift being worked on. This lives here rather than in the Train screen so
     * that it survives navigating away and back, and so that the recommendation can
     * be a flow off it instead of something the screen has to go and fetch.
     */
    private val _selectedExerciseId = MutableStateFlow<Long?>(null)
    val selectedExerciseId: StateFlow<Long?> = _selectedExerciseId.asStateFlow()

    /**
     * How the next set will be performed — "Close", "Paused", or empty for
     * unspecified. Held here rather than in the screen so that it survives
     * navigating away, exactly like the selected lift, and so the recommendation
     * can react to it.
     */
    private val _selectedVariation = MutableStateFlow("")
    val selectedVariation: StateFlow<String> = _selectedVariation.asStateFlow()

    // ---------- Day ----------

    val dayState: StateFlow<DayState> = _selectedDate
        .flatMapLatest { date ->
            combine(
                repo.foodLogFor(date),
                repo.sessionsOn(date),
                settingsStore.settings
            ) { rows, sessions, settings ->
                DayState(
                    date = date,
                    rows = rows,
                    totals = MacroMath.totalsOf(rows),
                    byMeal = MacroMath.totalsByMeal(rows),
                    sessions = sessions,
                    settings = settings
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DayState())

    val calendar: StateFlow<CalendarDots> =
        combine(repo.trainedDays, repo.loggedFoodDays) { trained, fuelled ->
            CalendarDots(trained.toSet(), fuelled.toSet())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarDots())

    // Water is logged from its own card; as a "usual suspect" it would crowd out food.
    val quickPicks: StateFlow<List<FoodItem>> = repo.quickPicks(9)
        .map { picks -> picks.filter { it.name != WATER_FOOD }.take(8) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentFoods: StateFlow<List<FoodItem>> = repo.recentFoods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val foods: StateFlow<List<FoodItem>> = repo.foods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectDate(date: LocalDate) {
        if (date <= Dates.today()) _selectedDate.value = date
    }

    // ---------- Training ----------

    private val activeSets = repo.activeSession.flatMapLatest { session ->
        if (session == null) flowOf(emptyList()) else repo.setsForSession(session.id)
    }

    val trainState: StateFlow<TrainState> = combine(
        repo.activeSession,
        activeSets,
        repo.exercises,
        repo.recentSessions()
    ) { active, sets, exercises, recent ->
        TrainState(active, sets, exercises, recent)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrainState())

    val progressState: StateFlow<ProgressState> = combine(
        repo.weights,
        repo.volumeByDay,
        repo.trainedDays,
        repo.loggedFoodDays
    ) { weights, volume, trained, fuelled ->
        ProgressState(weights, volume, trained.toSet(), fuelled.toSet())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressState())

    val dayMarkers: StateFlow<List<DayMarker>> = repo.dayMarkers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun markDay(dateEpochDay: Long, kind: String, note: String = "") =
        viewModelScope.launch { repo.markDay(dateEpochDay, kind, note) }

    fun clearDayMarker(dateEpochDay: Long) =
        viewModelScope.launch { repo.clearDayMarker(dateEpochDay) }

    /**
     * The coach's read on the last seven days.
     *
     * Recomputed whenever a set lands, the goal changes or a day is marked, because
     * all three change the answer. The guide is derived per exercise rather than
     * stored, so this costs a map build rather than a query.
     */
    val coachState: StateFlow<CoachState> = combine(
        repo.exercises,
        repo.allSets,
        repo.sessionDays,
        repo.dayMarkers,
        settingsStore.settings
    ) { catalogue, sets, sessionDays, markers, s ->
        val physique = Physiques.byId(s.goalId)
        val guides = catalogue.associate { it.id to ExerciseGuide.of(it) }
        val dayOf = sessionDays.associate { it.id to it.dateEpochDay }
        val today = Dates.todayEpochDay()

        val actual = Coach.weeklySets(
            history = sets,
            sessionDayOf = { dayOf[it] },
            guideFor = { guides[it] },
            today = today
        )
        val review = Coach.review(physique, actual)
        val thisWeek = (today - (Coach.WINDOW_DAYS - 1))..today
        val trained = sets
            .filter { dayOf[it.sessionId]?.let { d -> d in thisWeek } == true }
            .map { it.exerciseId }
            .toSet()

        CoachState(
            physique = physique,
            review = review,
            suggestions = Coach.suggestions(review, catalogue, { guides[it.id]!! }, trained),
            verdict = Coach.verdict(review),
            restDays = markers.count { it.dateEpochDay in thisWeek && it.kind == DayKinds.REST },
            missedDays = markers.count { it.dateEpochDay in thisWeek && it.kind == DayKinds.MISSED }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CoachState())

    fun chooseGoal(id: String) = viewModelScope.launch {
        settingsStore.save(settingsStore.settings.first().copy(goalId = id))
    }

    val patternState: StateFlow<PatternState> = combine(
        repo.dailyMacros,
        repo.sessionDays,
        settingsStore.settings
    ) { macros, sessionDays, s ->
        val today = Dates.todayEpochDay()
        val window = 30
        val byDay = macros.associate {
            it.dateEpochDay to Patterns.DayMacros(it.dateEpochDay, it.kcal, it.proteinG)
        }
        val trained = sessionDays.map { it.dateEpochDay }.toSet()
        val kcal = Patterns.series(today, window, byDay) { it.kcal }
        val protein = Patterns.series(today, window, byDay) { it.proteinG }
        val weekdays = Patterns.byWeekday(today, window, trained, byDay)

        PatternState(
            kcal = kcal,
            protein = protein,
            kcalMean = Patterns.rollingMean(kcal),
            proteinMean = Patterns.rollingMean(protein),
            weekdays = weekdays,
            weakestWeekday = Patterns.weakestWeekday(weekdays),
            kcalAdherence = Patterns.adherence(kcal, s.kcalTarget),
            proteinAdherence = Patterns.adherence(protein, s.proteinTargetG),
            kcalTarget = s.kcalTarget,
            proteinTarget = s.proteinTargetG,
            windowDays = window
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PatternState())

    val photoState: StateFlow<PhotoState> = repo.progressPhotos
        .map { photos ->
            val rows = photos.map { PhotoRow(it, repo.photoPath(it.fileName)) }
            PhotoState(
                photos = rows,
                comparison = Photos.spanToCompare(rows) { it.photo.dateEpochDay },
                bytesUsed = repo.photoBytesUsed()
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PhotoState())

    /** Paths of lift pictures, keyed by exercise, so the reference sheet can look one up. */
    val exercisePhotoFiles: StateFlow<Map<Long, String?>> = repo.exercisePhotos
        .map { rows -> rows.associate { it.exerciseId to repo.photoPath(it.fileName) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /**
     * Null means the import failed — an unreadable pick, or no store. Surfaced so the
     * screen can say so instead of silently doing nothing, which reads as a bug.
     */
    private val _photoError = MutableStateFlow<String?>(null)
    val photoError: StateFlow<String?> = _photoError.asStateFlow()

    fun clearPhotoError() { _photoError.value = null }

    fun addProgressPhoto(image: PickedImage, dateEpochDay: Long, label: String) =
        viewModelScope.launch {
            if (!repo.addProgressPhoto(image, dateEpochDay, label)) {
                _photoError.value = "That picture could not be read. Nothing was saved."
            }
        }

    fun deleteProgressPhoto(id: Long) = viewModelScope.launch { repo.deleteProgressPhoto(id) }

    fun editProgressPhoto(id: Long, label: String, note: String) =
        viewModelScope.launch { repo.editProgressPhoto(id, label, note) }

    fun attachExercisePhoto(exerciseId: Long, image: PickedImage) = viewModelScope.launch {
        if (!repo.attachExercisePhoto(exerciseId, image)) {
            _photoError.value = "That picture could not be read. Nothing was saved."
        }
    }

    fun detachExercisePhoto(exerciseId: Long) =
        viewModelScope.launch { repo.detachExercisePhoto(exerciseId) }

    val settings: StateFlow<GymSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GymSettings())

    // ---------- Splits ----------

    /**
     * Splits with their lifts resolved and ordered. Assembled here from three flows
     * because the exercise catalogue is already loaded and a Room @Relation would
     * lose the ordering that makes a split worth having.
     */
    val splitPlans: StateFlow<List<SplitPlan>> =
        combine(repo.splits, repo.splitExercises, repo.exercises) { splits, links, catalogue ->
            val byId = catalogue.associateBy { it.id }
            splits.map { split ->
                SplitPlan(
                    split = split,
                    exercises = links
                        .filter { it.splitId == split.id }
                        .sortedBy { it.position }
                        .mapNotNull { byId[it.exerciseId] }
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** The plan behind the session currently open, so its lifts are one tap away. */
    val activePlan: StateFlow<SplitPlan?> =
        combine(repo.activeSession, splitPlans) { session, plans ->
            session?.splitId?.let { id -> plans.firstOrNull { it.id == id } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val sessionSummaries: StateFlow<Map<Long, SessionSummary>> = repo.sessionSummaries
        .map { rows -> rows.associateBy { it.sessionId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun saveSplit(id: Long, name: String, exerciseIds: List<Long>) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        val position = splitPlans.value.firstOrNull { it.id == id }?.split?.position
            ?: splitPlans.value.size
        repo.saveSplit(id, name, position, exerciseIds)
    }

    fun deleteSplit(splitId: Long) = viewModelScope.launch { repo.deleteSplit(splitId) }

    // ---------- Sessions ----------

    fun startSession(label: String, splitId: Long? = null) = viewModelScope.launch {
        repo.startSession(label, _selectedDate.value, splitId)
    }

    fun startSessionFrom(plan: SplitPlan) = viewModelScope.launch {
        repo.startSession(plan.name, _selectedDate.value, plan.id)
    }

    /**
     * Which past session is open for editing. Held here rather than in the screen so
     * that its sets can be a database flow — edit a set inside the dialog and the
     * list behind it updates without anything being told to refresh.
     */
    private val _openSessionId = MutableStateFlow<Long?>(null)

    fun openSession(sessionId: Long?) {
        _openSessionId.value = sessionId
    }

    val openSessionDetail: StateFlow<SessionDetail?> = _openSessionId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                combine(
                    repo.recentSessions(50),
                    repo.setsForSession(id),
                    repo.sessionSummaries
                ) { sessions, sets, summaries ->
                    sessions.firstOrNull { it.id == id }?.let { session ->
                        SessionDetail(session, sets, summaries.firstOrNull { s -> s.sessionId == id })
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun renameSession(session: WorkoutSession, label: String) = viewModelScope.launch {
        repo.renameSession(session, label)
    }

    fun reopenSession(session: WorkoutSession) = viewModelScope.launch {
        repo.reopenSession(session)
        _openSessionId.value = null
    }

    fun deleteSession(sessionId: Long) = viewModelScope.launch {
        repo.deleteSession(sessionId)
        _openSessionId.value = null
    }

    private val _workoutReport = MutableStateFlow<LiftHistory.Report?>(null)

    /** The card shown after Finish, as every logging app does. Null once dismissed. */
    val workoutReport: StateFlow<LiftHistory.Report?> = _workoutReport.asStateFlow()

    fun finishSession() = viewModelScope.launch {
        val session = trainState.value.active ?: return@launch
        endSuperset()
        val sets = trainState.value.sets
        // Everything earlier on the same lifts, so a record means better than any
        // previous session. Read before closing, while the sets are still current.
        val before = sets.map { it.exerciseId }.distinct().flatMap { id ->
            repo.historyForExercise(id, HISTORY_LIMIT).first()
        }
        repo.finishSession(session)
        cancelRest()
        if (sets.isNotEmpty()) {
            val report = LiftHistory.report(session.startedAt, Dates.nowMillis(), sets, before)
            val bodyKg = repo.weights.first().maxByOrNull { it.dateEpochDay }?.weightKg ?: 0.0
            _workoutReport.value = report.copy(kcalBurned = Burn.kcal(report.minutes, bodyKg))
        }
    }

    /** Each lift's best set, newest record first. */
    val records: StateFlow<List<SetRow>> = repo.allSets
        .map { LiftHistory.bestPerLift(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun dismissWorkoutReport() {
        _workoutReport.value = null
    }

    /** A lift's whole logged history, newest first, for its history page. */
    fun liftHistory(exerciseId: Long): Flow<List<SetRow>> = repo.historyForExercise(exerciseId, HISTORY_LIMIT)

    fun logSet(
        exerciseId: Long,
        weightKg: Double,
        reps: Int,
        isWarmup: Boolean,
        variation: String = _selectedVariation.value,
        kind: String = "",
        rpe: Double = 0.0
    ) =
        viewModelScope.launch {
            val session = trainState.value.active ?: return@launch
            val index = Training.nextSetIndex(trainState.value.sets, exerciseId)
            val newId = repo.logSet(session.id, exerciseId, index, weightKg, reps, isWarmup, variation, kind, rpe)

            val superset = Supersets.after(_superset.value, exerciseId).takeIf { !isWarmup }
            if (superset != null) selectExercise(superset.exerciseId)

            if (!isWarmup) {
                val history = repo.historyForExercise(exerciseId).first()
                val logged = history.firstOrNull { it.setId == newId }
                if (logged != null && Training.isPersonalRecord(logged, history)) {
                    _lastPrSetId.value = newId
                }
                val exercise = trainState.value.exercises.firstOrNull { it.id == exerciseId }
                val stored = settingsStore.settings.first()
                // Mid-superset: straight on to the second lift, no rest.
                if (superset != null && !superset.rest) {
                    cancelRest()
                    return@launch
                }
                startRest(
                    seconds = if (stored.restByLift) RestTimes.forLift(exercise, stored.restSeconds) else stored.restSeconds,
                    exerciseName = logged?.exerciseName ?: exercise?.name
                )
            }
        }

    private val _superset = MutableStateFlow<Supersets.Pair?>(null)

    /** The two lifts being done back to back, if any. Not saved: a superset lasts a session. */
    val superset: StateFlow<Supersets.Pair?> = _superset.asStateFlow()

    fun startSuperset(first: Long, second: Long) {
        if (first != second) _superset.value = Supersets.Pair(first, second)
    }

    fun endSuperset() {
        _superset.value = null
    }

    fun deleteSet(setId: Long) = viewModelScope.launch {
        repo.deleteSet(setId)
        if (_lastPrSetId.value == setId) _lastPrSetId.value = null
    }

    /**
     * Clears the record marker on edit. The flag was earned by the numbers that were
     * there a moment ago; leaving it on a set whose weight just changed would be the
     * app asserting something it has not rechecked.
     */
    fun editSet(
        setId: Long,
        weightKg: Double,
        reps: Int,
        isWarmup: Boolean,
        variation: String = "",
        kind: String = "",
        rpe: Double = 0.0
    ) =
        viewModelScope.launch {
            repo.editSet(setId, weightKg, reps, isWarmup, variation, kind, rpe)
            if (_lastPrSetId.value == setId) _lastPrSetId.value = null
        }

    fun setExerciseNotes(exerciseId: Long, notes: String) = viewModelScope.launch {
        repo.setExerciseNotes(exerciseId, notes)
    }

    fun addExercise(name: String, group: String) = viewModelScope.launch {
        if (name.isNotBlank()) repo.addExercise(name.trim(), group)
    }

    fun selectExercise(id: Long?) {
        // A grip chosen for a bench press means nothing on a squat, and carrying it
        // across would silently file the squat's sets under it.
        if (id != _selectedExerciseId.value) _selectedVariation.value = ""
        _selectedExerciseId.value = id
    }

    /** Empty clears it back to unspecified, which is a real choice and not a no-op. */
    fun selectVariation(variation: String) {
        _selectedVariation.value = variation.trim()
    }

    /**
     * Recomputed whenever the lift changes or a set lands, because the history it
     * reads is a database flow. Log a set that hits the top of the range and the
     * target moves up while you are still standing there.
     */
    val guidance: StateFlow<LiftGuidance?> =
        combine(_selectedExerciseId, _selectedVariation, repo.exercises) { id, variation, catalogue ->
            id?.let { wanted -> catalogue.firstOrNull { it.id == wanted } } to variation
        }.flatMapLatest { (exercise, variation) ->
            if (exercise == null) {
                flowOf(null)
            } else {
                repo.historyForExercise(exercise.id).map { history ->
                    // Best and milestone stay across every variation on purpose: your
                    // best bench is your best bench, whatever grip got you there.
                    val best = Training.bestEstimatedOneRepMax(history)
                    // On an assisted machine the milestone is the next round number of
                    // help *below* the least you have used, not a max above.
                    val leastHelp = if (exercise.isAssisted) Assistance.leastHelp(history) else null
                    LiftGuidance(
                        exercise = exercise,
                        recommendation = Progression.recommend(exercise, history, variation),
                        bestE1rm = best,
                        leastHelp = leastHelp,
                        nextMilestone = leastHelp?.let { Assistance.nextMilestone(it) }
                            ?: Progression.nextMilestone(best),
                        milestoneProgress = leastHelp?.let { Assistance.milestoneProgress(it) }
                            ?: Progression.milestoneProgress(best),
                        recentSets = history.take(5),
                        lastTime = lastSessionOf(history, trainState.value.active?.id),
                        variation = variation,
                        guide = ExerciseGuide.of(exercise)
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ---------- Rest timer ----------

    /**
     * [exerciseName] is handed in rather than read back off [trainState]. The set that
     * started this rest has only just been inserted and the database flow has not
     * re-emitted yet, so reading the list here names the *previous* lift — or nothing
     * at all on the first set of a session.
     */
    private var restLeft = 0
    private var restLift = ""

    fun startRest(seconds: Int? = null, exerciseName: String? = null) {
        restJob?.cancel()
        val total = seconds ?: settings.value.restSeconds
        val lift = exerciseName
            ?: trainState.value.sets.lastOrNull()?.exerciseName
            ?: "the next lift"
        restLeft = total
        restLift = lift
        notifier.restStarted(total, lift)
        restJob = viewModelScope.launch {
            while (restLeft > 0) {
                _restRemaining.value = restLeft
                delay(1_000)
                restLeft--
            }
            _restRemaining.value = null
            notifier.restFinished(lift)
        }
    }

    /** The −15 / +15 under a running timer. The notification is re-armed to match. */
    fun adjustRest(deltaSeconds: Int) {
        if (_restRemaining.value == null) return
        restLeft = (restLeft + deltaSeconds).coerceIn(1, 900)
        _restRemaining.value = restLeft
        notifier.restStarted(restLeft, restLift)
    }

    fun cancelRest() {
        restJob?.cancel()
        _restRemaining.value = null
        notifier.restCancelled()
    }

    fun clearPrFlag() {
        _lastPrSetId.value = null
    }

    // ---------- Fuel ----------

    fun logFood(foodId: Long, meal: String = Meals.LUNCH, quantity: Double = 1.0) =
        viewModelScope.launch {
            if (quantity > 0) repo.logFood(_selectedDate.value, foodId, meal, quantity)
        }

    fun removeFoodLog(logId: Long) = viewModelScope.launch { repo.removeFoodLog(logId) }

    // ---------- Cardio ----------

    val cardio: StateFlow<List<CardioEntry>> = _selectedDate
        .flatMapLatest { repo.cardioOn(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun logCardio(activity: String, minutes: Int, distanceKm: Double) = viewModelScope.launch {
        repo.logCardio(_selectedDate.value, activity, minutes, distanceKm)
    }

    fun deleteCardio(id: Long) = viewModelScope.launch { repo.deleteCardio(id) }

    // ---------- Saved meals ----------

    val savedMeals: StateFlow<List<SavedMeals.Meal>> = repo.savedMealRows
        .map { SavedMeals.group(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Keeps the selected day's [slot] entries as a meal called [name]. */
    fun saveMeal(name: String, slot: String) = viewModelScope.launch {
        repo.saveMeal(name, dayState.value.rows.filter { it.meal == slot })
    }

    fun logSavedMeal(meal: SavedMeals.Meal, slot: String) = viewModelScope.launch {
        val day = _selectedDate.value
        meal.items.forEach { repo.logFood(day, it.foodId, slot, it.quantity) }
    }

    fun deleteSavedMeal(mealId: Long) = viewModelScope.launch { repo.deleteSavedMeal(mealId) }

    // ---------- Water ----------

    private fun waterFood(): FoodItem? = foods.value.firstOrNull { it.name == WATER_FOOD }

    fun addWater() {
        val water = waterFood() ?: return
        logFood(water.id, Meals.WATER, 1.0)
    }

    /** Takes back the most recent glass, which is the one a mis-tap just added. */
    fun removeWater() = viewModelScope.launch {
        dayState.value.rows.filter { it.meal == Meals.WATER }.maxByOrNull { it.loggedAt }
            ?.let { repo.removeFoodLog(it.logId) }
    }

    /**
     * Logs yesterday's [meal] again on the selected day: the same breakfast every
     * morning is the commonest diary there is, and the reason every food app has a
     * copy button. Returns how many entries were copied, zero if there was nothing.
     */
    fun copyMealFromDayBefore(meal: String, onDone: (Int) -> Unit = {}) = viewModelScope.launch {
        val day = _selectedDate.value
        val source = repo.foodLogFor(Dates.ofEpochDay(day.epochDay - 1)).first().filter { it.meal == meal }
        source.forEach { repo.logFood(day, it.foodId, meal, it.quantity) }
        onDone(source.size)
    }

    fun editFoodLog(logId: Long, quantity: Double, meal: String) = viewModelScope.launch {
        repo.editFoodLog(logId, quantity, meal)
    }

    fun saveFood(item: FoodItem) = viewModelScope.launch {
        if (item.id == 0L) repo.saveFood(item) else repo.updateFood(item)
    }

    // ---------- Body and settings ----------

    fun recordWeight(kg: Double) = viewModelScope.launch {
        if (kg > 0) repo.recordWeight(_selectedDate.value, kg)
    }

    // ---------- Tape measurements ----------

    val measurements: StateFlow<List<BodyMeasurement>> = repo.measurements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveMeasurements(cmByPart: Map<String, Double>) = viewModelScope.launch {
        // Always today: the dialog says so, and the week strip's selected day is
        // about the food and training log, not the tape.
        repo.saveMeasurements(Dates.todayEpochDay(), cmByPart)
    }

    /** Height and sex for the body-fat estimate, saved without touching anything else. */
    fun saveBody(heightCm: Double, isFemale: Boolean) = viewModelScope.launch {
        settingsStore.save(settingsStore.settings.first().copy(heightCm = heightCm, isFemale = isFemale))
    }

    // ---------- Export ----------

    enum class ExportKind(val subject: String) {
        SETS("Gym: every set"), FOOD("Gym: food log"), WEIGHTS("Gym: body weight")
    }

    /** Builds the CSV off the database and hands it to [share] on the main thread. */
    fun export(kind: ExportKind, share: (String, String) -> Unit) = viewModelScope.launch {
        val text = when (kind) {
            ExportKind.SETS -> Export.sets(repo.everySet())
            ExportKind.FOOD -> Export.food(repo.allFoodLog())
            ExportKind.WEIGHTS -> Export.weights(repo.weights.first())
        }
        share(kind.subject, text)
    }

    // ---------- First run ----------

    /**
     * Whether to show the welcome card. Starts false and only turns true once the
     * stored settings and both logs have actually loaded, so the card cannot flash
     * up on a cold start for someone who has been using the app for months.
     */
    val needsWelcome: StateFlow<Boolean> =
        combine(settingsStore.settings, repo.trainedDays, repo.loggedFoodDays) { s, trained, fuelled ->
            !s.welcomed && trained.isEmpty() && fuelled.isEmpty()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Takes the calculator's targets from the welcome card and closes it for good. */
    fun applyWelcomeTargets(t: Energy.Targets) = viewModelScope.launch {
        settingsStore.save(
            settingsStore.settings.first().copy(
                kcalTarget = t.kcal, proteinTargetG = t.proteinG, waterGlasses = t.waterGlasses, welcomed = true
            )
        )
    }

    fun dismissWelcome() = viewModelScope.launch {
        settingsStore.save(settingsStore.settings.first().copy(welcomed = true))
    }

    // ---------- Fasting ----------

    fun startFast(hours: Int) = viewModelScope.launch {
        // Read from the store, not the StateFlow: before anything collects it, that
        // holds the defaults, and saving a copy of them would wipe the real targets.
        settingsStore.save(settingsStore.settings.first().copy(fastStartedAt = Dates.nowMillis(), fastHours = hours))
    }

    fun endFast() = viewModelScope.launch {
        settingsStore.save(settingsStore.settings.first().copy(fastStartedAt = 0L))
    }

    fun saveSettings(updated: GymSettings) = viewModelScope.launch {
        settingsStore.save(updated)
        if (updated.remindersOn) {
            notifier.scheduleDailyReminder(updated.reminderHour, updated.reminderMinute)
        } else {
            notifier.cancelDailyReminder()
        }
        if (updated.waterReminders) notifier.scheduleWaterReminders() else notifier.cancelWaterReminders()
    }

    private fun lastSessionOf(history: List<SetRow>, activeSessionId: Long?): List<SetRow> {
        val earlier = history.filter { it.sessionId != activeSessionId && !it.isWarmup }
        val last = earlier.firstOrNull()?.sessionId ?: return emptyList()
        return earlier.filter { it.sessionId == last }.sortedBy { it.loggedAt }
    }

    private companion object {
        /** The seeded food the water card logs. */
        const val WATER_FOOD = "Water"

        /** Enough sets for years of one lift at a few sessions a week. */
        const val HISTORY_LIMIT = 2_000
    }
}
