package dev.avles.gym.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import dev.avles.gym.core.Dates
import dev.avles.gym.data.DayKinds
import dev.avles.gym.data.Meals
import dev.avles.gym.domain.Burn
import dev.avles.gym.domain.Energy
import dev.avles.gym.domain.MacroMath
import dev.avles.gym.domain.Training
import dev.avles.gym.domain.Weekly
import dev.avles.gym.ui.CalendarDots
import dev.avles.gym.ui.DayState
import dev.avles.gym.ui.components.BigNumber
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.EntryRow
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.MacroBars
import dev.avles.gym.ui.components.Meter
import dev.avles.gym.ui.components.Motion
import dev.avles.gym.ui.components.Motion.fadeThrough
import dev.avles.gym.ui.components.PillRow
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.components.StripLegend
import dev.avles.gym.ui.components.WaterCard
import dev.avles.gym.ui.components.WeekCard
import dev.avles.gym.ui.components.WeekStrip
import dev.avles.gym.ui.components.pressScale
import dev.avles.gym.ui.i18n.tr
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Sage
import dev.avles.gym.ui.theme.Slate
import kotlin.math.roundToInt
import kotlinx.datetime.LocalDate


@Composable
fun TodayScreen(
    day: DayState,
    dots: CalendarDots,
    onSelectDate: (LocalDate) -> Unit,
    onGoTrain: () -> Unit,
    onGoFuel: () -> Unit,
    dayKind: String?,
    onMarkDay: (String) -> Unit,
    modifier: Modifier = Modifier,
    onAddWater: () -> Unit = {},
    onRemoveWater: () -> Unit = {},
    week: Weekly.Comparison? = null,
    latestWeightKg: Double? = null,
    onWelcomeTargets: (Energy.Targets) -> Unit = {},
    onDismissWelcome: () -> Unit = {},
    showWelcome: Boolean = false,
    onRecordWeight: (Double) -> Unit = {}
) {
    var calculating by remember { mutableStateOf(false) }
    var weighing by remember { mutableStateOf(false) }
    val today = Dates.today()
    val protein = day.totals.proteinG
    val kcal = day.totals.kcal
    val proteinTarget = day.settings.proteinTargetG
    val kcalTarget = day.settings.kcalTarget
    val trainedToday = day.sessions.isNotEmpty()
    val water = day.rows.filter { it.meal == Meals.WATER }
    val glasses = water.sumOf { it.quantity }.roundToInt()
    val foodRows = day.rows.filter { it.meal != Meals.WATER }

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
                // Walking the week changes the heading, so it crossfades with the day
                // rather than snapping; the strip below says which way you moved.
                AnimatedContent(targetState = day.date, transitionSpec = { fadeThrough() }, label = "day") { date ->
                    Column {
                        Text(
                            if (date == today) "Today" else Dates.dayName(date),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Bone
                        )
                        Text(
                            Dates.dayAndMonth(date),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Mute
                        )
                    }
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
            if (showWelcome) {
                WelcomeCard(onSetTargets = { calculating = true }, onDismiss = onDismissWelcome)
                Spacer(Modifier.height(22.dp))
            }
            // The weigh-in belongs first thing in the morning, which is when this
            // screen is open; going to Progress for it is a detour nobody takes.
            if (day.date == today) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        latestWeightKg?.let { "Last weigh-in ${Training.formatWeight(it)} kg" } ?: "No weigh-in yet",
                        style = MaterialTheme.typography.labelSmall,
                        color = Faint,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { weighing = true }) { Text(tr("Weigh in"), color = Sage) }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // --- Fuel -----------------------------------------------------
        item {
            SectionHeading(
                title = tr("Fuel"),
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
            Spacer(Modifier.height(16.dp))
            MacroBars(day.totals, kcalTarget, proteinTarget)
            Spacer(Modifier.height(18.dp))
            WaterCard(glasses, day.settings.waterGlasses, onAddWater, onRemoveWater)
            Spacer(Modifier.height(16.dp))
            val fuelPress = remember { MutableInteractionSource() }
            Button(
                onClick = onGoFuel,
                interactionSource = fuelPress,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Sage.copy(alpha = 0.14f),
                    contentColor = Sage
                ),
                modifier = Modifier.fillMaxWidth().pressScale(fuelPress)
            ) { Text(tr("Log food")) }
            Spacer(Modifier.height(26.dp))
            Hairline()
            Spacer(Modifier.height(22.dp))
        }

        // --- Training -------------------------------------------------
        item {
            SectionHeading(
                title = tr("Training"),
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
                val minutes = day.sessions.sumOf { s -> s.endedAt?.let { ((it - s.startedAt) / 60_000).toInt() } ?: 0 }
                val burned = Burn.kcal(minutes, latestWeightKg ?: 0.0)
                if (burned > 0) {
                    Text(
                        "$minutes min, about $burned kcal burned",
                        style = MaterialTheme.typography.labelSmall,
                        color = Mute
                    )
                }
            } else {
                Text(
                    "The bar hasn't moved yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Faint
                )
            }
            Spacer(Modifier.height(14.dp))
            val trainPress = remember { MutableInteractionSource() }
            Button(
                onClick = onGoTrain,
                interactionSource = trainPress,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Brass.copy(alpha = 0.14f),
                    contentColor = Brass
                ),
                modifier = Modifier.fillMaxWidth().pressScale(trainPress)
            ) { Text(if (trainedToday) "Open session" else "Start a session") }

            // Offered only on a day with no session. Marking a day you trained as a
            // rest day is a contradiction the app has no business helping you write
            // down, and the control would just be clutter on every other day.
            if (!trainedToday) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Not training today?",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
                Spacer(Modifier.height(6.dp))
                PillRow(
                    options = DayKinds.ALL,
                    selected = dayKind ?: "",
                    accent = Mute,
                    onSelect = onMarkDay
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    when (dayKind) {
                        DayKinds.REST ->
                            "Planned recovery. Your streak carries across it."
                        DayKinds.MISSED ->
                            "Noted. The streak restarts, but the gap has a reason on it now."
                        else ->
                            "A rest day keeps your streak; a missed day is just recorded. " +
                                "Tap the same one again to clear it."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint,
                    modifier = Modifier.animateContentSize(Motion.standard())
                )
            }
            Spacer(Modifier.height(26.dp))
            Hairline()
            Spacer(Modifier.height(22.dp))
            if (week != null && day.date == today) {
                WeekCard(week, proteinTarget)
                Spacer(Modifier.height(26.dp))
                Hairline()
                Spacer(Modifier.height(22.dp))
            }
            SectionHeading(
                // The strip lets you walk back through the week, so this heading has to
                // say which day it is listing rather than always claiming to be today.
                title = if (day.date == today) "Logged today" else "Logged on ${Dates.dayAndMonth(day.date)}",
                trailing = if (foodRows.isEmpty()) null else "${foodRows.size} entries"
            )
            Spacer(Modifier.height(10.dp))
        }

        if (foodRows.isEmpty()) {
            item {
                EmptyNote(
                    if (day.date == today) "Nothing yet. Log the first thing you ate today."
                    else "Nothing was logged on this day."
                )
            }
        } else {
            items(foodRows.reversed(), key = { "entry-${it.logId}" }) { row ->
                EntryRow(
                    title = row.name,
                    detail = "${Training.formatWeight(row.quantity)} x ${row.unitLabel}   ${row.meal}",
                    trailing = "${(row.proteinG * row.quantity).roundToInt()} p",
                    accent = Sage,
                    modifier = Modifier.animateItem().padding(bottom = 6.dp)
                )
            }
        }
    }

    if (weighing) {
        WeightDialog(
            suggestion = latestWeightKg,
            onSave = { onRecordWeight(it); weighing = false },
            onDismiss = { weighing = false }
        )
    }

    if (calculating) {
        TargetCalculatorDialog(
            initialWeightKg = latestWeightKg,
            onUse = { onWelcomeTargets(it); calculating = false },
            onDismiss = { calculating = false }
        )
    }
}

/** The first thing a new install shows: targets first, because everything is measured against them. */
@Composable
private fun WelcomeCard(onSetTargets: () -> Unit, onDismiss: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Sage.copy(alpha = 0.10f))
            .padding(16.dp)
    ) {
        Text("Welcome", style = MaterialTheme.typography.titleMedium, color = Bone)
        Spacer(Modifier.height(4.dp))
        Text(
            "Everything here is measured against your protein and calorie targets. " +
                "Thirty seconds of weight, height and age sets them; you can change them any time in Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = Mute
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onSetTargets,
                colors = ButtonDefaults.buttonColors(containerColor = Sage, contentColor = Slate)
            ) { Text("Set my targets") }
            TextButton(onClick = onDismiss) { Text("Use the defaults", color = Mute) }
        }
    }
}
