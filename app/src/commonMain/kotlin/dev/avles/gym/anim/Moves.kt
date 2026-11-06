package dev.avles.gym.anim

/**
 * The one entry point: any lift, by its guide's pattern and equipment and its name,
 * to a demonstration.
 *
 * Every lift resolves to something. Patterns pick the family of movement, names pick
 * the variation within it, and a lift that matches nothing more specific still gets
 * its pattern's standard movement. That is what lets a custom exercise added later
 * show a sensible demo the moment it exists; a new seeded lift that needs a movement
 * of its own gets a keyword here (see the rule in handoff.md).
 */
object Moves {

    fun of(spec: Spec): Motion = when (spec.pattern) {
        "HORIZONTAL_PRESS" -> horizontalPress(spec)
        "VERTICAL_PRESS" -> verticalPress(spec)
        "HORIZONTAL_PULL" -> MovesPull.row(spec)
        "VERTICAL_PULL" -> verticalPull(spec)
        "SQUAT" -> MovesLegs.squat(spec)
        "LUNGE" -> MovesLegs.lunge(spec)
        "HINGE" -> MovesLegs.hinge(spec)
        "HIP_EXTENSION" -> MovesLegs.hips(spec)
        "LEG_CURL" -> MovesLegs.legCurl(spec)
        "LEG_EXTENSION" -> MovesLegs.legExtension(spec)
        "CALF_RAISE" -> MovesLegs.calfRaise(spec)
        "CURL" -> MovesCurl.curl(spec)
        "TRICEPS_EXTENSION" -> MovesCurl.triceps(spec)
        "WRIST" -> MovesCurl.wrist(spec)
        "LATERAL_RAISE" -> MovesArms.lateral(spec)
        "FRONT_RAISE" -> MovesArms.frontRaise(spec)
        "FLY" -> MovesArms.fly(spec)
        "SHRUG" -> MovesArms.shrug(spec)
        "GRIP" -> MovesArms.grip(spec)
        "CARRY" -> MovesArms.carry(spec)
        "CORE_FLEXION" -> MovesCore.flexion(spec)
        "CORE_ANTI_EXTENSION" -> MovesCore.antiExtension(spec)
        "CORE_ROTATION" -> MovesCore.rotation(spec)
        "EXPLOSIVE" -> MovesPower.of(spec)
        else -> fallback(spec)
    }

    private fun horizontalPress(spec: Spec): Motion = when {
        spec.has("dip") -> MovesPress.dip(spec)
        spec.has("push-up", "pushup", "press-up") -> MovesMore.inverted(spec)
        spec.has("squeeze press") && spec.gear == "DUMBBELL" -> MovesPress.bench(spec.copy(name = "close-grip dumbbell bench press"))
        spec.has("svend", "squeeze press") -> MovesMore.squeezePress(spec)
        spec.gear == "MACHINE" -> MovesMore.machineChestPress(spec)
        spec.gear == "CABLE" || spec.gear == "BAND" -> MovesMore.standingPress(spec)
        else -> MovesPress.bench(spec)
    }

    private fun verticalPress(spec: Spec): Motion = when {
        spec.has("external rotation", "cuban") -> MovesMore.externalRotation(spec)
        spec.has("handstand", "wall walk", "pike") -> MovesMore.inverted(spec)
        spec.has("landmine", "viking") -> MovesMore.landminePress(spec)
        else -> MovesPress.overhead(spec)
    }

    private fun verticalPull(spec: Spec): Motion = when {
        spec.has("pullover") && !spec.has("rope") && spec.gear != "CABLE" -> MovesPull.pullover(spec)
        spec.has("pull-up", "pullup", "chin-up", "chinup", "muscle-up") || spec.gear == "BODYWEIGHT" -> MovesPull.pullUp(spec)
        else -> MovesPull.pulldown(spec)
    }

    /** A lift whose pattern is unknown: the nearest thing by equipment, so the screen is never empty. */
    private fun fallback(spec: Spec): Motion = when (spec.gear) {
        "BARBELL", "SMITH", "TRAP_BAR" -> MovesLegs.hinge(spec)
        "CABLE" -> MovesPull.row(spec)
        "MACHINE" -> MovesMore.machineChestPress(spec)
        "KETTLEBELL" -> MovesPower.of(spec.copy(name = "kettlebell swing"))
        "BODYWEIGHT" -> MovesLegs.squat(spec.copy(name = "air squat"))
        else -> MovesArms.carry(spec.copy(name = "farmer's hold"))
    }
}
