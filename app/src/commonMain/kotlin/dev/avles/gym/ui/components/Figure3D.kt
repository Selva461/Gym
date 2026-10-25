package dev.avles.gym.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.VertexMode
import androidx.compose.ui.graphics.Vertices
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.avles.gym.anim.Avatar
import dev.avles.gym.anim.BodyMesh
import dev.avles.gym.anim.DEG
import dev.avles.gym.anim.Draw
import dev.avles.gym.anim.Motion
import dev.avles.gym.anim.Player
import dev.avles.gym.anim.Pose
import dev.avles.gym.anim.Renderer
import dev.avles.gym.anim.Step
import dev.avles.gym.anim.Vec3
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Slate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The demonstrator, live: [motion] played step by step by [avatar], turnable a full
 * 360 degrees with a finger and pinched closer or further.
 *
 * Every frame is worked out off the main thread (posing, skinning, projecting,
 * sorting) into a finished paint list, and the canvas only paints it. The skin goes
 * to the GPU as one vertex batch per run of triangles, where the platform can take
 * it, so a 20,000-triangle person costs about what a handful of shapes would.
 *
 * Until the avatar's mesh exists (half a second the first time, then cached) the
 * equipment shows alone with a short note, rather than a blank box.
 */
@Composable
fun Figure3D(
    motion: Motion,
    step: Step,
    avatar: Avatar,
    playing: Boolean,
    modifier: Modifier = Modifier,
    description: String = "Animated demonstration"
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var meshReady by remember(avatar) { mutableStateOf(BodyMesh.ready(avatar) != null) }
    var frame by remember { mutableStateOf<List<Op>>(emptyList()) }

    // The camera follows each step's suggested angle until the person turns it
    // themselves; after that it stays where they left it. A double tap gives it back.
    val yaw = remember(motion) { Animatable(motion.camera.yaw.toFloat()) }
    var pitch by remember(motion) { mutableStateOf(motion.camera.pitch) }
    var zoom by remember(motion) { mutableStateOf(1.0) }
    var turnedByHand by remember(motion) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(avatar) {
        if (!meshReady) {
            withContext(Dispatchers.Default) { BodyMesh.of(avatar) }
            meshReady = true
        }
    }
    LaunchedEffect(motion, step, turnedByHand) {
        if (!turnedByHand) yaw.animateTo((step.cameraYaw ?: motion.camera.yaw).toFloat(), tween(700))
    }

    val currentStep by rememberUpdatedState(step)
    val isPlaying by rememberUpdatedState(playing)
    val cameraNow by rememberUpdatedState(
        motion.camera.copy(yaw = yaw.value.toDouble(), pitch = pitch, distance = motion.camera.distance / zoom)
    )

    LaunchedEffect(motion, avatar, meshReady, size) {
        if (size.width == 0 || size.height == 0) return@LaunchedEffect
        var clock = 0.0
        var lastNanos = -1L
        var lastPaint = -1L
        var shown = currentStep
        var pose: Pose = Player.sample(motion, shown, 0.0)
        var from: Pose? = null
        var blend = 0.0
        var painted: Pair<Pose, Any>? = null
        while (isActive) {
            val now = withFrameNanos { it }
            val dt = if (lastNanos < 0) 0.0 else (now - lastNanos) / 1e9
            lastNanos = now
            val st = currentStep
            if (st != shown) {
                // Ease out of wherever the body is into the new step instead of jumping.
                from = pose; blend = 0.0; clock = 0.0; shown = st
            }
            if (isPlaying) clock += dt
            var next = Player.sample(motion, st, clock)
            from?.let { start ->
                blend += dt
                val t = blend / STEP_BLEND_SECONDS
                if (t >= 1.0) from = null else next = Player.transition(start, next, t)
            }
            pose = next
            val cam = cameraNow
            val key = pose to (cam to st.focus)
            // Nothing moved: keep the last picture. While playing, about 30 frames a
            // second is smooth for a slow rep and halves the work on a cheap phone.
            if (key == painted || (now - lastPaint) < FRAME_NANOS) continue
            painted = key
            lastPaint = now
            val w = size.width.toFloat()
            val h = size.height.toFloat()
            frame = withContext(Dispatchers.Default) { paintList(motion, pose, st, avatar, cam, w, h) }
        }
    }

    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Slate)
            .onSizeChanged { size = it }
            .pointerInput(motion) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    turnedByHand = true
                    scope.launch { yaw.snapTo(yaw.value - pan.x * DRAG_RADIANS_PER_PX) }
                    pitch = (pitch + pan.y * DRAG_RADIANS_PER_PX).coerceIn(-5 * DEG, 70 * DEG)
                    zoom = (zoom * gestureZoom).coerceIn(0.6, 2.2)
                }
            }
            .pointerInput(motion) {
                detectTapGestures(onDoubleTap = {
                    turnedByHand = false
                    pitch = motion.camera.pitch
                    zoom = 1.0
                })
            }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) { paint(frame) }
        if (!meshReady) {
            Text("Getting the demonstrator ready…", style = MaterialTheme.typography.labelSmall, color = Faint)
        }
    }
}

/**
 * Who demonstrates, everywhere a figure appears. Provided once at the top of the app
 * from the saved choice, so no screen has to pass it down.
 */
val LocalDemoAvatar = compositionLocalOf { Avatar.MALE }

/** How long the body takes to move from one step's position into the next. */
private const val STEP_BLEND_SECONDS = 0.6

private const val FRAME_NANOS = 30_000_000L

private const val DRAG_RADIANS_PER_PX = 0.008f

/**
 * Index arrays are 16-bit in the platform's vertex call, so one batch holds at most
 * this many corners; a body splits into two or three batches at most.
 */
private const val BATCH_CORNERS = 30_000

/** One thing to paint: a batch of skin triangles, or one shape of equipment or floor. */
internal sealed interface Op {
    class Skin(val vertices: Vertices) : Op
    class Shape(val draw: Draw) : Op
}

/** Pose, skin, project and sort one frame. Pure computation: safe on any thread. */
private fun paintList(motion: Motion, pose: Pose, step: Step, avatar: Avatar, camera: dev.avles.gym.anim.Camera, w: Float, h: Float): List<Op> {
    val (skeleton, prims) = motion.scene(pose, step.focus, avatar)
    val renderer = Renderer(w, h, camera)
    val floor = renderer.floor(Vec3.ZERO, 1.6, listOf(skeleton.pelvis))
    val draws = renderer.render(prims)
    val out = ArrayList<Op>(floor.size + 64)
    floor.forEach { out += Op.Shape(it) }
    if (!canDrawVertices) {
        draws.forEach { out += Op.Shape(it) }
        return out
    }
    // Consecutive triangles (already in back-to-front order) go out as one batch; any
    // piece of equipment between them closes the batch so the order is kept.
    val positions = ArrayList<Offset>(BATCH_CORNERS)
    val colours = ArrayList<Color>(BATCH_CORNERS)
    fun flush() {
        if (positions.isEmpty()) return
        out += Op.Skin(Vertices(VertexMode.Triangles, positions.toList(), positions.toList(), colours.toList(), List(positions.size) { it }))
        positions.clear(); colours.clear()
    }
    for (d in draws) {
        if (d is Draw.Tri) {
            if (positions.size + 3 > BATCH_CORNERS) flush()
            positions += Offset(d.x1, d.y1); positions += Offset(d.x2, d.y2); positions += Offset(d.x3, d.y3)
            colours += Color(d.c1); colours += Color(d.c2); colours += Color(d.c3)
        } else {
            flush()
            out += Op.Shape(d)
        }
    }
    flush()
    return out
}

private val skinPaint = Paint().apply { color = Color.White }

private fun DrawScope.paint(ops: List<Op>) {
    for (op in ops) when (op) {
        is Op.Skin -> drawIntoCanvas { it.drawVertices(op.vertices, BlendMode.Modulate, skinPaint) }
        is Op.Shape -> shape(op.draw)
    }
}

private fun DrawScope.shape(d: Draw) {
    when (d) {
        is Draw.Poly -> {
            val p = Path()
            p.moveTo(d.xs[0], d.ys[0])
            for (i in 1 until d.xs.size) p.lineTo(d.xs[i], d.ys[i])
            p.close()
            drawPath(p, Color(d.argb))
        }
        is Draw.Tri -> {
            // Only where the platform cannot batch vertices: one flat triangle each.
            val p = Path()
            p.moveTo(d.x1, d.y1); p.lineTo(d.x2, d.y2); p.lineTo(d.x3, d.y3); p.close()
            drawPath(p, Color(averageColour(d.c1, d.c2, d.c3)))
        }
        is Draw.Stroke -> drawLine(Color(d.argb), Offset(d.x1, d.y1), Offset(d.x2, d.y2), d.width, StrokeCap.Round)
        is Draw.Rod -> {
            val dx = d.x2 - d.x1
            val dy = d.y2 - d.y1
            val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
            val px = -dy / len
            val py = dx / len
            val r = maxOf(d.r1, d.r2)
            val mx = (d.x1 + d.x2) / 2
            val my = (d.y1 + d.y2) / 2
            val brush = Brush.linearGradient(
                0f to Color(d.lit), 0.45f to Color(d.base), 1f to Color(d.dark),
                start = Offset(mx + d.nx * r, my + d.ny * r),
                end = Offset(mx - d.nx * r - 0.01f, my - d.ny * r)
            )
            val p = Path()
            p.moveTo(d.x1 + px * d.r1, d.y1 + py * d.r1)
            p.lineTo(d.x2 + px * d.r2, d.y2 + py * d.r2)
            p.lineTo(d.x2 - px * d.r2, d.y2 - py * d.r2)
            p.lineTo(d.x1 - px * d.r1, d.y1 - py * d.r1)
            p.close()
            drawPath(p, brush)
            drawCircle(brush, d.r1, Offset(d.x1, d.y1))
            drawCircle(brush, d.r2, Offset(d.x2, d.y2))
        }
        is Draw.Disc -> {
            val brush = Brush.radialGradient(
                0f to Color(d.lit), 0.5f to Color(d.base), 1f to Color(d.dark),
                center = Offset(d.x + d.hx * d.r * 0.4f, d.y + d.hy * d.r * 0.4f),
                radius = (d.r * 1.4f).coerceAtLeast(0.5f)
            )
            drawCircle(brush, d.r, Offset(d.x, d.y))
        }
    }
}

private fun averageColour(a: Int, b: Int, c: Int): Int {
    fun ch(shift: Int) = (((a ushr shift) and 255) + ((b ushr shift) and 255) + ((c ushr shift) and 255)) / 3
    return (0xFF shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
}
