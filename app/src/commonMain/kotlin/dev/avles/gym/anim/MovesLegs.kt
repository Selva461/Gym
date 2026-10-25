package dev.avles.gym.anim

import kotlin.math.cos
import kotlin.math.sin

/** Legs and hips: squats, leg press, lunges, hinges, bridges, curls, extensions, calves. */
object MovesLegs {

    private const val CAM_SIDE = 80 * DEG

    /** Where the hands go for the way a squat or hinge is loaded. */
    private fun load(spec: Spec, pelvis: Vec3, pitch: Double, half: Double = 0.42): Triple<Vec3, Vec3, Vec3> {
        fun t(x: Double, y: Double, z: Double) = Kit.onTorso(pelvis, pitch, v(x, y, z))
        val sh = t(0.0, Body.SPINE - 0.05, 0.0)
        return when {
            spec.has("front squat", "front rack", "clean grip") || (spec.has("front") && spec.pattern == "SQUAT") ->
                Triple(t(0.2, Body.SPINE - 0.03, 0.17), t(-0.2, Body.SPINE - 0.03, 0.17), v(0.4, 0.3, 1.0))
            spec.has("goblet") -> Triple(t(0.06, Body.SPINE - 0.2, 0.23), t(-0.06, Body.SPINE - 0.2, 0.23), v(0.3, -1.0, 0.5))
            spec.has("zercher") -> Triple(t(0.12, Body.SPINE - 0.3, 0.3), t(-0.12, Body.SPINE - 0.3, 0.3), v(0.4, -1.0, 0.2))
            spec.has("overhead") -> Triple(sh + v(0.55, 0.52, -0.05), sh + v(-0.55, 0.52, -0.05), v(1.0, 0.0, 0.0))
            spec.gear == "DUMBBELL" || spec.gear == "KETTLEBELL" || spec.gear == "TRAP_BAR" || spec.has("suitcase", "farmer") ->
                Triple(v(0.26, sh.y - 0.6, sh.z), v(-0.26, sh.y - 0.6, sh.z), v(0.2, -1.0, -0.3))
            spec.gear == "BODYWEIGHT" || spec.gear == "OTHER" || spec.gear == "MACHINE" ->
                Triple(sh + v(0.14, -0.06, 0.55), sh + v(-0.14, -0.06, 0.55), v(0.3, -1.0, -0.2))
            else -> Triple(t(half, Body.SPINE - 0.02, -0.08), t(-half, Body.SPINE - 0.02, -0.08), v(0.6, -1.0, -0.6))
        }
    }

    private fun loadRig(spec: Spec, rails: Boolean): Rigging = Rigging { s, _ ->
        val out = mutableListOf<Prim>()
        when {
            spec.has("goblet") -> out += Gear.dumbbell((s.handL + s.handR) * 0.5 - v(0.0, 0.04, 0.0), Vec3.Y)
            spec.gear == "TRAP_BAR" -> out += Gear.trapBar(s.handL, s.handR)
            spec.gear == "KETTLEBELL" -> { out += Gear.kettlebell(s.handL); out += Gear.kettlebell(s.handR) }
            spec.gear == "DUMBBELL" -> { out += Gear.dumbbell(s.handL, Vec3.Z); out += Gear.dumbbell(s.handR, Vec3.Z) }
            spec.gear == "BARBELL" || spec.gear == "SMITH" || spec.gear == "EZ_BAR" -> out += Gear.barbell(s.handL, s.handR)
            spec.gear == "PLATE" -> out += Gear.plate((s.handL + s.handR) * 0.5, Vec3.Z)
        }
        if (rails) listOf(0.66, -0.66).forEach { x ->
            out += Prim.Box(v(x, 1.1, -0.05), Basis.IDENTITY, v(0.03, 1.1, 0.03), Palette.FRAME)
        }
        out
    }

    /** Back, front, goblet, box, Smith, overhead, bodyweight and one-leg squats. */
    fun squat(spec: Spec): Motion {
        if (spec.has("leg press")) return legPress(spec)
        if (spec.has("hack", "pendulum", "v-squat", "belt squat")) return machineSquat(spec)
        val wide = spec.wideStance
        val stance = if (wide) 0.36 else 0.2
        val upright = spec.has("front", "goblet", "zercher", "overhead") || spec.gear == "DUMBBELL"
        val depth = if (spec.has("box")) 0.56 else if (spec.has("wall sit")) 0.53 else 0.5
        val pitchDown = when {
            spec.has("sissy") -> -25 * DEG
            spec.has("wall sit") -> 0.0
            upright -> 20 * DEG
            wide -> 25 * DEG
            spec.has("low-bar") -> 45 * DEG
            else -> 35 * DEG
        }
        val pelvisDown = v(0.0, depth, if (spec.has("sissy")) 0.25 else if (spec.has("wall sit")) -0.32 else -0.26)
        val pelvisUp = v(0.0, Body.STAND - 0.01, -0.02)
        fun pose(pelvis: Vec3, pitch: Double, knees: Vec3): Pose {
            val (l, r, elbow) = load(spec, pelvis, pitch)
            return Pose(
                pelvis = pelvis, pitch = pitch, head = -pitch * 0.5,
                handL = l, handR = r,
                footL = v(stance, Kit.ANKLE, 0.0), footR = v(-stance, Kit.ANKLE, 0.0),
                elbowPole = elbow, kneePole = knees, footYaw = 0.0,
                toes = if (spec.has("sissy")) 0.8 else 0.0
            )
        }
        val up = pose(pelvisUp, 4 * DEG, v(if (wide) 0.6 else 0.25, 0.0, 1.0))
        val down = pose(pelvisDown, pitchDown, v(if (wide) 0.8 else 0.3, 0.0, 1.0))
        val fixed = mutableListOf<Prim>()
        if (spec.gear == "BARBELL" && !spec.has("overhead", "zercher")) fixed += Gear.rack(-0.3, up.handL.y, 0.62, 1.9)
        if (spec.has("box")) fixed += Gear.block(v(0.0, 0.23, -0.38), v(0.22, 0.23, 0.2))
        if (spec.has("wall sit")) fixed += Prim.Box(v(0.0, 1.0, -0.62), Basis.IDENTITY, v(0.8, 1.0, 0.03), Palette.FRAME)
        val oneLeg = spec.oneLeg
        val upP = if (oneLeg) up.copy(footR = v(-0.12, 0.35, 0.35), kneePoleR = v(0.0, 1.0, 1.0)) else up
        val downP = if (oneLeg) down.copy(footL = v(0.05, Kit.ANKLE, 0.0), footR = v(-0.15, 0.35, 0.55), kneePoleR = v(0.0, 1.0, 1.0)) else down
        return Motion(
            mapOf("setup" to upP, "grip" to upP, "top" to upP, "bottom" to downP),
            fixed, loadRig(spec, spec.gear == "SMITH"),
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.KNEES,
                setupNote = Kit.stanceNote(spec),
                gripNote = when {
                    spec.has("goblet") -> "Hold the dumbbell vertically against the chest, both hands cupped under the top plate."
                    spec.has("front") -> "Bar resting on the front of the shoulders, fingertips under it, elbows pointing forward."
                    spec.gear == "BARBELL" || spec.gear == "SMITH" -> "Bar on the upper back, not the neck. Hands just outside the shoulders, elbows down."
                    spec.gear == "DUMBBELL" -> "A dumbbell in each hand at your sides, arms long."
                    else -> "Arms out in front for balance."
                },
                startFocus = Focus.CORE, upSeconds = 1.0, downSeconds = 2.0, cameraYaw = CAM_SIDE
            ),
            Camera(yaw = 55 * DEG, pitch = 8 * DEG, distance = 3.8, target = v(0.0, 0.9, 0.0))
        )
    }

    private fun legPress(spec: Spec): Motion {
        val back = 55.0
        val pelvis = v(0.0, 0.42, -0.1)
        val pitch = -(90 - (90 - back)) * DEG // reclined
        val sledDir = v(0.0, sin(40 * DEG), cos(40 * DEG))
        val stance = if (spec.wideStance) 0.3 else 0.17
        val near = v(0.0, 0.62, 0.38)
        val far = near + sledDir * 0.36
        fun at(foot: Vec3) = Pose(
            pelvis = pelvis, pitch = pitch, head = 0.3,
            handL = v(0.28, 0.42, 0.05), handR = v(-0.28, 0.42, 0.05),
            footL = foot + v(stance, 0.0, 0.0), footR = foot + v(-stance, 0.0, 0.0),
            elbowPole = v(0.3, -1.0, 0.0), kneePole = v(0.25, 0.0, 1.0), footPitch = -50 * DEG
        )
        val bent = at(near)
        val straight = at(far)
        val seat = Gear.machineSeat(0.35, back, -0.08).dropLast(1)
        return Motion(
            mapOf("setup" to bent, "grip" to bent, "top" to straight, "bottom" to bent),
            seat + listOf(Prim.Capsule(v(0.0, 0.1, -0.3), v(0.0, 1.3, 1.6), 0.04, 0.04, Palette.FRAME)),
            Rigging { _, p ->
                val c = (p.footL + p.footR) * 0.5 + sledDir * 0.06
                listOf(Prim.Box(c, Basis(Vec3.X, v(0.0, cos(40 * DEG), -sin(40 * DEG)), sledDir), v(0.35, 0.3, 0.02), Palette.FRAME))
            },
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.KNEES,
                setupNote = "Back and hips flat on the pad, feet hip width in the middle of the platform.",
                gripNote = "Hold the side handles, release the safety catches, and do not lock the knees.",
                startFocus = Focus.HIPS, upSeconds = 1.0, downSeconds = 2.0, cameraYaw = CAM_SIDE
            ),
            Camera(yaw = 70 * DEG, pitch = 12 * DEG, distance = 3.6, target = v(0.0, 0.7, 0.3))
        )
    }

    private fun machineSquat(spec: Spec): Motion {
        val lean = -20 * DEG
        val stance = 0.18
        val feetZ = 0.35
        fun at(pelvis: Vec3) = Pose(
            pelvis = pelvis, pitch = lean, head = 0.1,
            handL = Kit.onTorso(pelvis, lean, v(0.24, Body.SPINE + 0.02, 0.05)),
            handR = Kit.onTorso(pelvis, lean, v(-0.24, Body.SPINE + 0.02, 0.05)),
            footL = v(stance, 0.2, feetZ), footR = v(-stance, 0.2, feetZ),
            elbowPole = v(0.6, -1.0, 0.0), kneePole = v(0.25, 0.0, 1.0), footPitch = -15 * DEG
        )
        val top = at(v(0.0, 1.0, -0.12))
        val bottom = at(v(0.0, 0.58, -0.2))
        return Motion(
            mapOf("setup" to top, "grip" to top, "top" to top, "bottom" to bottom),
            listOf(
                Prim.Box(v(0.0, 0.12, feetZ + 0.05), Basis.of(0.0, -15 * DEG), v(0.38, 0.03, 0.3), Palette.FRAME),
                Prim.Capsule(v(0.4, 0.1, -0.5), v(0.4, 1.9, -0.1), 0.04, 0.04, Palette.FRAME),
                Prim.Capsule(v(-0.4, 0.1, -0.5), v(-0.4, 1.9, -0.1), 0.04, 0.04, Palette.FRAME)
            ),
            Rigging { s, p ->
                val b = Basis.of(0.0, p.pitch)
                listOf(Prim.Box(p.pelvis + b.up * 0.3 - b.fwd * 0.17, Basis(b.side, b.fwd, -b.up), v(0.2, 0.03, 0.4), Palette.PAD)) +
                    listOf(s.shoulderL, s.shoulderR).map { sh -> Prim.Capsule(sh + b.up * 0.08 - b.fwd * 0.08, sh + b.up * 0.08 + b.fwd * 0.12, 0.05, 0.05, Palette.PAD) }
            },
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.KNEES,
                setupNote = "Back flat on the pad, shoulders under the pads, feet shoulder width and forward on the platform.",
                gripNote = "Hold the handles by the shoulder pads, release the safety.",
                startFocus = Focus.BACK, upSeconds = 1.0, downSeconds = 2.0, cameraYaw = CAM_SIDE
            ),
            Camera(yaw = 70 * DEG, pitch = 10 * DEG, distance = 3.7, target = v(0.0, 0.9, 0.0))
        )
    }

    /** Lunges forward, back and walking, split squats, Bulgarian split squats, step-ups, side lunges. */
    fun lunge(spec: Spec): Motion {
        if (spec.has("step-up", "step up")) return stepUp(spec)
        val side = spec.has("lateral", "side lunge", "cossack")
        val bulgarian = spec.has("bulgarian", "rear-foot", "rear foot")
        val frontZ = if (bulgarian) 0.3 else 0.4
        val backFoot = when {
            bulgarian -> v(-0.14, Gear.BENCH_TOP + 0.1, -0.62)
            spec.has("curtsy") -> v(0.12, 0.13, -0.38) // crossed behind the front leg
            else -> v(-0.14, 0.13, -0.42)
        }
        fun at(pelvisY: Double, pitch: Double, pelvisZ: Double): Pose {
            val pelvis = v(0.0, pelvisY, pelvisZ)
            val (l, r, elbow) = load(spec, pelvis, pitch)
            return Pose(
                pelvis = pelvis, pitch = pitch, head = -pitch * 0.4,
                handL = l, handR = r,
                footL = v(0.14, Kit.ANKLE, frontZ), footR = backFoot,
                elbowPole = elbow, kneePole = v(0.1, 0.0, 1.0),
                toesR = 0.0, footPitchR = if (bulgarian) -10 * DEG else 55 * DEG
            )
        }
        if (side) {
            fun sideAt(shift: Double, y: Double): Pose {
                val pelvis = v(shift, y, -0.05)
                val pitch = if (y < 0.8) 25 * DEG else 0.0
                val (l, r, elbow) = load(spec, pelvis, pitch)
                return Pose(
                    pelvis = pelvis, pitch = pitch,
                    handL = l, handR = r,
                    footL = v(0.5, Kit.ANKLE, 0.0), footR = v(-0.5, Kit.ANKLE, 0.0),
                    elbowPole = elbow, kneePole = v(0.6, 0.0, 1.0), footYaw = 0.0
                )
            }
            val top = sideAt(0.0, Body.STAND - 0.1)
            val bottom = sideAt(0.32, 0.55)
            return Motion(
                mapOf("setup" to top, "grip" to top, "top" to top, "bottom" to bottom),
                emptyList(), loadRig(spec, false),
                Kit.steps("setup", "grip", "top", "bottom", Focus.KNEES,
                    setupNote = "Feet wide, toes slightly out.", gripNote = "Hands together in front of the chest, or holding the weight there.",
                    startFocus = Focus.CORE, downSeconds = 2.0),
                Camera(yaw = 15 * DEG, pitch = 8 * DEG, distance = 3.8, target = v(0.0, 0.85, 0.0))
            )
        }
        val top = at(Body.STAND - 0.07, 4 * DEG, -0.02)
        val bottom = at(0.5, 12 * DEG, if (bulgarian) -0.12 else -0.04)
        val fixed = if (bulgarian) Gear.bench(0.0, -0.45, back = false) else emptyList()
        return Motion(
            mapOf("setup" to top, "grip" to top, "top" to top, "bottom" to bottom),
            fixed, loadRig(spec, spec.gear == "SMITH"),
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.KNEES,
                setupNote = if (bulgarian) "Back foot laces-down on the bench, front foot far enough forward that the knee stays over the foot."
                else "A long stride: front foot flat, back foot on the ball of the foot, hips square.",
                gripNote = when (spec.gear) {
                    "DUMBBELL" -> "A dumbbell in each hand at your sides."
                    "BARBELL", "SMITH" -> "Bar on the upper back, hands just outside the shoulders."
                    else -> "Hands on the hips or out in front for balance."
                },
                startFocus = Focus.CORE, upSeconds = 1.0, downSeconds = 1.8, cameraYaw = CAM_SIDE
            ),
            Camera(yaw = 70 * DEG, pitch = 8 * DEG, distance = 3.8, target = v(0.0, 0.85, 0.0))
        )
    }

    private fun stepUp(spec: Spec): Motion {
        val boxH = 0.45
        val boxZ = 0.32
        val knee = spec.has("knee")
        fun at(pelvis: Vec3, right: Vec3, pitch: Double): Pose {
            val (l, r, elbow) = load(spec, pelvis, pitch)
            return Pose(
                pelvis = pelvis, pitch = pitch, handL = l, handR = r,
                footL = v(0.13, boxH + Kit.ANKLE, boxZ), footR = right,
                elbowPole = elbow, kneePole = v(0.1, 0.0, 1.0), kneePoleR = if (knee) v(0.0, 1.0, 1.0) else null
            )
        }
        val bottom = at(v(0.0, 0.78, -0.02), v(-0.13, Kit.ANKLE, -0.15), 18 * DEG)
        val top = at(v(0.0, boxH + Body.STAND - 0.02, boxZ - 0.03), if (knee) v(-0.13, boxH + 0.5, boxZ + 0.25) else v(-0.13, boxH + Kit.ANKLE, boxZ), 2 * DEG)
        return Motion(
            mapOf("setup" to bottom, "grip" to bottom, "top" to bottom, "bottom" to top),
            listOf(Gear.block(v(0.0, boxH / 2, boxZ + 0.05), v(0.3, boxH / 2, 0.22))),
            loadRig(spec, false),
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.KNEES,
                setupNote = "Whole foot on the box, knee over the toes. Push through that heel, not the back foot.",
                gripNote = if (spec.gear == "DUMBBELL") "A dumbbell in each hand at your sides." else "Arms relaxed at your sides.",
                startFocus = Focus.CORE, upSeconds = 1.8, downSeconds = 1.2, cameraYaw = CAM_SIDE
            ),
            Camera(yaw = 65 * DEG, pitch = 8 * DEG, distance = 3.9, target = v(0.0, 1.0, 0.1))
        )
    }

    /** Deadlifts, Romanian deadlifts, good mornings, rack pulls, back extensions. */
    fun hinge(spec: Spec): Motion {
        if (spec.has("glute ham", "glute-ham")) return nordic(spec)
        if (spec.has("reverse hyper")) return reverseHyper()
        if (spec.has("back extension", "hyperextension", "45-degree")) return backExtension(spec)
        if (spec.has("good morning") && spec.has("seated")) return seatedGoodMorning(spec)
        val good = spec.has("good morning")
        val rdl = spec.has("romanian", "rdl", "stiff", "single-leg", "b-stance") || good
        val sumo = spec.has("sumo")
        val trap = spec.gear == "TRAP_BAR"
        val rack = spec.has("rack pull", "block pull")
        val deficit = spec.has("deficit")
        val stance = if (sumo) 0.38 else 0.15
        val half = when {
            sumo -> 0.16
            trap -> 0.3
            spec.has("snatch") -> 0.6
            spec.gear == "DUMBBELL" || spec.gear == "KETTLEBELL" -> 0.18
            else -> 0.24
        }
        val footY = Kit.ANKLE + if (deficit) 0.06 else 0.0
        fun at(pelvis: Vec3, pitch: Double, handY: Double?, single: Boolean = false): Pose {
            val sh = Kit.onTorso(pelvis, pitch, v(0.0, Body.SPINE - 0.05, 0.0))
            val (l, r, elbow) = if (good) load(spec, pelvis, pitch) else {
                val z = if (handY == null) sh.z else 0.06
                // A bar can be no higher than the hands hang; never above where they reach.
                val y = maxOf(handY ?: 0.0, Kit.hangY(sh + v(Body.SHOULDER_HALF, 0.0, 0.0), half, z))
                Triple(v(half, y, z), v(-half, y, z), v(0.3, -1.0, -0.5))
            }
            return Pose(
                pelvis = pelvis, pitch = pitch, head = -pitch * 0.45,
                handL = l, handR = r,
                footL = v(stance, footY, 0.0), footR = if (single) Kit.onTorso(pelvis, pitch, v(-0.12, -0.8, -0.35)).let { it.withY(it.y.coerceAtLeast(0.15)) } else v(-stance, footY, 0.0),
                elbowPole = elbow, kneePole = v(if (sumo) 0.8 else 0.15, 0.0, 1.0),
                kneePoleR = if (single) v(0.0, -1.0, -0.2) else null, footYaw = 0.0
            )
        }
        val single = spec.oneLeg
        val top = at(v(0.0, Body.STAND - 0.02, -0.03), 2 * DEG, if (good) null else 0.8)
        val bottom = when {
            good -> at(v(0.0, 0.88, -0.2), 80 * DEG, null)
            rdl -> at(v(0.0, 0.86, -0.22), 78 * DEG, null, single)
            else -> {
                // From the floor or the pins: the hips sit exactly as low as the arms need.
                val (pitch, z, barY) = when {
                    rack -> Triple(40 * DEG, -0.2, 0.52)
                    sumo -> Triple(42 * DEG, -0.22, Gear.PLATE_R)
                    trap -> Triple(38 * DEG, -0.12, Gear.PLATE_R)
                    else -> Triple(if (spec.has("snatch")) 58 * DEG else 55 * DEG, -0.26, Gear.PLATE_R)
                }
                val handZ = if (trap) 0.0 else 0.06
                at(v(0.0, Kit.pelvisToReach(v(half, barY, handZ), pitch, z), z), pitch, barY)
            }
        }
        val fromFloor = !rdl && !good
        val start = if (fromFloor) bottom else top
        val end = if (fromFloor) top else bottom
        val fixed = mutableListOf<Prim>()
        if (deficit) fixed += Prim.Box(v(0.0, 0.03, 0.0), Basis.IDENTITY, v(0.32, 0.03, 0.2), Palette.FRAME)
        if (rack) fixed += Gear.rack(0.06, 0.48, 0.62, 1.2)
        val moving = Rigging { s, _ ->
            when (spec.gear) {
                "TRAP_BAR" -> Gear.trapBar(s.handL, s.handR)
                "DUMBBELL" -> Gear.dumbbell(s.handL, Vec3.X) + Gear.dumbbell(s.handR, Vec3.X)
                "KETTLEBELL" -> Gear.kettlebell((s.handL + s.handR) * 0.5)
                "BODYWEIGHT" -> emptyList()
                "CABLE" -> Gear.cable(v(0.0, 0.1, 0.75), if (spec.oneArm) s.handR else (s.handL + s.handR) * 0.5)
                else -> Gear.barbell(s.handL, s.handR)
            }
        }
        if (spec.gear == "CABLE") fixed += Gear.cableTower(v(0.0, 0.0, 0.85), 0.1).first
        return Motion(
            mapOf("setup" to (if (fromFloor) bottom else top), "grip" to start, "top" to start, "bottom" to end),
            fixed, moving,
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.HIPS,
                setupNote = when {
                    good -> "Bar on the upper back, feet hip width, knees soft."
                    sumo -> "Feet wide, toes turned out, shins close to the bar."
                    else -> "Bar over the middle of the foot, feet hip width, shins a few centimetres from the bar."
                },
                gripNote = when {
                    good -> "Hands just outside the shoulders holding the bar on your back."
                    trap -> "Stand in the middle of the trap bar, neutral grip on the handles."
                    spec.gear == "DUMBBELL" -> "A dumbbell in each hand in front of the thighs."
                    else -> "Overhand or mixed grip, hands just outside the legs, arms straight."
                },
                startFocus = Focus.BACK,
                upSeconds = if (fromFloor) 2.0 else 1.2, downSeconds = if (fromFloor) 1.4 else 2.0,
                cameraYaw = CAM_SIDE
            ),
            Camera(yaw = 70 * DEG, pitch = 8 * DEG, distance = 3.7, target = v(0.0, 0.8, 0.0))
        )
    }

    private fun backExtension(spec: Spec): Motion {
        val padY = 0.95
        val ankle = v(0.0, 0.38, -0.56)
        val hipLine = (v(0.0, padY, 0.0) - ankle).normalized()
        val pelvis = v(0.0, padY + 0.05, 0.02)
        val inLine = kotlin.math.atan2(hipLine.z, hipLine.y)
        fun at(pitch: Double): Pose {
            val chest = Kit.onTorso(pelvis, pitch, v(0.0, Body.SPINE * 0.6, 0.18))
            return Pose(
                pelvis = pelvis, pitch = pitch, head = 0.0,
                handL = chest + v(0.1, 0.0, 0.0), handR = chest - v(0.1, 0.0, 0.0),
                footL = ankle + v(0.12, 0.0, 0.0), footR = ankle - v(0.12, 0.0, 0.0),
                elbowPole = v(1.0, 0.0, 0.0), kneePole = v(0.0, 0.0, 1.0), footPitch = -30 * DEG
            )
        }
        val top = at(inLine)
        val bottom = at(inLine + 75 * DEG)
        return Motion(
            mapOf("setup" to top, "grip" to top, "top" to top, "bottom" to bottom),
            listOf(
                Prim.Box(v(0.0, padY - 0.07, 0.04), Basis.of(0.0, inLine - 90 * DEG), v(0.27, 0.035, 0.14), Palette.PAD),
                Prim.Box(v(0.0, 0.05, -0.35), Basis.IDENTITY, v(0.3, 0.04, 0.45), Palette.FRAME),
                Prim.Capsule(ankle + v(-0.24, 0.08, 0.05), ankle + v(0.24, 0.08, 0.05), 0.045, 0.045, Palette.PAD)
            ) + listOf(0.27, -0.27).flatMap { x ->
                // Side rails, outside the legs so the frame reads from any angle.
                listOf(
                    Prim.Capsule(v(x, 0.09, -0.7), ankle.withX(x) + v(0.0, 0.08, 0.05), 0.022, 0.022, Palette.FRAME),
                    Prim.Capsule(ankle.withX(x) + v(0.0, 0.08, 0.05), v(x, padY - 0.09, 0.04), 0.022, 0.022, Palette.FRAME),
                    Prim.Capsule(v(x, 0.09, 0.1), v(x, padY - 0.09, 0.04), 0.022, 0.022, Palette.FRAME)
                )
            },
            Rigging { s, _ -> if (spec.gear == "PLATE") Gear.plate(s.chest + s.torso.fwd * 0.2, s.torso.fwd) else emptyList() },
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.BACK,
                setupNote = "Pad just below the hip bones so you can bend at the hips, heels against the foot pad.",
                gripNote = "Arms crossed on the chest, or a plate held there.",
                startFocus = Focus.HIPS, upSeconds = 1.4, downSeconds = 1.6, cameraYaw = CAM_SIDE
            ),
            Camera(yaw = 75 * DEG, pitch = 10 * DEG, distance = 3.6, target = v(0.0, 0.8, 0.0))
        )
    }

    /** Reverse hyper: chest on a high pad, the straight legs swing up behind to hip height. */
    private fun reverseHyper(): Motion {
        val pelvis = v(0.0, 1.05, 0.0)
        val pitch = 90 * DEG
        fun at(legUp: Double): Pose {
            val leg = Body.THIGH + Body.SHIN - 0.02
            val foot = pelvis + v(0.0, -cos(legUp), -sin(legUp)) * leg
            return Pose(
                pelvis = pelvis, pitch = pitch, head = 0.3,
                handL = v(0.24, 0.86, 0.6), handR = v(-0.24, 0.86, 0.6),
                footL = foot + v(0.1, 0.0, 0.0), footR = foot - v(0.1, 0.0, 0.0),
                elbowPole = v(0.5, -1.0, 0.0), kneePole = v(0.0, 0.0, 1.0), footPitch = 0.0
            )
        }
        val down = at(8 * DEG)
        val up = at(85 * DEG)
        return Motion(
            mapOf("setup" to down, "grip" to down, "bottom" to down, "top" to up),
            listOf(
                Prim.Box(v(0.0, 0.92, 0.32), Basis.IDENTITY, v(0.2, 0.03, 0.34), Palette.PAD),
                Prim.Box(v(0.0, 0.46, 0.4), Basis.IDENTITY, v(0.04, 0.44, 0.04), Palette.FRAME),
                Prim.Box(v(0.0, 0.03, 0.3), Basis.IDENTITY, v(0.3, 0.03, 0.45), Palette.FRAME),
                Prim.Cylinder(v(0.3, 0.86, 0.6), v(-0.3, 0.86, 0.6), 0.016, Palette.STEEL)
            ),
            Rigging { s, _ -> listOf(Prim.Cylinder(s.ankleL + v(0.08, 0.0, 0.0), s.ankleR - v(0.08, 0.0, 0.0), 0.045, Palette.PAD)) },
            Kit.steps(
                "setup", "grip", "bottom", "top", Focus.HIPS,
                setupNote = "Chest and belly on the pad with the hips just over the edge, ankles hooked under the roller.",
                gripNote = "Hold the handles at the front. Swing the legs up to hip height with the glutes, then lower under control.",
                startFocus = Focus.HIPS, upSeconds = 1.0, downSeconds = 1.6, cameraYaw = CAM_SIDE
            ),
            Camera(yaw = 75 * DEG, pitch = 10 * DEG, distance = 3.6, target = v(0.0, 0.8, 0.0))
        )
    }

    /** Seated good morning: bar on the back, sitting on a bench, hinging forward from the hips. */
    private fun seatedGoodMorning(spec: Spec): Motion {
        val base = MovesSeated.seated(-0.1).let { it.copy(footL = it.footL + v(0.1, 0.0, 0.0), footR = it.footR - v(0.1, 0.0, 0.0)) }
        fun at(pitch: Double): Pose {
            val (l, r, elbow) = load(spec, base.pelvis, pitch)
            return base.copy(pitch = pitch, head = -pitch * 0.45, handL = l, handR = r, elbowPole = elbow)
        }
        val up = at(0.0)
        val down = at(65 * DEG)
        return Motion(
            mapOf("setup" to up, "grip" to up, "top" to up, "bottom" to down),
            Gear.bench(0.0, -0.1, back = false),
            Rigging { s, _ -> Gear.barbell(s.handL, s.handR) },
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.HIPS,
                setupNote = "Sit on the end of a flat bench, feet wide and flat, bar on the upper back.",
                gripNote = "Hands just outside the shoulders. Keep the back flat and lean forward from the hips, not the waist.",
                startFocus = Focus.BACK, upSeconds = 1.2, downSeconds = 2.0, cameraYaw = CAM_SIDE
            ),
            Camera(yaw = 70 * DEG, pitch = 8 * DEG, distance = 3.4, target = v(0.0, 0.8, 0.0))
        )
    }

    /** Hip thrusts, glute bridges, kickbacks, pull-throughs, abduction and adduction. */
    fun hips(spec: Spec): Motion = when {
        spec.has("abduction", "adduction", "side walk") -> abduction(spec)
        spec.has("pull-through") -> pullThrough()
        spec.has("kickback", "donkey", "hydrant", "glute squeeze") -> kickback(spec)
        else -> bridge(spec)
    }

    private fun bridge(spec: Spec): Motion {
        val onBench = spec.has("hip thrust") && !spec.has("glute bridge")
        val shoulders = if (onBench) v(0.0, Gear.BENCH_TOP + 0.05, -0.4) else v(0.0, 0.12, -0.48)
        val feetZ = if (onBench) 0.42 else 0.25
        val single = spec.oneLeg
        fun at(pelvisY: Double): Pose {
            val pelvis = v(0.0, pelvisY, if (onBench) 0.02 else -0.05)
            val pitch = Kit.pitchTo(pelvis, shoulders)
            val barAt = pelvis + v(0.0, 0.14, 0.04)
            val loaded = spec.gear == "BARBELL" || spec.gear == "DUMBBELL" || spec.gear == "SMITH"
            // Unloaded, the arms lie long beside the body, palms down, the elbows kept off the floor.
            val restY = if (onBench) Gear.BENCH_TOP + 0.03 else 0.04
            val restZ = if (onBench) -0.25 else shoulders.z + 0.55
            return Pose(
                pelvis = pelvis, pitch = pitch, head = if (onBench) 0.6 else 0.2,
                handL = if (loaded) barAt + v(0.3, 0.0, 0.0) else v(0.28, restY, restZ),
                handR = if (loaded) barAt - v(0.3, 0.0, 0.0) else v(-0.28, restY, restZ),
                footL = v(0.16, Kit.ANKLE, feetZ), footR = if (single) v(-0.16, 0.6, feetZ + 0.25) else v(-0.16, Kit.ANKLE, feetZ),
                elbowPole = if (loaded || onBench) v(0.6, 0.0, -1.0) else v(0.6, 1.0, 0.0), kneePole = v(0.15, 0.0, 1.0)
            )
        }
        val bottom = at(if (onBench) 0.25 else 0.14)
        val top = at(if (onBench) 0.52 else 0.38)
        val fixed = if (onBench) listOf(
            Prim.Box(v(0.0, Gear.BENCH_TOP - 0.025, -0.55), Basis.IDENTITY, v(0.6, 0.025, 0.15), Palette.PAD),
            Prim.Box(v(0.0, 0.2, -0.55), Basis.IDENTITY, v(0.55, 0.2, 0.03), Palette.FRAME)
        ) else emptyList()
        return Motion(
            mapOf("setup" to bottom, "grip" to bottom, "top" to bottom, "bottom" to top),
            fixed,
            Rigging { s, _ ->
                when (spec.gear) {
                    "BARBELL", "SMITH" -> Gear.barbell(s.handL, s.handR)
                    "DUMBBELL" -> Gear.dumbbell((s.handL + s.handR) * 0.5, Vec3.X)
                    else -> emptyList()
                }
            },
            Kit.steps(
                "setup", "grip", "top", "bottom", Focus.HIPS,
                setupNote = if (onBench) "Upper back on the edge of the bench, feet flat, shins vertical at the top."
                else "Lie on your back, knees bent, feet flat and hip width, heels close to the glutes.",
                gripNote = if (spec.gear == "BARBELL") "Bar over the hip crease (use a pad), hands holding it in place." else "Arms on the floor or the bench for balance.",
                startFocus = Focus.CORE, upSeconds = 1.6, downSeconds = 1.0, cameraYaw = CAM_SIDE
            ),
            Camera(yaw = 75 * DEG, pitch = 12 * DEG, distance = 3.4, target = v(0.0, 0.5, 0.0))
        )
    }

    private fun kickback(spec: Spec): Motion {
        val standing = spec.gear == "CABLE" || spec.gear == "MACHINE" || spec.has("standing")
        if (standing) {
            val base = Kit.stand(0.12).copy(pelvis = v(0.0, Body.STAND - 0.03, -0.05), pitch = 25 * DEG,
                handL = v(0.22, 1.2, 0.45), handR = v(-0.22, 1.2, 0.45), elbowPole = v(0.3, -1.0, -0.3))
            val back = base.copy(footR = v(-0.12, 0.42, -0.62), kneePoleR = v(0.0, -1.0, 0.0))
            val (tower, pulley) = Gear.cableTower(v(0.0, 0.0, 0.75), 0.12)
            return Motion(
                mapOf("setup" to base, "grip" to base, "top" to base, "bottom" to back),
                if (spec.gear == "CABLE") tower else Gear.machineSeat(1.0, 90.0, 0.6).drop(4),
                Rigging { s, _ -> if (spec.gear == "CABLE") listOf(Prim.Line(pulley, s.ankleR, 0.008, Palette.CABLE)) else emptyList() },
                Kit.steps("setup", "grip", "top", "bottom", Focus.HIPS,
                    setupNote = "Ankle strap on, hold the frame, hinge slightly, standing knee soft.",
                    gripNote = "Both hands on the frame for balance.",
                    startFocus = Focus.CORE, upSeconds = 1.4, downSeconds = 1.0, cameraYaw = CAM_SIDE),
                Camera(yaw = 75 * DEG, pitch = 8 * DEG, distance = 3.6, target = v(0.0, 0.85, 0.0))
            )
        }
        // On all fours.
        val pelvis = v(0.0, 0.6, -0.28)
        val pitch = 92 * DEG
        val sh = Kit.onTorso(pelvis, pitch, v(0.0, Body.SPINE - 0.05, 0.0))
        val base = Pose(
            pelvis = pelvis, pitch = pitch, head = -0.6,
            handL = v(0.2, 0.05, sh.z + 0.02), handR = v(-0.2, 0.05, sh.z + 0.02),
            footL = v(0.13, 0.1, -0.78), footR = v(-0.13, 0.1, -0.78),
            elbowPole = v(0.0, 0.0, -1.0), kneePole = v(0.0, 0.0, 1.0), footPitch = -60 * DEG
        )
        val hydrant = spec.has("hydrant")
        val lifted = if (hydrant) base.copy(footR = v(-0.62, 0.55, -0.6), kneePoleR = v(1.0, 0.0, 0.5))
        else base.copy(footR = v(-0.13, 0.95, -0.55), kneePoleR = v(0.0, 0.0, -1.0))
        return Motion(
            mapOf("setup" to base, "grip" to base, "top" to base, "bottom" to lifted),
            emptyList(), Rigging { _, _ -> emptyList() },
            Kit.steps("setup", "grip", "top", "bottom", Focus.HIPS,
                setupNote = "Hands under the shoulders, knees under the hips, back flat.",
                gripNote = "Palms flat, fingers spread.", startFocus = Focus.CORE, upSeconds = 1.2, downSeconds = 1.0, cameraYaw = CAM_SIDE),
            Camera(yaw = 70 * DEG, pitch = 14 * DEG, distance = 3.4, target = v(0.0, 0.45, 0.0))
        )
    }

    private fun pullThrough(): Motion {
        val base = Kit.stand(0.3).copy(pelvis = v(0.0, Body.STAND - 0.03, 0.0), pitch = 0.0,
            handL = v(0.04, 0.78, 0.05), handR = v(-0.04, 0.78, 0.05), elbowPole = v(0.3, -1.0, 0.0))
        val down = base.copy(pelvis = v(0.0, 0.84, -0.22), pitch = 70 * DEG, head = -0.4,
            handL = v(0.04, 0.54, -0.18), handR = v(-0.04, 0.54, -0.18))
        val (tower, pulley) = Gear.cableTower(v(0.0, 0.0, -1.0), 0.12)
        return Motion(
            mapOf("setup" to base, "grip" to down, "top" to base, "bottom" to down),
            tower.map { if (it is Prim.Box) it.copy(c = it.c.copy(z = -it.c.z - 2.0)) else it }.let { tower },
            Rigging { s, _ -> listOf(Prim.Line(pulley, (s.handL + s.handR) * 0.5, 0.01, Palette.CABLE)) },
            Kit.steps("setup", "grip", "top", "bottom", Focus.HIPS,
                setupNote = "Face away from a low pulley, feet wide, rope between the legs.",
                gripNote = "Rope in both hands between the thighs, arms straight.", startFocus = Focus.BACK,
                upSeconds = 1.2, downSeconds = 1.6, cameraYaw = CAM_SIDE),
            Camera(yaw = 70 * DEG, pitch = 8 * DEG, distance = 3.8, target = v(0.0, 0.8, -0.2))
        )
    }

    private fun abduction(spec: Spec): Motion {
        val out = spec.has("abduction")
        val standing = spec.has("standing", "cable", "band") && !spec.has("machine")
        if (standing) {
            val base = Kit.stand(0.12).copy(handL = v(0.3, 1.1, 0.25), handR = v(-0.26, 0.85, 0.03))
            val moved = base.copy(footR = if (out) v(-0.5, 0.2, 0.0) else v(0.02, Kit.ANKLE + 0.02, 0.08), pelvis = base.pelvis + v(0.02, 0.0, 0.0))
            return Motion(
                mapOf("setup" to base, "grip" to base, "top" to base, "bottom" to moved),
                Gear.cableTower(v(0.9, 0.0, 0.0), 0.12).first, Rigging { _, _ -> emptyList() },
                Kit.steps("setup", "grip", "top", "bottom", Focus.HIPS, setupNote = "Stand side-on to the low pulley, strap on the ankle.",
                    gripNote = "Hold the frame for balance.", startFocus = Focus.CORE),
                Camera(yaw = 5 * DEG, pitch = 8 * DEG, distance = 3.8, target = v(0.0, 0.9, 0.0))
            )
        }
        val seat = MovesSeated.seated(-0.1, lean = -10.0).copy(handL = v(0.3, 0.55, 0.0), handR = v(-0.3, 0.55, 0.0))
        val narrow = seat.copy(footL = v(0.1, Kit.ANKLE + 0.1, 0.42), footR = v(-0.1, Kit.ANKLE + 0.1, 0.42), kneePole = v(0.1, 0.6, 1.0))
        val wide = seat.copy(footL = v(0.42, Kit.ANKLE + 0.1, 0.38), footR = v(-0.42, Kit.ANKLE + 0.1, 0.38), kneePole = v(0.9, 0.6, 1.0))
        val (start, end) = if (out) narrow to wide else wide to narrow
        return Motion(
            mapOf("setup" to start, "grip" to start, "top" to start, "bottom" to end),
            Gear.machineSeat(Gear.BENCH_TOP, 100.0, -0.1),
            Rigging { s, _ -> listOf(s.kneeL, s.kneeR).map { k -> Prim.Box(k + v(if (k.x > 0) (if (out) 0.07 else -0.07) else (if (out) -0.07 else 0.07), 0.0, 0.0), Basis.IDENTITY, v(0.02, 0.1, 0.12), Palette.PAD) } },
            Kit.steps("setup", "grip", "top", "bottom", Focus.HIPS,
                setupNote = "Sit back against the pad, pads on the ${if (out) "outside" else "inside"} of the knees.",
                gripNote = "Hold the side handles.", startFocus = Focus.CORE, cameraYaw = 0.0),
            Camera(yaw = 10 * DEG, pitch = 25 * DEG, distance = 3.2, target = v(0.0, 0.6, 0.1))
        )
    }

    /** Lying, seated and standing leg curls, Nordics and glute-ham raises. */
    fun legCurl(spec: Spec): Motion {
        if (spec.has("nordic", "glute ham", "glute-ham")) return nordic(spec)
        val seated = spec.has("seated")
        val standing = spec.has("standing")
        if (seated) {
            val seat = MovesSeated.seated(-0.15, seatY = 0.5, lean = -12.0).copy(handL = v(0.3, 0.58, 0.05), handR = v(-0.3, 0.58, 0.05))
            val straight = seat.copy(footL = v(0.14, 0.5, 0.75), footR = v(-0.14, 0.5, 0.75), kneePole = v(0.1, 1.0, 0.3))
            val curled = seat.copy(footL = v(0.14, 0.18, 0.3), footR = v(-0.14, 0.18, 0.3), kneePole = v(0.1, 1.0, 0.6))
            return Motion(
                mapOf("setup" to straight, "grip" to straight, "top" to straight, "bottom" to curled),
                Gear.machineSeat(0.5, 100.0, -0.15),
                Rigging { s, _ -> listOf(Prim.Cylinder(s.ankleL + v(0.08, -0.06, 0.0), s.ankleR - v(0.08, 0.06, 0.0), 0.05, Palette.PAD)) },
                Kit.steps("setup", "grip", "top", "bottom", Focus.KNEES,
                    setupNote = "Knees in line with the machine's pivot, lower pad just above the heels, thigh pad snug.",
                    gripNote = "Hold the handles to stay in the seat.", startFocus = Focus.HIPS, upSeconds = 1.6, downSeconds = 1.0, cameraYaw = CAM_SIDE),
                Camera(yaw = 75 * DEG, pitch = 10 * DEG, distance = 3.3, target = v(0.0, 0.6, 0.2))
            )
        }
        if (standing) {
            val base = Kit.stand(0.12).copy(pitch = 8 * DEG, handL = v(0.25, 1.15, 0.35), handR = v(-0.25, 1.15, 0.35), elbowPole = v(0.3, -1.0, -0.4))
            val curled = base.copy(footR = v(-0.12, 0.55, -0.32), kneePoleR = v(0.0, -1.0, 0.6))
            return Motion(
                mapOf("setup" to base, "grip" to base, "top" to base, "bottom" to curled),
                listOf(Prim.Box(v(0.0, 1.05, 0.45), Basis.IDENTITY, v(0.3, 0.04, 0.06), Palette.PAD), Prim.Box(v(0.0, 0.55, 0.5), Basis.IDENTITY, v(0.05, 0.55, 0.05), Palette.FRAME)),
                Rigging { s, _ -> listOf(Prim.Cylinder(s.ankleR + v(0.08, 0.0, -0.06), s.ankleR - v(0.08, 0.0, 0.06), 0.045, Palette.PAD)) },
                Kit.steps("setup", "grip", "top", "bottom", Focus.KNEES, setupNote = "Knee against the pad, pad just above the heel.",
                    gripNote = "Hold the handles in front.", startFocus = Focus.HIPS, upSeconds = 1.4, downSeconds = 1.0, cameraYaw = CAM_SIDE),
                Camera(yaw = 75 * DEG, pitch = 8 * DEG, distance = 3.4, target = v(0.0, 0.85, 0.0))
            )
        }
        // Lying face down.
        val padY = 0.62
        val pelvis = v(0.0, padY + 0.12, -0.05)
        val pitch = 96 * DEG
        val base = Pose(
            pelvis = pelvis, pitch = pitch, head = -0.4,
            handL = v(0.22, padY + 0.02, 0.62), handR = v(-0.22, padY + 0.02, 0.62),
            footL = v(0.12, padY + 0.1, -0.86), footR = v(-0.12, padY + 0.1, -0.86),
            elbowPole = v(0.6, 0.0, 1.0), kneePole = v(0.0, 0.0, 1.0), footPitch = -80 * DEG
        )
        val curled = base.copy(footL = v(0.12, 1.08, -0.52), footR = v(-0.12, 1.08, -0.52))
        return Motion(
            mapOf("setup" to base, "grip" to base, "top" to base, "bottom" to curled),
            listOf(
                Prim.Box(v(0.0, padY - 0.03, 0.05), Basis.IDENTITY, v(0.18, 0.03, 0.62), Palette.PAD),
                Prim.Box(v(0.0, padY / 2, 0.45), Basis.IDENTITY, v(0.04, padY / 2, 0.04), Palette.FRAME),
                Prim.Box(v(0.0, padY / 2, -0.45), Basis.IDENTITY, v(0.04, padY / 2, 0.04), Palette.FRAME),
                Prim.Box(v(0.0, 0.03, 0.0), Basis.IDENTITY, v(0.3, 0.03, 0.7), Palette.FRAME)
            ),
            Rigging { s, _ -> listOf(Prim.Cylinder(s.ankleL + v(0.08, 0.05, 0.0), s.ankleR + v(-0.08, 0.05, 0.0), 0.05, Palette.PAD)) },
            Kit.steps("setup", "grip", "top", "bottom", Focus.KNEES,
                setupNote = "Face down, knees just off the end of the pad, roller just above the heels.",
                gripNote = "Hold the handles under the bench.", startFocus = Focus.HIPS, upSeconds = 1.6, downSeconds = 1.0, cameraYaw = CAM_SIDE),
            Camera(yaw = 75 * DEG, pitch = 14 * DEG, distance = 3.5, target = v(0.0, 0.7, -0.1))
        )
    }

    private fun nordic(spec: Spec): Motion {
        val knee = v(0.0, 0.07, 0.0)
        fun at(angle: Double): Pose {
            val dir = v(0.0, cos(angle), sin(angle))
            val pelvis = knee + dir * Body.THIGH + v(0.0, 0.03, 0.0)
            val chest = Kit.onTorso(pelvis, angle, v(0.0, Body.SPINE * 0.62, 0.18))
            return Pose(
                pelvis = pelvis, pitch = angle, head = 0.0,
                handL = chest + v(0.12, -0.05, 0.05), handR = chest + v(-0.12, -0.05, 0.05),
                footL = v(0.12, 0.1, -0.42), footR = v(-0.12, 0.1, -0.42),
                elbowPole = v(0.5, -1.0, 0.0), kneePole = v(0.0, -1.0, 0.2), footPitch = -70 * DEG
            )
        }
        val top = at(0.0)
        val bottom = at(62 * DEG)
        return Motion(
            mapOf("setup" to top, "grip" to top, "top" to top, "bottom" to bottom),
            listOf(Prim.Box(v(0.0, 0.03, -0.1), Basis.IDENTITY, v(0.25, 0.03, 0.35), Palette.PAD),
                Prim.Capsule(v(-0.25, 0.2, -0.45), v(0.25, 0.2, -0.45), 0.04, 0.04, Palette.PAD)),
            Rigging { _, _ -> emptyList() },
            Kit.steps("setup", "grip", "top", "bottom", Focus.KNEES,
                setupNote = "Kneel on a pad with the heels hooked under something solid.",
                gripNote = "Hands ready in front of the chest to catch yourself.", startFocus = Focus.HIPS,
                upSeconds = 1.6, downSeconds = 3.0, cameraYaw = CAM_SIDE),
            Camera(yaw = 80 * DEG, pitch = 8 * DEG, distance = 3.4, target = v(0.0, 0.6, 0.2))
        )
    }

    /** The seated leg extension. */
    fun legExtension(spec: Spec): Motion {
        val seat = MovesSeated.seated(-0.1, seatY = 0.5, lean = -10.0).copy(handL = v(0.3, 0.55, 0.0), handR = v(-0.3, 0.55, 0.0))
        val bent = seat.copy(footL = v(0.13, 0.15, 0.48), footR = v(-0.13, 0.15, 0.48), kneePole = v(0.1, 1.0, 0.5))
        val straight = seat.copy(footL = v(0.13, 0.55, 0.8), footR = v(-0.13, 0.55, 0.8), kneePole = v(0.1, 1.0, 0.2))
        val single = spec.oneLeg
        return Motion(
            mapOf("setup" to bent, "grip" to bent, "top" to bent, "bottom" to if (single) straight.copy(footR = bent.footR) else straight),
            Gear.machineSeat(0.5, 100.0, -0.1),
            Rigging { s, _ -> listOf(Prim.Cylinder(s.ankleL + v(0.08, 0.0, 0.07), s.ankleR + v(-0.08, 0.0, 0.07), 0.05, Palette.PAD)) },
            Kit.steps("setup", "grip", "top", "bottom", Focus.KNEES,
                setupNote = "Knees in line with the pivot, pad on the front of the ankles, back against the seat.",
                gripNote = "Hold the side handles to stay down in the seat.", startFocus = Focus.HIPS,
                upSeconds = 1.6, downSeconds = 1.0, cameraYaw = CAM_SIDE),
            Camera(yaw = 75 * DEG, pitch = 10 * DEG, distance = 3.3, target = v(0.0, 0.6, 0.2))
        )
    }

    /** Standing, seated, single-leg and leg-press calf raises, and tibialis raises. */
    fun calfRaise(spec: Spec): Motion {
        val seated = spec.has("seated")
        val tib = spec.has("tibialis")
        if (spec.has("leg press", "calf press")) {
            val lp = legPress(spec)
            val straight = lp.pose("top")
            val pointed = straight.copy(footPitch = -15 * DEG, footL = straight.footL + v(0.0, 0.015, 0.02), footR = straight.footR + v(0.0, 0.015, 0.02))
            return lp.copy(poses = mapOf("setup" to straight, "grip" to straight, "top" to straight, "bottom" to pointed),
                steps = Kit.steps("setup", "grip", "top", "bottom", Focus.FEET, setupNote = "Only the balls of the feet on the bottom edge of the platform, legs straight.",
                    gripNote = "Hold the handles; the knees stay locked straight but not forced.", upSeconds = 1.0, downSeconds = 1.4, cameraYaw = CAM_SIDE))
        }
        if (seated) {
            val seat = MovesSeated.seated(-0.1).copy(footL = v(0.13, Kit.ANKLE + 0.1, 0.42), footR = v(-0.13, Kit.ANKLE + 0.1, 0.42),
                handL = v(0.2, 0.62, 0.35), handR = v(-0.2, 0.62, 0.35))
            val up = seat.copy(toes = 1.0, footL = seat.footL + v(0.0, 0.09, 0.0), footR = seat.footR + v(0.0, 0.09, 0.0),
                pelvis = seat.pelvis, handL = seat.handL + v(0.0, 0.09, 0.0), handR = seat.handR + v(0.0, 0.09, 0.0))
            return Motion(
                mapOf("setup" to seat, "grip" to seat, "top" to seat, "bottom" to up),
                Gear.bench(0.0, -0.3, back = false) + Gear.block(v(0.0, 0.05, 0.52), v(0.25, 0.05, 0.08)),
                Rigging { s, _ -> listOf(Prim.Box((s.kneeL + s.kneeR) * 0.5 + v(0.0, 0.08, 0.0), Basis.IDENTITY, v(0.25, 0.03, 0.08), Palette.PAD)) },
                Kit.steps("setup", "grip", "top", "bottom", Focus.FEET, setupNote = "Balls of the feet on the block, pad across the lower thighs.",
                    gripNote = "Hands on the pad.", startFocus = Focus.KNEES, upSeconds = 1.0, downSeconds = 1.4, cameraYaw = CAM_SIDE),
                Camera(yaw = 75 * DEG, pitch = 10 * DEG, distance = 3.0, target = v(0.0, 0.5, 0.2))
            )
        }
        val single = spec.oneLeg
        val base = Kit.stand(0.14).let {
            if (spec.gear == "DUMBBELL") it.copy(handL = v(0.27, 0.8, 0.02)) else it
        }.let { if (spec.gear == "MACHINE" || spec.gear == "SMITH" || spec.gear == "BARBELL") it.copy(
            handL = Kit.shoulder(it.pelvis, 0.0, 1.0) + v(0.22, 0.05, -0.05), handR = Kit.shoulder(it.pelvis, 0.0, -1.0) + v(-0.22, 0.05, -0.05),
            elbowPole = v(0.5, -1.0, -0.3)) else it }
        val start = if (single) base.copy(footR = v(-0.12, 0.3, -0.2), kneePoleR = v(0.0, -1.0, 0.5)) else base
        val lift = if (tib) 0.0 else 0.09
        val up = if (tib) start.copy(footPitch = -25 * DEG, toes = 0.0)
        else start.copy(
            toes = 1.0, pelvis = start.pelvis + v(0.0, lift, 0.0),
            footL = start.footL + v(0.0, lift, 0.0), footR = if (single) start.footR + v(0.0, lift, 0.0) else start.footR + v(0.0, lift, 0.0),
            handL = start.handL + v(0.0, lift, 0.0), handR = start.handR + v(0.0, lift, 0.0)
        )
        return Motion(
            mapOf("setup" to start, "grip" to start, "top" to start, "bottom" to up),
            if (spec.gear == "MACHINE") listOf(Prim.Box(v(0.0, 1.0, -0.35), Basis.IDENTITY, v(0.05, 1.0, 0.05), Palette.FRAME)) else emptyList(),
            Rigging { s, _ ->
                when (spec.gear) {
                    "DUMBBELL" -> Gear.dumbbell(s.handL, Vec3.Z)
                    "BARBELL", "SMITH" -> Gear.barbell(s.handL, s.handR)
                    "MACHINE" -> listOf(s.shoulderL, s.shoulderR).map { Prim.Capsule(it + v(0.0, 0.09, -0.1), it + v(0.0, 0.09, 0.12), 0.05, 0.05, Palette.PAD) }
                    else -> emptyList()
                }
            },
            Kit.steps("setup", "grip", "top", "bottom", Focus.FEET,
                setupNote = if (tib) "Back against a wall, heels on the floor a step in front of you." else "Balls of the feet on the edge of a step or the floor, legs straight.",
                gripNote = "Hold something for balance, or the weight at your side.", startFocus = Focus.KNEES,
                upSeconds = 1.0, downSeconds = 1.4, cameraYaw = CAM_SIDE),
            Camera(yaw = 70 * DEG, pitch = 6 * DEG, distance = 3.6, target = v(0.0, 0.8, 0.0))
        )
    }
}
