package dev.avles.gym

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import dev.avles.gym.ui.GymNav
import dev.avles.gym.ui.GymViewModel
import dev.avles.gym.ui.theme.GymTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GymViewModel by viewModels {
        GymViewModel.factory(application)
    }

    private val requestNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* either way, carry on */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        askForNotificationsOnce()
        setContent {
            GymTheme {
                GymNav(viewModel)
            }
        }
    }

    /**
     * Asked once, on first launch, and never nagged again. Declining costs the
     * rest timer's buzz and the nightly nudge; everything else keeps working.
     */
    private fun askForNotificationsOnce() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
