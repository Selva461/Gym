package dev.avles.gym.core

/**
 * True in a debug build. A constant per build, so that release code branching on it
 * is removed rather than skipped.
 */
internal expect val isDebugBuild: Boolean

/** Writes one line to the platform's own log: logcat on Android, the system log on iOS. */
internal expect fun writePlatformLog(level: GymLog.Level, line: String, error: Throwable?)
