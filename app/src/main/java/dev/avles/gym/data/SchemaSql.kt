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
}
