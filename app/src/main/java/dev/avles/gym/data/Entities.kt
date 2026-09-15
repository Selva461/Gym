package dev.avles.gym.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Dates are stored as epoch days (LocalDate.toEpochDay()) so a "day" is a plain
 * integer with no timezone ambiguity. Instants are stored as epoch millis.
 * Nothing here needs a Room TypeConverter, which keeps the schema boring on purpose.
 */

/**
 * The micronutrients worth tracking for someone lifting on a South Indian diet.
 *
 * Fifteen, not fifty. Each one earns its place either because a lifter is plausibly
 * short of it (iron, magnesium, zinc, vitamin D, B12, omega-3), because the diet
 * makes it easy to overshoot (sodium, saturated fat, sugar), or because it is the
 * cheapest signal that the day was built out of food rather than out of macros
 * (fibre, potassium, vitamin C).
 *
 * Stored per unit of the food, exactly like the macros, and summed the same way.
 * [plus] and [times] exist so that the totalling code stays one line instead of
 * fifteen, which is the whole reason this is a value type and not loose columns.
 *
 * Room flattens this into the parent table with @Embedded, so these are real
 * columns named fiberG, sodiumMg and so on — no converters, no JSON blob.
 */
data class Micros(
    val fiberG: Double = 0.0,
    val sugarG: Double = 0.0,
    val satFatG: Double = 0.0,
    val sodiumMg: Double = 0.0,
    val potassiumMg: Double = 0.0,
    val calciumMg: Double = 0.0,
    val ironMg: Double = 0.0,
    val magnesiumMg: Double = 0.0,
    val zincMg: Double = 0.0,
    val vitAMcg: Double = 0.0,
    val vitCMg: Double = 0.0,
    val vitDMcg: Double = 0.0,
    val vitB12Mcg: Double = 0.0,
    val folateMcg: Double = 0.0,
    val omega3G: Double = 0.0
) {
    operator fun plus(o: Micros) = Micros(
        fiberG + o.fiberG,
        sugarG + o.sugarG,
        satFatG + o.satFatG,
        sodiumMg + o.sodiumMg,
        potassiumMg + o.potassiumMg,
        calciumMg + o.calciumMg,
        ironMg + o.ironMg,
        magnesiumMg + o.magnesiumMg,
        zincMg + o.zincMg,
        vitAMcg + o.vitAMcg,
        vitCMg + o.vitCMg,
        vitDMcg + o.vitDMcg,
        vitB12Mcg + o.vitB12Mcg,
        folateMcg + o.folateMcg,
        omega3G + o.omega3G
    )

    operator fun times(q: Double) = Micros(
        fiberG * q,
        sugarG * q,
        satFatG * q,
        sodiumMg * q,
        potassiumMg * q,
        calciumMg * q,
        ironMg * q,
        magnesiumMg * q,
        zincMg * q,
        vitAMcg * q,
        vitCMg * q,
        vitDMcg * q,
        vitB12Mcg * q,
        folateMcg * q,
        omega3G * q
    )
}

@Entity(tableName = "exercise")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val muscleGroup: String,
    val isBodyweight: Boolean = false,
    val isCustom: Boolean = false,
    /**
     * The rep range this lift is progressed in, and the smallest jump worth making
     * on it. A barbell squat moves in 5 kg because that is two 2.5 kg plates and the
     * lift can absorb it; a lateral raise moves in 1 kg because it cannot. Getting
     * this per-exercise is what stops the recommendation being nonsense on isolation
     * work, and it is why these live on the exercise rather than in settings.
     */
    val repLow: Int = 6,
    val repHigh: Int = 10,
    val incrementKg: Double = 2.5
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
    val isCustom: Boolean = false,
    /**
     * A supplement is still a food — it has calories and it carries nutrients — but it
     * is logged from a different mental drawer, so it gets its own flag rather than
     * relying on the category string. It also makes the one honest claim in the whole
     * catalogue: a magnesium tablet's label is a measurement, not an estimate.
     */
    val isSupplement: Boolean = false,
    @Embedded val micros: Micros = Micros()
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
    val fatG: Double,
    val isSupplement: Boolean = false,
    @Embedded val micros: Micros = Micros()
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

    /**
     * Supplements are their own slot. Fish oil at 9 PM is not dinner, and burying it
     * inside a meal makes both the meal's numbers and the supplement's harder to read.
     */
    const val SUPPLEMENT = "Supplements"

    /** Every slot a day's log can be grouped under. */
    val ALL = listOf(BREAKFAST, LUNCH, SNACK, DINNER, SUPPLEMENT)

    /**
     * What the meal picker offers. Supplements are missing on purpose: a food that
     * is a supplement files itself there whatever the picker says, so putting it in
     * the row would be a fifth pill that does nothing but squeeze the other four.
     */
    val PICKABLE = listOf(BREAKFAST, LUNCH, SNACK, DINNER)

    /** Where an entry belongs, which the food itself decides. */
    fun slotFor(isSupplement: Boolean, chosen: String): String =
        if (isSupplement) SUPPLEMENT else chosen
}
