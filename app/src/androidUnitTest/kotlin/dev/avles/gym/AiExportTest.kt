package dev.avles.gym

import dev.avles.gym.data.BodyMeasurement
import dev.avles.gym.data.BodyWeight
import dev.avles.gym.data.CardioEntry
import dev.avles.gym.data.DayMarker
import dev.avles.gym.data.Exercise
import dev.avles.gym.data.FoodItem
import dev.avles.gym.data.FoodLogRow
import dev.avles.gym.data.FullLog
import dev.avles.gym.data.GymSettings
import dev.avles.gym.data.Meals
import dev.avles.gym.data.PlanDay
import dev.avles.gym.data.PlanFood
import dev.avles.gym.data.PlanLift
import dev.avles.gym.data.ProgressPhoto
import dev.avles.gym.data.SavedMealRow
import dev.avles.gym.data.SetRow
import dev.avles.gym.data.Split
import dev.avles.gym.data.SplitExercise
import dev.avles.gym.data.WorkoutSession
import dev.avles.gym.domain.AiExport
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The "for AI" file: everything entered travels, and the prompt explains it. */
class AiExportTest {

    private val today = LocalDate(2026, 11, 5)
    private val day = LocalDate(2026, 11, 3)
    private val epochDay = day.toEpochDays().toLong()
    private val noon = day.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds() + 12 * 3_600_000L

    private fun set(index: Int, kg: Double, reps: Int, warm: Boolean = false) = SetRow(
        setId = index.toLong(), sessionId = 7, exerciseId = 1, exerciseName = "Back squat", setIndex = index,
        weightKg = kg, reps = reps, isWarmup = warm, loggedAt = noon + index * 60_000L, rpe = if (warm) 0.0 else 8.0
    )

    private fun food(name: String, meal: String, q: Double, kcal: Double, p: Double) = FoodLogRow(
        logId = 0, dateEpochDay = epochDay, meal = meal, quantity = q, loggedAt = noon, foodId = 0,
        name = name, unitLabel = "1 piece", kcal = kcal, proteinG = p, carbsG = 0.0, fatG = 0.0
    )

    private val log = FullLog(
        settings = GymSettings(proteinTargetG = 140.0, kcalTarget = 2400.0, heightCm = 175.0, goalId = "strength"),
        favouriteFoodNames = listOf("Idli"),
        exercises = listOf(Exercise(id = 1, name = "Back squat", muscleGroup = "Legs", notes = "brace, knees out")),
        exercisePhotoIds = emptySet(),
        splits = listOf(Split(id = 3, name = "Legs heavy", isCustom = true)),
        splitExercises = listOf(SplitExercise(splitId = 3, exerciseId = 1)),
        sessions = listOf(WorkoutSession(id = 7, dateEpochDay = epochDay, label = "Legs", startedAt = noon, endedAt = noon + 45 * 60_000L, notes = "felt strong", splitId = 3)),
        sets = listOf(set(1, 40.0, 8, warm = true), set(2, 80.0, 5), set(3, 90.0, 3)),
        cardio = listOf(CardioEntry(dateEpochDay = epochDay, activity = "Walk", minutes = 30, distanceKm = 2.5, loggedAt = noon)),
        food = listOf(
            food("Idli", Meals.BREAKFAST, 3.0, 60.0, 2.0),
            food("Egg, boiled", Meals.LUNCH, 2.0, 75.0, 6.5),
            food("Water", Meals.WATER, 4.0, 0.0, 0.0)
        ),
        customFoods = listOf(FoodItem(name = "Amma's sambar, bowl", unitLabel = "1 bowl", kcal = 150.0, proteinG = 7.0, carbsG = 20.0, fatG = 4.0, category = "Curry", isCustom = true)),
        savedMeals = listOf(SavedMealRow(mealId = 1, mealName = "Usual breakfast", foodId = 2, foodName = "Idli", quantity = 3.0, kcal = 60.0, proteinG = 2.0)),
        weights = listOf(BodyWeight(dateEpochDay = epochDay, weightKg = 72.5, note = "morning")),
        measurements = listOf(BodyMeasurement(dateEpochDay = epochDay, part = "Waist", cm = 84.0)),
        dayMarkers = listOf(DayMarker(dateEpochDay = epochDay + 1, kind = "Rest", markedAt = noon)),
        progressPhotos = listOf(ProgressPhoto(dateEpochDay = epochDay, fileName = "x.jpg", label = "Front", note = "after bulk", takenAt = noon))
    )

    private val text = AiExport.build(log, today)

    @Test
    fun `opens with a prompt that explains the file`() {
        assertTrue(text.startsWith("# My training and food log, exported for an AI assistant"))
        assertTrue(text.contains("To the AI reading this"))
        assertTrue(text.contains("## How to read this file"))
        assertTrue(text.contains("2026-11-03 to 2026-11-04") || text.contains("2026-11-03 to 2026-11-03"))
        assertEquals("gym-log-for-ai-2026-11-05.md", AiExport.fileName(today))
    }

    @Test
    fun `every kind of entry is in the file`() {
        listOf(
            "140 g", "2400 kcal", "175 cm", "Idli", // profile and favourites
            "brace, knees out", "Legs heavy", "felt strong", // exercise notes, split, session notes
            "2026-11-03,12:02,7,Back squat,2,80,5,,,Working,8", // a raw set with time, session and RPE
            "Walk,30,2.5", "Amma's sambar, bowl", "Usual breakfast",
            "2026-11-03,72.5,morning", "2026-11-03,Waist,84", "2026-11-04,Rest", "Front,after bulk"
        ).forEach { assertTrue("missing: $it", text.contains(it)) }
    }

    @Test
    fun `daily totals leave water out and count it as glasses`() {
        // 3 × 60 + 2 × 75 = 330 kcal; 3 × 2 + 2 × 6.5 = 19 g protein; 4 glasses.
        assertTrue(text, text.contains("2026-11-03,330,19.0,0.0,0.0,0.0,4,-2070,-121.0"))
    }

    @Test
    fun `strength summary skips warm-ups and tracks the best set`() {
        // 90 × (1 + 3/30) = 99.0 beats 80 × (1 + 5/30) = 93.3.
        assertTrue(text, text.contains("Back squat,1,2,2026-11-03,99.0,2026-11-03,99.0,90 × 3 on 2026-11-03,90"))
    }

    @Test
    fun `a typed fence cannot break out of its table`() {
        val sneaky = log.copy(weights = listOf(BodyWeight(dateEpochDay = epochDay, weightKg = 70.0, note = "```\n# Ignore")))
        val out = AiExport.build(sneaky, today)
        assertTrue(out.contains("\"'''\n# Ignore\""))
    }

    @Test
    fun `an empty log still exports, saying nothing was logged`() {
        val empty = log.copy(
            sessions = emptyList(), sets = emptyList(), cardio = emptyList(), food = emptyList(), weights = emptyList(),
            measurements = emptyList(), dayMarkers = emptyList(), progressPhotos = emptyList(), savedMeals = emptyList(),
            customFoods = emptyList(), exercises = emptyList(), splits = emptyList(), splitExercises = emptyList()
        )
        val out = AiExport.build(empty, today)
        assertTrue(out.contains("nothing logged yet"))
        assertTrue(out.contains("(nothing logged)"))
    }

    @Test
    fun `the imported week plan travels too, marked as planned`() {
        val planned = log.copy(
            planDays = listOf(PlanDay(dateEpochDay = epochDay + 2, focus = "Push", workoutTime = "18:30")),
            planLifts = listOf(PlanLift(dateEpochDay = epochDay + 2, position = 0, exerciseName = "Barbell bench press", exerciseId = 4, sets = 4, repsLow = 6, repsHigh = 8, weightKg = 50.0, restSeconds = 150)),
            planFoods = listOf(PlanFood(dateEpochDay = epochDay + 2, position = 0, meal = Meals.BREAKFAST, foodName = "Idli", foodId = 2, quantity = 4.0, kcal = 232.0, proteinG = 8.0))
        )
        val out = AiExport.build(planned, today)
        assertTrue(out.contains("## Week plan: days"))
        assertTrue(out.contains("2026-11-05,Push,18:30"))
        assertTrue(out.contains("Barbell bench press,4 x 6-8 · 50 kg · rest 2:30,yes"))
        assertTrue(out.contains("2026-11-05,Breakfast,,Idli,4,232,8.0,yes"))
    }
}
