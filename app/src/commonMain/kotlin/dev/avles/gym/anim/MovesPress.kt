package dev.avles.gym.anim

import dev.avles.gym.anim.Kit.bothHands
import dev.avles.gym.anim.Kit.pair

/** Presses: bench, incline, decline, floor, overhead, push-ups and dips. */
object MovesPress {

    /** Lying on a bench (flat, incline or decline) pressing a bar, dumbbells or a Smith bar. */
    fun bench(spec: Spec): Motion {
        val angle = spec.angle
        val floor = spec.has("floor press")
        val db = spec.gear == "DUMBBELL"
        val half = if (db) 0.24 else spec.gripHalf
        val a = angle * DEG
        val torsoPitch = -90 * DEG + a
        val padY = if (floor) 0.0 else Gear.BENCH_TOP
        val pelvis = v(0.0, padY + 0.12, if (angle > 5) 0.08 else 0.12)
        val sh = Kit.shoulder(pelvis, torsoPitch, 1.0)
        val chestTop = Kit.onTorso(pelvis, torsoPitch, v(0.0, Body.SPINE * 0.72, 0.19))
        val armUp = Basis.of(0.0, torsoPitch).fwd // straight up from the chest's point of view
        val press = if (angle != 0.0) Vec3.Y else armUp
        // Straight up from the shoulders, as far as the arms reach for this grip width.
        val reach = kotlin.math.sqrt(0.6 * 0.6 - (half - Body.SHOULDER_HALF).let { it * it }.coerceAtMost(0.2))
        val topY = sh.y + reach * press.y
        val topZ = sh.z + reach * press.z
        val bottom = chestTop + Vec3.Y * 0.04 + (if (floor) Vec3.Y * 0.08 else Vec3.ZERO)

        val feet = if (floor) Pair(v(0.3, Kit.ANKLE + 0.02, 0.55), v(-0.3, Kit.ANKLE + 0.02, 0.55))
        else if (angle < 0) Pair(v(0.16, 0.55, 0.62), v(-0.16, 0.55, 0.62))
        else Pair(v(0.32, Kit.ANKLE, 0.62), v(-0.32, Kit.ANKLE, 0.62))
        val kneePole = v(0.25, 0.0, 1.0)
        val elbows = v(0.9, -0.5, -0.9)
        fun lying(hands: Pair<Vec3, Vec3>) = Pose(
            pelvis = pelvis, pitch = torsoPitch, head = if (floor) 0.0 else 0.1,
            handL = hands.first, handR = hands.second,
            footL = feet.first, footR = feet.second,
            elbowPole = elbows, kneePole = kneePole, footYaw = 0.25
        )
        val rackZ = sh.z - 0.12
        val poses = mapOf(
            "setup" to lying(pair(half, if (db) bottom.y + 0.05 else topY - 0.05, if (db) bottom.z else rackZ)),
            "grip" to lying(pair(half, if (db) bottom.y + 0.05 else topY - 0.05, if (db) bottom.z else rackZ)),
            "top" to lying(pair(if (db) 0.17 else half, topY, topZ)),
            "bottom" to lying(pair(half, bottom.y, bottom.z))
        )
        val fixed = mutableListOf<Prim>()
        if (!floor) fixed += Gear.bench(backAngle = if (angle < 0) angle else angle, seatZ = 0.15)
        if (angle < 0) fixed += Gear.block(v(0.0, 0.5, 0.62), v(0.2, 0.03, 0.06))
        if (!db && !floor) fixed += Gear.rack(rackZ - 0.08, topY - 0.02, 0.55)
        val moving = Rigging { s, _ ->
            if (db) Gear.dumbbell(s.handL, Vec3.X) + Gear.dumbbell(s.handR, Vec3.X)
            else Gear.barbell(s.handL, s.handR)
        }
        return Motion(
            poses, fixed, moving,
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.ELBOWS,
                setupNote = "Eyes under the bar, shoulder blades squeezed together and down, feet flat and planted.",
                gripNote = Kit.gripNote(spec),
                startFocus = Focus.SHOULDERS,
                cameraYaw = 70 * DEG
            ),
            Camera(yaw = 60 * DEG, pitch = 18 * DEG, distance = 3.2, target = v(0.0, 0.7, 0.0))
        )
    }

    /** Push-ups: flat, hands raised, feet raised, close, wide, on rings or a clap. */
    fun pushUp(spec: Spec): Motion {
        val incline = spec.has("incline")
        val feetUp = spec.has("decline", "feet elevated")
        val handY = when {
            incline -> Gear.BENCH_TOP + 0.03
            spec.has("deficit") -> 0.14
            else -> 0.04
        }
        val ankleY = if (feetUp) 0.62 else 0.16
        val half = when {
            spec.has("diamond") -> 0.07
            spec.has("close") -> 0.16
            spec.has("wide", "archer") -> 0.42
            else -> 0.27
        }
        val handZ = 0.45
        fun body(shoulderY: Double, shoulderDz: Double, ankleZ: Double): Pose {
            val sh = v(0.0, shoulderY, handZ + shoulderDz)
            val (pelvis, pitch) = Kit.plank(v(0.0, ankleY, ankleZ), sh)
            return Pose(
                pelvis = pelvis, pitch = pitch, head = -0.2,
                handL = v(half, handY, handZ), handR = v(-half, handY, handZ),
                footL = v(0.09, ankleY, ankleZ), footR = v(-0.09, ankleY, ankleZ),
                elbowPole = v(if (half < 0.2) 0.15 else 0.75, -0.5, -1.0), kneePole = v(0.0, 0.0, -1.0),
                footPitch = 75 * DEG
            )
        }
        val topY = handY + 0.6
        val ankleZ = handZ - kotlin.math.sqrt(Kit.LINE * Kit.LINE - (topY - ankleY) * (topY - ankleY))
        val top = body(topY, -0.02, ankleZ)
        val botY = handY + 0.2
        val bottom = body(botY, kotlin.math.sqrt(Kit.LINE * Kit.LINE - (botY - ankleY) * (botY - ankleY)) - (handZ - ankleZ) - 0.0, ankleZ)
        val poses = mapOf("setup" to top, "grip" to top, "top" to top, "bottom" to bottom)
        val fixed = mutableListOf<Prim>()
        if (incline) fixed += Gear.bench(0.0, handZ + 0.15)
        if (feetUp) fixed += Gear.block(v(0.0, (ankleY - 0.12) / 2, ankleZ - 0.05), v(0.22, (ankleY - 0.12) / 2, 0.2))
        val rings = spec.has("ring")
        return Motion(
            poses, fixed,
            Rigging { s, _ ->
                if (rings) listOf(Gear.band(s.handL + v(0.0, 1.5, 0.0), s.handL), Gear.band(s.handR + v(0.0, 1.5, 0.0), s.handR))
                else emptyList()
            },
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.ELBOWS,
                setupNote = "Feet together or hip width, body in one straight line from heels to head.",
                gripNote = when {
                    half < 0.12 -> "Hands together under the chest, thumbs and index fingers making a diamond."
                    half < 0.2 -> "Hands under the shoulders, elbows kept tight to the ribs."
                    half > 0.4 -> "Hands well outside the shoulders, fingers forward."
                    else -> "Hands just outside the shoulders, fingers spread and pointing forward."
                },
                startFocus = Focus.CORE, cameraYaw = 95 * DEG
            ),
            Camera(yaw = 75 * DEG, pitch = 14 * DEG, distance = 3.3, target = v(0.0, 0.45, -0.1))
        )
    }

    /** Dips: between parallel bars, off a bench, or on a machine. */
    fun dip(spec: Spec): Motion = when {
        spec.has("bench dip") -> benchDip()
        spec.gear == "MACHINE" -> machineDip()
        else -> barDip(spec.has("chest"))
    }

    private fun barDip(chest: Boolean): Motion {
        val barY = 1.2
        val lean = if (chest) 22 * DEG else 6 * DEG
        fun at(drop: Double, extraLean: Double): Pose {
            val sh = v(0.0, barY + 0.6 - drop, 0.02 - drop * (if (chest) 0.35 else 0.15))
            val pitch = lean + extraLean
            val pelvis = Kit.pelvisFor(sh, pitch)
            return Pose(
                pelvis = pelvis, pitch = pitch, head = -0.1,
                handL = v(0.27, barY, 0.05), handR = v(-0.27, barY, 0.05),
                footL = pelvis + v(0.08, -0.62, -0.35), footR = pelvis + v(-0.08, -0.62, -0.35),
                elbowPole = v(if (chest) 0.45 else 0.1, 0.0, -1.0), kneePole = v(0.0, -0.4, 1.0)
            )
        }
        val top = at(0.0, 0.0)
        val bottom = at(0.3, 0.08)
        val fixed = listOf(0.27, -0.27).flatMap { x ->
            listOf(
                Prim.Cylinder(v(x, barY, -0.35), v(x, barY, 0.5), 0.022, Palette.STEEL),
                Prim.Box(v(x * 1.15, barY / 2, 0.45), Basis.IDENTITY, v(0.03, barY / 2, 0.03), Palette.FRAME),
                Prim.Box(v(x * 1.15, barY / 2, -0.3), Basis.IDENTITY, v(0.03, barY / 2, 0.03), Palette.FRAME)
            )
        }
        return Motion(
            mapOf("setup" to top, "grip" to top, "top" to top, "bottom" to bottom), fixed,
            Rigging { _, _ -> emptyList() },
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.ELBOWS,
                setupNote = if (chest) "Lean the chest forward, knees bent, ankles crossed behind."
                else "Body upright, knees bent behind you, shoulders pressed down away from the ears.",
                gripNote = "Neutral grip on the bars, palms facing each other, wrists stacked over the elbows.",
                cameraYaw = 80 * DEG
            ),
            Camera(yaw = 60 * DEG, pitch = 10 * DEG, distance = 3.5, target = v(0.0, 1.0, 0.0))
        )
    }

    private fun benchDip(): Motion {
        val edgeZ = -0.05
        fun at(drop: Double): Pose {
            val sh = v(0.0, Gear.BENCH_TOP + 0.62 - drop, edgeZ + 0.06)
            val pitch = -4 * DEG
            val pelvis = Kit.pelvisFor(sh, pitch)
            return Pose(
                pelvis = pelvis, pitch = pitch,
                handL = v(0.17, Gear.BENCH_TOP + 0.03, edgeZ), handR = v(-0.17, Gear.BENCH_TOP + 0.03, edgeZ),
                footL = v(0.13, Kit.ANKLE, 0.7), footR = v(-0.13, Kit.ANKLE, 0.7),
                elbowPole = v(0.0, 0.0, -1.0), kneePole = v(0.1, 0.6, 1.0)
            )
        }
        val top = at(0.0)
        return Motion(
            mapOf("setup" to top, "grip" to top, "top" to top, "bottom" to at(0.28)),
            Gear.bench(0.0, edgeZ - 0.45, back = false),
            Rigging { _, _ -> emptyList() },
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.ELBOWS,
                setupNote = "Hands on the edge of the bench behind you, legs out in front, heels on the floor.",
                gripNote = "Fingers forward over the edge, hands shoulder width apart.",
                cameraYaw = 80 * DEG
            ),
            Camera(yaw = 70 * DEG, pitch = 12 * DEG, distance = 3.1, target = v(0.0, 0.6, 0.3))
        )
    }

    private fun machineDip(): Motion {
        val seat = MovesSeated.seated(0.0)
        fun hands(y: Double, z: Double) = seat.copy(handL = v(0.27, y, z), handR = v(-0.27, y, z), elbowPole = v(0.1, 0.2, -1.0))
        val top = hands(0.62, 0.12)
        val bottom = hands(0.98, 0.02)
        return Motion(
            mapOf("setup" to bottom, "grip" to bottom, "top" to top, "bottom" to bottom),
            Gear.machineSeat(),
            Rigging { s, _ ->
                listOf(s.handL, s.handR).map { h -> Prim.Capsule(h - v(0.0, 0.0, 0.05), h + v(0.0, 0.0, 0.25), 0.022, 0.022, Palette.STEEL) }
            },
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.ELBOWS,
                setupNote = "Sit tall with your back on the pad, feet flat on the floor.",
                gripNote = "Neutral grip on the handles at your sides, palms facing in.",
                cameraYaw = 80 * DEG
            ),
            Camera(yaw = 55 * DEG, pitch = 12 * DEG, distance = 3.0, target = v(0.0, 0.8, 0.0))
        )
    }

    /** Overhead presses: standing or seated, bar, dumbbells, kettlebells, machine or Smith. */
    fun overhead(spec: Spec): Motion {
        val seated = spec.seated || spec.gear == "MACHINE" || spec.gear == "SMITH"
        val db = spec.gear == "DUMBBELL" || spec.gear == "KETTLEBELL"
        val behindNeck = spec.has("behind-the-neck", "behind the neck")
        val arnold = spec.has("arnold")
        // Z press: sitting on the floor, legs straight out, nothing to lean against.
        val floor = spec.has("z press")
        val base = when {
            floor -> Kit.stand(0.15).copy(
                pelvis = v(0.0, 0.12, 0.0), footL = v(0.18, 0.1, 0.8), footR = v(-0.18, 0.1, 0.8),
                footPitch = -60 * DEG, kneePole = v(0.0, 1.0, 0.2)
            )
            seated -> MovesSeated.seated(0.0)
            else -> Kit.stand(0.15)
        }
        val sh = Kit.shoulder(base.pelvis, base.pitch, 1.0)
        val half = if (db) 0.25 else spec.gripHalf.coerceAtLeast(0.25)
        val z = if (behindNeck) -0.1 else 0.05
        val rackY = sh.y + 0.02
        val rackZ = if (behindNeck) -0.08 else 0.16
        val bottom = base.copy(handL = v(half, rackY, rackZ), handR = v(-half, rackY, rackZ), elbowPole = v(0.6, -1.0, if (db) -0.2 else 0.5))
        val top = base.copy(handL = v(if (db) 0.13 else half * 0.95, sh.y + 0.6, z), handR = v(if (db) -0.13 else -half * 0.95, sh.y + 0.6, z), elbowPole = v(1.0, 0.0, 0.0))
        val start = if (arnold) bottom.copy(handL = v(0.16, rackY + 0.05, 0.2), handR = v(-0.16, rackY + 0.05, 0.2), elbowPole = v(0.1, -1.0, 0.8)) else bottom
        val fixed = mutableListOf<Prim>()
        if (seated) fixed += Gear.bench(80.0, 0.12)
        val moving = Rigging { s, _ ->
            when {
                spec.gear == "KETTLEBELL" -> Gear.kettlebell(s.handL, v(0.0, 0.3, -1.0)) + Gear.kettlebell(s.handR, v(0.0, 0.3, -1.0))
                db -> Gear.dumbbell(s.handL, if (arnold) Vec3.Z else Vec3.X) + Gear.dumbbell(s.handR, if (arnold) Vec3.Z else Vec3.X)
                spec.gear == "MACHINE" -> listOf(Prim.Capsule(s.handL, s.handR, 0.02, 0.02, Palette.STEEL))
                else -> Gear.barbell(s.handL, s.handR, length = if (spec.gear == "EZ_BAR") 1.25 else 2.2)
            }
        }
        return Motion(
            mapOf("setup" to start, "grip" to start, "top" to top, "bottom" to start),
            fixed, moving,
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = if (floor) "Sit on the floor, legs straight and a little apart, back upright with no support."
                else if (seated) "Sit tall, back against the pad, feet flat and wide for balance."
                else "Feet hip width, glutes squeezed, ribs pulled down so the lower back does not arch.",
                gripNote = if (db) "Dumbbells at shoulder height, palms forward, elbows under the wrists." else Kit.gripNote(spec),
                startFocus = Focus.CORE, upSeconds = 1.6, downSeconds = 1.0
            ),
            Camera(yaw = 40 * DEG, pitch = 8 * DEG, distance = 3.6, target = v(0.0, if (floor) 0.6 else if (seated) 0.95 else 1.05, 0.0))
        )
    }
}
