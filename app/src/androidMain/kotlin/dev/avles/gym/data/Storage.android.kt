package dev.avles.gym.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room

/**
 * Where the database and the settings live on Android: exactly the files every
 * earlier version of the app wrote, so an update carries everything across.
 */
fun buildDatabase(context: Context): GymDatabase {
    val app = context.applicationContext
    return GymDatabase.build(
        Room.databaseBuilder<GymDatabase>(
            context = app,
            name = app.getDatabasePath(GymDatabase.FILE_NAME).absolutePath
        )
    )
}

/** One instance per process, which is what the delegate guarantees and DataStore requires. */
val Context.gymPreferences: DataStore<Preferences> by preferencesDataStore("gym_settings")
