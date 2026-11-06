package dev.avles.gym.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.avles.gym.domain.Muscle
import dev.avles.gym.domain.anatomy.BodyAtlas
import dev.avles.gym.domain.anatomy.BodyBuild
import dev.avles.gym.domain.anatomy.BodyView
import dev.avles.gym.domain.anatomy.Outline
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Ink
import dev.avles.gym.ui.theme.Mute

/**
 * A body diagram with the worked muscles shaded, front view and back view.
 *
 * The anatomy is [BodyAtlas]: about forty muscle outlines a side, authored once on
 * one half of the body and mirrored, so the figure is symmetric by construction, and
 * shared between the two views so both figures stand at the same size. [build]
 * matches the demonstrator the user picked (male or female proportions).
 *
 * Vectors, a few kilobytes, sharp at any size, nothing licensed, and it covers every
 * lift because it is driven by [Muscle] rather than by a picture per exercise.
 */

/** Unworked muscle, a step lighter than the silhouette so the anatomy still reads. */
private val Muscle0 = Color(0xFF3A4256)
/** Anatomy with no exercise-data name (tibialis, rotator cuff): shape only. */
private val Detail = Color(0xFF30374A)
private val Body = Color(0xFF262C3A)

/**
 * Front and back views side by side, with [primary] shaded solidly and [secondary]
 * at a lower intensity.
 */
@Composable
fun MuscleMap(
    primary: List<Muscle>,
    secondary: List<Muscle>,
    modifier: Modifier = Modifier,
    build: BodyBuild = BodyBuild.MALE,
    accent: Color = Brass
) {
    val described = (primary.map { it.label } + secondary.map { it.label }).joinToString(", ")
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Muscles worked: $described" },
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BodyView.entries.forEach { view ->
                val figure = remember(view, build) { BodyAtlas.figure(view, build) }
                val paths = remember(figure) {
                    figure.silhouette.map { it.toPath() } to figure.muscles.map { it to it.toPath() }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(BodyAtlas.WIDTH / BodyAtlas.HEIGHT)
                    ) {
                        drawBody(paths.first, paths.second, primary.toSet(), secondary.toSet(), accent)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = view.label,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = Faint,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        MuscleKey(primary, secondary, accent)
    }
}

/**
 * Names what is shaded. Without it the map is a picture of a person with orange
 * blobs on them — the legend is what turns it into information.
 */
@Composable
private fun MuscleKey(primary: List<Muscle>, secondary: List<Muscle>, accent: Color) {
    Column {
        if (primary.isNotEmpty()) {
            KeyLine("Works", primary.joinToString(", ") { it.label }, accent, accent)
        }
        if (secondary.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            KeyLine("Also", secondary.joinToString(", ") { it.label }, accent.copy(alpha = 0.42f).compositeOverSolid(Muscle0), Mute)
        }
    }
}

@Composable
private fun KeyLine(label: String, muscles: String, swatch: Color, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(9.dp)) {
            drawRoundRect(swatch, cornerRadius = CornerRadius(size.minDimension / 2f))
        }
        Text(
            text = "  $label  ",
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = Faint
        )
        Text(
            text = muscles,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(end = 2.dp)
        )
    }
}

private fun Outline.toPath(): Path = Path().apply {
    val p = path
    moveTo(p[0], p[1])
    var i = 2
    while (i + 5 < p.size) {
        cubicTo(p[i], p[i + 1], p[i + 2], p[i + 3], p[i + 4], p[i + 5])
        i += 6
    }
    close()
}

/**
 * Draws one view in atlas units scaled to the canvas: silhouette, then every muscle
 * outlined in the background colour so neighbouring muscles stay separate even when
 * both are lit.
 */
private fun DrawScope.drawBody(
    silhouette: List<Path>,
    muscles: List<Pair<Outline, Path>>,
    primary: Set<Muscle>,
    secondary: Set<Muscle>,
    accent: Color
) {
    val k = minOf(size.width / BodyAtlas.WIDTH, size.height / BodyAtlas.HEIGHT)
    val dx = (size.width - BodyAtlas.WIDTH * k) / 2f
    val edge = Stroke(width = 1.1f)
    val worked = accent.copy(alpha = 0.95f)
    val also = accent.copy(alpha = 0.42f).compositeOverSolid(Muscle0)
    withTransform({
        translate(dx, 0f)
        scale(k, k, pivot = androidx.compose.ui.geometry.Offset.Zero)
    }) {
        silhouette.forEach { drawPath(it, Body) }
        muscles.forEach { (outline, path) ->
            val m = outline.muscle
            val fill = when {
                m == null -> Detail
                m in primary -> worked
                m in secondary -> also
                else -> Muscle0
            }
            drawPath(path, fill)
            drawPath(path, Ink, style = edge)
        }
    }
}

/** [this] laid over an opaque [base], so a secondary muscle is a solid colour. */
private fun Color.compositeOverSolid(base: Color): Color = Color(
    red = red * alpha + base.red * (1 - alpha),
    green = green * alpha + base.green * (1 - alpha),
    blue = blue * alpha + base.blue * (1 - alpha)
)
