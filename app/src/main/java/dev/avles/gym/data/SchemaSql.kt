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
}
