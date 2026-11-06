package dev.avles.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.avles.gym.core.Dates
import dev.avles.gym.data.DayPlan
import dev.avles.gym.data.FoodLogRow
import dev.avles.gym.data.PlanFood
import dev.avles.gym.domain.plan.PlanCalendar
import dev.avles.gym.domain.plan.PlanImportResult
import dev.avles.gym.domain.plan.PlanValues
import dev.avles.gym.ui.PlanImportState
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Coral
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Sage
import dev.avles.gym.ui.theme.Slate
import kotlin.math.roundToInt

/**
 * The plan for the day on the Today screen: the focus and every planned lift with
 * its sets, reps and weight, then each meal's planned foods with a one-tap "Log"
 * for the ones the catalogue knows. With no plan it points at the week plan.
 */
@Composable
fun TodayPlanCard(
    plan: DayPlan?,
    isToday: Boolean,
    trainedToday: Boolean,
    logged: List<FoodLogRow>,
    unitOf: (Long?) -> String?,
    onOpenWeek: () -> Unit,
    onStartWorkout: (String) -> Unit,
    onLogFoods: (List<PlanFood>) -> Unit
) {
    if (plan == null || plan.isEmpty) {
        SectionHeading(if (isToday) "Today's plan" else "Plan for this day", trailing = "nothing planned", accent = Faint)
        Spacer(Modifier.height(6.dp))
        Text(
            "Plan the week with Claude in Excel and import it, and today's workout and food show here.",
            style = MaterialTheme.typography.labelSmall, color = Faint
        )
        TextButton(onClick = onOpenWeek) { Text("Open week plan", color = Brass) }
        Spacer(Modifier.height(14.dp))
        Hairline()
        Spacer(Modifier.height(22.dp))
        return
    }
    val accent = focusColor(plan.focus)
    SectionHeading(
        if (isToday) "Today's plan" else "Plan for this day",
        trailing = plan.focus.ifEmpty { "Workout" },
        accent = accent
    )
    Spacer(Modifier.height(10.dp))
    if (plan.lifts.isNotEmpty()) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.10f)).padding(14.dp)
        ) {
            val time = plan.day?.workoutTime.orEmpty()
            Text(
                listOfNotNull(plan.focus.ifEmpty { "Workout" }, time.ifEmpty { null }, "about ${PlanCalendar.workoutMinutes(plan)} min").joinToString(" · "),
                style = MaterialTheme.typography.titleSmall, color = Bone
            )
            Spacer(Modifier.height(4.dp))
            plan.lifts.forEach {
                LiftLine(it.exerciseName, PlanValues.liftLine(it.sets, it.repsLow, it.repsHigh, it.holdSeconds, it.weightKg, it.restSeconds), it.notes)
            }
            if (isToday && !trainedToday) {
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = { onStartWorkout(plan.focus.ifEmpty { "Workout" }) },
                    colors = ButtonDefaults.buttonColors(containerColor = accent.copy(alpha = 0.18f), contentColor = accent),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Start ${plan.focus.ifEmpty { "the" }} session") }
            }
        }
        Spacer(Modifier.height(14.dp))
    } else if (plan.focus.isNotEmpty()) {
        Text(plan.focus, style = MaterialTheme.typography.bodyLarge, color = accent)
        Spacer(Modifier.height(10.dp))
    }
    if (plan.foods.isNotEmpty()) {
        val (kcal, protein) = PlanCalendar.totals(plan.foods)
        Text("Planned food: ${kcal.roundToInt()} kcal · ${protein.roundToInt()} g protein", style = MaterialTheme.typography.labelMedium, color = Sage)
        plan.foods.groupBy { it.meal }.forEach { (meal, foods) ->
            Spacer(Modifier.height(10.dp))
            val loggable = foods.filter { it.foodId != null }
            val done = loggable.isNotEmpty() && loggable.all { f -> logged.any { it.foodId == f.foodId && it.meal == f.meal } }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val at = foods.firstOrNull { it.time.isNotEmpty() }?.time
                Text(listOfNotNull(meal, at).joinToString(" · "), style = MaterialTheme.typography.labelLarge, color = Bone, modifier = Modifier.weight(1f))
                when {
                    done -> Text("Logged", style = MaterialTheme.typography.labelMedium, color = Sage)
                    loggable.isNotEmpty() && plan.dateEpochDay <= Dates.todayEpochDay() ->
                        TextButton(onClick = { onLogFoods(loggable) }) { Text("Log ${meal.lowercase()}", color = Sage) }
                }
            }
            foods.forEach { FoodLine(it, unitOf(it.foodId)) }
        }
        Spacer(Modifier.height(6.dp))
    }
    TextButton(onClick = onOpenWeek) { Text("Open week plan", color = Brass) }
    Spacer(Modifier.height(14.dp))
    Hairline()
    Spacer(Modifier.height(22.dp))
}

/** What the import found, before anything is saved; and the outcome afterwards. */
@Composable
fun PlanImportDialog(state: PlanImportState, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    when (state) {
        PlanImportState.Idle -> Unit
        PlanImportState.Reading -> AlertDialog(
            onDismissRequest = {},
            containerColor = Slate,
            title = { Text("Reading the plan", color = Bone) },
            text = { CircularProgressIndicator(color = Sage) },
            confirmButton = {}
        )
        is PlanImportState.Done -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Slate,
            title = { Text("Week plan", color = Bone) },
            text = { Text(state.message, color = Mute, modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(onClick = onDismiss) { Text("OK", color = Sage) } }
        )
        is PlanImportState.Preview -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Slate,
            title = { Text("Import this plan?", color = Bone) },
            text = { PreviewBody(state.result) },
            confirmButton = { TextButton(onClick = onConfirm) { Text("Import", color = Sage) } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Mute) } }
        )
    }
}

@Composable
private fun PreviewBody(r: PlanImportResult) {
    val first = r.dates.minOrNull()
    val last = r.dates.maxOrNull()
    Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
        Text(r.summary(), style = MaterialTheme.typography.titleSmall, color = Bone)
        if (first != null && last != null) {
            Text(
                "${Dates.dayAndMonth(Dates.ofEpochDay(first))} to ${Dates.dayAndMonth(Dates.ofEpochDay(last))}. " +
                    "Any plan already on these days is replaced; nothing you logged is touched.",
                style = MaterialTheme.typography.labelSmall, color = Mute
            )
        }
        r.days.filter { it.focus.isNotEmpty() }.take(14).forEach {
            Text(
                "${PlanValues.shortWeekday(Dates.ofEpochDay(it.dateEpochDay))} ${Dates.dayAndMonth(Dates.ofEpochDay(it.dateEpochDay))}: ${it.focus}",
                style = MaterialTheme.typography.labelMedium, color = focusColor(it.focus)
            )
        }
        if (r.problems.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            val skipped = r.problems.filter { it.skipped }
            val notes = r.problems.filterNot { it.skipped }
            if (skipped.isNotEmpty()) {
                Text("Skipped (${skipped.size}): fix these rows and import again", style = MaterialTheme.typography.labelMedium, color = Coral)
                skipped.take(MAX_LISTED).forEach { Text("• $it", style = MaterialTheme.typography.labelSmall, color = Mute) }
                if (skipped.size > MAX_LISTED) Text("…and ${skipped.size - MAX_LISTED} more", style = MaterialTheme.typography.labelSmall, color = Faint)
            }
            if (notes.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("Imported with a note (${notes.size})", style = MaterialTheme.typography.labelMedium, color = Brass)
                notes.take(MAX_LISTED).forEach { Text("• $it", style = MaterialTheme.typography.labelSmall, color = Mute) }
                if (notes.size > MAX_LISTED) Text("…and ${notes.size - MAX_LISTED} more", style = MaterialTheme.typography.labelSmall, color = Faint)
            }
        }
    }
}

private const val MAX_LISTED = 40
