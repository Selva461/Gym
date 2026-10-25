package dev.avles.gym.anim

import dev.avles.gym.anim.Kit.pair

/** Elbow work: biceps curls, triceps extensions and pushdowns, and the wrist and forearm moves. */
object MovesCurl {

    private val STAND_SH = Body.STAND - 0.01 + Body.SPINE - 0.05

    private fun standing() = Kit.stand(0.15).copy(pelvis = v(0.0, Body.STAND - 0.01, 0.0))

    private fun curlHalf(spec: Spec) = when {
        spec.gear == "DUMBBELL" || spec.gear == "KETTLEBELL" || spec.gear == "BAND" -> 0.21
        spec.has("close") -> 0.13
        spec.has("wide") -> 0.3
        spec.gear == "CABLE" -> 0.17
        else -> 0.2
    }

    private fun handleAxis(spec: Spec) = if (spec.grip == Grip.NEUTRAL) Vec3.Z else Vec3.X

    // ---- biceps -------------------------------------------------------------

    fun curl(spec: Spec): Motion = when {
        spec.has("wrist", "pronation", "supination", "radial", "finger roll", "forearm curl") -> wrist(spec)
        spec.has("fat-grip", "rice bucket") -> MovesArms.grip(spec)
        spec.has("preacher", "machine", "spider", "over bench") || spec.gear == "MACHINE" -> padCurl(spec)
        spec.has("incline") -> inclineCurl(spec)
        spec.has("concentration") -> concentration()
        spec.has("overhead cable", "high cable") -> highCableCurl(spec)
        spec.has("ring") -> ringCurl()
        else -> standingCurl(spec)
    }

    private fun standingCurl(spec: Spec): Motion {
        val seated = spec.has("seated")
        val base = if (seated) MovesSeated.seated(-0.05) else standing()
        val shY = base.pelvis.y + Body.SPINE - 0.05
        val half = curlHalf(spec)
        val drag = spec.has("drag")
        val bayesian = spec.has("bayesian")
        val one = spec.oneArm || spec.has("single-arm", "alternating", "cross-body")
        val bottomZ = if (bayesian) -0.12 else 0.1
        val bottom = base.copy(handL = v(half, shY - 0.6, bottomZ), handR = v(-half, shY - 0.6, bottomZ), elbowPole = v(0.1, -0.4, -1.0))
        val topHands = when {
            drag -> pair(half, shY - 0.2, 0.12)
            bayesian -> pair(half, shY - 0.05, 0.05)
            spec.has("cross-body") -> Pair(v(0.03, shY - 0.1, 0.22), v(-0.03, shY - 0.1, 0.22))
            else -> pair(half, shY - 0.06, 0.24)
        }
        val topPole = if (drag) v(0.2, -0.2, -1.0) else v(0.1, -0.6, -1.0)
        val top = base.copy(handL = topHands.first, handR = topHands.second, elbowPole = topPole)
        fun oneArm(p: Pose) = if (one) p.copy(handL = bottom.handL, elbowPole = bottom.elbowPole, elbowPoleR = p.elbowPole) else p
        val pulleyZ = if (bayesian) -0.7 else 0.6
        return Motion(
            mapOf("setup" to bottom, "grip" to bottom, "bottom" to bottom, "top" to oneArm(top)),
            (if (spec.gear == "CABLE") Gear.cableTower(v(0.0, 0.0, pulleyZ + if (bayesian) -0.1 else 0.1), 0.12).first else emptyList()) +
                (if (seated) Gear.bench(0.0, -0.1, back = false) else emptyList()),
            MovesArms.held(spec, handleAxis(spec), listOf(v(0.0, 0.12, pulleyZ)), oneHand = false),
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = if (seated) "Sit tall on the end of a bench, feet flat." else "Feet hip width, knees soft, glutes tight so nothing swings.",
                gripNote = "${Kit.gripNote(spec, if (spec.gear == "DUMBBELL") "the dumbbells" else "the handle")} Elbows pinned to your sides.",
                startFocus = Focus.ELBOWS, upSeconds = 1.0, downSeconds = 2.0, cameraYaw = 75 * DEG
            ),
            Camera(yaw = 65 * DEG, pitch = 8 * DEG, distance = 3.2, target = v(0.0, 1.05, 0.0))
        )
    }

    /** Preacher, machine and spider curls: the upper arm rests on a pad. */
    private fun padCurl(spec: Spec): Motion {
        val spider = spec.has("spider", "over bench")
        val half = curlHalf(spec)
        val down: Pose
        val up: Pose
        val fixed: List<Prim>
        if (spider) {
            // Chest on an incline bench, arms hanging straight down in front of it.
            val pitch = 50 * DEG
            val pelvis = v(0.0, 0.72, -0.12)
            val base = Pose(
                pelvis = pelvis, pitch = pitch, head = -0.3,
                handL = v(0.0, 0.0, 0.0), handR = v(0.0, 0.0, 0.0),
                footL = v(0.18, Kit.ANKLE, -0.6), footR = v(-0.18, Kit.ANKLE, -0.6), kneePole = v(0.1, 0.0, 1.0)
            )
            fixed = Gear.bench(0.0, -0.1, back = false) + Prim.Box(
                Kit.onTorso(pelvis, pitch, v(0.0, 0.3, -0.16)), Basis.of(0.0, pitch), v(0.15, 0.4, 0.025), Palette.PAD
            )
            val sh = Kit.onTorso(pelvis, pitch, v(0.0, Body.SPINE - 0.05, 0.0))
            down = base.copy(handL = v(half, sh.y - 0.6, sh.z + 0.04), handR = v(-half, sh.y - 0.6, sh.z + 0.04), elbowPole = v(0.1, 0.0, -1.0))
            up = base.copy(handL = v(half, sh.y - 0.15, sh.z + 0.12), handR = v(-half, sh.y - 0.15, sh.z + 0.12), elbowPole = v(0.1, -0.5, -1.0))
        } else {
            // Upper arms lie down and forward along a pad sloped at 45 degrees.
            val base = MovesSeated.seated(-0.15, seatY = 0.58)
            val sh = Kit.onTorso(base.pelvis, 0.0, v(0.0, Body.SPINE - 0.05, 0.0))
            val slope = v(0.0, -0.707, 0.707)
            val normal = v(0.0, 0.707, 0.707)
            val elbow = sh + slope * (Body.UPPER_ARM - 0.01)
            val padCentre = (sh + elbow) * 0.5 + slope * 0.06 - normal * 0.075
            fixed = listOf(
                Prim.Box(padCentre, Basis(Vec3.X, normal, slope), v(0.22, 0.025, 0.2), Palette.PAD),
                Prim.Box(v(0.0, padCentre.y / 2, padCentre.z + 0.05), Basis.IDENTITY, v(0.04, padCentre.y / 2, 0.04), Palette.FRAME),
                Prim.Box(v(0.0, 0.03, 0.15), Basis.IDENTITY, v(0.35, 0.03, 0.4), Palette.FRAME),
                Prim.Box(v(0.0, 0.55, -0.07), Basis.IDENTITY, v(0.18, 0.03, 0.18), Palette.PAD),
                Prim.Box(v(0.0, 0.28, -0.07), Basis.IDENTITY, v(0.04, 0.26, 0.04), Palette.FRAME)
            )
            fun hands(dir: Vec3): Pair<Vec3, Vec3> {
                val h = elbow + dir.normalized() * (Body.FOREARM - 0.02)
                return Pair(v(half, h.y, h.z), v(-half, h.y, h.z))
            }
            val pole = v(0.0, -1.0, 0.6)
            // Nearly straight at the bottom (a soft elbow), forearm just past vertical at the top.
            val (dl, dr) = hands(slope + normal * 0.18)
            val (ul, ur) = hands(v(0.0, 1.0, -0.15))
            down = base.copy(handL = dl, handR = dr, elbowPole = pole)
            up = base.copy(handL = ul, handR = ur, elbowPole = pole)
        }
        return Motion(
            mapOf("setup" to down, "grip" to down, "bottom" to down, "top" to up),
            fixed,
            if (spec.gear == "MACHINE") Rigging { s, _ -> listOf(Prim.Cylinder(s.handL + v(0.06, 0.0, 0.0), s.handR - v(0.06, 0.0, 0.0), 0.016, Palette.STEEL)) }
            else MovesArms.held(spec, handleAxis(spec), listOf(v(0.0, 0.3, 0.8))),
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = if (spider) "Chest on an incline bench set to about 45 degrees, arms hanging straight down."
                else "Seat set so the armpits sit snugly over the top of the pad, the back of the arms flat on it.",
                gripNote = "${Kit.gripNote(spec, "the handle")} Elbows stay on the pad.",
                startFocus = Focus.ELBOWS, upSeconds = 1.0, downSeconds = 2.2, cameraYaw = 80 * DEG
            ),
            Camera(yaw = 70 * DEG, pitch = 10 * DEG, distance = 3.0, target = v(0.0, 0.95, 0.1))
        )
    }

    private fun inclineCurl(spec: Spec): Motion {
        val back = 55.0
        val pitch = -(90 - back) * DEG
        val pelvis = v(0.0, Gear.BENCH_TOP + 0.12, 0.12)
        val sh = Kit.onTorso(pelvis, pitch, v(0.0, Body.SPINE - 0.05, 0.0))
        fun at(l: Vec3, r: Vec3, pole: Vec3) = Pose(
            pelvis = pelvis, pitch = pitch, head = 0.15, handL = l, handR = r,
            footL = v(0.2, Kit.ANKLE, 0.62), footR = v(-0.2, Kit.ANKLE, 0.62),
            elbowPole = pole, kneePole = v(0.15, 0.0, 1.0)
        )
        val down = at(v(0.24, sh.y - 0.6, sh.z - 0.04), v(-0.24, sh.y - 0.6, sh.z - 0.04), v(0.1, 0.0, -1.0))
        val up = at(v(0.23, sh.y - 0.12, sh.z + 0.22), v(-0.23, sh.y - 0.12, sh.z + 0.22), v(0.1, -0.4, -1.0))
        return Motion(
            mapOf("setup" to down, "grip" to down, "bottom" to down, "top" to up),
            Gear.bench(back, 0.15),
            MovesArms.held(spec, handleAxis(spec)),
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = "Bench at about 55 degrees, back and head on the pad, arms hanging straight down behind the body.",
                gripNote = "${Kit.gripNote(spec, "the dumbbells")} Keep the elbows pointing at the floor.",
                startFocus = Focus.SHOULDERS, upSeconds = 1.0, downSeconds = 2.2, cameraYaw = 80 * DEG
            ),
            Camera(yaw = 75 * DEG, pitch = 10 * DEG, distance = 3.0, target = v(0.0, 0.8, 0.0))
        )
    }

    private fun concentration(): Motion {
        val seat = MovesSeated.seated(-0.1, lean = 35.0).copy(
            footL = v(0.3, Kit.ANKLE, 0.45), footR = v(-0.3, Kit.ANKLE, 0.45), kneePole = v(0.5, 0.0, 1.0)
        )
        val sh = Kit.onTorso(seat.pelvis, seat.pitch, v(-Body.SHOULDER_HALF, Body.SPINE - 0.05, 0.0))
        val down = seat.copy(handL = v(0.22, 0.55, 0.42), handR = v(sh.x + 0.02, sh.y - 0.6, sh.z + 0.08), elbowPoleR = v(-0.3, -0.2, -1.0), elbowPole = v(0.3, -1.0, 0.0))
        val up = down.copy(handR = v(sh.x + 0.06, sh.y - 0.12, sh.z + 0.14))
        return Motion(
            mapOf("setup" to down, "grip" to down, "bottom" to down, "top" to up),
            Gear.bench(0.0, -0.1, back = false),
            Rigging { s, _ -> Gear.dumbbell(s.handR, Vec3.X) },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = "Sit on the end of a bench, feet wide, lean forward and brace the back of the working arm against the inside of the thigh.",
                gripNote = "Underhand, the arm hanging straight down from the shoulder. The other hand rests on the other knee.",
                startFocus = Focus.ELBOWS, upSeconds = 1.0, downSeconds = 2.0, cameraYaw = 30 * DEG
            ),
            Camera(yaw = 20 * DEG, pitch = 14 * DEG, distance = 2.8, target = v(0.0, 0.8, 0.1))
        )
    }

    private fun highCableCurl(spec: Spec): Motion {
        val base = standing().copy(footL = v(0.18, Kit.ANKLE, 0.0), footR = v(-0.18, Kit.ANKLE, 0.0))
        val out = base.copy(handL = v(0.78, STAND_SH + 0.02, 0.06), handR = v(-0.78, STAND_SH + 0.02, 0.06), elbowPole = v(0.0, -1.0, -0.3))
        val curled = base.copy(handL = v(0.32, STAND_SH + 0.17, 0.1), handR = v(-0.32, STAND_SH + 0.17, 0.1), elbowPole = v(0.0, -1.0, -0.3))
        val left = Gear.cableTower(v(1.2, 0.0, 0.05), STAND_SH + 0.08)
        val right = Gear.cableTower(v(-1.2, 0.0, 0.05), STAND_SH + 0.08)
        return Motion(
            mapOf("setup" to out, "grip" to out, "bottom" to out, "top" to curled),
            left.first + right.first,
            MovesArms.held(spec, Vec3.Z, listOf(left.second, right.second)),
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = "Stand in the middle of a cable crossover, pulleys at shoulder height.",
                gripNote = "Underhand on both handles, arms straight out to the sides at shoulder height. Upper arms stay still.",
                startFocus = Focus.SHOULDERS, upSeconds = 1.0, downSeconds = 1.8, cameraYaw = 10 * DEG
            ),
            Camera(yaw = 10 * DEG, pitch = 8 * DEG, distance = 3.8, target = v(0.0, 1.15, 0.0))
        )
    }

    private fun ringCurl(): Motion {
        val ankle = v(0.0, Kit.ANKLE, 0.0)
        fun body(shZ: Double, shY: Double, handY: Double, handZ: Double): Pose {
            val (pelvis, pitch) = Kit.plank(ankle, v(0.0, shY, shZ))
            return Pose(
                pelvis = pelvis, pitch = pitch, head = -0.2,
                handL = v(0.18, handY, handZ), handR = v(-0.18, handY, handZ),
                footL = v(0.12, Kit.ANKLE, 0.0), footR = v(-0.12, Kit.ANKLE, 0.0),
                elbowPole = v(0.1, -1.0, -0.4), footPitch = 0.0
            )
        }
        val ringZ = 1.0
        val down = body(0.62, 0.95, 1.45, ringZ)
        val up = body(0.42, 1.18, 1.45, ringZ)
        return Motion(
            mapOf("setup" to down, "grip" to down, "bottom" to down, "top" to up),
            listOf(Prim.Line(v(0.18, 2.6, ringZ), v(0.18, 1.5, ringZ), 0.01, Palette.CABLE), Prim.Line(v(-0.18, 2.6, ringZ), v(-0.18, 1.5, ringZ), 0.01, Palette.CABLE)),
            Rigging { s, _ -> listOf(s.handL, s.handR).map { h -> Prim.Capsule(h - v(0.0, 0.04, 0.0), h + v(0.0, 0.04, 0.0), 0.02, 0.02, Palette.PAD) } },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = "Rings at about head height. Walk the feet forward until the body leans back, straight from head to heel.",
                gripNote = "Underhand on the rings, arms straight in front.",
                startFocus = Focus.CORE, upSeconds = 1.0, downSeconds = 2.0, cameraYaw = 80 * DEG
            ),
            Camera(yaw = 80 * DEG, pitch = 10 * DEG, distance = 3.6, target = v(0.0, 1.0, 0.5))
        )
    }

    // ---- triceps --------------------------------------------------------------

    fun triceps(spec: Spec): Motion = when {
        spec.has("close-grip dumbbell press", "tate", "crush press") -> MovesPress.bench(spec.copy(equipment = "DUMBBELL", name = "close-grip dumbbell bench press"))
        spec.has("jm press", "close-grip bench") -> MovesPress.bench(spec.copy(name = "close-grip bench press"))
        spec.has("bodyweight") -> bodyweightExtension()
        spec.has("skull", "lying") -> skullCrusher(spec)
        spec.has("kickback") -> kickback(spec)
        spec.has("overhead", "french", "bent-over cable") || (spec.gear == "MACHINE" && spec.has("overhead")) -> overhead(spec)
        spec.gear == "MACHINE" -> machineExtension()
        else -> pushdown(spec)
    }

    private fun pushdown(spec: Spec): Motion {
        val lean = 12 * DEG
        val base = standing().copy(pitch = lean, pelvis = v(0.0, Body.STAND - 0.03, -0.04), footL = v(0.15, Kit.ANKLE, 0.0), footR = v(-0.15, Kit.ANKLE, 0.0))
        val sh = Kit.onTorso(base.pelvis, lean, v(0.0, Body.SPINE - 0.05, 0.0))
        val elbowY = sh.y - 0.28
        val rope = spec.has("rope")
        val one = spec.has("single-arm", "cross-body")
        val half = when { rope -> 0.05; spec.has("v-bar") -> 0.06; else -> 0.12 }
        val wideEnd = if (rope || spec.has("wide finish")) 0.2 else half
        val top = base.copy(handL = v(half, elbowY + 0.1, sh.z + 0.3), handR = v(-half, elbowY + 0.1, sh.z + 0.3), elbowPole = v(0.1, -0.5, -1.0))
        val bottom = base.copy(handL = v(wideEnd, elbowY - 0.3, sh.z + 0.1), handR = v(-wideEnd, elbowY - 0.3, sh.z + 0.1), elbowPole = v(0.1, -0.2, -1.0))
        fun oneArm(p: Pose) = if (one) p.copy(handL = v(0.26, 0.85, 0.05), elbowPole = v(0.3, -1.0, -0.4), elbowPoleR = p.elbowPole) else p
        val (tower, pulley) = Gear.cableTower(v(0.0, 0.0, 0.62), 1.95)
        val bandAnchor = v(0.0, 1.95, 0.55)
        return Motion(
            mapOf("setup" to oneArm(top), "grip" to oneArm(top), "top" to oneArm(top), "bottom" to oneArm(bottom)),
            if (spec.has("band")) listOf(Prim.Box(v(0.0, 1.0, 0.62), Basis.IDENTITY, v(0.05, 1.0, 0.05), Palette.FRAME)) else tower,
            Rigging { s, _ ->
                val mid = (s.handL + s.handR) * 0.5
                when {
                    spec.has("band") -> listOf(Gear.band(bandAnchor, s.handL), Gear.band(bandAnchor, s.handR))
                    one -> Gear.cable(pulley, s.handR)
                    rope -> listOf(Prim.Line(pulley, mid + v(0.0, 0.08, 0.0), 0.008, Palette.CABLE),
                        Prim.Line(mid + v(0.0, 0.08, 0.0), s.handL, 0.014, Palette.PAD), Prim.Line(mid + v(0.0, 0.08, 0.0), s.handR, 0.014, Palette.PAD))
                    else -> listOf(Prim.Line(pulley, mid, 0.008, Palette.CABLE), Prim.Cylinder(s.handL + v(0.08, 0.0, 0.0), s.handR - v(0.08, 0.0, 0.0), 0.014, Palette.STEEL))
                }
            },
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.ELBOWS,
                setupNote = "Pulley at the top. Stand close, feet hip width, a small lean forward from the hips.",
                gripNote = when {
                    rope -> "Rope with thumbs up, palms facing each other. Spread the ends apart at the bottom."
                    spec.grip == Grip.UNDERHAND -> "Underhand on the bar, hands narrow."
                    else -> "Overhand on the bar, hands narrow."
                } + " Elbows pinned to your sides the whole time.",
                startFocus = Focus.ELBOWS, upSeconds = 1.6, downSeconds = 1.0, cameraYaw = 80 * DEG
            ),
            Camera(yaw = 70 * DEG, pitch = 8 * DEG, distance = 3.2, target = v(0.0, 1.05, 0.1))
        )
    }

    private fun overhead(spec: Spec): Motion {
        val seated = spec.has("seated") || spec.gear == "MACHINE"
        val bent = spec.has("bent-over")
        val cable = spec.gear == "CABLE"
        val one = spec.oneArm || spec.has("single-arm")
        val base = when {
            seated -> MovesSeated.seated(-0.05)
            bent || cable -> standing().copy(pitch = (if (bent) 45 else 12) * DEG, pelvis = v(0.0, Body.STAND - 0.05, -0.05),
                footL = v(0.15, Kit.ANKLE, 0.2), footR = v(-0.15, Kit.ANKLE, -0.2), kneePole = v(0.1, 0.0, 1.0))
            else -> standing()
        }
        val torso = Basis.of(0.0, base.pitch)
        val sh = base.pelvis + torso.up * (Body.SPINE - 0.05)
        val elbow = sh + torso.up * 0.27 + torso.fwd * 0.06
        val half = when { one -> 0.0; spec.gear == "DUMBBELL" -> 0.03; spec.has("rope") -> 0.05; else -> 0.12 }
        val bottomHand = elbow - torso.up * 0.26 - torso.fwd * 0.17
        val topHand = elbow + torso.up * 0.31 + torso.fwd * 0.04
        val pole = v(0.25, 0.4, 1.0)
        fun at(h: Vec3) = base.copy(handL = h + torso.side * half, handR = h - torso.side * half, elbowPole = pole)
        fun oneArm(p: Pose) = if (one) p.copy(handL = v(0.24, sh.y - 0.15, sh.z + 0.2), elbowPole = v(0.3, -1.0, -0.2), elbowPoleR = pole) else p
        val low = at(bottomHand)
        val high = at(topHand)
        val pulley = v(0.0, 0.5, -0.75)
        return Motion(
            mapOf("setup" to oneArm(high), "grip" to oneArm(high), "top" to oneArm(high), "bottom" to oneArm(low)),
            (if (seated) Gear.bench(if (spec.gear == "MACHINE") 85.0 else 0.0, -0.1, back = spec.gear == "MACHINE") else emptyList()) +
                (if (cable) Gear.cableTower(v(0.0, 0.0, -0.85), 0.5).first else emptyList()),
            Rigging { s, _ ->
                val mid = (s.handL + s.handR) * 0.5
                when {
                    cable -> Gear.cable(pulley, if (one) s.handR else mid)
                    spec.gear == "DUMBBELL" && !one -> Gear.dumbbell(mid, torso.up * -1.0 + torso.fwd * 0.0, 0.08)
                    spec.gear == "DUMBBELL" -> Gear.dumbbell(s.handR, Vec3.X)
                    spec.gear == "MACHINE" -> listOf(Prim.Cylinder(s.handL + v(0.06, 0.0, 0.0), s.handR - v(0.06, 0.0, 0.0), 0.016, Palette.STEEL))
                    else -> Gear.barbell(s.handL, s.handR, 1.25, 1, Gear.SMALL_PLATE_R)
                }
            },
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.ELBOWS,
                setupNote = when {
                    cable -> "Face away from the pulley, rope over the head, one foot forward, a slight lean."
                    seated -> "Sit tall with the back supported, core braced so the lower back does not arch."
                    else -> "Feet hip width, ribs down and glutes tight so the back does not arch."
                },
                gripNote = when {
                    spec.gear == "DUMBBELL" && !one -> "Both hands cupped under the top plate of one dumbbell, held overhead."
                    spec.has("rope") -> "Rope behind the head, thumbs up."
                    else -> "${Kit.gripNote(spec, "the handle")} Elbows point forward and stay by the ears."
                },
                startFocus = Focus.ELBOWS, upSeconds = 1.2, downSeconds = 2.0, cameraYaw = 75 * DEG
            ),
            Camera(yaw = 65 * DEG, pitch = 8 * DEG, distance = 3.3, target = v(0.0, 1.25, 0.0))
        )
    }

    private fun skullCrusher(spec: Spec): Motion {
        val incline = spec.has("incline")
        val angle = if (incline) 35.0 else 0.0
        val pitch = (-90 + angle) * DEG
        val pelvis = v(0.0, Gear.BENCH_TOP + 0.12, if (incline) 0.08 else 0.12)
        val torso = Basis.of(0.0, pitch)
        val sh = pelvis + torso.up * (Body.SPINE - 0.05)
        val armUp = torso.fwd
        val half = when { spec.gear == "DUMBBELL" -> 0.12; spec.gear == "CABLE" -> 0.08; else -> 0.12 }
        // The upper arms lean slightly back over the head; the elbows stay put.
        val elbowDir = (armUp + torso.up * 0.25).normalized()
        val elbow = sh + elbowDir * 0.28
        val top = elbow + elbowDir * 0.32
        val bottom = elbow + torso.up * 0.27 - armUp * 0.13
        fun lying(h: Vec3) = Pose(
            pelvis = pelvis, pitch = pitch, head = 0.1, handL = h + Vec3.X * half, handR = h - Vec3.X * half,
            footL = v(0.32, Kit.ANKLE, 0.62), footR = v(-0.32, Kit.ANKLE, 0.62),
            elbowPole = v(0.25, 1.0, 0.6), kneePole = v(0.25, 0.0, 1.0), footYaw = 0.25
        )
        val cable = spec.gear == "CABLE"
        val pulley = v(0.0, 0.3, -1.1)
        return Motion(
            mapOf("setup" to lying(top), "grip" to lying(top), "top" to lying(top), "bottom" to lying(bottom)),
            Gear.bench(angle, 0.15) + if (cable) Gear.cableTower(v(0.0, 0.0, -1.25), 0.3).first else emptyList(),
            Rigging { s, _ ->
                when (spec.gear) {
                    "DUMBBELL" -> Gear.dumbbell(s.handL, Vec3.Z) + Gear.dumbbell(s.handR, Vec3.Z)
                    "CABLE" -> Gear.cable(pulley, (s.handL + s.handR) * 0.5)
                    else -> Gear.barbell(s.handL, s.handR, 1.25, 1, Gear.SMALL_PLATE_R)
                }
            },
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.ELBOWS,
                setupNote = "Lie on the bench, feet planted, the weight pressed up over the shoulders.",
                gripNote = if (spec.gear == "DUMBBELL") "Palms facing each other, a dumbbell in each hand." else "Overhand, hands about shoulder width or a little closer on the bar.",
                startFocus = Focus.ELBOWS, upSeconds = 1.2, downSeconds = 2.0, cameraYaw = 80 * DEG
            ),
            Camera(yaw = 75 * DEG, pitch = 16 * DEG, distance = 3.0, target = v(0.0, 0.75, -0.1))
        )
    }

    private fun kickback(spec: Spec): Motion {
        val pitch = 70 * DEG
        val pelvis = v(0.0, 0.84, -0.14)
        val torso = Basis.of(0.0, pitch)
        val sh = pelvis + torso.up * (Body.SPINE - 0.05)
        val back = (torso.up * -1.0)
        val elbowOf = { side: Double -> sh + torso.side * (side * Body.SHOULDER_HALF) + back * 0.27 + Vec3(0.0, -0.06, 0.0) }
        fun pose(extended: Boolean): Pose {
            fun hand(side: Double): Vec3 {
                val e = elbowOf(side)
                return if (extended) e + (back * 0.97 + Vec3(0.0, -0.24, 0.0)).normalized() * 0.32 else e + v(0.0, -0.32, 0.03)
            }
            return Pose(
                pelvis = pelvis, pitch = pitch, head = -0.5,
                handL = hand(1.0), handR = hand(-1.0),
                footL = v(0.16, Kit.ANKLE, 0.02), footR = v(-0.16, Kit.ANKLE, 0.02),
                elbowPole = v(0.1, 1.0, 0.2), kneePole = v(0.1, 0.0, 1.0)
            )
        }
        val pulley = v(0.0, 0.2, 0.75)
        return Motion(
            mapOf("setup" to pose(false), "grip" to pose(false), "bottom" to pose(false), "top" to pose(true)),
            if (spec.gear == "CABLE") Gear.cableTower(v(0.0, 0.0, 0.9), 0.2).first else emptyList(),
            if (spec.gear == "CABLE") Rigging { s, _ -> Gear.cable(pulley, s.handR) + Gear.cable(pulley, s.handL) }
            else MovesArms.held(spec, Vec3.Z),
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = "Hinge forward until the chest is nearly level, knees soft, back flat. Upper arms tight to the sides and parallel to the floor.",
                gripNote = "Neutral grip, palms facing each other. Only the forearm moves.",
                startFocus = Focus.BACK, upSeconds = 1.0, downSeconds = 1.6, cameraYaw = 85 * DEG
            ),
            Camera(yaw = 80 * DEG, pitch = 10 * DEG, distance = 3.2, target = v(0.0, 0.9, 0.0))
        )
    }

    private fun machineExtension(): Motion {
        // Upper arms rest down and forward on a 45 degree pad; the forearms swing about the elbow.
        val seat = MovesSeated.seated(-0.12, seatY = 0.55)
        val sh = Kit.onTorso(seat.pelvis, 0.0, v(0.0, Body.SPINE - 0.05, 0.0))
        val slope = v(0.0, -0.707, 0.707)
        val normal = v(0.0, 0.707, 0.707)
        val elbow = sh + slope * (Body.UPPER_ARM - 0.01)
        val reach = Body.FOREARM - 0.02
        fun hands(dir: Vec3): Pair<Vec3, Vec3> {
            val h = elbow + dir.normalized() * reach
            return Pair(v(0.17, h.y, h.z), v(-0.17, h.y, h.z))
        }
        val pole = v(0.0, -1.0, 0.6)
        val flexed = hands(v(0.0, 1.0, -0.15))
        val extended = hands(slope + normal * 0.18)
        val up = seat.copy(handL = flexed.first, handR = flexed.second, elbowPole = pole)
        val down = seat.copy(handL = extended.first, handR = extended.second, elbowPole = pole)
        val padCentre = (sh + elbow) * 0.5 + slope * 0.06 - normal * 0.075
        return Motion(
            mapOf("setup" to up, "grip" to up, "bottom" to up, "top" to down),
            listOf(
                Prim.Box(padCentre, Basis(Vec3.X, normal, slope), v(0.22, 0.025, 0.2), Palette.PAD),
                Prim.Box(v(0.0, padCentre.y / 2, padCentre.z + 0.05), Basis.IDENTITY, v(0.04, padCentre.y / 2, 0.04), Palette.FRAME)
            ) + Gear.machineSeat(0.55, 90.0, -0.12),
            Rigging { s, _ -> listOf(Prim.Cylinder(s.handL + v(0.06, 0.0, 0.0), s.handR - v(0.06, 0.0, 0.0), 0.016, Palette.STEEL)) },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = "Seat set so the elbows line up with the machine's pivot, the back of the arms on the pad.",
                gripNote = "Handles in a neutral or overhand grip, wrists straight.",
                startFocus = Focus.ELBOWS, upSeconds = 1.0, downSeconds = 1.8, cameraYaw = 80 * DEG
            ),
            Camera(yaw = 70 * DEG, pitch = 10 * DEG, distance = 3.0, target = v(0.0, 0.95, 0.1))
        )
    }

    private fun bodyweightExtension(): Motion {
        val bar = v(0.0, 0.95, 0.5)
        val ankle = v(0.0, Kit.ANKLE, -0.98)
        fun at(shoulders: Vec3): Pose {
            val (pelvis, pitch) = Kit.plank(ankle, shoulders)
            return Pose(
                pelvis = pelvis, pitch = pitch, head = -0.1,
                handL = bar + v(0.14, 0.0, 0.0), handR = bar - v(0.14, 0.0, 0.0),
                footL = ankle + v(0.12, 0.0, 0.0), footR = ankle - v(0.12, 0.0, 0.0),
                elbowPole = v(0.15, 1.0, 0.6), footPitch = 60 * DEG
            )
        }
        val top = at(v(0.0, 1.05, -0.05))
        val bottom = at(v(0.0, 0.85, 0.15))
        return Motion(
            mapOf("setup" to top, "grip" to top, "top" to top, "bottom" to bottom),
            Gear.rack(bar.z, bar.y, 0.62, 1.2) + Prim.Cylinder(bar + v(0.7, 0.0, 0.0), bar - v(0.7, 0.0, 0.0), 0.016, Palette.STEEL),
            Rigging { _, _ -> emptyList() },
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.ELBOWS,
                setupNote = "Bar at about hip height. Walk the feet back until the body is one straight line, leaning on straight arms.",
                gripNote = "Overhand, hands shoulder width. Bend only at the elbows and bring the forehead under the bar.",
                startFocus = Focus.CORE, upSeconds = 1.2, downSeconds = 2.0, cameraYaw = 80 * DEG
            ),
            Camera(yaw = 80 * DEG, pitch = 10 * DEG, distance = 3.4, target = v(0.0, 0.7, -0.2))
        )
    }

    // ---- wrists and forearms -------------------------------------------------

    fun wrist(spec: Spec): Motion {
        if (spec.has("roller")) return roller()
        if (spec.has("behind")) {
            val base = standing()
            val low = base.copy(handL = v(0.22, 0.79, -0.11), handR = v(-0.22, 0.79, -0.11), elbowPole = v(0.2, -1.0, 1.0))
            val high = low.copy(handL = low.handL + v(0.0, 0.03, -0.02), handR = low.handR + v(0.0, 0.03, -0.02))
            return wristMotion(spec, low, high, emptyList(), "Stand tall with the bar behind the thighs, arms straight.")
        }
        // Seated, forearms along the thighs, hands just past the knees.
        val seat = MovesSeated.seated(-0.1, lean = 25.0).copy(footL = v(0.17, Kit.ANKLE, 0.42), footR = v(-0.17, Kit.ANKLE, 0.42))
        val half = if (spec.gear == "DUMBBELL") 0.16 else 0.14
        val low = seat.copy(handL = v(half, 0.57, 0.6), handR = v(-half, 0.57, 0.6), elbowPole = v(0.0, -1.0, 0.0))
        val high = low.copy(handL = low.handL + v(0.0, 0.035, -0.012), handR = low.handR + v(0.0, 0.035, -0.012))
        return wristMotion(spec, low, high, Gear.bench(0.0, -0.18, back = false),
            "Sit on the end of a bench, forearms flat along the thighs, wrists just past the knees.")
    }

    private fun wristMotion(spec: Spec, low: Pose, high: Pose, fixed: List<Prim>, setup: String): Motion = Motion(
        mapOf("setup" to low, "grip" to low, "bottom" to low, "top" to high),
        fixed + if (spec.gear == "CABLE") Gear.cableTower(v(0.0, 0.0, 1.0), 0.1).first else emptyList(),
        MovesArms.held(spec, if (spec.has("radial", "pronation")) Vec3.Y else Vec3.X, listOf(v(0.0, 0.1, 0.9))),
        Kit.steps(
            "setup", "grip", "bottom", "top", Focus.HANDS,
            setupNote = setup,
            gripNote = when {
                spec.has("reverse") -> "Overhand, palms down. Only the wrists move."
                spec.has("pronation", "supination") -> "Hold the dumbbell by one end and turn the palm down, then up."
                spec.has("radial") -> "Hold the dumbbell by one end, thumb up, and tip it up and down."
                else -> "Underhand, palms up. Let the bar roll to the fingertips, then curl it back."
            },
            startFocus = Focus.HANDS, upSeconds = 0.8, downSeconds = 1.2, cameraYaw = 70 * DEG
        ),
        Camera(yaw = 60 * DEG, pitch = 18 * DEG, distance = 2.6, target = v(0.0, 0.75, 0.25))
    )

    private fun roller(): Motion {
        val base = standing()
        val a = base.copy(handL = v(0.16, STAND_SH - 0.02, 0.58), handR = v(-0.16, STAND_SH - 0.02, 0.58), elbowPole = v(0.3, -1.0, -0.4))
        val b = a.copy(handL = a.handL + v(0.0, 0.03, 0.0), handR = a.handR + v(0.0, -0.03, 0.0))
        return Motion(
            mapOf("setup" to a, "grip" to a, "bottom" to a, "top" to b),
            emptyList(),
            Rigging { s, _ ->
                val mid = (s.handL + s.handR) * 0.5
                listOf(Prim.Cylinder(s.handL + v(0.06, 0.0, 0.0), s.handR - v(0.06, 0.0, 0.0), 0.02, Palette.PAD), Prim.Line(mid, mid - v(0.0, 0.7, 0.0), 0.004, Palette.CABLE)) +
                    Gear.plate(mid - v(0.0, 0.72, 0.0), Vec3.Y)
            },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.HANDS,
                setupNote = "Stand tall, arms straight out in front at shoulder height.",
                gripNote = "Overhand on the roller. Turn it one hand after the other to wind the weight up, then lower it slowly.",
                startFocus = Focus.SHOULDERS, upSeconds = 0.6, downSeconds = 0.6, cameraYaw = 40 * DEG
            ),
            Camera(yaw = 40 * DEG, pitch = 10 * DEG, distance = 3.0, target = v(0.0, 1.05, 0.2))
        )
    }
}
