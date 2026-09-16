package dev.avles.gym.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.avles.gym.core.GymLog
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Slate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Shows a stored picture, decoding it off the main thread.
 *
 * No image library. Coil or Glide would do this in one line, and both bring a
 * networking stack into an app whose INTERNET permission is deliberately stripped
 * at build time — a dependency that cannot do its main job and widens the
 * dependency surface for nothing. These are local files of known size, already
 * downscaled on the way in, so a decode and a cache key are all that is missing and
 * both are a few lines.
 *
 * [file] being null, missing or unreadable all render the same quiet placeholder.
 * A photo whose file is gone — a backup that carried the database but not the
 * pictures — is a real state, and it should read as "the picture is missing" rather
 * than as a crash or an empty hole.
 */
@Composable
fun PhotoImage(
    file: File?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    maxWidthPx: Int = 1080
) {
    // Keyed on path and last-modified so replacing a lift's picture re-decodes
    // rather than serving the previous bitmap from a stale key.
    val key = file?.let { "${it.absolutePath}:${it.lastModified()}" }

    val bitmap by produceState<ImageBitmap?>(initialValue = null, key) {
        value = withContext(Dispatchers.IO) {
            if (file == null || !file.isFile) return@withContext null
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.absolutePath, bounds)
                var sample = 1
                while (bounds.outWidth / sample > maxWidthPx * 2) sample *= 2
                BitmapFactory
                    .decodeFile(file.absolutePath, BitmapFactory.Options().apply { inSampleSize = sample })
                    ?.asImageBitmap()
            }.onFailure {
                GymLog.w(GymLog.Tag.UI) { "could not decode a stored photo" }
            }.getOrNull()
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Slate),
        contentAlignment = Alignment.Center
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (file != null && !file.isFile) {
            Text(
                "picture missing",
                style = MaterialTheme.typography.labelSmall,
                color = Faint
            )
        }
        // Still decoding renders as the empty panel, which is what a spinner that
        // appears for eighty milliseconds would look like anyway.
    }
}
