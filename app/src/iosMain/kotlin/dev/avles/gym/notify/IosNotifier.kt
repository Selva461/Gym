package dev.avles.gym.notify

import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNNotificationTrigger
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

/**
 * [Notifier] on iOS. Everything is scheduled with the system ahead of time,
 * because a backgrounded iOS app is suspended and its own timers stop: the rest
 * alert is booked when the rest starts, and the evening reminder is a repeating
 * calendar trigger that fires whether or not the app has run that day.
 */
class IosNotifier : Notifier {

    private val center: UNUserNotificationCenter
        get() = UNUserNotificationCenter.currentNotificationCenter()

    /** Asked once, on first launch. Declining costs the alerts and nothing else. */
    fun requestPermission() {
        center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound) { _, _ -> }
    }

    override fun restStarted(seconds: Int, exerciseName: String) {
        if (seconds <= 0) return
        post(
            id = REST,
            title = "Rest is up",
            body = "Next set of $exerciseName.",
            trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(seconds.toDouble(), repeats = false)
        )
    }

    override fun restCancelled() {
        center.removePendingNotificationRequestsWithIdentifiers(listOf(REST))
    }

    /** Already booked by [restStarted]; posting again would alert twice. */
    override fun restFinished(exerciseName: String) = Unit

    override fun scheduleDailyReminder(hour: Int, minute: Int) {
        val time = NSDateComponents()
        time.hour = hour.toLong()
        time.minute = minute.toLong()
        post(
            id = DAILY,
            title = "Close out today",
            body = "Log what's left of today's food and tonight's session.",
            trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(time, repeats = true)
        )
    }

    override fun cancelDailyReminder() {
        center.removePendingNotificationRequestsWithIdentifiers(listOf(DAILY))
    }

    override fun scheduleWaterReminders() {
        WATER_HOURS.forEach { hour ->
            val time = NSDateComponents()
            time.hour = hour.toLong()
            time.minute = 0
            post(
                id = "$WATER$hour",
                title = "Water",
                body = "A glass now. Tap + on the water card once you have.",
                trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(time, repeats = true)
            )
        }
    }

    override fun cancelWaterReminders() {
        center.removePendingNotificationRequestsWithIdentifiers(WATER_HOURS.map { "$WATER$it" })
    }

    /** Posting under an identifier already pending replaces it, which is what both callers want. */
    private fun post(id: String, title: String, body: String, trigger: UNNotificationTrigger) {
        val content = UNMutableNotificationContent()
        content.setTitle(title)
        content.setBody(body)
        content.setSound(UNNotificationSound.defaultSound)
        center.addNotificationRequest(
            UNNotificationRequest.requestWithIdentifier(id, content, trigger),
            withCompletionHandler = null
        )
    }

    private companion object {
        const val REST = "rest"
        const val DAILY = "daily"
        const val WATER = "water-"
        val WATER_HOURS = listOf(10, 12, 14, 16, 18, 20)
    }
}
