package dev.avles.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.avles.gym.data.Exercise
import dev.avles.gym.domain.Training
import dev.avles.gym.ui.LiftGuidance
import dev.avles.gym.ui.TrainState
import dev.avles.gym.ui.components.BigNumber
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.EntryRow
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.Meter
import dev.avles.gym.ui.components.PillRow
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Coral
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Raised
import dev.avles.gym.ui.theme.Slate
import kotlin.math.roundToInt

private val SESSION_LABELS = listOf("Push", "Pull", "Legs", "Full")

@Composable
fun TrainScreen(
    state: TrainState,
    restRemaining: Int?,
    prSetId: Long?,
    guidance: LiftGuidance?,
    onSelectExercise: (Long?) -> Unit,
    onStart: (String) -> Unit,
    onFinish: () -> Unit,
    onLogSet: (Long, Double, Int, Boolean) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onSkipRest: () -> Unit,
    onAddExercise: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.active == null) {
        StartSessionPane(state, onStart, modifier)
    } else {
        ActiveSessionPane(
            state = state,
            restRemaining = restRemaining,
            prSetId = prSetId,
            guidance = guidance,
            onSelectExercise = onSelectExercise,
            onFinish = onFinish,
            onLogSet = onLogSet,
            onDeleteSet = onDeleteSet,
            onSkipRest = onSkipRest,
            onAddExercise = onAddExercise,
            modifier = modifier
        )
    }
}

@Composable
private fun StartSessionPane(
    state: TrainState,
    onStart: (String) -> Unit,
    modifier: Modifier
) {
    var label by remember { mutableStateOf("Push") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)
    ) {
        item {
            Text("Train", style = MaterialTheme.typography.headlineMedium, color = Bone)
            Text(
                "Pick what today is, then start logging sets.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mute
            )
            Spacer(Modifier.height(22.dp))
            PillRow(SESSION_LABELS, label, Brass, { label = it })
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onStart(label) },
                colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Slate),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Start $label session", style = MaterialTheme.typography.labelLarge) }

            Spacer(Modifier.height(30.dp))
            Hairline()
            Spacer(Modifier.height(22.dp))
            SectionHeading("Recent sessions")
            Spacer(Modifier.height(10.dp))
        }

        if (state.recent.isEmpty()) {
            item { EmptyNote("No sessions yet. The first one is the only hard one.") }
        } else {
            items(state.recent, key = { it.id }) { session ->
                val minutes = session.endedAt?.let { ((it - session.startedAt) / 60000).toInt() }
                EntryRow(
                    title = session.label,
                    detail = java.time.LocalDate.ofEpochDay(session.dateEpochDay).toString(),
                    trailing = minutes?.let { "$it min" } ?: "open",
                    accent = Brass,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ActiveSessionPane(
    state: TrainState,
    restRemaining: Int?,
    prSetId: Long?,
    guidance: LiftGuidance?,
    onSelectExercise: (Long?) -> Unit,
    onFinish: () -> Unit,
    onLogSet: (Long, Double, Int, Boolean) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onSkipRest: () -> Unit,
    onAddExercise: (String, String) -> Unit,
    modifier: Modifier
) {
    val session = state.active ?: return
    val selectedExercise = guidance?.exercise
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var warmup by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }
    var showNewExercise by remember { mutableStateOf(false) }

    val volume = Training.volume(state.sets)
    val working = state.sets.count { !it.isWarmup }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "${session.label} session",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Bone
                    )
                    Text(
                        "$working working sets",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mute
                    )
                }
                TextButton(onClick = onFinish) { Text("Finish", color = Brass) }
            }

            Spacer(Modifier.height(20.dp))
            BigNumber(
                value = volume.roundToInt().toString(),
                unit = "kg lifted",
                caption = "working volume this session",
                accent = Brass
            )

            if (restRemaining != null) {
                Spacer(Modifier.height(16.dp))
                RestBanner(restRemaining, onSkipRest)
            }

            Spacer(Modifier.height(22.dp))
            Hairline()
            Spacer(Modifier.height(20.dp))

            // --- Set entry ------------------------------------------------
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate)
                    .clickable { showPicker = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    selectedExercise?.name ?: "Choose a lift",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selectedExercise == null) Faint else Bone
                )
            }

            if (guidance != null) {
                Spacer(Modifier.height(12.dp))
                TargetCard(
                    guidance = guidance,
                    onUseTarget = { w, r ->
                        weight = Training.formatWeight(w)
                        reps = r.toString()
                    }
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("kg") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it.filter { c -> c.isDigit() } },
                    label = { Text("reps") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))
            PillRow(
                options = listOf("Working set", "Warm-up"),
                selected = if (warmup) "Warm-up" else "Working set",
                accent = Brass,
                onSelect = { warmup = it == "Warm-up" }
            )

            Spacer(Modifier.height(12.dp))
            val canLog = selectedExercise != null &&
                (weight.toDoubleOrNull() ?: 0.0) > 0 &&
                (reps.toIntOrNull() ?: 0) > 0
            Button(
                onClick = {
                    val ex = selectedExercise ?: return@Button
                    onLogSet(
                        ex.id,
                        weight.toDoubleOrNull() ?: 0.0,
                        reps.toIntOrNull() ?: 0,
                        warmup
                    )
                    reps = ""
                },
                enabled = canLog,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Brass,
                    contentColor = Slate,
                    disabledContainerColor = Raised,
                    disabledContentColor = Faint
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Log set", style = MaterialTheme.typography.labelLarge) }

            Spacer(Modifier.height(26.dp))
            Hairline()
            Spacer(Modifier.height(20.dp))
            SectionHeading("This session", trailing = if (state.sets.isEmpty()) null else "newest last")
            Spacer(Modifier.height(10.dp))
        }

        if (state.sets.isEmpty()) {
            item { EmptyNote("No sets logged. Pick a lift and put the first one in.") }
        } else {
            items(state.sets, key = { it.setId }) { row ->
                val isPr = row.setId == prSetId
                EntryRow(
                    title = row.exerciseName + if (isPr) "  best yet" else "",
                    detail = if (row.isWarmup) "warm-up" else "set ${row.setIndex}",
                    trailing = "${Training.formatWeight(row.weightKg)} x ${row.reps}",
                    accent = if (row.isWarmup) Mute else Brass,
                    onClick = { onDeleteSet(row.setId) },
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            item {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tap a set to remove it.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
            }
        }
    }

    if (showPicker) {
        ExercisePicker(
            exercises = state.exercises,
            onPick = { onSelectExercise(it.id); showPicker = false },
            onAddNew = { showPicker = false; showNewExercise = true },
            onDismiss = { showPicker = false }
        )
    }

    if (showNewExercise) {
        NewExerciseDialog(
            onSave = { name, group -> onAddExercise(name, group); showNewExercise = false },
            onDismiss = { showNewExercise = false }
        )
    }
}

/**
 * The target for this lift, read off your own history.
 *
 * Tapping it fills the weight and reps fields, because the whole point is to remove
 * a decision from the thirty seconds between two sets — if it takes typing, it is
 * no faster than remembering.
 */
@Composable
private fun TargetCard(guidance: LiftGuidance, onUseTarget: (Double, Int) -> Unit) {
    val rec = guidance.recommendation

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brass.copy(alpha = 0.10f))
            .padding(16.dp)
    ) {
        if (rec == null) {
            Text(
                "No history on this lift yet",
                style = MaterialTheme.typography.labelMedium,
                color = Mute
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Log one working set and a target appears here from the next session on. " +
                    "The app will not guess a starting weight for you.",
                style = MaterialTheme.typography.labelSmall,
                color = Faint
            )
            return@Column
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (rec.isStepUp) "Step up" else "Next target",
                style = MaterialTheme.typography.labelMedium,
                color = if (rec.isStepUp) Brass else Mute
            )
            if (rec.isStalled) {
                Text("stalling", style = MaterialTheme.typography.labelSmall, color = Coral)
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                rec.headline,
                style = MaterialTheme.typography.displaySmall,
                color = Brass
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { onUseTarget(rec.weightKg, rec.reps) }) {
                Text("Use", color = Brass, style = MaterialTheme.typography.labelLarge)
            }
        }

        Text(
            rec.rationale,
            style = MaterialTheme.typography.labelSmall,
            color = Mute
        )

        if (guidance.bestE1rm > 0) {
            Spacer(Modifier.height(14.dp))
            Hairline()
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "estimated max ${Training.formatWeight(guidance.bestE1rm)} kg",
                    style = MaterialTheme.typography.labelSmall,
                    color = Mute
                )
                Text(
                    "next ${Training.formatWeight(guidance.nextMilestone)} kg",
                    style = MaterialTheme.typography.labelSmall,
                    color = Brass
                )
            }
            Spacer(Modifier.height(6.dp))
            Meter(progress = guidance.milestoneProgress, accent = Brass, height = 4)
        }
    }
}

@Composable
private fun RestBanner(secondsLeft: Int, onSkip: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Brass.copy(alpha = 0.12f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "%d:%02d".format(secondsLeft / 60, secondsLeft % 60),
                style = MaterialTheme.typography.displaySmall,
                color = Brass
            )
            Text("resting", style = MaterialTheme.typography.labelSmall, color = Mute)
        }
        TextButton(onClick = onSkip) { Text("Skip rest", color = Brass) }
    }
}

@Composable
private fun ExercisePicker(
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
                // The catalogue is past fifty lifts now. Scrolling to find a cable curl
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
                        items(inGroup, key = { it.id }) { exercise ->
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
private fun NewExerciseDialog(onSave: (String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("Push") }

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
                PillRow(listOf("Push", "Pull", "Legs", "Core"), group, Brass, { group = it })
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
