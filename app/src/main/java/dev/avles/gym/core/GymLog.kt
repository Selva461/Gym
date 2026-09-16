package dev.avles.gym.core

import android.util.Log
import dev.avles.gym.BuildConfig

/**
 * The one place this app is allowed to log, and the one place it keeps a record of
 * what it has been doing.
 *
 * Two jobs, and they pull in opposite directions:
 *
 * **Debuggable by a human.** When the Train tab closed the app, diagnosing it took a
 * screenshot, a question and a guess. There was no record of what the app had just
 * done. [recent] fixes that: every event goes into a small in-memory ring that the
 * debug build can put on screen, so the next time something misbehaves the answer is
 * in the app rather than in a reconstruction.
 *
 * **Silent in release.** This app holds body weights, what you ate and what you
 * lifted. `android.util.Log` writes to logcat, which is readable by anything holding
 * READ_LOGS on a rooted phone and by anyone with a USB cable and developer mode on.
 * So in a release build every call here is a branch on a compile-time constant that
 * R8 folds away, and the ring buffer is never written to. The strings are not
 * shortened, not filtered, not redacted — they are not produced at all.
 *
 * [proguard-rules.pro] additionally strips `android.util.Log` from release output, so
 * a stray hand-written call outside this class cannot leak either.
 */
object GymLog {

    /** Where an event came from. A short, stable string beats a class name R8 will rename. */
    object Tag {
        const val DB = "db"
        const val SEED = "seed"
        const val TRAIN = "train"
        const val FUEL = "fuel"
        const val NOTIFY = "notify"
        const val UI = "ui"
        const val BACKUP = "backup"
    }

    enum class Level { DEBUG, INFO, WARN, ERROR }

    /**
     * One thing that happened. [atMillis] is wall-clock rather than uptime because
     * the point is to line an event up against "when I tapped the thing".
     */
    data class Event(
        val atMillis: Long,
        val level: Level,
        val tag: String,
        val message: String,
        val error: String? = null
    )

    /**
     * The last [CAPACITY] events, oldest first. Bounded on purpose: an unbounded log
     * in a long-running app is a memory leak that only shows up after a week of use.
     *
     * Always empty in release. Reading it is safe either way.
     */
    private const val CAPACITY = 200
    private val ring = ArrayDeque<Event>(CAPACITY)

    /** A snapshot, safe to iterate while the app keeps logging. */
    @Synchronized
    fun recent(): List<Event> = ring.toList()

    @Synchronized
    fun clear() = ring.clear()

    @Synchronized
    private fun record(event: Event) {
        if (ring.size >= CAPACITY) ring.removeFirst()
        ring.addLast(event)
    }

    /**
     * Messages are taken as lambdas so that building the string — which may mean
     * formatting numbers or walking a list — does not happen at all in release.
     * A non-lambda signature would still cost the concatenation before the call.
     */
    fun d(tag: String, message: () -> String) = log(Level.DEBUG, tag, message, null)
    fun i(tag: String, message: () -> String) = log(Level.INFO, tag, message, null)
    fun w(tag: String, message: () -> String) = log(Level.WARN, tag, message, null)

    /**
     * Errors carry the throwable's type and message but never its stack trace into
     * the ring: a trace is long enough that a handful would push every other event
     * out of a 200-entry buffer, which is the opposite of what the buffer is for.
     * The full trace still goes to logcat in debug.
     */
    fun e(tag: String, error: Throwable? = null, message: () -> String) =
        log(Level.ERROR, tag, message, error)

    private fun log(level: Level, tag: String, message: () -> String, error: Throwable?) {
        if (!BuildConfig.DEBUG) return
        val text = message()
        record(
            Event(
                atMillis = System.currentTimeMillis(),
                level = level,
                tag = tag,
                message = text,
                error = error?.let { "${it::class.simpleName}: ${it.message}" }
            )
        )
        val full = "[$tag] $text"
        when (level) {
            Level.DEBUG -> Log.d(TAG, full)
            Level.INFO -> Log.i(TAG, full)
            Level.WARN -> Log.w(TAG, full)
            Level.ERROR -> Log.e(TAG, full, error)
        }
    }

    private const val TAG = "Gym"
}
