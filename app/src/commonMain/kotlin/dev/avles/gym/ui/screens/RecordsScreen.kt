package dev.avles.gym.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.avles.gym.core.Dates
import dev.avles.gym.data.Exercise
import dev.avles.gym.data.SetRow
import dev.avles.gym.domain.Training
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.EntryRow
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Mute
import kotlinx.coroutines.flow.Flow

/**
 * Every lift's best set, newest record first: the personal records page of the
 * logging apps. Tap one for that lift's whole history.
 */
@Composable
fun RecordsScreen(
    records: List<SetRow>,
    exercises: List<Exercise>,
    liftHistory: (Long) -> Flow<List<SetRow>>,
    modifier: Modifier = Modifier
) {
    var open by remember { mutableStateOf<Exercise?>(null) }

    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        item {
            Spacer(Modifier.height(16.dp))
            Text("Records", style = MaterialTheme.typography.headlineMedium, color = Bone)
            Text(
                "Your best set on every lift, the newest first.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mute
            )
            Spacer(Modifier.height(18.dp))
        }
        if (records.isEmpty()) {
            item { EmptyNote("Log a working set and its lift's record appears here.") }
        }
        items(records, key = { "record-${it.exerciseId}" }) { best ->
            val detail = Dates.dayAndMonth(Dates.ofEpochDay(Dates.epochDayOf(best.loggedAt))) +
                if (best.isAssisted) "   least help" else
                    "   est. max ${Training.formatWeight(Training.estimatedOneRepMax(best.weightKg, best.reps))} kg"
            EntryRow(
                title = best.exerciseName,
                detail = detail,
                trailing = Training.formatSet(best),
                accent = Brass,
                onClick = { open = exercises.firstOrNull { it.id == best.exerciseId } },
                modifier = Modifier.animateItem().padding(bottom = 6.dp)
            )
        }
        item { Spacer(Modifier.height(24.dp)) }
    }

    open?.let { exercise ->
        val history by remember(exercise.id) { liftHistory(exercise.id) }.collectAsStateWithLifecycle(emptyList())
        LiftHistoryDialog(exercise, history, onDismiss = { open = null })
    }
}
