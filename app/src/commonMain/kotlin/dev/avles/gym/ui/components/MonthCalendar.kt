package dev.avles.gym.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.avles.gym.core.Dates
import dev.avles.gym.domain.MonthGrid
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Raised
import dev.avles.gym.ui.theme.Sage

/**
 * A month on one screen: a trained day is a brass square, a planned rest day an
 * outline, and a sage dot under the date means food was logged. The view the
 * training apps open on, because a month is the span over which consistency is
 * visible and a week is not.
 */
@Composable
fun MonthCalendar(
    trained: Set<Long>,
    fuelled: Set<Long>,
    restDays: Set<Long>,
    modifier: Modifier = Modifier
) {
    val today = Dates.today()
    var month by remember { mutableStateOf(MonthGrid.shift(today, 0)) }
    val isCurrent = month.year == today.year && month.month == today.month

    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { month = MonthGrid.shift(month, -1) }) { Text("‹", color = Brass) }
            Text(
                MonthGrid.title(month),
                style = MaterialTheme.typography.titleMedium,
                color = Bone,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${MonthGrid.countIn(month, trained)} trained",
                style = MaterialTheme.typography.labelSmall,
                color = Brass
            )
            TextButton(onClick = { month = MonthGrid.shift(month, 1) }, enabled = !isCurrent) {
                Text("›", color = if (isCurrent) Faint else Brass)
            }
        }
        Row(Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint,
                    modifier = Modifier.weight(1f).padding(bottom = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        AnimatedContent(
            targetState = month,
            transitionSpec = {
                val forward = targetState > initialState
                (slideInHorizontally(tween(Motion.STANDARD)) { if (forward) it / 4 else -it / 4 } + fadeIn(tween(Motion.STANDARD)))
                    .togetherWith(fadeOut(tween(Motion.QUICK)))
            },
            label = "month"
        ) { shown ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MonthGrid.weeks(shown).forEach { week ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        week.forEach { day -> DayCell(day, trained, fuelled, restDays, today, Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Long?,
    trained: Set<Long>,
    fuelled: Set<Long>,
    restDays: Set<Long>,
    today: kotlinx.datetime.LocalDate,
    modifier: Modifier
) {
    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        if (day == null) return@Box
        val date = Dates.ofEpochDay(day)
        val didTrain = day in trained
        val isRest = day in restDays
        val shape = RoundedCornerShape(8.dp)
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(shape)
                .background(
                    when {
                        didTrain -> Brass.copy(alpha = 0.85f)
                        isRest -> Raised
                        else -> androidx.compose.ui.graphics.Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        didTrain -> Raised
                        date > today -> Faint
                        date == today -> Bone
                        else -> Mute
                    }
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    Modifier
                        .padding(top = 1.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (day in fuelled) Sage else androidx.compose.ui.graphics.Color.Transparent)
                        .padding(horizontal = 4.dp, vertical = 1.5.dp)
                )
            }
        }
    }
}
