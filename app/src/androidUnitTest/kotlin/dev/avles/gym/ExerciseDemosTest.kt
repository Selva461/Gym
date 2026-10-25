package dev.avles.gym

import dev.avles.gym.data.ExerciseDemos
import dev.avles.gym.data.SeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The demonstration map against the catalogue and the bundled files.
 *
 * A key that no longer names a lift (a rename in a seed file) or a stem with a
 * missing frame fails silently in the app — the sheet just shows no picture — so
 * both are checked here, against the real asset folder.
 */
class ExerciseDemosTest {

    private val demoDir: File = listOf(
        File("src/commonMain/composeResources/files/demo"),
        File("app/src/commonMain/composeResources/files/demo")
    ).first { it.isDirectory }

    @Test
    fun `every demo names a lift in the catalogue`() {
        val names = SeedData.exercises().map { it.name }.toSet()
        val unknown = ExerciseDemos.BY_NAME.keys - names
        assertTrue("demos for lifts that do not exist: $unknown", unknown.isEmpty())
    }

    @Test
    fun `every stem has both frames bundled`() {
        ExerciseDemos.BY_NAME.values.toSet().forEach { stem ->
            (0..1).forEach { i ->
                assertTrue("missing demo/$stem-$i.webp", File(demoDir, "$stem-$i.webp").isFile)
            }
        }
    }

    @Test
    fun `no frame is bundled that nothing uses`() {
        val used = ExerciseDemos.BY_NAME.values.flatMap { listOf("$it-0.webp", "$it-1.webp") }.toSet()
        val orphans = demoDir.list()!!.filter { it.endsWith(".webp") }.toSet() - used
        assertTrue("unused demo frames: $orphans", orphans.isEmpty())
    }

    @Test
    fun `coverage is what the README claims`() {
        assertEquals(273, ExerciseDemos.BY_NAME.size)
    }
}
