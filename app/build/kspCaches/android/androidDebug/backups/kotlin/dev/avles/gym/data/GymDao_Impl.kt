package dev.avles.gym.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class GymDao_Impl(
  __db: RoomDatabase,
) : GymDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfExercise: EntityInsertAdapter<Exercise>

  private val __insertAdapterOfWorkoutSession: EntityInsertAdapter<WorkoutSession>

  private val __insertAdapterOfSetEntry: EntityInsertAdapter<SetEntry>

  private val __insertAdapterOfFoodItem: EntityInsertAdapter<FoodItem>

  private val __insertAdapterOfFoodItem_1: EntityInsertAdapter<FoodItem>

  private val __insertAdapterOfFoodLog: EntityInsertAdapter<FoodLog>

  private val __insertAdapterOfSplit: EntityInsertAdapter<Split>

  private val __insertAdapterOfSplitExercise: EntityInsertAdapter<SplitExercise>

  private val __insertAdapterOfBodyWeight: EntityInsertAdapter<BodyWeight>

  private val __insertAdapterOfProgressPhoto: EntityInsertAdapter<ProgressPhoto>

  private val __insertAdapterOfExercisePhoto: EntityInsertAdapter<ExercisePhoto>

  private val __insertAdapterOfDayMarker: EntityInsertAdapter<DayMarker>

  private val __deleteAdapterOfSetEntry: EntityDeleteOrUpdateAdapter<SetEntry>

  private val __updateAdapterOfExercise: EntityDeleteOrUpdateAdapter<Exercise>

  private val __updateAdapterOfWorkoutSession: EntityDeleteOrUpdateAdapter<WorkoutSession>

  private val __updateAdapterOfFoodItem: EntityDeleteOrUpdateAdapter<FoodItem>

  private val __updateAdapterOfSplit: EntityDeleteOrUpdateAdapter<Split>
  init {
    this.__db = __db
    this.__insertAdapterOfExercise = object : EntityInsertAdapter<Exercise>() {
      protected override fun createQuery(): String =
          "INSERT OR IGNORE INTO `exercise` (`id`,`name`,`muscleGroup`,`isBodyweight`,`isCustom`,`repLow`,`repHigh`,`incrementKg`,`isAssisted`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Exercise) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.muscleGroup)
        val _tmp: Int = if (entity.isBodyweight) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        val _tmp_1: Int = if (entity.isCustom) 1 else 0
        statement.bindLong(5, _tmp_1.toLong())
        statement.bindLong(6, entity.repLow.toLong())
        statement.bindLong(7, entity.repHigh.toLong())
        statement.bindDouble(8, entity.incrementKg)
        val _tmp_2: Int = if (entity.isAssisted) 1 else 0
        statement.bindLong(9, _tmp_2.toLong())
      }
    }
    this.__insertAdapterOfWorkoutSession = object : EntityInsertAdapter<WorkoutSession>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `workout_session` (`id`,`dateEpochDay`,`label`,`startedAt`,`endedAt`,`notes`,`splitId`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WorkoutSession) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.dateEpochDay)
        statement.bindText(3, entity.label)
        statement.bindLong(4, entity.startedAt)
        val _tmpEndedAt: Long? = entity.endedAt
        if (_tmpEndedAt == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpEndedAt)
        }
        statement.bindText(6, entity.notes)
        val _tmpSplitId: Long? = entity.splitId
        if (_tmpSplitId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpSplitId)
        }
      }
    }
    this.__insertAdapterOfSetEntry = object : EntityInsertAdapter<SetEntry>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `set_entry` (`id`,`sessionId`,`exerciseId`,`setIndex`,`weightKg`,`reps`,`isWarmup`,`loggedAt`,`variation`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SetEntry) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.sessionId)
        statement.bindLong(3, entity.exerciseId)
        statement.bindLong(4, entity.setIndex.toLong())
        statement.bindDouble(5, entity.weightKg)
        statement.bindLong(6, entity.reps.toLong())
        val _tmp: Int = if (entity.isWarmup) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        statement.bindLong(8, entity.loggedAt)
        statement.bindText(9, entity.variation)
      }
    }
    this.__insertAdapterOfFoodItem = object : EntityInsertAdapter<FoodItem>() {
      protected override fun createQuery(): String =
          "INSERT OR IGNORE INTO `food_item` (`id`,`name`,`unitLabel`,`kcal`,`proteinG`,`carbsG`,`fatG`,`category`,`isCustom`,`isSupplement`,`aliases`,`source`,`fiberG`,`sugarG`,`satFatG`,`sodiumMg`,`potassiumMg`,`calciumMg`,`ironMg`,`magnesiumMg`,`zincMg`,`vitAMcg`,`vitCMg`,`vitDMcg`,`vitB12Mcg`,`folateMcg`,`omega3G`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FoodItem) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.unitLabel)
        statement.bindDouble(4, entity.kcal)
        statement.bindDouble(5, entity.proteinG)
        statement.bindDouble(6, entity.carbsG)
        statement.bindDouble(7, entity.fatG)
        statement.bindText(8, entity.category)
        val _tmp: Int = if (entity.isCustom) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        val _tmp_1: Int = if (entity.isSupplement) 1 else 0
        statement.bindLong(10, _tmp_1.toLong())
        statement.bindText(11, entity.aliases)
        statement.bindText(12, entity.source)
        val _tmpMicros: Micros = entity.micros
        statement.bindDouble(13, _tmpMicros.fiberG)
        statement.bindDouble(14, _tmpMicros.sugarG)
        statement.bindDouble(15, _tmpMicros.satFatG)
        statement.bindDouble(16, _tmpMicros.sodiumMg)
        statement.bindDouble(17, _tmpMicros.potassiumMg)
        statement.bindDouble(18, _tmpMicros.calciumMg)
        statement.bindDouble(19, _tmpMicros.ironMg)
        statement.bindDouble(20, _tmpMicros.magnesiumMg)
        statement.bindDouble(21, _tmpMicros.zincMg)
        statement.bindDouble(22, _tmpMicros.vitAMcg)
        statement.bindDouble(23, _tmpMicros.vitCMg)
        statement.bindDouble(24, _tmpMicros.vitDMcg)
        statement.bindDouble(25, _tmpMicros.vitB12Mcg)
        statement.bindDouble(26, _tmpMicros.folateMcg)
        statement.bindDouble(27, _tmpMicros.omega3G)
      }
    }
    this.__insertAdapterOfFoodItem_1 = object : EntityInsertAdapter<FoodItem>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `food_item` (`id`,`name`,`unitLabel`,`kcal`,`proteinG`,`carbsG`,`fatG`,`category`,`isCustom`,`isSupplement`,`aliases`,`source`,`fiberG`,`sugarG`,`satFatG`,`sodiumMg`,`potassiumMg`,`calciumMg`,`ironMg`,`magnesiumMg`,`zincMg`,`vitAMcg`,`vitCMg`,`vitDMcg`,`vitB12Mcg`,`folateMcg`,`omega3G`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FoodItem) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.unitLabel)
        statement.bindDouble(4, entity.kcal)
        statement.bindDouble(5, entity.proteinG)
        statement.bindDouble(6, entity.carbsG)
        statement.bindDouble(7, entity.fatG)
        statement.bindText(8, entity.category)
        val _tmp: Int = if (entity.isCustom) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        val _tmp_1: Int = if (entity.isSupplement) 1 else 0
        statement.bindLong(10, _tmp_1.toLong())
        statement.bindText(11, entity.aliases)
        statement.bindText(12, entity.source)
        val _tmpMicros: Micros = entity.micros
        statement.bindDouble(13, _tmpMicros.fiberG)
        statement.bindDouble(14, _tmpMicros.sugarG)
        statement.bindDouble(15, _tmpMicros.satFatG)
        statement.bindDouble(16, _tmpMicros.sodiumMg)
        statement.bindDouble(17, _tmpMicros.potassiumMg)
        statement.bindDouble(18, _tmpMicros.calciumMg)
        statement.bindDouble(19, _tmpMicros.ironMg)
        statement.bindDouble(20, _tmpMicros.magnesiumMg)
        statement.bindDouble(21, _tmpMicros.zincMg)
        statement.bindDouble(22, _tmpMicros.vitAMcg)
        statement.bindDouble(23, _tmpMicros.vitCMg)
        statement.bindDouble(24, _tmpMicros.vitDMcg)
        statement.bindDouble(25, _tmpMicros.vitB12Mcg)
        statement.bindDouble(26, _tmpMicros.folateMcg)
        statement.bindDouble(27, _tmpMicros.omega3G)
      }
    }
    this.__insertAdapterOfFoodLog = object : EntityInsertAdapter<FoodLog>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `food_log` (`id`,`dateEpochDay`,`foodId`,`meal`,`quantity`,`loggedAt`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FoodLog) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.dateEpochDay)
        statement.bindLong(3, entity.foodId)
        statement.bindText(4, entity.meal)
        statement.bindDouble(5, entity.quantity)
        statement.bindLong(6, entity.loggedAt)
      }
    }
    this.__insertAdapterOfSplit = object : EntityInsertAdapter<Split>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `split` (`id`,`name`,`position`,`isCustom`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Split) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.position.toLong())
        val _tmp: Int = if (entity.isCustom) 1 else 0
        statement.bindLong(4, _tmp.toLong())
      }
    }
    this.__insertAdapterOfSplitExercise = object : EntityInsertAdapter<SplitExercise>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `split_exercise` (`id`,`splitId`,`exerciseId`,`position`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SplitExercise) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.splitId)
        statement.bindLong(3, entity.exerciseId)
        statement.bindLong(4, entity.position.toLong())
      }
    }
    this.__insertAdapterOfBodyWeight = object : EntityInsertAdapter<BodyWeight>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `body_weight` (`id`,`dateEpochDay`,`weightKg`,`note`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: BodyWeight) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.dateEpochDay)
        statement.bindDouble(3, entity.weightKg)
        statement.bindText(4, entity.note)
      }
    }
    this.__insertAdapterOfProgressPhoto = object : EntityInsertAdapter<ProgressPhoto>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `progress_photo` (`id`,`dateEpochDay`,`fileName`,`label`,`note`,`takenAt`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ProgressPhoto) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.dateEpochDay)
        statement.bindText(3, entity.fileName)
        statement.bindText(4, entity.label)
        statement.bindText(5, entity.note)
        statement.bindLong(6, entity.takenAt)
      }
    }
    this.__insertAdapterOfExercisePhoto = object : EntityInsertAdapter<ExercisePhoto>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `exercise_photo` (`id`,`exerciseId`,`fileName`,`addedAt`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ExercisePhoto) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.exerciseId)
        statement.bindText(3, entity.fileName)
        statement.bindLong(4, entity.addedAt)
      }
    }
    this.__insertAdapterOfDayMarker = object : EntityInsertAdapter<DayMarker>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `day_marker` (`id`,`dateEpochDay`,`kind`,`note`,`markedAt`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DayMarker) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.dateEpochDay)
        statement.bindText(3, entity.kind)
        statement.bindText(4, entity.note)
        statement.bindLong(5, entity.markedAt)
      }
    }
    this.__deleteAdapterOfSetEntry = object : EntityDeleteOrUpdateAdapter<SetEntry>() {
      protected override fun createQuery(): String = "DELETE FROM `set_entry` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SetEntry) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfExercise = object : EntityDeleteOrUpdateAdapter<Exercise>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `exercise` SET `id` = ?,`name` = ?,`muscleGroup` = ?,`isBodyweight` = ?,`isCustom` = ?,`repLow` = ?,`repHigh` = ?,`incrementKg` = ?,`isAssisted` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Exercise) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.muscleGroup)
        val _tmp: Int = if (entity.isBodyweight) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        val _tmp_1: Int = if (entity.isCustom) 1 else 0
        statement.bindLong(5, _tmp_1.toLong())
        statement.bindLong(6, entity.repLow.toLong())
        statement.bindLong(7, entity.repHigh.toLong())
        statement.bindDouble(8, entity.incrementKg)
        val _tmp_2: Int = if (entity.isAssisted) 1 else 0
        statement.bindLong(9, _tmp_2.toLong())
        statement.bindLong(10, entity.id)
      }
    }
    this.__updateAdapterOfWorkoutSession = object : EntityDeleteOrUpdateAdapter<WorkoutSession>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `workout_session` SET `id` = ?,`dateEpochDay` = ?,`label` = ?,`startedAt` = ?,`endedAt` = ?,`notes` = ?,`splitId` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: WorkoutSession) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.dateEpochDay)
        statement.bindText(3, entity.label)
        statement.bindLong(4, entity.startedAt)
        val _tmpEndedAt: Long? = entity.endedAt
        if (_tmpEndedAt == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpEndedAt)
        }
        statement.bindText(6, entity.notes)
        val _tmpSplitId: Long? = entity.splitId
        if (_tmpSplitId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpSplitId)
        }
        statement.bindLong(8, entity.id)
      }
    }
    this.__updateAdapterOfFoodItem = object : EntityDeleteOrUpdateAdapter<FoodItem>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `food_item` SET `id` = ?,`name` = ?,`unitLabel` = ?,`kcal` = ?,`proteinG` = ?,`carbsG` = ?,`fatG` = ?,`category` = ?,`isCustom` = ?,`isSupplement` = ?,`aliases` = ?,`source` = ?,`fiberG` = ?,`sugarG` = ?,`satFatG` = ?,`sodiumMg` = ?,`potassiumMg` = ?,`calciumMg` = ?,`ironMg` = ?,`magnesiumMg` = ?,`zincMg` = ?,`vitAMcg` = ?,`vitCMg` = ?,`vitDMcg` = ?,`vitB12Mcg` = ?,`folateMcg` = ?,`omega3G` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: FoodItem) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.unitLabel)
        statement.bindDouble(4, entity.kcal)
        statement.bindDouble(5, entity.proteinG)
        statement.bindDouble(6, entity.carbsG)
        statement.bindDouble(7, entity.fatG)
        statement.bindText(8, entity.category)
        val _tmp: Int = if (entity.isCustom) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        val _tmp_1: Int = if (entity.isSupplement) 1 else 0
        statement.bindLong(10, _tmp_1.toLong())
        statement.bindText(11, entity.aliases)
        statement.bindText(12, entity.source)
        val _tmpMicros: Micros = entity.micros
        statement.bindDouble(13, _tmpMicros.fiberG)
        statement.bindDouble(14, _tmpMicros.sugarG)
        statement.bindDouble(15, _tmpMicros.satFatG)
        statement.bindDouble(16, _tmpMicros.sodiumMg)
        statement.bindDouble(17, _tmpMicros.potassiumMg)
        statement.bindDouble(18, _tmpMicros.calciumMg)
        statement.bindDouble(19, _tmpMicros.ironMg)
        statement.bindDouble(20, _tmpMicros.magnesiumMg)
        statement.bindDouble(21, _tmpMicros.zincMg)
        statement.bindDouble(22, _tmpMicros.vitAMcg)
        statement.bindDouble(23, _tmpMicros.vitCMg)
        statement.bindDouble(24, _tmpMicros.vitDMcg)
        statement.bindDouble(25, _tmpMicros.vitB12Mcg)
        statement.bindDouble(26, _tmpMicros.folateMcg)
        statement.bindDouble(27, _tmpMicros.omega3G)
        statement.bindLong(28, entity.id)
      }
    }
    this.__updateAdapterOfSplit = object : EntityDeleteOrUpdateAdapter<Split>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `split` SET `id` = ?,`name` = ?,`position` = ?,`isCustom` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Split) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.position.toLong())
        val _tmp: Int = if (entity.isCustom) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        statement.bindLong(5, entity.id)
      }
    }
  }

  public override suspend fun insertExercises(items: List<Exercise>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfExercise.insert(_connection, items)
  }

  public override suspend fun insertExercise(item: Exercise): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfExercise.insertAndReturnId(_connection, item)
    _result
  }

  public override suspend fun insertSession(session: WorkoutSession): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfWorkoutSession.insertAndReturnId(_connection, session)
    _result
  }

  public override suspend fun insertSet(entry: SetEntry): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfSetEntry.insertAndReturnId(_connection, entry)
    _result
  }

  public override suspend fun insertFoods(items: List<FoodItem>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfFoodItem.insert(_connection, items)
  }

  public override suspend fun upsertFood(item: FoodItem): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfFoodItem_1.insertAndReturnId(_connection, item)
    _result
  }

  public override suspend fun insertFoodLog(entry: FoodLog): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfFoodLog.insertAndReturnId(_connection, entry)
    _result
  }

  public override suspend fun insertSplit(split: Split): Long = performSuspending(__db, false, true)
      { _connection ->
    val _result: Long = __insertAdapterOfSplit.insertAndReturnId(_connection, split)
    _result
  }

  public override suspend fun insertSplitExercises(rows: List<SplitExercise>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSplitExercise.insert(_connection, rows)
  }

  public override suspend fun upsertWeight(entry: BodyWeight): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfBodyWeight.insert(_connection, entry)
  }

  public override suspend fun insertProgressPhoto(photo: ProgressPhoto): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfProgressPhoto.insertAndReturnId(_connection, photo)
    _result
  }

  public override suspend fun insertExercisePhoto(photo: ExercisePhoto): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfExercisePhoto.insertAndReturnId(_connection, photo)
    _result
  }

  public override suspend fun upsertDayMarker(marker: DayMarker): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfDayMarker.insertAndReturnId(_connection, marker)
    _result
  }

  public override suspend fun deleteSet(entry: SetEntry): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfSetEntry.handle(_connection, entry)
  }

  public override suspend fun updateExercises(items: List<Exercise>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfExercise.handleMultiple(_connection, items)
  }

  public override suspend fun updateSession(session: WorkoutSession): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfWorkoutSession.handle(_connection, session)
  }

  public override suspend fun updateFood(item: FoodItem): Unit = performSuspending(__db, false,
      true) { _connection ->
    __updateAdapterOfFoodItem.handle(_connection, item)
  }

  public override suspend fun updateSplit(split: Split): Unit = performSuspending(__db, false, true)
      { _connection ->
    __updateAdapterOfSplit.handle(_connection, split)
  }

  public override fun exercises(): Flow<List<Exercise>> {
    val _sql: String = "SELECT * FROM exercise ORDER BY muscleGroup, name"
    return createFlow(__db, false, arrayOf("exercise")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfMuscleGroup: Int = getColumnIndexOrThrow(_stmt, "muscleGroup")
        val _columnIndexOfIsBodyweight: Int = getColumnIndexOrThrow(_stmt, "isBodyweight")
        val _columnIndexOfIsCustom: Int = getColumnIndexOrThrow(_stmt, "isCustom")
        val _columnIndexOfRepLow: Int = getColumnIndexOrThrow(_stmt, "repLow")
        val _columnIndexOfRepHigh: Int = getColumnIndexOrThrow(_stmt, "repHigh")
        val _columnIndexOfIncrementKg: Int = getColumnIndexOrThrow(_stmt, "incrementKg")
        val _columnIndexOfIsAssisted: Int = getColumnIndexOrThrow(_stmt, "isAssisted")
        val _result: MutableList<Exercise> = mutableListOf()
        while (_stmt.step()) {
          val _item: Exercise
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpMuscleGroup: String
          _tmpMuscleGroup = _stmt.getText(_columnIndexOfMuscleGroup)
          val _tmpIsBodyweight: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsBodyweight).toInt()
          _tmpIsBodyweight = _tmp != 0
          val _tmpIsCustom: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsCustom).toInt()
          _tmpIsCustom = _tmp_1 != 0
          val _tmpRepLow: Int
          _tmpRepLow = _stmt.getLong(_columnIndexOfRepLow).toInt()
          val _tmpRepHigh: Int
          _tmpRepHigh = _stmt.getLong(_columnIndexOfRepHigh).toInt()
          val _tmpIncrementKg: Double
          _tmpIncrementKg = _stmt.getDouble(_columnIndexOfIncrementKg)
          val _tmpIsAssisted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsAssisted).toInt()
          _tmpIsAssisted = _tmp_2 != 0
          _item =
              Exercise(_tmpId,_tmpName,_tmpMuscleGroup,_tmpIsBodyweight,_tmpIsCustom,_tmpRepLow,_tmpRepHigh,_tmpIncrementKg,_tmpIsAssisted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun exerciseCount(): Int {
    val _sql: String = "SELECT COUNT(*) FROM exercise"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun exerciseNames(): List<String> {
    val _sql: String = "SELECT name FROM exercise"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun allExercisesOnce(): List<Exercise> {
    val _sql: String = "SELECT * FROM exercise ORDER BY muscleGroup, name"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfMuscleGroup: Int = getColumnIndexOrThrow(_stmt, "muscleGroup")
        val _columnIndexOfIsBodyweight: Int = getColumnIndexOrThrow(_stmt, "isBodyweight")
        val _columnIndexOfIsCustom: Int = getColumnIndexOrThrow(_stmt, "isCustom")
        val _columnIndexOfRepLow: Int = getColumnIndexOrThrow(_stmt, "repLow")
        val _columnIndexOfRepHigh: Int = getColumnIndexOrThrow(_stmt, "repHigh")
        val _columnIndexOfIncrementKg: Int = getColumnIndexOrThrow(_stmt, "incrementKg")
        val _columnIndexOfIsAssisted: Int = getColumnIndexOrThrow(_stmt, "isAssisted")
        val _result: MutableList<Exercise> = mutableListOf()
        while (_stmt.step()) {
          val _item: Exercise
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpMuscleGroup: String
          _tmpMuscleGroup = _stmt.getText(_columnIndexOfMuscleGroup)
          val _tmpIsBodyweight: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsBodyweight).toInt()
          _tmpIsBodyweight = _tmp != 0
          val _tmpIsCustom: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsCustom).toInt()
          _tmpIsCustom = _tmp_1 != 0
          val _tmpRepLow: Int
          _tmpRepLow = _stmt.getLong(_columnIndexOfRepLow).toInt()
          val _tmpRepHigh: Int
          _tmpRepHigh = _stmt.getLong(_columnIndexOfRepHigh).toInt()
          val _tmpIncrementKg: Double
          _tmpIncrementKg = _stmt.getDouble(_columnIndexOfIncrementKg)
          val _tmpIsAssisted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsAssisted).toInt()
          _tmpIsAssisted = _tmp_2 != 0
          _item =
              Exercise(_tmpId,_tmpName,_tmpMuscleGroup,_tmpIsBodyweight,_tmpIsCustom,_tmpRepLow,_tmpRepHigh,_tmpIncrementKg,_tmpIsAssisted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun staleSeededExerciseCount(groups: List<String>): Int {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT COUNT(*) FROM exercise WHERE isCustom = 0 AND muscleGroup NOT IN (")
    val _inputSize: Int = groups.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: String in groups) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun seededExercises(): List<Exercise> {
    val _sql: String = "SELECT * FROM exercise WHERE isCustom = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfMuscleGroup: Int = getColumnIndexOrThrow(_stmt, "muscleGroup")
        val _columnIndexOfIsBodyweight: Int = getColumnIndexOrThrow(_stmt, "isBodyweight")
        val _columnIndexOfIsCustom: Int = getColumnIndexOrThrow(_stmt, "isCustom")
        val _columnIndexOfRepLow: Int = getColumnIndexOrThrow(_stmt, "repLow")
        val _columnIndexOfRepHigh: Int = getColumnIndexOrThrow(_stmt, "repHigh")
        val _columnIndexOfIncrementKg: Int = getColumnIndexOrThrow(_stmt, "incrementKg")
        val _columnIndexOfIsAssisted: Int = getColumnIndexOrThrow(_stmt, "isAssisted")
        val _result: MutableList<Exercise> = mutableListOf()
        while (_stmt.step()) {
          val _item: Exercise
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpMuscleGroup: String
          _tmpMuscleGroup = _stmt.getText(_columnIndexOfMuscleGroup)
          val _tmpIsBodyweight: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsBodyweight).toInt()
          _tmpIsBodyweight = _tmp != 0
          val _tmpIsCustom: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsCustom).toInt()
          _tmpIsCustom = _tmp_1 != 0
          val _tmpRepLow: Int
          _tmpRepLow = _stmt.getLong(_columnIndexOfRepLow).toInt()
          val _tmpRepHigh: Int
          _tmpRepHigh = _stmt.getLong(_columnIndexOfRepHigh).toInt()
          val _tmpIncrementKg: Double
          _tmpIncrementKg = _stmt.getDouble(_columnIndexOfIncrementKg)
          val _tmpIsAssisted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsAssisted).toInt()
          _tmpIsAssisted = _tmp_2 != 0
          _item =
              Exercise(_tmpId,_tmpName,_tmpMuscleGroup,_tmpIsBodyweight,_tmpIsCustom,_tmpRepLow,_tmpRepHigh,_tmpIncrementKg,_tmpIsAssisted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun activeSession(): Flow<WorkoutSession?> {
    val _sql: String =
        "SELECT * FROM workout_session WHERE endedAt IS NULL ORDER BY startedAt DESC LIMIT 1"
    return createFlow(__db, false, arrayOf("workout_session")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDateEpochDay: Int = getColumnIndexOrThrow(_stmt, "dateEpochDay")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfSplitId: Int = getColumnIndexOrThrow(_stmt, "splitId")
        val _result: WorkoutSession?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDateEpochDay: Long
          _tmpDateEpochDay = _stmt.getLong(_columnIndexOfDateEpochDay)
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpSplitId: Long?
          if (_stmt.isNull(_columnIndexOfSplitId)) {
            _tmpSplitId = null
          } else {
            _tmpSplitId = _stmt.getLong(_columnIndexOfSplitId)
          }
          _result =
              WorkoutSession(_tmpId,_tmpDateEpochDay,_tmpLabel,_tmpStartedAt,_tmpEndedAt,_tmpNotes,_tmpSplitId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun sessionsOn(day: Long): Flow<List<WorkoutSession>> {
    val _sql: String =
        "SELECT * FROM workout_session WHERE dateEpochDay = ? ORDER BY startedAt DESC"
    return createFlow(__db, false, arrayOf("workout_session")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, day)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDateEpochDay: Int = getColumnIndexOrThrow(_stmt, "dateEpochDay")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfSplitId: Int = getColumnIndexOrThrow(_stmt, "splitId")
        val _result: MutableList<WorkoutSession> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutSession
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDateEpochDay: Long
          _tmpDateEpochDay = _stmt.getLong(_columnIndexOfDateEpochDay)
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpSplitId: Long?
          if (_stmt.isNull(_columnIndexOfSplitId)) {
            _tmpSplitId = null
          } else {
            _tmpSplitId = _stmt.getLong(_columnIndexOfSplitId)
          }
          _item =
              WorkoutSession(_tmpId,_tmpDateEpochDay,_tmpLabel,_tmpStartedAt,_tmpEndedAt,_tmpNotes,_tmpSplitId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun recentSessions(limit: Int): Flow<List<WorkoutSession>> {
    val _sql: String = "SELECT * FROM workout_session ORDER BY startedAt DESC LIMIT ?"
    return createFlow(__db, false, arrayOf("workout_session")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDateEpochDay: Int = getColumnIndexOrThrow(_stmt, "dateEpochDay")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfSplitId: Int = getColumnIndexOrThrow(_stmt, "splitId")
        val _result: MutableList<WorkoutSession> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutSession
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDateEpochDay: Long
          _tmpDateEpochDay = _stmt.getLong(_columnIndexOfDateEpochDay)
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpSplitId: Long?
          if (_stmt.isNull(_columnIndexOfSplitId)) {
            _tmpSplitId = null
          } else {
            _tmpSplitId = _stmt.getLong(_columnIndexOfSplitId)
          }
          _item =
              WorkoutSession(_tmpId,_tmpDateEpochDay,_tmpLabel,_tmpStartedAt,_tmpEndedAt,_tmpNotes,_tmpSplitId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun trainedDaysBetween(from: Long, to: Long): Flow<List<Long>> {
    val _sql: String =
        "SELECT DISTINCT dateEpochDay FROM workout_session WHERE dateEpochDay BETWEEN ? AND ?"
    return createFlow(__db, false, arrayOf("workout_session")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, from)
        _argIndex = 2
        _stmt.bindLong(_argIndex, to)
        val _result: MutableList<Long> = mutableListOf()
        while (_stmt.step()) {
          val _item: Long
          _item = _stmt.getLong(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun allTrainedDays(): Flow<List<Long>> {
    val _sql: String =
        "SELECT DISTINCT dateEpochDay FROM workout_session ORDER BY dateEpochDay DESC"
    return createFlow(__db, false, arrayOf("workout_session")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<Long> = mutableListOf()
        while (_stmt.step()) {
          val _item: Long
          _item = _stmt.getLong(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setById(id: Long): SetEntry? {
    val _sql: String = "SELECT * FROM set_entry WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfSetIndex: Int = getColumnIndexOrThrow(_stmt, "setIndex")
        val _columnIndexOfWeightKg: Int = getColumnIndexOrThrow(_stmt, "weightKg")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfIsWarmup: Int = getColumnIndexOrThrow(_stmt, "isWarmup")
        val _columnIndexOfLoggedAt: Int = getColumnIndexOrThrow(_stmt, "loggedAt")
        val _columnIndexOfVariation: Int = getColumnIndexOrThrow(_stmt, "variation")
        val _result: SetEntry?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSessionId: Long
          _tmpSessionId = _stmt.getLong(_columnIndexOfSessionId)
          val _tmpExerciseId: Long
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId)
          val _tmpSetIndex: Int
          _tmpSetIndex = _stmt.getLong(_columnIndexOfSetIndex).toInt()
          val _tmpWeightKg: Double
          _tmpWeightKg = _stmt.getDouble(_columnIndexOfWeightKg)
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpIsWarmup: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsWarmup).toInt()
          _tmpIsWarmup = _tmp != 0
          val _tmpLoggedAt: Long
          _tmpLoggedAt = _stmt.getLong(_columnIndexOfLoggedAt)
          val _tmpVariation: String
          _tmpVariation = _stmt.getText(_columnIndexOfVariation)
          _result =
              SetEntry(_tmpId,_tmpSessionId,_tmpExerciseId,_tmpSetIndex,_tmpWeightKg,_tmpReps,_tmpIsWarmup,_tmpLoggedAt,_tmpVariation)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun sessionById(id: Long): WorkoutSession? {
    val _sql: String = "SELECT * FROM workout_session WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDateEpochDay: Int = getColumnIndexOrThrow(_stmt, "dateEpochDay")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfSplitId: Int = getColumnIndexOrThrow(_stmt, "splitId")
        val _result: WorkoutSession?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDateEpochDay: Long
          _tmpDateEpochDay = _stmt.getLong(_columnIndexOfDateEpochDay)
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpSplitId: Long?
          if (_stmt.isNull(_columnIndexOfSplitId)) {
            _tmpSplitId = null
          } else {
            _tmpSplitId = _stmt.getLong(_columnIndexOfSplitId)
          }
          _result =
              WorkoutSession(_tmpId,_tmpDateEpochDay,_tmpLabel,_tmpStartedAt,_tmpEndedAt,_tmpNotes,_tmpSplitId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun sessionSummaries(): Flow<List<SessionSummary>> {
    val _sql: String = """
        |
        |        SELECT w.id AS sessionId,
        |               COUNT(s.id) AS setCount,
        |               COALESCE(SUM(CASE WHEN s.isWarmup = 0 AND COALESCE(e.isAssisted, 0) = 0
        |                                 THEN s.weightKg * s.reps ELSE 0 END), 0.0) AS volume
        |        FROM workout_session w
        |        LEFT JOIN set_entry s ON s.sessionId = w.id
        |        LEFT JOIN exercise e ON e.id = s.exerciseId
        |        GROUP BY w.id
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("workout_session", "set_entry", "exercise")) {
        _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfSessionId: Int = 0
        val _columnIndexOfSetCount: Int = 1
        val _columnIndexOfVolume: Int = 2
        val _result: MutableList<SessionSummary> = mutableListOf()
        while (_stmt.step()) {
          val _item: SessionSummary
          val _tmpSessionId: Long
          _tmpSessionId = _stmt.getLong(_columnIndexOfSessionId)
          val _tmpSetCount: Int
          _tmpSetCount = _stmt.getLong(_columnIndexOfSetCount).toInt()
          val _tmpVolume: Double
          _tmpVolume = _stmt.getDouble(_columnIndexOfVolume)
          _item = SessionSummary(_tmpSessionId,_tmpSetCount,_tmpVolume)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun setsForSession(sessionId: Long): Flow<List<SetRow>> {
    val _sql: String = """
        |
        |        SELECT s.id AS setId, s.sessionId AS sessionId, s.exerciseId AS exerciseId,
        |               e.name AS exerciseName, s.setIndex AS setIndex, s.weightKg AS weightKg,
        |               s.reps AS reps, s.isWarmup AS isWarmup, s.loggedAt AS loggedAt,
        |               s.variation AS variation, e.isAssisted AS isAssisted
        |        FROM set_entry s
        |        JOIN exercise e ON e.id = s.exerciseId
        |        WHERE s.sessionId = ?
        |        ORDER BY s.loggedAt ASC
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("set_entry", "exercise")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sessionId)
        val _columnIndexOfSetId: Int = 0
        val _columnIndexOfSessionId: Int = 1
        val _columnIndexOfExerciseId: Int = 2
        val _columnIndexOfExerciseName: Int = 3
        val _columnIndexOfSetIndex: Int = 4
        val _columnIndexOfWeightKg: Int = 5
        val _columnIndexOfReps: Int = 6
        val _columnIndexOfIsWarmup: Int = 7
        val _columnIndexOfLoggedAt: Int = 8
        val _columnIndexOfVariation: Int = 9
        val _columnIndexOfIsAssisted: Int = 10
        val _result: MutableList<SetRow> = mutableListOf()
        while (_stmt.step()) {
          val _item: SetRow
          val _tmpSetId: Long
          _tmpSetId = _stmt.getLong(_columnIndexOfSetId)
          val _tmpSessionId: Long
          _tmpSessionId = _stmt.getLong(_columnIndexOfSessionId)
          val _tmpExerciseId: Long
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId)
          val _tmpExerciseName: String
          _tmpExerciseName = _stmt.getText(_columnIndexOfExerciseName)
          val _tmpSetIndex: Int
          _tmpSetIndex = _stmt.getLong(_columnIndexOfSetIndex).toInt()
          val _tmpWeightKg: Double
          _tmpWeightKg = _stmt.getDouble(_columnIndexOfWeightKg)
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpIsWarmup: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsWarmup).toInt()
          _tmpIsWarmup = _tmp != 0
          val _tmpLoggedAt: Long
          _tmpLoggedAt = _stmt.getLong(_columnIndexOfLoggedAt)
          val _tmpVariation: String
          _tmpVariation = _stmt.getText(_columnIndexOfVariation)
          val _tmpIsAssisted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsAssisted).toInt()
          _tmpIsAssisted = _tmp_1 != 0
          _item =
              SetRow(_tmpSetId,_tmpSessionId,_tmpExerciseId,_tmpExerciseName,_tmpSetIndex,_tmpWeightKg,_tmpReps,_tmpIsWarmup,_tmpLoggedAt,_tmpVariation,_tmpIsAssisted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun historyForExercise(exerciseId: Long, limit: Int): Flow<List<SetRow>> {
    val _sql: String = """
        |
        |        SELECT s.id AS setId, s.sessionId AS sessionId, s.exerciseId AS exerciseId,
        |               e.name AS exerciseName, s.setIndex AS setIndex, s.weightKg AS weightKg,
        |               s.reps AS reps, s.isWarmup AS isWarmup, s.loggedAt AS loggedAt,
        |               s.variation AS variation, e.isAssisted AS isAssisted
        |        FROM set_entry s
        |        JOIN exercise e ON e.id = s.exerciseId
        |        WHERE s.exerciseId = ? AND s.isWarmup = 0
        |        ORDER BY s.loggedAt DESC
        |        LIMIT ?
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("set_entry", "exercise")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, exerciseId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfSetId: Int = 0
        val _columnIndexOfSessionId: Int = 1
        val _columnIndexOfExerciseId: Int = 2
        val _columnIndexOfExerciseName: Int = 3
        val _columnIndexOfSetIndex: Int = 4
        val _columnIndexOfWeightKg: Int = 5
        val _columnIndexOfReps: Int = 6
        val _columnIndexOfIsWarmup: Int = 7
        val _columnIndexOfLoggedAt: Int = 8
        val _columnIndexOfVariation: Int = 9
        val _columnIndexOfIsAssisted: Int = 10
        val _result: MutableList<SetRow> = mutableListOf()
        while (_stmt.step()) {
          val _item: SetRow
          val _tmpSetId: Long
          _tmpSetId = _stmt.getLong(_columnIndexOfSetId)
          val _tmpSessionId: Long
          _tmpSessionId = _stmt.getLong(_columnIndexOfSessionId)
          val _tmpExerciseId: Long
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId)
          val _tmpExerciseName: String
          _tmpExerciseName = _stmt.getText(_columnIndexOfExerciseName)
          val _tmpSetIndex: Int
          _tmpSetIndex = _stmt.getLong(_columnIndexOfSetIndex).toInt()
          val _tmpWeightKg: Double
          _tmpWeightKg = _stmt.getDouble(_columnIndexOfWeightKg)
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpIsWarmup: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsWarmup).toInt()
          _tmpIsWarmup = _tmp != 0
          val _tmpLoggedAt: Long
          _tmpLoggedAt = _stmt.getLong(_columnIndexOfLoggedAt)
          val _tmpVariation: String
          _tmpVariation = _stmt.getText(_columnIndexOfVariation)
          val _tmpIsAssisted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsAssisted).toInt()
          _tmpIsAssisted = _tmp_1 != 0
          _item =
              SetRow(_tmpSetId,_tmpSessionId,_tmpExerciseId,_tmpExerciseName,_tmpSetIndex,_tmpWeightKg,_tmpReps,_tmpIsWarmup,_tmpLoggedAt,_tmpVariation,_tmpIsAssisted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun volumeByDay(): Flow<List<DayVolume>> {
    val _sql: String = """
        |
        |        SELECT w.dateEpochDay AS dateEpochDay,
        |               SUM(CASE WHEN COALESCE(e.isAssisted, 0) = 0 THEN s.weightKg * s.reps ELSE 0 END) AS volume
        |        FROM set_entry s
        |        JOIN workout_session w ON w.id = s.sessionId
        |        LEFT JOIN exercise e ON e.id = s.exerciseId
        |        WHERE s.isWarmup = 0
        |        GROUP BY w.dateEpochDay
        |        ORDER BY w.dateEpochDay ASC
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("set_entry", "workout_session", "exercise")) {
        _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfDateEpochDay: Int = 0
        val _columnIndexOfVolume: Int = 1
        val _result: MutableList<DayVolume> = mutableListOf()
        while (_stmt.step()) {
          val _item: DayVolume
          val _tmpDateEpochDay: Long
          _tmpDateEpochDay = _stmt.getLong(_columnIndexOfDateEpochDay)
          val _tmpVolume: Double
          _tmpVolume = _stmt.getDouble(_columnIndexOfVolume)
          _item = DayVolume(_tmpDateEpochDay,_tmpVolume)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun foods(): Flow<List<FoodItem>> {
    val _sql: String = "SELECT * FROM food_item ORDER BY name"
    return createFlow(__db, false, arrayOf("food_item")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfUnitLabel: Int = getColumnIndexOrThrow(_stmt, "unitLabel")
        val _columnIndexOfKcal: Int = getColumnIndexOrThrow(_stmt, "kcal")
        val _columnIndexOfProteinG: Int = getColumnIndexOrThrow(_stmt, "proteinG")
        val _columnIndexOfCarbsG: Int = getColumnIndexOrThrow(_stmt, "carbsG")
        val _columnIndexOfFatG: Int = getColumnIndexOrThrow(_stmt, "fatG")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsCustom: Int = getColumnIndexOrThrow(_stmt, "isCustom")
        val _columnIndexOfIsSupplement: Int = getColumnIndexOrThrow(_stmt, "isSupplement")
        val _columnIndexOfAliases: Int = getColumnIndexOrThrow(_stmt, "aliases")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfFiberG: Int = getColumnIndexOrThrow(_stmt, "fiberG")
        val _columnIndexOfSugarG: Int = getColumnIndexOrThrow(_stmt, "sugarG")
        val _columnIndexOfSatFatG: Int = getColumnIndexOrThrow(_stmt, "satFatG")
        val _columnIndexOfSodiumMg: Int = getColumnIndexOrThrow(_stmt, "sodiumMg")
        val _columnIndexOfPotassiumMg: Int = getColumnIndexOrThrow(_stmt, "potassiumMg")
        val _columnIndexOfCalciumMg: Int = getColumnIndexOrThrow(_stmt, "calciumMg")
        val _columnIndexOfIronMg: Int = getColumnIndexOrThrow(_stmt, "ironMg")
        val _columnIndexOfMagnesiumMg: Int = getColumnIndexOrThrow(_stmt, "magnesiumMg")
        val _columnIndexOfZincMg: Int = getColumnIndexOrThrow(_stmt, "zincMg")
        val _columnIndexOfVitAMcg: Int = getColumnIndexOrThrow(_stmt, "vitAMcg")
        val _columnIndexOfVitCMg: Int = getColumnIndexOrThrow(_stmt, "vitCMg")
        val _columnIndexOfVitDMcg: Int = getColumnIndexOrThrow(_stmt, "vitDMcg")
        val _columnIndexOfVitB12Mcg: Int = getColumnIndexOrThrow(_stmt, "vitB12Mcg")
        val _columnIndexOfFolateMcg: Int = getColumnIndexOrThrow(_stmt, "folateMcg")
        val _columnIndexOfOmega3G: Int = getColumnIndexOrThrow(_stmt, "omega3G")
        val _result: MutableList<FoodItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: FoodItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpUnitLabel: String
          _tmpUnitLabel = _stmt.getText(_columnIndexOfUnitLabel)
          val _tmpKcal: Double
          _tmpKcal = _stmt.getDouble(_columnIndexOfKcal)
          val _tmpProteinG: Double
          _tmpProteinG = _stmt.getDouble(_columnIndexOfProteinG)
          val _tmpCarbsG: Double
          _tmpCarbsG = _stmt.getDouble(_columnIndexOfCarbsG)
          val _tmpFatG: Double
          _tmpFatG = _stmt.getDouble(_columnIndexOfFatG)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpIsCustom: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCustom).toInt()
          _tmpIsCustom = _tmp != 0
          val _tmpIsSupplement: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsSupplement).toInt()
          _tmpIsSupplement = _tmp_1 != 0
          val _tmpAliases: String
          _tmpAliases = _stmt.getText(_columnIndexOfAliases)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpMicros: Micros
          val _tmpFiberG: Double
          _tmpFiberG = _stmt.getDouble(_columnIndexOfFiberG)
          val _tmpSugarG: Double
          _tmpSugarG = _stmt.getDouble(_columnIndexOfSugarG)
          val _tmpSatFatG: Double
          _tmpSatFatG = _stmt.getDouble(_columnIndexOfSatFatG)
          val _tmpSodiumMg: Double
          _tmpSodiumMg = _stmt.getDouble(_columnIndexOfSodiumMg)
          val _tmpPotassiumMg: Double
          _tmpPotassiumMg = _stmt.getDouble(_columnIndexOfPotassiumMg)
          val _tmpCalciumMg: Double
          _tmpCalciumMg = _stmt.getDouble(_columnIndexOfCalciumMg)
          val _tmpIronMg: Double
          _tmpIronMg = _stmt.getDouble(_columnIndexOfIronMg)
          val _tmpMagnesiumMg: Double
          _tmpMagnesiumMg = _stmt.getDouble(_columnIndexOfMagnesiumMg)
          val _tmpZincMg: Double
          _tmpZincMg = _stmt.getDouble(_columnIndexOfZincMg)
          val _tmpVitAMcg: Double
          _tmpVitAMcg = _stmt.getDouble(_columnIndexOfVitAMcg)
          val _tmpVitCMg: Double
          _tmpVitCMg = _stmt.getDouble(_columnIndexOfVitCMg)
          val _tmpVitDMcg: Double
          _tmpVitDMcg = _stmt.getDouble(_columnIndexOfVitDMcg)
          val _tmpVitB12Mcg: Double
          _tmpVitB12Mcg = _stmt.getDouble(_columnIndexOfVitB12Mcg)
          val _tmpFolateMcg: Double
          _tmpFolateMcg = _stmt.getDouble(_columnIndexOfFolateMcg)
          val _tmpOmega3G: Double
          _tmpOmega3G = _stmt.getDouble(_columnIndexOfOmega3G)
          _tmpMicros =
              Micros(_tmpFiberG,_tmpSugarG,_tmpSatFatG,_tmpSodiumMg,_tmpPotassiumMg,_tmpCalciumMg,_tmpIronMg,_tmpMagnesiumMg,_tmpZincMg,_tmpVitAMcg,_tmpVitCMg,_tmpVitDMcg,_tmpVitB12Mcg,_tmpFolateMcg,_tmpOmega3G)
          _item =
              FoodItem(_tmpId,_tmpName,_tmpUnitLabel,_tmpKcal,_tmpProteinG,_tmpCarbsG,_tmpFatG,_tmpCategory,_tmpIsCustom,_tmpIsSupplement,_tmpMicros,_tmpAliases,_tmpSource)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun foodCount(): Int {
    val _sql: String = "SELECT COUNT(*) FROM food_item"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun foodNames(): List<String> {
    val _sql: String = "SELECT name FROM food_item"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun foodLogFor(day: Long): Flow<List<FoodLogRow>> {
    val _sql: String = """
        |
        |        SELECT l.id AS logId, l.dateEpochDay AS dateEpochDay, l.meal AS meal,
        |               l.quantity AS quantity, l.loggedAt AS loggedAt,
        |               f.id AS foodId, f.name AS name, f.unitLabel AS unitLabel,
        |               f.kcal AS kcal, f.proteinG AS proteinG, f.carbsG AS carbsG, f.fatG AS fatG,
        |               f.isSupplement AS isSupplement,
        |               f.fiberG AS fiberG, f.sugarG AS sugarG, f.satFatG AS satFatG,
        |               f.sodiumMg AS sodiumMg, f.potassiumMg AS potassiumMg, f.calciumMg AS calciumMg,
        |               f.ironMg AS ironMg, f.magnesiumMg AS magnesiumMg, f.zincMg AS zincMg,
        |               f.vitAMcg AS vitAMcg, f.vitCMg AS vitCMg, f.vitDMcg AS vitDMcg,
        |               f.vitB12Mcg AS vitB12Mcg, f.folateMcg AS folateMcg, f.omega3G AS omega3G
        |        FROM food_log l
        |        JOIN food_item f ON f.id = l.foodId
        |        WHERE l.dateEpochDay = ?
        |        ORDER BY l.loggedAt ASC
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("food_log", "food_item")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, day)
        val _columnIndexOfLogId: Int = 0
        val _columnIndexOfDateEpochDay: Int = 1
        val _columnIndexOfMeal: Int = 2
        val _columnIndexOfQuantity: Int = 3
        val _columnIndexOfLoggedAt: Int = 4
        val _columnIndexOfFoodId: Int = 5
        val _columnIndexOfName: Int = 6
        val _columnIndexOfUnitLabel: Int = 7
        val _columnIndexOfKcal: Int = 8
        val _columnIndexOfProteinG: Int = 9
        val _columnIndexOfCarbsG: Int = 10
        val _columnIndexOfFatG: Int = 11
        val _columnIndexOfIsSupplement: Int = 12
        val _columnIndexOfFiberG: Int = 13
        val _columnIndexOfSugarG: Int = 14
        val _columnIndexOfSatFatG: Int = 15
        val _columnIndexOfSodiumMg: Int = 16
        val _columnIndexOfPotassiumMg: Int = 17
        val _columnIndexOfCalciumMg: Int = 18
        val _columnIndexOfIronMg: Int = 19
        val _columnIndexOfMagnesiumMg: Int = 20
        val _columnIndexOfZincMg: Int = 21
        val _columnIndexOfVitAMcg: Int = 22
        val _columnIndexOfVitCMg: Int = 23
        val _columnIndexOfVitDMcg: Int = 24
        val _columnIndexOfVitB12Mcg: Int = 25
        val _columnIndexOfFolateMcg: Int = 26
        val _columnIndexOfOmega3G: Int = 27
        val _result: MutableList<FoodLogRow> = mutableListOf()
        while (_stmt.step()) {
          val _item: FoodLogRow
          val _tmpLogId: Long
          _tmpLogId = _stmt.getLong(_columnIndexOfLogId)
          val _tmpDateEpochDay: Long
          _tmpDateEpochDay = _stmt.getLong(_columnIndexOfDateEpochDay)
          val _tmpMeal: String
          _tmpMeal = _stmt.getText(_columnIndexOfMeal)
          val _tmpQuantity: Double
          _tmpQuantity = _stmt.getDouble(_columnIndexOfQuantity)
          val _tmpLoggedAt: Long
          _tmpLoggedAt = _stmt.getLong(_columnIndexOfLoggedAt)
          val _tmpFoodId: Long
          _tmpFoodId = _stmt.getLong(_columnIndexOfFoodId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpUnitLabel: String
          _tmpUnitLabel = _stmt.getText(_columnIndexOfUnitLabel)
          val _tmpKcal: Double
          _tmpKcal = _stmt.getDouble(_columnIndexOfKcal)
          val _tmpProteinG: Double
          _tmpProteinG = _stmt.getDouble(_columnIndexOfProteinG)
          val _tmpCarbsG: Double
          _tmpCarbsG = _stmt.getDouble(_columnIndexOfCarbsG)
          val _tmpFatG: Double
          _tmpFatG = _stmt.getDouble(_columnIndexOfFatG)
          val _tmpIsSupplement: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsSupplement).toInt()
          _tmpIsSupplement = _tmp != 0
          val _tmpMicros: Micros
          val _tmpFiberG: Double
          _tmpFiberG = _stmt.getDouble(_columnIndexOfFiberG)
          val _tmpSugarG: Double
          _tmpSugarG = _stmt.getDouble(_columnIndexOfSugarG)
          val _tmpSatFatG: Double
          _tmpSatFatG = _stmt.getDouble(_columnIndexOfSatFatG)
          val _tmpSodiumMg: Double
          _tmpSodiumMg = _stmt.getDouble(_columnIndexOfSodiumMg)
          val _tmpPotassiumMg: Double
          _tmpPotassiumMg = _stmt.getDouble(_columnIndexOfPotassiumMg)
          val _tmpCalciumMg: Double
          _tmpCalciumMg = _stmt.getDouble(_columnIndexOfCalciumMg)
          val _tmpIronMg: Double
          _tmpIronMg = _stmt.getDouble(_columnIndexOfIronMg)
          val _tmpMagnesiumMg: Double
          _tmpMagnesiumMg = _stmt.getDouble(_columnIndexOfMagnesiumMg)
          val _tmpZincMg: Double
          _tmpZincMg = _stmt.getDouble(_columnIndexOfZincMg)
          val _tmpVitAMcg: Double
          _tmpVitAMcg = _stmt.getDouble(_columnIndexOfVitAMcg)
          val _tmpVitCMg: Double
          _tmpVitCMg = _stmt.getDouble(_columnIndexOfVitCMg)
          val _tmpVitDMcg: Double
          _tmpVitDMcg = _stmt.getDouble(_columnIndexOfVitDMcg)
          val _tmpVitB12Mcg: Double
          _tmpVitB12Mcg = _stmt.getDouble(_columnIndexOfVitB12Mcg)
          val _tmpFolateMcg: Double
          _tmpFolateMcg = _stmt.getDouble(_columnIndexOfFolateMcg)
          val _tmpOmega3G: Double
          _tmpOmega3G = _stmt.getDouble(_columnIndexOfOmega3G)
          _tmpMicros =
              Micros(_tmpFiberG,_tmpSugarG,_tmpSatFatG,_tmpSodiumMg,_tmpPotassiumMg,_tmpCalciumMg,_tmpIronMg,_tmpMagnesiumMg,_tmpZincMg,_tmpVitAMcg,_tmpVitCMg,_tmpVitDMcg,_tmpVitB12Mcg,_tmpFolateMcg,_tmpOmega3G)
          _item =
              FoodLogRow(_tmpLogId,_tmpDateEpochDay,_tmpMeal,_tmpQuantity,_tmpLoggedAt,_tmpFoodId,_tmpName,_tmpUnitLabel,_tmpKcal,_tmpProteinG,_tmpCarbsG,_tmpFatG,_tmpIsSupplement,_tmpMicros)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun mostLoggedFoods(limit: Int): Flow<List<FoodItem>> {
    val _sql: String = """
        |
        |        SELECT f.* FROM food_log l
        |        JOIN food_item f ON f.id = l.foodId
        |        GROUP BY f.id
        |        ORDER BY COUNT(l.id) DESC, MAX(l.loggedAt) DESC
        |        LIMIT ?
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("food_log", "food_item")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfUnitLabel: Int = getColumnIndexOrThrow(_stmt, "unitLabel")
        val _columnIndexOfKcal: Int = getColumnIndexOrThrow(_stmt, "kcal")
        val _columnIndexOfProteinG: Int = getColumnIndexOrThrow(_stmt, "proteinG")
        val _columnIndexOfCarbsG: Int = getColumnIndexOrThrow(_stmt, "carbsG")
        val _columnIndexOfFatG: Int = getColumnIndexOrThrow(_stmt, "fatG")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsCustom: Int = getColumnIndexOrThrow(_stmt, "isCustom")
        val _columnIndexOfIsSupplement: Int = getColumnIndexOrThrow(_stmt, "isSupplement")
        val _columnIndexOfAliases: Int = getColumnIndexOrThrow(_stmt, "aliases")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfFiberG: Int = getColumnIndexOrThrow(_stmt, "fiberG")
        val _columnIndexOfSugarG: Int = getColumnIndexOrThrow(_stmt, "sugarG")
        val _columnIndexOfSatFatG: Int = getColumnIndexOrThrow(_stmt, "satFatG")
        val _columnIndexOfSodiumMg: Int = getColumnIndexOrThrow(_stmt, "sodiumMg")
        val _columnIndexOfPotassiumMg: Int = getColumnIndexOrThrow(_stmt, "potassiumMg")
        val _columnIndexOfCalciumMg: Int = getColumnIndexOrThrow(_stmt, "calciumMg")
        val _columnIndexOfIronMg: Int = getColumnIndexOrThrow(_stmt, "ironMg")
        val _columnIndexOfMagnesiumMg: Int = getColumnIndexOrThrow(_stmt, "magnesiumMg")
        val _columnIndexOfZincMg: Int = getColumnIndexOrThrow(_stmt, "zincMg")
        val _columnIndexOfVitAMcg: Int = getColumnIndexOrThrow(_stmt, "vitAMcg")
        val _columnIndexOfVitCMg: Int = getColumnIndexOrThrow(_stmt, "vitCMg")
        val _columnIndexOfVitDMcg: Int = getColumnIndexOrThrow(_stmt, "vitDMcg")
        val _columnIndexOfVitB12Mcg: Int = getColumnIndexOrThrow(_stmt, "vitB12Mcg")
        val _columnIndexOfFolateMcg: Int = getColumnIndexOrThrow(_stmt, "folateMcg")
        val _columnIndexOfOmega3G: Int = getColumnIndexOrThrow(_stmt, "omega3G")
        val _result: MutableList<FoodItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: FoodItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpUnitLabel: String
          _tmpUnitLabel = _stmt.getText(_columnIndexOfUnitLabel)
          val _tmpKcal: Double
          _tmpKcal = _stmt.getDouble(_columnIndexOfKcal)
          val _tmpProteinG: Double
          _tmpProteinG = _stmt.getDouble(_columnIndexOfProteinG)
          val _tmpCarbsG: Double
          _tmpCarbsG = _stmt.getDouble(_columnIndexOfCarbsG)
          val _tmpFatG: Double
          _tmpFatG = _stmt.getDouble(_columnIndexOfFatG)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpIsCustom: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCustom).toInt()
          _tmpIsCustom = _tmp != 0
          val _tmpIsSupplement: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsSupplement).toInt()
          _tmpIsSupplement = _tmp_1 != 0
          val _tmpAliases: String
          _tmpAliases = _stmt.getText(_columnIndexOfAliases)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpMicros: Micros
          val _tmpFiberG: Double
          _tmpFiberG = _stmt.getDouble(_columnIndexOfFiberG)
          val _tmpSugarG: Double
          _tmpSugarG = _stmt.getDouble(_columnIndexOfSugarG)
          val _tmpSatFatG: Double
          _tmpSatFatG = _stmt.getDouble(_columnIndexOfSatFatG)
          val _tmpSodiumMg: Double
          _tmpSodiumMg = _stmt.getDouble(_columnIndexOfSodiumMg)
          val _tmpPotassiumMg: Double
          _tmpPotassiumMg = _stmt.getDouble(_columnIndexOfPotassiumMg)
          val _tmpCalciumMg: Double
          _tmpCalciumMg = _stmt.getDouble(_columnIndexOfCalciumMg)
          val _tmpIronMg: Double
          _tmpIronMg = _stmt.getDouble(_columnIndexOfIronMg)
          val _tmpMagnesiumMg: Double
          _tmpMagnesiumMg = _stmt.getDouble(_columnIndexOfMagnesiumMg)
          val _tmpZincMg: Double
          _tmpZincMg = _stmt.getDouble(_columnIndexOfZincMg)
          val _tmpVitAMcg: Double
          _tmpVitAMcg = _stmt.getDouble(_columnIndexOfVitAMcg)
          val _tmpVitCMg: Double
          _tmpVitCMg = _stmt.getDouble(_columnIndexOfVitCMg)
          val _tmpVitDMcg: Double
          _tmpVitDMcg = _stmt.getDouble(_columnIndexOfVitDMcg)
          val _tmpVitB12Mcg: Double
          _tmpVitB12Mcg = _stmt.getDouble(_columnIndexOfVitB12Mcg)
          val _tmpFolateMcg: Double
          _tmpFolateMcg = _stmt.getDouble(_columnIndexOfFolateMcg)
          val _tmpOmega3G: Double
          _tmpOmega3G = _stmt.getDouble(_columnIndexOfOmega3G)
          _tmpMicros =
              Micros(_tmpFiberG,_tmpSugarG,_tmpSatFatG,_tmpSodiumMg,_tmpPotassiumMg,_tmpCalciumMg,_tmpIronMg,_tmpMagnesiumMg,_tmpZincMg,_tmpVitAMcg,_tmpVitCMg,_tmpVitDMcg,_tmpVitB12Mcg,_tmpFolateMcg,_tmpOmega3G)
          _item =
              FoodItem(_tmpId,_tmpName,_tmpUnitLabel,_tmpKcal,_tmpProteinG,_tmpCarbsG,_tmpFatG,_tmpCategory,_tmpIsCustom,_tmpIsSupplement,_tmpMicros,_tmpAliases,_tmpSource)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun allLoggedFoodDays(): Flow<List<Long>> {
    val _sql: String = "SELECT DISTINCT dateEpochDay FROM food_log ORDER BY dateEpochDay DESC"
    return createFlow(__db, false, arrayOf("food_log")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<Long> = mutableListOf()
        while (_stmt.step()) {
          val _item: Long
          _item = _stmt.getLong(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun splits(): Flow<List<Split>> {
    val _sql: String = "SELECT * FROM split ORDER BY position, name"
    return createFlow(__db, false, arrayOf("split")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfIsCustom: Int = getColumnIndexOrThrow(_stmt, "isCustom")
        val _result: MutableList<Split> = mutableListOf()
        while (_stmt.step()) {
          val _item: Split
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          val _tmpIsCustom: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCustom).toInt()
          _tmpIsCustom = _tmp != 0
          _item = Split(_tmpId,_tmpName,_tmpPosition,_tmpIsCustom)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun splitCount(): Int {
    val _sql: String = "SELECT COUNT(*) FROM split"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun splitExercises(): Flow<List<SplitExercise>> {
    val _sql: String = "SELECT * FROM split_exercise ORDER BY splitId, position"
    return createFlow(__db, false, arrayOf("split_exercise")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSplitId: Int = getColumnIndexOrThrow(_stmt, "splitId")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _result: MutableList<SplitExercise> = mutableListOf()
        while (_stmt.step()) {
          val _item: SplitExercise
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSplitId: Long
          _tmpSplitId = _stmt.getLong(_columnIndexOfSplitId)
          val _tmpExerciseId: Long
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId)
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          _item = SplitExercise(_tmpId,_tmpSplitId,_tmpExerciseId,_tmpPosition)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun weights(): Flow<List<BodyWeight>> {
    val _sql: String = "SELECT * FROM body_weight ORDER BY dateEpochDay ASC"
    return createFlow(__db, false, arrayOf("body_weight")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDateEpochDay: Int = getColumnIndexOrThrow(_stmt, "dateEpochDay")
        val _columnIndexOfWeightKg: Int = getColumnIndexOrThrow(_stmt, "weightKg")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _result: MutableList<BodyWeight> = mutableListOf()
        while (_stmt.step()) {
          val _item: BodyWeight
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDateEpochDay: Long
          _tmpDateEpochDay = _stmt.getLong(_columnIndexOfDateEpochDay)
          val _tmpWeightKg: Double
          _tmpWeightKg = _stmt.getDouble(_columnIndexOfWeightKg)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          _item = BodyWeight(_tmpId,_tmpDateEpochDay,_tmpWeightKg,_tmpNote)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun latestWeight(): Flow<BodyWeight?> {
    val _sql: String = "SELECT * FROM body_weight ORDER BY dateEpochDay DESC LIMIT 1"
    return createFlow(__db, false, arrayOf("body_weight")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDateEpochDay: Int = getColumnIndexOrThrow(_stmt, "dateEpochDay")
        val _columnIndexOfWeightKg: Int = getColumnIndexOrThrow(_stmt, "weightKg")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _result: BodyWeight?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDateEpochDay: Long
          _tmpDateEpochDay = _stmt.getLong(_columnIndexOfDateEpochDay)
          val _tmpWeightKg: Double
          _tmpWeightKg = _stmt.getDouble(_columnIndexOfWeightKg)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          _result = BodyWeight(_tmpId,_tmpDateEpochDay,_tmpWeightKg,_tmpNote)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun progressPhotos(): Flow<List<ProgressPhoto>> {
    val _sql: String = "SELECT * FROM progress_photo ORDER BY dateEpochDay DESC, takenAt DESC"
    return createFlow(__db, false, arrayOf("progress_photo")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDateEpochDay: Int = getColumnIndexOrThrow(_stmt, "dateEpochDay")
        val _columnIndexOfFileName: Int = getColumnIndexOrThrow(_stmt, "fileName")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfTakenAt: Int = getColumnIndexOrThrow(_stmt, "takenAt")
        val _result: MutableList<ProgressPhoto> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProgressPhoto
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDateEpochDay: Long
          _tmpDateEpochDay = _stmt.getLong(_columnIndexOfDateEpochDay)
          val _tmpFileName: String
          _tmpFileName = _stmt.getText(_columnIndexOfFileName)
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpTakenAt: Long
          _tmpTakenAt = _stmt.getLong(_columnIndexOfTakenAt)
          _item = ProgressPhoto(_tmpId,_tmpDateEpochDay,_tmpFileName,_tmpLabel,_tmpNote,_tmpTakenAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun progressPhotoById(id: Long): ProgressPhoto? {
    val _sql: String = "SELECT * FROM progress_photo WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDateEpochDay: Int = getColumnIndexOrThrow(_stmt, "dateEpochDay")
        val _columnIndexOfFileName: Int = getColumnIndexOrThrow(_stmt, "fileName")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfTakenAt: Int = getColumnIndexOrThrow(_stmt, "takenAt")
        val _result: ProgressPhoto?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDateEpochDay: Long
          _tmpDateEpochDay = _stmt.getLong(_columnIndexOfDateEpochDay)
          val _tmpFileName: String
          _tmpFileName = _stmt.getText(_columnIndexOfFileName)
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpTakenAt: Long
          _tmpTakenAt = _stmt.getLong(_columnIndexOfTakenAt)
          _result =
              ProgressPhoto(_tmpId,_tmpDateEpochDay,_tmpFileName,_tmpLabel,_tmpNote,_tmpTakenAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun referencedPhotoFileNames(): List<String> {
    val _sql: String =
        "SELECT fileName FROM progress_photo UNION SELECT fileName FROM exercise_photo"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun exercisePhotos(): Flow<List<ExercisePhoto>> {
    val _sql: String = "SELECT * FROM exercise_photo"
    return createFlow(__db, false, arrayOf("exercise_photo")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfFileName: Int = getColumnIndexOrThrow(_stmt, "fileName")
        val _columnIndexOfAddedAt: Int = getColumnIndexOrThrow(_stmt, "addedAt")
        val _result: MutableList<ExercisePhoto> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExercisePhoto
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpExerciseId: Long
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId)
          val _tmpFileName: String
          _tmpFileName = _stmt.getText(_columnIndexOfFileName)
          val _tmpAddedAt: Long
          _tmpAddedAt = _stmt.getLong(_columnIndexOfAddedAt)
          _item = ExercisePhoto(_tmpId,_tmpExerciseId,_tmpFileName,_tmpAddedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun exercisePhotoFor(exerciseId: Long): ExercisePhoto? {
    val _sql: String = "SELECT * FROM exercise_photo WHERE exerciseId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, exerciseId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfFileName: Int = getColumnIndexOrThrow(_stmt, "fileName")
        val _columnIndexOfAddedAt: Int = getColumnIndexOrThrow(_stmt, "addedAt")
        val _result: ExercisePhoto?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpExerciseId: Long
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId)
          val _tmpFileName: String
          _tmpFileName = _stmt.getText(_columnIndexOfFileName)
          val _tmpAddedAt: Long
          _tmpAddedAt = _stmt.getLong(_columnIndexOfAddedAt)
          _result = ExercisePhoto(_tmpId,_tmpExerciseId,_tmpFileName,_tmpAddedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun dayMarkers(): Flow<List<DayMarker>> {
    val _sql: String = "SELECT * FROM day_marker ORDER BY dateEpochDay DESC"
    return createFlow(__db, false, arrayOf("day_marker")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDateEpochDay: Int = getColumnIndexOrThrow(_stmt, "dateEpochDay")
        val _columnIndexOfKind: Int = getColumnIndexOrThrow(_stmt, "kind")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfMarkedAt: Int = getColumnIndexOrThrow(_stmt, "markedAt")
        val _result: MutableList<DayMarker> = mutableListOf()
        while (_stmt.step()) {
          val _item: DayMarker
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDateEpochDay: Long
          _tmpDateEpochDay = _stmt.getLong(_columnIndexOfDateEpochDay)
          val _tmpKind: String
          _tmpKind = _stmt.getText(_columnIndexOfKind)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpMarkedAt: Long
          _tmpMarkedAt = _stmt.getLong(_columnIndexOfMarkedAt)
          _item = DayMarker(_tmpId,_tmpDateEpochDay,_tmpKind,_tmpNote,_tmpMarkedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun dayMarkerFor(day: Long): DayMarker? {
    val _sql: String = "SELECT * FROM day_marker WHERE dateEpochDay = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, day)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDateEpochDay: Int = getColumnIndexOrThrow(_stmt, "dateEpochDay")
        val _columnIndexOfKind: Int = getColumnIndexOrThrow(_stmt, "kind")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfMarkedAt: Int = getColumnIndexOrThrow(_stmt, "markedAt")
        val _result: DayMarker?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDateEpochDay: Long
          _tmpDateEpochDay = _stmt.getLong(_columnIndexOfDateEpochDay)
          val _tmpKind: String
          _tmpKind = _stmt.getText(_columnIndexOfKind)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpMarkedAt: Long
          _tmpMarkedAt = _stmt.getLong(_columnIndexOfMarkedAt)
          _result = DayMarker(_tmpId,_tmpDateEpochDay,_tmpKind,_tmpNote,_tmpMarkedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun allWorkingSets(): Flow<List<SetRow>> {
    val _sql: String = """
        |
        |        SELECT s.id AS setId, s.sessionId AS sessionId, s.exerciseId AS exerciseId,
        |               e.name AS exerciseName, s.setIndex AS setIndex, s.weightKg AS weightKg,
        |               s.reps AS reps, s.isWarmup AS isWarmup, s.loggedAt AS loggedAt,
        |               s.variation AS variation, e.isAssisted AS isAssisted
        |        FROM set_entry s
        |        JOIN exercise e ON e.id = s.exerciseId
        |        WHERE s.isWarmup = 0
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("set_entry", "exercise")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfSetId: Int = 0
        val _columnIndexOfSessionId: Int = 1
        val _columnIndexOfExerciseId: Int = 2
        val _columnIndexOfExerciseName: Int = 3
        val _columnIndexOfSetIndex: Int = 4
        val _columnIndexOfWeightKg: Int = 5
        val _columnIndexOfReps: Int = 6
        val _columnIndexOfIsWarmup: Int = 7
        val _columnIndexOfLoggedAt: Int = 8
        val _columnIndexOfVariation: Int = 9
        val _columnIndexOfIsAssisted: Int = 10
        val _result: MutableList<SetRow> = mutableListOf()
        while (_stmt.step()) {
          val _item: SetRow
          val _tmpSetId: Long
          _tmpSetId = _stmt.getLong(_columnIndexOfSetId)
          val _tmpSessionId: Long
          _tmpSessionId = _stmt.getLong(_columnIndexOfSessionId)
          val _tmpExerciseId: Long
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId)
          val _tmpExerciseName: String
          _tmpExerciseName = _stmt.getText(_columnIndexOfExerciseName)
          val _tmpSetIndex: Int
          _tmpSetIndex = _stmt.getLong(_columnIndexOfSetIndex).toInt()
          val _tmpWeightKg: Double
          _tmpWeightKg = _stmt.getDouble(_columnIndexOfWeightKg)
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpIsWarmup: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsWarmup).toInt()
          _tmpIsWarmup = _tmp != 0
          val _tmpLoggedAt: Long
          _tmpLoggedAt = _stmt.getLong(_columnIndexOfLoggedAt)
          val _tmpVariation: String
          _tmpVariation = _stmt.getText(_columnIndexOfVariation)
          val _tmpIsAssisted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsAssisted).toInt()
          _tmpIsAssisted = _tmp_1 != 0
          _item =
              SetRow(_tmpSetId,_tmpSessionId,_tmpExerciseId,_tmpExerciseName,_tmpSetIndex,_tmpWeightKg,_tmpReps,_tmpIsWarmup,_tmpLoggedAt,_tmpVariation,_tmpIsAssisted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun dailyMacros(): Flow<List<DayMacroRow>> {
    val _sql: String = """
        |
        |        SELECT l.dateEpochDay AS dateEpochDay,
        |               SUM(f.kcal * l.quantity) AS kcal,
        |               SUM(f.proteinG * l.quantity) AS proteinG
        |        FROM food_log l
        |        JOIN food_item f ON f.id = l.foodId
        |        GROUP BY l.dateEpochDay
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("food_log", "food_item")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfDateEpochDay: Int = 0
        val _columnIndexOfKcal: Int = 1
        val _columnIndexOfProteinG: Int = 2
        val _result: MutableList<DayMacroRow> = mutableListOf()
        while (_stmt.step()) {
          val _item: DayMacroRow
          val _tmpDateEpochDay: Long
          _tmpDateEpochDay = _stmt.getLong(_columnIndexOfDateEpochDay)
          val _tmpKcal: Double
          _tmpKcal = _stmt.getDouble(_columnIndexOfKcal)
          val _tmpProteinG: Double
          _tmpProteinG = _stmt.getDouble(_columnIndexOfProteinG)
          _item = DayMacroRow(_tmpDateEpochDay,_tmpKcal,_tmpProteinG)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun sessionDays(): Flow<List<SessionDay>> {
    val _sql: String = "SELECT id, dateEpochDay FROM workout_session"
    return createFlow(__db, false, arrayOf("workout_session")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = 0
        val _columnIndexOfDateEpochDay: Int = 1
        val _result: MutableList<SessionDay> = mutableListOf()
        while (_stmt.step()) {
          val _item: SessionDay
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDateEpochDay: Long
          _tmpDateEpochDay = _stmt.getLong(_columnIndexOfDateEpochDay)
          _item = SessionDay(_tmpId,_tmpDateEpochDay)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun editSet(
    id: Long,
    weightKg: Double,
    reps: Int,
    isWarmup: Boolean,
    variation: String,
  ) {
    val _sql: String =
        "UPDATE set_entry SET weightKg = ?, reps = ?, isWarmup = ?, variation = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindDouble(_argIndex, weightKg)
        _argIndex = 2
        _stmt.bindLong(_argIndex, reps.toLong())
        _argIndex = 3
        val _tmp: Int = if (isWarmup) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 4
        _stmt.bindText(_argIndex, variation)
        _argIndex = 5
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteSessionRow(id: Long) {
    val _sql: String = "DELETE FROM workout_session WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteSetsForSession(sessionId: Long) {
    val _sql: String = "DELETE FROM set_entry WHERE sessionId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sessionId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteFoodLog(id: Long) {
    val _sql: String = "DELETE FROM food_log WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun editFoodLog(
    id: Long,
    quantity: Double,
    meal: String,
  ) {
    val _sql: String = "UPDATE food_log SET quantity = ?, meal = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindDouble(_argIndex, quantity)
        _argIndex = 2
        _stmt.bindText(_argIndex, meal)
        _argIndex = 3
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteSplitRow(id: Long) {
    val _sql: String = "DELETE FROM split WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearSplitExercises(splitId: Long) {
    val _sql: String = "DELETE FROM split_exercise WHERE splitId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, splitId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun detachSessionsFromSplit(splitId: Long) {
    val _sql: String = "UPDATE workout_session SET splitId = NULL WHERE splitId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, splitId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteProgressPhotoRow(id: Long) {
    val _sql: String = "DELETE FROM progress_photo WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun editProgressPhoto(
    id: Long,
    label: String,
    note: String,
  ) {
    val _sql: String = "UPDATE progress_photo SET label = ?, note = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, label)
        _argIndex = 2
        _stmt.bindText(_argIndex, note)
        _argIndex = 3
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteExercisePhotoRow(exerciseId: Long) {
    val _sql: String = "DELETE FROM exercise_photo WHERE exerciseId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, exerciseId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearDayMarker(day: Long) {
    val _sql: String = "DELETE FROM day_marker WHERE dateEpochDay = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, day)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
