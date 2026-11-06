package dev.avles.gym

import dev.avles.gym.data.SeedData
import dev.avles.gym.domain.ExerciseGuide
import dev.avles.gym.domain.Muscle
import dev.avles.gym.domain.anatomy.BodyAtlas
import dev.avles.gym.domain.anatomy.BodyBuild
import dev.avles.gym.domain.anatomy.BodyView
import dev.avles.gym.anim.Avatar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * The muscle map is only as honest as its coverage: a muscle with no outline would
 * be named in the legend and silently missing from the picture.
 */
class MuscleMapTest {

    @Test
    fun `every muscle the data knows has a region on the map`() {
        Muscle.entries.forEach { m ->
            assertTrue("${m.label} has no region on either view", BodyAtlas.viewsOf(m).isNotEmpty())
        }
    }

    @Test
    fun `every muscle any seeded lift works is drawn`() {
        SeedData.exercises().forEach { e ->
            val g = ExerciseGuide.of(e)
            (g.primary + g.secondary).forEach { m ->
                assertTrue("${e.name}: ${m.label} is not on the map", BodyAtlas.viewsOf(m).isNotEmpty())
            }
        }
    }

    /** Symmetric by construction; this is what keeps it that way. */
    @Test
    fun `every view of every build is mirror symmetric`() {
        BodyBuild.PRESETS.forEach { build ->
            BodyView.entries.forEach { view ->
                val f = BodyAtlas.figure(view, build)
                (f.silhouette + f.muscles).groupBy { it.muscle }.forEach { (muscle, outlines) ->
                    // The start point is repeated as the last segment's end; skip it so each vertex counts once.
                    val xs = outlines.flatMap { o -> o.path.filterIndexed { i, _ -> i >= 2 && i % 2 == 0 } }
                    val left = xs.sumOf { (BodyAtlas.MIDLINE - it).toDouble().coerceAtLeast(0.0) }
                    val right = xs.sumOf { (it - BodyAtlas.MIDLINE).toDouble().coerceAtLeast(0.0) }
                    assertTrue("$view ${build.id} ${muscle?.label ?: "outline"} is lopsided", abs(left - right) < 0.5)
                }
            }
        }
    }

    @Test
    fun `every outline stays inside the drawing box`() {
        BodyBuild.PRESETS.forEach { build ->
            BodyView.entries.forEach { view ->
                val f = BodyAtlas.figure(view, build)
                (f.silhouette + f.muscles).forEach { o ->
                    o.path.forEachIndexed { i, v ->
                        val max = if (i % 2 == 0) BodyAtlas.WIDTH else BodyAtlas.HEIGHT
                        assertTrue("$view ${build.id}: $v outside 0..$max", v.isFinite() && v in 0f..max)
                    }
                }
            }
        }
    }

    @Test
    fun `the map follows the chosen demonstrator`() {
        assertEquals(BodyBuild.MALE, Avatar.MALE.anatomy)
        assertEquals(BodyBuild.FEMALE, Avatar.FEMALE.anatomy)
        // Female build: narrower waist, wider hips than the authored male figure.
        assertTrue(BodyBuild.FEMALE.widthAt(182f) < 1f)
        assertTrue(BodyBuild.FEMALE.widthAt(220f) > 1f)
    }

    @Test
    fun `a broken build cannot fold the figure`() {
        val bad = BodyBuild("bad", profile = listOf(0f to Float.NaN, 200f to 50f), arms = -3f)
        assertEquals(1f, bad.widthAt(0f))
        assertEquals(1.3f, bad.widthAt(300f))
        BodyView.entries.forEach { BodyAtlas.figure(it, bad) }
    }
}
