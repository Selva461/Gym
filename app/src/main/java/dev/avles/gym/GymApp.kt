package dev.avles.gym

import android.app.Application
import dev.avles.gym.data.GymDatabase
import dev.avles.gym.data.GymRepository
import dev.avles.gym.data.PhotoStore
import dev.avles.gym.data.SettingsStore
import dev.avles.gym.notify.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Hand-rolled container instead of a DI framework. One app, one user, five screens:
 * a graph builder would be more machinery than the problem deserves.
 */
class GymApp : Application() {

    lateinit var repository: GymRepository
        private set
    lateinit var settings: SettingsStore
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        repository = GymRepository(GymDatabase.get(this).dao(), PhotoStore(this))
        settings = SettingsStore(this)
        Notifications.createChannels(this)

        appScope.launch {
            repository.seedCatalogue()
            // Sweeps up photo files whose row is gone. On the IO scope and off the
            // startup path's critical section — it is a directory listing, and
            // nothing on screen is waiting for it.
            repository.reconcilePhotoFiles()
            val s = settings.settings.first()
            if (s.remindersOn) {
                Notifications.scheduleDailyReminder(this@GymApp, s.reminderHour, s.reminderMinute)
            }
        }
    }
}
