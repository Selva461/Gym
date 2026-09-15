package dev.avles.gym.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Dates are stored as epoch days (LocalDate.toEpochDay()) so a "day" is a plain
 * integer with no timezone ambiguity. Instants are stored as epoch millis.
 * Nothing here needs a Room TypeConverter, which keeps the schema boring on purpose.
 */

@Entity(tableName = "exercise")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val muscleGroup: String,
    val isBodyweight: Boolean = false,
    val isCustom: Boolean = false
)

@Entity(tableName = "workout_session", indices = [Index("dateEpochDay")])
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val label: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val notes: String = ""
)

@Entity(tableName = "set_entry", indices = [Index("sessionId"), Index("exerciseId")])
data class SetEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val setIndex: Int,
    val weightKg: Double,
    val reps: Int,
    val isWarmup: Boolean = false,
    val loggedAt: Long
)

@Entity(tableName = "food_item")
data class FoodItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** What one unit of this food is, in the words you'd actually use: "1 idli", "100 g", "1 scoop (30 g)". */
    val unitLabel: String,
    val kcal: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val category: String,
    val isCustom: Boolean = false
)

@Entity(tableName = "food_log", indices = [Index("dateEpochDay"), Index("foodId")])
data class FoodLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val foodId: Long,
    val meal: String,
    val quantity: Double,
    val loggedAt: Long
)

@Entity(tableName = "body_weight", indices = [Index(value = ["dateEpochDay"], unique = true)])
data class BodyWeight(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val weightKg: Double,
    val note: String = ""
)

/** Joined row for the food log list: entry plus the food it points at. */
data class FoodLogRow(
    val logId: Long,
    val dateEpochDay: Long,
    val meal: String,
    val quantity: Double,
    val loggedAt: Long,
    val foodId: Long,
    val name: String,
    val unitLabel: String,
    val kcal: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double
)

/** Joined row for a logged set: the set plus its exercise name. */
data class SetRow(
    val setId: Long,
    val sessionId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val setIndex: Int,
    val weightKg: Double,
    val reps: Int,
    val isWarmup: Boolean,
    val loggedAt: Long
)

data class DayVolume(val dateEpochDay: Long, val volume: Double)

object Meals {
    const val BREAKFAST = "Breakfast"
    const val LUNCH = "Lunch"
    const val DINNER = "Dinner"
    const val SNACK = "Snack"
    val ALL = listOf(BREAKFAST, LUNCH, SNACK, DINNER)
}
