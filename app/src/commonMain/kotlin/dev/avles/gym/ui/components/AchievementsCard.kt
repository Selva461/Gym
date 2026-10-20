package dev.avles.gym.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.avles.gym.domain.Achievements
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Mute

/** Earned badges as a scrolling row, then the next one in each family with its meter. */
@Composable
fun AchievementsCard(totals: Achievements.Totals, modifier: Modifier = Modifier) {
    val all = Achievements.all(totals)
    val earned = all.filter { it.earned }
    val next = Achievements.next(totals)
    Column(modifier.fillMaxWidth()) {
        SectionHeading("Achievements", trailing = "${earned.size} of ${all.size}", accent = Brass)
        Spacer(Modifier.height(10.dp))
        if (earned.isEmpty()) {
            Text("Your first session earns the first one.", style = MaterialTheme.typography.bodyMedium, color = Faint)
        } else {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                earned.reversed().forEach { badge ->
                    Text(
                        badge.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = Brass,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brass.copy(alpha = 0.14f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
        if (next.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Text("Next up", style = MaterialTheme.typography.labelSmall, color = Mute)
            next.forEach { badge ->
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(badge.title, style = MaterialTheme.typography.bodyMedium, color = Bone)
                    Text("${(badge.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = Mute)
                }
                Spacer(Modifier.height(4.dp))
                Meter(progress = badge.progress, accent = Brass, height = 4)
            }
        }
    }
}
