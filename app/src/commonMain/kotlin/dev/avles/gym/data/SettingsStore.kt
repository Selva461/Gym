package dev.avles.gym.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
    val goalId: String = "balanced",
    /** Daily water target in 250 ml glasses. */
    val waterGlasses: Int = 8,
    /** When the running fast began, epoch millis; zero when not fasting. */
    val fastStartedAt: Long = 0L,
    /** How long a fast is meant to run, in hours. */
    val fastHours: Int = 16,
    /** For the body-fat estimate; zero until entered. */
    val heightCm: Double = 0.0,
    val isFemale: Boolean = false,
    /** Rest longer after heavy compounds and shorter after isolation lifts. */
    val restByLift: Boolean = true,
    /** Target body weight; zero for none. */
    val goalWeightKg: Double = 0.0,
    /** The first-run welcome has been answered or dismissed. */
    val welcomed: Boolean = false
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
        val water = intPreferencesKey("water_glasses")
        val fastStart = longPreferencesKey("fast_started_at")
        val fastHours = intPreferencesKey("fast_hours")
        val height = doublePreferencesKey("height_cm")
        val female = intPreferencesKey("is_female")
        val restByLift = intPreferencesKey("rest_by_lift")
        val goalWeight = doublePreferencesKey("goal_weight_kg")
        val welcomed = intPreferencesKey("welcomed")
    }

    val settings: Flow<GymSettings> = store.data.map { p ->
        GymSettings(
            proteinTargetG = p[Keys.protein] ?: 150.0,
            kcalTarget = p[Keys.kcal] ?: 2100.0,
            restSeconds = p[Keys.rest] ?: 120,
            reminderHour = p[Keys.hour] ?: 21,
            reminderMinute = p[Keys.minute] ?: 45,
            remindersOn = (p[Keys.on] ?: 1) == 1,
            goalId = p[Keys.goal] ?: "balanced",
            waterGlasses = p[Keys.water] ?: 8,
            fastStartedAt = p[Keys.fastStart] ?: 0L,
            fastHours = p[Keys.fastHours] ?: 16,
            heightCm = p[Keys.height] ?: 0.0,
            isFemale = (p[Keys.female] ?: 0) == 1,
            restByLift = (p[Keys.restByLift] ?: 1) == 1,
            goalWeightKg = p[Keys.goalWeight] ?: 0.0,
            welcomed = (p[Keys.welcomed] ?: 0) == 1
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
            p[Keys.water] = s.waterGlasses
            p[Keys.fastStart] = s.fastStartedAt
            p[Keys.fastHours] = s.fastHours
            p[Keys.height] = s.heightCm
            p[Keys.female] = if (s.isFemale) 1 else 0
            p[Keys.restByLift] = if (s.restByLift) 1 else 0
            p[Keys.goalWeight] = s.goalWeightKg
            p[Keys.welcomed] = if (s.welcomed) 1 else 0
        }
    }
}
