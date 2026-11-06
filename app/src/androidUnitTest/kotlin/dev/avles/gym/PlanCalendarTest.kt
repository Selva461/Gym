package dev.avles.gym

import dev.avles.gym.data.DayPlan
import dev.avles.gym.data.Meals
import dev.avles.gym.data.PlanDay
import dev.avles.gym.data.PlanFood
import dev.avles.gym.data.PlanLift
import dev.avles.gym.domain.plan.FocusKind
import dev.avles.gym.domain.plan.PlanBlock
import dev.avles.gym.domain.plan.PlanCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** How a planned day becomes calendar blocks: times, lengths, colours and side-by-side lanes. */
class PlanCalendarTest {

    private fun lift(sets: Int, rest: Int = 90) = PlanLift(dateEpochDay = 1, position = 0, exerciseName = "x", sets = sets, repsLow = 8, repsHigh = 8, restSeconds = rest)
    private fun food(meal: String, time: String = "") = PlanFood(dateEpochDay = 1, position = 0, meal = meal, foodName = meal + " food", quantity = 1.0, time = time)

    @Test
    fun `focus words map to a kind`() {
        assertEquals(FocusKind.LEGS, FocusKind.of("Legs"))
        assertEquals(FocusKind.PUSH, FocusKind.of("Chest & triceps"))
        assertEquals(FocusKind.PULL, FocusKind.of("Back and biceps"))
        assertEquals(FocusKind.UPPER, FocusKind.of("Upper (push focus)"))
        assertEquals(FocusKind.REST, FocusKind.of("Rest day"))
        assertEquals(FocusKind.CARDIO, FocusKind.of("HIIT"))
        assertEquals(FocusKind.OTHER, FocusKind.of("Sports"))
        assertEquals(FocusKind.OTHER, FocusKind.of(""))
    }

    @Test
    fun `workout and meals become blocks at their times`() {
        val plan = DayPlan(
            1, PlanDay(dateEpochDay = 1, focus = "Legs", workoutTime = "18:00"),
            listOf(lift(4), lift(3), lift(3)),
            listOf(food(Meals.BREAKFAST, "07:30"), food(Meals.BREAKFAST), food(Meals.DINNER), food(Meals.LUNCH))
        )
        val blocks = PlanCalendar.blocks(plan)
        assertEquals(listOf("Breakfast", "Lunch", "Legs", "Dinner"), blocks.map { it.title })
        assertEquals(7 * 60 + 30, blocks[0].startMin) // the time the plan gave
        assertEquals(13 * 60, blocks[1].startMin)     // the slot's usual time
        // 10 sets x (45 s + 90 s) = 22.5 min + 10 warm-up, to the next 5.
        assertEquals(35, PlanCalendar.workoutMinutes(plan))
        assertEquals(18 * 60 + 35, blocks[2].endMin)
    }

    @Test
    fun `a workout without a time is not on the timeline`() {
        val plan = DayPlan(1, PlanDay(dateEpochDay = 1, focus = "Push"), listOf(lift(3)), emptyList())
        assertTrue(PlanCalendar.blocks(plan).isEmpty())
    }

    @Test
    fun `overlapping blocks share the width, separate ones do not`() {
        val b = { s: Int, e: Int -> PlanBlock("t", "", s, e, false) }
        val placed = PlanCalendar.layout(listOf(b(600, 700), b(630, 660), b(800, 900), b(650, 720)))
        val byStart = placed.associateBy { it.block.startMin }
        assertEquals(3, byStart.getValue(600).lanes)
        assertEquals(setOf(0, 1, 2), listOf(600, 630, 650).map { byStart.getValue(it).lane }.toSet())
        assertEquals(1, byStart.getValue(800).lanes)
    }
}
