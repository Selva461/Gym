package dev.avles.gym.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.avles.gym.domain.MacroMath
import dev.avles.gym.domain.Training
import dev.avles.gym.ui.CalendarDots
import dev.avles.gym.ui.DayState
import dev.avles.gym.ui.components.BigNumber
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.EntryRow
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.Meter
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.components.StripLegend
import dev.avles.gym.ui.components.WeekStrip
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Sage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val dayName = DateTimeFormatter.ofPattern("EEEE")
private val dayDate = DateTimeFormatter.ofPattern("d MMMM")

@Composable
fun TodayScreen(
    day: DayState,
    dots: CalendarDots,
    onSelectDate: (LocalDate) -> Unit,
    onGoTrain: () -> Unit,
    onGoFuel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val protein = day.totals.proteinG
    val kcal = day.totals.kcal
    val proteinTarget = day.settings.proteinTargetG
    val kcalTarget = day.settings.kcalTarget
    val trainedToday = day.sessions.isNotEmpty()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        if (day.date == today) "Today" else day.date.format(dayName),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Bone
                    )
                    Text(
                        day.date.format(dayDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mute
                    )
                }
                StripLegend()
            }
            Spacer(Modifier.height(14.dp))
            WeekStrip(
                selected = day.date,
                today = today,
                trainedDays = dots.trained,
                fuelledDays = dots.fuelled,
                onSelect = onSelectDate
            )
            Spacer(Modifier.height(22.dp))
            Hairline()
            Spacer(Modifier.height(22.dp))
        }

        // --- Fuel -----------------------------------------------------
        item {
            SectionHeading(
                title = "Fuel",
                trailing = "${kcal.roundToInt()} of ${kcalTarget.roundToInt()} kcal",
                accent = Mute
            )
            Spacer(Modifier.height(14.dp))
            BigNumber(
                value = protein.roundToInt().toString(),
                unit = "g protein",
                caption = "target ${proteinTarget.roundToInt()} g",
                accent = Sage
            )
            Spacer(Modifier.height(12.dp))
            Meter(
                progress = MacroMath.ratio(protein, proteinTarget),
                accent = Sage
            )
            Spacer(Modifier.height(8.dp))
            Meter(
                progress = MacroMath.ratio(kcal, kcalTarget),
                accent = Mute,
                overflow = MacroMath.rawRatio(kcal, kcalTarget) > 1.05,
                height = 3
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onGoFuel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Sage.copy(alpha = 0.14f),
                    contentColor = Sage
                ),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Log food") }
            Spacer(Modifier.height(26.dp))
            Hairline()
            Spacer(Modifier.height(22.dp))
        }

        // --- Training -------------------------------------------------
        item {
            SectionHeading(
                title = "Training",
                trailing = if (trainedToday) day.sessions.first().label else "nothing logged",
                accent = if (trainedToday) Brass else Faint
            )
            Spacer(Modifier.height(14.dp))
            if (trainedToday) {
                val open = day.sessions.any { it.endedAt == null }
                BigNumber(
                    value = day.sessions.size.toString(),
                    unit = if (day.sessions.size == 1) "session" else "sessions",
                    caption = if (open) "one still open" else "done for the day",
                    accent = Brass
                )
            } else {
                Text(
                    "The bar hasn't moved yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Faint
                )
            }
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onGoTrain,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Brass.copy(alpha = 0.14f),
                    contentColor = Brass
                ),
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (trainedToday) "Open session" else "Start a session") }
            Spacer(Modifier.height(26.dp))
            Hairline()
            Spacer(Modifier.height(22.dp))
            SectionHeading(
                // The strip lets you walk back through the week, so this heading has to
                // say which day it is listing rather than always claiming to be today.
                title = if (day.date == today) "Logged today" else "Logged on ${day.date.format(dayDate)}",
                trailing = if (day.rows.isEmpty()) null else "${day.rows.size} entries"
            )
            Spacer(Modifier.height(10.dp))
        }

        if (day.rows.isEmpty()) {
            item {
                EmptyNote(
                    if (day.date == today) "Nothing yet. Log the first thing you ate today."
                    else "Nothing was logged on this day."
                )
            }
        } else {
            items(day.rows.reversed(), key = { "entry-${it.logId}" }) { row ->
                EntryRow(
                    title = row.name,
                    detail = "${Training.formatWeight(row.quantity)} x ${row.unitLabel}   ${row.meal}",
                    trailing = "${(row.proteinG * row.quantity).roundToInt()} p",
                    accent = Sage,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}
