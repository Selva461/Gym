package dev.avles.gym.data

/**
 * The second half of the food catalogue.
 *
 * Split from [SeedData] because that file reached the 800-line ceiling
 * `ArchitectureTest` enforces, which is the ceiling doing its job: a catalogue
 * grows forever and a file should not. New foods go here, or in a third file when
 * this one fills. [SeedData.foods] concatenates them, and that is the only place
 * that needs to know this file exists.
 *
 * Juices get their own category rather than being filed under Drinks. A glass of
 * sugarcane juice is 180 kcal of almost pure sugar and a glass of tender coconut
 * is 46; burying both among the teas and coffees makes the one people drink for
 * health indistinguishable from the one that is a dessert. Filed apart, the list
 * itself makes the point.
 *
 * Every value is an estimate for a home or street-stall portion. See the note in
 * README about what that means and why editing the five you actually eat is worth
 * more than any number of entries.
 */
internal object SeedFoodsMore {

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

    fun all(): List<FoodItem> =
        freshJuices() + blendedJuices() + packagedDrinks() +
            moreSouthIndianTiffin() + moreSouthIndianRice() + moreSouthIndianSides() +
            moreNorthIndianMains() + moreNorthIndianBreads() + streetFood() +
            moreSouthIndianNonVeg() + moreSweets() + moreStaples()

    // ---- Juices: pressed, nothing added ------------------------------------

    private fun freshJuices() = listOf(
        f("Sugarcane juice with ginger and lime", "1 glass (250 ml)", 188.0, 0.5, 46.0, 0.2, "Juices", sug = 43.0, na = 32.0, k = 165.0, ca = 32.0, fe = 1.0, mg = 26.0, zn = 0.2, vc = 12.0),
        f("Mosambi (sweet lime) juice", "1 glass (250 ml)", 105.0, 1.5, 25.0, 0.3, "Juices", fib = 0.5, sug = 21.0, na = 5.0, k = 420.0, ca = 40.0, fe = 0.4, mg = 22.0, zn = 0.1, vc = 90.0, fol = 40.0),
        f("Pomegranate juice", "1 glass (250 ml)", 134.0, 0.4, 33.0, 0.3, "Juices", sug = 31.0, na = 22.0, k = 533.0, ca = 27.0, fe = 0.2, mg = 17.0, zn = 0.2, vc = 0.3, fol = 60.0),
        f("Watermelon juice", "1 glass (250 ml)", 80.0, 1.6, 19.0, 0.4, "Juices", sug = 16.0, na = 5.0, k = 300.0, ca = 20.0, fe = 0.5, mg = 25.0, zn = 0.2, va = 70.0, vc = 20.0),
        f("Pineapple juice", "1 glass (250 ml)", 132.0, 0.9, 32.0, 0.3, "Juices", fib = 0.5, sug = 25.0, na = 3.0, k = 325.0, ca = 32.0, fe = 0.8, mg = 30.0, zn = 0.3, vc = 64.0, fol = 45.0),
        f("Grape juice", "1 glass (250 ml)", 152.0, 0.9, 37.0, 0.3, "Juices", sug = 36.0, na = 13.0, k = 263.0, ca = 28.0, fe = 0.6, mg = 25.0, zn = 0.1, vc = 0.3),
        f("Apple juice", "1 glass (250 ml)", 114.0, 0.2, 28.0, 0.3, "Juices", fib = 0.5, sug = 24.0, na = 10.0, k = 250.0, ca = 20.0, fe = 0.5, mg = 12.0, vc = 2.0),
        f("Carrot juice", "1 glass (250 ml)", 94.0, 2.2, 22.0, 0.4, "Juices", fib = 1.9, sug = 9.0, na = 68.0, k = 690.0, ca = 57.0, fe = 1.1, mg = 36.0, zn = 0.4, va = 2256.0, vc = 20.0, fol = 9.0),
        f("Beetroot juice", "1 glass (250 ml)", 100.0, 2.5, 23.0, 0.3, "Juices", fib = 1.0, sug = 19.0, na = 190.0, k = 660.0, ca = 30.0, fe = 1.0, mg = 40.0, zn = 0.4, vc = 8.0, fol = 135.0),
        f("Amla (gooseberry) juice", "60 ml shot", 30.0, 0.3, 7.0, 0.1, "Juices", fib = 1.2, sug = 4.0, na = 4.0, k = 120.0, ca = 15.0, fe = 0.6, mg = 6.0, vc = 300.0),
        f("Ash gourd (poosanikai) juice", "1 glass (250 ml)", 40.0, 1.0, 9.0, 0.2, "Juices", fib = 1.5, sug = 5.0, na = 15.0, k = 250.0, ca = 45.0, fe = 0.7, mg = 20.0, vc = 25.0),
        f("Wheatgrass shot", "30 ml", 15.0, 1.0, 2.0, 0.1, "Juices", fib = 0.5, na = 5.0, k = 45.0, ca = 8.0, fe = 0.6, mg = 8.0, vc = 3.0),
        f("Orange and carrot juice", "1 glass (250 ml)", 110.0, 1.8, 25.0, 0.4, "Juices", fib = 1.2, sug = 18.0, na = 35.0, k = 600.0, ca = 45.0, fe = 0.8, mg = 30.0, zn = 0.2, va = 1100.0, vc = 80.0, fol = 45.0),
        f("Lemon and mint juice", "1 glass (250 ml)", 60.0, 0.3, 15.0, 0.1, "Juices", sug = 13.0, na = 10.0, k = 90.0, ca = 15.0, fe = 0.3, mg = 8.0, vc = 30.0),
        f("Cucumber and lime cooler", "1 glass (250 ml)", 45.0, 0.8, 10.0, 0.2, "Juices", fib = 0.8, sug = 6.0, na = 8.0, k = 200.0, ca = 20.0, fe = 0.3, mg = 15.0, vc = 12.0)
    )

    // ---- Juices: blended, with milk, curd or pulp --------------------------

    private fun blendedJuices() = listOf(
        f("Mango milkshake", "1 glass (300 ml)", 285.0, 7.5, 48.0, 7.5, "Juices", fib = 1.8, sug = 44.0, sat = 4.5, na = 80.0, k = 480.0, ca = 240.0, fe = 0.4, mg = 40.0, zn = 0.9, va = 190.0, vc = 30.0, vd = 1.2, b12 = 0.9, fol = 30.0),
        f("Banana milkshake", "1 glass (300 ml)", 265.0, 8.0, 44.0, 7.0, "Juices", fib = 2.4, sug = 36.0, sat = 4.2, na = 85.0, k = 620.0, ca = 250.0, fe = 0.5, mg = 55.0, zn = 1.0, vc = 9.0, vd = 1.2, b12 = 0.9, fol = 30.0),
        f("Chikoo (sapota) milkshake", "1 glass (300 ml)", 290.0, 7.0, 50.0, 7.5, "Juices", fib = 4.0, sug = 42.0, sat = 4.5, na = 80.0, k = 420.0, ca = 240.0, fe = 1.0, mg = 40.0, zn = 0.8, vc = 16.0, vd = 1.2, b12 = 0.9),
        f("Avocado milkshake", "1 glass (300 ml)", 330.0, 8.0, 32.0, 19.0, "Juices", fib = 5.0, sug = 24.0, sat = 6.0, na = 80.0, k = 700.0, ca = 250.0, fe = 0.8, mg = 55.0, zn = 1.0, vc = 12.0, vd = 1.2, b12 = 0.9, fol = 80.0),
        f("Muskmelon juice", "1 glass (250 ml)", 85.0, 2.0, 20.0, 0.4, "Juices", fib = 1.2, sug = 18.0, na = 30.0, k = 480.0, ca = 20.0, fe = 0.5, mg = 30.0, zn = 0.3, va = 480.0, vc = 55.0, fol = 45.0),
        f("Mixed fruit juice", "1 glass (250 ml)", 125.0, 1.2, 30.0, 0.4, "Juices", fib = 1.0, sug = 26.0, na = 12.0, k = 400.0, ca = 30.0, fe = 0.6, mg = 25.0, zn = 0.2, va = 120.0, vc = 55.0, fol = 40.0),
        f("Mango lassi", "1 glass (300 ml)", 295.0, 8.5, 50.0, 7.0, "Juices", fib = 1.5, sug = 46.0, sat = 4.3, na = 95.0, k = 480.0, ca = 280.0, fe = 0.3, mg = 35.0, zn = 1.1, va = 180.0, vc = 25.0, vd = 0.3, b12 = 0.8, fol = 25.0),
        f("Cold-pressed green juice", "1 glass (250 ml)", 72.0, 2.5, 15.0, 0.5, "Juices", fib = 2.0, sug = 9.0, na = 60.0, k = 620.0, ca = 90.0, fe = 1.8, mg = 45.0, zn = 0.4, va = 700.0, vc = 60.0, fol = 110.0)
    )

    // ---- Packaged and street drinks ---------------------------------------

    private fun packagedDrinks() = listOf(
        f("Packaged mango drink (Maaza, Frooti)", "200 ml", 130.0, 0.2, 32.0, 0.1, "Drinks", sug = 31.0, na = 15.0, k = 60.0, ca = 8.0, vc = 4.0),
        f("Packaged orange drink", "200 ml", 110.0, 0.2, 27.0, 0.1, "Drinks", sug = 26.0, na = 20.0, k = 50.0, vc = 12.0),
        f("Bottled buttermilk (chaas)", "200 ml", 60.0, 3.2, 6.0, 2.2, "Drinks", sug = 5.0, sat = 1.4, na = 310.0, k = 220.0, ca = 120.0, fe = 0.1, mg = 18.0, zn = 0.5, b12 = 0.4),
        f("Boost or Horlicks with milk", "1 glass (250 ml)", 205.0, 8.0, 30.0, 5.5, "Drinks", sug = 22.0, sat = 3.3, na = 130.0, k = 380.0, ca = 320.0, fe = 4.5, mg = 45.0, zn = 2.0, va = 200.0, vc = 18.0, vd = 2.5, b12 = 1.0, fol = 60.0),
        f("Iced tea, sweetened", "300 ml", 105.0, 0.0, 26.0, 0.0, "Drinks", sug = 26.0, na = 10.0, k = 30.0),
        f("Cold coffee with ice cream", "1 glass (300 ml)", 320.0, 8.0, 42.0, 13.0, "Drinks", sug = 38.0, sat = 8.0, na = 120.0, k = 400.0, ca = 280.0, mg = 35.0, zn = 1.0, va = 90.0, vd = 1.4, b12 = 1.0),
        f("Diet cola", "330 ml", 2.0, 0.0, 0.0, 0.0, "Drinks", na = 40.0, k = 10.0),
        f("Electrolyte drink (ORS)", "500 ml", 90.0, 0.0, 22.0, 0.0, "Drinks", sug = 22.0, na = 1040.0, k = 780.0, ca = 5.0, mg = 5.0),
        f("Sparkling water", "330 ml", 0.0, 0.0, 0.0, 0.0, "Drinks", na = 12.0, k = 2.0, ca = 10.0, mg = 3.0)
    )

    // ---- More South Indian tiffin ------------------------------------------

    private fun moreSouthIndianTiffin() = listOf(
        f("Jowar dosa", "1 dosa", 110.0, 3.0, 20.0, 2.2, "Dosa", fib = 2.4, sat = 0.9, na = 205.0, k = 125.0, ca = 20.0, fe = 1.6, mg = 45.0, zn = 0.8, fol = 14.0),
        f("Moong dal dosa (pesarattu upma)", "1 plate", 295.0, 12.0, 42.0, 9.0, "Tiffin", fib = 6.0, sug = 1.5, sat = 3.5, na = 420.0, k = 420.0, ca = 60.0, fe = 3.2, mg = 80.0, zn = 1.5, fol = 130.0),
        f("Appam with stew", "2 appam with stew", 380.0, 11.0, 52.0, 14.0, "Tiffin", fib = 3.0, sug = 5.0, sat = 8.0, na = 560.0, k = 450.0, ca = 90.0, fe = 2.0, mg = 50.0, zn = 1.3, va = 120.0, vc = 10.0),
        f("Puttu with kadala curry", "1 plate", 420.0, 14.0, 66.0, 11.0, "Tiffin", fib = 9.0, sug = 4.0, sat = 6.0, na = 520.0, k = 560.0, ca = 90.0, fe = 3.6, mg = 90.0, zn = 2.0, fol = 140.0),
        f("Kothu parotta", "1 plate", 560.0, 18.0, 62.0, 26.0, "Tiffin", fib = 3.5, sug = 4.0, sat = 12.0, na = 980.0, k = 400.0, ca = 90.0, fe = 3.5, mg = 55.0, zn = 2.2, va = 90.0, vc = 8.0, b12 = 0.6),
        f("Parotta, plain", "1 parotta", 250.0, 5.0, 34.0, 10.0, "Tiffin", fib = 1.4, sug = 1.0, sat = 5.0, na = 330.0, k = 70.0, ca = 20.0, fe = 1.4, mg = 18.0, zn = 0.6),
        f("Veg kurma", "1 cup", 185.0, 4.5, 14.0, 12.0, "Sides", fib = 3.5, sug = 4.0, sat = 6.5, na = 480.0, k = 350.0, ca = 70.0, fe = 1.6, mg = 40.0, zn = 0.8, va = 210.0, vc = 18.0),
        f("Rava kichadi", "1 cup", 245.0, 6.0, 36.0, 8.5, "Tiffin", fib = 2.5, sug = 3.0, sat = 4.0, na = 460.0, k = 220.0, ca = 45.0, fe = 1.5, mg = 35.0, zn = 0.9, va = 130.0, vc = 12.0),
        f("Aval (poha) upma", "1 cup", 215.0, 4.5, 38.0, 5.5, "Tiffin", fib = 2.0, sug = 2.0, sat = 2.5, na = 380.0, k = 160.0, ca = 25.0, fe = 2.8, mg = 30.0, zn = 0.8),
        f("Keerai vadai", "2 pieces", 170.0, 6.0, 17.0, 8.5, "Tiffin", fib = 4.2, sat = 2.8, na = 330.0, k = 300.0, ca = 90.0, fe = 2.6, mg = 50.0, zn = 1.0, va = 320.0, vc = 12.0, fol = 110.0)
    )

    // ---- More South Indian rice --------------------------------------------

    private fun moreSouthIndianRice() = listOf(
        f("Mushroom biryani", "1 plate", 520.0, 14.0, 74.0, 18.0, "Rice", fib = 4.0, sug = 4.0, sat = 8.0, na = 820.0, k = 520.0, ca = 70.0, fe = 3.0, mg = 70.0, zn = 1.8, va = 110.0, vc = 8.0, vd = 0.4),
        f("Paneer biryani", "1 plate", 610.0, 22.0, 72.0, 26.0, "Rice", fib = 3.5, sug = 5.0, sat = 13.0, na = 850.0, k = 450.0, ca = 380.0, fe = 2.8, mg = 60.0, zn = 2.2, va = 190.0, vc = 8.0, b12 = 0.8),
        f("Bisi bele bath", "1 cup", 340.0, 11.0, 52.0, 10.0, "Rice", fib = 6.0, sug = 4.0, sat = 4.5, na = 720.0, k = 480.0, ca = 70.0, fe = 3.0, mg = 70.0, zn = 1.6, va = 180.0, vc = 14.0, fol = 90.0),
        f("Mint (pudina) rice", "1 cup", 285.0, 6.0, 46.0, 8.5, "Rice", fib = 2.4, sug = 2.0, sat = 3.5, na = 420.0, k = 220.0, ca = 60.0, fe = 2.2, mg = 40.0, zn = 0.9, va = 200.0, vc = 10.0),
        f("Curd rice with pomegranate", "1 cup", 240.0, 8.0, 38.0, 6.0, "Rice", fib = 1.5, sug = 8.0, sat = 3.5, na = 380.0, k = 320.0, ca = 200.0, fe = 0.8, mg = 30.0, zn = 1.0, b12 = 0.5),
        f("Brown rice, cooked", "1 cup", 215.0, 5.0, 45.0, 1.8, "Rice", fib = 3.5, sug = 0.7, sat = 0.4, na = 10.0, k = 150.0, ca = 20.0, fe = 1.0, mg = 84.0, zn = 1.2, fol = 18.0)
    )

    // ---- More South Indian sides -------------------------------------------

    private fun moreSouthIndianSides() = listOf(
        f("Milagu kuzhambu", "1 cup", 125.0, 3.0, 12.0, 7.5, "Sides", fib = 3.0, sug = 2.0, sat = 2.6, na = 600.0, k = 300.0, ca = 55.0, fe = 2.0, mg = 40.0, zn = 0.7),
        f("Pitlai", "1 cup", 175.0, 7.0, 20.0, 8.0, "Sides", fib = 5.5, sug = 3.0, sat = 3.5, na = 540.0, k = 420.0, ca = 70.0, fe = 2.4, mg = 55.0, zn = 1.2, vc = 12.0, fol = 100.0),
        f("Olan", "1 cup", 150.0, 3.0, 12.0, 10.0, "Sides", fib = 3.0, sug = 4.0, sat = 7.5, na = 340.0, k = 380.0, ca = 45.0, fe = 1.0, mg = 35.0, zn = 0.6, va = 180.0, vc = 12.0),
        f("Thoran (cabbage and coconut)", "1 cup", 115.0, 3.0, 11.0, 7.0, "Sides", fib = 4.0, sug = 4.0, sat = 5.0, na = 300.0, k = 280.0, ca = 60.0, fe = 1.0, mg = 25.0, zn = 0.5, va = 60.0, vc = 40.0, fol = 50.0),
        f("Beans usili", "1 cup", 190.0, 9.0, 18.0, 9.0, "Sides", fib = 7.0, sug = 3.0, sat = 3.0, na = 420.0, k = 400.0, ca = 80.0, fe = 2.6, mg = 60.0, zn = 1.3, va = 90.0, vc = 14.0, fol = 140.0),
        f("Vendakkai (okra) poriyal", "1 cup", 125.0, 3.0, 12.0, 7.5, "Sides", fib = 4.5, sug = 3.0, sat = 4.0, na = 320.0, k = 350.0, ca = 90.0, fe = 1.0, mg = 55.0, zn = 0.7, va = 60.0, vc = 22.0, fol = 70.0),
        f("Cabbage kootu", "1 cup", 165.0, 7.0, 20.0, 6.5, "Sides", fib = 6.0, sug = 4.0, sat = 3.0, na = 420.0, k = 380.0, ca = 70.0, fe = 2.2, mg = 50.0, zn = 1.1, vc = 30.0, fol = 110.0),
        f("Vathal kuzhambu with sundakkai", "1 cup", 130.0, 3.0, 13.0, 7.5, "Sides", fib = 3.5, sug = 3.0, sat = 2.6, na = 680.0, k = 340.0, ca = 60.0, fe = 2.2, mg = 40.0, zn = 0.7, vc = 8.0),
        f("Tomato thokku", "2 tbsp", 85.0, 1.0, 7.0, 6.0, "Sides", fib = 1.5, sug = 4.0, sat = 1.4, na = 420.0, k = 220.0, ca = 20.0, fe = 0.8, mg = 15.0, zn = 0.2, va = 150.0, vc = 14.0),
        f("Coconut chutney with roasted gram", "2 tbsp", 95.0, 2.2, 4.0, 8.0, "Sides", fib = 2.2, sug = 1.5, sat = 6.5, na = 180.0, k = 110.0, ca = 20.0, fe = 0.7, mg = 20.0, zn = 0.4),
        f("Tomato onion chutney", "2 tbsp", 60.0, 1.2, 6.0, 3.5, "Sides", fib = 1.5, sug = 3.0, sat = 0.8, na = 220.0, k = 180.0, ca = 18.0, fe = 0.7, mg = 12.0, zn = 0.2, va = 90.0, vc = 12.0)
    )

    // ---- North Indian mains -------------------------------------------------

    private fun moreNorthIndianMains() = listOf(
        f("Chole masala", "1 cup", 285.0, 12.0, 42.0, 8.0, "North Indian", fib = 12.0, sug = 6.0, sat = 2.6, na = 680.0, k = 620.0, ca = 90.0, fe = 3.4, mg = 85.0, zn = 2.0, va = 100.0, vc = 10.0, fol = 180.0),
        f("Chicken tikka masala", "1 cup", 395.0, 30.0, 13.0, 25.0, "North Indian", fib = 2.0, sug = 7.0, sat = 10.0, na = 860.0, k = 560.0, ca = 110.0, fe = 2.4, mg = 50.0, zn = 2.6, va = 200.0, vc = 10.0, b12 = 0.9),
        f("Mutton rogan josh", "1 cup", 410.0, 28.0, 10.0, 28.0, "North Indian", fib = 1.8, sug = 4.0, sat = 11.0, na = 780.0, k = 520.0, ca = 60.0, fe = 3.6, mg = 45.0, zn = 4.5, va = 130.0, vc = 6.0, b12 = 2.4),
        f("Kadhi pakora", "1 cup", 250.0, 9.0, 24.0, 13.0, "North Indian", fib = 3.0, sug = 6.0, sat = 4.5, na = 680.0, k = 380.0, ca = 240.0, fe = 1.8, mg = 45.0, zn = 1.2, b12 = 0.4, fol = 70.0),
        f("Egg curry, North Indian", "1 cup", 285.0, 16.0, 12.0, 20.0, "North Indian", fib = 2.0, sug = 5.0, sat = 6.5, na = 620.0, k = 380.0, ca = 90.0, fe = 2.6, mg = 35.0, zn = 1.6, va = 220.0, vc = 8.0, vd = 2.2, b12 = 1.1, fol = 60.0)
    )

    // ---- North Indian breads and rice --------------------------------------

    private fun moreNorthIndianBreads() = listOf(
        f("Garlic naan", "1 naan", 300.0, 8.5, 47.0, 9.0, "North Indian", fib = 2.2, sug = 3.0, sat = 4.6, na = 520.0, k = 140.0, ca = 75.0, fe = 2.3, mg = 28.0, zn = 0.9, va = 70.0),
        f("Lachha paratha", "1 paratha", 265.0, 6.0, 38.0, 10.0, "North Indian", fib = 3.0, sug = 1.0, sat = 5.0, na = 340.0, k = 110.0, ca = 30.0, fe = 1.8, mg = 35.0, zn = 0.9),
        f("Aloo paratha with butter", "1 paratha", 330.0, 7.0, 46.0, 13.0, "North Indian", fib = 4.0, sug = 2.0, sat = 6.0, na = 480.0, k = 420.0, ca = 45.0, fe = 2.2, mg = 45.0, zn = 1.0, va = 80.0, vc = 12.0),
        f("Paneer paratha", "1 paratha", 360.0, 13.0, 42.0, 16.0, "North Indian", fib = 3.5, sug = 2.0, sat = 8.0, na = 500.0, k = 220.0, ca = 300.0, fe = 2.0, mg = 40.0, zn = 1.4, va = 120.0, b12 = 0.5),
        f("Missi roti", "1 roti", 155.0, 6.0, 25.0, 3.5, "North Indian", fib = 5.0, sug = 1.0, sat = 0.6, na = 280.0, k = 250.0, ca = 35.0, fe = 2.2, mg = 55.0, zn = 1.2, fol = 80.0),
        f("Rumali roti", "1 roti", 115.0, 3.5, 22.0, 1.5, "North Indian", fib = 1.2, sug = 0.5, sat = 0.4, na = 180.0, k = 60.0, ca = 15.0, fe = 1.2, mg = 18.0, zn = 0.5),
        f("Veg pulao", "1 cup", 290.0, 6.5, 48.0, 8.0, "Rice", fib = 3.5, sug = 4.0, sat = 3.5, na = 460.0, k = 280.0, ca = 45.0, fe = 1.8, mg = 40.0, zn = 1.0, va = 220.0, vc = 14.0)
    )

    // ---- Street food --------------------------------------------------------

    private fun streetFood() = listOf(
        f("Pani puri (6 pieces)", "6 pieces", 210.0, 4.5, 34.0, 6.5, "Snacks", fib = 3.5, sug = 4.0, sat = 1.6, na = 780.0, k = 320.0, ca = 40.0, fe = 1.8, mg = 35.0, zn = 0.8, vc = 8.0),
        f("Bhel puri", "1 plate", 240.0, 5.5, 38.0, 8.0, "Snacks", fib = 4.0, sug = 6.0, sat = 1.8, na = 760.0, k = 380.0, ca = 45.0, fe = 2.2, mg = 40.0, zn = 0.9, va = 90.0, vc = 12.0),
        f("Sev puri", "1 plate", 280.0, 6.0, 36.0, 12.0, "Snacks", fib = 4.0, sug = 5.0, sat = 2.4, na = 820.0, k = 360.0, ca = 50.0, fe = 2.4, mg = 42.0, zn = 1.0, vc = 10.0),
        f("Samosa", "1 piece", 260.0, 5.0, 30.0, 13.0, "Snacks", fib = 3.0, sug = 1.5, sat = 3.2, na = 420.0, k = 260.0, ca = 25.0, fe = 1.4, mg = 25.0, zn = 0.6, vc = 8.0),
        f("Vada pav", "1 piece", 300.0, 7.0, 42.0, 12.0, "Snacks", fib = 3.5, sug = 3.0, sat = 3.0, na = 640.0, k = 320.0, ca = 60.0, fe = 2.2, mg = 35.0, zn = 0.9),
        f("Pav bhaji", "1 plate", 420.0, 10.0, 56.0, 18.0, "Snacks", fib = 7.0, sug = 8.0, sat = 8.0, na = 980.0, k = 620.0, ca = 90.0, fe = 3.0, mg = 55.0, zn = 1.3, va = 320.0, vc = 45.0),
        f("Masala puri", "1 plate", 300.0, 8.0, 44.0, 10.0, "Snacks", fib = 6.0, sug = 4.0, sat = 2.2, na = 820.0, k = 440.0, ca = 55.0, fe = 2.6, mg = 50.0, zn = 1.2, vc = 12.0, fol = 90.0),
        f("Dahi puri", "1 plate", 265.0, 7.0, 36.0, 10.0, "Snacks", fib = 3.5, sug = 7.0, sat = 3.0, na = 700.0, k = 380.0, ca = 150.0, fe = 1.8, mg = 35.0, zn = 0.9, b12 = 0.3),
        f("Egg puff", "1 piece", 290.0, 9.0, 26.0, 17.0, "Snacks", fib = 1.2, sug = 2.0, sat = 8.0, na = 480.0, k = 140.0, ca = 45.0, fe = 1.6, mg = 18.0, zn = 0.9, va = 140.0, vd = 1.1, b12 = 0.5),
        f("Chicken roll", "1 roll", 380.0, 20.0, 40.0, 16.0, "Snacks", fib = 2.5, sug = 4.0, sat = 5.5, na = 820.0, k = 320.0, ca = 60.0, fe = 2.4, mg = 35.0, zn = 1.8, va = 90.0, vc = 8.0, b12 = 0.5),
        f("Corn chaat", "1 cup", 180.0, 5.0, 32.0, 4.5, "Snacks", fib = 4.0, sug = 6.0, sat = 1.4, na = 380.0, k = 320.0, ca = 30.0, fe = 1.0, mg = 45.0, zn = 0.9, va = 60.0, vc = 12.0, fol = 60.0),
        f("Sundal", "1 cup", 190.0, 9.0, 28.0, 5.0, "Snacks", fib = 8.0, sug = 3.0, sat = 2.4, na = 320.0, k = 420.0, ca = 70.0, fe = 2.6, mg = 60.0, zn = 1.6, vc = 6.0, fol = 150.0)
    )

    // ---- More South Indian non-veg -------------------------------------------

    private fun moreSouthIndianNonVeg() = listOf(
        f("Chettinad chicken curry", "1 cup", 320.0, 27.0, 10.0, 19.0, "Protein", fib = 3.0, sug = 3.0, sat = 8.0, na = 720.0, k = 480.0, ca = 70.0, fe = 2.6, mg = 50.0, zn = 2.2, va = 90.0, vc = 10.0, b12 = 0.6),
        f("Fish moilee", "1 cup", 300.0, 24.0, 8.0, 20.0, "Protein", fib = 2.0, sug = 3.0, sat = 12.0, na = 640.0, k = 520.0, ca = 60.0, fe = 1.4, mg = 50.0, zn = 1.2, va = 80.0, vc = 8.0, vd = 3.5, b12 = 1.8, o3 = 0.9),
        f("Fish fry, tawa", "1 piece", 250.0, 26.0, 6.0, 13.0, "Protein", fib = 0.6, sat = 3.0, na = 520.0, k = 420.0, ca = 55.0, fe = 1.4, mg = 45.0, zn = 0.9, vd = 4.0, b12 = 2.0, o3 = 0.9),
        f("Egg kuzhambu", "1 cup", 240.0, 14.0, 10.0, 16.0, "Protein", fib = 2.4, sug = 3.0, sat = 5.5, na = 620.0, k = 380.0, ca = 80.0, fe = 2.4, mg = 40.0, zn = 1.5, va = 200.0, vc = 8.0, vd = 2.0, b12 = 1.0, fol = 55.0),
        f("Grilled chicken breast, masala", "150 g", 245.0, 45.0, 2.0, 6.0, "Protein", sat = 1.6, na = 420.0, k = 520.0, ca = 20.0, fe = 1.4, mg = 50.0, zn = 1.8, b12 = 0.5)
    )

    // ---- Sweets --------------------------------------------------------------

    private fun moreSweets() = listOf(
        f("Rasmalai", "2 pieces", 320.0, 9.0, 40.0, 14.0, "Sweets", sug = 36.0, sat = 8.0, na = 100.0, k = 220.0, ca = 300.0, fe = 0.4, mg = 25.0, zn = 0.9, va = 110.0, vd = 0.6, b12 = 0.6),
        f("Kaju katli", "3 pieces", 210.0, 4.0, 26.0, 10.0, "Sweets", fib = 0.8, sug = 22.0, sat = 2.4, na = 20.0, k = 160.0, ca = 25.0, fe = 1.2, mg = 60.0, zn = 1.2),
        f("Payasam (semiya)", "1 cup", 245.0, 6.0, 40.0, 7.0, "Sweets", fib = 1.0, sug = 32.0, sat = 4.2, na = 80.0, k = 240.0, ca = 200.0, fe = 0.6, mg = 30.0, zn = 0.8, vd = 0.8, b12 = 0.6),
        f("Ice cream, vanilla", "1 scoop", 145.0, 2.5, 17.0, 7.5, "Sweets", sug = 16.0, sat = 4.6, na = 55.0, k = 130.0, ca = 90.0, mg = 10.0, zn = 0.3, va = 70.0, vd = 0.3, b12 = 0.3)
    )

    // ---- Staples and basics -------------------------------------------------

    private fun moreStaples() = listOf(
        f("Barley, cooked", "1 cup", 193.0, 3.5, 44.0, 0.7, "Grains", fib = 6.0, sug = 0.4, na = 5.0, k = 146.0, ca = 17.0, fe = 2.1, mg = 35.0, zn = 1.3, fol = 25.0),
        f("Millet (kodo), cooked", "1 cup", 190.0, 6.0, 38.0, 1.5, "Grains", fib = 6.5, sug = 0.5, na = 8.0, k = 160.0, ca = 30.0, fe = 2.0, mg = 80.0, zn = 1.5, fol = 30.0),
        f("Whole wheat bread", "2 slices", 160.0, 8.0, 28.0, 2.2, "Grains", fib = 4.0, sug = 3.0, sat = 0.5, na = 260.0, k = 150.0, ca = 60.0, fe = 1.8, mg = 50.0, zn = 1.2, fol = 30.0),
        f("Oats, rolled, cooked", "1 cup", 160.0, 6.0, 27.0, 3.2, "Grains", fib = 4.0, sug = 0.6, sat = 0.6, na = 5.0, k = 160.0, ca = 20.0, fe = 1.8, mg = 60.0, zn = 1.5, fol = 14.0),
        f("Paneer, raw", "100 g", 296.0, 18.0, 4.0, 23.0, "Dairy", sug = 4.0, sat = 14.0, na = 22.0, k = 130.0, ca = 480.0, fe = 0.2, mg = 25.0, zn = 1.1, va = 200.0, vd = 0.3, b12 = 0.8),
        f("Tofu, firm", "100 g", 144.0, 17.0, 3.0, 8.0, "Protein", fib = 2.0, sug = 0.6, sat = 1.1, na = 14.0, k = 237.0, ca = 350.0, fe = 2.7, mg = 58.0, zn = 1.6, fol = 19.0),
        f("Greek yoghurt, plain", "150 g", 100.0, 17.0, 6.0, 0.7, "Dairy", sug = 5.0, sat = 0.4, na = 55.0, k = 220.0, ca = 165.0, fe = 0.1, mg = 17.0, zn = 0.9, b12 = 0.8, fol = 11.0)
    )
}
