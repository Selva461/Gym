package dev.avles.gym.data

import dev.avles.gym.domain.Assistance

/**
 * Starter catalogue: lifts with a progression range, and food with macros and
 * micronutrients.
 *
 * READ THIS ONCE, IT MATTERS:
 *
 * **Macros are approximations for a home-style portion.** Restaurant oil, coconut
 * and ghee vary enormously — the same chicken curry swings 150 kcal between two
 * kitchens. Every food is editable in Fuel, and correcting the five or six things
 * you eat weekly is worth more than any number of extra entries.
 *
 * **Micronutrients are rougher still.** There is no canonical reference for a
 * home-made kara kuzhambu the way there is for 100 g of chicken breast. Whole foods
 * here — eggs, milk, fish, spinach, nuts — are close to published values. Composite
 * dishes are built up from their main ingredients and should be read as "this meal
 * was a decent source of iron", never as a measurement.
 *
 * **A zero means not estimated, not proven absent.** Where a value was not worth
 * guessing it is left at zero rather than invented. The nutrient panel says so.
 *
 * **Supplements are the exception.** A magnesium tablet's label is a measurement.
 * Those numbers are as good as the bottle you bought.
 */
object SeedData {

    // ---------------------------------------------------------------- lifts

    /**
     * The lift catalogue and the starter splits live in [SeedExercises]. They are
     * long enough to need their own file, and nothing about them is food.
     */
    /** Assisted machines are flagged here, once, by the same rule a lift you add gets. */
    fun exercises(): List<Exercise> = SeedExercises.all().map {
        if (Assistance.looksAssisted(it.name)) it.copy(isAssisted = true) else it
    }

    fun splits(): List<Pair<String, List<String>>> = SeedExercises.splits()

    /**
     * The muscle groups the lift picker organises by and the new-lift dialog offers.
     * Exposed here so the UI and the tests have one place to ask, rather than each
     * keeping its own copy of the list to drift out of sync.
     */
    fun muscleGroups(): List<String> = SeedExercises.Groups.ALL

    // ----------------------------------------------------------------- food

    /**
     * Compact constructor so a food fits on one line and the file stays readable.
     * Everything after the category is optional and defaults to zero, which reads
     * as "not estimated".
     *
     * fib fibre g · sug sugar g · sat saturated fat g · na sodium mg · k potassium mg
     * ca calcium mg · fe iron mg · mg magnesium mg · zn zinc mg · va vitamin A mcg
     * vc vitamin C mg · vd vitamin D mcg · b12 vitamin B12 mcg · fol folate mcg
     * o3 omega-3 g
     */
    private fun f(
        name: String, unit: String, kcal: Double, p: Double, c: Double, fat: Double, cat: String,
        fib: Double = 0.0, sug: Double = 0.0, sat: Double = 0.0,
        na: Double = 0.0, k: Double = 0.0, ca: Double = 0.0,
        fe: Double = 0.0, mg: Double = 0.0, zn: Double = 0.0,
        va: Double = 0.0, vc: Double = 0.0, vd: Double = 0.0,
        b12: Double = 0.0, fol: Double = 0.0, o3: Double = 0.0,
        supp: Boolean = false
    ) = FoodItem(
        name = name, unitLabel = unit, kcal = kcal, proteinG = p, carbsG = c, fatG = fat,
        category = cat, isSupplement = supp,
        micros = Micros(
            fiberG = fib, sugarG = sug, satFatG = sat, sodiumMg = na, potassiumMg = k,
            calciumMg = ca, ironMg = fe, magnesiumMg = mg, zincMg = zn, vitAMcg = va,
            vitCMg = vc, vitDMcg = vd, vitB12Mcg = b12, folateMcg = fol, omega3G = o3
        )
    )

    fun foods(): List<FoodItem> = dosa() + idli() + tiffin() + rice() + southIndianSides() +
        southIndianNonVeg() + eggsAndDairy() + legumes() + worldCuisine() + breadsAndGrains() +
        vegetables() + fruit() + nutsAndSnacks() + drinks() + fats() + supplements() +
        moreDosa() + moreTiffin() + moreRice() + moreSides() + moreNonVeg() + northIndian() +
        sweets() + moreDrinks() + moreProtein() + moreVegetables() + moreFruit() +
        moreNuts() + moreDairy() + moreGrains() + moreSupplements() +
        SeedFoodsMore.all() + SeedFoodsTamil.all() + SeedFoodsStaples.all() +
        SeedFoodsRestaurant.all() + SeedFoodsMenus.all() + SeedFoodsBakery.all() +
        SeedFoodsStalls.all() + SeedFoodsStreet.all()

    /** Dosa, properly. Each one is a different food and the numbers say so. */
    private fun dosa(): List<FoodItem> = listOf(
        f("Plain dosa", "1 dosa", 133.0, 2.7, 22.0, 3.7, "Dosa", fib = 1.2, sat = 1.6, na = 210.0, k = 60.0, ca = 12.0, fe = 0.8, mg = 20.0, zn = 0.4, fol = 12.0),
        f("Masala dosa", "1 dosa", 285.0, 5.5, 44.0, 9.5, "Dosa", fib = 3.2, sat = 4.2, na = 420.0, k = 340.0, ca = 26.0, fe = 1.4, mg = 34.0, zn = 0.7, vc = 9.0, fol = 24.0),
        f("Ghee roast dosa", "1 dosa", 210.0, 3.0, 24.0, 11.0, "Dosa", fib = 1.2, sat = 6.5, na = 240.0, k = 65.0, ca = 14.0, fe = 0.9, mg = 21.0, zn = 0.4, va = 60.0),
        f("Paper roast dosa", "1 dosa", 165.0, 3.0, 26.0, 5.0, "Dosa", fib = 1.4, sat = 2.2, na = 250.0, k = 70.0, ca = 14.0, fe = 0.9, mg = 23.0, zn = 0.4),
        f("Onion dosa", "1 dosa", 175.0, 3.4, 27.0, 5.8, "Dosa", fib = 2.0, sug = 2.0, sat = 2.6, na = 280.0, k = 150.0, ca = 18.0, fe = 1.0, mg = 24.0, zn = 0.5, vc = 4.0, fol = 18.0),
        f("Rava dosa", "1 dosa", 195.0, 3.6, 26.0, 8.5, "Dosa", fib = 1.4, sat = 3.4, na = 350.0, k = 110.0, ca = 20.0, fe = 1.1, mg = 22.0, zn = 0.5),
        f("Rava masala dosa", "1 dosa", 300.0, 5.4, 42.0, 12.0, "Dosa", fib = 3.0, sat = 5.0, na = 480.0, k = 330.0, ca = 28.0, fe = 1.5, mg = 32.0, zn = 0.7, vc = 8.0),
        f("Set dosa", "1 piece", 95.0, 2.2, 15.0, 2.6, "Dosa", fib = 0.9, sat = 1.2, na = 160.0, k = 50.0, ca = 10.0, fe = 0.6, mg = 14.0, zn = 0.3),
        f("Neer dosa", "1 dosa", 75.0, 1.3, 14.0, 1.4, "Dosa", fib = 0.5, sat = 1.0, na = 120.0, k = 35.0, ca = 6.0, fe = 0.4, mg = 10.0, zn = 0.2),
        f("Ragi dosa", "1 dosa", 125.0, 3.2, 21.0, 3.0, "Dosa", fib = 2.8, sat = 1.3, na = 200.0, k = 110.0, ca = 108.0, fe = 1.6, mg = 46.0, zn = 0.6, fol = 10.0),
        f("Oats dosa", "1 dosa", 130.0, 4.0, 19.0, 4.0, "Dosa", fib = 2.6, sat = 1.4, na = 220.0, k = 130.0, ca = 22.0, fe = 1.3, mg = 40.0, zn = 0.8),
        f("Wheat dosa", "1 dosa", 140.0, 4.0, 23.0, 3.6, "Dosa", fib = 2.4, sat = 1.4, na = 230.0, k = 100.0, ca = 18.0, fe = 1.2, mg = 34.0, zn = 0.7),
        f("Pesarattu", "1 dosa", 155.0, 8.0, 22.0, 3.8, "Dosa", fib = 3.6, sat = 1.3, na = 240.0, k = 300.0, ca = 30.0, fe = 2.0, mg = 50.0, zn = 1.0, vc = 3.0, fol = 90.0),
        f("Adai", "1 adai", 205.0, 9.5, 27.0, 6.5, "Dosa", fib = 4.5, sat = 2.2, na = 300.0, k = 340.0, ca = 40.0, fe = 2.4, mg = 55.0, zn = 1.2, fol = 80.0),
        f("Podi dosa", "1 dosa", 190.0, 4.2, 24.0, 8.5, "Dosa", fib = 2.0, sat = 3.0, na = 420.0, k = 110.0, ca = 30.0, fe = 1.4, mg = 30.0, zn = 0.6)
    )

    private fun idli(): List<FoodItem> = listOf(
        f("Idli", "1 piece", 58.0, 2.0, 12.0, 0.2, "Idli", fib = 0.6, sat = 0.1, na = 120.0, k = 40.0, ca = 8.0, fe = 0.5, mg = 12.0, zn = 0.3, fol = 8.0),
        f("Rava idli", "1 piece", 85.0, 2.4, 14.0, 2.2, "Idli", fib = 0.8, sat = 1.1, na = 210.0, k = 60.0, ca = 14.0, fe = 0.6, mg = 12.0, zn = 0.3),
        f("Kanchipuram idli", "1 piece", 110.0, 3.0, 17.0, 3.4, "Idli", fib = 1.0, sat = 1.8, na = 230.0, k = 70.0, ca = 14.0, fe = 0.7, mg = 16.0, zn = 0.4, va = 25.0),
        f("Podi idli", "2 pieces with podi and oil", 190.0, 5.2, 27.0, 7.0, "Idli", fib = 2.0, sat = 2.4, na = 460.0, k = 120.0, ca = 34.0, fe = 1.5, mg = 36.0, zn = 0.8),
        f("Idli with sambar", "2 idli + 1 cup sambar", 255.0, 10.0, 44.0, 4.4, "Idli", fib = 5.0, sat = 1.2, na = 620.0, k = 420.0, ca = 60.0, fe = 2.6, mg = 60.0, zn = 1.3, vc = 6.0, fol = 90.0),
        f("Thatte idli", "1 piece", 95.0, 3.2, 19.0, 0.4, "Idli", fib = 1.0, sat = 0.2, na = 190.0, k = 65.0, ca = 13.0, fe = 0.8, mg = 19.0, zn = 0.5),
        f("Ghee podi mini idli", "6 pieces", 230.0, 6.0, 33.0, 8.5, "Idli", fib = 2.2, sat = 4.6, na = 520.0, k = 140.0, ca = 38.0, fe = 1.7, mg = 40.0, zn = 0.9, va = 55.0)
    )

    private fun tiffin(): List<FoodItem> = listOf(
        f("Idiyappam", "1 piece", 85.0, 1.8, 19.0, 0.3, "Tiffin", fib = 0.7, sat = 0.2, na = 60.0, k = 25.0, ca = 6.0, fe = 0.3, mg = 9.0, zn = 0.2),
        f("Appam", "1 piece", 120.0, 2.2, 24.0, 1.5, "Tiffin", fib = 0.8, sug = 2.5, sat = 1.1, na = 130.0, k = 55.0, ca = 10.0, fe = 0.5, mg = 12.0, zn = 0.3),
        f("Puttu", "1 cup", 185.0, 4.0, 36.0, 2.5, "Tiffin", fib = 2.6, sat = 1.9, na = 90.0, k = 120.0, ca = 20.0, fe = 1.2, mg = 38.0, zn = 0.7),
        f("Rava upma", "1 cup", 250.0, 6.0, 38.0, 8.0, "Tiffin", fib = 2.2, sat = 3.2, na = 480.0, k = 160.0, ca = 26.0, fe = 1.4, mg = 28.0, zn = 0.7, vc = 4.0),
        f("Semiya upma", "1 cup", 230.0, 5.4, 38.0, 6.2, "Tiffin", fib = 1.8, sat = 2.6, na = 440.0, k = 130.0, ca = 20.0, fe = 1.2, mg = 24.0, zn = 0.6),
        f("Aval upma", "1 cup", 215.0, 4.4, 38.0, 5.4, "Tiffin", fib = 2.4, sat = 2.2, na = 400.0, k = 180.0, ca = 22.0, fe = 2.6, mg = 30.0, zn = 0.7),
        f("Ven pongal", "1 cup", 280.0, 7.0, 40.0, 9.0, "Tiffin", fib = 2.6, sat = 4.8, na = 450.0, k = 200.0, ca = 26.0, fe = 1.8, mg = 44.0, zn = 1.1, va = 50.0, fol = 45.0),
        f("Sakkarai pongal", "1 cup", 380.0, 6.0, 64.0, 11.0, "Tiffin", fib = 2.0, sug = 32.0, sat = 6.5, na = 90.0, k = 220.0, ca = 30.0, fe = 1.9, mg = 40.0, zn = 1.0, va = 60.0),
        f("Medu vada", "1 piece", 135.0, 4.0, 13.0, 7.5, "Tiffin", fib = 1.8, sat = 2.4, na = 250.0, k = 130.0, ca = 18.0, fe = 1.2, mg = 30.0, zn = 0.7, fol = 40.0),
        f("Masala vada", "1 piece", 150.0, 5.5, 16.0, 7.5, "Tiffin", fib = 3.0, sat = 2.4, na = 280.0, k = 200.0, ca = 24.0, fe = 1.6, mg = 36.0, zn = 0.9, fol = 60.0),
        f("Dahi vada", "2 pieces", 240.0, 8.0, 26.0, 11.0, "Tiffin", fib = 2.4, sug = 5.0, sat = 4.4, na = 400.0, k = 300.0, ca = 130.0, fe = 1.5, mg = 40.0, zn = 1.2, b12 = 0.4, va = 40.0),
        f("Uttapam", "1 piece", 190.0, 4.5, 30.0, 5.5, "Tiffin", fib = 2.2, sat = 2.4, na = 300.0, k = 160.0, ca = 20.0, fe = 1.1, mg = 26.0, zn = 0.6, vc = 5.0),
        f("Onion uttapam", "1 piece", 210.0, 5.0, 32.0, 6.5, "Tiffin", fib = 2.6, sug = 2.5, sat = 2.8, na = 340.0, k = 200.0, ca = 24.0, fe = 1.2, mg = 28.0, zn = 0.6, vc = 6.0),
        f("Parotta", "1 parotta", 260.0, 5.0, 36.0, 10.0, "Tiffin", fib = 1.6, sat = 5.0, na = 380.0, k = 80.0, ca = 18.0, fe = 1.4, mg = 20.0, zn = 0.6),
        f("Veechu parotta", "1 parotta", 310.0, 6.0, 40.0, 13.5, "Tiffin", fib = 1.8, sat = 6.5, na = 430.0, k = 95.0, ca = 22.0, fe = 1.6, mg = 24.0, zn = 0.7),
        f("Kothu parotta, chicken", "1 plate", 520.0, 24.0, 52.0, 24.0, "Tiffin", fib = 3.0, sat = 9.0, na = 980.0, k = 420.0, ca = 60.0, fe = 3.2, mg = 50.0, zn = 2.4, b12 = 0.5, vc = 8.0),
        f("Chapati", "1 chapati", 104.0, 3.0, 18.0, 2.5, "Tiffin", fib = 2.2, sat = 1.0, na = 150.0, k = 90.0, ca = 12.0, fe = 1.1, mg = 30.0, zn = 0.6, fol = 12.0),
        f("Poori", "1 piece", 130.0, 2.6, 15.0, 6.5, "Tiffin", fib = 1.2, sat = 2.8, na = 130.0, k = 60.0, ca = 10.0, fe = 0.9, mg = 20.0, zn = 0.5),
        f("Bhatura", "1 piece", 250.0, 5.0, 33.0, 11.0, "Tiffin", fib = 1.4, sat = 4.5, na = 340.0, k = 80.0, ca = 20.0, fe = 1.4, mg = 22.0, zn = 0.6),
        f("Naan", "1 piece", 260.0, 7.5, 45.0, 5.0, "Tiffin", fib = 2.0, sug = 3.0, sat = 2.0, na = 420.0, k = 110.0, ca = 60.0, fe = 2.4, mg = 26.0, zn = 0.8),
        f("Butter naan", "1 piece", 310.0, 7.5, 45.0, 11.0, "Tiffin", fib = 2.0, sug = 3.0, sat = 5.5, na = 480.0, k = 115.0, ca = 65.0, fe = 2.4, mg = 26.0, zn = 0.8, va = 70.0),
        f("Tandoori roti", "1 piece", 145.0, 4.5, 27.0, 2.2, "Tiffin", fib = 3.0, sat = 0.9, na = 220.0, k = 120.0, ca = 18.0, fe = 1.6, mg = 38.0, zn = 0.9)
    )

    private fun rice(): List<FoodItem> = listOf(
        f("Steamed rice", "1 cup (150 g)", 205.0, 4.2, 45.0, 0.4, "Rice", fib = 0.6, sat = 0.1, na = 2.0, k = 55.0, ca = 15.0, fe = 1.9, mg = 19.0, zn = 0.8, fol = 90.0),
        f("Brown rice", "1 cup (150 g)", 215.0, 5.0, 45.0, 1.7, "Rice", fib = 3.5, sat = 0.4, na = 5.0, k = 150.0, ca = 20.0, fe = 1.0, mg = 84.0, zn = 1.2, fol = 18.0),
        f("Curd rice", "1 cup", 230.0, 7.0, 35.0, 6.0, "Rice", fib = 0.8, sug = 5.0, sat = 3.4, na = 380.0, k = 220.0, ca = 180.0, fe = 1.0, mg = 26.0, zn = 1.0, b12 = 0.5, va = 45.0),
        f("Lemon rice", "1 cup", 265.0, 5.0, 44.0, 7.5, "Rice", fib = 1.6, sat = 2.6, na = 420.0, k = 130.0, ca = 26.0, fe = 1.6, mg = 30.0, zn = 0.9, vc = 8.0),
        f("Tamarind rice", "1 cup", 300.0, 5.5, 47.0, 10.0, "Rice", fib = 2.4, sug = 4.0, sat = 3.2, na = 560.0, k = 240.0, ca = 34.0, fe = 2.0, mg = 36.0, zn = 1.0),
        f("Coconut rice", "1 cup", 310.0, 5.0, 44.0, 13.0, "Rice", fib = 2.2, sat = 9.0, na = 400.0, k = 160.0, ca = 24.0, fe = 1.7, mg = 32.0, zn = 0.9),
        f("Tomato rice", "1 cup", 270.0, 5.4, 44.0, 8.0, "Rice", fib = 2.0, sug = 4.0, sat = 2.8, na = 450.0, k = 260.0, ca = 26.0, fe = 1.8, mg = 30.0, zn = 0.9, vc = 12.0, va = 45.0),
        f("Ghee rice", "1 cup", 300.0, 5.0, 45.0, 11.0, "Rice", fib = 1.0, sat = 6.5, na = 340.0, k = 90.0, ca = 20.0, fe = 1.7, mg = 22.0, zn = 0.9, va = 70.0),
        f("Jeera rice", "1 cup", 255.0, 4.6, 44.0, 6.5, "Rice", fib = 1.0, sat = 3.5, na = 300.0, k = 80.0, ca = 22.0, fe = 1.8, mg = 22.0, zn = 0.9),
        f("Bisibelebath", "1 cup", 330.0, 10.0, 48.0, 11.0, "Rice", fib = 5.0, sat = 4.6, na = 620.0, k = 420.0, ca = 50.0, fe = 2.8, mg = 55.0, zn = 1.5, vc = 10.0, fol = 110.0),
        f("Sambar rice", "1 cup", 290.0, 9.5, 45.0, 8.0, "Rice", fib = 4.6, sat = 2.6, na = 640.0, k = 420.0, ca = 46.0, fe = 2.6, mg = 52.0, zn = 1.4, vc = 6.0, fol = 105.0),
        f("Vegetable biryani", "1 plate", 420.0, 10.0, 62.0, 14.0, "Rice", fib = 4.5, sug = 4.0, sat = 5.5, na = 780.0, k = 420.0, ca = 60.0, fe = 2.6, mg = 50.0, zn = 1.4, vc = 10.0, va = 120.0),
        f("Chicken biryani", "1 plate", 560.0, 30.0, 62.0, 21.0, "Rice", fib = 3.4, sug = 3.5, sat = 7.5, na = 950.0, k = 520.0, ca = 60.0, fe = 3.2, mg = 58.0, zn = 2.6, b12 = 0.6, vc = 8.0),
        f("Mutton biryani", "1 plate", 640.0, 30.0, 62.0, 29.0, "Rice", fib = 3.4, sug = 3.5, sat = 11.0, na = 980.0, k = 540.0, ca = 62.0, fe = 4.2, mg = 60.0, zn = 4.5, b12 = 2.2, vc = 8.0),
        f("Egg biryani", "1 plate", 500.0, 19.0, 62.0, 20.0, "Rice", fib = 3.4, sug = 3.5, sat = 7.0, na = 880.0, k = 440.0, ca = 70.0, fe = 3.4, mg = 52.0, zn = 2.0, vd = 2.2, b12 = 1.1, va = 150.0),
        f("Prawn biryani", "1 plate", 520.0, 27.0, 62.0, 18.0, "Rice", fib = 3.2, sug = 3.5, sat = 6.5, na = 960.0, k = 440.0, ca = 90.0, fe = 3.0, mg = 62.0, zn = 2.2, b12 = 1.4, o3 = 0.3),
        f("Veg fried rice", "1 cup", 300.0, 6.5, 48.0, 9.0, "Rice", fib = 2.6, sug = 3.0, sat = 1.8, na = 880.0, k = 220.0, ca = 30.0, fe = 1.6, mg = 30.0, zn = 0.9, vc = 12.0),
        f("Chicken fried rice", "1 cup", 370.0, 18.0, 48.0, 11.0, "Rice", fib = 2.2, sug = 3.0, sat = 2.6, na = 980.0, k = 320.0, ca = 32.0, fe = 1.8, mg = 36.0, zn = 1.6, b12 = 0.3)
    )

    private fun southIndianSides(): List<FoodItem> = listOf(
        f("Sambar", "1 cup", 140.0, 6.0, 20.0, 4.0, "Sides", fib = 5.0, sug = 3.0, sat = 1.0, na = 520.0, k = 420.0, ca = 45.0, fe = 2.2, mg = 48.0, zn = 1.0, vc = 8.0, fol = 90.0, va = 60.0),
        f("Rasam", "1 cup", 60.0, 2.0, 8.0, 2.0, "Sides", fib = 1.6, sat = 0.5, na = 480.0, k = 260.0, ca = 22.0, fe = 1.2, mg = 24.0, zn = 0.4, vc = 14.0, fol = 30.0),
        f("Pepper rasam", "1 cup", 65.0, 2.2, 8.0, 2.4, "Sides", fib = 1.8, sat = 0.6, na = 500.0, k = 280.0, ca = 26.0, fe = 1.4, mg = 26.0, zn = 0.5, vc = 12.0),
        f("Tomato rasam", "1 cup", 70.0, 2.0, 10.0, 2.2, "Sides", fib = 1.8, sug = 3.5, sat = 0.5, na = 470.0, k = 330.0, ca = 22.0, fe = 1.2, mg = 24.0, zn = 0.4, vc = 20.0, va = 60.0),
        f("Vatha kuzhambu", "1 cup", 160.0, 3.0, 18.0, 8.5, "Sides", fib = 3.0, sug = 4.0, sat = 1.6, na = 780.0, k = 340.0, ca = 40.0, fe = 1.8, mg = 32.0, zn = 0.6),
        f("Mor kuzhambu", "1 cup", 150.0, 5.0, 13.0, 9.0, "Sides", fib = 2.0, sug = 4.0, sat = 5.0, na = 560.0, k = 300.0, ca = 150.0, fe = 1.2, mg = 30.0, zn = 0.8, b12 = 0.4),
        f("Puli kuzhambu", "1 cup", 155.0, 3.2, 17.0, 8.0, "Sides", fib = 3.2, sug = 4.0, sat = 1.6, na = 740.0, k = 360.0, ca = 42.0, fe = 1.9, mg = 34.0, zn = 0.6),
        f("Kara kuzhambu", "1 cup", 170.0, 4.0, 17.0, 9.5, "Sides", fib = 3.4, sat = 2.6, na = 760.0, k = 380.0, ca = 46.0, fe = 2.0, mg = 36.0, zn = 0.7, vc = 6.0),
        f("Meen kuzhambu", "1 cup", 210.0, 20.0, 9.0, 10.5, "Sides", fib = 2.0, sat = 3.0, na = 720.0, k = 420.0, ca = 60.0, fe = 1.8, mg = 45.0, zn = 1.0, vd = 3.5, b12 = 2.4, o3 = 0.6),
        f("Mixed veg poriyal", "1/2 cup", 80.0, 2.0, 8.0, 4.5, "Sides", fib = 3.0, sug = 2.5, sat = 2.8, na = 220.0, k = 240.0, ca = 40.0, fe = 1.0, mg = 22.0, zn = 0.4, vc = 22.0, va = 180.0, fol = 40.0),
        f("Beans poriyal", "1/2 cup", 75.0, 2.2, 8.0, 4.0, "Sides", fib = 3.2, sat = 2.6, na = 210.0, k = 220.0, ca = 40.0, fe = 1.0, mg = 22.0, zn = 0.4, vc = 8.0, va = 40.0, fol = 34.0),
        f("Cabbage poriyal", "1/2 cup", 70.0, 1.8, 8.0, 3.8, "Sides", fib = 2.6, sug = 3.0, sat = 2.6, na = 200.0, k = 200.0, ca = 36.0, fe = 0.7, mg = 16.0, zn = 0.3, vc = 28.0, fol = 44.0),
        f("Keerai masiyal", "1/2 cup", 85.0, 4.0, 7.0, 4.5, "Sides", fib = 3.4, sat = 2.4, na = 250.0, k = 480.0, ca = 130.0, fe = 3.0, mg = 70.0, zn = 0.6, vc = 18.0, va = 470.0, fol = 140.0),
        f("Avial", "1/2 cup", 140.0, 3.0, 11.0, 9.5, "Sides", fib = 3.4, sug = 3.0, sat = 6.5, na = 280.0, k = 320.0, ca = 55.0, fe = 1.2, mg = 30.0, zn = 0.5, vc = 20.0, va = 160.0),
        f("Kootu", "1/2 cup", 120.0, 5.0, 13.0, 5.5, "Sides", fib = 4.0, sat = 3.4, na = 300.0, k = 300.0, ca = 45.0, fe = 1.6, mg = 38.0, zn = 0.8, vc = 10.0, fol = 70.0),
        f("Coconut chutney", "2 tbsp", 90.0, 1.5, 3.0, 8.0, "Sides", fib = 2.0, sug = 1.2, sat = 6.5, na = 180.0, k = 90.0, ca = 10.0, fe = 0.6, mg = 16.0, zn = 0.4),
        f("Tomato chutney", "2 tbsp", 55.0, 1.0, 6.0, 3.0, "Sides", fib = 1.2, sug = 3.0, sat = 0.6, na = 220.0, k = 150.0, ca = 10.0, fe = 0.6, mg = 10.0, zn = 0.2, vc = 10.0, va = 40.0),
        f("Onion chutney", "2 tbsp", 65.0, 1.2, 7.0, 3.6, "Sides", fib = 1.4, sug = 3.4, sat = 0.8, na = 230.0, k = 130.0, ca = 12.0, fe = 0.7, mg = 10.0, zn = 0.2, vc = 4.0),
        f("Mint chutney", "2 tbsp", 45.0, 1.2, 4.0, 2.8, "Sides", fib = 1.4, sat = 1.8, na = 200.0, k = 130.0, ca = 30.0, fe = 1.0, mg = 14.0, zn = 0.2, vc = 8.0, va = 90.0),
        f("Milagai podi with oil", "1 tbsp", 90.0, 2.6, 6.0, 6.0, "Sides", fib = 2.0, sat = 1.0, na = 320.0, k = 110.0, ca = 26.0, fe = 1.2, mg = 30.0, zn = 0.5),
        f("Appalam", "1 piece", 45.0, 1.8, 6.0, 1.6, "Sides", fib = 1.0, sat = 0.3, na = 320.0, k = 90.0, ca = 8.0, fe = 0.5, mg = 14.0, zn = 0.3),
        f("Mango pickle", "1 tbsp", 60.0, 0.3, 3.0, 5.5, "Sides", fib = 0.6, sug = 1.5, sat = 0.7, na = 620.0, k = 30.0, ca = 6.0, fe = 0.3, mg = 5.0, zn = 0.1)
    )

    private fun southIndianNonVeg(): List<FoodItem> = listOf(
        f("Chicken curry, Chettinad", "1 cup (200 g)", 290.0, 27.0, 8.0, 17.0, "Protein", fib = 2.0, sat = 6.0, na = 680.0, k = 420.0, ca = 40.0, fe = 2.2, mg = 45.0, zn = 2.2, b12 = 0.5, vc = 6.0),
        f("Chicken gravy, home style", "1 cup", 240.0, 25.0, 6.0, 13.0, "Protein", fib = 1.4, sat = 4.4, na = 620.0, k = 400.0, ca = 32.0, fe = 1.8, mg = 40.0, zn = 2.0, b12 = 0.5),
        f("Pepper chicken", "1 cup", 260.0, 28.0, 5.0, 14.0, "Protein", fib = 1.4, sat = 4.8, na = 700.0, k = 430.0, ca = 36.0, fe = 2.0, mg = 44.0, zn = 2.3, b12 = 0.5),
        f("Chicken 65", "100 g", 250.0, 20.0, 12.0, 14.0, "Protein", fib = 0.8, sat = 4.0, na = 780.0, k = 290.0, ca = 26.0, fe = 1.5, mg = 30.0, zn = 1.6, b12 = 0.4),
        f("Chicken kurma", "1 cup", 280.0, 24.0, 10.0, 16.0, "Protein", fib = 2.2, sug = 3.0, sat = 7.5, na = 640.0, k = 400.0, ca = 55.0, fe = 2.0, mg = 42.0, zn = 2.0, b12 = 0.5),
        f("Butter chicken", "1 cup", 380.0, 26.0, 12.0, 25.0, "Protein", fib = 1.6, sug = 6.0, sat = 12.0, na = 820.0, k = 450.0, ca = 90.0, fe = 2.0, mg = 40.0, zn = 2.1, b12 = 0.7, va = 180.0),
        f("Chicken tikka", "4 pieces", 210.0, 28.0, 4.0, 9.0, "Protein", fib = 0.6, sug = 2.0, sat = 3.2, na = 600.0, k = 400.0, ca = 60.0, fe = 1.4, mg = 34.0, zn = 1.8, b12 = 0.6),
        f("Grilled chicken breast", "100 g", 165.0, 31.0, 0.0, 3.6, "Protein", sat = 1.0, na = 74.0, k = 256.0, ca = 15.0, fe = 1.0, mg = 29.0, zn = 1.0, b12 = 0.3),
        f("Tandoori chicken, leg", "1 piece", 195.0, 24.0, 2.0, 10.0, "Protein", fib = 0.4, sug = 1.0, sat = 3.0, na = 520.0, k = 300.0, ca = 40.0, fe = 1.3, mg = 28.0, zn = 2.0, b12 = 0.5),
        f("Mutton kuzhambu", "1 cup", 330.0, 24.0, 7.0, 23.0, "Protein", fib = 1.8, sat = 9.5, na = 700.0, k = 380.0, ca = 36.0, fe = 3.4, mg = 38.0, zn = 4.5, b12 = 2.4),
        f("Mutton chukka", "1 cup", 350.0, 28.0, 6.0, 24.0, "Protein", fib = 1.6, sat = 10.0, na = 740.0, k = 400.0, ca = 34.0, fe = 3.8, mg = 40.0, zn = 5.0, b12 = 2.6),
        f("Vanjaram fish fry", "1 piece (100 g)", 210.0, 22.0, 5.0, 11.0, "Protein", fib = 0.4, sat = 2.6, na = 520.0, k = 400.0, ca = 30.0, fe = 1.2, mg = 40.0, zn = 0.8, vd = 6.0, b12 = 2.0, o3 = 0.8),
        f("Nethili fry", "50 g", 140.0, 13.0, 4.0, 8.0, "Protein", fib = 0.3, sat = 1.8, na = 480.0, k = 200.0, ca = 130.0, fe = 1.6, mg = 25.0, zn = 0.8, vd = 3.0, b12 = 3.0, o3 = 0.7),
        f("Sardine curry", "1 cup", 240.0, 22.0, 7.0, 13.0, "Protein", fib = 1.6, sat = 3.4, na = 640.0, k = 420.0, ca = 240.0, fe = 2.4, mg = 45.0, zn = 1.2, vd = 5.0, b12 = 8.0, o3 = 1.4),
        f("Prawn masala", "1 cup", 200.0, 24.0, 6.0, 9.0, "Protein", fib = 1.4, sat = 2.4, na = 760.0, k = 280.0, ca = 90.0, fe = 1.6, mg = 50.0, zn = 1.8, b12 = 1.4, o3 = 0.3),
        f("Prawn fry", "100 g", 175.0, 22.0, 4.0, 8.0, "Protein", fib = 0.4, sat = 2.0, na = 700.0, k = 260.0, ca = 80.0, fe = 1.4, mg = 45.0, zn = 1.6, b12 = 1.3, o3 = 0.3),
        f("Crab masala", "1 cup", 190.0, 20.0, 7.0, 9.0, "Protein", fib = 1.4, sat = 2.6, na = 820.0, k = 300.0, ca = 95.0, fe = 1.2, mg = 45.0, zn = 4.0, b12 = 6.0, o3 = 0.4),
        f("Grilled salmon", "100 g", 208.0, 22.0, 0.0, 13.0, "Protein", sat = 3.1, na = 60.0, k = 380.0, ca = 12.0, fe = 0.8, mg = 30.0, zn = 0.6, vd = 11.0, b12 = 3.2, o3 = 2.2),
        f("Tuna, canned in water", "100 g", 116.0, 26.0, 0.0, 1.0, "Protein", sat = 0.3, na = 320.0, k = 240.0, ca = 10.0, fe = 1.0, mg = 28.0, zn = 0.7, vd = 1.7, b12 = 2.5, o3 = 0.3)
    )

    private fun eggsAndDairy(): List<FoodItem> = listOf(
        f("Boiled egg", "1 large", 78.0, 6.3, 0.6, 5.3, "Protein", sat = 1.6, na = 62.0, k = 63.0, ca = 25.0, fe = 0.6, mg = 6.0, zn = 0.5, va = 80.0, vd = 1.1, b12 = 0.6, fol = 22.0, o3 = 0.04),
        f("Egg white", "1 large", 17.0, 3.6, 0.2, 0.1, "Protein", na = 55.0, k = 54.0, ca = 2.0, mg = 4.0, zn = 0.0, b12 = 0.1),
        f("Egg podimas", "2 eggs", 210.0, 13.0, 3.0, 16.0, "Protein", fib = 0.8, sat = 4.6, na = 380.0, k = 220.0, ca = 60.0, fe = 1.8, mg = 20.0, zn = 1.2, va = 170.0, vd = 2.2, b12 = 1.1, fol = 50.0),
        f("Omelette", "2 eggs", 200.0, 13.0, 2.0, 15.0, "Protein", fib = 0.4, sat = 4.4, na = 340.0, k = 190.0, ca = 55.0, fe = 1.6, mg = 16.0, zn = 1.1, va = 165.0, vd = 2.2, b12 = 1.1, fol = 46.0),
        f("Egg curry", "2 eggs in gravy", 290.0, 14.0, 10.0, 21.0, "Protein", fib = 2.0, sug = 3.5, sat = 6.5, na = 620.0, k = 340.0, ca = 75.0, fe = 2.2, mg = 28.0, zn = 1.3, va = 200.0, vd = 2.2, b12 = 1.1, vc = 8.0),
        f("Toned milk", "200 ml", 118.0, 6.4, 9.6, 6.2, "Dairy", sug = 9.6, sat = 4.0, na = 85.0, k = 300.0, ca = 240.0, fe = 0.1, mg = 22.0, zn = 0.8, va = 90.0, vd = 1.2, b12 = 0.9, fol = 10.0),
        f("Full fat milk", "200 ml", 150.0, 7.0, 10.0, 8.5, "Dairy", sug = 10.0, sat = 5.4, na = 90.0, k = 320.0, ca = 250.0, fe = 0.1, mg = 24.0, zn = 0.9, va = 100.0, vd = 1.3, b12 = 1.0),
        f("Curd", "1 cup (200 g)", 120.0, 7.0, 9.0, 6.0, "Dairy", sug = 8.0, sat = 3.8, na = 90.0, k = 320.0, ca = 240.0, fe = 0.2, mg = 24.0, zn = 1.1, va = 60.0, vd = 0.2, b12 = 0.8, fol = 18.0),
        f("Greek yogurt", "150 g", 130.0, 15.0, 6.0, 5.0, "Dairy", sug = 5.0, sat = 3.2, na = 60.0, k = 220.0, ca = 170.0, fe = 0.1, mg = 18.0, zn = 0.9, b12 = 0.9),
        f("Buttermilk", "1 glass (200 ml)", 55.0, 3.0, 5.0, 2.2, "Dairy", sug = 4.5, sat = 1.4, na = 260.0, k = 250.0, ca = 130.0, fe = 0.1, mg = 18.0, zn = 0.5, b12 = 0.4),
        f("Paneer", "100 g", 296.0, 18.0, 4.0, 23.0, "Dairy", sug = 3.0, sat = 14.0, na = 20.0, k = 130.0, ca = 480.0, fe = 0.3, mg = 22.0, zn = 1.9, va = 180.0, b12 = 0.8),
        f("Cheese slice", "1 slice (20 g)", 70.0, 4.0, 1.0, 6.0, "Dairy", sat = 3.8, na = 320.0, k = 25.0, ca = 130.0, fe = 0.1, mg = 6.0, zn = 0.6, va = 55.0, b12 = 0.2),
        f("Cottage cheese, low fat", "100 g", 98.0, 11.0, 3.4, 4.3, "Dairy", sug = 2.7, sat = 2.7, na = 360.0, k = 104.0, ca = 83.0, fe = 0.1, mg = 8.0, zn = 0.4, b12 = 0.4)
    )

    private fun legumes(): List<FoodItem> = listOf(
        f("Toor dal, cooked", "1 cup", 200.0, 11.0, 32.0, 3.0, "Legumes", fib = 8.0, sat = 0.6, na = 320.0, k = 420.0, ca = 40.0, fe = 2.4, mg = 60.0, zn = 1.4, fol = 130.0),
        f("Moong dal, cooked", "1 cup", 190.0, 12.0, 30.0, 2.5, "Legumes", fib = 7.5, sat = 0.5, na = 300.0, k = 480.0, ca = 45.0, fe = 2.6, mg = 70.0, zn = 1.5, fol = 160.0),
        f("Chana masala", "1 cup", 270.0, 12.0, 38.0, 8.0, "Legumes", fib = 10.0, sug = 5.0, sat = 1.6, na = 640.0, k = 520.0, ca = 80.0, fe = 3.2, mg = 78.0, zn = 2.0, vc = 10.0, fol = 180.0),
        f("Rajma", "1 cup", 260.0, 13.0, 40.0, 5.5, "Legumes", fib = 11.0, sug = 4.0, sat = 1.2, na = 600.0, k = 620.0, ca = 70.0, fe = 3.6, mg = 82.0, zn = 1.8, vc = 6.0, fol = 200.0),
        f("Soya chunks, cooked", "1 cup", 170.0, 26.0, 14.0, 2.0, "Legumes", fib = 6.0, sat = 0.4, na = 400.0, k = 520.0, ca = 120.0, fe = 5.0, mg = 90.0, zn = 2.4, fol = 90.0),
        f("Tofu", "100 g", 144.0, 15.0, 3.0, 9.0, "Legumes", fib = 2.0, sat = 1.3, na = 15.0, k = 140.0, ca = 350.0, fe = 2.7, mg = 60.0, zn = 1.6, fol = 20.0, o3 = 0.4),
        f("Sprouted moong", "1 cup", 60.0, 5.0, 10.0, 0.4, "Legumes", fib = 3.0, sat = 0.1, na = 10.0, k = 300.0, ca = 30.0, fe = 1.4, mg = 40.0, zn = 0.8, vc = 18.0, fol = 130.0),
        f("Black chana sundal", "1/2 cup", 160.0, 8.0, 22.0, 4.5, "Legumes", fib = 7.0, sat = 3.0, na = 280.0, k = 340.0, ca = 60.0, fe = 2.4, mg = 55.0, zn = 1.4, fol = 120.0)
    )

    private fun worldCuisine(): List<FoodItem> = listOf(
        // Italian
        f("Spaghetti, tomato sauce", "1 plate", 380.0, 13.0, 68.0, 7.0, "Italian", fib = 5.0, sug = 8.0, sat = 1.4, na = 620.0, k = 480.0, ca = 50.0, fe = 2.8, mg = 55.0, zn = 1.4, vc = 14.0, va = 70.0, fol = 90.0),
        f("Pasta alfredo", "1 plate", 580.0, 18.0, 62.0, 29.0, "Italian", fib = 3.4, sug = 5.0, sat = 16.0, na = 820.0, k = 300.0, ca = 260.0, fe = 2.2, mg = 45.0, zn = 2.0, va = 220.0, b12 = 0.6),
        f("Margherita pizza", "1 slice", 220.0, 9.0, 27.0, 8.5, "Italian", fib = 1.8, sug = 3.0, sat = 4.0, na = 520.0, k = 150.0, ca = 150.0, fe = 1.6, mg = 20.0, zn = 1.0, va = 70.0, b12 = 0.3),
        f("Pepperoni pizza", "1 slice", 285.0, 12.0, 28.0, 14.0, "Italian", fib = 1.8, sug = 3.0, sat = 6.0, na = 720.0, k = 180.0, ca = 160.0, fe = 1.8, mg = 22.0, zn = 1.3, b12 = 0.4),
        f("Lasagna", "1 portion", 420.0, 24.0, 35.0, 21.0, "Italian", fib = 3.0, sug = 7.0, sat = 10.0, na = 880.0, k = 520.0, ca = 280.0, fe = 3.0, mg = 45.0, zn = 3.2, b12 = 1.6, va = 180.0),
        f("Mushroom risotto", "1 plate", 430.0, 11.0, 62.0, 15.0, "Italian", fib = 2.6, sug = 3.0, sat = 7.5, na = 740.0, k = 400.0, ca = 120.0, fe = 1.8, mg = 40.0, zn = 1.6, vd = 0.4, b12 = 0.3),

        // Chinese and Indo-Chinese
        f("Hakka noodles, veg", "1 plate", 340.0, 9.0, 52.0, 11.0, "Chinese", fib = 4.0, sug = 4.0, sat = 2.0, na = 1100.0, k = 280.0, ca = 40.0, fe = 2.2, mg = 40.0, zn = 1.2, vc = 14.0, va = 120.0),
        f("Chicken noodles", "1 plate", 420.0, 22.0, 52.0, 14.0, "Chinese", fib = 3.4, sug = 4.0, sat = 3.0, na = 1250.0, k = 380.0, ca = 42.0, fe = 2.4, mg = 46.0, zn = 1.9, b12 = 0.3, vc = 10.0),
        f("Gobi manchurian", "1 plate", 350.0, 8.0, 40.0, 18.0, "Chinese", fib = 4.0, sug = 8.0, sat = 3.0, na = 1150.0, k = 340.0, ca = 50.0, fe = 1.8, mg = 30.0, zn = 0.8, vc = 40.0),
        f("Chicken manchurian", "1 plate", 400.0, 24.0, 30.0, 20.0, "Chinese", fib = 2.4, sug = 9.0, sat = 3.6, na = 1300.0, k = 400.0, ca = 45.0, fe = 2.0, mg = 36.0, zn = 1.8, b12 = 0.4),
        f("Chilli chicken", "1 plate", 380.0, 26.0, 22.0, 20.0, "Chinese", fib = 2.2, sug = 7.0, sat = 3.6, na = 1250.0, k = 420.0, ca = 40.0, fe = 1.8, mg = 38.0, zn = 2.0, b12 = 0.4, vc = 30.0),
        f("Spring roll, veg", "1 piece", 130.0, 3.0, 16.0, 6.0, "Chinese", fib = 1.6, sug = 1.5, sat = 1.2, na = 340.0, k = 120.0, ca = 20.0, fe = 0.8, mg = 12.0, zn = 0.3, vc = 8.0),
        f("Veg momos", "4 pieces", 220.0, 6.0, 38.0, 4.5, "Chinese", fib = 2.6, sug = 2.0, sat = 1.0, na = 520.0, k = 200.0, ca = 30.0, fe = 1.8, mg = 26.0, zn = 0.8, vc = 10.0),
        f("Chicken momos", "4 pieces", 280.0, 16.0, 36.0, 8.0, "Chinese", fib = 2.0, sug = 2.0, sat = 2.2, na = 640.0, k = 280.0, ca = 32.0, fe = 2.0, mg = 30.0, zn = 1.4, b12 = 0.3),

        // Middle Eastern
        f("Chicken shawarma wrap", "1 wrap", 480.0, 32.0, 44.0, 19.0, "Middle Eastern", fib = 3.6, sug = 4.0, sat = 5.0, na = 1020.0, k = 520.0, ca = 130.0, fe = 3.2, mg = 55.0, zn = 2.6, b12 = 0.6, vc = 10.0),
        f("Falafel", "3 pieces", 250.0, 10.0, 24.0, 13.0, "Middle Eastern", fib = 6.0, sug = 2.0, sat = 1.8, na = 480.0, k = 420.0, ca = 60.0, fe = 3.0, mg = 60.0, zn = 1.6, fol = 150.0),
        f("Hummus", "3 tbsp", 150.0, 5.0, 12.0, 9.0, "Middle Eastern", fib = 4.0, sug = 0.5, sat = 1.4, na = 300.0, k = 200.0, ca = 45.0, fe = 1.8, mg = 40.0, zn = 1.1, fol = 60.0),
        f("Pita bread", "1 piece", 165.0, 5.5, 33.0, 0.8, "Middle Eastern", fib = 1.6, sug = 0.8, sat = 0.2, na = 320.0, k = 70.0, ca = 50.0, fe = 1.6, mg = 16.0, zn = 0.5),
        f("Shish tawook", "150 g", 260.0, 38.0, 3.0, 10.0, "Middle Eastern", fib = 0.4, sug = 1.5, sat = 2.4, na = 620.0, k = 500.0, ca = 50.0, fe = 1.4, mg = 42.0, zn = 2.2, b12 = 0.5),

        // Japanese
        f("Salmon sushi", "2 pieces", 120.0, 7.0, 15.0, 3.5, "Japanese", fib = 0.6, sug = 2.5, sat = 0.8, na = 260.0, k = 110.0, ca = 8.0, fe = 0.4, mg = 14.0, zn = 0.3, vd = 3.0, b12 = 0.9, o3 = 0.6),
        f("Chicken ramen", "1 bowl", 520.0, 28.0, 62.0, 17.0, "Japanese", fib = 4.0, sug = 5.0, sat = 4.5, na = 1800.0, k = 560.0, ca = 70.0, fe = 3.4, mg = 60.0, zn = 2.4, b12 = 0.7, vd = 0.6),
        f("Edamame", "1 cup", 190.0, 17.0, 14.0, 8.0, "Japanese", fib = 8.0, sug = 3.0, sat = 1.0, na = 240.0, k = 680.0, ca = 98.0, fe = 3.5, mg = 100.0, zn = 2.1, vc = 10.0, fol = 480.0, o3 = 0.4),

        // Mexican
        f("Chicken burrito", "1 burrito", 620.0, 32.0, 72.0, 22.0, "Mexican", fib = 8.0, sug = 4.0, sat = 7.0, na = 1250.0, k = 700.0, ca = 200.0, fe = 4.2, mg = 80.0, zn = 3.0, b12 = 0.8, vc = 14.0),
        f("Beef taco", "1 taco", 210.0, 11.0, 17.0, 11.0, "Mexican", fib = 2.4, sug = 1.5, sat = 4.4, na = 400.0, k = 220.0, ca = 90.0, fe = 1.6, mg = 26.0, zn = 2.0, b12 = 0.8),
        f("Guacamole", "3 tbsp", 110.0, 1.4, 6.0, 10.0, "Mexican", fib = 4.5, sug = 0.5, sat = 1.4, na = 180.0, k = 340.0, ca = 12.0, fe = 0.5, mg = 22.0, zn = 0.4, vc = 8.0, fol = 60.0),
        f("Nachos with cheese", "1 plate", 480.0, 13.0, 48.0, 27.0, "Mexican", fib = 4.0, sug = 3.0, sat = 9.0, na = 900.0, k = 280.0, ca = 300.0, fe = 1.8, mg = 60.0, zn = 1.8, va = 130.0, b12 = 0.5),

        // Continental and American
        f("Grilled chicken salad", "1 bowl", 320.0, 34.0, 12.0, 15.0, "Continental", fib = 4.0, sug = 5.0, sat = 3.0, na = 520.0, k = 700.0, ca = 90.0, fe = 2.2, mg = 60.0, zn = 2.0, vc = 30.0, va = 320.0, b12 = 0.4, fol = 90.0),
        f("Caesar salad with chicken", "1 bowl", 400.0, 30.0, 10.0, 26.0, "Continental", fib = 3.0, sug = 3.0, sat = 6.0, na = 880.0, k = 520.0, ca = 180.0, fe = 1.8, mg = 45.0, zn = 2.0, vc = 12.0, va = 260.0, b12 = 0.5),
        f("Sirloin steak", "150 g", 330.0, 40.0, 0.0, 18.0, "Continental", sat = 7.0, na = 110.0, k = 520.0, ca = 20.0, fe = 3.6, mg = 40.0, zn = 7.5, b12 = 3.2),
        f("Cheeseburger", "1 burger", 540.0, 27.0, 42.0, 29.0, "Continental", fib = 2.6, sug = 8.0, sat = 12.0, na = 1050.0, k = 420.0, ca = 240.0, fe = 4.0, mg = 40.0, zn = 4.2, b12 = 2.0),
        f("Club sandwich", "1 sandwich", 480.0, 26.0, 44.0, 22.0, "Continental", fib = 3.4, sug = 6.0, sat = 7.0, na = 1150.0, k = 480.0, ca = 180.0, fe = 3.2, mg = 50.0, zn = 2.4, b12 = 0.8, va = 120.0),
        f("French fries", "1 medium", 340.0, 4.0, 44.0, 17.0, "Continental", fib = 4.0, sug = 0.4, sat = 2.6, na = 340.0, k = 640.0, ca = 20.0, fe = 1.0, mg = 40.0, zn = 0.6, vc = 12.0),
        f("Scrambled eggs", "2 eggs", 200.0, 13.0, 2.0, 15.0, "Continental", sat = 5.0, na = 340.0, k = 190.0, ca = 70.0, fe = 1.6, mg = 16.0, zn = 1.1, va = 180.0, vd = 2.2, b12 = 1.1),
        f("Pancakes", "2 pieces", 350.0, 8.0, 52.0, 12.0, "Continental", fib = 1.6, sug = 18.0, sat = 5.0, na = 620.0, k = 200.0, ca = 200.0, fe = 2.4, mg = 24.0, zn = 0.8, va = 90.0, b12 = 0.3),
        f("Baked beans", "1/2 cup", 130.0, 7.0, 25.0, 0.6, "Continental", fib = 6.0, sug = 10.0, sat = 0.2, na = 520.0, k = 420.0, ca = 60.0, fe = 2.0, mg = 40.0, zn = 1.4, fol = 60.0),
        f("Roast potatoes", "1 cup", 230.0, 4.0, 36.0, 8.0, "Continental", fib = 4.0, sug = 2.0, sat = 1.2, na = 300.0, k = 780.0, ca = 20.0, fe = 1.2, mg = 45.0, zn = 0.7, vc = 20.0)
    )

    private fun breadsAndGrains(): List<FoodItem> = listOf(
        f("White bread", "1 slice", 75.0, 2.5, 14.0, 1.0, "Grains", fib = 0.8, sug = 1.5, sat = 0.2, na = 150.0, k = 35.0, ca = 40.0, fe = 0.9, mg = 7.0, zn = 0.3, fol = 30.0),
        f("Brown bread", "1 slice", 80.0, 3.2, 14.0, 1.2, "Grains", fib = 2.0, sug = 1.5, sat = 0.3, na = 140.0, k = 70.0, ca = 30.0, fe = 1.0, mg = 20.0, zn = 0.5, fol = 20.0),
        f("Multigrain bread", "1 slice", 90.0, 4.0, 15.0, 1.6, "Grains", fib = 2.6, sug = 1.6, sat = 0.3, na = 150.0, k = 90.0, ca = 35.0, fe = 1.2, mg = 28.0, zn = 0.6),
        f("Plain oats, dry", "40 g", 150.0, 5.2, 26.0, 2.7, "Grains", fib = 4.0, sug = 0.4, sat = 0.5, na = 2.0, k = 150.0, ca = 22.0, fe = 1.8, mg = 55.0, zn = 1.2, fol = 12.0),
        f("Muesli", "50 g", 190.0, 5.0, 33.0, 4.5, "Grains", fib = 5.0, sug = 10.0, sat = 1.0, na = 40.0, k = 250.0, ca = 40.0, fe = 2.0, mg = 60.0, zn = 1.4),
        f("Cornflakes", "30 g", 110.0, 2.0, 25.0, 0.2, "Grains", fib = 0.8, sug = 3.0, sat = 0.1, na = 220.0, k = 30.0, ca = 4.0, fe = 4.0, mg = 4.0, zn = 0.5, b12 = 0.6, fol = 100.0),
        f("Quinoa, cooked", "1 cup", 220.0, 8.0, 39.0, 3.5, "Grains", fib = 5.0, sug = 1.6, sat = 0.4, na = 13.0, k = 320.0, ca = 30.0, fe = 2.8, mg = 118.0, zn = 2.0, fol = 78.0),
        f("Couscous, cooked", "1 cup", 175.0, 6.0, 36.0, 0.3, "Grains", fib = 2.2, sat = 0.1, na = 8.0, k = 90.0, ca = 12.0, fe = 0.6, mg = 13.0, zn = 0.4, fol = 24.0)
    )

    private fun vegetables(): List<FoodItem> = listOf(
        f("Spinach, cooked", "1 cup", 41.0, 5.3, 6.8, 0.5, "Vegetables", fib = 4.3, sug = 0.8, sat = 0.1, na = 126.0, k = 839.0, ca = 245.0, fe = 6.4, mg = 157.0, zn = 1.4, vc = 18.0, va = 943.0, fol = 263.0),
        f("Broccoli, steamed", "1 cup", 55.0, 3.7, 11.0, 0.6, "Vegetables", fib = 5.1, sug = 2.2, sat = 0.1, na = 64.0, k = 457.0, ca = 62.0, fe = 1.0, mg = 33.0, zn = 0.7, vc = 101.0, va = 120.0, fol = 168.0),
        f("Carrot, raw", "1 medium", 25.0, 0.6, 6.0, 0.1, "Vegetables", fib = 1.7, sug = 2.9, na = 42.0, k = 195.0, ca = 20.0, fe = 0.2, mg = 7.0, zn = 0.1, vc = 3.6, va = 509.0, fol = 12.0),
        f("Tomato", "1 medium", 22.0, 1.1, 4.8, 0.2, "Vegetables", fib = 1.5, sug = 3.2, na = 6.0, k = 292.0, ca = 12.0, fe = 0.3, mg = 14.0, zn = 0.2, vc = 17.0, va = 51.0, fol = 18.0),
        f("Cucumber", "1 cup sliced", 16.0, 0.7, 3.8, 0.1, "Vegetables", fib = 0.5, sug = 1.7, na = 2.0, k = 152.0, ca = 16.0, fe = 0.3, mg = 13.0, zn = 0.2, vc = 2.8, fol = 7.0),
        f("Beetroot, cooked", "1/2 cup", 37.0, 1.4, 8.5, 0.2, "Vegetables", fib = 1.7, sug = 6.8, na = 65.0, k = 259.0, ca = 14.0, fe = 0.7, mg = 20.0, zn = 0.3, vc = 3.1, fol = 68.0),
        f("Mushroom, sauteed", "1 cup", 44.0, 3.4, 8.0, 0.6, "Vegetables", fib = 2.0, sug = 3.0, na = 240.0, k = 420.0, ca = 9.0, fe = 0.7, mg = 18.0, zn = 0.9, vd = 0.4, b12 = 0.1, fol = 18.0),
        f("Bell pepper", "1 medium", 31.0, 1.0, 7.0, 0.3, "Vegetables", fib = 2.5, sug = 4.2, na = 4.0, k = 251.0, ca = 10.0, fe = 0.4, mg = 12.0, zn = 0.3, vc = 152.0, va = 157.0, fol = 55.0),
        f("Cauliflower, cooked", "1 cup", 29.0, 2.3, 5.1, 0.6, "Vegetables", fib = 2.9, sug = 2.4, na = 19.0, k = 176.0, ca = 20.0, fe = 0.4, mg = 12.0, zn = 0.2, vc = 55.0, fol = 55.0),
        f("Okra, cooked", "1 cup", 36.0, 2.0, 7.0, 0.3, "Vegetables", fib = 3.2, sug = 3.8, na = 10.0, k = 300.0, ca = 120.0, fe = 0.6, mg = 55.0, zn = 0.6, vc = 26.0, va = 70.0, fol = 74.0),
        f("Drumstick, cooked", "1 cup", 42.0, 3.0, 8.0, 0.3, "Vegetables", fib = 3.5, sug = 2.0, na = 45.0, k = 460.0, ca = 55.0, fe = 0.7, mg = 45.0, zn = 0.5, vc = 90.0, va = 45.0, fol = 55.0),
        f("Onion, raw", "1 medium", 44.0, 1.2, 10.0, 0.1, "Vegetables", fib = 1.9, sug = 4.7, na = 4.0, k = 161.0, ca = 23.0, fe = 0.2, mg = 10.0, zn = 0.2, vc = 7.4, fol = 19.0)
    )

    private fun fruit(): List<FoodItem> = listOf(
        f("Banana", "1 medium", 105.0, 1.3, 27.0, 0.4, "Fruit", fib = 3.1, sug = 14.4, sat = 0.1, na = 1.0, k = 422.0, ca = 6.0, fe = 0.3, mg = 32.0, zn = 0.2, vc = 10.3, va = 4.0, fol = 24.0),
        f("Apple", "1 medium", 95.0, 0.5, 25.0, 0.3, "Fruit", fib = 4.4, sug = 19.0, na = 2.0, k = 195.0, ca = 11.0, fe = 0.2, mg = 9.0, zn = 0.1, vc = 8.4, va = 5.0, fol = 5.0),
        f("Orange", "1 medium", 62.0, 1.2, 15.0, 0.2, "Fruit", fib = 3.1, sug = 12.0, na = 0.0, k = 237.0, ca = 52.0, fe = 0.1, mg = 13.0, zn = 0.1, vc = 70.0, va = 14.0, fol = 40.0),
        f("Papaya", "1 cup", 62.0, 0.7, 16.0, 0.4, "Fruit", fib = 2.5, sug = 11.0, na = 12.0, k = 264.0, ca = 30.0, fe = 0.4, mg = 30.0, zn = 0.1, vc = 88.0, va = 68.0, fol = 53.0),
        f("Mango", "1 cup", 99.0, 1.4, 25.0, 0.6, "Fruit", fib = 2.6, sug = 23.0, na = 2.0, k = 277.0, ca = 18.0, fe = 0.3, mg = 17.0, zn = 0.1, vc = 60.0, va = 89.0, fol = 71.0),
        f("Guava", "1 medium", 37.0, 1.4, 8.0, 0.5, "Fruit", fib = 3.0, sug = 5.0, na = 1.0, k = 229.0, ca = 10.0, fe = 0.2, mg = 12.0, zn = 0.1, vc = 126.0, va = 17.0, fol = 27.0),
        f("Watermelon", "1 cup", 46.0, 0.9, 11.5, 0.2, "Fruit", fib = 0.6, sug = 9.4, na = 2.0, k = 170.0, ca = 11.0, fe = 0.4, mg = 15.0, zn = 0.2, vc = 12.3, va = 43.0, fol = 5.0),
        f("Pomegranate", "1/2 cup", 72.0, 1.5, 16.0, 1.0, "Fruit", fib = 3.5, sug = 12.0, na = 3.0, k = 205.0, ca = 9.0, fe = 0.3, mg = 11.0, zn = 0.3, vc = 9.0, fol = 33.0),
        f("Grapes", "1 cup", 104.0, 1.1, 27.0, 0.2, "Fruit", fib = 1.4, sug = 23.0, na = 3.0, k = 288.0, ca = 15.0, fe = 0.5, mg = 11.0, zn = 0.1, vc = 16.0, va = 5.0, fol = 3.0),
        f("Sapota", "1 medium", 140.0, 0.7, 34.0, 1.9, "Fruit", fib = 9.0, sug = 26.0, na = 20.0, k = 328.0, ca = 36.0, fe = 1.1, mg = 20.0, zn = 0.2, vc = 17.0, fol = 18.0),
        f("Dates", "2 pieces", 133.0, 1.2, 36.0, 0.2, "Fruit", fib = 3.2, sug = 32.0, na = 2.0, k = 334.0, ca = 30.0, fe = 0.5, mg = 26.0, zn = 0.2, fol = 10.0)
    )

    private fun nutsAndSnacks(): List<FoodItem> = listOf(
        f("Roasted peanuts", "30 g", 170.0, 7.7, 5.0, 14.5, "Nuts", fib = 2.6, sug = 1.3, sat = 2.0, na = 90.0, k = 200.0, ca = 26.0, fe = 1.4, mg = 50.0, zn = 1.0, fol = 68.0),
        f("Almonds", "10 pieces", 70.0, 2.6, 2.4, 6.1, "Nuts", fib = 1.5, sug = 0.5, sat = 0.5, na = 0.0, k = 96.0, ca = 37.0, fe = 0.5, mg = 39.0, zn = 0.4, fol = 6.0),
        f("Cashews", "10 pieces", 90.0, 3.0, 5.0, 7.2, "Nuts", fib = 0.5, sug = 1.0, sat = 1.3, na = 3.0, k = 105.0, ca = 8.0, fe = 1.0, mg = 45.0, zn = 0.9, fol = 8.0),
        f("Walnuts", "4 halves", 105.0, 2.4, 2.2, 10.5, "Nuts", fib = 1.1, sug = 0.5, sat = 1.0, na = 0.0, k = 66.0, ca = 20.0, fe = 0.5, mg = 25.0, zn = 0.5, fol = 12.0, o3 = 1.3),
        f("Pumpkin seeds", "20 g", 110.0, 6.0, 3.0, 9.0, "Nuts", fib = 1.2, sat = 1.7, na = 4.0, k = 160.0, ca = 10.0, fe = 1.8, mg = 118.0, zn = 1.5, fol = 12.0),
        f("Flax seeds, ground", "1 tbsp", 55.0, 1.9, 3.0, 4.3, "Nuts", fib = 2.8, sat = 0.4, na = 3.0, k = 84.0, ca = 26.0, fe = 0.6, mg = 40.0, zn = 0.4, fol = 9.0, o3 = 2.4),
        f("Chia seeds", "1 tbsp", 60.0, 2.0, 5.0, 3.8, "Nuts", fib = 4.1, sat = 0.4, na = 2.0, k = 50.0, ca = 76.0, fe = 0.9, mg = 40.0, zn = 0.5, o3 = 2.5),
        f("Murukku", "1 piece", 90.0, 1.5, 9.0, 5.5, "Snacks", fib = 0.8, sat = 2.4, na = 180.0, k = 45.0, ca = 10.0, fe = 0.5, mg = 12.0, zn = 0.3),
        f("Mixture", "30 g", 160.0, 4.0, 15.0, 9.5, "Snacks", fib = 2.0, sug = 1.0, sat = 3.4, na = 380.0, k = 140.0, ca = 20.0, fe = 1.2, mg = 30.0, zn = 0.6),
        f("Banana chips", "30 g", 160.0, 0.7, 17.0, 10.5, "Snacks", fib = 2.0, sug = 5.0, sat = 9.0, na = 60.0, k = 160.0, ca = 4.0, fe = 0.3, mg = 22.0, zn = 0.2),
        f("Marie biscuit", "2 pieces", 60.0, 1.0, 10.0, 1.8, "Snacks", fib = 0.3, sug = 3.0, sat = 0.9, na = 60.0, k = 20.0, ca = 8.0, fe = 0.4, mg = 4.0, zn = 0.1),
        f("Dark chocolate 70%", "20 g", 120.0, 1.6, 9.0, 8.6, "Snacks", fib = 2.2, sug = 6.0, sat = 5.0, na = 4.0, k = 145.0, ca = 15.0, fe = 2.4, mg = 46.0, zn = 0.6)
    )

    private fun drinks(): List<FoodItem> = listOf(
        f("Filter coffee with sugar", "150 ml", 90.0, 3.0, 12.0, 3.0, "Drinks", sug = 10.0, sat = 1.9, na = 45.0, k = 160.0, ca = 110.0, fe = 0.1, mg = 14.0, zn = 0.4, b12 = 0.4, va = 30.0),
        f("Black coffee", "1 cup", 5.0, 0.3, 0.0, 0.0, "Drinks", na = 5.0, k = 116.0, ca = 5.0, mg = 7.0),
        f("Tea with milk and sugar", "150 ml", 85.0, 2.4, 12.0, 2.8, "Drinks", sug = 10.0, sat = 1.7, na = 40.0, k = 120.0, ca = 90.0, fe = 0.1, mg = 12.0, zn = 0.3, b12 = 0.3),
        f("Green tea", "1 cup", 2.0, 0.0, 0.0, 0.0, "Drinks", na = 2.0, k = 20.0, mg = 2.0),
        f("Tender coconut water", "1 glass (250 ml)", 46.0, 1.7, 9.0, 0.5, "Drinks", fib = 2.6, sug = 6.3, na = 252.0, k = 600.0, ca = 58.0, fe = 0.7, mg = 60.0, zn = 0.3, vc = 5.8, fol = 7.0),
        f("Fresh lime soda, sweet", "1 glass", 110.0, 0.2, 28.0, 0.0, "Drinks", sug = 27.0, na = 30.0, k = 40.0, ca = 8.0, mg = 4.0, vc = 12.0),
        f("Orange juice", "1 glass (250 ml)", 112.0, 1.7, 26.0, 0.5, "Drinks", fib = 0.5, sug = 21.0, na = 2.0, k = 496.0, ca = 27.0, fe = 0.5, mg = 27.0, zn = 0.1, vc = 124.0, fol = 74.0),
        f("Cola", "330 ml", 139.0, 0.0, 35.0, 0.0, "Drinks", sug = 35.0, na = 15.0, k = 4.0),
        // Beer's calories are mostly ethanol at 7 kcal/g, which is not one of the four
        // macros this app tracks, so its kcal cannot be derived from them. SeedDataTest
        // exempts it by name for exactly this reason.
        f("Beer", "330 ml", 143.0, 1.6, 11.0, 0.0, "Drinks", sug = 0.0, na = 14.0, k = 96.0, ca = 14.0, mg = 21.0, zn = 0.1, fol = 21.0)
    )

    private fun fats(): List<FoodItem> = listOf(
        f("Coconut oil", "1 tsp", 40.0, 0.0, 0.0, 4.5, "Fats", sat = 3.9),
        f("Gingelly oil", "1 tsp", 40.0, 0.0, 0.0, 4.5, "Fats", sat = 0.6),
        f("Sunflower oil", "1 tsp", 40.0, 0.0, 0.0, 4.5, "Fats", sat = 0.5),
        f("Olive oil", "1 tsp", 40.0, 0.0, 0.0, 4.5, "Fats", sat = 0.6),
        f("Ghee", "1 tsp", 45.0, 0.0, 0.0, 5.0, "Fats", sat = 3.1, va = 45.0),
        f("Butter", "1 tsp", 36.0, 0.0, 0.0, 4.1, "Fats", sat = 2.6, na = 32.0, va = 35.0, vd = 0.1)
    )

    // ------------------------------------------------- the second helping
    //
    // Everything below was added after the first round of real use, when the
    // catalogue turned out to be thin exactly where this diet is thick. Same rules
    // as above: per-unit values, home-style portions, estimates that you should
    // correct for the handful of things you actually eat every week.

    /** Dosa keeps going. Fifteen was not all of them. */
    private fun moreDosa(): List<FoodItem> = listOf(
        f("Mysore masala dosa", "1 dosa", 320.0, 6.0, 45.0, 12.0, "Dosa", fib = 3.4, sat = 5.0, na = 520.0, k = 350.0, ca = 30.0, fe = 1.6, mg = 36.0, zn = 0.8, vc = 9.0),
        f("Butter masala dosa", "1 dosa", 340.0, 5.8, 44.0, 15.0, "Dosa", fib = 3.2, sat = 7.5, na = 540.0, k = 340.0, ca = 32.0, fe = 1.5, mg = 34.0, zn = 0.7, va = 90.0),
        f("Kal dosa", "1 dosa", 110.0, 2.4, 19.0, 2.6, "Dosa", fib = 1.0, sat = 1.2, na = 180.0, k = 55.0, ca = 11.0, fe = 0.7, mg = 18.0, zn = 0.4),
        f("Karam dosa", "1 dosa", 185.0, 4.0, 26.0, 7.5, "Dosa", fib = 2.2, sat = 2.6, na = 430.0, k = 120.0, ca = 26.0, fe = 1.3, mg = 28.0, zn = 0.6),
        f("Egg dosa", "1 dosa", 215.0, 9.5, 23.0, 9.5, "Dosa", fib = 1.2, sat = 3.0, na = 320.0, k = 130.0, ca = 34.0, fe = 1.4, mg = 24.0, zn = 0.8, va = 85.0, vd = 1.1, b12 = 0.6),
        f("Kambu dosai", "1 dosa", 135.0, 3.6, 22.0, 3.2, "Dosa", fib = 2.6, sat = 1.3, na = 200.0, k = 130.0, ca = 20.0, fe = 2.2, mg = 60.0, zn = 0.8),
        f("Thinai dosai", "1 dosa", 130.0, 3.4, 22.0, 3.0, "Dosa", fib = 2.4, sat = 1.2, na = 195.0, k = 110.0, ca = 18.0, fe = 1.6, mg = 44.0, zn = 0.7),
        f("Samai dosai", "1 dosa", 128.0, 3.2, 22.0, 2.9, "Dosa", fib = 2.5, sat = 1.2, na = 190.0, k = 105.0, ca = 15.0, fe = 1.4, mg = 40.0, zn = 0.7),
        f("Vella dosai", "1 dosa", 195.0, 3.0, 34.0, 5.0, "Dosa", fib = 1.4, sug = 14.0, sat = 2.6, na = 120.0, k = 90.0, ca = 20.0, fe = 1.2, mg = 22.0, zn = 0.4)
    )

    private fun moreTiffin(): List<FoodItem> = listOf(
        f("Kuzhi paniyaram", "3 pieces", 165.0, 4.0, 22.0, 6.5, "Tiffin", fib = 1.8, sat = 2.4, na = 280.0, k = 110.0, ca = 20.0, fe = 1.1, mg = 26.0, zn = 0.6),
        f("Sweet paniyaram", "3 pieces", 210.0, 3.2, 32.0, 7.5, "Tiffin", fib = 1.4, sug = 15.0, sat = 3.4, na = 90.0, k = 120.0, ca = 22.0, fe = 1.2, mg = 24.0, zn = 0.5),
        f("Rava kesari", "1 cup", 340.0, 5.0, 52.0, 12.0, "Tiffin", fib = 1.2, sug = 30.0, sat = 7.0, na = 120.0, k = 90.0, ca = 26.0, fe = 1.4, mg = 20.0, zn = 0.6, va = 80.0),
        f("Poha", "1 cup", 210.0, 4.2, 38.0, 5.0, "Tiffin", fib = 2.2, sug = 2.0, sat = 1.8, na = 400.0, k = 170.0, ca = 20.0, fe = 2.8, mg = 28.0, zn = 0.7, vc = 6.0),
        f("Arisi upma", "1 cup", 245.0, 5.0, 40.0, 7.0, "Tiffin", fib = 2.0, sat = 2.8, na = 450.0, k = 130.0, ca = 22.0, fe = 1.5, mg = 26.0, zn = 0.7),
        f("Godhumai upma", "1 cup", 235.0, 6.5, 37.0, 6.5, "Tiffin", fib = 3.6, sat = 2.6, na = 440.0, k = 150.0, ca = 24.0, fe = 1.8, mg = 42.0, zn = 0.9),
        f("Ragi mudde", "1 ball", 220.0, 5.0, 44.0, 1.6, "Tiffin", fib = 4.6, sat = 0.5, na = 60.0, k = 180.0, ca = 210.0, fe = 2.6, mg = 82.0, zn = 1.0),
        f("Ragi kanji", "1 glass", 150.0, 4.0, 30.0, 1.4, "Tiffin", fib = 3.2, sug = 4.0, sat = 0.4, na = 220.0, k = 160.0, ca = 180.0, fe = 2.0, mg = 66.0, zn = 0.8),
        f("Kambu koozh", "1 glass", 175.0, 5.0, 32.0, 2.6, "Tiffin", fib = 3.4, sat = 0.6, na = 320.0, k = 200.0, ca = 40.0, fe = 3.0, mg = 90.0, zn = 1.2),
        f("Lemon sevai", "1 cup", 230.0, 4.0, 42.0, 5.5, "Tiffin", fib = 1.6, sat = 2.2, na = 380.0, k = 100.0, ca = 18.0, fe = 1.2, mg = 22.0, zn = 0.5, vc = 8.0),
        f("Coconut sevai", "1 cup", 265.0, 4.2, 40.0, 9.5, "Tiffin", fib = 2.4, sat = 6.5, na = 340.0, k = 120.0, ca = 20.0, fe = 1.3, mg = 26.0, zn = 0.6),
        f("Tamarind sevai", "1 cup", 255.0, 4.0, 43.0, 7.5, "Tiffin", fib = 2.2, sug = 4.0, sat = 2.6, na = 480.0, k = 190.0, ca = 26.0, fe = 1.6, mg = 28.0, zn = 0.6),
        f("Ammini kozhukattai", "1 cup", 190.0, 4.0, 34.0, 4.5, "Tiffin", fib = 1.8, sat = 2.6, na = 300.0, k = 90.0, ca = 16.0, fe = 1.0, mg = 22.0, zn = 0.5),
        f("Sweet kozhukattai", "2 pieces", 240.0, 3.6, 40.0, 7.5, "Tiffin", fib = 2.4, sug = 18.0, sat = 5.5, na = 60.0, k = 130.0, ca = 20.0, fe = 1.2, mg = 28.0, zn = 0.6),
        f("Bonda", "1 piece", 145.0, 3.0, 17.0, 7.0, "Tiffin", fib = 1.6, sat = 2.4, na = 280.0, k = 160.0, ca = 18.0, fe = 1.0, mg = 22.0, zn = 0.5, vc = 5.0),
        f("Onion bajji", "1 piece", 105.0, 2.4, 11.0, 5.5, "Tiffin", fib = 1.4, sug = 1.5, sat = 1.8, na = 230.0, k = 110.0, ca = 14.0, fe = 0.9, mg = 18.0, zn = 0.4, vc = 3.0),
        f("Potato bajji", "1 piece", 120.0, 2.4, 14.0, 6.0, "Tiffin", fib = 1.4, sat = 2.0, na = 240.0, k = 180.0, ca = 12.0, fe = 0.9, mg = 18.0, zn = 0.4, vc = 6.0),
        f("Plantain bajji", "1 piece", 115.0, 2.2, 14.0, 5.6, "Tiffin", fib = 1.6, sug = 2.0, sat = 1.9, na = 230.0, k = 160.0, ca = 12.0, fe = 0.8, mg = 18.0, zn = 0.4, vc = 4.0),
        f("Thattai", "2 pieces", 130.0, 2.4, 14.0, 7.0, "Snacks", fib = 1.2, sat = 2.6, na = 260.0, k = 70.0, ca = 14.0, fe = 0.8, mg = 20.0, zn = 0.4),
        f("Ribbon pakoda", "30 g", 160.0, 3.4, 16.0, 9.0, "Snacks", fib = 1.6, sat = 3.2, na = 320.0, k = 100.0, ca = 18.0, fe = 1.0, mg = 26.0, zn = 0.5),
        f("Seedai", "5 pieces", 140.0, 2.2, 16.0, 7.5, "Snacks", fib = 1.0, sat = 3.4, na = 240.0, k = 60.0, ca = 12.0, fe = 0.7, mg = 16.0, zn = 0.3),
        f("Vada curry", "1 cup", 280.0, 10.0, 28.0, 14.0, "Tiffin", fib = 5.0, sat = 4.4, na = 620.0, k = 340.0, ca = 45.0, fe = 2.4, mg = 50.0, zn = 1.3, vc = 6.0, fol = 90.0),
        f("Idli podi, dry", "1 tbsp", 60.0, 2.4, 6.0, 3.0, "Sides", fib = 1.8, sat = 0.6, na = 300.0, k = 100.0, ca = 24.0, fe = 1.1, mg = 28.0, zn = 0.5),
        f("Aappam with coconut milk", "1 aappam", 190.0, 3.0, 28.0, 7.5, "Tiffin", fib = 1.2, sug = 5.0, sat = 6.0, na = 140.0, k = 110.0, ca = 16.0, fe = 0.8, mg = 20.0, zn = 0.4),
        f("Idiyappam with coconut milk", "2 pieces", 230.0, 4.0, 38.0, 7.0, "Tiffin", fib = 1.6, sug = 3.0, sat = 5.5, na = 90.0, k = 120.0, ca = 18.0, fe = 0.8, mg = 24.0, zn = 0.5)
    )

    private fun moreRice(): List<FoodItem> = listOf(
        f("Kuska", "1 plate", 380.0, 8.0, 58.0, 13.0, "Rice", fib = 2.4, sug = 2.0, sat = 5.5, na = 760.0, k = 220.0, ca = 40.0, fe = 2.2, mg = 38.0, zn = 1.1),
        f("Mutton kuska", "1 plate", 520.0, 22.0, 58.0, 22.0, "Rice", fib = 2.6, sug = 2.5, sat = 9.0, na = 880.0, k = 420.0, ca = 46.0, fe = 3.6, mg = 48.0, zn = 3.6, b12 = 1.8),
        f("Thakkali sadam", "1 cup", 265.0, 5.0, 44.0, 7.5, "Rice", fib = 2.0, sug = 4.0, sat = 2.6, na = 440.0, k = 260.0, ca = 26.0, fe = 1.8, mg = 30.0, zn = 0.9, vc = 14.0, va = 60.0),
        f("Ellu sadam", "1 cup", 300.0, 6.5, 44.0, 11.0, "Rice", fib = 2.6, sat = 2.0, na = 420.0, k = 170.0, ca = 130.0, fe = 2.6, mg = 55.0, zn = 1.4),
        f("Milagu sadam", "1 cup", 260.0, 5.0, 44.0, 7.0, "Rice", fib = 1.4, sat = 3.4, na = 400.0, k = 110.0, ca = 26.0, fe = 1.8, mg = 26.0, zn = 0.9),
        f("Mango rice", "1 cup", 275.0, 5.0, 46.0, 8.0, "Rice", fib = 2.2, sug = 5.0, sat = 2.8, na = 430.0, k = 200.0, ca = 26.0, fe = 1.7, mg = 30.0, zn = 0.9, vc = 12.0),
        f("Vangi bath", "1 cup", 290.0, 6.0, 46.0, 9.0, "Rice", fib = 3.4, sug = 3.0, sat = 3.0, na = 520.0, k = 280.0, ca = 32.0, fe = 1.9, mg = 34.0, zn = 1.0, vc = 6.0),
        f("Chitranna", "1 cup", 270.0, 5.0, 45.0, 7.5, "Rice", fib = 2.0, sat = 2.6, na = 430.0, k = 140.0, ca = 24.0, fe = 1.6, mg = 28.0, zn = 0.8, vc = 8.0),
        f("Coconut milk rice", "1 cup", 320.0, 5.4, 46.0, 12.5, "Rice", fib = 1.8, sug = 3.0, sat = 9.0, na = 360.0, k = 160.0, ca = 22.0, fe = 1.7, mg = 30.0, zn = 0.9),
        f("Keerai sadam", "1 cup", 240.0, 7.0, 40.0, 6.0, "Rice", fib = 3.4, sat = 2.4, na = 400.0, k = 420.0, ca = 120.0, fe = 3.4, mg = 70.0, zn = 1.1, vc = 14.0, va = 380.0, fol = 110.0)
    )

    private fun moreSides(): List<FoodItem> = listOf(
        f("Poricha kuzhambu", "1 cup", 150.0, 6.0, 18.0, 6.0, "Sides", fib = 4.6, sat = 3.4, na = 520.0, k = 340.0, ca = 45.0, fe = 1.9, mg = 44.0, zn = 0.9, fol = 80.0),
        f("Ennai kathirikai", "1/2 cup", 175.0, 2.6, 13.0, 12.5, "Sides", fib = 3.6, sug = 3.0, sat = 2.2, na = 420.0, k = 300.0, ca = 30.0, fe = 1.2, mg = 28.0, zn = 0.5),
        f("Kadalai curry", "1 cup", 245.0, 11.0, 34.0, 7.5, "Sides", fib = 9.0, sug = 4.0, sat = 3.4, na = 580.0, k = 480.0, ca = 70.0, fe = 3.0, mg = 72.0, zn = 1.8, vc = 8.0, fol = 160.0),
        f("Pattani sundal", "1/2 cup", 145.0, 7.0, 20.0, 4.0, "Sides", fib = 6.0, sug = 2.0, sat = 2.6, na = 260.0, k = 300.0, ca = 40.0, fe = 1.8, mg = 44.0, zn = 1.1, vc = 6.0, fol = 100.0),
        f("Mochai kuzhambu", "1 cup", 200.0, 9.0, 26.0, 6.5, "Sides", fib = 7.0, sat = 3.0, na = 560.0, k = 420.0, ca = 50.0, fe = 2.4, mg = 60.0, zn = 1.3, fol = 130.0),
        f("Urulaikizhangu roast", "1/2 cup", 165.0, 2.4, 22.0, 7.5, "Sides", fib = 2.4, sat = 2.6, na = 300.0, k = 420.0, ca = 12.0, fe = 0.9, mg = 26.0, zn = 0.4, vc = 14.0),
        f("Kathirikai roast", "1/2 cup", 130.0, 1.8, 12.0, 8.5, "Sides", fib = 3.2, sug = 3.0, sat = 1.6, na = 280.0, k = 250.0, ca = 20.0, fe = 0.8, mg = 22.0, zn = 0.3),
        f("Vazhakkai poriyal", "1/2 cup", 120.0, 1.8, 18.0, 4.5, "Sides", fib = 3.0, sug = 2.0, sat = 3.0, na = 220.0, k = 320.0, ca = 14.0, fe = 0.7, mg = 26.0, zn = 0.3, vc = 10.0),
        f("Vendakkai poriyal", "1/2 cup", 100.0, 2.2, 10.0, 5.5, "Sides", fib = 3.4, sug = 2.0, sat = 3.0, na = 210.0, k = 260.0, ca = 90.0, fe = 0.7, mg = 44.0, zn = 0.5, vc = 18.0, fol = 60.0),
        f("Chow chow kootu", "1/2 cup", 110.0, 4.0, 13.0, 4.8, "Sides", fib = 3.6, sat = 3.0, na = 280.0, k = 240.0, ca = 36.0, fe = 1.2, mg = 30.0, zn = 0.6, vc = 8.0, fol = 60.0),
        f("Poosanikai kootu", "1/2 cup", 105.0, 3.8, 12.0, 4.6, "Sides", fib = 3.2, sat = 2.8, na = 270.0, k = 220.0, ca = 34.0, fe = 1.1, mg = 28.0, zn = 0.5, vc = 6.0),
        f("Murungai keerai poriyal", "1/2 cup", 95.0, 4.4, 8.0, 5.0, "Sides", fib = 3.4, sat = 2.6, na = 240.0, k = 400.0, ca = 180.0, fe = 2.6, mg = 60.0, zn = 0.6, vc = 40.0, va = 380.0, fol = 90.0),
        f("Arai keerai masiyal", "1/2 cup", 85.0, 4.0, 7.0, 4.4, "Sides", fib = 3.2, sat = 2.4, na = 250.0, k = 460.0, ca = 150.0, fe = 3.2, mg = 68.0, zn = 0.6, vc = 20.0, va = 440.0, fol = 130.0),
        f("Thayir pachadi", "1/2 cup", 90.0, 3.4, 7.0, 5.0, "Sides", fib = 1.0, sug = 4.0, sat = 3.0, na = 240.0, k = 200.0, ca = 120.0, fe = 0.4, mg = 20.0, zn = 0.6, b12 = 0.4),
        f("Vegetable stew", "1 cup", 190.0, 4.0, 16.0, 12.5, "Sides", fib = 3.4, sug = 4.0, sat = 9.0, na = 420.0, k = 380.0, ca = 45.0, fe = 1.4, mg = 34.0, zn = 0.6, vc = 16.0, va = 180.0),
        f("Paruppu usili", "1/2 cup", 175.0, 8.5, 16.0, 8.0, "Sides", fib = 5.4, sat = 3.4, na = 300.0, k = 330.0, ca = 50.0, fe = 2.2, mg = 52.0, zn = 1.2, fol = 120.0),
        f("Curd chilli", "3 pieces", 70.0, 1.0, 9.0, 3.4, "Sides", fib = 1.0, sat = 1.4, na = 480.0, k = 80.0, ca = 14.0, fe = 0.5, mg = 12.0, zn = 0.2),
        f("Garlic chutney", "2 tbsp", 70.0, 1.6, 7.0, 4.0, "Sides", fib = 1.2, sug = 1.0, sat = 0.8, na = 260.0, k = 130.0, ca = 26.0, fe = 0.8, mg = 12.0, zn = 0.3, vc = 5.0)
    )

    private fun moreNonVeg(): List<FoodItem> = listOf(
        f("Chicken chukka", "1 cup", 300.0, 30.0, 6.0, 17.0, "Protein", fib = 1.6, sat = 5.5, na = 720.0, k = 440.0, ca = 34.0, fe = 2.2, mg = 46.0, zn = 2.4, b12 = 0.5),
        f("Chicken varuval", "1 cup", 320.0, 29.0, 8.0, 19.0, "Protein", fib = 1.8, sat = 6.0, na = 780.0, k = 430.0, ca = 36.0, fe = 2.3, mg = 44.0, zn = 2.4, b12 = 0.5),
        f("Nattu kozhi kuzhambu", "1 cup", 270.0, 28.0, 7.0, 14.0, "Protein", fib = 2.0, sat = 4.6, na = 700.0, k = 470.0, ca = 40.0, fe = 2.8, mg = 48.0, zn = 2.6, b12 = 0.6),
        f("Kozhi rasam", "1 cup", 95.0, 9.0, 5.0, 4.5, "Protein", fib = 1.0, sat = 1.4, na = 620.0, k = 280.0, ca = 22.0, fe = 1.2, mg = 24.0, zn = 0.8, b12 = 0.3),
        f("Mutton pepper fry", "1 cup", 360.0, 30.0, 5.0, 25.0, "Protein", fib = 1.4, sat = 10.0, na = 720.0, k = 420.0, ca = 32.0, fe = 3.8, mg = 42.0, zn = 5.2, b12 = 2.6),
        f("Mutton kola urundai", "3 pieces", 260.0, 20.0, 8.0, 17.0, "Protein", fib = 1.6, sat = 6.5, na = 640.0, k = 300.0, ca = 30.0, fe = 2.8, mg = 32.0, zn = 3.6, b12 = 1.8),
        f("Attu kal paya", "1 cup", 210.0, 18.0, 6.0, 13.0, "Protein", fib = 1.2, sat = 5.0, na = 700.0, k = 260.0, ca = 60.0, fe = 2.2, mg = 26.0, zn = 2.6, b12 = 1.4),
        f("Mutton liver fry", "100 g", 220.0, 26.0, 5.0, 10.0, "Protein", fib = 0.6, sat = 3.4, na = 420.0, k = 350.0, ca = 12.0, fe = 9.0, mg = 26.0, zn = 5.0, va = 7000.0, b12 = 60.0, fol = 280.0),
        f("Sankara fish fry", "1 piece (100 g)", 190.0, 21.0, 5.0, 9.5, "Protein", fib = 0.4, sat = 2.2, na = 480.0, k = 380.0, ca = 40.0, fe = 1.2, mg = 38.0, zn = 0.8, vd = 4.0, b12 = 1.8, o3 = 0.5),
        f("Karuvadu kuzhambu", "1 cup", 200.0, 20.0, 8.0, 10.0, "Protein", fib = 1.8, sat = 2.6, na = 1400.0, k = 380.0, ca = 200.0, fe = 2.6, mg = 48.0, zn = 1.4, vd = 3.0, b12 = 3.0, o3 = 0.6),
        f("Eral thokku", "1 cup", 215.0, 25.0, 7.0, 10.0, "Protein", fib = 1.6, sat = 2.6, na = 780.0, k = 300.0, ca = 95.0, fe = 1.8, mg = 52.0, zn = 1.9, b12 = 1.4, o3 = 0.3),
        f("Nandu rasam", "1 cup", 110.0, 12.0, 5.0, 4.5, "Protein", fib = 1.0, sat = 1.2, na = 700.0, k = 260.0, ca = 70.0, fe = 1.0, mg = 34.0, zn = 2.4, b12 = 3.4, o3 = 0.2),
        f("Kaadai fry", "1 bird", 230.0, 25.0, 3.0, 13.0, "Protein", fib = 0.4, sat = 3.6, na = 480.0, k = 300.0, ca = 20.0, fe = 4.0, mg = 30.0, zn = 2.6, b12 = 0.5),
        f("Meen varuval", "1 piece (100 g)", 200.0, 22.0, 4.0, 10.5, "Protein", fib = 0.4, sat = 2.4, na = 500.0, k = 390.0, ca = 34.0, fe = 1.2, mg = 40.0, zn = 0.8, vd = 4.5, b12 = 2.0, o3 = 0.6),
        f("Vanjaram kuzhambu", "1 cup", 230.0, 22.0, 8.0, 12.0, "Protein", fib = 1.8, sat = 3.0, na = 700.0, k = 430.0, ca = 55.0, fe = 1.8, mg = 46.0, zn = 1.0, vd = 5.0, b12 = 2.2, o3 = 0.8),
        f("Sura puttu", "1 cup", 190.0, 24.0, 4.0, 9.0, "Protein", fib = 1.4, sat = 4.0, na = 560.0, k = 340.0, ca = 60.0, fe = 1.6, mg = 42.0, zn = 1.0, b12 = 2.0, o3 = 0.4),
        f("Chicken lollipop", "3 pieces", 290.0, 22.0, 12.0, 17.0, "Protein", fib = 0.8, sug = 3.0, sat = 4.4, na = 820.0, k = 300.0, ca = 28.0, fe = 1.6, mg = 30.0, zn = 1.8, b12 = 0.4),
        f("Chicken drumstick fry", "1 piece", 190.0, 20.0, 4.0, 10.0, "Protein", fib = 0.4, sat = 3.0, na = 480.0, k = 260.0, ca = 20.0, fe = 1.2, mg = 24.0, zn = 1.8, b12 = 0.4),
        f("Fish head curry", "1 cup", 230.0, 20.0, 9.0, 13.0, "Protein", fib = 2.0, sat = 4.0, na = 740.0, k = 420.0, ca = 90.0, fe = 2.0, mg = 44.0, zn = 1.2, vd = 3.0, b12 = 2.2, o3 = 0.7),
        f("Chicken keema", "1 cup", 280.0, 26.0, 7.0, 16.0, "Protein", fib = 1.6, sat = 5.0, na = 680.0, k = 420.0, ca = 32.0, fe = 2.0, mg = 40.0, zn = 2.2, b12 = 0.5)
    )

    private fun northIndian(): List<FoodItem> = listOf(
        f("Dal makhani", "1 cup", 330.0, 12.0, 32.0, 17.0, "North Indian", fib = 9.0, sug = 3.0, sat = 9.0, na = 640.0, k = 480.0, ca = 90.0, fe = 3.4, mg = 70.0, zn = 1.8, va = 130.0, fol = 150.0),
        f("Palak paneer", "1 cup", 300.0, 14.0, 12.0, 22.0, "North Indian", fib = 4.0, sug = 4.0, sat = 11.0, na = 620.0, k = 620.0, ca = 420.0, fe = 4.0, mg = 100.0, zn = 1.8, vc = 20.0, va = 700.0, fol = 180.0),
        f("Paneer butter masala", "1 cup", 380.0, 15.0, 16.0, 28.0, "North Indian", fib = 2.6, sug = 8.0, sat = 15.0, na = 780.0, k = 420.0, ca = 380.0, fe = 1.8, mg = 44.0, zn = 1.6, va = 280.0, b12 = 0.6),
        f("Shahi paneer", "1 cup", 390.0, 15.0, 17.0, 29.0, "North Indian", fib = 2.4, sug = 9.0, sat = 15.0, na = 760.0, k = 400.0, ca = 370.0, fe = 1.8, mg = 46.0, zn = 1.6, va = 260.0),
        f("Matar paneer", "1 cup", 320.0, 15.0, 18.0, 21.0, "North Indian", fib = 5.0, sug = 5.0, sat = 11.0, na = 680.0, k = 430.0, ca = 340.0, fe = 2.2, mg = 56.0, zn = 1.7, vc = 12.0, va = 180.0, fol = 80.0),
        f("Malai kofta", "1 cup", 420.0, 12.0, 26.0, 30.0, "North Indian", fib = 3.4, sug = 8.0, sat = 13.0, na = 720.0, k = 420.0, ca = 260.0, fe = 2.0, mg = 50.0, zn = 1.4, va = 220.0),
        f("Aloo gobi", "1 cup", 190.0, 4.5, 24.0, 9.0, "North Indian", fib = 5.0, sug = 4.0, sat = 1.6, na = 520.0, k = 560.0, ca = 45.0, fe = 1.4, mg = 40.0, zn = 0.6, vc = 50.0, va = 60.0, fol = 70.0),
        f("Bhindi masala", "1 cup", 165.0, 3.4, 15.0, 10.0, "North Indian", fib = 4.6, sug = 4.0, sat = 1.6, na = 440.0, k = 420.0, ca = 120.0, fe = 1.0, mg = 60.0, zn = 0.7, vc = 24.0, va = 90.0, fol = 80.0),
        f("Baingan bharta", "1 cup", 180.0, 3.4, 16.0, 11.5, "North Indian", fib = 5.4, sug = 6.0, sat = 1.8, na = 480.0, k = 460.0, ca = 40.0, fe = 1.2, mg = 38.0, zn = 0.5, vc = 14.0, va = 70.0),
        f("Dal tadka", "1 cup", 220.0, 11.0, 30.0, 6.5, "North Indian", fib = 8.0, sug = 2.0, sat = 2.4, na = 560.0, k = 420.0, ca = 45.0, fe = 2.6, mg = 60.0, zn = 1.4, fol = 140.0),
        f("Kadhi", "1 cup", 190.0, 7.0, 16.0, 11.0, "North Indian", fib = 2.0, sug = 5.0, sat = 4.4, na = 620.0, k = 320.0, ca = 180.0, fe = 1.4, mg = 40.0, zn = 0.9, b12 = 0.4),
        f("Jeera aloo", "1 cup", 200.0, 3.4, 30.0, 8.0, "North Indian", fib = 3.4, sug = 2.0, sat = 1.4, na = 420.0, k = 620.0, ca = 20.0, fe = 1.2, mg = 40.0, zn = 0.5, vc = 20.0),
        f("Rajma masala", "1 cup", 275.0, 14.0, 42.0, 5.5, "North Indian", fib = 11.5, sug = 4.0, sat = 1.2, na = 620.0, k = 640.0, ca = 75.0, fe = 3.8, mg = 86.0, zn = 1.9, vc = 6.0, fol = 210.0),
        f("Chole", "1 cup", 285.0, 12.0, 40.0, 8.5, "North Indian", fib = 10.5, sug = 5.0, sat = 1.6, na = 680.0, k = 540.0, ca = 85.0, fe = 3.4, mg = 80.0, zn = 2.1, vc = 10.0, fol = 190.0)
    )

    private fun sweets(): List<FoodItem> = listOf(
        f("Mysore pak", "1 piece", 190.0, 2.0, 20.0, 11.5, "Sweets", fib = 0.8, sug = 16.0, sat = 7.0, na = 20.0, k = 60.0, ca = 14.0, fe = 0.8, mg = 20.0, zn = 0.3, va = 60.0),
        f("Jangiri", "1 piece", 160.0, 1.8, 26.0, 6.0, "Sweets", fib = 0.8, sug = 20.0, sat = 2.6, na = 15.0, k = 50.0, ca = 12.0, fe = 0.7, mg = 16.0, zn = 0.3),
        f("Adhirasam", "1 piece", 180.0, 1.6, 30.0, 6.0, "Sweets", fib = 0.8, sug = 18.0, sat = 2.8, na = 20.0, k = 70.0, ca = 14.0, fe = 0.8, mg = 18.0, zn = 0.3),
        f("Rava laddu", "1 piece", 165.0, 2.4, 24.0, 7.0, "Sweets", fib = 0.8, sug = 16.0, sat = 4.0, na = 15.0, k = 60.0, ca = 20.0, fe = 0.7, mg = 14.0, zn = 0.3, va = 40.0),
        f("Coconut burfi", "1 piece", 175.0, 1.8, 20.0, 10.0, "Sweets", fib = 1.6, sug = 17.0, sat = 8.0, na = 20.0, k = 80.0, ca = 16.0, fe = 0.7, mg = 18.0, zn = 0.3),
        f("Tirunelveli halwa", "1 piece", 210.0, 1.4, 30.0, 10.0, "Sweets", fib = 0.4, sug = 26.0, sat = 6.0, na = 30.0, k = 50.0, ca = 12.0, fe = 0.6, mg = 10.0, zn = 0.2, va = 50.0),
        f("Palkova", "2 tbsp", 150.0, 4.0, 20.0, 6.0, "Sweets", sug = 19.0, sat = 3.8, na = 45.0, k = 160.0, ca = 150.0, fe = 0.1, mg = 16.0, zn = 0.5, b12 = 0.4, va = 60.0),
        f("Boli", "1 piece", 230.0, 4.0, 38.0, 7.0, "Sweets", fib = 1.6, sug = 20.0, sat = 3.6, na = 60.0, k = 110.0, ca = 20.0, fe = 1.2, mg = 24.0, zn = 0.5),
        f("Gulab jamun", "1 piece", 150.0, 2.0, 22.0, 6.0, "Sweets", sug = 18.0, sat = 3.0, na = 40.0, k = 60.0, ca = 40.0, fe = 0.4, mg = 8.0, zn = 0.3),
        f("Rasgulla", "1 piece", 110.0, 2.6, 22.0, 1.2, "Sweets", sug = 20.0, sat = 0.8, na = 30.0, k = 50.0, ca = 50.0, fe = 0.2, mg = 6.0, zn = 0.3),
        f("Paal payasam", "1 cup", 280.0, 6.0, 44.0, 9.0, "Sweets", fib = 0.6, sug = 30.0, sat = 5.5, na = 90.0, k = 250.0, ca = 200.0, fe = 0.6, mg = 26.0, zn = 0.8, b12 = 0.6, va = 80.0),
        f("Semiya payasam", "1 cup", 260.0, 5.4, 42.0, 8.0, "Sweets", fib = 0.8, sug = 28.0, sat = 4.6, na = 85.0, k = 220.0, ca = 180.0, fe = 0.8, mg = 24.0, zn = 0.7, b12 = 0.5)
    )

    private fun moreDrinks(): List<FoodItem> = listOf(
        f("Panagam", "1 glass", 120.0, 0.4, 30.0, 0.2, "Drinks", sug = 28.0, na = 180.0, k = 120.0, ca = 20.0, fe = 0.8, mg = 12.0, vc = 4.0),
        f("Sukku malli coffee", "1 cup", 60.0, 0.6, 14.0, 0.3, "Drinks", sug = 12.0, na = 15.0, k = 80.0, ca = 20.0, fe = 0.5, mg = 10.0),
        f("Jigarthanda", "1 glass", 320.0, 7.0, 48.0, 11.0, "Drinks", fib = 0.6, sug = 42.0, sat = 6.5, na = 120.0, k = 320.0, ca = 240.0, fe = 0.6, mg = 30.0, zn = 0.8, b12 = 0.7, va = 90.0),
        f("Rose milk", "1 glass", 210.0, 6.0, 32.0, 6.5, "Drinks", sug = 28.0, sat = 4.0, na = 90.0, k = 300.0, ca = 240.0, fe = 0.2, mg = 24.0, zn = 0.8, b12 = 0.9, va = 90.0),
        f("Badam milk", "1 glass", 230.0, 8.0, 28.0, 9.5, "Drinks", fib = 1.2, sug = 24.0, sat = 4.4, na = 95.0, k = 360.0, ca = 280.0, fe = 0.6, mg = 60.0, zn = 1.0, b12 = 0.9, va = 95.0),
        f("Nannari sharbat", "1 glass", 130.0, 0.2, 32.0, 0.1, "Drinks", sug = 30.0, na = 20.0, k = 40.0, ca = 10.0, mg = 4.0),
        f("Sugarcane juice", "1 glass", 180.0, 0.4, 44.0, 0.2, "Drinks", sug = 42.0, na = 25.0, k = 150.0, ca = 30.0, fe = 1.4, mg = 20.0),
        f("Sweet lassi", "1 glass", 220.0, 7.0, 34.0, 6.0, "Drinks", sug = 30.0, sat = 3.8, na = 100.0, k = 330.0, ca = 250.0, fe = 0.2, mg = 26.0, zn = 1.0, b12 = 0.8),
        f("Salted lassi", "1 glass", 110.0, 6.5, 10.0, 5.0, "Drinks", sug = 8.0, sat = 3.2, na = 420.0, k = 320.0, ca = 240.0, fe = 0.2, mg = 24.0, zn = 0.9, b12 = 0.8),
        f("Masala chaas", "1 glass", 70.0, 3.4, 7.0, 2.6, "Drinks", sug = 5.0, sat = 1.6, na = 380.0, k = 280.0, ca = 150.0, fe = 0.2, mg = 20.0, zn = 0.5, b12 = 0.4)
    )

    private fun moreProtein(): List<FoodItem> = listOf(
        f("Turkey breast", "100 g", 135.0, 30.0, 0.0, 1.0, "Protein", sat = 0.3, na = 60.0, k = 290.0, ca = 12.0, fe = 0.7, mg = 28.0, zn = 1.4, b12 = 0.4),
        f("Lean beef mince", "100 g", 215.0, 26.0, 0.0, 12.0, "Protein", sat = 4.8, na = 70.0, k = 320.0, ca = 18.0, fe = 2.6, mg = 22.0, zn = 6.0, b12 = 2.5),
        f("Pork chop", "100 g", 230.0, 26.0, 0.0, 14.0, "Protein", sat = 5.0, na = 60.0, k = 380.0, ca = 20.0, fe = 0.9, mg = 26.0, zn = 2.0, b12 = 0.7),
        f("Lamb chop", "100 g", 260.0, 25.0, 0.0, 18.0, "Protein", sat = 8.0, na = 72.0, k = 310.0, ca = 16.0, fe = 1.8, mg = 24.0, zn = 4.4, b12 = 2.6),
        f("Duck breast", "100 g", 200.0, 23.0, 0.0, 11.0, "Protein", sat = 3.6, na = 90.0, k = 280.0, ca = 12.0, fe = 2.4, mg = 20.0, zn = 1.9, b12 = 0.4),
        f("Chicken sausage", "2 pieces", 180.0, 14.0, 4.0, 12.0, "Protein", fib = 0.4, sat = 3.6, na = 700.0, k = 200.0, ca = 20.0, fe = 1.0, mg = 16.0, zn = 1.2, b12 = 0.4),
        f("Egg bhurji", "2 eggs", 215.0, 13.0, 4.0, 16.0, "Protein", fib = 0.8, sug = 2.0, sat = 4.8, na = 400.0, k = 230.0, ca = 60.0, fe = 1.8, mg = 20.0, zn = 1.2, va = 180.0, vd = 2.2, b12 = 1.1),
        f("Grilled prawns", "100 g", 110.0, 23.0, 1.0, 1.5, "Protein", sat = 0.4, na = 480.0, k = 260.0, ca = 70.0, fe = 1.4, mg = 40.0, zn = 1.6, b12 = 1.3, o3 = 0.3),
        f("Calamari, grilled", "100 g", 120.0, 20.0, 3.0, 2.5, "Protein", sat = 0.7, na = 400.0, k = 280.0, ca = 40.0, fe = 0.9, mg = 34.0, zn = 1.6, b12 = 1.2, o3 = 0.4),
        f("Mackerel, grilled", "100 g", 235.0, 22.0, 0.0, 16.0, "Protein", sat = 3.8, na = 90.0, k = 340.0, ca = 15.0, fe = 1.6, mg = 76.0, zn = 0.9, vd = 16.0, b12 = 8.7, o3 = 2.6),
        f("Pomfret fry", "1 piece (100 g)", 190.0, 20.0, 4.0, 10.0, "Protein", fib = 0.3, sat = 2.4, na = 460.0, k = 360.0, ca = 30.0, fe = 1.0, mg = 34.0, zn = 0.8, vd = 3.0, b12 = 1.6, o3 = 0.5),
        f("Rohu curry", "1 cup", 210.0, 20.0, 7.0, 11.0, "Protein", fib = 1.6, sat = 3.0, na = 640.0, k = 380.0, ca = 50.0, fe = 1.6, mg = 38.0, zn = 1.0, vd = 2.5, b12 = 1.8, o3 = 0.4),
        f("Tilapia, grilled", "100 g", 130.0, 26.0, 0.0, 2.7, "Protein", sat = 0.9, na = 56.0, k = 380.0, ca = 14.0, fe = 0.6, mg = 34.0, zn = 0.4, vd = 3.1, b12 = 1.6, o3 = 0.2),
        f("Basa fillet, grilled", "100 g", 105.0, 18.0, 0.0, 3.5, "Protein", sat = 1.4, na = 80.0, k = 300.0, ca = 12.0, fe = 0.4, mg = 26.0, zn = 0.4, vd = 1.0, b12 = 1.2, o3 = 0.2),
        f("Chicken soup", "1 cup", 110.0, 10.0, 6.0, 5.0, "Protein", fib = 0.8, sat = 1.6, na = 680.0, k = 220.0, ca = 20.0, fe = 0.8, mg = 18.0, zn = 0.7, b12 = 0.3),
        f("Bone broth", "1 cup", 60.0, 9.0, 2.0, 1.5, "Protein", sat = 0.6, na = 520.0, k = 200.0, ca = 30.0, fe = 0.6, mg = 14.0, zn = 0.6, b12 = 0.3),
        f("Protein bar", "1 bar (60 g)", 220.0, 20.0, 22.0, 7.0, "Protein", fib = 4.0, sug = 5.0, sat = 3.0, na = 200.0, k = 200.0, ca = 150.0, fe = 2.4, mg = 60.0, zn = 1.6),
        f("Chicken shawarma plate", "1 plate", 520.0, 40.0, 30.0, 26.0, "Protein", fib = 3.0, sug = 3.0, sat = 6.0, na = 980.0, k = 620.0, ca = 120.0, fe = 3.0, mg = 60.0, zn = 3.0, b12 = 0.7)
    )

    private fun moreVegetables(): List<FoodItem> = listOf(
        f("Zucchini, cooked", "1 cup", 29.0, 2.1, 5.0, 0.5, "Vegetables", fib = 2.0, sug = 2.5, na = 5.0, k = 456.0, ca = 24.0, fe = 0.6, mg = 40.0, zn = 0.4, vc = 12.0, va = 100.0, fol = 30.0),
        f("Asparagus, cooked", "1 cup", 40.0, 4.3, 7.4, 0.4, "Vegetables", fib = 3.6, sug = 2.5, na = 13.0, k = 404.0, ca = 41.0, fe = 1.6, mg = 25.0, zn = 1.1, vc = 14.0, va = 90.0, fol = 268.0),
        f("Green peas, cooked", "1 cup", 134.0, 8.6, 25.0, 0.4, "Vegetables", fib = 8.8, sug = 9.5, na = 5.0, k = 434.0, ca = 43.0, fe = 2.5, mg = 62.0, zn = 1.9, vc = 23.0, va = 64.0, fol = 101.0),
        f("Sweet potato, boiled", "1 medium", 130.0, 2.5, 30.0, 0.2, "Vegetables", fib = 4.5, sug = 9.0, na = 45.0, k = 500.0, ca = 40.0, fe = 0.8, mg = 30.0, zn = 0.4, vc = 22.0, va = 960.0, fol = 12.0),
        f("Boiled potato", "1 medium", 130.0, 3.0, 30.0, 0.2, "Vegetables", fib = 2.5, sug = 1.4, na = 10.0, k = 620.0, ca = 12.0, fe = 0.8, mg = 32.0, zn = 0.4, vc = 20.0, fol = 20.0),
        f("Sweet corn", "1 cup", 145.0, 5.0, 31.0, 2.0, "Vegetables", fib = 3.6, sug = 6.0, na = 25.0, k = 340.0, ca = 8.0, fe = 0.8, mg = 44.0, zn = 0.8, vc = 10.0, va = 20.0, fol = 60.0),
        f("Lettuce", "1 cup", 8.0, 0.6, 1.5, 0.1, "Vegetables", fib = 0.7, sug = 0.6, na = 6.0, k = 100.0, ca = 12.0, fe = 0.3, mg = 5.0, zn = 0.1, vc = 2.0, va = 200.0, fol = 30.0),
        f("Kale, cooked", "1 cup", 43.0, 3.5, 7.3, 0.6, "Vegetables", fib = 2.6, sug = 1.6, na = 30.0, k = 300.0, ca = 180.0, fe = 1.2, mg = 30.0, zn = 0.3, vc = 53.0, va = 885.0, fol = 17.0),
        f("Radish", "1 cup sliced", 19.0, 0.8, 4.0, 0.1, "Vegetables", fib = 1.9, sug = 2.2, na = 45.0, k = 270.0, ca = 29.0, fe = 0.4, mg = 12.0, zn = 0.3, vc = 17.0, fol = 29.0),
        f("Bottle gourd, cooked", "1 cup", 30.0, 1.0, 7.0, 0.2, "Vegetables", fib = 1.6, sug = 2.0, na = 15.0, k = 210.0, ca = 26.0, fe = 0.4, mg = 18.0, zn = 0.2, vc = 10.0),
        f("Ridge gourd, cooked", "1 cup", 34.0, 1.2, 7.5, 0.3, "Vegetables", fib = 2.0, sug = 2.2, na = 12.0, k = 220.0, ca = 30.0, fe = 0.5, mg = 20.0, zn = 0.2, vc = 12.0),
        f("Snake gourd, cooked", "1 cup", 28.0, 1.0, 6.4, 0.2, "Vegetables", fib = 1.8, sug = 1.8, na = 14.0, k = 200.0, ca = 26.0, fe = 0.4, mg = 16.0, zn = 0.2, vc = 9.0),
        f("Ash gourd, cooked", "1 cup", 26.0, 0.8, 6.0, 0.2, "Vegetables", fib = 1.6, sug = 1.6, na = 12.0, k = 180.0, ca = 24.0, fe = 0.4, mg = 14.0, zn = 0.2, vc = 8.0),
        f("Cluster beans, cooked", "1 cup", 50.0, 3.0, 10.0, 0.4, "Vegetables", fib = 4.4, sug = 2.0, na = 20.0, k = 280.0, ca = 130.0, fe = 1.6, mg = 40.0, zn = 0.5, vc = 24.0, va = 80.0, fol = 70.0)
    )

    private fun moreFruit(): List<FoodItem> = listOf(
        f("Pineapple", "1 cup", 82.0, 0.9, 22.0, 0.2, "Fruit", fib = 2.3, sug = 16.0, na = 2.0, k = 180.0, ca = 21.0, fe = 0.3, mg = 20.0, zn = 0.1, vc = 79.0, va = 5.0, fol = 30.0),
        f("Strawberry", "1 cup", 49.0, 1.0, 12.0, 0.5, "Fruit", fib = 3.0, sug = 7.4, na = 2.0, k = 233.0, ca = 24.0, fe = 0.6, mg = 20.0, zn = 0.2, vc = 89.0, fol = 36.0),
        f("Blueberry", "1 cup", 84.0, 1.1, 21.0, 0.5, "Fruit", fib = 3.6, sug = 15.0, na = 1.0, k = 114.0, ca = 9.0, fe = 0.4, mg = 9.0, zn = 0.2, vc = 14.0, va = 4.0, fol = 9.0),
        f("Kiwi", "1 medium", 42.0, 0.8, 10.0, 0.4, "Fruit", fib = 2.1, sug = 6.2, na = 2.0, k = 215.0, ca = 23.0, fe = 0.2, mg = 12.0, zn = 0.1, vc = 64.0, va = 3.0, fol = 17.0),
        f("Pear", "1 medium", 101.0, 0.6, 27.0, 0.2, "Fruit", fib = 5.5, sug = 17.0, na = 2.0, k = 206.0, ca = 15.0, fe = 0.3, mg = 12.0, zn = 0.2, vc = 7.5, fol = 12.0),
        f("Plum", "2 medium", 60.0, 0.9, 15.0, 0.4, "Fruit", fib = 1.8, sug = 13.0, na = 0.0, k = 208.0, ca = 12.0, fe = 0.3, mg = 14.0, zn = 0.2, vc = 19.0, va = 34.0, fol = 10.0),
        f("Custard apple", "1 medium", 145.0, 2.4, 35.0, 0.6, "Fruit", fib = 4.4, sug = 26.0, na = 14.0, k = 380.0, ca = 30.0, fe = 0.7, mg = 32.0, zn = 0.2, vc = 30.0, fol = 20.0),
        f("Jackfruit", "1 cup", 155.0, 2.4, 38.0, 1.0, "Fruit", fib = 2.5, sug = 31.0, na = 3.0, k = 500.0, ca = 40.0, fe = 1.0, mg = 48.0, zn = 0.3, vc = 22.0, va = 10.0, fol = 40.0),
        f("Muskmelon", "1 cup", 54.0, 1.3, 13.0, 0.3, "Fruit", fib = 1.4, sug = 12.0, na = 25.0, k = 417.0, ca = 14.0, fe = 0.3, mg = 19.0, zn = 0.3, vc = 58.0, va = 270.0, fol = 34.0),
        f("Lychee", "1 cup", 125.0, 1.6, 31.0, 0.8, "Fruit", fib = 2.5, sug = 29.0, na = 2.0, k = 325.0, ca = 10.0, fe = 0.6, mg = 20.0, zn = 0.1, vc = 136.0, fol = 27.0),
        f("Fig, fresh", "2 medium", 74.0, 0.8, 19.0, 0.3, "Fruit", fib = 2.9, sug = 16.0, na = 1.0, k = 232.0, ca = 35.0, fe = 0.4, mg = 17.0, zn = 0.2, vc = 2.0, va = 7.0, fol = 6.0),
        f("Raisins", "30 g", 90.0, 0.9, 24.0, 0.1, "Fruit", fib = 1.1, sug = 18.0, na = 3.0, k = 226.0, ca = 15.0, fe = 0.6, mg = 10.0, zn = 0.1, vc = 0.7, fol = 2.0)
    )

    private fun moreNuts(): List<FoodItem> = listOf(
        f("Pistachios", "30 g", 170.0, 6.0, 8.0, 13.5, "Nuts", fib = 3.0, sug = 2.2, sat = 1.7, na = 1.0, k = 300.0, ca = 30.0, fe = 1.2, mg = 34.0, zn = 0.7, vc = 1.5, fol = 15.0),
        f("Hazelnuts", "30 g", 190.0, 4.5, 5.0, 18.0, "Nuts", fib = 2.9, sug = 1.3, sat = 1.3, na = 0.0, k = 200.0, ca = 34.0, fe = 1.4, mg = 49.0, zn = 0.7, fol = 34.0),
        f("Sunflower seeds", "30 g", 175.0, 6.0, 6.0, 15.0, "Nuts", fib = 2.6, sug = 0.8, sat = 1.3, na = 3.0, k = 194.0, ca = 23.0, fe = 1.6, mg = 98.0, zn = 1.5, fol = 68.0),
        f("Sesame seeds", "1 tbsp", 52.0, 1.6, 2.1, 4.5, "Nuts", fib = 1.1, sat = 0.6, na = 1.0, k = 42.0, ca = 88.0, fe = 1.3, mg = 32.0, zn = 0.7, fol = 9.0),
        f("Melon seeds", "20 g", 110.0, 5.0, 3.0, 9.0, "Nuts", fib = 1.2, sat = 1.7, na = 4.0, k = 130.0, ca = 10.0, fe = 1.6, mg = 105.0, zn = 1.4),
        f("Peanut butter", "1 tbsp", 95.0, 4.0, 3.5, 8.0, "Nuts", fib = 1.0, sug = 1.5, sat = 1.6, na = 70.0, k = 105.0, ca = 12.0, fe = 0.3, mg = 25.0, zn = 0.5, fol = 12.0),
        f("Almond butter", "1 tbsp", 98.0, 3.4, 3.0, 9.0, "Nuts", fib = 1.6, sug = 0.7, sat = 0.7, na = 2.0, k = 120.0, ca = 55.0, fe = 0.6, mg = 45.0, zn = 0.5, fol = 10.0),
        f("Mixed nuts", "30 g", 180.0, 5.0, 6.0, 16.0, "Nuts", fib = 2.4, sug = 1.4, sat = 2.2, na = 4.0, k = 200.0, ca = 30.0, fe = 1.2, mg = 60.0, zn = 1.0, fol = 20.0)
    )

    private fun moreDairy(): List<FoodItem> = listOf(
        f("Skimmed milk", "200 ml", 70.0, 7.0, 10.0, 0.4, "Dairy", sug = 10.0, sat = 0.2, na = 90.0, k = 340.0, ca = 250.0, fe = 0.1, mg = 24.0, zn = 0.9, vd = 1.2, b12 = 1.0),
        f("Cheddar cheese", "30 g", 120.0, 7.0, 0.4, 10.0, "Dairy", sat = 6.0, na = 180.0, k = 30.0, ca = 200.0, fe = 0.1, mg = 8.0, zn = 0.9, va = 90.0, b12 = 0.3),
        f("Mozzarella", "30 g", 85.0, 6.3, 0.6, 6.3, "Dairy", sat = 3.7, na = 140.0, k = 24.0, ca = 150.0, fe = 0.1, mg = 6.0, zn = 0.9, va = 50.0, b12 = 0.7),
        f("Cream cheese", "2 tbsp", 100.0, 2.0, 1.5, 10.0, "Dairy", sug = 1.2, sat = 5.6, na = 100.0, k = 40.0, ca = 30.0, fe = 0.1, mg = 3.0, zn = 0.2, va = 110.0),
        f("Flavoured yogurt", "150 g", 140.0, 5.0, 24.0, 2.5, "Dairy", sug = 20.0, sat = 1.6, na = 70.0, k = 220.0, ca = 170.0, fe = 0.1, mg = 18.0, zn = 0.8, b12 = 0.5),
        f("Shrikhand", "1/2 cup", 250.0, 6.0, 34.0, 10.0, "Dairy", sug = 30.0, sat = 6.0, na = 80.0, k = 220.0, ca = 200.0, fe = 0.3, mg = 20.0, zn = 0.8, b12 = 0.6, va = 80.0),
        f("Khoa", "30 g", 125.0, 4.5, 6.0, 9.0, "Dairy", sug = 5.0, sat = 5.6, na = 30.0, k = 120.0, ca = 150.0, fe = 0.2, mg = 12.0, zn = 0.5, va = 70.0, b12 = 0.3),
        f("Curd, low fat", "1 cup (200 g)", 90.0, 8.0, 10.0, 2.0, "Dairy", sug = 9.0, sat = 1.2, na = 95.0, k = 340.0, ca = 260.0, fe = 0.2, mg = 26.0, zn = 1.2, b12 = 0.9)
    )

    private fun moreGrains(): List<FoodItem> = listOf(
        f("Basmati rice, cooked", "1 cup (150 g)", 200.0, 4.4, 44.0, 0.5, "Grains", fib = 0.7, sat = 0.1, na = 2.0, k = 55.0, ca = 12.0, fe = 1.5, mg = 20.0, zn = 0.8, fol = 80.0),
        f("Foxtail millet, cooked", "1 cup", 190.0, 5.6, 38.0, 1.6, "Grains", fib = 4.0, sat = 0.3, na = 5.0, k = 180.0, ca = 20.0, fe = 1.6, mg = 60.0, zn = 1.4),
        f("Pearl millet, cooked", "1 cup", 200.0, 6.4, 38.0, 2.2, "Grains", fib = 4.4, sat = 0.4, na = 6.0, k = 200.0, ca = 26.0, fe = 3.0, mg = 90.0, zn = 1.6),
        f("Finger millet, cooked", "1 cup", 185.0, 5.0, 38.0, 1.2, "Grains", fib = 4.2, sat = 0.3, na = 8.0, k = 160.0, ca = 200.0, fe = 2.4, mg = 76.0, zn = 1.2),
        f("Dalia, cooked", "1 cup", 180.0, 6.0, 36.0, 1.0, "Grains", fib = 4.6, sat = 0.2, na = 8.0, k = 150.0, ca = 20.0, fe = 1.6, mg = 50.0, zn = 1.0),
        f("Wheat rava, cooked", "1 cup", 190.0, 6.2, 38.0, 1.2, "Grains", fib = 3.0, sat = 0.2, na = 6.0, k = 120.0, ca = 18.0, fe = 1.4, mg = 40.0, zn = 0.9),
        f("Bulgur, cooked", "1 cup", 150.0, 5.6, 34.0, 0.4, "Grains", fib = 8.0, sug = 0.2, sat = 0.1, na = 9.0, k = 124.0, ca = 18.0, fe = 1.7, mg = 58.0, zn = 1.0, fol = 33.0),
        f("Rice noodles, cooked", "1 cup", 190.0, 3.2, 44.0, 0.4, "Grains", fib = 1.8, sat = 0.1, na = 33.0, k = 10.0, ca = 7.0, fe = 0.2, mg = 8.0, zn = 0.4),
        f("Whole wheat pasta, cooked", "1 cup", 174.0, 7.5, 37.0, 0.8, "Grains", fib = 6.0, sug = 1.1, sat = 0.1, na = 4.0, k = 130.0, ca = 21.0, fe = 1.5, mg = 60.0, zn = 1.1, fol = 7.0),
        f("Sourdough bread", "1 slice", 93.0, 3.5, 18.0, 0.6, "Grains", fib = 1.0, sug = 0.8, sat = 0.1, na = 180.0, k = 45.0, ca = 20.0, fe = 1.0, mg = 12.0, zn = 0.4, fol = 30.0)
    )

    private fun moreSupplements(): List<FoodItem> = listOf(
        f("BCAA scoop", "1 scoop (7 g)", 28.0, 7.0, 0.0, 0.0, "Supplement", supp = true, na = 80.0),
        f("Glutamine", "5 g", 20.0, 5.0, 0.0, 0.0, "Supplement", supp = true),
        f("Beta-alanine", "3 g", 0.0, 0.0, 0.0, 0.0, "Supplement", supp = true),
        f("L-carnitine", "1 serving", 5.0, 1.0, 0.0, 0.0, "Supplement", supp = true),
        f("Pre-workout scoop", "1 scoop", 15.0, 0.0, 4.0, 0.0, "Supplement", supp = true, na = 150.0, mg = 50.0),
        f("Mass gainer", "1 scoop (100 g)", 380.0, 22.0, 70.0, 3.0, "Supplement", supp = true, fib = 2.0, sug = 12.0, sat = 1.4, na = 220.0, k = 400.0, ca = 300.0, fe = 4.0, mg = 90.0, zn = 3.0),
        f("Collagen peptides", "1 scoop (10 g)", 36.0, 9.0, 0.0, 0.0, "Supplement", supp = true, na = 40.0),
        f("Melatonin 5 mg", "1 tablet", 0.0, 0.0, 0.0, 0.0, "Supplement", supp = true),
        f("Algal omega-3", "1 capsule", 10.0, 0.0, 0.0, 1.0, "Supplement", supp = true, sat = 0.2, o3 = 0.4),
        f("Vitamin E 400 IU", "1 capsule", 10.0, 0.0, 0.0, 1.0, "Supplement", supp = true, sat = 0.2),
        f("Biotin 10000 mcg", "1 tablet", 0.0, 0.0, 0.0, 0.0, "Supplement", supp = true),
        f("Probiotic capsule", "1 capsule", 5.0, 0.0, 1.0, 0.0, "Supplement", supp = true)
    )

    /**
     * The one honest corner of the catalogue. A label is a measurement, so unlike
     * everything above, these numbers are as accurate as the bottle you bought.
     *
     * Doses are the common retail strengths — adjust each one to match yours. Nothing
     * here is a recommendation to take any of it; the app logs what you take so that
     * the nutrient panel reflects reality, and that is all.
     */
    private fun supplements(): List<FoodItem> = listOf(
        f("Whey isolate", "1 scoop (30 g)", 110.0, 25.0, 1.0, 0.5, "Supplement", supp = true, sat = 0.2, na = 60.0, k = 160.0, ca = 130.0, fe = 0.3, mg = 25.0, zn = 0.6),
        f("Raw whey concentrate, unflavoured", "1 scoop (30 g)", 120.0, 24.0, 2.0, 1.5, "Supplement", supp = true, sug = 1.8, sat = 0.9, na = 55.0, k = 150.0, ca = 120.0, fe = 0.2, mg = 22.0, zn = 0.5),
        f("Casein", "1 scoop (30 g)", 110.0, 24.0, 3.0, 0.5, "Supplement", supp = true, sug = 1.0, sat = 0.3, na = 180.0, k = 120.0, ca = 400.0, fe = 0.2, mg = 20.0, zn = 0.6),
        f("Creatine monohydrate", "5 g", 0.0, 0.0, 0.0, 0.0, "Supplement", supp = true),
        f("Fish oil 1000 mg", "1 capsule", 10.0, 0.0, 0.0, 1.0, "Supplement", supp = true, sat = 0.3, o3 = 0.3),
        f("Omega-3, triple strength", "1 capsule", 15.0, 0.0, 0.0, 1.5, "Supplement", supp = true, sat = 0.2, o3 = 0.9),
        f("Cod liver oil", "1 capsule", 10.0, 0.0, 0.0, 1.0, "Supplement", supp = true, sat = 0.2, va = 250.0, vd = 2.5, o3 = 0.25),
        f("Magnesium glycinate 200 mg", "1 tablet", 0.0, 0.0, 0.0, 0.0, "Supplement", supp = true, mg = 200.0),
        f("Magnesium citrate 330 mg", "1 sachet", 5.0, 0.0, 1.0, 0.0, "Supplement", supp = true, mg = 330.0),
        f("Vitamin D3 2000 IU", "1 capsule", 0.0, 0.0, 0.0, 0.0, "Supplement", supp = true, vd = 50.0),
        f("Vitamin B12 1500 mcg", "1 tablet", 0.0, 0.0, 0.0, 0.0, "Supplement", supp = true, b12 = 1500.0),
        f("Zinc 25 mg", "1 tablet", 0.0, 0.0, 0.0, 0.0, "Supplement", supp = true, zn = 25.0),
        f("Multivitamin", "1 tablet", 5.0, 0.0, 1.0, 0.0, "Supplement", supp = true, fe = 8.0, mg = 50.0, zn = 11.0, va = 800.0, vc = 90.0, vd = 10.0, b12 = 2.5, fol = 400.0),
        f("Calcium 500 mg with D3", "1 tablet", 0.0, 0.0, 0.0, 0.0, "Supplement", supp = true, ca = 500.0, vd = 5.0),
        f("Iron with folic acid", "1 tablet", 0.0, 0.0, 0.0, 0.0, "Supplement", supp = true, fe = 20.0, fol = 400.0),
        f("Vitamin C 500 mg", "1 tablet", 0.0, 0.0, 0.0, 0.0, "Supplement", supp = true, vc = 500.0),
        f("Ashwagandha 600 mg", "1 capsule", 5.0, 0.0, 1.0, 0.0, "Supplement", supp = true),
        f("Caffeine 200 mg", "1 tablet", 0.0, 0.0, 0.0, 0.0, "Supplement", supp = true),
        f("Electrolyte sachet", "1 sachet", 35.0, 0.0, 9.0, 0.0, "Supplement", supp = true, sug = 8.0, na = 400.0, k = 200.0, mg = 30.0),
        f("EAA scoop", "1 scoop (10 g)", 40.0, 9.0, 0.0, 0.0, "Supplement", supp = true, na = 100.0),
        f("Psyllium husk", "1 tbsp", 10.0, 0.0, 5.0, 0.0, "Supplement", supp = true, fib = 5.0, k = 30.0)
    )
}
