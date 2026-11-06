package dev.avles.gym.domain.plan

import dev.avles.gym.core.xlsx.Cell
import dev.avles.gym.core.xlsx.SheetSpec
import dev.avles.gym.core.xlsx.Style
import dev.avles.gym.core.xlsx.Xlsx
import dev.avles.gym.data.Exercise
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * The plan spreadsheet a person downloads, hands to an AI (or fills in by hand) and
 * imports back. Training only. Its first sheet is written for the AI: exactly what
 * to put where, so that the file comes back importable. The Week sheet lists the
 * next month of dates for the split days; the Workout sheet holds each split's
 * exercises; the Exercises sheet lists this phone's catalogue so the AI uses names
 * the app recognises. Each split day becomes a split under Train > Your splits.
 */
object PlanTemplate {

    /** What the template is built from. [weightKg] is the latest weigh-in, if any. */
    class Input(
        val today: LocalDate,
        val exercises: List<Exercise>,
        val weightKg: Double? = null
    )

    /** A month of dates, ready for a split name each. */
    const val DAYS_PREFILLED = 30
    private const val FORMATTED_ROWS = 400

    fun fileName(today: LocalDate): String = "gym-month-plan-$today.xlsx"

    fun build(input: Input): ByteArray = Xlsx.write(sheets(input))

    fun sheets(input: Input): List<SheetSpec> = listOf(
        readMe(input),
        week(input),
        dataSheet(PlanSheets.WORKOUT, PlanSheets.Workout.ALL, emptyList(), textColumns = setOf(0, 4)),
        exercises(input.exercises)
    )

    private fun header(columns: List<PlanSheets.Column>) = columns.map { Cell.Text(it.header, Style.HEADER) }

    /**
     * Empty body cells pre-formatted as text in [textColumns], so a person typing
     * 2026-11-09 or 8-12 into Excel keeps text instead of Excel turning it into a date.
     */
    private fun dataSheet(
        name: String,
        columns: List<PlanSheets.Column>,
        body: List<List<Cell>>,
        textColumns: Set<Int>
    ): SheetSpec {
        val rows = ArrayList<List<Cell>>()
        rows += header(columns)
        rows += body
        while (rows.size <= FORMATTED_ROWS) {
            rows += columns.indices.map { if (it in textColumns) Cell.Text("", Style.TEXT) else Cell.Text("") }
        }
        return SheetSpec(name, rows, widths = columns.map { it.width }, frozenRows = 1)
    }

    private fun week(input: Input): SheetSpec {
        val body = (0 until DAYS_PREFILLED).map { i ->
            val date = input.today.plus(DatePeriod(days = i))
            listOf(Cell.Text(date.toString(), Style.TEXT), Cell.Text(PlanValues.shortWeekday(date)), Cell.Text(""), Cell.Text("", Style.TEXT), Cell.Text(""))
        }
        return dataSheet(PlanSheets.WEEK, PlanSheets.Week.ALL, body, textColumns = setOf(0, 3))
    }

    private fun exercises(all: List<Exercise>): SheetSpec {
        val rows = ArrayList<List<Cell>>()
        rows += listOf(Cell.Text("Exercise name", Style.HEADER), Cell.Text("Muscle group", Style.HEADER), Cell.Text("Bodyweight", Style.HEADER), Cell.Text("Usual reps", Style.HEADER))
        all.sortedWith(compareBy({ it.muscleGroup }, { it.name })).forEach {
            rows += listOf(Cell.Text(it.name), Cell.Text(it.muscleGroup), Cell.Text(if (it.isBodyweight) "yes" else ""), Cell.Text("${it.repLow}-${it.repHigh}"))
        }
        return SheetSpec(PlanSheets.EXERCISES, rows, widths = listOf(34.0, 16.0, 11.0, 11.0), frozenRows = 1)
    }

    // ---- the instructions -----------------------------------------------------

    private fun readMe(input: Input): SheetSpec {
        val rows = ArrayList<List<Cell>>()
        fun title(t: String) { rows += listOf(Cell.Text(t, Style.TITLE)) }
        fun line(t: String) { rows += listOf(Cell.Text(t, Style.WRAP)) }
        fun note(t: String) { rows += listOf(Cell.Text(t, Style.NOTE)) }
        fun gap() { rows += listOf(Cell.Text("")) }
        fun heading(t: String) { rows += listOf(Cell.Text(t, Style.HEADER)) }
        val d0 = input.today
        fun day(n: Int) = d0.plus(DatePeriod(days = n))

        title("Month training plan for the Gym app")
        note("Made by the app on $d0. Fill it in, then import it: Today > Week plan > Import plan.")
        gap()
        heading("How to use this file")
        line("1. Give this file to an AI assistant such as Claude and ask it to plan your split days for the month. If you have it, attach the \"Export everything for AI\" file from Settings > Your data too, so it can see your history.")
        line("2. Ask it to give the filled-in file back as an Excel .xlsx file, and download it.")
        line("3. In the app open Today > Week plan > Import plan and pick the file. Each split becomes a split under Train > Your splits, ready to start. Choose \"Start today\" to begin the plan today whatever dates the file has, or keep the file's dates.")
        gap()
        heading("INSTRUCTIONS FOR THE AI: follow these exactly so the file imports")
        line("You are filling in a training plan that a phone app will import automatically. Training only, no food. Fill only the sheets \"Week\" and \"Workout\". Keep every sheet name and the header row (row 1) of each sheet exactly as they are. Leave \"Read me\" and \"Exercises\" unchanged.")
        line("Return the result as an .xlsx file. Every cell must be a plain value: no formulas, macros, merged cells, extra header rows, totals rows, blank separator rows, or notes below the tables. One row per item. Comments go in the Notes column.")
        line("Dates: write every date as text in the form YYYY-MM-DD (for example $d0). The Week sheet already lists $DAYS_PREFILLED days starting $d0; keep them or add rows (up to ${PlanImport.MAX_DAYS} days).")
        gap()
        heading("Sheet \"Week\": one row per date, the split for that day")
        line("Focus: the split's name, at most 40 characters, written exactly the same every time it repeats, such as \"Day 1 - Lower A\", Push, Pull, Legs, Upper or Full body, or Rest. Rotating splits are fine: just repeat the cycle down the month. Day: Mon to Sun (a check only; the date decides). Workout time: optional, 24-hour HH:MM such as 18:30. Notes: optional.")
        heading("Sheet \"Workout\": each split's exercises, written once")
        line("Write each split's exercises once, on the first date that split appears in the Week sheet. Every later day with the same Focus gets the same exercises automatically, so do not repeat them. Only add Workout rows for a later date if that day should be different. Rest days have no Workout rows.")
        line("Order: 1, 2, 3... Exercise: use the exact name from the \"Exercises\" sheet, so the app can put it in the split and link it to the person's history (a name not in that list is shown in the plan but cannot go in the split). Sets: a whole number from 1 to 20. Reps: a number (8), a range (8-12), seconds for a hold (45s) or AMRAP; write it as text. Weight (kg): a number, or blank for bodyweight or when unsure. Rest (sec): seconds between sets, such as 90. Notes: optional cues.")
        gap()
        heading("About the person")
        line("Goal: get physically stronger" + (input.weightKg?.let { "; latest body weight ${PlanValues.trim(it)} kg" } ?: "") +
            ". They train in a commercial gym with barbells, dumbbells, cables and machines. Ask the person before planning if anything important is missing, such as how many days a week they can train or any injuries.")
        gap()
        heading("Example rows (do not copy these into the sheets unless they fit the plan)")
        note("Week:     $d0 | ${PlanValues.shortWeekday(d0)} | Day 1 - Lower | 18:00 |")
        note("Week:     ${day(1)} | ${PlanValues.shortWeekday(day(1))} | Day 2 - Upper | 18:00 |")
        note("Week:     ${day(2)} | ${PlanValues.shortWeekday(day(2))} | Rest | |")
        note("Week:     ${day(3)} | ${PlanValues.shortWeekday(day(3))} | Day 1 - Lower | 18:00 | (no Workout rows needed: it repeats Day 1)")
        note("Workout:  $d0 | 1 | Back squat | 4 | 6-8 | 60 | 150 | Brace hard, depth to parallel")
        note("Workout:  $d0 | 2 | Romanian deadlift | 3 | 8-10 | 50 | 120 |")
        note("Workout:  $d0 | 3 | Plank | 3 | 45s | | 60 |")
        note("Workout:  ${day(1)} | 1 | Barbell bench press | 4 | 6-8 | 50 | 150 |")
        note("Workout:  ${day(1)} | 2 | Lat pulldown | 3 | 8-12 | 60 | 90 |")
        return SheetSpec(PlanSheets.README, rows, widths = listOf(130.0))
    }
}
