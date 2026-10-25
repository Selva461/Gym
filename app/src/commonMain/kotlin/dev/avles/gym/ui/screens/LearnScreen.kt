package dev.avles.gym.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.avles.gym.data.Exercise
import dev.avles.gym.domain.ExerciseGuide
import dev.avles.gym.ui.components.ChipRow
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.EntryRow
import dev.avles.gym.ui.i18n.tr
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Sage

/**
 * Every lift in the catalogue, searchable, each one opening onto its demonstrator.
 *
 * Search matches the name, the muscle group and the muscles the guide says it
 * works, so "glutes" finds hip thrusts even though the word is not in the name.
 * Custom lifts made on the phone are here too; they get their pattern's movement.
 */
@Composable
fun LearnScreen(
    exercises: List<Exercise>,
    onOpen: (Exercise) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf(ALL) }
    val groups = remember(exercises) { listOf(ALL) + exercises.map { it.muscleGroup }.distinct().sorted() }
    // Guides are cheap but not free; worked out once per catalogue, not per keystroke.
    val searchable = remember(exercises) {
        exercises.map { e ->
            val g = ExerciseGuide.of(e)
            Triple(e, g.summary, (listOf(e.name, e.muscleGroup, g.equipment.label) + g.primary.map { it.label }).joinToString(" ").lowercase())
        }
    }
    val matches = remember(searchable, query, group) {
        val words = query.lowercase().split(' ').filter { it.isNotBlank() }
        searchable.filter { (e, _, text) ->
            (group == ALL || e.muscleGroup == group) && words.all { text.contains(it) }
        }.sortedBy { it.first.name }
    }

    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        item {
            Spacer(Modifier.height(16.dp))
            Text(tr("Learn"), style = MaterialTheme.typography.headlineMedium, color = Bone)
            Text(
                "Any lift, shown by a demonstrator you can turn all the way round, a step at a time.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mute
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it.take(60) },
                label = { Text("Search lifts or muscles") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            ChipRow(options = groups, selected = group, accent = Sage, onSelect = { group = it })
            Spacer(Modifier.height(12.dp))
            Text(
                "${matches.size} of ${exercises.size}",
                style = MaterialTheme.typography.labelSmall,
                color = Mute
            )
            Spacer(Modifier.height(6.dp))
        }
        if (matches.isEmpty()) {
            item { EmptyNote(if (exercises.isEmpty()) "The catalogue is loading." else "Nothing matches \"$query\". Try a muscle, like \"chest\".") }
        }
        items(matches, key = { "learn-${it.first.id}" }) { (exercise, summary, _) ->
            EntryRow(
                title = exercise.name,
                detail = summary,
                trailing = "›",
                accent = Brass,
                onClick = { onOpen(exercise) },
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

private const val ALL = "All"
