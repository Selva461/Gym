package dev.avles.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import dev.avles.gym.data.SetRow
import dev.avles.gym.data.SplitPlan
import dev.avles.gym.domain.Training
import dev.avles.gym.ui.SessionDetail
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.PillRow
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Coral
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Raised
import dev.avles.gym.ui.theme.Slate
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * Editing a logged set.
 *
 * This replaces tap-to-delete. Deleting on a single tap was fine when a mis-logged
 * set was the only thing that could be wrong with it, but the common case is
 * mistyping one digit of a weight — and re-entering the whole set to fix 8 into 80
 * is worse than a dialog. Delete is still here, one tap further in.
 */
@Composable
fun SetEditorDialog(
    row: SetRow,
    onSave: (Double, Int, Boolean) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var weight by remember(row.setId) { mutableStateOf(Training.formatWeight(row.weightKg)) }
    var reps by remember(row.setId) { mutableStateOf(row.reps.toString()) }
    var warmup by remember(row.setId) { mutableStateOf(row.isWarmup) }

    val valid = (weight.toDoubleOrNull() ?: 0.0) > 0 && (reps.toIntOrNull() ?: 0) > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text(row.exerciseName, color = Bone) },
        text = {
            Column {
                Text(
                    if (row.isWarmup) "Warm-up" else "Set ${row.setIndex}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Mute
                )
                Spacer(Modifier.height(14.dp))
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
                Spacer(Modifier.height(14.dp))
                PillRow(
                    options = listOf("Working set", "Warm-up"),
                    selected = if (warmup) "Warm-up" else "Working set",
                    accent = Brass,
                    onSelect = { warmup = it == "Warm-up" }
                )
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onDelete) { Text("Delete this set", color = Coral) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onSave(
                        weight.toDoubleOrNull() ?: row.weightKg,
                        reps.toIntOrNull() ?: row.reps,
                        warmup
                    )
                }
            ) { Text("Save", color = if (valid) Brass else Faint) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Mute) } }
    )
}

/**
 * A past session, opened. Rename it, re-open it to add more work, delete it, or fix
 * any set on it.
 *
 * Deleting takes a second tap and says how much it is about to destroy, because
 * unlike a single set a session can be an hour of work and there is no undo.
 */
@Composable
fun SessionDetailDialog(
    detail: SessionDetail,
    canReopen: Boolean,
    onRename: (String) -> Unit,
    onReopen: () -> Unit,
    onDeleteSession: () -> Unit,
    onEditSet: (SetRow) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember(detail.session.id) { mutableStateOf(detail.session.label) }
    var confirmingDelete by remember(detail.session.id) { mutableStateOf(false) }

    val setCount = detail.sets.size
    val volume = Training.volume(detail.sets)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text("Edit session", color = Bone) },
        text = {
            Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState())) {
                Text(
                    LocalDate.ofEpochDay(detail.session.dateEpochDay).toString() +
                        "  ·  $setCount ${if (setCount == 1) "set" else "sets"}" +
                        "  ·  ${volume.roundToInt()} kg" +
                        if (detail.session.endedAt == null) "  ·  still open" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = Mute
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (canReopen && detail.session.endedAt != null) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onReopen) {
                        Text("Re-open and add more sets", color = Brass)
                    }
                }

                Spacer(Modifier.height(14.dp))
                Hairline()
                Spacer(Modifier.height(12.dp))

                if (detail.sets.isEmpty()) {
                    EmptyNote("No sets on this session.")
                } else {
                    Text(
                        "Tap a set to change it",
                        style = MaterialTheme.typography.labelSmall,
                        color = Faint
                    )
                    Spacer(Modifier.height(8.dp))
                    detail.sets.forEach { row ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onEditSet(row) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    row.exerciseName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Bone
                                )
                                Text(
                                    if (row.isWarmup) "warm-up" else "set ${row.setIndex}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Faint
                                )
                            }
                            Text(
                                "${Training.formatWeight(row.weightKg)} × ${row.reps}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (row.isWarmup) Mute else Brass
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Hairline()
                Spacer(Modifier.height(12.dp))
                if (!confirmingDelete) {
                    TextButton(onClick = { confirmingDelete = true }) {
                        Text("Delete this session", color = Coral)
                    }
                } else {
                    Text(
                        "Deleting removes the session and all $setCount of its " +
                            "${if (setCount == 1) "set" else "sets"}. This cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Coral
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onDeleteSession,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Coral,
                                contentColor = Slate
                            )
                        ) { Text("Delete for good") }
                        TextButton(onClick = { confirmingDelete = false }) {
                            Text("Keep it", color = Mute)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = label.isNotBlank() && label != detail.session.label,
                onClick = { onRename(label) }
            ) {
                Text(
                    "Save name",
                    color = if (label.isNotBlank() && label != detail.session.label) Brass else Faint
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close", color = Mute) } }
    )
}

/**
 * Building a split: a name and an ordered list of lifts.
 *
 * Order is editable with arrows rather than drag-and-drop. Dragging inside a
 * scrolling dialog fights the scroll, needs a library, and this list is six items
 * long — two taps to move something is not the bottleneck.
 */
@Composable
fun SplitEditorDialog(
    initial: SplitPlan?,
    catalogue: List<Exercise>,
    onSave: (String, List<Long>) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val key = initial?.id ?: 0L
    var name by remember(key) { mutableStateOf(initial?.name ?: "") }
    // The type argument is spelled out on purpose. `?: emptyList()` two lambdas deep
    // leaves the element type unpinned, the delegate then resolves to Any?, and every
    // later use of `chosen` fails with an error that names none of that.
    var chosen by remember(key) {
        mutableStateOf<List<Long>>(initial?.exercises?.map { it.id }.orEmpty())
    }
    var query by remember(key) { mutableStateOf("") }
    var confirmingDelete by remember(key) { mutableStateOf(false) }

    val byId = remember(catalogue) { catalogue.associateBy { it.id } }
    val available = remember(query, catalogue, chosen) {
        catalogue
            .filter { it.id !in chosen }
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .take(if (query.isBlank()) 8 else 20)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text(if (initial == null) "New split" else "Edit split", color = Bone) },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("Push A") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))
                Text(
                    if (chosen.isEmpty()) "No lifts yet" else "${chosen.size} lifts, in order",
                    style = MaterialTheme.typography.labelMedium,
                    color = Mute
                )
                Spacer(Modifier.height(6.dp))

                chosen.forEachIndexed { index, id ->
                    val exercise = byId[id] ?: return@forEachIndexed
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${index + 1}.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Faint,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            exercise.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Bone,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            enabled = index > 0,
                            onClick = { chosen = chosen.swapped(index, index - 1) }
                        ) { Text("↑", color = if (index > 0) Mute else Faint) }
                        TextButton(
                            enabled = index < chosen.size - 1,
                            onClick = { chosen = chosen.swapped(index, index + 1) }
                        ) { Text("↓", color = if (index < chosen.size - 1) Mute else Faint) }
                        TextButton(onClick = { chosen = chosen - id }) {
                            Text("×", color = Coral)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Hairline()
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Add a lift") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                available.forEach { exercise ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { chosen = chosen + exercise.id }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                exercise.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Bone
                            )
                            Text(
                                exercise.muscleGroup,
                                style = MaterialTheme.typography.labelSmall,
                                color = Faint
                            )
                        }
                        Text("+", style = MaterialTheme.typography.titleMedium, color = Brass)
                    }
                }
                if (available.isEmpty() && query.isNotBlank()) {
                    Text(
                        "Nothing matches \"$query\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Faint,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }

                if (onDelete != null) {
                    Spacer(Modifier.height(12.dp))
                    Hairline()
                    Spacer(Modifier.height(10.dp))
                    if (!confirmingDelete) {
                        TextButton(onClick = { confirmingDelete = true }) {
                            Text("Delete this split", color = Coral)
                        }
                    } else {
                        Text(
                            "Sessions already run from this split keep their name and " +
                                "every set. Only the plan goes.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Mute
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onDelete,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Coral,
                                    contentColor = Slate
                                )
                            ) { Text("Delete split") }
                            TextButton(onClick = { confirmingDelete = false }) {
                                Text("Keep it", color = Mute)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onSave(name, chosen) }
            ) { Text("Save split", color = if (name.isNotBlank()) Brass else Faint) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Mute) } }
    )
}

private fun List<Long>.swapped(a: Int, b: Int): List<Long> =
    toMutableList().also { it[a] = this[b]; it[b] = this[a] }

/** The lifts on the open session's split, as a row of one-tap chips. */
@Composable
fun SplitLiftRow(
    plan: SplitPlan,
    selectedId: Long?,
    onPick: (Exercise) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        Text(
            "${plan.name} · tap a lift",
            style = MaterialTheme.typography.labelSmall,
            color = Faint
        )
        Spacer(Modifier.height(8.dp))
        // A plain Column, not a LazyColumn. This sits inside the Train screen's own
        // lazy list, and a second vertical scroller nested in the first is awkward to
        // drag even when its height is bounded. A split is five to eight lifts; there
        // is nothing here worth virtualising.
        plan.exercises.forEach { exercise ->
            val on = exercise.id == selectedId
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (on) Brass.copy(alpha = 0.16f) else Raised)
                    .clickable { onPick(exercise) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    exercise.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (on) Brass else Bone,
                    maxLines = 1
                )
            }
        }
    }
}
