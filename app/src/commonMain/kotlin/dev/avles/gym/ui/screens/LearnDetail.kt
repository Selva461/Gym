package dev.avles.gym.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.avles.gym.anim.Avatar
import dev.avles.gym.anim.Demos
import dev.avles.gym.anim.Player
import dev.avles.gym.anim.Step
import dev.avles.gym.anim.StepKind
import dev.avles.gym.data.Exercise
import dev.avles.gym.domain.ExerciseGuide
import dev.avles.gym.ui.components.ChipRow
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.Figure3D
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.PillRow
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Coral
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Sage
import kotlinx.coroutines.delay

/**
 * One lift, taught: the demonstrator walks through set-up, grip, start position, the
 * rep slowly, the rep with the beginner's points, then the whole thing at speed.
 *
 * Playing moves through the steps on its own (a held position for a few seconds, a
 * moving step for two reps) and stops on the full movement, which loops. Tapping a
 * step jumps to it. The written guide sits underneath, the same text the session's
 * reference dialog shows.
 */
@Composable
fun LearnDetailScreen(
    exercise: Exercise,
    avatar: Avatar,
    onChooseAvatar: (Avatar) -> Unit,
    modifier: Modifier = Modifier
) {
    val guide = remember(exercise) { ExerciseGuide.of(exercise) }
    val motion = remember(exercise) { Demos.of(exercise, guide) }
    var index by rememberSaveable(exercise.id) { mutableIntStateOf(0) }
    var playing by rememberSaveable(exercise.id) { mutableStateOf(true) }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(exercise.name, style = MaterialTheme.typography.headlineMedium, color = Bone)
        Text(guide.summary, style = MaterialTheme.typography.bodyMedium, color = Mute)
        Spacer(Modifier.height(14.dp))

        if (motion == null) {
            EmptyNote("No demonstration for this one yet. The written guide is below.")
        } else {
            val steps = motion.steps
            val at = index.coerceIn(0, steps.lastIndex)
            val step = steps[at]

            LaunchedEffect(at, playing, motion) {
                if (!playing || at == steps.lastIndex) return@LaunchedEffect
                delay((stepSeconds(step) * 1000).toLong())
                index = at + 1
            }

            Figure3D(
                motion = motion,
                step = step,
                avatar = avatar,
                playing = playing,
                description = "${exercise.name}: ${step.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp)
                    .height(360.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Drag to turn it all the way round, pinch to zoom, double-tap to reset the view.",
                style = MaterialTheme.typography.labelSmall,
                color = Faint
            )
            Spacer(Modifier.height(12.dp))

            PillRow(
                options = Avatar.PRESETS.map { it.label },
                selected = avatar.label,
                accent = Sage,
                onSelect = { label -> Avatar.PRESETS.firstOrNull { it.label == label }?.let(onChooseAvatar) }
            )
            Spacer(Modifier.height(12.dp))

            ChipRow(
                options = steps.mapIndexed { i, s -> "${i + 1}  ${s.title}" },
                selected = "${at + 1}  ${step.title}",
                accent = Brass,
                onSelect = { chosen ->
                    index = steps.indices.firstOrNull { "${it + 1}  ${steps[it].title}" == chosen } ?: at
                }
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { index = (at - 1).coerceAtLeast(0) }, enabled = at > 0) {
                    Text("Back", color = if (at > 0) Mute else Faint)
                }
                TextButton(onClick = { playing = !playing }) {
                    Text(if (playing) "Pause" else "Play", color = Brass)
                }
                TextButton(onClick = { index = (at + 1).coerceAtMost(steps.lastIndex) }, enabled = at < steps.lastIndex) {
                    Text("Next", color = if (at < steps.lastIndex) Mute else Faint)
                }
            }

            AnimatedContent(targetState = at, label = "step-text") { shown ->
                StepText(steps[shown], guide)
            }
        }

        Spacer(Modifier.height(8.dp))
        ExerciseGuideSections(guide)
        Spacer(Modifier.height(28.dp))
    }
}

/** How long auto-play stays on a step: a held position a few seconds, a moving one two reps. */
private fun stepSeconds(step: Step): Double {
    val cycle = Player.cycleSeconds(step)
    return if (cycle <= 0.0) HELD_SECONDS else (cycle * 2).coerceIn(HELD_SECONDS, 20.0)
}

private const val HELD_SECONDS = 4.0

/** What to read while a step plays: the motion's own note first, then the guide. */
@Composable
private fun StepText(step: Step, guide: ExerciseGuide) {
    val (points, warnings) = when (step.kind) {
        StepKind.SETUP -> listOfNotNull(step.note ?: guide.setup.firstOrNull()) to emptyList()
        StepKind.GRIP -> listOfNotNull(step.note ?: guide.setup.lastOrNull()) to emptyList()
        StepKind.START -> guide.setup.drop(1) to emptyList()
        StepKind.REP -> guide.execution to emptyList()
        StepKind.TIPS -> guide.cues.map { "“$it”" } to guide.mistakes.take(2)
        StepKind.FULL -> listOf("The whole rep at normal speed. Watch from the side, then turn it to the front.") to emptyList()
    }
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Hairline()
        Spacer(Modifier.height(10.dp))
        Text(step.title, style = MaterialTheme.typography.titleMedium, color = Bone)
        Spacer(Modifier.height(6.dp))
        points.forEach { Text(it, style = MaterialTheme.typography.bodyMedium, color = Bone, modifier = Modifier.padding(bottom = 6.dp)) }
        if (warnings.isNotEmpty()) {
            Text("Avoid", style = MaterialTheme.typography.labelSmall, color = Faint, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
            warnings.forEach { Text(it, style = MaterialTheme.typography.bodyMedium, color = Coral, modifier = Modifier.padding(bottom = 6.dp)) }
        }
    }
}
