package dev.avles.gym.data

/**
 * The menus most people in Tamil Nadu eat from: the vegetarian chains (Sangeetha,
 * Geetham, Saravana Bhavan and their like) and the non-vegetarian ones (Anjappar,
 * Thalappakatti, Junior Kuppanna, Buhari), plus the egg counter and the meals.
 *
 * Meals and combos are logged as one item because that is how they are eaten and
 * ordered: a veg meals plate is rice with sambar, rasam, kootu, poriyal, appalam,
 * curd and payasam; chicken, mutton, fish and egg meals add that gravy and a fry.
 * The figures are for the first serving of an unlimited plate, so a second round
 * of rice is a second entry. Dishes the catalogue already had under these menus'
 * names (the dosas, idlis, biryanis, chukkas and the rest) are not repeated.
 * Calories are derived from the macros (4/4/9, fibre at 2).
 */
internal object SeedFoodsMenus {

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

    fun all(): List<FoodItem> = eggs() + mushroom() + mealsAndCombos() + vegRestaurantMenu() + nonvegRestaurantMenu()

    private fun eggs() = listOf(
        f("Masala omelette", "2 eggs", 200.0, 13.0, 4.0, 15.0, "Protein", aka = "Onion omelette, Kalakki omelette", fib = 0.8, sug = 1.5, sat = 4.0, na = 420.0, k = 180.0, ca = 60.0, fe = 1.9, mg = 14.0, zn = 1.3, va = 170.0, vd = 2.0, b12 = 1.0, fol = 50.0),
        f("Double egg omelette", "2 eggs", 180.0, 12.5, 1.2, 14.0, "Protein", aka = "Plain omelette, 2 egg omelette", sat = 3.8, na = 300.0, k = 140.0, ca = 55.0, fe = 1.8, mg = 12.0, zn = 1.3, va = 160.0, vd = 2.0, b12 = 1.0, fol = 45.0),
        f("Cheese omelette", "2 eggs", 280.0, 18.0, 2.0, 22.0, "Protein", sat = 9.0, na = 520.0, k = 150.0, ca = 230.0, fe = 1.9, mg = 18.0, zn = 2.2, va = 240.0, vd = 2.1, b12 = 1.3, fol = 48.0),
        f("Half boil", "1 egg", 75.0, 6.3, 0.4, 5.5, "Protein", aka = "Half boiled egg, Half fry", sat = 1.6, na = 160.0, k = 65.0, ca = 26.0, fe = 0.9, mg = 6.0, zn = 0.6, va = 80.0, vd = 1.0, b12 = 0.5, fol = 22.0),
        f("Kalakki", "1 egg", 105.0, 6.5, 1.5, 8.0, "Protein", aka = "Mutta kalakki, Egg kalakki", sat = 2.2, na = 220.0, k = 80.0, ca = 28.0, fe = 1.0, mg = 7.0, zn = 0.6, va = 85.0, vd = 1.0, b12 = 0.5, fol = 24.0),
        f("Egg pepper fry", "2 eggs", 195.0, 13.0, 5.0, 14.0, "Protein", aka = "Muttai milagu varuval", fib = 1.0, sat = 3.6, na = 480.0, k = 190.0, ca = 60.0, fe = 2.2, mg = 18.0, zn = 1.4, va = 160.0, vd = 2.0, b12 = 1.0, fol = 48.0),
        f("Egg 65", "2 eggs", 265.0, 14.0, 12.0, 18.0, "Protein", aka = "Muttai 65", fib = 0.5, sat = 3.4, na = 620.0, k = 170.0, ca = 60.0, fe = 2.0, mg = 16.0, zn = 1.3, va = 150.0, vd = 2.0, b12 = 1.0, fol = 46.0),
        f("Chilli egg", "2 eggs", 255.0, 14.0, 14.0, 16.0, "Chinese", aka = "Egg chilli", fib = 1.5, sug = 5.0, sat = 3.2, na = 900.0, k = 220.0, ca = 60.0, fe = 2.1, mg = 18.0, zn = 1.3, va = 160.0, vc = 20.0, vd = 2.0, b12 = 1.0, fol = 48.0),
        f("Egg masala", "2 eggs", 230.0, 14.0, 8.0, 16.0, "Protein", aka = "Muttai masala, Egg masala gravy", fib = 2.0, sug = 3.0, sat = 3.6, na = 560.0, k = 260.0, ca = 70.0, fe = 2.4, mg = 22.0, zn = 1.4, va = 190.0, vc = 8.0, vd = 2.0, b12 = 1.0, fol = 55.0),
        f("Egg kothu parotta", "1 plate", 595.0, 18.0, 70.0, 28.0, "Tiffin", aka = "Muttai kothu", fib = 4.0, sug = 4.0, sat = 9.0, na = 1100.0, k = 320.0, ca = 80.0, fe = 3.0, mg = 36.0, zn = 1.8, va = 150.0, vd = 1.5, b12 = 0.8, fol = 60.0)
    )

    private fun mushroom() = listOf(
        f("Mushroom gravy", "1 cup", 170.0, 5.0, 12.0, 12.0, "Sides", aka = "Mushroom curry, Kaalan kuzhambu, Kaalan gravy", fib = 3.0, sug = 4.0, sat = 2.4, na = 600.0, k = 420.0, ca = 30.0, fe = 1.0, mg = 18.0, zn = 0.9, vc = 8.0, fol = 24.0),
        f("Mushroom masala", "1 cup", 220.0, 6.0, 14.0, 16.0, "North Indian", aka = "Mushroom masala North Indian", fib = 3.0, sug = 5.0, sat = 5.0, na = 650.0, k = 430.0, ca = 40.0, fe = 1.1, mg = 20.0, zn = 1.0, vc = 10.0, fol = 26.0),
        f("Mushroom 65", "1 plate", 280.0, 7.0, 24.0, 18.0, "Snacks", aka = "Kaalan 65", fib = 3.0, sug = 2.0, sat = 2.8, na = 700.0, k = 400.0, ca = 20.0, fe = 1.0, mg = 18.0, zn = 0.9, vc = 4.0),
        f("Mushroom Chettinad", "1 cup", 200.0, 6.0, 12.0, 15.0, "Sides", aka = "Chettinad mushroom", fib = 3.0, sug = 3.0, sat = 4.0, na = 620.0, k = 420.0, ca = 30.0, fe = 1.2, mg = 20.0, zn = 0.9, vc = 6.0)
    )

    private fun mealsAndCombos() = listOf(
        f("Veg meals", "1 plate", 875.0, 22.0, 150.0, 24.0, "Meals", aka = "Full meals, Tamil meals, South Indian meals, Saapadu, Unlimited meals", fib = 14.0, sug = 12.0, sat = 8.0, na = 1900.0, k = 1100.0, ca = 260.0, fe = 5.0, mg = 140.0, zn = 3.5, va = 180.0, vc = 30.0, b12 = 0.4, fol = 160.0),
        f("Veg meals, limited", "1 plate", 670.0, 18.0, 120.0, 16.0, "Meals", aka = "Limited meals, South Indian thali", fib = 12.0, sug = 9.0, sat = 5.5, na = 1500.0, k = 900.0, ca = 200.0, fe = 4.0, mg = 110.0, zn = 2.8, va = 150.0, vc = 25.0, b12 = 0.3, fol = 130.0),
        f("Mini meals", "1 plate", 570.0, 15.0, 100.0, 14.0, "Meals", aka = "Mini saapadu", fib = 9.0, sug = 7.0, sat = 5.0, na = 1300.0, k = 700.0, ca = 170.0, fe = 3.2, mg = 90.0, zn = 2.2, va = 110.0, vc = 18.0, b12 = 0.3, fol = 100.0),
        f("Mini tiffin", "1 plate", 645.0, 15.0, 95.0, 24.0, "Meals", aka = "Sangeetha mini tiffin, Tiffin combo", fib = 6.0, sug = 14.0, sat = 9.0, na = 1300.0, k = 380.0, ca = 90.0, fe = 2.6, mg = 60.0, zn = 1.8, fol = 50.0),
        f("North Indian thali", "1 plate", 910.0, 28.0, 130.0, 34.0, "Meals", aka = "Thali, Punjabi thali", fib = 14.0, sug = 12.0, sat = 14.0, na = 2000.0, k = 1000.0, ca = 320.0, fe = 5.5, mg = 130.0, zn = 3.6, va = 200.0, vc = 30.0, b12 = 0.5, fol = 170.0),
        f("Chicken meals", "1 plate", 1100.0, 45.0, 150.0, 38.0, "Meals", aka = "Kozhi saapadu, Chicken full meals", fib = 12.0, sug = 10.0, sat = 11.0, na = 2300.0, k = 1300.0, ca = 230.0, fe = 6.0, mg = 150.0, zn = 5.0, va = 150.0, vc = 25.0, b12 = 0.9, fol = 140.0),
        f("Mutton meals", "1 plate", 1140.0, 42.0, 150.0, 44.0, "Meals", aka = "Mutton saapadu, Mutton full meals", fib = 12.0, sug = 10.0, sat = 15.0, na = 2300.0, k = 1300.0, ca = 230.0, fe = 7.0, mg = 150.0, zn = 8.0, va = 150.0, vc = 25.0, b12 = 2.4, fol = 140.0),
        f("Fish meals", "1 plate", 1000.0, 40.0, 148.0, 30.0, "Meals", aka = "Meen saapadu, Fish full meals", fib = 12.0, sug = 10.0, sat = 8.0, na = 2300.0, k = 1350.0, ca = 280.0, fe = 5.5, mg = 160.0, zn = 3.8, va = 150.0, vc = 25.0, vd = 4.0, b12 = 2.5, fol = 140.0, o3 = 0.6),
        f("Egg meals", "1 plate", 965.0, 30.0, 150.0, 30.0, "Meals", aka = "Muttai saapadu", fib = 12.0, sug = 10.0, sat = 9.0, na = 2100.0, k = 1150.0, ca = 260.0, fe = 6.0, mg = 140.0, zn = 4.0, va = 280.0, vc = 25.0, vd = 2.0, b12 = 1.2, fol = 190.0),
        f("Non-veg meals", "1 plate", 1145.0, 48.0, 150.0, 42.0, "Meals", aka = "Non veg full meals, Non veg saapadu", fib = 12.0, sug = 10.0, sat = 13.0, na = 2500.0, k = 1350.0, ca = 250.0, fe = 6.8, mg = 160.0, zn = 6.0, va = 150.0, vc = 25.0, b12 = 1.8, fol = 140.0),
        f("Variety rice combo", "1 plate", 680.0, 14.0, 110.0, 22.0, "Meals", aka = "Mixed rice combo, Lemon tamarind curd rice", fib = 6.0, sug = 5.0, sat = 6.0, na = 1500.0, k = 450.0, ca = 150.0, fe = 2.6, mg = 70.0, zn = 1.8, vc = 10.0, fol = 60.0),
        f("Chicken biryani combo", "1 plate", 910.0, 40.0, 110.0, 36.0, "Meals", aka = "Biryani with raita, brinjal and egg", fib = 6.0, sug = 8.0, sat = 12.0, na = 2000.0, k = 700.0, ca = 180.0, fe = 4.0, mg = 80.0, zn = 4.0, va = 120.0, vc = 10.0, b12 = 1.2, fol = 80.0),
        f("Parotta salna combo", "2 parotta", 685.0, 22.0, 75.0, 34.0, "Meals", aka = "Parotta with chicken salna", fib = 4.0, sug = 4.0, sat = 12.0, na = 1300.0, k = 400.0, ca = 60.0, fe = 2.6, mg = 40.0, zn = 1.8, b12 = 0.3),
        f("Chapati kurma combo", "2 chapati", 360.0, 12.0, 50.0, 14.0, "Meals", aka = "Chapati with veg kurma", fib = 7.0, sug = 4.0, sat = 4.0, na = 700.0, k = 380.0, ca = 60.0, fe = 2.6, mg = 60.0, zn = 1.4, vc = 8.0),
        f("Idli vada combo", "2 idli + 1 vada", 410.0, 12.0, 58.0, 16.0, "Meals", aka = "Idli vada sambar", fib = 6.0, sug = 3.0, sat = 3.2, na = 1000.0, k = 380.0, ca = 70.0, fe = 2.4, mg = 50.0, zn = 1.4, fol = 50.0),
        f("Pongal vada combo", "1 plate", 495.0, 12.0, 60.0, 24.0, "Meals", aka = "Pongal vada sambar", fib = 5.0, sug = 2.0, sat = 9.0, na = 1000.0, k = 320.0, ca = 60.0, fe = 2.2, mg = 50.0, zn = 1.4, fol = 45.0),
        f("Poori masala combo", "2 poori", 465.0, 9.0, 60.0, 22.0, "Meals", aka = "Poori kizhangu", fib = 4.0, sug = 3.0, sat = 4.0, na = 700.0, k = 420.0, ca = 40.0, fe = 2.0, mg = 40.0, zn = 1.0, vc = 10.0),
        f("Chole bhature", "2 bhature", 790.0, 18.0, 95.0, 40.0, "North Indian", aka = "Chana bhatura", fib = 10.0, sug = 6.0, sat = 8.0, na = 1400.0, k = 600.0, ca = 110.0, fe = 5.0, mg = 90.0, zn = 2.6, vc = 8.0, fol = 170.0)
    )

    private fun vegRestaurantMenu() = listOf(
        f("Onion rava dosa", "1 dosa", 250.0, 5.0, 32.0, 12.0, "Dosa", aka = "Rava onion dosa", fib = 2.0, sug = 2.0, sat = 4.5, na = 420.0, k = 140.0, ca = 25.0, fe = 1.2, mg = 24.0, zn = 0.6),
        f("Cheese dosa", "1 dosa", 270.0, 10.0, 26.0, 14.0, "Dosa", fib = 1.2, sat = 7.0, na = 480.0, k = 80.0, ca = 200.0, fe = 0.9, mg = 24.0, zn = 1.2),
        f("Paneer dosa", "1 dosa", 310.0, 12.0, 30.0, 16.0, "Dosa", aka = "Paneer masala dosa", fib = 2.0, sug = 2.0, sat = 8.0, na = 480.0, k = 150.0, ca = 200.0, fe = 1.0, mg = 26.0, zn = 1.2),
        f("Tomato uttapam", "1 uttapam", 215.0, 5.0, 34.0, 7.0, "Tiffin", aka = "Thakkali uttapam", fib = 2.5, sug = 3.0, sat = 2.4, na = 380.0, k = 200.0, ca = 20.0, fe = 1.1, mg = 26.0, zn = 0.6, vc = 8.0),
        f("Mixed vegetable uttapam", "1 uttapam", 235.0, 6.0, 36.0, 8.0, "Tiffin", aka = "Veg uttapam", fib = 3.5, sug = 3.0, sat = 2.6, na = 400.0, k = 230.0, ca = 30.0, fe = 1.2, mg = 28.0, zn = 0.6, va = 60.0, vc = 8.0),
        f("Podi uttapam", "1 uttapam", 230.0, 5.0, 34.0, 9.0, "Tiffin", fib = 2.5, sug = 1.0, sat = 3.0, na = 420.0, k = 150.0, ca = 40.0, fe = 1.4, mg = 30.0, zn = 0.7),
        f("Adai avial", "1 adai + avial", 340.0, 12.0, 44.0, 14.0, "Dosa", aka = "Adai with avial", fib = 6.0, sug = 3.0, sat = 6.0, na = 500.0, k = 420.0, ca = 60.0, fe = 2.4, mg = 60.0, zn = 1.5, vc = 8.0, fol = 60.0),
        f("Mysore bonda", "2 bonda", 240.0, 4.0, 30.0, 12.0, "Snacks", fib = 1.0, sug = 1.0, sat = 2.0, na = 260.0, k = 60.0, ca = 20.0, fe = 0.8, mg = 10.0, zn = 0.3),
        f("Veg cutlet", "2 cutlets", 230.0, 4.0, 28.0, 12.0, "Snacks", aka = "Vegetable cutlet", fib = 3.0, sug = 2.0, sat = 2.0, na = 380.0, k = 280.0, ca = 25.0, fe = 1.0, mg = 22.0, zn = 0.4, va = 80.0, vc = 6.0),
        f("Gobi 65", "1 plate", 250.0, 5.0, 24.0, 16.0, "Snacks", aka = "Cauliflower 65", fib = 4.0, sug = 3.0, sat = 2.4, na = 620.0, k = 320.0, ca = 30.0, fe = 1.0, mg = 20.0, zn = 0.4, vc = 40.0),
        f("Paneer 65", "1 plate", 370.0, 18.0, 16.0, 26.0, "Snacks", fib = 1.0, sug = 1.0, sat = 12.0, na = 650.0, k = 150.0, ca = 360.0, fe = 0.6, mg = 20.0, zn = 2.0),
        f("Paneer tikka", "6 pieces", 325.0, 20.0, 8.0, 24.0, "North Indian", fib = 1.5, sug = 3.0, sat = 12.0, na = 560.0, k = 200.0, ca = 420.0, fe = 0.6, mg = 24.0, zn = 2.2, vc = 10.0),
        f("Kadai paneer", "1 cup", 340.0, 16.0, 12.0, 26.0, "North Indian", aka = "Kadhai paneer", fib = 3.0, sug = 5.0, sat = 12.0, na = 700.0, k = 320.0, ca = 340.0, fe = 1.0, mg = 26.0, zn = 2.0, vc = 30.0),
        f("Paneer tikka masala", "1 cup", 375.0, 18.0, 14.0, 28.0, "North Indian", fib = 2.5, sug = 6.0, sat = 13.0, na = 760.0, k = 330.0, ca = 380.0, fe = 1.0, mg = 26.0, zn = 2.2, vc = 12.0),
        f("Dal fry", "1 cup", 200.0, 9.0, 26.0, 8.0, "North Indian", aka = "Dal fry restaurant", fib = 6.0, sug = 2.0, sat = 3.0, na = 600.0, k = 380.0, ca = 40.0, fe = 2.4, mg = 50.0, zn = 1.2, fol = 120.0),
        f("Veg kolhapuri", "1 cup", 210.0, 5.0, 18.0, 14.0, "North Indian", aka = "Vegetable kolhapuri", fib = 5.0, sug = 5.0, sat = 4.0, na = 650.0, k = 380.0, ca = 50.0, fe = 1.4, mg = 30.0, zn = 0.6, va = 120.0, vc = 20.0),
        f("Kaju masala", "1 cup", 400.0, 10.0, 20.0, 32.0, "North Indian", aka = "Kaju curry, Cashew masala", fib = 3.0, sug = 6.0, sat = 10.0, na = 700.0, k = 380.0, ca = 50.0, fe = 2.6, mg = 110.0, zn = 2.4),
        f("Butter kulcha", "1 kulcha", 255.0, 6.0, 40.0, 8.0, "North Indian", aka = "Kulcha", fib = 1.5, sug = 2.0, sat = 4.5, na = 420.0, k = 80.0, ca = 40.0, fe = 1.6, mg = 16.0, zn = 0.5),
        f("Paneer fried rice", "1 plate", 550.0, 16.0, 78.0, 20.0, "Chinese", fib = 3.0, sug = 3.0, sat = 8.0, na = 1200.0, k = 260.0, ca = 250.0, fe = 1.4, mg = 36.0, zn = 1.6),
        f("Veg Schezwan fried rice", "1 plate", 495.0, 8.0, 82.0, 16.0, "Chinese", aka = "Veg Szechuan rice", fib = 4.0, sug = 5.0, sat = 2.6, na = 1500.0, k = 300.0, ca = 40.0, fe = 1.6, mg = 36.0, zn = 1.2, vc = 15.0),
        f("Veg Schezwan noodles", "1 plate", 465.0, 8.0, 70.0, 18.0, "Chinese", aka = "Veg Szechuan noodles", fib = 4.5, sug = 6.0, sat = 2.8, na = 1600.0, k = 300.0, ca = 40.0, fe = 1.8, mg = 36.0, zn = 1.0, vc = 15.0),
        f("Badam halwa", "75 g", 285.0, 4.5, 30.0, 16.5, "Sweets", aka = "Almond halwa", fib = 1.5, sug = 25.0, sat = 7.0, na = 20.0, k = 140.0, ca = 60.0, fe = 0.8, mg = 50.0, zn = 0.6),
        f("Kasi halwa", "75 g", 245.0, 1.0, 38.0, 10.0, "Sweets", aka = "Ash gourd halwa, Poosanikai halwa", fib = 1.0, sug = 30.0, sat = 6.0, na = 10.0, k = 80.0, ca = 15.0, fe = 0.3),
        f("Falooda", "1 glass", 435.0, 8.0, 70.0, 14.0, "Sweets", aka = "Royal falooda", fib = 2.0, sug = 50.0, sat = 8.0, na = 120.0, k = 380.0, ca = 260.0, fe = 0.6, mg = 30.0, zn = 1.0),
        f("Kulfi", "1 stick", 185.0, 4.0, 22.0, 9.0, "Sweets", aka = "Malai kulfi", sug = 18.0, sat = 5.5, na = 60.0, k = 190.0, ca = 140.0, mg = 14.0, zn = 0.5),
        f("Fruit salad with ice cream", "1 cup", 280.0, 4.0, 50.0, 8.0, "Sweets", fib = 4.0, sug = 40.0, sat = 5.0, na = 50.0, k = 420.0, ca = 100.0, fe = 0.6, mg = 30.0, vc = 30.0),
        f("Onion raita", "1 cup", 80.0, 4.0, 8.0, 4.0, "Sides", aka = "Raita, Vengaya thayir pachadi", fib = 0.8, sug = 6.0, sat = 2.6, na = 280.0, k = 230.0, ca = 140.0, mg = 16.0, zn = 0.6, b12 = 0.4)
    )

    private fun nonvegRestaurantMenu() = listOf(
        f("Mutton Chettinad", "1 cup", 320.0, 24.0, 8.0, 22.0, "Protein", aka = "Chettinad mutton curry", fib = 2.0, sug = 2.0, sat = 7.0, na = 700.0, k = 400.0, ca = 40.0, fe = 3.0, mg = 30.0, zn = 5.0, b12 = 2.2),
        f("Pichi potta kozhi", "1 plate", 305.0, 30.0, 6.0, 18.0, "Protein", aka = "Shredded chicken fry, Pichi potta chicken", fib = 1.5, sat = 4.0, na = 650.0, k = 380.0, ca = 30.0, fe = 1.6, mg = 34.0, zn = 2.0, b12 = 0.4),
        f("Nattu kozhi pepper fry", "1 plate", 295.0, 28.0, 6.0, 18.0, "Protein", aka = "Country chicken pepper fry", fib = 1.5, sat = 4.0, na = 640.0, k = 360.0, ca = 30.0, fe = 2.0, mg = 34.0, zn = 2.4, b12 = 0.5),
        f("Fish 65", "1 plate", 285.0, 24.0, 12.0, 16.0, "Protein", aka = "Meen 65", fib = 0.5, sat = 2.8, na = 620.0, k = 420.0, ca = 40.0, fe = 1.0, mg = 36.0, zn = 0.7, vd = 4.0, b12 = 1.5, o3 = 0.3),
        f("Prawn 65", "1 plate", 285.0, 22.0, 14.0, 16.0, "Protein", aka = "Eral 65", fib = 0.5, sat = 2.6, na = 700.0, k = 280.0, ca = 80.0, fe = 1.0, mg = 40.0, zn = 1.4, b12 = 1.0),
        f("Kadai chicken", "1 cup", 345.0, 28.0, 10.0, 22.0, "North Indian", aka = "Kadhai chicken", fib = 2.5, sug = 4.0, sat = 6.0, na = 720.0, k = 440.0, ca = 40.0, fe = 1.8, mg = 36.0, zn = 2.0, vc = 25.0, b12 = 0.4),
        f("Chicken Nilgiri kurma", "1 cup", 355.0, 26.0, 10.0, 24.0, "Protein", aka = "Nilgiri chicken, Green chicken kurma", fib = 3.0, sug = 3.0, sat = 10.0, na = 650.0, k = 420.0, ca = 50.0, fe = 2.2, mg = 40.0, zn = 1.8, va = 120.0, vc = 15.0, b12 = 0.4),
        f("Mutton keema", "1 cup", 290.0, 22.0, 6.0, 20.0, "Protein", aka = "Kheema, Mutton kheema", fib = 1.5, sug = 2.0, sat = 7.0, na = 600.0, k = 380.0, ca = 30.0, fe = 2.8, mg = 28.0, zn = 4.5, b12 = 2.0),
        f("Mutton soup", "1 bowl", 110.0, 10.0, 4.0, 6.0, "Protein", aka = "Elumbu soup, Mutton bone soup", fib = 0.5, sat = 2.4, na = 700.0, k = 250.0, ca = 30.0, fe = 1.2, mg = 14.0, zn = 1.6, b12 = 0.8),
        f("Chicken tikka biryani", "1 plate", 725.0, 34.0, 90.0, 26.0, "Rice", fib = 3.0, sug = 3.0, sat = 8.0, na = 1500.0, k = 450.0, ca = 80.0, fe = 2.4, mg = 60.0, zn = 2.6, b12 = 0.5),
        f("Dindigul mutton biryani", "1 plate", 710.0, 30.0, 85.0, 28.0, "Rice", aka = "Thalappakatti biryani, Seeraga samba biryani", fib = 2.0, sug = 2.0, sat = 10.0, na = 1500.0, k = 450.0, ca = 60.0, fe = 3.4, mg = 50.0, zn = 5.0, b12 = 2.0),
        f("Chicken 65 biryani", "1 plate", 755.0, 32.0, 90.0, 30.0, "Rice", fib = 2.0, sug = 2.0, sat = 8.0, na = 1600.0, k = 420.0, ca = 60.0, fe = 2.4, mg = 55.0, zn = 2.4, b12 = 0.5),
        f("Mutton kothu parotta", "1 plate", 650.0, 22.0, 70.0, 32.0, "Tiffin", aka = "Mutton kothu", fib = 4.0, sug = 4.0, sat = 11.0, na = 1200.0, k = 360.0, ca = 60.0, fe = 3.6, mg = 40.0, zn = 4.0, b12 = 1.6)
    )
}
