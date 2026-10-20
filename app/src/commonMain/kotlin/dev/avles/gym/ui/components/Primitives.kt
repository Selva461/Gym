package dev.avles.gym.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.avles.gym.ui.components.Motion.rollNumber
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Line
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Raised
import dev.avles.gym.ui.theme.Slate

/** A single hairline. The app separates sections with these rather than boxing everything. */
@Composable
fun Hairline(modifier: Modifier = Modifier, color: Color = Line) {
    Box(modifier.fillMaxWidth().height(1.dp).background(color))
}

/**
 * Section heading in sentence case with an optional trailing value. No all-caps
 * eyebrow, no divider chrome — the space above it does the separating.
 */
@Composable
fun SectionHeading(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    accent: Color = Mute
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = Bone)
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.labelMedium, color = accent)
        }
    }
}

/**
 * The app's one loud element. A quantity, set large and light, with its unit
 * riding small alongside so the numeral keeps the whole optical weight.
 */
@Composable
fun BigNumber(
    value: String,
    unit: String,
    caption: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            AnimatedContent(targetState = value, transitionSpec = { rollNumber() }, label = "big-number") {
                Text(
                    it,
                    style = MaterialTheme.typography.displayMedium,
                    color = accent
                )
            }
            Text(
                unit,
                style = MaterialTheme.typography.titleMedium,
                color = accent.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
        }
        Text(caption, style = MaterialTheme.typography.labelSmall, color = Mute)
    }
}

/**
 * A flat meter rather than a ring. Rings look good in screenshots; a bar reads
 * faster at a glance and leaves room for the number that actually matters.
 * Going past the target turns the overflow segment a different colour instead of
 * silently capping, so an overshoot is visible rather than hidden.
 */
@Composable
fun Meter(
    progress: Float,
    accent: Color,
    modifier: Modifier = Modifier,
    overflow: Boolean = false,
    height: Int = 6
) {
    // Starts empty and fills to the value, so a meter that comes into view shows
    // how far along the day is rather than simply being there.
    val target = progress.coerceIn(0f, 1f)
    val animated = remember { Animatable(0f) }
    LaunchedEffect(target) {
        animated.animateTo(target, tween(durationMillis = 650, easing = FastOutSlowInEasing))
    }
    val fill by animateColorAsState(
        if (overflow) MaterialTheme.colorScheme.error else accent,
        Motion.standard(),
        label = "meter-colour"
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(height.dp))
            .background(Raised)
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated.value)
                .height(height.dp)
                .clip(RoundedCornerShape(height.dp))
                .background(fill)
        )
    }
}

/**
 * A logged thing. Left edge carries the accent of whichever half of the app it
 * belongs to; no shadow, no full radius — it is a row in a list, not a card.
 */
@Composable
fun EntryRow(
    title: String,
    detail: String,
    trailing: String,
    accent: Color,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
    // A record lights the row in its accent and lets it fade back over a second and
    // a half: enough to catch the eye between sets, gone before it becomes noise.
    val glow = remember { Animatable(0f) }
    LaunchedEffect(highlight) {
        if (highlight) {
            glow.snapTo(1f)
            glow.animateTo(0f, tween(1600))
        }
    }
    val lit = accent.copy(alpha = 0.32f).compositeOver(Slate)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.pressable(shape, pressedScale = 0.985f, onClick = onClick)
                else Modifier.clip(shape)
            )
            // Read in the draw phase, so the fade repaints the row without recomposing it.
            .drawBehind { drawRect(lerp(Slate, lit, glow.value)) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(3.dp).height(34.dp).background(accent))
        Column(
            Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = Bone,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(detail, style = MaterialTheme.typography.labelSmall, color = Mute)
        }
        Text(
            trailing,
            style = MaterialTheme.typography.titleMedium,
            color = accent,
            modifier = Modifier.padding(end = 14.dp)
        )
    }
}

/** Selector pills. Used for meals and for session labels. */
@Composable
fun PillRow(
    options: List<String>,
    selected: String,
    accent: Color,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isOn = option == selected
            val fill by animateColorAsState(
                if (isOn) accent.copy(alpha = 0.16f) else Raised, Motion.quick(), label = "pill"
            )
            val ink by animateColorAsState(if (isOn) accent else Mute, Motion.quick(), label = "pill-text")
            Box(
                Modifier
                    .weight(1f)
                    .pressable(RoundedCornerShape(10.dp)) { onSelect(option) }
                    .background(fill)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    option,
                    style = MaterialTheme.typography.labelMedium,
                    color = ink,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Empty states point at the next action instead of apologising for being empty.
 */
@Composable
fun EmptyNote(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = Faint,
        modifier = modifier.fillMaxWidth().padding(vertical = 18.dp)
    )
}

/**
 * A horizontally scrolling row of selectable chips.
 *
 * [PillRow] splits the width evenly between its options, which works for four meals
 * and falls apart at thirteen muscle groups — every label ends up ellipsised to two
 * characters. This scrolls instead, so each chip is as wide as its own word.
 */
@Composable
fun ChipRow(
    options: List<String>,
    selected: String,
    accent: Color,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options, key = { "chip-$it" }) { option ->
            Chip(option, isOn = option == selected, accent = accent) { onSelect(option) }
        }
    }
}

@Composable
fun Dot(color: Color, size: Int = 4) {
    Box(
        Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(size.dp))
            .background(color)
    )
}

/**
 * One selectable chip. Shared by every horizontally scrolling filter in the app so
 * they all light up, press and tick the same way.
 */
@Composable
fun Chip(label: String, isOn: Boolean, accent: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val fill by animateColorAsState(
        if (isOn) accent.copy(alpha = 0.18f) else Raised, Motion.quick(), label = "chip"
    )
    val ink by animateColorAsState(if (isOn) accent else Mute, Motion.quick(), label = "chip-text")
    Box(
        modifier
            .pressable(RoundedCornerShape(10.dp), onClick = onClick)
            .background(fill)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = ink, maxLines = 1)
    }
}
