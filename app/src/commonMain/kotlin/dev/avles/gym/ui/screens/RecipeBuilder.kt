package dev.avles.gym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.avles.gym.data.FoodItem
import dev.avles.gym.domain.FoodSearch
import dev.avles.gym.domain.Recipes
import dev.avles.gym.ui.components.pressable
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Raised
import dev.avles.gym.ui.theme.Sage
import dev.avles.gym.ui.theme.Slate
import kotlin.math.roundToInt

/** One ingredient line while the recipe is being put together; the amount is as typed. */
private class Line(val food: FoodItem, amount: String) {
    var amount by mutableStateOf(amount)
}

/**
 * A home recipe from the catalogue: search, add ingredients with how many of each
 * unit went in, say how many servings it made, save. The result is a custom food in
 * "My recipes", logged like any other and editable in the food editor.
 */
@Composable
internal fun RecipeBuilderDialog(catalogue: List<FoodItem>, onSave: (FoodItem) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("4") }
    var query by remember { mutableStateOf("") }
    val lines = remember { mutableStateListOf<Line>() }
    val parts = lines.map { Recipes.Part(it.food, it.amount.toDoubleOrNull() ?: 0.0) }
    val n = servings.toIntOrNull()?.coerceIn(1, 50) ?: 1
    val preview = Recipes.combine(name.ifBlank { "Recipe" }, parts, n)
    val matches = remember(query, catalogue) {
        if (query.isBlank()) emptyList() else FoodSearch.search(catalogue, query, limit = 6)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text("Build a recipe", color = Bone) },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, singleLine = true,
                    label = { Text("Name") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = servings, onValueChange = { servings = it.filter(Char::isDigit) }, singleLine = true,
                    label = { Text("Servings it makes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                lines.forEach { line ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(line.food.name, style = MaterialTheme.typography.bodyMedium, color = Bone)
                            Text("× ${line.food.unitLabel}", style = MaterialTheme.typography.labelSmall, color = Faint)
                        }
                        OutlinedTextField(
                            value = line.amount,
                            onValueChange = { line.amount = it.filter { c -> c.isDigit() || c == '.' } },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.width(72.dp)
                        )
                        TextButton(onClick = { lines.remove(line) }) { Text("✕", color = Mute) }
                    }
                }
                OutlinedTextField(
                    value = query, onValueChange = { query = it }, singleLine = true,
                    label = { Text("Add an ingredient") }, modifier = Modifier.fillMaxWidth()
                )
                matches.forEach { food ->
                    Text(
                        "${food.name}  ·  ${food.unitLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Bone,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .pressable(RoundedCornerShape(8.dp)) { lines.add(Line(food, "1")); query = "" }
                            .background(Raised)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    if (lines.isEmpty()) "Add what went into the pot, with how many of each unit."
                    else "Per serving: ${preview.proteinG.roundToInt()} g protein, ${preview.kcal.roundToInt()} kcal, " +
                        "${preview.carbsG.roundToInt()} g carbs, ${preview.fatG.roundToInt()} g fat",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (lines.isEmpty()) Faint else Sage
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(Recipes.combine(name, parts, n)) },
                enabled = name.isNotBlank() && parts.any { it.quantity > 0 },
                colors = ButtonDefaults.buttonColors(containerColor = Sage, contentColor = Slate)
            ) { Text("Save recipe") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Mute) } }
    )
}
