package dev.avles.gym.data

/**
 * The rest of what a week in Tamil Nadu puts on the plate: Aavin and other dairy by
 * packet colour, biscuit, chip and chocolate brands, cool drinks and energy drinks,
 * alcohol, fish by species, millet dishes, breakfast and festival foods, more
 * supplement brands, home dishes per 100 g for weighing, and chain menus (McDonald's,
 * KFC, Domino's, Subway, Burger King, Starbucks, A2B).
 *
 * Packaged foods follow their labels at the time of writing; chain items are the
 * chains' own published figures rounded, and recipes change, so they are close
 * rather than exact. Calories are derived from the macros (4/4/9, fibre at 2),
 * except alcohol, whose energy is mostly ethanol at 7 kcal/g and is given directly.
 */
internal object SeedFoodsWeek {

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

    fun all(): List<FoodItem> = tamilNaduDairy() + biscuitsAndChips() + chocolates() + coolDrinks() + alcohol() + fishBySpecies() + millets() + breakfastVariants() + festivalFoods() + supplementBrands() + homeDishesByWeight() + chainMenus()

    private fun tamilNaduDairy() = listOf(
        f("Aavin toned milk", "1 glass (200 ml)", 115.0, 6.2, 9.4, 6.0, "Dairy", aka = "Aavin green, Green packet milk, Aavin milk", fib = 0.0, sug = 9.4, sat = 3.8, na = 90.0, k = 300.0, ca = 240.0),
        f("Aavin standardised milk", "1 glass (200 ml)", 145.0, 6.4, 9.6, 9.0, "Dairy", aka = "Aavin orange, Orange packet milk", fib = 0.0, sug = 9.6, sat = 5.6, na = 90.0, k = 300.0, ca = 240.0),
        f("Aavin full cream milk", "1 glass (200 ml)", 175.0, 6.6, 9.6, 12.0, "Dairy", aka = "Aavin premium, Aavin pink, Full cream packet milk", fib = 0.0, sug = 9.6, sat = 7.5, na = 90.0, k = 300.0, ca = 240.0),
        f("Aavin double toned milk", "1 glass (200 ml)", 90.0, 6.2, 9.4, 3.0, "Dairy", aka = "Aavin blue, Blue packet milk, Aavin delite", fib = 0.0, sug = 9.4, sat = 1.9, na = 90.0, k = 300.0, ca = 240.0),
        f("Aavin curd", "1 cup (200 g)", 115.0, 6.4, 8.6, 6.0, "Dairy", aka = "Aavin thayir, Aavin dahi", fib = 0.0, sug = 8.6, sat = 3.8, na = 90.0, k = 300.0, ca = 240.0),
        f("Aavin paneer", "100 g", 280.0, 18.0, 2.0, 22.0, "Dairy", aka = "Aavin panneer", fib = 0.0, sug = 2.0, sat = 14.0, na = 30.0, k = 100.0, ca = 480.0),
        f("Aavin ghee", "1 tsp (5 g)", 45.0, 0.0, 0.0, 5.0, "Fats", aka = "Aavin nei", fib = 0.0, sug = 0.0, sat = 3.1, na = 0.0, k = 0.0),
        f("Aavin milkshake", "1 bottle (200 ml)", 175.0, 6.0, 26.0, 5.0, "Drinks", aka = "Aavin flavoured milk, Aavin badam milk, Aavin rose milk", fib = 0.0, sug = 24.0, sat = 3.0, na = 100.0, k = 300.0, ca = 220.0),
        f("Aavin spiced buttermilk", "1 pack (200 ml)", 35.0, 2.0, 3.0, 1.5, "Drinks", aka = "Aavin mor, Aavin neer mor", fib = 0.0, sug = 3.0, sat = 1.0, na = 300.0, k = 120.0, ca = 100.0),
        f("Aavin ice cream cup", "1 cup (100 ml)", 180.0, 3.0, 22.0, 9.0, "Sweets", aka = "Aavin ice cream, Aavin vanilla", fib = 0.0, sug = 20.0, sat = 6.0, na = 60.0, k = 150.0, ca = 100.0),
        f("Arun ice cream cup", "1 cup (100 ml)", 200.0, 3.0, 24.0, 10.0, "Sweets", aka = "Arun ice cream, Arun vanilla", fib = 0.0, sug = 21.0, sat = 7.0, na = 60.0, k = 150.0, ca = 100.0),
        f("Hatsun Arokya milk", "1 glass (200 ml)", 115.0, 6.2, 9.6, 6.0, "Dairy", aka = "Arokya milk, Hatsun milk", fib = 0.0, sug = 9.6, sat = 3.8, na = 90.0, k = 300.0, ca = 240.0),
        f("Amul Taaza milk", "1 glass (200 ml)", 115.0, 6.2, 9.4, 6.0, "Dairy", aka = "Amul Taaza, Amul toned milk", fib = 0.0, sug = 9.4, sat = 3.8, na = 90.0, k = 300.0, ca = 240.0),
        f("Amul butter", "1 tbsp (10 g)", 75.0, 0.1, 0.1, 8.0, "Fats", aka = "Amul salted butter", fib = 0.0, sug = 0.0, sat = 5.1, na = 80.0, k = 0.0),
        f("Amul cheese slice", "1 slice (20 g)", 65.0, 4.0, 0.8, 5.0, "Dairy", aka = "Amul cheese, Processed cheese slice", fib = 0.0, sug = 0.0, sat = 3.3, na = 270.0, k = 0.0, ca = 150.0),
        f("Amul Kool", "1 bottle (180 ml)", 130.0, 5.4, 21.0, 2.7, "Drinks", aka = "Amul Kool cafe, Kool badam, Amul flavoured milk", fib = 0.0, sug = 19.0, sat = 1.7, na = 80.0, k = 250.0, ca = 200.0),
        f("Amul Masti buttermilk", "1 pack (200 ml)", 35.0, 2.2, 3.4, 1.2, "Drinks", aka = "Amul chaas, Masti chaas", fib = 0.0, sug = 3.4, sat = 0.8, na = 280.0, k = 120.0, ca = 100.0),
        f("Amul lassi", "1 pack (200 ml)", 150.0, 5.0, 26.0, 3.0, "Drinks", aka = "Amul sweet lassi", fib = 0.0, sug = 24.0, sat = 2.0, na = 80.0, k = 250.0, ca = 200.0)
    )

    private fun biscuitsAndChips() = listOf(
        f("Parle-G", "4 biscuits (25 g)", 115.0, 1.7, 19.0, 3.5, "Snacks", aka = "Parle G, Glucose biscuit", fib = 0.5, sug = 6.0, sat = 1.6, na = 60.0, k = 30.0),
        f("Britannia Good Day", "3 biscuits (30 g)", 155.0, 2.0, 19.5, 7.5, "Snacks", aka = "Good Day cashew, Good Day butter cookies", fib = 0.4, sug = 7.0, sat = 3.6, na = 90.0, k = 40.0),
        f("Britannia Marie Gold", "5 biscuits (30 g)", 130.0, 2.4, 22.0, 3.6, "Snacks", aka = "Marie Gold", fib = 0.6, sug = 6.0, sat = 1.7, na = 100.0, k = 40.0),
        f("Hide & Seek", "4 biscuits (40 g)", 200.0, 2.6, 26.0, 9.6, "Snacks", aka = "Hide and Seek, Parle chocolate chip cookies", fib = 1.0, sug = 12.0, sat = 4.8, na = 80.0, k = 60.0),
        f("Britannia Bourbon", "3 biscuits (36 g)", 175.0, 2.0, 25.5, 7.5, "Snacks", aka = "Bourbon, Chocolate cream biscuit", fib = 0.8, sug = 13.0, sat = 3.7, na = 90.0, k = 60.0),
        f("Parle Monaco", "6 biscuits (20 g)", 95.0, 1.6, 13.0, 4.2, "Snacks", aka = "Monaco, Salted cracker", fib = 0.4, sug = 1.5, sat = 2.0, na = 160.0, k = 30.0),
        f("Sunfeast Dark Fantasy", "2 cookies (25 g)", 120.0, 1.5, 15.5, 6.0, "Snacks", aka = "Dark Fantasy, Choco fills", fib = 0.6, sug = 8.0, sat = 3.0, na = 50.0, k = 50.0),
        f("Oreo", "3 biscuits (33 g)", 155.0, 1.6, 23.0, 6.6, "Snacks", aka = "Oreo cookies, Cadbury Oreo", fib = 0.8, sug = 12.5, sat = 3.2, na = 110.0, k = 60.0),
        f("Britannia NutriChoice digestive", "2 biscuits (25 g)", 115.0, 2.0, 16.5, 5.0, "Snacks", aka = "Digestive biscuit, NutriChoice", fib = 1.5, sug = 3.5, sat = 2.3, na = 120.0, k = 60.0),
        f("Britannia Milk Bikis", "4 biscuits (28 g)", 130.0, 2.0, 20.0, 4.6, "Snacks", aka = "Milk Bikis, Milk biscuit", fib = 0.4, sug = 6.5, sat = 2.2, na = 80.0, k = 50.0, ca = 40.0),
        f("Lay's classic salted", "1 pack (26 g)", 140.0, 1.8, 14.0, 9.0, "Snacks", aka = "Lays, Lays chips, Potato chips pack", fib = 1.0, sug = 0.3, sat = 3.7, na = 170.0, k = 300.0),
        f("Kurkure masala munch", "1 pack (30 g)", 165.0, 1.9, 17.0, 10.0, "Snacks", aka = "Kurkure", fib = 0.6, sug = 0.8, sat = 4.5, na = 250.0, k = 60.0),
        f("Bingo Mad Angles", "1 pack (30 g)", 160.0, 2.2, 18.5, 8.5, "Snacks", aka = "Mad Angles, Bingo chips", fib = 0.6, sug = 1.0, sat = 3.8, na = 230.0, k = 80.0),
        f("Haldiram's aloo bhujia", "30 g", 185.0, 3.5, 12.5, 14.0, "Snacks", aka = "Haldirams bhujia, Aloo bhujiya", fib = 1.5, sug = 0.5, sat = 5.5, na = 300.0, k = 200.0),
        f("Haldiram's navratan mixture", "30 g", 175.0, 4.0, 13.0, 12.5, "Snacks", aka = "Navratan mix, Haldirams mixture", fib = 2.0, sug = 1.5, sat = 5.0, na = 280.0, k = 150.0),
        f("Haldiram's moong dal", "30 g", 160.0, 7.0, 14.0, 9.0, "Snacks", aka = "Moong dal namkeen, Fried moong dal", fib = 1.5, sug = 0.3, sat = 3.5, na = 250.0, k = 250.0),
        f("Too Yumm multigrain chips", "1 pack (28 g)", 125.0, 2.2, 18.0, 5.0, "Snacks", aka = "Too Yumm", fib = 1.5, sug = 1.0, sat = 2.2, na = 200.0, k = 80.0),
        f("Act II butter popcorn", "1 pack popped (30 g)", 145.0, 2.7, 17.0, 8.0, "Snacks", aka = "Act 2 popcorn, Microwave popcorn", fib = 3.5, sug = 0.3, sat = 3.8, na = 200.0, k = 80.0),
        f("Pringles original", "10 crisps (25 g)", 130.0, 1.0, 13.0, 8.5, "Snacks", aka = "Pringles", fib = 0.8, sug = 0.3, sat = 2.4, na = 130.0, k = 250.0)
    )

    private fun chocolates() = listOf(
        f("Cadbury Dairy Milk, small", "1 bar (13 g)", 70.0, 1.0, 7.4, 3.8, "Sweets", aka = "Dairy Milk, Cadbury chocolate", fib = 0.0, sug = 7.2, sat = 2.3, na = 15.0, k = 50.0, ca = 30.0),
        f("Cadbury Dairy Milk Silk", "1 bar (60 g)", 320.0, 4.6, 33.6, 18.6, "Sweets", aka = "Silk, Cadbury Silk", fib = 1.0, sug = 33.0, sat = 11.0, na = 50.0, k = 220.0, ca = 120.0),
        f("KitKat", "2 fingers (18 g)", 90.0, 1.3, 11.0, 4.5, "Sweets", aka = "Kit Kat, Nestle KitKat", fib = 0.3, sug = 8.5, sat = 3.0, na = 15.0, k = 50.0),
        f("Snickers", "1 bar (50 g)", 245.0, 4.5, 30.0, 12.0, "Sweets", aka = "Snickers bar", fib = 1.0, sug = 25.0, sat = 4.5, na = 110.0, k = 150.0),
        f("Cadbury 5 Star", "1 bar (40 g)", 195.0, 1.6, 28.0, 8.4, "Sweets", aka = "Five Star, 5 Star", fib = 0.0, sug = 22.0, sat = 5.0, na = 60.0, k = 80.0),
        f("Nestle Munch", "1 bar (23 g)", 115.0, 1.4, 15.0, 5.6, "Sweets", aka = "Munch", fib = 0.3, sug = 10.0, sat = 3.5, na = 30.0, k = 50.0),
        f("Ferrero Rocher", "1 piece (12.5 g)", 70.0, 1.0, 5.5, 5.0, "Sweets", aka = "Ferrero", fib = 0.5, sug = 5.0, sat = 1.8, na = 5.0, k = 60.0)
    )

    private fun coolDrinks() = listOf(
        f("Coca-Cola", "1 bottle (300 ml)", 130.0, 0.0, 32.0, 0.0, "Drinks", aka = "Coke, Coca Cola", fib = 0.0, sug = 32.0, sat = 0.0, na = 30.0, k = 0.0),
        f("Pepsi", "1 bottle (300 ml)", 130.0, 0.0, 33.0, 0.0, "Drinks", aka = "Pepsi cola", fib = 0.0, sug = 33.0, sat = 0.0, na = 30.0, k = 0.0),
        f("Thums Up", "1 bottle (300 ml)", 130.0, 0.0, 32.0, 0.0, "Drinks", aka = "Thumbs up", fib = 0.0, sug = 32.0, sat = 0.0, na = 30.0, k = 0.0),
        f("Sprite", "1 bottle (300 ml)", 120.0, 0.0, 30.0, 0.0, "Drinks", aka = "Sprite lemon", fib = 0.0, sug = 30.0, sat = 0.0, na = 30.0, k = 0.0),
        f("7Up", "1 bottle (300 ml)", 120.0, 0.0, 30.0, 0.0, "Drinks", aka = "Seven Up, 7 Up", fib = 0.0, sug = 30.0, sat = 0.0, na = 30.0, k = 0.0),
        f("Fanta", "1 bottle (300 ml)", 140.0, 0.0, 35.0, 0.0, "Drinks", aka = "Fanta orange", fib = 0.0, sug = 35.0, sat = 0.0, na = 30.0, k = 0.0),
        f("Mirinda", "1 bottle (300 ml)", 135.0, 0.0, 34.0, 0.0, "Drinks", aka = "Mirinda orange", fib = 0.0, sug = 34.0, sat = 0.0, na = 30.0, k = 0.0),
        f("Bovonto", "1 bottle (300 ml)", 130.0, 0.0, 33.0, 0.0, "Drinks", aka = "Kali Mark Bovonto, Grape soda", fib = 0.0, sug = 33.0, sat = 0.0, na = 30.0, k = 0.0),
        f("Mountain Dew", "1 bottle (300 ml)", 140.0, 0.0, 35.0, 0.0, "Drinks", aka = "Dew", fib = 0.0, sug = 35.0, sat = 0.0, na = 30.0, k = 0.0),
        f("Diet Coke", "1 can (300 ml)", 0.0, 0.0, 0.0, 0.0, "Drinks", aka = "Coke Zero, Diet cola, Pepsi Black", fib = 0.0, sug = 0.0, sat = 0.0, na = 40.0, k = 0.0),
        f("Maaza", "1 bottle (250 ml)", 150.0, 0.0, 37.0, 0.0, "Drinks", aka = "Maaza mango drink", fib = 0.0, sug = 35.0, sat = 0.0, na = 30.0, k = 80.0, vc = 10.0),
        f("Frooti", "1 pack (200 ml)", 110.0, 0.0, 28.0, 0.0, "Drinks", aka = "Frooti mango", fib = 0.0, sug = 27.0, sat = 0.0, na = 20.0, k = 60.0),
        f("Slice mango drink", "1 bottle (250 ml)", 145.0, 0.0, 36.0, 0.0, "Drinks", aka = "Slice", fib = 0.0, sug = 34.0, sat = 0.0, na = 30.0, k = 80.0),
        f("Red Bull", "1 can (250 ml)", 110.0, 0.0, 27.5, 0.0, "Drinks", aka = "Redbull, Energy drink", fib = 0.0, sug = 27.5, sat = 0.0, na = 100.0, k = 0.0, b12 = 5.0),
        f("Monster Energy", "1 can (350 ml)", 155.0, 0.0, 38.5, 0.0, "Drinks", aka = "Monster", fib = 0.0, sug = 38.5, sat = 0.0, na = 270.0, k = 0.0, b12 = 6.0),
        f("Sting energy drink", "1 bottle (250 ml)", 120.0, 0.0, 30.0, 0.0, "Drinks", aka = "Sting", fib = 0.0, sug = 30.0, sat = 0.0, na = 80.0, k = 0.0),
        f("Gatorade", "1 bottle (500 ml)", 120.0, 0.0, 30.0, 0.0, "Drinks", aka = "Gatorade sports drink, Sports drink", fib = 0.0, sug = 28.0, sat = 0.0, na = 250.0, k = 60.0),
        f("Paper Boat aamras", "1 pack (200 ml)", 120.0, 0.3, 30.0, 0.0, "Juices", aka = "Paper Boat mango, Paper Boat", fib = 0.5, sug = 28.0, sat = 0.0, na = 20.0, k = 120.0, vc = 10.0),
        f("Real fruit juice", "1 glass (200 ml)", 105.0, 0.4, 26.0, 0.0, "Juices", aka = "Real juice, Real mixed fruit", fib = 0.0, sug = 25.0, sat = 0.0, na = 20.0, k = 150.0, vc = 20.0),
        f("Tropicana orange", "1 glass (200 ml)", 90.0, 1.0, 22.0, 0.0, "Juices", aka = "Tropicana juice", fib = 0.0, sug = 20.0, sat = 0.0, na = 10.0, k = 300.0, vc = 40.0),
        f("Glucon-D", "2 tbsp in water (25 g)", 100.0, 0.0, 24.5, 0.0, "Drinks", aka = "Glucon D, Glucose drink", fib = 0.0, sug = 24.5, sat = 0.0, na = 0.0, k = 0.0, ca = 20.0),
        f("Tang", "2 tbsp in water (25 g)", 95.0, 0.0, 24.0, 0.0, "Drinks", aka = "Tang orange, Tang drink", fib = 0.0, sug = 23.0, sat = 0.0, na = 20.0, k = 0.0, vc = 15.0),
        f("Horlicks with milk", "1 glass (200 ml + 20 g)", 200.0, 9.0, 26.0, 6.6, "Drinks", aka = "Horlicks", fib = 0.5, sug = 18.0, sat = 4.0, na = 150.0, k = 400.0, ca = 330.0, fe = 3.0),
        f("Boost with milk", "1 glass (200 ml + 20 g)", 195.0, 8.0, 26.0, 6.4, "Drinks", aka = "Boost", fib = 0.5, sug = 19.0, sat = 4.0, na = 140.0, k = 400.0, ca = 300.0, fe = 3.0)
    )

    private fun alcohol() = listOf(
        f("Mild beer", "1 bottle (330 ml)", 140.0, 1.5, 11.0, 0.0, "Alcohol", aka = "Kingfisher, Lager, Light beer", fib = 0.0, sug = 0.0, sat = 0.0, na = 15.0, k = 90.0),
        f("Strong beer", "1 can (500 ml)", 290.0, 2.5, 18.0, 0.0, "Alcohol", aka = "Kingfisher Strong, Haywards 5000, Knock Out", fib = 0.0, sug = 0.0, sat = 0.0, na = 20.0, k = 130.0),
        f("Beer, large bottle", "1 bottle (650 ml)", 280.0, 3.0, 21.0, 0.0, "Alcohol", aka = "Beer 650 ml, Pint bottle", fib = 0.0, sug = 0.0, sat = 0.0, na = 25.0, k = 170.0),
        f("Wheat beer", "1 bottle (330 ml)", 160.0, 1.6, 14.0, 0.0, "Alcohol", aka = "Bira White, Witbier, Craft beer", fib = 0.0, sug = 0.0, sat = 0.0, na = 15.0, k = 100.0),
        f("Whisky, small peg", "1 peg (30 ml)", 70.0, 0.0, 0.0, 0.0, "Alcohol", aka = "Whiskey, Peg, McDowell's, Royal Stag, Officer's Choice", fib = 0.0, sug = 0.0, sat = 0.0, na = 5.0, k = 0.0),
        f("Whisky, large peg", "1 large peg (60 ml)", 140.0, 0.0, 0.0, 0.0, "Alcohol", aka = "Whiskey large, Double peg", fib = 0.0, sug = 0.0, sat = 0.0, na = 5.0, k = 0.0),
        f("Rum", "1 peg (30 ml)", 65.0, 0.0, 0.0, 0.0, "Alcohol", aka = "Old Monk, Bacardi", fib = 0.0, sug = 0.0, sat = 0.0, na = 5.0, k = 0.0),
        f("Vodka", "1 peg (30 ml)", 65.0, 0.0, 0.0, 0.0, "Alcohol", aka = "Smirnoff, Magic Moments", fib = 0.0, sug = 0.0, sat = 0.0, na = 5.0, k = 0.0),
        f("Gin", "1 peg (30 ml)", 65.0, 0.0, 0.0, 0.0, "Alcohol", aka = "Bombay Sapphire, Greater Than", fib = 0.0, sug = 0.0, sat = 0.0, na = 5.0, k = 0.0),
        f("Brandy", "1 peg (30 ml)", 70.0, 0.0, 0.0, 0.0, "Alcohol", aka = "Mansion House, Honey Bee", fib = 0.0, sug = 0.0, sat = 0.0, na = 5.0, k = 0.0),
        f("Tequila shot", "1 shot (30 ml)", 65.0, 0.0, 0.0, 0.0, "Alcohol", aka = "Tequila", fib = 0.0, sug = 0.0, sat = 0.0, na = 5.0, k = 0.0),
        f("Red wine", "1 glass (150 ml)", 125.0, 0.1, 4.0, 0.0, "Alcohol", aka = "Wine, Sula red", fib = 0.0, sug = 1.0, sat = 0.0, na = 5.0, k = 190.0),
        f("White wine", "1 glass (150 ml)", 120.0, 0.1, 4.0, 0.0, "Alcohol", aka = "Sula white, Chardonnay", fib = 0.0, sug = 1.5, sat = 0.0, na = 5.0, k = 110.0),
        f("Bacardi Breezer", "1 bottle (275 ml)", 185.0, 0.0, 30.0, 0.0, "Alcohol", aka = "Breezer", fib = 0.0, sug = 30.0, sat = 0.0, na = 5.0, k = 0.0),
        f("Whisky with cola", "60 ml + 200 ml cola", 225.0, 0.0, 21.0, 0.0, "Alcohol", aka = "Whiskey coke, Whisky coke", fib = 0.0, sug = 21.0, sat = 0.0, na = 20.0, k = 0.0),
        f("Rum and cola", "60 ml + 200 ml cola", 215.0, 0.0, 21.0, 0.0, "Alcohol", aka = "Rum coke, Cuba libre", fib = 0.0, sug = 21.0, sat = 0.0, na = 20.0, k = 0.0),
        f("Gin and tonic", "60 ml + 200 ml tonic", 200.0, 0.0, 18.0, 0.0, "Alcohol", aka = "G and T, GnT", fib = 0.0, sug = 18.0, sat = 0.0, na = 20.0, k = 0.0),
        f("Toddy", "1 glass (250 ml)", 100.0, 0.5, 8.0, 0.0, "Alcohol", aka = "Kallu, Palm toddy, Pathaneer kallu", fib = 0.0, sug = 4.0, sat = 0.0, na = 5.0, k = 150.0)
    )

    private fun fishBySpecies() = listOf(
        f("Ayala fry", "1 fish (100 g)", 210.0, 19.0, 4.0, 13.0, "Protein", aka = "Mackerel fry, Bangda fry, Ayala meen varuval", fib = 0.0, sug = 0.0, sat = 3.0, na = 400.0, k = 350.0, ca = 40.0, fe = 1.5, o3 = 1.5),
        f("Mathi fry", "4 fish (100 g)", 235.0, 21.0, 4.0, 15.0, "Protein", aka = "Sardine fry, Chaalai meen fry, Mathi varuthathu", fib = 0.0, sug = 0.0, sat = 3.5, na = 400.0, k = 380.0, ca = 300.0, fe = 2.5, o3 = 1.4),
        f("Nandu fry", "1 plate (150 g)", 225.0, 22.0, 8.0, 12.0, "Protein", aka = "Crab fry, Nandu varuval", fib = 1.0, sug = 0.0, sat = 2.0, na = 600.0, k = 350.0, ca = 120.0, zn = 4.0),
        f("Kanava fry", "1 plate (150 g)", 270.0, 24.0, 14.0, 13.0, "Protein", aka = "Squid fry, Calamari fry, Koonthal fry", fib = 0.5, sug = 0.0, sat = 2.5, na = 500.0, k = 300.0, b12 = 1.5),
        f("Ayala curry", "1 bowl (200 g)", 230.0, 20.0, 7.0, 14.0, "Protein", aka = "Mackerel curry, Ayala meen kuzhambu", fib = 1.5, sug = 2.0, sat = 5.0, na = 650.0, k = 450.0, o3 = 1.4),
        f("Pomfret curry", "1 bowl (200 g)", 215.0, 22.0, 7.0, 11.0, "Protein", aka = "Vavval meen kuzhambu, Pomfret kuzhambu", fib = 1.0, sug = 2.0, sat = 4.0, na = 650.0, k = 450.0),
        f("Salmon curry", "1 bowl (200 g)", 290.0, 24.0, 6.0, 19.0, "Protein", aka = "Salmon masala, Kala meen curry", fib = 1.0, sug = 2.0, sat = 4.0, na = 600.0, k = 500.0, o3 = 2.0),
        f("Prawn coconut curry", "1 bowl (200 g)", 235.0, 20.0, 8.0, 14.0, "Protein", aka = "Eral thengai kuzhambu, Chemmeen curry, Prawn malai curry", fib = 1.5, sug = 2.0, sat = 9.0, na = 700.0, k = 350.0),
        f("Vanjaram grill", "1 slice (150 g)", 200.0, 30.0, 2.0, 8.0, "Protein", aka = "Seer fish grill, Surmai grill, Vanjaram tawa fry", fib = 0.0, sug = 0.0, sat = 1.8, na = 450.0, k = 500.0, o3 = 0.8),
        f("Tandoori pomfret", "1 fish (200 g)", 245.0, 34.0, 5.0, 10.0, "Protein", aka = "Pomfret tandoori", fib = 0.5, sug = 1.0, sat = 2.2, na = 600.0, k = 600.0),
        f("Tuna steak", "150 g", 215.0, 38.0, 0.0, 7.0, "Protein", aka = "Grilled tuna, Choora grill", fib = 0.0, sug = 0.0, sat = 1.6, na = 300.0, k = 550.0, b12 = 3.0, o3 = 1.0),
        f("Fish tikka", "6 pieces (150 g)", 245.0, 30.0, 5.0, 12.0, "Protein", aka = "Mahi tikka, Fish tikka kebab", fib = 0.5, sug = 1.5, sat = 2.5, na = 600.0, k = 500.0),
        f("Fish biryani", "1 plate (350 g)", 605.0, 28.0, 80.0, 20.0, "Rice", aka = "Meen biryani, Fish dum biryani", fib = 3.0, sug = 3.0, sat = 6.0, na = 900.0, k = 500.0),
        f("Koduva fry", "1 slice (120 g)", 220.0, 24.0, 4.0, 12.0, "Protein", aka = "Barramundi fry, Asian seabass fry", fib = 0.0, sug = 0.0, sat = 2.5, na = 450.0, k = 400.0),
        f("Sankara meen fry", "1 slice (120 g)", 210.0, 24.0, 4.0, 11.0, "Protein", aka = "Red snapper fry, Sankara fry", fib = 0.0, sug = 0.0, sat = 2.3, na = 450.0, k = 400.0),
        f("Paal sura puttu", "1 bowl (150 g)", 200.0, 24.0, 4.0, 10.0, "Protein", aka = "Shark puttu, Sura puttu", fib = 1.0, sug = 0.0, sat = 2.0, na = 500.0, k = 350.0)
    )

    private fun millets() = listOf(
        f("Thinai pongal", "1 bowl (200 g)", 220.0, 7.0, 34.0, 7.0, "Tiffin", aka = "Foxtail millet pongal", fib = 3.0, sug = 0.5, sat = 3.5, na = 380.0, k = 180.0, fe = 1.5),
        f("Samai upma", "1 bowl (200 g)", 195.0, 5.0, 30.0, 7.0, "Tiffin", aka = "Little millet upma", fib = 3.5, sug = 1.0, sat = 1.0, na = 380.0, k = 150.0, fe = 2.0),
        f("Varagu sambar sadam", "1 plate (250 g)", 255.0, 8.0, 42.0, 7.0, "Rice", aka = "Kodo millet sambar rice, Varagu bisibelabath", fib = 5.0, sug = 3.0, sat = 2.0, na = 550.0, k = 350.0),
        f("Kuthiraivali rice", "1 cup cooked (150 g)", 155.0, 4.0, 34.0, 1.5, "Grains", aka = "Barnyard millet rice, Kuthiraivali sadam", fib = 5.0, sug = 0.0, sat = 0.0, na = 5.0, k = 90.0, fe = 2.5),
        f("Thinai payasam", "1 cup (150 g)", 240.0, 5.0, 38.0, 8.0, "Sweets", aka = "Foxtail millet kheer, Thinai kheer", fib = 1.5, sug = 22.0, sat = 5.0, na = 40.0, k = 200.0, ca = 100.0),
        f("Millet idli", "2 idli (100 g)", 125.0, 4.0, 26.0, 1.0, "Idli", aka = "Siruthaniya idli, Multi millet idli", fib = 2.5, sug = 0.0, sat = 0.0, na = 200.0, k = 100.0, fe = 1.0),
        f("Ragi puttu", "1 cup (150 g)", 185.0, 4.5, 38.0, 3.0, "Tiffin", aka = "Kezhvaragu puttu", fib = 5.0, sug = 4.0, sat = 2.0, na = 20.0, k = 200.0, ca = 220.0, fe = 2.0),
        f("Kambu sadam", "1 cup (200 g)", 185.0, 6.0, 38.0, 2.0, "Grains", aka = "Pearl millet rice, Bajra rice, Kambu choru", fib = 5.0, sug = 0.0, sat = 0.0, na = 10.0, k = 150.0, fe = 3.0, mg = 60.0),
        f("Ragi koozh", "1 glass (250 ml)", 115.0, 3.0, 22.0, 2.5, "Drinks", aka = "Kezhvaragu koozh, Ragi kool", fib = 3.0, sug = 2.0, sat = 1.5, na = 250.0, k = 180.0, ca = 150.0),
        f("Ragi roti", "1 roti (60 g)", 130.0, 3.0, 24.0, 3.0, "Tiffin", aka = "Ragi rotti, Kezhvaragu roti", fib = 3.0, sug = 0.0, sat = 0.5, na = 150.0, k = 120.0, ca = 120.0)
    )

    private fun breakfastVariants() = listOf(
        f("Kara kozhukattai", "4 pieces (150 g)", 205.0, 5.0, 34.0, 6.0, "Tiffin", aka = "Ulundu kozhukattai, Spicy kozhukattai", fib = 2.0, sug = 0.0, sat = 1.0, na = 350.0, k = 100.0),
        f("Pidi kozhukattai", "4 pieces (150 g)", 205.0, 4.5, 36.0, 5.0, "Tiffin", aka = "Uppu kozhukattai", fib = 2.0, sug = 0.0, sat = 1.5, na = 300.0, k = 100.0),
        f("Keerai adai", "1 adai (120 g)", 200.0, 9.0, 30.0, 6.0, "Tiffin", aka = "Spinach adai, Murungai keerai adai", fib = 5.0, sug = 0.0, sat = 1.0, na = 300.0, k = 350.0, ca = 80.0, fe = 3.0, va = 150.0),
        f("Idiyappam with kurma", "3 idiyappam + kurma", 320.0, 7.0, 50.0, 11.0, "Tiffin", aka = "Idiyappam veg kurma, Sevai kurma", fib = 4.0, sug = 3.0, sat = 5.0, na = 450.0, k = 250.0),
        f("Kara paniyaram", "6 pieces (120 g)", 210.0, 5.0, 30.0, 8.0, "Tiffin", aka = "Spicy paniyaram, Kara kuzhi paniyaram", fib = 2.0, sug = 0.0, sat = 1.2, na = 350.0, k = 120.0),
        f("Ragi idiyappam", "3 pieces (120 g)", 160.0, 3.5, 36.0, 1.0, "Tiffin", aka = "Kezhvaragu idiyappam", fib = 4.0, sug = 0.0, sat = 0.0, na = 10.0, k = 150.0, ca = 180.0),
        f("Mini idli sambar", "14 mini idli + sambar", 255.0, 9.0, 46.0, 5.0, "Idli", aka = "Sambar mini idli, 14 idli", fib = 5.0, sug = 3.0, sat = 1.5, na = 700.0, k = 350.0),
        f("Sevai upma", "1 plate (200 g)", 240.0, 5.0, 42.0, 6.0, "Tiffin", aka = "Lemon sevai, Rice sevai, Santhakai", fib = 2.0, sug = 0.0, sat = 1.0, na = 350.0, k = 100.0),
        f("Adai aviyal", "1 adai + aviyal", 320.0, 12.0, 44.0, 12.0, "Tiffin", aka = "Adai with aviyal", fib = 7.0, sug = 3.0, sat = 5.0, na = 500.0, k = 500.0, fe = 3.0)
    )

    private fun festivalFoods() = listOf(
        f("Nombu kanji", "1 bowl (250 g)", 210.0, 8.0, 32.0, 6.0, "Tiffin", aka = "Ramzan kanji, Nonbu kanji", fib = 2.0, sug = 0.0, sat = 2.5, na = 450.0, k = 200.0),
        f("Karthigai pori urundai", "2 balls (50 g)", 170.0, 1.5, 40.0, 0.5, "Sweets", aka = "Pori urundai, Nel pori, Karthigai pori", fib = 1.0, sug = 30.0, sat = 0.0, na = 20.0, k = 100.0, fe = 1.5),
        f("Nei appam", "3 pieces (90 g)", 270.0, 3.0, 40.0, 11.0, "Sweets", aka = "Ghee appam", fib = 1.5, sug = 20.0, sat = 6.0, na = 30.0, k = 120.0),
        f("Pongal kuzhambu", "1 bowl (200 g)", 170.0, 6.0, 24.0, 7.0, "Sides", aka = "Ezhu kari kuzhambu, Thai pongal kootu", fib = 6.0, sug = 5.0, sat = 2.0, na = 500.0, k = 500.0, va = 100.0),
        f("Mullu murukku", "3 pieces (30 g)", 155.0, 2.5, 18.0, 8.5, "Snacks", aka = "Spiky murukku", fib = 1.0, sug = 0.0, sat = 1.5, na = 180.0, k = 50.0),
        f("Manoharam", "2 pieces (40 g)", 180.0, 2.0, 28.0, 7.0, "Sweets", aka = "Manohara, Sweet murukku", fib = 1.0, sug = 16.0, sat = 1.2, na = 60.0, k = 60.0),
        f("Sweet somas", "2 pieces (60 g)", 255.0, 3.0, 32.0, 13.0, "Sweets", aka = "Somasi, Kajjikayalu", fib = 1.5, sug = 14.0, sat = 3.0, na = 50.0, k = 80.0),
        f("Kai murukku", "2 pieces (35 g)", 175.0, 2.5, 22.0, 9.0, "Snacks", aka = "Hand murukku, Suttu murukku", fib = 1.0, sug = 0.0, sat = 1.6, na = 200.0, k = 50.0),
        f("Kulkuls", "10 pieces (35 g)", 165.0, 2.0, 20.0, 8.5, "Sweets", aka = "Kalkal, Christmas kulkul", fib = 0.6, sug = 6.0, sat = 2.5, na = 50.0, k = 40.0),
        f("Uppu seedai", "15 pieces (30 g)", 160.0, 2.0, 18.0, 9.0, "Snacks", aka = "Seedai, Gokulashtami seedai", fib = 1.0, sug = 0.0, sat = 4.0, na = 180.0, k = 50.0),
        f("Vella seedai", "10 pieces (30 g)", 145.0, 1.5, 20.0, 7.0, "Sweets", aka = "Sweet seedai", fib = 1.0, sug = 8.0, sat = 3.0, na = 20.0, k = 50.0),
        f("Paal kozhukattai", "1 bowl (150 g)", 210.0, 4.0, 36.0, 6.0, "Sweets", aka = "Milk kozhukattai", fib = 1.0, sug = 16.0, sat = 3.5, na = 40.0, k = 180.0, ca = 120.0)
    )

    private fun supplementBrands() = listOf(
        f("C4 pre-workout", "1 scoop (6.5 g)", 5.0, 0.0, 1.0, 0.0, "Supplement", aka = "Cellucor C4, C4 original", fib = 0.0, sug = 0.0, sat = 0.0, na = 150.0, k = 0.0, supp = true),
        f("MuscleBlaze PRE workout", "1 scoop (10 g)", 10.0, 0.0, 2.0, 0.0, "Supplement", aka = "MB pre workout, MB PRE", fib = 0.0, sug = 0.0, sat = 0.0, na = 120.0, k = 0.0, supp = true),
        f("Xtend BCAA", "1 scoop (13 g)", 30.0, 7.0, 0.0, 0.0, "Supplement", aka = "Scivation Xtend", fib = 0.0, sug = 0.0, sat = 0.0, na = 180.0, k = 150.0, supp = true),
        f("MuscleBlaze BCAA Pro", "1 scoop (10 g)", 30.0, 7.0, 0.0, 0.0, "Supplement", aka = "MB BCAA", fib = 0.0, sug = 0.0, sat = 0.0, na = 100.0, k = 0.0, supp = true),
        f("Fast&Up Reload", "1 tablet in water", 15.0, 0.0, 3.5, 0.0, "Supplement", aka = "Fast and Up electrolyte, Fast&Up", fib = 0.0, sug = 1.0, sat = 0.0, na = 200.0, k = 80.0, mg = 20.0, supp = true),
        f("MuscleBlaze fish oil", "1 softgel (1000 mg)", 10.0, 0.0, 0.0, 1.0, "Supplement", aka = "MB fish oil, MB omega 3", fib = 0.0, sug = 0.0, sat = 0.3, na = 0.0, k = 0.0, o3 = 0.3, supp = true),
        f("HK Vitals multivitamin", "1 tablet", 0.0, 0.0, 0.0, 0.0, "Supplement", aka = "HealthKart multivitamin, HK multivitamin", fib = 0.0, sug = 0.0, sat = 0.0, na = 0.0, k = 0.0, zn = 10.0, vc = 40.0, vd = 10.0, b12 = 1.0, supp = true),
        f("Ensure", "1 serving (55 g in water)", 240.0, 9.3, 33.0, 8.0, "Supplement", aka = "Abbott Ensure, Ensure powder", fib = 1.0, sug = 10.0, sat = 1.0, na = 200.0, k = 400.0, ca = 200.0, fe = 3.0, vd = 2.5, supp = true),
        f("Protinex original", "2 scoops (30 g)", 100.0, 9.3, 15.0, 0.3, "Supplement", aka = "Protinex", fib = 0.0, sug = 6.0, sat = 0.0, na = 150.0, k = 350.0, ca = 200.0, fe = 3.0, supp = true),
        f("Ultimate Nutrition Prostar whey", "1 scoop (30 g)", 120.0, 25.0, 1.5, 1.5, "Supplement", aka = "Prostar whey, UN whey", fib = 0.0, sug = 1.0, sat = 1.0, na = 70.0, k = 130.0, ca = 100.0, supp = true),
        f("Oziva Protein & Herbs, women", "1 scoop (35 g)", 125.0, 23.0, 5.0, 1.5, "Supplement", aka = "Oziva women protein", fib = 1.0, sug = 1.0, sat = 0.5, na = 250.0, k = 150.0, fe = 5.0, supp = true)
    )

    private fun homeDishesByWeight() = listOf(
        f("Chicken curry, per 100 g", "100 g", 145.0, 13.0, 4.0, 9.0, "Protein", aka = "Home chicken curry, Kozhi kuzhambu by weight", fib = 0.8, sug = 1.5, sat = 2.2, na = 350.0, k = 250.0),
        f("Mutton curry, per 100 g", "100 g", 175.0, 13.0, 3.5, 12.5, "Protein", aka = "Home mutton curry, Kari kuzhambu by weight", fib = 0.6, sug = 1.2, sat = 4.5, na = 350.0, k = 250.0, zn = 2.5),
        f("Fish curry, per 100 g", "100 g", 110.0, 12.0, 4.0, 5.5, "Protein", aka = "Meen kuzhambu by weight", fib = 0.6, sug = 1.5, sat = 1.8, na = 350.0, k = 250.0),
        f("Egg curry, per 100 g", "100 g", 135.0, 8.0, 4.0, 10.0, "Protein", aka = "Muttai kuzhambu by weight", fib = 0.6, sug = 1.5, sat = 2.5, na = 300.0, k = 150.0),
        f("Dal, per 100 g", "100 g", 95.0, 5.5, 13.0, 3.0, "Legumes", aka = "Paruppu by weight, Dal tadka by weight", fib = 3.0, sug = 1.0, sat = 1.2, na = 250.0, k = 250.0, fe = 1.5),
        f("Sambar, per 100 g", "100 g", 55.0, 2.8, 8.0, 2.0, "Sides", aka = "Sambar by weight", fib = 2.2, sug = 1.5, sat = 0.4, na = 280.0, k = 200.0),
        f("Rice, cooked, per 100 g", "100 g", 125.0, 2.7, 28.0, 0.3, "Rice", aka = "White rice by weight, Saadam by weight", fib = 0.4, sug = 0.0, sat = 0.0, na = 1.0, k = 35.0),
        f("Poriyal, per 100 g", "100 g", 85.0, 2.5, 9.0, 5.0, "Vegetables", aka = "Vegetable stir fry by weight, Thoran by weight", fib = 3.0, sug = 3.0, sat = 2.5, na = 200.0, k = 250.0),
        f("Chicken biryani, per 100 g", "100 g", 165.0, 8.0, 20.0, 6.0, "Rice", aka = "Biryani by weight", fib = 0.8, sug = 0.8, sat = 2.0, na = 300.0, k = 150.0),
        f("Curd rice, per 100 g", "100 g", 100.0, 3.0, 17.0, 2.5, "Rice", aka = "Thayir sadam by weight", fib = 0.3, sug = 2.0, sat = 1.5, na = 180.0, k = 100.0, ca = 60.0),
        f("Paneer curry, per 100 g", "100 g", 160.0, 7.0, 7.0, 12.0, "North Indian", aka = "Paneer gravy by weight", fib = 1.0, sug = 3.0, sat = 6.0, na = 350.0, k = 150.0, ca = 150.0),
        f("Rasam, per 100 g", "100 g", 35.0, 1.0, 5.0, 1.2, "Sides", aka = "Rasam by weight", fib = 0.8, sug = 1.0, sat = 0.3, na = 300.0, k = 120.0)
    )

    private fun chainMenus() = listOf(
        f("McAloo Tikki burger", "1 burger (145 g)", 330.0, 8.0, 50.0, 12.0, "Continental", aka = "McDonalds aloo tikki, McAloo", fib = 4.0, sug = 6.0, sat = 4.0, na = 700.0, k = 300.0),
        f("McChicken burger", "1 burger (170 g)", 405.0, 15.0, 45.0, 19.0, "Continental", aka = "McDonalds McChicken", fib = 3.0, sug = 5.0, sat = 4.0, na = 800.0, k = 300.0),
        f("McSpicy chicken burger", "1 burger (200 g)", 490.0, 22.0, 48.0, 24.0, "Continental", aka = "McSpicy", fib = 3.0, sug = 5.0, sat = 5.0, na = 1000.0, k = 350.0),
        f("Chicken Maharaja Mac", "1 burger (300 g)", 650.0, 30.0, 62.0, 32.0, "Continental", aka = "Maharaja Mac, McDonalds Maharaja", fib = 4.0, sug = 8.0, sat = 8.0, na = 1300.0, k = 450.0),
        f("McVeggie burger", "1 burger (165 g)", 370.0, 10.0, 48.0, 16.0, "Continental", aka = "McDonalds veggie burger", fib = 4.0, sug = 6.0, sat = 4.0, na = 750.0, k = 300.0),
        f("McDonald's fries, medium", "1 medium (110 g)", 330.0, 4.0, 42.0, 17.0, "Snacks", aka = "McD fries, Medium fries", fib = 4.0, sug = 0.0, sat = 2.5, na = 250.0, k = 600.0),
        f("Chicken McNuggets, 6", "6 pieces (100 g)", 255.0, 15.0, 16.0, 15.0, "Protein", aka = "McNuggets, Nuggets 6 piece", fib = 1.0, sug = 0.0, sat = 2.5, na = 550.0, k = 250.0),
        f("McFlurry Oreo", "1 regular (150 g)", 310.0, 7.0, 46.0, 11.0, "Sweets", aka = "McFlurry", fib = 0.5, sug = 36.0, sat = 6.0, na = 180.0, k = 300.0, ca = 200.0),
        f("KFC Hot & Crispy chicken", "1 piece (125 g)", 265.0, 20.0, 8.0, 17.0, "Protein", aka = "KFC chicken piece, Hot and crispy", fib = 0.5, sug = 0.0, sat = 4.0, na = 700.0, k = 250.0),
        f("KFC Zinger burger", "1 burger (200 g)", 490.0, 24.0, 50.0, 22.0, "Continental", aka = "KFC zinger", fib = 3.0, sug = 5.0, sat = 4.5, na = 1100.0, k = 350.0),
        f("KFC rice bowl, chicken", "1 bowl (320 g)", 465.0, 20.0, 62.0, 16.0, "Meals", aka = "KFC rice bowlz", fib = 3.0, sug = 4.0, sat = 3.5, na = 1200.0, k = 350.0),
        f("KFC popcorn chicken", "1 regular (90 g)", 245.0, 16.0, 14.0, 14.0, "Protein", aka = "KFC popcorn", fib = 0.5, sug = 0.0, sat = 3.0, na = 650.0, k = 220.0),
        f("Domino's Margherita slice", "1 slice, regular (75 g)", 125.0, 5.0, 17.0, 4.5, "Italian", aka = "Dominos margherita", fib = 1.0, sug = 1.5, sat = 2.3, na = 280.0, k = 80.0, ca = 90.0),
        f("Domino's Peppy Paneer slice", "1 slice, regular (85 g)", 155.0, 6.5, 17.0, 7.0, "Italian", aka = "Dominos peppy paneer", fib = 1.0, sug = 2.0, sat = 3.5, na = 320.0, k = 100.0, ca = 110.0),
        f("Domino's Pepper BBQ chicken slice", "1 slice, regular (85 g)", 150.0, 7.0, 17.0, 6.0, "Italian", aka = "Dominos pepper barbecue chicken", fib = 1.0, sug = 2.5, sat = 2.8, na = 380.0, k = 100.0),
        f("Domino's Farmhouse slice", "1 slice, regular (85 g)", 135.0, 5.0, 17.0, 5.5, "Italian", aka = "Dominos farmhouse", fib = 1.3, sug = 2.0, sat = 2.6, na = 300.0, k = 110.0),
        f("Domino's choco lava cake", "1 cake (100 g)", 285.0, 4.0, 34.0, 15.0, "Sweets", aka = "Dominos lava cake", fib = 1.5, sug = 24.0, sat = 8.0, na = 180.0, k = 150.0),
        f("Domino's garlic breadsticks", "4 pieces (100 g)", 295.0, 8.0, 42.0, 11.0, "Italian", aka = "Dominos garlic bread", fib = 2.0, sug = 2.0, sat = 4.5, na = 500.0, k = 120.0),
        f("Subway paneer tikka sub, 6 inch", "1 sub (230 g)", 365.0, 17.0, 50.0, 12.0, "Continental", aka = "Subway paneer", fib = 5.0, sug = 7.0, sat = 5.0, na = 900.0, k = 350.0, ca = 250.0),
        f("Subway chicken teriyaki sub, 6 inch", "1 sub (230 g)", 350.0, 25.0, 52.0, 6.0, "Continental", aka = "Subway chicken, Subway teriyaki", fib = 5.0, sug = 10.0, sat = 1.5, na = 950.0, k = 400.0),
        f("Subway Veggie Delite, 6 inch", "1 sub (180 g)", 215.0, 8.0, 42.0, 3.0, "Continental", aka = "Subway veg", fib = 5.0, sug = 6.0, sat = 0.6, na = 450.0, k = 300.0),
        f("Subway roast chicken salad", "1 bowl (300 g)", 140.0, 20.0, 10.0, 3.0, "Protein", aka = "Subway salad", fib = 4.0, sug = 5.0, sat = 0.8, na = 600.0, k = 500.0, vc = 20.0),
        f("Burger King Whopper", "1 burger (270 g)", 630.0, 28.0, 50.0, 36.0, "Continental", aka = "Whopper, BK Whopper", fib = 3.0, sug = 11.0, sat = 11.0, na = 1000.0, k = 450.0),
        f("Burger King Veg Whopper", "1 burger (250 g)", 510.0, 14.0, 58.0, 26.0, "Continental", aka = "Veg Whopper", fib = 5.0, sug = 10.0, sat = 5.0, na = 950.0, k = 400.0),
        f("Burger King crispy veg", "1 burger (150 g)", 340.0, 8.0, 45.0, 15.0, "Continental", aka = "BK crispy veg", fib = 4.0, sug = 6.0, sat = 3.0, na = 700.0, k = 250.0),
        f("Starbucks caffe latte, tall", "1 tall (354 ml)", 185.0, 12.0, 18.0, 7.0, "Drinks", aka = "Starbucks latte", fib = 0.0, sug = 17.0, sat = 4.5, na = 170.0, k = 500.0, ca = 400.0),
        f("Starbucks cappuccino, tall", "1 tall (354 ml)", 115.0, 7.5, 11.0, 4.5, "Drinks", aka = "Starbucks cappuccino", fib = 0.0, sug = 10.0, sat = 2.8, na = 110.0, k = 330.0, ca = 250.0),
        f("Java chip frappuccino, tall", "1 tall (354 ml)", 365.0, 5.0, 55.0, 14.0, "Drinks", aka = "Starbucks frappuccino", fib = 1.0, sug = 50.0, sat = 9.0, na = 200.0, k = 350.0, ca = 180.0),
        f("Caramel macchiato, tall", "1 tall (354 ml)", 195.0, 8.0, 27.0, 6.0, "Drinks", aka = "Starbucks caramel macchiato", fib = 0.0, sug = 25.0, sat = 3.5, na = 120.0, k = 380.0, ca = 280.0),
        f("A2B mini tiffin", "1 plate", 705.0, 16.0, 100.0, 28.0, "Meals", aka = "Adyar Ananda Bhavan mini tiffin, Saravana mini tiffin, Mini tiffin", fib = 6.0, sug = 15.0, sat = 10.0, na = 1200.0, k = 500.0)
    )
}
