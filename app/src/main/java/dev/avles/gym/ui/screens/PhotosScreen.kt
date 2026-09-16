package dev.avles.gym.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import dev.avles.gym.domain.Photos
import dev.avles.gym.ui.PhotoRow
import dev.avles.gym.ui.PhotoState
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.PhotoImage
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Coral
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import java.time.LocalDate

/**
 * Progress photos: a timeline, and the oldest against the newest.
 *
 * Pictures never leave the phone and never enter its gallery. They are copied into
 * the app's private storage, where only this app's UID can read them, and they are
 * deliberately left out of cloud backup — a weight history encrypted on somebody's
 * server is a reasonable trade, a body in a mirror is a different question, and it
 * is not one to answer on the user's behalf by default. The screen says so, once,
 * where it matters rather than buried in Settings.
 *
 * Picking uses the system photo picker, which needs no permission at all: it runs
 * outside this process and hands back exactly the one image chosen. Asking for
 * READ_MEDIA_IMAGES would grant access to every picture on the phone forever, to
 * import one.
 */
@Composable
fun PhotosScreen(
    state: PhotoState,
    error: String?,
    today: LocalDate,
    onAdd: (android.net.Uri, Long, String) -> Unit,
    onDelete: (Long) -> Unit,
    onEdit: (Long, String, String) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingLabel by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<PhotoRow?>(null) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onAdd(uri, today.toEpochDay(), pendingLabel)
        pendingLabel = ""
    }

    LazyColumn(modifier = modifier.padding(horizontal = 20.dp)) {
        item {
            Spacer(Modifier.height(16.dp))
            SectionHeading("Progress photos")
            Spacer(Modifier.height(6.dp))
            Text(
                "Kept inside the app, not in your gallery, and left out of cloud " +
                    "backup on purpose. Reinstalling the app loses them.",
                style = MaterialTheme.typography.labelSmall,
                color = Faint
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Brass.copy(alpha = 0.16f),
                    contentColor = Brass
                ),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Add today's photo") }
            Spacer(Modifier.height(20.dp))
        }

        state.comparison?.let { (first, last) ->
            item {
                Hairline()
                Spacer(Modifier.height(18.dp))
                val days = Photos.daysBetween(first.photo.dateEpochDay, last.photo.dateEpochDay)
                SectionHeading("Then and now", trailing = "$days days apart")
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ComparisonHalf(first, "Then", Modifier.weight(1f))
                    ComparisonHalf(last, "Now", Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        item {
            Hairline()
            Spacer(Modifier.height(18.dp))
            SectionHeading(
                "Every photo",
                trailing = if (state.photos.isEmpty()) null else "tap to label or remove"
            )
            Spacer(Modifier.height(10.dp))
        }

        if (state.photos.isEmpty()) {
            item {
                EmptyNote(
                    "No photos yet. One a month from the same spot, same light, " +
                        "tells you more than the scale does."
                )
            }
        } else {
            items(state.photos, key = { "photo-${it.photo.id}" }) { row ->
                PhotoCard(row, onClick = { editing = row })
                Spacer(Modifier.height(12.dp))
            }
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "${state.photos.size} photos, ${state.bytesUsed / 1024 / 1024} MB",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    editing?.let { row ->
        PhotoEditorDialog(
            row = row,
            onSave = { label, note -> onEdit(row.photo.id, label, note); editing = null },
            onDelete = { onDelete(row.photo.id); editing = null },
            onDismiss = { editing = null }
        )
    }

    error?.let {
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Could not add that photo", color = Bone) },
            text = { Text(it, color = Mute) },
            confirmButton = { TextButton(onClick = onDismissError) { Text("OK", color = Brass) } }
        )
    }
}

@Composable
private fun ComparisonHalf(row: PhotoRow, label: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        PhotoImage(
            file = row.file,
            contentDescription = "$label, ${LocalDate.ofEpochDay(row.photo.dateEpochDay)}",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
        )
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Brass)
        Text(
            LocalDate.ofEpochDay(row.photo.dateEpochDay).toString(),
            style = MaterialTheme.typography.labelSmall,
            color = Faint
        )
    }
}

@Composable
private fun PhotoCard(row: PhotoRow, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        PhotoImage(
            file = row.file,
            contentDescription = row.photo.label.ifBlank { "Progress photo" },
            modifier = Modifier
                .height(96.dp)
                .aspectRatio(0.75f)
        )
        Column(Modifier.padding(start = 14.dp)) {
            Text(
                LocalDate.ofEpochDay(row.photo.dateEpochDay).toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = Bone
            )
            if (row.photo.label.isNotBlank()) {
                Text(row.photo.label, style = MaterialTheme.typography.labelSmall, color = Brass)
            }
            if (row.photo.note.isNotBlank()) {
                Text(row.photo.note, style = MaterialTheme.typography.labelSmall, color = Mute)
            }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onClick, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                Text("Label or remove", color = Faint, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun PhotoEditorDialog(
    row: PhotoRow,
    onSave: (String, String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember(row.photo.id) { mutableStateOf(row.photo.label) }
    var note by remember(row.photo.id) { mutableStateOf(row.photo.note) }
    // Deleting a photo is not recoverable and the file goes with the row, so it
    // takes a second tap — the same rule a session delete follows.
    var confirmingDelete by remember(row.photo.id) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LocalDate.ofEpochDay(row.photo.dateEpochDay).toString(), color = Bone) },
        text = {
            Column {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Angle — front, side, back") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
                TextButton(onClick = { if (confirmingDelete) onDelete() else confirmingDelete = true }) {
                    Text(
                        if (confirmingDelete) "Tap again to delete this photo" else "Delete this photo",
                        color = Coral
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(label, note) }) { Text("Save", color = Brass) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Mute) } }
    )
}
