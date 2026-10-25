package dev.avles.gym.anim

/**
 * Everything about a lift the demonstration needs, read from its name the same way
 * the written guide reads it. Pattern and equipment arrive as the guide's own enum
 * names, so the two can never disagree about what a lift is.
 */
data class Spec(
    val name: String,
    val pattern: String,
    val equipment: String,
    val group: String
) {
    private val n = name.lowercase()
    fun has(vararg words: String) = words.any { n.contains(it) }

    val gear: String get() = equipment

    /** Hands: how far apart, as half the distance between them, in metres. */
    val gripHalf: Double
        get() = when {
            has("close-grip", "close grip", "narrow", "diamond") -> 0.17
            has("wide-grip", "wide grip", "wide") -> 0.50
            has("snatch-grip") -> 0.58
            else -> 0.38
        }

    val grip: Grip
        get() = when {
            has("reverse-grip", "reverse grip", "underhand", "chin-up", "supinated") -> Grip.UNDERHAND
            has("hammer", "neutral", "rope", "v-bar", "farmer", "suitcase") -> Grip.NEUTRAL
            has("reverse curl", "reverse wrist", "zottman") -> Grip.OVERHAND
            pattern == "CURL" -> Grip.UNDERHAND
            else -> Grip.OVERHAND
        }

    val angle: Double
        get() = when {
            has("decline") -> -18.0
            has("low incline") -> 20.0
            has("incline") -> 35.0
            else -> 0.0
        }

    val seated: Boolean get() = has("seated", "machine", "preacher") && !has("standing")
    val lying: Boolean get() = has("lying", "skull", "bench press", "floor press", "pullover", "fly") && !has("standing", "cable fly", "cable crossover")
    val oneArm: Boolean get() = has("single-arm", "one-arm", "one arm", "kroc", "meadows", "suitcase", "concentration")
    val oneLeg: Boolean get() = has("single-leg", "one-leg", "pistol", "skater", "b-stance")
    val wideStance: Boolean get() = has("sumo", "wide stance", "wide-stance", "cossack", "plie")
}

enum class Grip(val label: String) {
    OVERHAND("Overhand: palms facing away, knuckles up"),
    UNDERHAND("Underhand: palms facing up, towards you"),
    NEUTRAL("Neutral: palms facing each other, thumbs up")
}
