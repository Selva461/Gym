package dev.avles.gym.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Notifications.ACTION_WATER -> Notifications.waterReminder(context)
            Notifications.ACTION_MEAL -> Notifications.mealReminder(context, intent.getStringExtra(Notifications.EXTRA_MEAL) ?: "")
            else -> Notifications.dailyReminder(context)
        }
    }
}
