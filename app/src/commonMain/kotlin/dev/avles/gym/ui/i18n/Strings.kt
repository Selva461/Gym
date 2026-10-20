package dev.avles.gym.ui.i18n

/**
 * Tamil for the words you see most: tabs, screen titles, section headings and the
 * main buttons, the way the Indian diet apps offer a regional language. Longer
 * explanatory text stays English for now. Any label without a translation falls
 * back to English rather than showing a key.
 */
object Strings {
    const val ENGLISH = "en"
    const val TAMIL = "ta"

    private val TA = mapOf(
        "Today" to "இன்று",
        "Train" to "பயிற்சி",
        "Fuel" to "உணவு",
        "Progress" to "முன்னேற்றம்",
        "Settings" to "அமைப்புகள்",
        "Training" to "பயிற்சி",
        "Logged today" to "இன்று பதிவு செய்தவை",
        "Log food" to "உணவைப் பதிவு செய்",
        "Start a session" to "பயிற்சியைத் தொடங்கு",
        "Open session" to "பயிற்சியைத் திற",
        "Log set" to "செட்டைப் பதிவு செய்",
        "Finish" to "முடி",
        "Weigh in" to "எடை பதிவு",
        "Save settings" to "அமைப்புகளைச் சேமி",
        "Usual suspects" to "வழக்கமானவை",
        "Recent" to "சமீபத்தியவை",
        "Saved meals" to "சேமித்த உணவுகள்",
        "Your splits" to "உங்கள் பயிற்சித் திட்டங்கள்",
        "Recent sessions" to "சமீபத்திய பயிற்சிகள்",
        "This week" to "இந்த வாரம்",
        "Achievements" to "சாதனைகள்",
        "Body weight" to "உடல் எடை",
        "Consistency" to "தொடர்ச்சி",
        "Nutrients" to "ஊட்டச்சத்துகள்",
        "Fasting" to "உண்ணாவிரதம்",
        "Cardio" to "கார்டியோ",
        "Search food" to "உணவைத் தேடு",
        "Daily targets" to "தினசரி இலக்குகள்",
        "Rest timer" to "ஓய்வு நேரம்",
        "Nightly reminder" to "இரவு நினைவூட்டல்",
        "Your data" to "உங்கள் தரவு",
        "Language" to "மொழி",
        "Protein" to "புரதம்",
        "Carbs" to "மாவுச்சத்து",
        "Fat" to "கொழுப்பு",
        "Breakfast" to "காலை உணவு",
        "Lunch" to "மதிய உணவு",
        "Snack" to "சிற்றுண்டி",
        "Dinner" to "இரவு உணவு"
    )

    fun of(language: String, english: String): String =
        if (language == TAMIL) TA[english] ?: english else english
}
