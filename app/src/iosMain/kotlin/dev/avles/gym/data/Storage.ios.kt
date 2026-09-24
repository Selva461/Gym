@file:OptIn(ExperimentalForeignApi::class)

package dev.avles.gym.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import okio.Path.Companion.toPath
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.posix.memcpy

/**
 * The app's private directory on iOS: Application Support, which the Files app
 * never shows and the user never browses. Created on first use.
 */
internal fun appDataDirectory(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )
    return requireNotNull(url?.path) { "iOS gave no Application Support directory" }
}

/** The same database file name as Android, in the iOS app's own directory. */
fun buildDatabase(): GymDatabase = GymDatabase.build(
    Room.databaseBuilder<GymDatabase>(name = "${appDataDirectory()}/${GymDatabase.FILE_NAME}")
)

fun createPreferences(): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
    produceFile = { "${appDataDirectory()}/gym_settings.preferences_pb".toPath() }
)

/** An NSData's bytes as a Kotlin array, copied once. */
internal fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val out = ByteArray(size)
    if (size > 0) out.usePinned { memcpy(it.addressOf(0), bytes, length) }
    return out
}
