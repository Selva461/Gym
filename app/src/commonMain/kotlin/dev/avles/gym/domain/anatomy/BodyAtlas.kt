package dev.avles.gym.domain.anatomy

import dev.avles.gym.domain.Muscle

/**
 * The muscle map's anatomy as plain geometry: no Compose, no platform, so it is unit
 * tested like the rest of `domain/` and the UI only has to fill the shapes it is given.
 *
 * Every shape is authored once, on the viewer's left half of a 200 × 420 box (the
 * midline is x = 100, the top of the head y = 0, the soles y ≈ 416), and mirrored. So
 * the figure is symmetric by construction, not by care: there is no right-hand copy
 * to drift out of line. The front and back views also share one silhouette
 * ([AtlasBase]), so the two figures always stand in the same place at the same size.
 *
 * A [BodyBuild] then warps the authored (male) proportions: the female build pulls
 * the shoulders and waist in and lets the hips out. Muscles and silhouette go through
 * the same warp, which is what keeps them lined up for every build, including any
 * custom one added later.
 */
object BodyAtlas {

    /** Authoring box. The UI scales it to fit, keeping this aspect ratio. */
    const val WIDTH = 200f
    const val HEIGHT = 420f
    const val MIDLINE = WIDTH / 2f

    private val cache = HashMap<Pair<BodyView, BodyBuild>, BodyFigure>()

    /** One view of one build, mirrored and warped. Built once, then cached. */
    fun figure(view: BodyView, build: BodyBuild): BodyFigure =
        cache.getOrPut(view to build) {
            BodyFigure(
                silhouette = AtlasBase.regions.flatMap { it.outlines(build) },
                muscles = regions(view).flatMap { it.outlines(build) }
            )
        }

    /** The authored regions of a view, in draw order (later ones on top). */
    internal fun regions(view: BodyView): List<Region> = when (view) {
        BodyView.FRONT -> AtlasFront.regions
        BodyView.BACK -> AtlasBack.regions
    }

    /** Which views show [muscle] at all. Empty would mean the map cannot show it. */
    fun viewsOf(muscle: Muscle): Set<BodyView> =
        BodyView.entries.filter { v -> regions(v).any { it.muscle == muscle } }.toSet()
}

enum class BodyView(val label: String) { FRONT("Front"), BACK("Back") }

/** Which part of the body a shape belongs to, which decides how a build warps it. */
enum class BodyPart { HEAD, TRUNK, ARM, LEG }

/**
 * Proportions relative to the authored figure. [profile] is a list of (y, width
 * factor) pairs for the trunk and legs, interpolated between; arms and head scale
 * uniformly about the midline so a limb is never bent by the profile.
 *
 * New builds are new values: nothing else needs to change for a custom avatar to
 * get a matching map. Factors are clamped so a bad value cannot fold the figure.
 */
data class BodyBuild(
    val id: String,
    val profile: List<Pair<Float, Float>>,
    val arms: Float = 1f,
    val head: Float = 1f
) {
    /** Width factor at height [y] for the trunk and legs. */
    fun widthAt(y: Float): Float {
        val p = profile
        if (p.isEmpty()) return 1f
        if (y <= p.first().first) return safe(p.first().second)
        for (i in 1 until p.size) {
            val (y1, k1) = p[i]
            if (y <= y1) {
                val (y0, k0) = p[i - 1]
                val t = if (y1 > y0) (y - y0) / (y1 - y0) else 1f
                return safe(k0 + (k1 - k0) * t)
            }
        }
        return safe(p.last().second)
    }

    internal fun factor(part: BodyPart, y: Float): Float = when (part) {
        BodyPart.HEAD -> safe(head)
        BodyPart.ARM -> safe(arms)
        BodyPart.TRUNK, BodyPart.LEG -> widthAt(y)
    }

    private fun safe(k: Float) = if (k.isNaN()) 1f else k.coerceIn(0.7f, 1.3f)

    companion object {
        /** The figure as authored. */
        val MALE = BodyBuild("male", profile = emptyList())

        /** Narrower shoulders and waist, wider hips and thighs. */
        val FEMALE = BodyBuild(
            id = "female",
            profile = listOf(
                60f to 0.92f, 100f to 0.9f, 130f to 0.9f, 160f to 0.86f, 182f to 0.85f,
                200f to 0.98f, 216f to 1.07f, 250f to 1.06f, 290f to 1.02f, 310f to 0.98f,
                350f to 0.95f, 420f to 0.94f
            ),
            arms = 0.9f,
            head = 0.96f
        )

        val PRESETS = listOf(MALE, FEMALE)

        fun byId(id: String?): BodyBuild = PRESETS.firstOrNull { it.id == id } ?: MALE
    }
}

/**
 * One closed, smooth outline ready to fill. [path] is a start point followed by
 * cubic segments: x0, y0, then (c1x, c1y, c2x, c2y, x, y) per segment, in the
 * 200 × 420 box. [muscle] is null for silhouette and for anatomical detail the
 * exercise data has no name for (tibialis, the rotator cuff), drawn but never lit.
 */
class Outline(val muscle: Muscle?, val path: FloatArray)

class BodyFigure(val silhouette: List<Outline>, val muscles: List<Outline>)

/**
 * An authored shape. [points] are x, y pairs on the viewer's left half.
 *
 * [spansMidline] shapes run from the midline round the left side and back to it
 * (head, abs, the torso) and are joined to their mirror into one outline; every
 * other shape is drawn twice, once on each side.
 */
internal class Region(
    val muscle: Muscle?,
    val part: BodyPart,
    val points: IntArray,
    val spansMidline: Boolean
) {
    init {
        require(points.size >= 6 && points.size % 2 == 0) { "a region needs at least three points" }
    }

    fun outlines(build: BodyBuild): List<Outline> {
        val xs = FloatArray(points.size / 2) { points[it * 2].toFloat() }
        val ys = FloatArray(points.size / 2) { points[it * 2 + 1].toFloat() }
        val m = BodyAtlas.MIDLINE
        if (spansMidline) {
            // Left half, then the mirror walked back up, dropping points on the line so
            // the join has no doubled vertex (which would make a cusp).
            val lx = ArrayList<Float>(); val ly = ArrayList<Float>()
            for (i in xs.indices) { lx += xs[i]; ly += ys[i] }
            for (i in xs.indices.reversed()) {
                if (xs[i] >= m) continue
                lx += 2 * m - xs[i]; ly += ys[i]
            }
            return listOf(Outline(muscle, smooth(warp(lx, ly, build), ly)))
        }
        val left = warp(xs.toList(), ys.toList(), build)
        val right = FloatArray(left.size) { 2 * m - left[it] }
        return listOf(
            Outline(muscle, smooth(left, ys.toList())),
            Outline(muscle, smooth(right.reversedArray(), ys.toList().reversed()))
        )
    }

    private fun warp(xs: List<Float>, ys: List<Float>, build: BodyBuild): FloatArray {
        val m = BodyAtlas.MIDLINE
        return FloatArray(xs.size) { i -> m + (xs[i] - m) * build.factor(part, ys[i]) }
    }
}

/**
 * A closed Catmull-Rom curve through the points, as cubic Bézier segments. It passes
 * through every authored point, so what is typed is where the edge goes.
 */
internal fun smooth(xs: FloatArray, ys: List<Float>): FloatArray {
    val n = xs.size
    val out = FloatArray(2 + n * 6)
    out[0] = xs[0]; out[1] = ys[0]
    for (i in 0 until n) {
        val p0 = (i - 1 + n) % n; val p1 = i; val p2 = (i + 1) % n; val p3 = (i + 2) % n
        val o = 2 + i * 6
        out[o] = xs[p1] + (xs[p2] - xs[p0]) / 6f
        out[o + 1] = ys[p1] + (ys[p2] - ys[p0]) / 6f
        out[o + 2] = xs[p2] - (xs[p3] - xs[p1]) / 6f
        out[o + 3] = ys[p2] - (ys[p3] - ys[p1]) / 6f
        out[o + 4] = xs[p2]
        out[o + 5] = ys[p2]
    }
    return out
}

// Authoring helpers, so the atlas files read as tables of points.

/** A muscle drawn on both sides. */
internal fun pair(muscle: Muscle, part: BodyPart, vararg p: Int) = Region(muscle, part, p, false)

/** A muscle that crosses the midline, authored as its left half. */
internal fun span(muscle: Muscle, part: BodyPart, vararg p: Int) = Region(muscle, part, p, true)

/** Anatomy with no exercise-data name: drawn for shape, never highlighted. */
internal fun detail(part: BodyPart, vararg p: Int) = Region(null, part, p, false)

/** Silhouette pieces. */
internal fun base(part: BodyPart, vararg p: Int) = Region(null, part, p, false)
internal fun baseSpan(part: BodyPart, vararg p: Int) = Region(null, part, p, true)
