package dev.avles.gym.domain

/**
 * The rules for storing pictures, kept away from Android so they can be tested.
 *
 * A photo is the most sensitive thing this app will ever hold — a body in a mirror
 * is not a row of numbers — and it is also the first thing here that arrives from
 * *outside* the app. Everything else in this database was typed into it. A picked
 * image comes through another app's content provider, carrying a name and a type
 * that neither this app nor the user chose, and that is a different trust problem
 * from every other value in the schema.
 *
 * So none of the file's own metadata is kept. The name is generated here, the
 * extension comes from a fixed list, and the bytes are copied into the app's
 * private directory rather than referenced where they lie. See SECURITY.md.
 */
object Photos {

    /**
     * Where pictures live, relative to `filesDir` — internal storage, readable only
     * by this app's UID.
     *
     * Not `getExternalFilesDir`, which any app holding storage permission can read
     * on older releases, and not MediaStore, which would put progress photos in the
     * phone's gallery beside everything else and hand them to every gallery app,
     * cloud sync and lock-screen slideshow on the device.
     */
    const val DIRECTORY = "photos"

    /**
     * Image types the app will accept, mapped to the extension it stores them under.
     *
     * An allowlist rather than "whatever the provider said". The extension is only
     * a label here — nothing executes these files — but a name is a name, and one
     * assembled from an untrusted string is how a file ends up somewhere it was not
     * meant to be.
     */
    private val EXTENSIONS = mapOf(
        "image/jpeg" to "jpg",
        "image/jpg" to "jpg",
        "image/png" to "png",
        "image/webp" to "webp",
        "image/heic" to "heic",
        "image/heif" to "heif"
    )

    /** Falls back to jpg, which is what the encoder writes when re-encoding anyway. */
    fun extensionFor(mimeType: String?): String =
        EXTENSIONS[mimeType?.lowercase()?.substringBefore(';')?.trim()] ?: "jpg"

    /**
     * A name for a newly stored picture.
     *
     * [token] is expected to be a UUID; anything it contains that is not a letter,
     * a digit or a dash is stripped rather than trusted, because the one thing that
     * must not happen here is a caller passing something derived from the picked
     * file and this function faithfully building a path out of it.
     */
    fun newFileName(token: String, mimeType: String?): String {
        val safe = token.lowercase().filter { it.isLetterOrDigit() || it == '-' }.take(48)
        val stem = safe.ifBlank { "photo" }
        return "$stem.${extensionFor(mimeType)}"
    }

    /**
     * Whether a name out of the database is safe to resolve inside [DIRECTORY].
     *
     * Every name is written by [newFileName], so in a correct app this is always
     * true — which is exactly why it is checked. A database that has been restored,
     * edited or corrupted is the case that matters, and "it cannot happen" is not a
     * property of a file path that came off disk.
     *
     * The character allowlist at the end is what does the work: it admits letters,
     * digits, dash and dot and nothing else, so separators, null bytes and every
     * other way of escaping the directory fail on it. The named checks above are
     * there to be read, so that the intent survives someone widening the allowlist.
     */
    fun isSafeFileName(name: String): Boolean =
        name.isNotBlank() &&
            name.length <= 64 &&
            !name.startsWith(".") &&
            ".." !in name &&
            name.all { it.isLetterOrDigit() || it == '-' || it == '.' }

    /**
     * Files in the photo directory that nothing in the database points at.
     *
     * These accumulate: a delete that removes the row but not the file, a copy that
     * succeeded just before the insert that failed. Photos are megabytes, so left
     * alone this is the app slowly filling the phone with pictures nothing will ever
     * show. Reconciling on launch is cheap and makes the leak impossible rather than
     * unlikely.
     */
    fun orphanedFiles(onDisk: Set<String>, referenced: Set<String>): Set<String> =
        onDisk - referenced

    /**
     * Rows whose file is gone — the other direction, and the one the UI has to
     * handle rather than clean up. A restored backup that carried the database but
     * not the pictures lands here, and the honest response is to say the picture is
     * missing rather than to show a broken frame or to delete the row and lose the
     * date it recorded.
     */
    fun missingFiles(onDisk: Set<String>, referenced: Set<String>): Set<String> =
        referenced - onDisk

    /**
     * The two pictures worth putting side by side: the oldest and the newest.
     *
     * That is the comparison people actually want — the point of a progress photo is
     * the distance travelled, and adjacent weeks look identical. Null when there is
     * nothing to compare, which is one photo, none, or several from a single day; a
     * photo against itself is not a comparison.
     */
    fun <T> spanToCompare(items: List<T>, dayOf: (T) -> Long): Pair<T, T>? {
        if (items.size < 2) return null
        val sorted = items.sortedBy(dayOf)
        val first = sorted.first()
        val last = sorted.last()
        return if (dayOf(first) == dayOf(last)) null else first to last
    }

    /** Days apart, for the caption under a comparison. */
    fun daysBetween(fromEpochDay: Long, toEpochDay: Long): Long = toEpochDay - fromEpochDay
}
