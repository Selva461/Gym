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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.avles.gym.data.BodyMeasurement
import dev.avles.gym.data.GymSettings
import dev.avles.gym.domain.Measurements
import dev.avles.gym.domain.Numbers
import dev.avles.gym.domain.Plates
import dev.avles.gym.ui.components.BigNumber
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.Hairline
import dev.avles.gym.ui.components.PillRow
import dev.avles.gym.ui.components.SectionHeading
import dev.avles.gym.ui.components.TrendChart
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Sage
import dev.avles.gym.ui.theme.Slate

/**
 * Tape measurements over time and the body-fat estimate they give: the page the
 * tracking apps keep beside the scale, because a waist going down while the weight
 * holds is the recomposition the scale cannot show.
 */
@Composable
fun MeasurementsScreen(
    measurements: List<BodyMeasurement>,
    settings: GymSettings,
    onSave: (Map<String, Double>) -> Unit,
    onSaveBody: (Double, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var measuring by remember { mutableStateOf(false) }
    val trends = remember(measurements) { Measurements.trends(measurements) }
    val latest = trends.associate { it.part to it.latestCm }
    val bodyFat = Measurements.navyBodyFat(
        isFemale = settings.isFemale,
        heightCm = settings.heightCm,
        neckCm = latest["Neck"] ?: 0.0,
        waistCm = latest["Waist"] ?: 0.0,
        hipsCm = latest["Hips"]
    )

    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        item {
            Spacer(Modifier.height(16.dp))
            Text("Measurements", style = MaterialTheme.typography.headlineMedium, color = Bone)
            Text(
                "The tape, for what the scale misses. Measure in the morning, same spots each time.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mute
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { measuring = true },
                colors = ButtonDefaults.buttonColors(containerColor = Sage.copy(alpha = 0.14f), contentColor = Sage),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Measure today") }
            Spacer(Modifier.height(22.dp))

            SectionHeading("Body fat", trailing = "US Navy method", accent = Sage)
            Spacer(Modifier.height(10.dp))
            if (bodyFat != null) {
                BigNumber(Numbers.fixed(bodyFat, 1), "%", "estimated from neck, waist" + if (settings.isFemale) ", hips and height" else " and height", Sage)
            } else {
                Text(
                    "Needs your height below and a neck and waist measurement" +
                        (if (settings.isFemale) ", plus hips." else "."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Faint
                )
            }
            Spacer(Modifier.height(12.dp))
            BodyRow(settings, onSaveBody)
            Spacer(Modifier.height(22.dp))
            Hairline()
            Spacer(Modifier.height(18.dp))
        }

        if (trends.isEmpty()) {
            item { EmptyNote("No measurements yet. Waist is the one worth starting with.") }
        }
        items(trends, key = { "part-${it.part}" }) { t ->
            Column(Modifier.animateItem().padding(bottom = 18.dp)) {
                SectionHeading(
                    t.part,
                    trailing = "${Plates.label(t.latestCm)} cm" + when {
                        t.points.size < 2 -> ""
                        t.changeCm > 0 -> "  (+${Plates.label(t.changeCm)})"
                        t.changeCm < 0 -> "  (−${Plates.label(-t.changeCm)})"
                        else -> "  (same)"
                    },
                    accent = Sage
                )
                Spacer(Modifier.height(6.dp))
                TrendChart(values = t.points, accent = Sage, heightDp = 70, emptyMessage = "A second measurement draws a line.")
            }
        }
    }

    if (measuring) {
        MeasureDialog(
            latest = latest,
            onSave = { onSave(it); measuring = false },
            onDismiss = { measuring = false }
        )
    }
}

@Composable
private fun BodyRow(settings: GymSettings, onSaveBody: (Double, Boolean) -> Unit) {
    var height by remember(settings.heightCm) {
        mutableStateOf(if (settings.heightCm > 0) Plates.label(settings.heightCm) else "")
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = height,
            onValueChange = { height = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("height, cm") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
        )
        Column(Modifier.weight(1.3f)) {
            PillRow(
                options = listOf("Male", "Female"),
                selected = if (settings.isFemale) "Female" else "Male",
                accent = Sage,
                onSelect = { onSaveBody(height.toDoubleOrNull() ?: settings.heightCm, it == "Female") }
            )
        }
    }
    TextButton(onClick = { onSaveBody(height.toDoubleOrNull() ?: 0.0, settings.isFemale) }) {
        Text("Save height", color = Sage)
    }
}

@Composable
private fun MeasureDialog(latest: Map<String, Double>, onSave: (Map<String, Double>) -> Unit, onDismiss: () -> Unit) {
    val values = remember { mutableStateMapOf<String, String>() }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate,
        title = { Text("Today's measurements", color = Bone) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "In centimetres. Leave out any you did not measure.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
                Spacer(Modifier.height(8.dp))
                Measurements.PARTS.forEach { part ->
                    OutlinedTextField(
                        value = values[part] ?: "",
                        onValueChange = { values[part] = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(part + (latest[part]?.let { "  (last ${Plates.label(it)})" } ?: "")) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    )
                }
            }
        },
        confirmButton = {
            val parsed = values.mapNotNull { (k, v) -> v.toDoubleOrNull()?.takeIf { it > 0 }?.let { k to it } }.toMap()
            TextButton(enabled = parsed.isNotEmpty(), onClick = { onSave(parsed) }) {
                Text("Save", color = if (parsed.isNotEmpty()) Sage else Faint)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Mute) } }
    )
}
