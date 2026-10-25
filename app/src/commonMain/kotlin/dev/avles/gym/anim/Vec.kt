package dev.avles.gym.anim

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A point or direction in the demo's world, in metres.
 *
 * Y is up, the floor is y = 0, and the figure faces +Z with its left side towards +X.
 * Everything in the 3D demonstration is built from these, so the maths is kept here
 * in one small, dependency-free place that runs the same on Android and iOS.
 */
data class Vec3(val x: Double, val y: Double, val z: Double) {
    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
    operator fun times(s: Double) = Vec3(x * s, y * s, z * s)
    operator fun unaryMinus() = Vec3(-x, -y, -z)
    infix fun dot(o: Vec3) = x * o.x + y * o.y + z * o.z
    infix fun cross(o: Vec3) = Vec3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x)
    val length: Double get() = sqrt(this dot this)

    fun normalized(): Vec3 {
        val l = length
        return if (l < 1e-9) ZERO else this * (1.0 / l)
    }

    fun withX(v: Double) = copy(x = v)
    fun withY(v: Double) = copy(y = v)
    fun withZ(v: Double) = copy(z = v)

    companion object {
        val ZERO = Vec3(0.0, 0.0, 0.0)
        val X = Vec3(1.0, 0.0, 0.0)
        val Y = Vec3(0.0, 1.0, 0.0)
        val Z = Vec3(0.0, 0.0, 1.0)
    }
}

fun v(x: Double, y: Double, z: Double) = Vec3(x, y, z)

fun lerp(a: Double, b: Double, t: Double) = a + (b - a) * t
fun lerp(a: Vec3, b: Vec3, t: Double) = a + (b - a) * t

/** Ease in and out, so every movement starts and stops the way a controlled rep does. */
fun smooth(t: Double): Double {
    val c = t.coerceIn(0.0, 1.0)
    return c * c * (3 - 2 * c)
}

/** An orthonormal frame: three unit axes. Used for the torso and for equipment. */
data class Basis(val side: Vec3, val up: Vec3, val fwd: Vec3) {
    /** A local offset (x along side, y along up, z along fwd) in world space. */
    fun apply(local: Vec3): Vec3 = side * local.x + up * local.y + fwd * local.z

    companion object {
        val IDENTITY = Basis(Vec3.X, Vec3.Y, Vec3.Z)

        /**
         * Yaw about the vertical, then pitch (positive leans forward, -90° lies on your
         * back), then roll (side bend). The order a body actually moves in.
         */
        fun of(yaw: Double, pitch: Double, roll: Double = 0.0): Basis {
            fun rot(p: Vec3): Vec3 {
                // roll about Z
                val r = Vec3(p.x * cos(roll) - p.y * sin(roll), p.x * sin(roll) + p.y * cos(roll), p.z)
                // pitch about X: +pitch tips up (Y) towards forward (Z)
                val q = Vec3(r.x, r.y * cos(pitch) - r.z * sin(pitch), r.y * sin(pitch) + r.z * cos(pitch))
                // yaw about Y
                return Vec3(q.x * cos(yaw) + q.z * sin(yaw), q.y, -q.x * sin(yaw) + q.z * cos(yaw))
            }
            return Basis(rot(Vec3.X), rot(Vec3.Y), rot(Vec3.Z))
        }
    }
}

const val DEG = kotlin.math.PI / 180.0
