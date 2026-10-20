package dev.avles.gym.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.avles.gym.GymApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Alarms do not survive a reboot, so put the nightly nudge back. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        val app = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val s = (app as GymApp).container.settings.settings.first()
                if (s.remindersOn) {
                    Notifications.scheduleDailyReminder(app, s.reminderHour, s.reminderMinute)
                }
                if (s.waterReminders) Notifications.scheduleWaterReminders(app)
            } finally {
                pending.finish()
            }
        }
    }
}
