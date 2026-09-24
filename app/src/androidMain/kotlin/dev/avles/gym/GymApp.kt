package dev.avles.gym

import android.app.Application
import dev.avles.gym.data.AndroidPhotoStore
import dev.avles.gym.data.buildDatabase
import dev.avles.gym.data.gymPreferences
import dev.avles.gym.notify.AndroidNotifier
import dev.avles.gym.notify.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * The Android entry point. Makes the four things only Android can make — the
 * database file, the settings file, the photo store and the notifier — and hands
 * them to [AppContainer], which assembles the rest the same way iOS does.
 */
class GymApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Notifications.createChannels(this)
        container = AppContainer.create(
            database = buildDatabase(this),
            preferences = gymPreferences,
            photos = AndroidPhotoStore(this),
            notifier = AndroidNotifier(this)
        )
        // Seeding, the photo sweep and the reminder are all off the startup path's
        // critical section: nothing on screen is waiting for them.
        appScope.launch { container.startUp() }
    }
}
