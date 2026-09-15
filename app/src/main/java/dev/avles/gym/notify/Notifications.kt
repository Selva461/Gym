package dev.avles.gym.notify

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dev.avles.gym.MainActivity
import dev.avles.gym.R
import java.util.Calendar

object Notifications {

    const val CHANNEL_REST = "gym_rest"
    const val CHANNEL_REMINDER = "gym_reminder"

    private const val ID_REST = 1001
    private const val ID_REMINDER = 1002
    private const val REQUEST_DAILY = 2001

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REST,
                "Rest timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Fires when a rest period between sets runs out."
                enableVibration(true)
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REMINDER,
                "Daily log reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "A nightly nudge to close out the day's food and training."
            }
        )
    }

    private fun openAppIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun canPost(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    fun restFinished(context: Context, exerciseName: String) {
        if (!canPost(context)) return
        val n = NotificationCompat.Builder(context, CHANNEL_REST)
            .setSmallIcon(R.drawable.ic_stat_gym)
            .setContentTitle("Rest is up")
            .setContentText("Next set of $exerciseName.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))
            .build()
        NotificationManagerCompat.from(context).notify(ID_REST, n)
    }

    fun dailyReminder(context: Context) {
        if (!canPost(context)) return
        val n = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_stat_gym)
            .setContentTitle("Close out today")
            .setContentText("Log what's left of today's food and tonight's session.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))
            .build()
        NotificationManagerCompat.from(context).notify(ID_REMINDER, n)
    }

    /**
     * Inexact on purpose. A nightly nudge does not justify asking for the exact
     * alarm permission, and drifting a few minutes past 21:45 costs nothing.
     */
    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarms.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            next.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            reminderPendingIntent(context)
        )
    }

    fun cancelDailyReminder(context: Context) {
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarms.cancel(reminderPendingIntent(context))
    }

    private fun reminderPendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_DAILY,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}
