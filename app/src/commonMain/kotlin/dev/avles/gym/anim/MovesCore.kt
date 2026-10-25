package dev.avles.gym.anim

import dev.avles.gym.anim.Kit.pair

/** Abs and trunk: crunches, sit-ups, leg raises, planks, rollouts, chops and twists. */
object MovesCore {

    private val MAT = Prim.Box(v(0.0, 0.006, -0.15), Basis.IDENTITY, v(0.32, 0.006, 0.95), Palette.PAD)
    private const val BAR_Y = 2.25

    /** Lying face up on the floor, head towards -Z, knees bent with feet flat unless [feet] says otherwise. */
    private fun supine(lift: Double = 0.0, feet: Pair<Vec3, Vec3>? = null, hands: Pair<Vec3, Vec3>? = null): Pose {
        val pelvis = v(0.0, 0.11, 0.0)
        val pitch = -90 * DEG + lift
        val head = hands ?: Pair(
            Kit.onTorso(pelvis, pitch, v(0.1, Body.SPINE + 0.13, -0.05)),
            Kit.onTorso(pelvis, pitch, v(-0.1, Body.SPINE + 0.13, -0.05))
        )
        val f = feet ?: Pair(v(0.12, Kit.ANKLE, 0.42), v(-0.12, Kit.ANKLE, 0.42))
        return Pose(
            pelvis = pelvis, pitch = pitch, head = 0.2 + lift * 0.3,
            handL = head.first, handR = head.second, footL = f.first, footR = f.second,
            elbowPole = v(1.0, 0.0, 0.3), kneePole = v(0.15, 0.0, 1.0)
        )
    }

    private fun hang(lean: Double = -3 * DEG): Pose {
        val sh = v(0.0, BAR_Y - 0.6, -0.03)
        val pelvis = Kit.pelvisFor(sh, lean)
        return Pose(
            pelvis = pelvis, pitch = lean, head = 0.1,
            handL = v(0.36, BAR_Y, 0.0), handR = v(-0.36, BAR_Y, 0.0),
            footL = pelvis + v(0.1, -0.84, -0.04), footR = pelvis + v(-0.1, -0.84, -0.04),
            elbowPole = v(0.8, -0.2, -0.3), kneePole = v(0.0, 0.3, 1.0), footPitch = 35 * DEG
        )
    }

    /** Kneeling upright, shins on the floor behind. */
    private fun kneeling(pitch: Double, pelvisZ: Double = 0.0): Pose {
        val pelvis = v(0.0, 0.56, pelvisZ)
        return Pose(
            pelvis = pelvis, pitch = pitch, head = -pitch * 0.4,
            handL = v(0.2, 1.0, 0.2), handR = v(-0.2, 1.0, 0.2),
            footL = v(0.12, Kit.ANKLE, pelvisZ - 0.42), footR = v(-0.12, Kit.ANKLE, pelvisZ - 0.42),
            kneePole = v(0.1, -1.0, 0.4), footPitch = 75 * DEG
        )
    }

    private fun motion(
        poses: Map<String, Pose>, fixed: List<Prim>, steps: List<Step>, camera: Camera, rig: Rigging = Rigging { _, _ -> emptyList() }
    ) = Motion(poses, fixed, rig, steps, camera)

    private val FLOOR_CAM = Camera(yaw = 75 * DEG, pitch = 22 * DEG, distance = 3.0, target = v(0.0, 0.3, 0.0))
    private val HANG_CAM = Camera(yaw = 45 * DEG, pitch = 6 * DEG, distance = 4.2, target = v(0.0, 1.35, 0.0))

    // ---- flexion ---------------------------------------------------------------

    fun flexion(spec: Spec): Motion = when {
        spec.has("rotary", "torso machine") -> rotary()
        spec.has("cable crunch", "kneeling cable") -> cableCrunch(spec)
        spec.gear == "MACHINE" -> machineCrunch()
        spec.has("hanging", "toes to bar", "captain") -> hanging(spec)
        spec.has("sit-up") -> sitUp(spec)
        spec.has("reverse crunch") -> reverseCrunch()
        spec.has("bicycle") -> bicycle()
        spec.has("v-up") -> vUp()
        spec.has("dragon flag") -> dragonFlag()
        spec.has("l-sit") -> lSit()
        spec.has("mountain climber") -> MovesPower.mountainClimber()
        spec.has("flutter", "scissor") -> kicks(spec)
        spec.has("windshield") -> wipers()
        spec.has("bird dog") -> birdDog()
        spec.has("stir the pot") -> plank(spec)
        else -> crunch()
    }

    private fun crunch(): Motion {
        val down = supine()
        val up = supine(30 * DEG)
        return motion(
            mapOf("setup" to down, "grip" to down, "bottom" to down, "top" to up), listOf(MAT),
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "Lie on your back, knees bent, feet flat and hip width.",
                gripNote = "Fingertips lightly behind the ears, elbows wide. Do not pull on the head.",
                startFocus = Focus.CORE, upSeconds = 1.0, downSeconds = 1.5),
            FLOOR_CAM
        )
    }

    private fun sitUp(spec: Spec): Motion {
        val decline = spec.has("decline")
        val weighted = spec.has("weighted")
        val chest = { lift: Double ->
            val p = v(0.0, 0.11, 0.0)
            val pitch = -90 * DEG + lift
            Pair(Kit.onTorso(p, pitch, v(0.08, Body.SPINE - 0.15, 0.16)), Kit.onTorso(p, pitch, v(-0.08, Body.SPINE - 0.15, 0.16)))
        }
        val feet = Pair(v(0.12, Kit.ANKLE, 0.42), v(-0.12, Kit.ANKLE, 0.42))
        val down = supine(if (decline) -10 * DEG else 0.0, feet, if (weighted) chest(0.0) else null)
        val up = supine(80 * DEG, feet, if (weighted) chest(80 * DEG) else null)
        return motion(
            mapOf("setup" to down, "grip" to down, "bottom" to down, "top" to up),
            if (decline) Gear.bench(0.0, 0.0) else listOf(MAT),
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = if (decline) "Hook the feet under the pads of a decline bench, knees bent." else "Lie back, knees bent, feet anchored or flat.",
                gripNote = if (weighted) "Hug a plate to the chest with both arms." else "Hands lightly by the ears or crossed on the chest.",
                startFocus = Focus.CORE, upSeconds = 1.2, downSeconds = 1.8),
            FLOOR_CAM,
            if (weighted) Rigging { s, _ -> Gear.plate((s.handL + s.handR) * 0.5, s.torso.fwd) } else Rigging { _, _ -> emptyList() }
        )
    }

    private fun reverseCrunch(): Motion {
        val arms = Pair(v(0.3, 0.03, -0.1), v(-0.3, 0.03, -0.1))
        val down = supine(0.0, Pair(v(0.1, 0.4, 0.4), v(-0.1, 0.4, 0.4)), arms)
        val up = supine(0.0, Pair(v(0.1, 0.62, -0.05), v(-0.1, 0.62, -0.05)), arms).copy(pelvis = v(0.0, 0.16, -0.02))
        return motion(
            mapOf("setup" to down, "grip" to down, "bottom" to down, "top" to up), listOf(MAT),
            Kit.steps("setup", "grip", "bottom", "top", Focus.HIPS,
                setupNote = "Lie on your back, knees bent at 90 degrees, shins level with the floor.",
                gripNote = "Arms flat by your sides, palms down, for balance.",
                startFocus = Focus.CORE, upSeconds = 1.0, downSeconds = 1.8),
            FLOOR_CAM
        )
    }

    private fun bicycle(): Motion {
        val a = supine(28 * DEG, Pair(v(0.12, 0.42, 0.15), v(-0.12, 0.3, 0.75))).copy(twist = 25 * DEG)
        val b = supine(28 * DEG, Pair(v(0.12, 0.3, 0.75), v(-0.12, 0.42, 0.15))).copy(twist = -25 * DEG)
        return motion(
            mapOf("setup" to supine(), "grip" to supine(), "bottom" to a, "top" to b), listOf(MAT),
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "Lie on your back, lift the shoulders and both feet off the floor.",
                gripNote = "Fingertips behind the ears, elbows wide. Turn the shoulder, not the elbow, towards the knee.",
                startFocus = Focus.CORE, upSeconds = 0.8, downSeconds = 0.8),
            FLOOR_CAM
        )
    }

    private fun vUp(): Motion {
        val flat = supine(0.0, Pair(v(0.1, Kit.ANKLE, 0.86), v(-0.1, Kit.ANKLE, 0.86)), Pair(v(0.25, 0.08, -1.0), v(-0.25, 0.08, -1.0)))
        val pelvis = v(0.0, 0.1, 0.0)
        val upP = -35 * DEG
        val reach = Kit.onTorso(pelvis, upP, v(0.0, Body.SPINE + 0.45, 0.1))
        val up = supine(55 * DEG, Pair(v(0.1, 0.6, 0.6), v(-0.1, 0.6, 0.6)), pair(0.1, reach.y, reach.z + 0.2))
        return motion(
            mapOf("setup" to flat, "grip" to flat, "bottom" to flat, "top" to up), listOf(MAT),
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "Lie flat, legs straight and together, arms stretched overhead.",
                gripNote = "Reach the hands towards the toes as both halves of the body rise together.",
                startFocus = Focus.CORE, upSeconds = 0.9, downSeconds = 1.5),
            FLOOR_CAM
        )
    }

    private fun kicks(spec: Spec): Motion {
        val arms = Pair(v(0.3, 0.03, -0.05), v(-0.3, 0.03, -0.05))
        val scissor = spec.has("scissor")
        val a = supine(10 * DEG, if (scissor) Pair(v(0.3, 0.28, 0.8), v(-0.05, 0.2, 0.82)) else Pair(v(0.1, 0.32, 0.8), v(-0.1, 0.16, 0.84)), arms)
        val b = supine(10 * DEG, if (scissor) Pair(v(0.05, 0.2, 0.82), v(-0.3, 0.28, 0.8)) else Pair(v(0.1, 0.16, 0.84), v(-0.1, 0.32, 0.8)), arms)
        return motion(
            mapOf("setup" to supine(), "grip" to a, "bottom" to a, "top" to b), listOf(MAT),
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "Lie on your back, press the lower back into the floor.",
                gripNote = "Hands flat under or beside the hips. Legs straight, a few inches off the floor.",
                startFocus = Focus.CORE, upSeconds = 0.5, downSeconds = 0.5),
            FLOOR_CAM
        )
    }

    private fun wipers(): Motion {
        val arms = Pair(v(0.7, 0.03, -0.35), v(-0.7, 0.03, -0.35))
        val mid = supine(0.0, Pair(v(0.06, 0.88, 0.05), v(-0.06, 0.88, 0.05)), arms)
        val left = supine(0.0, Pair(v(0.65, 0.38, 0.05), v(0.53, 0.28, 0.05)), arms)
        val right = supine(0.0, Pair(v(-0.53, 0.28, 0.05), v(-0.65, 0.38, 0.05)), arms)
        return motion(
            mapOf("setup" to mid, "grip" to mid, "mid" to mid, "left" to left, "right" to right), listOf(MAT),
            Kit.flow("setup", "grip", listOf("mid", "left", "mid", "right"), Focus.CORE, listOf(1.0, 1.0, 1.0, 1.0),
                setupNote = "Lie on your back, legs straight up over the hips.",
                gripNote = "Arms out wide on the floor, palms down, to pin the shoulders.",
                startFocus = Focus.CORE),
            FLOOR_CAM
        )
    }

    private fun dragonFlag(): Motion {
        val shoulders = v(0.0, Gear.BENCH_TOP + 0.08, -0.55)
        fun line(angle: Double): Pose {
            val dir = v(0.0, kotlin.math.sin(angle), kotlin.math.cos(angle))
            val p = shoulders + dir * (Body.SPINE - 0.05)
            val pitch = kotlin.math.atan2(-dir.z, -dir.y)
            val ankle = shoulders + dir * Kit.LINE
            return Pose(
                pelvis = p, pitch = pitch, head = 0.4,
                handL = v(0.12, Gear.BENCH_TOP + 0.08, -0.82), handR = v(-0.12, Gear.BENCH_TOP + 0.08, -0.82),
                footL = ankle + v(0.06, 0.0, 0.0), footR = ankle + v(-0.06, 0.0, 0.0),
                elbowPole = v(0.6, 0.4, 0.4), footPitch = -60 * DEG
            )
        }
        val up = line(75 * DEG)
        val down = line(18 * DEG)
        return motion(
            mapOf("setup" to up, "grip" to up, "top" to up, "bottom" to down), Gear.bench(0.0, 0.2),
            Kit.steps("setup", "grip", "top", "bottom", Focus.CORE,
                setupNote = "Lie on a bench and swing the body up until only the shoulders touch it.",
                gripNote = "Grab the bench behind the head, or a post, with both hands.",
                startFocus = Focus.CORE, upSeconds = 1.2, downSeconds = 3.0),
            Camera(yaw = 80 * DEG, pitch = 12 * DEG, distance = 3.6, target = v(0.0, 0.9, 0.0))
        )
    }

    private fun lSit(): Motion {
        val pelvis = v(0.0, 0.28, 0.0)
        val hold = Pose(
            pelvis = pelvis, pitch = -8 * DEG, head = 0.0,
            handL = v(0.24, 0.2, 0.02), handR = v(-0.24, 0.2, 0.02),
            footL = v(0.08, 0.27, 0.86), footR = v(-0.08, 0.27, 0.86),
            elbowPole = v(0.3, -0.3, -1.0), footPitch = -60 * DEG
        )
        val held = hold.copy(pelvis = pelvis + v(0.0, 0.01, 0.0), footL = hold.footL + v(0.0, 0.02, 0.0), footR = hold.footR + v(0.0, 0.02, 0.0))
        return motion(
            mapOf("setup" to hold, "grip" to hold, "bottom" to hold, "top" to held),
            listOf(0.24, -0.24).flatMap { x -> listOf(Prim.Cylinder(v(x, 0.2, -0.15), v(x, 0.2, 0.15), 0.016, Palette.STEEL), Prim.Box(v(x, 0.1, 0.0), Basis.IDENTITY, v(0.02, 0.1, 0.12), Palette.FRAME)) },
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "Parallettes or two sturdy boxes either side of the hips.",
                gripNote = "Press straight down, arms locked, shoulders pushed away from the ears, legs straight out in front.",
                startFocus = Focus.SHOULDERS, upSeconds = 2.0, downSeconds = 2.0),
            Camera(yaw = 70 * DEG, pitch = 12 * DEG, distance = 2.8, target = v(0.0, 0.4, 0.2))
        )
    }

    private fun birdDog(): Motion {
        fun quad(reach: Boolean): Pose {
            val pelvis = v(0.0, 0.62, -0.22)
            val pitch = 90 * DEG
            val sh = Kit.onTorso(pelvis, pitch, v(0.0, Body.SPINE - 0.05, 0.0))
            return Pose(
                pelvis = pelvis, pitch = pitch, head = -0.9,
                handL = if (reach) v(0.12, 0.9, sh.z + 0.56) else v(0.17, 0.04, sh.z), handR = v(-0.17, 0.04, sh.z),
                footL = v(0.12, 0.08, pelvis.z - 0.42), footR = if (reach) v(-0.12, 0.62, pelvis.z - 0.88) else v(-0.12, 0.08, pelvis.z - 0.42),
                elbowPole = v(0.2, 0.0, -1.0), kneePole = v(0.0, -1.0, 0.3), footPitch = 75 * DEG
            )
        }
        return motion(
            mapOf("setup" to quad(false), "grip" to quad(false), "bottom" to quad(false), "top" to quad(true)), listOf(MAT),
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "On hands and knees: hands under the shoulders, knees under the hips, back flat.",
                gripNote = "Reach one arm forward and the opposite leg back until both are level with the body.",
                startFocus = Focus.BACK, upSeconds = 1.2, downSeconds = 1.2),
            Camera(yaw = 75 * DEG, pitch = 18 * DEG, distance = 3.0, target = v(0.0, 0.45, 0.0))
        )
    }

    private fun hanging(spec: Spec): Motion {
        val chair = spec.has("captain")
        val base = if (chair) hang().copy(
            handL = v(0.24, 1.2, 0.2), handR = v(-0.24, 1.2, 0.2), elbowPole = v(0.1, -1.0, -0.3)
        ) else hang()
        val hip = base.pelvis - v(0.0, 0.03, 0.0)
        val top = when {
            spec.has("toes to bar") -> {
                // Swing the hips back under the bar and fold until the toes touch it.
                val lean = -25 * DEG
                val pelvis = Kit.pelvisFor(v(0.0, BAR_Y - 0.58, -0.08), lean)
                base.copy(pitch = lean, pelvis = pelvis,
                    footL = pelvis + v(0.1, 0.62, 0.55), footR = pelvis + v(-0.1, 0.62, 0.55), footPitch = -40 * DEG)
            }
            spec.has("knee") || chair -> base.copy(footL = hip + v(0.1, -0.43, 0.44), footR = hip + v(-0.1, -0.43, 0.44), kneePole = v(0.0, 1.0, 0.6))
            else -> base.copy(pitch = -12 * DEG, footL = hip + v(0.08, 0.0, 0.85), footR = hip + v(-0.08, 0.0, 0.85), footPitch = -50 * DEG)
        }
        val fixed = if (chair) listOf(
            Prim.Box(v(0.0, 1.05, -0.12), Basis.IDENTITY, v(0.18, 0.35, 0.03), Palette.PAD),
            Prim.Box(v(0.24, 1.14, 0.12), Basis.IDENTITY, v(0.05, 0.03, 0.22), Palette.PAD),
            Prim.Box(v(-0.24, 1.14, 0.12), Basis.IDENTITY, v(0.05, 0.03, 0.22), Palette.PAD),
            Prim.Box(v(0.0, 0.9, -0.22), Basis.IDENTITY, v(0.35, 0.9, 0.04), Palette.FRAME)
        ) else Gear.pullUpBar(BAR_Y)
        val chairBase = if (chair) base.copy(pelvis = v(0.0, 1.3 - 0.55, -0.04), handL = v(0.24, 1.17, 0.18), handR = v(-0.24, 1.17, 0.18)) else base
        fun fix(p: Pose) = if (chair) p.copy(pelvis = chairBase.pelvis, handL = chairBase.handL, handR = chairBase.handR,
            footL = p.footL - base.pelvis + chairBase.pelvis, footR = p.footR - base.pelvis + chairBase.pelvis, elbowPole = v(0.1, -1.0, -0.6)) else p
        return motion(
            mapOf("setup" to fix(base), "grip" to fix(base), "bottom" to fix(base), "top" to fix(top)), fixed,
            Kit.steps("setup", "grip", "bottom", "top", Focus.HIPS,
                setupNote = if (chair) "Back against the pad, forearms on the rests, legs hanging." else "Hang from the bar at arm's length, legs together, no swing.",
                gripNote = if (chair) "Grip the handles at the front of the arm rests." else "Overhand, hands shoulder width, shoulders slightly engaged.",
                startFocus = Focus.SHOULDERS, upSeconds = 1.2, downSeconds = 2.0, cameraYaw = 70 * DEG),
            HANG_CAM.copy(target = if (chair) v(0.0, 1.0, 0.0) else HANG_CAM.target)
        )
    }

    private fun cableCrunch(spec: Spec): Motion {
        val standing = spec.has("standing")
        val base = if (standing) Kit.stand(0.16).copy(pelvis = v(0.0, Body.STAND - 0.02, 0.0)) else kneeling(10 * DEG)
        val pulleyZ = if (standing) -0.6 else 0.6
        fun at(lean: Double): Pose {
            val p = base.copy(pitch = lean, head = -lean * 0.3)
            val ear = { x: Double -> Kit.onTorso(p.pelvis, lean, v(x, Body.SPINE + 0.1, 0.06)) }
            return p.copy(handL = ear(0.07), handR = ear(-0.07), elbowPole = v(0.4, -1.0, 0.6))
        }
        val up = at(10 * DEG)
        val down = at(if (standing) 45 * DEG else 70 * DEG)
        val (tower, pulley) = Gear.cableTower(v(0.0, 0.0, pulleyZ + if (standing) -0.1 else 0.1), 1.95)
        return motion(
            mapOf("setup" to up, "grip" to up, "top" to up, "bottom" to down), tower + if (standing) emptyList() else listOf(MAT),
            Kit.steps("setup", "grip", "top", "bottom", Focus.CORE,
                setupNote = if (standing) "Back to a high pulley, feet hip width, knees soft." else "Kneel facing a high pulley, about an arm's length away.",
                gripNote = "Rope beside the head, hands by the ears. Curl the ribs towards the hips; the hips stay still.",
                startFocus = Focus.CORE, upSeconds = 1.2, downSeconds = 1.0, cameraYaw = 80 * DEG),
            Camera(yaw = 75 * DEG, pitch = 10 * DEG, distance = 3.2, target = v(0.0, 0.8, 0.0)),
            Rigging { s, _ -> Gear.cable(pulley, (s.handL + s.handR) * 0.5) }
        )
    }

    private fun machineCrunch(): Motion {
        val seat = MovesSeated.seated(-0.12)
        fun at(lean: Double): Pose {
            val h = { x: Double -> Kit.onTorso(seat.pelvis, lean, v(x, Body.SPINE - 0.02, 0.12)) }
            return seat.copy(pitch = lean, handL = h(0.2), handR = h(-0.2), elbowPole = v(0.6, -1.0, 0.2))
        }
        return motion(
            mapOf("setup" to at(0.0), "grip" to at(0.0), "top" to at(0.0), "bottom" to at(35 * DEG)), Gear.machineSeat(Gear.BENCH_TOP, 85.0, -0.12),
            Kit.steps("setup", "grip", "top", "bottom", Focus.CORE,
                setupNote = "Seat so the pads sit on the upper chest, feet under the rollers.",
                gripNote = "Handles by the shoulders. Crunch the ribs down; do not pull with the arms.",
                startFocus = Focus.CORE, upSeconds = 1.2, downSeconds = 1.0, cameraYaw = 75 * DEG),
            Camera(yaw = 70 * DEG, pitch = 10 * DEG, distance = 3.0, target = v(0.0, 0.85, 0.0)),
            Rigging { s, _ -> listOf(Prim.Capsule(s.shoulderL + s.torso.fwd * 0.08, s.shoulderR + s.torso.fwd * 0.08, 0.04, 0.04, Palette.PAD)) }
        )
    }

    private fun rotary(): Motion {
        val seat = MovesSeated.seated(-0.12)
        fun at(t: Double) = seat.copy(
            twist = t,
            handL = Kit.onTorso(seat.pelvis, 0.0, v(0.16, Body.SPINE - 0.15, 0.3), t), handR = Kit.onTorso(seat.pelvis, 0.0, v(-0.16, Body.SPINE - 0.15, 0.3), t),
            elbowPole = v(0.6, -1.0, -0.3)
        )
        return motion(
            mapOf("setup" to at(0.0), "grip" to at(0.0), "bottom" to at(40 * DEG), "top" to at(-40 * DEG)), Gear.machineSeat(Gear.BENCH_TOP, 90.0, -0.12),
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "Sit with the knees locked into the pads, chest against the chest pad.",
                gripNote = "Hands on the handles in front. Turn from the waist, slowly, both ways.",
                startFocus = Focus.CORE, upSeconds = 1.4, downSeconds = 1.4, cameraYaw = 20 * DEG),
            Camera(yaw = 20 * DEG, pitch = 25 * DEG, distance = 3.0, target = v(0.0, 0.85, 0.0))
        )
    }

    // ---- anti-extension ---------------------------------------------------------

    fun antiExtension(spec: Spec): Motion = when {
        spec.has("pallof") -> pallof()
        spec.has("side plank", "copenhagen") -> sidePlank(spec)
        spec.has("hollow") -> hollow()
        spec.has("dead bug") -> deadBug()
        spec.has("ab wheel", "rollout") -> rollout()
        else -> plank(spec)
    }

    private fun plank(spec: Spec): Motion {
        val ball = spec.has("stir the pot")
        val shY = if (ball) 0.78 else 0.36
        val sh = v(0.0, shY, 0.55)
        val ankle = v(0.0, Kit.ANKLE, sh.z - kotlin.math.sqrt(Kit.LINE * Kit.LINE - (shY - Kit.ANKLE) * (shY - Kit.ANKLE)))
        val (pelvis, pitch) = Kit.plank(ankle, sh)
        val elbowY = if (ball) 0.55 else 0.05
        val hold = Pose(
            pelvis = pelvis, pitch = pitch, head = -0.9,
            handL = v(0.08, elbowY + 0.02, sh.z + 0.26), handR = v(-0.08, elbowY + 0.02, sh.z + 0.26),
            footL = ankle + v(0.12, 0.0, 0.0), footR = ankle + v(-0.12, 0.0, 0.0),
            elbowPole = v(0.2, -1.0, -0.2), footPitch = 75 * DEG
        )
        val squeeze = if (ball) hold.copy(handL = hold.handL + v(0.06, 0.0, 0.05), handR = hold.handR + v(0.06, 0.0, 0.05))
        else hold.copy(pelvis = hold.pelvis + v(0.0, 0.01, 0.0))
        val fixed = mutableListOf<Prim>(MAT)
        if (ball) fixed += Prim.Ball(v(0.0, 0.3, sh.z + 0.15), 0.3, Palette.PAD)
        return motion(
            mapOf("setup" to hold, "grip" to hold, "bottom" to hold, "top" to squeeze), fixed,
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "Forearms on the ${if (ball) "ball" else "floor"}, elbows under the shoulders, legs straight back, feet together.",
                gripNote = if (ball) "Hands clasped on the ball. Draw small circles with the forearms." else "Fists relaxed, forearms parallel. Squeeze glutes and brace as if about to be punched.",
                startFocus = Focus.CORE, upSeconds = if (ball) 1.0 else 2.5, downSeconds = if (ball) 1.0 else 2.5),
            Camera(yaw = 70 * DEG, pitch = 14 * DEG, distance = 3.2, target = v(0.0, 0.3, 0.0)),
            if (spec.has("weighted")) Rigging { s, _ -> Gear.plate(s.chest + s.torso.fwd * -0.17, s.torso.fwd) } else Rigging { _, _ -> emptyList() }
        )
    }

    private fun sidePlank(spec: Spec): Motion {
        // Rolled onto the left side, head towards +X, chest facing +Z, leaning on the left forearm.
        val roll = -74 * DEG
        val b = Basis.of(0.0, 0.0, roll)
        val elbow = v(0.42, 0.04, 0.0)
        val shoulderL = elbow + v(0.0, Body.UPPER_ARM, 0.0)
        val neck = shoulderL - b.side * Body.SHOULDER_HALF + b.up * 0.05
        val pelvis = neck - b.up * Body.SPINE
        val down = pelvis - b.up * (Kit.LINE - (Body.SPINE - 0.05))
        val copen = spec.has("copenhagen")
        fun at(lift: Double) = Pose(
            pelvis = pelvis + v(0.0, lift, 0.0), roll = roll, head = 0.0,
            handL = elbow + v(0.0, 0.0, 0.3), handR = pelvis + b.side * -0.18 + b.up * 0.05,
            footL = down.withY(Kit.ANKLE), footR = if (copen) down.withY(Gear.BENCH_TOP + 0.06) + v(0.06, 0.0, 0.0) else down.withY(Kit.ANKLE + 0.1),
            elbowPole = v(0.2, -1.0, -0.4), kneePole = v(0.0, 0.0, 1.0), footYaw = 90 * DEG
        )
        val fixed = mutableListOf<Prim>(MAT.copy(half = v(0.95, 0.006, 0.32)))
        if (copen) fixed += Gear.bench(0.0, 0.0, back = false).map { if (it is Prim.Box) it.copy(c = v(it.c.z + down.x - 0.1, it.c.y, it.c.x), half = v(it.half.z, it.half.y, it.half.x)) else it }
        return motion(
            mapOf("setup" to at(-0.12), "grip" to at(0.0), "bottom" to at(0.0), "top" to at(0.02)), fixed,
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = if (copen) "Side on, top foot or knee on a bench, bottom leg free underneath." else "Lie on your side, elbow under the shoulder, feet stacked.",
                gripNote = "Bottom forearm flat and pointing forward, top hand on the hip. Lift the hips until the body is a straight line.",
                startFocus = Focus.HIPS, upSeconds = 2.5, downSeconds = 2.5, cameraYaw = 0.0),
            Camera(yaw = 5 * DEG, pitch = 14 * DEG, distance = 3.2, target = v(0.0, 0.35, 0.0))
        )
    }

    private fun hollow(): Motion {
        val pelvis = v(0.0, 0.1, 0.0)
        val reach = { lift: Double -> Kit.onTorso(pelvis, -90 * DEG + lift, v(0.0, Body.SPINE + 0.55, 0.05)) }
        val legs = Pair(v(0.06, 0.25, 0.86), v(-0.06, 0.25, 0.86))
        val a = supine(20 * DEG, legs, pair(0.1, reach(20 * DEG).y, reach(20 * DEG).z)).copy(footPitch = -60 * DEG)
        val b = a.copy(pelvis = a.pelvis + v(0.0, 0.005, 0.0))
        return motion(
            mapOf("setup" to supine(), "grip" to a, "bottom" to a, "top" to b), listOf(MAT),
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "Lie on your back and press the lower back flat into the floor.",
                gripNote = "Arms straight overhead by the ears, shoulders and legs lifted, body shaped like a banana.",
                startFocus = Focus.CORE, upSeconds = 2.5, downSeconds = 2.5),
            FLOOR_CAM
        )
    }

    private fun deadBug(): Motion {
        val pelvis = v(0.0, 0.11, 0.0)
        val shoulderOf = { x: Double -> Kit.onTorso(pelvis, -90 * DEG, v(x, Body.SPINE - 0.05, 0.0)) }
        val up = { x: Double -> shoulderOf(x) + v(0.0, 0.58, 0.0) }
        val knees = Pair(v(0.12, 0.5, 0.45), v(-0.12, 0.5, 0.45))
        val start = supine(0.0, knees, Pair(up(0.19), up(-0.19))).copy(kneePole = v(0.0, 0.0, 1.0), elbowPole = v(1.0, 0.0, 0.0))
        val reach = start.copy(handR = shoulderOf(-0.19) + v(0.0, 0.12, -0.56), footL = v(0.12, 0.15, 0.86))
        return motion(
            mapOf("setup" to start, "grip" to start, "bottom" to start, "top" to reach), listOf(MAT),
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "Lie on your back, knees and hips bent at 90 degrees, lower back flat on the floor.",
                gripNote = "Arms straight up over the shoulders. Lower one arm and the opposite leg together, slowly.",
                startFocus = Focus.CORE, upSeconds = 1.4, downSeconds = 1.4),
            FLOOR_CAM
        )
    }

    private fun rollout(): Motion {
        val k = kneeling(0.0)
        // Knees stay put; the hips travel forward and down as the wheel rolls out.
        fun at(pelvis: Vec3, pitch: Double): Pose {
            val sh = Kit.onTorso(pelvis, pitch, v(0.0, Body.SPINE - 0.05, 0.0))
            val handZ = sh.z + kotlin.math.sqrt((0.58 * 0.58 - (sh.y - 0.1) * (sh.y - 0.1)).coerceAtLeast(0.0))
            return k.copy(
                pelvis = pelvis, pitch = pitch, head = -pitch * 0.6,
                handL = v(0.1, 0.1, handZ), handR = v(-0.1, 0.1, handZ), elbowPole = v(0.2, -1.0, -1.0)
            )
        }
        val start = at(v(0.0, 0.5, -0.05), 72 * DEG)
        val out = at(v(0.0, 0.36, 0.14), 86 * DEG)
        return motion(
            mapOf("setup" to start, "grip" to start, "bottom" to start, "top" to out), listOf(MAT),
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "Kneel on a mat, the wheel on the floor under the shoulders.",
                gripNote = "Both hands on the wheel's handles, arms straight. Roll out only as far as the back stays flat.",
                startFocus = Focus.CORE, upSeconds = 1.6, downSeconds = 2.0, cameraYaw = 80 * DEG),
            Camera(yaw = 75 * DEG, pitch = 12 * DEG, distance = 3.2, target = v(0.0, 0.45, 0.3)),
            Rigging { s, _ -> val m = (s.handL + s.handR) * 0.5; listOf(Prim.Cylinder(m - v(0.05, 0.0, 0.0), m + v(0.05, 0.0, 0.0), 0.1, Palette.PLATE), Prim.Cylinder(s.handL, s.handR, 0.014, Palette.STEEL)) }
        )
    }

    private fun pallof(): Motion {
        val base = Kit.stand(0.2).copy(pelvis = v(0.0, Body.STAND - 0.05, 0.0), kneePole = v(0.2, 0.0, 1.0))
        val chest = base.copy(handL = v(0.02, 1.18, 0.16), handR = v(-0.02, 1.18, 0.16), elbowPole = v(0.6, -1.0, -0.2))
        val out = base.copy(handL = v(0.02, 1.2, 0.6), handR = v(-0.02, 1.2, 0.6), elbowPole = v(0.6, -1.0, -0.2))
        val (tower, pulley) = Gear.cableTower(v(1.0, 0.0, 0.2), 1.2)
        return motion(
            mapOf("setup" to chest, "grip" to chest, "bottom" to chest, "top" to out), tower,
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "Stand side-on to a cable at chest height, feet wide, knees soft.",
                gripNote = "Both hands round the handle at the chest. Press straight out and do not let it twist you.",
                startFocus = Focus.CORE, upSeconds = 1.0, downSeconds = 1.0, cameraYaw = 20 * DEG),
            Camera(yaw = 20 * DEG, pitch = 12 * DEG, distance = 3.6, target = v(0.3, 1.0, 0.0)),
            Rigging { s, _ -> Gear.cable(pulley, (s.handL + s.handR) * 0.5) }
        )
    }

    // ---- rotation ------------------------------------------------------------------

    fun rotation(spec: Spec): Motion = when {
        spec.has("side bend") -> sideBend(spec)
        spec.has("russian") -> russianTwist()
        else -> chop(spec)
    }

    private fun chop(spec: Spec): Motion {
        val landmine = spec.has("landmine")
        val highToLow = spec.has("high to low")
        val base = Kit.stand(0.24).copy(pelvis = v(0.0, Body.STAND - 0.05, 0.0), kneePole = v(0.25, 0.0, 1.0))
        fun at(t: Double, y: Double, side: Double) = base.copy(
            twist = t, pitch = 6 * DEG,
            handL = v(side * 0.55 + 0.03, y, 0.35), handR = v(side * 0.55 - 0.03, y, 0.35),
            elbowPole = v(0.3, -1.0, -0.3)
        )
        val high = if (landmine) at(50 * DEG, 1.25, 0.85) else at(45 * DEG, 1.52, 0.75)
        val low = if (landmine) at(-50 * DEG, 1.25, -0.85) else at(-45 * DEG, 0.95, -0.72)
        val (tower, pulley) = Gear.cableTower(v(1.05, 0.0, 0.3), if (highToLow) 1.95 else 0.15)
        val first = if (highToLow || landmine) high else low
        val last = if (highToLow || landmine) low else high
        return motion(
            mapOf("setup" to first, "grip" to first, "bottom" to first, "top" to last),
            if (landmine) listOf(Prim.Ball(v(0.0, 0.05, -0.9), 0.06, Palette.FRAME)) else tower,
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = if (landmine) "Face the landmine, feet wide, bar end held at arm's length in front of the chest."
                else "Stand side-on to the cable, feet wider than the hips, knees soft.",
                gripNote = "Both hands on the ${if (landmine) "end of the bar" else "handle"}, arms long. Turn the chest and hips together, pivoting on the back foot.",
                startFocus = Focus.CORE, upSeconds = 1.0, downSeconds = 1.0, cameraYaw = 15 * DEG),
            Camera(yaw = 15 * DEG, pitch = 12 * DEG, distance = 3.8, target = v(0.2, 1.0, 0.0)),
            Rigging { s, _ ->
                val m = (s.handL + s.handR) * 0.5
                if (landmine) listOf(Prim.Cylinder(v(0.0, 0.05, -0.9), m, 0.025, Palette.STEEL)) + Gear.plate(m + (m - v(0.0, 0.05, -0.9)).normalized() * 0.12, (m - v(0.0, 0.05, -0.9)))
                else Gear.cable(pulley, m)
            }
        )
    }

    private fun sideBend(spec: Spec): Motion {
        val base = Kit.stand(0.17).copy(pelvis = v(0.0, Body.STAND - 0.02, 0.0))
        fun at(roll: Double): Pose {
            val b = Basis.of(0.0, 0.0, roll)
            val shR = base.pelvis + b.up * (Body.SPINE - 0.05) - b.side * Body.SHOULDER_HALF
            return base.copy(roll = roll, handR = shR + v(0.0, -0.6, 0.02), handL = Kit.onTorso(base.pelvis, 0.0, v(0.12, Body.SPINE + 0.12, -0.04)), elbowPole = v(1.0, 0.0, 0.2))
        }
        val cable = spec.gear == "CABLE"
        val (tower, pulley) = Gear.cableTower(v(-0.7, 0.0, 0.1), 0.12)
        return motion(
            mapOf("setup" to at(0.0), "grip" to at(0.0), "bottom" to at(-22 * DEG), "top" to at(14 * DEG)), if (cable) tower else emptyList(),
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "Stand tall, feet hip width, the weight in one hand.",
                gripNote = "Other hand behind the head. Bend straight to the side; do not lean forward or back.",
                startFocus = Focus.CORE, upSeconds = 1.2, downSeconds = 1.4, cameraYaw = 0.0),
            Camera(yaw = 0.0, pitch = 8 * DEG, distance = 3.2, target = v(0.0, 1.0, 0.0)),
            Rigging { s, _ -> if (cable) Gear.cable(pulley, s.handR) else Gear.dumbbell(s.handR, Vec3.Z) }
        )
    }

    private fun russianTwist(): Motion {
        val pelvis = v(0.0, 0.12, 0.0)
        val lean = -45 * DEG
        fun at(t: Double): Pose {
            val h = Kit.onTorso(pelvis, lean, v(0.0, Body.SPINE - 0.2, 0.36), t)
            return Pose(
                pelvis = pelvis, pitch = lean, twist = t, head = 0.2,
                handL = h + v(0.03, 0.0, 0.0), handR = h - v(0.03, 0.0, 0.0),
                footL = v(0.12, 0.2, 0.5), footR = v(-0.12, 0.2, 0.5),
                elbowPole = v(0.6, -1.0, -0.2), kneePole = v(0.1, 0.0, 1.0)
            )
        }
        return motion(
            mapOf("setup" to at(0.0), "grip" to at(0.0), "bottom" to at(40 * DEG), "top" to at(-40 * DEG)), listOf(MAT),
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "Sit, lean back to about 45 degrees, knees bent, feet just off the floor (or down, to make it easier).",
                gripNote = "Hands together in front of the chest, or holding a plate. Turn the shoulders side to side.",
                startFocus = Focus.CORE, upSeconds = 0.9, downSeconds = 0.9, cameraYaw = 30 * DEG),
            Camera(yaw = 30 * DEG, pitch = 18 * DEG, distance = 2.8, target = v(0.0, 0.4, 0.1))
        )
    }
}
