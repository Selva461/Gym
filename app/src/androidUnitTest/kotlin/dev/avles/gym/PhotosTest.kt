package dev.avles.gym

import dev.avles.gym.domain.Photos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Photos are the first thing in this app that arrives from outside it.
 *
 * Every other value in the database was typed in. A picked image comes through
 * another app's content provider carrying a filename and a MIME type that neither
 * this app nor the person using it chose, and a filename is one string join away
 * from being a path. That is what most of this file is about.
 */
class PhotosTest {

    /**
     * The one that matters. If a name derived from outside the app ever reached the
     * filesystem unfiltered, "../../databases/gym.db" is a request to overwrite the
     * training log with a JPEG.
     */
    @Test
    fun `a generated name cannot escape the photo directory`() {
        val hostile = listOf(
            "../../databases/gym.db",
            "../../../etc/passwd",
            "..%2f..%2fgym.db",
            "a/b/c",
            "a\\b\\c",
            ".hidden",
            "name with spaces; rm -rf /",
            "'; DROP TABLE progress_photo; --"
        )
        hostile.forEach { input ->
            val name = Photos.newFileName(input, "image/jpeg")
            assertTrue(
                "\"$input\" produced \"$name\", which is not safe",
                Photos.isSafeFileName(name)
            )
            assertFalse("\"$name\" contains a separator", '/' in name || '\\' in name)
            assertFalse("\"$name\" can climb out of the directory", ".." in name)
        }
    }

    @Test
    fun `a name is still produced when the token has nothing usable in it`() {
        val name = Photos.newFileName("../../", "image/png")
        assertEquals("photo.png", name)
        assertTrue(Photos.isSafeFileName(name))
    }

    @Test
    fun `a uuid survives intact, because that is the normal case`() {
        val uuid = "3f2504e0-4f89-11d3-9a0c-0305e82c3301"
        assertEquals("$uuid.jpg", Photos.newFileName(uuid, "image/jpeg"))
    }

    /**
     * The extension comes from a fixed list, not from whatever the provider claimed.
     * An unknown type is stored as jpg rather than rejected — the picker only offers
     * images, and refusing a photo because its MIME string was odd would be the app
     * losing something the user has no way to get back.
     */
    @Test
    fun `the extension comes from an allowlist`() {
        assertEquals("jpg", Photos.extensionFor("image/jpeg"))
        assertEquals("png", Photos.extensionFor("image/png"))
        assertEquals("webp", Photos.extensionFor("image/webp"))
        assertEquals("heic", Photos.extensionFor("image/heic"))
        // Parameters and case are normal in a real MIME header.
        assertEquals("jpg", Photos.extensionFor("IMAGE/JPEG; charset=binary"))
        // Anything else, including something trying to pick its own extension.
        assertEquals("jpg", Photos.extensionFor(null))
        assertEquals("jpg", Photos.extensionFor("application/x-sh"))
        assertEquals("jpg", Photos.extensionFor("image/../../evil"))
    }

    /**
     * Names read back out of the database are checked too. Every one was written by
     * newFileName, so this is always true in a correct app — which is the point. The
     * case that matters is a database restored, edited or corrupted, where "it cannot
     * happen" stops being a property of the string.
     */
    @Test
    fun `a name read back from the database is validated, not trusted`() {
        listOf(
            "../gym.db", "photos/../../x.jpg", "/etc/passwd", ".ssh", "",
            "  ", "a".repeat(65) + ".jpg", "file name.jpg", "x;y.jpg"
        ).forEach {
            assertFalse("\"$it\" was accepted", Photos.isSafeFileName(it))
        }
        listOf("3f2504e0-4f89-11d3.jpg", "photo.png", "abc123.webp").forEach {
            assertTrue("\"$it\" was rejected", Photos.isSafeFileName(it))
        }
    }

    /**
     * Photos are megabytes. A delete that removes the row but not the file, done
     * fifty times, is a gigabyte of pictures nothing will ever display.
     */
    @Test
    fun `files nothing points at are found so they can be cleaned up`() {
        val onDisk = setOf("a.jpg", "b.jpg", "c.jpg")
        val referenced = setOf("a.jpg", "c.jpg")
        assertEquals(setOf("b.jpg"), Photos.orphanedFiles(onDisk, referenced))
        assertEquals(emptySet<String>(), Photos.missingFiles(onDisk, referenced))
    }

    /**
     * The other direction is not cleanup. A backup that carried the database but not
     * the pictures lands here, and deleting those rows would throw away the dates —
     * the only part still worth having. The UI says the picture is missing instead.
     */
    @Test
    fun `rows whose file is gone are reported rather than deleted`() {
        val onDisk = setOf("a.jpg")
        val referenced = setOf("a.jpg", "gone.jpg")
        assertEquals(setOf("gone.jpg"), Photos.missingFiles(onDisk, referenced))
        assertEquals(emptySet<String>(), Photos.orphanedFiles(onDisk, referenced))
    }

    /**
     * Oldest against newest, because adjacent weeks look identical and the distance
     * travelled is the whole reason for taking these.
     */
    @Test
    fun `the comparison spans the whole history, not the last two`() {
        val days = listOf(100L, 250L, 175L, 300L)
        val span = Photos.spanToCompare(days) { it }
        assertEquals(100L to 300L, span)
        assertEquals(200L, Photos.daysBetween(100L, 300L))
    }

    @Test
    fun `there is no comparison to make from a single day`() {
        assertNull(Photos.spanToCompare(emptyList<Long>()) { it })
        assertNull(Photos.spanToCompare(listOf(100L)) { it })
        // Three angles shot the same morning is not a before and after.
        assertNull(Photos.spanToCompare(listOf(100L, 100L, 100L)) { it })
    }
}
