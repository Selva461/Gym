package dev.avles.gym.data

/**
 * Tamil Nadu curries — the long tail the rest of the catalogue skipped.
 *
 * [SeedData] and [SeedFoodsMore] already carry the everyday ones (sambar, rasam,
 * kara, vatha, puli, mor and meen kuzhambu, the Chettinad chicken and mutton, the
 * common poriyals and kootus). This file is everything else a Tamil kitchen puts
 * on a plate: each kuzhambu named for what goes in it, sambar the way it actually
 * varies, the herbal rasams, the tiffin gravies, and the Chettinad, Kongu and
 * coastal non-vegetarian dishes.
 *
 * Two things differ from the files before it.
 *
 * **Calories are derived, not typed.** Each entry's energy is its protein, carbs
 * and fat at 4/4/9 kcal a gram, fibre at 2, rounded to 5 — the same arithmetic
 * `FoodChecks` holds every food to — so no entry can disagree with itself. The
 * macros are home-style estimates for the stated portion, and as with every food
 * here, editing the ones you eat most is worth more than any number of entries.
 *
 * **Every dish carries its other names.** "Kathirikai" is brinjal is eggplant;
 * search matches any of them, so nobody has to know the Tamil to find the dish.
 */
internal object SeedFoodsTamil {

    private fun f(
        name: String, unit: String, kcal: Double, p: Double, c: Double, fat: Double, cat: String,
        aka: String,
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

    fun all(): List<FoodItem> = kuzhambu() + sambar() + kootu() + poriyal() + rasam() + tiffin() + nonveg()

    /** Tamarind kuzhambus — the everyday gravy with rice, each named for what goes in it. */
    private fun kuzhambu(): List<FoodItem> = listOf(
        f("Vendakkai puli kuzhambu", "1 cup", 140.0, 3.5, 16.0, 8.0, "Sides", aka = "Okra tamarind curry / Ladies finger kuzhambu", fib = 4.0, sug = 4.0, sat = 1.4, na = 700.0, k = 400.0, ca = 70.0, fe = 1.6, mg = 45.0, zn = 0.6, va = 20.0, vc = 12.0, fol = 60.0),
        f("Kathirikai kara kuzhambu", "1 cup", 150.0, 3.0, 16.0, 9.0, "Sides", aka = "Brinjal / Eggplant spicy kuzhambu", fib = 4.5, sug = 5.0, sat = 1.5, na = 720.0, k = 420.0, ca = 40.0, fe = 1.4, mg = 30.0, zn = 0.5, vc = 6.0, fol = 30.0),
        f("Poondu kuzhambu", "1 cup", 170.0, 4.0, 18.0, 9.5, "Sides", aka = "Garlic kuzhambu", fib = 3.0, sug = 3.0, sat = 1.6, na = 740.0, k = 380.0, ca = 50.0, fe = 1.6, mg = 32.0, zn = 0.8, vc = 10.0, fol = 20.0),
        f("Vengaya kuzhambu", "1 cup", 160.0, 3.0, 18.0, 9.0, "Sides", aka = "Small onion / Shallot kuzhambu / Chinna vengaya", fib = 3.2, sug = 6.0, sat = 1.5, na = 720.0, k = 360.0, ca = 45.0, fe = 1.4, mg = 30.0, zn = 0.6, vc = 8.0, fol = 30.0),
        f("Manathakkali vathal kuzhambu", "1 cup", 125.0, 3.0, 12.0, 8.0, "Sides", aka = "Black nightshade / Sun berry vathal kuzhambu", fib = 3.5, sug = 2.0, sat = 1.4, na = 760.0, k = 340.0, ca = 70.0, fe = 2.6, mg = 38.0, zn = 0.6, vc = 4.0),
        f("Thakkali kuzhambu", "1 cup", 135.0, 3.0, 14.0, 8.0, "Sides", aka = "Tomato kuzhambu / Tomato curry", fib = 3.0, sug = 7.0, sat = 1.4, na = 650.0, k = 450.0, ca = 30.0, fe = 1.2, mg = 28.0, zn = 0.4, va = 60.0, vc = 24.0, fol = 25.0),
        f("Murungakkai puli kuzhambu", "1 cup", 140.0, 3.5, 15.0, 8.0, "Sides", aka = "Drumstick tamarind curry", fib = 3.8, sug = 3.0, sat = 1.4, na = 700.0, k = 420.0, ca = 60.0, fe = 1.4, mg = 40.0, zn = 0.6, vc = 40.0, fol = 40.0),
        f("Vazhakkai kuzhambu", "1 cup", 175.0, 3.0, 24.0, 8.0, "Sides", aka = "Raw banana / Plantain kuzhambu", fib = 3.5, sug = 4.0, sat = 1.4, na = 700.0, k = 520.0, ca = 30.0, fe = 1.2, mg = 40.0, zn = 0.4, vc = 10.0, fol = 30.0),
        f("Paruppu urundai kuzhambu", "1 cup", 215.0, 11.0, 26.0, 9.0, "Sides", aka = "Lentil dumpling curry / Dal balls kuzhambu", fib = 6.0, sug = 3.0, sat = 1.6, na = 700.0, k = 480.0, ca = 60.0, fe = 2.8, mg = 62.0, zn = 1.4, vc = 6.0, fol = 120.0),
        f("Kollu kuzhambu", "1 cup", 180.0, 9.0, 24.0, 7.0, "Sides", aka = "Horse gram curry / Kulthi", fib = 6.5, sug = 2.0, sat = 1.2, na = 620.0, k = 420.0, ca = 90.0, fe = 3.4, mg = 70.0, zn = 1.2, fol = 80.0),
        f("Kondakadalai kuzhambu", "1 cup", 215.0, 10.0, 30.0, 8.0, "Sides", aka = "Black chickpea / Kala chana kuzhambu", fib = 8.0, sug = 4.0, sat = 1.4, na = 640.0, k = 480.0, ca = 60.0, fe = 3.0, mg = 70.0, zn = 1.6, fol = 150.0),
        f("Karamani kuzhambu", "1 cup", 190.0, 9.0, 26.0, 7.0, "Sides", aka = "Black-eyed peas / Thattai payaru / Lobia kuzhambu", fib = 6.5, sug = 3.0, sat = 1.2, na = 620.0, k = 450.0, ca = 50.0, fe = 2.8, mg = 80.0, zn = 1.4, fol = 180.0),
        f("Pavakkai pitlai", "1 cup", 155.0, 6.0, 18.0, 8.0, "Sides", aka = "Bitter gourd pitlai / Karela", fib = 5.5, sug = 3.0, sat = 4.5, na = 560.0, k = 420.0, ca = 45.0, fe = 1.8, mg = 50.0, zn = 0.9, vc = 40.0, fol = 90.0),
        f("Kathirikai rasavangi", "1 cup", 150.0, 6.0, 18.0, 7.0, "Sides", aka = "Brinjal rasavangi / Eggplant", fib = 5.5, sug = 4.0, sat = 3.5, na = 540.0, k = 420.0, ca = 45.0, fe = 1.8, mg = 48.0, zn = 0.9, vc = 6.0, fol = 80.0),
        f("Molagootal", "1 cup", 135.0, 6.0, 14.0, 7.0, "Sides", aka = "Molakootal / Palakkad vegetable and moong curry", fib = 4.5, sug = 3.0, sat = 5.0, na = 420.0, k = 380.0, ca = 60.0, fe = 1.6, mg = 40.0, zn = 0.8, va = 120.0, vc = 14.0, fol = 70.0)
    )

    /** Sambar, the way it actually varies from house to house. */
    private fun sambar(): List<FoodItem> = listOf(
        f("Vengaya sambar", "1 cup", 140.0, 6.0, 21.0, 4.5, "Sides", aka = "Small onion sambar / Shallot sambar", fib = 5.0, sug = 5.0, sat = 1.0, na = 520.0, k = 420.0, ca = 45.0, fe = 2.0, mg = 46.0, zn = 1.0, vc = 8.0, fol = 85.0),
        f("Murungakkai sambar", "1 cup", 125.0, 6.5, 19.0, 4.0, "Sides", aka = "Drumstick sambar", fib = 5.5, sug = 3.0, sat = 1.0, na = 520.0, k = 480.0, ca = 70.0, fe = 2.2, mg = 50.0, zn = 1.0, vc = 40.0, fol = 95.0),
        f("Mullangi sambar", "1 cup", 120.0, 5.5, 18.0, 4.0, "Sides", aka = "Radish sambar / Mooli", fib = 5.0, sug = 4.0, sat = 1.0, na = 530.0, k = 440.0, ca = 55.0, fe = 2.0, mg = 44.0, zn = 0.9, vc = 16.0, fol = 90.0),
        f("Kathirikai sambar", "1 cup", 130.0, 5.5, 19.0, 4.5, "Sides", aka = "Brinjal sambar / Eggplant", fib = 5.5, sug = 4.0, sat = 1.0, na = 520.0, k = 450.0, ca = 45.0, fe = 2.0, mg = 46.0, zn = 0.9, vc = 6.0, fol = 85.0),
        f("Arachuvitta sambar", "1 cup", 165.0, 6.5, 20.0, 8.0, "Sides", aka = "Freshly ground coconut sambar", fib = 5.5, sug = 3.0, sat = 4.5, na = 540.0, k = 440.0, ca = 50.0, fe = 2.2, mg = 52.0, zn = 1.1, vc = 8.0, fol = 90.0),
        f("Tiffin sambar", "1 cup", 115.0, 5.0, 17.0, 4.0, "Sides", aka = "Hotel sambar for idli and dosa", fib = 4.0, sug = 4.0, sat = 1.0, na = 560.0, k = 360.0, ca = 40.0, fe = 1.8, mg = 40.0, zn = 0.9, vc = 8.0, fol = 70.0),
        f("Keerai sambar", "1 cup", 125.0, 7.0, 18.0, 4.0, "Sides", aka = "Spinach sambar / Greens", fib = 5.5, sug = 2.0, sat = 1.0, na = 520.0, k = 520.0, ca = 110.0, fe = 3.2, mg = 65.0, zn = 1.1, va = 300.0, vc = 16.0, fol = 140.0)
    )

    /** Kootu — lentils and a vegetable, mild, with coconut. */
    private fun kootu(): List<FoodItem> = listOf(
        f("Keerai kootu", "1/2 cup", 100.0, 5.5, 11.0, 5.0, "Sides", aka = "Spinach kootu / Greens dal", fib = 4.5, sug = 1.0, sat = 3.0, na = 300.0, k = 420.0, ca = 110.0, fe = 2.8, mg = 60.0, zn = 0.8, va = 260.0, vc = 16.0, fol = 120.0),
        f("Suraikkai kootu", "1/2 cup", 100.0, 4.5, 11.0, 5.0, "Sides", aka = "Bottle gourd kootu / Lauki", fib = 3.5, sug = 2.0, sat = 3.2, na = 280.0, k = 260.0, ca = 40.0, fe = 1.2, mg = 30.0, zn = 0.7, vc = 8.0, fol = 60.0),
        f("Pudalangai kootu", "1/2 cup", 100.0, 4.5, 11.0, 5.0, "Sides", aka = "Snake gourd kootu", fib = 3.8, sug = 2.0, sat = 3.2, na = 280.0, k = 280.0, ca = 40.0, fe = 1.2, mg = 30.0, zn = 0.7, vc = 10.0, fol = 60.0),
        f("Vazhaithandu kootu", "1/2 cup", 105.0, 4.0, 13.0, 5.0, "Sides", aka = "Banana stem kootu", fib = 4.5, sug = 2.0, sat = 3.2, na = 280.0, k = 340.0, ca = 40.0, fe = 1.4, mg = 30.0, zn = 0.6, vc = 6.0, fol = 50.0),
        f("Avarakkai kootu", "1/2 cup", 110.0, 5.5, 13.0, 5.0, "Sides", aka = "Broad beans / Flat beans kootu", fib = 5.0, sug = 2.0, sat = 3.2, na = 290.0, k = 300.0, ca = 45.0, fe = 1.6, mg = 36.0, zn = 0.8, vc = 12.0, fol = 80.0),
        f("Poricha kootu", "1/2 cup", 110.0, 5.5, 13.0, 5.0, "Sides", aka = "Moong dal and vegetable kootu", fib = 4.2, sug = 2.0, sat = 3.2, na = 280.0, k = 300.0, ca = 40.0, fe = 1.5, mg = 38.0, zn = 0.8, vc = 10.0, fol = 75.0)
    )

    /** Poriyal, varuval and roast — the dry sides. */
    private fun poriyal(): List<FoodItem> = listOf(
        f("Carrot poriyal", "1/2 cup", 80.0, 1.8, 10.0, 4.5, "Sides", aka = "Carrot stir fry", fib = 3.0, sug = 5.0, sat = 2.8, na = 200.0, k = 260.0, ca = 35.0, fe = 0.4, mg = 14.0, zn = 0.3, va = 700.0, vc = 5.0, fol = 18.0),
        f("Beetroot poriyal", "1/2 cup", 85.0, 2.2, 11.0, 4.5, "Sides", aka = "Beetroot stir fry", fib = 3.0, sug = 7.0, sat = 2.8, na = 220.0, k = 320.0, ca = 20.0, fe = 0.8, mg = 22.0, zn = 0.4, vc = 4.0, fol = 80.0),
        f("Kovakkai poriyal", "1/2 cup", 75.0, 1.8, 7.0, 5.0, "Sides", aka = "Ivy gourd / Tindora / Kundru stir fry", fib = 3.0, sug = 1.0, sat = 1.5, na = 200.0, k = 180.0, ca = 30.0, fe = 1.0, mg = 18.0, zn = 0.3, va = 150.0, vc = 12.0, fol = 20.0),
        f("Pavakkai poriyal", "1/2 cup", 90.0, 2.0, 9.0, 6.0, "Sides", aka = "Bitter gourd fry / Karela", fib = 3.5, sug = 2.0, sat = 1.2, na = 220.0, k = 300.0, ca = 25.0, fe = 0.8, mg = 20.0, zn = 0.6, va = 40.0, vc = 50.0, fol = 60.0),
        f("Avarakkai poriyal", "1/2 cup", 80.0, 3.0, 9.0, 4.5, "Sides", aka = "Broad beans / Flat beans stir fry", fib = 3.5, sug = 2.0, sat = 2.8, na = 210.0, k = 240.0, ca = 40.0, fe = 1.0, mg = 24.0, zn = 0.5, vc = 10.0, fol = 40.0),
        f("Keerai poriyal", "1/2 cup", 70.0, 3.2, 6.0, 4.5, "Sides", aka = "Spinach stir fry / Greens poriyal", fib = 3.0, sug = 0.5, sat = 2.6, na = 240.0, k = 480.0, ca = 110.0, fe = 2.6, mg = 70.0, zn = 0.6, va = 400.0, vc = 18.0, fol = 140.0),
        f("Chow chow poriyal", "1/2 cup", 70.0, 1.5, 7.0, 4.5, "Sides", aka = "Chayote stir fry / Bangalore brinjal", fib = 2.5, sug = 2.0, sat = 2.8, na = 200.0, k = 140.0, ca = 20.0, fe = 0.4, mg = 14.0, zn = 0.6, vc = 8.0, fol = 60.0),
        f("Pudalangai poriyal", "1/2 cup", 70.0, 2.0, 6.0, 4.5, "Sides", aka = "Snake gourd stir fry", fib = 2.5, sug = 1.5, sat = 2.8, na = 200.0, k = 200.0, ca = 30.0, fe = 0.6, mg = 16.0, zn = 0.4, vc = 10.0, fol = 40.0),
        f("Kothavarangai poriyal", "1/2 cup", 75.0, 2.5, 8.0, 4.5, "Sides", aka = "Cluster beans / Guar stir fry", fib = 3.5, sug = 1.0, sat = 2.8, na = 210.0, k = 220.0, ca = 70.0, fe = 1.2, mg = 24.0, zn = 0.4, va = 60.0, vc = 8.0, fol = 50.0),
        f("Kathirikai poriyal", "1/2 cup", 85.0, 1.8, 9.0, 5.5, "Sides", aka = "Brinjal / Eggplant stir fry", fib = 3.5, sug = 4.0, sat = 1.2, na = 220.0, k = 260.0, ca = 15.0, fe = 0.4, mg = 16.0, zn = 0.2, vc = 3.0, fol = 20.0),
        f("Cauliflower poriyal", "1/2 cup", 70.0, 2.4, 7.0, 4.5, "Sides", aka = "Gobi stir fry", fib = 2.8, sug = 2.0, sat = 2.6, na = 220.0, k = 280.0, ca = 25.0, fe = 0.5, mg = 16.0, zn = 0.3, vc = 45.0, fol = 55.0),
        f("Senai kizhangu roast", "1/2 cup", 150.0, 2.0, 22.0, 7.0, "Sides", aka = "Elephant foot yam roast / Suran", fib = 3.5, sug = 1.0, sat = 1.2, na = 260.0, k = 480.0, ca = 30.0, fe = 0.8, mg = 30.0, zn = 0.4, vc = 8.0, fol = 20.0),
        f("Seppankizhangu roast", "1/2 cup", 160.0, 2.0, 22.0, 8.0, "Sides", aka = "Colocasia / Arbi / Taro roast", fib = 3.5, sug = 0.5, sat = 1.4, na = 280.0, k = 460.0, ca = 30.0, fe = 0.8, mg = 30.0, zn = 0.3, vc = 4.0, fol = 20.0),
        f("Vazhaipoo usili", "1/2 cup", 125.0, 6.0, 14.0, 6.0, "Sides", aka = "Banana flower and lentil stir fry", fib = 5.0, sug = 1.0, sat = 1.1, na = 260.0, k = 400.0, ca = 50.0, fe = 1.8, mg = 50.0, zn = 0.8, fol = 70.0),
        f("Urulaikizhangu podimas", "1/2 cup", 130.0, 2.4, 20.0, 5.0, "Sides", aka = "Potato podimas / Potato stir fry", fib = 2.4, sug = 1.0, sat = 2.8, na = 260.0, k = 420.0, ca = 14.0, fe = 0.8, mg = 26.0, zn = 0.4, vc = 14.0, fol = 18.0),
        f("Mushroom pepper fry", "1/2 cup", 105.0, 4.0, 7.0, 7.0, "Sides", aka = "Kaalan milagu varuval", fib = 2.0, sug = 2.0, sat = 1.2, na = 420.0, k = 420.0, ca = 10.0, fe = 0.8, mg = 16.0, zn = 0.8, vd = 0.3, fol = 20.0),
        f("Meal maker varuval", "1/2 cup", 160.0, 16.0, 10.0, 7.0, "Protein", aka = "Soya chunks fry / Soya chukka", fib = 4.0, sug = 2.0, sat = 1.2, na = 420.0, k = 620.0, ca = 100.0, fe = 3.5, mg = 90.0, zn = 1.6, fol = 110.0),
        f("Mango pachadi", "1/2 cup", 105.0, 1.0, 22.0, 2.0, "Sides", aka = "Maangai pachadi / Sweet and sour mango", fib = 2.0, sug = 18.0, sat = 0.6, na = 120.0, k = 200.0, ca = 20.0, fe = 0.6, mg = 14.0, zn = 0.1, va = 60.0, vc = 18.0)
    )

    /** Rasam, including the herbal ones. */
    private fun rasam(): List<FoodItem> = listOf(
        f("Paruppu rasam", "1 cup", 70.0, 4.0, 10.0, 2.2, "Sides", aka = "Dal rasam / Lentil rasam", fib = 2.0, sug = 2.0, sat = 0.6, na = 480.0, k = 300.0, ca = 26.0, fe = 1.4, mg = 30.0, zn = 0.6, vc = 12.0, fol = 50.0),
        f("Poondu rasam", "1 cup", 65.0, 2.2, 9.0, 2.4, "Sides", aka = "Garlic rasam", fib = 1.6, sug = 2.0, sat = 0.6, na = 480.0, k = 280.0, ca = 26.0, fe = 1.2, mg = 24.0, zn = 0.5, vc = 14.0, fol = 20.0),
        f("Elumichai rasam", "1 cup", 65.0, 3.5, 9.0, 2.0, "Sides", aka = "Lemon rasam", fib = 1.8, sug = 1.5, sat = 0.5, na = 470.0, k = 260.0, ca = 22.0, fe = 1.2, mg = 26.0, zn = 0.5, vc = 22.0, fol = 40.0),
        f("Jeera rasam", "1 cup", 55.0, 2.0, 8.0, 2.2, "Sides", aka = "Seeragam rasam / Cumin rasam", fib = 1.6, sug = 1.5, sat = 0.5, na = 470.0, k = 260.0, ca = 40.0, fe = 2.0, mg = 28.0, zn = 0.4, vc = 12.0, fol = 20.0),
        f("Pineapple rasam", "1 cup", 80.0, 3.0, 14.0, 2.0, "Sides", aka = "Annasi rasam", fib = 1.8, sug = 7.0, sat = 0.5, na = 460.0, k = 300.0, ca = 24.0, fe = 1.1, mg = 28.0, zn = 0.5, vc = 30.0, fol = 40.0),
        f("Kollu rasam", "1 cup", 75.0, 5.0, 11.0, 2.0, "Sides", aka = "Horse gram rasam / Kulthi", fib = 3.0, sug = 1.0, sat = 0.5, na = 460.0, k = 320.0, ca = 60.0, fe = 2.2, mg = 40.0, zn = 0.8, fol = 40.0),
        f("Mudakathan rasam", "1 cup", 55.0, 2.0, 8.0, 2.0, "Sides", aka = "Balloon vine rasam / Mudakathan keerai", fib = 2.0, sug = 1.5, sat = 0.5, na = 470.0, k = 280.0, ca = 40.0, fe = 1.4, mg = 26.0, zn = 0.4, vc = 16.0, fol = 30.0),
        f("Thuthuvalai rasam", "1 cup", 55.0, 2.0, 8.0, 2.0, "Sides", aka = "Thoothuvalai / Solanum trilobatum rasam", fib = 2.0, sug = 1.5, sat = 0.5, na = 470.0, k = 280.0, ca = 40.0, fe = 1.4, mg = 26.0, zn = 0.4, vc = 18.0, fol = 30.0)
    )

    /** Gravies that go with tiffin — poori, parotta, idli, dosa. */
    private fun tiffin(): List<FoodItem> = listOf(
        f("Poori masala", "1 cup", 175.0, 3.5, 26.0, 7.0, "Tiffin", aka = "Poori kizhangu / Potato masala for poori", fib = 3.5, sug = 4.0, sat = 1.2, na = 560.0, k = 560.0, ca = 30.0, fe = 1.2, mg = 36.0, zn = 0.5, vc = 20.0, fol = 30.0),
        f("Bombay chutney", "1 cup", 130.0, 4.5, 16.0, 6.0, "Tiffin", aka = "Besan gravy / Kadalai maavu chutney", fib = 2.5, sug = 3.0, sat = 1.1, na = 520.0, k = 200.0, ca = 20.0, fe = 1.4, mg = 30.0, zn = 0.7, vc = 6.0, fol = 40.0),
        f("Kosamalli", "1 cup", 140.0, 3.0, 20.0, 6.5, "Tiffin", aka = "Chettinad brinjal and potato side for idli", fib = 4.5, sug = 5.0, sat = 1.2, na = 560.0, k = 520.0, ca = 30.0, fe = 1.2, mg = 36.0, zn = 0.5, vc = 14.0, fol = 35.0),
        f("Kothsu", "1 cup", 135.0, 3.0, 18.0, 7.0, "Tiffin", aka = "Chidambaram gothsu / Brinjal gothsu", fib = 5.0, sug = 6.0, sat = 1.3, na = 620.0, k = 480.0, ca = 40.0, fe = 1.4, mg = 34.0, zn = 0.5, vc = 8.0, fol = 30.0),
        f("Empty salna", "1 cup", 155.0, 4.0, 12.0, 11.0, "Tiffin", aka = "Veg salna for parotta / Plain salna", fib = 3.0, sug = 4.0, sat = 5.5, na = 620.0, k = 320.0, ca = 40.0, fe = 1.4, mg = 30.0, zn = 0.6, vc = 10.0, fol = 30.0),
        f("Vellai kurma", "1 cup", 185.0, 5.0, 14.0, 13.0, "Tiffin", aka = "White kurma / Coconut vegetable kurma", fib = 3.5, sug = 3.0, sat = 8.0, na = 480.0, k = 340.0, ca = 50.0, fe = 1.4, mg = 40.0, zn = 0.8, vc = 14.0, fol = 40.0)
    )

    /** Non-vegetarian — Chettinad, Kongu, Madurai and the coast. */
    private fun nonveg(): List<FoodItem> = listOf(
        f("Chicken salna", "1 cup", 250.0, 18.0, 9.0, 16.0, "Protein", aka = "Parotta salna / Kozhi salna", fib = 2.0, sug = 3.0, sat = 6.0, na = 720.0, k = 380.0, ca = 40.0, fe = 1.8, mg = 36.0, zn = 1.6, b12 = 0.4),
        f("Mutton salna", "1 cup", 270.0, 16.0, 8.0, 20.0, "Protein", aka = "Parotta salna / Aattu kari salna", fib = 2.0, sug = 3.0, sat = 8.0, na = 720.0, k = 340.0, ca = 36.0, fe = 2.4, mg = 32.0, zn = 3.4, b12 = 2.0),
        f("Mutton kurma", "1 cup", 320.0, 22.0, 9.0, 22.0, "Protein", aka = "Aattu kari kurma / Mutton korma", fib = 2.0, sug = 3.0, sat = 10.0, na = 640.0, k = 380.0, ca = 50.0, fe = 2.8, mg = 38.0, zn = 4.2, b12 = 2.2),
        f("Mutton uppu kari", "1 cup", 330.0, 30.0, 4.0, 22.0, "Protein", aka = "Chettinad salt mutton / Uppu kari", fib = 1.0, sug = 1.0, sat = 9.5, na = 820.0, k = 420.0, ca = 30.0, fe = 3.6, mg = 36.0, zn = 5.2, b12 = 2.6),
        f("Mutton kola urundai kuzhambu", "1 cup", 260.0, 16.0, 9.0, 18.0, "Protein", aka = "Mutton meatball curry", fib = 2.0, sug = 2.0, sat = 7.0, na = 700.0, k = 340.0, ca = 40.0, fe = 2.4, mg = 30.0, zn = 3.4, b12 = 1.8),
        f("Aattu kaal paya", "1 cup", 185.0, 14.0, 6.0, 12.0, "Protein", aka = "Goat trotters soup curry / Paya", fib = 1.0, sug = 1.0, sat = 5.0, na = 700.0, k = 260.0, ca = 60.0, fe = 1.6, mg = 20.0, zn = 2.0, b12 = 0.8),
        f("Kudal kuzhambu", "1 cup", 220.0, 16.0, 8.0, 14.0, "Protein", aka = "Goat intestine curry / Boti", fib = 2.0, sug = 2.0, sat = 5.5, na = 720.0, k = 300.0, ca = 40.0, fe = 3.0, mg = 30.0, zn = 2.6, b12 = 3.0),
        f("Aattu moolai varuval", "100 g", 165.0, 11.0, 3.0, 12.0, "Protein", aka = "Goat brain fry / Brain masala", fib = 0.5, sug = 0.5, sat = 3.5, na = 420.0, k = 280.0, ca = 12.0, fe = 2.0, mg = 14.0, zn = 1.2, b12 = 10.0),
        f("Pallipalayam chicken", "1 cup", 250.0, 28.0, 4.0, 14.0, "Protein", aka = "Kongu coconut chilli chicken", fib = 1.2, sug = 1.0, sat = 5.0, na = 700.0, k = 420.0, ca = 40.0, fe = 1.8, mg = 42.0, zn = 2.2, b12 = 0.5),
        f("Nattu kozhi varuval", "1 cup", 280.0, 30.0, 5.0, 16.0, "Protein", aka = "Country chicken fry", fib = 1.4, sug = 1.0, sat = 5.0, na = 760.0, k = 440.0, ca = 40.0, fe = 2.4, mg = 44.0, zn = 2.6, b12 = 0.6),
        f("Nandu kuzhambu", "1 cup", 175.0, 16.0, 9.0, 9.0, "Protein", aka = "Crab tamarind curry", fib = 2.0, sug = 2.0, sat = 2.0, na = 780.0, k = 320.0, ca = 90.0, fe = 1.2, mg = 44.0, zn = 3.4, b12 = 5.0, o3 = 0.3),
        f("Eral kuzhambu", "1 cup", 185.0, 18.0, 9.0, 9.0, "Protein", aka = "Prawn tamarind curry / Shrimp kuzhambu", fib = 2.0, sug = 2.0, sat = 2.0, na = 760.0, k = 300.0, ca = 80.0, fe = 1.6, mg = 44.0, zn = 1.6, b12 = 1.2, o3 = 0.3),
        f("Nethili meen kuzhambu", "1 cup", 195.0, 18.0, 9.0, 10.0, "Protein", aka = "Anchovy curry / Nethili kuzhambu", fib = 2.0, sug = 2.0, sat = 2.6, na = 760.0, k = 360.0, ca = 180.0, fe = 2.4, mg = 42.0, zn = 1.4, vd = 3.0, b12 = 3.0, o3 = 0.6),
        f("Kanava masala", "1 cup", 190.0, 20.0, 8.0, 9.0, "Protein", aka = "Squid masala / Kanava thokku", fib = 1.6, sug = 2.0, sat = 2.0, na = 720.0, k = 320.0, ca = 40.0, fe = 1.2, mg = 44.0, zn = 1.8, b12 = 1.4, o3 = 0.4),
        f("Karuvadu varuval", "50 g", 170.0, 22.0, 3.0, 8.0, "Protein", aka = "Dry fish fry / Karuvattu varuval", fib = 0.5, sat = 1.6, na = 1400.0, k = 300.0, ca = 120.0, fe = 1.6, mg = 50.0, zn = 1.0, b12 = 3.0, o3 = 0.3),
        f("Egg roast", "2 eggs", 205.0, 13.0, 8.0, 14.0, "Protein", aka = "Muttai roast / Egg masala roast", fib = 1.8, sug = 3.0, sat = 4.0, na = 560.0, k = 300.0, ca = 70.0, fe = 2.0, mg = 24.0, zn = 1.4, va = 180.0, vd = 2.0, b12 = 1.0, fol = 50.0)
    )
}
