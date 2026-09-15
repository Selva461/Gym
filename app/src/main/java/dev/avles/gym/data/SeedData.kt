package dev.avles.gym.data

/**
 * Starter catalogue.
 *
 * IMPORTANT, AND PLEASE READ ONCE: every macro number below is an approximation
 * for a home-style South Indian portion. Restaurant oil, coconut and ghee vary
 * enormously, so treat these as a starting point, not a measurement. Every food
 * is editable in Fuel > the food's row > Edit. Correcting the five or six things
 * you eat most often is worth more than any amount of extra entries.
 */
object SeedData {

    fun exercises(): List<Exercise> = listOf(
        // Push
        Exercise(name = "Barbell bench press", muscleGroup = "Push"),
        Exercise(name = "Incline dumbbell press", muscleGroup = "Push"),
        Exercise(name = "Overhead press", muscleGroup = "Push"),
        Exercise(name = "Dumbbell shoulder press", muscleGroup = "Push"),
        Exercise(name = "Cable fly", muscleGroup = "Push"),
        Exercise(name = "Lateral raise", muscleGroup = "Push"),
        Exercise(name = "Triceps pushdown", muscleGroup = "Push"),
        Exercise(name = "Overhead triceps extension", muscleGroup = "Push"),
        Exercise(name = "Push-up", muscleGroup = "Push", isBodyweight = true),
        // Pull
        Exercise(name = "Deadlift", muscleGroup = "Pull"),
        Exercise(name = "Barbell row", muscleGroup = "Pull"),
        Exercise(name = "Lat pulldown", muscleGroup = "Pull"),
        Exercise(name = "Seated cable row", muscleGroup = "Pull"),
        Exercise(name = "Pull-up", muscleGroup = "Pull", isBodyweight = true),
        Exercise(name = "Face pull", muscleGroup = "Pull"),
        Exercise(name = "Barbell curl", muscleGroup = "Pull"),
        Exercise(name = "Hammer curl", muscleGroup = "Pull"),
        // Legs
        Exercise(name = "Back squat", muscleGroup = "Legs"),
        Exercise(name = "Romanian deadlift", muscleGroup = "Legs"),
        Exercise(name = "Leg press", muscleGroup = "Legs"),
        Exercise(name = "Leg curl", muscleGroup = "Legs"),
        Exercise(name = "Leg extension", muscleGroup = "Legs"),
        Exercise(name = "Walking lunge", muscleGroup = "Legs"),
        Exercise(name = "Standing calf raise", muscleGroup = "Legs"),
        // Core
        Exercise(name = "Hanging leg raise", muscleGroup = "Core", isBodyweight = true),
        Exercise(name = "Cable crunch", muscleGroup = "Core"),
        Exercise(name = "Plank", muscleGroup = "Core", isBodyweight = true)
    )

    fun foods(): List<FoodItem> = listOf(
        // --- Tiffin and grains ---
        FoodItem(name = "Idli", unitLabel = "1 piece", kcal = 58.0, proteinG = 2.0, carbsG = 12.0, fatG = 0.2, category = "Tiffin"),
        FoodItem(name = "Plain dosa", unitLabel = "1 dosa", kcal = 133.0, proteinG = 2.7, carbsG = 22.0, fatG = 3.7, category = "Tiffin"),
        FoodItem(name = "Ghee roast dosa", unitLabel = "1 dosa", kcal = 210.0, proteinG = 3.0, carbsG = 24.0, fatG = 11.0, category = "Tiffin"),
        FoodItem(name = "Idiyappam", unitLabel = "1 piece", kcal = 85.0, proteinG = 1.8, carbsG = 19.0, fatG = 0.3, category = "Tiffin"),
        FoodItem(name = "Appam", unitLabel = "1 piece", kcal = 120.0, proteinG = 2.2, carbsG = 24.0, fatG = 1.5, category = "Tiffin"),
        FoodItem(name = "Parotta", unitLabel = "1 parotta", kcal = 260.0, proteinG = 5.0, carbsG = 36.0, fatG = 10.0, category = "Tiffin"),
        FoodItem(name = "Chapati", unitLabel = "1 chapati", kcal = 104.0, proteinG = 3.0, carbsG = 18.0, fatG = 2.5, category = "Tiffin"),
        FoodItem(name = "Ven pongal", unitLabel = "1 cup", kcal = 280.0, proteinG = 7.0, carbsG = 40.0, fatG = 9.0, category = "Tiffin"),
        FoodItem(name = "Rava upma", unitLabel = "1 cup", kcal = 250.0, proteinG = 6.0, carbsG = 38.0, fatG = 8.0, category = "Tiffin"),
        FoodItem(name = "Steamed rice", unitLabel = "1 cup (150 g)", kcal = 205.0, proteinG = 4.2, carbsG = 45.0, fatG = 0.4, category = "Grains"),
        FoodItem(name = "Curd rice", unitLabel = "1 cup", kcal = 230.0, proteinG = 7.0, carbsG = 35.0, fatG = 6.0, category = "Grains"),
        FoodItem(name = "Bagrry's plain oats, dry", unitLabel = "40 g", kcal = 150.0, proteinG = 5.2, carbsG = 26.0, fatG = 2.7, category = "Staples"),

        // --- Gravies and sides ---
        FoodItem(name = "Sambar", unitLabel = "1 cup", kcal = 140.0, proteinG = 6.0, carbsG = 20.0, fatG = 4.0, category = "Sides"),
        FoodItem(name = "Rasam", unitLabel = "1 cup", kcal = 60.0, proteinG = 2.0, carbsG = 8.0, fatG = 2.0, category = "Sides"),
        FoodItem(name = "Coconut chutney", unitLabel = "2 tbsp", kcal = 90.0, proteinG = 1.5, carbsG = 3.0, fatG = 8.0, category = "Sides"),
        FoodItem(name = "Mixed veg poriyal", unitLabel = "1/2 cup", kcal = 80.0, proteinG = 2.0, carbsG = 8.0, fatG = 4.5, category = "Sides"),

        // --- Protein, the part that decides a recomp ---
        FoodItem(name = "Chicken curry, Chettinad", unitLabel = "1 cup (200 g)", kcal = 290.0, proteinG = 27.0, carbsG = 8.0, fatG = 17.0, category = "Protein"),
        FoodItem(name = "Chicken gravy, home style", unitLabel = "1 cup", kcal = 240.0, proteinG = 25.0, carbsG = 6.0, fatG = 13.0, category = "Protein"),
        FoodItem(name = "Chicken 65", unitLabel = "100 g", kcal = 250.0, proteinG = 20.0, carbsG = 12.0, fatG = 14.0, category = "Protein"),
        FoodItem(name = "Grilled chicken breast", unitLabel = "100 g", kcal = 165.0, proteinG = 31.0, carbsG = 0.0, fatG = 3.6, category = "Protein"),
        FoodItem(name = "Tandoori chicken, leg", unitLabel = "1 piece", kcal = 195.0, proteinG = 24.0, carbsG = 2.0, fatG = 10.0, category = "Protein"),
        FoodItem(name = "Mutton kuzhambu", unitLabel = "1 cup", kcal = 330.0, proteinG = 24.0, carbsG = 7.0, fatG = 23.0, category = "Protein"),
        FoodItem(name = "Vanjaram fish fry", unitLabel = "1 piece (100 g)", kcal = 210.0, proteinG = 22.0, carbsG = 5.0, fatG = 11.0, category = "Protein"),
        FoodItem(name = "Nethili fry", unitLabel = "50 g", kcal = 140.0, proteinG = 13.0, carbsG = 4.0, fatG = 8.0, category = "Protein"),
        FoodItem(name = "Prawn masala", unitLabel = "1 cup", kcal = 200.0, proteinG = 24.0, carbsG = 6.0, fatG = 9.0, category = "Protein"),
        FoodItem(name = "Boiled egg", unitLabel = "1 large", kcal = 78.0, proteinG = 6.3, carbsG = 0.6, fatG = 5.3, category = "Protein"),
        FoodItem(name = "Egg podimas", unitLabel = "2 eggs", kcal = 210.0, proteinG = 13.0, carbsG = 3.0, fatG = 16.0, category = "Protein"),
        FoodItem(name = "Omelette", unitLabel = "2 eggs", kcal = 200.0, proteinG = 13.0, carbsG = 2.0, fatG = 15.0, category = "Protein"),
        FoodItem(name = "Nakpro Raw Whey, unflavoured", unitLabel = "1 scoop (30 g)", kcal = 120.0, proteinG = 24.0, carbsG = 2.0, fatG = 1.5, category = "Staples"),

        // --- Dairy, drinks, snacks ---
        FoodItem(name = "Curd", unitLabel = "1 cup (200 g)", kcal = 120.0, proteinG = 7.0, carbsG = 9.0, fatG = 6.0, category = "Dairy"),
        FoodItem(name = "Toned milk", unitLabel = "200 ml", kcal = 118.0, proteinG = 6.4, carbsG = 9.6, fatG = 6.2, category = "Dairy"),
        FoodItem(name = "Filter coffee with sugar", unitLabel = "150 ml", kcal = 90.0, proteinG = 3.0, carbsG = 12.0, fatG = 3.0, category = "Drinks"),
        FoodItem(name = "Black coffee", unitLabel = "1 cup", kcal = 5.0, proteinG = 0.3, carbsG = 0.0, fatG = 0.0, category = "Drinks"),
        FoodItem(name = "Banana", unitLabel = "1 medium", kcal = 105.0, proteinG = 1.3, carbsG = 27.0, fatG = 0.4, category = "Fruit"),
        FoodItem(name = "Roasted peanuts", unitLabel = "30 g", kcal = 170.0, proteinG = 7.7, carbsG = 5.0, fatG = 14.5, category = "Snacks")
    )
}
