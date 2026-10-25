package dev.avles.gym.anim

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** A surface colour. Every piece of the scene picks from [Palette], so it all matches. */
data class Mat(val argb: Int)

/**
 * The house style, in one place. One body colour, one metal, one pad, so the figure
 * and every machine read as a single set — the request was that nothing look
 * different from anything else.
 */
object Palette {
    val BODY = Mat(0xFFD8D2C8.toInt())
    val BODY_FOCUS = Mat(0xFFE2B65A.toInt())
    val FRAME = Mat(0xFF59616E.toInt())
    val STEEL = Mat(0xFFB9C0CA.toInt())
    val PLATE = Mat(0xFF2E333B.toInt())
    val PLATE_RIM = Mat(0xFFC9A24A.toInt())
    val PAD = Mat(0xFF3C424C.toInt())
    val CABLE = Mat(0xFF9AA3AE.toInt())
    val BAND = Mat(0xFFB65A4E.toInt())
    const val FLOOR = 0xFF1B2027.toInt()
    const val FLOOR_LINE = 0xFF262C35.toInt()
    const val SHADOW = 0x55000000
}

/** What the scene is made of. Bodies are capsules and balls; equipment adds boxes and cylinders. */
sealed class Prim {
    data class Capsule(val a: Vec3, val b: Vec3, val ra: Double, val rb: Double, val mat: Mat) : Prim()
    data class Ball(val c: Vec3, val r: Double, val mat: Mat) : Prim()
    data class Box(val c: Vec3, val basis: Basis, val half: Vec3, val mat: Mat) : Prim()
    data class Cylinder(val a: Vec3, val b: Vec3, val r: Double, val mat: Mat) : Prim()
    data class Line(val a: Vec3, val b: Vec3, val width: Double, val mat: Mat) : Prim()
    /** A whole person: the [avatar]'s skin bent to [skeleton], with [focus] lit. */
    data class Body(val skeleton: Skeleton, val avatar: Avatar, val focus: Focus) : Prim()
}

/** Something to paint, already in screen pixels, with its distance for back-to-front order. */
sealed class Draw {
    abstract val depth: Double

    data class Poly(val xs: FloatArray, val ys: FloatArray, val argb: Int, override val depth: Double) : Draw()

    /** A limb: a tapered rod with round ends, shaded from [lit] on one edge to [dark] on the other. */
    data class Rod(
        val x1: Float, val y1: Float, val x2: Float, val y2: Float,
        val r1: Float, val r2: Float,
        val lit: Int, val base: Int, val dark: Int,
        /** Unit vector across the rod pointing at its lit edge. */
        val nx: Float, val ny: Float,
        override val depth: Double
    ) : Draw()

    data class Disc(
        val x: Float, val y: Float, val r: Float,
        val lit: Int, val base: Int, val dark: Int,
        val hx: Float, val hy: Float,
        override val depth: Double
    ) : Draw()

    /** One triangle of skin, coloured at each corner and blended between them. */
    data class Tri(
        val x1: Float, val y1: Float, val x2: Float, val y2: Float, val x3: Float, val y3: Float,
        val c1: Int, val c2: Int, val c3: Int,
        override val depth: Double
    ) : Draw()

    data class Stroke(
        val x1: Float, val y1: Float, val x2: Float, val y2: Float,
        val width: Float, val argb: Int, override val depth: Double
    ) : Draw()
}

/**
 * An orbit camera: it circles [target] at [distance], [yaw] around the vertical and
 * [pitch] above the horizon. Dragging a finger changes yaw and pitch, which is the
 * whole 360-degree view.
 */
data class Camera(
    val yaw: Double = 35 * DEG,
    val pitch: Double = 12 * DEG,
    val distance: Double = 3.6,
    val target: Vec3 = Vec3(0.0, 0.85, 0.0),
    val fov: Double = 0.95
) {
    val eye: Vec3 get() = target + Vec3(sin(yaw) * cos(pitch), sin(pitch), cos(yaw) * cos(pitch)) * distance
}

/** Turns a list of primitives into flat paint commands, back to front. Platform-free. */
class Renderer(val width: Float, val height: Float, val camera: Camera) {

    private val eye = camera.eye
    private val fwd = (camera.target - eye).normalized()
    private val right = (fwd cross Vec3.Y).normalized()
    private val up = right cross fwd
    private val focal = min(width, height) * camera.fov
    private val cx = width / 2.0
    private val cy = height / 2.0

    /** A key light above and to the left of the viewer, so every view is readable. */
    private val light = (up * 0.9 - right * 0.55 - fwd * 0.6).normalized()
    private val lightScreen = Pair((light dot right).toFloat(), (-(light dot up)).toFloat()).let { (x, y) ->
        val l = sqrt(x * x + y * y).coerceAtLeast(1e-4f); Pair(x / l, y / l)
    }

    private class P(val x: Double, val y: Double, val z: Double)

    private fun project(p: Vec3): P {
        val d = p - eye
        val z = max(d dot fwd, 0.05)
        return P(cx + focal * (d dot right) / z, cy - focal * (d dot up) / z, z)
    }

    fun render(prims: List<Prim>, out: MutableList<Draw> = mutableListOf()): List<Draw> {
        prims.forEach { prim ->
            when (prim) {
                is Prim.Capsule -> capsule(prim, out)
                is Prim.Ball -> ball(prim, out)
                is Prim.Box -> box(prim, out)
                is Prim.Cylinder -> cylinder(prim, out)
                is Prim.Line -> line(prim, out)
                is Prim.Body -> body(prim, out)
            }
        }
        out.sortByDescending { it.depth }
        return out
    }

    /** The floor and the figure's shadow: always underneath, so drawn first and unsorted. */
    fun floor(centre: Vec3, radius: Double, shadowAt: List<Vec3>): List<Draw> {
        val out = mutableListOf<Draw>()
        out += ring(centre, radius, Palette.FLOOR, 48)
        for (i in 1..3) out += ringOutline(centre, radius * i / 3.6, Palette.FLOOR_LINE)
        shadowAt.forEach { p -> out += ring(Vec3(p.x, 0.001, p.z), 0.16, Palette.SHADOW, 20) }
        return out
    }

    private fun ring(c: Vec3, r: Double, argb: Int, n: Int): Draw.Poly {
        val xs = FloatArray(n); val ys = FloatArray(n)
        for (i in 0 until n) {
            val a = 2 * PI * i / n
            val p = project(Vec3(c.x + cos(a) * r, c.y, c.z + sin(a) * r))
            xs[i] = p.x.toFloat(); ys[i] = p.y.toFloat()
        }
        return Draw.Poly(xs, ys, argb, 1e9)
    }

    private fun ringOutline(c: Vec3, r: Double, argb: Int): List<Draw> {
        val n = 48
        val pts = (0..n).map { i ->
            val a = 2 * PI * i / n
            project(Vec3(c.x + cos(a) * r, c.y, c.z + sin(a) * r))
        }
        return pts.zipWithNext { a, b ->
            Draw.Stroke(a.x.toFloat(), a.y.toFloat(), b.x.toFloat(), b.y.toFloat(), 1.2f, argb, 1e9)
        }
    }

    private fun tones(mat: Mat): Triple<Int, Int, Int> =
        Triple(shade(mat.argb, 1.18), shade(mat.argb, 0.92), shade(mat.argb, 0.52))

    private fun capsule(c: Prim.Capsule, out: MutableList<Draw>) {
        val a = project(c.a); val b = project(c.b)
        val ra = focal * c.ra / a.z; val rb = focal * c.rb / b.z
        var nx = -(b.y - a.y); var ny = (b.x - a.x)
        val l = sqrt(nx * nx + ny * ny)
        if (l < 1e-6) { nx = lightScreen.first.toDouble(); ny = lightScreen.second.toDouble() } else { nx /= l; ny /= l }
        if (nx * lightScreen.first + ny * lightScreen.second < 0) { nx = -nx; ny = -ny }
        val (lit, base, dark) = tones(c.mat)
        out += Draw.Rod(
            a.x.toFloat(), a.y.toFloat(), b.x.toFloat(), b.y.toFloat(), ra.toFloat(), rb.toFloat(),
            lit, base, dark, nx.toFloat(), ny.toFloat(), (a.z + b.z) / 2 - bias(c.mat)
        )
    }

    private fun ball(c: Prim.Ball, out: MutableList<Draw>) {
        val p = project(c.c)
        val (lit, base, dark) = tones(c.mat)
        out += Draw.Disc(
            p.x.toFloat(), p.y.toFloat(), (focal * c.r / p.z).toFloat(), lit, base, dark,
            lightScreen.first, lightScreen.second, p.z - c.r * 0.5 - bias(c.mat)
        )
    }

    private fun line(c: Prim.Line, out: MutableList<Draw>) {
        val a = project(c.a); val b = project(c.b)
        out += Draw.Stroke(
            a.x.toFloat(), a.y.toFloat(), b.x.toFloat(), b.y.toFloat(),
            max(1.0, focal * c.width / ((a.z + b.z) / 2)).toFloat(), c.mat.argb, (a.z + b.z) / 2
        )
    }

    /** Flat faces lit by how directly they face the light. Back faces are skipped. */
    private fun face(corners: List<Vec3>, normal: Vec3, mat: Mat, out: MutableList<Draw>, bias: Double = 0.0) {
        val centre = corners.fold(Vec3.ZERO) { s, p -> s + p } * (1.0 / corners.size)
        if (normal dot (eye - centre) <= 0) return
        val k = 0.42 + 0.7 * max(0.0, normal dot light)
        val pts = corners.map(::project)
        out += Draw.Poly(
            pts.map { it.x.toFloat() }.toFloatArray(), pts.map { it.y.toFloat() }.toFloatArray(),
            shade(mat.argb, k), (centre - eye).length + bias
        )
    }

    private fun box(bx: Prim.Box, out: MutableList<Draw>) {
        // A long pad is cut into short pieces so each sorts against the body lying on it.
        val longest = maxOf(bx.half.x, bx.half.y, bx.half.z)
        if (longest > 0.16) {
            val n = kotlin.math.ceil(longest / 0.12).toInt()
            val (axis, h) = when (longest) {
                bx.half.z -> bx.basis.fwd to bx.half.copy(z = bx.half.z / n)
                bx.half.y -> bx.basis.up to bx.half.copy(y = bx.half.y / n)
                else -> bx.basis.side to bx.half.copy(x = bx.half.x / n)
            }
            for (i in 0 until n) {
                val off = -longest + longest / n * (2 * i + 1)
                boxPiece(bx.copy(c = bx.c + axis * off, half = h), out)
            }
        } else boxPiece(bx, out)
    }

    private fun boxPiece(bx: Prim.Box, out: MutableList<Draw>) {
        val (s, u, f) = Triple(bx.basis.side * bx.half.x, bx.basis.up * bx.half.y, bx.basis.fwd * bx.half.z)
        val c = bx.c
        fun q(a: Vec3, b: Vec3, n: Vec3) {
            val ctr = c + n
            face(listOf(ctr + a + b, ctr - a + b, ctr - a - b, ctr + a - b), n.normalized(), bx.mat, out)
        }
        q(s, f, u); q(f, s, -u)
        q(u, f, s); q(f, u, -s)
        q(s, u, f); q(u, s, -f)
    }

    private fun cylinder(cy: Prim.Cylinder, out: MutableList<Draw>) {
        val axis = (cy.b - cy.a).normalized()
        val ref = if (abs(axis dot Vec3.Y) < 0.9) Vec3.Y else Vec3.X
        val e1 = (axis cross ref).normalized()
        val e2 = axis cross e1
        val n = 16
        val ringA = (0 until n).map { i -> val t = 2 * PI * i / n; cy.a + (e1 * cos(t) + e2 * sin(t)) * cy.r }
        val ringB = ringA.map { it + (cy.b - cy.a) }
        for (i in 0 until n) {
            val j = (i + 1) % n
            val mid = (2 * PI * (i + 0.5)) / n
            face(listOf(ringA[i], ringA[j], ringB[j], ringB[i]), e1 * cos(mid) + e2 * sin(mid), cy.mat, out)
        }
        face(ringB, axis, cy.mat, out, -0.001)
        face(ringA.reversed(), -axis, cy.mat, out, -0.001)
    }

    /**
     * A person, skinned to the pose. Drawing never builds a mesh (that takes a moment):
     * until [BodyMesh.of] has been called for the avatar, off the main thread, the
     * body is simply left out of the frame.
     */
    private fun body(b: Prim.Body, out: MutableList<Draw>) {
        val mesh = BodyMesh.ready(b.avatar) ?: return
        val n = mesh.size
        val pos = DoubleArray(n * 3); val nrm = DoubleArray(n * 3)
        mesh.skin(b.skeleton, pos, nrm)
        val mask = mesh.focusMask(b.focus)
        val sx = FloatArray(n); val sy = FloatArray(n); val col = IntArray(n)
        val accent = Palette.BODY_FOCUS.argb
        for (i in 0 until n) {
            val p = project(Vec3(pos[i * 3], pos[i * 3 + 1], pos[i * 3 + 2]))
            sx[i] = p.x.toFloat(); sy[i] = p.y.toFloat()
            val nl = nrm[i * 3] * light.x + nrm[i * 3 + 1] * light.y + nrm[i * 3 + 2] * light.z
            // Light that wraps a little round the form, as it does on skin and cloth.
            val k = 0.3 + 0.8 * ((nl + 0.3) / 1.3).coerceIn(0.0, 1.0)
            val base = if (mask != null && mask[i] > 0f) mix(mesh.color[i], accent, 0.7) else mesh.color[i]
            col[i] = shade(base, k)
        }
        val t = mesh.tris
        for (k in t.indices step 3) {
            val a = t[k]; val c = t[k + 1]; val d = t[k + 2]
            // Facing away from the camera on screen: the back of the body, skipped.
            val cross = (sx[c] - sx[a]) * (sy[d] - sy[a]) - (sy[c] - sy[a]) * (sx[d] - sx[a])
            if (cross >= 0f) continue
            val mx = (pos[a * 3] + pos[c * 3] + pos[d * 3]) / 3 - eye.x
            val my = (pos[a * 3 + 1] + pos[c * 3 + 1] + pos[d * 3 + 1]) / 3 - eye.y
            val mz = (pos[a * 3 + 2] + pos[c * 3 + 2] + pos[d * 3 + 2]) / 3 - eye.z
            out += Draw.Tri(
                sx[a], sy[a], sx[c], sy[c], sx[d], sy[d], col[a], col[c], col[d],
                sqrt(mx * mx + my * my + mz * mz) - BODY_BIAS
            )
        }
    }

    /** The body rests on the equipment, so where the two touch the body wins. */
    private fun bias(mat: Mat) = if (mat == Palette.BODY || mat == Palette.BODY_FOCUS) BODY_BIAS else 0.0

    companion object {
        const val BODY_BIAS = 0.06

        fun mix(a: Int, b: Int, t: Double): Int {
            fun ch(shift: Int) = (((a ushr shift) and 0xFF) * (1 - t) + ((b ushr shift) and 0xFF) * t).roundToInt().coerceIn(0, 255)
            return (0xFF shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
        }

        fun shade(argb: Int, k: Double): Int {
            val a = (argb ushr 24) and 0xFF
            fun ch(shift: Int) = (((argb ushr shift) and 0xFF) * k).roundToInt().coerceIn(0, 255)
            return (a shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
        }
    }
}
