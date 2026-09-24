@file:OptIn(ExperimentalForeignApi::class)

package dev.avles.gym.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import dev.avles.gym.data.IosImage
import dev.avles.gym.data.PickedImage
import dev.avles.gym.data.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import platform.Foundation.NSFileManager
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.math.roundToInt

internal actual fun decodeImageFile(path: String, maxWidthPx: Int): ImageBitmap? {
    val data = NSFileManager.defaultManager.contentsAtPath(path) ?: return null
    val image = Image.makeFromEncoded(data.toByteArray())
    if (image.width <= maxWidthPx * 2) return image.toComposeImageBitmap()
    // Scaled on the way to the screen so a grid of thumbnails is not a grid of
    // full-size bitmaps, which is how an iOS app gets killed for memory.
    val width = maxWidthPx
    val height = (image.height * maxWidthPx.toFloat() / image.width).roundToInt().coerceAtLeast(1)
    val surface = Surface.makeRasterN32Premul(width, height)
    surface.canvas.drawImageRect(image, Rect.makeWH(width.toFloat(), height.toFloat()))
    return surface.makeImageSnapshot().toComposeImageBitmap()
}

@Composable
internal actual fun rememberImagePicker(onPicked: (PickedImage?) -> Unit): () -> Unit {
    val latest by rememberUpdatedState(onPicked)
    // Held here because the picker keeps only a weak reference to its delegate.
    val delegate = remember { PickerDelegate { image -> latest(image) } }
    return remember(delegate) {
        {
            val configuration = PHPickerConfiguration()
            configuration.filter = PHPickerFilter.imagesFilter
            configuration.selectionLimit = 1
            val picker = PHPickerViewController(configuration = configuration)
            picker.delegate = delegate
            topViewController()?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

/**
 * Receives the picker's answer. The image's bytes arrive on a background queue and
 * are handed on on the main one, where Compose expects its callbacks.
 */
private class PickerDelegate(
    private val onResult: (PickedImage?) -> Unit
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val result = didFinishPicking.firstOrNull() as? PHPickerResult
        if (result == null) {
            onResult(null)
            return
        }
        result.itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data, _ ->
            dispatch_async(dispatch_get_main_queue()) { onResult(data?.let(::IosImage)) }
        }
    }
}

private fun topViewController(): UIViewController? {
    var top = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (top?.presentedViewController != null) top = top.presentedViewController
    return top
}
