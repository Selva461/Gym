package dev.avles.gym.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dev.avles.gym.core.GymLog
import dev.avles.gym.domain.Photos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** What the Android photo picker hands back: a URI into another app's storage. */
class UriImage(val uri: Uri) : PickedImage

/**
 * Where pictures physically live on Android, and the only code that touches them.
 *
 * Three decisions worth knowing before changing anything here.
 *
 * **The bytes are copied, not referenced.** The picker hands back a `content://`
 * URI pointing into another app's storage, under a grant that dies with this
 * process. A row holding one of those would work this afternoon and show nothing
 * next week. So the image is read once, re-encoded, and written into this app's
 * own directory; the URI is never stored.
 *
 * **Internal storage, not the gallery.** `filesDir` is readable only by this app's
 * UID. Writing progress photos to MediaStore would put them in the phone's gallery
 * beside holiday pictures and hand them to every gallery app, cloud sync and
 * lock-screen slideshow on the device. That is not a thing to do with photographs
 * of somebody's body without them asking for it.
 *
 * **The picture is re-encoded rather than copied verbatim.** It costs a decode, and
 * it buys three things: a 12 MB phone photo becomes a few hundred KB, so a year of
 * these does not fill the phone; the orientation tag is applied to the pixels, so
 * nothing downstream has to remember to rotate; and whatever else the original file
 * carried — GPS coordinates, device serial, the lot — does not survive the round
 * trip. A progress photo with the gym's location in it is a worse thing to keep
 * than one without.
 */
class AndroidPhotoStore(private val context: Context) : PhotoStore {

    private val directory: File
        get() = File(context.filesDir, Photos.DIRECTORY).apply { if (!exists()) mkdirs() }

    /**
     * Resolves a stored name to a file, or null if the name is not one this app
     * would have written.
     *
     * Every name in the database came from [Photos.newFileName], so the check is
     * always redundant in a correct app — which is why it is here. A restored or
     * edited database is the case that matters, and a path assembled from a string
     * that came off disk is not safe merely because it usually is.
     */
    private fun fileFor(fileName: String): File? {
        if (!Photos.isSafeFileName(fileName)) {
            GymLog.w(GymLog.Tag.BACKUP) { "refusing to resolve an unsafe photo name" }
            return null
        }
        return File(directory, fileName)
    }

    override fun pathFor(fileName: String): String? = fileFor(fileName)?.absolutePath

    /** Names currently on disk, for reconciling against what the database believes. */
    override fun fileNamesOnDisk(): Set<String> =
        directory.listFiles()?.mapNotNull { if (it.isFile) it.name else null }?.toSet() ?: emptySet()

    /**
     * Copies a picked image in and returns the name it was stored under, or null if
     * it could not be read.
     *
     * Failure is null rather than an exception: the caller is a button press, the
     * user picked something unreadable, and the honest response is to say so and
     * leave the database untouched — not to crash the app between two sets.
     */
    override suspend fun import(image: PickedImage): String? {
        val uri = (image as? UriImage)?.uri ?: return null
        // Decoding and re-encoding a phone photo takes long enough to drop frames,
        // so it never runs on the thread that draws.
        return withContext(Dispatchers.IO) { importFrom(uri) }
    }

    private fun importFrom(uri: Uri): String? {
        val mimeType = runCatching { context.contentResolver.getType(uri) }.getOrNull()
        val fileName = Photos.newFileName(UUID.randomUUID().toString(), "image/jpeg")
        val target = fileFor(fileName) ?: return null

        return try {
            val bitmap = decodeScaled(uri) ?: return null
            val upright = applyOrientation(uri, bitmap)
            target.outputStream().use { out ->
                upright.compress(Bitmap.CompressFormat.JPEG, QUALITY, out)
            }
            if (upright !== bitmap) bitmap.recycle()
            upright.recycle()
            GymLog.i(GymLog.Tag.BACKUP) { "stored a photo (source type $mimeType)" }
            fileName
        } catch (e: Exception) {
            // A partially written file is worse than none: it would be an orphan the
            // reconciler has to find, or a row pointing at a corrupt image.
            runCatching { target.delete() }
            GymLog.e(GymLog.Tag.BACKUP, e) { "could not store the picked photo" }
            null
        }
    }

    /** Removes a file. Missing is success — the caller wanted it gone. */
    override fun delete(fileName: String): Boolean =
        fileFor(fileName)?.let { !it.exists() || it.delete() } ?: false

    /** Total bytes the photos take, so Settings can say rather than the user guessing. */
    override fun bytesUsed(): Long =
        directory.listFiles()?.filter { it.isFile }?.sumOf { it.length() } ?: 0L

    /**
     * Decodes at roughly the size we intend to keep.
     *
     * Two passes: the first reads only the header for the dimensions, the second
     * decodes with inSampleSize so the full-resolution bitmap never exists. A 108
     * megapixel photo decoded at full size is around 400 MB of heap and an
     * OutOfMemoryError; this keeps it to a few megabytes.
     */
    private fun decodeScaled(uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        } ?: return null

        var sample = 1
        var longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / sample > MAX_EDGE_PX * 2) sample *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        longest = maxOf(decoded.width, decoded.height)
        if (longest <= MAX_EDGE_PX) return decoded

        val scale = MAX_EDGE_PX.toFloat() / longest
        val scaled = Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * scale).toInt().coerceAtLeast(1),
            (decoded.height * scale).toInt().coerceAtLeast(1),
            true
        )
        if (scaled !== decoded) decoded.recycle()
        return scaled
    }

    /**
     * Rotates the pixels to match the file's orientation tag.
     *
     * A phone held upright usually writes landscape pixels plus "rotate 90". Neither
     * BitmapFactory nor Compose applies that, so without this every portrait photo
     * shows on its side. Doing it once here means nothing downstream has to know.
     */
    private fun applyOrientation(uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = runCatching {
            context.contentResolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return runCatching {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }.getOrDefault(bitmap)
    }

    private companion object {
        /**
         * Long edge in pixels. Comfortably sharper than any phone screen for a photo
         * viewed at full width, and about 200-400 KB per picture — so a photo a day
         * for a year is well under a tenth of a gigabyte.
         */
        const val MAX_EDGE_PX = 1440

        /** High enough that skin and lighting survive; low enough to matter for size. */
        const val QUALITY = 88
    }
}
