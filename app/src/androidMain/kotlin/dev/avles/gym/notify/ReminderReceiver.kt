package dev.avles.gym.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Notifications.ACTION_WATER) Notifications.waterReminder(context)
        else Notifications.dailyReminder(context)
    }
}
