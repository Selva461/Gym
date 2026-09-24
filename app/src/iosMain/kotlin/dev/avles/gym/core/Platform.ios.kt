package dev.avles.gym.core

import platform.Foundation.NSLog
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
internal actual val isDebugBuild: Boolean
    get() = kotlin.native.Platform.isDebugBinary

internal actual fun writePlatformLog(level: GymLog.Level, line: String, error: Throwable?) {
    val detail = error?.let { " (${it::class.simpleName}: ${it.message})" }.orEmpty()
    // NSLog reads its argument as a format string, so a stray % in a message would
    // be taken for a specifier and read memory that was never passed.
    NSLog("Gym ${level.name} $line$detail".replace("%", "%%"))
}
