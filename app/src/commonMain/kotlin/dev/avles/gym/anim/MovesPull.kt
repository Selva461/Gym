package dev.avles.gym.anim

/** Pulls: rows, face pulls, rear-delt flyes, pull-ups, pulldowns and pullovers. */
object MovesPull {

    private fun handPair(half: Double, p: Vec3) = Pair(v(p.x + half, p.y, p.z), v(p.x - half, p.y, p.z))

    /** Rows of every kind. */
    fun row(spec: Spec): Motion = when {
        spec.has("face pull") || (spec.gear == "CABLE" && spec.has("high row")) -> facePull(spec)
        spec.gear == "CABLE" && !spec.has("bent-over", "standing") -> cableRow(spec)
        spec.has("inverted", "ring row", "bodyweight row") -> invertedRow(spec)
        spec.has("rear delt", "reverse fly", "reverse pec deck", "reverse flye") -> rearDelt(spec)
        spec.has("upright row") -> MovesArms.lateral(spec)
        spec.has("chest-supported", "seal row", "incline dumbbell row", "prone") || spec.gear == "MACHINE" -> supportedRow(spec)
        spec.oneArm || (spec.gear == "DUMBBELL" && spec.has("single-arm", "one-arm", "kroc")) -> oneArmRow(spec)
        else -> bentRow(spec)
    }

    private fun bentRow(spec: Spec): Motion {
        val pitch = when {
            spec.has("pendlay", "seal") -> 82 * DEG
            spec.has("yates") -> 32 * DEG
            else -> 52 * DEG
        }
        val pelvis = v(0.0, 0.84, -0.12)
        val half = when {
            spec.gear == "DUMBBELL" || spec.gear == "KETTLEBELL" -> 0.2
            spec.has("t-bar", "landmine", "meadows") -> 0.06
            else -> spec.gripHalf
        }
        val sh = Kit.onTorso(pelvis, pitch, v(0.0, Body.SPINE - 0.05, 0.0))
        val bottomHand = v(0.0, sh.y - 0.58, sh.z + 0.04)
        val topHand = Kit.onTorso(pelvis, pitch, v(0.0, Body.SPINE * 0.38, 0.2))
        val base = Pose(
            pelvis = pelvis, pitch = pitch, head = -pitch * 0.55,
            handL = v(0.2, 0.8, 0.1), handR = v(-0.2, 0.8, 0.1),
            footL = v(0.15, Kit.ANKLE, 0.0), footR = v(-0.15, Kit.ANKLE, 0.0),
            elbowPole = v(if (half > 0.3) 0.55 else 0.2, 0.3, -1.0), kneePole = v(0.1, 0.0, 1.0)
        )
        val bottom = base.copy(handL = handPair(half, bottomHand).first, handR = handPair(half, bottomHand).second)
        val top = base.copy(handL = handPair(half, topHand).first, handR = handPair(half, topHand).second)
        val stand = Kit.stand(0.15).let {
            val y = Kit.hangY(Kit.shoulder(it.pelvis, 0.0, 1.0), half, 0.1)
            it.copy(handL = v(half, y, 0.1), handR = v(-half, y, 0.1))
        }
        val moving = Rigging { s, _ ->
            when (spec.gear) {
                "DUMBBELL" -> Gear.dumbbell(s.handL, Vec3.Z) + Gear.dumbbell(s.handR, Vec3.Z)
                "KETTLEBELL" -> Gear.kettlebell(s.handL) + Gear.kettlebell(s.handR)
                else -> if (spec.has("t-bar", "landmine", "meadows")) {
                    val mid = (s.handL + s.handR) * 0.5
                    listOf(Prim.Cylinder(mid + v(0.0, 0.0, 0.25), v(0.0, 0.05, -1.4), 0.025, Palette.STEEL)) +
                        Gear.plate(mid + v(0.0, 0.0, 0.32), Vec3.Z)
                } else Gear.barbell(s.handL, s.handR)
            }
        }
        return Motion(
            mapOf("setup" to stand, "grip" to bottom, "top" to top, "bottom" to bottom),
            emptyList(), moving,
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = "Feet hip width, soft knees, push the hips back until the chest is over the bar. Back flat.",
                gripNote = Kit.gripNote(spec, if (spec.gear == "DUMBBELL") "the dumbbells" else "the bar"),
                startFocus = Focus.BACK, upSeconds = 1.6, downSeconds = 1.0, cameraYaw = 80 * DEG
            ),
            Camera(yaw = 70 * DEG, pitch = 10 * DEG, distance = 3.4, target = v(0.0, 0.85, 0.0))
        )
    }

    private fun oneArmRow(spec: Spec): Motion {
        // Left knee and hand on the bench, right foot on the floor, right arm rows.
        val pitch = 78 * DEG
        val pelvis = v(-0.05, 0.78, -0.2)
        val sh = Kit.shoulder(pelvis, pitch, -1.0)
        val base = Pose(
            pelvis = pelvis, pitch = pitch, head = -0.6,
            handL = v(0.2, Gear.BENCH_TOP + 0.03, 0.32), handR = v(sh.x, sh.y - 0.58, sh.z + 0.04),
            footL = v(0.14, Gear.BENCH_TOP + 0.08, -0.45), footR = v(-0.3, Kit.ANKLE, -0.1),
            elbowPole = v(0.1, 0.0, -1.0), kneePole = v(0.0, -1.0, 0.6), kneePoleR = v(0.1, 0.0, 1.0),
            footPitch = 0.0
        )
        val top = base.copy(handR = Kit.onTorso(pelvis, pitch, v(-0.2, Body.SPINE * 0.3, 0.15)), elbowPoleR = v(0.1, 0.3, -1.0))
        val bench = Gear.bench(0.0, -0.3).map {
            if (it is Prim.Box) it.copy(c = it.c + v(0.2, 0.0, 0.0)) else it
        }
        return Motion(
            mapOf("setup" to base, "grip" to base, "bottom" to base, "top" to top),
            bench,
            Rigging { s, _ -> if (spec.gear == "KETTLEBELL") Gear.kettlebell(s.handR) else Gear.dumbbell(s.handR, Vec3.Z) },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = "One knee and the same-side hand on the bench, other foot planted wide on the floor. Back flat like a table.",
                gripNote = "Neutral grip, palm facing the bench, arm hanging straight under the shoulder.",
                startFocus = Focus.BACK, upSeconds = 1.6, downSeconds = 1.0, cameraYaw = -70 * DEG
            ),
            Camera(yaw = -60 * DEG, pitch = 12 * DEG, distance = 3.2, target = v(0.0, 0.7, 0.0))
        )
    }

    private fun supportedRow(spec: Spec): Motion {
        val machine = spec.gear == "MACHINE" && !spec.has("t-bar")
        if (machine) {
            val seat = MovesSeated.seated(-0.2, lean = 8.0)
            val half = if (spec.has("wide")) 0.4 else 0.22
            val reach = seat.copy(handL = v(half, 1.05, 0.48), handR = v(-half, 1.05, 0.48), elbowPole = v(0.3, -0.3, -1.0))
            val pulled = reach.copy(handL = v(half, 1.02, 0.12), handR = v(-half, 1.02, 0.12))
            val pad = listOf(
                Prim.Box(v(0.0, 1.05, 0.3), Basis.IDENTITY, v(0.16, 0.18, 0.04), Palette.PAD),
                Prim.Box(v(0.0, 0.55, 0.3), Basis.IDENTITY, v(0.04, 0.5, 0.04), Palette.FRAME)
            )
            return Motion(
                mapOf("setup" to reach, "grip" to reach, "bottom" to reach, "top" to pulled),
                Gear.machineSeat(Gear.BENCH_TOP, 90.0, -0.25).take(1) + pad + Gear.machineSeat(Gear.BENCH_TOP, 90.0, -0.25).drop(2),
                Rigging { s, _ -> listOf(s.handL, s.handR).map { h -> Prim.Capsule(h, h + v(0.0, 0.0, 0.3), 0.02, 0.02, Palette.STEEL) } },
                Kit.steps(
                    "setup", "grip", "bottom", "top", Focus.ELBOWS,
                    setupNote = "Seat height so the handles are level with the lower chest. Chest on the pad.",
                    gripNote = Kit.gripNote(spec, "the handles"),
                    upSeconds = 1.6, downSeconds = 1.0, cameraYaw = 80 * DEG
                ),
                Camera(yaw = 70 * DEG, pitch = 10 * DEG, distance = 3.2, target = v(0.0, 0.8, 0.0))
            )
        }
        val angle = if (spec.has("seal")) 0.0 else 35.0
        val pitch = (90 - angle) * DEG
        val padY = if (spec.has("seal")) 0.75 else Gear.BENCH_TOP
        val chestPelvis = if (angle > 0) v(0.0, 0.72, -0.12) else v(0.0, padY + 0.13, -0.3)
        val sh = Kit.onTorso(chestPelvis, pitch, v(0.0, Body.SPINE - 0.05, 0.0))
        val half = if (spec.gear == "BARBELL") spec.gripHalf else 0.2
        val base = Pose(
            pelvis = chestPelvis, pitch = pitch, head = -0.3,
            handL = v(half, sh.y - 0.58, sh.z + 0.02), handR = v(-half, sh.y - 0.58, sh.z + 0.02),
            footL = v(0.18, Kit.ANKLE, -0.6), footR = v(-0.18, Kit.ANKLE, -0.6),
            elbowPole = v(0.35, 0.2, -1.0), kneePole = v(0.1, 0.0, 1.0)
        )
        val top = base.copy(
            handL = Kit.onTorso(chestPelvis, pitch, v(half, Body.SPINE * 0.38, 0.22)),
            handR = Kit.onTorso(chestPelvis, pitch, v(-half, Body.SPINE * 0.38, 0.22))
        )
        val chestPad = Basis.of(0.0, pitch)
        val bench = if (angle > 0) Gear.bench(0.0, -0.1, back = false) + listOf(
            Prim.Box(
                Kit.onTorso(chestPelvis, pitch, v(0.0, 0.3, -0.17)), Basis(Vec3.X, -chestPad.fwd, chestPad.up),
                v(0.15, 0.025, 0.4), Palette.PAD
            )
        ) else listOf(
            Prim.Box(v(0.0, padY - 0.02, -0.1), Basis.IDENTITY, v(0.15, 0.03, 0.55), Palette.PAD),
            Prim.Box(v(0.0, padY / 2, 0.3), Basis.IDENTITY, v(0.04, padY / 2, 0.04), Palette.FRAME),
            Prim.Box(v(0.0, padY / 2, -0.5), Basis.IDENTITY, v(0.04, padY / 2, 0.04), Palette.FRAME)
        )
        val seal = angle == 0.0
        val poses = if (seal) {
            val lying = base.copy(footL = v(0.15, padY + 0.05, -1.0), footR = v(-0.15, padY + 0.05, -1.0), kneePole = v(0.0, 0.0, -1.0))
            mapOf("setup" to lying, "grip" to lying, "bottom" to lying,
                "top" to lying.copy(handL = top.handL, handR = top.handR))
        } else mapOf("setup" to base, "grip" to base, "bottom" to base, "top" to top)
        return Motion(
            poses, bench,
            Rigging { s, _ ->
                if (spec.gear == "BARBELL") Gear.barbell(s.handL, s.handR)
                else Gear.dumbbell(s.handL, Vec3.Z) + Gear.dumbbell(s.handR, Vec3.Z)
            },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = "Chest on the pad, toes on the floor, neck long — the bench takes your lower back out of it.",
                gripNote = Kit.gripNote(spec, if (spec.gear == "BARBELL") "the bar" else "the dumbbells"),
                upSeconds = 1.6, downSeconds = 1.0, cameraYaw = 80 * DEG
            ),
            Camera(yaw = 70 * DEG, pitch = 12 * DEG, distance = 3.2, target = v(0.0, 0.7, 0.0))
        )
    }

    private fun cableRow(spec: Spec): Motion {
        val single = spec.oneArm
        val half = when {
            spec.has("wide") -> 0.4
            spec.has("rope") -> 0.12
            else -> 0.1
        }
        val seat = Pose(
            pelvis = v(0.0, Gear.BENCH_TOP + 0.1, -0.2), pitch = 0.0,
            handL = v(half, 0.82, 0.48), handR = v(-half, 0.82, 0.48),
            footL = v(0.15, 0.25, 0.48), footR = v(-0.15, 0.25, 0.48),
            elbowPole = v(0.2, -0.3, -1.0), kneePole = v(0.1, 0.5, 1.0)
        )
        val reach = seat.copy(pitch = 25 * DEG, handL = v(half, 0.8, 0.5), handR = v(-half, 0.8, 0.5))
        val pulled = seat.copy(pitch = -4 * DEG, handL = v(half, 0.8, 0.02), handR = v(-half, 0.8, 0.02))
        val r = if (single) reach.copy(handL = v(0.26, 0.75, 0.2)) else reach
        val p = if (single) pulled.copy(handL = v(0.26, 0.75, 0.2), twist = -0.25) else pulled
        val (tower, pulley) = Gear.cableTower(v(0.0, 0.0, 1.15), 0.32)
        val fixed = tower + Gear.bench(0.0, -0.45, back = false) + Gear.block(v(0.0, 0.2, 0.55), v(0.25, 0.04, 0.05))
        return Motion(
            mapOf("setup" to r, "grip" to r, "bottom" to r, "top" to p),
            fixed,
            Rigging { s, _ ->
                if (single) Gear.cable(pulley, s.handR)
                else Gear.cable(pulley, (s.handL + s.handR) * 0.5) + listOf(Prim.Capsule(s.handL, s.handR, 0.018, 0.018, Palette.STEEL))
            },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = "Feet on the platform, knees soft, sit tall with the chest up.",
                gripNote = Kit.gripNote(spec, "the handle"),
                upSeconds = 1.6, downSeconds = 1.0, cameraYaw = 80 * DEG
            ),
            Camera(yaw = 70 * DEG, pitch = 12 * DEG, distance = 3.4, target = v(0.0, 0.65, 0.3))
        )
    }

    private fun invertedRow(spec: Spec): Motion {
        val barY = 0.95
        val ankle = v(0.0, Kit.ANKLE, 1.0)
        val bottom = supine(ankle, v(0.0, barY - 0.58, -0.04), barY)
        val top = supine(ankle, v(0.0, barY - 0.16, 0.08), barY)
        val fixed = if (spec.has("ring")) listOf(
            Gear.band(v(0.3, 2.2, 0.0), v(0.3, barY, 0.0)), Gear.band(v(-0.3, 2.2, 0.0), v(-0.3, barY, 0.0))
        ) else Gear.rack(0.0, barY, 0.62, 1.5) + listOf(Prim.Cylinder(v(-0.7, barY, 0.0), v(0.7, barY, 0.0), 0.016, Palette.STEEL))
        return Motion(
            mapOf("setup" to bottom, "grip" to bottom, "bottom" to bottom, "top" to top),
            fixed, Rigging { _, _ -> emptyList() },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = "Lie under the bar, heels on the floor, body straight like a plank facing up.",
                gripNote = Kit.gripNote(spec),
                upSeconds = 1.6, downSeconds = 1.0, cameraYaw = 90 * DEG
            ),
            Camera(yaw = 80 * DEG, pitch = 10 * DEG, distance = 3.4, target = v(0.0, 0.6, 0.3))
        )
    }

    /** Face up, straight from the heels to the shoulders, hands on a bar above. */
    fun supine(ankle: Vec3, shoulders: Vec3, barY: Double): Pose {
        val dir = (shoulders - ankle).normalized()
        // Spine points from pelvis towards the shoulders; chest faces up.
        val pitch = kotlin.math.atan2(dir.z, dir.y)
        val pelvis = Kit.pelvisFor(shoulders, pitch)
        return Pose(
            pelvis = pelvis, pitch = pitch, head = 0.2,
            handL = v(0.3, barY, shoulders.z + 0.02), handR = v(-0.3, barY, shoulders.z + 0.02),
            footL = v(0.12, ankle.y, ankle.z), footR = v(-0.12, ankle.y, ankle.z),
            elbowPole = v(0.5, 0.3, -1.0), kneePole = v(0.0, 0.0, 1.0), footYaw = kotlin.math.PI
        )
    }

    private fun facePull(spec: Spec): Motion {
        val base = Kit.stand(0.16).copy(pelvis = v(0.0, Body.STAND - 0.02, -0.05), pitch = -6 * DEG)
        // A high row is the same pull from a high pulley, finishing at the chest with the elbows lower.
        val toChest = spec.has("high row")
        val reach = base.copy(handL = v(0.08, 1.5, 0.45), handR = v(-0.08, 1.5, 0.45), elbowPole = v(0.6, 0.6, -0.2))
        val pulled = if (toChest) base.copy(handL = v(0.2, 1.3, 0.2), handR = v(-0.2, 1.3, 0.2), elbowPole = v(1.0, -0.4, -0.6))
        else base.copy(handL = v(0.3, 1.58, 0.12), handR = v(-0.3, 1.58, 0.12), elbowPole = v(1.0, 0.3, -0.6))
        val (tower, pulley) = Gear.cableTower(v(0.0, 0.0, 1.2), 1.62)
        return Motion(
            mapOf("setup" to reach, "grip" to reach, "bottom" to reach, "top" to pulled),
            tower,
            Rigging { s, _ -> listOf(Prim.Line(pulley, s.handL, 0.01, Palette.CABLE), Prim.Line(pulley, s.handR, 0.01, Palette.CABLE)) },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = if (toChest) "Pulley set high. Step back until the weight lifts, feet hip width, chest up."
                else "Pulley at forehead height. Step back until the weight lifts, feet hip width, slight lean back.",
                gripNote = when {
                    toChest -> "Handles or a bar, palms down, arms reaching up and forward. Pull to the upper chest, shoulder blades together."
                    spec.has("band") -> "Band in both hands, palms facing each other."
                    else -> "Rope with thumbs pointing back at you, palms facing each other."
                },
                upSeconds = 1.6, downSeconds = 1.0, cameraYaw = 50 * DEG
            ),
            Camera(yaw = 40 * DEG, pitch = 10 * DEG, distance = 3.6, target = v(0.0, 1.1, 0.3))
        )
    }

    fun rearDelt(spec: Spec): Motion {
        val machine = spec.gear == "MACHINE" || spec.has("pec deck")
        if (machine) {
            val seat = MovesSeated.seated(-0.05, lean = 6.0)
            val front = seat.copy(handL = v(0.1, 1.3, 0.55), handR = v(-0.1, 1.3, 0.55), elbowPole = v(1.0, 0.0, -0.3))
            val open = seat.copy(handL = v(0.72, 1.3, 0.12), handR = v(-0.72, 1.3, 0.12), elbowPole = v(0.2, 1.0, -0.5))
            return Motion(
                mapOf("setup" to front, "grip" to front, "bottom" to front, "top" to open),
                listOf(
                    Prim.Box(v(0.0, 1.15, 0.3), Basis.IDENTITY, v(0.16, 0.25, 0.04), Palette.PAD),
                    Prim.Box(v(0.0, 1.0, 0.62), Basis.IDENTITY, v(0.05, 1.0, 0.05), Palette.FRAME)
                ) + Gear.machineSeat(Gear.BENCH_TOP, 90.0, -0.05).take(1) + Gear.machineSeat(Gear.BENCH_TOP, 90.0, -0.05).drop(2).dropLast(1),
                Rigging { s, _ -> listOf(s.handL, s.handR).map { h -> Prim.Capsule(h - v(0.0, 0.08, 0.0), h + v(0.0, 0.08, 0.0), 0.02, 0.02, Palette.STEEL) } },
                Kit.steps(
                    "setup", "grip", "bottom", "top", Focus.SHOULDERS,
                    setupNote = "Face the pad, chest against it, handles at shoulder height.",
                    gripNote = "Neutral grip on the handles, arms long with a slight bend in the elbows.",
                    upSeconds = 1.6, downSeconds = 1.0
                ),
                Camera(yaw = 160 * DEG, pitch = 18 * DEG, distance = 3.2, target = v(0.0, 1.0, 0.0))
            )
        }
        val pitch = 70 * DEG
        val pelvis = v(0.0, 0.82, -0.14)
        val sh = Kit.onTorso(pelvis, pitch, v(0.0, Body.SPINE - 0.05, 0.0))
        val base = Pose(
            pelvis = pelvis, pitch = pitch, head = -0.5,
            handL = v(0.1, sh.y - 0.57, sh.z + 0.06), handR = v(-0.1, sh.y - 0.57, sh.z + 0.06),
            footL = v(0.15, Kit.ANKLE, 0.0), footR = v(-0.15, Kit.ANKLE, 0.0),
            elbowPole = v(1.0, 0.0, -0.3), kneePole = v(0.1, 0.0, 1.0)
        )
        val open = base.copy(handL = v(0.68, sh.y - 0.12, sh.z), handR = v(-0.68, sh.y - 0.12, sh.z), elbowPole = v(0.3, 0.2, -1.0))
        val cable = spec.gear == "CABLE"
        val towers = if (cable) Gear.cableTower(v(1.2, 0.0, 0.3), 0.3) else null
        val towersR = if (cable) Gear.cableTower(v(-1.2, 0.0, 0.3), 0.3) else null
        return Motion(
            mapOf("setup" to base, "grip" to base, "bottom" to base, "top" to open),
            (towers?.first ?: emptyList()) + (towersR?.first ?: emptyList()),
            Rigging { s, _ ->
                if (cable) listOf(Prim.Line(towersR!!.second, s.handL, 0.008, Palette.CABLE), Prim.Line(towers!!.second, s.handR, 0.008, Palette.CABLE))
                else Gear.dumbbell(s.handL, Vec3.Z) + Gear.dumbbell(s.handR, Vec3.Z)
            },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.SHOULDERS,
                setupNote = "Hinge forward until the chest is nearly parallel to the floor, knees soft, back flat.",
                gripNote = "Neutral grip, palms facing each other, a slight bend in the elbows that stays fixed.",
                upSeconds = 1.6, downSeconds = 1.0
            ),
            Camera(yaw = 20 * DEG, pitch = 18 * DEG, distance = 3.4, target = v(0.0, 0.9, 0.0))
        )
    }

    /** Pull-ups and chin-ups, weighted, assisted or on a machine. */
    fun pullUp(spec: Spec): Motion {
        val barY = 2.25
        val half = when {
            spec.has("chin-up", "chinup", "close") -> 0.2
            spec.has("neutral") -> 0.25
            spec.has("wide") -> 0.52
            else -> 0.38
        }
        fun hang(shY: Double, lean: Double): Pose {
            val sh = v(0.0, shY, -0.04)
            val pelvis = Kit.pelvisFor(sh, lean)
            return Pose(
                pelvis = pelvis, pitch = lean, head = 0.15,
                handL = v(half, barY, 0.0), handR = v(-half, barY, 0.0),
                footL = pelvis + v(0.1, -0.78, -0.22), footR = pelvis + v(-0.1, -0.78, -0.22),
                elbowPole = v(0.8, -0.2, -0.3), kneePole = v(0.0, -0.3, 1.0), footPitch = 40 * DEG
            )
        }
        val bottom = hang(barY - kotlin.math.sqrt(0.6 * 0.6 - (half - Body.SHOULDER_HALF) * (half - Body.SHOULDER_HALF)), -4 * DEG)
        val top = hang(barY - 0.14, -14 * DEG)
        val assisted = spec.has("assisted")
        if (spec.has("muscle-up")) return muscleUp(barY, half, bottom, top)
        return Motion(
            mapOf("setup" to bottom, "grip" to bottom, "bottom" to bottom, "top" to top),
            Gear.pullUpBar(barY) + if (assisted) listOf(Prim.Box(v(0.0, 0.9, -0.18), Basis.IDENTITY, v(0.22, 0.03, 0.15), Palette.PAD)) else emptyList(),
            Rigging { s, _ -> if (spec.has("weighted")) Gear.dumbbell((s.kneeL + s.kneeR) * 0.5 - v(0.0, 0.1, 0.0), Vec3.X) else emptyList() },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = "Hang at arm's length, legs together or ankles crossed, ribs down so you do not swing.",
                gripNote = Kit.gripNote(spec),
                startFocus = Focus.SHOULDERS, upSeconds = 1.8, downSeconds = 1.0, cameraYaw = 30 * DEG
            ),
            Camera(yaw = 30 * DEG, pitch = 6 * DEG, distance = 4.2, target = v(0.0, 1.35, 0.0))
        )
    }

    /** Muscle-up: a pull-up that carries on over the bar into a straight-arm support. */
    private fun muscleUp(barY: Double, half: Double, bottom: Pose, top: Pose): Motion {
        fun over(shoulders: Vec3, lean: Double, feet: Vec3, elbowPole: Vec3): Pose {
            val pelvis = Kit.pelvisFor(shoulders, lean)
            return top.copy(
                pelvis = pelvis, pitch = lean, head = 0.0,
                footL = pelvis + feet + v(0.1, 0.0, 0.0), footR = pelvis + feet - v(0.1, 0.0, 0.0),
                elbowPole = elbowPole, footPitch = 30 * DEG
            )
        }
        // Chest over the bar with the elbows bent behind, then pressed out to straight arms.
        val turn = over(v(0.0, barY + 0.12, 0.08), 35 * DEG, v(0.0, -0.75, 0.25), v(0.4, 0.2, -1.0))
        val support = over(v(0.0, barY + 0.5, -0.04), 10 * DEG, v(0.0, -0.82, 0.08), v(0.4, 0.0, -1.0))
        val rep = listOf("bottom", "top", "turn", "support", "turn", "bottom")
        return Motion(
            mapOf("setup" to bottom, "grip" to bottom, "bottom" to bottom, "top" to top, "turn" to turn, "support" to support),
            Gear.pullUpBar(barY),
            Rigging { _, _ -> emptyList() },
            Kit.flow(
                "setup", "grip", rep, Focus.ELBOWS, listOf(0.7, 0.4, 0.6, 1.0, 1.2, 0.8),
                setupNote = "Hang from the bar with a false grip if you have one, the wrist over the bar.",
                gripNote = "Hands about shoulder width. Pull explosively to the lower chest, then drive the chest over the bar and press to straight arms.",
                startFocus = Focus.SHOULDERS, cameraYaw = 80 * DEG
            ),
            Camera(yaw = 70 * DEG, pitch = 6 * DEG, distance = 4.4, target = v(0.0, 1.6, 0.0))
        )
    }

    /** Lat pulldowns: seated on the machine, kneeling at a cable, or single-arm. */
    fun pulldown(spec: Spec): Motion {
        if (spec.has("straight-arm", "pullover")) return pullover(spec)
        if (spec.has("pull-up", "pullup", "chin-up", "chinup", "muscle-up")) return pullUp(spec)
        val kneeling = spec.has("kneeling")
        val half = when {
            spec.has("close", "v-bar", "neutral") -> 0.12
            spec.has("wide", "behind") -> 0.5
            spec.has("reverse", "underhand") -> 0.2
            else -> 0.4
        }
        val single = spec.oneArm
        val seat = if (kneeling) Pose(
            pelvis = v(0.0, 0.62, -0.05), handL = v(0.0, 0.0, 0.0), handR = v(0.0, 0.0, 0.0),
            footL = v(0.15, 0.1, -0.45), footR = v(-0.15, 0.1, -0.45), kneePole = v(0.0, -1.0, 0.3), footPitch = 0.0
        ) else MovesSeated.seated(-0.1, seatY = 0.5, lean = -8.0)
        val behind = spec.has("behind")
        val shY = Kit.shoulder(seat.pelvis, seat.pitch, 1.0).y
        // Arms long overhead: as high as the arm reaches for this grip width.
        val barY = shY + kotlin.math.sqrt(0.6 * 0.6 - (half - Body.SHOULDER_HALF) * (half - Body.SHOULDER_HALF))
        val top = seat.copy(
            handL = v(half, barY, 0.08), handR = v(if (single) -0.25 else -half, if (single) shY - 0.25 else barY, if (single) 0.2 else 0.08),
            elbowPole = v(0.8, 0.0, -0.3)
        )
        val bottomY = if (behind) shY + 0.1 else shY - 0.04
        val bottomZ = if (behind) -0.12 else 0.16
        val bottom = top.copy(
            handL = v(half + 0.05, bottomY, bottomZ),
            handR = if (single) top.handR else v(-half - 0.05, bottomY, bottomZ),
            elbowPole = v(0.7, -0.4, -0.6), head = if (behind) -0.3 else 0.0
        )
        val pulley = v(0.0, 2.4, 0.15)
        val fixed = mutableListOf<Prim>()
        if (!kneeling) {
            fixed += Gear.machineSeat(0.5, 90.0, -0.1).take(1)
            fixed += Prim.Box(v(0.0, 0.78, 0.35), Basis.IDENTITY, v(0.22, 0.04, 0.05), Palette.PAD) // knee pad
            fixed += Prim.Box(v(0.0, 1.2, -0.45), Basis.IDENTITY, v(0.05, 1.2, 0.05), Palette.FRAME)
        }
        fixed += Prim.Box(v(0.0, 2.42, -0.15), Basis.IDENTITY, v(0.05, 0.03, 0.3), Palette.FRAME)
        fixed += Prim.Ball(pulley, 0.05, Palette.STEEL)
        return Motion(
            mapOf("setup" to top, "grip" to top, "bottom" to top, "top" to bottom),
            fixed,
            Rigging { s, _ ->
                if (single) Gear.cable(pulley, s.handL)
                else {
                    val mid = (s.handL + s.handR) * 0.5
                    listOf(Prim.Line(pulley, mid, 0.008, Palette.CABLE)) +
                        if (half < 0.15) listOf(Prim.Capsule(s.handL, s.handR, 0.02, 0.02, Palette.STEEL))
                        else listOf(Prim.Cylinder(s.handL + (s.handL - s.handR).normalized() * 0.15, s.handR - (s.handL - s.handR).normalized() * 0.15, 0.015, Palette.STEEL))
                }
            },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = if (kneeling) "Kneel facing the cable, hips over knees, arms reaching up."
                else "Thighs locked under the pad, feet flat, sit tall with a slight lean back.",
                gripNote = Kit.gripNote(spec),
                startFocus = Focus.SHOULDERS, upSeconds = 1.8, downSeconds = 1.0, cameraYaw = 35 * DEG
            ),
            Camera(yaw = 35 * DEG, pitch = 10 * DEG, distance = 3.8, target = v(0.0, 1.25, 0.0))
        )
    }

    /** Pullovers: lying with a dumbbell, or the standing straight-arm cable version. */
    fun pullover(spec: Spec): Motion {
        if (spec.gear == "CABLE" || spec.has("straight-arm")) {
            val pelvis = v(0.0, Body.STAND - 0.03, -0.1)
            val pitch = 22 * DEG
            val base = Kit.stand(0.15).copy(pelvis = pelvis, pitch = pitch, footL = v(0.15, Kit.ANKLE, -0.05), footR = v(-0.15, Kit.ANKLE, -0.05))
            val half = if (spec.has("rope")) 0.12 else 0.3
            val up = base.copy(handL = v(half, 1.72, 0.55), handR = v(-half, 1.72, 0.55), elbowPole = v(0.5, 0.0, -1.0))
            val down = base.copy(handL = v(half, 0.9, 0.3), handR = v(-half, 0.9, 0.3), elbowPole = v(0.6, 0.0, -1.0))
            val (tower, pulley) = Gear.cableTower(v(0.0, 0.0, 1.0), 2.0)
            return Motion(
                mapOf("setup" to up, "grip" to up, "bottom" to up, "top" to down),
                tower,
                Rigging { s, _ -> listOf(Prim.Line(pulley, (s.handL + s.handR) * 0.5, 0.008, Palette.CABLE), Prim.Capsule(s.handL, s.handR, 0.016, 0.016, Palette.STEEL)) },
                Kit.steps(
                    "setup", "grip", "bottom", "top", Focus.SHOULDERS,
                    setupNote = "Step back from a high pulley, hinge slightly, arms long and reaching up to it.",
                    gripNote = "Overhand on the bar or the rope, elbows locked with a slight bend.",
                    upSeconds = 1.6, downSeconds = 1.0, cameraYaw = 80 * DEG
                ),
                Camera(yaw = 75 * DEG, pitch = 10 * DEG, distance = 3.6, target = v(0.0, 1.1, 0.3))
            )
        }
        val pelvis = v(0.0, Gear.BENCH_TOP + 0.12, 0.12)
        val pitch = -90 * DEG
        val sh = Kit.shoulder(pelvis, pitch, 1.0)
        val over = Pose(
            pelvis = pelvis, pitch = pitch,
            handL = v(0.05, sh.y + 0.58, sh.z + 0.05), handR = v(-0.05, sh.y + 0.58, sh.z + 0.05),
            footL = v(0.3, Kit.ANKLE, 0.62), footR = v(-0.3, Kit.ANKLE, 0.62),
            elbowPole = v(0.6, 0.0, 1.0), kneePole = v(0.25, 0.0, 1.0)
        )
        val back = over.copy(handL = v(0.05, sh.y + 0.08, sh.z - 0.55), handR = v(-0.05, sh.y + 0.08, sh.z - 0.55))
        return Motion(
            mapOf("setup" to over, "grip" to over, "bottom" to over, "top" to back),
            Gear.bench(0.0, 0.15),
            Rigging { s, _ -> Gear.dumbbell((s.handL + s.handR) * 0.5 + v(0.0, 0.05, 0.0), (s.handL - s.elbowL).normalized()) },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.SHOULDERS,
                setupNote = "Lie on the bench, feet planted, the dumbbell held over the chest.",
                gripNote = "Both palms flat under the top plate of the dumbbell, thumbs wrapped around the handle.",
                upSeconds = 1.6, downSeconds = 2.0, cameraYaw = 80 * DEG
            ),
            Camera(yaw = 75 * DEG, pitch = 16 * DEG, distance = 3.2, target = v(0.0, 0.7, 0.0))
        )
    }
}
