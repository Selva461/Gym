package dev.avles.gym.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import dev.avles.gym.data.PickedImage

/**
 * Decodes a stored photo at roughly [maxWidthPx] wide, or returns null if the file
 * is missing or unreadable. Blocking: call it off the thread that draws.
 *
 * Stored photos are already upright and already scaled — the platform's
 * PhotoStore did that on the way in — so this only has to avoid decoding a
 * thumbnail at full size.
 */
internal expect fun decodeImageFile(path: String, maxWidthPx: Int): ImageBitmap?

/**
 * The platform's own photo picker. Returns the function that opens it; [onPicked]
 * gets what was chosen, or null if the picker was dismissed.
 *
 * Neither platform needs a permission for this. The picker runs outside the app
 * and hands back only the one picture chosen, where a storage permission would
 * grant every picture on the phone, forever, to import one.
 */
@Composable
internal expect fun rememberImagePicker(onPicked: (PickedImage?) -> Unit): () -> Unit
