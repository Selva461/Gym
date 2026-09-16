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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import dev.avles.gym.domain.Muscle
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Line
import dev.avles.gym.ui.theme.Mute

/**
 * A body diagram with the worked muscles shaded — front view and back view.
 *
 * This is the piece that replaces the animated demo the app cannot ship. It carries
 * the same information the red shading in one of those GIFs carries, which is the
 * part that actually answers "what does this work", and it does it as vectors: a
 * few kilobytes, sharp at any size, no licensing question, and it covers all 394
 * lifts because it is driven by [Muscle] rather than by a picture per exercise.
 *
 * Deliberately schematic. A recognisable silhouette with regions you can point at
 * beats an anatomical drawing nobody can read at 120dp, and it makes the honest
 * claim: this is roughly where the work lands, not a rendering of your anatomy.
 *
 * Everything is laid out in a normalised 0..1 box and scaled at draw time, so the
 * figure is resolution-independent and the coordinates below can be read as
 * fractions of the body: 0.0 is the top of the head, 1.0 is the soles of the feet.
 */

/** Which side of the body a region is visible from. */
private enum class View { FRONT, BACK }

/**
 * One drawable patch of muscle, in normalised coordinates.
 *
 * [mirrored] draws the same shape on both sides of the midline, which is what
 * nearly every muscle wants and saves declaring each one twice. [rounding] is a
 * fraction of the shape's smaller dimension, so 0.5 gives a capsule and 0.15 a
 * soft-cornered slab.
 */
private data class Patch(
    val view: View,
    val muscle: Muscle,
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val mirrored: Boolean = true,
    val rounding: Float = 0.5f
)

/**
 * The silhouette, drawn underneath everything as a flat shape so that unworked
 * muscle still reads as part of a body rather than as empty space.
 */
private val SILHOUETTE = listOf(
    // head, neck
    Triple(0.50f, 0.055f, 0.105f) to 0.085f,
    Triple(0.50f, 0.115f, 0.060f) to 0.040f
)

private val PATCHES = listOf(
    // ---- front ----------------------------------------------------------
    Patch(View.FRONT, Muscle.TRAPS_UPPER, 0.385f, 0.132f, 0.115f, 0.040f, rounding = 0.4f),
    Patch(View.FRONT, Muscle.DELT_FRONT, 0.290f, 0.160f, 0.105f, 0.085f),
    Patch(View.FRONT, Muscle.DELT_SIDE, 0.262f, 0.168f, 0.070f, 0.080f),
    Patch(View.FRONT, Muscle.CHEST_UPPER, 0.385f, 0.178f, 0.110f, 0.034f, rounding = 0.35f),
    Patch(View.FRONT, Muscle.CHEST_MID, 0.385f, 0.214f, 0.110f, 0.036f, rounding = 0.35f),
    Patch(View.FRONT, Muscle.CHEST_LOWER, 0.385f, 0.252f, 0.110f, 0.030f, rounding = 0.35f),
    Patch(View.FRONT, Muscle.SERRATUS, 0.348f, 0.262f, 0.040f, 0.048f, rounding = 0.3f),
    Patch(View.FRONT, Muscle.BICEPS, 0.268f, 0.250f, 0.070f, 0.078f),
    Patch(View.FRONT, Muscle.BRACHIALIS, 0.262f, 0.312f, 0.060f, 0.036f),
    Patch(View.FRONT, Muscle.FOREARM_FLEXORS, 0.252f, 0.348f, 0.068f, 0.092f),
    Patch(View.FRONT, Muscle.GRIP, 0.248f, 0.442f, 0.060f, 0.046f),
    Patch(View.FRONT, Muscle.ABS, 0.437f, 0.290f, 0.058f, 0.135f, mirrored = false, rounding = 0.2f),
    Patch(View.FRONT, Muscle.OBLIQUES, 0.372f, 0.300f, 0.048f, 0.115f, rounding = 0.3f),
    Patch(View.FRONT, Muscle.HIP_FLEXORS, 0.395f, 0.428f, 0.075f, 0.040f, rounding = 0.3f),
    Patch(View.FRONT, Muscle.ADDUCTORS, 0.412f, 0.470f, 0.060f, 0.120f, rounding = 0.3f),
    Patch(View.FRONT, Muscle.ABDUCTORS, 0.322f, 0.462f, 0.052f, 0.090f, rounding = 0.3f),
    Patch(View.FRONT, Muscle.QUADS, 0.345f, 0.470f, 0.110f, 0.200f, rounding = 0.35f),
    Patch(View.FRONT, Muscle.CALVES_GASTROC, 0.360f, 0.712f, 0.088f, 0.130f, rounding = 0.4f),
    Patch(View.FRONT, Muscle.CALVES_SOLEUS, 0.368f, 0.842f, 0.072f, 0.056f, rounding = 0.4f),

    // ---- back -----------------------------------------------------------
    Patch(View.BACK, Muscle.TRAPS_UPPER, 0.372f, 0.128f, 0.128f, 0.055f, rounding = 0.35f),
    Patch(View.BACK, Muscle.TRAPS_MID, 0.390f, 0.188f, 0.108f, 0.070f, rounding = 0.25f),
    Patch(View.BACK, Muscle.RHOMBOIDS, 0.398f, 0.196f, 0.075f, 0.052f, rounding = 0.2f),
    Patch(View.BACK, Muscle.DELT_REAR, 0.286f, 0.162f, 0.105f, 0.082f),
    Patch(View.BACK, Muscle.LATS, 0.350f, 0.232f, 0.145f, 0.135f, rounding = 0.3f),
    Patch(View.BACK, Muscle.TRICEPS, 0.264f, 0.246f, 0.074f, 0.088f),
    Patch(View.BACK, Muscle.FOREARM_EXTENSORS, 0.252f, 0.344f, 0.068f, 0.096f),
    Patch(View.BACK, Muscle.GRIP, 0.248f, 0.444f, 0.060f, 0.046f),
    Patch(View.BACK, Muscle.ERECTORS, 0.440f, 0.300f, 0.052f, 0.132f, mirrored = false, rounding = 0.25f),
    Patch(View.BACK, Muscle.GLUTES, 0.372f, 0.432f, 0.118f, 0.098f, rounding = 0.4f),
    Patch(View.BACK, Muscle.HAMSTRINGS, 0.352f, 0.534f, 0.108f, 0.170f, rounding = 0.35f),
    Patch(View.BACK, Muscle.CALVES_GASTROC, 0.358f, 0.710f, 0.092f, 0.132f, rounding = 0.4f),
    Patch(View.BACK, Muscle.CALVES_SOLEUS, 0.368f, 0.844f, 0.072f, 0.056f, rounding = 0.4f)
)

/**
 * Front and back views side by side, with [primary] shaded solidly and [secondary]
 * at a lower intensity.
 *
 * A muscle with no patch on either view simply does not draw — grip on a carry is
 * real work with nowhere sensible to put it — so the map never claims to be the
 * complete list. That is what the text beside it is for.
 */
@Composable
fun MuscleMap(
    primary: List<Muscle>,
    secondary: List<Muscle>,
    modifier: Modifier = Modifier,
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
            listOf(View.FRONT to "Front", View.BACK to "Back").forEach { (view, label) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.52f)
                    ) {
                        drawBody(view, primary, secondary, accent)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = label,
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
            KeyLine("Also", secondary.joinToString(", ") { it.label }, accent.copy(alpha = 0.32f), Mute)
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

/**
 * Draws one view: silhouette first, then every patch that belongs to a worked
 * muscle on top. Primary is drawn after secondary so an overlap reads as primary,
 * which is the more useful of the two to see.
 */
private fun DrawScope.drawBody(
    view: View,
    primary: List<Muscle>,
    secondary: List<Muscle>,
    accent: Color
) {
    val w = size.width
    val h = size.height

    fun patch(p: Patch, color: Color) {
        val radius = CornerRadius(minOf(p.w * w, p.h * h) * p.rounding)
        drawRoundRect(
            color = color,
            topLeft = Offset(p.x * w, p.y * h),
            size = Size(p.w * w, p.h * h),
            cornerRadius = radius
        )
        if (p.mirrored) {
            // Mirror about the midline. The right edge of the mirrored copy sits the
            // same distance from 0.5 as the original's left edge, which keeps the
            // figure symmetrical without declaring every muscle twice.
            val mirroredX = (1f - p.x - p.w) * w
            drawRoundRect(
                color = color,
                topLeft = Offset(mirroredX, p.y * h),
                size = Size(p.w * w, p.h * h),
                cornerRadius = radius
            )
        }
    }

    // Head and neck, which are never shaded but are what make the shape read as a body.
    SILHOUETTE.forEach { (spec, height) ->
        val (cx, cy, width) = spec
        drawOval(
            color = Line,
            topLeft = Offset((cx - width / 2f) * w, (cy - height / 2f) * h),
            size = Size(width * w, height * h)
        )
    }

    // Everything unworked, so the figure is a body rather than floating patches.
    PATCHES.filter { it.view == view }.forEach { patch(it, Line) }

    PATCHES.filter { it.view == view && it.muscle in secondary }
        .forEach { patch(it, accent.copy(alpha = 0.30f)) }
    PATCHES.filter { it.view == view && it.muscle in primary }
        .forEach { patch(it, accent.copy(alpha = 0.92f)) }
}
