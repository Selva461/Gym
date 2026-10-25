package dev.avles.gym.anim

import kotlin.math.cos
import kotlin.math.sin

/**
 * The equipment, built from the same few shapes and the same [Palette] as the figure,
 * so a bench, a cable stack and a barbell all look like one set.
 *
 * Sizes are real gym sizes: a 2.2 m Olympic bar, 45 cm plates, a bench pad 45 cm off
 * the floor. That is what makes the figure's hands land where yours would.
 */
object Gear {

    const val BENCH_TOP = 0.45
    const val PLATE_R = 0.225

    /** The 10 kg plates usual on arm work: curls, extensions, wrist curls. */
    const val SMALL_PLATE_R = 0.13

    private fun box(c: Vec3, half: Vec3, mat: Mat, basis: Basis = Basis.IDENTITY) = Prim.Box(c, basis, half, mat)

    // ---- benches and seats ------------------------------------------------

    /**
     * A bench along Z. [backAngle] is the back pad's angle from flat in degrees:
     * 0 flat, 30–45 incline, negative decline. The seat sits at [seatZ].
     */
    fun bench(backAngle: Double = 0.0, seatZ: Double = 0.0, back: Boolean = true): List<Prim> {
        val out = mutableListOf<Prim>()
        val padTh = 0.05
        // Feet of the frame
        out += box(v(0.0, 0.03, seatZ + 0.45), v(0.24, 0.03, 0.03), Palette.FRAME)
        out += box(v(0.0, 0.03, seatZ - 0.62), v(0.24, 0.03, 0.03), Palette.FRAME)
        out += box(v(0.0, 0.05, seatZ - 0.08), v(0.03, 0.02, 0.55), Palette.FRAME)
        out += box(v(0.0, 0.22, seatZ + 0.42), v(0.03, 0.2, 0.03), Palette.FRAME)
        out += box(v(0.0, 0.22, seatZ - 0.55), v(0.03, 0.2, 0.03), Palette.FRAME)
        // Seat
        out += box(v(0.0, BENCH_TOP - padTh / 2, seatZ + 0.22), v(0.15, padTh / 2, 0.22), Palette.PAD)
        if (!back) return out
        // Back pad hinged at the back of the seat, rising towards -Z.
        val a = backAngle * DEG
        val len = 0.8
        val hinge = v(0.0, BENCH_TOP - padTh / 2, seatZ)
        val dir = v(0.0, sin(a), -cos(a))
        val basis = Basis(Vec3.X, (dir cross Vec3.X).let { if (it.y < 0) -it else it }, dir)
        out += Prim.Box(hinge + dir * (len / 2), basis, v(0.15, padTh / 2, len / 2), Palette.PAD)
        if (backAngle > 5) {
            // The strut that holds an incline up.
            out += Prim.Capsule(v(0.0, 0.08, seatZ - 0.5), hinge + dir * (len * 0.6) - v(0.0, 0.04, 0.0), 0.02, 0.02, Palette.FRAME)
        }
        return out
    }

    /** Uprights with J-hooks behind a bench, or a squat rack around the lifter. */
    fun rack(z: Double, hookY: Double, half: Double = 0.6, height: Double = hookY + 0.12): List<Prim> = listOf(-half, half).flatMap { x ->
        listOf(
            box(v(x, height / 2, z), v(0.035, height / 2, 0.035), Palette.FRAME),
            box(v(x, hookY - 0.03, z + 0.06), v(0.03, 0.025, 0.06), Palette.STEEL)
        )
    } + box(v(0.0, 0.02, z), v(half + 0.05, 0.02, 0.06), Palette.FRAME)

    /** A pull-up bar at [height], posts either side. */
    fun pullUpBar(height: Double, z: Double = 0.0): List<Prim> = listOf(
        box(v(0.62, height / 2, z), v(0.035, height / 2, 0.035), Palette.FRAME),
        box(v(-0.62, height / 2, z), v(0.035, height / 2, 0.035), Palette.FRAME),
        Prim.Cylinder(v(-0.66, height, z), v(0.66, height, z), 0.016, Palette.STEEL)
    )

    /** A cable tower at [at] with its pulley at [pulleyY]. Returns the pulley position too. */
    fun cableTower(at: Vec3, pulleyY: Double): Pair<List<Prim>, Vec3> {
        val pulley = v(at.x, pulleyY, at.z)
        val out = listOf(
            box(v(at.x, 1.05, at.z - 0.12), v(0.22, 1.05, 0.05), Palette.FRAME),
            box(v(at.x, 0.35, at.z - 0.06), v(0.12, 0.3, 0.05), Palette.PLATE),
            box(v(at.x, 0.03, at.z), v(0.32, 0.03, 0.25), Palette.FRAME),
            Prim.Ball(pulley, 0.045, Palette.STEEL),
            Prim.Capsule(pulley, v(at.x, 0.7, at.z - 0.06), 0.008, 0.008, Palette.CABLE)
        )
        return out to pulley
    }

    /** A machine seat with a back pad, the shape every plate-loaded and selector machine shares. */
    fun machineSeat(seatY: Double = 0.48, backAngle: Double = 80.0, z: Double = 0.0): List<Prim> {
        val a = backAngle * DEG
        val dir = v(0.0, sin(a), -cos(a))
        val basis = Basis(Vec3.X, (dir cross Vec3.X).let { if (it.y < 0) -it else it }, dir)
        return listOf(
            box(v(0.0, seatY - 0.03, z + 0.18), v(0.2, 0.035, 0.2), Palette.PAD),
            Prim.Box(v(0.0, seatY, z) + dir * 0.35, basis, v(0.2, 0.035, 0.32), Palette.PAD),
            box(v(0.0, (seatY - 0.06) / 2, z + 0.1), v(0.04, (seatY - 0.06) / 2, 0.04), Palette.FRAME),
            box(v(0.0, 0.03, z + 0.1), v(0.35, 0.03, 0.35), Palette.FRAME),
            box(v(0.0, 1.0, z - 0.45), v(0.3, 1.0, 0.05), Palette.FRAME)
        )
    }

    /** A plyo box or step. */
    fun block(c: Vec3, half: Vec3): Prim = box(c, half, Palette.PAD)

    // ---- things that move with the hands ---------------------------------

    /** An Olympic bar centred between the hands, along the line through them. */
    fun barbell(l: Vec3, r: Vec3, length: Double = 2.2, plates: Int = 2, plateR: Double = PLATE_R): List<Prim> {
        val mid = (l + r) * 0.5
        val axis = (l - r).normalized().let { if (it.length < 0.5) Vec3.X else it }
        val half = length / 2
        val out = mutableListOf<Prim>()
        out += Prim.Cylinder(mid - axis * half, mid + axis * half, 0.014, Palette.STEEL)
        for (s in listOf(-1.0, 1.0)) {
            val start = mid + axis * (s * (half - 0.42))
            out += Prim.Cylinder(start, start + axis * (s * 0.02), 0.035, Palette.STEEL) // collar
            for (i in 0 until plates) {
                val a = start + axis * (s * (0.03 + i * 0.055))
                out += Prim.Cylinder(a, a + axis * (s * 0.045), plateR - i * 0.04, if (i == 0) Palette.PLATE_RIM else Palette.PLATE)
            }
        }
        return out
    }

    /** A dumbbell held in [hand], its handle along [axis]. */
    fun dumbbell(hand: Vec3, axis: Vec3, headR: Double = 0.075): List<Prim> {
        val a = axis.normalized()
        return listOf(
            Prim.Cylinder(hand - a * 0.07, hand + a * 0.07, 0.016, Palette.STEEL),
            Prim.Cylinder(hand + a * 0.07, hand + a * 0.16, headR, Palette.PLATE),
            Prim.Cylinder(hand - a * 0.07, hand - a * 0.16, headR, Palette.PLATE)
        )
    }

    /** A kettlebell hanging from [hand]. */
    fun kettlebell(hand: Vec3, down: Vec3 = -Vec3.Y): List<Prim> {
        val d = down.normalized()
        return listOf(
            Prim.Capsule(hand - Vec3.X * 0.05, hand + Vec3.X * 0.05, 0.014, 0.014, Palette.STEEL),
            Prim.Ball(hand + d * 0.14, 0.11, Palette.PLATE)
        )
    }

    /** A single plate held flat in both hands. */
    fun plate(at: Vec3, facing: Vec3): List<Prim> =
        listOf(Prim.Cylinder(at - facing.normalized() * 0.02, at + facing.normalized() * 0.02, 0.21, Palette.PLATE_RIM))

    /** A cable from a pulley to a handle. */
    fun cable(pulley: Vec3, handle: Vec3): List<Prim> = listOf(
        Prim.Line(pulley, handle, 0.006, Palette.CABLE),
        Prim.Capsule(handle - Vec3.X * 0.06, handle + Vec3.X * 0.06, 0.016, 0.016, Palette.STEEL)
    )

    /** A resistance band from an anchor point to the hands. */
    fun band(anchor: Vec3, hand: Vec3): Prim = Prim.Line(anchor, hand, 0.012, Palette.BAND)

    /** The hexagonal trap bar around the lifter. */
    fun trapBar(l: Vec3, r: Vec3): List<Prim> {
        val mid = (l + r) * 0.5
        val out = mutableListOf<Prim>()
        val pts = (0 until 6).map { i ->
            val a = i * 60 * DEG + 30 * DEG
            v(mid.x + cos(a) * 0.36, mid.y, mid.z + sin(a) * 0.32)
        }
        for (i in 0 until 6) out += Prim.Capsule(pts[i], pts[(i + 1) % 6], 0.016, 0.016, Palette.STEEL)
        for (s in listOf(-1.0, 1.0)) {
            val a = v(mid.x + s * 0.36, mid.y, mid.z)
            out += Prim.Cylinder(a, a + Vec3.X * (s * 0.3), 0.014, Palette.STEEL)
            out += Prim.Cylinder(a + Vec3.X * (s * 0.12), a + Vec3.X * (s * 0.17), PLATE_R, Palette.PLATE_RIM)
        }
        return out
    }
}
