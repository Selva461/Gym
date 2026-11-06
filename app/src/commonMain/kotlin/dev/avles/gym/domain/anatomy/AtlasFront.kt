package dev.avles.gym.domain.anatomy

import dev.avles.gym.domain.Muscle.ABDUCTORS
import dev.avles.gym.domain.Muscle.ABS
import dev.avles.gym.domain.Muscle.ADDUCTORS
import dev.avles.gym.domain.Muscle.BICEPS
import dev.avles.gym.domain.Muscle.BRACHIALIS
import dev.avles.gym.domain.Muscle.CALVES_GASTROC
import dev.avles.gym.domain.Muscle.CALVES_SOLEUS
import dev.avles.gym.domain.Muscle.CHEST_LOWER
import dev.avles.gym.domain.Muscle.CHEST_MID
import dev.avles.gym.domain.Muscle.CHEST_UPPER
import dev.avles.gym.domain.Muscle.DELT_FRONT
import dev.avles.gym.domain.Muscle.DELT_SIDE
import dev.avles.gym.domain.Muscle.FOREARM_EXTENSORS
import dev.avles.gym.domain.Muscle.FOREARM_FLEXORS
import dev.avles.gym.domain.Muscle.GRIP
import dev.avles.gym.domain.Muscle.HIP_FLEXORS
import dev.avles.gym.domain.Muscle.OBLIQUES
import dev.avles.gym.domain.Muscle.QUADS
import dev.avles.gym.domain.Muscle.SERRATUS
import dev.avles.gym.domain.Muscle.TRAPS_UPPER
import dev.avles.gym.domain.anatomy.BodyPart.ARM
import dev.avles.gym.domain.anatomy.BodyPart.LEG
import dev.avles.gym.domain.anatomy.BodyPart.TRUNK

/**
 * The front view, muscle by muscle. Draw order is list order, so a muscle that tucks
 * under another (side delt under front delt) comes first.
 *
 * Muscles that are one [dev.avles.gym.domain.Muscle] but several visible bellies
 * (the three quads, the segments of the abs) are separate regions with the same
 * muscle, so they light together but keep their anatomical outlines.
 */
internal object AtlasFront {
    val regions: List<Region> = listOf(
        // ---- neck and shoulders
        pair(TRAPS_UPPER, TRUNK, 90,52, 86,62, 76,67, 62,71, 76,72, 88,68),
        pair(DELT_SIDE, ARM, 54,73, 45,78, 38,90, 36,106, 41,114, 47,104, 48,90, 52,80),
        pair(DELT_FRONT, ARM, 64,71, 56,73, 50,82, 48,98, 51,114, 57,104, 63,90, 68,78),

        // ---- chest: clavicular, sternal and costal heads of the pec
        pair(CHEST_UPPER, TRUNK, 98,73, 86,70, 72,73, 64,80, 60,90, 78,88, 98,88),
        pair(CHEST_MID, TRUNK, 98,90, 80,90, 61,92, 58,102, 66,108, 84,108, 98,106),
        pair(CHEST_LOWER, TRUNK, 98,108, 84,110, 66,110, 61,114, 70,122, 86,126, 98,124),
        pair(SERRATUS, TRUNK, 60,118, 66,122, 70,132, 66,142, 61,138, 58,128),

        // ---- arm
        pair(BICEPS, ARM, 54,114, 46,116, 40,128, 38,144, 42,156, 50,150, 55,132),
        pair(BRACHIALIS, ARM, 37,128, 34,142, 35,157, 40,158, 39,146),
        pair(FOREARM_EXTENSORS, ARM, 35,159, 30,168, 26,186, 25,204, 29,206, 31,186, 36,168),
        pair(FOREARM_FLEXORS, ARM, 44,160, 37,162, 33,176, 30,194, 30,210, 35,208, 40,190, 45,172),
        pair(GRIP, ARM, 24,213, 20,226, 20,240, 25,250, 31,250, 34,236, 33,217),

        // ---- core: rectus in three segments a side, linea alba left unshaded
        span(ABS, TRUNK, 99,127, 87,128, 86,148, 99,149),
        span(ABS, TRUNK, 99,152, 86,151, 86,172, 99,173),
        span(ABS, TRUNK, 99,176, 86,175, 87,196, 92,212, 99,218),
        pair(OBLIQUES, TRUNK, 83,130, 72,140, 64,152, 63,172, 64,190, 73,201, 84,206, 84,176, 83,152),

        // ---- hips and thighs
        pair(ABDUCTORS, LEG, 62,206, 58,220, 57,240, 61,248, 66,230, 69,212),
        pair(HIP_FLEXORS, LEG, 86,212, 76,216, 76,226, 84,236, 94,236, 92,222),
        pair(QUADS, LEG, 67,218, 61,238, 60,264, 64,286, 70,296, 73,270, 73,242, 72,224),
        pair(QUADS, LEG, 76,230, 74,250, 75,274, 79,292, 87,290, 89,264, 87,242, 82,232),
        pair(QUADS, LEG, 90,260, 85,280, 88,296, 95,294, 97,278),
        pair(ADDUCTORS, LEG, 96,236, 89,242, 87,256, 91,262, 98,254, 99,240),

        // ---- lower leg: the inner gastrocnemius shows from the front, and the
        // soleus at the outer edge; tibialis is shape only
        pair(CALVES_GASTROC, LEG, 89,312, 85,328, 86,348, 91,342, 92,322),
        pair(CALVES_GASTROC, LEG, 66,312, 63,328, 65,346, 69,334, 69,318),
        pair(CALVES_SOLEUS, LEG, 65,348, 66,366, 71,378, 72,360, 69,344),
        detail(LEG, 72,312, 70,330, 72,356, 76,378, 80,376, 80,350, 80,320, 77,310)
    )
}
