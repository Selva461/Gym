package dev.avles.gym.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.avles.gym.domain.Numbers
import dev.avles.gym.domain.Weekly
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Sage
import kotlin.math.roundToInt

/** The last seven days against the seven before: days trained, volume, protein. */
@Composable
fun WeekCard(week: Weekly.Comparison, proteinTargetG: Double, modifier: Modifier = Modifier) {
    val now = week.thisWeek
    val before = week.lastWeek
    Column(modifier.fillMaxWidth()) {
        SectionHeading("This week", trailing = "last 7 days vs the 7 before")
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Stat(
                value = now.daysTrained.toString(),
                label = if (now.daysTrained == 1) "day trained" else "days trained",
                delta = (now.daysTrained - before.daysTrained).let { if (it > 0) "+$it" else if (it < 0) "−${-it}" else "same" },
                accent = Brass,
                modifier = Modifier.weight(1f)
            )
            Stat(
                value = if (now.volumeKg >= 10_000) Numbers.fixed(now.volumeKg / 1000, 1) + "k" else now.volumeKg.roundToInt().toString(),
                label = "kg moved",
                delta = Weekly.change(now.volumeKg, before.volumeKg),
                accent = Brass,
                modifier = Modifier.weight(1f)
            )
            Stat(
                value = now.avgProteinG?.roundToInt()?.toString() ?: "–",
                label = "g protein a day",
                delta = now.avgProteinG?.let { "${(it / proteinTargetG * 100).roundToInt()}% of target" },
                accent = Sage,
                modifier = Modifier.weight(1f)
            )
        }
        if (now.daysLogged in 1..6) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Protein averaged over the ${now.daysLogged} days food was logged.",
                style = MaterialTheme.typography.labelSmall,
                color = Faint
            )
        }
    }
}

@Composable
private fun Stat(value: String, label: String, delta: String?, accent: Color, modifier: Modifier) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = accent)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Mute)
        if (delta != null) Text(delta, style = MaterialTheme.typography.labelSmall, color = Bone)
    }
}
