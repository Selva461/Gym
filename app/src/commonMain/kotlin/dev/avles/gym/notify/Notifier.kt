package dev.avles.gym.notify

/**
 * The things this app tells the phone to show: the end of a rest, the evening
 * reminder, and the water and meal nudges when asked for. Each platform schedules them its own way — an alarm and a
 * broadcast on Android, a calendar trigger on iOS — and the screens neither know
 * nor care which.
 */
interface Notifier {

    /**
     * A rest of [seconds] has begun. iOS suspends a backgrounded app, so the in-app
     * countdown cannot fire anything there; it schedules the alert up front instead.
     * Android keeps running the countdown and ignores this.
     */
    fun restStarted(seconds: Int, exerciseName: String) {}

    /** The rest ended early — skipped, or replaced by a new one. */
    fun restCancelled() {}

    /** The in-app countdown reached zero. Android alerts here; iOS already scheduled it. */
    fun restFinished(exerciseName: String)

    fun scheduleDailyReminder(hour: Int, minute: Int)

    fun cancelDailyReminder()

    /** A nudge to drink every two hours from 10:00 to 20:00. Off unless asked for. */
    fun scheduleWaterReminders() {}

    fun cancelWaterReminders() {}

    /** A nudge to log at breakfast, lunch and dinner ([MEAL_TIMES]). Off unless asked for. */
    fun scheduleMealReminders() {}

    fun cancelMealReminders() {}

    companion object {
        /** Meal, hour, minute: after the meal is usually eaten, not before. */
        val MEAL_TIMES = listOf(Triple("Breakfast", 9, 30), Triple("Lunch", 14, 0), Triple("Dinner", 21, 0))
    }
}
