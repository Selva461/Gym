package dev.avles.gym.data

/**
 * The exact DDL the v3 migration runs.
 *
 * This lives in its own file, holding nothing but strings, for one reason: Room
 * validates the schema it finds on disk against the one it expects on *every* open,
 * so a hand-written CREATE TABLE that differs from Room's own by a word is a crash
 * on launch for anyone upgrading with data. Keeping the statements here, away from
 * any Android type, means a plain JVM unit test can load them and compare them
 * against the schema Room actually exported at build time — see MigrationSqlTest.
 *
 * Matching Room means: backticked identifiers, `INTEGER PRIMARY KEY AUTOINCREMENT
 * NOT NULL` for an autoGenerate Long key, `TEXT`/`INTEGER`/`REAL` affinities, and no
 * DEFAULT clause, because Room only emits one when @ColumnInfo(defaultValue = ...)
 * asks for it. Kotlin default values are invisible to the schema.
 */
object SchemaSql {

    const val CREATE_SPLIT =
        "CREATE TABLE IF NOT EXISTS `split` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`name` TEXT NOT NULL, " +
            "`position` INTEGER NOT NULL, " +
            "`isCustom` INTEGER NOT NULL)"

    const val CREATE_SPLIT_EXERCISE =
        "CREATE TABLE IF NOT EXISTS `split_exercise` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`splitId` INTEGER NOT NULL, " +
            "`exerciseId` INTEGER NOT NULL, " +
            "`position` INTEGER NOT NULL)"

    const val INDEX_SPLIT_EXERCISE_SPLIT_ID =
        "CREATE INDEX IF NOT EXISTS `index_split_exercise_splitId` " +
            "ON `split_exercise` (`splitId`)"

    const val INDEX_SPLIT_EXERCISE_EXERCISE_ID =
        "CREATE INDEX IF NOT EXISTS `index_split_exercise_exerciseId` " +
            "ON `split_exercise` (`exerciseId`)"

    /** Nullable, no default: a session logged before splits existed has no split. */
    const val ADD_SESSION_SPLIT_ID =
        "ALTER TABLE `workout_session` ADD COLUMN `splitId` INTEGER"

    /** Every statement the v3 migration runs, in order. */
    val V3_STATEMENTS = listOf(
        CREATE_SPLIT,
        CREATE_SPLIT_EXERCISE,
        INDEX_SPLIT_EXERCISE_SPLIT_ID,
        INDEX_SPLIT_EXERCISE_EXERCISE_ID,
        ADD_SESSION_SPLIT_ID
    )

    // ---- v4: how a set was performed ---------------------------------------

    /**
     * Grip, stance or tempo on a logged set.
     *
     * NOT NULL with an empty default, which is the only shape SQLite will accept for
     * a column added to a table that already has rows — and it is also the honest
     * value. Every set logged before this column existed keeps a meaning the app can
     * defend: not "standard grip", which would be inventing data, but "nobody said".
     *
     * The DEFAULT clause has to survive into Room's own expected schema or the two
     * disagree and Room throws on every launch after the upgrade. That is what the
     * @ColumnInfo(defaultValue = "''") on SetEntry.variation is for, and
     * [EXPECTED_SET_ENTRY_VARIATION_COLUMN] is what MigrationSqlTest checks it
     * against in the exported schema.
     */
    const val ADD_SET_VARIATION =
        "ALTER TABLE `set_entry` ADD COLUMN `variation` TEXT NOT NULL DEFAULT ''"

    /**
     * How Room spells that column in the schema it exports. If these two ever drift
     * apart the app crashes on launch for anyone with data, so the test compares
     * them rather than trusting that both were written by someone paying attention.
     */
    const val EXPECTED_SET_ENTRY_VARIATION_COLUMN =
        "`variation` TEXT NOT NULL DEFAULT ''"

    /** Every statement the v4 migration runs, in order. */
    val V4_STATEMENTS = listOf(ADD_SET_VARIATION)

    // ---- v5: pictures ------------------------------------------------------
    //
    // New tables rather than added columns, so there is no DEFAULT question here:
    // Room emits no DEFAULT clause for a Kotlin default value, and on a CREATE that
    // matches, because there are no existing rows for one to apply to.

    const val CREATE_PROGRESS_PHOTO =
        "CREATE TABLE IF NOT EXISTS `progress_photo` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`dateEpochDay` INTEGER NOT NULL, " +
            "`fileName` TEXT NOT NULL, " +
            "`label` TEXT NOT NULL, " +
            "`note` TEXT NOT NULL, " +
            "`takenAt` INTEGER NOT NULL)"

    const val INDEX_PROGRESS_PHOTO_DATE =
        "CREATE INDEX IF NOT EXISTS `index_progress_photo_dateEpochDay` " +
            "ON `progress_photo` (`dateEpochDay`)"

    const val CREATE_EXERCISE_PHOTO =
        "CREATE TABLE IF NOT EXISTS `exercise_photo` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`exerciseId` INTEGER NOT NULL, " +
            "`fileName` TEXT NOT NULL, " +
            "`addedAt` INTEGER NOT NULL)"

    /** Unique, because a lift has one picture. Room spells a unique index this way. */
    const val INDEX_EXERCISE_PHOTO_EXERCISE_ID =
        "CREATE UNIQUE INDEX IF NOT EXISTS `index_exercise_photo_exerciseId` " +
            "ON `exercise_photo` (`exerciseId`)"

    // ---- v6: what a day was, when it was not a training day ----------------

    const val CREATE_DAY_MARKER =
        "CREATE TABLE IF NOT EXISTS `day_marker` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`dateEpochDay` INTEGER NOT NULL, " +
            "`kind` TEXT NOT NULL, " +
            "`note` TEXT NOT NULL, " +
            "`markedAt` INTEGER NOT NULL)"

    /** Unique: a day is one thing. */
    const val INDEX_DAY_MARKER_DATE =
        "CREATE UNIQUE INDEX IF NOT EXISTS `index_day_marker_dateEpochDay` " +
            "ON `day_marker` (`dateEpochDay`)"

    /** Every statement the v6 migration runs, in order. */
    val V6_STATEMENTS = listOf(CREATE_DAY_MARKER, INDEX_DAY_MARKER_DATE)

    // ---- v7: alternative names and provenance on foods --------------------

    const val ADD_FOOD_ALIASES =
        "ALTER TABLE `food_item` ADD COLUMN `aliases` TEXT NOT NULL DEFAULT ''"

    const val ADD_FOOD_SOURCE =
        "ALTER TABLE `food_item` ADD COLUMN `source` TEXT NOT NULL DEFAULT ''"

    /** How Room spells these in its exported schema, for MigrationSqlTest. */
    const val EXPECTED_FOOD_ALIASES_COLUMN = "`aliases` TEXT NOT NULL DEFAULT ''"
    const val EXPECTED_FOOD_SOURCE_COLUMN = "`source` TEXT NOT NULL DEFAULT ''"

    /** Every statement the v7 migration runs, in order. */
    val V7_STATEMENTS = listOf(ADD_FOOD_ALIASES, ADD_FOOD_SOURCE)

    // ---- v8: lifts whose weight is help, not load ---------------------------

    const val ADD_EXERCISE_ASSISTED =
        "ALTER TABLE `exercise` ADD COLUMN `isAssisted` INTEGER NOT NULL DEFAULT 0"

    /**
     * Existing lifts are flagged by the same rule new ones are
     * ([dev.avles.gym.domain.Assistance.looksAssisted]): by name. That covers the
     * two seeded machines and any assisted lift you added yourself, and history
     * already logged on them is read the right way round from the first launch.
     */
    const val FLAG_ASSISTED_EXERCISES =
        "UPDATE `exercise` SET `isAssisted` = 1 WHERE LOWER(`name`) LIKE '%assisted%'"

    const val EXPECTED_EXERCISE_ASSISTED_COLUMN = "`isAssisted` INTEGER NOT NULL DEFAULT 0"

    val V8_STATEMENTS = listOf(ADD_EXERCISE_ASSISTED, FLAG_ASSISTED_EXERCISES)

    // ---- v9: tape measurements ----------------------------------------------

    const val CREATE_BODY_MEASUREMENT =
        "CREATE TABLE IF NOT EXISTS `body_measurement` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`dateEpochDay` INTEGER NOT NULL, " +
            "`part` TEXT NOT NULL, " +
            "`cm` REAL NOT NULL)"

    /** Unique on the pair: one figure per part per day. */
    const val INDEX_BODY_MEASUREMENT_DATE_PART =
        "CREATE UNIQUE INDEX IF NOT EXISTS `index_body_measurement_dateEpochDay_part` " +
            "ON `body_measurement` (`dateEpochDay`, `part`)"

    val V9_STATEMENTS = listOf(CREATE_BODY_MEASUREMENT, INDEX_BODY_MEASUREMENT_DATE_PART)

    // ---- v10: drop sets, sets to failure, and RPE ---------------------------

    const val ADD_SET_KIND = "ALTER TABLE `set_entry` ADD COLUMN `kind` TEXT NOT NULL DEFAULT ''"
    const val ADD_SET_RPE = "ALTER TABLE `set_entry` ADD COLUMN `rpe` REAL NOT NULL DEFAULT 0"

    const val EXPECTED_SET_KIND_COLUMN = "`kind` TEXT NOT NULL DEFAULT ''"
    const val EXPECTED_SET_RPE_COLUMN = "`rpe` REAL NOT NULL DEFAULT 0"

    val V10_STATEMENTS = listOf(ADD_SET_KIND, ADD_SET_RPE)

    // ---- v11: your own notes on a lift --------------------------------------

    const val ADD_EXERCISE_NOTES = "ALTER TABLE `exercise` ADD COLUMN `notes` TEXT NOT NULL DEFAULT ''"
    const val EXPECTED_EXERCISE_NOTES_COLUMN = "`notes` TEXT NOT NULL DEFAULT ''"

    val V11_STATEMENTS = listOf(ADD_EXERCISE_NOTES)

    // ---- v12: saved meals ---------------------------------------------------

    const val CREATE_SAVED_MEAL =
        "CREATE TABLE IF NOT EXISTS `saved_meal` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`name` TEXT NOT NULL, " +
            "`createdAt` INTEGER NOT NULL)"

    const val CREATE_SAVED_MEAL_ITEM =
        "CREATE TABLE IF NOT EXISTS `saved_meal_item` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`mealId` INTEGER NOT NULL, " +
            "`foodId` INTEGER NOT NULL, " +
            "`quantity` REAL NOT NULL)"

    const val INDEX_SAVED_MEAL_ITEM_MEAL =
        "CREATE INDEX IF NOT EXISTS `index_saved_meal_item_mealId` ON `saved_meal_item` (`mealId`)"

    val V12_STATEMENTS = listOf(CREATE_SAVED_MEAL, CREATE_SAVED_MEAL_ITEM, INDEX_SAVED_MEAL_ITEM_MEAL)

    /** Every statement the v5 migration runs, in order. */
    val V5_STATEMENTS = listOf(
        CREATE_PROGRESS_PHOTO,
        INDEX_PROGRESS_PHOTO_DATE,
        CREATE_EXERCISE_PHOTO,
        INDEX_EXERCISE_PHOTO_EXERCISE_ID
    )
}
