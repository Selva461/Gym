package dev.avles.gym.anim

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * The one figure every demonstration uses: proportions of a 1.78 m adult, no face.
 *
 * It is a template on purpose. Every lift is the same body doing a different thing,
 * so what changes between two demos is the movement and the equipment, never the
 * character — and a lift added next year is drawn by the same body as this one.
 */
object Body {
    const val HIP_HALF = 0.10
    const val SPINE = 0.50
    const val SHOULDER_HALF = 0.19
    const val NECK = 0.09
    const val HEAD_R = 0.105
    const val UPPER_ARM = 0.29
    const val FOREARM = 0.33 // elbow to the middle of the grip
    const val THIGH = 0.44
    const val SHIN = 0.43
    const val ANKLE_H = 0.08
    const val FOOT = 0.24

    /** Pelvis height standing tall with feet under the hips. */
    const val STAND = THIGH + SHIN + ANKLE_H - 0.01
}

/**
 * One key position of a movement.
 *
 * The body is posed the way a coach would describe it — where the hips are, how far
 * the torso leans, where the hands and feet are — and the elbows and knees are worked
 * out from that ([Skeleton.solve]). Hands and feet are therefore exactly where the
 * equipment is: a bar never floats a centimetre off the palm, and a foot never sinks
 * through the floor, because those positions are the inputs rather than the results.
 *
 * Poles say which way an elbow or a knee points, in the torso's own frame
 * (x outwards on that side, y along the spine, z the way the chest faces).
 */
data class Pose(
    val pelvis: Vec3,
    val pitch: Double = 0.0,
    val yaw: Double = 0.0,
    val roll: Double = 0.0,
    val head: Double = 0.0,
    val handL: Vec3,
    val handR: Vec3,
    val footL: Vec3,
    val footR: Vec3,
    val elbowPole: Vec3 = Vec3(0.35, -1.0, -0.4),
    val kneePole: Vec3 = Vec3(0.15, 0.0, 1.0),
    /** Which way the feet point, about the vertical. */
    val footYaw: Double = 0.0,
    /** 0 flat foot, 1 up on the toes. */
    val toes: Double = 0.0,
    /** Toes pointing down from the ankle, radians: about 80° in a plank or a push-up. */
    val footPitch: Double = 0.0,
    /** Shoulders lifted towards the ears, metres: a shrug. */
    val shrug: Double = 0.0,
    /** Upper body turned against the hips about the spine, radians: a twist or a chop. */
    val twist: Double = 0.0,
    /** Separate poles for one side, for single-arm work. Null mirrors the main one. */
    val elbowPoleR: Vec3? = null,
    val kneePoleR: Vec3? = null,
    /** The right foot's toes and pitch when it differs from the left (a lunge's back foot). */
    val toesR: Double? = null,
    val footPitchR: Double? = null
) {
    fun lerpTo(o: Pose, t: Double) = Pose(
        pelvis = lerp(pelvis, o.pelvis, t),
        pitch = lerp(pitch, o.pitch, t), yaw = lerp(yaw, o.yaw, t), roll = lerp(roll, o.roll, t),
        head = lerp(head, o.head, t),
        handL = lerp(handL, o.handL, t), handR = lerp(handR, o.handR, t),
        footL = lerp(footL, o.footL, t), footR = lerp(footR, o.footR, t),
        elbowPole = lerp(elbowPole, o.elbowPole, t), kneePole = lerp(kneePole, o.kneePole, t),
        footYaw = lerp(footYaw, o.footYaw, t), toes = lerp(toes, o.toes, t),
        footPitch = lerp(footPitch, o.footPitch, t),
        shrug = lerp(shrug, o.shrug, t), twist = lerp(twist, o.twist, t),
        elbowPoleR = if (elbowPoleR == null && o.elbowPoleR == null) null
        else lerp(elbowPoleR ?: elbowPole, o.elbowPoleR ?: o.elbowPole, t),
        kneePoleR = if (kneePoleR == null && o.kneePoleR == null) null
        else lerp(kneePoleR ?: kneePole, o.kneePoleR ?: o.kneePole, t),
        toesR = if (toesR == null && o.toesR == null) null else lerp(toesR ?: toes, o.toesR ?: o.toes, t),
        footPitchR = if (footPitchR == null && o.footPitchR == null) null
        else lerp(footPitchR ?: footPitch, o.footPitchR ?: o.footPitch, t)
    )

    /** The same pose with both hands moved by [d]. */
    fun hands(d: Vec3) = copy(handL = handL + d, handR = handR + d)
}

/** Every joint of the figure for one moment, in world space. */
data class Skeleton(
    val pelvis: Vec3, val chest: Vec3, val neck: Vec3, val head: Vec3,
    val torso: Basis,
    /** The pelvis's own frame (the torso above it can twist against it) and the head's. */
    val hips: Basis, val headFrame: Basis,
    val shoulderL: Vec3, val shoulderR: Vec3,
    val elbowL: Vec3, val elbowR: Vec3,
    val handL: Vec3, val handR: Vec3,
    val hipL: Vec3, val hipR: Vec3,
    val kneeL: Vec3, val kneeR: Vec3,
    val ankleL: Vec3, val ankleR: Vec3,
    val toeL: Vec3, val toeR: Vec3,
    val heelL: Vec3, val heelR: Vec3
) {
    companion object {

        fun solve(p: Pose): Skeleton {
            val b = Basis.of(p.yaw, p.pitch, p.roll)
            val neck = p.pelvis + b.up * Body.SPINE
            val chest = p.pelvis + b.up * (Body.SPINE * 0.62)
            val headBasis = Basis.of(p.yaw, p.pitch + p.head, p.roll)
            val head = neck + headBasis.up * (Body.NECK + Body.HEAD_R * 0.8)
            val upper = if (p.twist == 0.0) b else Basis.of(p.yaw + p.twist, p.pitch, p.roll)
            val shoulderL = neck + b.up * (p.shrug - 0.05) + upper.side * Body.SHOULDER_HALF
            val shoulderR = neck + b.up * (p.shrug - 0.05) - upper.side * Body.SHOULDER_HALF
            val hipL = p.pelvis + b.side * Body.HIP_HALF - b.up * 0.03
            val hipR = p.pelvis - b.side * Body.HIP_HALF - b.up * 0.03

            fun pole(local: Vec3, sign: Double) = b.apply(Vec3(local.x * sign, local.y, local.z))
            fun armPole(local: Vec3, sign: Double) = upper.apply(Vec3(local.x * sign, local.y, local.z))

            val elbowL = ik(shoulderL, p.handL, Body.UPPER_ARM, Body.FOREARM, armPole(p.elbowPole, 1.0))
            val elbowR = ik(shoulderR, p.handR, Body.UPPER_ARM, Body.FOREARM, armPole(p.elbowPoleR ?: p.elbowPole, -1.0))
            val handL = reach(shoulderL, elbowL, p.handL, Body.FOREARM)
            val handR = reach(shoulderR, elbowR, p.handR, Body.FOREARM)

            // The ankle is the input. On the toes it is lifted and the toe stays put;
            // with the foot pitched (a plank) the toes point at the floor.
            fun dir(splay: Double, fp: Double) = Vec3(
                sin(p.yaw + p.footYaw + splay) * cos(fp), -sin(fp), cos(p.yaw + p.footYaw + splay) * cos(fp)
            )
            fun toe(ankle: Vec3, splay: Double, fp: Double, toes: Double): Vec3 =
                ankle + dir(splay, fp) * (Body.FOOT * 0.72) +
                    Vec3(0.0, -(Body.ANKLE_H - 0.02) * cos(fp) - toes * 0.09, 0.0)
            fun heel(ankle: Vec3, splay: Double, fp: Double): Vec3 =
                ankle - dir(splay, fp) * (Body.FOOT * 0.22) + Vec3(0.0, -(Body.ANKLE_H - 0.03) * cos(fp), 0.0)
            val fpR = p.footPitchR ?: p.footPitch
            val toesR = p.toesR ?: p.toes

            // A foot never goes through the floor: if a pose asks for that (up on the toes,
            // toes pointed in a plank), the ankle is raised just enough and the leg re-aims.
            fun grounded(target: Vec3, splay: Double, fp: Double, toes: Double): Vec3 {
                val low = min(toe(target, splay, fp, toes).y, heel(target, splay, fp).y)
                return if (low < 0.0) Vec3(target.x, target.y - low, target.z) else target
            }
            val footL = grounded(p.footL, 0.12, p.footPitch, p.toes)
            val footR = grounded(p.footR, -0.12, fpR, toesR)

            val kneeL = ik(hipL, footL, Body.THIGH, Body.SHIN, pole(p.kneePole, 1.0))
            val kneeR = ik(hipR, footR, Body.THIGH, Body.SHIN, pole(p.kneePoleR ?: p.kneePole, -1.0))
            val ankleL = reach(hipL, kneeL, footL, Body.SHIN)
            val ankleR = reach(hipR, kneeR, footR, Body.SHIN)

            return Skeleton(
                pelvis = p.pelvis, chest = chest, neck = neck, head = head, torso = upper,
                hips = b, headFrame = headBasis,
                shoulderL = shoulderL, shoulderR = shoulderR,
                elbowL = elbowL, elbowR = elbowR, handL = handL, handR = handR,
                hipL = hipL, hipR = hipR, kneeL = kneeL, kneeR = kneeR,
                ankleL = ankleL, ankleR = ankleR,
                toeL = toe(ankleL, 0.12, p.footPitch, p.toes), toeR = toe(ankleR, -0.12, fpR, toesR),
                heelL = heel(ankleL, 0.12, p.footPitch), heelR = heel(ankleR, -0.12, fpR)
            )
        }

        /**
         * Two-bone inverse kinematics: where the middle joint goes so the end reaches
         * [target], bending towards [pole]. Out of reach, the limb straightens and
         * points at the target instead of stretching.
         */
        fun ik(root: Vec3, target: Vec3, a: Double, b: Double, pole: Vec3): Vec3 {
            val toT = target - root
            val d = min(max(toT.length, 1e-4), (a + b) * 0.9995)
            val dir = toT.normalized().let { if (it.length < 0.5) Vec3(0.0, -1.0, 0.0) else it }
            val cosA = ((a * a + d * d - b * b) / (2 * a * d)).coerceIn(-1.0, 1.0)
            val angle = acos(cosA)
            var perp = pole - dir * (pole dot dir)
            if (perp.length < 1e-6) perp = (dir cross Vec3.X).let { if (it.length < 1e-6) dir cross Vec3.Z else it }
            perp = perp.normalized()
            return root + dir * (a * cos(angle)) + perp * (a * sin(angle))
        }

        /** The end of a limb: the target, or as far towards it as the limb reaches. */
        private fun reach(root: Vec3, mid: Vec3, target: Vec3, b: Double): Vec3 =
            mid + (target - mid).normalized() * b
    }
}
