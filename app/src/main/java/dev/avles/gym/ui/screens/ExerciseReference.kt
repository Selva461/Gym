package dev.avles.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.avles.gym.data.Exercise
import dev.avles.gym.domain.ExerciseGuide
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.MuscleMap
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Coral
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Raised

/**
 * What a lift is, how it moves, and what it works.
 *
 * Everything here is computed from the exercise row by [ExerciseGuide] — there is no
 * per-exercise content to load, no image to ship and no network call, which is why
 * it works for all 394 seeded lifts and for anything invented on the phone
 * afterwards.
 *
 * The order is the order you need it in standing at the rack: what it works, how to
 * set up, what to do, the two words to hold in your head during the set, and last
 * the things that go wrong. Mistakes are at the bottom on purpose — useful to read
 * once, not something to scroll past every time.
 */
@Composable
fun ExerciseReferenceDialog(
    exercise: Exercise,
    onDismiss: () -> Unit
) {
    val guide = ExerciseGuide.of(exercise)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Raised,
        title = {
            Column {
                Text(exercise.name, style = MaterialTheme.typography.titleMedium, color = Bone)
                Spacer(Modifier.height(2.dp))
                Text(guide.summary, style = MaterialTheme.typography.labelSmall, color = Brass)
            }
        },
        text = {
            Column(
                Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                MuscleMap(
                    primary = guide.primary,
                    secondary = guide.secondary,
                    modifier = Modifier.fillMaxWidth()
                )

                ReferenceSection("Set up", guide.setup)
                ReferenceSection("The movement", guide.execution)

                if (guide.cues.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Hairline()
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "While you lift",
                        style = MaterialTheme.typography.labelSmall,
                        color = Faint
                    )
                    Spacer(Modifier.height(6.dp))
                    // Cues are what you repeat to yourself mid-set, so they are set as
                    // short standalone phrases rather than another bulleted paragraph.
                    guide.cues.forEach { cue ->
                        Text(
                            text = "“$cue”",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Brass,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }

                ReferenceSection("What goes wrong", guide.mistakes, bulletColor = Coral)

                if (guide.variations.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Hairline()
                    Spacer(Modifier.height(12.dp))
                    Text("Ways to vary it", style = MaterialTheme.typography.labelSmall, color = Faint)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Logged per set. Each one keeps its own history and its own " +
                            "records, so a close grip never sets the target for a wide one.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Faint
                    )
                    Spacer(Modifier.height(8.dp))
                    guide.variations.forEach { axis ->
                        Row(Modifier.padding(bottom = 4.dp)) {
                            Text(
                                text = axis.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = Mute,
                                modifier = Modifier.width(96.dp)
                            )
                            Text(
                                text = axis.options.joinToString("  ·  "),
                                style = MaterialTheme.typography.labelSmall,
                                color = Bone
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Hairline()
                Spacer(Modifier.height(10.dp))
                // Said plainly rather than buried. The guide is derived from the
                // movement pattern, which makes it right about the mechanics and
                // silent about you — and the reader should know which is which.
                Text(
                    "Written from the movement pattern, not for your body. It is " +
                        "right about which way the weight goes and what usually " +
                        "goes wrong; it has never seen you lift.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = Brass) }
        }
    )
}

/** A headed list of points, each with a bullet that carries the section's tone. */
@Composable
private fun ReferenceSection(
    heading: String,
    points: List<String>,
    bulletColor: Color = Brass
) {
    if (points.isEmpty()) return
    Spacer(Modifier.height(16.dp))
    Hairline()
    Spacer(Modifier.height(12.dp))
    Text(heading, style = MaterialTheme.typography.labelSmall, color = Faint)
    Spacer(Modifier.height(6.dp))
    points.forEach { point ->
        Row(
            modifier = Modifier.padding(bottom = 6.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                Modifier
                    .padding(top = 7.dp, end = 10.dp)
                    .height(4.dp)
                    .width(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(bulletColor)
            )
            Text(point, style = MaterialTheme.typography.bodyMedium, color = Bone)
        }
    }
}

/**
 * The grip, stance and tempo chips shown under the lift while a session is open.
 *
 * Every axis carries an explicit "Any" at the front rather than relying on tapping
 * the selected chip again to clear it: unspecified is a real choice — it is what
 * every set logged before this feature existed is — and a choice you cannot see is
 * one people assume they have already made.
 */
@Composable
fun VariationPicker(
    axes: List<dev.avles.gym.domain.VariationAxis>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (axes.isEmpty()) return
    Column(modifier) {
        axes.forEach { axis ->
            Text(
                text = axis.name,
                style = MaterialTheme.typography.labelSmall,
                color = Faint,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            dev.avles.gym.ui.components.ChipRow(
                options = listOf(ANY) + axis.options,
                selected = selected.ifBlank { ANY },
                accent = Brass,
                onSelect = { onSelect(if (it == ANY) "" else it) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** The label for "unspecified", which is stored as an empty string, never as this word. */
private const val ANY = "Any"
