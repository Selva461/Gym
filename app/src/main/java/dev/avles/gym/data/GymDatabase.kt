package dev.avles.gym.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
        DayMarker::class
    ],
    version = 6,
    exportSchema = true
)
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
            override fun migrate(db: SupportSQLiteDatabase) {
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
                foodColumns.forEach { db.execSQL("ALTER TABLE food_item ADD COLUMN $it") }

                listOf(
                    "repLow INTEGER NOT NULL DEFAULT 6",
                    "repHigh INTEGER NOT NULL DEFAULT 10",
                    "incrementKg REAL NOT NULL DEFAULT 2.5"
                ).forEach { db.execSQL("ALTER TABLE exercise ADD COLUMN $it") }
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
            override fun migrate(db: SupportSQLiteDatabase) {
                SchemaSql.V3_STATEMENTS.forEach(db::execSQL)
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
            override fun migrate(db: SupportSQLiteDatabase) {
                SchemaSql.V4_STATEMENTS.forEach(db::execSQL)
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
            override fun migrate(db: SupportSQLiteDatabase) {
                SchemaSql.V5_STATEMENTS.forEach(db::execSQL)
            }
        }

        /**
         * v5 → v6: rest days and missed days.
         *
         * One new table, nothing touched. A day with no marker is exactly what it
         * was before — unknown — so nothing already logged changes meaning.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                SchemaSql.V6_STATEMENTS.forEach(db::execSQL)
            }
        }

        @Volatile private var instance: GymDatabase? = null

        fun get(context: Context): GymDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                GymDatabase::class.java,
                "gym.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .build()
                .also { instance = it }
        }
    }
}
