package dev.avles.gym

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.avles.gym.data.FoodDataset
import dev.avles.gym.data.GymDatabase
import dev.avles.gym.data.GymRepository
import dev.avles.gym.data.PhotoStore
import dev.avles.gym.data.SettingsStore
import dev.avles.gym.notify.Notifier
import dev.avles.gym.resources.Res
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Everything the app is built from, wired once.
 *
 * A hand-rolled container rather than a DI framework: one app, one user, a handful
 * of screens, and a graph builder would be more machinery than the problem needs.
 * Each platform's entry point — GymApp on Android, MainViewController on iOS —
 * supplies the four things only it can make, and everything else is assembled here,
 * identically on both.
 */
class AppContainer(
    /** Held only for [dev.avles.gym.data.Backup]; everything else goes through [repository]. */
    val database: GymDatabase,
    val repository: GymRepository,
    val settings: SettingsStore,
    val notifier: Notifier
) {

    /**
     * Work done once per launch, off the thread that draws: top up the catalogue,
     * sweep photo files nothing points at, and re-arm the reminders.
     */
    suspend fun startUp() {
        repository.seedCatalogue()
        repository.reconcilePhotoFiles()
        val s = settings.settings.first()
        if (s.remindersOn) notifier.scheduleDailyReminder(s.reminderHour, s.reminderMinute)
        if (s.waterReminders) notifier.scheduleWaterReminders()
        if (s.mealReminders) notifier.scheduleMealReminders()
    }

    companion object {

        @OptIn(ExperimentalResourceApi::class)
        fun create(
            database: GymDatabase,
            preferences: DataStore<Preferences>,
            photos: PhotoStore,
            notifier: Notifier
        ): AppContainer = AppContainer(
            database = database,
            repository = GymRepository(
                dao = database.dao(),
                photos = photos,
                bundledFoods = { Res.readBytes("files/${FoodDataset.ASSET_PATH}").decodeToString() }
            ),
            settings = SettingsStore(preferences),
            notifier = notifier
        )
    }
}
