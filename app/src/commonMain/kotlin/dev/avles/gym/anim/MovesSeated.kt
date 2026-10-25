package dev.avles.gym.anim

/** Sitting: on a bench or a machine seat. The base most machines share. */
object MovesSeated {

    /** Sitting upright on a seat whose top is [seatY], hips at [z], feet flat in front. */
    fun seated(z: Double, seatY: Double = Gear.BENCH_TOP, lean: Double = 0.0): Pose {
        val pelvis = v(0.0, seatY + 0.1, z + 0.08)
        return Pose(
            pelvis = pelvis, pitch = lean * DEG,
            handL = v(0.24, seatY + 0.18, z + 0.32), handR = v(-0.24, seatY + 0.18, z + 0.32),
            footL = v(0.16, Kit.ANKLE, z + 0.52), footR = v(-0.16, Kit.ANKLE, z + 0.52),
            elbowPole = v(0.3, -1.0, -0.4), kneePole = v(0.12, 0.6, 1.0)
        )
    }
}
