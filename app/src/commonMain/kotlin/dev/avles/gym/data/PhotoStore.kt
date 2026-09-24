package dev.avles.gym.data

import dev.avles.gym.core.GymLog
import dev.avles.gym.domain.Photos

/**
 * An image the platform's picker handed back, not yet copied into the app.
 *
 * Deliberately empty: on Android it is a content URI, on iOS the picked image's
 * bytes, and nothing outside the platform's own [PhotoStore] can do anything useful
 * with either. Common code only carries it from the picker to the store.
 */
interface PickedImage

/**
 * Where progress photos and lift pictures live: files in the app's private storage,
 * named by the database, never in the phone's gallery.
 *
 * The rules — which names are safe, what counts as an orphan — are in [Photos] and
 * are the same on both platforms. Only reading, scaling and writing the pixels is
 * platform work, and that is all an implementation of this does.
 */
interface PhotoStore {

    /**
     * Copies [image] in, upright and no larger than the app ever shows it, and
     * returns the stored file's name — or null if it could not be read, in which
     * case nothing is left behind.
     */
    suspend fun import(image: PickedImage): String?

    /** The absolute path of a stored photo, or null for a name that is not safe to resolve. */
    fun pathFor(fileName: String): String?

    fun fileNamesOnDisk(): Set<String>

    /** True when the file is gone afterwards, including when it was never there. */
    fun delete(fileName: String): Boolean

    fun bytesUsed(): Long
}

/**
 * Removes files no row points at. Called once on launch.
 *
 * Cheap — a directory listing and a set difference — and it turns "we might leak a
 * few hundred KB every time a delete half-fails" into something that cannot
 * accumulate.
 */
fun PhotoStore.deleteOrphans(referenced: Set<String>): Int {
    var removed = 0
    Photos.orphanedFiles(fileNamesOnDisk(), referenced).forEach { if (delete(it)) removed++ }
    if (removed > 0) GymLog.i(GymLog.Tag.BACKUP) { "cleaned up $removed orphaned photo(s)" }
    return removed
}
