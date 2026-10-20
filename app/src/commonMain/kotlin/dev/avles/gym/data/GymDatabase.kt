package dev.avles.gym.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [
        Exercise::class,
        WorkoutSession::class,
        SetEntry::class,
        FoodItem::class,
        FoodLog::class,
        BodyWeight::class,
        Split::class,
        SplitExercise::class,
        ProgressPhoto::class,
        ExercisePhoto::class,
        DayMarker::class,
        BodyMeasurement::class
    ],
    version = 10,
    exportSchema = true
)
@ConstructedBy(GymDatabaseConstructor::class)
abstract class GymDatabase : RoomDatabase() {

    abstract fun dao(): GymDao

    companion object {

        /**
         * v1 → v2: micronutrients on food, progression ranges on exercises.
         *
         * Written out rather than reached for `fallbackToDestructiveMigration`, which
         * would have been two lines and would have deleted every set and meal already
         * logged. A schema change is not a good enough reason to throw away someone's
         * training history.
         *
         * Every column is NOT NULL with a default, so existing rows are valid the
         * moment they are added — which is also why this needs no data backfill. A
         * zero micronutrient on a food seeded under v1 reads as "not estimated",
         * exactly as it does for a food seeded under v2.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                val foodColumns = listOf(
                    "isSupplement INTEGER NOT NULL DEFAULT 0",
                    "fiberG REAL NOT NULL DEFAULT 0",
                    "sugarG REAL NOT NULL DEFAULT 0",
                    "satFatG REAL NOT NULL DEFAULT 0",
                    "sodiumMg REAL NOT NULL DEFAULT 0",
                    "potassiumMg REAL NOT NULL DEFAULT 0",
                    "calciumMg REAL NOT NULL DEFAULT 0",
                    "ironMg REAL NOT NULL DEFAULT 0",
                    "magnesiumMg REAL NOT NULL DEFAULT 0",
                    "zincMg REAL NOT NULL DEFAULT 0",
                    "vitAMcg REAL NOT NULL DEFAULT 0",
                    "vitCMg REAL NOT NULL DEFAULT 0",
                    "vitDMcg REAL NOT NULL DEFAULT 0",
                    "vitB12Mcg REAL NOT NULL DEFAULT 0",
                    "folateMcg REAL NOT NULL DEFAULT 0",
                    "omega3G REAL NOT NULL DEFAULT 0"
                )
                foodColumns.forEach { connection.execSQL("ALTER TABLE food_item ADD COLUMN $it") }

                listOf(
                    "repLow INTEGER NOT NULL DEFAULT 6",
                    "repHigh INTEGER NOT NULL DEFAULT 10",
                    "incrementKg REAL NOT NULL DEFAULT 2.5"
                ).forEach { connection.execSQL("ALTER TABLE exercise ADD COLUMN $it") }
            }
        }

        /**
         * v2 → v3: saved workout splits.
         *
         * The statements live in [SchemaSql] rather than inline, so that a plain JVM
         * unit test can compare them against the schema Room exported at build time.
         * Room validates the on-disk schema against its own on every open, and a
         * hand-written CREATE TABLE that differs by a word is a crash on launch for
         * anyone upgrading with data — which is exactly the failure a migration exists
         * to prevent. MigrationSqlTest is what keeps these honest.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                SchemaSql.V3_STATEMENTS.forEach { connection.execSQL(it) }
            }
        }

        /**
         * v3 → v4: how a set was performed.
         *
         * One column, and deliberately nothing else. No backfill runs: sets logged
         * before this existed become unspecified rather than "standard grip", because
         * the app has no idea which grip they used and guessing would put invented
         * data into a training history. Progression treats unspecified as its own
         * bucket for the same reason.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                SchemaSql.V4_STATEMENTS.forEach { connection.execSQL(it) }
            }
        }

        /**
         * v4 → v5: progress photos, and a picture per lift.
         *
         * Two new tables and nothing touched, so there is no way for this one to
         * lose data. The pictures themselves are files in the app's private
         * directory; these tables only name them.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(connection: SQLiteConnection) {
                SchemaSql.V5_STATEMENTS.forEach { connection.execSQL(it) }
            }
        }

        /**
         * v5 → v6: rest days and missed days.
         *
         * One new table, nothing touched. A day with no marker is exactly what it
         * was before — unknown — so nothing already logged changes meaning.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(connection: SQLiteConnection) {
                SchemaSql.V6_STATEMENTS.forEach { connection.execSQL(it) }
            }
        }

        /**
         * v6 → v7: other names for a food, and where its numbers came from.
         *
         * Two columns with empty defaults. Every food already in the database gets
         * no aliases and no stated source, which is accurate: nobody recorded either.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(connection: SQLiteConnection) {
                SchemaSql.V7_STATEMENTS.forEach { connection.execSQL(it) }
            }
        }

        /**
         * v7 → v8: which lifts are assisted, where more weight is easier. One column,
         * then the existing rows flagged by name, so history already logged on an
         * assisted machine is read the right way round straight away.
         */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(connection: SQLiteConnection) {
                SchemaSql.V8_STATEMENTS.forEach { connection.execSQL(it) }
            }
        }

        /**
         * v8 → v9: a table for tape measurements. New and empty, so nothing already
         * logged is touched.
         */
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(connection: SQLiteConnection) {
                SchemaSql.V9_STATEMENTS.forEach { connection.execSQL(it) }
            }
        }

        /**
         * v9 → v10: a set's kind (drop set, to failure) and its RPE. Two columns with
         * defaults that say "a straight set, effort not recorded", which is exactly
         * what every set logged before this was.
         */
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(connection: SQLiteConnection) {
                SchemaSql.V10_STATEMENTS.forEach { connection.execSQL(it) }
            }
        }

        val MIGRATIONS = arrayOf(
            MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
            MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10
        )

        /** The file name, the same on both platforms; only the directory differs. */
        const val FILE_NAME = "gym.db"

        /**
         * Finishes a builder the platform started with its own file location.
         *
         * The bundled driver means the SQLite underneath is the one compiled into the
         * app, not whichever version the phone shipped with, so a query behaves the
         * same on a five-year-old Android as on a new iPhone. Queries run on the IO
         * dispatcher, never on the thread that draws.
         */
        fun build(builder: RoomDatabase.Builder<GymDatabase>): GymDatabase =
            builder
                .addMigrations(*MIGRATIONS)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
    }
}

/** Filled in by Room's code generator for each platform. */
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object GymDatabaseConstructor : RoomDatabaseConstructor<GymDatabase> {
    override fun initialize(): GymDatabase
}
