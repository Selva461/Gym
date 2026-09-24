package dev.avles.gym.core

import android.util.Log
import dev.avles.gym.BuildConfig

internal actual val isDebugBuild: Boolean
    get() = BuildConfig.DEBUG

private const val TAG = "Gym"

internal actual fun writePlatformLog(level: GymLog.Level, line: String, error: Throwable?) {
    when (level) {
        GymLog.Level.DEBUG -> Log.d(TAG, line)
        GymLog.Level.INFO -> Log.i(TAG, line)
        GymLog.Level.WARN -> Log.w(TAG, line)
        GymLog.Level.ERROR -> Log.e(TAG, line, error)
    }
}
