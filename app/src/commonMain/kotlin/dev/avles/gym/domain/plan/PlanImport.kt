package dev.avles.gym.domain.plan

import dev.avles.gym.core.epochDay
import dev.avles.gym.core.xlsx.SheetData
import dev.avles.gym.core.xlsx.SheetRow
import dev.avles.gym.core.xlsx.Xlsx
import dev.avles.gym.core.xlsx.XmlException
import dev.avles.gym.core.zip.ZipException
import dev.avles.gym.data.Exercise
import dev.avles.gym.data.PlanDay
import dev.avles.gym.data.PlanLift
import dev.avles.gym.domain.plan.PlanSheets.Column
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** Something about one row of the file. [skipped] rows were left out; the rest were imported with a caveat. */
data class PlanProblem(val sheet: String, val row: Int, val message: String, val skipped: Boolean) {
    override fun toString(): String = "$sheet row $row: $message"
}

/**
 * A split the plan defines: a day's focus and the lifts planned under it, taken
 * from the first date that has that focus and lifts. Saved under Train > Your
 * splits on import, so a session can be started from it.
 */
class PlanSplit(val name: String, val lifts: List<PlanLift>) {
    /** The catalogue lifts, in order and once each; a split can only hold lifts the app knows. */
    val exerciseIds: List<Long> get() = lifts.mapNotNull { it.exerciseId }.distinct()
}

/**
 * What a plan file turned into: the rows ready to save, the splits it defines,
 * everything worth telling the person, and [fatal] when the file could not be read
 * at all. Every date in [dates] is replaced as a whole when this is saved, so
 * importing an edited plan again simply overwrites it.
 */
class PlanImportResult(
    val days: List<PlanDay>,
    val lifts: List<PlanLift>,
    val problems: List<PlanProblem>,
    val fatal: String? = null,
    val splits: List<PlanSplit> = emptyList()
) {
    val dates: Set<Long> = (days.map { it.dateEpochDay } + lifts.map { it.dateEpochDay }).toSet()
    val isEmpty: Boolean get() = dates.isEmpty()
    val skippedCount: Int get() = problems.count { it.skipped }
    val firstDate: Long? get() = dates.minOrNull()

    fun summary(): String =
        "${dates.size} ${plural(dates.size, "day")}, ${lifts.size} ${plural(lifts.size, "exercise")}, " +
            "${splits.size} ${plural(splits.size, "split")}"

    /**
     * The same plan moved so that its first day is [start]: every date shifts by the
     * same number of days, so a rotation keeps its order and its rest days. This is
     * "Start today": a plan written for other dates begins now instead.
     */
    fun startingOn(start: Long): PlanImportResult {
        val first = firstDate ?: return this
        val by = start - first
        if (by == 0L) return this
        return PlanImportResult(
            days = days.map { it.copy(dateEpochDay = it.dateEpochDay + by) },
            lifts = lifts.map { it.copy(dateEpochDay = it.dateEpochDay + by) },
            problems = problems,
            fatal = fatal,
            splits = splits
        )
    }

    private fun plural(n: Int, word: String) = if (n == 1) word else word + "s"

    companion object {
        fun failed(message: String) = PlanImportResult(emptyList(), emptyList(), emptyList(), message)
    }
}

/** The exercise catalogue a plan is matched against, by name reduced with [PlanSheets.nameKey]. */
class PlanCatalogue(exercises: List<Exercise>) {
    private val exerciseByKey: Map<String, Exercise> = exercises.associateBy { PlanSheets.nameKey(it.name) }

    fun exercise(name: String): Exercise? = exerciseByKey[PlanSheets.nameKey(name)]
}

/**
 * Turns a plan spreadsheet into plan rows, strictly.
 *
 * The file comes from outside the app (an AI or a person filled it in), so every
 * cell is treated as untrusted: values are parsed against a fixed set of formats,
 * held to sensible ranges, and cut to length. A row that breaks a rule is skipped
 * and reported with its sheet and row number, so the person can fix exactly that
 * line; one bad row never loses the rest of the plan, and no input can make the
 * import throw. Nothing in the file is ever executed: [Xlsx] reads values only.
 */
object PlanImport {

    const val MAX_DAYS = 120
    const val MAX_LIFTS_PER_DAY = 40
    private const val MAX_ROWS_PER_SHEET = 3_000
    private const val NAME_CHARS = 80
    private const val NOTE_CHARS = 300
    private const val FOCUS_CHARS = 40
    private const val PAST_DAYS = 31
    private const val FUTURE_DAYS = 400

    /** Reads [bytes] as a plan file. Never throws; a file it cannot read comes back with [PlanImportResult.fatal]. */
    fun read(bytes: ByteArray, catalogue: PlanCatalogue, today: LocalDate): PlanImportResult = try {
        parse(Xlsx.read(bytes), catalogue, today)
    } catch (e: ZipException) {
        PlanImportResult.failed("That file could not be opened as an Excel workbook (.xlsx): ${e.message}")
    } catch (e: XmlException) {
        PlanImportResult.failed("That workbook is damaged or not a spreadsheet: ${e.message}")
    } catch (e: Exception) {
        PlanImportResult.failed("That file could not be read as a plan.")
    }

    fun parse(sheets: List<SheetData>, catalogue: PlanCatalogue, today: LocalDate): PlanImportResult {
        fun sheet(name: String) = sheets.firstOrNull { PlanSheets.headerKey(it.name) == PlanSheets.headerKey(name) }
        val week = sheet(PlanSheets.WEEK)
        val workout = sheet(PlanSheets.WORKOUT)
        if (week == null && workout == null) {
            return PlanImportResult.failed(
                "This is not the plan template: it needs a sheet called Week or Workout. " +
                    "Get the template from the Week plan screen and fill that in."
            )
        }
        val ctx = Context(catalogue, today)
        week?.let { ctx.readWeek(it) }
        workout?.let { ctx.readWorkout(it) }
        return ctx.finish()
    }

    private class Context(val catalogue: PlanCatalogue, val today: LocalDate) {
        val problems = ArrayList<PlanProblem>()
        val days = LinkedHashMap<Long, PlanDay>()
        val lifts = ArrayList<Triple<Int, Int, PlanLift>>() // order, row, lift
        val earliest = today.plus(DatePeriod(days = -PAST_DAYS)).epochDay
        val latest = today.plus(DatePeriod(days = FUTURE_DAYS)).epochDay

        fun skip(sheet: String, row: Int, message: String) { problems += PlanProblem(sheet, row, message, skipped = true) }
        fun note(sheet: String, row: Int, message: String) { problems += PlanProblem(sheet, row, message, skipped = false) }

        /** The header row's column for each of [columns], or null (with a problem) when a required one is missing. */
        fun header(sheet: SheetData, columns: List<Column>): Pair<Map<Column, Int>, List<SheetRow>>? {
            val headerRow = sheet.rows.firstOrNull() ?: return null
            val found = HashMap<Column, Int>()
            headerRow.cells.forEachIndexed { i, text ->
                val key = PlanSheets.headerKey(text)
                columns.firstOrNull { key in it.keys && it !in found }?.let { found[it] = i }
            }
            val missing = columns.filter { it.required && it !in found }
            if (missing.isNotEmpty()) {
                skip(sheet.name, headerRow.number, "the sheet was skipped: row ${headerRow.number} must be the header row " +
                    "and needs the column(s) ${missing.joinToString { "\"${it.header}\"" }}")
                return null
            }
            var body = sheet.rows.drop(1)
            if (body.size > MAX_ROWS_PER_SHEET) {
                skip(sheet.name, body[MAX_ROWS_PER_SHEET].number, "only the first $MAX_ROWS_PER_SHEET rows were read")
                body = body.take(MAX_ROWS_PER_SHEET)
            }
            return found to body
        }

        fun date(sheet: String, row: SheetRow, col: Int?): Long? {
            val raw = cell(row, col)
            if (raw.isBlank()) { skip(sheet, row.number, "no date"); return null }
            val d = PlanValues.date(raw)
            if (d == null) { skip(sheet, row.number, "\"$raw\" is not a date; write dates as YYYY-MM-DD, like ${today}"); return null }
            val day = d.epochDay
            if (day < earliest || day > latest) { skip(sheet, row.number, "$d is too far from today to plan"); return null }
            if (day !in days && days.size >= MAX_DAYS) { skip(sheet, row.number, "a plan can cover at most $MAX_DAYS days"); return null }
            return day
        }

        fun ensureDay(day: Long) { if (day !in days) days[day] = PlanDay(dateEpochDay = day, focus = "") }

        fun readWeek(sheet: SheetData) {
            val c = PlanSheets.Week
            val (cols, rows) = header(sheet, c.ALL) ?: return
            rows.forEach { row ->
                // The template arrives with dates and day names already filled in; a row
                // nobody planned anything on is not a plan for that day and is passed over.
                val untouched = listOf(c.FOCUS, c.TIME, c.NOTES).all { cell(row, cols[it]).isBlank() }
                if (untouched) return@forEach
                val day = date(sheet.name, row, cols[c.DATE]) ?: return@forEach
                val focus = PlanValues.clean(cell(row, cols[c.FOCUS]))
                if (focus.length > FOCUS_CHARS) { skip(sheet.name, row.number, "Focus is longer than $FOCUS_CHARS characters"); return@forEach }
                val dayName = cell(row, cols[c.DAY]).trim()
                if (dayName.isNotEmpty() && !PlanValues.weekdayMatches(dayName, day)) {
                    note(sheet.name, row.number, "Day says \"$dayName\" but ${PlanValues.iso(day)} is a ${PlanValues.weekday(day)}; the date was used")
                }
                val rawTime = cell(row, cols[c.TIME])
                val time = PlanValues.time(rawTime)
                if (rawTime.isNotBlank() && time == null) note(sheet.name, row.number, "\"$rawTime\" is not a time like 18:30, so no time was set")
                if (day in days && days.getValue(day).focus.isNotEmpty()) note(sheet.name, row.number, "${PlanValues.iso(day)} appears twice; this row was used")
                days[day] = PlanDay(
                    dateEpochDay = day,
                    focus = focus,
                    workoutTime = time.orEmpty(),
                    notes = PlanValues.clean(cell(row, cols[c.NOTES])).take(NOTE_CHARS)
                )
            }
        }

        fun readWorkout(sheet: SheetData) {
            val c = PlanSheets.Workout
            val (cols, rows) = header(sheet, c.ALL) ?: return
            rows.forEach { row ->
                val day = date(sheet.name, row, cols[c.DATE]) ?: return@forEach
                val name = PlanValues.clean(cell(row, cols[c.EXERCISE]))
                if (name.isEmpty()) { skip(sheet.name, row.number, "no exercise name"); return@forEach }
                if (name.length > NAME_CHARS) { skip(sheet.name, row.number, "the exercise name is longer than $NAME_CHARS characters"); return@forEach }
                val rawSets = cell(row, cols[c.SETS])
                val sets = PlanValues.whole(rawSets, 1, 20)
                if (sets == null) { skip(sheet.name, row.number, "Sets must be a whole number from 1 to 20 (got \"$rawSets\")"); return@forEach }
                val rawReps = cell(row, cols[c.REPS])
                val reps = PlanValues.reps(rawReps)
                if (reps == null) {
                    skip(sheet.name, row.number, "Reps must be a number (8), a range (8-12), seconds (45s) or AMRAP (got \"$rawReps\")" +
                        if (rawReps.toDoubleOrNull()?.let { it > 1000 } == true) "; Excel may have turned a range into a date, so type it as text" else "")
                    return@forEach
                }
                val rawWeight = cell(row, cols[c.WEIGHT])
                val weight = PlanValues.weight(rawWeight)
                if (weight == null) { skip(sheet.name, row.number, "Weight (kg) must be a number from 0 to 1000, or blank (got \"$rawWeight\")"); return@forEach }
                val rawRest = cell(row, cols[c.REST])
                val rest = PlanValues.seconds(rawRest, 0, 900)
                if (rest == null) note(sheet.name, row.number, "Rest \"$rawRest\" is not a number of seconds up to 900, so no rest was set")
                val order = PlanValues.whole(cell(row, cols[c.ORDER]), 1, 999) ?: Int.MAX_VALUE
                if (lifts.count { it.third.dateEpochDay == day } >= MAX_LIFTS_PER_DAY) {
                    skip(sheet.name, row.number, "at most $MAX_LIFTS_PER_DAY exercises a day"); return@forEach
                }
                val match = catalogue.exercise(name)
                if (match == null) note(sheet.name, row.number, "\"$name\" is not in the app's exercise list; it was kept as written")
                ensureDay(day)
                lifts += Triple(order, row.number, PlanLift(
                    dateEpochDay = day,
                    position = 0,
                    exerciseName = match?.name ?: name,
                    exerciseId = match?.id,
                    sets = sets,
                    repsLow = reps.low,
                    repsHigh = reps.high,
                    holdSeconds = reps.holdSeconds,
                    weightKg = weight,
                    restSeconds = rest ?: 0,
                    notes = PlanValues.clean(cell(row, cols[c.NOTES])).take(NOTE_CHARS)
                ))
            }
        }

        fun finish(): PlanImportResult {
            val liftsByDay = lifts
                .sortedWith(compareBy({ it.third.dateEpochDay }, { it.first }, { it.second }))
                .groupBy { it.third.dateEpochDay }
                .mapValues { (_, list) -> list.mapIndexed { i, t -> t.third.copy(position = i) } }
                .toMutableMap()
            val orderedDays = days.values.sortedBy { it.dateEpochDay }

            // Each focus with lifts is a split, defined by the first date that has it.
            val splits = LinkedHashMap<String, PlanSplit>()
            orderedDays.forEach { d ->
                val dayLifts = liftsByDay[d.dateEpochDay].orEmpty()
                val key = PlanSheets.nameKey(d.focus)
                if (key.isNotEmpty() && dayLifts.isNotEmpty() && key !in splits) splits[key] = PlanSplit(d.focus, dayLifts)
            }
            // A day that names a split but has no Workout rows of its own gets that split's
            // lifts, so a month only needs each split's exercises written once.
            orderedDays.forEach { d ->
                if (liftsByDay[d.dateEpochDay].isNullOrEmpty()) {
                    splits[PlanSheets.nameKey(d.focus)]?.let { split ->
                        liftsByDay[d.dateEpochDay] = split.lifts.map { it.copy(dateEpochDay = d.dateEpochDay) }
                    }
                }
            }
            return PlanImportResult(
                days = orderedDays,
                lifts = liftsByDay.keys.sorted().flatMap { liftsByDay.getValue(it) },
                problems = problems.sortedWith(compareBy({ sheetOrder(it.sheet) }, { it.row })),
                splits = splits.values.toList()
            )
        }

        private fun sheetOrder(name: String) = when (PlanSheets.headerKey(name)) {
            PlanSheets.headerKey(PlanSheets.WEEK) -> 0
            else -> 1
        }

        fun cell(row: SheetRow, col: Int?): String = if (col == null) "" else row[col]
    }
}
