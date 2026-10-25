package dev.avles.gym.anim

import dev.avles.gym.domain.anatomy.BodyBuild

/**
 * One demonstrator: body shape, hair, clothes and skin, as plain data.
 *
 * Every avatar shares the same skeleton, so every motion fits every avatar with no
 * extra work. What differs is only what is sculpted around the bones and how it is
 * dressed. A new avatar, a custom one a user builds, or a different skin tone is a
 * new value of this class, never new drawing code; [BodyMesh] grows its skin from
 * whatever it is given and caches the result per avatar.
 *
 * All sizes are metres. [Shape.validated] clamps them to ranges that still make a
 * plausible person, so a bad value from a settings screen or a saved file can never
 * produce a broken mesh.
 */
data class Avatar(
    /** Stable key for saving the user's choice. Never shown. */
    val id: String,
    val label: String,
    val shape: Shape,
    val hair: Hair,
    val outfit: Outfit,
    val skin: Int,
    /** Proportions for the 2D muscle map, so it matches the demonstrator. */
    val anatomy: BodyBuild = BodyBuild.MALE
) {
    companion object {
        /** A man of ordinary build who trains a couple of times a week. */
        val MALE = Avatar(
            id = "male", label = "Male",
            shape = Shape(),
            hair = Hair(HairStyle.SHORT, 0xFF2B221E.toInt()),
            outfit = Outfit(TopStyle.TEE, 0xFF3E5D82.toInt(), BottomStyle.SHORTS, 0xFF2E333C.toInt()),
            skin = 0xFFC9977A.toInt()
        )

        /** A woman of ordinary build who trains a couple of times a week. */
        val FEMALE = Avatar(
            id = "female", label = "Female",
            shape = Shape(
                hipWidth = 0.172, hipDepth = 0.108, glutes = 1.12, bellyWidth = 0.128, bellyDepth = 0.095,
                waistWidth = 0.114, waistDepth = 0.092, chestWidth = 0.134, chestDepth = 0.104,
                backWidth = 0.145, backDepth = 0.075, bust = 1.0, traps = 0.047, neck = 0.05,
                head = 0.95, arms = 0.86, thighs = 0.09, innerThigh = 0.062, calves = 0.046
            ),
            hair = Hair(HairStyle.PONYTAIL, 0xFF3B2A21.toInt()),
            outfit = Outfit(TopStyle.TANK, 0xFF3F7F86.toInt(), BottomStyle.LEGGINGS, 0xFF2A2D38.toInt()),
            skin = 0xFFC9977A.toInt(),
            anatomy = BodyBuild.FEMALE
        )

        val PRESETS: List<Avatar> = listOf(MALE, FEMALE)

        /** The preset saved under [id], or the first preset for an unknown or missing id. */
        fun byId(id: String?): Avatar = PRESETS.firstOrNull { it.id == id } ?: PRESETS.first()

        /** The saved choice, or before anything is chosen the preset matching [female]. */
        fun chosen(id: String?, female: Boolean): Avatar =
            if (id.isNullOrBlank()) (if (female) FEMALE else MALE) else byId(id)
    }
}

/**
 * The body's proportions: half-widths and half-depths of the main masses, plus
 * scale factors where a size is relative. The defaults are the male preset.
 */
data class Shape(
    val hipWidth: Double = 0.158,
    val hipDepth: Double = 0.105,
    /** Seat size relative to the default. */
    val glutes: Double = 1.0,
    val bellyWidth: Double = 0.142,
    val bellyDepth: Double = 0.105,
    val waistWidth: Double = 0.14,
    val waistDepth: Double = 0.108,
    val chestWidth: Double = 0.154,
    val chestDepth: Double = 0.118,
    val backWidth: Double = 0.168,
    val backDepth: Double = 0.085,
    /** 0 for a flat male chest with pecs; around 1 for an average bust. */
    val bust: Double = 0.0,
    val traps: Double = 0.05,
    val neck: Double = 0.056,
    /** Head size relative to the default. */
    val head: Double = 1.0,
    /** Arm thickness relative to the default. */
    val arms: Double = 1.0,
    /** Thigh radius at the hip. */
    val thighs: Double = 0.085,
    val innerThigh: Double = 0.055,
    val calves: Double = 0.05
) {
    /** The same shape with every value pulled into a range that still sculpts a person. */
    fun validated(): Shape = Shape(
        hipWidth = hipWidth.fit(0.12, 0.22), hipDepth = hipDepth.fit(0.08, 0.15), glutes = glutes.fit(0.7, 1.5),
        bellyWidth = bellyWidth.fit(0.1, 0.2), bellyDepth = bellyDepth.fit(0.07, 0.17),
        waistWidth = waistWidth.fit(0.09, 0.2), waistDepth = waistDepth.fit(0.07, 0.17),
        chestWidth = chestWidth.fit(0.11, 0.2), chestDepth = chestDepth.fit(0.08, 0.16),
        backWidth = backWidth.fit(0.12, 0.22), backDepth = backDepth.fit(0.06, 0.12),
        bust = bust.fit(0.0, 1.6), traps = traps.fit(0.03, 0.07), neck = neck.fit(0.04, 0.07),
        head = head.fit(0.85, 1.12), arms = arms.fit(0.7, 1.4), thighs = thighs.fit(0.065, 0.12),
        innerThigh = innerThigh.fit(0.04, 0.08), calves = calves.fit(0.035, 0.07)
    )

    private fun Double.fit(lo: Double, hi: Double) = if (isNaN()) lo else coerceIn(lo, hi)
}

enum class HairStyle { SHORT, PONYTAIL, NONE }

data class Hair(val style: HairStyle, val colour: Int)

enum class TopStyle { TEE, TANK }
enum class BottomStyle { SHORTS, LEGGINGS }

data class Outfit(
    val top: TopStyle,
    val topColour: Int,
    val bottom: BottomStyle,
    val bottomColour: Int,
    val shoeColour: Int = 0xFFE3E5E9.toInt(),
    val soleColour: Int = 0xFF41464F.toInt()
)
