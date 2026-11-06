package dev.avles.gym

import dev.avles.gym.anim.Avatar
import dev.avles.gym.anim.BodyMesh
import dev.avles.gym.anim.Body
import dev.avles.gym.anim.Demos
import dev.avles.gym.anim.Focus
import dev.avles.gym.anim.Player
import dev.avles.gym.anim.Renderer
import dev.avles.gym.anim.Draw
import dev.avles.gym.anim.Shape
import dev.avles.gym.anim.Skeleton
import dev.avles.gym.anim.Step
import dev.avles.gym.anim.StepKind
import dev.avles.gym.anim.Vec3
import dev.avles.gym.data.Exercise
import dev.avles.gym.data.SeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.hypot

/**
 * The 3D demonstrator, checked for every lift without a screen: each one has a
 * motion, every pose in it is one a body can actually make, the hands are on the
 * equipment and the feet on the floor or the platform, and the two bodies build.
 *
 * The rule in handoff.md is that a new lift gets its movement in the same commit;
 * these are what hold it. A lift that resolves to a broken motion fails here by
 * name, before anyone sees it floating above the bench.
 */
class DemonstratorTest {

    private val exercises = SeedData.exercises()

    private fun nan(v: Vec3) = v.x.isNaN() || v.y.isNaN() || v.z.isNaN()

    @Test
    fun `every seeded lift has a demonstration with the six steps`() {
        exercises.forEach { e ->
            val m = assertNotNullNamed(e)
            assertEquals("${e.name} steps", StepKind.entries, m.steps.map { it.kind })
        }
    }

    @Test
    fun `every pose of every lift is reachable and stays above the floor`() {
        val problems = mutableListOf<String>()
        exercises.forEach { e ->
            val m = Demos.of(e) ?: return@forEach
            m.poses.forEach { (name, p) ->
                val s = Skeleton.solve(p)
                val joints = listOf(s.handL, s.handR, s.elbowL, s.elbowR, s.kneeL, s.kneeR, s.toeL, s.toeR, s.heelL, s.heelR)
                if (joints.any(::nan)) problems += "${e.name}/$name: NaN"
                val hand = maxOf((s.handL - p.handL).length, (s.handR - p.handR).length)
                if (hand > REACH_SLACK) problems += "${e.name}/$name: hand misses by $hand"
                fun foot(a: Vec3, t: Vec3) = hypot(a.x - t.x, a.z - t.z) + maxOf(0.0, t.y - a.y)
                val feet = maxOf(foot(s.ankleL, p.footL), foot(s.ankleR, p.footR))
                if (feet > REACH_SLACK) problems += "${e.name}/$name: foot misses by $feet"
                val low = joints.minOf { it.y }
                if (low < -0.03) problems += "${e.name}/$name: below the floor at $low"
            }
        }
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }

    @Test
    fun `limbs keep their length in every pose`() {
        exercises.forEach { e ->
            val m = Demos.of(e) ?: return@forEach
            m.poses.forEach { (name, p) ->
                val s = Skeleton.solve(p)
                listOf(
                    (s.elbowL - s.shoulderL).length to Body.UPPER_ARM, (s.elbowR - s.shoulderR).length to Body.UPPER_ARM,
                    (s.kneeL - s.hipL).length to Body.THIGH, (s.kneeR - s.hipR).length to Body.THIGH
                ).forEach { (got, want) -> assertEquals("${e.name}/$name limb", want, got, 1e-6) }
            }
        }
    }

    @Test
    fun `a custom lift with an unknown name still gets a demonstration`() {
        listOf(
            Exercise(name = "Grandma's secret squat thing", muscleGroup = "Quads"),
            Exercise(name = "", muscleGroup = "Chest"),
            Exercise(name = "x".repeat(500), muscleGroup = "Something new", isBodyweight = true)
        ).forEach { assertNotNull(it.name.take(20), Demos.of(it)) }
    }

    @Test
    fun `playing a step never produces an impossible pose`() {
        val m = Demos.of(exercises.first { it.name == "Barbell bench press" })!!
        m.steps.forEach { step ->
            listOf(0.0, 0.7, 3.1, 1e6, -2.0, Double.NaN).forEach { t ->
                val p = Player.sample(m, step, t)
                assertTrue("${step.kind} at $t", !nan(p.pelvis) && !nan(p.handL) && !nan(p.footR))
            }
        }
        // A step whose timings are all zero holds its first pose instead of dividing by zero.
        val frozen = Step(StepKind.REP, "x", m.steps.first { it.poses.size > 1 }.poses, seconds = listOf(0.0, 0.0))
        assertEquals(m.pose(frozen.poses[0]), Player.sample(m, frozen, 1.0))
    }

    @Test
    fun `a body shape from anywhere is pulled into a range that still makes a person`() {
        val wild = Shape(hipWidth = 9.0, waistWidth = -1.0, bust = Double.NaN, arms = Double.POSITIVE_INFINITY).validated()
        assertEquals(0.22, wild.hipWidth, 0.0)
        assertEquals(0.09, wild.waistWidth, 0.0)
        assertEquals(0.0, wild.bust, 0.0)
        assertEquals(1.4, wild.arms, 0.0)
        assertEquals(Shape(), Shape().validated())
    }

    @Test
    fun `the saved demonstrator choice always resolves`() {
        assertEquals(Avatar.MALE, Avatar.chosen("", female = false))
        assertEquals(Avatar.FEMALE, Avatar.chosen(null, female = true))
        assertEquals(Avatar.FEMALE, Avatar.chosen("female", female = false))
        assertEquals(Avatar.PRESETS.first(), Avatar.chosen("removed-in-a-later-version", female = true))
        assertEquals(Avatar.PRESETS.size, Avatar.PRESETS.map { it.id }.toSet().size)
    }

    @Test
    fun `both bodies build, skin cleanly and draw`() {
        Avatar.PRESETS.forEach { avatar ->
            val mesh = BodyMesh.of(avatar)
            assertTrue("${avatar.id} too coarse: ${mesh.size}", mesh.size > 5_000)
            assertEquals(0, mesh.tris.size % 3)
            assertTrue(mesh.tris.all { it in 0 until mesh.size })
            val m = Demos.of(exercises.first { it.name == "Deadlift" })!!
            val (_, prims) = m.scene(m.pose(m.steps.last().poses.last()), Focus.BACK, avatar)
            val draws = Renderer(400f, 400f, m.camera).render(prims)
            val skin = draws.filterIsInstance<Draw.Tri>()
            assertTrue("${avatar.id} drew ${skin.size} triangles", skin.size > 2_000)
            assertTrue(skin.none { it.x1.isNaN() || it.y2.isNaN() || it.x3.isNaN() })
            // Back to front: the painter's order the screen relies on.
            assertTrue(draws.zipWithNext().all { (a, b) -> a.depth >= b.depth })
        }
    }

    @Test
    fun `the camera can go all the way round without losing the figure`() {
        val m = Demos.of(exercises.first { it.name == "Back squat" })!!
        val (_, prims) = m.scene(m.pose(m.steps.first().poses.first()), Focus.NONE, Avatar.MALE)
        BodyMesh.of(Avatar.MALE)
        for (deg in 0 until 360 step 30) {
            val draws = Renderer(300f, 300f, m.camera.copy(yaw = deg * Math.PI / 180)).render(prims)
            val onScreen = draws.filterIsInstance<Draw.Tri>().count { abs(it.x1 - 150f) < 150f && abs(it.y1 - 150f) < 150f }
            assertTrue("yaw $deg shows $onScreen triangles", onScreen > 1_000)
        }
    }

    private fun assertNotNullNamed(e: Exercise) = Demos.of(e).also { assertNotNull("${e.name} has no demonstration", it) }!!

    private companion object {
        /** How far a hand or foot may land from where the movement put it, metres. */
        const val REACH_SLACK = 0.04
    }
}
