package dev.avles.gym.anim

/**
 * The shared vocabulary every movement is written in: standing, the torso's frame,
 * the six standard steps. Keeping it here is what keeps forty movements consistent.
 */
object Kit {

    const val ANKLE = Body.ANKLE_H

    /** Where a point fixed to the torso ends up, for a pelvis and a lean. */
    fun onTorso(pelvis: Vec3, pitch: Double, local: Vec3, yaw: Double = 0.0): Vec3 =
        pelvis + Basis.of(yaw, pitch).apply(local)

    /** Shoulder height for a pelvis and a lean, handy for placing hands. */
    fun shoulder(pelvis: Vec3, pitch: Double, side: Double): Vec3 =
        onTorso(pelvis, pitch, v(side * Body.SHOULDER_HALF, Body.SPINE - 0.05, 0.0))

    /** The pelvis that puts the middle of the shoulders at [shoulders] for a lean of [pitch]. */
    fun pelvisFor(shoulders: Vec3, pitch: Double, yaw: Double = 0.0): Vec3 =
        shoulders - Basis.of(yaw, pitch).up * (Body.SPINE - 0.05)

    /** The lean that points the spine from [pelvis] at [shoulders]. */
    fun pitchTo(pelvis: Vec3, shoulders: Vec3): Double {
        val d = shoulders - pelvis
        return kotlin.math.atan2(d.z, d.y)
    }

    /** Straight from ankle to shoulder, as in a plank: the pelvis and lean for that line. */
    fun plank(ankle: Vec3, shoulders: Vec3): Pair<Vec3, Double> {
        val dir = (shoulders - ankle).normalized()
        val pitch = kotlin.math.atan2(dir.z, dir.y)
        return pelvisFor(shoulders, pitch) to pitch
    }

    /** How far a hand reaches from the shoulder with the elbow just short of locked. */
    const val ARM_REACH = 0.6

    /** Height of a hand hanging at full reach from [shoulder], at [x] across and [z] forward. */
    fun hangY(shoulder: Vec3, x: Double, z: Double): Double {
        val dx = x - shoulder.x; val dz = z - shoulder.z
        return shoulder.y - kotlin.math.sqrt((ARM_REACH * ARM_REACH - dx * dx - dz * dz).coerceAtLeast(0.0))
    }

    /**
     * The pelvis height that lets straight arms just reach [hand] for a lean of [pitch]
     * with the pelvis at depth [pelvisZ]: how low the hips must go to take a bar off the floor.
     */
    fun pelvisToReach(hand: Vec3, pitch: Double, pelvisZ: Double): Double {
        val up = Basis.of(0.0, pitch).up * (Body.SPINE - 0.05)
        val dx = kotlin.math.abs(hand.x) - Body.SHOULDER_HALF
        val dz = hand.z - (pelvisZ + up.z)
        val dy = kotlin.math.sqrt((ARM_REACH * ARM_REACH - dx * dx - dz * dz).coerceAtLeast(0.0))
        return hand.y + dy - up.y
    }

    /** Body length from ankle to shoulder with straight legs. */
    const val LINE = Body.SPINE - 0.05 + 0.03 + Body.THIGH + Body.SHIN

    /** Standing tall, arms by the sides. */
    fun stand(stance: Double = 0.14, footZ: Double = 0.0, pelvisY: Double = Body.STAND): Pose = Pose(
        pelvis = v(0.0, pelvisY, 0.0),
        handL = v(0.26, 0.82, 0.03), handR = v(-0.26, 0.82, 0.03),
        footL = v(stance, ANKLE, footZ), footR = v(-stance, ANKLE, footZ),
        footYaw = 0.0
    )

    fun Pose.handsAt(l: Vec3, r: Vec3) = copy(handL = l, handR = r)
    fun Pose.feetAt(l: Vec3, r: Vec3) = copy(footL = l, footR = r)

    /** Both hands on a bar or handles at height [y], depth [z], [half] apart from the middle. */
    fun pair(half: Double, y: Double, z: Double, x: Double = 0.0) = Pair(v(x + half, y, z), v(x - half, y, z))

    fun Pose.bothHands(p: Pair<Vec3, Vec3>) = copy(handL = p.first, handR = p.second)

    /**
     * The walk-through every lift gets, in the same order: set up, grip, start
     * position, the rep slowly, the rep with the beginner's points, then at speed.
     */
    fun steps(
        setup: String,
        grip: String,
        start: String,
        down: String,
        repFocus: Focus,
        setupNote: String? = null,
        gripNote: String? = null,
        startFocus: Focus = Focus.BACK,
        upSeconds: Double = 1.0,
        downSeconds: Double = 2.0,
        cameraYaw: Double? = null
    ): List<Step> = listOf(
        Step(StepKind.SETUP, "Set up", listOf(setup), Focus.FEET, note = setupNote, cameraYaw = cameraYaw),
        Step(StepKind.GRIP, "Grip", listOf(grip), Focus.HANDS, note = gripNote, cameraYaw = cameraYaw),
        Step(StepKind.START, "Start position", listOf(start), startFocus, cameraYaw = cameraYaw),
        Step(StepKind.REP, "The rep, slowly", listOf(start, down), repFocus, listOf(downSeconds * 1.5, upSeconds * 1.5), cameraYaw = cameraYaw),
        Step(StepKind.TIPS, "For beginners", listOf(start, down), Focus.NONE, listOf(downSeconds * 1.25, upSeconds * 1.25), cameraYaw = cameraYaw),
        Step(StepKind.FULL, "Full movement", listOf(start, down), Focus.NONE, listOf(downSeconds, upSeconds))
    )

    /**
     * The same walk-through for a lift with more than two positions in a rep (a clean,
     * a burpee, a get-up): [rep] plays in order and loops, each leg taking [seconds].
     */
    fun flow(
        setup: String,
        grip: String,
        rep: List<String>,
        repFocus: Focus,
        seconds: List<Double>,
        setupNote: String? = null,
        gripNote: String? = null,
        startFocus: Focus = Focus.BACK,
        cameraYaw: Double? = null
    ): List<Step> = listOf(
        Step(StepKind.SETUP, "Set up", listOf(setup), Focus.FEET, note = setupNote, cameraYaw = cameraYaw),
        Step(StepKind.GRIP, "Grip", listOf(grip), Focus.HANDS, note = gripNote, cameraYaw = cameraYaw),
        Step(StepKind.START, "Start position", listOf(rep[0]), startFocus, cameraYaw = cameraYaw),
        Step(StepKind.REP, "The rep, slowly", rep, repFocus, seconds.map { it * 1.8 }, cameraYaw = cameraYaw),
        Step(StepKind.TIPS, "For beginners", rep, Focus.NONE, seconds.map { it * 1.4 }, cameraYaw = cameraYaw),
        Step(StepKind.FULL, "Full movement", rep, Focus.NONE, seconds)
    )

    fun gripNote(spec: Spec, what: String = "the bar"): String {
        val width = when {
            spec.gripHalf < 0.25 -> "Hands close, about a fist or two apart on $what."
            spec.gripHalf > 0.45 -> "Hands wide, well outside shoulder width on $what."
            else -> "Hands just outside shoulder width on $what."
        }
        return "$width ${spec.grip.label}. Wrap the thumb around."
    }

    fun stanceNote(spec: Spec): String = when {
        spec.wideStance -> "Feet wide, toes turned out about 30 degrees."
        spec.oneLeg -> "Weight on one foot, the other only for balance."
        else -> "Feet about hip width, toes pointing slightly out, weight through the whole foot."
    }
}
