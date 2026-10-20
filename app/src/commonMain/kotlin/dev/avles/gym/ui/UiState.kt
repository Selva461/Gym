package dev.avles.gym.ui

import dev.avles.gym.core.Dates
import dev.avles.gym.data.BodyWeight
import dev.avles.gym.data.DayVolume
import dev.avles.gym.data.Exercise
import dev.avles.gym.data.FoodLogRow
import dev.avles.gym.data.GymSettings
import dev.avles.gym.data.ProgressPhoto
import dev.avles.gym.data.SessionSummary
import dev.avles.gym.data.SetRow
import dev.avles.gym.data.WorkoutSession
import dev.avles.gym.domain.Coach
import dev.avles.gym.domain.ExerciseGuide
import dev.avles.gym.domain.Patterns
import dev.avles.gym.domain.Physique
import dev.avles.gym.domain.Physiques
import dev.avles.gym.domain.Recommendation
import dev.avles.gym.domain.Totals
import kotlinx.datetime.LocalDate

/*
 * The state each screen renders, assembled by [GymViewModel]. Split out of the
 * ViewModel file to keep it under the size ArchitectureTest allows.
 */

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
    /** The least help ever used, on an assisted lift; null on every other lift. */
    val leastHelp: Double? = null,
    val nextMilestone: Double = 0.0,
    val milestoneProgress: Float = 0f,
    val recentSets: List<SetRow> = emptyList(),
    /**
     * The working sets from the last session this lift was done in, oldest first:
     * the "previous" column of every logging app, and the first thing anyone checks
     * before loading the bar.
     */
    val lastTime: List<SetRow> = emptyList(),
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
