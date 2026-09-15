package dev.avles.gym.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.avles.gym.GymApp
import dev.avles.gym.data.BodyWeight
import dev.avles.gym.data.DayVolume
import dev.avles.gym.data.Exercise
import dev.avles.gym.data.FoodItem
import dev.avles.gym.data.FoodLogRow
import dev.avles.gym.data.GymSettings
import dev.avles.gym.data.Meals
import dev.avles.gym.data.SetRow
import dev.avles.gym.data.WorkoutSession
import dev.avles.gym.domain.MacroMath
import dev.avles.gym.domain.Progression
import dev.avles.gym.domain.Recommendation
import dev.avles.gym.domain.Totals
import dev.avles.gym.domain.Training
import dev.avles.gym.notify.Notifications
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
import java.time.LocalDate

data class DayState(
    val date: LocalDate = LocalDate.now(),
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
    val recentSets: List<SetRow> = emptyList()
)

data class ProgressState(
    val weights: List<BodyWeight> = emptyList(),
    val volume: List<DayVolume> = emptyList(),
    val trained: Set<Long> = emptySet(),
    val fuelled: Set<Long> = emptySet()
)

@OptIn(ExperimentalCoroutinesApi::class)
class GymViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as GymApp).repository
    private val settingsStore = (app as GymApp).settings

    private val _selectedDate = MutableStateFlow(LocalDate.now())
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
        if (!date.isAfter(LocalDate.now())) _selectedDate.value = date
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

    val settings: StateFlow<GymSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GymSettings())

    fun startSession(label: String) = viewModelScope.launch {
        repo.startSession(label, _selectedDate.value)
    }

    fun finishSession() = viewModelScope.launch {
        trainState.value.active?.let { repo.finishSession(it) }
        cancelRest()
    }

    fun logSet(exerciseId: Long, weightKg: Double, reps: Int, isWarmup: Boolean) =
        viewModelScope.launch {
            val session = trainState.value.active ?: return@launch
            val index = Training.nextSetIndex(trainState.value.sets, exerciseId)
            val newId = repo.logSet(session.id, exerciseId, index, weightKg, reps, isWarmup)

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

    fun addExercise(name: String, group: String) = viewModelScope.launch {
        if (name.isNotBlank()) repo.addExercise(name.trim(), group)
    }

    fun selectExercise(id: Long?) {
        _selectedExerciseId.value = id
    }

    /**
     * Recomputed whenever the lift changes or a set lands, because the history it
     * reads is a database flow. Log a set that hits the top of the range and the
     * target moves up while you are still standing there.
     */
    val guidance: StateFlow<LiftGuidance?> =
        combine(_selectedExerciseId, repo.exercises) { id, catalogue ->
            id?.let { wanted -> catalogue.firstOrNull { it.id == wanted } }
        }.flatMapLatest { exercise ->
            if (exercise == null) {
                flowOf(null)
            } else {
                repo.historyForExercise(exercise.id).map { history ->
                    val best = Training.bestEstimatedOneRepMax(history)
                    LiftGuidance(
                        exercise = exercise,
                        recommendation = Progression.recommend(exercise, history),
                        bestE1rm = best,
                        nextMilestone = Progression.nextMilestone(best),
                        milestoneProgress = Progression.milestoneProgress(best),
                        recentSets = history.take(5)
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
            Notifications.restFinished(getApplication<Application>(), lift)
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

    fun saveFood(item: FoodItem) = viewModelScope.launch {
        if (item.id == 0L) repo.saveFood(item) else repo.updateFood(item)
    }

    // ---------- Body and settings ----------

    fun recordWeight(kg: Double) = viewModelScope.launch {
        if (kg > 0) repo.recordWeight(_selectedDate.value, kg)
    }

    fun saveSettings(updated: GymSettings) = viewModelScope.launch {
        settingsStore.save(updated)
        val context = getApplication<Application>()
        if (updated.remindersOn) {
            Notifications.scheduleDailyReminder(context, updated.reminderHour, updated.reminderMinute)
        } else {
            Notifications.cancelDailyReminder(context)
        }
    }

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                GymViewModel(app) as T
        }
    }
}
