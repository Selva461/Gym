package dev.avles.gym.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.avles.gym.AppContainer
import dev.avles.gym.ui.theme.GymTheme

/**
 * The whole app as one composable, which is what each platform puts on screen.
 *
 * The view model is scoped to whatever owns this composition — the activity on
 * Android, the view controller on iOS — so it outlives a rotation and dies with
 * the app, and neither platform has to know how it is built.
 */
@Composable
fun GymRoot(container: AppContainer) {
    val vm = viewModel {
        GymViewModel(container.repository, container.settings, container.notifier)
    }
    GymTheme {
        GymNav(vm)
    }
}
