package dev.avles.gym.domain

import dev.avles.gym.core.Dates
import dev.avles.gym.data.BodyWeight
import dev.avles.gym.data.FoodLogRow
import dev.avles.gym.data.SetRow

/**
 * The log as spreadsheets: one CSV each for sets, food and body weight, the export
 * every serious tracker offers so the data is never trapped in the app. RFC 4180,
 * which is what Sheets and Excel read: commas and quotes inside a field are quoted.
 * Dates are ISO (2026-10-20) so a spreadsheet sorts them as dates.
 */
object Export {

    fun sets(sets: List<SetRow>): String = csv(
        listOf("date", "exercise", "set", "kg", "reps", "warm-up", "assisted", "variation", "estimated 1RM"),
        sets.sortedBy { it.loggedAt }.map {
            listOf(
                iso(Dates.epochDayOf(it.loggedAt)), it.exerciseName, it.setIndex.toString(),
                Plates.label(it.weightKg), it.reps.toString(),
                yesNo(it.isWarmup), yesNo(it.isAssisted), Variations.label(it.variation),
                if (it.isWarmup || it.isAssisted) "" else Numbers.fixed(Training.estimatedOneRepMax(it.weightKg, it.reps), 1)
            )
        }
    )

    fun food(rows: List<FoodLogRow>): String = csv(
        listOf("date", "meal", "food", "quantity", "unit", "kcal", "protein g", "carbs g", "fat g", "fibre g"),
        rows.sortedWith(compareBy({ it.dateEpochDay }, { it.loggedAt })).map {
            listOf(
                iso(it.dateEpochDay), it.meal, it.name, Plates.label(it.quantity), it.unitLabel,
                Numbers.fixed(it.kcal * it.quantity, 0), Numbers.fixed(it.proteinG * it.quantity, 1),
                Numbers.fixed(it.carbsG * it.quantity, 1), Numbers.fixed(it.fatG * it.quantity, 1),
                Numbers.fixed(it.micros.fiberG * it.quantity, 1)
            )
        }
    )

    fun weights(weights: List<BodyWeight>): String = csv(
        listOf("date", "kg", "note"),
        weights.sortedBy { it.dateEpochDay }.map { listOf(iso(it.dateEpochDay), Plates.label(it.weightKg), it.note) }
    )

    fun csv(header: List<String>, rows: List<List<String>>): String =
        (listOf(header) + rows).joinToString("\n") { row -> row.joinToString(",") { field(it) } } + "\n"

    /** Quoted only when it has to be, with inner quotes doubled. */
    fun field(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"" + value.replace("\"", "\"\"") + "\""
        else value

    private fun iso(epochDay: Long): String = Dates.ofEpochDay(epochDay).toString()
    private fun yesNo(b: Boolean) = if (b) "yes" else ""
}
