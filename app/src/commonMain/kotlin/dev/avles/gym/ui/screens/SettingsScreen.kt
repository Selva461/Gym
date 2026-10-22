package dev.avles.gym.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.avles.gym.data.GymSettings
import dev.avles.gym.ui.i18n.tr
import dev.avles.gym.ui.i18n.Strings
import dev.avles.gym.ui.components.PillRow
import dev.avles.gym.domain.Training
import dev.avles.gym.notify.Notifier
import dev.avles.gym.ui.GymViewModel
import dev.avles.gym.ui.components.rememberBackupActions
import dev.avles.gym.ui.components.rememberTextSharer
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.Motion.fadeThrough
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.components.pressScale
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Raised
import dev.avles.gym.ui.theme.Sage
import dev.avles.gym.ui.theme.Slate
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    settings: GymSettings,
    onSave: (GymSettings) -> Unit,
    modifier: Modifier = Modifier,
    latestWeightKg: Double? = null,
    onExport: (GymViewModel.ExportKind, (String, String) -> Unit) -> Unit = { _, _ -> }
) {
    val share = rememberTextSharer()
    var protein by remember(settings) { mutableStateOf(settings.proteinTargetG.roundToInt().toString()) }
    var kcal by remember(settings) { mutableStateOf(settings.kcalTarget.roundToInt().toString()) }
    var rest by remember(settings) { mutableStateOf(settings.restSeconds.toString()) }
    var hour by remember(settings) { mutableStateOf(settings.reminderHour.toString()) }
    var minute by remember(settings) { mutableStateOf(settings.reminderMinute.toString()) }
    var remindersOn by remember(settings) { mutableStateOf(settings.remindersOn) }
    var restByLift by remember(settings) { mutableStateOf(settings.restByLift) }
    var waterReminders by remember(settings) { mutableStateOf(settings.waterReminders) }
    var mealReminders by remember(settings) { mutableStateOf(settings.mealReminders) }
    var language by remember(settings) { mutableStateOf(settings.language) }
    var goalWeight by remember(settings) {
        mutableStateOf(if (settings.goalWeightKg > 0) Training.formatWeight(settings.goalWeightKg) else "")
    }
    var water by remember(settings) { mutableStateOf(settings.waterGlasses.toString()) }
    var calculating by remember { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(tr("Settings"), style = MaterialTheme.typography.headlineMedium, color = Bone)
        Spacer(Modifier.height(16.dp))
        SectionHeading(tr("Language"), accent = Sage)
        Spacer(Modifier.height(8.dp))
        PillRow(
            options = listOf("English", "தமிழ்"),
            selected = if (language == Strings.TAMIL) "தமிழ்" else "English",
            accent = Sage,
            onSelect = { language = if (it == "தமிழ்") Strings.TAMIL else Strings.ENGLISH }
        )
        Text(
            "Tabs, headings and main buttons. Saved with the button at the bottom.",
            style = MaterialTheme.typography.labelSmall,
            color = Faint
        )
        Spacer(Modifier.height(24.dp))

        SectionHeading(tr("Daily targets"), accent = Sage)
        Spacer(Modifier.height(6.dp))
        Text(
            "Your numbers, not a formula's. Change them whenever the goal changes. " +
                "Carbohydrate and fat follow from these: fat a quarter of the calories, " +
                "carbohydrate the rest.",
            style = MaterialTheme.typography.labelSmall,
            color = Faint
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumField(protein, { protein = it }, "protein, g", Modifier.weight(1f))
            NumField(kcal, { kcal = it }, "calories", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumField(water, { water = it }, "water, glasses", Modifier.weight(1f))
            OutlinedTextField(
                value = goalWeight,
                onValueChange = { goalWeight = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("goal weight, kg") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }
        TextButton(onClick = { calculating = true }) {
            Text("Work these out from my body", color = Sage)
        }

        Spacer(Modifier.height(26.dp))
        Hairline()
        Spacer(Modifier.height(20.dp))

        SectionHeading(tr("Rest timer"), accent = Brass)
        Spacer(Modifier.height(12.dp))
        NumField(rest, { rest = it }, "seconds between working sets", Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Fit the rest to the lift", style = MaterialTheme.typography.bodyLarge, color = Bone)
                Text(
                    "3 min after heavy compounds, 90 s after isolation work, yours for the rest.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
            }
            Switch(
                checked = restByLift,
                onCheckedChange = { restByLift = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Slate,
                    checkedTrackColor = Brass,
                    uncheckedTrackColor = Raised
                )
            )
        }

        Spacer(Modifier.height(26.dp))
        Hairline()
        Spacer(Modifier.height(20.dp))

        SectionHeading(tr("Nightly reminder"), accent = Brass)
        Spacer(Modifier.height(6.dp))
        Text(
            "One notification to close out the day's log. It arrives near this time, not exactly on it.",
            style = MaterialTheme.typography.labelSmall,
            color = Faint
        )
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Remind me", style = MaterialTheme.typography.bodyLarge, color = Bone)
            Switch(
                checked = remindersOn,
                onCheckedChange = { remindersOn = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Slate,
                    checkedTrackColor = Brass,
                    uncheckedTrackColor = Raised
                )
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Remind me to drink water", style = MaterialTheme.typography.bodyLarge, color = Bone)
                Text("Every two hours, 10:00 to 20:00.", style = MaterialTheme.typography.labelSmall, color = Faint)
            }
            Switch(
                checked = waterReminders,
                onCheckedChange = { waterReminders = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Slate, checkedTrackColor = Brass, uncheckedTrackColor = Raised)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Remind me to log meals", style = MaterialTheme.typography.bodyLarge, color = Bone)
                Text(
                    Notifier.MEAL_TIMES.joinToString(", ") { (m, h, min) -> "${m.lowercase()} ${h}:${min.toString().padStart(2, '0')}" }
                        .replaceFirstChar { it.uppercase() } + ".",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
            }
            Switch(
                checked = mealReminders,
                onCheckedChange = { mealReminders = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Slate, checkedTrackColor = Brass, uncheckedTrackColor = Raised)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumField(hour, { hour = it }, "hour, 0-23", Modifier.weight(1f))
            NumField(minute, { minute = it }, "minute", Modifier.weight(1f))
        }

        Spacer(Modifier.height(28.dp))
        // The button itself says it worked, for a second and a half, because saving
        // otherwise changes nothing on screen and leaves you tapping it twice.
        var savedTick by remember { mutableIntStateOf(0) }
        LaunchedEffect(savedTick) {
            if (savedTick > 0) {
                delay(1500)
                savedTick = 0
            }
        }
        val haptics = LocalHapticFeedback.current
        val savePress = remember { MutableInteractionSource() }
        Button(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                savedTick += 1
                // copy, not a new GymSettings: a fresh one silently reset every field
                // this form does not show, the coach's goal among them.
                onSave(
                    settings.copy(
                        proteinTargetG = protein.toDoubleOrNull() ?: settings.proteinTargetG,
                        kcalTarget = kcal.toDoubleOrNull() ?: settings.kcalTarget,
                        restSeconds = (rest.toIntOrNull() ?: settings.restSeconds).coerceIn(15, 900),
                        reminderHour = (hour.toIntOrNull() ?: settings.reminderHour).coerceIn(0, 23),
                        reminderMinute = (minute.toIntOrNull() ?: settings.reminderMinute).coerceIn(0, 59),
                        remindersOn = remindersOn,
                        restByLift = restByLift,
                        waterReminders = waterReminders,
                        mealReminders = mealReminders,
                        language = language,
                        goalWeightKg = goalWeight.toDoubleOrNull()?.takeIf { it in 30.0..300.0 } ?: 0.0,
                        waterGlasses = (water.toIntOrNull() ?: settings.waterGlasses).coerceIn(1, 30)
                    )
                )
            },
            interactionSource = savePress,
            colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Slate),
            modifier = Modifier.fillMaxWidth().height(52.dp).pressScale(savePress)
        ) {
            AnimatedContent(targetState = savedTick > 0, transitionSpec = { fadeThrough() }, label = "saved") { saved ->
                Text(if (saved) "Saved" else "Save settings", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(Modifier.height(28.dp))
        Hairline()
        Spacer(Modifier.height(20.dp))
        SectionHeading(tr("Your data"), accent = Sage)
        Spacer(Modifier.height(6.dp))
        Text(
            "As spreadsheets (CSV), through the share sheet: save them to Files or Drive, " +
                "or send them to yourself. Nothing leaves the phone unless you pick where it goes.",
            style = MaterialTheme.typography.labelSmall,
            color = Faint
        )
        Row {
            TextButton(onClick = { onExport(GymViewModel.ExportKind.SETS, share) }) { Text("Sets", color = Sage) }
            TextButton(onClick = { onExport(GymViewModel.ExportKind.FOOD, share) }) { Text("Food log", color = Sage) }
            TextButton(onClick = { onExport(GymViewModel.ExportKind.WEIGHTS, share) }) { Text("Weight", color = Sage) }
        }
        // A whole-log file you can keep in Drive and put back on a new phone. Restoring
        // replaces everything, so it asks first; the replaced log is kept beside it.
        var backupNote by remember { mutableStateOf<String?>(null) }
        var confirmRestore by remember { mutableStateOf(false) }
        val backup = rememberBackupActions { backupNote = it }
        if (backup != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                "Or the whole log as one backup file, to restore on this phone or a new one. " +
                    "Photos are not included.",
                style = MaterialTheme.typography.labelSmall,
                color = Faint
            )
            Row {
                TextButton(onClick = { backupNote = null; backup.backUp() }) { Text("Back up", color = Sage) }
                TextButton(onClick = { backupNote = null; confirmRestore = true }) { Text("Restore", color = Sage) }
            }
            backupNote?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Mute) }
        }
        if (confirmRestore && backup != null) {
            AlertDialog(
                onDismissRequest = { confirmRestore = false },
                containerColor = Slate,
                title = { Text("Restore a backup?", color = Bone) },
                text = {
                    Text(
                        "Everything logged on this phone is replaced by the backup, and the app restarts. " +
                            "The current log is kept as gym-before-restore.db in the app's files.",
                        color = Mute
                    )
                },
                confirmButton = {
                    TextButton(onClick = { confirmRestore = false; backup.restore() }) { Text("Pick the file", color = Sage) }
                },
                dismissButton = { TextButton(onClick = { confirmRestore = false }) { Text("Cancel", color = Mute) } }
            )
        }

        Spacer(Modifier.height(28.dp))
        Hairline()
        Spacer(Modifier.height(18.dp))
        Text(
            "Everything lives on this phone. There is no account, no server, and the app "
                + "holds no network permission at all — it cannot reach the internet even "
                + "if it wanted to.",
            style = MaterialTheme.typography.bodyMedium,
            color = Mute
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "One exception, stated plainly: if you have Android's own backup switched on, "
                + "your log is included in it so that it survives a reinstall. It is "
                + "encrypted with your screen lock before it leaves the phone, and is "
                + "skipped entirely on a device that cannot do that. Android Settings › "
                + "Google › Backup turns it off.",
            style = MaterialTheme.typography.labelSmall,
            color = Faint
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Seeded food values are estimates for home-style portions. Correct the few you eat "
                + "most often and the rest stops mattering.",
            style = MaterialTheme.typography.labelSmall,
            color = Faint
        )
        Spacer(Modifier.height(40.dp))
    }

    if (calculating) {
        TargetCalculatorDialog(
            initialWeightKg = latestWeightKg,
            onUse = { t ->
                kcal = t.kcal.roundToInt().toString()
                protein = t.proteinG.roundToInt().toString()
                water = t.waterGlasses.toString()
                calculating = false
            },
            onDismiss = { calculating = false }
        )
    }
}

@Composable
private fun NumField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() }) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}
