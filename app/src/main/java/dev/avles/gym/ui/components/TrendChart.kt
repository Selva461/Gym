package dev.avles.gym.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Line

/**
 * Drawn by hand rather than pulled from a charting library. Two reasons: one less
 * dependency to keep alive in CI, and a stock chart would arrive with axes,
 * gridlines and a legend that this screen does not need. What matters is the
 * shape of the line and where it ends.
 */
@Composable
fun TrendChart(
    values: List<Double>,
    accent: Color,
    modifier: Modifier = Modifier,
    heightDp: Int = 140,
    emptyMessage: String = "Two entries will draw a line here."
) {
    if (values.size < 2) {
        Box(
            modifier.fillMaxWidth().height(heightDp.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(emptyMessage, style = MaterialTheme.typography.bodyMedium, color = Faint)
        }
        return
    }

    val min = values.min()
    val max = values.max()
    val span = if (max - min < 0.0001) 1.0 else max - min

    Canvas(modifier.fillMaxWidth().height(heightDp.dp)) {
        val w = size.width
        val h = size.height
        val padY = h * 0.12f
        val usableH = h - padY * 2

        fun px(i: Int) = w * (i.toFloat() / (values.size - 1).toFloat())
        fun py(v: Double) = padY + usableH - (((v - min) / span).toFloat() * usableH)

        // Baseline, so a flat line still has something to sit on.
        drawLine(
            color = Line,
            start = Offset(0f, h - padY / 2),
            end = Offset(w, h - padY / 2),
            strokeWidth = 1f
        )

        val line = Path().apply {
            moveTo(px(0), py(values[0]))
            for (i in 1 until values.size) lineTo(px(i), py(values[i]))
        }

        val fill = Path().apply {
            addPath(line)
            lineTo(px(values.size - 1), h)
            lineTo(px(0), h)
            close()
        }

        drawPath(
            path = fill,
            brush = Brush.verticalGradient(
                listOf(accent.copy(alpha = 0.22f), Color.Transparent)
            )
        )

        drawPath(path = line, color = accent, style = Stroke(width = 2.5f))

        // Only the latest point gets a marker. Dotting every point makes noise
        // look like information.
        drawCircle(
            color = accent,
            radius = 4f,
            center = Offset(px(values.size - 1), py(values.last()))
        )
    }
}

/** Bars for weekly training volume. Same restraint: no axes, no gridlines. */
@Composable
fun BarStrip(
    values: List<Double>,
    accent: Color,
    modifier: Modifier = Modifier,
    heightDp: Int = 90
) {
    if (values.isEmpty()) {
        EmptyNote("Finish a session and the week fills in here.", modifier)
        return
    }
    val max = values.max().coerceAtLeast(1.0)
    Canvas(modifier.fillMaxWidth().height(heightDp.dp)) {
        val slot = size.width / values.size
        val barW = slot * 0.5f
        values.forEachIndexed { i, v ->
            val barH = ((v / max).toFloat() * size.height).coerceAtLeast(2f)
            drawRoundRect(
                color = if (v <= 0.0) Line else accent,
                topLeft = Offset(i * slot + (slot - barW) / 2, size.height - barH),
                size = androidx.compose.ui.geometry.Size(barW, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 3)
            )
        }
    }
}
