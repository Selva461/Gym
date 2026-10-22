package dev.avles.gym.data

/**
 * Protein powders and gainers by brand, as sold in India: Optimum Nutrition,
 * MuscleBlaze, MyProtein, Dymatize, Isopure, MuscleTech, GNC, Avvatar, Nakpro,
 * AS-IT-IS, BigMuscles, Amul, plus plant and soy protein. Flagged as supplements,
 * so they file under the Supplements slot whatever meal is picked.
 *
 * Per the scoop the tub states, close to the labels at the time of writing.
 * Flavours differ by a few grams of carbohydrate; the label on your tub wins.
 * Calories are derived from the macros (4/4/9, fibre at 2).
 */
internal object SeedFoodsSupplements {

    private fun f(
        name: String, unit: String, kcal: Double, p: Double, c: Double, fat: Double, cat: String,
        aka: String = "",
        fib: Double = 0.0, sug: Double = 0.0, sat: Double = 0.0,
        na: Double = 0.0, k: Double = 0.0, ca: Double = 0.0,
        fe: Double = 0.0, mg: Double = 0.0, zn: Double = 0.0,
        va: Double = 0.0, vc: Double = 0.0, vd: Double = 0.0,
        b12: Double = 0.0, fol: Double = 0.0, o3: Double = 0.0,
        supp: Boolean = false
    ) = FoodItem(
        name = name, unitLabel = unit, kcal = kcal, proteinG = p, carbsG = c, fatG = fat,
        category = cat, aliases = aka, isSupplement = supp,
        micros = Micros(
            fiberG = fib, sugarG = sug, satFatG = sat, sodiumMg = na, potassiumMg = k,
            calciumMg = ca, ironMg = fe, magnesiumMg = mg, zincMg = zn, vitAMcg = va,
            vitCMg = vc, vitDMcg = vd, vitB12Mcg = b12, folateMcg = fol, omega3G = o3
        )
    )

    fun all(): List<FoodItem> = proteinPowders() + proteinSpreads()

    private fun proteinPowders() = listOf(
        f("ON Gold Standard whey", "1 scoop (30.4 g)", 120.0, 24.0, 3.0, 1.5, "Supplement", aka = "Optimum Nutrition whey, ON whey, Gold Standard", fib = 0.0, sug = 1.0, sat = 1.0, na = 130.0, k = 160.0, ca = 130.0, supp = true),
        f("ON Gold Standard casein", "1 scoop (34 g)", 115.0, 24.0, 3.0, 1.0, "Supplement", aka = "ON casein, Optimum casein", fib = 0.0, sug = 1.0, sat = 0.5, na = 200.0, k = 80.0, ca = 500.0, supp = true),
        f("MuscleBlaze Biozyme whey", "1 scoop (33 g)", 130.0, 25.0, 4.5, 1.6, "Supplement", aka = "MB Biozyme, MuscleBlaze whey", fib = 0.0, sug = 1.5, sat = 1.0, na = 110.0, k = 150.0, ca = 150.0, supp = true),
        f("MuscleBlaze Raw whey", "1 scoop (30 g)", 120.0, 24.0, 2.0, 1.8, "Supplement", aka = "MB raw whey, Unflavoured whey", fib = 0.0, sug = 1.5, sat = 1.2, na = 60.0, k = 150.0, ca = 140.0, supp = true),
        f("MyProtein Impact whey", "1 scoop (25 g)", 105.0, 21.0, 1.0, 1.9, "Supplement", aka = "Myprotein whey, Impact whey", fib = 0.0, sug = 1.0, sat = 1.3, na = 50.0, k = 100.0, ca = 100.0, supp = true),
        f("Dymatize ISO100", "1 scoop (30 g)", 110.0, 25.0, 1.0, 0.5, "Supplement", aka = "Dymatize isolate, ISO 100", fib = 0.0, sug = 1.0, sat = 0.2, na = 160.0, k = 160.0, ca = 100.0, supp = true),
        f("Isopure zero carb", "1 scoop (31 g)", 105.0, 25.0, 0.0, 0.5, "Supplement", aka = "Isopure isolate", fib = 0.0, sug = 0.0, sat = 0.2, na = 230.0, k = 60.0, ca = 100.0, supp = true),
        f("MuscleTech Nitro-Tech", "1 scoop (46 g)", 160.0, 30.0, 4.0, 2.5, "Supplement", aka = "Nitrotech, Nitro Tech whey", fib = 0.0, sug = 2.0, sat = 1.5, na = 180.0, k = 250.0, ca = 150.0, supp = true),
        f("GNC Pro Performance whey", "1 scoop (33 g)", 125.0, 24.0, 4.0, 1.5, "Supplement", aka = "GNC whey", fib = 0.0, sug = 2.0, sat = 1.0, na = 120.0, k = 150.0, ca = 130.0, supp = true),
        f("Avvatar whey", "1 scoop (33 g)", 135.0, 25.0, 4.0, 2.0, "Supplement", aka = "Avvatar Alpha whey", fib = 0.0, sug = 2.0, sat = 1.2, na = 70.0, k = 160.0, ca = 140.0, supp = true),
        f("Nakpro Perform whey", "1 scoop (30 g)", 120.0, 22.0, 4.0, 2.0, "Supplement", aka = "Nakpro whey", fib = 0.0, sug = 2.0, sat = 1.2, na = 70.0, k = 150.0, ca = 120.0, supp = true),
        f("AS-IT-IS whey concentrate", "1 scoop (30 g)", 120.0, 24.0, 2.0, 2.0, "Supplement", aka = "Asitis whey, As it is whey", fib = 0.0, sug = 1.5, sat = 1.3, na = 50.0, k = 150.0, ca = 140.0, supp = true),
        f("BigMuscles Premium Gold whey", "1 scoop (33 g)", 135.0, 24.0, 5.0, 2.0, "Supplement", aka = "Big Muscles whey", fib = 0.0, sug = 2.0, sat = 1.2, na = 100.0, k = 150.0, ca = 130.0, supp = true),
        f("Amul whey protein", "1 scoop (34 g)", 130.0, 25.0, 3.5, 1.5, "Supplement", aka = "Amul whey", fib = 0.0, sug = 2.0, sat = 1.0, na = 90.0, k = 160.0, ca = 150.0, supp = true),
        f("Plant protein powder", "1 scoop (33 g)", 130.0, 25.0, 3.0, 2.0, "Supplement", aka = "Pea protein, Vegan protein, Oziva plant protein", fib = 1.0, sug = 0.5, sat = 0.4, na = 350.0, k = 100.0, fe = 5.0, supp = true),
        f("Soy protein isolate", "1 scoop (30 g)", 115.0, 26.0, 1.0, 1.0, "Supplement", aka = "Soya protein powder", fib = 0.0, sug = 0.0, sat = 0.2, na = 300.0, k = 250.0, fe = 4.0, supp = true),
        f("MuscleBlaze Super Gainer XXL", "1 serving (75 g)", 280.0, 10.0, 55.0, 2.0, "Supplement", aka = "MB mass gainer, Super gainer", fib = 0.0, sug = 8.0, sat = 1.0, na = 120.0, k = 200.0, ca = 150.0, supp = true),
        f("ON Serious Mass", "2 scoops (167 g)", 1250.0, 50.0, 252.0, 4.5, "Supplement", aka = "Serious mass gainer", fib = 0.0, sug = 20.0, sat = 2.0, na = 400.0, k = 1200.0, ca = 600.0, fe = 6.0, supp = true)
    )

    private fun proteinSpreads() = listOf(
        f("Pintola high protein peanut butter", "2 tbsp (32 g)", 195.0, 10.0, 6.0, 15.0, "Nuts", aka = "Pintola peanut butter, High protein peanut butter", fib = 2.0, sug = 1.0, sat = 3.0, na = 40.0, k = 200.0, mg = 50.0, zn = 1.0)
    )
}
