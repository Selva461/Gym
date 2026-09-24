package dev.avles.gym.data

import dev.avles.gym.domain.FoodChecks
import kotlin.math.round

/**
 * Reads the Indian food nutrition dataset into foods this app can log.
 *
 * The file is 724 rows but only about 120 dishes. Almost every row is the same
 * dish at another weight — Tandoori Chicken at 50 g, 75 g, 100 g and so on up to
 * 400 g — and within a dish the numbers scale exactly linearly. This app already
 * expresses portion through a quantity on the log entry, so importing every row
 * would put thirteen near-identical entries in the food picker and make the
 * catalogue worse. Each dish becomes **one** food:
 *
 *  - a countable thing with a believable weight ("regular — chapati serving", 36 g)
 *    keeps it as the unit, because people log "2 chapatis", not "72 g of chapati";
 *  - everything else is per 100 g, or per 100 ml for drinks. A cup, glass, bowl or
 *    plate is whatever size yours is, and the source's are not believable anyway:
 *    it has a 210 ml tea cup of chai and a 450 ml tea cup of coffee.
 *
 * Every dish is then put through [FoodChecks], the gate every built-in food passes,
 * and declined rather than imported when it fails. Three kinds fail:
 *
 *  - **blank macros.** Some dishes carry calories but no protein, carbohydrate or
 *    fat, because the dataset's own sources conflicted. Importing blank as zero
 *    would tell someone that tandoori chicken contains no protein, in an app whose
 *    first number is protein.
 *  - **self-contradiction.** A row declaring 240 kcal whose own macros add up to
 *    172 is wrong somewhere, and there is no way to tell which figure.
 *  - **impossible dishes.** The source's deep-fried recipes count every drop of the
 *    frying oil rather than what the food absorbs, so a plate of pakoras arrives at
 *    4,550 kcal with 485 g of fat. The numbers agree with each other perfectly and
 *    are still not food.
 *
 * Dishes this app already has are left alone — seeding never overwrites — and that
 * includes ones it has under another name, which is what [SAME_AS] is for.
 *
 * Micronutrients the dataset leaves blank stay zero, which in this app has always
 * meant "not estimated" rather than "none present".
 *
 * Pure Kotlin — no java.io, no String.format — so the same parser runs on iOS.
 */
object FoodDataset {

    /** Where the bundled file lives, under composeResources/files. */
    const val ASSET_PATH = "foods/indian_food_nutrition_v2.csv"

    /** Shown in the food editor. Specific enough to judge, short enough to read. */
    private const val INDB_SOURCE = "INDB 2024.11 recipe estimate"
    private const val THIRD_PARTY_SOURCE = "IFCT 2017 via third-party reference"

    data class Rejection(val dish: String, val reason: String)

    data class Result(
        val foods: List<FoodItem>,
        val rejected: List<Rejection>,
        /** Dishes this app already had, left alone. Seeding never overwrites. */
        val alreadyPresent: List<String>
    )

    /**
     * The dataset's twelve categories, folded onto the ones this app already files
     * under, so that importing a file does not scatter the food picker's filter row
     * into regions nobody browses by.
     *
     * Data, not logic — changing where something files is an edit to this map.
     */
    private val CATEGORIES = mapOf(
        "Tamil tiffin" to "Tiffin",
        "Tamil meals" to "Rice",
        "Kerala cuisine" to "Sides",
        "Andhra/Telangana/Hyderabadi" to "Sides",
        "Karnataka/Udupi" to "Tiffin",
        "North Indian restaurant staples" to "North Indian",
        "Biryani, rice and Indo-Chinese" to "Rice",
        "Grilled/tandoori/barbecue chicken, kebabs, tikka" to "Protein",
        "Shawarma, rolls, wraps, kathi rolls" to "Snacks",
        "Sides, starters, chaats, snacks" to "Snacks",
        "Juices, milkshakes, tender coconut, traditional drinks" to "Drinks",
        "Desserts and sweets" to "Sweets"
    )

    /** The category whose units are millilitres. */
    private const val DRINKS = "Drinks"

    /**
     * Dishes in the file that this app already has under a different name. Matching
     * names by spelling catches "Lemon Rice" against "Lemon rice"; it cannot know
     * that a salted lassi and "Lassi (salted)" are the same glass.
     */
    private val SAME_AS = mapOf(
        "Chapati/Roti" to "Chapati",
        "Upma" to "Rava upma",
        "Vegetable Fried Rice" to "Veg fried rice",
        "Lassi (salted)" to "Salted lassi",
        "Hot tea" to "Tea with milk and sugar",
        "Iced tea" to "Iced tea, sweetened"
    )

    /** Spellings in the source that would look like mistakes in a list, fixed wherever they appear. */
    private val SPELLINGS = listOf(
        "parantha/paratha" to "paratha",
        "parantha" to "paratha",
        "pakora/pakoda" to "pakora",
        "Espreso" to "Espresso"
    )

    /** Units that are a vessel rather than a thing. See the class comment. */
    private val VESSELS = setOf("cup", "glass", "bowl", "plate", "katori")

    /** Heavier than this, "one" of something is a recipe yield, not a serving. */
    private const val MAX_PIECE_G = 150.0

    private val DASH = " ${Char(0x2014)} "
    private val BOM = Char(0xFEFF)

    fun parse(csv: String, existingNames: Set<String>): Result {
        val table = parseCsv(csv)
        if (table.isEmpty()) return Result(emptyList(), emptyList(), emptyList())
        val header = table.first().map { it.trim().removePrefix(BOM.toString()) }
        val rows = table.drop(1).map { cells -> header.zip(cells).toMap() }

        val existing = existingNames.map(::key).toSet()
        val byDish = LinkedHashMap<String, MutableList<Map<String, String>>>()
        rows.forEach { row ->
            val name = naming(row).first
            if (name.isNotEmpty()) byDish.getOrPut(key(name)) { mutableListOf() }.add(row)
        }

        val foods = mutableListOf<FoodItem>()
        val rejected = mutableListOf<Rejection>()
        val present = mutableListOf<String>()

        byDish.values.forEach { group ->
            val (name, aliases) = naming(group.first())
            val knownAs = listOfNotNull(name, SAME_AS[name]) + aliases.split(" / ")
            if (knownAs.any { it.isNotBlank() && key(it) in existing }) {
                present += name
                return@forEach
            }
            val complete = group.filter { r -> MACROS.all { number(r, it) != null } }
            if (complete.isEmpty()) {
                rejected += Rejection(name, "calories without protein, carbohydrate or fat in the source")
                return@forEach
            }
            val (food, grams) = toFood(name, aliases, complete)
            val problems = FoodChecks.problems(food) + FoodChecks.dishProblems(food, grams)
            if (problems.isNotEmpty()) {
                rejected += Rejection(name, problems.joinToString("; "))
                return@forEach
            }
            foods += food
        }
        return Result(foods, rejected, present)
    }

    /**
     * The food for a dish, and the weight of one of its units.
     *
     * The source's own "regular" serving wins where it is a countable thing of a
     * believable weight; otherwise any row will do, because within a dish they scale
     * linearly, and it is rescaled to 100 g.
     */
    private fun toFood(
        name: String,
        aliases: String,
        rows: List<Map<String, String>>
    ): Pair<FoodItem, Double> {
        val row = rows.firstOrNull { it["variant"].orEmpty().startsWith("regular (") } ?: rows.first()
        val weight = number(row, "serving_weight_g_or_ml") ?: 100.0
        val noun = SPELLINGS.fold(servingNoun(row["serving_description"].orEmpty())) { acc, (from, to) ->
            acc.replace(from, to)
        }
        val piece = row["variant"].orEmpty().startsWith("regular (") &&
            noun.substringAfterLast(' ') !in VESSELS && weight <= MAX_PIECE_G
        val scale = if (piece) 1.0 else 100.0 / weight
        val category = CATEGORIES[row["category"].orEmpty().trim()] ?: "Sides"
        val ml = if (category == DRINKS) "ml" else "g"
        val unit = if (piece) "1 $noun (${round(weight).toInt()} $ml)" else "100 $ml"

        fun v(column: String) = round1((number(row, column) ?: 0.0) * scale)

        val food = FoodItem(
            name = name,
            unitLabel = unit,
            kcal = v("per_serving_kcal"),
            proteinG = v("protein_g"),
            carbsG = v("carbs_g"),
            fatG = v("fat_g"),
            category = category,
            micros = Micros(
                fiberG = v("fiber_g"),
                sugarG = v("sugar_g"),
                satFatG = v("saturated_fat_g"),
                sodiumMg = v("sodium_mg"),
                potassiumMg = v("potassium_mg"),
                calciumMg = v("calcium_mg"),
                ironMg = v("iron_mg"),
                magnesiumMg = v("magnesium_mg"),
                zincMg = v("zinc_mg"),
                vitAMcg = v("vitamin_a_mcg"),
                vitCMg = v("vitamin_c_mg"),
                vitDMcg = v("vitamin_d_mcg"),
                vitB12Mcg = v("vitamin_b12_mcg"),
                folateMcg = v("folate_mcg")
            ),
            aliases = aliases,
            source = if (row["data_source"].orEmpty().contains("INDB", ignoreCase = true)) INDB_SOURCE
                else THIRD_PARTY_SOURCE
        )
        return food to if (piece) weight else 100.0
    }

    /** "Tandoori Chicken — 50 g" and "Chapati/Roti — regular" both name one dish. */
    internal fun dishName(row: Map<String, String>): String =
        row["name"].orEmpty().substringBefore(DASH).trim()

    /**
     * What to call a dish, and what else it is called.
     *
     * The source writes the second language into the name —
     * "Potato samosa (Aloo ka samosa)" — and repeats it as the local name. Left as
     * is, half the North Indian list would be too long to read in a row, so a
     * parenthetical that is the local name moves out to the aliases, where search
     * still finds it. One that starts in lower case qualifies the dish instead —
     * "Lassi (salted)", "Fruit Punch (with squashes)" — and stays, or two different
     * punches would end up with one name.
     */
    internal fun naming(row: Map<String, String>): Pair<String, String> {
        var name = SPELLINGS.fold(dishName(row)) { acc, (from, to) -> acc.replace(from, to) }
        var local = SPELLINGS.fold(row["local_name"].orEmpty().trim()) { acc, (from, to) -> acc.replace(from, to) }
        val paren = Regex("""^(.*\S)\s*\(([^()]+)\)$""").find(name)
        if (paren != null) {
            val inside = paren.groupValues[2].trim()
            if (inside.firstOrNull()?.isUpperCase() == true) {
                name = paren.groupValues[1].trim()
                if (local.isEmpty()) local = inside
            } else if (key(local) == key(inside)) {
                local = ""
            }
        }
        val alias = local.takeIf { it.isNotEmpty() && key(it) != key(name) && !name.contains(it, ignoreCase = true) }
        return name to alias.orEmpty()
    }

    /**
     * What a dish is keyed on, so that "Lemon Rice" and "Lemon rice" — both present
     * in the source under different categories — are one dish, and so that a name
     * already in this app's catalogue is recognised whatever its capitalisation.
     */
    fun key(name: String): String = name.lowercase().filter { it.isLetterOrDigit() }

    /** "regular — chapati serving, 1x source serving" -> "chapati". */
    internal fun servingNoun(description: String): String =
        description.substringAfter(DASH, description)
            .substringBefore(" serving")
            .trim()
            .ifEmpty { "serving" }

    private val MACROS = listOf("per_serving_kcal", "protein_g", "carbs_g", "fat_g")

    private fun number(row: Map<String, String>, column: String): Double? =
        row[column]?.trim()?.takeIf { it.isNotEmpty() }?.toDoubleOrNull()

    private fun round1(x: Double): Double = round(x * 10.0) / 10.0

    /**
     * RFC 4180, which is what a spreadsheet writes: commas inside quotes, doubled
     * quotes as an escaped quote, CRLF or LF. Written out rather than borrowed
     * because the one library that would do this is JVM-only, and this has to run
     * on iOS as well.
     */
    fun parseCsv(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var i = 0
        val s = text.removePrefix(BOM.toString())
        while (i < s.length) {
            val c = s[i]
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < s.length && s[i + 1] == '"') {
                        field.append('"')
                        i++
                    } else {
                        quoted = false
                    }
                } else {
                    field.append(c)
                }
            } else {
                when (c) {
                    '"' -> quoted = true
                    ',' -> { row.add(field.toString()); field.clear() }
                    '\r' -> Unit
                    '\n' -> {
                        row.add(field.toString()); field.clear()
                        rows.add(row); row = mutableListOf()
                    }
                    else -> field.append(c)
                }
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) {
            row.add(field.toString())
            rows.add(row)
        }
        return rows.filter { r -> r.any { it.isNotBlank() } }
    }
}
