package dev.avles.gym.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import dev.avles.gym.core.Dates
import dev.avles.gym.data.Exercise
import dev.avles.gym.data.SetRow
import dev.avles.gym.domain.Alternatives
import dev.avles.gym.domain.ExerciseGuide
import dev.avles.gym.domain.LiftHistory
import dev.avles.gym.domain.Numbers
import dev.avles.gym.domain.Plates
import dev.avles.gym.domain.Training
import dev.avles.gym.domain.Warmups
import dev.avles.gym.ui.LiftGuidance
import dev.avles.gym.ui.components.BigNumber
import dev.avles.gym.ui.components.Chip
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.Meter
import dev.avles.gym.ui.components.Motion
import dev.avles.gym.ui.components.Motion.fadeThrough
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.components.TrendChart
import dev.avles.gym.ui.components.pressable
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Coral
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Slate
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/** Time since the session started, ticking. Every logging app keeps this in view. */
@Composable
fun SessionClock(startedAt: Long) {
    var now by remember { mutableLongStateOf(Dates.nowMillis()) }
    LaunchedEffect(startedAt) {
        while (true) {
            now = Dates.nowMillis()
            delay(1_000)
        }
    }
    val seconds = ((now - startedAt) / 1000).coerceAtLeast(0).toInt()
    Text(Numbers.clock(seconds), style = MaterialTheme.typography.bodyMedium, color = Brass)
}

/**
 * The previous session's sets on this lift, each one a chip that fills the weight
 * and reps fields when tapped: repeating last week's set is the commonest thing
 * anyone logs, and the "previous" column is how the big apps make it one tap.
 */
@Composable
fun LastTimeRow(sets: List<SetRow>, onUse: (SetRow) -> Unit, modifier: Modifier = Modifier) {
    if (sets.isEmpty()) return
    Column(modifier) {
        Text(
            "Last time, " + Dates.dayAndMonth(Dates.ofEpochDay(dayOf(sets.first().loggedAt))),
            style = MaterialTheme.typography.labelSmall,
            color = Mute
        )
        Spacer(Modifier.height(6.dp))
        // A scrolling Row rather than a LazyRow: two identical sets are two chips, and
        // a lazy list would need keys that two equal labels cannot give it.
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sets.forEach { set -> Chip(Training.formatSet(set), isOn = false, accent = Brass) { onUse(set) } }
        }
    }
}

private fun dayOf(millis: Long): Long = Dates.epochDayOf(millis)

/**
 * One lift, all of it: the best set and the records, how the estimated max has
 * moved session by session, and the rep-max table read off the best. The page
 * Strong and Hevy open when you tap an exercise.
 */
@Composable
fun LiftHistoryDialog(exercise: Exercise, history: List<SetRow>, onDismiss: () -> Unit) {
    val trend = remember(history) { LiftHistory.trend(history) }
    val records = remember(history) { LiftHistory.records(history) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text(exercise.name, color = Bone) },
        text = {
            Column(Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState())) {
                if (records.workingSets == 0) {
                    Text(
                        "Nothing logged on this lift yet. Its records and chart start with the first working set.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Faint
                    )
                    return@Column
                }
                if (exercise.isAssisted) {
                    val least = history.filter { !it.isWarmup }.minOfOrNull { it.weightKg } ?: 0.0
                    BigNumber(Training.formatWeight(least), "kg", "least help used", Brass)
                } else {
                    BigNumber(
                        Training.formatWeight(records.bestE1rm),
                        "kg",
                        "estimated one-rep max, from ${records.bestSet?.let { Training.formatSet(it) } ?: "-"}",
                        Brass
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "${records.sessions} sessions · ${records.workingSets} working sets",
                    style = MaterialTheme.typography.labelSmall,
                    color = Mute
                )

                if (!exercise.isAssisted) {
                    Spacer(Modifier.height(16.dp))
                    SectionHeading("Strength over time", trailing = "best set each session")
                    Spacer(Modifier.height(8.dp))
                    TrendChart(
                        values = trend.map { it.bestE1rm },
                        accent = Brass,
                        heightDp = 120,
                        emptyMessage = "Two sessions draw a line here."
                    )
                }

                Spacer(Modifier.height(16.dp))
                SectionHeading("Records")
                Spacer(Modifier.height(6.dp))
                records.heaviest?.let { RecordLine("Heaviest", Training.formatSet(it)) }
                records.mostReps?.let { RecordLine("Most reps", Training.formatSet(it)) }
                if (records.bestSessionVolume > 0) {
                    RecordLine("Best session", "${records.bestSessionVolume.roundToInt()} kg")
                }

                if (!exercise.isAssisted && records.bestE1rm > 0) {
                    Spacer(Modifier.height(16.dp))
                    SectionHeading("What you should manage", trailing = "from your best")
                    Spacer(Modifier.height(6.dp))
                    LiftHistory.repMaxTable(records.bestE1rm, 1..10).chunked(2).forEach { pair ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            pair.forEach { (reps, kg) ->
                                RecordLine("$reps reps", "${Training.formatWeight(kg)} kg", Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Past ten reps the estimate is a guess, so the table stops there.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Faint
                    )
                }

                Spacer(Modifier.height(16.dp))
                SectionHeading("Recent sessions")
                Spacer(Modifier.height(6.dp))
                trend.takeLast(8).reversed().forEach { point ->
                    RecordLine(
                        Dates.dayAndMonth(Dates.ofEpochDay(dayOf(point.loggedAt))),
                        Training.formatSet(point.topSet)
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = Brass) } }
    )
}

@Composable
private fun RecordLine(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Mute)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Bone)
    }
}

/**
 * The card after Finish: time, sets, volume, the lifts done and the records beaten.
 * Every logging app ends a workout this way, because finishing deserves a moment
 * and because it is when you are most likely to read your own numbers.
 */
@Composable
fun WorkoutReportDialog(report: LiftHistory.Report, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text("Workout done", color = Bone) },
        text = {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BigNumber(report.minutes.toString(), "min", "time", Brass)
                    BigNumber(report.workingSets.toString(), "sets", "working", Brass)
                    BigNumber(compactKg(report.volumeKg), "kg", "volume", Brass)
                }
                Spacer(Modifier.height(16.dp))
                Hairline()
                Spacer(Modifier.height(12.dp))
                if (report.records.isEmpty()) {
                    Text(
                        "No records tonight. Most sessions are like this, and they are the ones that make the next record.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mute
                    )
                } else {
                    Text(
                        if (report.records.size == 1) "A new record" else "${report.records.size} new records",
                        style = MaterialTheme.typography.titleMedium,
                        color = Brass
                    )
                    Spacer(Modifier.height(6.dp))
                    report.records.forEach { r ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brass.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(r.exerciseName, style = MaterialTheme.typography.bodyMedium, color = Bone)
                            Text(Training.formatSet(r), style = MaterialTheme.typography.bodyMedium, color = Brass)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (report.kcalBurned > 0) {
                    Text(
                        "About ${report.kcalBurned} kcal burned, from the time and your last weigh-in.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Mute
                    )
                    Spacer(Modifier.height(6.dp))
                }
                Text(
                    report.lifts.joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done", color = Brass) } }
    )
}

/** 12 400 kg reads as "12.4k"; the card has three numbers side by side. */
private fun compactKg(kg: Double): String =
    if (kg >= 10_000) Numbers.fixed(kg / 1000, 1) + "k" else kg.roundToInt().toString()

/** The −15 and +15 seconds under a running rest timer. */
@Composable
fun RestAdjustRow(onAdjust: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(-15, 15).forEach { delta ->
            Chip(if (delta < 0) "−15 s" else "+15 s", isOn = false, accent = Brass) { onAdjust(delta) }
        }
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
internal fun TargetCard(guidance: LiftGuidance, onUseTarget: (Double, Int) -> Unit) {
    val rec = guidance.recommendation

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brass.copy(alpha = 0.10f))
            .animateContentSize(Motion.standard())
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
            AnimatedContent(targetState = rec.headline, transitionSpec = { fadeThrough() }, label = "target") {
                Text(it, style = MaterialTheme.typography.displaySmall, color = Brass)
            }
            Spacer(Modifier.weight(1f))
            val haptics = LocalHapticFeedback.current
            TextButton(onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onUseTarget(rec.weightKg, rec.reps)
            }) {
                Text("Use", color = Brass, style = MaterialTheme.typography.labelLarge)
            }
        }

        Text(
            rec.rationale,
            style = MaterialTheme.typography.labelSmall,
            color = Mute
        )

        // The ramp up to the target, for a loaded lift heavy enough to need one.
        val bar = guidance.guide?.equipment?.let(Plates::barFor)
        val warmups = if (guidance.exercise.isAssisted || bar == null) emptyList() else Warmups.plan(rec.weightKg, bar)
        if (warmups.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Warm up  " + warmups.joinToString("  ·  ") {
                    "${Training.formatWeight(it.weightKg)} × ${it.reps}"
                },
                style = MaterialTheme.typography.labelSmall,
                color = Faint
            )
        }

        val leastHelp = guidance.leastHelp
        if (guidance.bestE1rm > 0 || leastHelp != null) {
            Spacer(Modifier.height(14.dp))
            Hairline()
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (leastHelp != null) "least help ${Training.formatWeight(leastHelp)} kg"
                    else "estimated max ${Training.formatWeight(guidance.bestE1rm)} kg",
                    style = MaterialTheme.typography.labelSmall,
                    color = Mute
                )
                Text(
                    when {
                        leastHelp == null -> "next ${Training.formatWeight(guidance.nextMilestone)} kg"
                        leastHelp <= 0.0 -> "next: the unassisted lift"
                        else -> "next ${Training.formatWeight(guidance.nextMilestone)} kg help"
                    },
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
internal fun RestBanner(secondsLeft: Int, onSkip: () -> Unit, onAdjust: (Int) -> Unit) {
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
                Numbers.clock(secondsLeft),
                style = MaterialTheme.typography.displaySmall,
                color = Brass
            )
            Text("resting", style = MaterialTheme.typography.labelSmall, color = Mute)
        }
        RestAdjustRow(onAdjust)
        TextButton(onClick = onSkip) { Text("Skip", color = Brass) }
    }
}

/** Your note on a lift: seat height, pin hole, the grip a shoulder dislikes. */
@Composable
fun LiftNoteDialog(exercise: Exercise, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember(exercise.id) { mutableStateOf(exercise.notes) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text(exercise.name, color = Bone) },
        text = {
            Column {
                Text(
                    "Shown under the lift every time you pick it.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.take(300) },
                    label = { Text("e.g. seat on 4, pins at hole 3") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(text) }) { Text("Save", color = Brass) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Mute) } }
    )
}

/** Lifts for the same muscle on other equipment, for when the machine is taken. */
@Composable
fun SwapLiftDialog(target: Exercise, catalogue: List<Exercise>, onPick: (Exercise) -> Unit, onDismiss: () -> Unit) {
    val swaps = remember(target.id, catalogue) { Alternatives.forLift(target, catalogue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text("Instead of ${target.name}", color = Bone) },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())) {
                Text(
                    "Same muscle, the same movement first, on other equipment first.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
                Spacer(Modifier.height(8.dp))
                swaps.forEach { lift ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .pressable(RoundedCornerShape(8.dp), pressedScale = 0.985f) { onPick(lift) }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(lift.name, style = MaterialTheme.typography.bodyLarge, color = Bone)
                        Text(
                            ExerciseGuide.of(lift).equipment.label + "  ·  ${lift.repLow}-${lift.repHigh} reps",
                            style = MaterialTheme.typography.labelSmall,
                            color = Mute
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = Brass) } }
    )
}
