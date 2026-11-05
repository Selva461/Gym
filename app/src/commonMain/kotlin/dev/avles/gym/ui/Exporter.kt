package dev.avles.gym.ui

import dev.avles.gym.core.Dates
import dev.avles.gym.data.GymRepository
import dev.avles.gym.data.SettingsStore
import dev.avles.gym.domain.AiExport
import dev.avles.gym.domain.Export
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Reads the log and formats it for leaving the phone: the per-table CSVs and the
 * single "for AI" file. Kept out of [GymViewModel] so the view model only routes
 * taps here; formatting runs on [Dispatchers.Default] because a year of sets and
 * meals is megabytes of text and building it on the main thread would stutter.
 */
internal class Exporter(private val repo: GymRepository, private val settings: SettingsStore) {

    suspend fun csv(kind: GymViewModel.ExportKind): String = when (kind) {
        GymViewModel.ExportKind.SETS -> repo.everySet().let { withContext(Dispatchers.Default) { Export.sets(it) } }
        GymViewModel.ExportKind.FOOD -> repo.allFoodLog().let { withContext(Dispatchers.Default) { Export.food(it) } }
        GymViewModel.ExportKind.WEIGHTS -> Export.weights(repo.weights.first())
    }

    /** The file name and the Markdown text of the whole log, prompt first. */
    suspend fun forAi(): Pair<String, String> {
        val log = repo.fullLog(settings.settings.first())
        val today = Dates.today()
        return AiExport.fileName(today) to withContext(Dispatchers.Default) { AiExport.build(log, today) }
    }
}
