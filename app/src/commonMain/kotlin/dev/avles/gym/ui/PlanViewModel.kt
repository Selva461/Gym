package dev.avles.gym.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.avles.gym.core.Dates
import dev.avles.gym.core.epochDay
import dev.avles.gym.data.DayPlan
import dev.avles.gym.data.GymRepository
import dev.avles.gym.data.PlanFood
import dev.avles.gym.data.PlanRepository
import dev.avles.gym.data.SettingsStore
import dev.avles.gym.domain.plan.PlanCatalogue
import dev.avles.gym.domain.plan.PlanImport
import dev.avles.gym.domain.plan.PlanImportResult
import dev.avles.gym.domain.plan.PlanTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** Where an import stands: idle, reading the file, showing what it found, or finished with a message. */
sealed class PlanImportState {
    data object Idle : PlanImportState()
    data object Reading : PlanImportState()
    data class Preview(val result: PlanImportResult) : PlanImportState()
    data class Done(val message: String) : PlanImportState()
}

/**
 * The week plan: the calendar's week, the day shown on Today, the spreadsheet
 * template and the import. Its own view model so the plan grows without touching
 * [GymViewModel], which is at the file-size cap. File bytes arrive from the
 * platform pickers; parsing and template building run on [Dispatchers.Default].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlanViewModel(
    private val plans: PlanRepository,
    private val repo: GymRepository,
    private val settings: SettingsStore
) : ViewModel() {

    private val _weekStart = MutableStateFlow(mondayOf(Dates.today()))
    val weekStart: StateFlow<LocalDate> = _weekStart.asStateFlow()

    /** The seven days of the week on screen, Monday first. */
    val week: StateFlow<List<DayPlan>> = _weekStart
        .flatMapLatest { start -> plans.range(start.epochDay, start.epochDay + 6) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _focusDate = MutableStateFlow(Dates.today())

    /** The plan for the day the Today screen is showing. */
    val dayPlan: StateFlow<DayPlan?> = _focusDate
        .flatMapLatest { d -> plans.range(d.epochDay, d.epochDay).map { it.firstOrNull() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _import = MutableStateFlow<PlanImportState>(PlanImportState.Idle)
    val importState: StateFlow<PlanImportState> = _import.asStateFlow()

    fun showDay(date: LocalDate) { _focusDate.value = date }

    fun shiftWeek(weeks: Int) { _weekStart.value = _weekStart.value.plus(DatePeriod(days = 7 * weeks)) }

    fun thisWeek() { _weekStart.value = mondayOf(Dates.today()) }

    /** The template's bytes, with this phone's catalogue and targets in it. */
    suspend fun templateBytes(): ByteArray {
        val today = Dates.today()
        val input = PlanTemplate.Input(
            today = today,
            exercises = repo.exercises.first(),
            weightKg = repo.weights.first().maxByOrNull { it.dateEpochDay }?.weightKg
        )
        return withContext(Dispatchers.Default) { PlanTemplate.build(input) }
    }

    /** A file the person picked: [bytes] to check, or [error] when it could not be read at all. */
    fun onFile(bytes: ByteArray?, error: String?) {
        if (bytes == null) {
            _import.value = error?.let { PlanImportState.Done(it) } ?: PlanImportState.Idle
            return
        }
        _import.value = PlanImportState.Reading
        viewModelScope.launch {
            val catalogue = PlanCatalogue(repo.exercises.first())
            val result = withContext(Dispatchers.Default) { PlanImport.read(bytes, catalogue, Dates.today()) }
            _import.value = when {
                result.fatal != null -> PlanImportState.Done(result.fatal)
                result.isEmpty -> PlanImportState.Done(
                    "Nothing to import: the plan sheets are empty." +
                        if (result.problems.isNotEmpty()) " ${result.problems.size} row(s) had problems:\n" +
                            result.problems.take(8).joinToString("\n") else ""
                )
                else -> PlanImportState.Preview(result)
            }
        }
    }

    /**
     * Saves the previewed plan, replacing the dates it covers, and saves each split it
     * defines under Train > Your splits (a split with the same name is updated, so
     * importing again does not make duplicates). With [startToday] the whole plan is
     * moved so its first day is today. The calendar then jumps to the plan's first week.
     */
    fun confirmImport(startToday: Boolean) {
        val preview = _import.value as? PlanImportState.Preview ?: return
        val r = if (startToday) preview.result.startingOn(Dates.todayEpochDay()) else preview.result
        _import.value = PlanImportState.Reading
        viewModelScope.launch {
            val message = runCatching {
                plans.replace(r.dates, r.days, r.lifts, emptyList())
                val saved = saveSplits(r)
                "Imported ${r.summary()}." +
                    (if (saved > 0) " $saved ${if (saved == 1) "split is" else "splits are"} in Train > Your splits." else "") +
                    if (r.skippedCount > 0) " ${r.skippedCount} row(s) were skipped." else ""
            }.getOrElse { "The plan could not be saved: ${it.message}" }
            r.dates.minOrNull()?.let { _weekStart.value = mondayOf(Dates.ofEpochDay(maxOf(it, Dates.todayEpochDay()))) }
            _import.value = PlanImportState.Done(message)
        }
    }

    /** Creates or updates one split per planned split day; returns how many were saved. */
    private suspend fun saveSplits(r: PlanImportResult): Int {
        val existing = repo.splits.first()
        var next = existing.size
        var saved = 0
        r.splits.forEach { split ->
            val ids = split.exerciseIds
            if (ids.isEmpty()) return@forEach
            val match = existing.firstOrNull { it.name.trim().equals(split.name.trim(), ignoreCase = true) }
            repo.saveSplit(match?.id ?: 0L, split.name, match?.position ?: next++, ids)
            saved++
        }
        return saved
    }

    fun dismissImport() { _import.value = PlanImportState.Idle }

    fun note(message: String) { _import.value = PlanImportState.Done(message) }

    /** Logs planned foods that matched the catalogue, on their planned day (never a future one). */
    fun logPlanned(foods: List<PlanFood>) = viewModelScope.launch {
        val today = Dates.todayEpochDay()
        foods.forEach { f ->
            val id = f.foodId ?: return@forEach
            if (f.dateEpochDay <= today) repo.logFood(Dates.ofEpochDay(f.dateEpochDay), id, f.meal, f.quantity)
        }
    }

    /** Removes the plan for the week on screen. */
    fun clearWeek() = viewModelScope.launch {
        val start = _weekStart.value.epochDay
        plans.clear((start..start + 6).toSet())
    }

    private companion object {
        fun mondayOf(date: LocalDate): LocalDate = date.minus(DatePeriod(days = date.dayOfWeek.ordinal))
    }
}
