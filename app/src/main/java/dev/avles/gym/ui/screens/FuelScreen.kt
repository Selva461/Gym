package dev.avles.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import dev.avles.gym.data.FoodItem
import dev.avles.gym.data.Meals
import dev.avles.gym.domain.MacroMath
import dev.avles.gym.domain.Training
import dev.avles.gym.ui.DayState
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.EntryRow
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.Meter
import dev.avles.gym.ui.components.PillRow
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Raised
import dev.avles.gym.ui.theme.Sage
import dev.avles.gym.ui.theme.Slate
import kotlin.math.roundToInt

@Composable
fun FuelScreen(
    day: DayState,
    quickPicks: List<FoodItem>,
    catalogue: List<FoodItem>,
    onLog: (Long, String, Double) -> Unit,
    onRemove: (Long) -> Unit,
    onSaveFood: (FoodItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var meal by remember { mutableStateOf(defaultMealForNow()) }
    var query by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf<FoodItem?>(null) }
    var editing by remember { mutableStateOf<FoodItem?>(null) }
    var creating by remember { mutableStateOf(false) }

    val matches = remember(query, catalogue) {
        if (query.isBlank()) emptyList()
        else catalogue.filter { it.name.contains(query, ignoreCase = true) }.take(12)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)
    ) {
        item {
            Text("Fuel", style = MaterialTheme.typography.headlineMedium, color = Bone)
            Row(Modifier.fillMaxWidth().padding(top = 2.dp)) {
                Text(
                    "${day.totals.proteinG.roundToInt()} g protein",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Sage
                )
                Text(
                    "   ${day.totals.kcal.roundToInt()} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mute
                )
            }
            Spacer(Modifier.height(12.dp))
            Meter(
                progress = MacroMath.ratio(day.totals.proteinG, day.settings.proteinTargetG),
                accent = Sage
            )
            Spacer(Modifier.height(20.dp))
            PillRow(Meals.ALL, meal, Sage, { meal = it })
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search food") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
        }

        if (matches.isNotEmpty()) {
            items(matches, key = { "m" + it.id }) { food ->
                FoodPickRow(
                    food = food,
                    onTap = { pending = food },
                    onEdit = { editing = food }
                )
            }
            item { Spacer(Modifier.height(10.dp)) }
        } else if (query.isBlank()) {
            item {
                SectionHeading(
                    "Usual suspects",
                    trailing = if (quickPicks.isEmpty()) "builds as you log" else null
                )
                Spacer(Modifier.height(10.dp))
                if (quickPicks.isEmpty()) {
                    EmptyNote("Search for something to log. What you log most will surface here.")
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quickPicks, key = { "q" + it.id }) { food ->
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Raised)
                                    .clickable { pending = food }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Text(
                                        food.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Bone
                                    )
                                    Text(
                                        "${food.proteinG.roundToInt()} g protein",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Sage
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        } else {
            item { EmptyNote("Nothing matches \"$query\". Add it as a new food below.") }
        }

        item {
            TextButton(onClick = { creating = true }) {
                Text("Add a food to the catalogue", color = Sage)
            }
            Spacer(Modifier.height(10.dp))
            Hairline()
            Spacer(Modifier.height(18.dp))
        }

        Meals.ALL.forEach { slot ->
            val rows = day.rows.filter { it.meal == slot }
            if (rows.isNotEmpty()) {
                val totals = MacroMath.totalsOf(rows)
                item {
                    SectionHeading(
                        slot,
                        trailing = "${totals.proteinG.roundToInt()} g   ${totals.kcal.roundToInt()} kcal",
                        accent = Sage
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(rows, key = { it.logId }) { row ->
                    EntryRow(
                        title = row.name,
                        detail = "${Training.formatWeight(row.quantity)} x ${row.unitLabel}",
                        trailing = "${(row.proteinG * row.quantity).roundToInt()} p",
                        accent = Sage,
                        onClick = { onRemove(row.logId) },
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }

        if (day.rows.isEmpty()) {
            item { EmptyNote("Nothing logged for this day yet.") }
        } else {
            item {
                Text(
                    "Tap an entry to remove it.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
            }
        }
    }

    pending?.let { food ->
        QuantityDialog(
            food = food,
            meal = meal,
            onConfirm = { qty -> onLog(food.id, meal, qty); pending = null },
            onDismiss = { pending = null }
        )
    }

    editing?.let { food ->
        FoodEditor(
            initial = food,
            onSave = { onSaveFood(it); editing = null },
            onDismiss = { editing = null }
        )
    }

    if (creating) {
        FoodEditor(
            initial = FoodItem(
                name = "", unitLabel = "1 serving", kcal = 0.0, proteinG = 0.0,
                carbsG = 0.0, fatG = 0.0, category = "Custom", isCustom = true
            ),
            onSave = { onSaveFood(it); creating = false },
            onDismiss = { creating = false }
        )
    }
}

@Composable
private fun FoodPickRow(food: FoodItem, onTap: () -> Unit, onEdit: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Slate)
            .clickable { onTap() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(food.name, style = MaterialTheme.typography.bodyLarge, color = Bone)
            Text(
                "${food.unitLabel}   ${food.kcal.roundToInt()} kcal",
                style = MaterialTheme.typography.labelSmall,
                color = Mute
            )
        }
        Text(
            "${food.proteinG.roundToInt()} p",
            style = MaterialTheme.typography.titleMedium,
            color = Sage
        )
        TextButton(onClick = onEdit) { Text("Edit", color = Mute) }
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun QuantityDialog(
    food: FoodItem,
    meal: String,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var qty by remember { mutableStateOf("1") }
    val parsed = qty.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text(food.name, color = Bone) },
        text = {
            Column {
                Text(
                    "How many? One is ${food.unitLabel}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mute
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it.filter { c -> c.isDigit() || c == '.' } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("0.5", "1", "1.5", "2").forEach { preset ->
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Raised)
                                .clickable { qty = preset }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(preset, style = MaterialTheme.typography.labelMedium, color = Bone)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "${(food.proteinG * parsed).roundToInt()} g protein, " +
                        "${(food.kcal * parsed).roundToInt()} kcal into $meal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Sage
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(parsed) },
                enabled = parsed > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Sage, contentColor = Slate)
            ) { Text("Add to $meal") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Mute) } }
    )
}

@Composable
private fun FoodEditor(
    initial: FoodItem,
    onSave: (FoodItem) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var unit by remember { mutableStateOf(initial.unitLabel) }
    var kcal by remember { mutableStateOf(trimZero(initial.kcal)) }
    var protein by remember { mutableStateOf(trimZero(initial.proteinG)) }
    var carbs by remember { mutableStateOf(trimZero(initial.carbsG)) }
    var fat by remember { mutableStateOf(trimZero(initial.fatG)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text(if (initial.id == 0L) "New food" else "Edit food", color = Bone) },
        text = {
            Column {
                Text(
                    "Values are per one unit. Measure yours once and these stop being guesses.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = unit, onValueChange = { unit = it },
                    label = { Text("One unit is") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(kcal, { kcal = it }, "kcal", Modifier.weight(1f))
                    NumberField(protein, { protein = it }, "protein", Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(carbs, { carbs = it }, "carbs", Modifier.weight(1f))
                    NumberField(fat, { fat = it }, "fat", Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        initial.copy(
                            name = name.trim(),
                            unitLabel = unit.ifBlank { "1 serving" },
                            kcal = kcal.toDoubleOrNull() ?: 0.0,
                            proteinG = protein.toDoubleOrNull() ?: 0.0,
                            carbsG = carbs.toDoubleOrNull() ?: 0.0,
                            fatG = fat.toDoubleOrNull() ?: 0.0
                        )
                    )
                }
            ) { Text("Save food", color = if (name.isNotBlank()) Sage else Faint) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Mute) } }
    )
}

@Composable
private fun NumberField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() || c == '.' }) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}

private fun trimZero(v: Double): String =
    if (v == 0.0) "" else if (v % 1.0 == 0.0) v.roundToInt().toString() else v.toString()

private fun defaultMealForNow(): String = when (java.time.LocalTime.now().hour) {
    in 4..10 -> Meals.BREAKFAST
    in 11..15 -> Meals.LUNCH
    in 16..19 -> Meals.SNACK
    else -> Meals.DINNER
}
