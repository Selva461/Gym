package dev.avles.gym.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.avles.gym.data.GymSettings
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Raised
import dev.avles.gym.ui.theme.Sage
import dev.avles.gym.ui.theme.Slate
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    settings: GymSettings,
    onSave: (GymSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var protein by remember(settings) { mutableStateOf(settings.proteinTargetG.roundToInt().toString()) }
    var kcal by remember(settings) { mutableStateOf(settings.kcalTarget.roundToInt().toString()) }
    var rest by remember(settings) { mutableStateOf(settings.restSeconds.toString()) }
    var hour by remember(settings) { mutableStateOf(settings.reminderHour.toString()) }
    var minute by remember(settings) { mutableStateOf(settings.reminderMinute.toString()) }
    var remindersOn by remember(settings) { mutableStateOf(settings.remindersOn) }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, color = Bone)
        Spacer(Modifier.height(24.dp))

        SectionHeading("Daily targets", accent = Sage)
        Spacer(Modifier.height(6.dp))
        Text(
            "Your numbers, not a formula's. Change them whenever the goal changes.",
            style = MaterialTheme.typography.labelSmall,
            color = Faint
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumField(protein, { protein = it }, "protein, g", Modifier.weight(1f))
            NumField(kcal, { kcal = it }, "calories", Modifier.weight(1f))
        }

        Spacer(Modifier.height(26.dp))
        Hairline()
        Spacer(Modifier.height(20.dp))

        SectionHeading("Rest timer", accent = Brass)
        Spacer(Modifier.height(12.dp))
        NumField(rest, { rest = it }, "seconds between working sets", Modifier.fillMaxWidth())

        Spacer(Modifier.height(26.dp))
        Hairline()
        Spacer(Modifier.height(20.dp))

        SectionHeading("Nightly reminder", accent = Brass)
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumField(hour, { hour = it }, "hour, 0-23", Modifier.weight(1f))
            NumField(minute, { minute = it }, "minute", Modifier.weight(1f))
        }

        Spacer(Modifier.height(28.dp))
        Button(
            onClick = {
                onSave(
                    GymSettings(
                        proteinTargetG = protein.toDoubleOrNull() ?: settings.proteinTargetG,
                        kcalTarget = kcal.toDoubleOrNull() ?: settings.kcalTarget,
                        restSeconds = (rest.toIntOrNull() ?: settings.restSeconds).coerceIn(15, 900),
                        reminderHour = (hour.toIntOrNull() ?: settings.reminderHour).coerceIn(0, 23),
                        reminderMinute = (minute.toIntOrNull() ?: settings.reminderMinute).coerceIn(0, 59),
                        remindersOn = remindersOn
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Slate),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text("Save settings", style = MaterialTheme.typography.labelLarge) }

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
