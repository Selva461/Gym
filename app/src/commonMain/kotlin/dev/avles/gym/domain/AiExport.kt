package dev.avles.gym.domain

import dev.avles.gym.core.Dates
import dev.avles.gym.data.FoodLogRow
import dev.avles.gym.data.FullLog
import dev.avles.gym.data.Meals
import dev.avles.gym.data.SetRow
import dev.avles.gym.domain.plan.PlanValues
import kotlinx.datetime.LocalDate

/**
 * The whole log as one Markdown file written for an AI assistant to read.
 *
 * It opens with a prompt that says what the app is, how each thing was logged and
 * what every column means, then gives pre-computed summaries (language models add
 * up thousands of rows badly), then every raw table as CSV in a fenced block. The
 * raw tables are the source of truth; the summaries are a convenience derived
 * from them here, so the two cannot disagree.
 *
 * Pure: it formats a [FullLog] and touches nothing, so it is unit tested directly.
 * A new kind of entry joins by adding a field to [FullLog] and a [table] call in
 * [build], with its line in [GUIDE].
 */
object AiExport {

    const val MIME = "text/plain"

    fun fileName(today: LocalDate): String = "gym-log-for-ai-$today.md"

    fun build(log: FullLog, today: LocalDate): String = buildString {
        append(prompt(log, today))
        append(profile(log))
        append("\n# Summaries (computed from the raw tables below)\n")
        table("Daily nutrition totals", dailyNutrition(log))
        table("Workout sessions", sessionSummary(log))
        table("Strength by exercise (working sets only)", strengthByExercise(log.sets))
        append("\n# Raw data (everything entered, oldest first)\n")
        table("Sets", Export.csv(SET_HEADER, log.sets.sortedBy { it.loggedAt }.map(::setRow)))
        table("Cardio", cardio(log))
        table("Food log", Export.csv(FOOD_HEADER, log.food.sortedWith(compareBy({ it.dateEpochDay }, { it.loggedAt })).map(::foodRow)))
        table("Body weight", Export.csv(listOf("date", "kg", "note"), log.weights.sortedBy { it.dateEpochDay }.map { listOf(iso(it.dateEpochDay), Plates.label(it.weightKg), it.note) }))
        table("Tape measurements", Export.csv(listOf("date", "part", "cm"), log.measurements.sortedWith(compareBy({ it.dateEpochDay }, { it.part })).map { listOf(iso(it.dateEpochDay), it.part, Numbers.trimmed(it.cm)) }))
        table("Rest and missed days", Export.csv(listOf("date", "kind", "note"), log.dayMarkers.sortedBy { it.dateEpochDay }.map { listOf(iso(it.dateEpochDay), it.kind, it.note) }))
        table("Exercises (custom, with notes, or used)", exercises(log))
        table("Workout splits (planned days)", splits(log))
        table("My foods and recipes (custom, per one unit)", customFoods(log))
        table("Saved meals", Export.csv(listOf("meal", "food", "quantity", "kcal per unit", "protein g per unit"), log.savedMeals.map { listOf(it.mealName, it.foodName, Plates.label(it.quantity), Numbers.fixed(it.kcal, 0), Numbers.fixed(it.proteinG, 1)) }))
        table("Week plan: days (imported from the plan spreadsheet; planned, not done)", Export.csv(listOf("date", "focus", "workout time", "notes"), log.planDays.sortedBy { it.dateEpochDay }.map { listOf(iso(it.dateEpochDay), it.focus, it.workoutTime, it.notes) }))
        table("Week plan: exercises", Export.csv(listOf("date", "order", "exercise", "planned", "in app catalogue", "notes"), log.planLifts.sortedWith(compareBy({ it.dateEpochDay }, { it.position })).map { listOf(iso(it.dateEpochDay), (it.position + 1).toString(), it.exerciseName, PlanValues.liftLine(it.sets, it.repsLow, it.repsHigh, it.holdSeconds, it.weightKg, it.restSeconds), yesNo(it.exerciseId != null), it.notes) }))
        table("Week plan: meals", Export.csv(listOf("date", "meal", "time", "food", "quantity", "kcal", "protein g", "in app catalogue", "notes"), log.planFoods.sortedWith(compareBy({ it.dateEpochDay }, { it.position })).map { listOf(iso(it.dateEpochDay), it.meal, it.time, it.foodName, Plates.label(it.quantity), Numbers.fixed(it.kcal, 0), Numbers.fixed(it.proteinG, 1), yesNo(it.foodId != null), it.notes) }))
        table("Progress photos (pictures not included)", Export.csv(listOf("date", "label", "note"), log.progressPhotos.sortedBy { it.dateEpochDay }.map { listOf(iso(it.dateEpochDay), it.label, it.note) }))
    }

    // ---- the prompt ---------------------------------------------------------

    private fun prompt(log: FullLog, today: LocalDate): String {
        val days = (log.sets.map { Dates.epochDayOf(it.loggedAt) } + log.food.map { it.dateEpochDay } +
            log.weights.map { it.dateEpochDay } + log.cardio.map { it.dateEpochDay }).sorted()
        val span = if (days.isEmpty()) "nothing logged yet" else "${iso(days.first())} to ${iso(days.last())}"
        return """
            |# My training and food log, exported for an AI assistant
            |
            |**To the AI reading this:** this file is my complete personal log from an offline
            |gym and food tracking app on my phone, exported on $today. It covers $span:
            |${log.sessions.size} workout sessions, ${log.sets.size} sets, ${log.cardio.size} cardio entries,
            |${log.food.size} food entries, ${log.weights.size} weigh-ins, ${log.measurements.size} tape measurements.
            |
            |My goal is to get physically stronger. Please:
            |1. Read the guide below so you know exactly how each thing was logged.
            |2. Review my training (frequency, volume, progression on each lift, stalls) and my
            |   nutrition (calories and protein against my targets, consistency, food quality).
            |3. Give me a concrete plan: a weekly training programme with sets, reps and how to
            |   progress the weight, and daily calorie and protein targets with food ideas.
            |   I mostly eat Tamil Nadu / South Indian food, so suggest foods I would actually eat.
            |4. Point out anything that looks unsafe, and ask me about anything the data cannot tell you.
            |Only use numbers that are in this file. A day with nothing logged means I did not log
            |it, not that I ate nothing or skipped training.
            |
            |$GUIDE
            |""".trimMargin()
    }

    /** How the app records things. Kept beside the code that writes the tables. */
    private val GUIDE = """
        |## How to read this file
        |
        |- **Dates** are local calendar dates (YYYY-MM-DD); **times** are local 24-hour HH:MM.
        |  Weights are kilograms, lengths centimetres, energy kcal, nutrients grams unless the
        |  column says mg or mcg. Tables are CSV (RFC 4180: a field with a comma is in quotes).
        |- **Sets**: one row per set as I logged it in the gym. `kg` is the load I typed in:
        |  for barbell lifts the bar plus plates. The app does not record whether a dumbbell
        |  weight is per hand or the pair, so ask me if it matters. `0` on a bodyweight
        |  exercise means bodyweight only. If `assisted` is yes the kg is
        |  machine *assistance* (more kg = easier), so it has no 1RM. `warm-up` sets are not
        |  working sets. `type` is Working, Warm-up, Drop (weight reduced mid-set) or Failure (taken to failure).
        |  `RPE` is effort out of 10 (10 = nothing left, 8 = two reps left), blank if not entered.
        |  `estimated 1RM` uses the Epley formula, kg × (1 + reps/30), working sets only.
        |  `variation` is grip, stance, tempo or equipment changes. `session` joins to Sessions.
        |- **Sessions**: one workout. `split` is the planned day it was started from, if any.
        |  `volume kg` sums kg × reps over working, unassisted sets.
        |- **Cardio**: activity, minutes and optional distance in km.
        |- **Food log**: one row per item eaten. `quantity` is how many of `unit` (for example
        |  1.5 × "1 cup (200 g)"). The nutrient columns are already multiplied by quantity.
        |  Values come from the app's food database (built-in estimates, Indian dishes mostly)
        |  or from foods I created myself. Meals are Breakfast, Lunch, Snack, Dinner,
        |  Supplements; `Water` rows are glasses of water (250 ml each), not food.
        |- **Daily nutrition totals** excludes water rows and counts glasses separately.
        |- **Body weight**: at most one weigh-in per day. **Tape measurements**: one per body
        |  part per day (Neck, Chest, Arm, Waist, Hips, Thigh, Calf).
        |- **Rest and missed days**: Rest = planned recovery; Missed = meant to train and could not.
        |- **Exercises**: `rep range` is the target I set for progression; `step kg` is how much
        |  the app adds when I hit the top of the range. Notes are my own cues.
        |- **Splits**: my planned workout days and the exercises in each, in order.
        |- **Week plan**: a plan I imported from the app's Excel template (often written by an AI).
        |  It is what I meant to do, not what I did; compare it with Sets and Food log. `planned`
        |  is sets x reps (or seconds for a hold, AMRAP = as many as possible), kg and rest.
        |  Meal kcal and protein are for the whole quantity.
        |""".trimMargin()

    private fun profile(log: FullLog): String {
        val s = log.settings
        val goal = Physiques.byId(s.goalId)
        val fasting = if (s.fastStartedAt > 0) "a ${s.fastHours} h fast started ${iso(Dates.epochDayOf(s.fastStartedAt))} ${Dates.clockOf(s.fastStartedAt)}" else "off"
        val lines = listOf(
            "Physique goal set in the app" to "${goal.label}: ${goal.summary}",
            "Daily calorie target" to "${Numbers.fixed(s.kcalTarget, 0)} kcal",
            "Daily protein target" to "${Numbers.fixed(s.proteinTargetG, 0)} g",
            "Daily water target" to "${s.waterGlasses} glasses of 250 ml",
            "Goal body weight" to if (s.goalWeightKg > 0) "${Plates.label(s.goalWeightKg)} kg" else "not set",
            "Height" to if (s.heightCm > 0) "${Numbers.trimmed(s.heightCm)} cm" else "not set",
            "Sex (for body-fat maths)" to if (s.isFemale) "female" else "male",
            "Rest timer" to "${s.restSeconds} s" + if (s.restByLift) ", adjusted per lift" else "",
            "Fasting timer" to fasting,
            "Favourite foods" to log.favouriteFoodNames.joinToString(", ").ifEmpty { "none" },
            "App language" to if (s.language == "ta") "Tamil" else "English"
        )
        return "\n## My profile and targets\n\n" + lines.joinToString("\n") { "- **${it.first}:** ${it.second}" } + "\n"
    }

    // ---- summaries ----------------------------------------------------------

    private fun dailyNutrition(log: FullLog): String {
        val target = log.settings
        val rows = log.food.groupBy { it.dateEpochDay }.entries.sortedBy { it.key }.map { (day, entries) ->
            val food = entries.filter { it.meal != Meals.WATER }
            val kcal = food.sumOf { it.kcal * it.quantity }
            val protein = food.sumOf { it.proteinG * it.quantity }
            listOf(
                iso(day), Numbers.fixed(kcal, 0), Numbers.fixed(protein, 1),
                Numbers.fixed(food.sumOf { it.carbsG * it.quantity }, 1), Numbers.fixed(food.sumOf { it.fatG * it.quantity }, 1),
                Numbers.fixed(food.sumOf { it.micros.fiberG * it.quantity }, 1),
                Plates.label(entries.filter { it.meal == Meals.WATER }.sumOf { it.quantity }),
                Numbers.fixed(kcal - target.kcalTarget, 0), Numbers.fixed(protein - target.proteinTargetG, 1)
            )
        }
        return Export.csv(listOf("date", "kcal", "protein g", "carbs g", "fat g", "fibre g", "water glasses", "kcal vs target", "protein g vs target"), rows)
    }

    private fun sessionSummary(log: FullLog): String {
        val setsBySession = log.sets.groupBy { it.sessionId }
        val splitNames = log.splits.associate { it.id to it.name }
        val rows = log.sessions.sortedBy { it.startedAt }.map { session ->
            val sets = setsBySession[session.id].orEmpty()
            val minutes = session.endedAt?.let { ((it - session.startedAt) / 60_000).toString() } ?: "unfinished"
            listOf(
                session.id.toString(), iso(session.dateEpochDay), Dates.clockOf(session.startedAt), minutes,
                session.label, session.splitId?.let { splitNames[it] }.orEmpty(),
                sets.count { !it.isWarmup }.toString(), Numbers.fixed(Training.volume(sets), 0),
                sets.map { it.exerciseName }.distinct().joinToString("; "), session.notes
            )
        }
        return Export.csv(listOf("session", "date", "start", "minutes", "label", "split", "working sets", "volume kg", "exercises", "notes"), rows)
    }

    private fun strengthByExercise(sets: List<SetRow>): String {
        val rows = sets.filter { !it.isWarmup && !it.isAssisted }.groupBy { it.exerciseName }.entries.sortedBy { it.key }.map { (name, list) ->
            val byDay = list.groupBy { Dates.epochDayOf(it.loggedAt) }.entries.sortedBy { it.key }
            val first = byDay.first()
            val last = byDay.last()
            val best = list.maxBy { Training.estimatedOneRepMax(it.weightKg, it.reps) }
            listOf(
                name, byDay.size.toString(), list.size.toString(),
                iso(first.key), Numbers.fixed(Training.bestEstimatedOneRepMax(first.value), 1),
                iso(last.key), Numbers.fixed(Training.bestEstimatedOneRepMax(last.value), 1),
                "${Plates.label(best.weightKg)} × ${best.reps} on ${iso(Dates.epochDayOf(best.loggedAt))}",
                Plates.label(list.maxOf { it.weightKg })
            )
        }
        return Export.csv(listOf("exercise", "days trained", "working sets", "first day", "first est. 1RM", "latest day", "latest est. 1RM", "best set (by est. 1RM)", "heaviest kg"), rows)
    }

    // ---- raw tables ---------------------------------------------------------

    private val SET_HEADER = listOf("date", "time", "session", "exercise", "set", "kg", "reps", "warm-up", "assisted", "type", "RPE", "variation", "estimated 1RM")

    private fun setRow(it: SetRow): List<String> = listOf(
        iso(Dates.epochDayOf(it.loggedAt)), Dates.clockOf(it.loggedAt), it.sessionId.toString(), it.exerciseName,
        it.setIndex.toString(), Plates.label(it.weightKg), it.reps.toString(),
        yesNo(it.isWarmup), yesNo(it.isAssisted), SetKinds.pickerLabel(it.isWarmup, it.kind),
        if (it.rpe > 0) SetKinds.rpeLabel(it.rpe) else "", Variations.label(it.variation),
        if (it.isWarmup || it.isAssisted) "" else Numbers.fixed(Training.estimatedOneRepMax(it.weightKg, it.reps), 1)
    )

    private val FOOD_HEADER = listOf(
        "date", "time", "meal", "food", "quantity", "unit", "kcal", "protein g", "carbs g", "fat g", "fibre g", "sugar g",
        "sat fat g", "sodium mg", "potassium mg", "calcium mg", "iron mg", "magnesium mg", "zinc mg", "vit A mcg",
        "vit C mg", "vit D mcg", "vit B12 mcg", "folate mcg", "omega-3 g", "supplement"
    )

    private fun foodRow(it: FoodLogRow): List<String> {
        val q = it.quantity
        val m = it.micros * q
        return listOf(
            iso(it.dateEpochDay), Dates.clockOf(it.loggedAt), it.meal, it.name, Plates.label(q), it.unitLabel,
            Numbers.fixed(it.kcal * q, 0), Numbers.fixed(it.proteinG * q, 1), Numbers.fixed(it.carbsG * q, 1), Numbers.fixed(it.fatG * q, 1)
        ) + listOf(m.fiberG, m.sugarG, m.satFatG).map { Numbers.fixed(it, 1) } +
            listOf(m.sodiumMg, m.potassiumMg, m.calciumMg).map { Numbers.fixed(it, 0) } +
            listOf(m.ironMg, m.magnesiumMg, m.zincMg).map { Numbers.fixed(it, 1) } +
            listOf(Numbers.fixed(m.vitAMcg, 0), Numbers.fixed(m.vitCMg, 1), Numbers.fixed(m.vitDMcg, 1), Numbers.fixed(m.vitB12Mcg, 2), Numbers.fixed(m.folateMcg, 0), Numbers.fixed(m.omega3G, 2), yesNo(it.isSupplement))
    }

    private fun cardio(log: FullLog): String = Export.csv(
        listOf("date", "time", "activity", "minutes", "km"),
        log.cardio.sortedBy { it.loggedAt }.map {
            listOf(iso(it.dateEpochDay), Dates.clockOf(it.loggedAt), it.activity, it.minutes.toString(), if (it.distanceKm > 0) Numbers.trimmed(it.distanceKm) else "")
        }
    )

    private fun exercises(log: FullLog): String {
        val used = log.sets.map { it.exerciseId }.toSet() + log.splitExercises.map { it.exerciseId }
        val rows = log.exercises
            .filter { it.isCustom || it.notes.isNotBlank() || it.id in used || it.id in log.exercisePhotoIds }
            .sortedBy { it.name }
            .map {
                listOf(
                    it.name, it.muscleGroup, yesNo(it.isCustom), yesNo(it.isBodyweight), yesNo(it.isAssisted),
                    "${it.repLow}-${it.repHigh}", Plates.label(it.incrementKg), yesNo(it.id in log.exercisePhotoIds), it.notes
                )
            }
        return Export.csv(listOf("exercise", "muscle group", "custom", "bodyweight", "assisted", "rep range", "step kg", "own photo", "notes"), rows)
    }

    private fun splits(log: FullLog): String {
        val names = log.exercises.associate { it.id to it.name }
        val rows = log.splits.sortedBy { it.position }.map { split ->
            val list = log.splitExercises.filter { it.splitId == split.id }.sortedBy { it.position }.mapNotNull { names[it.exerciseId] }
            listOf(split.name, yesNo(split.isCustom), list.joinToString("; "))
        }
        return Export.csv(listOf("split", "made by me", "exercises in order"), rows)
    }

    private fun customFoods(log: FullLog): String = Export.csv(
        listOf("food", "unit", "category", "kcal", "protein g", "carbs g", "fat g", "fibre g", "supplement", "also called"),
        log.customFoods.sortedBy { it.name }.map {
            listOf(
                it.name, it.unitLabel, it.category, Numbers.fixed(it.kcal, 0), Numbers.fixed(it.proteinG, 1),
                Numbers.fixed(it.carbsG, 1), Numbers.fixed(it.fatG, 1), Numbers.fixed(it.micros.fiberG, 1), yesNo(it.isSupplement), it.aliases
            )
        }
    )

    // ---- formatting ---------------------------------------------------------

    /** A heading and a fenced CSV block, or a note that nothing was logged. */
    private fun StringBuilder.table(title: String, csv: String) {
        append("\n## ").append(title).append('\n')
        if (csv.count { it == '\n' } <= 1) { append("\n(nothing logged)\n"); return }
        // A typed note holding a fence would close the block early; that is all it can break.
        append("\n```csv\n").append(csv.replace("```", "'''")).append("```\n")
    }

    private fun iso(epochDay: Long): String = Dates.ofEpochDay(epochDay).toString()
    private fun yesNo(b: Boolean) = if (b) "yes" else ""
}
