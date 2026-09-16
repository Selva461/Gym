package dev.avles.gym.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import dev.avles.gym.data.Exercise
import dev.avles.gym.data.SeedData
import dev.avles.gym.domain.Training
import dev.avles.gym.ui.components.ChipRow
import dev.avles.gym.ui.components.EntryRow
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Slate

/**
 * Choosing a lift out of a catalogue of nearly four hundred, and adding one that
 * is not in it.
 *
 * Split out of TrainScreen when that file crossed the 800-line ceiling
 * ArchitectureTest enforces. The ceiling is not decoration: these two dialogs have
 * nothing to do with running a session, and the screen was only holding them
 * because that is where they were first written.
 */

@Composable
internal fun ExercisePicker(
    exercises: List<Exercise>,
    onPick: (Exercise) -> Unit,
    onAddNew: () -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val matches = remember(query, exercises) {
        if (query.isBlank()) exercises
        else exercises.filter { it.name.contains(query, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text("Choose a lift", color = Bone) },
        text = {
            Column {
                // The catalogue is past eighty lifts now. Scrolling to find a cable curl
                // between two sets is exactly the friction this app exists to remove.
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.height(340.dp)) {
                    matches.groupBy { it.muscleGroup }.forEach { (group, inGroup) ->
                        item {
                            Text(
                                group,
                                style = MaterialTheme.typography.labelMedium,
                                color = Brass,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(inGroup, key = { "lift-${it.id}" }) { exercise ->
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onPick(exercise) }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    exercise.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Bone
                                )
                                Text(
                                    "${exercise.repLow}-${exercise.repHigh} reps  ·  " +
                                        "+${Training.formatWeight(exercise.incrementKg)} kg steps",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Faint
                                )
                            }
                        }
                    }
                    if (matches.isEmpty()) {
                        item {
                            Text(
                                "Nothing matches \"$query\".",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Faint,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onAddNew) { Text("Add a lift", color = Brass) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close", color = Mute) } }
    )
}

@Composable
internal fun NewExerciseDialog(onSave: (String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var group by remember { mutableStateOf(SeedData.muscleGroups().first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text("Add a lift", color = Bone) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "Muscle group",
                    style = MaterialTheme.typography.labelSmall,
                    color = Mute
                )
                Spacer(Modifier.height(6.dp))
                // Thirteen groups: a ChipRow scrolls, where PillRow would squeeze
                // every label down to two ellipsised characters.
                ChipRow(SeedData.muscleGroups(), group, Brass, { group = it })
                Spacer(Modifier.height(10.dp))
                Text(
                    "New lifts progress in 6-10 reps and 2.5 kg steps until you change them.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onSave(name, group) }) {
                Text("Save lift", color = if (name.isNotBlank()) Brass else Faint)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Mute) } }
    )
}
