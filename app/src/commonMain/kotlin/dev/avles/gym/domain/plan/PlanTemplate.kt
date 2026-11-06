package dev.avles.gym.domain.plan

import dev.avles.gym.core.xlsx.Cell
import dev.avles.gym.core.xlsx.ListRule
import dev.avles.gym.core.xlsx.SheetSpec
import dev.avles.gym.core.xlsx.Style
import dev.avles.gym.core.xlsx.Xlsx
import dev.avles.gym.data.Exercise
import dev.avles.gym.data.FoodItem
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.math.roundToInt

/**
 * The plan spreadsheet a person downloads, hands to an AI (or fills in by hand) and
 * imports back. Its first sheet is written for the AI: exactly what to put where,
 * in which format, so that the file comes back importable. The Week, Workout and
 * Meals sheets are empty apart from their headers and the next seven dates; the
 * Exercises and Foods sheets list this phone's own catalogue, custom foods and
 * recipes included, so the AI can use names the app will recognise.
 */
object PlanTemplate {

    /** What the template is built from. [weightKg] is the latest weigh-in, if any. */
    class Input(
        val today: LocalDate,
        val exercises: List<Exercise>,
        val foods: List<FoodItem>,
        val proteinTargetG: Double,
        val kcalTarget: Double,
        val weightKg: Double? = null
    )

    const val DAYS_PREFILLED = 7
    private const val FORMATTED_ROWS = 400

    fun fileName(today: LocalDate): String = "gym-week-plan-$today.xlsx"

    fun build(input: Input): ByteArray = Xlsx.write(sheets(input))

    fun sheets(input: Input): List<SheetSpec> = listOf(
        readMe(input),
        week(input),
        dataSheet(PlanSheets.WORKOUT, PlanSheets.Workout.ALL, emptyList(), textColumns = setOf(0, 4)),
        dataSheet(
            PlanSheets.MEALS, PlanSheets.MealsSheet.ALL, emptyList(), textColumns = setOf(0, 2),
            lists = listOf(ListRule("B2:B${FORMATTED_ROWS + 1}", PlanSheets.MEAL_SLOTS))
        ),
        exercises(input.exercises),
        foods(input.foods)
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
        textColumns: Set<Int>,
        lists: List<ListRule> = emptyList()
    ): SheetSpec {
        val rows = ArrayList<List<Cell>>()
        rows += header(columns)
        rows += body
        while (rows.size <= FORMATTED_ROWS) {
            rows += columns.indices.map { if (it in textColumns) Cell.Text("", Style.TEXT) else Cell.Text("") }
        }
        return SheetSpec(name, rows, widths = columns.map { it.width }, frozenRows = 1, lists = lists)
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

    private fun foods(all: List<FoodItem>): SheetSpec {
        val rows = ArrayList<List<Cell>>()
        rows += listOf("Food name", "Unit (Quantity 1 =)", "Calories", "Protein (g)", "Carbs (g)", "Fat (g)", "Category", "Other names")
            .map { Cell.Text(it, Style.HEADER) }
        all.sortedWith(compareBy({ it.category }, { it.name })).forEach {
            rows += listOf(
                Cell.Text(it.name), Cell.Text(it.unitLabel),
                Cell.Number(it.kcal.roundToInt().toDouble()), Cell.Number(round1(it.proteinG)),
                Cell.Number(round1(it.carbsG)), Cell.Number(round1(it.fatG)),
                Cell.Text(it.category), Cell.Text(it.aliases)
            )
        }
        return SheetSpec(PlanSheets.FOODS, rows, widths = listOf(36.0, 20.0, 9.0, 11.0, 9.0, 8.0, 16.0, 40.0), frozenRows = 1)
    }

    private fun round1(d: Double) = (d * 10).roundToInt() / 10.0

    // ---- the instructions -----------------------------------------------------

    private fun readMe(input: Input): SheetSpec {
        val rows = ArrayList<List<Cell>>()
        fun title(t: String) { rows += listOf(Cell.Text(t, Style.TITLE)) }
        fun line(t: String) { rows += listOf(Cell.Text(t, Style.WRAP)) }
        fun note(t: String) { rows += listOf(Cell.Text(t, Style.NOTE)) }
        fun gap() { rows += listOf(Cell.Text("")) }
        fun heading(t: String) { rows += listOf(Cell.Text(t, Style.HEADER)) }

        title("Week plan for the Gym app")
        note("Made by the app on ${input.today}. Fill it in, then import it: Today > Week plan > Import plan.")
        gap()
        heading("How to use this file")
        line("1. Give this whole file to an AI assistant such as Claude and ask it to plan your week (training and food). If you have it, attach the \"Export everything for AI\" file from Settings > Your data too, so it can see your history.")
        line("2. Ask it to give the filled-in file back as an Excel .xlsx file, and download it.")
        line("3. In the app open Today > Week plan > Import plan and pick the file. The app checks every row and tells you about anything it could not use; importing the same dates again replaces them.")
        gap()
        heading("INSTRUCTIONS FOR THE AI: follow these exactly so the file imports")
        line("You are filling in a training and meal plan that a phone app will import automatically. Fill only the sheets \"Week\", \"Workout\" and \"Meals\". Keep every sheet name and the header row (row 1) of each sheet exactly as they are. Leave \"Read me\", \"Exercises\" and \"Foods\" unchanged.")
        line("Return the result as an .xlsx file. Every cell must be a plain value: no formulas, macros, merged cells, extra header rows, totals rows, blank separator rows, or notes below the tables. One row per item. Comments go in the Notes column.")
        line("Dates: write every date as text in the form YYYY-MM-DD (for example ${input.today}). The Week sheet already lists the next $DAYS_PREFILLED days starting ${input.today}; keep them, change them to the week the person asks for, or add rows for more weeks (up to ${PlanImport.MAX_DAYS} days). Use the same dates in all three sheets.")
        gap()
        heading("Sheet \"Week\": one row per date")
        line("Date: YYYY-MM-DD. Day: Mon, Tue, Wed, Thu, Fri, Sat or Sun (a check only; the date decides). Focus: a short name for the day, at most 40 characters, such as Legs, Push, Pull, Upper, Lower, Full body, Chest and back, Cardio or Rest. Workout time: optional, 24-hour HH:MM such as 18:30. Notes: optional.")
        heading("Sheet \"Workout\": one row per exercise per date")
        line("List exercises in the order they should be done, or number them in Order (1, 2, 3...). Exercise: use the exact name from the \"Exercises\" sheet whenever one fits, so the app links it to the person's history; a name not in that list is still imported and shown as written. Sets: a whole number from 1 to 20. Reps: a number (8), a range (8-12), seconds for a hold (45s) or AMRAP; write it as text. Weight (kg): a number, or leave it blank for bodyweight or when unsure. Rest (sec): seconds between sets, such as 90. Notes: optional cues. Rest days have no Workout rows.")
        heading("Sheet \"Meals\": one row per food")
        line("Meal: one of Breakfast, Lunch, Snack, Dinner, Supplements. Time: optional HH:MM. Food: use the exact \"Food name\" from the \"Foods\" sheet. Quantity counts that food's unit, shown in the Foods sheet's \"Unit (Quantity 1 =)\" column: for Idli the unit is \"1 piece\", so Quantity 4 means four idlis; for a food whose unit is \"100 g\", Quantity 1.5 means 150 g. Quantity is a number from 0.05 to 50. Unit: optional, copy it from the Foods sheet so the person can read the plan. For a food that is not in the Foods sheet, write its name and Quantity and fill Calories and Protein (g) for the whole quantity; for listed foods the app works these out itself. Notes: optional.")
        gap()
        heading("About the person")
        line("Goal: get physically stronger. Daily targets in the app: ${input.proteinTargetG.roundToInt()} g protein and ${input.kcalTarget.roundToInt()} kcal" +
            (input.weightKg?.let { ", latest body weight ${PlanValues.trim(it)} kg" } ?: "") + ". Plan meals that meet these targets on training days.")
        line("They mostly eat Tamil Nadu and South Indian food (idli, dosa, pongal, sambar, rasam, rice meals, chapati, egg and chicken dishes, curd, sundal), so plan food they would actually eat, using the names in the Foods sheet. They train in a commercial gym with barbells, dumbbells, cables and machines.")
        line("Ask the person before planning if anything important is missing, such as how many days a week they can train or any injuries.")
        gap()
        heading("Example rows (do not copy these into the sheets unless they fit the plan)")
        note("Week:     ${input.today} | ${PlanValues.shortWeekday(input.today)} | Legs | 18:00 | Heavy day")
        note("Workout:  ${input.today} | 1 | Back squat | 4 | 6-8 | 60 | 150 | Brace hard, depth to parallel")
        note("Workout:  ${input.today} | 2 | Romanian deadlift | 3 | 8-10 | 50 | 120 |")
        note("Workout:  ${input.today} | 3 | Plank | 3 | 45s | | 60 |")
        note("Meals:    ${input.today} | Breakfast | 08:00 | Idli | 4 | 1 piece | | |")
        note("Meals:    ${input.today} | Breakfast | 08:00 | Sambar | 1 | 1 cup | | |")
        note("Meals:    ${input.today} | Lunch | 13:30 | Steamed rice | 1.5 | 1 cup (150 g) | | |")
        note("Meals:    ${input.today} | Lunch | 13:30 | Chicken curry, Chettinad | 1 | 1 cup (200 g) | | |")
        note("Meals:    ${input.today} | Snack | 17:00 | Sundal | 1 | 1 cup | | |")
        note("Meals:    ${input.today} | Dinner | 20:30 | Chapati | 3 | 1 chapati | | |")
        note("Meals:    ${input.today} | Dinner | 20:30 | Home-made paneer bhurji | 1 | 1 bowl | 320 | 20 | not in the list, so calories and protein are given")
        return SheetSpec(PlanSheets.README, rows, widths = listOf(130.0))
    }
}
