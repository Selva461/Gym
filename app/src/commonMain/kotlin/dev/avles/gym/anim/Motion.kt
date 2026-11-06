package dev.avles.gym.anim

/** What a step of the demonstration teaches. The words come from the lift's written guide. */
enum class StepKind { SETUP, GRIP, START, REP, TIPS, FULL }

/**
 * One step of the walk-through: which poses to show (one pose is held; two or more
 * loop), which part of the body to light up, and where the camera likes to be.
 */
data class Step(
    val kind: StepKind,
    val title: String,
    val poses: List<String>,
    val focus: Focus = Focus.NONE,
    /** Seconds to move between consecutive poses. */
    val seconds: List<Double> = List(poses.size) { 1.2 },
    val cameraYaw: Double? = null,
    val cameraPitch: Double? = null,
    /** Anything the motion itself knows that the written guide does not: grip width, stance. */
    val note: String? = null
)

/** The moving parts of the equipment, worked out from where the body is each frame. */
fun interface Rigging {
    fun prims(s: Skeleton, p: Pose): List<Prim>
}

/**
 * A complete demonstration: named poses, the equipment that stays put, the equipment
 * that moves with the body, the steps, and the rep as a loop of poses.
 */
data class Motion(
    val poses: Map<String, Pose>,
    val fixed: List<Prim>,
    val moving: Rigging,
    val steps: List<Step>,
    val camera: Camera = Camera()
) {
    fun pose(name: String): Pose = poses[name] ?: error("motion has no pose '$name'")

    /** Everything to draw for [pose] with [focus] lit, demonstrated by [avatar]. */
    fun scene(pose: Pose, focus: Focus, avatar: Avatar = Avatar.MALE): Pair<Skeleton, List<Prim>> {
        val s = Skeleton.solve(pose)
        return s to (fixed + moving.prims(s, pose) + Prim.Body(s, avatar, focus))
    }
}

/**
 * Plays one step: where in its loop of poses we are after [seconds].
 *
 * A held step (one pose) simply returns it. A loop goes through each pose in order and
 * back to the first, easing in and out of every one, the way a controlled rep pauses
 * at the top and the bottom.
 */
object Player {

    /** The pause at each end of a movement, seconds. */
    private const val HOLD = 0.25

    fun sample(motion: Motion, step: Step, seconds: Double, speed: Double = 1.0): Pose {
        val poses = step.poses.map(motion::pose)
        if (poses.size == 1) return poses[0]
        val legs = legs(step)
        val hold = HOLD
        val total = legs.sum() + hold * poses.size
        if (!(total > 0.0) || seconds.isNaN()) return poses[0]
        var t = (seconds * speed) % total
        for (i in poses.indices) {
            if (t < hold) return poses[i]
            t -= hold
            val leg = legs[i]
            if (t < leg) return poses[i].lerpTo(poses[(i + 1) % poses.size], smooth(t / leg))
            t -= leg
        }
        return poses[0]
    }

    /** How long one pass through [step] takes at normal speed; zero for a held pose. */
    fun cycleSeconds(step: Step): Double =
        if (step.poses.size < 2) 0.0 else legs(step).sum() + HOLD * step.poses.size

    /** Seconds for each move between poses, one per pose; missing ones default to 1.2. */
    private fun legs(step: Step): List<Double> =
        step.seconds.take(step.poses.size).let { if (it.size < step.poses.size) it + List(step.poses.size - it.size) { 1.2 } else it }

    /** Moving from the last pose of one step into the first of the next. */
    fun transition(from: Pose, to: Pose, t: Double): Pose = from.lerpTo(to, smooth(t))
}
