package dev.avles.gym.data

/**
 * Everything the person has put into the app, read in one go for the "for AI"
 * export ([dev.avles.gym.domain.AiExport]). One field per kind of thing they can
 * enter, so a new table only has to be added here and in the export to travel.
 *
 * Photos travel as their dates, labels and notes only: the pictures themselves
 * are too large for a text file and too private to leave the phone by default.
 */
data class FullLog(
    val settings: GymSettings,
    val favouriteFoodNames: List<String>,
    val exercises: List<Exercise>,
    val exercisePhotoIds: Set<Long>,
    val splits: List<Split>,
    val splitExercises: List<SplitExercise>,
    val sessions: List<WorkoutSession>,
    val sets: List<SetRow>,
    val cardio: List<CardioEntry>,
    val food: List<FoodLogRow>,
    val customFoods: List<FoodItem>,
    val savedMeals: List<SavedMealRow>,
    val weights: List<BodyWeight>,
    val measurements: List<BodyMeasurement>,
    val dayMarkers: List<DayMarker>,
    val progressPhotos: List<ProgressPhoto>
)
