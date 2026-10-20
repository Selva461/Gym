package dev.avles.gym.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class GymDatabase_Impl : GymDatabase() {
  private val _gymDao: Lazy<GymDao> = lazy {
    GymDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(12,
        "42217f036b20720b3472ae428673da25", "3aa7fed716e8515eb52e0f05ab49ce47") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `exercise` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `muscleGroup` TEXT NOT NULL, `isBodyweight` INTEGER NOT NULL, `isCustom` INTEGER NOT NULL, `repLow` INTEGER NOT NULL, `repHigh` INTEGER NOT NULL, `incrementKg` REAL NOT NULL, `isAssisted` INTEGER NOT NULL DEFAULT 0, `notes` TEXT NOT NULL DEFAULT '')")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `workout_session` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dateEpochDay` INTEGER NOT NULL, `label` TEXT NOT NULL, `startedAt` INTEGER NOT NULL, `endedAt` INTEGER, `notes` TEXT NOT NULL, `splitId` INTEGER)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_session_dateEpochDay` ON `workout_session` (`dateEpochDay`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `set_entry` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `exerciseId` INTEGER NOT NULL, `setIndex` INTEGER NOT NULL, `weightKg` REAL NOT NULL, `reps` INTEGER NOT NULL, `isWarmup` INTEGER NOT NULL, `loggedAt` INTEGER NOT NULL, `variation` TEXT NOT NULL DEFAULT '', `kind` TEXT NOT NULL DEFAULT '', `rpe` REAL NOT NULL DEFAULT 0)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_set_entry_sessionId` ON `set_entry` (`sessionId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_set_entry_exerciseId` ON `set_entry` (`exerciseId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `food_item` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `unitLabel` TEXT NOT NULL, `kcal` REAL NOT NULL, `proteinG` REAL NOT NULL, `carbsG` REAL NOT NULL, `fatG` REAL NOT NULL, `category` TEXT NOT NULL, `isCustom` INTEGER NOT NULL, `isSupplement` INTEGER NOT NULL, `aliases` TEXT NOT NULL DEFAULT '', `source` TEXT NOT NULL DEFAULT '', `fiberG` REAL NOT NULL, `sugarG` REAL NOT NULL, `satFatG` REAL NOT NULL, `sodiumMg` REAL NOT NULL, `potassiumMg` REAL NOT NULL, `calciumMg` REAL NOT NULL, `ironMg` REAL NOT NULL, `magnesiumMg` REAL NOT NULL, `zincMg` REAL NOT NULL, `vitAMcg` REAL NOT NULL, `vitCMg` REAL NOT NULL, `vitDMcg` REAL NOT NULL, `vitB12Mcg` REAL NOT NULL, `folateMcg` REAL NOT NULL, `omega3G` REAL NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `food_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dateEpochDay` INTEGER NOT NULL, `foodId` INTEGER NOT NULL, `meal` TEXT NOT NULL, `quantity` REAL NOT NULL, `loggedAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_food_log_dateEpochDay` ON `food_log` (`dateEpochDay`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_food_log_foodId` ON `food_log` (`foodId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `body_weight` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dateEpochDay` INTEGER NOT NULL, `weightKg` REAL NOT NULL, `note` TEXT NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_body_weight_dateEpochDay` ON `body_weight` (`dateEpochDay`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `split` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `position` INTEGER NOT NULL, `isCustom` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `split_exercise` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `splitId` INTEGER NOT NULL, `exerciseId` INTEGER NOT NULL, `position` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_split_exercise_splitId` ON `split_exercise` (`splitId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_split_exercise_exerciseId` ON `split_exercise` (`exerciseId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `progress_photo` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dateEpochDay` INTEGER NOT NULL, `fileName` TEXT NOT NULL, `label` TEXT NOT NULL, `note` TEXT NOT NULL, `takenAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_progress_photo_dateEpochDay` ON `progress_photo` (`dateEpochDay`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `exercise_photo` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `exerciseId` INTEGER NOT NULL, `fileName` TEXT NOT NULL, `addedAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_exercise_photo_exerciseId` ON `exercise_photo` (`exerciseId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `day_marker` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dateEpochDay` INTEGER NOT NULL, `kind` TEXT NOT NULL, `note` TEXT NOT NULL, `markedAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_day_marker_dateEpochDay` ON `day_marker` (`dateEpochDay`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `body_measurement` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dateEpochDay` INTEGER NOT NULL, `part` TEXT NOT NULL, `cm` REAL NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_body_measurement_dateEpochDay_part` ON `body_measurement` (`dateEpochDay`, `part`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `saved_meal` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `saved_meal_item` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `mealId` INTEGER NOT NULL, `foodId` INTEGER NOT NULL, `quantity` REAL NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_saved_meal_item_mealId` ON `saved_meal_item` (`mealId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '42217f036b20720b3472ae428673da25')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `exercise`")
        connection.execSQL("DROP TABLE IF EXISTS `workout_session`")
        connection.execSQL("DROP TABLE IF EXISTS `set_entry`")
        connection.execSQL("DROP TABLE IF EXISTS `food_item`")
        connection.execSQL("DROP TABLE IF EXISTS `food_log`")
        connection.execSQL("DROP TABLE IF EXISTS `body_weight`")
        connection.execSQL("DROP TABLE IF EXISTS `split`")
        connection.execSQL("DROP TABLE IF EXISTS `split_exercise`")
        connection.execSQL("DROP TABLE IF EXISTS `progress_photo`")
        connection.execSQL("DROP TABLE IF EXISTS `exercise_photo`")
        connection.execSQL("DROP TABLE IF EXISTS `day_marker`")
        connection.execSQL("DROP TABLE IF EXISTS `body_measurement`")
        connection.execSQL("DROP TABLE IF EXISTS `saved_meal`")
        connection.execSQL("DROP TABLE IF EXISTS `saved_meal_item`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsExercise: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsExercise.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExercise.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExercise.put("muscleGroup", TableInfo.Column("muscleGroup", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExercise.put("isBodyweight", TableInfo.Column("isBodyweight", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercise.put("isCustom", TableInfo.Column("isCustom", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExercise.put("repLow", TableInfo.Column("repLow", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExercise.put("repHigh", TableInfo.Column("repHigh", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExercise.put("incrementKg", TableInfo.Column("incrementKg", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExercise.put("isAssisted", TableInfo.Column("isAssisted", "INTEGER", true, 0, "0",
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExercise.put("notes", TableInfo.Column("notes", "TEXT", true, 0, "''",
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysExercise: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesExercise: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoExercise: TableInfo = TableInfo("exercise", _columnsExercise, _foreignKeysExercise,
            _indicesExercise)
        val _existingExercise: TableInfo = read(connection, "exercise")
        if (!_infoExercise.equals(_existingExercise)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |exercise(dev.avles.gym.data.Exercise).
              | Expected:
              |""".trimMargin() + _infoExercise + """
              |
              | Found:
              |""".trimMargin() + _existingExercise)
        }
        val _columnsWorkoutSession: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWorkoutSession.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSession.put("dateEpochDay", TableInfo.Column("dateEpochDay", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSession.put("label", TableInfo.Column("label", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSession.put("startedAt", TableInfo.Column("startedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSession.put("endedAt", TableInfo.Column("endedAt", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSession.put("notes", TableInfo.Column("notes", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSession.put("splitId", TableInfo.Column("splitId", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWorkoutSession: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWorkoutSession: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesWorkoutSession.add(TableInfo.Index("index_workout_session_dateEpochDay", false,
            listOf("dateEpochDay"), listOf("ASC")))
        val _infoWorkoutSession: TableInfo = TableInfo("workout_session", _columnsWorkoutSession,
            _foreignKeysWorkoutSession, _indicesWorkoutSession)
        val _existingWorkoutSession: TableInfo = read(connection, "workout_session")
        if (!_infoWorkoutSession.equals(_existingWorkoutSession)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |workout_session(dev.avles.gym.data.WorkoutSession).
              | Expected:
              |""".trimMargin() + _infoWorkoutSession + """
              |
              | Found:
              |""".trimMargin() + _existingWorkoutSession)
        }
        val _columnsSetEntry: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSetEntry.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSetEntry.put("sessionId", TableInfo.Column("sessionId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSetEntry.put("exerciseId", TableInfo.Column("exerciseId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSetEntry.put("setIndex", TableInfo.Column("setIndex", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSetEntry.put("weightKg", TableInfo.Column("weightKg", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSetEntry.put("reps", TableInfo.Column("reps", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSetEntry.put("isWarmup", TableInfo.Column("isWarmup", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSetEntry.put("loggedAt", TableInfo.Column("loggedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSetEntry.put("variation", TableInfo.Column("variation", "TEXT", true, 0, "''",
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSetEntry.put("kind", TableInfo.Column("kind", "TEXT", true, 0, "''",
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSetEntry.put("rpe", TableInfo.Column("rpe", "REAL", true, 0, "0",
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSetEntry: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSetEntry: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSetEntry.add(TableInfo.Index("index_set_entry_sessionId", false,
            listOf("sessionId"), listOf("ASC")))
        _indicesSetEntry.add(TableInfo.Index("index_set_entry_exerciseId", false,
            listOf("exerciseId"), listOf("ASC")))
        val _infoSetEntry: TableInfo = TableInfo("set_entry", _columnsSetEntry,
            _foreignKeysSetEntry, _indicesSetEntry)
        val _existingSetEntry: TableInfo = read(connection, "set_entry")
        if (!_infoSetEntry.equals(_existingSetEntry)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |set_entry(dev.avles.gym.data.SetEntry).
              | Expected:
              |""".trimMargin() + _infoSetEntry + """
              |
              | Found:
              |""".trimMargin() + _existingSetEntry)
        }
        val _columnsFoodItem: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFoodItem.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("unitLabel", TableInfo.Column("unitLabel", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("kcal", TableInfo.Column("kcal", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("proteinG", TableInfo.Column("proteinG", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("carbsG", TableInfo.Column("carbsG", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("fatG", TableInfo.Column("fatG", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("category", TableInfo.Column("category", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("isCustom", TableInfo.Column("isCustom", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("isSupplement", TableInfo.Column("isSupplement", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("aliases", TableInfo.Column("aliases", "TEXT", true, 0, "''",
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("source", TableInfo.Column("source", "TEXT", true, 0, "''",
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("fiberG", TableInfo.Column("fiberG", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("sugarG", TableInfo.Column("sugarG", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("satFatG", TableInfo.Column("satFatG", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("sodiumMg", TableInfo.Column("sodiumMg", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("potassiumMg", TableInfo.Column("potassiumMg", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("calciumMg", TableInfo.Column("calciumMg", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("ironMg", TableInfo.Column("ironMg", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("magnesiumMg", TableInfo.Column("magnesiumMg", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("zincMg", TableInfo.Column("zincMg", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("vitAMcg", TableInfo.Column("vitAMcg", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("vitCMg", TableInfo.Column("vitCMg", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("vitDMcg", TableInfo.Column("vitDMcg", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("vitB12Mcg", TableInfo.Column("vitB12Mcg", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("folateMcg", TableInfo.Column("folateMcg", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodItem.put("omega3G", TableInfo.Column("omega3G", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFoodItem: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFoodItem: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoFoodItem: TableInfo = TableInfo("food_item", _columnsFoodItem,
            _foreignKeysFoodItem, _indicesFoodItem)
        val _existingFoodItem: TableInfo = read(connection, "food_item")
        if (!_infoFoodItem.equals(_existingFoodItem)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |food_item(dev.avles.gym.data.FoodItem).
              | Expected:
              |""".trimMargin() + _infoFoodItem + """
              |
              | Found:
              |""".trimMargin() + _existingFoodItem)
        }
        val _columnsFoodLog: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFoodLog.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodLog.put("dateEpochDay", TableInfo.Column("dateEpochDay", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodLog.put("foodId", TableInfo.Column("foodId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodLog.put("meal", TableInfo.Column("meal", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodLog.put("quantity", TableInfo.Column("quantity", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFoodLog.put("loggedAt", TableInfo.Column("loggedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFoodLog: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFoodLog: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesFoodLog.add(TableInfo.Index("index_food_log_dateEpochDay", false,
            listOf("dateEpochDay"), listOf("ASC")))
        _indicesFoodLog.add(TableInfo.Index("index_food_log_foodId", false, listOf("foodId"),
            listOf("ASC")))
        val _infoFoodLog: TableInfo = TableInfo("food_log", _columnsFoodLog, _foreignKeysFoodLog,
            _indicesFoodLog)
        val _existingFoodLog: TableInfo = read(connection, "food_log")
        if (!_infoFoodLog.equals(_existingFoodLog)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |food_log(dev.avles.gym.data.FoodLog).
              | Expected:
              |""".trimMargin() + _infoFoodLog + """
              |
              | Found:
              |""".trimMargin() + _existingFoodLog)
        }
        val _columnsBodyWeight: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsBodyWeight.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBodyWeight.put("dateEpochDay", TableInfo.Column("dateEpochDay", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBodyWeight.put("weightKg", TableInfo.Column("weightKg", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBodyWeight.put("note", TableInfo.Column("note", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBodyWeight: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesBodyWeight: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesBodyWeight.add(TableInfo.Index("index_body_weight_dateEpochDay", true,
            listOf("dateEpochDay"), listOf("ASC")))
        val _infoBodyWeight: TableInfo = TableInfo("body_weight", _columnsBodyWeight,
            _foreignKeysBodyWeight, _indicesBodyWeight)
        val _existingBodyWeight: TableInfo = read(connection, "body_weight")
        if (!_infoBodyWeight.equals(_existingBodyWeight)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |body_weight(dev.avles.gym.data.BodyWeight).
              | Expected:
              |""".trimMargin() + _infoBodyWeight + """
              |
              | Found:
              |""".trimMargin() + _existingBodyWeight)
        }
        val _columnsSplit: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSplit.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSplit.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSplit.put("position", TableInfo.Column("position", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSplit.put("isCustom", TableInfo.Column("isCustom", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSplit: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSplit: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSplit: TableInfo = TableInfo("split", _columnsSplit, _foreignKeysSplit,
            _indicesSplit)
        val _existingSplit: TableInfo = read(connection, "split")
        if (!_infoSplit.equals(_existingSplit)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |split(dev.avles.gym.data.Split).
              | Expected:
              |""".trimMargin() + _infoSplit + """
              |
              | Found:
              |""".trimMargin() + _existingSplit)
        }
        val _columnsSplitExercise: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSplitExercise.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSplitExercise.put("splitId", TableInfo.Column("splitId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSplitExercise.put("exerciseId", TableInfo.Column("exerciseId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSplitExercise.put("position", TableInfo.Column("position", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSplitExercise: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSplitExercise: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSplitExercise.add(TableInfo.Index("index_split_exercise_splitId", false,
            listOf("splitId"), listOf("ASC")))
        _indicesSplitExercise.add(TableInfo.Index("index_split_exercise_exerciseId", false,
            listOf("exerciseId"), listOf("ASC")))
        val _infoSplitExercise: TableInfo = TableInfo("split_exercise", _columnsSplitExercise,
            _foreignKeysSplitExercise, _indicesSplitExercise)
        val _existingSplitExercise: TableInfo = read(connection, "split_exercise")
        if (!_infoSplitExercise.equals(_existingSplitExercise)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |split_exercise(dev.avles.gym.data.SplitExercise).
              | Expected:
              |""".trimMargin() + _infoSplitExercise + """
              |
              | Found:
              |""".trimMargin() + _existingSplitExercise)
        }
        val _columnsProgressPhoto: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsProgressPhoto.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressPhoto.put("dateEpochDay", TableInfo.Column("dateEpochDay", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressPhoto.put("fileName", TableInfo.Column("fileName", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressPhoto.put("label", TableInfo.Column("label", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressPhoto.put("note", TableInfo.Column("note", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProgressPhoto.put("takenAt", TableInfo.Column("takenAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysProgressPhoto: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesProgressPhoto: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesProgressPhoto.add(TableInfo.Index("index_progress_photo_dateEpochDay", false,
            listOf("dateEpochDay"), listOf("ASC")))
        val _infoProgressPhoto: TableInfo = TableInfo("progress_photo", _columnsProgressPhoto,
            _foreignKeysProgressPhoto, _indicesProgressPhoto)
        val _existingProgressPhoto: TableInfo = read(connection, "progress_photo")
        if (!_infoProgressPhoto.equals(_existingProgressPhoto)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |progress_photo(dev.avles.gym.data.ProgressPhoto).
              | Expected:
              |""".trimMargin() + _infoProgressPhoto + """
              |
              | Found:
              |""".trimMargin() + _existingProgressPhoto)
        }
        val _columnsExercisePhoto: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsExercisePhoto.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExercisePhoto.put("exerciseId", TableInfo.Column("exerciseId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercisePhoto.put("fileName", TableInfo.Column("fileName", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExercisePhoto.put("addedAt", TableInfo.Column("addedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysExercisePhoto: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesExercisePhoto: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesExercisePhoto.add(TableInfo.Index("index_exercise_photo_exerciseId", true,
            listOf("exerciseId"), listOf("ASC")))
        val _infoExercisePhoto: TableInfo = TableInfo("exercise_photo", _columnsExercisePhoto,
            _foreignKeysExercisePhoto, _indicesExercisePhoto)
        val _existingExercisePhoto: TableInfo = read(connection, "exercise_photo")
        if (!_infoExercisePhoto.equals(_existingExercisePhoto)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |exercise_photo(dev.avles.gym.data.ExercisePhoto).
              | Expected:
              |""".trimMargin() + _infoExercisePhoto + """
              |
              | Found:
              |""".trimMargin() + _existingExercisePhoto)
        }
        val _columnsDayMarker: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDayMarker.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDayMarker.put("dateEpochDay", TableInfo.Column("dateEpochDay", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDayMarker.put("kind", TableInfo.Column("kind", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDayMarker.put("note", TableInfo.Column("note", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDayMarker.put("markedAt", TableInfo.Column("markedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDayMarker: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDayMarker: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesDayMarker.add(TableInfo.Index("index_day_marker_dateEpochDay", true,
            listOf("dateEpochDay"), listOf("ASC")))
        val _infoDayMarker: TableInfo = TableInfo("day_marker", _columnsDayMarker,
            _foreignKeysDayMarker, _indicesDayMarker)
        val _existingDayMarker: TableInfo = read(connection, "day_marker")
        if (!_infoDayMarker.equals(_existingDayMarker)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |day_marker(dev.avles.gym.data.DayMarker).
              | Expected:
              |""".trimMargin() + _infoDayMarker + """
              |
              | Found:
              |""".trimMargin() + _existingDayMarker)
        }
        val _columnsBodyMeasurement: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsBodyMeasurement.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBodyMeasurement.put("dateEpochDay", TableInfo.Column("dateEpochDay", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBodyMeasurement.put("part", TableInfo.Column("part", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBodyMeasurement.put("cm", TableInfo.Column("cm", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBodyMeasurement: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesBodyMeasurement: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesBodyMeasurement.add(TableInfo.Index("index_body_measurement_dateEpochDay_part",
            true, listOf("dateEpochDay", "part"), listOf("ASC", "ASC")))
        val _infoBodyMeasurement: TableInfo = TableInfo("body_measurement", _columnsBodyMeasurement,
            _foreignKeysBodyMeasurement, _indicesBodyMeasurement)
        val _existingBodyMeasurement: TableInfo = read(connection, "body_measurement")
        if (!_infoBodyMeasurement.equals(_existingBodyMeasurement)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |body_measurement(dev.avles.gym.data.BodyMeasurement).
              | Expected:
              |""".trimMargin() + _infoBodyMeasurement + """
              |
              | Found:
              |""".trimMargin() + _existingBodyMeasurement)
        }
        val _columnsSavedMeal: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSavedMeal.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedMeal.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedMeal.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSavedMeal: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSavedMeal: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSavedMeal: TableInfo = TableInfo("saved_meal", _columnsSavedMeal,
            _foreignKeysSavedMeal, _indicesSavedMeal)
        val _existingSavedMeal: TableInfo = read(connection, "saved_meal")
        if (!_infoSavedMeal.equals(_existingSavedMeal)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |saved_meal(dev.avles.gym.data.SavedMeal).
              | Expected:
              |""".trimMargin() + _infoSavedMeal + """
              |
              | Found:
              |""".trimMargin() + _existingSavedMeal)
        }
        val _columnsSavedMealItem: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSavedMealItem.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedMealItem.put("mealId", TableInfo.Column("mealId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedMealItem.put("foodId", TableInfo.Column("foodId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSavedMealItem.put("quantity", TableInfo.Column("quantity", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSavedMealItem: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSavedMealItem: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSavedMealItem.add(TableInfo.Index("index_saved_meal_item_mealId", false,
            listOf("mealId"), listOf("ASC")))
        val _infoSavedMealItem: TableInfo = TableInfo("saved_meal_item", _columnsSavedMealItem,
            _foreignKeysSavedMealItem, _indicesSavedMealItem)
        val _existingSavedMealItem: TableInfo = read(connection, "saved_meal_item")
        if (!_infoSavedMealItem.equals(_existingSavedMealItem)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |saved_meal_item(dev.avles.gym.data.SavedMealItem).
              | Expected:
              |""".trimMargin() + _infoSavedMealItem + """
              |
              | Found:
              |""".trimMargin() + _existingSavedMealItem)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "exercise", "workout_session",
        "set_entry", "food_item", "food_log", "body_weight", "split", "split_exercise",
        "progress_photo", "exercise_photo", "day_marker", "body_measurement", "saved_meal",
        "saved_meal_item")
  }

  public override fun clearAllTables() {
    super.performClear(false, "exercise", "workout_session", "set_entry", "food_item", "food_log",
        "body_weight", "split", "split_exercise", "progress_photo", "exercise_photo", "day_marker",
        "body_measurement", "saved_meal", "saved_meal_item")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(GymDao::class, GymDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun dao(): GymDao = _gymDao.value
}
