package dev.avles.gym.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.avles.gym.core.Dates
import dev.avles.gym.data.FoodItem
import dev.avles.gym.data.FoodLogRow
import dev.avles.gym.data.Meals
import dev.avles.gym.data.Micros
import dev.avles.gym.domain.FoodEdits
import dev.avles.gym.domain.FoodSearch
import dev.avles.gym.domain.MacroMath
import dev.avles.gym.domain.Numbers
import dev.avles.gym.domain.Training
import dev.avles.gym.ui.DayState
import dev.avles.gym.ui.components.Chip
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.EntryRow
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.MacroBars
import dev.avles.gym.ui.components.Motion
import dev.avles.gym.ui.components.NutrientPanel
import dev.avles.gym.ui.components.PillRow
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.components.WaterCard
import dev.avles.gym.ui.components.pressable
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Coral
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
    onEditEntry: (Long, Double, String) -> Unit,
    onSaveFood: (FoodItem) -> Unit,
    modifier: Modifier = Modifier,
    onAddWater: () -> Unit = {},
    onRemoveWater: () -> Unit = {},
    onCopyMeal: (String, (Int) -> Unit) -> Unit = { _, _ -> }
) {
    var meal by remember { mutableStateOf(defaultMealForNow()) }
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf<FoodItem?>(null) }
    var editing by remember { mutableStateOf<FoodItem?>(null) }
    var creating by remember { mutableStateOf(false) }
    var nutrientsOpen by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<FoodLogRow?>(null) }
    var copyNote by remember { mutableStateOf<String?>(null) }
    val foodRows = day.rows.filter { it.meal != Meals.WATER }
    val glasses = day.rows.filter { it.meal == Meals.WATER }.sumOf { it.quantity }.roundToInt()

    val categories = remember(catalogue) {
        catalogue.map { it.category }.distinct().sorted()
    }

    // Search wins over the category filter: typing is the specific intent, browsing is
    // the vague one. The cap rises when a category is chosen, because then you really
    // are looking at a list rather than picking the obvious match.
    val matches = remember(query, category, catalogue) {
        when {
            query.isNotBlank() ->
                FoodSearch.search(catalogue, query)
            category != null -> catalogue.filter { it.category == category }
            else -> emptyList()
        }
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
            MacroBars(day.totals, day.settings.kcalTarget, day.settings.proteinTargetG)
            Spacer(Modifier.height(18.dp))
            WaterCard(glasses, day.settings.waterGlasses, onAddWater, onRemoveWater)
            Spacer(Modifier.height(20.dp))
            PillRow(Meals.PICKABLE, meal, Sage, { meal = it; copyNote = null })
            // The same breakfast most mornings is the commonest food diary there is.
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = {
                    onCopyMeal(meal) { n ->
                        copyNote = if (n == 0) "Nothing was logged for ${meal.lowercase()} the day before."
                        else "Copied $n ${if (n == 1) "entry" else "entries"}. Tap one below to change it."
                    }
                }) {
                    Text("Same ${meal.lowercase()} as the day before", color = Sage, style = MaterialTheme.typography.labelMedium)
                }
            }
            copyNote?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Faint) }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search food") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            // Browsing by category is how you find the eighth dosa variety when you
            // cannot remember what it is called.
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories, key = { it }) { name ->
                    val on = category == name
                    Chip(name, isOn = on, accent = Sage) { category = if (on) null else name }
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        if (matches.isNotEmpty()) {
            item {
                Text(
                    "${matches.size} ${if (matches.size == 1) "match" else "matches"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(matches, key = { "match-${it.id}" }) { food ->
                FoodPickRow(
                    food = food,
                    onTap = { pending = food },
                    onEdit = { editing = food }
                )
            }
            item { Spacer(Modifier.height(10.dp)) }
        } else if (query.isBlank() && category == null) {
            item {
                SectionHeading(
                    "Usual suspects",
                    trailing = if (quickPicks.isEmpty()) "builds as you log" else null
                )
                Spacer(Modifier.height(10.dp))
                if (quickPicks.isEmpty()) {
                    EmptyNote("Search, or pick a category above. What you log most will surface here.")
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quickPicks, key = { "quick-${it.id}" }) { food ->
                            Box(
                                Modifier
                                    .animateItem()
                                    .pressable(RoundedCornerShape(10.dp)) { pending = food }
                                    .background(Raised)
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
            NutrientPanel(
                micros = day.totals.micros,
                expanded = nutrientsOpen,
                onToggle = { nutrientsOpen = !nutrientsOpen },
                anythingLogged = day.rows.isNotEmpty()
            )
            Spacer(Modifier.height(18.dp))
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
                        accent = if (slot == Meals.SUPPLEMENT) Brass else Sage
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(rows, key = { "entry-${it.logId}" }) { row ->
                    EntryRow(
                        title = row.name,
                        detail = "${Training.formatWeight(row.quantity)} x ${row.unitLabel}",
                        trailing = "${(row.proteinG * row.quantity).roundToInt()} p",
                        accent = if (row.isSupplement) Brass else Sage,
                        onClick = { editingEntry = row },
                        modifier = Modifier.animateItem().padding(bottom = 6.dp)
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }

        if (foodRows.isEmpty()) {
            item { EmptyNote("Nothing logged for this day yet.") }
        } else {
            item {
                Text(
                    "Tap an entry to change the amount or remove it.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
            }
        }
    }

    editingEntry?.let { row ->
        FoodEntryEditorDialog(
            row = row,
            onSave = { qty, slot -> onEditEntry(row.logId, qty, slot); editingEntry = null },
            onDelete = { onRemove(row.logId); editingEntry = null },
            onDismiss = { editingEntry = null }
        )
    }

    val haptics = LocalHapticFeedback.current
    pending?.let { food ->
        QuantityDialog(
            food = food,
            meal = Meals.slotFor(food.isSupplement, meal),
            onConfirm = { qty ->
                onLog(food.id, Meals.slotFor(food.isSupplement, meal), qty)
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                pending = null
            },
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

/**
 * Editing a logged entry.
 *
 * Replaces tap-to-delete for the same reason the set editor does: the usual mistake
 * is two idli logged as one, not the whole entry being wrong, and re-searching the
 * catalogue to fix a quantity is a worse trade than one dialog. A supplement cannot
 * be moved out of its slot, because that slot is decided by the food itself.
 */
@Composable
private fun FoodEntryEditorDialog(
    row: FoodLogRow,
    onSave: (Double, String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var qty by remember(row.logId) { mutableStateOf(trimZero(row.quantity).ifBlank { "1" }) }
    var slot by remember(row.logId) { mutableStateOf(row.meal) }
    val parsed = qty.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text(row.name, color = Bone) },
        text = {
            Column {
                Text(
                    "One is ${row.unitLabel}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mute
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("How many") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                PresetRow(qty) { qty = it }
                if (!row.isSupplement) {
                    Spacer(Modifier.height(14.dp))
                    PillRow(Meals.PICKABLE, slot, Sage, { slot = it })
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "${(row.proteinG * parsed).roundToInt()} g protein, " +
                        "${(row.kcal * parsed).roundToInt()} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Sage
                )
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = onDelete) { Text("Remove this entry", color = Coral) }
            }
        },
        confirmButton = {
            TextButton(enabled = parsed > 0, onClick = { onSave(parsed, slot) }) {
                Text("Save", color = if (parsed > 0) Sage else Faint)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Mute) } }
    )
}

@Composable
private fun FoodPickRow(food: FoodItem, onTap: () -> Unit, onEdit: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .pressable(RoundedCornerShape(10.dp), pressedScale = 0.985f, onClick = onTap)
            .background(Slate)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(food.name, style = MaterialTheme.typography.bodyLarge, color = Bone)
            if (food.aliases.isNotBlank()) {
                Text(food.aliases, style = MaterialTheme.typography.labelSmall, color = Faint)
            }
            Text(
                "${food.unitLabel}   ${food.kcal.roundToInt()} kcal" +
                    if (food.isSupplement) "   supplement" else "",
                style = MaterialTheme.typography.labelSmall,
                color = if (food.isSupplement) Brass else Mute
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
                PresetRow(qty) { qty = it }
                Spacer(Modifier.height(14.dp))
                Text(
                    "${(food.proteinG * parsed).roundToInt()} g protein, " +
                        "${(food.kcal * parsed).roundToInt()} kcal into $meal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Sage
                )
                val notable = notableMicros(food.micros, parsed)
                if (notable.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        notable,
                        style = MaterialTheme.typography.labelSmall,
                        color = Faint
                    )
                }
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

/**
 * The two or three micronutrients this food actually moves, so the confirm step says
 * something useful about a fish oil capsule without listing fifteen zeroes.
 */
private fun notableMicros(micros: Micros, qty: Double): String {
    val scaled = micros * qty
    val bits = buildList {
        if (scaled.omega3G >= 0.1) add("${Numbers.fixed(scaled.omega3G, 1)} g omega-3")
        if (scaled.magnesiumMg >= 20) add("${scaled.magnesiumMg.roundToInt()} mg magnesium")
        if (scaled.ironMg >= 1.0) add("${Numbers.fixed(scaled.ironMg, 1)} mg iron")
        if (scaled.calciumMg >= 80) add("${scaled.calciumMg.roundToInt()} mg calcium")
        if (scaled.vitDMcg >= 1.0) add("${Numbers.fixed(scaled.vitDMcg, 1)} mcg vitamin D")
        if (scaled.fiberG >= 2.0) add("${Numbers.fixed(scaled.fiberG, 1)} g fibre")
    }
    return bits.take(3).joinToString(" · ")
}

@Composable
private fun FoodEditor(
    initial: FoodItem,
    onSave: (FoodItem) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var aliases by remember { mutableStateOf(initial.aliases) }
    var unit by remember { mutableStateOf(initial.unitLabel) }
    var kcal by remember { mutableStateOf(trimZero(initial.kcal)) }
    var protein by remember { mutableStateOf(trimZero(initial.proteinG)) }
    var carbs by remember { mutableStateOf(trimZero(initial.carbsG)) }
    var fat by remember { mutableStateOf(trimZero(initial.fatG)) }
    var showMicros by remember { mutableStateOf(false) }

    // One string per micronutrient. Verbose, but the alternative is a map keyed by
    // string and losing every compile-time guarantee that the field exists.
    var fiber by remember { mutableStateOf(trimZero(initial.micros.fiberG)) }
    var sugar by remember { mutableStateOf(trimZero(initial.micros.sugarG)) }
    var satFat by remember { mutableStateOf(trimZero(initial.micros.satFatG)) }
    var sodium by remember { mutableStateOf(trimZero(initial.micros.sodiumMg)) }
    var potassium by remember { mutableStateOf(trimZero(initial.micros.potassiumMg)) }
    var calcium by remember { mutableStateOf(trimZero(initial.micros.calciumMg)) }
    var iron by remember { mutableStateOf(trimZero(initial.micros.ironMg)) }
    var magnesium by remember { mutableStateOf(trimZero(initial.micros.magnesiumMg)) }
    var zinc by remember { mutableStateOf(trimZero(initial.micros.zincMg)) }
    var vitA by remember { mutableStateOf(trimZero(initial.micros.vitAMcg)) }
    var vitC by remember { mutableStateOf(trimZero(initial.micros.vitCMg)) }
    var vitD by remember { mutableStateOf(trimZero(initial.micros.vitDMcg)) }
    var b12 by remember { mutableStateOf(trimZero(initial.micros.vitB12Mcg)) }
    var folate by remember { mutableStateOf(trimZero(initial.micros.folateMcg)) }
    var omega3 by remember { mutableStateOf(trimZero(initial.micros.omega3G)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text(if (initial.id == 0L) "New food" else "Edit food", color = Bone) },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                Text(
                    "Values are per one unit. Measure yours once and these stop being guesses.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
                if (initial.source.isNotBlank()) {
                    Text(
                        "Numbers from: ${initial.source}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Mute
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = aliases, onValueChange = { aliases = it },
                    label = { Text("Also called") }, singleLine = true,
                    placeholder = { Text("Other names, separated by /") },
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

                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { showMicros = !showMicros }) {
                    Text(
                        if (showMicros) "Hide micronutrients" else "Micronutrients",
                        color = Sage
                    )
                }

                if (showMicros) {
                    Text(
                        "Leave a value at zero if you do not know it. Zero reads as " +
                            "\"not estimated\" everywhere in the app, not as \"contains none\".",
                        style = MaterialTheme.typography.labelSmall,
                        color = Faint
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField(fiber, { fiber = it }, "fibre g", Modifier.weight(1f))
                        NumberField(sugar, { sugar = it }, "sugar g", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField(satFat, { satFat = it }, "sat fat g", Modifier.weight(1f))
                        NumberField(sodium, { sodium = it }, "sodium mg", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField(potassium, { potassium = it }, "potassium mg", Modifier.weight(1f))
                        NumberField(calcium, { calcium = it }, "calcium mg", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField(iron, { iron = it }, "iron mg", Modifier.weight(1f))
                        NumberField(magnesium, { magnesium = it }, "magnesium mg", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField(zinc, { zinc = it }, "zinc mg", Modifier.weight(1f))
                        NumberField(vitA, { vitA = it }, "vit A mcg", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField(vitC, { vitC = it }, "vit C mg", Modifier.weight(1f))
                        NumberField(vitD, { vitD = it }, "vit D mcg", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField(b12, { b12 = it }, "B12 mcg", Modifier.weight(1f))
                        NumberField(folate, { folate = it }, "folate mcg", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    NumberField(omega3, { omega3 = it }, "omega-3 g", Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    val edited = initial.copy(
                        name = name.trim(),
                        aliases = aliases.trim(),
                        unitLabel = unit.ifBlank { "1 serving" },
                        kcal = kcal.toDoubleOrNull() ?: 0.0,
                        proteinG = protein.toDoubleOrNull() ?: 0.0,
                        carbsG = carbs.toDoubleOrNull() ?: 0.0,
                        fatG = fat.toDoubleOrNull() ?: 0.0,
                        micros = Micros(
                            fiberG = fiber.toDoubleOrNull() ?: 0.0,
                            sugarG = sugar.toDoubleOrNull() ?: 0.0,
                            satFatG = satFat.toDoubleOrNull() ?: 0.0,
                            sodiumMg = sodium.toDoubleOrNull() ?: 0.0,
                            potassiumMg = potassium.toDoubleOrNull() ?: 0.0,
                            calciumMg = calcium.toDoubleOrNull() ?: 0.0,
                            ironMg = iron.toDoubleOrNull() ?: 0.0,
                            magnesiumMg = magnesium.toDoubleOrNull() ?: 0.0,
                            zincMg = zinc.toDoubleOrNull() ?: 0.0,
                            vitAMcg = vitA.toDoubleOrNull() ?: 0.0,
                            vitCMg = vitC.toDoubleOrNull() ?: 0.0,
                            vitDMcg = vitD.toDoubleOrNull() ?: 0.0,
                            vitB12Mcg = b12.toDoubleOrNull() ?: 0.0,
                            folateMcg = folate.toDoubleOrNull() ?: 0.0,
                            omega3G = omega3.toDoubleOrNull() ?: 0.0
                        )
                    )
                    onSave(FoodEdits.withProvenance(initial, edited))
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

private fun defaultMealForNow(): String = when (Dates.hourNow()) {
    in 4..10 -> Meals.BREAKFAST
    in 11..15 -> Meals.LUNCH
    in 16..19 -> Meals.SNACK
    else -> Meals.DINNER
}

/**
 * Half, one, one and a half, two. The one matching what is typed lights up, so a
 * preset reads as the current choice rather than a button that did something.
 */
@Composable
private fun PresetRow(current: String, onPick: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("0.5", "1", "1.5", "2").forEach { preset ->
            val on = current.toDoubleOrNull() == preset.toDouble()
            val fill by animateColorAsState(
                if (on) Sage.copy(alpha = 0.18f) else Raised, Motion.quick(), label = "preset"
            )
            Box(
                Modifier
                    .weight(1f)
                    .pressable(RoundedCornerShape(8.dp)) { onPick(preset) }
                    .background(fill)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(preset, style = MaterialTheme.typography.labelMedium, color = if (on) Sage else Bone)
            }
        }
    }
}
