package dev.avles.gym.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.avles.gym.data.Micros
import dev.avles.gym.domain.Direction
import dev.avles.gym.domain.Numbers
import dev.avles.gym.domain.NutrientRef
import dev.avles.gym.domain.Nutrients
import dev.avles.gym.ui.i18n.tr
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Coral
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Raised
import dev.avles.gym.ui.theme.Sage

/**
 * Collapsed by default, and that is the design rather than an oversight.
 *
 * Fifteen bars is a wall, and this app's one job is to log a meal in ten seconds.
 * So the closed state says the single useful sentence — what today is short of —
 * and the full table is one tap away for the evening when you actually want to read
 * it. Same reasoning as the rest of the app: the number that changes a decision goes
 * on top, everything else goes behind a tap.
 */
@Composable
fun NutrientPanel(
    micros: Micros,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    anythingLogged: Boolean = true
) {
    val short = Nutrients.shortfalls(micros)
    val over = Nutrients.exceeded(micros)

    Column(modifier.fillMaxWidth()) {
        SectionHeading(
            title = tr("Nutrients"),
            trailing = if (expanded) "hide" else "show all",
            accent = Sage,
            modifier = Modifier.pressable(RoundedCornerShape(8.dp), pressedScale = 0.99f, onClick = onToggle)
        )
        Spacer(Modifier.height(8.dp))

        if (!anythingLogged) {
            Text(
                "Log something and the day's vitamins and minerals add up here.",
                style = MaterialTheme.typography.bodyMedium,
                color = Faint
            )
            return@Column
        }

        Text(
            buildSummary(short, over),
            style = MaterialTheme.typography.bodyMedium,
            color = if (over.isNotEmpty()) Coral else Mute
        )

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(Motion.standard()) + fadeIn(Motion.enter()),
            exit = shrinkVertically(Motion.standard()) + fadeOut(Motion.quick())
        ) {
            Column {
                Spacer(Modifier.height(16.dp))
                Nutrients.ALL.forEach { ref ->
                    NutrientRow(ref, micros)
                    Spacer(Modifier.height(12.dp))
                }
                Text(
                    "Reference intakes for an adult, not targets this app worked out for you. " +
                        "A zero means the food was never estimated for that nutrient, not that " +
                        "it contains none — home-style dishes are built up from their main " +
                        "ingredients and should be read as a direction, not a measurement.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint
                )
            }
        }
    }
}

private fun buildSummary(short: List<NutrientRef>, over: List<NutrientRef>): String {
    val parts = mutableListOf<String>()
    if (short.isNotEmpty()) parts += "Low on ${short.joinToString(", ") { it.label.lowercase() }}"
    if (over.isNotEmpty()) parts += "over on ${over.joinToString(", ") { it.label.lowercase() }}"
    return when {
        parts.isEmpty() -> "Nothing notably short or over today."
        else -> parts.joinToString(" · ") + "."
    }
}

@Composable
private fun NutrientRow(ref: NutrientRef, micros: Micros) {
    val value = ref.read(micros)
    val isOver = Nutrients.isOver(ref, micros)
    val accent = if (isOver) Coral else if (ref.direction == Direction.CEILING) Mute else Sage

    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(ref.label, style = MaterialTheme.typography.bodyMedium, color = Bone)
            Text(
                "${format(value, ref.decimals)} / ${format(ref.reference, ref.decimals)} ${ref.unit}",
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
        }
        Spacer(Modifier.height(5.dp))
        Meter(
            progress = Nutrients.progress(ref, micros),
            accent = accent,
            overflow = isOver,
            height = 3
        )
        if (ref.note.isNotEmpty()) {
            Spacer(Modifier.height(3.dp))
            Text(ref.note, style = MaterialTheme.typography.labelSmall, color = Faint)
        }
    }
}

/** Locale-pinned for the same reason weights are: a comma here would read as a thousands mark. */
private fun format(v: Double, decimals: Int): String =
    Numbers.fixed(v, decimals)

/**
 * A single nutrient, small, for putting one number next to a meal without opening
 * the whole panel.
 */
@Composable
fun NutrientChip(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Raised)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Mute)
        Spacer(Modifier.width(6.dp))
        Text(value, style = MaterialTheme.typography.labelMedium, color = Sage)
    }
}
