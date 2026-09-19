package dev.avles.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.avles.gym.domain.Coach
import dev.avles.gym.domain.Physiques
import dev.avles.gym.ui.CoachState
import dev.avles.gym.ui.components.ChipRow
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Coral
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Line
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Sage
import dev.avles.gym.ui.theme.Slate
import kotlin.math.roundToInt

/**
 * Whether the last seven days served the goal, and what is missing if not.
 *
 * The order is the order the question gets asked: what am I training for, how did
 * this week go, which muscles are short, what should I add. The caveats sit at the
 * bottom because they are read once — but they are on the screen rather than in a
 * help page, because for this goal the most important sentence is that abs are a
 * body-fat story and no amount of lateral raises changes that.
 *
 * Every number here is the log read back. Nothing is predicted.
 */
@Composable
fun CoachScreen(
    state: CoachState,
    onChooseGoal: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.padding(horizontal = 20.dp)) {
        item {
            Spacer(Modifier.height(16.dp))
            Text("Goal", style = MaterialTheme.typography.headlineMedium, color = Bone)
            Text(
                "Your last seven days, measured against what you are training for.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mute
            )
            Spacer(Modifier.height(18.dp))
            ChipRow(
                options = Physiques.ALL.map { it.label },
                selected = state.physique.label,
                accent = Brass,
                onSelect = { label ->
                    Physiques.ALL.firstOrNull { it.label == label }?.let { onChooseGoal(it.id) }
                }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                state.physique.summary,
                style = MaterialTheme.typography.labelSmall,
                color = Faint
            )

            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate)
                    .padding(16.dp)
            ) {
                Column {
                    Text(state.verdict, style = MaterialTheme.typography.bodyMedium, color = Bone)
                    if (state.restDays > 0 || state.missedDays > 0) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            buildString {
                                if (state.restDays > 0) append("${state.restDays} rest day${plural(state.restDays)}")
                                if (state.restDays > 0 && state.missedDays > 0) append(" · ")
                                if (state.missedDays > 0) append("${state.missedDays} missed")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Faint
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Hairline()
            Spacer(Modifier.height(18.dp))
            SectionHeading("This week by muscle", trailing = "sets of target")
            Spacer(Modifier.height(10.dp))
        }

        if (state.review.isEmpty()) {
            item { EmptyNote("Pick a goal and log a session; this fills itself in.") }
        } else {
            items(state.review, key = { "muscle-${it.muscle.name}" }) { MuscleBar(it) }
        }

        if (state.suggestions.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                Hairline()
                Spacer(Modifier.height(18.dp))
                SectionHeading("Worth adding", trailing = "for the gaps above")
                Spacer(Modifier.height(4.dp))
                Text(
                    "Lifts whose main job is the muscle that is short. Not ones that " +
                        "happen to involve it.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
                Spacer(Modifier.height(10.dp))
            }
            items(state.suggestions, key = { "sugg-${it.exercise.id}-${it.forMuscle.name}" }) {
                Column(Modifier.padding(bottom = 10.dp)) {
                    Text(it.exercise.name, style = MaterialTheme.typography.bodyMedium, color = Bone)
                    Text(it.reason, style = MaterialTheme.typography.labelSmall, color = Faint)
                }
            }
        }

        if (state.physique.notes.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                Hairline()
                Spacer(Modifier.height(18.dp))
                SectionHeading("Worth knowing")
                Spacer(Modifier.height(10.dp))
                state.physique.notes.forEach { note ->
                    Row(Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            Modifier
                                .padding(top = 7.dp, end = 10.dp)
                                .height(4.dp)
                                .width(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Mute)
                        )
                        Text(note, style = MaterialTheme.typography.bodyMedium, color = Mute)
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

/**
 * One muscle: sets done against sets asked for.
 *
 * The bar is capped at the target rather than scaled to the largest value, so a
 * muscle at triple volume does not squash everything else into a sliver — the
 * question is "am I there yet", not "which is biggest".
 */
@Composable
private fun MuscleBar(week: Coach.MuscleWeek) {
    val accent = if (week.onTarget) Sage else if (week.ratio < 0.4f) Coral else Brass
    Column(Modifier.padding(bottom = 12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(week.muscle.label, style = MaterialTheme.typography.bodyMedium, color = Bone)
            Text(
                "${trim(week.sets)} / ${week.target}",
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
        }
        Spacer(Modifier.height(5.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Line)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction = week.ratio.coerceIn(0f, 1f))
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(accent)
            )
        }
    }
}

private fun trim(sets: Double): String =
    if (sets % 1.0 == 0.0) sets.roundToInt().toString() else String.format("%.1f", sets)

private fun plural(n: Int) = if (n == 1) "" else "s"
