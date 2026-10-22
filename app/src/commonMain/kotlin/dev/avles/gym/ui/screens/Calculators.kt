package dev.avles.gym.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import dev.avles.gym.domain.Energy
import dev.avles.gym.domain.LiftHistory
import dev.avles.gym.domain.Plates
import dev.avles.gym.domain.Training
import dev.avles.gym.ui.components.ChipRow
import dev.avles.gym.ui.components.Motion.fadeThrough
import dev.avles.gym.ui.components.PillRow
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Coral
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Sage
import dev.avles.gym.ui.theme.Slate
import kotlin.math.roundToInt

/**
 * The onboarding calculator every nutrition app opens with, offered here instead of
 * forced: weight, height, age, sex, how active, what for. It fills the target fields
 * and leaves saving to you, because a formula's answer is a starting point.
 */
@Composable
fun TargetCalculatorDialog(
    initialWeightKg: Double?,
    onUse: (Energy.Targets) -> Unit,
    onDismiss: () -> Unit
) {
    var weight by remember { mutableStateOf(initialWeightKg?.let { Training.formatWeight(it) } ?: "") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf(Energy.Sex.MALE) }
    var activity by remember { mutableStateOf(Energy.Activity.MODERATE) }
    var goal by remember { mutableStateOf(Energy.Goal.MAINTAIN) }

    val targets = Energy.targets(
        weightKg = weight.toDoubleOrNull() ?: 0.0,
        heightCm = height.toDoubleOrNull() ?: 0.0,
        age = age.toIntOrNull() ?: 0,
        sex = sex,
        activity = activity,
        goal = goal
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text("Work out my targets", color = Bone) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallNumber(weight, { weight = it }, "kg", Modifier.weight(1f), decimal = true)
                    SmallNumber(height, { height = it }, "cm", Modifier.weight(1f))
                    SmallNumber(age, { age = it }, "age", Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                PillRow(
                    options = listOf("Male", "Female"),
                    selected = if (sex == Energy.Sex.MALE) "Male" else "Female",
                    accent = Sage,
                    onSelect = { sex = if (it == "Male") Energy.Sex.MALE else Energy.Sex.FEMALE }
                )
                Spacer(Modifier.height(14.dp))
                Text("How active", style = MaterialTheme.typography.labelSmall, color = Mute)
                Spacer(Modifier.height(6.dp))
                ChipRow(
                    options = Energy.Activity.entries.map { it.label },
                    selected = activity.label,
                    accent = Sage,
                    onSelect = { label -> activity = Energy.Activity.entries.first { it.label == label } }
                )
                Spacer(Modifier.height(14.dp))
                Text("What for", style = MaterialTheme.typography.labelSmall, color = Mute)
                Spacer(Modifier.height(6.dp))
                PillRow(
                    options = Energy.Goal.entries.map { it.label },
                    selected = goal.label,
                    accent = Sage,
                    onSelect = { label -> goal = Energy.Goal.entries.first { it.label == label } }
                )
                Spacer(Modifier.height(18.dp))
                AnimatedContent(targetState = targets, transitionSpec = { fadeThrough() }, label = "targets") { t ->
                    if (t == null) {
                        Text(
                            "Fill in weight, height and age.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Faint
                        )
                    } else {
                        Column {
                            Text(
                                "${t.kcal.roundToInt()} kcal · ${t.proteinG.roundToInt()} g protein",
                                style = MaterialTheme.typography.titleMedium,
                                color = Sage
                            )
                            Text(
                                "${t.carbsG.roundToInt()} g carbohydrate · ${t.fatG.roundToInt()} g fat · " +
                                    "${t.waterGlasses} glasses of water",
                                style = MaterialTheme.typography.labelSmall,
                                color = Mute
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Maintenance is about ${t.maintenanceKcal.roundToInt()} kcal. Weigh in for " +
                                    "three weeks: if the trend is not moving the way you want, move the " +
                                    "calories 150 at a time. Your scale outranks this formula.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Faint
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = targets != null, onClick = { targets?.let(onUse) }) {
                Text("Use these", color = if (targets != null) Sage else Faint)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Mute) } }
    )
}

/**
 * What goes on each side of the bar for the weight typed in the set field. Bar
 * choice is part of it because a 10 kg EZ bar and a 20 kg Olympic bar make the same
 * total from different plates.
 */
@Composable
fun PlateCalculatorDialog(initialKg: Double?, onDismiss: () -> Unit, initialBarKg: Double = Plates.OLYMPIC_BAR_KG) {
    var total by remember { mutableStateOf(initialKg?.let { Training.formatWeight(it) } ?: "") }
    var bar by remember { mutableStateOf(initialBarKg) }
    val kg = total.toDoubleOrNull()
    val loading = kg?.let { Plates.load(it, bar) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text("Plates", color = Bone) },
        text = {
            Column {
                SmallNumber(total, { total = it }, "total weight, kg", Modifier.fillMaxWidth(), decimal = true)
                Spacer(Modifier.height(12.dp))
                Text("Bar", style = MaterialTheme.typography.labelSmall, color = Mute)
                Spacer(Modifier.height(6.dp))
                PillRow(
                    options = Plates.BARS_KG.map { Plates.label(it) + " kg" },
                    selected = Plates.label(bar) + " kg",
                    accent = Brass,
                    onSelect = { label -> bar = Plates.BARS_KG.first { Plates.label(it) + " kg" == label } }
                )
                Spacer(Modifier.height(18.dp))
                when {
                    loading == null -> Text("Type a weight.", color = Faint, style = MaterialTheme.typography.bodyMedium)
                    loading.belowBar -> Text(
                        "That is less than the bar.",
                        color = Coral,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    else -> {
                        Text("Each side", style = MaterialTheme.typography.labelSmall, color = Mute)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            Plates.describe(loading.perSide),
                            style = MaterialTheme.typography.titleLarge,
                            color = Brass
                        )
                        Spacer(Modifier.height(12.dp))
                        PlateStack(loading.perSide)
                        if (!loading.isExact) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Closest is ${Plates.label(loading.loadedKg)} kg, " +
                                    "${Plates.label(loading.shortKg)} kg short. The plates go in 1.25s.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Coral
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done", color = Brass) } }
    )
}

/** The plates drawn to scale on a sleeve, so the loading can be checked at a glance. */
@Composable
private fun PlateStack(perSide: List<Double>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(36.dp).height(6.dp).background(Mute))
        perSide.forEach { plate ->
            // Height by plate: a 25 is full height, a 1.25 a third of it.
            val h = (18 + plate * 1.8).roundToInt()
            Spacer(
                Modifier
                    .padding(horizontal = 1.dp)
                    .size(width = if (plate >= 10) 10.dp else 7.dp, height = h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (plate >= 20) Brass else if (plate >= 5) Bone else Mute)
            )
        }
        Spacer(Modifier.width(14.dp).height(6.dp).background(Mute))
    }
}

@Composable
private fun SmallNumber(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
    decimal: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() || (decimal && c == '.') }) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
        modifier = modifier
    )
}

/**
 * Weight and reps in, estimated one-rep max out, with what that predicts for other
 * rep counts and the percentages used to plan warm-ups and back-off sets.
 */
@Composable
fun OneRepMaxDialog(onDismiss: () -> Unit) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    val e1rm = Training.estimatedOneRepMax(weight.toDoubleOrNull() ?: 0.0, reps.toIntOrNull() ?: 0)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text("One-rep max", color = Bone) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallNumber(weight, { weight = it }, "kg", Modifier.weight(1f), decimal = true)
                    SmallNumber(reps, { reps = it }, "reps", Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                if (e1rm <= 0) {
                    Text("A weight and reps you have done.", style = MaterialTheme.typography.bodyMedium, color = Faint)
                } else {
                    Text(
                        "${Training.formatWeight(e1rm)} kg",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Brass
                    )
                    Text("estimated max (Epley)", style = MaterialTheme.typography.labelSmall, color = Mute)
                    Spacer(Modifier.height(12.dp))
                    LiftHistory.repMaxTable(e1rm, 1..10).forEach { (r, kg) ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("$r reps", style = MaterialTheme.typography.bodyMedium, color = Mute)
                            Text("${Training.formatWeight(kg)} kg", style = MaterialTheme.typography.bodyMedium, color = Bone)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        LiftHistory.percentTable(e1rm).joinToString("   ") { (pct, kg) -> "$pct% ${Training.formatWeight(kg)}" },
                        style = MaterialTheme.typography.labelSmall,
                        color = Faint
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done", color = Brass) } }
    )
}
