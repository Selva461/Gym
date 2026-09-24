package dev.avles.gym.notify

/**
 * The two things this app ever tells the phone to show: the end of a rest, and one
 * evening reminder. Each platform schedules them its own way — an alarm and a
 * broadcast on Android, a calendar trigger on iOS — and the screens neither know
 * nor care which.
 */
interface Notifier {

    fun restFinished(exerciseName: String)

    fun scheduleDailyReminder(hour: Int, minute: Int)

    fun cancelDailyReminder()
}
