package dev.avles.gym.data

import androidx.room.ColumnInfo
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
    val notes: String = "",
    /**
     * The split this session was started from, if any. Nullable because a session
     * can still be started ad hoc, and because every session logged before splits
     * existed has no answer to this question.
     */
    val splitId: Long? = null
)

/**
 * A named workout day — "Push A", "Legs heavy", whatever you actually call it —
 * holding the lifts you do on it, in order.
 *
 * The point is to stop choosing. Starting a session from a split means its lifts
 * are already listed on the Train screen, one tap each, instead of being hunted
 * out of a catalogue of eighty between two sets.
 */
@Entity(tableName = "split")
data class Split(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val position: Int = 0,
    val isCustom: Boolean = false
)

/**
 * One lift's place in one split. A join table rather than a list column, so that
 * renaming or deleting a lift cannot leave a split holding a dangling string.
 */
@Entity(
    tableName = "split_exercise",
    indices = [Index("splitId"), Index("exerciseId")]
)
data class SplitExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val splitId: Long,
    val exerciseId: Long,
    val position: Int = 0
)

/**
 * A split with its lifts resolved and in order.
 *
 * Assembled in Kotlin from three flows rather than with @Relation, because the
 * order matters and a junction relation does not preserve it. The exercise
 * catalogue is already in memory, so this costs a map lookup per entry.
 */
data class SplitPlan(
    val split: Split,
    val exercises: List<Exercise>
) {
    val id: Long get() = split.id
    val name: String get() = split.name
}

@Entity(tableName = "set_entry", indices = [Index("sessionId"), Index("exerciseId")])
data class SetEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val setIndex: Int,
    val weightKg: Double,
    val reps: Int,
    val isWarmup: Boolean = false,
    val loggedAt: Long,
    /**
     * How this set was performed, when it differs enough from the default to be
     * worth its own history — "Close grip", "Paused", "Sumo". An attribute of the
     * set rather than a new exercise, so that the catalogue stays 394 rows instead
     * of exploding into every combination of lift and grip.
     *
     * Empty means unspecified, and that is its own bucket rather than a wildcard.
     * Every set logged before variations existed is unspecified and stays that way:
     * the app does not know what grip those sets used, and quietly relabelling them
     * "standard" would be inventing data. See [dev.avles.gym.domain.Progression] for
     * what that means for the next-set recommendation.
     *
     * @ColumnInfo(defaultValue) is load-bearing, not decoration. SQLite cannot add a
     * NOT NULL column without a default, so the migration's ALTER has to supply one —
     * and Room only writes a DEFAULT into its expected schema when this annotation
     * asks for it. Without the annotation the migration and Room's expectation differ
     * by that clause, and Room throws on launch for everyone upgrading with data.
     */
    @ColumnInfo(defaultValue = "''") val variation: String = ""
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
    val loggedAt: Long,
    /**
     * Encoded by [dev.avles.gym.domain.Variations] — several axes at once, so that
     * close grip *and* paused is expressible. Formatted for display there too,
     * rather than here, so there is one implementation of what it reads as.
     */
    val variation: String = ""
)

data class DayVolume(val dateEpochDay: Long, val volume: Double)

/** Set count and working volume per session, for the recent-sessions list. */
data class SessionSummary(val sessionId: Long, val setCount: Int, val volume: Double)

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

/**
 * A progress photo: one picture, one day.
 *
 * Only the *name* of the file is stored, never a content:// URI from the picker.
 * Those are revocable — the grant dies with the process — and they point at
 * another app's storage, so a row holding one would work today and show nothing
 * next week. The bytes are copied into this app's private directory instead, and
 * this row names the copy.
 *
 * Several per day is deliberate: front, side and back is how people actually take
 * these, and forcing one would make the second overwrite the first.
 */
@Entity(tableName = "progress_photo", indices = [Index("dateEpochDay")])
data class ProgressPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val fileName: String,
    /** "Front", "Side", "Back" — free text, because it is your shorthand, not a schema. */
    val label: String = "",
    val note: String = "",
    val takenAt: Long
)

/**
 * A picture you attached to a lift — the legal way to get the demo image you
 * wanted into the app, since it cannot download one and would not ship somebody
 * else's licensed animation.
 *
 * Unique on exerciseId: one picture per lift. A gallery per exercise would be a
 * different feature, and the thing being solved here is "show me which way this
 * moves", which one picture does.
 */
@Entity(tableName = "exercise_photo", indices = [Index(value = ["exerciseId"], unique = true)])
data class ExercisePhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val fileName: String,
    val addedAt: Long
)

/**
 * What a day was, when it was not a training day.
 *
 * Two different things get recorded here and the difference matters. A **rest**
 * day is planned recovery — part of a training programme, not an absence from it.
 * A **missed** day is one you meant to train and could not: ill, travelling, work
 * ran late. Both are worth knowing three months later when you are looking at a
 * gap in the chart and wondering what happened.
 *
 * [kind] is a string rather than an enum column so that adding "deload" or
 * "injured" later is data rather than a migration. The values live in [DayKinds].
 *
 * Unique on the date: a day is one thing. Logging a session on a day marked rest
 * is not forbidden — plans change, and an app that argues with you about what you
 * actually did is worse than one that records it.
 */
@Entity(tableName = "day_marker", indices = [Index(value = ["dateEpochDay"], unique = true)])
data class DayMarker(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val kind: String,
    val note: String = "",
    val markedAt: Long
)

object DayKinds {
    /** Planned recovery. Bridges a training streak rather than breaking it. */
    const val REST = "Rest"

    /** Meant to train, could not. Recorded honestly; does not bridge. */
    const val MISSED = "Missed"

    val ALL = listOf(REST, MISSED)

    fun bridgesStreak(kind: String): Boolean = kind == REST
}

/** A session's id and the day it happened, for windowing weekly volume. */
data class SessionDay(val id: Long, val dateEpochDay: Long)

/** A day's eating, summed in SQL. Days with nothing logged simply do not appear. */
data class DayMacroRow(val dateEpochDay: Long, val kcal: Double, val proteinG: Double)
