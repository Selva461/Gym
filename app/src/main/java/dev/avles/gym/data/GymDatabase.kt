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
        SplitExercise::class
    ],
    version = 3,
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
         * The CREATE TABLE statements are written to match what Room generates for
         * these entities exactly — backticked identifiers, AUTOINCREMENT on the
         * primary key, and no DEFAULT clauses, because Room only emits a default
         * when @ColumnInfo(defaultValue = ...) says so. Room validates the schema it
         * finds against the one it expects on every open, so a near-miss here is a
         * crash on launch rather than a subtle bug. The index names follow Room's own
         * index_<table>_<column> convention for the same reason.
         *
         * splitId is added nullable with no default: sessions logged before splits
         * existed genuinely have no split, and that is not the same as zero.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `split` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`position` INTEGER NOT NULL, " +
                        "`isCustom` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `split_exercise` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`splitId` INTEGER NOT NULL, " +
                        "`exerciseId` INTEGER NOT NULL, " +
                        "`position` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_split_exercise_splitId` " +
                        "ON `split_exercise` (`splitId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_split_exercise_exerciseId` " +
                        "ON `split_exercise` (`exerciseId`)"
                )
                db.execSQL("ALTER TABLE `workout_session` ADD COLUMN `splitId` INTEGER")
            }
        }

        @Volatile private var instance: GymDatabase? = null

        fun get(context: Context): GymDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                GymDatabase::class.java,
                "gym.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                .also { instance = it }
        }
    }
}
