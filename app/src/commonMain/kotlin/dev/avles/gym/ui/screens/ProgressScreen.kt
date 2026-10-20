package dev.avles.gym.ui.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.avles.gym.core.Dates
import dev.avles.gym.domain.Achievements
import dev.avles.gym.domain.Consistency
import dev.avles.gym.domain.Training
import dev.avles.gym.domain.WeightGoal
import dev.avles.gym.ui.ProgressState
import dev.avles.gym.ui.components.AchievementsCard
import dev.avles.gym.ui.components.BarStrip
import dev.avles.gym.ui.components.BigNumber
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.MonthCalendar
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.components.TrendChart
import dev.avles.gym.ui.components.pressScale
import dev.avles.gym.ui.i18n.tr
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Sage
import dev.avles.gym.ui.theme.Slate
import kotlin.math.abs

@Composable
fun ProgressScreen(
    state: ProgressState,
    onRecordWeight: (Double) -> Unit,
    onOpenPhotos: () -> Unit,
    onOpenGoal: () -> Unit,
    onOpenPatterns: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenRecords: () -> Unit = {},
    onOpenMeasurements: () -> Unit = {},
    liftsWithRecords: Int = 0,
    goalWeightKg: Double = 0.0,
    restDays: Set<Long> = emptySet()
) {
    var logging by remember { mutableStateOf(false) }
    var oneRepMax by remember { mutableStateOf(false) }
    val today = Dates.todayEpochDay()

    val weights = state.weights
    val latest = weights.lastOrNull()
    val first = weights.firstOrNull()
    val change = if (latest != null && first != null) latest.weightKg - first.weightKg else 0.0

    val volumeByDay = remember(state.volume) { state.volume.associate { it.dateEpochDay to it.volume } }
    val lastFourteen = remember(volumeByDay, today) {
        (13 downTo 0).map { volumeByDay[today - it] ?: 0.0 }
    }

    val trainStreak = Consistency.currentStreak(state.trained, today)
    val fuelStreak = Consistency.currentStreak(state.fuelled, today)
    val trainedInTwoWeeks = Consistency.daysHitIn(state.trained, today, 14)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)
    ) {
        item {
            Text(tr("Progress"), style = MaterialTheme.typography.headlineMedium, color = Bone)
            Text(
                "The slow numbers. Read these monthly, not daily.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mute
            )
            Spacer(Modifier.height(14.dp))
            // Photos belong here rather than in a sixth tab: they are the same
            // question as the weight trend, answered with your eyes instead of a
            // number, and they are the one that tells you when the scale is lying.
            androidx.compose.material3.TextButton(
                onClick = onOpenPhotos,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text("Progress photos", color = Brass, style = MaterialTheme.typography.bodyMedium)
            }
            androidx.compose.material3.TextButton(
                onClick = onOpenGoal,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text(
                    "Does my training serve my goal?",
                    color = Brass,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            androidx.compose.material3.TextButton(
                onClick = onOpenMeasurements,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text("Measurements and body fat", color = Brass, style = MaterialTheme.typography.bodyMedium)
            }
            androidx.compose.material3.TextButton(
                onClick = { oneRepMax = true },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text("One-rep max calculator", color = Brass, style = MaterialTheme.typography.bodyMedium)
            }
            androidx.compose.material3.TextButton(
                onClick = onOpenRecords,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text("Personal records", color = Brass, style = MaterialTheme.typography.bodyMedium)
            }
            androidx.compose.material3.TextButton(
                onClick = onOpenPatterns,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text(
                    "Eating and training patterns",
                    color = Brass,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(16.dp))

            SectionHeading(
                tr("Body weight"),
                trailing = latest?.let { "last ${Training.formatWeight(it.weightKg)} kg" }
            )
            Spacer(Modifier.height(14.dp))
            if (latest != null) {
                BigNumber(
                    value = Training.formatWeight(latest.weightKg),
                    unit = "kg",
                    caption = if (weights.size > 1) {
                        val direction = if (change < 0) "down" else "up"
                        "$direction ${Training.formatWeight(abs(change))} kg since you started logging"
                    } else {
                        "first entry recorded"
                    },
                    accent = Sage
                )
            } else {
                Text(
                    "No weight recorded yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Faint
                )
            }
            if (latest != null && goalWeightKg > 0) {
                val rate = WeightGoal.weeklyRate(weights, today)
                val days = WeightGoal.daysToGoal(latest.weightKg, goalWeightKg, rate)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Goal ${Training.formatWeight(goalWeightKg)} kg · " + when {
                        days == 0 -> "reached"
                        rate == null -> "three weigh-ins this month give a projected date"
                        days == null -> "the four-week trend is heading the other way " +
                            "(${if (rate >= 0) "+" else "−"}${Training.formatWeight(abs(rate))} kg a week)"
                        else -> "${if (rate < 0) "losing" else "gaining"} ${Training.formatWeight(abs(rate))} kg a week, " +
                            "about ${Dates.dayAndMonth(Dates.ofEpochDay(today + days))}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Mute
                )
            }
            Spacer(Modifier.height(14.dp))
            TrendChart(
                values = weights.map { it.weightKg },
                accent = Sage,
                emptyMessage = "Record a second weight and the trend appears."
            )
            Spacer(Modifier.height(12.dp))
            val recordPress = remember { MutableInteractionSource() }
            Button(
                onClick = { logging = true },
                interactionSource = recordPress,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Sage.copy(alpha = 0.14f),
                    contentColor = Sage
                ),
                modifier = Modifier.fillMaxWidth().pressScale(recordPress)
            ) { Text("Record today's weight") }

            Spacer(Modifier.height(28.dp))
            Hairline()
            Spacer(Modifier.height(22.dp))

            SectionHeading("Training volume", trailing = "last 14 days")
            Spacer(Modifier.height(14.dp))
            BarStrip(lastFourteen, Brass)
            Spacer(Modifier.height(10.dp))
            Text(
                "Trained $trainedInTwoWeeks of the last 14 days.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mute
            )

            Spacer(Modifier.height(28.dp))
            Hairline()
            Spacer(Modifier.height(22.dp))

            SectionHeading(tr("Consistency"))
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                BigNumber(
                    value = trainStreak.toString(),
                    unit = if (trainStreak == 1) "day" else "days",
                    caption = "training run",
                    accent = Brass,
                    modifier = Modifier.weight(1f)
                )
                BigNumber(
                    value = fuelStreak.toString(),
                    unit = if (fuelStreak == 1) "day" else "days",
                    caption = "food logging run",
                    accent = Sage,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(16.dp))
            Column {
                Text(
                    "Longest training run: ${Consistency.longestStreak(state.trained)} days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Faint
                )
                Text(
                    "Longest logging run: ${Consistency.longestStreak(state.fuelled)} days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Faint
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "A missed day does not undo a month. The run restarts, the training does not.",
                style = MaterialTheme.typography.labelSmall,
                color = Faint
            )
            Spacer(Modifier.height(26.dp))
            Hairline()
            Spacer(Modifier.height(22.dp))
            AchievementsCard(
                Achievements.Totals(
                    daysTrained = state.trained.size,
                    longestRun = Consistency.longestStreak(state.trained),
                    kilosMoved = state.volume.sumOf { it.volume },
                    liftsWithRecords = liftsWithRecords,
                    daysFoodLogged = state.fuelled.size
                )
            )
            Spacer(Modifier.height(26.dp))
            Hairline()
            Spacer(Modifier.height(22.dp))
            MonthCalendar(trained = state.trained, fuelled = state.fuelled, restDays = restDays)
            Spacer(Modifier.height(6.dp))
            Text(
                "Brass is a training day, grey a planned rest, the green mark food logged.",
                style = MaterialTheme.typography.labelSmall,
                color = Faint
            )
        }
    }

    if (oneRepMax) OneRepMaxDialog(onDismiss = { oneRepMax = false })

    if (logging) {
        WeightDialog(
            suggestion = latest?.weightKg,
            onSave = { onRecordWeight(it); logging = false },
            onDismiss = { logging = false }
        )
    }
}

@Composable
internal fun WeightDialog(
    suggestion: Double?,
    onSave: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember {
        mutableStateOf(suggestion?.let { Training.formatWeight(it) } ?: "")
    }
    val parsed = value.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text("Record weight", color = Bone) },
        text = {
            Column {
                Text(
                    "Same scale, same time of day, and day-to-day noise stops mattering.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mute
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("kg") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(enabled = parsed > 0, onClick = { onSave(parsed) }) {
                Text("Save", color = if (parsed > 0) Sage else Faint)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Mute) } }
    )
}

