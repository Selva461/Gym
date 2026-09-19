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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.avles.gym.domain.Patterns
import dev.avles.gym.ui.PatternState
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.components.TrendChart
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Line
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Sage
import kotlin.math.roundToInt

/**
 * The shape of your last thirty days.
 *
 * Totals say how much and streaks say how often. This says *when*, which is where
 * the fixable things live — "I train four times a week" and "I never train on a
 * Friday" are both true at once, and only the second tells you what to change.
 *
 * Unlogged days are gaps, never zeroes. A line that draws a day you forgot to log
 * as a day you ate nothing is a picture of your logging habit rather than your
 * eating, and it would make every one of these charts a little bit of a lie.
 */
@Composable
fun PatternsScreen(state: PatternState, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.padding(horizontal = 20.dp)) {
        item {
            Spacer(Modifier.height(16.dp))
            Text("Patterns", style = MaterialTheme.typography.headlineMedium, color = Bone)
            Text(
                "The last ${state.windowDays} days. Days you did not log are gaps, not zeroes.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mute
            )
            Spacer(Modifier.height(24.dp))

            SectionHeading("Protein", trailing = "target ${state.proteinTarget.roundToInt()} g")
            Spacer(Modifier.height(8.dp))
            // The seven-day mean rather than the raw line: the day-to-day noise is
            // the difference between a Tuesday and a Sunday, which is exactly what
            // this is trying to see past.
            TrendChart(
                values = state.proteinMean.filterNotNull(),
                accent = Sage,
                emptyMessage = "Log a few days of food and the line appears."
            )
            Spacer(Modifier.height(6.dp))
            AdherenceLine("protein", state.proteinAdherence, Sage)

            Spacer(Modifier.height(24.dp))
            SectionHeading("Calories", trailing = "target ${state.kcalTarget.roundToInt()}")
            Spacer(Modifier.height(8.dp))
            TrendChart(
                values = state.kcalMean.filterNotNull(),
                accent = Brass,
                emptyMessage = "Log a few days of food and the line appears."
            )
            Spacer(Modifier.height(6.dp))
            AdherenceLine("calories", state.kcalAdherence, Brass)

            Spacer(Modifier.height(28.dp))
            Hairline()
            Spacer(Modifier.height(20.dp))
            SectionHeading("By day of the week", trailing = "how often you train")
            Spacer(Modifier.height(12.dp))
        }

        if (state.weekdays.all { it.samples == 0 }) {
            item { EmptyNote("A month of logging fills this in.") }
        } else {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.weekdays.forEach { day ->
                        WeekdayColumn(day, Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(14.dp))

                // Stated only when it is actually a pattern. Two Fridays is not one,
                // and naming noise as a finding invites reorganising a week around it.
                state.weakestWeekday?.let { weak ->
                    Text(
                        "${weak.label} is the day you skip. You have trained " +
                            "${weak.trainedDays} of ${weak.samples} of them.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Bone
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Not necessarily a problem — a rest day has to land somewhere. " +
                            "Worth knowing if it was not deliberate.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Faint
                    )
                } ?: Text(
                    "No weekday stands out yet.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

/**
 * One weekday: a bar for how often it was trained, and the mean protein on it.
 *
 * Greyed below three samples rather than hidden. The reader can see the day exists
 * and that there is not yet enough of it to say anything, which is more honest
 * than an empty slot and less misleading than a confident bar.
 */
@Composable
private fun WeekdayColumn(day: Patterns.DayOfWeek, modifier: Modifier = Modifier) {
    val accent = if (day.enoughToRead) Brass else Line
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Line),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height((72 * day.trainedRate).dp.coerceAtLeast(2.dp))
                    .clip(RoundedCornerShape(4.dp))
                    .background(accent)
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(
            day.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (day.enoughToRead) Mute else Faint,
            textAlign = TextAlign.Center
        )
        Text(
            if (day.loggedFoodDays == 0) "—" else "${day.meanProteinG.roundToInt()}g",
            style = MaterialTheme.typography.labelSmall,
            color = Faint,
            textAlign = TextAlign.Center
        )
    }
}

/** Of the days you logged — not of the days that passed. The label says so. */
@Composable
private fun AdherenceLine(what: String, rate: Float, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(44.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Line)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction = rate.coerceIn(0f, 1f))
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent)
            )
        }
        Text(
            "  ${(rate * 100).roundToInt()}% of logged days within 15% of $what target",
            style = MaterialTheme.typography.labelSmall,
            color = Faint
        )
    }
}
