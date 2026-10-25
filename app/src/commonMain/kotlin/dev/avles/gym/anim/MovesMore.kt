package dev.avles.gym.anim

/**
 * Presses that are not on a bench or overhead with a bar: chest-press machines,
 * standing cable and band presses, the Svend press, landmine presses, rotator-cuff
 * work, and the inverted presses (pike and handstand push-ups, wall walks).
 */
object MovesMore {

    private fun standing() = Kit.stand(0.15).copy(pelvis = v(0.0, Body.STAND - 0.01, 0.0))

    /** Seated chest press on a machine: incline and decline change the line of the handles. */
    fun machineChestPress(spec: Spec): Motion {
        val seat = MovesSeated.seated(-0.18)
        val shY = seat.pelvis.y + Body.SPINE - 0.05
        val dy = when { spec.has("incline") -> 0.22; spec.has("decline") -> -0.16; else -> 0.0 }
        val one = spec.oneArm || spec.has("iso-lateral", "single-arm")
        val back = seat.copy(handL = v(0.27, shY - 0.08, 0.1), handR = v(-0.27, shY - 0.08, 0.1), elbowPole = v(1.0, -0.4, -0.6))
        val shZ = seat.pelvis.z
        val outY = shY - 0.06 + dy
        // Arms long but not locked: as far forward as they reach at that height.
        val outZ = shZ + kotlin.math.sqrt((0.58 * 0.58 - 0.01 * 0.01 - (outY - shY) * (outY - shY)).coerceAtLeast(0.04))
        val out = seat.copy(handL = v(0.2, outY, outZ), handR = v(-0.2, outY, outZ), elbowPole = v(1.0, -0.8, -0.2))
        fun oneArm(p: Pose) = if (one) p.copy(handL = back.handL, elbowPole = back.elbowPole, elbowPoleR = p.elbowPole) else p
        return Motion(
            mapOf("setup" to back, "grip" to back, "bottom" to back, "top" to oneArm(out)),
            Gear.machineSeat(Gear.BENCH_TOP, 82.0, -0.18) + listOf(
                Prim.Box(v(0.45, 0.9, 0.25), Basis.IDENTITY, v(0.04, 0.9, 0.04), Palette.FRAME),
                Prim.Box(v(-0.45, 0.9, 0.25), Basis.IDENTITY, v(0.04, 0.9, 0.04), Palette.FRAME)
            ),
            Rigging { s, _ -> listOf(s.handL, s.handR).map { h -> Prim.Capsule(h - v(0.0, 0.09, 0.0), h + v(0.0, 0.09, 0.0), 0.018, 0.018, Palette.STEEL) } },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = "Seat height so the handles line up with the middle of the chest. Back and head on the pad, feet flat.",
                gripNote = "Handles in a full grip, wrists straight, elbows a little below the shoulders.",
                startFocus = Focus.SHOULDERS, upSeconds = 1.0, downSeconds = 2.0, cameraYaw = 70 * DEG
            ),
            Camera(yaw = 60 * DEG, pitch = 12 * DEG, distance = 3.2, target = v(0.0, 0.95, 0.1))
        )
    }

    /** Standing press away from a cable or band anchored behind. */
    fun standingPress(spec: Spec): Motion {
        val base = standing().copy(footL = v(0.15, Kit.ANKLE, 0.22), footR = v(-0.15, Kit.ANKLE, -0.25), toesR = 0.5, pitch = 8 * DEG, kneePole = v(0.15, 0.0, 1.0))
        val shY = base.pelvis.y + Body.SPINE - 0.06
        val one = spec.oneArm
        val back = base.copy(handL = v(0.26, shY - 0.05, 0.1), handR = v(-0.26, shY - 0.05, 0.1), elbowPole = v(1.0, -0.5, -0.6))
        val out = base.copy(handL = v(0.12, shY - 0.02, 0.62), handR = v(-0.12, shY - 0.02, 0.62), elbowPole = v(1.0, -0.8, -0.2))
        fun oneArm(p: Pose) = if (one) p.copy(handL = v(0.26, 0.85, 0.05), elbowPole = v(0.3, -1.0, -0.3), elbowPoleR = p.elbowPole) else p
        val band = spec.gear == "BAND"
        val anchors = listOf(v(0.5, shY, -0.75), v(-0.5, shY, -0.75))
        return Motion(
            mapOf("setup" to oneArm(back), "grip" to oneArm(back), "bottom" to oneArm(back), "top" to oneArm(out)),
            if (band) listOf(Prim.Box(v(0.0, 0.9, -0.8), Basis.IDENTITY, v(0.6, 0.9, 0.04), Palette.FRAME))
            else Gear.cableTower(v(0.6, 0.0, -0.9), shY).first + Gear.cableTower(v(-0.6, 0.0, -0.9), shY).first,
            Rigging { s, _ ->
                val hands = if (one) listOf(s.handR to anchors[1]) else listOf(s.handL to anchors[0], s.handR to anchors[1])
                hands.flatMap { (h, a) -> if (band) listOf(Gear.band(a, h)) else Gear.cable(a, h) }
            },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = "Back to the ${if (band) "anchor" else "cables"}, one foot forward, a slight forward lean, core tight.",
                gripNote = "Handles at chest height, palms down. Press forward and slightly together, then control them back.",
                startFocus = Focus.CORE, upSeconds = 1.0, downSeconds = 1.6, cameraYaw = 75 * DEG
            ),
            Camera(yaw = 65 * DEG, pitch = 10 * DEG, distance = 3.6, target = v(0.0, 1.1, 0.0))
        )
    }

    /** Svend press and squeeze press: two plates or dumbbells pressed together and pushed out from the chest. */
    fun squeezePress(spec: Spec): Motion {
        val base = standing()
        val shY = base.pelvis.y + Body.SPINE - 0.06
        val close = base.copy(handL = v(0.03, shY - 0.1, 0.18), handR = v(-0.03, shY - 0.1, 0.18), elbowPole = v(1.0, -0.6, -0.4))
        val out = base.copy(handL = v(0.03, shY - 0.08, 0.6), handR = v(-0.03, shY - 0.08, 0.6), elbowPole = v(1.0, -0.6, -0.2))
        return Motion(
            mapOf("setup" to close, "grip" to close, "bottom" to close, "top" to out),
            emptyList(),
            Rigging { s, _ -> Gear.plate((s.handL + s.handR) * 0.5, Vec3.X) },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.HANDS,
                setupNote = "Stand tall, feet hip width.",
                gripNote = "Two small plates pressed flat between the palms at the chest, fingers pointing forward. Squeeze hard the whole time.",
                startFocus = Focus.SHOULDERS, upSeconds = 1.0, downSeconds = 1.4, cameraYaw = 75 * DEG
            ),
            Camera(yaw = 65 * DEG, pitch = 10 * DEG, distance = 3.0, target = v(0.0, 1.15, 0.1))
        )
    }

    /** Landmine and Viking presses: the bar pivots on the floor behind, so the press goes up and forward. */
    fun landminePress(spec: Spec): Motion {
        val base = standing().copy(footL = v(0.17, Kit.ANKLE, 0.15), footR = v(-0.17, Kit.ANKLE, -0.15), pitch = 8 * DEG)
        val shY = base.pelvis.y + Body.SPINE - 0.06
        val one = spec.has("landmine")
        val low = base.copy(handL = if (one) v(0.24, 0.85, 0.05) else v(0.1, shY + 0.02, 0.2), handR = v(-0.08, shY + 0.02, 0.2), elbowPole = v(0.4, -1.0, 0.4))
        val high = low.copy(handR = v(-0.05, shY + 0.42, 0.45), handL = if (one) low.handL else v(0.07, shY + 0.42, 0.45))
        val pivot = v(0.0, 0.05, -1.2)
        return Motion(
            mapOf("setup" to low, "grip" to low, "bottom" to low, "top" to high),
            listOf(Prim.Ball(pivot, 0.06, Palette.FRAME)),
            Rigging { s, _ ->
                val grip = if (one) s.handR else (s.handL + s.handR) * 0.5
                val dir = (grip - pivot).normalized()
                listOf(Prim.Cylinder(pivot, grip + dir * 0.12, 0.025, Palette.STEEL)) + Gear.plate(grip + dir * 0.2, dir)
            },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.SHOULDERS,
                setupNote = "Face away from the pivot, staggered stance, the bar end at the shoulder.",
                gripNote = if (one) "End of the bar in one hand at the shoulder, elbow tucked in front." else "Both hands on the handles, neutral grip.",
                startFocus = Focus.CORE, upSeconds = 1.0, downSeconds = 1.6, cameraYaw = 75 * DEG
            ),
            Camera(yaw = 65 * DEG, pitch = 8 * DEG, distance = 3.6, target = v(0.0, 1.2, 0.0))
        )
    }

    /** External rotation and the Cuban press: the elbow stays put and the forearm turns up and out. */
    fun externalRotation(spec: Spec): Motion {
        val base = standing()
        val shY = base.pelvis.y + Body.SPINE - 0.05
        val cuban = spec.has("cuban")
        if (cuban) {
            val hang = base.copy(handL = v(0.14, 0.82, 0.13), handR = v(-0.14, 0.82, 0.13), elbowPole = v(1.0, 0.0, -0.3))
            val row = base.copy(handL = v(0.32, shY - 0.02, 0.1), handR = v(-0.32, shY - 0.02, 0.1), elbowPole = v(1.0, 0.6, -0.2))
            val rotated = base.copy(handL = v(0.4, shY + 0.3, 0.02), handR = v(-0.4, shY + 0.3, 0.02), elbowPole = v(1.0, -0.2, 0.0))
            val press = base.copy(handL = v(0.16, shY + 0.6, 0.0), handR = v(-0.16, shY + 0.6, 0.0), elbowPole = v(1.0, 0.0, 0.0))
            return Motion(
                mapOf("setup" to hang, "grip" to hang, "hang" to hang, "row" to row, "rotate" to rotated, "press" to press),
                emptyList(), MovesArms.held(spec, Vec3.X),
                Kit.flow("setup", "grip", listOf("hang", "row", "rotate", "press", "rotate", "row"), Focus.SHOULDERS, listOf(1.0, 0.9, 0.9, 0.9, 0.9, 1.0),
                    setupNote = "Stand tall, light dumbbells hanging in front of the thighs.",
                    gripNote = "Overhand. Row the elbows out to shoulder height, turn the forearms up, then press.",
                    startFocus = Focus.SHOULDERS, cameraYaw = 20 * DEG),
                Camera(yaw = 20 * DEG, pitch = 8 * DEG, distance = 3.2, target = v(0.0, 1.2, 0.0))
            )
        }
        val elbowY = shY - 0.28
        val inward = base.copy(handL = v(0.06, elbowY, 0.3), handR = v(-0.26, 0.82, 0.04), elbowPole = v(0.2, -1.0, -0.3), elbowPoleR = v(0.3, -1.0, -0.4))
        val outward = inward.copy(handL = v(0.42, elbowY, 0.16))
        val cable = spec.gear == "CABLE"
        val (tower, pulley) = Gear.cableTower(v(-0.7, 0.0, 0.25), elbowY)
        return Motion(
            mapOf("setup" to inward, "grip" to inward, "bottom" to inward, "top" to outward),
            if (cable) tower else emptyList(),
            Rigging { s, _ -> if (cable) Gear.cable(pulley, s.handL) else Gear.dumbbell(s.handL, Vec3.Y) },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.SHOULDERS,
                setupNote = if (cable) "Stand side-on to a cable set at elbow height, the working arm furthest from it." else "Stand tall, or lie on your side, the working elbow against the ribs.",
                gripNote = "Elbow bent to 90 degrees and pinned to the side (a rolled towel helps). Turn the forearm outward like a door opening.",
                startFocus = Focus.ELBOWS, upSeconds = 1.2, downSeconds = 1.6, cameraYaw = 10 * DEG
            ),
            Camera(yaw = 10 * DEG, pitch = 20 * DEG, distance = 2.8, target = v(0.0, 1.05, 0.1))
        )
    }

    /** Pike push-ups, handstand push-ups and wall walks: pressing with the body upside down or nearly. */
    fun inverted(spec: Spec): Motion {
        val wallZ = -0.14
        val handZ = 0.15
        fun handstand(drop: Double): Pose {
            val sh = v(0.0, 0.62 - drop, handZ - 0.02)
            val pelvis = sh + v(0.0, Body.SPINE - 0.05, -0.03)
            return Pose(
                pelvis = pelvis, pitch = 180 * DEG, head = 0.2,
                handL = v(0.24, 0.03, handZ), handR = v(-0.24, 0.03, handZ),
                footL = v(0.08, pelvis.y + 0.86, wallZ + 0.04), footR = v(-0.08, pelvis.y + 0.86, wallZ + 0.04),
                elbowPole = v(0.6, 0.0, -1.0), kneePole = v(0.0, 0.0, 1.0), footPitch = -40 * DEG
            )
        }
        if (spec.has("handstand", "wall walk")) {
            val walk = spec.has("wall walk")
            // A push-up with the feet against the wall.
            val plankPose = MovesPower.mountainClimber().pose("setup").let { p ->
                val dz = v(0.0, 0.0, wallZ + 0.04 - p.footL.z)
                p.copy(pelvis = p.pelvis + dz, handL = p.handL + dz, handR = p.handR + dz, footL = p.footL + dz, footR = p.footR + dz)
            }
            val top = handstand(0.0)
            val bottom = handstand(0.27)
            // Halfway up the wall: shoulders an arm's length above the hands, hips flexed, feet high on the wall.
            val midPitch = 150 * DEG
            val spine = Basis.of(0.0, midPitch).up * (Body.SPINE - 0.05)
            val midShoulders = v(0.0, 0.03 + Kit.ARM_REACH - 0.04, 0.0)
            val midPelvis = v(0.0, midShoulders.y - spine.y, -0.02)
            val midHandZ = midPelvis.z + spine.z + 0.06
            val mid = plankPose.copy(
                pelvis = midPelvis, pitch = midPitch,
                footL = v(0.1, midPelvis.y + 0.78, wallZ + 0.04), footR = v(-0.1, midPelvis.y + 0.78, wallZ + 0.04),
                handL = v(0.24, 0.03, midHandZ), handR = v(-0.24, 0.03, midHandZ)
            )
            val poses = mapOf("setup" to top, "grip" to top, "top" to top, "bottom" to bottom, "plank" to plankPose, "mid" to mid)
            val wall = listOf(Prim.Box(v(0.0, 1.2, wallZ - 0.03), Basis.IDENTITY, v(0.9, 1.2, 0.03), Palette.FRAME))
            return Motion(
                poses, wall, Rigging { _, _ -> emptyList() },
                if (walk) Kit.flow("setup", "grip", listOf("plank", "mid", "top", "mid"), Focus.SHOULDERS, listOf(1.4, 1.4, 1.4, 1.4),
                    setupNote = "Start in a push-up with the feet against the wall.",
                    gripNote = "Walk the feet up the wall and the hands in towards it, body tight, until nearly vertical.",
                    startFocus = Focus.CORE, cameraYaw = 80 * DEG)
                else Kit.steps("setup", "grip", "top", "bottom", Focus.SHOULDERS,
                    setupNote = "Kick up into a handstand with the heels resting on the wall.",
                    gripNote = "Hands a little wider than the shoulders, fingers spread. Lower the head to the floor between the hands.",
                    startFocus = Focus.CORE, upSeconds = 1.4, downSeconds = 2.0, cameraYaw = 80 * DEG),
                Camera(yaw = 80 * DEG, pitch = 10 * DEG, distance = 3.6, target = v(0.0, 0.95, 0.0))
            )
        }
        if (spec.has("pike")) return pike(spec)
        return MovesPress.pushUp(spec)
    }

    /** Pike push-up: hips high in an upside-down V, the head lowering between the hands. */
    private fun pike(spec: Spec): Motion {
        // Torso 30 degrees below level and legs 60 degrees: a right angle at the hips.
        val pitch = 120 * DEG
        val feetUp = spec.has("feet elevated", "elevated", "box")
        val ankleY = if (feetUp) 0.5 else Kit.ANKLE
        val handZ = 0.2
        val leg = Body.THIGH + Body.SHIN - 0.03
        fun at(shoulders: Vec3, pitchNow: Double): Pose {
            val up = Basis.of(0.0, pitchNow).up * (Body.SPINE - 0.05)
            val pelvis = shoulders - up
            val dy = (pelvis.y - ankleY).coerceAtMost(leg - 0.01)
            val ankleZ = pelvis.z - kotlin.math.sqrt(leg * leg - dy * dy)
            return Pose(
                pelvis = pelvis, pitch = pitchNow, head = 0.3,
                handL = v(0.24, 0.03, handZ), handR = v(-0.24, 0.03, handZ),
                footL = v(0.1, ankleY, ankleZ), footR = v(-0.1, ankleY, ankleZ),
                elbowPole = v(0.5, 0.0, -1.0), kneePole = v(0.0, 0.0, 1.0), footPitch = 50 * DEG
            )
        }
        val top = at(v(0.0, 0.03 + Kit.ARM_REACH - 0.04, handZ - 0.06), pitch)
        val bottom = at(v(0.0, 0.3, handZ - 0.12), pitch + 8 * DEG)
        val fixed = if (feetUp) listOf(Gear.block(v(0.0, (ankleY - 0.1) / 2, top.footL.z - 0.05), v(0.22, (ankleY - 0.1) / 2, 0.2))) else emptyList()
        return Motion(
            mapOf("setup" to top, "grip" to top, "top" to top, "bottom" to bottom),
            fixed, Rigging { _, _ -> emptyList() },
            Kit.steps("setup", "grip", "top", "bottom", Focus.SHOULDERS,
                setupNote = "From a push-up, walk the feet in and lift the hips high so the body makes an upside-down V.",
                gripNote = "Hands shoulder width, fingers spread. Lower the top of the head towards the floor just in front of the hands.",
                startFocus = Focus.CORE, upSeconds = 1.2, downSeconds = 2.0, cameraYaw = 80 * DEG),
            Camera(yaw = 80 * DEG, pitch = 10 * DEG, distance = 3.4, target = v(0.0, 0.55, -0.1))
        )
    }
}
