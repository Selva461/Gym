package dev.avles.gym.data

/**
 * The packaged breakfast and protein foods people who train actually buy: Yoga
 * Bar and MuscleBlaze oats, the muesli brands, protein bars, and the high-protein
 * dairy in the chiller. Named by brand because that is what is on the shelf and
 * in the search box.
 *
 * Per the serving the pack states, with figures close to the labels at the time
 * of writing. Recipes change: the label on your pack wins, and editing the entry
 * once to match it is worth it for something eaten every day. Calories are derived
 * from the macros (4/4/9, fibre at 2).
 */
internal object SeedFoodsBrands {

    private fun f(
        name: String, unit: String, kcal: Double, p: Double, c: Double, fat: Double, cat: String,
        aka: String = "",
        fib: Double = 0.0, sug: Double = 0.0, sat: Double = 0.0,
        na: Double = 0.0, k: Double = 0.0, ca: Double = 0.0,
        fe: Double = 0.0, mg: Double = 0.0, zn: Double = 0.0,
        va: Double = 0.0, vc: Double = 0.0, vd: Double = 0.0,
        b12: Double = 0.0, fol: Double = 0.0, o3: Double = 0.0
    ) = FoodItem(
        name = name, unitLabel = unit, kcal = kcal, proteinG = p, carbsG = c, fatG = fat,
        category = cat, aliases = aka,
        micros = Micros(
            fiberG = fib, sugarG = sug, satFatG = sat, sodiumMg = na, potassiumMg = k,
            calciumMg = ca, ironMg = fe, magnesiumMg = mg, zincMg = zn, vitAMcg = va,
            vitCMg = vc, vitDMcg = vd, vitB12Mcg = b12, folateMcg = fol, omega3G = o3
        )
    )

    fun all(): List<FoodItem> = oatsMuesliAndProteinFoods()

    private fun oatsMuesliAndProteinFoods() = listOf(
        f("Yoga Bar protein oats", "50 g", 180.0, 12.5, 26.0, 4.0, "Grains", aka = "Yogabar oats, Yoga Bar high protein oats", fib = 5.0, sug = 1.0, sat = 0.8, na = 60.0, k = 220.0, fe = 2.4, mg = 70.0, zn = 1.6),
        f("Yoga Bar protein muesli", "50 g", 200.0, 11.0, 28.0, 6.0, "Grains", aka = "Yogabar protein muesli", fib = 5.0, sug = 5.0, sat = 1.2, na = 70.0, k = 250.0, fe = 2.4, mg = 60.0, zn = 1.4),
        f("Yoga Bar super muesli", "50 g", 200.0, 7.0, 32.0, 6.0, "Grains", aka = "Yogabar muesli", fib = 5.0, sug = 6.0, sat = 1.0, na = 40.0, k = 220.0, fe = 2.2, mg = 55.0, zn = 1.2),
        f("Yoga Bar protein bar", "1 bar (60 g)", 235.0, 20.0, 22.0, 9.0, "Protein", aka = "Yogabar protein bar, 20 g protein bar", fib = 6.0, sug = 4.0, sat = 4.0, na = 150.0, k = 200.0, ca = 120.0, mg = 40.0),
        f("Yoga Bar multigrain bar", "1 bar (38 g)", 150.0, 4.0, 24.0, 5.0, "Snacks", aka = "Yogabar energy bar, Breakfast bar", fib = 3.0, sug = 9.0, sat = 1.5, na = 30.0, k = 150.0, mg = 35.0),
        f("MuscleBlaze high protein oats", "50 g", 190.0, 15.0, 26.0, 3.5, "Grains", aka = "MB protein oats, High protein oats", fib = 4.0, sug = 1.0, sat = 0.7, na = 80.0, k = 220.0, fe = 2.0, mg = 60.0),
        f("Kellogg's muesli fruit and nut", "45 g", 165.0, 4.5, 32.0, 3.0, "Grains", aka = "Kelloggs muesli", fib = 3.0, sug = 9.0, sat = 0.6, na = 60.0, k = 200.0, fe = 4.0, mg = 40.0),
        f("Bagrry's muesli no added sugar", "45 g", 165.0, 5.0, 30.0, 4.0, "Grains", aka = "Bagrrys muesli, Sugar free muesli", fib = 4.5, sug = 3.0, sat = 0.7, na = 20.0, k = 220.0, fe = 2.0, mg = 50.0),
        f("True Elements protein muesli", "45 g", 175.0, 9.0, 26.0, 5.0, "Grains", aka = "Protein muesli", fib = 4.0, sug = 4.0, sat = 0.8, na = 30.0, k = 250.0, fe = 2.4, mg = 60.0),
        f("Granola", "45 g", 205.0, 4.5, 30.0, 8.0, "Grains", aka = "Baked granola, Crunchy muesli", fib = 3.0, sug = 9.0, sat = 1.5, na = 40.0, k = 180.0, mg = 50.0),
        f("Muesli with milk", "1 bowl", 285.0, 11.0, 42.0, 9.0, "Grains", aka = "Muesli bowl", fib = 4.0, sug = 19.0, sat = 4.0, na = 120.0, k = 550.0, ca = 260.0, fe = 2.0, b12 = 0.8),
        f("Oats porridge with milk", "1 bowl", 270.0, 12.0, 37.0, 9.0, "Grains", aka = "Oats kanji with milk, Milk oats", fib = 4.0, sug = 10.0, sat = 4.0, na = 110.0, k = 500.0, ca = 260.0, fe = 1.8, mg = 70.0, b12 = 0.8),
        f("Oats with whey", "1 bowl", 270.0, 30.0, 30.0, 4.0, "Grains", aka = "Protein oats with whey", fib = 4.0, sug = 2.0, sat = 1.0, na = 120.0, k = 400.0, ca = 150.0, fe = 1.8, mg = 70.0),
        f("RiteBite Max Protein bar", "1 bar (67 g)", 255.0, 20.0, 26.0, 9.0, "Protein", aka = "Max protein bar", fib = 6.0, sug = 5.0, sat = 5.0, na = 150.0, k = 200.0, ca = 100.0),
        f("Whole Truth protein bar", "1 bar (52 g)", 220.0, 15.0, 24.0, 8.0, "Protein", aka = "The Whole Truth bar", fib = 5.0, sug = 10.0, sat = 2.0, na = 40.0, k = 250.0, mg = 50.0),
        f("Amul protein buttermilk", "200 ml", 100.0, 15.0, 6.0, 1.5, "Dairy", aka = "High protein buttermilk", fib = 0.0, sug = 6.0, sat = 1.0, na = 300.0, k = 250.0, ca = 200.0),
        f("Amul protein lassi", "200 ml", 160.0, 15.0, 20.0, 2.0, "Dairy", aka = "High protein lassi", fib = 0.0, sug = 18.0, sat = 1.4, na = 100.0, k = 300.0, ca = 250.0),
        f("Epigamia Greek yogurt", "90 g cup", 80.0, 7.0, 9.0, 2.0, "Dairy", aka = "Greek yogurt cup, Flavoured Greek yogurt", fib = 0.0, sug = 8.0, sat = 1.3, na = 40.0, k = 150.0, ca = 110.0)
    )
}
