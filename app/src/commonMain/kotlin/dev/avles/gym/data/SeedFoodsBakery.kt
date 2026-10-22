package dev.avles.gym.data

/**
 * The bakery and the sweet stall: puffs, buns, pastries, cakes and biscuits from
 * the Iyengar and corner bakeries, the savouries sold by weight (sev, omapodi,
 * chips), and the milk and ghee sweets the catalogue did not already carry.
 *
 * A piece is the usual shop size: a bun about 70 g, a pastry about 70 g, a laddu
 * or a burfi about 40 g. Bakery sweets vary with the shop more than most foods,
 * mostly in sugar and fat. Calories are derived from the macros (4/4/9, fibre
 * at 2).
 */
internal object SeedFoodsBakery {

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

    fun all(): List<FoodItem> = bakerySavouries() + bakerySweets() + sweetStall()

    private fun bakerySavouries() = listOf(
        f("Chicken puff", "1 puff", 315.0, 9.0, 30.0, 18.0, "Snacks", aka = "Chicken puffs", fib = 1.2, sug = 2.0, sat = 9.0, na = 480.0, k = 150.0, ca = 20.0, fe = 1.4, mg = 14.0, zn = 0.6, b12 = 0.2),
        f("Paneer puff", "1 puff", 330.0, 8.0, 30.0, 20.0, "Snacks", fib = 1.2, sug = 2.0, sat = 10.0, na = 420.0, k = 100.0, ca = 120.0, fe = 1.2, mg = 12.0, zn = 0.7),
        f("Mushroom puff", "1 puff", 280.0, 5.0, 30.0, 16.0, "Snacks", fib = 1.8, sug = 2.0, sat = 8.0, na = 420.0, k = 180.0, ca = 15.0, fe = 1.2, mg = 12.0, zn = 0.5),
        f("Pizza puff", "1 piece", 295.0, 7.0, 32.0, 16.0, "Snacks", fib = 1.5, sug = 3.0, sat = 6.0, na = 450.0, k = 150.0, ca = 80.0, fe = 1.4, mg = 14.0, zn = 0.6),
        f("Kachori", "1 piece", 210.0, 4.0, 22.0, 12.0, "Snacks", aka = "Dal kachori", fib = 2.0, sug = 1.0, sat = 2.4, na = 250.0, k = 120.0, ca = 20.0, fe = 1.2, mg = 20.0, zn = 0.5),
        f("Khari", "4 sticks", 155.0, 2.5, 16.0, 9.0, "Snacks", aka = "Khari biscuit, Puff biscuit", fib = 0.6, sug = 1.0, sat = 4.5, na = 180.0, k = 30.0, ca = 8.0, fe = 0.6, mg = 6.0, zn = 0.2),
        f("Bakery veg sandwich", "1 sandwich", 240.0, 6.0, 32.0, 10.0, "Snacks", aka = "Veg sandwich", fib = 2.0, sug = 4.0, sat = 3.0, na = 450.0, k = 160.0, ca = 60.0, fe = 1.4, mg = 18.0, zn = 0.5, vc = 6.0),
        f("Bread pakora", "1 piece", 230.0, 5.0, 26.0, 12.0, "Snacks", aka = "Bread bajji", fib = 2.0, sug = 2.0, sat = 2.0, na = 350.0, k = 120.0, ca = 30.0, fe = 1.4, mg = 20.0, zn = 0.5),
        f("Cheese balls", "6 pieces", 250.0, 7.0, 20.0, 16.0, "Snacks", fib = 1.0, sat = 6.0, na = 400.0, k = 120.0, ca = 150.0, fe = 0.6, mg = 12.0, zn = 0.8),
        f("Kara sev", "30 g", 155.0, 4.0, 15.0, 9.0, "Snacks", aka = "Sev, Besan sev", fib = 2.0, sat = 1.3, na = 220.0, k = 150.0, ca = 15.0, fe = 1.2, mg = 30.0, zn = 0.6),
        f("Omapodi", "30 g", 160.0, 4.0, 15.0, 9.5, "Snacks", aka = "Ompodi", fib = 2.0, sat = 1.4, na = 200.0, k = 150.0, ca = 20.0, fe = 1.2, mg = 30.0, zn = 0.6),
        f("Butter murukku", "30 g", 160.0, 3.0, 17.0, 9.0, "Snacks", fib = 1.5, sat = 2.5, na = 150.0, k = 60.0, ca = 10.0, fe = 0.8, mg = 16.0, zn = 0.4),
        f("Tapioca chips", "30 g", 165.0, 0.5, 19.0, 10.0, "Snacks", aka = "Maravalli chips, Kappa chips", fib = 1.0, sat = 4.5, na = 160.0, k = 120.0, ca = 8.0, fe = 0.2, mg = 8.0),
        f("Jackfruit chips", "30 g", 160.0, 1.0, 18.0, 10.0, "Snacks", aka = "Chakka chips, Pala chips", fib = 2.0, sug = 4.0, sat = 5.0, na = 120.0, k = 150.0, ca = 10.0, fe = 0.3, mg = 10.0),
        f("Masala peanuts", "30 g", 170.0, 6.0, 10.0, 12.0, "Snacks", aka = "Masala kadalai", fib = 2.0, sug = 1.0, sat = 1.8, na = 200.0, k = 180.0, ca = 20.0, fe = 0.8, mg = 45.0, zn = 0.8, fol = 30.0),
        f("Cornflakes mixture", "30 g", 165.0, 3.0, 16.0, 10.0, "Snacks", fib = 1.5, sug = 2.0, sat = 2.0, na = 200.0, k = 90.0, ca = 10.0, fe = 1.6, mg = 18.0, zn = 0.4),
        f("Croissant", "1 croissant", 230.0, 5.0, 26.0, 12.0, "Snacks", fib = 1.2, sug = 4.0, sat = 7.0, na = 300.0, k = 70.0, ca = 20.0, fe = 1.2, mg = 10.0, zn = 0.4),
        f("Cake rusk", "2 pieces", 180.0, 2.6, 24.0, 8.0, "Snacks", fib = 0.4, sug = 10.0, sat = 3.0, na = 80.0, k = 40.0, ca = 12.0, fe = 0.6, mg = 6.0, zn = 0.2),
        f("Milk bread", "2 slices", 165.0, 5.0, 30.0, 3.0, "Grains", aka = "Sandwich bread", fib = 1.0, sug = 5.0, sat = 1.0, na = 300.0, k = 70.0, ca = 60.0, fe = 1.6, mg = 12.0, zn = 0.4)
    )

    private fun bakerySweets() = listOf(
        f("Cream bun", "1 bun", 260.0, 5.0, 38.0, 10.0, "Sweets", fib = 1.0, sug = 16.0, sat = 5.0, na = 200.0, k = 70.0, ca = 40.0, fe = 1.2, mg = 12.0, zn = 0.4),
        f("Jam bun", "1 bun", 230.0, 5.0, 42.0, 5.0, "Sweets", fib = 1.2, sug = 18.0, sat = 2.0, na = 200.0, k = 80.0, ca = 30.0, fe = 1.2, mg = 12.0, zn = 0.4),
        f("Butter bun", "1 bun", 270.0, 5.0, 36.0, 12.0, "Sweets", aka = "Coimbatore butter bun", fib = 1.0, sug = 12.0, sat = 7.0, na = 220.0, k = 70.0, ca = 35.0, fe = 1.2, mg = 12.0, zn = 0.4),
        f("Coconut bun", "1 bun", 285.0, 5.0, 40.0, 12.0, "Sweets", aka = "Dilkush, Dilpasand", fib = 2.5, sug = 16.0, sat = 8.0, na = 200.0, k = 110.0, ca = 30.0, fe = 1.4, mg = 16.0, zn = 0.5),
        f("Honey cake", "1 piece", 245.0, 3.0, 36.0, 10.0, "Sweets", fib = 0.5, sug = 24.0, sat = 4.0, na = 150.0, k = 50.0, ca = 25.0, fe = 0.6, mg = 6.0, zn = 0.2),
        f("Black forest pastry", "1 pastry", 310.0, 3.5, 34.0, 18.0, "Sweets", aka = "Black forest cake", fib = 1.0, sug = 26.0, sat = 10.0, na = 150.0, k = 100.0, ca = 40.0, fe = 1.2, mg = 16.0, zn = 0.4),
        f("Pineapple pastry", "1 pastry", 280.0, 3.0, 34.0, 15.0, "Sweets", aka = "Pineapple cake", fib = 0.5, sug = 26.0, sat = 8.0, na = 140.0, k = 70.0, ca = 35.0, fe = 0.6, mg = 8.0, zn = 0.2),
        f("Chocolate pastry", "1 pastry", 335.0, 4.0, 36.0, 20.0, "Sweets", aka = "Chocolate truffle pastry", fib = 1.5, sug = 28.0, sat = 11.0, na = 160.0, k = 150.0, ca = 40.0, fe = 1.6, mg = 30.0, zn = 0.6),
        f("Plum cake", "1 slice", 235.0, 3.0, 34.0, 10.0, "Sweets", aka = "Fruit cake", fib = 1.2, sug = 24.0, sat = 3.0, na = 150.0, k = 120.0, ca = 30.0, fe = 0.8, mg = 10.0, zn = 0.3),
        f("Cupcake", "1 cupcake", 210.0, 2.5, 28.0, 10.0, "Sweets", fib = 0.5, sug = 18.0, sat = 4.0, na = 150.0, k = 40.0, ca = 25.0, fe = 0.6, mg = 6.0, zn = 0.2),
        f("Chocolate muffin", "1 muffin", 390.0, 5.0, 48.0, 20.0, "Sweets", aka = "Muffin", fib = 2.0, sug = 28.0, sat = 6.0, na = 300.0, k = 180.0, ca = 60.0, fe = 2.0, mg = 30.0, zn = 0.6),
        f("Brownie", "1 piece", 300.0, 3.5, 36.0, 16.0, "Sweets", aka = "Chocolate brownie", fib = 1.5, sug = 26.0, sat = 6.0, na = 120.0, k = 120.0, ca = 20.0, fe = 1.6, mg = 30.0, zn = 0.6),
        f("Donut", "1 donut", 300.0, 4.0, 36.0, 16.0, "Sweets", aka = "Doughnut, Chocolate donut", fib = 1.2, sug = 18.0, sat = 7.0, na = 250.0, k = 80.0, ca = 30.0, fe = 1.4, mg = 14.0, zn = 0.4),
        f("Nankhatai", "2 pieces", 170.0, 2.0, 18.0, 10.0, "Sweets", aka = "Iyengar butter biscuit", fib = 0.5, sug = 9.0, sat = 6.0, na = 30.0, k = 25.0, ca = 8.0, fe = 0.5, mg = 6.0, zn = 0.2),
        f("Osmania biscuit", "2 biscuits", 150.0, 2.0, 18.0, 8.0, "Snacks", fib = 0.5, sug = 7.0, sat = 4.0, na = 80.0, k = 30.0, ca = 10.0, fe = 0.6, mg = 6.0, zn = 0.2),
        f("Coconut biscuit", "2 biscuits", 140.0, 1.6, 18.0, 7.0, "Snacks", fib = 1.0, sug = 8.0, sat = 4.0, na = 80.0, k = 30.0, ca = 8.0, fe = 0.6, mg = 6.0, zn = 0.2),
        f("Salt biscuit", "4 biscuits", 125.0, 2.0, 16.0, 6.0, "Snacks", aka = "Cream crackers, Salted crackers", fib = 0.6, sug = 1.0, sat = 3.0, na = 200.0, k = 30.0, ca = 10.0, fe = 0.6, mg = 6.0, zn = 0.2),
        f("Chocolate chip cookies", "2 cookies", 160.0, 2.0, 20.0, 8.0, "Sweets", aka = "Cookies", fib = 0.8, sug = 11.0, sat = 4.0, na = 90.0, k = 50.0, ca = 10.0, fe = 0.8, mg = 12.0, zn = 0.3),
        f("Ice cream cone", "1 cone", 215.0, 3.0, 28.0, 10.0, "Sweets", aka = "Cornetto", fib = 0.5, sug = 20.0, sat = 6.0, na = 60.0, k = 150.0, ca = 90.0, mg = 12.0, zn = 0.4),
        f("Choco bar", "1 bar", 190.0, 2.0, 18.0, 12.0, "Sweets", aka = "Chocobar ice cream", sug = 15.0, sat = 9.0, na = 30.0, k = 110.0, ca = 60.0, mg = 12.0, zn = 0.3),
        f("Chocolate milkshake", "1 glass", 325.0, 9.0, 50.0, 10.0, "Drinks", aka = "Chocolate shake", fib = 1.0, sug = 44.0, sat = 6.0, na = 150.0, k = 450.0, ca = 280.0, fe = 0.8, mg = 40.0, zn = 1.0, b12 = 0.9)
    )

    private fun sweetStall() = listOf(
        f("Ghee Mysore pak", "1 piece", 245.0, 2.0, 14.0, 20.0, "Sweets", aka = "Soft Mysore pak, Krishna Mysore pak", fib = 0.6, sug = 12.0, sat = 12.0, na = 5.0, k = 40.0, ca = 8.0, fe = 0.4, mg = 12.0, zn = 0.2),
        f("Motichoor laddu", "1 laddu", 175.0, 2.0, 24.0, 8.0, "Sweets", aka = "Motichur ladoo", fib = 0.8, sug = 17.0, sat = 4.0, na = 10.0, k = 50.0, ca = 10.0, fe = 0.5, mg = 10.0, zn = 0.2),
        f("Besan laddu", "1 laddu", 190.0, 3.5, 22.0, 10.0, "Sweets", aka = "Kadalai maavu laddu", fib = 1.6, sug = 14.0, sat = 6.0, na = 5.0, k = 120.0, ca = 10.0, fe = 0.8, mg = 25.0, zn = 0.4),
        f("Dry fruit laddu", "1 laddu", 180.0, 4.0, 24.0, 8.0, "Sweets", aka = "Dates and nuts laddu", fib = 3.0, sug = 18.0, sat = 1.5, na = 5.0, k = 250.0, ca = 30.0, fe = 0.8, mg = 35.0, zn = 0.5),
        f("Coconut laddu", "1 laddu", 125.0, 1.5, 15.0, 7.0, "Sweets", aka = "Thengai laddu", fib = 2.0, sug = 12.0, sat = 6.0, na = 10.0, k = 60.0, ca = 6.0, fe = 0.4, mg = 6.0),
        f("Kalakand", "1 piece", 150.0, 4.0, 18.0, 7.0, "Sweets", sug = 16.0, sat = 4.5, na = 40.0, k = 100.0, ca = 120.0, mg = 10.0, zn = 0.4),
        f("Milk burfi", "1 piece", 150.0, 3.5, 20.0, 6.0, "Sweets", aka = "Barfi, Paal burfi", sug = 18.0, sat = 4.0, na = 40.0, k = 100.0, ca = 110.0, mg = 10.0, zn = 0.4),
        f("Badusha", "1 piece", 225.0, 2.0, 28.0, 12.0, "Sweets", aka = "Balushahi", fib = 0.6, sug = 16.0, sat = 6.0, na = 20.0, k = 30.0, ca = 8.0, fe = 0.6, mg = 6.0, zn = 0.2),
        f("Basundi", "1 cup", 305.0, 9.0, 40.0, 12.0, "Sweets", sug = 34.0, sat = 7.5, na = 90.0, k = 330.0, ca = 300.0, mg = 28.0, zn = 1.0, b12 = 0.8),
        f("Rabri", "1 cup", 240.0, 7.0, 30.0, 10.0, "Sweets", aka = "Rabdi", sug = 26.0, sat = 6.0, na = 70.0, k = 260.0, ca = 240.0, mg = 22.0, zn = 0.8, b12 = 0.6),
        f("Sandesh", "2 pieces", 160.0, 6.0, 20.0, 6.0, "Sweets", sug = 18.0, sat = 3.8, na = 30.0, k = 90.0, ca = 140.0, mg = 10.0, zn = 0.5),
        f("Cham cham", "1 piece", 145.0, 3.0, 24.0, 4.0, "Sweets", sug = 20.0, sat = 2.5, na = 20.0, k = 60.0, ca = 80.0, mg = 6.0, zn = 0.3),
        f("Kala jamun", "1 piece", 160.0, 2.0, 22.0, 7.0, "Sweets", aka = "Black jamun", sug = 17.0, sat = 3.5, na = 20.0, k = 50.0, ca = 40.0, mg = 6.0, zn = 0.2),
        f("Ellu urundai", "1 ball", 120.0, 3.5, 12.0, 7.0, "Sweets", aka = "Sesame ball, Til laddu, Ellu mittai", fib = 2.0, sug = 9.0, sat = 1.0, na = 5.0, k = 120.0, ca = 150.0, fe = 2.0, mg = 60.0, zn = 1.2),
        f("Kadalai urundai", "1 ball", 130.0, 4.5, 13.0, 7.0, "Sweets", aka = "Peanut ball", fib = 1.5, sug = 10.0, sat = 1.0, na = 5.0, k = 140.0, ca = 20.0, fe = 0.8, mg = 35.0, zn = 0.6),
        f("Pori urundai", "2 balls", 95.0, 1.0, 22.0, 0.3, "Sweets", aka = "Puffed rice ball, Murmura laddu", fib = 0.3, sug = 12.0, na = 10.0, k = 60.0, ca = 10.0, fe = 1.0, mg = 8.0),
        f("Dates halwa", "75 g", 240.0, 2.0, 42.0, 8.0, "Sweets", aka = "Pericham pazham halwa", fib = 3.0, sug = 34.0, sat = 4.0, na = 10.0, k = 350.0, ca = 40.0, fe = 0.8, mg = 30.0, zn = 0.3),
        f("Bread halwa", "75 g", 245.0, 3.0, 32.0, 12.0, "Sweets", aka = "Double ka meetha", fib = 0.8, sug = 20.0, sat = 6.0, na = 120.0, k = 80.0, ca = 50.0, fe = 0.8, mg = 10.0, zn = 0.3),
        f("Achu murukku", "2 pieces", 115.0, 1.5, 14.0, 6.0, "Sweets", aka = "Rose cookies, Achappam", fib = 0.3, sug = 5.0, sat = 1.0, na = 20.0, k = 30.0, ca = 10.0, fe = 0.4, mg = 6.0),
        f("Pineapple kesari", "1 cup", 325.0, 3.0, 52.0, 12.0, "Sweets", aka = "Pineapple sheera", fib = 1.0, sug = 30.0, sat = 7.0, na = 30.0, k = 80.0, ca = 20.0, fe = 0.8, mg = 14.0, zn = 0.4, vc = 8.0)
    )
}
