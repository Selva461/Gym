package dev.avles.gym.anim

import kotlin.math.sqrt

/**
 * Power and conditioning: the Olympic lifts and their parts, kettlebell ballistics,
 * jumps, burpees, sleds, crawls and ropes. These have more than two positions in a
 * rep, so each is a short sequence played in order ([Kit.flow]).
 */
object MovesPower {

    private val SIDE = Camera(yaw = 70 * DEG, pitch = 8 * DEG, distance = 3.8, target = v(0.0, 1.0, 0.0))

    private fun stand(): Pose = Kit.stand(0.15).copy(pelvis = v(0.0, Body.STAND - 0.01, 0.0))

    // ---- positions every lift here is built from ----------------------------

    /** Bar on the floor over the middle of the foot, hips back, back flat, arms long. */
    private fun floor(half: Double, wide: Boolean = false): Pose {
        val pitch = (if (wide) 55 else 50) * DEG
        val hand = v(half, Gear.PLATE_R + 0.01, 0.08)
        return Pose(
            pelvis = v(0.0, Kit.pelvisToReach(hand, pitch, -0.24), -0.24), pitch = pitch, head = -0.5,
            handL = hand, handR = hand.withX(-half),
            footL = v(0.15, Kit.ANKLE, 0.0), footR = v(-0.15, Kit.ANKLE, 0.0),
            elbowPole = v(0.4, 0.0, -1.0), kneePole = v(0.3, 0.0, 1.0)
        )
    }

    /** Hands hanging at full reach from the shoulders of [p], [half] apart, at depth [z]. */
    private fun hanging(p: Pose, half: Double, z: Double): Pose {
        val sh = Kit.onTorso(p.pelvis, p.pitch, v(Body.SHOULDER_HALF, Body.SPINE - 0.05 + p.shrug, 0.0))
        val y = Kit.hangY(sh, half, z)
        return p.copy(handL = v(half, y, z), handR = v(-half, y, z))
    }

    /** The bar at the hips with the legs bent: the hang position and the start of the second pull. */
    private fun hang(half: Double): Pose = hanging(Pose(
        pelvis = v(0.0, 0.84, -0.14), pitch = 28 * DEG, head = -0.2,
        handL = v(half, 0.66, 0.12), handR = v(-half, 0.66, 0.12),
        footL = v(0.15, Kit.ANKLE, 0.0), footR = v(-0.15, Kit.ANKLE, 0.0),
        elbowPole = v(0.4, 0.0, -1.0), kneePole = v(0.25, 0.0, 1.0)
    ), half, 0.12)

    /** Full extension: up on the toes, hips through, shoulders shrugged, bar at the hips. */
    private fun extension(half: Double): Pose = hanging(stand().copy(
        pelvis = v(0.0, Body.STAND + 0.06, 0.02), pitch = -6 * DEG, toes = 1.0, shrug = 0.06, head = 0.1,
        elbowPole = v(0.8, 0.4, -0.4),
        footL = v(0.15, Kit.ANKLE + 0.07, 0.0), footR = v(-0.15, Kit.ANKLE + 0.07, 0.0)
    ), half, 0.12)

    /** Bar on the front of the shoulders, elbows high, at a squat depth [pelvisY]. */
    private fun rack(pelvisY: Double, pitch: Double = 12 * DEG, z: Double = -0.08): Pose {
        val pelvis = v(0.0, pelvisY, z)
        return Pose(
            pelvis = pelvis, pitch = pitch, head = -pitch * 0.4,
            handL = Kit.onTorso(pelvis, pitch, v(0.2, Body.SPINE - 0.03, 0.17)),
            handR = Kit.onTorso(pelvis, pitch, v(-0.2, Body.SPINE - 0.03, 0.17)),
            footL = v(0.2, Kit.ANKLE, 0.0), footR = v(-0.2, Kit.ANKLE, 0.0),
            elbowPole = v(0.4, 0.3, 1.0), kneePole = v(0.35, 0.0, 1.0)
        )
    }

    /** Arms locked overhead, bar over the back of the head, at a pelvis height. */
    private fun overhead(half: Double, pelvisY: Double, pitch: Double = 0.0, z: Double = 0.0): Pose {
        val pelvis = v(0.0, pelvisY, z)
        val sh = Kit.onTorso(pelvis, pitch, v(0.0, Body.SPINE - 0.05, 0.0))
        val reach = sqrt((0.6 * 0.6 - half * half * 0.6).coerceAtLeast(0.1))
        return Pose(
            pelvis = pelvis, pitch = pitch, head = -pitch * 0.5,
            handL = v(half, sh.y + reach, 0.0), handR = v(-half, sh.y + reach, 0.0),
            footL = v(0.2, Kit.ANKLE, 0.0), footR = v(-0.2, Kit.ANKLE, 0.0),
            elbowPole = v(0.6, 0.0, -1.0), kneePole = v(0.35, 0.0, 1.0)
        )
    }

    private fun bar(spec: Spec): Rigging = when (spec.gear) {
        "DUMBBELL" -> Rigging { s, _ -> Gear.dumbbell(s.handL, Vec3.X) + Gear.dumbbell(s.handR, Vec3.X) }
        "KETTLEBELL" -> Rigging { s, _ -> Gear.kettlebell(s.handL) + Gear.kettlebell(s.handR) }
        else -> Rigging { s, _ -> Gear.barbell(s.handL, s.handR) }
    }

    // ---- the Olympic lifts ------------------------------------------------------

    fun of(spec: Spec): Motion = when {
        spec.has("snatch-grip deadlift") -> MovesLegs.hinge(spec)
        spec.has("high pull") -> MovesArms.lateral(spec)
        spec.has("jump squat") -> jump(spec, box = false, broad = false)
        spec.has("box jump") -> jump(spec, box = true, broad = false)
        spec.has("broad jump") -> jump(spec, box = false, broad = true)
        spec.has("swing") -> swing(spec)
        spec.has("turkish") -> getUp()
        spec.has("sled") -> sled(spec)
        spec.has("burpee") -> burpee(false)
        spec.has("devil press", "man maker") -> burpee(true)
        spec.has("bear crawl") -> bearCrawl()
        spec.has("battle rope") -> ropes()
        spec.has("jump rope") -> skip()
        spec.has("sandbag") -> sandbag()
        spec.has("thruster") -> thruster(spec)
        spec.has("kettlebell clean", "kettlebell snatch") -> kbSnatchOrClean(spec)
        spec.has("jerk") && !spec.has("clean") -> jerk(spec)
        spec.has("snatch") -> snatch(spec)
        else -> clean(spec)
    }

    private fun clean(spec: Spec): Motion {
        val half = 0.27
        val fromHang = spec.has("hang")
        val pull = spec.has("pull")
        val power = spec.has("power", "complex")
        val withJerk = spec.has("jerk", "complex")
        val start = if (fromHang) hang(half) else floor(half)
        val catchY = if (power) 0.78 else 0.55
        val poses = mutableMapOf(
            "setup" to stand(), "grip" to start, "start" to start, "knee" to hang(half),
            "extend" to extension(half), "catch" to rack(catchY, if (power) 12 * DEG else 18 * DEG, if (power) -0.08 else -0.2),
            "stand" to rack(Body.STAND - 0.02, 2 * DEG, 0.0),
            "dip" to rack(0.84, 4 * DEG, -0.04),
            "jerk" to overhead(0.27, 0.8, 0.0).let { it.copy(footL = v(0.15, Kit.ANKLE, 0.38), footR = v(-0.15, Kit.ANKLE, -0.48), toesR = 0.8, kneePoleR = v(0.0, -0.5, 1.0)) }
        )
        val rep = when {
            pull -> listOf("start", "knee", "extend")
            withJerk -> listOf("start", "knee", "extend", "catch", "stand", "dip", "jerk", "stand")
            fromHang -> listOf("start", "extend", "catch", "stand")
            else -> listOf("start", "knee", "extend", "catch", "stand")
        }
        val legs = rep.map { if (it == "extend" || it == "catch") 0.35 else 0.7 }
        return Motion(
            poses, emptyList(), bar(spec),
            Kit.flow("setup", "grip", rep, Focus.HIPS, legs,
                setupNote = "Feet hip width under the bar, the bar over the middle of the foot.",
                gripNote = "Overhand, just outside the legs, hook grip if you know it. Arms straight like ropes.",
                startFocus = Focus.BACK, cameraYaw = 75 * DEG),
            SIDE
        )
    }

    private fun snatch(spec: Spec): Motion {
        val half = 0.58
        val fromHang = spec.has("hang")
        val pull = spec.has("pull")
        val power = spec.has("power")
        val start = if (fromHang) hang(half) else floor(half, wide = true)
        val poses = mapOf(
            "setup" to stand(), "grip" to start, "start" to start, "knee" to hang(half),
            "extend" to extension(half),
            "catch" to overhead(half, if (power) 0.8 else 0.56, if (power) 10 * DEG else 28 * DEG, if (power) -0.06 else -0.22),
            "stand" to overhead(half, Body.STAND - 0.02)
        )
        val rep = when {
            pull -> listOf("start", "knee", "extend")
            fromHang -> listOf("start", "extend", "catch", "stand")
            else -> listOf("start", "knee", "extend", "catch", "stand")
        }
        return Motion(
            poses, emptyList(), bar(spec),
            Kit.flow("setup", "grip", rep, Focus.HIPS, rep.map { if (it == "extend" || it == "catch") 0.35 else 0.7 },
                setupNote = "Feet hip width, the bar over the middle of the foot.",
                gripNote = "Wide grip: the bar should sit in the hip crease when you stand with straight arms. Hook grip.",
                startFocus = Focus.BACK, cameraYaw = 75 * DEG),
            SIDE
        )
    }

    private fun jerk(spec: Spec): Motion {
        val split = spec.has("split")
        val catchPose = if (split) overhead(0.27, 0.8).copy(footL = v(0.15, Kit.ANKLE, 0.38), footR = v(-0.15, Kit.ANKLE, -0.48), toesR = 0.8, kneePoleR = v(0.0, -0.5, 1.0))
        else overhead(0.27, 0.84, 4 * DEG)
        val poses = mapOf(
            "setup" to rack(Body.STAND - 0.02, 2 * DEG, 0.0), "grip" to rack(Body.STAND - 0.02, 2 * DEG, 0.0),
            "stand" to rack(Body.STAND - 0.02, 2 * DEG, 0.0), "dip" to rack(0.84, 4 * DEG, -0.04),
            "drive" to overhead(0.27, Body.STAND + 0.05).copy(toes = 1.0), "catch" to catchPose,
            "lock" to overhead(0.27, Body.STAND - 0.02)
        )
        return Motion(
            poses, Gear.rack(-0.5, 1.4, 0.62, 1.6), bar(spec),
            Kit.flow("setup", "grip", listOf("stand", "dip", "drive", "catch", "lock"), Focus.KNEES, listOf(0.6, 0.25, 0.3, 0.6, 0.9),
                setupNote = "Bar in the front rack, feet hip width, weight on the heels.",
                gripNote = "Just outside shoulder width, full hand round the bar, elbows a little in front of the bar.",
                startFocus = Focus.CORE, cameraYaw = 75 * DEG),
            SIDE.copy(target = v(0.0, 1.2, 0.0))
        )
    }

    private fun thruster(spec: Spec): Motion {
        val db = spec.gear == "DUMBBELL"
        fun rk(y: Double, p: Double, z: Double) = rack(y, p, z).let { if (db) it.copy(handL = it.handL + v(0.05, 0.02, -0.02), handR = it.handR + v(-0.05, 0.02, -0.02)) else it }
        val poses = mapOf(
            "setup" to rk(Body.STAND - 0.02, 2 * DEG, 0.0), "grip" to rk(Body.STAND - 0.02, 2 * DEG, 0.0),
            "top" to rk(Body.STAND - 0.02, 2 * DEG, 0.0), "bottom" to rk(0.52, 20 * DEG, -0.24),
            "drive" to rk(Body.STAND - 0.02, 2 * DEG, 0.0).copy(toes = 0.4), "press" to overhead(if (db) 0.24 else 0.27, Body.STAND - 0.02)
        )
        return Motion(
            poses, emptyList(), bar(spec),
            Kit.flow("setup", "grip", listOf("top", "bottom", "drive", "press"), Focus.KNEES, listOf(1.2, 0.5, 0.4, 0.9),
                setupNote = "Feet shoulder width, toes slightly out, weight in the front rack.",
                gripNote = if (db) "A dumbbell on each shoulder, elbows forward." else "Bar in the front rack, hands just outside the shoulders.",
                startFocus = Focus.CORE, cameraYaw = 70 * DEG),
            SIDE
        )
    }

    // ---- kettlebells -------------------------------------------------------------

    private fun swingBottom() = Pose(
        pelvis = v(0.0, 0.82, -0.22), pitch = 62 * DEG, head = -0.5,
        handL = v(0.03, 0.55, -0.12), handR = v(-0.03, 0.55, -0.12),
        footL = v(0.22, Kit.ANKLE, 0.0), footR = v(-0.22, Kit.ANKLE, 0.0),
        elbowPole = v(0.1, 0.0, -1.0), kneePole = v(0.3, 0.0, 1.0), footYaw = 0.0
    )

    private fun swing(spec: Spec): Motion {
        val top = Kit.stand(0.22).copy(pelvis = v(0.0, Body.STAND - 0.01, 0.0), handL = v(0.03, 1.25, 0.58), handR = v(-0.03, 1.25, 0.58), elbowPole = v(0.2, -1.0, -0.2))
        val bottom = swingBottom()
        return Motion(
            mapOf("setup" to bottom.copy(pelvis = bottom.pelvis.withY(Kit.pelvisToReach(v(0.03, 0.2, 0.3), bottom.pitch, bottom.pelvis.z)),
                handL = v(0.03, 0.2, 0.3), handR = v(-0.03, 0.2, 0.3)), "grip" to bottom, "bottom" to bottom, "top" to top),
            emptyList(),
            Rigging { s, _ ->
                val m = (s.handL + s.handR) * 0.5
                val down = (m - (s.shoulderL + s.shoulderR) * 0.5).normalized()
                Gear.kettlebell(m, down)
            },
            Kit.steps("setup", "grip", "bottom", "top", Focus.HIPS,
                setupNote = "Feet a little wider than the hips, the bell a foot in front of you on the floor.",
                gripNote = "Both hands on the handle, thumbs wrapped. Hike it back between the legs like a football snap.",
                startFocus = Focus.BACK, upSeconds = 0.5, downSeconds = 0.6, cameraYaw = 80 * DEG),
            SIDE
        )
    }

    private fun kbSnatchOrClean(spec: Spec): Motion {
        val snatch = spec.has("snatch")
        val bottom = swingBottom().copy(handL = v(0.24, 0.8, 0.0), handR = v(-0.04, 0.55, -0.12))
        val mid = Kit.stand(0.22).copy(pelvis = v(0.0, Body.STAND - 0.01, 0.0), handR = v(-0.12, 1.22, 0.3), elbowPoleR = v(0.8, 0.5, -0.4))
        val racked = mid.copy(handR = v(-0.12, 1.3, 0.17), elbowPoleR = v(0.2, -1.0, 0.3))
        val up = mid.copy(handR = v(-0.19, 2.0, 0.0), elbowPoleR = v(0.6, 0.0, -1.0))
        val rep = if (snatch) listOf("bottom", "mid", "up") else listOf("bottom", "rack", "up", "rack")
        return Motion(
            mapOf("setup" to bottom, "grip" to bottom, "bottom" to bottom, "mid" to mid, "rack" to racked, "up" to up),
            emptyList(),
            Rigging { s, _ -> Gear.kettlebell(s.handR, (s.handR - s.elbowR).normalized() * -1.0) },
            Kit.flow("setup", "grip", rep, Focus.SHOULDERS, if (snatch) listOf(0.6, 0.4, 0.8) else listOf(0.6, 0.8, 0.8, 0.8),
                setupNote = "Feet a little wider than the hips, the bell between the feet.",
                gripNote = "One hand on the handle, gripped at the corner. Punch the hand through at the top so the bell rolls round the wrist.",
                startFocus = Focus.BACK, cameraYaw = 60 * DEG),
            SIDE.copy(target = v(0.0, 1.15, 0.0))
        )
    }

    private fun getUp(): Motion {
        // Right arm holds the bell straight up throughout.
        fun pose(pelvis: Vec3, pitch: Double, roll: Double, handL: Vec3, footL: Vec3, footR: Vec3, kneePoleR: Vec3? = null, toesR: Double? = null): Pose {
            val b = Basis.of(0.0, pitch, roll)
            val shR = pelvis + b.up * (Body.SPINE - 0.05) - b.side * Body.SHOULDER_HALF
            val shL = pelvis + b.up * (Body.SPINE - 0.05) + b.side * Body.SHOULDER_HALF
            // A support hand on the floor sits where the arm can actually reach, in the direction asked.
            val support = if (handL.y > 0.1) handL else {
                val flat = v(handL.x - shL.x, 0.0, handL.z - shL.z)
                val dy = (shL.y - handL.y).coerceAtMost(Kit.ARM_REACH - 0.02)
                val most = kotlin.math.sqrt((Kit.ARM_REACH - 0.02).let { it * it } - dy * dy)
                val d = flat.length.coerceAtMost(most).coerceAtLeast(1e-6)
                val dir = if (flat.length < 1e-6) Vec3.X else flat.normalized()
                v(shL.x + dir.x * d, handL.y, shL.z + dir.z * d)
            }
            return Pose(
                pelvis = pelvis, pitch = pitch, roll = roll, head = 0.0,
                handL = support, handR = shR + v(0.0, 0.6, 0.0),
                footL = footL, footR = footR, elbowPole = v(0.6, -1.0, 0.0), elbowPoleR = v(0.3, 0.0, -1.0),
                kneePole = v(0.2, 0.0, 1.0), kneePoleR = kneePoleR, toesR = toesR
            )
        }
        val lying = pose(v(0.0, 0.11, 0.0), -90 * DEG, 0.0, v(0.45, 0.03, -0.2), v(0.3, Kit.ANKLE, 0.82), v(-0.12, Kit.ANKLE, 0.42))
        val elbow = pose(v(0.0, 0.12, 0.0), -45 * DEG, 25 * DEG, v(0.38, 0.03, -0.12), v(0.3, Kit.ANKLE, 0.82), v(-0.12, Kit.ANKLE, 0.42))
        val hand = pose(v(0.0, 0.18, 0.0), -15 * DEG, 30 * DEG, v(0.45, 0.03, -0.18), v(0.3, Kit.ANKLE, 0.82), v(-0.12, Kit.ANKLE, 0.42))
        // Hips as high as a straight support arm allows: the left shoulder sits an arm's length above the hand.
        val bridgeTilt = Basis.of(0.0, -40 * DEG, 35 * DEG)
        val bridgeY = 0.03 + Kit.ARM_REACH - 0.06 - (bridgeTilt.up * (Body.SPINE - 0.05) + bridgeTilt.side * Body.SHOULDER_HALF).y
        val bridge = pose(v(0.0, bridgeY, 0.15), -40 * DEG, 35 * DEG, v(0.45, 0.03, -0.18), v(0.25, Kit.ANKLE, 0.82), v(-0.12, Kit.ANKLE, 0.5))
        val kneel = pose(v(0.0, 0.58, 0.1), 0.0, 0.0, v(0.3, 0.82, 0.12), v(0.12, Kit.ANKLE, -0.32), v(-0.12, Kit.ANKLE, 0.55), toesR = null)
            .copy(kneePole = v(0.1, -1.0, 0.5), footPitch = 70 * DEG, kneePoleR = v(0.1, 0.3, 1.0))
        val stand = pose(v(0.0, Body.STAND - 0.01, 0.0), 0.0, 0.0, v(0.26, 0.82, 0.03), v(0.15, Kit.ANKLE, 0.0), v(-0.15, Kit.ANKLE, 0.0))
        val rep = listOf("lying", "elbow", "hand", "bridge", "kneel", "stand", "kneel", "hand", "lying")
        return Motion(
            mapOf("setup" to lying, "grip" to lying, "lying" to lying, "elbow" to elbow, "hand" to hand, "bridge" to bridge, "kneel" to kneel, "stand" to stand),
            listOf(Prim.Box(v(0.0, 0.006, 0.2), Basis.IDENTITY, v(0.6, 0.006, 0.9), Palette.PAD)),
            Rigging { s, _ -> Gear.kettlebell(s.handR + v(0.0, 0.06, 0.0), (s.handR - s.elbowR).normalized() * -1.0) },
            Kit.flow("setup", "grip", rep, Focus.SHOULDERS, listOf(1.4, 1.2, 1.2, 1.4, 1.6, 1.6, 1.4, 1.2, 1.4),
                setupNote = "Lie on your back with the bell beside you. Same-side knee bent, foot flat; other leg and arm out at 45 degrees.",
                gripNote = "Press the bell straight up with one hand and keep your eyes on it the whole way. Wrist straight, arm locked.",
                startFocus = Focus.SHOULDERS, cameraYaw = 40 * DEG),
            Camera(yaw = 40 * DEG, pitch = 14 * DEG, distance = 3.8, target = v(0.0, 0.7, 0.2))
        )
    }

    // ---- jumps and conditioning ----------------------------------------------------

    private fun squatPose(pelvisY: Double, pitch: Double, z: Double = 0.0, feetY: Double = Kit.ANKLE, feetZ: Double = 0.0): Pose {
        val pelvis = v(0.0, pelvisY, z - 0.2 * (1 - (pelvisY - 0.5) / 0.45).coerceIn(0.0, 1.0))
        return Pose(
            pelvis = pelvis, pitch = pitch, head = -pitch * 0.5,
            handL = Kit.onTorso(pelvis, pitch, v(0.2, Body.SPINE - 0.4, 0.3)), handR = Kit.onTorso(pelvis, pitch, v(-0.2, Body.SPINE - 0.4, 0.3)),
            footL = v(0.17, feetY, feetZ), footR = v(-0.17, feetY, feetZ), elbowPole = v(0.3, -1.0, -0.3), kneePole = v(0.3, 0.0, 1.0)
        )
    }

    private fun jump(spec: Spec, box: Boolean, broad: Boolean): Motion {
        val dip = squatPose(0.62, 35 * DEG).copy(handL = v(0.25, 0.72, -0.25), handR = v(-0.25, 0.72, -0.25), elbowPole = v(0.2, 1.0, -0.5))
        val air = squatPose(Body.STAND + 0.32, -2 * DEG, if (broad) 0.45 else if (box) 0.3 else 0.0).let {
            it.copy(footL = it.pelvis + v(0.15, -0.86, 0.02), footR = it.pelvis + v(-0.15, -0.86, 0.02), footPitch = 40 * DEG,
                handL = v(0.25, 1.9, it.pelvis.z + 0.2), handR = v(-0.25, 1.9, it.pelvis.z + 0.2), elbowPole = v(0.3, 0.0, -1.0))
        }
        val land = if (box) squatPose(0.6 + 0.62, 25 * DEG, 0.62, 0.6 + Kit.ANKLE, 0.62)
        else if (broad) squatPose(0.66, 32 * DEG, 0.95, Kit.ANKLE, 0.95) else squatPose(0.66, 28 * DEG)
        val stand = stand()
        val fixed = if (box) listOf(Gear.block(v(0.0, 0.3, 0.62), v(0.35, 0.3, 0.28))) else emptyList()
        return Motion(
            mapOf("setup" to stand, "grip" to stand, "stand" to stand, "dip" to dip, "air" to air, "land" to land),
            fixed, Rigging { _, _ -> emptyList() },
            Kit.flow("setup", "grip", listOf("stand", "dip", "air", "land"), Focus.KNEES, listOf(0.6, 0.3, 0.35, 0.9),
                setupNote = if (box) "Stand a short step from a sturdy box, feet hip width." else "Feet hip width, plenty of space in front.",
                gripNote = "Swing the arms back as you dip, then throw them up as you jump. Land softly with bent knees.",
                startFocus = Focus.KNEES, cameraYaw = 80 * DEG),
            SIDE.copy(target = v(0.0, 0.9, if (broad) 0.45 else 0.2))
        )
    }

    private fun plankTop(kneeL: Boolean = false, kneeR: Boolean = false, dumbbells: Boolean = false): Pose {
        val sh = v(0.0, 0.64, 0.45)
        val ankle = v(0.0, Kit.ANKLE, sh.z - sqrt(Kit.LINE * Kit.LINE - (sh.y - Kit.ANKLE) * (sh.y - Kit.ANKLE)))
        val (pelvis, pitch) = Kit.plank(ankle, sh)
        val handY = if (dumbbells) 0.1 else 0.03
        return Pose(
            pelvis = pelvis, pitch = pitch, head = -0.9,
            handL = v(0.2, handY, sh.z + 0.02), handR = v(-0.2, handY, sh.z + 0.02),
            footL = if (kneeL) pelvis + v(0.12, -0.25, 0.42) else ankle + v(0.1, 0.0, 0.0),
            footR = if (kneeR) pelvis + v(-0.12, -0.25, 0.42) else ankle + v(-0.1, 0.0, 0.0),
            elbowPole = v(0.4, 0.0, -1.0), kneePole = v(0.0, -1.0, 0.2), footPitch = 75 * DEG
        )
    }

    fun mountainClimber(): Motion {
        val a = plankTop(kneeL = true)
        val b = plankTop(kneeR = true)
        return Motion(
            mapOf("setup" to plankTop(), "grip" to plankTop(), "bottom" to a, "top" to b), emptyList(), Rigging { _, _ -> emptyList() },
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "Top of a push-up: hands under the shoulders, body in one line.",
                gripNote = "Hands flat, fingers spread. Drive one knee towards the chest, then switch.",
                startFocus = Focus.CORE, upSeconds = 0.35, downSeconds = 0.35, cameraYaw = 75 * DEG),
            Camera(yaw = 75 * DEG, pitch = 14 * DEG, distance = 3.2, target = v(0.0, 0.4, 0.0))
        )
    }

    private fun burpee(dumbbells: Boolean): Motion {
        val stand = stand()
        val floorHand = v(0.2, if (dumbbells) 0.1 else 0.03, 0.36)
        val crouch = squatPose(0.55, 60 * DEG, 0.0).let {
            it.copy(pelvis = v(0.0, Kit.pelvisToReach(floorHand, 60 * DEG, -0.12), -0.12), handL = floorHand, handR = floorHand.withX(-0.2), elbowPole = v(0.4, 0.0, -1.0))
        }
        val plank = plankTop(dumbbells = dumbbells)
        val chest = plank.copy(pelvis = plank.pelvis - v(0.0, 0.3, 0.0), pitch = plank.pitch + 4 * DEG).let { if (dumbbells) plank else it }
        val top = if (dumbbells) stand.copy(handL = v(0.24, 1.98, 0.0), handR = v(-0.24, 1.98, 0.0), elbowPole = v(0.6, 0.0, -1.0))
        else squatPose(Body.STAND + 0.2, 0.0).let {
            it.copy(footL = it.pelvis + v(0.15, -0.86, 0.0), footR = it.pelvis + v(-0.15, -0.86, 0.0), footPitch = 35 * DEG,
                handL = v(0.22, 2.0, 0.05), handR = v(-0.22, 2.0, 0.05), elbowPole = v(0.3, 0.0, -1.0))
        }
        val rep = listOf("stand", "crouch", "plank", "chest", "plank", "crouch", "top")
        return Motion(
            mapOf("setup" to stand, "grip" to crouch, "stand" to stand, "crouch" to crouch, "plank" to plank, "chest" to chest, "top" to top),
            emptyList(),
            if (dumbbells) Rigging { s, _ -> Gear.dumbbell(s.handL, Vec3.Z) + Gear.dumbbell(s.handR, Vec3.Z) } else Rigging { _, _ -> emptyList() },
            Kit.flow("setup", "grip", rep, Focus.CORE, listOf(0.5, 0.4, 0.5, 0.5, 0.4, 0.5, 0.6),
                setupNote = "Stand with feet hip width${if (dumbbells) ", a dumbbell outside each foot" else ""}.",
                gripNote = if (dumbbells) "Hands on the dumbbell handles, which point forward." else "Hands flat on the floor just in front of the feet.",
                startFocus = Focus.CORE, cameraYaw = 75 * DEG),
            SIDE.copy(target = v(0.0, 0.8, 0.0))
        )
    }

    private fun sled(spec: Spec): Motion {
        val push = spec.has("push")
        fun stride(left: Boolean): Pose {
            val pitch = if (push) 50 * DEG else -18 * DEG
            val pelvis = v(0.0, if (push) 0.86 else 0.9, 0.0)
            val forward = if (push) 0.3 else -0.28
            val back = if (push) -0.4 else 0.2
            val handZ = if (push) 0.62 else 0.3
            val handY = if (push) 1.1 else 0.95
            return Pose(
                pelvis = pelvis, pitch = pitch, head = if (push) -0.5 else 0.2,
                handL = v(0.22, handY, handZ), handR = v(-0.22, handY, handZ),
                footL = v(0.13, Kit.ANKLE, if (left) forward else back), footR = v(-0.13, Kit.ANKLE, if (left) back else forward),
                elbowPole = v(0.4, -1.0, -0.4), kneePole = v(0.1, 0.0, 1.0),
                toes = if (left) 0.0 else 0.7, toesR = if (left) 0.7 else 0.0
            )
        }
        val sledZ = if (push) 0.85 else 1.25
        val sledPrims = listOf(
            Prim.Box(v(0.0, 0.08, sledZ + 0.25), Basis.IDENTITY, v(0.35, 0.06, 0.35), Palette.FRAME),
            Prim.Box(v(0.0, 0.25, sledZ + 0.25), Basis.IDENTITY, v(0.06, 0.15, 0.06), Palette.STEEL)
        ) + Gear.plate(v(0.0, 0.35, sledZ + 0.25), Vec3.Y) +
            (if (push) listOf(0.22, -0.22).map { x -> Prim.Capsule(v(x, 0.12, sledZ), v(x, 1.2, sledZ - 0.25), 0.02, 0.02, Palette.STEEL) } else emptyList())
        return Motion(
            mapOf("setup" to stride(true), "grip" to stride(true), "bottom" to stride(true), "top" to stride(false)),
            sledPrims,
            if (push) Rigging { _, _ -> emptyList() } else Rigging { s, _ -> listOf(Prim.Line(s.handL, v(0.15, 0.12, sledZ), 0.012, Palette.PAD), Prim.Line(s.handR, v(-0.15, 0.12, sledZ), 0.012, Palette.PAD)) },
            Kit.steps("setup", "grip", "bottom", "top", Focus.KNEES,
                setupNote = if (push) "Hands on the uprights, arms straight, body leaning in at about 45 degrees." else "Face the sled, straps in your hands, sit back slightly.",
                gripNote = if (push) "Drive through the balls of the feet with short powerful steps." else "Walk backwards with short steps, arms long, chest up.",
                startFocus = Focus.BACK, upSeconds = 0.45, downSeconds = 0.45, cameraYaw = 80 * DEG),
            SIDE.copy(target = v(0.0, 0.8, 0.3))
        )
    }

    private fun bearCrawl(): Motion {
        fun at(leftForward: Boolean): Pose {
            val pelvis = v(0.0, 0.58, -0.2)
            val f = if (leftForward) 0.08 else -0.08
            return Pose(
                pelvis = pelvis, pitch = 85 * DEG, head = -0.9,
                handL = v(0.18, 0.03, 0.35 + f), handR = v(-0.18, 0.03, 0.35 - f),
                footL = v(0.14, Kit.ANKLE, -0.52 - f), footR = v(-0.14, Kit.ANKLE, -0.52 + f),
                elbowPole = v(0.3, 0.0, -1.0), kneePole = v(0.1, -0.4, 1.0), toes = 0.9
            )
        }
        return Motion(
            mapOf("setup" to at(true), "grip" to at(true), "bottom" to at(true), "top" to at(false)), emptyList(), Rigging { _, _ -> emptyList() },
            Kit.steps("setup", "grip", "bottom", "top", Focus.CORE,
                setupNote = "On hands and toes, knees bent and hovering just off the floor, back flat.",
                gripNote = "Hands under the shoulders. Move the opposite hand and foot together in small steps.",
                startFocus = Focus.CORE, upSeconds = 0.5, downSeconds = 0.5, cameraYaw = 75 * DEG),
            Camera(yaw = 70 * DEG, pitch = 16 * DEG, distance = 3.2, target = v(0.0, 0.4, 0.0))
        )
    }

    private fun ropes(): Motion {
        val base = squatPose(0.75, 20 * DEG)
        val a = base.copy(handL = v(0.22, 1.25, 0.45), handR = v(-0.22, 0.75, 0.5))
        val b = base.copy(handL = v(0.22, 0.75, 0.5), handR = v(-0.22, 1.25, 0.45))
        val anchor = v(0.0, 0.15, 3.0)
        return Motion(
            mapOf("setup" to base, "grip" to base, "bottom" to a, "top" to b), listOf(Prim.Ball(anchor, 0.08, Palette.FRAME)),
            Rigging { s, _ ->
                listOf(s.handL, s.handR).flatMap { h ->
                    val mid1 = lerp(h, anchor, 0.33) + v(0.0, (h.y - 1.0) * 0.6, 0.0)
                    val mid2 = lerp(h, anchor, 0.66) - v(0.0, (h.y - 1.0) * 0.3, 0.0)
                    listOf(Prim.Line(h, mid1, 0.03, Palette.PAD), Prim.Line(mid1, mid2, 0.03, Palette.PAD), Prim.Line(mid2, anchor, 0.03, Palette.PAD))
                }
            },
            Kit.steps("setup", "grip", "bottom", "top", Focus.SHOULDERS,
                setupNote = "Feet shoulder width, half squat, chest up, facing the anchor.",
                gripNote = "One rope end in each hand, thumbs on top. Whip the arms up and down, one after the other.",
                startFocus = Focus.KNEES, upSeconds = 0.25, downSeconds = 0.25, cameraYaw = 60 * DEG),
            SIDE.copy(target = v(0.0, 0.9, 0.6))
        )
    }

    private fun skip(): Motion {
        val down = stand().copy(toes = 0.7, pelvis = v(0.0, Body.STAND + 0.02, 0.0),
            handL = v(0.3, 0.98, 0.1), handR = v(-0.3, 0.98, 0.1), elbowPole = v(0.3, -1.0, -0.5),
            footL = v(0.1, Kit.ANKLE + 0.05, 0.0), footR = v(-0.1, Kit.ANKLE + 0.05, 0.0))
        val up = down.copy(pelvis = down.pelvis + v(0.0, 0.06, 0.0), footL = down.footL + v(0.0, 0.06, 0.0), footR = down.footR + v(0.0, 0.06, 0.0), toes = 1.0, footPitch = 20 * DEG)
        return Motion(
            mapOf("setup" to down, "grip" to down, "bottom" to down, "top" to up), emptyList(),
            Rigging { s, p ->
                val a = s.handL; val b = s.handR
                val low = p.pelvis.y > Body.STAND + 0.05
                val y = if (low) 0.02 else 2.0
                listOf(Prim.Line(a, v(0.35, y, if (low) 0.25 else -0.1), 0.006, Palette.CABLE), Prim.Line(b, v(-0.35, y, if (low) 0.25 else -0.1), 0.006, Palette.CABLE),
                    Prim.Line(v(0.35, y, if (low) 0.25 else -0.1), v(-0.35, y, if (low) 0.25 else -0.1), 0.006, Palette.CABLE))
            },
            Kit.steps("setup", "grip", "bottom", "top", Focus.FEET,
                setupNote = "Stand tall, feet together, on the balls of the feet.",
                gripNote = "Handles held loosely at hip height, elbows close. Turn the rope with the wrists, small low hops.",
                startFocus = Focus.FEET, upSeconds = 0.2, downSeconds = 0.2, cameraYaw = 40 * DEG),
            SIDE.copy(yaw = 40 * DEG)
        )
    }

    private fun sandbag(): Motion {
        val low = squatPose(0.5, 40 * DEG).copy(handL = v(0.12, 0.55, 0.35), handR = v(-0.12, 0.55, 0.35), elbowPole = v(0.6, -1.0, 0.2))
        val lap = squatPose(0.6, 20 * DEG).copy(handL = v(0.15, 0.95, 0.25), handR = v(-0.15, 0.95, 0.25), elbowPole = v(0.6, -1.0, 0.3))
        val top = stand().copy(handL = v(0.05, 1.45, 0.02), handR = v(-0.25, 1.48, 0.1), elbowPole = v(0.6, -0.5, 0.4), toes = 0.4)
        return Motion(
            mapOf("setup" to low, "grip" to low, "low" to low, "lap" to lap, "top" to top), emptyList(),
            Rigging { s, p ->
                val c = if (p.handL.y > 1.3) s.shoulderR + v(0.0, 0.12, -0.05) else (s.handL + s.handR) * 0.5 + v(0.0, 0.05, 0.08)
                listOf(Prim.Capsule(c - v(0.2, 0.0, 0.0), c + v(0.2, 0.0, 0.0), 0.14, 0.14, Palette.BAND))
            },
            Kit.flow("setup", "grip", listOf("low", "lap", "top"), Focus.HIPS, listOf(0.9, 0.6, 1.0),
                setupNote = "Bag between the feet, squat down with a flat back.",
                gripNote = "Arms wrapped under and round the bag, hugging it to the body. Stand up explosively and roll it over one shoulder.",
                startFocus = Focus.BACK, cameraYaw = 60 * DEG),
            SIDE
        )
    }
}
