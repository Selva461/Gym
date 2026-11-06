package dev.avles.gym.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * The week plan: what you mean to train and eat on each day, imported from the
 * plan spreadsheet (see dev.avles.gym.domain.plan). It sits beside the log, never
 * inside it. Nothing here counts as done until it is logged the normal way, so a
 * plan can be replaced wholesale without touching a single set or meal.
 *
 * Rows refer to the catalogue by id when the import found a match and always keep
 * the name as written, so a plan still reads correctly if a food is later renamed
 * or an exercise deleted. Dates are epoch days, like everywhere else.
 */

/** One planned day: its focus ("Legs", "Push", "Rest") and when the workout is meant to start. */
@Entity(tableName = "plan_day", indices = [Index(value = ["dateEpochDay"], unique = true)])
data class PlanDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val focus: String,
    /** "18:00", or empty when no time was planned. */
    val workoutTime: String = "",
    val notes: String = ""
)

/**
 * One planned exercise. Reps are a range ([repsLow] to [repsHigh], equal for a fixed
 * number); a timed hold such as a plank has zero reps and [holdSeconds] instead.
 * [weightKg] and [restSeconds] are zero when the plan left them open.
 */
@Entity(tableName = "plan_lift", indices = [Index("dateEpochDay")])
data class PlanLift(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val position: Int,
    val exerciseName: String,
    val exerciseId: Long? = null,
    val sets: Int,
    val repsLow: Int = 0,
    val repsHigh: Int = 0,
    val holdSeconds: Int = 0,
    val weightKg: Double = 0.0,
    val restSeconds: Int = 0,
    val notes: String = ""
)

/**
 * One planned food in a meal slot ([Meals]). [quantity] counts the food's own unit
 * when it matched the catalogue; [kcal] and [proteinG] are for the whole quantity,
 * worked out from the catalogue on import, or taken from the sheet for a food the
 * app does not know. [time] is "13:00" or empty for the slot's usual time.
 */
@Entity(tableName = "plan_food", indices = [Index("dateEpochDay")])
data class PlanFood(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val position: Int,
    val meal: String,
    val foodName: String,
    val foodId: Long? = null,
    val quantity: Double,
    val kcal: Double = 0.0,
    val proteinG: Double = 0.0,
    val time: String = "",
    val notes: String = ""
)

/** One day of the plan with everything on it. [day] is null when only lifts or foods were planned. */
data class DayPlan(
    val dateEpochDay: Long,
    val day: PlanDay?,
    val lifts: List<PlanLift>,
    val foods: List<PlanFood>
) {
    val focus: String get() = day?.focus.orEmpty()
    val isEmpty: Boolean get() = focus.isEmpty() && lifts.isEmpty() && foods.isEmpty()
}
