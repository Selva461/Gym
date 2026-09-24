package dev.avles.gym.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

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
 * lifted. The platform log — logcat on Android, the system log on iOS — is readable
 * by anything holding READ_LOGS on a rooted phone and by anyone with a cable and
 * developer mode on. So in a release build every call here is a branch on a
 * compile-time constant ([isDebugBuild]) that the optimiser folds away, and the ring
 * buffer is never written to. The strings are not
 * shortened, not filtered, not redacted — they are not produced at all.
 *
 * Writing the line is the only platform-specific part, in [writePlatformLog]. On
 * Android, proguard-rules.pro additionally strips `android.util.Log` from release
 * output, so a stray hand-written call elsewhere cannot leak either.
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

    /**
     * Held as an immutable list swapped atomically, rather than a deque behind a
     * lock: @Synchronized is JVM-only, and a debug log of two hundred entries does
     * not need anything cleverer than copy-on-write.
     */
    private val ring = MutableStateFlow<List<Event>>(emptyList())

    /** A snapshot, safe to iterate while the app keeps logging. */
    fun recent(): List<Event> = ring.value

    fun clear() {
        ring.value = emptyList()
    }

    private fun record(event: Event) {
        ring.update { (it + event).takeLast(CAPACITY) }
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
        if (!isDebugBuild) return
        val text = message()
        record(
            Event(
                atMillis = Dates.nowMillis(),
                level = level,
                tag = tag,
                message = text,
                error = error?.let { "${it::class.simpleName}: ${it.message}" }
            )
        )
        writePlatformLog(level, "[$tag] $text", error)
    }
}
