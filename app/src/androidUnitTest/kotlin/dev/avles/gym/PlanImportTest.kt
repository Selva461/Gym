package dev.avles.gym

import dev.avles.gym.core.epochDay
import dev.avles.gym.core.xlsx.Cell
import dev.avles.gym.core.xlsx.SheetData
import dev.avles.gym.core.xlsx.SheetRow
import dev.avles.gym.core.xlsx.SheetSpec
import dev.avles.gym.core.xlsx.Xlsx
import dev.avles.gym.data.Exercise
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
 * The plan import: what an AI-filled spreadsheet turns into (plan days and the
 * splits they define), and how strictly it is checked. A good row imports exactly; a bad row is skipped with its sheet and
 * row number; nothing the file holds can make the import throw.
 */
class PlanImportTest {

    private val today = LocalDate(2026, 11, 9) // a Monday
    private val squat = Exercise(id = 1, name = "Back squat", muscleGroup = "Quads")
    private val rdl = Exercise(id = 2, name = "Romanian deadlift", muscleGroup = "Hamstrings")
    private val plank = Exercise(id = 3, name = "Plank", muscleGroup = "Core", isBodyweight = true)
    private val bench = Exercise(id = 4, name = "Barbell bench press", muscleGroup = "Chest")
    private val catalogue = PlanCatalogue(listOf(squat, rdl, plank, bench))

    private fun sheet(name: String, vararg rows: List<String>) =
        SheetData(name, rows.mapIndexed { i, cells -> SheetRow(i + 1, cells) })

    private val weekHeader = listOf("Date", "Day", "Focus", "Workout time", "Notes")
    private val workoutHeader = listOf("Date", "Order", "Exercise", "Sets", "Reps", "Weight (kg)", "Rest (sec)", "Notes")

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
                    listOf("2026-11-09", "3", "Plank", "3", "45s", "", "60", ""))
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
        assertTrue(result.problems.isEmpty())

        // Legs has lifts, so it is a split; Push and Rest have none, so they are not.
        assertEquals(listOf("Legs"), result.splits.map { it.name })
        assertEquals(listOf(1L, 2L, 3L), result.splits.single().exerciseIds)
    }

    @Test
    fun `a split's lifts are written once and every later day with that focus gets them`() {
        val result = PlanImport.parse(
            listOf(
                sheet("Week", weekHeader,
                    listOf("2026-11-09", "Mon", "Day 1 - Lower", "", ""),
                    listOf("2026-11-10", "Tue", "Day 2 - Upper", "", ""),
                    listOf("2026-11-11", "Wed", "Rest", "", ""),
                    listOf("2026-11-12", "Thu", "day 1 - lower", "", ""),  // same split, different case
                    listOf("2026-11-13", "Fri", "Day 2 - Upper", "", ""),
                    listOf("2026-11-14", "Sat", "Day 3 - Unwritten", "", "")),
                sheet("Workout", workoutHeader,
                    listOf("2026-11-09", "1", "Back squat", "3", "6", "65", "180", ""),
                    listOf("2026-11-09", "2", "Plank", "3", "45s", "", "60", ""),
                    listOf("2026-11-10", "1", "Barbell bench press", "3", "8", "50", "120", ""),
                    listOf("2026-11-10", "2", "Made-up machine", "3", "12", "", "60", ""),
                    // A later day written out on purpose keeps its own lifts.
                    listOf("2026-11-13", "1", "Romanian deadlift", "3", "8", "40", "120", ""))
            ),
            catalogue, today
        )
        assertEquals(listOf("Day 1 - Lower", "Day 2 - Upper"), result.splits.map { it.name })
        assertEquals(listOf(1L, 3L), result.splits[0].exerciseIds)
        assertEquals(listOf(4L), result.splits[1].exerciseIds) // the unknown lift cannot go in a split
        val byDay = result.lifts.groupBy { it.dateEpochDay }
        val thu = LocalDate(2026, 11, 12).epochDay
        assertEquals(listOf("Back squat", "Plank"), byDay.getValue(thu).map { it.exerciseName })
        assertEquals(65.0, byDay.getValue(thu)[0].weightKg, 0.0)
        assertEquals(listOf("Romanian deadlift"), byDay.getValue(LocalDate(2026, 11, 13).epochDay).map { it.exerciseName })
        assertNull(byDay[LocalDate(2026, 11, 11).epochDay])
        assertNull(byDay[LocalDate(2026, 11, 14).epochDay])
        assertEquals(6, result.days.size)
    }

    @Test
    fun `start today moves every day by the same amount and keeps the rotation`() {
        val result = PlanImport.parse(
            listOf(
                sheet("Week", weekHeader,
                    listOf("2026-11-12", "Thu", "Lower", "", ""),
                    listOf("2026-11-13", "Fri", "Rest", "", ""),
                    listOf("2026-11-14", "Sat", "Lower", "", "")),
                sheet("Workout", workoutHeader, listOf("2026-11-12", "1", "Back squat", "3", "6", "", "", ""))
            ),
            catalogue, today
        )
        val moved = result.startingOn(today.epochDay)
        assertEquals((0L..2L).map { today.epochDay + it }, moved.days.map { it.dateEpochDay })
        assertEquals(listOf("Lower", "Rest", "Lower"), moved.days.map { it.focus })
        assertEquals(listOf(today.epochDay, today.epochDay + 2), moved.lifts.map { it.dateEpochDay })
        assertEquals(today.epochDay, moved.firstDate)
        assertEquals(result.splits.map { it.name }, moved.splits.map { it.name })
        assertTrue(result.startingOn(result.firstDate!!) === result)
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
                    listOf("2026-11-09", "", "Back squat", "4", "6-8", "", "", ""))     // row 8: fine
            ),
            catalogue, today
        )
        assertEquals(listOf(2, 3, 4, 5, 6, 7), result.problems.filter { it.skipped && it.sheet == "Workout" }.map { it.row })
        assertEquals(1, result.lifts.size)
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
        val input = PlanTemplate.Input(today, listOf(squat, rdl, plank), 72.5)
        val bytes = PlanTemplate.build(input)
        val sheets = Xlsx.read(bytes)
        // Training only: no food sheets.
        assertEquals(listOf(PlanSheets.README, PlanSheets.WEEK, PlanSheets.WORKOUT, PlanSheets.EXERCISES), sheets.map { it.name })
        val week = sheets.first { it.name == PlanSheets.WEEK }
        assertEquals(PlanSheets.Week.ALL.map { it.header }, week.rows[0].cells.take(5))
        assertEquals("2026-11-09", week.rows[1][0])
        assertEquals("Mon", week.rows[1][1])
        assertEquals(1 + PlanTemplate.DAYS_PREFILLED, week.rows.size)
        assertEquals(30, PlanTemplate.DAYS_PREFILLED) // a month of split days
        assertTrue(sheets[0].rows.any { it[0].contains("72.5 kg") })

        val untouched = PlanImport.read(bytes, catalogue, today)
        assertNull(untouched.fatal)
        assertTrue(untouched.isEmpty)
        assertTrue(untouched.problems.isEmpty())
    }

    @Test
    fun `a template filled in and saved again imports`() {
        val input = PlanTemplate.Input(today, listOf(squat))
        val filled = PlanTemplate.sheets(input).map { spec ->
            when (spec.name) {
                PlanSheets.WEEK -> SheetSpec(spec.name, listOf(spec.rows[0], listOf(Cell.Text("2026-11-09"), Cell.Text("Mon"), Cell.Text("Legs"))))
                PlanSheets.WORKOUT -> SheetSpec(spec.name, listOf(spec.rows[0],
                    // A date Excel stored as a serial number, and reps as a plain number.
                    listOf(Cell.Number(46335.0), Cell.Text(""), Cell.Text("Back squat"), Cell.Number(5.0), Cell.Number(5.0), Cell.Number(80.0))))
                else -> spec
            }
        }
        val result = PlanImport.read(Xlsx.write(filled), catalogue, today)
        assertNull(result.fatal)
        assertEquals("Legs", result.days.single().focus)
        assertEquals(today.epochDay, result.lifts.single().dateEpochDay)
        assertEquals(80.0, result.lifts.single().weightKg, 0.0)
        assertEquals("Legs", result.splits.single().name)
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
     * A file made the way another AI would make it: the app's earlier template, filled
     * in and saved by Python's openpyxl (shared strings, deflate, a real datetime cell),
     * matched against the real built-in catalogue. Its Meals sheet is from before plans
     * became training only, and is ignored.
     */
    @Test
    fun `a plan filled in by openpyxl imports against the real catalogue`() {
        val bytes = fixture("plan-filled-by-openpyxl.xlsx").readBytes()
        val real = PlanCatalogue(SeedData.exercises())
        val result = PlanImport.read(bytes, real, LocalDate(2026, 11, 6))
        assertNull(result.fatal)
        assertEquals(listOf("Legs", "Push", "Pull", "Rest", "Upper", "Lower", "Rest"), result.days.map { it.focus })
        assertEquals(10, result.lifts.size)
        assertEquals("Barbell bench press", result.lifts.first { it.dateEpochDay == LocalDate(2026, 11, 7).epochDay }.exerciseName)
        assertEquals(listOf("Legs", "Push", "Pull"), result.splits.map { it.name })
        // The made-up lift stays out of the split (seed exercises have no ids yet, so count matches).
        assertEquals(2, result.splits[2].lifts.count { it.exerciseId != null })
        assertTrue(result.problems.none { it.skipped })
        val notes = result.problems.map { it.message }
        assertEquals(1, notes.size)
        assertTrue(notes.single().contains("Face pull machine thing"))
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
