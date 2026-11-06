package dev.avles.gym.domain.anatomy

import dev.avles.gym.domain.anatomy.BodyPart.ARM
import dev.avles.gym.domain.anatomy.BodyPart.HEAD
import dev.avles.gym.domain.anatomy.BodyPart.LEG
import dev.avles.gym.domain.anatomy.BodyPart.TRUNK

/**
 * The silhouette both views stand in: head, neck, torso, arms hanging a little away
 * from the body so the arm muscles can be seen, legs a hand's width apart.
 *
 * Points are (x, y) on the viewer's left half of the 200 × 420 box, see [BodyAtlas].
 */
internal object AtlasBase {
    val regions: List<Region> = listOf(
        baseSpan(HEAD, 100,5, 91,7, 85,13, 83,23, 84,33, 87,41, 93,47, 100,49),
        baseSpan(TRUNK, 100,42, 91,44, 90,56, 84,66, 100,70),
        baseSpan(
            TRUNK,
            100,60, 82,63, 64,68, 52,75, 46,88, 50,106, 56,128, 59,150, 62,170,
            61,188, 58,204, 58,220, 66,232, 84,238, 100,240
        ),
        // upper arm, forearm, hand
        base(ARM, 52,74, 42,80, 36,96, 33,118, 32,140, 33,158, 42,163, 50,146, 54,124, 56,100, 58,84),
        base(ARM, 33,154, 28,170, 24,192, 24,212, 32,216, 38,200, 44,178, 45,160),
        base(ARM, 24,210, 19,224, 19,240, 24,252, 31,252, 35,238, 34,218),
        // thigh, knee and shin, foot
        base(LEG, 58,204, 55,234, 56,262, 61,288, 66,302, 80,306, 92,298, 97,262, 99,236, 92,226, 70,214),
        base(LEG, 66,294, 61,318, 62,344, 67,372, 70,394, 84,396, 86,374, 91,340, 93,316, 91,296),
        base(LEG, 70,390, 64,402, 60,412, 68,417, 88,417, 90,404, 86,392)
    )
}
