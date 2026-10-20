package dev.avles.gym.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.avles.gym.core.Dates
import dev.avles.gym.data.Exercise
import dev.avles.gym.data.PickedImage
import dev.avles.gym.data.SessionSummary
import dev.avles.gym.data.SetRow
import dev.avles.gym.data.SplitPlan
import dev.avles.gym.data.WorkoutSession
import dev.avles.gym.domain.LiftHistory
import dev.avles.gym.domain.Plates
import dev.avles.gym.domain.SetKinds
import dev.avles.gym.domain.Supersets
import dev.avles.gym.domain.Training
import dev.avles.gym.domain.Variations
import dev.avles.gym.ui.LiftGuidance
import dev.avles.gym.ui.SessionDetail
import dev.avles.gym.ui.TrainState
import dev.avles.gym.ui.components.BigNumber
import dev.avles.gym.ui.components.ChipRow
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.EntryRow
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.Haptic
import dev.avles.gym.ui.components.Motion
import dev.avles.gym.ui.components.Motion.fadeThrough
import dev.avles.gym.ui.components.PillRow
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.components.pressScale
import dev.avles.gym.ui.components.pressable
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Raised
import dev.avles.gym.ui.theme.Slate
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

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
    exercisePhotos: Map<Long, String?>,
    onAttachExercisePhoto: (Long, PickedImage) -> Unit,
    onDetachExercisePhoto: (Long) -> Unit,
    onStartFromSplit: (SplitPlan) -> Unit,
    onStartBlank: (String) -> Unit,
    onSaveSplit: (Long, String, List<Long>) -> Unit,
    onDeleteSplit: (Long) -> Unit,
    onOpenSession: (Long?) -> Unit,
    onRenameSession: (WorkoutSession, String) -> Unit,
    onReopenSession: (WorkoutSession) -> Unit,
    onDeleteSession: (Long) -> Unit,
    onFinish: () -> Unit,
    onLogSet: (Long, Double, Int, Boolean, String, Double) -> Unit,
    onEditSet: (Long, Double, Int, Boolean, String, String, Double) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onSkipRest: () -> Unit,
    onAddExercise: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    onAdjustRest: (Int) -> Unit = {},
    workoutReport: LiftHistory.Report? = null,
    onDismissReport: () -> Unit = {},
    liftHistory: (Long) -> Flow<List<SetRow>> = { flowOf(emptyList()) },
    onSetNotes: (Long, String) -> Unit = { _, _ -> },
    superset: Supersets.Pair? = null,
    onStartSuperset: (Long, Long) -> Unit = { _, _ -> },
    onEndSuperset: () -> Unit = {}
) {
    // Hoisted here because a set can be opened from the live session or from a past
    // one inside the session dialog, and both should land on the same editor.
    var editingSet by remember { mutableStateOf<SetRow?>(null) }

    // Starting or finishing a session swaps the whole screen; it settles in rather
    // than cutting, so the change of mode is felt as well as seen.
    AnimatedContent(targetState = state.active == null, transitionSpec = { fadeThrough() }, label = "train-mode") { idle ->
        if (idle) {
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
                exercisePhotos = exercisePhotos,
                onAttachExercisePhoto = onAttachExercisePhoto,
                onDetachExercisePhoto = onDetachExercisePhoto,
                onFinish = onFinish,
                onLogSet = onLogSet,
                onEditSetRow = { editingSet = it },
                onSkipRest = onSkipRest,
                onAdjustRest = onAdjustRest,
                onAddExercise = onAddExercise,
                liftHistory = liftHistory,
                onSetNotes = onSetNotes,
                superset = superset,
                onStartSuperset = onStartSuperset,
                onEndSuperset = onEndSuperset,
                modifier = modifier
            )
        }
    }

    workoutReport?.let { WorkoutReportDialog(it, onDismissReport) }

    openSession?.let { detail ->
        SessionDetailDialog(
            detail = detail,
            canReopen = state.active == null,
            onRename = { onRenameSession(detail.session, it) },
            onReopen = { onReopenSession(detail.session) },
            onDeleteSession = { onDeleteSession(detail.session.id) },
            onEditSet = { editingSet = it },
            onDismiss = { onOpenSession(null) },
            onSaveAsSplit = { name, ids -> onSaveSplit(0L, name, ids) }
        )
    }

    editingSet?.let { row ->
        SetEditorDialog(
            row = row,
            onSave = { w, r, warm, kind, rpe ->
                onEditSet(row.setId, w, r, warm, row.variation, kind, rpe)
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
                    modifier = Modifier.animateItem(),
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

            AnimatedVisibility(
                visible = showAdHoc,
                enter = expandVertically(Motion.standard()) + fadeIn(Motion.enter()),
                exit = shrinkVertically(Motion.standard()) + fadeOut(Motion.quick())
            ) {
                Column {
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
                    append(Dates.ofEpochDay(session.dateEpochDay).toString())
                    summary?.let { append("   ${it.setCount} sets   ${it.volume.roundToInt()} kg") }
                }
                EntryRow(
                    title = session.label,
                    detail = detail,
                    trailing = minutes?.let { "$it min" } ?: "open",
                    accent = Brass,
                    onClick = { onOpenSession(session.id) },
                    modifier = Modifier.animateItem().padding(bottom = 6.dp)
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
private fun SplitCard(plan: SplitPlan, onStart: () -> Unit, onEdit: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
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
                .pressable(haptic = Haptic.CONFIRM, onClick = onStart)
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
    exercisePhotos: Map<Long, String?>,
    onAttachExercisePhoto: (Long, PickedImage) -> Unit,
    onDetachExercisePhoto: (Long) -> Unit,
    onFinish: () -> Unit,
    onLogSet: (Long, Double, Int, Boolean, String, Double) -> Unit,
    onEditSetRow: (SetRow) -> Unit,
    onSkipRest: () -> Unit,
    onAdjustRest: (Int) -> Unit,
    onAddExercise: (String, String) -> Unit,
    liftHistory: (Long) -> Flow<List<SetRow>>,
    onSetNotes: (Long, String) -> Unit,
    superset: Supersets.Pair?,
    onStartSuperset: (Long, Long) -> Unit,
    onEndSuperset: () -> Unit,
    modifier: Modifier
) {
    val session = state.active ?: return
    val selectedExercise = guidance?.exercise
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var setType by remember { mutableStateOf(SetKinds.WORKING_LABEL) }
    var rpe by remember { mutableDoubleStateOf(0.0) }
    val warmup = setType == SetKinds.WARMUP_LABEL
    var showPicker by remember { mutableStateOf(false) }
    var showNewExercise by remember { mutableStateOf(false) }
    var showingReference by remember { mutableStateOf<Exercise?>(null) }
    var showingHistory by remember { mutableStateOf<Exercise?>(null) }
    var showPlates by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Exercise?>(null) }
    var swapping by remember { mutableStateOf<Exercise?>(null) }
    var pairing by remember { mutableStateOf<Exercise?>(null) }

    val volume = Training.volume(state.sets)
    val working = state.sets.count { !it.isWarmup }
    val haptics = LocalHapticFeedback.current
    // A new record gets a second, stronger buzz after the confirm of logging it, and
    // its row glows. Only a record set while this pane is open: the one already on
    // screen when you come back to the tab is old news and stays quiet.
    val recordOnArrival = remember { prSetId }
    val freshRecord = prSetId?.takeIf { it != recordOnArrival }
    LaunchedEffect(freshRecord) {
        if (freshRecord != null) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$working working sets  ·  ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Mute
                        )
                        SessionClock(session.startedAt)
                    }
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

            AnimatedVisibility(
                visible = restRemaining != null,
                enter = expandVertically(Motion.standard()) + fadeIn(Motion.enter()),
                exit = shrinkVertically(Motion.standard()) + fadeOut(Motion.quick())
            ) {
                // Holds the last value while the banner folds away, so it never
                // collapses showing a blank clock.
                val last = remember { IntArray(1) }
                if (restRemaining != null) last[0] = restRemaining
                Column {
                    Spacer(Modifier.height(16.dp))
                    RestBanner(restRemaining ?: last[0], onSkipRest, onAdjustRest)
                }
            }

            Spacer(Modifier.height(22.dp))
            Hairline()
            Spacer(Modifier.height(20.dp))

            if (superset != null) {
                val names = state.exercises.associate { it.id to it.name }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brass.copy(alpha = 0.12f))
                        .padding(start = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Superset: ${names[superset.first] ?: "?"} + ${names[superset.second] ?: "?"}. " +
                            "Rest comes after the second.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Brass,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onEndSuperset) { Text("End", color = Brass) }
                }
                Spacer(Modifier.height(12.dp))
            }

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
                    .pressable(RoundedCornerShape(14.dp), pressedScale = 0.985f) { showPicker = true }
                    .background(Slate)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    selectedExercise?.name ?: "Choose a lift",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selectedExercise == null) Faint else Bone
                )
            }
            selectedExercise?.notes?.takeIf { it.isNotBlank() }?.let { note ->
                Text(
                    note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mute,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            if (guidance != null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showingReference = selectedExercise }) {
                        Text("How to do it", color = Brass, style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = { showingHistory = selectedExercise }) {
                        Text("History", color = Brass, style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = { editingNote = selectedExercise }) {
                        Text("Note", color = Brass, style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = { swapping = selectedExercise }) {
                        Text("Swap", color = Brass, style = MaterialTheme.typography.labelSmall)
                    }
                    if (superset == null) {
                        TextButton(onClick = { pairing = selectedExercise }) {
                            Text("Superset", color = Brass, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                // On its own line now that four actions share the row above, which
                // would otherwise squeeze it to one word per line on a narrow phone.
                guidance.guide?.let {
                    Text(
                        it.summary,
                        style = MaterialTheme.typography.labelSmall,
                        color = Faint,
                        modifier = Modifier.padding(start = 12.dp)
                    )
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
                LastTimeRow(
                    sets = guidance.lastTime,
                    onUse = { set ->
                        weight = Training.formatWeight(set.weightKg)
                        reps = set.reps.toString()
                    },
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(if (selectedExercise?.isAssisted == true) "kg of help" else "kg") },
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

            val bar = guidance?.guide?.equipment?.let(Plates::barFor)
            if (selectedExercise != null && !selectedExercise.isAssisted && bar != null) {
                TextButton(onClick = { showPlates = true }) {
                    Text("What plates?", color = Brass, style = MaterialTheme.typography.labelSmall)
                }
            }

            if (selectedExercise?.isAssisted == true) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Assisted machine: more weight is easier. You progress by taking weight off, " +
                        "and your best is the least help for the most reps.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
            }

            Spacer(Modifier.height(12.dp))
            PillRow(
                options = SetKinds.PICKER.map { it.first },
                selected = setType,
                accent = Brass,
                onSelect = { setType = it }
            )
            // How hard it felt, for those who track it. Optional: "–" logs no RPE.
            if (!warmup) {
                Spacer(Modifier.height(8.dp))
                ChipRow(
                    options = listOf("RPE –") + SetKinds.RPE_CHOICES.map { "RPE " + SetKinds.rpeLabel(it) },
                    selected = if (rpe > 0) "RPE " + SetKinds.rpeLabel(rpe) else "RPE –",
                    accent = Brass,
                    onSelect = { label -> rpe = label.removePrefix("RPE ").toDoubleOrNull() ?: 0.0 }
                )
            }

            Spacer(Modifier.height(12.dp))
            // Zero is a real weight on an assisted machine: no help at all.
            val kg = weight.toDoubleOrNull()
            val canLog = selectedExercise != null &&
                kg != null && (kg > 0 || (kg == 0.0 && selectedExercise.isAssisted)) &&
                (reps.toIntOrNull() ?: 0) > 0
            val logPress = remember { MutableInteractionSource() }
            Button(
                onClick = {
                    val ex = selectedExercise ?: return@Button
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    onLogSet(
                        ex.id,
                        weight.toDoubleOrNull() ?: 0.0,
                        reps.toIntOrNull() ?: 0,
                        warmup,
                        SetKinds.kindFor(setType),
                        if (warmup) 0.0 else rpe
                    )
                    reps = ""
                },
                enabled = canLog,
                interactionSource = logPress,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Brass,
                    contentColor = Slate,
                    disabledContainerColor = Raised,
                    disabledContentColor = Faint
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp).pressScale(logPress)
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
                        Variations.label(row.variation).takeIf { it.isNotBlank() },
                        SetKinds.describe(row).takeIf { it.isNotBlank() }
                    ).joinToString("  ·  "),
                    trailing = Training.formatSet(row),
                    accent = if (row.isWarmup) Mute else Brass,
                    highlight = row.setId == freshRecord,
                    onClick = { onEditSetRow(row) },
                    modifier = Modifier.animateItem().padding(bottom = 6.dp)
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

    showingHistory?.let { exercise ->
        val history by remember(exercise.id) { liftHistory(exercise.id) }
            .collectAsStateWithLifecycle(emptyList())
        LiftHistoryDialog(exercise, history, onDismiss = { showingHistory = null })
    }

    pairing?.let { first ->
        ExercisePicker(
            exercises = state.exercises.filter { it.id != first.id },
            onPick = { second -> onStartSuperset(first.id, second.id); pairing = null },
            onAddNew = { pairing = null; showNewExercise = true },
            onDismiss = { pairing = null }
        )
    }

    swapping?.let { exercise ->
        SwapLiftDialog(
            target = exercise,
            catalogue = state.exercises,
            onPick = { onSelectExercise(it.id); swapping = null },
            onDismiss = { swapping = null }
        )
    }

    editingNote?.let { exercise ->
        LiftNoteDialog(
            exercise = exercise,
            onSave = { onSetNotes(exercise.id, it); editingNote = null },
            onDismiss = { editingNote = null }
        )
    }

    if (showPlates) {
        PlateCalculatorDialog(
            initialKg = weight.toDoubleOrNull(),
            initialBarKg = guidance?.guide?.equipment?.let(Plates::barFor) ?: Plates.OLYMPIC_BAR_KG,
            onDismiss = { showPlates = false }
        )
    }

    showingReference?.let { exercise ->
        ExerciseReferenceDialog(
            exercise = exercise,
            onDismiss = { showingReference = null },
            photoPath = exercisePhotos[exercise.id],
            onAttachPhoto = { uri -> onAttachExercisePhoto(exercise.id, uri) },
            onRemovePhoto = { onDetachExercisePhoto(exercise.id) }
        )
    }
}
