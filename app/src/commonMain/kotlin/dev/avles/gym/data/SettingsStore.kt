package dev.avles.gym.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class GymSettings(
    val proteinTargetG: Double = 150.0,
    val kcalTarget: Double = 2100.0,
    val restSeconds: Int = 120,
    val reminderHour: Int = 21,
    val reminderMinute: Int = 45,
    val remindersOn: Boolean = true,
    /**
     * Which [dev.avles.gym.domain.Physique] the coach measures against. Stored as
     * its id rather than its index, so reordering the list later cannot silently
     * change somebody's goal.
     */
    val goalId: String = "balanced"
)

class SettingsStore(private val store: DataStore<Preferences>) {

    private object Keys {
        val protein = doublePreferencesKey("protein_target_g")
        val kcal = doublePreferencesKey("kcal_target")
        val rest = intPreferencesKey("rest_seconds")
        val hour = intPreferencesKey("reminder_hour")
        val minute = intPreferencesKey("reminder_minute")
        val on = intPreferencesKey("reminders_on")
        val goal = stringPreferencesKey("goal_id")
    }

    val settings: Flow<GymSettings> = store.data.map { p ->
        GymSettings(
            proteinTargetG = p[Keys.protein] ?: 150.0,
            kcalTarget = p[Keys.kcal] ?: 2100.0,
            restSeconds = p[Keys.rest] ?: 120,
            reminderHour = p[Keys.hour] ?: 21,
            reminderMinute = p[Keys.minute] ?: 45,
            remindersOn = (p[Keys.on] ?: 1) == 1,
            goalId = p[Keys.goal] ?: "balanced"
        )
    }

    suspend fun save(s: GymSettings) {
        store.edit { p ->
            p[Keys.protein] = s.proteinTargetG
            p[Keys.kcal] = s.kcalTarget
            p[Keys.rest] = s.restSeconds
            p[Keys.hour] = s.reminderHour
            p[Keys.minute] = s.reminderMinute
            p[Keys.on] = if (s.remindersOn) 1 else 0
            p[Keys.goal] = s.goalId
        }
    }
}
