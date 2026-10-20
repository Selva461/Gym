package dev.avles.gym.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import dev.avles.gym.data.CardioEntry
import dev.avles.gym.domain.Cardio
import dev.avles.gym.domain.Plates
import dev.avles.gym.ui.components.ChipRow
import dev.avles.gym.ui.components.EntryRow
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Coral
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Slate

/** The day's cardio with a button to log more. Tap an entry to remove it. */
@Composable
fun CardioSection(
    entries: List<CardioEntry>,
    bodyKg: Double,
    onLog: (String, Int, Double) -> Unit,
    onDelete: (Long) -> Unit
) {
    var logging by remember { mutableStateOf(false) }
    var removing by remember { mutableStateOf<CardioEntry?>(null) }
    Column(Modifier.fillMaxWidth()) {
        SectionHeading("Cardio", trailing = if (entries.isEmpty()) null else "${entries.sumOf { it.minutes }} min today")
        Spacer(Modifier.height(10.dp))
        entries.forEach { e ->
            val kcal = Cardio.kcal(e.activity, e.minutes, bodyKg)
            EntryRow(
                title = e.activity,
                detail = listOfNotNull(
                    if (e.distanceKm > 0) "${Plates.label(e.distanceKm)} km" else null,
                    Cardio.pace(e.minutes, e.distanceKm),
                    if (kcal > 0) "about $kcal kcal" else null
                ).joinToString("  ·  "),
                trailing = "${e.minutes} min",
                accent = Brass,
                onClick = { removing = e },
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        TextButton(onClick = { logging = true }) { Text("Log cardio", color = Brass) }
    }
    if (logging) CardioDialog(onSave = { a, m, km -> onLog(a, m, km); logging = false }, onDismiss = { logging = false })
    removing?.let { e ->
        AlertDialog(
            onDismissRequest = { removing = null },
            containerColor = Slate,
            title = { Text("${e.activity}, ${e.minutes} min", color = Bone) },
            text = { Text("Remove this from the day?", color = Mute) },
            confirmButton = { TextButton(onClick = { onDelete(e.id); removing = null }) { Text("Remove", color = Coral) } },
            dismissButton = { TextButton(onClick = { removing = null }) { Text("Keep", color = Mute) } }
        )
    }
}

@Composable
private fun CardioDialog(onSave: (String, Int, Double) -> Unit, onDismiss: () -> Unit) {
    var activity by remember { mutableStateOf(Cardio.ACTIVITIES.first().name) }
    var minutes by remember { mutableStateOf("") }
    var km by remember { mutableStateOf("") }
    val mins = minutes.toIntOrNull() ?: 0
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text("Log cardio", color = Bone) },
        text = {
            Column {
                ChipRow(Cardio.ACTIVITIES.map { it.name }, activity, Brass, { activity = it })
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { minutes = it.filter(Char::isDigit).take(3) },
                        label = { Text("minutes") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    if (Cardio.activity(activity).hasDistance) {
                        OutlinedTextField(
                            value = km,
                            onValueChange = { km = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                            label = { Text("km (optional)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = mins > 0, onClick = {
                onSave(activity, mins, if (Cardio.activity(activity).hasDistance) km.toDoubleOrNull() ?: 0.0 else 0.0)
            }) { Text("Save", color = if (mins > 0) Brass else Faint) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Mute) } }
    )
}
