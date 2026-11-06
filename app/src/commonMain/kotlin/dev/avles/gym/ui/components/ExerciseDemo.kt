package dev.avles.gym.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.avles.gym.core.GymLog
import dev.avles.gym.data.ExerciseDemos
import dev.avles.gym.resources.Res
import dev.avles.gym.ui.theme.Slate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * The movement, animated: start position, end position, back again.
 *
 * Two frames rather than a GIF, because that is what almost every exercise GIF
 * actually is once you strip the tweening — and two WebP stills cost about 25 KB
 * where the animated version of the same thing costs ten times that. All 273
 * bundled lifts together are roughly 6.4 MB, which is what made shipping them
 * possible at all: the app has no network permission and never will, so anything
 * it shows has to already be inside the app.
 *
 * Renders nothing when the lift has no bundled demonstration, which is most of
 * them — see [ExerciseDemos] for why the matching was kept deliberately strict.
 * The muscle map and the written guide are underneath either way.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun ExerciseDemo(
    exerciseName: String,
    modifier: Modifier = Modifier,
    frameMillis: Long = 850
) {
    val stem = ExerciseDemos.assetStemFor(exerciseName) ?: return

    val frames by produceState<List<ImageBitmap>>(initialValue = emptyList(), stem) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                (0..1).mapNotNull { index ->
                    Res.readBytes("files/demo/$stem-$index.webp").decodeToImageBitmap()
                }
            }.onFailure {
                GymLog.w(GymLog.Tag.UI) { "no demo frames for $stem" }
            }.getOrDefault(emptyList())
        }
    }

    if (frames.isEmpty()) return

    var index by remember(stem) { mutableIntStateOf(0) }
    LaunchedEffect(stem, frames.size) {
        // A plain loop rather than an infinite transition: the two positions are a
        // sequence, not an interpolation, and the pause at each end is what makes it
        // readable as a movement instead of a flicker.
        while (frames.size > 1) {
            delay(frameMillis)
            index = (index + 1) % frames.size
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Slate),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = index.coerceIn(0, frames.lastIndex),
            animationSpec = tween(durationMillis = 260),
            label = "demo"
        ) { frame ->
            Image(
                bitmap = frames[frame],
                contentDescription = "Demonstration of $exerciseName",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** Whether this lift has a bundled demonstration, so callers can lay out for it. */
fun hasExerciseDemo(exerciseName: String): Boolean =
    ExerciseDemos.assetStemFor(exerciseName) != null
