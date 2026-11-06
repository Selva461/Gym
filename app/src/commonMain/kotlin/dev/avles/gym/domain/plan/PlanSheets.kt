package dev.avles.gym.domain.plan

/**
 * The layout of the plan spreadsheet, in one place: the template is written from
 * it and the import reads by it, so the two cannot drift apart. Columns are found
 * by their header text, not their position, so a sheet with columns moved or
 * extra columns added still imports. A new column is one line here, plus its
 * handling in [PlanImport]. Plans are training only: meals were dropped to keep
 * the file and the import simple.
 */
object PlanSheets {

    const val README = "Read me"
    const val WEEK = "Week"
    const val WORKOUT = "Workout"
    const val EXERCISES = "Exercises"

    /** A column: its header as written in the template, other headers accepted for it, and its width. */
    class Column(val header: String, val required: Boolean, val width: Double, vararg val aliases: String) {
        val keys: Set<String> = (listOf(header) + aliases).map(::headerKey).toSet()
    }

    object Week {
        val DATE = Column("Date", true, 12.0)
        val DAY = Column("Day", false, 7.0, "Weekday")
        val FOCUS = Column("Focus", true, 18.0, "Split", "Workout", "Day focus", "Muscle group")
        val TIME = Column("Workout time", false, 13.0, "Time", "Start time")
        val NOTES = Column("Notes", false, 40.0, "Note")
        val ALL = listOf(DATE, DAY, FOCUS, TIME, NOTES)
    }

    object Workout {
        val DATE = Column("Date", true, 12.0)
        val ORDER = Column("Order", false, 7.0, "#", "No", "Position")
        val EXERCISE = Column("Exercise", true, 30.0, "Exercise name", "Lift")
        val SETS = Column("Sets", true, 6.0)
        val REPS = Column("Reps", true, 9.0, "Rep range", "Reps or time")
        val WEIGHT = Column("Weight (kg)", false, 11.0, "Weight", "Kg", "Load")
        val REST = Column("Rest (sec)", false, 10.0, "Rest", "Rest seconds", "Rest (s)")
        val NOTES = Column("Notes", false, 40.0, "Note", "Cues")
        val ALL = listOf(DATE, ORDER, EXERCISE, SETS, REPS, WEIGHT, REST, NOTES)
    }

    /** Header text reduced to letters and digits, lower case: "Weight (kg)" is "weightkg". */
    fun headerKey(text: String): String = buildString { text.forEach { if (it.isLetterOrDigit()) append(it.lowercaseChar()) } }

    /** A catalogue or plan name reduced for matching: lower case, runs of punctuation as one space. */
    fun nameKey(text: String): String {
        val sb = StringBuilder()
        var space = false
        text.forEach { ch ->
            if (ch.isLetterOrDigit()) {
                if (space && sb.isNotEmpty()) sb.append(' ')
                sb.append(ch.lowercaseChar())
                space = false
            } else space = true
        }
        return sb.toString()
    }
}
