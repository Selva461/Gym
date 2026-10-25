package dev.avles.gym.anim

import dev.avles.gym.anim.Kit.pair

/** Shoulder raises, upright rows, flyes, shrugs, grip work and carries. */
object MovesArms {

    private const val SH_Y = Body.STAND + Body.SPINE - 0.05

    /** Patterns loaded light enough that the bar carries small plates. */
    private val ARM_WORK = setOf("CURL", "TRICEPS_EXTENSION", "WRIST", "FRONT_RAISE", "LATERAL_RAISE")

    /** What the hands hold, drawn at the hands every frame. Dumbbell handles lie along [axis]. */
    internal fun held(spec: Spec, axis: Vec3 = Vec3.Z, pulleys: List<Vec3> = emptyList(), oneHand: Boolean = false) = Rigging { s, _ ->
        val hands = if (oneHand) listOf(s.handR) else listOf(s.handL, s.handR)
        val mid = (s.handL + s.handR) * 0.5
        when (spec.gear) {
            "BARBELL", "EZ_BAR", "SMITH" -> if (oneHand) Gear.dumbbell(s.handR, axis) else Gear.barbell(
                s.handL, s.handR, if (spec.gear == "EZ_BAR") 1.25 else 2.0, if (spec.gear == "EZ_BAR") 1 else 2,
                if (spec.pattern in ARM_WORK) Gear.SMALL_PLATE_R else Gear.PLATE_R
            )
            "TRAP_BAR" -> Gear.trapBar(s.handL, s.handR)
            "KETTLEBELL" -> hands.flatMap { Gear.kettlebell(it) }
            "PLATE" -> Gear.plate(mid, Vec3.Z)
            "CABLE" -> {
                val ps = if (pulleys.isEmpty()) listOf(v(0.0, 0.1, 0.6)) else pulleys
                if (ps.size == 1 && !oneHand) Gear.cable(ps[0], mid)
                else hands.zip(if (ps.size == 1) List(hands.size) { ps[0] } else ps).flatMap { (h, p) -> Gear.cable(p, h) }
            }
            "BAND" -> hands.map { h -> Gear.band(v(h.x * 0.6, 0.02, 0.05), h) }
            "BODYWEIGHT", "OTHER", "MACHINE" -> emptyList()
            else -> hands.flatMap { Gear.dumbbell(it, axis) }
        }
    }

    private fun standing(stance: Double = 0.15) = Kit.stand(stance).copy(pelvis = v(0.0, Body.STAND - 0.01, 0.0))

    private fun seatedOrStanding(spec: Spec): Pair<Pose, List<Prim>> =
        if (spec.has("seated")) {
            val p = MovesSeated.seated(-0.05)
            p to Gear.bench(0.0, -0.1, back = false)
        } else standing() to emptyList()

    /** Lateral raises of every kind, and upright rows, which share the side of the shoulder. */
    fun lateral(spec: Spec): Motion {
        if (spec.has("upright row", "high pull")) return uprightRow(spec)
        val (base, seat) = seatedOrStanding(spec)
        val shY = base.pelvis.y + Body.SPINE - 0.05
        val one = spec.oneArm || (spec.gear == "CABLE" && spec.has("single-arm", "leaning"))
        val y = spec.has("y-raise", "scaption")
        val w = spec.has("w-raise")
        fun hands(up: Boolean): Pair<Vec3, Vec3> = when {
            w -> if (up) pair(0.42, shY + 0.12, -0.02) else pair(0.32, shY - 0.22, 0.12)
            y -> if (up) pair(0.5, shY + (if (spec.has("y-raise")) 0.35 else 0.0), 0.42) else pair(0.16, shY - 0.58, 0.14)
            else -> if (up) pair(0.74, shY - 0.03, 0.1) else pair(0.21, shY - 0.58, 0.1)
        }
        val pole = if (w) v(1.0, -0.4, -0.3) else v(0.3, 0.4, -1.0)
        fun at(up: Boolean): Pose {
            val (l, r) = hands(up)
            val p = base.copy(handL = l, handR = r, elbowPole = pole)
            return if (one) p.copy(handL = v(0.2, shY - 0.3, 0.35), elbowPoleR = pole, elbowPole = v(0.3, -1.0, -0.2)) else p
        }
        val machine = spec.gear == "MACHINE"
        val pulleys = listOf(v(0.32, 0.12, 0.08), v(-0.32, 0.12, 0.08))
        val towers = if (spec.gear == "CABLE") Gear.cableTower(v(0.45, 0.0, 0.1), 0.12).first else emptyList()
        val rig = if (machine) Rigging { s, _ ->
            listOf(s.elbowL, s.elbowR).map { e -> Prim.Capsule(e - v(0.0, 0.06, 0.0), e + v(0.0, 0.06, 0.0), 0.045, 0.045, Palette.PAD) }
        } else held(spec, Vec3.Z, if (one) listOf(pulleys[0]) else pulleys, oneHand = one)
        return Motion(
            mapOf("setup" to at(false), "grip" to at(false), "bottom" to at(false), "top" to at(true)),
            seat + towers + (if (machine) Gear.machineSeat(Gear.BENCH_TOP, 90.0, -0.1) else emptyList()),
            rig,
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.SHOULDERS,
                setupNote = if (spec.has("seated") || machine) "Sit tall at the end of the bench, feet flat, chest up."
                else "Feet hip width, knees soft, lean very slightly forward from the hips.",
                gripNote = when {
                    machine -> "Pads against the outside of the upper arms, handles held lightly."
                    spec.gear == "CABLE" -> "Handle from the low pulley in the far hand, cable running in front of the body."
                    else -> "Dumbbells at your sides, palms facing in, a soft bend in the elbows that stays fixed."
                },
                startFocus = Focus.SHOULDERS, upSeconds = 1.2, downSeconds = 2.0, cameraYaw = 10 * DEG
            ),
            Camera(yaw = 15 * DEG, pitch = 8 * DEG, distance = 3.4, target = v(0.0, 1.1, 0.0))
        )
    }

    private fun uprightRow(spec: Spec): Motion {
        val base = standing()
        val half = if (spec.gear == "DUMBBELL" || spec.gear == "KETTLEBELL") 0.14 else 0.13
        val high = spec.has("high pull")
        val bottom = base.copy(handL = v(half, 0.8, 0.13), handR = v(-half, 0.8, 0.13), elbowPole = v(1.0, -0.2, -0.3))
        val top = base.copy(handL = v(half, SH_Y - 0.04, 0.16), handR = v(-half, SH_Y - 0.04, 0.16), elbowPole = v(1.0, 0.8, -0.2), shrug = if (high) 0.04 else 0.0)
        val start = if (high) bottom.copy(pelvis = v(0.0, 0.86, -0.08), pitch = 22 * DEG, kneePole = v(0.2, 0.0, 1.0)).let {
            it.copy(handL = v(half, 0.66, 0.2), handR = v(-half, 0.66, 0.2))
        } else bottom
        val pulley = listOf(v(0.0, 0.12, 0.55))
        return Motion(
            mapOf("setup" to bottom, "grip" to bottom, "bottom" to start, "top" to top),
            if (spec.gear == "CABLE") Gear.cableTower(v(0.0, 0.0, 0.75), 0.12).first else emptyList(),
            held(spec, Vec3.X, pulley),
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.ELBOWS,
                setupNote = if (high) "Bar at mid-thigh, hips back a little, then stand up hard and pull." else "Stand tall, bar resting against the thighs.",
                gripNote = "Overhand, hands about a fist outside the hips. Lead with the elbows, not the hands.",
                startFocus = Focus.SHOULDERS, upSeconds = if (high) 0.6 else 1.2, downSeconds = 1.8, cameraYaw = 30 * DEG
            ),
            Camera(yaw = 30 * DEG, pitch = 8 * DEG, distance = 3.4, target = v(0.0, 1.1, 0.0))
        )
    }

    /** Front raises with dumbbells, a bar, a cable or a plate. */
    fun frontRaise(spec: Spec): Motion {
        val base = standing()
        val half = when (spec.gear) { "PLATE" -> 0.11; "BARBELL" -> 0.24; "CABLE" -> 0.1; else -> 0.2 }
        val bottom = base.copy(handL = v(half, 0.8, 0.14), handR = v(-half, 0.8, 0.14), elbowPole = v(0.3, -0.3, -1.0))
        val top = base.copy(handL = v(half, SH_Y + 0.02, 0.6), handR = v(-half, SH_Y + 0.02, 0.6), elbowPole = v(0.3, 0.3, -1.0))
        return Motion(
            mapOf("setup" to bottom, "grip" to bottom, "bottom" to bottom, "top" to top),
            if (spec.gear == "CABLE") Gear.cableTower(v(0.0, 0.0, -0.55), 0.12).first else emptyList(),
            held(spec, Vec3.X, listOf(v(0.0, 0.12, -0.45))),
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.SHOULDERS,
                setupNote = "Feet hip width, glutes tight so the body does not rock.",
                gripNote = if (spec.gear == "PLATE") "Plate held at 3 and 9 o'clock, arms long." else "Overhand, weights resting on the front of the thighs.",
                upSeconds = 1.2, downSeconds = 2.0, cameraYaw = 75 * DEG
            ),
            Camera(yaw = 70 * DEG, pitch = 8 * DEG, distance = 3.4, target = v(0.0, 1.1, 0.2))
        )
    }

    /** Chest flyes on a bench, at a cable station or on the pec deck. Rear-delt flyes go to the pulls. */
    fun fly(spec: Spec): Motion {
        if (spec.has("rear delt", "reverse")) return MovesPull.rearDelt(spec)
        if (spec.gear == "MACHINE" || spec.has("pec deck")) return pecDeck()
        if (spec.gear == "CABLE" && !spec.has("bench")) return cableFly(spec)
        val angle = spec.angle * DEG
        val pitch = -90 * DEG + angle
        val pelvis = v(0.0, Gear.BENCH_TOP + 0.12, if (spec.angle > 5) 0.08 else 0.12)
        val sh = Kit.shoulder(pelvis, pitch, 1.0)
        val up = Basis.of(0.0, pitch).fwd
        val top = pair(0.12, sh.y + up.y * 0.58, sh.z + up.z * 0.58)
        val open = pair(0.66, sh.y - 0.04, sh.z + 0.02)
        fun lying(h: Pair<Vec3, Vec3>, pole: Vec3) = Pose(
            pelvis = pelvis, pitch = pitch, head = 0.1, handL = h.first, handR = h.second,
            footL = v(0.32, Kit.ANKLE, 0.62), footR = v(-0.32, Kit.ANKLE, 0.62),
            elbowPole = pole, kneePole = v(0.25, 0.0, 1.0), footYaw = 0.25
        )
        val towers = if (spec.gear == "CABLE") Gear.cableTower(v(0.9, 0.0, sh.z), 0.1).first + Gear.cableTower(v(-0.9, 0.0, sh.z), 0.1).first else emptyList()
        return Motion(
            mapOf("setup" to lying(top, v(1.0, 0.0, -0.4)), "grip" to lying(top, v(1.0, 0.0, -0.4)),
                "top" to lying(top, v(1.0, 0.0, -0.4)), "bottom" to lying(open, v(0.2, -1.0, -0.6))),
            Gear.bench(spec.angle, 0.15) + towers,
            held(spec, Vec3.Z, listOf(v(0.85, 0.1, sh.z), v(-0.85, 0.1, sh.z))),
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.SHOULDERS,
                setupNote = "Lie back with the weights on your thighs, then kick them up over the chest. Feet planted.",
                gripNote = "Palms facing each other, a soft bend in the elbows that stays the same all the way.",
                startFocus = Focus.SHOULDERS, cameraYaw = 25 * DEG
            ),
            Camera(yaw = 30 * DEG, pitch = 22 * DEG, distance = 3.2, target = v(0.0, 0.7, 0.0))
        )
    }

    private fun cableFly(spec: Spec): Motion {
        val base = standing(0.16).copy(footL = v(0.15, Kit.ANKLE, 0.15), footR = v(-0.15, Kit.ANKLE, -0.15), pitch = 12 * DEG,
            pelvis = v(0.0, Body.STAND - 0.03, -0.02), kneePole = v(0.1, 0.0, 1.0))
        val pulleyY = when { spec.has("low-to-high") -> 0.15; spec.has("high-to-low") -> 1.95; else -> 1.6 }
        val one = spec.oneArm
        val towerL = Gear.cableTower(v(0.95, 0.0, -0.1), pulleyY)
        val towerR = Gear.cableTower(v(-0.95, 0.0, -0.1), pulleyY)
        val endY = when { spec.has("low-to-high") -> SH_Y; spec.has("high-to-low") -> 0.95; else -> 1.1 }
        // Arms open towards the pulleys, as far as they reach.
        val toward = v(0.75, pulleyY - SH_Y, 0.0).normalized() * 0.57
        val open = base.copy(handL = v(Body.SHOULDER_HALF + toward.x, SH_Y + toward.y, 0.08), handR = v(-Body.SHOULDER_HALF - toward.x, SH_Y + toward.y, 0.08), elbowPole = v(0.3, -1.0, -0.5))
        val closed = base.copy(handL = v(0.06, endY, 0.5), handR = v(-0.06, endY, 0.5), elbowPole = v(1.0, -0.6, -0.3))
        fun oneSide(p: Pose) = if (one) p.copy(handL = v(0.25, 1.0, 0.1), elbowPole = v(0.3, -1.0, 0.0), elbowPoleR = p.elbowPole) else p
        return Motion(
            mapOf("setup" to oneSide(open), "grip" to oneSide(open), "bottom" to oneSide(open), "top" to oneSide(closed)),
            towerL.first + towerR.first,
            held(spec, Vec3.Z, listOf(towerL.second, towerR.second).let { if (one) listOf(it[1]) else it }, oneHand = one),
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.SHOULDERS,
                setupNote = "Pulleys set ${if (pulleyY < 0.5) "low" else if (pulleyY > 1.8) "high" else "about shoulder height"}. One step forward from the towers, staggered stance, slight lean.",
                gripNote = "A handle in each hand, palms facing forward, elbows softly bent and kept that way.",
                upSeconds = 1.2, downSeconds = 1.8, cameraYaw = 20 * DEG
            ),
            Camera(yaw = 25 * DEG, pitch = 12 * DEG, distance = 3.8, target = v(0.0, 1.1, 0.0))
        )
    }

    private fun pecDeck(): Motion {
        val seat = MovesSeated.seated(-0.12, lean = 0.0)
        val shY = seat.pelvis.y + Body.SPINE - 0.05
        val open = seat.copy(handL = v(0.62, shY - 0.02, -0.02), handR = v(-0.62, shY - 0.02, -0.02), elbowPole = v(0.2, -1.0, -0.4))
        val closed = seat.copy(handL = v(0.07, shY - 0.02, 0.48), handR = v(-0.07, shY - 0.02, 0.48), elbowPole = v(1.0, -0.6, -0.2))
        return Motion(
            mapOf("setup" to open, "grip" to open, "bottom" to open, "top" to closed),
            Gear.machineSeat(Gear.BENCH_TOP, 90.0, -0.12),
            Rigging { s, _ -> listOf(s.handL, s.handR).map { h -> Prim.Capsule(h - v(0.0, 0.1, 0.0), h + v(0.0, 0.1, 0.0), 0.018, 0.018, Palette.STEEL) } },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.SHOULDERS,
                setupNote = "Seat height so the handles are level with the chest. Back flat on the pad.",
                gripNote = "Handles in a neutral grip, elbows slightly bent, chest up.",
                upSeconds = 1.2, downSeconds = 1.8, cameraYaw = 20 * DEG
            ),
            Camera(yaw = 25 * DEG, pitch = 15 * DEG, distance = 3.3, target = v(0.0, 1.0, 0.0))
        )
    }

    /** Shrugs and their relatives: high pulls, overhead shrugs, band pull-aparts. */
    fun shrug(spec: Spec): Motion {
        if (spec.has("high pull")) return uprightRow(spec)
        if (spec.has("pull-apart")) return pullApart()
        val overhead = spec.has("overhead")
        val prone = spec.has("incline", "prone")
        val lean = when { prone -> 50 * DEG; spec.has("leaning") -> 25 * DEG; else -> 0.0 }
        val base = if (prone) Pose(
            pelvis = v(0.0, 0.72, -0.12), pitch = lean, head = -0.3,
            handL = v(0.0, 0.0, 0.0), handR = v(0.0, 0.0, 0.0),
            footL = v(0.18, Kit.ANKLE, -0.6), footR = v(-0.18, Kit.ANKLE, -0.6), kneePole = v(0.1, 0.0, 1.0)
        ) else standing().copy(pitch = lean, pelvis = if (lean > 0) v(0.0, Body.STAND - 0.04, -0.06) else standing().pelvis)
        val behind = spec.has("behind")
        val half = when (spec.gear) { "BARBELL", "SMITH" -> 0.24; "TRAP_BAR" -> 0.3; else -> 0.26 }
        val lift = 0.07
        fun at(up: Boolean): Pose {
            val k = if (up) lift else 0.0
            val torso = Basis.of(0.0, base.pitch)
            val shoulder = base.pelvis + torso.up * (Body.SPINE - 0.05 + k)
            val hand = when {
                overhead -> shoulder + v(0.0, 0.6, -0.02)
                lean > 0 -> shoulder + v(0.0, -0.6, 0.0)
                behind -> v(0.0, 0.8 + k, -0.1)
                spec.gear == "BARBELL" || spec.gear == "SMITH" -> v(0.0, 0.8 + k, 0.13)
                else -> v(0.0, 0.8 + k, 0.03)
            }
            return base.copy(
                handL = hand + v(if (overhead) 0.42 else half, 0.0, 0.0), handR = hand - v(if (overhead) 0.42 else half, 0.0, 0.0),
                shrug = k, elbowPole = if (behind) v(0.2, -1.0, 1.0) else v(0.4, -1.0, -0.4)
            )
        }
        val fixed = mutableListOf<Prim>()
        if (prone) fixed += Gear.bench(0.0, -0.1, back = false) + Prim.Box(
            Kit.onTorso(base.pelvis, lean, v(0.0, 0.3, -0.16)), Basis.of(0.0, lean), v(0.15, 0.4, 0.025), Palette.PAD
        )
        if (spec.gear == "SMITH" || spec.gear == "MACHINE") fixed += listOf(0.7, -0.7).map { x -> Prim.Box(v(x, 1.1, 0.13), Basis.IDENTITY, v(0.03, 1.1, 0.03), Palette.FRAME) }
        return Motion(
            mapOf("setup" to at(false), "grip" to at(false), "bottom" to at(false), "top" to at(true)),
            fixed + (if (spec.gear == "CABLE") Gear.cableTower(v(0.0, 0.0, 0.6), 0.1).first else emptyList()),
            held(spec, if (spec.gear == "DUMBBELL") Vec3.Z else Vec3.X, listOf(v(0.0, 0.1, 0.5))),
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.SHOULDERS,
                setupNote = if (prone) "Chest on an incline bench, arms hanging straight down." else "Stand tall, arms long, the weight hanging from straight arms.",
                gripNote = Kit.gripNote(spec, if (spec.gear == "DUMBBELL") "the dumbbells" else "the bar"),
                upSeconds = 0.8, downSeconds = 1.4, cameraYaw = 60 * DEG
            ),
            Camera(yaw = 50 * DEG, pitch = 8 * DEG, distance = 3.2, target = v(0.0, 1.1, 0.0))
        )
    }

    private fun pullApart(): Motion {
        val base = standing()
        val closed = base.copy(handL = v(0.18, SH_Y - 0.02, 0.58), handR = v(-0.18, SH_Y - 0.02, 0.58), elbowPole = v(0.4, -1.0, -0.4))
        val open = base.copy(handL = v(0.72, SH_Y - 0.02, 0.12), handR = v(-0.72, SH_Y - 0.02, 0.12), elbowPole = v(0.2, -1.0, -0.6))
        return Motion(
            mapOf("setup" to closed, "grip" to closed, "bottom" to closed, "top" to open),
            emptyList(),
            Rigging { s, _ -> listOf(Gear.band(s.handL, s.handR)) },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.SHOULDERS,
                setupNote = "Stand tall, arms straight out in front at shoulder height.",
                gripNote = "Band in both hands, overhand, hands shoulder width.",
                upSeconds = 1.0, downSeconds = 1.6, cameraYaw = 20 * DEG
            ),
            Camera(yaw = 20 * DEG, pitch = 12 * DEG, distance = 3.2, target = v(0.0, 1.15, 0.0))
        )
    }

    /** Hangs, pinches and grippers: holds, so the rep is a squeeze rather than a movement. */
    fun grip(spec: Spec): Motion {
        if (spec.has("hang")) {
            val barY = 2.25
            val towel = spec.has("towel")
            val handY = if (towel) barY - 0.2 else barY
            fun hang(shrug: Double): Pose {
                val sh = v(0.0, handY - 0.6 + shrug, -0.03)
                val pelvis = Kit.pelvisFor(sh, -3 * DEG)
                return Pose(
                    pelvis = pelvis, pitch = -3 * DEG, head = 0.1,
                    handL = v(if (towel) 0.2 else 0.36, handY, 0.0), handR = v(if (towel) -0.2 else -0.36, handY, 0.0),
                    footL = pelvis + v(0.1, -0.82, -0.08), footR = pelvis + v(-0.1, -0.82, -0.08),
                    elbowPole = v(0.8, -0.2, -0.3), kneePole = v(0.0, 0.0, 1.0), footPitch = 35 * DEG
                )
            }
            return Motion(
                mapOf("setup" to hang(0.0), "grip" to hang(0.0), "bottom" to hang(0.0), "top" to hang(0.03)),
                Gear.pullUpBar(barY),
                Rigging { s, _ -> if (towel) listOf(s.handL, s.handR).map { h -> Prim.Line(h, v(h.x, barY, 0.0), 0.03, Palette.PAD) } else emptyList() },
                Kit.steps(
                    "setup", "grip", "bottom", "top", Focus.HANDS,
                    setupNote = "Step up and take the bar, then lift the feet. Hang still, shoulders slightly engaged.",
                    gripNote = if (towel) "A towel over the bar each side, squeezed hard in each fist." else "Overhand, thumbs wrapped, hands shoulder width.",
                    startFocus = Focus.HANDS, upSeconds = 3.0, downSeconds = 3.0, cameraYaw = 30 * DEG
                ),
                Camera(yaw = 30 * DEG, pitch = 6 * DEG, distance = 4.2, target = v(0.0, 1.35, 0.0))
            )
        }
        val base = standing()
        val gripper = spec.has("gripper")
        val bucket = spec.has("bucket")
        val pinch = spec.gear == "PLATE"
        val hold = when {
            gripper -> base.copy(handL = v(0.2, 1.05, 0.32), handR = v(-0.2, 1.05, 0.32), elbowPole = v(0.3, -1.0, -0.3))
            bucket -> base.copy(pelvis = v(0.0, 0.84, -0.1), pitch = 40 * DEG, head = -0.4,
                handL = v(0.07, 0.62, 0.32), handR = v(-0.07, 0.62, 0.32), elbowPole = v(0.6, -0.5, -0.5), kneePole = v(0.15, 0.0, 1.0))
            else -> base.copy(handL = v(0.24, 0.8, 0.02), handR = v(-0.24, 0.8, 0.02))
        }
        val squeeze = if (gripper) hold.copy(handL = hold.handL + v(-0.01, 0.0, 0.0), handR = hold.handR + v(0.01, 0.0, 0.0)) else hold.copy(shrug = 0.01)
        return Motion(
            mapOf("setup" to hold, "grip" to hold, "bottom" to hold, "top" to squeeze),
            if (bucket) listOf(Prim.Cylinder(v(0.0, 0.0, 0.32), v(0.0, 0.68, 0.32), 0.17, Palette.PAD)) else emptyList(),
            when {
                gripper -> Rigging { s, _ -> listOf(s.handL, s.handR).map { h -> Prim.Capsule(h - v(0.0, 0.06, 0.0), h + v(0.0, 0.05, 0.0), 0.012, 0.012, Palette.STEEL) } }
                pinch -> Rigging { s, _ -> listOf(s.handL, s.handR).map { h -> Prim.Cylinder(h - v(0.02, 0.06, 0.0), h + v(0.02, -0.06, 0.0), 0.18, Palette.PLATE_RIM) } }
                bucket -> Rigging { _, _ -> emptyList() }
                else -> held(spec, Vec3.Z, emptyList(), false).let { r -> Rigging { s, p -> r.prims(s, p).map { if (it is Prim.Cylinder && it.r < 0.03) it.copy(r = 0.028) else it } } }
            },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.HANDS,
                setupNote = "Stand tall, shoulders down and back.",
                gripNote = when {
                    gripper -> "Gripper deep in the palm, handle across the base of the fingers."
                    pinch -> "Two plates smooth side out, pinched between thumb and flat fingers."
                    bucket -> "Hands buried in the rice. Open, close and twist the fingers against it."
                    else -> "Thick handles held at your sides, squeezed as hard as you can."
                },
                startFocus = Focus.HANDS, upSeconds = 1.0, downSeconds = 1.0, cameraYaw = 30 * DEG
            ),
            Camera(yaw = 30 * DEG, pitch = 10 * DEG, distance = 3.0, target = v(0.0, 1.0, 0.0))
        )
    }

    /** Farmer's walks and suitcase carries: a walking cycle with the weight at the sides. */
    fun carry(spec: Spec): Motion {
        val one = spec.has("suitcase")
        val march = spec.has("march")
        val toes = if (spec.has("toes")) 1.0 else 0.0
        val shrug = if (spec.has("shrug")) 0.05 else 0.0
        val base = standing(0.12).copy(toes = toes, pelvis = v(0.0, Body.STAND - 0.02 + toes * 0.07, 0.0))
        fun step(left: Boolean): Pose {
            val f = if (left) 1.0 else -1.0
            val lift = if (march) 0.3 else 0.0
            val front = v(0.0, Kit.ANKLE + lift + toes * 0.07, 0.2 + if (march) -0.05 else 0.0)
            val back = v(0.0, Kit.ANKLE + toes * 0.07, -0.16)
            val l = (if (left) front else back) + v(0.12, 0.0, 0.0)
            val r = (if (left) back else front) + v(-0.12, 0.0, 0.0)
            val y = base.pelvis.y - 0.02 + shrug
            return base.copy(
                footL = l, footR = r, shrug = shrug,
                handL = v(0.26, 0.8 + shrug, 0.03 - f * 0.02), handR = v(-0.26, 0.8 + shrug, 0.03 + f * 0.02),
                pelvis = base.pelvis.copy(y = y), toesR = if (left) 0.6 else toes, toes = if (left) toes else 0.6,
                kneePole = v(0.1, 0.0, 1.0)
            )
        }
        return Motion(
            mapOf("setup" to base, "grip" to base, "bottom" to step(true), "top" to step(false)),
            emptyList(),
            if (one) held(spec, Vec3.Z, oneHand = true) else held(spec, Vec3.Z),
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "Weights beside the feet. Squat down with a flat back and stand up with them.",
                gripNote = if (one) "One weight in one hand. Stay perfectly upright: do not lean away from it." else "A weight in each hand, crushed hard, shoulders back and down.",
                upSeconds = 0.6, downSeconds = 0.6, cameraYaw = 60 * DEG
            ),
            Camera(yaw = 55 * DEG, pitch = 8 * DEG, distance = 3.4, target = v(0.0, 0.95, 0.0))
        )
    }
}
