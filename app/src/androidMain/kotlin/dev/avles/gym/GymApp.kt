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

    /**
     * Lazy rather than set in [onCreate], because [dev.avles.gym.share.TrainingProvider]
     * can be asked for data before onCreate has run: Android starts content providers
     * first. Whoever reaches for the container first builds it, exactly once.
     */
    val container: AppContainer by lazy {
        AppContainer.create(
            database = buildDatabase(this),
            preferences = gymPreferences,
            photos = AndroidPhotoStore(this),
            notifier = AndroidNotifier(this)
        )
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Notifications.createChannels(this)
        // Seeding, the photo sweep and the reminder are all off the startup path's
        // critical section: nothing on screen is waiting for them.
        appScope.launch { container.startUp() }
    }
}
