package dev.avles.gym.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.avles.gym.domain.Energy
import dev.avles.gym.domain.MacroMath
import dev.avles.gym.domain.Plates
import dev.avles.gym.domain.Totals
import dev.avles.gym.ui.components.Motion.rollNumber
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Raised
import dev.avles.gym.ui.theme.Sage
import kotlin.math.roundToInt

/** Water blue. Only the water card uses it, so it reads as water and nothing else. */
val Water = Color(0xFF5FA8D3)

/**
 * Protein, carbohydrate and fat side by side against their targets, the row every
 * nutrition app puts under the calorie number. Carbohydrate and fat targets follow
 * from the calorie and protein ones (see [Energy.split]), so there is nothing extra
 * to set up.
 */
@Composable
fun MacroBars(totals: Totals, kcalTarget: Double, proteinTargetG: Double, modifier: Modifier = Modifier) {
    val split = Energy.split(kcalTarget, proteinTargetG)
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        MacroColumn("Protein", totals.proteinG, split.proteinG, Sage, Modifier.weight(1f))
        MacroColumn("Carbs", totals.carbsG, split.carbsG, Brass, Modifier.weight(1f))
        MacroColumn("Fat", totals.fatG, split.fatG, Mute, Modifier.weight(1f))
    }
}

@Composable
private fun MacroColumn(label: String, value: Double, target: Double, accent: Color, modifier: Modifier) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            AnimatedContent(value.roundToInt().toString(), transitionSpec = { rollNumber() }, label = label) {
                Text(it, style = MaterialTheme.typography.titleMedium, color = Bone)
            }
            Text(
                " / ${target.roundToInt()} g",
                style = MaterialTheme.typography.labelSmall,
                color = Faint,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Meter(
            progress = MacroMath.ratio(value, target),
            accent = accent,
            overflow = MacroMath.rawRatio(value, target) > 1.1,
            height = 4
        )
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Mute)
    }
}

/**
 * Glasses of water as a row of drops that fill as you tap, with a minus for the
 * glass you did not drink. The tracker HealthifyMe and every habit app have, at the
 * cost of one tap per glass.
 */
@Composable
fun WaterCard(glasses: Int, goal: Int, onAdd: () -> Unit, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                AnimatedContent(glasses.toString(), transitionSpec = { rollNumber() }, label = "water") {
                    Text(it, style = MaterialTheme.typography.headlineSmall, color = Water)
                }
                Text(
                    " of $goal glasses · ${Plates.label(glasses * Energy.GLASS_ML / 1000.0)} L",
                    style = MaterialTheme.typography.labelSmall,
                    color = Mute,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoundButton("−", enabled = glasses > 0, onClick = onRemove)
                RoundButton("+", enabled = true, haptic = Haptic.CONFIRM, onClick = onAdd)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(maxOf(goal, glasses).coerceAtMost(16)) { i ->
                val fill by animateColorAsState(
                    if (i < glasses) Water else Raised, Motion.standard(), label = "drop"
                )
                Box(Modifier.size(width = 14.dp, height = 20.dp).background(fill, RoundedCornerShape(4.dp, 4.dp, 7.dp, 7.dp)))
            }
        }
    }
}

@Composable
private fun RoundButton(text: String, enabled: Boolean, haptic: Haptic = Haptic.TICK, onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .pressable(RoundedCornerShape(18.dp), enabled = enabled, haptic = haptic, pressedScale = 0.9f, onClick = onClick)
            .background(if (enabled) Water.copy(alpha = 0.18f) else Raised),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = if (enabled) Water else Faint)
    }
}
