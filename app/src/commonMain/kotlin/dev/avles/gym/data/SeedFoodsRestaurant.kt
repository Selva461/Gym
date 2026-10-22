package dev.avles.gym.data

/**
 * What gets ordered at the restaurant: the Indo-Chinese menu every Indian
 * restaurant prints (garlic, chilli, dragon, Schezwan, manchurian, fried rice,
 * noodles, soups) and the grill counter's chicken by the quarter, half and whole
 * bird: Arabian grill, barbecue, tandoori, al faham, peri peri, with kuboos and
 * the mayonnaise that comes with it.
 *
 * A plate is the usual restaurant portion, about 200 g for a dry starter and a
 * full plate for rice and noodles. A grilled bird is the edible meat and skin
 * of a 1 kg chicken. Restaurant oil varies more than anything else here, so treat
 * the fat as a middle estimate. Calories are derived from the macros (4/4/9,
 * fibre at 2), the arithmetic FoodChecks enforces.
 */
internal object SeedFoodsRestaurant {

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

    fun all(): List<FoodItem> = grillAndTandoori() + indoChineseMains() + indoChineseRiceAndNoodles() + indoChineseSoupsAndStarters()

    private fun grillAndTandoori() = listOf(
        f("Grill chicken, quarter", "quarter bird", 295.0, 35.0, 3.0, 16.0, "Protein", aka = "Quarter grill, Arabian grill chicken", sat = 4.5, na = 520.0, k = 330.0, ca = 20.0, fe = 1.2, mg = 32.0, zn = 2.2, b12 = 0.4),
        f("Grill chicken, half", "half bird", 590.0, 70.0, 6.0, 32.0, "Protein", aka = "Half grill, Arabian grill chicken", sat = 9.0, na = 1040.0, k = 660.0, ca = 40.0, fe = 2.4, mg = 64.0, zn = 4.4, b12 = 0.8),
        f("Grill chicken, full", "whole bird", 1185.0, 140.0, 12.0, 64.0, "Protein", aka = "Full grill, Arabian grill chicken", sat = 18.0, na = 2080.0, k = 1320.0, ca = 80.0, fe = 4.8, mg = 128.0, zn = 8.8, b12 = 1.6),
        f("BBQ chicken, quarter", "quarter bird", 300.0, 34.0, 7.0, 15.0, "Protein", aka = "Quarter barbecue chicken", sug = 4.0, sat = 4.2, na = 560.0, k = 320.0, ca = 20.0, fe = 1.2, mg = 30.0, zn = 2.1, b12 = 0.4),
        f("BBQ chicken, half", "half bird", 600.0, 68.0, 14.0, 30.0, "Protein", aka = "Half barbecue, Half BBQ", sug = 8.0, sat = 8.4, na = 1120.0, k = 640.0, ca = 40.0, fe = 2.4, mg = 60.0, zn = 4.2, b12 = 0.8),
        f("BBQ chicken, full", "whole bird", 1195.0, 136.0, 28.0, 60.0, "Protein", aka = "Full barbecue, Full BBQ", sug = 16.0, sat = 16.8, na = 2240.0, k = 1280.0, ca = 80.0, fe = 4.8, mg = 120.0, zn = 8.4, b12 = 1.6),
        f("Tandoori chicken, quarter", "quarter bird", 260.0, 36.0, 4.0, 11.0, "Protein", aka = "Quarter tandoori", fib = 0.5, sat = 3.0, na = 600.0, k = 360.0, ca = 40.0, fe = 1.5, mg = 34.0, zn = 2.2, b12 = 0.4),
        f("Tandoori chicken, half", "half bird", 515.0, 72.0, 8.0, 22.0, "Protein", aka = "Half tandoori, Half chicken tandoori", fib = 1.0, sat = 6.0, na = 1200.0, k = 720.0, ca = 80.0, fe = 3.0, mg = 68.0, zn = 4.4, b12 = 0.8),
        f("Tandoori chicken, full", "whole bird", 1030.0, 144.0, 16.0, 44.0, "Protein", aka = "Full tandoori, Full chicken tandoori", fib = 2.0, sat = 12.0, na = 2400.0, k = 1440.0, ca = 160.0, fe = 6.0, mg = 136.0, zn = 8.8, b12 = 1.6),
        f("Al faham chicken, half", "half bird", 615.0, 70.0, 8.0, 34.0, "Protein", aka = "Alfaham, Al faham", fib = 1.0, sat = 9.5, na = 1100.0, k = 650.0, ca = 50.0, fe = 2.8, mg = 64.0, zn = 4.4, b12 = 0.8),
        f("Al faham chicken, full", "whole bird", 1230.0, 140.0, 16.0, 68.0, "Protein", aka = "Alfaham full", fib = 2.0, sat = 19.0, na = 2200.0, k = 1300.0, ca = 100.0, fe = 5.6, mg = 128.0, zn = 8.8, b12 = 1.6),
        f("Peri peri chicken, half", "half bird", 555.0, 70.0, 6.0, 28.0, "Protein", aka = "Piri piri chicken", fib = 0.5, sat = 7.5, na = 1150.0, k = 650.0, ca = 40.0, fe = 2.6, mg = 62.0, zn = 4.4, vc = 8.0, b12 = 0.8),
        f("Chicken seekh kebab", "2 kebabs", 230.0, 22.0, 4.0, 14.0, "Protein", aka = "Seekh kabab", fib = 0.5, sat = 4.5, na = 520.0, k = 260.0, ca = 25.0, fe = 1.4, mg = 24.0, zn = 1.6, b12 = 0.3),
        f("Tangdi kebab", "2 legs", 295.0, 34.0, 4.0, 16.0, "Protein", aka = "Tangdi kabab, Chicken leg kebab", fib = 0.5, sat = 4.5, na = 640.0, k = 340.0, ca = 50.0, fe = 1.6, mg = 34.0, zn = 3.2, b12 = 0.5),
        f("Reshmi kebab", "6 pieces", 320.0, 30.0, 5.0, 20.0, "Protein", aka = "Reshmi kabab", sat = 7.0, na = 620.0, k = 320.0, ca = 60.0, fe = 1.2, mg = 32.0, zn = 1.8, b12 = 0.4),
        f("Chicken malai tikka", "6 pieces", 360.0, 30.0, 6.0, 24.0, "Protein", aka = "Malai kebab, Murgh malai tikka", sat = 9.5, na = 600.0, k = 330.0, ca = 80.0, fe = 1.2, mg = 32.0, zn = 1.8, b12 = 0.4),
        f("Kuboos", "1 piece", 165.0, 5.5, 33.0, 1.5, "Tiffin", aka = "Khubz, Arabic bread", fib = 1.5, sug = 1.0, na = 320.0, k = 70.0, ca = 30.0, fe = 1.8, mg = 16.0, zn = 0.5),
        f("Mayonnaise", "2 tbsp", 205.0, 0.3, 1.0, 22.0, "Fats", aka = "Mayo, Grill mayo", sat = 3.4, na = 180.0, k = 6.0, ca = 2.0, vd = 0.1),
        f("Garlic dip", "2 tbsp", 170.0, 0.5, 2.0, 18.0, "Fats", aka = "Toum, Garlic sauce", sat = 2.0, na = 200.0, k = 20.0, ca = 4.0)
    )

    private fun indoChineseMains() = listOf(
        f("Garlic chicken", "1 plate", 395.0, 32.0, 18.0, 22.0, "Chinese", aka = "Hot garlic chicken, Chicken garlic", fib = 1.5, sug = 6.0, sat = 3.5, na = 1300.0, k = 450.0, ca = 30.0, fe = 1.6, mg = 40.0, zn = 2.0, vc = 6.0, b12 = 0.4),
        f("Chilli chicken, gravy", "1 plate", 385.0, 30.0, 22.0, 20.0, "Chinese", aka = "Chilli chicken gravy", fib = 2.0, sug = 8.0, sat = 3.2, na = 1450.0, k = 470.0, ca = 30.0, fe = 1.6, mg = 40.0, zn = 2.0, vc = 30.0, b12 = 0.4),
        f("Chicken manchurian, gravy", "1 plate", 405.0, 28.0, 24.0, 22.0, "Chinese", aka = "Manchurian gravy chicken", fib = 1.5, sug = 7.0, sat = 3.4, na = 1500.0, k = 420.0, ca = 30.0, fe = 1.6, mg = 36.0, zn = 1.9, vc = 8.0, b12 = 0.4),
        f("Dragon chicken", "1 plate", 470.0, 30.0, 30.0, 26.0, "Chinese", fib = 1.5, sug = 10.0, sat = 4.0, na = 1400.0, k = 420.0, ca = 30.0, fe = 1.8, mg = 38.0, zn = 2.0, vc = 20.0, b12 = 0.4),
        f("Schezwan chicken", "1 plate", 395.0, 30.0, 20.0, 22.0, "Chinese", aka = "Szechuan chicken, Sichuan chicken", fib = 2.0, sug = 7.0, sat = 3.4, na = 1550.0, k = 460.0, ca = 30.0, fe = 1.8, mg = 40.0, zn = 2.0, vc = 25.0, b12 = 0.4),
        f("Lemon chicken", "1 plate", 420.0, 28.0, 28.0, 22.0, "Chinese", fib = 1.0, sug = 14.0, sat = 3.4, na = 1100.0, k = 400.0, ca = 25.0, fe = 1.4, mg = 34.0, zn = 1.8, vc = 15.0, b12 = 0.4),
        f("Ginger chicken", "1 plate", 360.0, 30.0, 16.0, 20.0, "Chinese", fib = 1.5, sug = 5.0, sat = 3.0, na = 1250.0, k = 440.0, ca = 25.0, fe = 1.5, mg = 38.0, zn = 2.0, vc = 6.0, b12 = 0.4),
        f("Black pepper chicken", "1 plate", 360.0, 32.0, 14.0, 20.0, "Chinese", aka = "Pepper chicken, Chinese style", fib = 1.5, sug = 4.0, sat = 3.0, na = 1300.0, k = 450.0, ca = 30.0, fe = 1.8, mg = 40.0, zn = 2.1, vc = 10.0, b12 = 0.4),
        f("Kung pao chicken", "1 plate", 435.0, 32.0, 20.0, 26.0, "Chinese", aka = "Kung po chicken", fib = 3.0, sug = 8.0, sat = 4.0, na = 1400.0, k = 520.0, ca = 35.0, fe = 2.0, mg = 70.0, zn = 2.4, vc = 20.0, b12 = 0.4),
        f("Honey chilli chicken", "1 plate", 450.0, 28.0, 36.0, 22.0, "Chinese", fib = 1.0, sug = 18.0, sat = 3.4, na = 1200.0, k = 400.0, ca = 25.0, fe = 1.4, mg = 34.0, zn = 1.8, vc = 15.0, b12 = 0.4),
        f("Salt and pepper chicken", "1 plate", 415.0, 30.0, 20.0, 24.0, "Chinese", aka = "Crispy chicken, Salt n pepper chicken", fib = 1.0, sug = 2.0, sat = 3.8, na = 1350.0, k = 420.0, ca = 25.0, fe = 1.5, mg = 36.0, zn = 1.9, vc = 8.0, b12 = 0.4),
        f("Sweet and sour chicken", "1 plate", 440.0, 26.0, 40.0, 20.0, "Chinese", fib = 1.5, sug = 22.0, sat = 3.0, na = 1050.0, k = 420.0, ca = 25.0, fe = 1.4, mg = 32.0, zn = 1.7, vc = 20.0, b12 = 0.3),
        f("Hong Kong chicken", "1 plate", 420.0, 30.0, 22.0, 24.0, "Chinese", fib = 1.5, sug = 8.0, sat = 3.8, na = 1400.0, k = 440.0, ca = 30.0, fe = 1.6, mg = 38.0, zn = 2.0, vc = 15.0, b12 = 0.4),
        f("Drums of heaven", "6 pieces", 485.0, 30.0, 24.0, 30.0, "Chinese", aka = "Sauced chicken lollipop", fib = 1.0, sug = 8.0, sat = 5.0, na = 1400.0, k = 380.0, ca = 30.0, fe = 1.6, mg = 34.0, zn = 2.4, b12 = 0.4),
        f("Chilli prawns", "1 plate", 325.0, 26.0, 20.0, 16.0, "Chinese", aka = "Chilli shrimp", fib = 1.5, sug = 7.0, sat = 2.4, na = 1500.0, k = 380.0, ca = 90.0, fe = 1.2, mg = 50.0, zn = 1.8, vc = 25.0, b12 = 1.2),
        f("Garlic prawns", "1 plate", 310.0, 26.0, 16.0, 16.0, "Chinese", aka = "Hot garlic prawns", fib = 1.0, sug = 5.0, sat = 2.4, na = 1400.0, k = 360.0, ca = 90.0, fe = 1.2, mg = 48.0, zn = 1.8, vc = 6.0, b12 = 1.2),
        f("Golden fried prawns", "8 pieces", 390.0, 22.0, 26.0, 22.0, "Chinese", aka = "Batter fried prawns", fib = 1.0, sug = 1.0, sat = 3.2, na = 900.0, k = 280.0, ca = 80.0, fe = 1.2, mg = 40.0, zn = 1.5, b12 = 1.0),
        f("Chilli fish", "1 plate", 350.0, 28.0, 20.0, 18.0, "Chinese", fib = 1.5, sug = 7.0, sat = 2.8, na = 1350.0, k = 480.0, ca = 40.0, fe = 1.0, mg = 45.0, zn = 0.8, vc = 25.0, b12 = 1.5, o3 = 0.3),
        f("Chilli paneer", "1 plate", 420.0, 20.0, 18.0, 30.0, "Chinese", aka = "Paneer chilli", fib = 2.0, sug = 7.0, sat = 14.0, na = 1200.0, k = 300.0, ca = 420.0, fe = 0.9, mg = 30.0, zn = 2.4, vc = 25.0, b12 = 0.4),
        f("Veg manchurian, gravy", "8 balls", 335.0, 7.0, 34.0, 20.0, "Chinese", aka = "Vegetable manchurian", fib = 5.0, sug = 8.0, sat = 3.0, na = 1400.0, k = 420.0, ca = 50.0, fe = 1.6, mg = 36.0, zn = 0.8, vc = 20.0),
        f("Chilli mushroom", "1 plate", 250.0, 8.0, 20.0, 16.0, "Chinese", aka = "Mushroom chilli", fib = 4.0, sug = 6.0, sat = 2.4, na = 1200.0, k = 560.0, ca = 15.0, fe = 1.2, mg = 22.0, zn = 1.2, vc = 20.0),
        f("Chilli baby corn", "1 plate", 280.0, 6.0, 30.0, 16.0, "Chinese", aka = "Baby corn chilli", fib = 4.0, sug = 8.0, sat = 2.4, na = 1150.0, k = 320.0, ca = 15.0, fe = 0.9, mg = 30.0, zn = 0.8, vc = 20.0),
        f("Honey chilli potato", "1 plate", 390.0, 5.0, 50.0, 20.0, "Chinese", fib = 4.0, sug = 16.0, sat = 3.0, na = 900.0, k = 700.0, ca = 20.0, fe = 1.2, mg = 40.0, zn = 0.6, vc = 15.0),
        f("Crispy corn", "1 plate", 340.0, 6.0, 40.0, 18.0, "Chinese", aka = "Salt and pepper corn", fib = 4.0, sug = 5.0, sat = 2.6, na = 850.0, k = 320.0, ca = 10.0, fe = 0.8, mg = 40.0, zn = 0.9, vc = 8.0)
    )

    private fun indoChineseRiceAndNoodles() = listOf(
        f("Chicken Schezwan fried rice", "1 plate", 585.0, 22.0, 85.0, 18.0, "Chinese", aka = "Schezwan rice, Szechuan fried rice", fib = 3.0, sug = 5.0, sat = 3.0, na = 1600.0, k = 380.0, ca = 40.0, fe = 1.8, mg = 50.0, zn = 2.0, vc = 10.0, b12 = 0.3),
        f("Egg fried rice", "1 plate", 525.0, 16.0, 80.0, 16.0, "Chinese", fib = 2.5, sug = 3.0, sat = 3.0, na = 1300.0, k = 280.0, ca = 50.0, fe = 1.6, mg = 36.0, zn = 1.6, b12 = 0.6),
        f("Mixed fried rice", "1 plate", 590.0, 26.0, 82.0, 18.0, "Chinese", aka = "Mixed non-veg fried rice", fib = 2.5, sug = 3.0, sat = 3.2, na = 1450.0, k = 380.0, ca = 60.0, fe = 2.0, mg = 50.0, zn = 2.4, b12 = 0.8),
        f("Triple Schezwan rice", "1 plate", 795.0, 28.0, 110.0, 28.0, "Chinese", aka = "Triple Szechuan rice", fib = 5.0, sug = 10.0, sat = 4.6, na = 2200.0, k = 520.0, ca = 60.0, fe = 2.6, mg = 60.0, zn = 2.6, vc = 20.0, b12 = 0.5),
        f("Burnt garlic fried rice", "1 plate", 500.0, 9.0, 82.0, 16.0, "Chinese", aka = "Burnt garlic rice", fib = 3.0, sug = 3.0, sat = 2.4, na = 1100.0, k = 260.0, ca = 30.0, fe = 1.4, mg = 30.0, zn = 1.2, vc = 6.0),
        f("Chicken Schezwan noodles", "1 plate", 540.0, 22.0, 70.0, 20.0, "Chinese", aka = "Schezwan noodles, Szechuan noodles", fib = 4.0, sug = 6.0, sat = 3.2, na = 1700.0, k = 360.0, ca = 40.0, fe = 2.2, mg = 50.0, zn = 1.8, vc = 15.0, b12 = 0.3),
        f("Egg noodles", "1 plate", 475.0, 16.0, 68.0, 16.0, "Chinese", aka = "Egg Hakka noodles", fib = 3.5, sug = 4.0, sat = 3.0, na = 1350.0, k = 260.0, ca = 45.0, fe = 2.0, mg = 40.0, zn = 1.4, vc = 8.0, b12 = 0.6),
        f("Mixed noodles", "1 plate", 540.0, 26.0, 70.0, 18.0, "Chinese", aka = "Mixed Hakka noodles", fib = 3.5, sug = 4.0, sat = 3.2, na = 1500.0, k = 360.0, ca = 60.0, fe = 2.2, mg = 50.0, zn = 2.2, vc = 8.0, b12 = 0.8),
        f("Chicken American chopsuey", "1 plate", 635.0, 24.0, 78.0, 26.0, "Chinese", aka = "American chop suey", fib = 3.0, sug = 18.0, sat = 4.0, na = 1500.0, k = 380.0, ca = 50.0, fe = 2.0, mg = 40.0, zn = 1.8, vc = 15.0, b12 = 0.5)
    )

    private fun indoChineseSoupsAndStarters() = listOf(
        f("Chicken sweet corn soup", "1 bowl", 125.0, 9.0, 16.0, 3.0, "Chinese", fib = 1.0, sug = 4.0, sat = 0.7, na = 900.0, k = 200.0, ca = 15.0, fe = 0.6, mg = 14.0, zn = 0.6, b12 = 0.2),
        f("Veg sweet corn soup", "1 bowl", 100.0, 3.0, 18.0, 2.0, "Chinese", fib = 1.5, sug = 4.0, sat = 0.4, na = 850.0, k = 180.0, ca = 12.0, fe = 0.5, mg = 14.0, zn = 0.4),
        f("Chicken hot and sour soup", "1 bowl", 115.0, 10.0, 10.0, 4.0, "Chinese", aka = "Hot n sour soup", fib = 1.0, sug = 2.0, sat = 0.8, na = 1100.0, k = 220.0, ca = 20.0, fe = 1.0, mg = 16.0, zn = 0.7, b12 = 0.2),
        f("Veg hot and sour soup", "1 bowl", 85.0, 3.0, 12.0, 3.0, "Chinese", fib = 2.0, sug = 3.0, sat = 0.5, na = 1050.0, k = 200.0, ca = 20.0, fe = 0.8, mg = 14.0, zn = 0.4, vc = 5.0),
        f("Chicken manchow soup", "1 bowl", 170.0, 10.0, 18.0, 7.0, "Chinese", aka = "Manchow soup", fib = 1.5, sug = 3.0, sat = 1.2, na = 1200.0, k = 230.0, ca = 20.0, fe = 1.0, mg = 18.0, zn = 0.7, b12 = 0.2),
        f("Veg manchow soup", "1 bowl", 140.0, 4.0, 18.0, 6.0, "Chinese", fib = 2.0, sug = 3.0, sat = 1.0, na = 1150.0, k = 210.0, ca = 20.0, fe = 0.8, mg = 16.0, zn = 0.4, vc = 6.0),
        f("Lemon coriander soup", "1 bowl", 65.0, 3.0, 10.0, 2.0, "Chinese", fib = 1.5, sug = 2.0, sat = 0.3, na = 900.0, k = 180.0, ca = 20.0, fe = 0.8, mg = 12.0, zn = 0.3, vc = 15.0),
        f("Chicken spring roll", "2 rolls", 260.0, 10.0, 24.0, 14.0, "Chinese", fib = 1.5, sug = 2.0, sat = 2.2, na = 600.0, k = 160.0, ca = 20.0, fe = 1.2, mg = 16.0, zn = 0.8),
        f("Fried chicken momos", "6 pieces", 335.0, 16.0, 32.0, 16.0, "Chinese", aka = "Fried momos", fib = 1.5, sug = 1.0, sat = 2.6, na = 700.0, k = 200.0, ca = 25.0, fe = 1.6, mg = 22.0, zn = 1.2, b12 = 0.2),
        f("Fried chicken wontons", "6 pieces", 320.0, 14.0, 26.0, 18.0, "Chinese", aka = "Chicken wonton", fib = 1.0, sug = 1.0, sat = 2.8, na = 650.0, k = 180.0, ca = 20.0, fe = 1.4, mg = 18.0, zn = 1.0, b12 = 0.2)
    )
}
