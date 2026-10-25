package dev.avles.gym.data

/**
 * The kitchen cupboard, and the home dishes the earlier batches missed.
 *
 * First the raw ingredients, per 100 g, for weighing before cooking: dals and pulses,
 * flours, rice and millets, chicken and fish, a few vegetables and thick coconut
 * milk. Then Chettinad sweets and paniyaram, thuvaiyal and podi, keerai dishes,
 * seasonal fruit and drinks (nungu, kodukkapuli, panakam), paneer bhurji and meal
 * maker curry, and a handful of Karnataka dishes.
 *
 * Raw values follow IFCT 2017 and USDA FoodData Central, rounded; home dishes are
 * estimates for a typical recipe. Calories are derived from the macros (4/4/9,
 * fibre at 2), so they agree with the numbers shown beside them.
 */
internal object SeedFoodsKitchen {

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

    fun all(): List<FoodItem> = dalsRaw() + flourAndGrainsRaw() + meatAndFishRaw() + vegetablesRaw() +
        chettinad() + thuvaiyalAndPodi() + greens() + seasonal() + homeProtein() + karnataka()

    private fun dalsRaw() = listOf(
        f("Toor dal, raw", "100 g", 325.0, 22.0, 63.0, 1.5, "Legumes", aka = "Thuvaram paruppu, Arhar dal, Tur dal, uncooked", fib = 15.0, sug = 3.0, sat = 0.3, na = 17.0, k = 1390.0, ca = 130.0, fe = 5.0, mg = 180.0),
        f("Moong dal, raw", "100 g", 330.0, 24.5, 59.0, 1.2, "Legumes", aka = "Pasi paruppu, Split yellow moong, uncooked", fib = 8.0, sug = 2.0, sat = 0.3, na = 15.0, k = 1250.0, ca = 75.0, fe = 4.0, mg = 190.0),
        f("Urad dal, raw", "100 g", 325.0, 24.0, 59.0, 1.6, "Legumes", aka = "Ulutham paruppu, Black gram dal, uncooked", fib = 11.0, sug = 1.0, sat = 0.2, na = 38.0, k = 980.0, ca = 140.0, fe = 4.0, mg = 180.0),
        f("Masoor dal, raw", "100 g", 325.0, 24.6, 60.0, 1.1, "Legumes", aka = "Mysore paruppu, Red lentils, uncooked", fib = 11.0, sug = 2.0, sat = 0.2, na = 6.0, k = 680.0, ca = 35.0, fe = 6.5, mg = 47.0),
        f("Chana dal, raw", "100 g", 345.0, 20.5, 61.0, 5.6, "Legumes", aka = "Kadalai paruppu, Bengal gram dal, uncooked", fib = 15.0, sug = 3.0, sat = 0.6, na = 24.0, k = 875.0, ca = 55.0, fe = 5.0, mg = 120.0),
        f("Green gram, whole, raw", "100 g", 325.0, 23.9, 62.6, 1.2, "Legumes", aka = "Pachai payaru, Whole moong, uncooked", fib = 16.0, sug = 6.6, sat = 0.3, na = 15.0, k = 1246.0, ca = 132.0, fe = 6.7, mg = 189.0),
        f("Chickpeas, raw", "100 g", 365.0, 20.5, 63.0, 6.0, "Legumes", aka = "Vellai kondakadalai, Kabuli chana, uncooked", fib = 12.0, sug = 11.0, sat = 0.6, na = 24.0, k = 875.0, ca = 105.0, fe = 4.3, mg = 115.0),
        f("Black chana, raw", "100 g", 340.0, 20.0, 60.0, 5.0, "Legumes", aka = "Karuppu kondakadalai, Kala chana, uncooked", fib = 12.0, sug = 8.0, sat = 0.5, na = 24.0, k = 850.0, ca = 150.0, fe = 6.0, mg = 140.0),
        f("Rajma, raw", "100 g", 310.0, 23.6, 60.0, 0.8, "Legumes", aka = "Kidney beans, uncooked", fib = 15.0, sug = 2.0, sat = 0.1, na = 12.0, k = 1400.0, ca = 140.0, fe = 8.0, mg = 140.0),
        f("Horse gram, raw", "100 g", 305.0, 22.0, 57.0, 0.5, "Legumes", aka = "Kollu, Kulthi, uncooked", fib = 8.0, sug = 1.5, sat = 0.1, na = 12.0, k = 760.0, ca = 287.0, fe = 7.0, mg = 150.0),
        f("Peanuts, raw", "100 g", 590.0, 25.8, 16.0, 49.0, "Nuts", aka = "Verkadalai, Groundnut, Kadalai", fib = 8.5, sug = 4.0, sat = 6.8, na = 18.0, k = 705.0, ca = 92.0, fe = 4.6, mg = 168.0, zn = 3.3, fol = 240.0)
    )

    private fun flourAndGrainsRaw() = listOf(
        f("Refined flour (maida)", "100 g", 350.0, 10.3, 76.0, 1.0, "Grains", aka = "Maida, All-purpose flour", fib = 2.7, sug = 0.3, sat = 0.2, na = 2.0, k = 107.0, ca = 15.0, fe = 1.2, mg = 22.0),
        f("Rice flour", "100 g", 350.0, 6.0, 80.0, 1.4, "Grains", aka = "Arisi maavu, Idiyappam flour", fib = 2.4, sug = 0.1, sat = 0.4, na = 0.0, k = 76.0, ca = 10.0, fe = 0.4, mg = 35.0),
        f("Corn flour", "100 g", 365.0, 0.3, 91.0, 0.1, "Grains", aka = "Cornflour, Cornstarch, Corn maavu", fib = 0.9, sug = 0.0, sat = 0.0, na = 9.0, k = 3.0, ca = 2.0, fe = 0.5),
        f("Jowar flour", "100 g", 325.0, 10.4, 72.0, 1.9, "Grains", aka = "Cholam maavu, Sorghum flour", fib = 9.7, sug = 1.9, sat = 0.3, na = 2.0, k = 350.0, ca = 25.0, fe = 4.0, mg = 130.0),
        f("Bajra flour", "100 g", 340.0, 11.0, 67.5, 5.4, "Grains", aka = "Kambu maavu, Pearl millet flour", fib = 11.5, sug = 1.0, sat = 1.0, na = 5.0, k = 300.0, ca = 27.0, fe = 6.4, mg = 125.0),
        f("Broken wheat, raw", "100 g", 340.0, 12.0, 76.0, 1.3, "Grains", aka = "Godhumai rava, Cracked wheat, Dalia, uncooked", fib = 12.5, sug = 0.4, sat = 0.2, na = 2.0, k = 400.0, ca = 35.0, fe = 3.5, mg = 160.0),
        f("Basmati rice, raw", "100 g", 350.0, 8.5, 78.0, 0.6, "Grains", aka = "Basmati arisi, uncooked", fib = 1.3, sug = 0.2, sat = 0.2, na = 1.0, k = 115.0, ca = 10.0, fe = 0.8, mg = 25.0),
        f("Brown rice, raw", "100 g", 350.0, 7.5, 76.0, 2.7, "Grains", aka = "Kaikuthal arisi, Hand-pounded rice, uncooked", fib = 3.5, sug = 0.9, sat = 0.5, na = 4.0, k = 250.0, ca = 20.0, fe = 1.5, mg = 140.0),
        f("Foxtail millet, raw", "100 g", 310.0, 12.3, 60.0, 4.3, "Grains", aka = "Thinai arisi, Kangni, uncooked", fib = 8.0, sug = 1.0, sat = 0.7, na = 4.0, k = 250.0, ca = 31.0, fe = 2.8, mg = 81.0),
        f("Little millet, raw", "100 g", 325.0, 7.7, 67.0, 4.7, "Grains", aka = "Samai arisi, Kutki, uncooked", fib = 7.6, sug = 1.0, sat = 0.8, na = 8.0, k = 130.0, ca = 17.0, fe = 1.3, mg = 91.0),
        f("Kodo millet, raw", "100 g", 295.0, 8.3, 66.0, 1.4, "Grains", aka = "Varagu arisi, Kodra, uncooked", fib = 6.4, sug = 1.0, sat = 0.3, na = 5.0, k = 145.0, ca = 15.0, fe = 2.3, mg = 122.0),
        f("Barnyard millet, raw", "100 g", 285.0, 6.2, 65.5, 2.2, "Grains", aka = "Kuthiraivali arisi, Sanwa, uncooked", fib = 10.0, sug = 1.0, sat = 0.4, na = 5.0, k = 150.0, ca = 19.0, fe = 5.0, mg = 82.0)
    )

    private fun meatAndFishRaw() = listOf(
        f("Chicken leg, with skin, raw", "100 g", 155.0, 18.0, 0.0, 9.0, "Protein", aka = "Chicken drumstick and thigh, Kozhi kaal, uncooked", sat = 2.5, na = 90.0, k = 230.0, ca = 10.0, fe = 0.9, zn = 1.9, b12 = 0.4),
        f("Chicken mince, raw", "100 g", 140.0, 17.5, 0.0, 8.0, "Protein", aka = "Chicken keema, Ground chicken, uncooked", sat = 2.3, na = 60.0, k = 300.0, ca = 8.0, fe = 0.8, zn = 1.5, b12 = 0.5),
        f("Mackerel, raw", "100 g", 200.0, 19.0, 0.0, 14.0, "Protein", aka = "Ayala meen, Bangda, uncooked", sat = 3.3, na = 90.0, k = 314.0, ca = 12.0, fe = 1.6, vd = 16.0, b12 = 8.7, o3 = 2.3),
        f("Tilapia, raw", "100 g", 95.0, 20.0, 0.0, 1.7, "Protein", aka = "Jilebi meen, uncooked", sat = 0.6, na = 52.0, k = 302.0, ca = 10.0, fe = 0.6, vd = 3.1, b12 = 1.6, o3 = 0.2),
        f("Pomfret, raw", "100 g", 120.0, 19.0, 0.0, 5.0, "Protein", aka = "Vavval meen, uncooked", sat = 1.5, na = 80.0, k = 300.0, ca = 20.0, fe = 0.8, b12 = 1.5, o3 = 0.5),
        f("Tuna, raw", "100 g", 100.0, 23.0, 0.0, 1.0, "Protein", aka = "Choora meen, Soorai meen, uncooked", sat = 0.3, na = 40.0, k = 440.0, ca = 10.0, fe = 1.0, vd = 1.7, b12 = 2.2, o3 = 0.3),
        f("Basa fillet, raw", "100 g", 85.0, 15.0, 0.0, 3.0, "Protein", aka = "Basa fish, Pangasius, uncooked", sat = 1.0, na = 60.0, k = 250.0, ca = 10.0, fe = 0.3, o3 = 0.1)
    )

    private fun vegetablesRaw() = listOf(
        f("Potato, raw", "100 g", 75.0, 2.0, 17.5, 0.1, "Vegetables", aka = "Urulaikizhangu, Aloo, uncooked", fib = 2.2, sug = 0.8, sat = 0.0, na = 6.0, k = 425.0, ca = 12.0, fe = 0.8, mg = 23.0, vc = 19.7),
        f("Sweet potato, raw", "100 g", 80.0, 1.6, 20.0, 0.1, "Vegetables", aka = "Sakkaravalli kizhangu, uncooked", fib = 3.0, sug = 4.2, sat = 0.0, na = 55.0, k = 337.0, ca = 30.0, fe = 0.6, va = 709.0, vc = 2.4),
        f("Spinach, raw", "100 g", 25.0, 2.9, 3.6, 0.4, "Vegetables", aka = "Palak, Pasalai keerai, uncooked", fib = 2.2, sug = 0.4, sat = 0.1, na = 79.0, k = 558.0, ca = 99.0, fe = 2.7, mg = 79.0, va = 469.0, vc = 28.0, fol = 194.0),
        f("Capsicum", "100 g", 20.0, 0.9, 4.6, 0.3, "Vegetables", aka = "Kudai milagai, Bell pepper, Shimla mirch", fib = 1.7, sug = 2.4, sat = 0.0, na = 3.0, k = 175.0, ca = 10.0, fe = 0.3, vc = 80.0),
        f("Button mushroom, raw", "100 g", 25.0, 3.1, 3.3, 0.3, "Vegetables", aka = "Kaalan, Mushroom, uncooked", fib = 1.0, sug = 2.0, sat = 0.1, na = 5.0, k = 318.0, ca = 3.0, fe = 0.5, vd = 0.2),
        f("Green peas, raw", "100 g", 75.0, 5.4, 14.5, 0.4, "Vegetables", aka = "Pachai pattani, Matar, uncooked", fib = 5.1, sug = 5.7, sat = 0.1, na = 5.0, k = 244.0, ca = 25.0, fe = 1.5, mg = 33.0, vc = 40.0),
        f("Coconut milk, thick", "100 ml", 245.0, 2.3, 5.5, 24.0, "Fats", aka = "Thengai paal, First extract coconut milk", fib = 2.2, sug = 3.3, sat = 21.0, na = 15.0, k = 263.0, ca = 16.0, fe = 1.6)
    )

    private fun chettinad() = listOf(
        f("Kandarappam", "1 piece (40 g)", 140.0, 2.0, 22.0, 5.0, "Sweets", aka = "Kandharappam, Chettinad kandarappam", fib = 0.5, sug = 11.0, sat = 1.0, na = 10.0, k = 60.0),
        f("Kavuni arisi", "1 cup (150 g)", 275.0, 4.0, 48.0, 8.0, "Sweets", aka = "Kavuni arisi halwa, Chettinad black rice sweet", fib = 3.0, sug = 20.0, sat = 6.0, na = 20.0, k = 150.0),
        f("Paal paniyaram", "3 pieces with milk (120 g)", 210.0, 5.0, 30.0, 8.0, "Sweets", aka = "Chettinad paal paniyaram, Milk paniyaram", fib = 0.5, sug = 13.0, sat = 4.0, na = 40.0, k = 150.0, ca = 100.0),
        f("Vellai paniyaram", "4 pieces (100 g)", 195.0, 4.0, 30.0, 7.0, "Tiffin", aka = "White paniyaram, Chettinad vellai paniyaram", fib = 1.0, sug = 0.5, sat = 1.0, na = 220.0, k = 80.0)
    )

    private fun thuvaiyalAndPodi() = listOf(
        f("Paruppu thuvaiyal", "2 tbsp (30 g)", 70.0, 3.5, 8.0, 3.0, "Sides", aka = "Paruppu thogayal, Dal chutney", fib = 2.0, sug = 0.5, sat = 1.5, na = 200.0, k = 120.0),
        f("Thengai thuvaiyal", "2 tbsp (30 g)", 90.0, 1.2, 4.0, 8.0, "Sides", aka = "Coconut thuvaiyal, Thengai thogayal", fib = 2.5, sug = 1.0, sat = 6.5, na = 180.0, k = 100.0),
        f("Pirandai thuvaiyal", "2 tbsp (30 g)", 60.0, 1.5, 5.0, 4.0, "Sides", aka = "Pirandai thogayal, Adamant creeper chutney", fib = 2.0, sug = 0.5, sat = 1.0, na = 200.0, k = 100.0, ca = 60.0),
        f("Kothamalli thuvaiyal", "2 tbsp (30 g)", 55.0, 1.5, 4.0, 4.0, "Sides", aka = "Coriander thogayal, Kothamalli chutney", fib = 1.5, sug = 0.5, sat = 2.5, na = 200.0, k = 150.0),
        f("Inji puli", "1 tbsp (20 g)", 50.0, 0.3, 10.0, 1.0, "Sides", aka = "Puli inji, Ginger tamarind pickle", fib = 0.8, sug = 7.0, sat = 0.1, na = 250.0, k = 80.0),
        f("Karuveppilai podi", "1 tbsp (10 g)", 35.0, 1.8, 4.5, 1.5, "Sides", aka = "Curry leaf podi, Kariveppilai podi", fib = 2.0, sug = 0.3, sat = 0.2, na = 120.0, k = 80.0, ca = 40.0, fe = 1.0),
        f("Ellu podi", "1 tbsp (10 g)", 55.0, 2.0, 2.5, 4.5, "Sides", aka = "Sesame podi, Ellu milagai podi", fib = 1.2, sug = 0.2, sat = 0.7, na = 100.0, k = 50.0, ca = 90.0, fe = 1.4),
        f("Paruppu podi", "1 tbsp (10 g)", 35.0, 2.2, 5.0, 1.0, "Sides", aka = "Dal powder, Kandi podi", fib = 1.5, sug = 0.3, sat = 0.2, na = 120.0, k = 100.0)
    )

    private fun greens() = listOf(
        f("Ponnanganni keerai poriyal", "1 cup (100 g)", 80.0, 3.5, 7.0, 5.0, "Vegetables", aka = "Ponnanganni, Dwarf copperleaf greens", fib = 3.5, sug = 0.5, sat = 3.0, na = 220.0, k = 300.0, ca = 200.0, fe = 2.0),
        f("Agathi keerai poriyal", "1 cup (100 g)", 95.0, 5.0, 9.0, 5.0, "Vegetables", aka = "Agathi keerai, Agathi greens, Sesbania leaves", fib = 3.0, sug = 0.5, sat = 3.0, na = 200.0, k = 300.0, ca = 600.0, fe = 3.5),
        f("Siru keerai kootu", "1 cup (150 g)", 110.0, 6.0, 12.0, 5.0, "Vegetables", aka = "Sirukeerai kootu, Amaranth and dal", fib = 4.0, sug = 1.0, sat = 3.0, na = 350.0, k = 450.0, ca = 250.0, fe = 3.0),
        f("Panankizhangu", "2 tubers (100 g)", 120.0, 2.0, 30.0, 0.2, "Vegetables", aka = "Panai kizhangu, Palm sprout, Boiled palm tuber", fib = 6.0, sug = 1.0, sat = 0.0, na = 30.0, k = 400.0)
    )

    private fun seasonal() = listOf(
        f("Nungu", "3 pieces (100 g)", 40.0, 0.8, 10.0, 0.1, "Fruit", aka = "Ice apple, Palm fruit, Tadgola", fib = 1.0, sug = 8.0, sat = 0.0, na = 5.0, k = 300.0, ca = 25.0),
        f("Kodukkapuli", "1 cup (100 g)", 85.0, 2.5, 18.0, 0.5, "Fruit", aka = "Kodukka puli, Manila tamarind, Jungle jalebi", fib = 1.2, sug = 14.0, sat = 0.1, k = 220.0, ca = 13.0, vc = 130.0),
        f("Elandha pazham", "10 fruits (50 g)", 40.0, 0.4, 10.0, 0.1, "Fruit", aka = "Ilantha pazham, Indian jujube, Ber", fib = 0.6, sug = 7.0, sat = 0.0, k = 120.0, vc = 35.0),
        f("Panakam", "1 glass (200 ml)", 100.0, 0.3, 25.0, 0.1, "Drinks", aka = "Panagam, Jaggery ginger drink, Rama Navami panakam", sug = 24.0, na = 30.0, k = 150.0),
        f("Nilavembu kashayam", "1 cup (100 ml)", 5.0, 0.2, 1.0, 0.0, "Drinks", aka = "Nilavembu kudineer, Kashayam, Herbal decoction", na = 5.0, k = 20.0)
    )

    private fun homeProtein() = listOf(
        f("Paneer bhurji", "1 serving (150 g)", 325.0, 20.0, 8.0, 24.0, "Protein", aka = "Paneer scramble, Paneer podimas", fib = 1.5, sug = 3.0, sat = 13.0, na = 500.0, k = 200.0, ca = 400.0),
        f("Soya chunk curry", "1 cup (200 g)", 205.0, 18.0, 18.0, 8.0, "Protein", aka = "Meal maker curry, Meal maker kuzhambu, Soya chunks gravy", fib = 5.0, sug = 3.0, sat = 1.5, na = 600.0, k = 700.0, ca = 150.0, fe = 6.0),
        f("Chicken liver fry", "1 serving (100 g)", 185.0, 20.0, 4.0, 10.0, "Protein", aka = "Kozhi eral fry, Chicken liver masala", fib = 0.5, sug = 1.0, sat = 3.0, na = 400.0, k = 280.0, fe = 9.0, va = 3300.0, b12 = 17.0, fol = 500.0)
    )

    private fun karnataka() = listOf(
        f("Mangalorean chicken ghee roast", "1 serving (150 g)", 310.0, 27.0, 6.0, 20.0, "Protein", aka = "Chicken ghee roast, Kundapur ghee roast", fib = 1.0, sug = 2.0, sat = 11.0, na = 700.0, k = 350.0),
        f("Kori rotti", "1 plate (300 g)", 480.0, 26.0, 55.0, 18.0, "Meals", aka = "Mangalore kori rotti, Chicken curry with rice wafers", fib = 3.0, sug = 3.0, sat = 9.0, na = 900.0, k = 500.0),
        f("Maddur vada", "1 vada (40 g)", 150.0, 2.5, 18.0, 8.0, "Snacks", aka = "Maddur vade", fib = 1.0, sug = 0.5, sat = 1.0, na = 200.0, k = 60.0),
        f("Chow chow bath", "1 plate (250 g)", 530.0, 9.0, 75.0, 22.0, "Tiffin", aka = "Khara bath and kesari bath, Bangalore chow chow bath", fib = 3.0, sug = 25.0, sat = 9.0, na = 500.0, k = 200.0)
    )
}
