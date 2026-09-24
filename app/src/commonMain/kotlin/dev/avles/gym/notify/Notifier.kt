package dev.avles.gym.notify

/**
 * The two things this app ever tells the phone to show: the end of a rest, and one
 * evening reminder. Each platform schedules them its own way — an alarm and a
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
}
