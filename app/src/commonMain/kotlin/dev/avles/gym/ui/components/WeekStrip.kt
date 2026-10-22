package dev.avles.gym.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.avles.gym.core.Dates
import dev.avles.gym.core.epochDay
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Raised
import dev.avles.gym.ui.theme.Sage
import kotlinx.datetime.LocalDate

/**
 * The seven days around the selected one, with two dots under each day: brass if
 * something was trained, sage if something was eaten. It is the same calendar rail
 * Notify uses, doing a different job — here the dots are a consistency record you
 * read in half a second, so a gap in the row is the whole feedback mechanism.
 */
@Composable
fun WeekStrip(
    selected: LocalDate,
    today: LocalDate,
    trainedDays: Set<Long>,
    fuelledDays: Set<Long>,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val start = selected.epochDay - 3
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        (0..6).forEach { offset ->
            val epoch = start + offset
            val day = Dates.ofEpochDay(epoch)
            val isSelected = day == selected
            val isFuture = day > today
            val fill by animateColorAsState(
                if (isSelected) Raised else Color.Transparent, Motion.quick(), label = "week-day"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .pressable(RoundedCornerShape(12.dp), enabled = !isFuture) { onSelect(day) }
                    .background(fill)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    day.dayOfWeek.name.take(1),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFuture) Faint else Mute
                )
                Text(
                    day.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = when {
                        isFuture -> Faint
                        isSelected -> Bone
                        else -> Mute
                    },
                    modifier = Modifier.padding(top = 2.dp)
                )
                Row(
                    modifier = Modifier.padding(top = 6.dp).height(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    if (trainedDays.contains(epoch)) Dot(Brass)
                    if (fuelledDays.contains(epoch)) Dot(Sage)
                }
            }
        }
    }
}

@Composable
fun StripLegend(modifier: Modifier = Modifier) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(4.dp).clip(RoundedCornerShape(4.dp)).background(Brass))
        Text("trained", style = MaterialTheme.typography.labelSmall, color = Faint)
        Box(Modifier.size(4.dp).clip(RoundedCornerShape(4.dp)).background(Sage))
        Text("logged food", style = MaterialTheme.typography.labelSmall, color = Faint)
    }
}
