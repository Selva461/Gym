package dev.avles.gym.anim

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * The bones the skin hangs from. Each sculpted shape belongs to one, and each point
 * of the skin follows a blend of the few bones it is closest to.
 */
object Bone {
    const val PELVIS = 0
    const val CHEST = 1
    const val HEAD = 2
    const val UPPER_ARM_L = 3
    const val FOREARM_L = 4
    const val UPPER_ARM_R = 5
    const val FOREARM_R = 6
    const val THIGH_L = 7
    const val SHIN_L = 8
    const val FOOT_L = 9
    const val THIGH_R = 10
    const val SHIN_R = 11
    const val FOOT_R = 12
    const val COUNT = 13

    fun isArm(b: Int) = b in UPPER_ARM_L..FOREARM_R
    fun isUpperArm(b: Int) = b == UPPER_ARM_L || b == UPPER_ARM_R
    fun isForearm(b: Int) = b == FOREARM_L || b == FOREARM_R
    fun isThigh(b: Int) = b == THIGH_L || b == THIGH_R
    fun isShin(b: Int) = b == SHIN_L || b == SHIN_R
    fun isFoot(b: Int) = b == FOOT_L || b == FOOT_R
}

/**
 * One sculpted volume: a tapered rod or an ellipsoid, as a signed distance in metres.
 * Bodies are a smooth blend of about fifty of these, the way a sculptor blocks out a
 * figure in clay, and the skin is the surface of the blend.
 */
sealed class Blob(val bone: Int, val hair: Boolean) {
    abstract val cx: Double; abstract val cy: Double; abstract val cz: Double
    /** Radius of a sphere around (cx, cy, cz) holding the whole shape. */
    abstract val reach: Double
    abstract fun dist(x: Double, y: Double, z: Double): Double

    class Rod(val a: Vec3, val b: Vec3, val ra: Double, val rb: Double, bone: Int, hair: Boolean = false) : Blob(bone, hair) {
        private val d = b - a
        private val len2 = d dot d
        override val cx = (a.x + b.x) / 2; override val cy = (a.y + b.y) / 2; override val cz = (a.z + b.z) / 2
        override val reach = sqrt(len2) / 2 + max(ra, rb)
        override fun dist(x: Double, y: Double, z: Double): Double {
            val px = x - a.x; val py = y - a.y; val pz = z - a.z
            val t = ((px * d.x + py * d.y + pz * d.z) / len2).coerceIn(0.0, 1.0)
            val qx = px - d.x * t; val qy = py - d.y * t; val qz = pz - d.z * t
            return sqrt(qx * qx + qy * qy + qz * qz) - (ra + (rb - ra) * t)
        }
    }

    class Ellipsoid(val c: Vec3, val axes: Basis, val r: Vec3, bone: Int, hair: Boolean = false) : Blob(bone, hair) {
        override val cx = c.x; override val cy = c.y; override val cz = c.z
        override val reach = maxOf(r.x, r.y, r.z)
        override fun dist(x: Double, y: Double, z: Double): Double {
            val px = x - c.x; val py = y - c.y; val pz = z - c.z
            val lx = (px * axes.side.x + py * axes.side.y + pz * axes.side.z) / r.x
            val ly = (px * axes.up.x + py * axes.up.y + pz * axes.up.z) / r.y
            val lz = (px * axes.fwd.x + py * axes.fwd.y + pz * axes.fwd.z) / r.z
            val k0 = sqrt(lx * lx + ly * ly + lz * lz)
            val k1 = sqrt(lx * lx / (r.x * r.x) + ly * ly / (r.y * r.y) + lz * lz / (r.z * r.z))
            return if (k1 < 1e-9) -minOf(r.x, r.y, r.z) else k0 * (k0 - 1.0) / k1
        }
    }
}

/**
 * The two bodies, sculpted around the rest skeleton.
 *
 * Proportions are an average adult's rather than an athlete's: a little softness at
 * the waist, ordinary shoulders, no showpiece muscle. The face is left smooth on
 * purpose (only the shape of a nose and ears, so you can tell which way the head
 * faces); hair, clothes and shoes are what make the figure read as a person.
 */
object Sculpt {

    /** Arms a little out from the sides and feet apart, so the limbs sculpt cleanly. */
    val REST: Pose = Pose(
        pelvis = v(0.0, Body.STAND, 0.0),
        handL = v(0.53, 0.86, 0.06), handR = v(-0.53, 0.86, 0.06),
        footL = v(0.13, Body.ANKLE_H, 0.0), footR = v(-0.13, Body.ANKLE_H, 0.0)
    )
    val rest: Skeleton by lazy { Skeleton.solve(REST) }

    /** How much neighbouring shapes melt together, metres. */
    const val BLEND = 0.022

    fun blobs(avatar: Avatar): List<Blob> {
        val s = rest
        val d = avatar.shape.validated()
        val out = ArrayList<Blob>(64)
        fun e(c: Vec3, rx: Double, ry: Double, rz: Double, bone: Int, axes: Basis = Basis.IDENTITY, hair: Boolean = false) {
            out += Blob.Ellipsoid(c, axes, v(rx, ry, rz), bone, hair)
        }
        fun rod(a: Vec3, b: Vec3, ra: Double, rb: Double, bone: Int, hair: Boolean = false) {
            out += Blob.Rod(a, b, ra, rb, bone, hair)
        }
        val P = s.pelvis.y

        // ---- pelvis: hips, seat, lower belly -------------------------------
        e(v(0.0, P - 0.02, -0.005), d.hipWidth, 0.105, d.hipDepth, Bone.PELVIS)
        for (sx in listOf(1.0, -1.0)) {
            val g = d.glutes
            e(v(sx * (0.072 + (g - 1) * 0.067), P - 0.075, -0.055), 0.082 * g, 0.092 * (1 + (g - 1) * 0.7), 0.072 * g, Bone.PELVIS)
        }
        e(v(0.0, P + 0.07, 0.012), d.bellyWidth, 0.095, d.bellyDepth, Bone.PELVIS)

        // ---- trunk: waist, ribs, chest, back, shoulders ---------------------
        e(v(0.0, P + 0.17, 0.008), d.waistWidth, 0.11, d.waistDepth, Bone.CHEST)
        e(v(0.0, P + 0.31, -0.002), d.chestWidth, 0.15, d.chestDepth, Bone.CHEST)
        e(v(0.0, P + 0.37, -0.03), d.backWidth, 0.11, d.backDepth, Bone.CHEST)
        for (sx in listOf(1.0, -1.0)) {
            if (d.bust > 0.05) {
                val k = 0.8 + 0.2 * d.bust
                e(v(sx * 0.062, P + 0.33, 0.07 * k), 0.068 * k, 0.062 * k, 0.062 * k, Bone.CHEST)
            } else {
                e(v(sx * 0.07, P + 0.36, 0.048), 0.085, 0.064, 0.055, Bone.CHEST)
            }
            // Trapezius: the slope from the neck down to the shoulder.
            rod(v(sx * 0.035, P + 0.5, -0.025), v(sx * 0.15, P + 0.45, -0.012), d.traps, d.traps * 0.83, Bone.CHEST)
        }

        // ---- head and neck ---------------------------------------------------
        val n = s.neck
        val hr = d.head
        rod(n + v(0.0, -0.04, -0.012), n + v(0.0, 0.1, 0.006), d.neck, d.neck * 0.9, Bone.HEAD)
        val skull = n + v(0.0, 0.19 * hr, -0.008)
        e(skull, 0.077 * hr, 0.098 * hr, 0.094 * hr, Bone.HEAD)
        e(n + v(0.0, 0.12 * hr, 0.018), 0.058 * hr, 0.06 * hr, 0.066 * hr, Bone.HEAD) // jaw and chin
        e(n + v(0.0, 0.205 * hr, 0.028), 0.07 * hr, 0.062 * hr, 0.068 * hr, Bone.HEAD) // brow and forehead
        e(n + v(0.0, 0.165 * hr, 0.092 * hr), 0.011, 0.02, 0.014, Bone.HEAD) // nose
        for (sx in listOf(1.0, -1.0)) e(n + v(sx * 0.077 * hr, 0.17 * hr, -0.005), 0.012, 0.027, 0.019, Bone.HEAD)
        when (avatar.hair.style) {
            HairStyle.PONYTAIL -> {
                e(skull + v(0.0, 0.022 * hr, -0.008), 0.086 * hr, 0.094 * hr, 0.102 * hr, Bone.HEAD, hair = true)
                e(skull + v(0.0, 0.03 * hr, -0.098 * hr), 0.042, 0.045, 0.04, Bone.HEAD, hair = true) // gathered at the crown
                rod(skull + v(0.0, 0.0, -0.11 * hr), skull + v(0.0, -0.13, -0.12 * hr), 0.032, 0.02, Bone.HEAD, hair = true) // ponytail
            }
            HairStyle.SHORT -> e(skull + v(0.0, 0.03 * hr, -0.012), 0.083 * hr, 0.088 * hr, 0.098 * hr, Bone.HEAD, hair = true)
            HairStyle.NONE -> Unit
        }

        // ---- arms -------------------------------------------------------------
        for (left in listOf(true, false)) {
            val sh = if (left) s.shoulderL else s.shoulderR
            val el = if (left) s.elbowL else s.elbowR
            val ha = if (left) s.handL else s.handR
            val ua = if (left) Bone.UPPER_ARM_L else Bone.UPPER_ARM_R
            val fa = if (left) Bone.FOREARM_L else Bone.FOREARM_R
            val sx = if (left) 1.0 else -1.0
            val ub = along(sh, el)
            val k = d.arms
            e(sh + ub.up * 0.035 + v(sx * 0.008, 0.0, 0.0), 0.058 * k, 0.075 * k, 0.06 * k, ua, ub) // deltoid
            rod(sh, el, 0.05 * k, 0.04 * k, ua)
            e(lerp(sh, el, 0.48) + ub.fwd * 0.016 * k, 0.04 * k, 0.1, 0.036 * k, ua, ub) // biceps
            e(lerp(sh, el, 0.42) - ub.fwd * 0.018 * k, 0.042 * k, 0.11, 0.038 * k, ua, ub) // triceps
            val fb = along(el, ha)
            val wrist = el + fb.up * (Body.FOREARM - 0.075)
            e(lerp(el, wrist, 0.3), 0.045 * k, 0.1, 0.04 * k, fa, fb)
            rod(el, wrist, 0.042 * k, 0.026 * k, fa)
            // A closed hand: palm, curled fingers, thumb.
            e(ha - fb.up * 0.02, 0.026 * k, 0.05 * k, 0.04 * k, fa, fb)
            e(ha + fb.up * 0.03 + fb.fwd * 0.006, 0.024 * k, 0.03 * k, 0.04 * k, fa, fb)
            e(ha + fb.fwd * 0.03 - fb.up * 0.01, 0.014, 0.03, 0.014, fa, fb)
        }

        // ---- legs -------------------------------------------------------------
        for (left in listOf(true, false)) {
            val hip = if (left) s.hipL else s.hipR
            val knee = if (left) s.kneeL else s.kneeR
            val ankle = if (left) s.ankleL else s.ankleR
            val heel = if (left) s.heelL else s.heelR
            val toe = if (left) s.toeL else s.toeR
            val th = if (left) Bone.THIGH_L else Bone.THIGH_R
            val sh = if (left) Bone.SHIN_L else Bone.SHIN_R
            val ft = if (left) Bone.FOOT_L else Bone.FOOT_R
            val sx = if (left) 1.0 else -1.0
            val tb = along(hip, knee)
            rod(hip + v(sx * 0.01, 0.0, 0.0), knee, d.thighs, 0.052, th)
            e(lerp(hip, knee, 0.42) + tb.fwd * 0.03 + v(sx * 0.008, 0.0, 0.0), 0.062, 0.17, 0.056, th, tb) // quads
            e(lerp(hip, knee, 0.4) - tb.fwd * 0.03, 0.06, 0.16, 0.052, th, tb) // hamstrings
            e(lerp(hip, knee, 0.3) - v(sx * 0.03, 0.0, 0.0), d.innerThigh, 0.12, 0.055, th, tb) // inner thigh
            e(knee + tb.fwd * 0.032, 0.036, 0.034, 0.024, sh, tb) // kneecap
            val sb = along(knee, ankle)
            rod(knee, ankle, 0.052, 0.036, sh)
            e(lerp(knee, ankle, 0.3) - sb.fwd * 0.03, d.calves, 0.11, 0.046, sh, sb) // calf
            // A trainer: heel to toe, flat underneath.
            val fb = alongFlat(heel, toe)
            e(lerp(heel, toe, 0.48) + v(0.0, 0.015, 0.0), 0.046, 0.04, 0.142, ft, fb)
            rod(ankle - v(0.0, 0.01, 0.0), heel + v(0.0, 0.02, 0.01), 0.04, 0.035, ft)
        }
        return out
    }

    /** A frame whose `up` runs along a limb from [a] to [b], `fwd` as close to the chest's front as fits. */
    fun along(a: Vec3, b: Vec3): Basis {
        val up = (b - a).normalized()
        val fwd = (Vec3.Z - up * (Vec3.Z dot up)).normalized()
        return Basis(up cross fwd, up, fwd)
    }

    /** A foot's frame: `fwd` from heel to toe, `up` as near vertical as fits. */
    private fun alongFlat(heel: Vec3, toe: Vec3): Basis {
        val fwd = (toe - heel).normalized()
        val up = (Vec3.Y - fwd * (Vec3.Y dot fwd)).normalized()
        return Basis(up cross fwd, up, fwd)
    }

    /** Smooth minimum: two shapes joined with a fillet [k] wide instead of a crease. */
    fun smin(a: Double, b: Double, k: Double): Double {
        val h = max(k - abs(a - b), 0.0) / k
        return min(a, b) - h * h * k * 0.25
    }
}
