package dev.avles.gym.notify

import android.content.Context

/** [Notifier] on Android: [Notifications] with the application context bound in. */
class AndroidNotifier(private val context: Context) : Notifier {

    override fun restFinished(exerciseName: String) =
        Notifications.restFinished(context, exerciseName)

    override fun scheduleDailyReminder(hour: Int, minute: Int) =
        Notifications.scheduleDailyReminder(context, hour, minute)

    override fun cancelDailyReminder() = Notifications.cancelDailyReminder(context)

    override fun scheduleWaterReminders() = Notifications.scheduleWaterReminders(context)

    override fun cancelWaterReminders() = Notifications.cancelWaterReminders(context)

    override fun scheduleMealReminders() = Notifications.scheduleMealReminders(context)

    override fun cancelMealReminders() = Notifications.cancelMealReminders(context)
}
