package dev.avles.gym

import dev.avles.gym.core.epochDay
import dev.avles.gym.core.xlsx.Cell
import dev.avles.gym.core.xlsx.SheetData
import dev.avles.gym.core.xlsx.SheetRow
import dev.avles.gym.core.xlsx.SheetSpec
import dev.avles.gym.core.xlsx.Xlsx
import dev.avles.gym.data.Exercise
import dev.avles.gym.data.FoodItem
import dev.avles.gym.data.Meals
import dev.avles.gym.data.SeedData
import dev.avles.gym.domain.plan.PlanCatalogue
import dev.avles.gym.domain.plan.PlanImport
import dev.avles.gym.domain.plan.PlanSheets
import dev.avles.gym.domain.plan.PlanTemplate
import dev.avles.gym.domain.plan.PlanValues
import dev.avles.gym.domain.plan.Reps
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The plan import: what an AI-filled spreadsheet turns into, and how strictly it
 * is checked. A good row imports exactly; a bad row is skipped with its sheet and
 * row number; nothing the file holds can make the import throw.
 */
class PlanImportTest {

    private val today = LocalDate(2026, 11, 9) // a Monday
    private val squat = Exercise(id = 1, name = "Back squat", muscleGroup = "Quads")
    private val rdl = Exercise(id = 2, name = "Romanian deadlift", muscleGroup = "Hamstrings")
    private val plank = Exercise(id = 3, name = "Plank", muscleGroup = "Core", isBodyweight = true)
    private val idli = FoodItem(id = 10, name = "Idli", unitLabel = "1 piece", kcal = 58.0, proteinG = 2.0, carbsG = 12.0, fatG = 0.2, category = "Tiffin", aliases = "Idly / Steamed rice cake")
    private val sambar = FoodItem(id = 11, name = "Sambar", unitLabel = "1 cup", kcal = 130.0, proteinG = 6.0, carbsG = 18.0, fatG = 4.0, category = "Sides")
    private val fishOil = FoodItem(id = 12, name = "Fish oil capsule", unitLabel = "1 capsule", kcal = 10.0, proteinG = 0.0, carbsG = 0.0, fatG = 1.0, category = "Supplement", isSupplement = true)
    private val catalogue = PlanCatalogue(listOf(squat, rdl, plank), listOf(idli, sambar, fishOil))

    private fun sheet(name: String, vararg rows: List<String>) =
        SheetData(name, rows.mapIndexed { i, cells -> SheetRow(i + 1, cells) })

    private val weekHeader = listOf("Date", "Day", "Focus", "Workout time", "Notes")
    private val workoutHeader = listOf("Date", "Order", "Exercise", "Sets", "Reps", "Weight (kg)", "Rest (sec)", "Notes")
    private val mealsHeader = listOf("Date", "Meal", "Time", "Food", "Quantity", "Unit", "Calories", "Protein (g)", "Notes")

    @Test
    fun `a well filled plan imports exactly`() {
        val result = PlanImport.parse(
            listOf(
                sheet("Week", weekHeader,
                    listOf("2026-11-09", "Mon", "Legs", "18:00", "Heavy"),
                    listOf("2026-11-10", "Tue", "Push", "", ""),
                    listOf("2026-11-11", "Wed", "Rest", "", "")),
                sheet("Workout", workoutHeader,
                    listOf("2026-11-09", "2", "romanian  deadlift", "3", "8–10", "50", "120", ""),
                    listOf("2026-11-09", "1", "Back Squat", "4", "6-8", "60 kg", "2:30", "Brace"),
                    listOf("2026-11-09", "3", "Plank", "3", "45s", "", "60", "")),
                sheet("Meals", mealsHeader,
                    listOf("2026-11-09", "Dinner", "20:30", "Sambar", "1", "", "", "", ""),
                    listOf("2026-11-09", "breakfast", "8:00", "idly", "4", "1 piece", "", "", ""),
                    listOf("2026-11-09", "Breakfast", "", "Fish oil capsule", "2", "", "", "", ""),
                    listOf("2026-11-09", "Snacks", "", "Sundal from the stall", "1", "", "190", "9", ""))
            ),
            catalogue, today
        )
        assertNull(result.fatal)
        assertEquals(3, result.days.size)
        assertEquals(listOf("Legs", "Push", "Rest"), result.days.map { it.focus })
        assertEquals("18:00", result.days[0].workoutTime)

        assertEquals(listOf("Back squat", "Romanian deadlift", "Plank"), result.lifts.map { it.exerciseName })
        assertEquals(listOf(0, 1, 2), result.lifts.map { it.position })
        val s = result.lifts[0]
        assertEquals(1L, s.exerciseId); assertEquals(4, s.sets); assertEquals(6, s.repsLow); assertEquals(8, s.repsHigh)
        assertEquals(60.0, s.weightKg, 0.0); assertEquals(150, s.restSeconds)
        assertEquals(45, result.lifts[2].holdSeconds)

        val foods = result.foods
        // Slot order, then sheet order; a supplement files itself under Supplements, last.
        assertEquals(listOf("Idli", "Sundal from the stall", "Sambar", "Fish oil capsule"), foods.map { it.foodName })
        assertEquals(Meals.SUPPLEMENT, foods[3].meal)
        assertEquals(232.0, foods[0].kcal, 0.01) // 4 x 58, from the catalogue
        assertEquals(190.0, foods[1].kcal, 0.01) // unknown food: taken from the sheet
        assertNull(foods[1].foodId)
        assertTrue(result.problems.none { it.skipped })
        assertTrue(result.problems.any { it.message.contains("Sundal from the stall") })
    }

    @Test
    fun `bad rows are skipped with their row number, good rows still import`() {
        val result = PlanImport.parse(
            listOf(
                sheet("Workout", workoutHeader,
                    listOf("09/11/2026", "", "Back squat", "4", "6-8", "", "", ""),     // row 2: ambiguous date
                    listOf("2026-11-09", "", "Back squat", "four", "6-8", "", "", ""),  // row 3: sets not a number
                    listOf("2026-11-09", "", "Back squat", "4", "lots", "", "", ""),    // row 4: reps unreadable
                    listOf("2026-11-09", "", "", "4", "6-8", "", "", ""),               // row 5: no name
                    listOf("2026-11-09", "", "Back squat", "4", "6-8", "heavy", "", ""),// row 6: weight unreadable
                    listOf("2031-01-01", "", "Back squat", "4", "6-8", "", "", ""),     // row 7: too far ahead
                    listOf("2026-11-09", "", "Back squat", "4", "6-8", "", "", "")),    // row 8: fine
                sheet("Meals", mealsHeader,
                    listOf("2026-11-09", "Elevenses", "", "Idli", "2", "", "", "", ""), // row 2: unknown slot
                    listOf("2026-11-09", "Lunch", "", "Idli", "0", "", "", "", ""),     // row 3: zero quantity
                    listOf("2026-11-09", "Lunch", "", "Idli", "2", "", "", "", ""))     // row 4: fine
            ),
            catalogue, today
        )
        assertEquals(listOf(2, 3, 4, 5, 6, 7), result.problems.filter { it.skipped && it.sheet == "Workout" }.map { it.row })
        assertEquals(listOf(2, 3), result.problems.filter { it.skipped && it.sheet == "Meals" }.map { it.row })
        assertEquals(1, result.lifts.size)
        assertEquals(1, result.foods.size)
        assertTrue(result.problems.first().message.contains("YYYY-MM-DD"))
    }

    @Test
    fun `a sheet missing a required column is skipped as a whole and says which`() {
        val result = PlanImport.parse(
            listOf(sheet("Workout", listOf("Date", "Exercise", "Sets"), listOf("2026-11-09", "Back squat", "4"))),
            catalogue, today
        )
        assertTrue(result.isEmpty)
        assertTrue(result.problems.single().message.contains("\"Reps\""))
    }

    @Test
    fun `files that are not plans are refused with a reason`() {
        assertNotNull(PlanImport.read("hello".encodeToByteArray(), catalogue, today).fatal)
        assertNotNull(PlanImport.read(ByteArray(0), catalogue, today).fatal)
        val other = Xlsx.write(listOf(SheetSpec("Budget", listOf(listOf(Cell.Text("Rent"))))))
        assertTrue(PlanImport.read(other, catalogue, today).fatal!!.contains("Week"))
    }

    @Test
    fun `the template itself reads back, and an untouched template imports nothing`() {
        val input = PlanTemplate.Input(today, listOf(squat, rdl, plank), listOf(idli, sambar, fishOil), 150.0, 2400.0, 72.5)
        val bytes = PlanTemplate.build(input)
        val sheets = Xlsx.read(bytes)
        assertEquals(
            listOf(PlanSheets.README, PlanSheets.WEEK, PlanSheets.WORKOUT, PlanSheets.MEALS, PlanSheets.EXERCISES, PlanSheets.FOODS),
            sheets.map { it.name }
        )
        val week = sheets.first { it.name == PlanSheets.WEEK }
        assertEquals(PlanSheets.Week.ALL.map { it.header }, week.rows[0].cells.take(5))
        assertEquals("2026-11-09", week.rows[1][0])
        assertEquals("Mon", week.rows[1][1])
        assertEquals(1 + PlanTemplate.DAYS_PREFILLED, week.rows.size)
        assertEquals("Idli", sheets.first { it.name == PlanSheets.FOODS }.rows.drop(1).first { it[0] == "Idli" }[0])
        assertTrue(sheets[0].rows.any { it[0].contains("150 g protein") })

        val untouched = PlanImport.read(bytes, catalogue, today)
        assertNull(untouched.fatal)
        assertTrue(untouched.isEmpty)
        assertTrue(untouched.problems.isEmpty())
    }

    @Test
    fun `a template filled in and saved again imports`() {
        val input = PlanTemplate.Input(today, listOf(squat), listOf(idli), 150.0, 2400.0)
        val filled = PlanTemplate.sheets(input).map { spec ->
            when (spec.name) {
                PlanSheets.WEEK -> SheetSpec(spec.name, listOf(spec.rows[0], listOf(Cell.Text("2026-11-09"), Cell.Text("Mon"), Cell.Text("Legs"))))
                PlanSheets.WORKOUT -> SheetSpec(spec.name, listOf(spec.rows[0],
                    // A date Excel stored as a serial number, and reps as a plain number.
                    listOf(Cell.Number(46335.0), Cell.Text(""), Cell.Text("Back squat"), Cell.Number(5.0), Cell.Number(5.0), Cell.Number(80.0))))
                PlanSheets.MEALS -> SheetSpec(spec.name, listOf(spec.rows[0],
                    listOf(Cell.Text("2026-11-09"), Cell.Text("Breakfast"), Cell.Number(0.3333333), Cell.Text("Idli"), Cell.Number(3.0))))
                else -> spec
            }
        }
        val result = PlanImport.read(Xlsx.write(filled), catalogue, today)
        assertNull(result.fatal)
        assertEquals("Legs", result.days.single().focus)
        assertEquals(today.epochDay, result.lifts.single().dateEpochDay)
        assertEquals(80.0, result.lifts.single().weightKg, 0.0)
        assertEquals("08:00", result.foods.single().time)
        assertTrue(result.problems.toString(), result.problems.isEmpty())
    }

    @Test
    fun `value parsers accept the usual shapes and nothing else`() {
        assertEquals(LocalDate(2026, 11, 9), PlanValues.date("2026-11-09"))
        assertEquals(LocalDate(2026, 11, 9), PlanValues.date("2026-11-09T00:00:00"))
        assertEquals(LocalDate(2026, 11, 9), PlanValues.date("46335"))
        assertNull(PlanValues.date("09/11/2026"))
        assertNull(PlanValues.date("2026-02-30"))
        assertNull(PlanValues.date("12"))

        assertEquals("18:30", PlanValues.time("18:30"))
        assertEquals("18:30", PlanValues.time("6:30 pm"))
        assertEquals("00:15", PlanValues.time("12:15 AM"))
        assertEquals("18:00", PlanValues.time("0.75"))
        assertNull(PlanValues.time("6"))
        assertNull(PlanValues.time("25:00"))

        assertEquals(Reps(8, 8), PlanValues.reps("8"))
        assertEquals(Reps(8, 12), PlanValues.reps("8 to 12"))
        assertEquals(Reps(0, 0, 90), PlanValues.reps("1:30"))
        assertEquals(Reps(0, 0, 60), PlanValues.reps("1 min"))
        assertEquals(Reps(0, 0), PlanValues.reps("AMRAP"))
        assertNull(PlanValues.reps("12-8"))
        assertNull(PlanValues.reps("0"))
        assertNull(PlanValues.reps("46245"))

        assertEquals(0.0, PlanValues.weight("BW")!!, 0.0)
        assertEquals(22.5, PlanValues.weight("22,5 kg")!!, 0.0)
        assertNull(PlanValues.weight("-5"))
        assertEquals(90, PlanValues.seconds("90s", 0, 900))
        assertNull(PlanValues.seconds("20 min", 0, 900))
        assertEquals("4 x 6-8 · 60 kg · rest 2:30", PlanValues.liftLine(4, 6, 8, 0, 60.0, 150))
    }

    @Test
    fun `hostile text is cleaned and capped`() {
        val long = "x".repeat(500)
        val result = PlanImport.parse(
            listOf(sheet("Workout", workoutHeader,
                listOf("2026-11-09", "", "Back\u0000 squat\n", "3", "5", "", "", long),
                listOf("2026-11-09", "", long, "3", "5", "", "", ""))),
            catalogue, today
        )
        assertEquals("Back squat", result.lifts.single().exerciseName) // control characters gone, so it matched
        assertEquals(300, result.lifts.single().notes.length)
        assertTrue(result.problems.single().skipped)
    }

    /**
     * A file made the way another AI would make it: the app's template, filled in
     * and saved by Python's openpyxl (shared strings, deflate, a real datetime cell),
     * matched against the real built-in catalogue.
     */
    @Test
    fun `a plan filled in by openpyxl imports against the real catalogue`() {
        val bytes = fixture("plan-filled-by-openpyxl.xlsx").readBytes()
        val real = PlanCatalogue(SeedData.exercises(), SeedData.foods())
        val result = PlanImport.read(bytes, real, LocalDate(2026, 11, 6))
        assertNull(result.fatal)
        assertEquals(listOf("Legs", "Push", "Pull", "Rest", "Upper", "Lower", "Rest"), result.days.map { it.focus })
        assertEquals(10, result.lifts.size)
        assertEquals(9, result.foods.size)
        assertEquals("Barbell bench press", result.lifts.first { it.dateEpochDay == LocalDate(2026, 11, 7).epochDay }.exerciseName)
        val skipped = result.problems.filter { it.skipped }
        assertEquals(listOf("Meals" to 11), skipped.map { it.sheet to it.row })
        val notes = result.problems.filterNot { it.skipped }.map { it.message }
        assertEquals(2, notes.size) // the made-up exercise and the made-up food
        assertTrue(notes.any { it.contains("Face pull machine thing") })
        assertTrue(notes.any { it.contains("Home-made paneer bhurji") })
    }

    private fun fixture(name: String): File {
        var dir: File? = File(System.getProperty("user.dir")!!).absoluteFile
        while (dir != null) {
            val f = File(dir, "app/src/androidUnitTest/resources/$name")
            if (f.isFile) return f
            dir = dir.parentFile
        }
        throw IllegalStateException("fixture $name not found from ${System.getProperty("user.dir")}")
    }
}
