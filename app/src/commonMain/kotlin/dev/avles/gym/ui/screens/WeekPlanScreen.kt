package dev.avles.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.avles.gym.core.Dates
import dev.avles.gym.core.epochDay
import dev.avles.gym.data.DayPlan
import dev.avles.gym.domain.plan.FocusKind
import dev.avles.gym.domain.plan.PlanBlock
import dev.avles.gym.domain.plan.PlanCalendar
import dev.avles.gym.domain.plan.PlanValues
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.PlanFileActions
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Coral
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Ink
import dev.avles.gym.ui.theme.Line
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Raised
import dev.avles.gym.ui.theme.Sage
import dev.avles.gym.ui.theme.Slate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.math.roundToInt

/** The calendar colour of a day's focus. One hue per kind, so a week reads at a glance. */
internal fun focusColor(focus: String): Color = when (FocusKind.of(focus)) {
    FocusKind.LEGS -> Brass
    FocusKind.PUSH -> Coral
    FocusKind.PULL -> Color(0xFF5B8DEF)
    FocusKind.UPPER -> Color(0xFF9B7BEA)
    FocusKind.LOWER -> Color(0xFF4FB3BF)
    FocusKind.FULL -> Sage
    FocusKind.CARDIO -> Color(0xFFE57AA8)
    FocusKind.CORE -> Color(0xFFD9C55B)
    FocusKind.REST -> Faint
    FocusKind.OTHER -> Mute
}

/**
 * The week plan as a calendar, laid out like a work calendar's week view: seven
 * day columns with each day's focus as an all-day event, today's date circled,
 * and the chosen day below as an hour-by-hour timeline of its workout and meals,
 * followed by the full list of what is planned. Import and the template live
 * here, so the whole plan round trip starts from one screen.
 */
@Composable
fun WeekPlanScreen(
    weekStart: LocalDate,
    week: List<DayPlan>,
    files: PlanFileActions?,
    unitOf: (Long?) -> String?,
    onShiftWeek: (Int) -> Unit,
    onThisWeek: () -> Unit,
    onClearWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = Dates.today()
    var selectedDay by rememberSaveable { mutableStateOf(today.epochDay) }
    if (week.isNotEmpty() && week.none { it.dateEpochDay == selectedDay }) {
        selectedDay = week.firstOrNull { it.dateEpochDay == today.epochDay }?.dateEpochDay ?: week.first().dateEpochDay
    }
    var openBlock by remember { mutableStateOf<PlanBlock?>(null) }
    var confirmClear by remember { mutableStateOf(false) }
    val selected = week.firstOrNull { it.dateEpochDay == selectedDay }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        val end = weekStart.plus(DatePeriod(days = 6))
        Text("Week plan", style = MaterialTheme.typography.headlineMedium, color = Bone)
        Text(
            "${Dates.dayAndMonth(weekStart)} – ${Dates.dayAndMonth(end)} ${end.year}",
            style = MaterialTheme.typography.bodyMedium, color = Mute
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onShiftWeek(-1) }) { Text("‹ Prev", color = Mute) }
            TextButton(onClick = { onThisWeek(); selectedDay = today.epochDay }) { Text("This week", color = Brass) }
            TextButton(onClick = { onShiftWeek(1) }) { Text("Next ›", color = Mute) }
        }
        if (files != null) {
            Row {
                TextButton(onClick = files.pickPlan) { Text("Import plan", color = Sage) }
                TextButton(onClick = files.saveTemplate) { Text("Get the Excel template", color = Sage) }
            }
        }
        Spacer(Modifier.height(8.dp))
        WeekGrid(week, today, selectedDay) { selectedDay = it }
        Spacer(Modifier.height(18.dp))
        Hairline()
        Spacer(Modifier.height(16.dp))

        if (week.all { it.isEmpty }) {
            EmptyNote(
                "Nothing planned this week. Get the Excel template, give it to Claude to plan your " +
                    "training and meals, then import the filled file here."
            )
        }
        if (selected != null) DayDetail(selected, unitOf) { openBlock = it }
        if (week.any { !it.isEmpty }) {
            Spacer(Modifier.height(18.dp))
            TextButton(onClick = { confirmClear = true }) { Text("Clear this week's plan", color = Faint) }
        }
    }

    openBlock?.let { block ->
        val plan = selected
        AlertDialog(
            onDismissRequest = { openBlock = null },
            containerColor = Slate,
            title = { Text(block.title, color = Bone) },
            text = {
                Column {
                    Text("${clock(block.startMin)} – ${clock(block.endMin)}", color = Mute, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(8.dp))
                    if (block.isWorkout && plan != null) plan.lifts.forEach { LiftLine(it.exerciseName, PlanValues.liftLine(it.sets, it.repsLow, it.repsHigh, it.holdSeconds, it.weightKg, it.restSeconds), it.notes) }
                    else plan?.foods?.filter { it.meal == block.meal }?.forEach { FoodLine(it, unitOf(it.foodId)) }
                }
            },
            confirmButton = { TextButton(onClick = { openBlock = null }) { Text("Close", color = Mute) } }
        )
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            containerColor = Slate,
            title = { Text("Clear this week?", color = Bone) },
            text = { Text("The plan for these seven days is removed. Nothing you logged is touched.", color = Mute) },
            confirmButton = { TextButton(onClick = { confirmClear = false; onClearWeek() }) { Text("Clear", color = Coral) } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel", color = Mute) } }
        )
    }
}

/** Seven columns, Monday first: weekday, date (today circled), and the day's focus as an all-day event. */
@Composable
private fun WeekGrid(week: List<DayPlan>, today: LocalDate, selectedDay: Long, onSelect: (Long) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        week.forEach { plan ->
            val date = Dates.ofEpochDay(plan.dateEpochDay)
            val isToday = date == today
            val isSelected = plan.dateEpochDay == selectedDay
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Raised else Color.Transparent)
                    .clickable { onSelect(plan.dateEpochDay) }
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(PlanValues.shortWeekday(date), style = MaterialTheme.typography.labelSmall, color = if (isToday) Brass else Mute)
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(if (isToday) Brass else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isToday) Ink else Bone,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Spacer(Modifier.height(6.dp))
                FocusTile(plan)
            }
        }
    }
}

/** The all-day event: a tinted tile with a coloured edge, as a calendar draws one. */
@Composable
private fun FocusTile(plan: DayPlan) {
    val label = plan.focus.ifEmpty { if (plan.lifts.isNotEmpty()) "Workout" else "" }
    val tint = if (label.isEmpty()) Line else focusColor(label)
    Row(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(if (label.isEmpty()) Color.Transparent else tint.copy(alpha = 0.20f))
            .border(1.dp, if (label.isEmpty()) Line else Color.Transparent, RoundedCornerShape(5.dp))
    ) {
        if (label.isNotEmpty()) Box(Modifier.width(3.dp).fillMaxSize().background(tint))
        Text(
            label,
            modifier = Modifier.padding(horizontal = 3.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
            color = if (label.isEmpty()) Faint else Bone,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** One day: heading with totals, an hour-by-hour timeline, then everything planned in full. */
@Composable
private fun DayDetail(plan: DayPlan, unitOf: (Long?) -> String?, onOpen: (PlanBlock) -> Unit) {
    val date = Dates.ofEpochDay(plan.dateEpochDay)
    val (kcal, protein) = PlanCalendar.totals(plan.foods)
    SectionHeading(
        title = "${Dates.dayName(date)} ${Dates.dayAndMonth(date)}",
        trailing = plan.focus.ifEmpty { null },
        accent = focusColor(plan.focus)
    )
    if (plan.foods.isNotEmpty()) {
        Text("Planned food: ${kcal.roundToInt()} kcal · ${protein.roundToInt()} g protein", style = MaterialTheme.typography.labelSmall, color = Sage)
    }
    plan.day?.notes?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Mute) }
    if (plan.isEmpty) {
        Spacer(Modifier.height(8.dp))
        EmptyNote("Nothing planned for this day.")
        return
    }
    val blocks = PlanCalendar.blocks(plan)
    if (plan.lifts.isNotEmpty() && blocks.none { it.isWorkout }) {
        Spacer(Modifier.height(8.dp))
        Text(
            "All day: ${plan.focus.ifEmpty { "Workout" }} · ${plan.lifts.size} exercises (no time planned)",
            style = MaterialTheme.typography.labelMedium, color = focusColor(plan.focus)
        )
    }
    if (blocks.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        Timeline(blocks, plan.focus, onOpen)
    }
    if (plan.lifts.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        SectionHeading("Workout", trailing = "about ${PlanCalendar.workoutMinutes(plan)} min", accent = Brass)
        Spacer(Modifier.height(6.dp))
        plan.lifts.forEach { LiftLine(it.exerciseName, PlanValues.liftLine(it.sets, it.repsLow, it.repsHigh, it.holdSeconds, it.weightKg, it.restSeconds), it.notes) }
    }
    if (plan.foods.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        SectionHeading("Meals", trailing = "${kcal.roundToInt()} kcal", accent = Sage)
        plan.foods.groupBy { it.meal }.forEach { (meal, foods) ->
            Spacer(Modifier.height(8.dp))
            val (k, p) = PlanCalendar.totals(foods)
            Text("$meal · ${k.roundToInt()} kcal · ${p.roundToInt()} g protein", style = MaterialTheme.typography.labelMedium, color = Mute)
            foods.forEach { FoodLine(it, unitOf(it.foodId)) }
        }
    }
}

private val HOUR_HEIGHT = 44.dp
private val GUTTER = 42.dp

/** The day view: hour lines down the side, events placed by time, overlapping ones side by side. */
@Composable
private fun Timeline(blocks: List<PlanBlock>, focus: String, onOpen: (PlanBlock) -> Unit) {
    val firstHour = minOf(6, blocks.minOf { it.startMin } / 60)
    val lastHour = maxOf(22, (blocks.maxOf { it.endMin } + 59) / 60).coerceAtMost(24)
    val hours = lastHour - firstHour
    val placed = PlanCalendar.layout(blocks)
    BoxWithConstraints(Modifier.fillMaxWidth().height(HOUR_HEIGHT * hours)) {
        val laneArea = maxWidth - GUTTER
        for (h in 0 until hours) {
            Row(Modifier.offset(y = HOUR_HEIGHT * h).fillMaxWidth()) {
                Text(
                    PlanValues.hhmm(firstHour + h, 0),
                    modifier = Modifier.width(GUTTER).offset(y = (-6).dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Faint
                )
                Box(Modifier.fillMaxWidth().height(1.dp).background(Line))
            }
        }
        placed.forEach { p ->
            val b = p.block
            val top = HOUR_HEIGHT * ((b.startMin - firstHour * 60) / 60f)
            val length = maxOf(b.endMin - b.startMin, PlanCalendar.MIN_VISIBLE)
            val blockHeight = HOUR_HEIGHT * (length / 60f) - 2.dp
            val laneWidth = laneArea / p.lanes
            val color = if (b.isWorkout) focusColor(focus) else Sage
            Row(
                Modifier
                    .offset(x = GUTTER + laneWidth * p.lane, y = top + 1.dp)
                    .width(laneWidth - 2.dp)
                    .height(blockHeight)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color.copy(alpha = 0.22f))
                    .clickable { onOpen(b) }
            ) {
                Box(Modifier.width(3.dp).fillMaxSize().background(color))
                Column(Modifier.padding(horizontal = 6.dp, vertical = 3.dp)) {
                    Text(
                        "${clock(b.startMin)} ${b.title}",
                        style = MaterialTheme.typography.labelMedium, color = Bone, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    if (b.detail.isNotEmpty()) {
                        Text(
                            b.detail,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Mute,
                            maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun LiftLine(name: String, line: String, notes: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(name, style = MaterialTheme.typography.bodyMedium, color = Bone)
        Text(line, style = MaterialTheme.typography.labelSmall, color = Brass)
        if (notes.isNotBlank()) Text(notes, style = MaterialTheme.typography.labelSmall, color = Faint)
    }
}

@Composable
internal fun FoodLine(food: dev.avles.gym.data.PlanFood, unit: String?) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                "${PlanValues.trim(food.quantity)} × ${food.foodName}",
                style = MaterialTheme.typography.bodyMedium, color = if (food.foodId == null) Mute else Bone
            )
            val detail = listOfNotNull(unit?.let { "1 = $it" }, food.time.ifEmpty { null }, food.notes.ifEmpty { null }).joinToString(" · ")
            if (detail.isNotEmpty()) Text(detail, style = MaterialTheme.typography.labelSmall, color = Faint)
        }
        Text(
            "${food.kcal.roundToInt()} kcal · ${food.proteinG.roundToInt()} p",
            style = MaterialTheme.typography.labelSmall, color = Sage, textAlign = TextAlign.End
        )
    }
}

private fun clock(minutes: Int): String = PlanValues.hhmm((minutes / 60).coerceAtMost(23), if (minutes >= 24 * 60) 59 else minutes % 60)
