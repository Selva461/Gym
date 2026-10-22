package dev.avles.gym.data

/**
 * Staples: what a plate is built from, rather than finished dishes.
 *
 * The rest of the catalogue is dishes, which suits logging a meal. It suits
 * weighing one less well. Anyone who cooks their own chicken and puts it on a
 * scale needs "100 g, raw" and "100 g, cooked" as different foods (cooking drives
 * out water, so the same piece is a third denser afterwards), and anyone making
 * dosa needs the batter, not the dosa. The same goes for flour, dry rava and dry
 * soya chunks, which are weighed before they are cooked.
 *
 * Also here: the bananas sold in Tamil Nadu by name (a nendran is two poovans),
 * boiled legumes with nothing added, and the bakery and tea-stall snacks the
 * dish lists skipped.
 *
 * Calories are derived from each food's macros (4/4/9, fibre at 2), the same
 * arithmetic FoodChecks enforces. Generated, then reviewed; see the README.
 */
internal object SeedFoodsStaples {

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

    fun all(): List<FoodItem> = weighed() + fruit() + vegetables() + dryStaples() + legumes() + snacks() + sweets() + dairyAndDrinks()

    private fun weighed() = listOf(
        f("Chicken breast, raw", "100 g", 115.0, 22.5, 0.0, 2.6, "Protein", aka = "Chicken breast uncooked", sat = 0.6, na = 45.0, k = 370.0, ca = 5.0, fe = 0.4, mg = 28.0, zn = 0.7, b12 = 0.2, fol = 4.0),
        f("Chicken breast, cooked weight", "100 g", 155.0, 31.0, 0.0, 3.6, "Protein", aka = "Boiled chicken breast, Cooked chicken breast", sat = 1.0, na = 74.0, k = 256.0, ca = 15.0, fe = 1.0, mg = 29.0, zn = 1.0, b12 = 0.3, fol = 4.0),
        f("Chicken thigh, skinless, raw", "100 g", 115.0, 19.7, 0.0, 3.9, "Protein", aka = "Leg piece raw", sat = 1.0, na = 95.0, k = 240.0, ca = 8.0, fe = 0.8, mg = 23.0, zn = 1.6, b12 = 0.5),
        f("Chicken thigh, cooked weight", "100 g", 180.0, 26.0, 0.0, 8.2, "Protein", aka = "Leg piece cooked", sat = 2.3, na = 106.0, k = 270.0, ca = 12.0, fe = 1.1, mg = 24.0, zn = 2.4, b12 = 0.4),
        f("Mutton, raw", "100 g", 105.0, 20.6, 0.0, 2.3, "Protein", aka = "Goat meat raw, Aattu kari", sat = 0.7, na = 82.0, k = 385.0, ca = 13.0, fe = 2.8, mg = 23.0, zn = 4.0, b12 = 1.1),
        f("Mutton, cooked weight", "100 g", 135.0, 27.0, 0.0, 3.0, "Protein", aka = "Goat meat cooked", sat = 0.9, na = 86.0, k = 405.0, ca = 17.0, fe = 3.7, mg = 26.0, zn = 5.3, b12 = 1.2),
        f("Rohu fish, raw", "100 g", 80.0, 17.0, 0.0, 1.4, "Protein", aka = "Rohu meen", sat = 0.3, na = 50.0, k = 290.0, ca = 20.0, fe = 1.0, mg = 25.0, zn = 0.8, b12 = 1.5, o3 = 0.2),
        f("Seer fish, raw", "100 g", 105.0, 21.0, 0.0, 2.5, "Protein", aka = "Vanjaram raw, King fish", sat = 0.6, na = 60.0, k = 440.0, ca = 25.0, fe = 0.6, mg = 32.0, zn = 0.5, vd = 6.0, b12 = 2.5, o3 = 0.5),
        f("Sardine, raw", "100 g", 180.0, 20.9, 0.0, 10.5, "Protein", aka = "Mathi meen, Chaala", sat = 2.8, na = 100.0, k = 397.0, ca = 80.0, fe = 2.9, mg = 39.0, zn = 1.3, vd = 4.8, b12 = 8.9, o3 = 1.5),
        f("Prawns, raw", "100 g", 85.0, 20.0, 0.0, 0.5, "Protein", aka = "Eral raw, Shrimp", na = 119.0, k = 264.0, ca = 64.0, fe = 0.5, mg = 35.0, zn = 1.3, b12 = 1.1, o3 = 0.3),
        f("Soya chunks, dry", "50 g", 160.0, 26.0, 16.5, 0.3, "Protein", aka = "Meal maker dry, Soya nuggets", fib = 6.5, sug = 3.5, na = 10.0, k = 1200.0, ca = 180.0, fe = 10.0, mg = 145.0, zn = 2.4, fol = 170.0),
        f("Tempeh", "100 g", 200.0, 20.3, 7.6, 10.8, "Protein", fib = 3.5, sat = 2.2, na = 9.0, k = 412.0, ca = 111.0, fe = 2.7, mg = 81.0, zn = 1.1, b12 = 0.1, fol = 24.0),
        f("Paneer, low fat", "100 g", 170.0, 24.0, 5.0, 6.0, "Dairy", aka = "Skimmed milk paneer", sat = 3.8, na = 40.0, k = 100.0, ca = 480.0, mg = 25.0, zn = 2.5, b12 = 0.5)
    )

    private fun fruit() = listOf(
        f("Nendran banana", "1 medium", 170.0, 1.8, 42.0, 0.3, "Fruit", aka = "Kerala banana, Ethapazham", fib = 3.0, sug = 25.0, na = 4.0, k = 520.0, ca = 8.0, fe = 0.6, mg = 40.0, zn = 0.2, va = 60.0, vc = 15.0, fol = 30.0),
        f("Poovan banana", "1 small", 75.0, 0.8, 18.0, 0.2, "Fruit", aka = "Small banana, Elakki", fib = 1.5, sug = 11.0, na = 1.0, k = 250.0, ca = 4.0, fe = 0.2, mg = 19.0, zn = 0.1, vc = 6.0, fol = 14.0),
        f("Red banana", "1 medium", 95.0, 1.3, 23.0, 0.3, "Fruit", aka = "Sevvazhai", fib = 2.6, sug = 14.0, na = 1.0, k = 360.0, ca = 5.0, fe = 0.3, mg = 27.0, zn = 0.2, va = 30.0, vc = 9.0, fol = 20.0),
        f("Rasthali banana", "1 small", 85.0, 0.9, 20.0, 0.3, "Fruit", fib = 1.8, sug = 13.0, na = 1.0, k = 290.0, ca = 4.0, fe = 0.2, mg = 22.0, zn = 0.1, vc = 7.0, fol = 16.0),
        f("Jamun", "100 g", 65.0, 0.7, 15.6, 0.2, "Fruit", aka = "Naval pazham, Java plum", fib = 0.6, sug = 12.0, na = 14.0, k = 79.0, ca = 19.0, fe = 0.2, mg = 15.0, vc = 14.0),
        f("Avocado, half", "half (100 g)", 160.0, 2.0, 8.5, 14.7, "Fruit", aka = "Butter fruit", fib = 6.7, sug = 0.7, sat = 2.1, na = 7.0, k = 485.0, ca = 12.0, fe = 0.6, mg = 29.0, zn = 0.6, va = 7.0, vc = 10.0, fol = 81.0),
        f("Dragon fruit", "100 g", 55.0, 1.2, 13.0, 0.4, "Fruit", aka = "Pitaya", fib = 3.0, sug = 8.0, k = 230.0, ca = 18.0, fe = 0.7, mg = 40.0, vc = 3.0),
        f("Cherries", "1 cup", 90.0, 1.5, 22.0, 0.3, "Fruit", fib = 2.9, sug = 18.0, k = 310.0, ca = 18.0, fe = 0.5, mg = 15.0, va = 4.0, vc = 10.0, fol = 6.0),
        f("Peach", "1 medium", 60.0, 1.4, 14.3, 0.4, "Fruit", fib = 2.3, sug = 12.6, k = 285.0, ca = 9.0, fe = 0.4, mg = 14.0, va = 24.0, vc = 10.0, fol = 6.0),
        f("Wood apple", "100 g", 125.0, 7.1, 18.1, 3.7, "Fruit", aka = "Vilampazham, Bael", fib = 5.0, sug = 8.0, k = 600.0, ca = 130.0, fe = 0.6, mg = 40.0, vc = 3.0),
        f("Amla, fresh", "2 pieces", 20.0, 0.4, 5.0, 0.3, "Fruit", aka = "Nellikai, Indian gooseberry", fib = 2.2, sug = 1.5, k = 100.0, ca = 12.0, fe = 0.2, mg = 5.0, vc = 300.0),
        f("Star fruit", "1 medium", 25.0, 0.9, 6.0, 0.3, "Fruit", aka = "Thambaratham, Carambola", fib = 2.5, sug = 3.6, na = 2.0, k = 120.0, ca = 3.0, fe = 0.1, mg = 9.0, vc = 31.0, fol = 11.0),
        f("Dried figs", "3 pieces", 65.0, 0.8, 16.0, 0.2, "Fruit", aka = "Anjeer", fib = 2.4, sug = 12.0, na = 2.0, k = 170.0, ca = 40.0, fe = 0.5, mg = 17.0, zn = 0.1),
        f("Dried apricots", "5 pieces", 90.0, 1.2, 22.0, 0.2, "Fruit", fib = 2.5, sug = 19.0, na = 3.0, k = 410.0, ca = 19.0, fe = 0.9, mg = 11.0, zn = 0.1, va = 63.0),
        f("Tender coconut flesh", "1 cup", 60.0, 1.3, 7.0, 3.2, "Fruit", aka = "Elaneer vazhukkai, Malai", fib = 2.0, sug = 4.0, sat = 2.8, na = 20.0, k = 250.0, ca = 10.0, fe = 0.8, mg = 20.0, zn = 0.3),
        f("Fresh coconut, grated", "2 tbsp", 55.0, 0.5, 2.3, 5.0, "Nuts", aka = "Thengai thuruval", fib = 1.4, sug = 1.0, sat = 4.4, na = 3.0, k = 53.0, ca = 2.0, fe = 0.4, mg = 5.0, zn = 0.2)
    )

    private fun vegetables() = listOf(
        f("Brinjal, cooked", "1 cup", 35.0, 0.8, 8.7, 0.2, "Vegetables", aka = "Kathirikai, Eggplant, Aubergine", fib = 2.5, sug = 3.2, na = 2.0, k = 120.0, ca = 6.0, fe = 0.3, mg = 11.0, zn = 0.1, vc = 1.0, fol = 14.0),
        f("Cabbage, raw", "1 cup shredded", 15.0, 0.9, 4.0, 0.1, "Vegetables", aka = "Muttaikose", fib = 1.8, sug = 2.2, na = 13.0, k = 120.0, ca = 28.0, fe = 0.3, mg = 8.0, zn = 0.1, vc = 26.0, fol = 30.0),
        f("French beans, cooked", "1 cup", 45.0, 2.4, 9.9, 0.4, "Vegetables", aka = "Beans, Green beans", fib = 4.0, sug = 1.8, na = 1.0, k = 180.0, ca = 55.0, fe = 0.8, mg = 22.0, zn = 0.3, va = 44.0, vc = 12.0, fol = 41.0),
        f("Pumpkin, cooked", "1 cup", 50.0, 1.8, 12.0, 0.2, "Vegetables", aka = "Parangikai, Yellow pumpkin", fib = 2.7, sug = 5.0, na = 2.0, k = 560.0, ca = 37.0, fe = 1.4, mg = 22.0, zn = 0.6, va = 1200.0, vc = 12.0, fol = 20.0),
        f("Raw banana, boiled", "100 g", 130.0, 1.3, 31.0, 0.4, "Vegetables", aka = "Vazhakkai, Plantain", fib = 2.3, sug = 6.0, na = 4.0, k = 465.0, ca = 3.0, fe = 0.6, mg = 32.0, zn = 0.1, va = 45.0, vc = 11.0, fol = 22.0),
        f("Elephant yam, cooked", "100 g", 100.0, 1.2, 25.0, 0.1, "Vegetables", aka = "Senai kizhangu, Suran", fib = 4.0, sug = 0.5, na = 8.0, k = 670.0, ca = 14.0, fe = 0.5, mg = 18.0, zn = 0.2, vc = 12.0, fol = 16.0),
        f("Colocasia, boiled", "100 g", 85.0, 1.5, 21.0, 0.1, "Vegetables", aka = "Seppankizhangu, Arbi, Taro", fib = 4.0, sug = 0.5, na = 11.0, k = 480.0, ca = 18.0, fe = 0.7, mg = 30.0, zn = 0.3, vc = 5.0, fol = 19.0),
        f("Tapioca, boiled", "100 g", 155.0, 1.4, 38.0, 0.3, "Vegetables", aka = "Maravalli kizhangu, Kappa, Cassava", fib = 1.8, sug = 1.7, na = 14.0, k = 270.0, ca = 16.0, fe = 0.3, mg = 21.0, zn = 0.3, vc = 20.0, fol = 27.0),
        f("Bitter gourd, cooked", "100 g", 20.0, 0.8, 4.3, 0.2, "Vegetables", aka = "Pavakkai, Karela", fib = 2.0, sug = 2.0, na = 6.0, k = 320.0, ca = 9.0, fe = 0.4, mg = 16.0, zn = 0.8, va = 6.0, vc = 33.0, fol = 51.0),
        f("Amaranth leaves, cooked", "1 cup", 30.0, 2.8, 5.4, 0.2, "Vegetables", aka = "Mulai keerai, Thandu keerai", fib = 2.5, na = 28.0, k = 840.0, ca = 275.0, fe = 3.0, mg = 73.0, zn = 1.1, va = 180.0, vc = 54.0, fol = 75.0),
        f("Corn on the cob, roasted", "1 cob", 105.0, 3.4, 21.0, 1.5, "Vegetables", aka = "Bhutta, Makka cholam", fib = 2.4, sug = 4.5, na = 120.0, k = 220.0, ca = 3.0, fe = 0.5, mg = 26.0, zn = 0.6, vc = 5.0, fol = 23.0),
        f("Cucumber, tomato and onion salad", "1 bowl", 30.0, 1.3, 7.0, 0.2, "Vegetables", aka = "Kachumber, Green salad", fib = 1.8, sug = 4.0, na = 150.0, k = 300.0, ca = 25.0, fe = 0.4, mg = 18.0, zn = 0.2, va = 40.0, vc = 18.0, fol = 22.0),
        f("Beetroot, raw", "1 medium", 35.0, 1.3, 8.0, 0.1, "Vegetables", fib = 2.2, sug = 5.5, na = 64.0, k = 266.0, ca = 13.0, fe = 0.7, mg = 19.0, zn = 0.3, vc = 4.0, fol = 89.0)
    )

    private fun dryStaples() = listOf(
        f("Rice, raw", "100 g", 340.0, 6.8, 78.0, 0.5, "Grains", aka = "Sona masoori raw, Arisi", fib = 1.3, na = 5.0, k = 115.0, ca = 10.0, fe = 0.7, mg = 25.0, zn = 1.1, fol = 8.0),
        f("Wheat flour (atta)", "100 g", 330.0, 12.0, 72.0, 1.7, "Grains", aka = "Godhumai maavu, Whole wheat flour", fib = 11.0, sug = 0.4, na = 2.0, k = 360.0, ca = 30.0, fe = 4.0, mg = 125.0, zn = 2.6, fol = 30.0),
        f("Ragi flour", "100 g", 305.0, 7.3, 72.0, 1.3, "Grains", aka = "Kezhvaragu maavu, Finger millet flour", fib = 11.0, sug = 0.6, na = 11.0, k = 410.0, ca = 344.0, fe = 3.9, mg = 137.0, zn = 2.3, fol = 18.0),
        f("Besan", "100 g", 360.0, 22.0, 58.0, 6.7, "Grains", aka = "Kadalai maavu, Gram flour", fib = 11.0, sug = 11.0, sat = 0.7, na = 64.0, k = 846.0, ca = 45.0, fe = 4.9, mg = 166.0, zn = 2.8, fol = 437.0),
        f("Rava, dry", "100 g", 345.0, 12.7, 73.0, 1.0, "Grains", aka = "Sooji, Semolina", fib = 3.9, na = 1.0, k = 186.0, ca = 17.0, fe = 1.2, mg = 47.0, zn = 1.1, fol = 72.0),
        f("Vermicelli, dry", "100 g", 345.0, 10.0, 75.0, 1.3, "Grains", aka = "Semiya", fib = 3.0, sug = 2.0, na = 6.0, k = 160.0, ca = 20.0, fe = 1.4, mg = 30.0, zn = 0.9),
        f("Poha, dry", "100 g", 340.0, 6.6, 77.0, 1.2, "Grains", aka = "Aval, Flattened rice", fib = 2.5, na = 5.0, k = 130.0, ca = 20.0, fe = 8.0, mg = 40.0, zn = 1.0),
        f("Idli dosa batter", "100 g", 90.0, 3.9, 19.0, 0.3, "Grains", aka = "Maavu, Fermented batter", fib = 1.0, na = 230.0, k = 60.0, ca = 10.0, fe = 0.6, mg = 15.0, zn = 0.4, fol = 10.0),
        f("Phulka", "1 phulka", 70.0, 2.7, 15.0, 0.4, "Tiffin", aka = "Thin roti without oil, Chapati no oil", fib = 2.4, na = 60.0, k = 60.0, ca = 8.0, fe = 0.8, mg = 25.0, zn = 0.5, fol = 6.0),
        f("Overnight oats with milk and banana", "1 jar", 295.0, 11.0, 50.0, 7.0, "Continental", aka = "Oats with milk", fib = 5.5, sug = 18.0, sat = 3.0, na = 70.0, k = 600.0, ca = 200.0, fe = 2.0, mg = 95.0, zn = 2.0, va = 60.0, vc = 5.0, b12 = 0.6, fol = 30.0),
        f("Peanut butter toast", "2 slices", 230.0, 10.5, 27.0, 10.0, "Continental", aka = "Brown bread with peanut butter", fib = 4.5, sug = 5.0, sat = 2.0, na = 380.0, k = 260.0, ca = 60.0, fe = 1.8, mg = 70.0, zn = 1.3, fol = 40.0)
    )

    private fun legumes() = listOf(
        f("Chickpeas, boiled", "1 cup", 250.0, 14.5, 45.0, 4.2, "Legumes", aka = "Vellai kondakadalai, Kabuli chana", fib = 12.5, sug = 8.0, sat = 0.4, na = 11.0, k = 477.0, ca = 80.0, fe = 4.7, mg = 79.0, zn = 2.5, vc = 2.0, fol = 282.0),
        f("Black chana, boiled", "1 cup", 240.0, 14.0, 44.0, 3.5, "Legumes", aka = "Karuppu kondakadalai, Kala chana", fib = 13.0, sug = 4.0, sat = 0.4, na = 20.0, k = 470.0, ca = 100.0, fe = 5.0, mg = 75.0, zn = 2.4, fol = 180.0),
        f("Rajma, boiled", "1 cup", 205.0, 15.3, 40.0, 0.9, "Legumes", aka = "Kidney beans", fib = 11.0, sug = 0.6, na = 4.0, k = 713.0, ca = 50.0, fe = 5.2, mg = 74.0, zn = 1.9, fol = 230.0),
        f("Horse gram, boiled", "1 cup", 170.0, 13.0, 33.0, 0.5, "Legumes", aka = "Kollu", fib = 8.0, na = 10.0, k = 450.0, ca = 170.0, fe = 4.5, mg = 90.0, zn = 1.6, fol = 60.0),
        f("Green gram, boiled", "1 cup", 190.0, 14.0, 39.0, 0.8, "Legumes", aka = "Pachai payaru, Whole moong", fib = 15.0, sug = 4.0, na = 4.0, k = 537.0, ca = 55.0, fe = 2.8, mg = 97.0, zn = 1.7, vc = 2.0, fol = 321.0),
        f("Cowpea, boiled", "1 cup", 180.0, 13.0, 36.0, 0.9, "Legumes", aka = "Karamani, Lobia, Black eyed peas", fib = 11.0, sug = 5.5, na = 7.0, k = 475.0, ca = 41.0, fe = 4.3, mg = 91.0, zn = 2.2, fol = 358.0),
        f("Masoor dal, cooked", "1 cup", 210.0, 18.0, 40.0, 0.8, "Legumes", aka = "Red lentils", fib = 15.6, sug = 3.6, na = 4.0, k = 731.0, ca = 38.0, fe = 6.6, mg = 71.0, zn = 2.5, vc = 3.0, fol = 358.0),
        f("Chana dal, cooked", "1 cup", 185.0, 14.0, 34.0, 1.5, "Legumes", aka = "Kadalai paruppu, Bengal gram dal", fib = 10.0, sug = 2.0, na = 8.0, k = 420.0, ca = 40.0, fe = 3.0, mg = 70.0, zn = 2.0, fol = 150.0),
        f("Soy milk, unsweetened", "1 glass (250 ml)", 75.0, 7.0, 3.0, 4.0, "Drinks", aka = "Soya milk", fib = 1.2, sug = 1.0, sat = 0.5, na = 90.0, k = 300.0, ca = 300.0, fe = 1.0, mg = 40.0, zn = 0.6, vd = 2.5, b12 = 1.0)
    )

    private fun snacks() = listOf(
        f("Roasted gram", "30 g", 100.0, 6.7, 17.5, 1.6, "Snacks", aka = "Pottukadalai, Dalia, Roasted chana", fib = 5.0, sug = 1.0, na = 7.0, k = 250.0, ca = 17.0, fe = 2.8, mg = 50.0, zn = 1.0, fol = 60.0),
        f("Roasted makhana", "30 g", 125.0, 2.9, 23.0, 2.5, "Snacks", aka = "Fox nuts, Lotus seeds", fib = 1.5, sat = 1.4, na = 120.0, k = 150.0, ca = 18.0, fe = 0.4, mg = 20.0, zn = 0.4),
        f("Boiled peanuts", "1/2 cup shelled", 160.0, 6.8, 10.6, 11.0, "Snacks", aka = "Avicha verkadalai", fib = 4.4, sug = 1.0, sat = 1.5, na = 380.0, k = 90.0, ca = 27.0, fe = 0.5, mg = 51.0, zn = 0.9, fol = 37.0),
        f("Onion pakoda", "100 g", 310.0, 6.0, 32.0, 18.0, "Snacks", aka = "Vengaya pakoda, Onion pakora", fib = 3.0, sug = 4.0, sat = 2.5, na = 480.0, k = 230.0, ca = 40.0, fe = 1.8, mg = 40.0, zn = 0.8, vc = 3.0, fol = 40.0),
        f("Kara boondi", "30 g", 145.0, 3.0, 14.0, 9.0, "Snacks", aka = "Masala boondi", fib = 1.5, sat = 1.4, na = 210.0, k = 110.0, ca = 12.0, fe = 0.9, mg = 24.0, zn = 0.5),
        f("Popcorn, home made", "3 cups", 130.0, 3.0, 19.0, 5.5, "Snacks", fib = 3.6, sat = 0.8, na = 180.0, k = 80.0, ca = 2.0, fe = 0.8, mg = 36.0, zn = 0.8),
        f("Potato chips", "1 small packet (30 g)", 160.0, 2.0, 15.0, 10.5, "Snacks", aka = "Wafers, Crisps", fib = 1.2, sug = 0.2, sat = 4.0, na = 160.0, k = 400.0, ca = 7.0, fe = 0.4, mg = 20.0, zn = 0.3, vc = 6.0),
        f("Veg puff", "1 puff", 260.0, 4.5, 30.0, 14.0, "Snacks", aka = "Bakery puff", fib = 1.5, sug = 2.5, sat = 7.0, na = 420.0, k = 150.0, ca = 20.0, fe = 1.2, mg = 14.0, zn = 0.4),
        f("Bread omelette", "1 plate", 295.0, 17.0, 26.0, 14.0, "Snacks", aka = "Egg bread omelette", fib = 1.5, sug = 3.0, sat = 4.0, na = 640.0, k = 230.0, ca = 90.0, fe = 2.4, mg = 26.0, zn = 1.6, va = 180.0, vd = 2.0, b12 = 1.0, fol = 60.0),
        f("Rusk", "2 pieces", 80.0, 2.4, 15.0, 1.5, "Snacks", aka = "Toast biscuit", fib = 0.4, sug = 3.0, sat = 0.7, na = 60.0, k = 30.0, ca = 8.0, fe = 0.5, mg = 6.0, zn = 0.2),
        f("Cream biscuits", "2 biscuits", 120.0, 1.4, 17.5, 5.0, "Snacks", aka = "Sandwich biscuits", sug = 8.0, sat = 2.5, na = 60.0, k = 25.0, ca = 8.0, fe = 0.6, mg = 5.0, zn = 0.1),
        f("Digestive biscuits", "2 biscuits", 140.0, 2.0, 19.5, 6.5, "Snacks", aka = "Wheat biscuits", fib = 1.0, sug = 5.0, sat = 3.0, na = 170.0, k = 60.0, ca = 12.0, fe = 0.8, mg = 10.0, zn = 0.2),
        f("Sweet bun", "1 bun", 185.0, 5.0, 33.0, 4.0, "Snacks", aka = "Bakery bun", fib = 1.0, sug = 8.0, sat = 1.8, na = 220.0, k = 60.0, ca = 30.0, fe = 1.2, mg = 12.0, zn = 0.4),
        f("Chicken nuggets", "6 pieces", 285.0, 15.0, 16.0, 18.0, "Continental", fib = 1.0, sat = 3.5, na = 600.0, k = 250.0, ca = 15.0, fe = 0.9, mg = 22.0, zn = 0.8, b12 = 0.2)
    )

    private fun sweets() = listOf(
        f("Jalebi", "2 pieces", 200.0, 1.2, 32.0, 7.5, "Sweets", aka = "Jilebi", sug = 22.0, sat = 2.0, na = 10.0, k = 15.0, ca = 6.0, fe = 0.3),
        f("Boondi laddu", "1 laddu", 170.0, 2.2, 24.0, 7.5, "Sweets", aka = "Boondi laddoo", fib = 0.8, sug = 16.0, sat = 3.5, na = 20.0, k = 60.0, ca = 10.0, fe = 0.6, mg = 10.0, zn = 0.2),
        f("Soan papdi", "1 piece", 145.0, 1.5, 18.0, 7.5, "Sweets", aka = "Son papdi", sug = 12.0, sat = 4.0, na = 10.0, k = 30.0, ca = 8.0, fe = 0.4),
        f("Milk peda", "1 peda", 80.0, 1.8, 12.0, 2.5, "Sweets", aka = "Paal peda", sug = 11.0, sat = 1.5, na = 20.0, k = 70.0, ca = 60.0, mg = 6.0, zn = 0.2),
        f("Peanut chikki", "1 piece", 130.0, 4.5, 13.0, 7.0, "Sweets", aka = "Kadalai mittai", fib = 1.2, sug = 10.0, sat = 1.0, na = 10.0, k = 140.0, ca = 20.0, fe = 0.9, mg = 35.0, zn = 0.6, fol = 25.0),
        f("Milk chocolate", "25 g", 135.0, 1.9, 15.0, 7.5, "Sweets", aka = "Chocolate bar", fib = 0.8, sug = 13.0, sat = 4.5, na = 20.0, k = 95.0, ca = 50.0, fe = 0.6, mg = 16.0, zn = 0.6),
        f("Plain cake", "1 slice", 230.0, 3.0, 32.0, 10.0, "Sweets", aka = "Sponge cake, Tea cake", fib = 0.5, sug = 20.0, sat = 3.0, na = 200.0, k = 60.0, ca = 40.0, fe = 0.8, mg = 8.0, zn = 0.3),
        f("Honey", "1 tbsp", 70.0, 0.1, 17.0, 0.0, "Sweets", aka = "Then", sug = 17.0, na = 1.0, k = 11.0, ca = 1.0, fe = 0.1),
        f("Jaggery", "1 tbsp", 60.0, 0.1, 14.6, 0.0, "Sweets", aka = "Vellam, Gur", sug = 13.0, na = 4.0, k = 160.0, ca = 12.0, fe = 0.5, mg = 10.0),
        f("Sugar", "1 tsp", 20.0, 0.0, 5.0, 0.0, "Sweets", aka = "Sakkarai", sug = 5.0)
    )

    private fun dairyAndDrinks() = listOf(
        f("Water", "1 glass (250 ml)", 0.0, 0.0, 0.0, 0.0, "Drinks", aka = "Thanni, Drinking water"),
        f("Turmeric milk", "1 glass", 140.0, 6.2, 15.0, 6.0, "Drinks", aka = "Manjal paal, Haldi doodh", sug = 15.0, sat = 3.7, na = 90.0, k = 300.0, ca = 240.0, fe = 0.3, mg = 22.0, zn = 0.8, va = 60.0, b12 = 0.8),
        f("Protein milk, bottled", "1 bottle (200 ml)", 130.0, 15.0, 12.0, 2.5, "Dairy", aka = "High protein milkshake", sug = 8.0, sat = 1.6, na = 150.0, k = 380.0, ca = 400.0, fe = 0.2, mg = 30.0, zn = 1.0, b12 = 0.8),
        f("Fresh cream", "1 tbsp", 35.0, 0.3, 0.5, 3.7, "Dairy", aka = "Malai", sug = 0.5, sat = 2.3, na = 4.0, k = 10.0, ca = 10.0, va = 35.0),
        f("Condensed milk", "1 tbsp", 65.0, 1.6, 11.0, 1.7, "Dairy", aka = "Milkmaid", sug = 11.0, sat = 1.1, na = 25.0, k = 74.0, ca = 57.0, mg = 5.0, zn = 0.2),
        f("Hung curd", "100 g", 85.0, 7.0, 5.0, 4.0, "Dairy", aka = "Thick curd", sug = 4.0, sat = 2.6, na = 40.0, k = 180.0, ca = 150.0, mg = 14.0, zn = 0.7, b12 = 0.5)
    )
}
