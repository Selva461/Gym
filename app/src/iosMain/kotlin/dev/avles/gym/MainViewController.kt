package dev.avles.gym

import androidx.compose.ui.window.ComposeUIViewController
import dev.avles.gym.data.IosPhotoStore
import dev.avles.gym.data.buildDatabase
import dev.avles.gym.data.createPreferences
import dev.avles.gym.notify.IosNotifier
import dev.avles.gym.ui.GymRoot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.UIKit.UIViewController

/**
 * The iOS counterpart of GymApp: makes the four platform parts once for the life
 * of the process and hands them to [AppContainer], which builds the rest exactly
 * as it does on Android.
 */
private object IosApp {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val container: AppContainer by lazy {
        val notifier = IosNotifier()
        AppContainer.create(
            database = buildDatabase(),
            preferences = createPreferences(),
            photos = IosPhotoStore(),
            notifier = notifier
        ).also { built ->
            notifier.requestPermission()
            scope.launch { built.startUp() }
        }
    }
}

/** Called from Swift: the whole app as one view controller. */
fun MainViewController(): UIViewController = ComposeUIViewController { GymRoot(IosApp.container) }
