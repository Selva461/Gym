package dev.avles.gym

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The security decisions, pinned so they cannot be undone by accident.
 *
 * Every one of these was a deliberate choice with a reason, and every one is the
 * kind of thing that gets quietly reversed — a dependency pulls in a permission, a
 * keep rule gets widened to silence a crash, someone flips a flag while debugging
 * and forgets. None of those would fail a build or show up in review. They fail
 * here instead.
 *
 * Read alongside SECURITY.md, which explains the threat model these defend.
 */
class SecurityPostureTest {

    private val manifest by lazy { projectFile("app/src/main/AndroidManifest.xml").readText() }
    private val proguard by lazy { projectFile("app/proguard-rules.pro").readText() }
    private val buildFile by lazy { projectFile("app/build.gradle.kts").readText() }

    /**
     * The strongest property this app has. Not declaring INTERNET is not enough on
     * its own — the manifest merger folds in the permissions of every dependency, so
     * a library that wants network access gets it silently. tools:node="remove"
     * makes the absence a build-time guarantee instead.
     */
    @Test
    fun `network access is actively removed, not merely undeclared`() {
        listOf("android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE").forEach {
            val declared = Regex("""<uses-permission[^>]*$it[^>]*>""").find(manifest)?.value
            assertTrue("$it is not mentioned in the manifest at all — the removal is gone", declared != null)
            assertTrue(
                "$it is declared without tools:node=\"remove\":\n  $declared",
                declared!!.contains("tools:node=\"remove\"")
            )
        }
    }

    /**
     * An app that holds a health log should hold as little permission as possible.
     * This is an allowlist rather than a count: a new permission fails until someone
     * writes down why it is here.
     */
    @Test
    fun `the app asks for nothing beyond what it needs to run`() {
        val allowed = setOf(
            // Posting the rest-timer notification and the nightly reminder.
            "android.permission.POST_NOTIFICATIONS",
            // Re-arming the nightly reminder after a reboot; without it the reminder
            // silently stops working until the app is next opened.
            "android.permission.RECEIVE_BOOT_COMPLETED",
            // The rest timer buzzing when the phone is face down on a bench.
            "android.permission.VIBRATE",
            // Declared only so that the merger removes them.
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE"
        )
        val asked = Regex("""uses-permission android:name="([^"]+)"""")
            .findAll(manifest).map { it.groupValues[1] }.toSet()
        val unexpected = asked - allowed
        assertTrue(
            "New permission(s) with no stated reason: $unexpected. Add it to this list " +
                "with a comment saying what needs it, or take it out.",
            unexpected.isEmpty()
        )
    }

    /**
     * An exported component is an entry point any other app on the phone can call.
     * The launcher activity has to be exported; the reminder receiver must not be,
     * or any installed app could fire a fake reminder. BootReceiver is exported
     * because only the system can send BOOT_COMPLETED, and the permission attribute
     * is what narrows it to the system.
     */
    @Test
    fun `nothing is exported without a reason`() {
        val components = Regex("""<(activity|receiver|service|provider)\b[^>]*>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(manifest).map { it.value }.toList()
        assertTrue("no components found — the manifest parse is wrong", components.isNotEmpty())

        components.filter { it.contains("android:exported=\"true\"") }.forEach { component ->
            val isLauncher = component.contains("MainActivity")
            val isPermissionGuarded = component.contains("android:permission=")
            assertTrue(
                "An exported component is neither the launcher nor permission-guarded:\n$component",
                isLauncher || isPermissionGuarded
            )
        }
    }

    /**
     * A debuggable APK can have its database read straight out of it with
     * `adb run-as` on an unlocked phone with USB debugging on. For a health log that
     * is the exposure worth engineering around.
     */
    @Test
    fun `the release build is minified, shrunk and not debuggable`() {
        val release = buildFile.substringAfter("release {").substringBefore("\n        }")
        assertTrue("release is not minified", release.contains("isMinifyEnabled = true"))
        assertTrue("release does not shrink resources", release.contains("isShrinkResources = true"))
        assertTrue("release is debuggable", release.contains("isDebuggable = false"))
        assertTrue("release does not apply proguard-rules.pro", release.contains("proguard-rules.pro"))
    }

    /**
     * The failure this test exists for: `-keepclassmembers class dev.avles.gym.data.** { *; }`
     * shipped in an earlier build. It kept every column of every table under its
     * original name, handing a decompiler the entire data model in a build whose
     * point was to be hard to read — and Room never needed it, because KSP resolves
     * columns at compile time.
     *
     * A keep rule is a hole in the obfuscation. A wildcard one over app code is a
     * hole big enough to see the whole app through.
     */
    @Test
    fun `no keep rule exempts the app's own code from obfuscation`() {
        val appKeeps = proguard.lines()
            .map { it.substringBefore('#').trim() }
            .filter { it.startsWith("-keep") }
            .filter { it.contains("dev.avles.gym") }
            .filter { it.contains("**") || it.contains("{ *; }") || it.contains("{*;}") }
        assertTrue(
            "These keep rules exempt app code from obfuscation:\n" +
                appKeeps.joinToString("\n") { "  $it" } +
                "\nIf one is genuinely needed, narrow it to the single class that needs it.",
            appKeeps.isEmpty()
        )
    }

    /** Logging must not survive R8 even if a hand-written call slips past review. */
    @Test
    fun `logging is stripped from release output`() {
        assertTrue(
            "proguard-rules.pro no longer strips android.util.Log",
            proguard.contains("-assumenosideeffects class android.util.Log")
        )
    }

    /**
     * A release keystore in the repository is the single worst thing that can happen
     * to a signed app: anyone with the file can ship an update that Android will
     * install over yours. The committed debug key is fine and is deliberate — it is
     * Android's own published default and protects nothing — so this test names it
     * explicitly and rejects everything else.
     */
    @Test
    fun `no signing material beyond the public debug key is in the repository`() {
        val keystores = projectRoot().walkTopDown()
            .filter { it.isFile }
            .filter { it.extension in setOf("jks", "keystore", "p12", "pfx") }
            .filterNot { it.path.contains("/build/") }
            .map { it.relativeTo(projectRoot()).path }
            .toList()
        assertTrue(
            "Signing material found that is not the published debug key: $keystores",
            keystores.all { it == "app/keystore/debug.keystore" }
        )
        assertFalse(
            "A release password appears to be hardcoded in the build file",
            Regex("""storePassword\s*=\s*"(?!android")""").containsMatchIn(buildFile)
        )
    }

    // ---- locating the repository -------------------------------------------

    private fun projectRoot(): File {
        var dir: File? = File(System.getProperty("user.dir")!!).absoluteFile
        while (dir != null) {
            if (File(dir, "app/src/main/AndroidManifest.xml").isFile) return dir
            dir = dir.parentFile
        }
        throw IllegalStateException("Could not find the project root from ${System.getProperty("user.dir")}")
    }

    private fun projectFile(path: String) = File(projectRoot(), path).also {
        check(it.isFile) { "Expected $path to exist at ${it.absolutePath}" }
    }
}
