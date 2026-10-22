package dev.avles.gym.data

/**
 * The tea stall and the juice shop. The tea stall's cup is about 120 ml with
 * milk and two spoons of sugar unless it says otherwise, and its snacks are the
 * ones fried beside it. The juice shop's glass is about 300 ml and sweetened the
 * way it is sold; asking for no sugar takes most of the calories out of a juice,
 * and a milkshake's are mostly the ice cream and sugar. Calories are derived from
 * the macros (4/4/9, fibre at 2).
 */
internal object SeedFoodsStalls {

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

    fun all(): List<FoodItem> = teaStall() + juiceShop()

    private fun teaStall() = listOf(
        f("Ginger tea", "1 cup", 75.0, 2.4, 11.0, 2.4, "Drinks", aka = "Inji tea, Adrak chai", sug = 10.0, sat = 1.5, na = 30.0, k = 110.0, ca = 80.0, mg = 8.0, zn = 0.3, b12 = 0.3),
        f("Masala tea", "1 cup", 75.0, 2.4, 11.0, 2.4, "Drinks", aka = "Masala chai", sug = 10.0, sat = 1.5, na = 30.0, k = 110.0, ca = 80.0, mg = 8.0, zn = 0.3, b12 = 0.3),
        f("Elaichi tea", "1 cup", 75.0, 2.4, 11.0, 2.4, "Drinks", aka = "Cardamom tea, Yelakkai tea", sug = 10.0, sat = 1.5, na = 30.0, k = 110.0, ca = 80.0, mg = 8.0, zn = 0.3, b12 = 0.3),
        f("Tea without sugar", "1 cup", 45.0, 2.2, 3.4, 2.4, "Drinks", aka = "Sugarless tea, Tea no sugar", sug = 3.4, sat = 1.5, na = 30.0, k = 110.0, ca = 80.0, mg = 8.0, zn = 0.3, b12 = 0.3),
        f("Filter coffee without sugar", "1 cup", 50.0, 2.6, 4.0, 2.8, "Drinks", aka = "Sugarless coffee, Coffee no sugar", sug = 4.0, sat = 1.8, na = 35.0, k = 180.0, ca = 90.0, mg = 14.0, zn = 0.4, b12 = 0.3),
        f("Karupatti coffee", "1 cup", 85.0, 2.6, 12.0, 2.8, "Drinks", aka = "Palm jaggery coffee, Nattu sakkarai coffee", sug = 11.0, sat = 1.8, na = 40.0, k = 220.0, ca = 100.0, fe = 0.4, mg = 18.0, zn = 0.4, b12 = 0.3),
        f("Lemon tea", "1 cup", 40.0, 0.1, 10.0, 0.0, "Drinks", aka = "Sulaimani, Black tea with lemon", sug = 10.0, na = 5.0, k = 60.0, vc = 5.0),
        f("Hot milk with sugar", "1 glass", 140.0, 6.2, 15.0, 6.4, "Drinks", aka = "Paal, Glass of milk", sug = 15.0, sat = 4.0, na = 90.0, k = 300.0, ca = 240.0, mg = 22.0, zn = 0.8, va = 60.0, b12 = 0.8),
        f("Herbal tea", "1 cup", 0.0, 0.0, 0.5, 0.0, "Drinks", aka = "Tulsi tea, Chamomile tea", k = 20.0),
        f("Milagai bajji", "2 bajji", 195.0, 3.0, 20.0, 12.0, "Snacks", aka = "Chilli bajji, Mirchi bajji", fib = 2.0, sug = 2.0, sat = 2.0, na = 320.0, k = 160.0, ca = 25.0, fe = 1.2, mg = 20.0, zn = 0.5, vc = 30.0),
        f("Egg bajji", "2 pieces", 185.0, 8.0, 12.0, 12.0, "Snacks", aka = "Muttai bajji", fib = 1.0, sat = 2.4, na = 300.0, k = 90.0, ca = 35.0, fe = 1.2, mg = 12.0, zn = 0.7, va = 80.0, b12 = 0.5),
        f("Bun butter jam", "1 bun", 275.0, 5.0, 42.0, 10.0, "Snacks", aka = "Maska bun, Bun butter", fib = 1.2, sug = 16.0, sat = 6.0, na = 260.0, k = 70.0, ca = 35.0, fe = 1.2, mg = 12.0, zn = 0.4),
        f("Butter sweet corn cup", "1 cup", 185.0, 4.0, 30.0, 6.0, "Snacks", aka = "Sweet corn cup, Masala corn", fib = 3.0, sug = 6.0, sat = 3.5, na = 280.0, k = 250.0, ca = 5.0, fe = 0.6, mg = 30.0, zn = 0.7, vc = 6.0)
    )

    private fun juiceShop() = listOf(
        f("Fresh lime juice, sweet", "1 glass", 105.0, 0.2, 26.0, 0.0, "Juices", aka = "Lemon juice, Nimbu pani, Lime juice", sug = 25.0, na = 10.0, k = 40.0, vc = 12.0),
        f("Fresh lime soda, salted", "1 glass", 10.0, 0.2, 2.0, 0.0, "Juices", aka = "Salt lime soda", sug = 1.0, na = 450.0, k = 40.0, vc = 12.0),
        f("Masala soda", "1 glass", 80.0, 0.2, 20.0, 0.0, "Juices", aka = "Nimbu masala soda, Jaljeera soda", sug = 19.0, na = 500.0, k = 40.0, vc = 10.0),
        f("Goli soda", "1 bottle", 70.0, 0.0, 18.0, 0.0, "Juices", aka = "Gholi soda, Banta soda", sug = 18.0, na = 20.0),
        f("Paneer soda", "1 bottle", 80.0, 0.0, 20.0, 0.0, "Juices", aka = "Rose soda, Panneer soda", sug = 20.0, na = 20.0),
        f("Papaya juice", "1 glass", 125.0, 1.0, 30.0, 0.3, "Juices", fib = 1.5, sug = 26.0, na = 10.0, k = 320.0, ca = 40.0, mg = 24.0, va = 90.0, vc = 90.0, fol = 55.0),
        f("Guava juice", "1 glass", 130.0, 1.2, 32.0, 0.4, "Juices", aka = "Koyya juice", fib = 2.0, sug = 26.0, na = 10.0, k = 280.0, ca = 20.0, mg = 18.0, va = 30.0, vc = 120.0, fol = 40.0),
        f("ABC juice", "1 glass", 125.0, 2.0, 30.0, 0.3, "Juices", aka = "Apple beetroot carrot juice", fib = 2.0, sug = 24.0, na = 90.0, k = 520.0, ca = 40.0, fe = 0.8, mg = 30.0, zn = 0.3, va = 900.0, vc = 12.0, fol = 80.0),
        f("Lychee juice", "1 glass", 140.0, 0.8, 34.0, 0.0, "Juices", aka = "Litchi juice", sug = 32.0, na = 5.0, k = 180.0, ca = 8.0, mg = 10.0, vc = 40.0),
        f("Fresh mango juice", "1 glass", 165.0, 1.2, 40.0, 0.6, "Juices", aka = "Mango juice", fib = 1.5, sug = 36.0, na = 5.0, k = 260.0, ca = 20.0, mg = 16.0, va = 90.0, vc = 50.0, fol = 60.0),
        f("Strawberry milkshake", "1 glass", 305.0, 8.0, 48.0, 9.0, "Juices", aka = "Strawberry shake", fib = 1.0, sug = 44.0, sat = 5.5, na = 140.0, k = 420.0, ca = 280.0, mg = 30.0, zn = 1.0, vc = 20.0, b12 = 0.9),
        f("Vanilla milkshake", "1 glass", 305.0, 8.0, 46.0, 10.0, "Juices", aka = "Vanilla shake", sug = 42.0, sat = 6.0, na = 150.0, k = 380.0, ca = 290.0, mg = 28.0, zn = 1.0, b12 = 0.9),
        f("Oreo milkshake", "1 glass", 425.0, 9.0, 62.0, 16.0, "Juices", aka = "Oreo shake", fib = 1.5, sug = 48.0, sat = 9.0, na = 260.0, k = 420.0, ca = 280.0, fe = 2.0, mg = 36.0, zn = 1.1, b12 = 0.9),
        f("Butterscotch milkshake", "1 glass", 365.0, 8.0, 56.0, 12.0, "Juices", aka = "Butterscotch shake", sug = 50.0, sat = 7.5, na = 180.0, k = 380.0, ca = 280.0, mg = 28.0, zn = 1.0, b12 = 0.9),
        f("Dry fruit milkshake", "1 glass", 365.0, 11.0, 50.0, 14.0, "Juices", aka = "Dry fruits shake", fib = 3.0, sug = 40.0, sat = 4.0, na = 130.0, k = 600.0, ca = 300.0, fe = 1.4, mg = 70.0, zn = 1.4, b12 = 0.9),
        f("Dates milkshake", "1 glass", 295.0, 8.0, 52.0, 7.0, "Juices", aka = "Dates shake, Pericham pazham milkshake", fib = 3.0, sug = 46.0, sat = 4.5, na = 120.0, k = 620.0, ca = 280.0, fe = 0.8, mg = 50.0, zn = 1.0, b12 = 0.9),
        f("Tender coconut milkshake", "1 glass", 290.0, 6.0, 40.0, 12.0, "Juices", aka = "Elaneer shake, Tender coconut shake", fib = 1.5, sug = 34.0, sat = 9.0, na = 120.0, k = 450.0, ca = 220.0, mg = 40.0, zn = 0.8, b12 = 0.7),
        f("Kulfi falooda", "1 glass", 440.0, 9.0, 66.0, 16.0, "Sweets", aka = "Falooda with kulfi", fib = 1.5, sug = 50.0, sat = 10.0, na = 130.0, k = 400.0, ca = 320.0, fe = 0.6, mg = 32.0, zn = 1.0, b12 = 0.8),
        f("Pista milk", "1 glass", 230.0, 8.0, 30.0, 9.0, "Drinks", aka = "Pistachio milk", fib = 1.0, sug = 26.0, sat = 4.5, na = 110.0, k = 380.0, ca = 280.0, fe = 0.6, mg = 40.0, zn = 1.0, b12 = 0.8),
        f("Badam pisin drink", "1 glass", 195.0, 3.0, 40.0, 3.0, "Drinks", aka = "Almond gum drink, Gond katira", fib = 1.0, sug = 34.0, sat = 1.8, na = 50.0, k = 150.0, ca = 100.0, mg = 12.0, zn = 0.3),
        f("Ragi malt", "1 glass", 155.0, 5.0, 28.0, 3.0, "Drinks", aka = "Kezhvaragu malt, Ragi milk", fib = 2.5, sug = 10.0, sat = 1.8, na = 60.0, k = 220.0, ca = 200.0, fe = 1.2, mg = 40.0, zn = 0.8, b12 = 0.3),
        f("Sathu maavu malt", "1 glass", 200.0, 8.0, 30.0, 6.0, "Drinks", aka = "Health mix malt, Multigrain malt", fib = 3.0, sug = 12.0, sat = 2.4, na = 70.0, k = 300.0, ca = 180.0, fe = 2.0, mg = 60.0, zn = 1.2, b12 = 0.3)
    )
}
