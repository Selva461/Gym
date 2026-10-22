package dev.avles.gym.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.avles.gym.data.FoodItem
import dev.avles.gym.data.Micros
import dev.avles.gym.domain.Numbers
import dev.avles.gym.domain.Portions
import dev.avles.gym.ui.components.PillRow
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Sage
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Slate
import kotlin.math.roundToInt

/**
 * How much of [food] to log. When its unit says what it weighs, a toggle switches
 * between servings and grams (or ml), so "180 g of rice" needs no arithmetic. The
 * star pins the food at the top of Fuel.
 */
@Composable
internal fun QuantityDialog(
    food: FoodItem,
    meal: String,
    isFavourite: Boolean,
    onToggleFavourite: () -> Unit,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val measure = remember(food.unitLabel) { Portions.measureOf(food.unitLabel) }
    var byWeight by remember { mutableStateOf(false) }
    var qty by remember { mutableStateOf("1") }
    val typed = qty.toDoubleOrNull() ?: 0.0
    val parsed = if (byWeight && measure != null) Portions.unitsFor(typed, measure) else typed
    val servingsLabel = "Servings"
    val weightLabel = if (measure?.unit == "ml") "Millilitres" else "Grams"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(food.name, color = Bone, modifier = Modifier.weight(1f))
                TextButton(onClick = onToggleFavourite) {
                    Text(if (isFavourite) "★" else "☆", color = if (isFavourite) Brass else Mute,
                        style = MaterialTheme.typography.titleLarge)
                }
            }
        },
        text = {
            Column {
                Text(
                    if (byWeight && measure != null) "How many ${measure.unit}? One serving is ${food.unitLabel}."
                    else "How many? One is ${food.unitLabel}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mute
                )
                if (measure != null) {
                    Spacer(Modifier.height(10.dp))
                    PillRow(
                        options = listOf(servingsLabel, weightLabel),
                        selected = if (byWeight) weightLabel else servingsLabel,
                        accent = Sage,
                        onSelect = { picked ->
                            val toWeight = picked == weightLabel
                            if (toWeight != byWeight) {
                                // Carry the amount across, so switching never loses what was typed.
                                val units = parsed
                                qty = if (toWeight) Numbers.fixed(units * measure.amount, 0)
                                else Numbers.fixed(units, 2).trimEnd('0').trimEnd('.')
                                byWeight = toWeight
                            }
                        }
                    )
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it.filter { c -> c.isDigit() || c == '.' } },
                    singleLine = true,
                    suffix = { if (byWeight && measure != null) Text(measure.unit, color = Mute) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                if (!byWeight) {
                    Spacer(Modifier.height(14.dp))
                    PresetRow(qty) { qty = it }
                }
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
                    Text(notable, style = MaterialTheme.typography.labelSmall, color = Faint)
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
