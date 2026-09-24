package dev.avles.gym.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import dev.avles.gym.domain.MacroMath
import dev.avles.gym.data.DayKinds
import dev.avles.gym.data.DayMarker
import dev.avles.gym.domain.Coach
import dev.avles.gym.domain.Patterns
import dev.avles.gym.domain.Physique
import dev.avles.gym.domain.Physiques
import dev.avles.gym.domain.ExerciseGuide
import dev.avles.gym.domain.Photos
import dev.avles.gym.domain.Progression
import dev.avles.gym.domain.Recommendation
import dev.avles.gym.domain.Totals
import dev.avles.gym.domain.Training
import dev.avles.gym.notify.Notifier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import kotlinx.datetime.LocalDate

data class DayState(
    val date: LocalDate = Dates.today(),
    val rows: List<FoodLogRow> = emptyList(),
    val totals: Totals = Totals(),
    val byMeal: Map<String, Totals> = emptyMap(),
    val sessions: List<WorkoutSession> = emptyList(),
    val settings: GymSettings = GymSettings()
)

data class CalendarDots(
    val trained: Set<Long> = emptySet(),
    val fuelled: Set<Long> = emptySet()
)

data class TrainState(
    val active: WorkoutSession? = null,
    val sets: List<SetRow> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
    val recent: List<WorkoutSession> = emptyList()
)

/**
 * Everything the Train screen needs to say about the lift you have picked: what to
 * try next, where your estimated max sits, and the round number above it.
 *
 * Assembled in the ViewModel rather than in the screen because it is a join across
 * the exercise catalogue and that lift's history, and a composable is the wrong
 * place to be doing joins.
 */
data class LiftGuidance(
    val exercise: Exercise,
    val recommendation: Recommendation? = null,
    val bestE1rm: Double = 0.0,
    val nextMilestone: Double = 0.0,
    val milestoneProgress: Float = 0f,
    val recentSets: List<SetRow> = emptyList(),
    /** How the next set will be performed. Empty means unspecified. */
    val variation: String = "",
    /**
     * The lift's reference — equipment, muscles, how to do it, what goes wrong, and
     * which variation axes it offers. Derived from the exercise row, so it costs a
     * computation rather than a query and is available for custom lifts too.
     */
    val guide: ExerciseGuide? = null
)

/** A past session opened for inspection: what it was, and every set on it. */
data class SessionDetail(
    val session: WorkoutSession,
    val sets: List<SetRow>,
    val summary: SessionSummary? = null
)

/**
 * The photo timeline, with each row's file resolved once here rather than in the
 * composable — a screen should not be doing filesystem work while it lays out.
 *
 * [comparison] is the oldest against the newest, which is the comparison worth
 * showing: adjacent weeks look identical, and the distance travelled is the whole
 * point of taking these.
 */
data class PhotoState(
    val photos: List<PhotoRow> = emptyList(),
    val comparison: Pair<PhotoRow, PhotoRow>? = null,
    val bytesUsed: Long = 0L
)

/** One photo, with the path of the file it lives in. A null [path] means the name was unsafe. */
data class PhotoRow(val photo: ProgressPhoto, val path: String?)

/**
 * What the last seven days of training did for the chosen goal.
 *
 * Assembled here rather than in the screen because it is a join across the
 * catalogue, the session dates and every logged set — and because the screen
 * should be able to render it without knowing how a week is counted.
 */
data class CoachState(
    val physique: Physique = Physiques.BALANCED,
    val review: List<Coach.MuscleWeek> = emptyList(),
    val suggestions: List<Coach.Suggestion> = emptyList(),
    val verdict: String = "",
    /** Rest and missed days, so the screen can say why a quiet week was quiet. */
    val restDays: Int = 0,
    val missedDays: Int = 0
)

/**
 * The shape of your weeks: what you ate, when you trained, and which weekday you
 * quietly skip. Thirty days, because a week is too short to show a pattern and a
 * year is too long to show a change.
 */
data class PatternState(
    val kcal: List<Double?> = emptyList(),
    val protein: List<Double?> = emptyList(),
    val kcalMean: List<Double?> = emptyList(),
    val proteinMean: List<Double?> = emptyList(),
    val weekdays: List<Patterns.DayOfWeek> = emptyList(),
    val weakestWeekday: Patterns.DayOfWeek? = null,
    val kcalAdherence: Float = 0f,
    val proteinAdherence: Float = 0f,
    val kcalTarget: Double = 0.0,
    val proteinTarget: Double = 0.0,
    val windowDays: Int = 30
)

data class ProgressState(
    val weights: List<BodyWeight> = emptyList(),
    val volume: List<DayVolume> = emptyList(),
    val trained: Set<Long> = emptySet(),
    val fuelled: Set<Long> = emptySet()
)

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

    val quickPicks: StateFlow<List<FoodItem>> = repo.quickPicks()
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

    fun finishSession() = viewModelScope.launch {
        trainState.value.active?.let { repo.finishSession(it) }
        cancelRest()
    }

    fun logSet(
        exerciseId: Long,
        weightKg: Double,
        reps: Int,
        isWarmup: Boolean,
        variation: String = _selectedVariation.value
    ) =
        viewModelScope.launch {
            val session = trainState.value.active ?: return@launch
            val index = Training.nextSetIndex(trainState.value.sets, exerciseId)
            val newId = repo.logSet(session.id, exerciseId, index, weightKg, reps, isWarmup, variation)

            if (!isWarmup) {
                val history = repo.historyForExercise(exerciseId).first()
                val logged = history.firstOrNull { it.setId == newId }
                if (logged != null && Training.isPersonalRecord(logged, history)) {
                    _lastPrSetId.value = newId
                }
                startRest(
                    exerciseName = logged?.exerciseName
                        ?: trainState.value.exercises.firstOrNull { it.id == exerciseId }?.name
                )
            }
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
        variation: String = ""
    ) =
        viewModelScope.launch {
            repo.editSet(setId, weightKg, reps, isWarmup, variation)
            if (_lastPrSetId.value == setId) _lastPrSetId.value = null
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
                    LiftGuidance(
                        exercise = exercise,
                        recommendation = Progression.recommend(exercise, history, variation),
                        bestE1rm = best,
                        nextMilestone = Progression.nextMilestone(best),
                        milestoneProgress = Progression.milestoneProgress(best),
                        recentSets = history.take(5),
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
    fun startRest(seconds: Int? = null, exerciseName: String? = null) {
        restJob?.cancel()
        val total = seconds ?: settings.value.restSeconds
        val lift = exerciseName
            ?: trainState.value.sets.lastOrNull()?.exerciseName
            ?: "the next lift"
        restJob = viewModelScope.launch {
            var left = total
            while (left > 0) {
                _restRemaining.value = left
                delay(1_000)
                left--
            }
            _restRemaining.value = null
            notifier.restFinished(lift)
        }
    }

    fun cancelRest() {
        restJob?.cancel()
        _restRemaining.value = null
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

    fun saveSettings(updated: GymSettings) = viewModelScope.launch {
        settingsStore.save(updated)
        if (updated.remindersOn) {
            notifier.scheduleDailyReminder(updated.reminderHour, updated.reminderMinute)
        } else {
            notifier.cancelDailyReminder()
        }
    }
}
