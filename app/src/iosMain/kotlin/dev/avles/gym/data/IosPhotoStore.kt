@file:OptIn(ExperimentalForeignApi::class)

package dev.avles.gym.data

import dev.avles.gym.core.GymLog
import dev.avles.gym.domain.Photos
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSUUID
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import kotlin.math.max
import kotlin.math.min

/** What the iOS photo picker hands back: the picked image, still encoded. */
class IosImage(val data: NSData) : PickedImage

/**
 * Where pictures live on iOS. The same three decisions as on Android, for the
 * same reasons: the bytes are copied in rather than referenced; they stay in the
 * app's private directory, never the photo library; and they are re-encoded, which
 * applies the orientation to the pixels, shrinks a 12 MB photo to a few hundred KB
 * and drops the location and camera metadata along the way.
 *
 * The directory is also excluded from iCloud backup, matching Android, where
 * photos are left out of cloud backup on purpose.
 */
class IosPhotoStore : PhotoStore {

    private val directory: String by lazy {
        val path = "${appDataDirectory()}/${Photos.DIRECTORY}"
        NSFileManager.defaultManager.createDirectoryAtPath(
            path,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
        NSURL.fileURLWithPath(path).setResourceValue(true, forKey = NSURLIsExcludedFromBackupKey, error = null)
        path
    }

    override suspend fun import(image: PickedImage): String? {
        val data = (image as? IosImage)?.data ?: return null
        return withContext(Dispatchers.IO) {
            val fileName = Photos.newFileName(NSUUID().UUIDString, "image/jpeg")
            val path = pathFor(fileName) ?: return@withContext null
            val jpeg = UIImage.imageWithData(data)?.let { UIImageJPEGRepresentation(upright(it), QUALITY) }
            if (jpeg != null && NSFileManager.defaultManager.createFileAtPath(path, contents = jpeg, attributes = null)) {
                GymLog.i(GymLog.Tag.BACKUP) { "stored a photo" }
                fileName
            } else {
                // A half-written file would be an orphan or a corrupt picture.
                NSFileManager.defaultManager.removeItemAtPath(path, error = null)
                GymLog.w(GymLog.Tag.BACKUP) { "could not store the picked photo" }
                null
            }
        }
    }

    override fun pathFor(fileName: String): String? {
        if (!Photos.isSafeFileName(fileName)) {
            GymLog.w(GymLog.Tag.BACKUP) { "refusing to resolve an unsafe photo name" }
            return null
        }
        return "$directory/$fileName"
    }

    override fun fileNamesOnDisk(): Set<String> =
        NSFileManager.defaultManager.contentsOfDirectoryAtPath(directory, error = null)
            ?.mapNotNull { it as? String }
            ?.toSet()
            ?: emptySet()

    override fun delete(fileName: String): Boolean {
        val path = pathFor(fileName) ?: return false
        val files = NSFileManager.defaultManager
        return !files.fileExistsAtPath(path) || files.removeItemAtPath(path, error = null)
    }

    override fun bytesUsed(): Long = fileNamesOnDisk().sumOf { sizeOf("$directory/$it") }

    private fun sizeOf(path: String): Long {
        val attributes = NSFileManager.defaultManager.attributesOfItemAtPath(path, error = null) ?: return 0L
        return when (val size = attributes[NSFileSize]) {
            is NSNumber -> size.longLongValue
            is Long -> size
            else -> 0L
        }
    }

    /**
     * Redraws the picture upright and no longer than [MAX_EDGE_PX] on its long
     * side. Drawing a UIImage applies its orientation, which is the whole trick:
     * the result has none left to apply.
     */
        private fun upright(source: UIImage): UIImage {
        val (width, height) = source.size.useContents { width to height }
        val scale = min(1.0, MAX_EDGE_PX / max(width, height))
        val w = (width * scale).coerceAtLeast(1.0)
        val h = (height * scale).coerceAtLeast(1.0)
        val format = UIGraphicsImageRendererFormat()
        format.scale = 1.0
        val renderer = UIGraphicsImageRenderer(size = CGSizeMake(w, h), format = format)
        return renderer.imageWithActions { _ -> source.drawInRect(CGRectMake(0.0, 0.0, w, h)) }
    }

    private companion object {
        const val MAX_EDGE_PX = 1440.0
        const val QUALITY = 0.88
    }
}
