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
import androidx.compose.foundation.layout.width
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
import dev.avles.gym.data.SeedData
import dev.avles.gym.data.SessionSummary
import dev.avles.gym.data.SetRow
import dev.avles.gym.data.SplitPlan
import dev.avles.gym.data.WorkoutSession
import dev.avles.gym.domain.Training
import dev.avles.gym.ui.LiftGuidance
import dev.avles.gym.ui.SessionDetail
import dev.avles.gym.ui.TrainState
import dev.avles.gym.ui.components.BigNumber
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.EntryRow
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.Meter
import dev.avles.gym.ui.components.ChipRow
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

private val AD_HOC_LABELS = listOf("Push", "Pull", "Legs", "Full")

@Composable
fun TrainScreen(
    state: TrainState,
    restRemaining: Int?,
    prSetId: Long?,
    guidance: LiftGuidance?,
    splits: List<SplitPlan>,
    activePlan: SplitPlan?,
    summaries: Map<Long, SessionSummary>,
    openSession: SessionDetail?,
    onSelectExercise: (Long?) -> Unit,
    onSelectVariation: (String) -> Unit,
    onStartFromSplit: (SplitPlan) -> Unit,
    onStartBlank: (String) -> Unit,
    onSaveSplit: (Long, String, List<Long>) -> Unit,
    onDeleteSplit: (Long) -> Unit,
    onOpenSession: (Long?) -> Unit,
    onRenameSession: (WorkoutSession, String) -> Unit,
    onReopenSession: (WorkoutSession) -> Unit,
    onDeleteSession: (Long) -> Unit,
    onFinish: () -> Unit,
    onLogSet: (Long, Double, Int, Boolean) -> Unit,
    onEditSet: (Long, Double, Int, Boolean, String) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onSkipRest: () -> Unit,
    onAddExercise: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Hoisted here because a set can be opened from the live session or from a past
    // one inside the session dialog, and both should land on the same editor.
    var editingSet by remember { mutableStateOf<SetRow?>(null) }

    if (state.active == null) {
        StartSessionPane(
            state = state,
            splits = splits,
            summaries = summaries,
            onStartFromSplit = onStartFromSplit,
            onStartBlank = onStartBlank,
            onSaveSplit = onSaveSplit,
            onDeleteSplit = onDeleteSplit,
            onOpenSession = onOpenSession,
            modifier = modifier
        )
    } else {
        ActiveSessionPane(
            state = state,
            restRemaining = restRemaining,
            prSetId = prSetId,
            guidance = guidance,
            activePlan = activePlan,
            onSelectExercise = onSelectExercise,
            onSelectVariation = onSelectVariation,
            onFinish = onFinish,
            onLogSet = onLogSet,
            onEditSetRow = { editingSet = it },
            onSkipRest = onSkipRest,
            onAddExercise = onAddExercise,
            modifier = modifier
        )
    }

    openSession?.let { detail ->
        SessionDetailDialog(
            detail = detail,
            canReopen = state.active == null,
            onRename = { onRenameSession(detail.session, it) },
            onReopen = { onReopenSession(detail.session) },
            onDeleteSession = { onDeleteSession(detail.session.id) },
            onEditSet = { editingSet = it },
            onDismiss = { onOpenSession(null) }
        )
    }

    editingSet?.let { row ->
        SetEditorDialog(
            row = row,
            onSave = { w, r, warm ->
                onEditSet(row.setId, w, r, warm, row.variation)
                editingSet = null
            },
            onDelete = { onDeleteSet(row.setId); editingSet = null },
            onDismiss = { editingSet = null }
        )
    }
}

@Composable
private fun StartSessionPane(
    state: TrainState,
    splits: List<SplitPlan>,
    summaries: Map<Long, SessionSummary>,
    onStartFromSplit: (SplitPlan) -> Unit,
    onStartBlank: (String) -> Unit,
    onSaveSplit: (Long, String, List<Long>) -> Unit,
    onDeleteSplit: (Long) -> Unit,
    onOpenSession: (Long?) -> Unit,
    modifier: Modifier
) {
    var editingSplit by remember { mutableStateOf<SplitPlan?>(null) }
    var creatingSplit by remember { mutableStateOf(false) }
    var showAdHoc by remember { mutableStateOf(false) }
    var adHocLabel by remember { mutableStateOf("Push") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)
    ) {
        item {
            Text("Train", style = MaterialTheme.typography.headlineMedium, color = Bone)
            Text(
                "Start a split and its lifts come with it.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mute
            )
            Spacer(Modifier.height(20.dp))
            SectionHeading("Your splits", trailing = "${splits.size}")
            Spacer(Modifier.height(10.dp))
        }

        if (splits.isEmpty()) {
            item { EmptyNote("No splits yet. Build one and you never pick lifts from scratch again.") }
        } else {
            // Namespaced keys. These splits and the sessions further down live in the
            // SAME LazyColumn, and Compose requires keys unique across the whole list,
            // not per items() block. Both were keyed on a raw row id, so split 1 and
            // session 1 collided and Compose threw on composition — the Train tab
            // closed the app outright the moment you had one past session.
            items(splits, key = { "split-${it.id}" }) { plan ->
                SplitCard(
                    plan = plan,
                    onStart = { onStartFromSplit(plan) },
                    onEdit = { editingSplit = plan }
                )
            }
        }

        item {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { creatingSplit = true }) {
                    Text("New split", color = Brass)
                }
                TextButton(onClick = { showAdHoc = !showAdHoc }) {
                    Text(if (showAdHoc) "Hide" else "Start without a split", color = Mute)
                }
            }

            if (showAdHoc) {
                Spacer(Modifier.height(8.dp))
                PillRow(AD_HOC_LABELS, adHocLabel, Brass, { adHocLabel = it })
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { onStartBlank(adHocLabel) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brass.copy(alpha = 0.16f),
                        contentColor = Brass
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Start $adHocLabel session") }
            }

            Spacer(Modifier.height(28.dp))
            Hairline()
            Spacer(Modifier.height(22.dp))
            SectionHeading(
                "Recent sessions",
                trailing = if (state.recent.isEmpty()) null else "tap to edit"
            )
            Spacer(Modifier.height(10.dp))
        }

        if (state.recent.isEmpty()) {
            item { EmptyNote("No sessions yet. The first one is the only hard one.") }
        } else {
            items(state.recent, key = { "session-${it.id}" }) { session ->
                val summary = summaries[session.id]
                val minutes = session.endedAt?.let { ((it - session.startedAt) / 60000).toInt() }
                val detail = buildString {
                    append(java.time.LocalDate.ofEpochDay(session.dateEpochDay).toString())
                    summary?.let { append("   ${it.setCount} sets   ${it.volume.roundToInt()} kg") }
                }
                EntryRow(
                    title = session.label,
                    detail = detail,
                    trailing = minutes?.let { "$it min" } ?: "open",
                    accent = Brass,
                    onClick = { onOpenSession(session.id) },
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }

    if (creatingSplit) {
        SplitEditorDialog(
            initial = null,
            catalogue = state.exercises,
            onSave = { name, ids -> onSaveSplit(0L, name, ids); creatingSplit = false },
            onDelete = null,
            onDismiss = { creatingSplit = false }
        )
    }

    editingSplit?.let { plan ->
        SplitEditorDialog(
            initial = plan,
            catalogue = state.exercises,
            onSave = { name, ids -> onSaveSplit(plan.id, name, ids); editingSplit = null },
            onDelete = { onDeleteSplit(plan.id); editingSplit = null },
            onDismiss = { editingSplit = null }
        )
    }
}

@Composable
private fun SplitCard(plan: SplitPlan, onStart: () -> Unit, onEdit: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
            .background(Slate),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(3.dp).height(50.dp).background(Brass))
        Column(
            Modifier
                .weight(1f)
                .clickable { onStart() }
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Text(plan.name, style = MaterialTheme.typography.bodyLarge, color = Bone)
            Text(
                if (plan.exercises.isEmpty()) {
                    "no lifts yet"
                } else {
                    plan.exercises.take(3).joinToString(", ") { it.name } +
                        if (plan.exercises.size > 3) " +${plan.exercises.size - 3}" else ""
                },
                style = MaterialTheme.typography.labelSmall,
                color = Faint,
                maxLines = 1
            )
        }
        TextButton(onClick = onEdit) { Text("Edit", color = Mute) }
    }
}

@Composable
private fun ActiveSessionPane(
    state: TrainState,
    restRemaining: Int?,
    prSetId: Long?,
    guidance: LiftGuidance?,
    activePlan: SplitPlan?,
    onSelectExercise: (Long?) -> Unit,
    onSelectVariation: (String) -> Unit,
    onFinish: () -> Unit,
    onLogSet: (Long, Double, Int, Boolean) -> Unit,
    onEditSetRow: (SetRow) -> Unit,
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
    var showingReference by remember { mutableStateOf<Exercise?>(null) }

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

            // The split's own lifts, which is the whole point of having started from
            // one: no searching a catalogue of eighty-odd between two sets.
            if (activePlan != null && activePlan.exercises.isNotEmpty()) {
                SplitLiftRow(
                    plan = activePlan,
                    selectedId = selectedExercise?.id,
                    onPick = { onSelectExercise(it.id) }
                )
                Spacer(Modifier.height(12.dp))
            }

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
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showingReference = selectedExercise }) {
                        Text("How to do it", color = Brass, style = MaterialTheme.typography.labelSmall)
                    }
                    guidance.guide?.let {
                        Text(
                            it.summary,
                            style = MaterialTheme.typography.labelSmall,
                            color = Faint
                        )
                    }
                }

                // Grip and stance live directly under the lift because they have to be
                // set before the set is logged, not remembered afterwards.
                guidance.guide?.let { g ->
                    VariationPicker(
                        axes = g.variations,
                        selected = guidance.variation,
                        onSelect = onSelectVariation,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))
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
            items(state.sets, key = { "set-${it.setId}" }) { row ->
                val isPr = row.setId == prSetId
                EntryRow(
                    title = row.exerciseName + if (isPr) "  best yet" else "",
                    detail = listOfNotNull(
                        if (row.isWarmup) "warm-up" else "set ${row.setIndex}",
                        row.variationLabel
                    ).joinToString("  ·  "),
                    trailing = "${Training.formatWeight(row.weightKg)} x ${row.reps}",
                    accent = if (row.isWarmup) Mute else Brass,
                    onClick = { onEditSetRow(row) },
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            item {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tap a set to change or remove it.",
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

    showingReference?.let { exercise ->
        ExerciseReferenceDialog(exercise = exercise, onDismiss = { showingReference = null })
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
private fun NewExerciseDialog(onSave: (String, String) -> Unit, onDismiss: () -> Unit) {
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
