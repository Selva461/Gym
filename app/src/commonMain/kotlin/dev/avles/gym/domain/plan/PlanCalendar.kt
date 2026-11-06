package dev.avles.gym.domain.plan

import dev.avles.gym.data.DayPlan
import dev.avles.gym.data.Meals
import dev.avles.gym.data.PlanFood

/** What kind of day a focus describes, so the calendar can colour it. Matched on words, in order. */
enum class FocusKind(vararg val words: String) {
    REST("rest", "off", "recovery", "deload"),
    CARDIO("cardio", "run", "walk", "cycle", "swim", "hiit", "conditioning"),
    LEGS("leg", "squat", "quad", "hamstring", "glute", "calf"),
    PUSH("push", "chest", "shoulder", "tricep"),
    PULL("pull", "back", "bicep", "deadlift"),
    UPPER("upper"),
    LOWER("lower"),
    FULL("full", "total", "whole"),
    CORE("core", "abs", "mobility", "stretch", "yoga"),
    OTHER;

    companion object {
        fun of(focus: String): FocusKind {
            val f = focus.lowercase()
            if (f.isBlank()) return OTHER
            // "Upper" and "Lower" before the muscle words, so "Upper (push focus)" reads as upper.
            listOf(REST, UPPER, LOWER, FULL).firstOrNull { k -> k.words.any { f.contains(it) } }?.let { return it }
            return entries.firstOrNull { k -> k.words.any { f.contains(it) } } ?: OTHER
        }
    }
}

/** Something on a day's timeline: the workout or one meal, from [startMin] to [endMin] after midnight. */
data class PlanBlock(
    val title: String,
    val detail: String,
    val startMin: Int,
    val endMin: Int,
    val isWorkout: Boolean,
    val meal: String = ""
)

/** A block with its lane: [lane] of [lanes] side-by-side columns, as calendars draw overlapping events. */
data class PlacedBlock(val block: PlanBlock, val lane: Int, val lanes: Int)

/**
 * Lays a planned day out as a calendar day: a block for the workout at its planned
 * time, a block per meal at the time the plan gave or the slot's usual time, and
 * overlapping blocks side by side. Pure, so the layout is unit tested.
 */
object PlanCalendar {

    /** Usual start and length, in minutes, for each slot when the plan gave no time. */
    val SLOT_TIMES: Map<String, Pair<Int, Int>> = mapOf(
        Meals.BREAKFAST to (8 * 60 to 30),
        Meals.LUNCH to (13 * 60 to 45),
        Meals.SNACK to (17 * 60 to 20),
        Meals.DINNER to (20 * 60 + 30 to 40),
        Meals.SUPPLEMENT to (21 * 60 + 30 to 15)
    )

    private const val WORK_SECONDS_PER_SET = 45
    private const val DEFAULT_REST = 90
    private const val WARM_UP_MIN = 10

    /** Roughly how long the planned workout takes, to the nearest 5 minutes, 20 to 150. */
    fun workoutMinutes(plan: DayPlan): Int {
        if (plan.lifts.isEmpty()) return 0
        val seconds = plan.lifts.sumOf { l ->
            val work = if (l.holdSeconds > 0) l.holdSeconds else WORK_SECONDS_PER_SET
            l.sets * (work + (if (l.restSeconds > 0) l.restSeconds else DEFAULT_REST))
        }
        val minutes = WARM_UP_MIN + seconds / 60
        return ((minutes + 4) / 5 * 5).coerceIn(20, 150)
    }

    /** The day's timed blocks, earliest first. A workout with no planned time is left out (it shows as all-day). */
    fun blocks(plan: DayPlan): List<PlanBlock> {
        val out = ArrayList<PlanBlock>()
        val start = plan.day?.workoutTime?.let(PlanValues::minutesOf)
        if (start != null && (plan.lifts.isNotEmpty() || plan.focus.isNotEmpty())) {
            val length = workoutMinutes(plan).takeIf { it > 0 } ?: 60
            out += PlanBlock(
                title = plan.focus.ifEmpty { "Workout" },
                detail = if (plan.lifts.isEmpty()) "" else "${plan.lifts.size} exercises · about $length min",
                startMin = start,
                endMin = (start + length).coerceAtMost(24 * 60),
                isWorkout = true
            )
        }
        plan.foods.groupBy { it.meal }.forEach { (meal, foods) ->
            val (usual, length) = SLOT_TIMES[meal] ?: (12 * 60 to 30)
            val at = foods.firstNotNullOfOrNull { PlanValues.minutesOf(it.time) } ?: usual
            out += PlanBlock(
                title = meal,
                detail = foods.joinToString(", ") { it.foodName },
                startMin = at,
                endMin = (at + length).coerceAtMost(24 * 60),
                isWorkout = false,
                meal = meal
            )
        }
        return out.sortedWith(compareBy({ it.startMin }, { !it.isWorkout }))
    }

    /** Side-by-side lanes for overlapping blocks: each cluster of overlaps shares its width equally. */
    fun layout(blocks: List<PlanBlock>): List<PlacedBlock> {
        val sorted = blocks.sortedBy { it.startMin }
        val result = ArrayList<PlacedBlock>()
        var cluster = ArrayList<Pair<PlanBlock, Int>>()
        var laneEnds = ArrayList<Int>()
        var clusterEnd = -1
        fun flush() {
            val lanes = laneEnds.size.coerceAtLeast(1)
            cluster.forEach { (b, lane) -> result += PlacedBlock(b, lane, lanes) }
            cluster = ArrayList(); laneEnds = ArrayList(); clusterEnd = -1
        }
        sorted.forEach { b ->
            if (cluster.isNotEmpty() && b.startMin >= clusterEnd) flush()
            val lane = laneEnds.indexOfFirst { it <= b.startMin }.let { if (it < 0) { laneEnds += 0; laneEnds.size - 1 } else it }
            laneEnds[lane] = maxOf(b.endMin, b.startMin + MIN_VISIBLE)
            cluster += b to lane
            clusterEnd = maxOf(clusterEnd, b.endMin, b.startMin + MIN_VISIBLE)
        }
        flush()
        return result
    }

    /** Blocks shorter than this are drawn at this length so their title fits, and lanes respect that. */
    const val MIN_VISIBLE = 30

    fun totals(foods: List<PlanFood>): Pair<Double, Double> = foods.sumOf { it.kcal } to foods.sumOf { it.proteinG }
}
