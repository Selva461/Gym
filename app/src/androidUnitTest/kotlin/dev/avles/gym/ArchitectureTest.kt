package dev.avles.gym

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The layering, enforced rather than described.
 *
 * A convention that lives only in a document is a convention that decays — the rule
 * gets broken by someone in a hurry, nothing complains, and six months later the
 * layers are a suggestion. These tests are the thing that complains.
 *
 * They read the source as text rather than reflecting over compiled classes, which
 * means they run anywhere Kotlin does, need no Android SDK, and catch a bad import
 * at the point it is written instead of at the point it causes a problem.
 *
 * The rules are in [RULES] with the reason each one exists. If a rule is genuinely
 * wrong, change it there and say why in the commit — do not add an exception here.
 */
class ArchitectureTest {

    /**
     * One forbidden relationship: files under [inPackage] must not mention
     * [mustNotReference]. [because] is printed on failure, because a layering
     * violation is only obvious to the person who wrote the rule.
     */
    private data class Rule(
        val inPackage: String,
        val mustNotReference: String,
        val because: String,
        val except: Set<String> = emptySet(),
        /** Which Kotlin Multiplatform source sets the rule covers. */
        val sourceSets: List<String> = ALL_SOURCE_SETS
    )

    private val RULES = listOf(
        Rule(
            inPackage = "",
            mustNotReference = "import android.",
            because = "commonMain is compiled for iOS as well, where Android does not exist. " +
                "Anything Android-specific belongs in androidMain, behind one of the " +
                "seams in commonMain (PhotoStore, Notifier, the expect declarations).",
            sourceSets = listOf("commonMain")
        ),
        Rule(
            inPackage = "",
            mustNotReference = "import java.",
            because = "commonMain is compiled for iOS as well, where the JDK does not " +
                "exist. Dates go through core/Dates, number formatting through " +
                "domain/Numbers, and files through a platform seam.",
            sourceSets = listOf("commonMain")
        ),
        Rule(
            inPackage = "domain",
            mustNotReference = "import android.",
            because = "The domain layer is pure Kotlin and must stay that way. It is what " +
                "the whole off-device test suite runs against — every progression, macro " +
                "and seed test compiles without an Android SDK because of this rule. One " +
                "android import ends that."
        ),
        Rule(
            inPackage = "domain",
            mustNotReference = "import dev.avles.gym.ui",
            because = "Dependencies point inward. The domain cannot know what a screen is."
        ),
        Rule(
            inPackage = "data",
            mustNotReference = "import dev.avles.gym.ui",
            because = "Dependencies point inward. Storage cannot know what a screen is."
        ),
        Rule(
            inPackage = "ui",
            mustNotReference = "import androidx.room",
            because = "Screens read state from the view model, never the database. Without " +
                "this rule a screen eventually runs a query on the main thread and the " +
                "app janks for a reason nobody can find."
        ),
        Rule(
            inPackage = "",
            mustNotReference = "import android.util.Log",
            because = "All logging goes through GymLog, which compiles itself out of " +
                "release builds. A direct Log call would write body weights and meals to " +
                "logcat in a shipped build. See SECURITY.md.",
            except = setOf("core/Platform.android.kt")
        ),
        Rule(
            inPackage = "",
            mustNotReference = "println(",
            because = "Same reason as above, and a println in an Android app is almost " +
                "always a debugging leftover."
        )
    )

    /**
     * A file this long is a file nobody wants to open, and the request that drove
     * this work was to keep every part of the app easy to change. The ceiling is not
     * aspirational: the two biggest files sit just under it, so the next feature has
     * to go in a new file rather than being appended to an existing one. That is the
     * intended effect.
     */
    private val MAX_LINES = 800

    @Test
    fun `no layer reaches somewhere it should not`() {
        val failures = mutableListOf<String>()
        RULES.forEach { rule ->
            sourceFiles(rule.inPackage, rule.sourceSets)
                .filterNot { file -> rule.except.any { relativePath(file).endsWith(it) } }
                .forEach { file ->
                    file.readLines().forEachIndexed { index, line ->
                        // A rule naming its own reason in a comment is not a violation.
                        if (line.contains(rule.mustNotReference) && !line.trimStart().startsWith("*")) {
                            failures += "${relativePath(file)}:${index + 1} references " +
                                "\"${rule.mustNotReference}\"\n      ${rule.because}"
                        }
                    }
                }
        }
        assertTrue(
            "\n\nThe layering is broken in ${failures.size} place(s):\n\n" +
                failures.joinToString("\n\n") { "  $it" } + "\n",
            failures.isEmpty()
        )
    }

    @Test
    fun `no source file has grown past the point of being readable`() {
        val tooLong = sourceFiles("")
            .map { relativePath(it) to it.readLines().size }
            .filter { it.second > MAX_LINES }
            .sortedByDescending { it.second }
        assertTrue(
            "\n\nThese files are over $MAX_LINES lines and should be split before being " +
                "added to:\n" + tooLong.joinToString("\n") { "  ${it.first} — ${it.second} lines" } + "\n",
            tooLong.isEmpty()
        )
    }

    /**
     * Room builds its SQL at compile time from @Query annotations. A query assembled
     * from a Kotlin string template is a query whose text depends on runtime values,
     * which is the shape SQL injection takes — and Room cannot check it either.
     * Parameters belong in `:bindings`, which Room binds properly.
     */
    @Test
    fun `no query is assembled by string interpolation`() {
        val offenders = sourceFiles("data").flatMap { file ->
            val text = file.readLines()
            text.mapIndexedNotNull { index, line ->
                val inQuery = text.take(index + 1).asReversed()
                    .takeWhile { !it.trimStart().startsWith("suspend fun") && !it.trimStart().startsWith("fun") }
                    .any { it.contains("@Query") }
                if (inQuery && line.contains("\${")) "${relativePath(file)}:${index + 1}" else null
            }
        }
        assertTrue(
            "\n\nSQL built by interpolation — use a :binding instead:\n" +
                offenders.joinToString("\n") { "  $it" } + "\n",
            offenders.isEmpty()
        )
    }

    // ---- locating the source tree ------------------------------------------
    //
    // Gradle runs tests with the module directory as the working directory and the
    // off-device harness runs them from the repository root, so neither path can be
    // hardcoded. Walking up until the source sets appear handles both, and fails
    // loudly rather than silently passing on an empty file list.

    private fun appSrc(): File {
        var dir: File? = File(System.getProperty("user.dir")!!).absoluteFile
        while (dir != null) {
            listOf("app/src", "src")
                .map { File(dir, it) }
                .firstOrNull { File(it, "commonMain/kotlin/dev/avles/gym").isDirectory }
                ?.let { return it }
            dir = dir.parentFile
        }
        throw IllegalStateException(
            "Could not find the source tree from ${System.getProperty("user.dir")}"
        )
    }

    private fun sourceFiles(inPackage: String, sourceSets: List<String> = ALL_SOURCE_SETS): List<File> {
        val files = sourceSets.flatMap { set ->
            val base = File(appSrc(), "$set/kotlin/dev/avles/gym")
            val root = if (inPackage.isBlank()) base else File(base, inPackage)
            if (!root.isDirectory) emptyList()
            else root.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        }
        check(files.isNotEmpty() || inPackage.isNotBlank()) {
            "No Kotlin files found for $sourceSets — the rule would pass vacuously"
        }
        return files
    }

    /** "commonMain:data/GymRepository.kt" — which source set, then where in the package. */
    private fun relativePath(file: File): String {
        val path = file.absolutePath
        val set = ALL_SOURCE_SETS.firstOrNull { path.contains("/$it/") } ?: "?"
        return "$set:" + path.substringAfter("dev/avles/gym/", file.name)
    }

    private companion object {
        val ALL_SOURCE_SETS = listOf("commonMain", "androidMain", "iosMain")
    }
}
