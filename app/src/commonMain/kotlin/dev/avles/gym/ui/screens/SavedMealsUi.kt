package dev.avles.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.avles.gym.domain.Plates
import dev.avles.gym.domain.SavedMeals
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.components.pressable
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Coral
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Raised
import dev.avles.gym.ui.theme.Sage
import dev.avles.gym.ui.theme.Slate
import kotlin.math.roundToInt

/** The saved meals as a row of chips; tapping one opens it to log or delete. */
@Composable
fun SavedMealsRow(meals: List<SavedMeals.Meal>, slot: String, onLog: (SavedMeals.Meal) -> Unit, onDelete: (Long) -> Unit) {
    if (meals.isEmpty()) return
    var open by remember { mutableStateOf<SavedMeals.Meal?>(null) }
    Column {
        Spacer(Modifier.height(16.dp))
        SectionHeading("Saved meals", trailing = "logs into ${slot.lowercase()}")
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(meals, key = { "saved-${it.id}" }) { meal ->
                Column(
                    Modifier
                        .animateItem()
                        .pressable(RoundedCornerShape(10.dp)) { open = meal }
                        .background(Raised)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(meal.name, style = MaterialTheme.typography.labelMedium, color = Bone)
                    Text(
                        "${meal.proteinG.roundToInt()} g protein · ${meal.kcal.roundToInt()} kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = Sage
                    )
                }
            }
        }
    }
    open?.let { meal ->
        var confirmingDelete by remember(meal.id) { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { open = null },
            containerColor = Slate,
            title = { Text(meal.name, color = Bone) },
            text = {
                Column {
                    meal.items.forEach {
                        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(it.foodName, style = MaterialTheme.typography.bodyMedium, color = Bone, modifier = Modifier.weight(1f))
                            Text("× ${Plates.label(it.quantity)}", style = MaterialTheme.typography.bodyMedium, color = Mute)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = {
                        if (confirmingDelete) {
                            onDelete(meal.id)
                            open = null
                        } else {
                            confirmingDelete = true
                        }
                    }) {
                        Text(if (confirmingDelete) "Tap again to delete" else "Delete this saved meal", color = Coral)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onLog(meal); open = null }) { Text("Log to ${slot.lowercase()}", color = Sage) }
            },
            dismissButton = { TextButton(onClick = { open = null }) { Text("Cancel", color = Mute) } }
        )
    }
}

/** Names a meal's entries and keeps them as a saved meal. */
@Composable
fun SaveMealButton(slot: String, onSave: (String) -> Unit) {
    var naming by remember { mutableStateOf(false) }
    TextButton(onClick = { naming = true }) {
        Text("Save this ${slot.lowercase()} for next time", color = Faint, style = MaterialTheme.typography.labelSmall)
    }
    if (naming) {
        var name by remember { mutableStateOf("My ${slot.lowercase()}") }
        AlertDialog(
            onDismissRequest = { naming = false },
            containerColor = Slate,
            title = { Text("Save meal", color = Bone) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = { onSave(name); naming = false }) {
                    Text("Save", color = Sage)
                }
            },
            dismissButton = { TextButton(onClick = { naming = false }) { Text("Cancel", color = Mute) } }
        )
    }
}

/** Calories and macros with no food behind them: a meal out nobody can look up. */
@Composable
fun QuickAddButton(slot: String, onAdd: (Double, Double, Double, Double) -> Unit) {
    var open by remember { mutableStateOf(false) }
    TextButton(onClick = { open = true }) { Text("Quick add calories", color = Sage) }
    if (!open) return
    val fields = remember { androidx.compose.runtime.mutableStateListOf("", "", "", "") }
    val labels = listOf("kcal", "protein, g", "carbs, g", "fat, g")
    val kcal = fields[0].toDoubleOrNull() ?: 0.0
    val protein = fields[1].toDoubleOrNull() ?: 0.0
    AlertDialog(
        onDismissRequest = { open = false },
        containerColor = Slate,
        title = { Text("Quick add to ${slot.lowercase()}", color = Bone) },
        text = {
            Column {
                Text(
                    "For a meal you cannot look up. Calories and protein are enough; carbs and fat if you know them.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
                Spacer(Modifier.height(8.dp))
                labels.forEachIndexed { i, label ->
                    OutlinedTextField(
                        value = fields[i],
                        onValueChange = { v -> fields[i] = v.filter { c -> c.isDigit() || c == '.' }.take(6) },
                        label = { Text(label) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    )
                }
            }
        },
        confirmButton = {
            val ok = kcal > 0 || protein > 0
            TextButton(enabled = ok, onClick = {
                onAdd(kcal, protein, fields[2].toDoubleOrNull() ?: 0.0, fields[3].toDoubleOrNull() ?: 0.0)
                open = false
            }) { Text("Add", color = if (ok) Sage else Faint) }
        },
        dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel", color = Mute) } }
    )
}
