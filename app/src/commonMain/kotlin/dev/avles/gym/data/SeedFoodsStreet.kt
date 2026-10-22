package dev.avles.gym.data

/**
 * Street food, fast food, and the cuisines the catalogue was still thin on: chaat
 * and rolls from the carts, burgers, fried chicken and pizza by the slice from the
 * chains, Kerala, Andhra and Hyderabadi dishes, Arabian mandi, instant noodles and
 * cereal, and the bowls and shakes people eat for training.
 *
 * A portion is what the counter hands over: one burger, one slice, one roll, one
 * plate. Fast-food numbers are close to what the chains publish for their regular
 * sizes; street food varies with the cart. Calories are derived from the macros
 * (4/4/9, fibre at 2).
 */
internal object SeedFoodsStreet {

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

    fun all(): List<FoodItem> = streetFood() + fastFood() + kerala() + andhraAndHyderabadi() + arabian() + instantAndPackaged() + healthyBowlsAndGymFood()

    private fun streetFood() = listOf(
        f("Aloo tikki chaat", "1 plate", 355.0, 7.0, 48.0, 16.0, "Snacks", aka = "Tikki chaat", fib = 5.0, sug = 8.0, sat = 3.0, na = 700.0, k = 600.0),
        f("Samosa chaat", "1 plate", 455.0, 10.0, 58.0, 22.0, "Snacks", fib = 7.0, sug = 10.0, sat = 4.0, na = 900.0, k = 500.0),
        f("Papdi chaat", "1 plate", 350.0, 8.0, 45.0, 16.0, "Snacks", aka = "Dahi papdi chaat", fib = 4.0, sug = 12.0, sat = 3.0, na = 750.0, k = 300.0),
        f("Raj kachori", "1 piece", 490.0, 12.0, 60.0, 24.0, "Snacks", fib = 8.0, sug = 12.0, sat = 4.0, na = 900.0, k = 450.0),
        f("Ragda pattice", "1 plate", 350.0, 10.0, 50.0, 14.0, "Snacks", aka = "Ragda patties", fib = 8.0, sug = 6.0, sat = 2.5, na = 800.0, k = 600.0),
        f("Dabeli", "1 piece", 290.0, 6.0, 42.0, 12.0, "Snacks", aka = "Kutchi dabeli", fib = 4.0, sug = 10.0, sat = 3.0, na = 500.0, k = 300.0),
        f("Misal pav", "1 plate", 465.0, 16.0, 60.0, 20.0, "Snacks", fib = 10.0, sug = 6.0, sat = 4.0, na = 1100.0, k = 650.0),
        f("Pav", "2 pav", 210.0, 6.0, 38.0, 4.0, "Grains", aka = "Ladi pav, Bun pav", fib = 1.5, sug = 4.0, sat = 1.5, na = 380.0, k = 80.0),
        f("Egg roll", "1 roll", 380.0, 14.0, 42.0, 18.0, "Snacks", aka = "Egg kathi roll, Egg frankie", fib = 2.0, sug = 3.0, sat = 4.0, na = 700.0, k = 200.0, b12 = 0.6),
        f("Paneer kathi roll", "1 roll", 440.0, 18.0, 44.0, 22.0, "Snacks", aka = "Paneer roll, Paneer frankie", fib = 3.0, sug = 4.0, sat = 9.0, na = 800.0, k = 220.0, ca = 280.0),
        f("Chicken tikka roll", "1 roll", 435.0, 26.0, 44.0, 18.0, "Snacks", aka = "Chicken kathi roll, Chicken frankie", fib = 3.0, sug = 4.0, sat = 4.0, na = 900.0, k = 350.0, b12 = 0.3),
        f("Burma atho", "1 plate", 390.0, 8.0, 60.0, 14.0, "Snacks", aka = "Atho, Burmese noodles", fib = 4.0, sug = 4.0, sat = 2.0, na = 1000.0, k = 300.0),
        f("Kaalan", "1 plate", 260.0, 5.0, 30.0, 14.0, "Snacks", aka = "Kalan, Street mushroom masala", fib = 4.0, sug = 3.0, sat = 2.0, na = 800.0, k = 400.0),
        f("Chicken pakoda", "100 g", 290.0, 18.0, 14.0, 18.0, "Protein", aka = "Chicken pakora", fib = 1.0, sug = 0.0, sat = 3.0, na = 600.0, k = 280.0, b12 = 0.3),
        f("Tandoori momos", "6 pieces", 305.0, 16.0, 30.0, 14.0, "Chinese", fib = 1.5, sug = 2.0, sat = 4.0, na = 700.0, k = 220.0),
        f("Chole kulche", "1 plate", 485.0, 16.0, 70.0, 18.0, "North Indian", aka = "Chole kulcha", fib = 10.0, sug = 5.0, sat = 4.0, na = 900.0, k = 500.0, fe = 4.0),
        f("Batata vada", "2 pieces", 240.0, 4.0, 30.0, 12.0, "Snacks", aka = "Aloo bonda", fib = 3.0, sug = 1.0, sat = 2.0, na = 400.0, k = 400.0),
        f("Sabudana vada", "2 pieces", 255.0, 3.0, 34.0, 12.0, "Snacks", aka = "Sago vada", fib = 1.5, sug = 1.0, sat = 2.0, na = 300.0, k = 150.0),
        f("Chana chaat", "1 bowl", 185.0, 10.0, 32.0, 4.0, "Snacks", aka = "Chickpea chaat, Sundal chaat", fib = 9.0, sug = 4.0, sat = 0.6, na = 500.0, k = 400.0, fe = 2.5),
        f("Fruit chaat", "1 bowl", 140.0, 2.0, 34.0, 0.6, "Snacks", aka = "Fruit bowl with chaat masala", fib = 5.0, sug = 26.0, sat = 0.0, na = 200.0, k = 450.0, vc = 50.0)
    )

    private fun fastFood() = listOf(
        f("Veg burger", "1 burger", 340.0, 9.0, 46.0, 14.0, "Continental", aka = "Aloo tikki burger", fib = 4.0, sug = 7.0, sat = 3.0, na = 700.0, k = 300.0),
        f("Chicken burger", "1 burger", 440.0, 22.0, 44.0, 20.0, "Continental", fib = 2.5, sug = 7.0, sat = 4.5, na = 950.0, k = 350.0, b12 = 0.3),
        f("Crispy chicken burger", "1 burger", 525.0, 24.0, 50.0, 26.0, "Continental", aka = "Zinger burger, Fried chicken burger", fib = 3.0, sug = 7.0, sat = 5.0, na = 1100.0, k = 380.0, b12 = 0.3),
        f("Double patty burger", "1 burger", 590.0, 32.0, 44.0, 32.0, "Continental", aka = "Double cheeseburger", fib = 2.0, sug = 8.0, sat = 13.0, na = 1200.0, k = 450.0, b12 = 2.0),
        f("Fried chicken", "1 piece", 255.0, 20.0, 8.0, 16.0, "Continental", aka = "KFC style chicken, Fried chicken drumstick", fib = 0.5, sug = 0.0, sat = 4.0, na = 600.0, k = 220.0, b12 = 0.3),
        f("Chicken popcorn", "1 regular", 310.0, 18.0, 20.0, 18.0, "Continental", aka = "Popcorn chicken", fib = 1.0, sug = 0.0, sat = 3.5, na = 800.0, k = 250.0),
        f("Chicken wings", "6 wings", 395.0, 30.0, 6.0, 28.0, "Continental", aka = "Hot wings, Buffalo wings", fib = 0.5, sug = 3.0, sat = 8.0, na = 1000.0, k = 300.0, b12 = 0.5),
        f("Chicken strips", "3 strips", 275.0, 22.0, 16.0, 14.0, "Continental", aka = "Chicken tenders", fib = 1.0, sug = 0.0, sat = 3.0, na = 700.0, k = 280.0),
        f("Peri peri fries", "1 medium", 345.0, 4.0, 44.0, 18.0, "Continental", aka = "Masala fries", fib = 4.0, sug = 0.5, sat = 3.0, na = 500.0, k = 700.0),
        f("Cheese fries", "1 serving", 420.0, 9.0, 44.0, 24.0, "Continental", aka = "Loaded fries", fib = 4.0, sug = 1.0, sat = 10.0, na = 700.0, k = 650.0, ca = 200.0),
        f("Potato wedges", "1 medium", 280.0, 4.0, 36.0, 14.0, "Continental", fib = 3.5, sug = 0.5, sat = 2.5, na = 500.0, k = 650.0),
        f("Onion rings", "8 rings", 320.0, 4.0, 36.0, 18.0, "Continental", fib = 2.0, sug = 4.0, sat = 3.0, na = 500.0, k = 150.0),
        f("Veg pizza", "1 slice", 220.0, 8.0, 28.0, 9.0, "Italian", aka = "Farmhouse pizza, Veggie pizza", fib = 2.0, sug = 3.0, sat = 4.0, na = 500.0, k = 180.0, ca = 140.0),
        f("Chicken pizza", "1 slice", 255.0, 12.0, 28.0, 11.0, "Italian", aka = "Chicken tikka pizza, BBQ chicken pizza", fib = 2.0, sug = 3.0, sat = 4.5, na = 650.0, k = 200.0, ca = 140.0),
        f("Paneer pizza", "1 slice", 245.0, 10.0, 28.0, 11.0, "Italian", aka = "Paneer tikka pizza", fib = 2.0, sug = 3.0, sat = 5.5, na = 550.0, k = 170.0, ca = 200.0),
        f("Cheese burst pizza", "1 slice", 295.0, 11.0, 30.0, 15.0, "Italian", fib = 1.5, sug = 3.0, sat = 7.0, na = 700.0, k = 170.0, ca = 220.0),
        f("Garlic bread", "2 pieces", 245.0, 5.0, 30.0, 12.0, "Italian", fib = 1.5, sug = 2.0, sat = 5.0, na = 400.0, k = 80.0),
        f("Cheesy garlic bread", "2 pieces", 295.0, 9.0, 30.0, 16.0, "Italian", aka = "Garlic bread with cheese", fib = 1.5, sug = 2.0, sat = 8.0, na = 550.0, k = 90.0, ca = 180.0),
        f("Mac and cheese", "1 bowl", 440.0, 16.0, 50.0, 20.0, "Continental", aka = "Macaroni and cheese", fib = 2.0, sug = 6.0, sat = 10.0, na = 800.0, k = 250.0, ca = 300.0),
        f("Hot dog", "1 hot dog", 295.0, 12.0, 26.0, 16.0, "Continental", fib = 1.0, sug = 4.0, sat = 6.0, na = 800.0, k = 180.0),
        f("Chicken wrap", "1 wrap", 410.0, 24.0, 40.0, 18.0, "Continental", aka = "Grilled chicken wrap", fib = 3.0, sug = 3.0, sat = 4.0, na = 900.0, k = 380.0),
        f("Paneer wrap", "1 wrap", 425.0, 18.0, 40.0, 22.0, "Continental", fib = 3.0, sug = 3.0, sat = 9.0, na = 800.0, k = 250.0, ca = 280.0),
        f("Veg sub", "6 inch", 265.0, 9.0, 46.0, 6.0, "Continental", aka = "Veggie sub, Subway veg", fib = 5.0, sug = 7.0, sat = 1.5, na = 700.0, k = 400.0),
        f("Chicken sub", "6 inch", 340.0, 24.0, 46.0, 8.0, "Continental", aka = "Chicken teriyaki sub, Subway chicken", fib = 5.0, sug = 7.0, sat = 2.5, na = 900.0, k = 450.0),
        f("Chicken quesadilla", "1 quesadilla", 440.0, 26.0, 36.0, 22.0, "Mexican", aka = "Quesadilla", fib = 2.0, sug = 2.0, sat = 10.0, na = 900.0, k = 300.0, ca = 300.0),
        f("Chicken burrito bowl", "1 bowl", 560.0, 34.0, 70.0, 18.0, "Mexican", aka = "Burrito bowl", fib = 10.0, sug = 4.0, sat = 6.0, na = 1200.0, k = 800.0),
        f("Fish fingers", "6 pieces", 270.0, 14.0, 22.0, 14.0, "Continental", fib = 1.0, sug = 1.0, sat = 2.5, na = 500.0, k = 250.0),
        f("Grilled cheese sandwich", "1 sandwich", 310.0, 12.0, 30.0, 16.0, "Continental", aka = "Cheese sandwich", fib = 1.5, sug = 3.0, sat = 9.0, na = 750.0, k = 120.0, ca = 300.0),
        f("Chocolate sundae", "1 cup", 320.0, 6.0, 48.0, 12.0, "Sweets", aka = "Hot fudge sundae", fib = 1.0, sug = 40.0, sat = 7.0, na = 150.0, k = 300.0, ca = 180.0),
        f("Fried apple pie", "1 pie", 245.0, 2.5, 32.0, 12.0, "Sweets", aka = "Apple pie", fib = 1.5, sug = 13.0, sat = 5.0, na = 170.0, k = 60.0)
    )

    private fun kerala() = listOf(
        f("Kadala curry", "1 cup", 205.0, 9.0, 26.0, 9.0, "Sides", aka = "Black chana curry, Kerala kadala", fib = 8.0, sug = 3.0, sat = 4.0, na = 500.0, k = 400.0, fe = 3.0),
        f("Kerala beef fry", "1 plate", 330.0, 28.0, 6.0, 22.0, "Protein", aka = "Beef ularthiyathu, Beef fry", fib = 1.5, sug = 1.0, sat = 7.0, na = 700.0, k = 400.0, fe = 3.0, zn = 6.0, b12 = 2.5),
        f("Kerala fish curry", "1 cup", 215.0, 22.0, 6.0, 12.0, "Protein", aka = "Meen curry, Fish moilee style curry", fib = 1.5, sug = 2.0, sat = 6.0, na = 700.0, k = 450.0, b12 = 2.0, o3 = 0.4),
        f("Karimeen pollichathu", "1 fish", 270.0, 26.0, 6.0, 16.0, "Protein", aka = "Pearl spot fry", fib = 1.0, sug = 1.0, sat = 6.0, na = 600.0, k = 400.0, b12 = 2.0),
        f("Kappa and meen curry", "1 plate", 470.0, 24.0, 60.0, 16.0, "Meals", aka = "Tapioca with fish curry", fib = 4.0, sug = 3.0, sat = 8.0, na = 800.0, k = 700.0),
        f("Thalassery biryani", "1 plate", 690.0, 30.0, 85.0, 26.0, "Rice", aka = "Malabar biryani", fib = 2.0, sug = 2.0, sat = 9.0, na = 1400.0, k = 420.0),
        f("Kerala sadya", "1 leaf", 965.0, 22.0, 160.0, 30.0, "Meals", aka = "Onam sadya, Sadhya", fib = 16.0, sug = 22.0, sat = 16.0, na = 1800.0, k = 1200.0),
        f("Ada pradhaman", "1 cup", 325.0, 3.0, 52.0, 12.0, "Sweets", aka = "Palada pradhaman", fib = 1.5, sug = 36.0, sat = 9.0, na = 40.0, k = 200.0),
        f("Unniyappam", "3 pieces", 195.0, 2.0, 30.0, 8.0, "Sweets", fib = 1.5, sug = 14.0, sat = 5.0, na = 30.0, k = 150.0),
        f("Pazham pori", "2 pieces", 300.0, 3.0, 46.0, 12.0, "Snacks", aka = "Banana fritters, Ethakka appam", fib = 3.0, sug = 18.0, sat = 3.0, na = 80.0, k = 400.0)
    )

    private fun andhraAndHyderabadi() = listOf(
        f("Gongura chicken", "1 cup", 310.0, 26.0, 8.0, 20.0, "Protein", aka = "Gongura kodi", fib = 3.0, sug = 2.0, sat = 4.0, na = 700.0, k = 420.0),
        f("Gongura mutton", "1 cup", 340.0, 24.0, 8.0, 24.0, "Protein", aka = "Gongura mamsam", fib = 3.0, sug = 2.0, sat = 7.0, na = 700.0, k = 420.0, zn = 4.5, b12 = 2.0),
        f("Chicken haleem", "1 bowl", 360.0, 22.0, 30.0, 18.0, "Protein", aka = "Haleem", fib = 6.0, sug = 2.0, sat = 6.0, na = 700.0, k = 400.0),
        f("Mutton haleem", "1 bowl", 385.0, 20.0, 30.0, 22.0, "Protein", aka = "Hyderabadi haleem", fib = 6.0, sug = 2.0, sat = 8.0, na = 700.0, k = 400.0, b12 = 1.8),
        f("Qubani ka meetha", "1 cup", 235.0, 2.0, 50.0, 4.0, "Sweets", aka = "Apricot dessert", fib = 4.0, sug = 40.0, sat = 2.0, na = 20.0, k = 500.0),
        f("Mirchi ka salan", "1 cup", 225.0, 4.0, 14.0, 18.0, "Sides", fib = 4.0, sug = 5.0, sat = 3.0, na = 500.0, k = 350.0),
        f("Bagara baingan", "1 cup", 220.0, 4.0, 14.0, 18.0, "Sides", aka = "Baghare baingan", fib = 6.0, sug = 5.0, sat = 3.0, na = 500.0, k = 400.0),
        f("Chicken do pyaza", "1 cup", 320.0, 26.0, 10.0, 20.0, "North Indian", fib = 3.0, sug = 4.0, sat = 5.0, na = 700.0, k = 420.0),
        f("Rajma chawal", "1 plate", 450.0, 16.0, 80.0, 10.0, "Meals", aka = "Rajma rice", fib = 12.0, sug = 3.0, sat = 3.0, na = 700.0, k = 700.0, fe = 4.0),
        f("Kadhi chawal", "1 plate", 440.0, 10.0, 75.0, 12.0, "Meals", aka = "Kadhi rice", fib = 4.0, sug = 6.0, sat = 4.0, na = 800.0, k = 400.0),
        f("Chole chawal", "1 plate", 470.0, 14.0, 82.0, 12.0, "Meals", aka = "Chole rice", fib = 12.0, sug = 4.0, sat = 2.0, na = 800.0, k = 600.0, fe = 4.0)
    )

    private fun arabian() = listOf(
        f("Chicken mandi", "1 plate", 775.0, 42.0, 90.0, 28.0, "Middle Eastern", aka = "Mandi, Kuzhimandhi", fib = 3.0, sug = 3.0, sat = 8.0, na = 1300.0, k = 600.0, b12 = 0.6),
        f("Mutton mandi", "1 plate", 840.0, 40.0, 90.0, 36.0, "Middle Eastern", aka = "Mutton kuzhimandhi", fib = 3.0, sug = 3.0, sat = 13.0, na = 1300.0, k = 600.0, zn = 7.0, b12 = 2.4),
        f("Chicken kabsa", "1 plate", 715.0, 38.0, 88.0, 24.0, "Middle Eastern", aka = "Kabsa", fib = 3.0, sug = 4.0, sat = 7.0, na = 1200.0, k = 600.0),
        f("Rumali shawarma", "1 roll", 410.0, 22.0, 36.0, 20.0, "Middle Eastern", aka = "Shawarma roll", fib = 2.0, sug = 2.0, sat = 4.0, na = 900.0, k = 350.0),
        f("Falafel wrap", "1 wrap", 420.0, 12.0, 52.0, 20.0, "Middle Eastern", aka = "Falafel roll", fib = 8.0, sug = 4.0, sat = 3.0, na = 900.0, k = 450.0)
    )

    private fun instantAndPackaged() = listOf(
        f("Instant noodles", "1 pack", 325.0, 7.0, 44.0, 14.0, "Grains", aka = "Maggi, Masala noodles, Top Ramen", fib = 2.0, sug = 1.0, sat = 6.0, na = 1000.0, k = 150.0),
        f("Cup noodles", "1 cup", 290.0, 6.0, 40.0, 12.0, "Grains", fib = 2.0, sug = 2.0, sat = 5.0, na = 1100.0, k = 150.0),
        f("Instant masala oats", "1 pack", 170.0, 5.0, 30.0, 4.0, "Grains", aka = "Masala oats", fib = 4.0, sug = 3.0, sat = 1.0, na = 500.0, k = 150.0, fe = 2.0),
        f("Cornflakes with milk", "1 bowl", 230.0, 9.0, 40.0, 4.0, "Grains", aka = "Cereal with milk", fib = 1.0, sug = 16.0, sat = 2.5, na = 300.0, k = 300.0, ca = 250.0, fe = 4.0),
        f("Chocos with milk", "1 bowl", 260.0, 9.0, 46.0, 5.0, "Grains", aka = "Chocolate cereal with milk", fib = 2.0, sug = 22.0, sat = 3.0, na = 250.0, k = 350.0, ca = 250.0, fe = 4.0),
        f("Chocolate spread toast", "2 slices", 285.0, 6.0, 40.0, 12.0, "Snacks", aka = "Nutella toast", fib = 3.0, sug = 18.0, sat = 3.0, na = 300.0, k = 200.0)
    )

    private fun healthyBowlsAndGymFood() = listOf(
        f("Paneer salad", "1 bowl", 270.0, 18.0, 10.0, 18.0, "Continental", aka = "Paneer tikka salad", fib = 3.0, sug = 5.0, sat = 10.0, na = 300.0, k = 350.0, ca = 420.0),
        f("Sprouts salad", "1 bowl", 130.0, 9.0, 22.0, 2.0, "Legumes", aka = "Mulaikattiya payaru salad", fib = 7.0, sug = 4.0, sat = 0.3, na = 200.0, k = 400.0, vc = 20.0, fol = 150.0),
        f("Quinoa salad", "1 bowl", 260.0, 8.0, 38.0, 10.0, "Continental", fib = 6.0, sug = 4.0, sat = 1.5, na = 300.0, k = 450.0, mg = 90.0),
        f("Greek yogurt bowl with fruit", "1 bowl", 210.0, 15.0, 30.0, 4.0, "Dairy", aka = "Yogurt parfait", fib = 3.0, sug = 22.0, sat = 2.0, na = 80.0, k = 400.0, ca = 180.0),
        f("Chicken rice bowl", "1 bowl", 530.0, 38.0, 70.0, 12.0, "Continental", aka = "Protein bowl, Grilled chicken rice bowl", fib = 4.0, sug = 3.0, sat = 3.0, na = 800.0, k = 600.0, b12 = 0.4),
        f("Egg white omelette", "4 whites", 65.0, 14.0, 1.0, 0.3, "Protein", aka = "Egg white scramble", fib = 0.0, sug = 1.0, sat = 0.0, na = 230.0, k = 210.0),
        f("Peanut butter banana smoothie", "1 glass", 390.0, 14.0, 50.0, 16.0, "Juices", aka = "PB banana shake", fib = 4.0, sug = 30.0, sat = 3.0, na = 200.0, k = 800.0, ca = 300.0),
        f("Protein shake with milk", "1 glass", 255.0, 32.0, 16.0, 7.0, "Drinks", aka = "Whey with milk", fib = 0.0, sug = 15.0, sat = 4.0, na = 250.0, k = 550.0, ca = 400.0, b12 = 1.0)
    )
}
