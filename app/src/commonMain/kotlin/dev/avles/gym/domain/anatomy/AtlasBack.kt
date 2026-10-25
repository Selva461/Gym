package dev.avles.gym.domain.anatomy

import dev.avles.gym.domain.Muscle.ABDUCTORS
import dev.avles.gym.domain.Muscle.ADDUCTORS
import dev.avles.gym.domain.Muscle.CALVES_GASTROC
import dev.avles.gym.domain.Muscle.CALVES_SOLEUS
import dev.avles.gym.domain.Muscle.DELT_REAR
import dev.avles.gym.domain.Muscle.DELT_SIDE
import dev.avles.gym.domain.Muscle.ERECTORS
import dev.avles.gym.domain.Muscle.FOREARM_EXTENSORS
import dev.avles.gym.domain.Muscle.FOREARM_FLEXORS
import dev.avles.gym.domain.Muscle.GLUTES
import dev.avles.gym.domain.Muscle.GRIP
import dev.avles.gym.domain.Muscle.HAMSTRINGS
import dev.avles.gym.domain.Muscle.LATS
import dev.avles.gym.domain.Muscle.OBLIQUES
import dev.avles.gym.domain.Muscle.RHOMBOIDS
import dev.avles.gym.domain.Muscle.TRAPS_MID
import dev.avles.gym.domain.Muscle.TRAPS_UPPER
import dev.avles.gym.domain.Muscle.TRICEPS
import dev.avles.gym.domain.anatomy.BodyPart.ARM
import dev.avles.gym.domain.anatomy.BodyPart.LEG
import dev.avles.gym.domain.anatomy.BodyPart.TRUNK

/**
 * The back view, muscle by muscle, on the same silhouette as the front. Draw order
 * is list order: the soleus is listed before the gastrocnemius that covers it.
 *
 * The rhomboids sit under the traps in life; like every anatomy chart, the map
 * draws them in the window between the mid and lower traps so they can be lit.
 */
internal object AtlasBack {
    val regions: List<Region> = listOf(
        // ---- traps: upper, then mid, then the lower fibres (filed under mid)
        span(TRAPS_UPPER, TRUNK, 100,46, 91,50, 87,62, 72,68, 58,74, 76,77, 90,80, 100,82),
        pair(TRAPS_MID, TRUNK, 99,83, 88,81, 74,79, 62,81, 74,89, 90,91, 99,92),
        pair(RHOMBOIDS, TRUNK, 99,94, 90,93, 80,95, 77,104, 85,111, 99,112),
        pair(TRAPS_MID, TRUNK, 99,114, 88,113, 85,122, 93,134, 99,139),

        // ---- shoulder blade and shoulder
        detail(TRUNK, 76,92, 63,88, 57,100, 62,114, 74,112, 79,102),
        pair(DELT_SIDE, ARM, 54,73, 45,78, 38,90, 36,106, 41,114, 47,104, 48,90, 52,80),
        pair(DELT_REAR, ARM, 62,76, 53,78, 47,88, 46,104, 50,112, 56,100, 62,88),

        // ---- lats and lower back
        pair(LATS, TRUNK, 78,114, 62,114, 56,124, 57,144, 61,164, 68,180, 80,192, 90,198, 89,172, 88,150, 84,132),
        pair(OBLIQUES, TRUNK, 62,168, 61,188, 64,200, 72,198, 70,184),
        pair(ERECTORS, TRUNK, 99,142, 93,140, 89,160, 88,184, 91,204, 99,210),

        // ---- arm
        pair(TRICEPS, ARM, 39,114, 34,130, 34,148, 39,152, 43,136, 45,118),
        pair(TRICEPS, ARM, 49,112, 46,128, 44,150, 41,158, 50,152, 54,130, 55,116),
        pair(FOREARM_FLEXORS, ARM, 44,160, 39,172, 38,192, 36,208, 39,208, 43,190, 46,172),
        pair(FOREARM_EXTENSORS, ARM, 39,160, 32,164, 28,180, 26,200, 28,210, 34,208, 37,188, 40,170),
        pair(GRIP, ARM, 24,213, 20,226, 20,240, 25,250, 31,250, 34,236, 33,217),

        // ---- hips: glute medius above and to the side, glute max over it
        pair(ABDUCTORS, LEG, 86,198, 72,198, 61,206, 59,220, 66,214, 80,206),
        pair(GLUTES, LEG, 98,206, 82,206, 66,214, 60,230, 63,250, 78,258, 94,256, 99,238),

        // ---- thighs: biceps femoris outside, semitendinosus inside, adductor magnus between
        pair(ADDUCTORS, LEG, 98,258, 91,262, 91,276, 98,272),
        pair(HAMSTRINGS, LEG, 66,256, 62,272, 63,292, 71,302, 78,284, 78,262),
        pair(HAMSTRINGS, LEG, 81,262, 81,286, 86,300, 94,298, 96,280, 92,264),

        // ---- lower leg
        pair(CALVES_SOLEUS, LEG, 64,338, 68,368, 75,386, 85,386, 90,364, 91,342, 80,358, 72,354),
        pair(CALVES_GASTROC, LEG, 67,304, 62,320, 64,344, 72,354, 78,334, 78,312),
        pair(CALVES_GASTROC, LEG, 81,304, 81,332, 84,354, 90,346, 92,322, 90,306)
    )
}
