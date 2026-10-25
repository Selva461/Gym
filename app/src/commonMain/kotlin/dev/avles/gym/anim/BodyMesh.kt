package dev.avles.gym.anim

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A body's skin: a smooth triangle mesh grown from [Sculpt]'s shapes, with each point
 * tied to the bones it moves with and coloured skin, hair, top, shorts or shoes.
 *
 * Built once per [Avatar] the first time it is needed (a fraction of a second) and
 * then only bent each frame by [skin], so there is no model file to ship or licence.
 * Any [Avatar] works, preset or custom: the mesh is grown from its data.
 */
class BodyMesh private constructor(val avatar: Avatar) {

    val size: Int
    /** Rest-pose positions and normals, x y z per point. */
    val rest: DoubleArray
    val normals: DoubleArray
    /** Up to three bones per point and how much each one counts. */
    val boneOf: IntArray
    val weight: DoubleArray
    val color: IntArray
    /** The bone that counts most, for clothing and for lighting up a focus. */
    val main: IntArray
    val tris: IntArray

    init {
        val blobs = Sculpt.blobs(avatar)
        val field = Field(blobs)
        val m = SurfaceNets.mesh(field, CELL)
        size = m.points.size / 3
        rest = m.points
        normals = DoubleArray(size * 3)
        boneOf = IntArray(size * 3)
        weight = DoubleArray(size * 3)
        color = IntArray(size)
        main = IntArray(size)
        for (i in 0 until size) {
            val n = field.gradient(rest[i * 3], rest[i * 3 + 1], rest[i * 3 + 2])
            normals[i * 3] = n.x; normals[i * 3 + 1] = n.y; normals[i * 3 + 2] = n.z
        }
        // Wind every triangle so its front faces out of the body.
        val t = m.tris
        for (k in t.indices step 3) {
            val a = t[k]; val b = t[k + 1]; val c = t[k + 2]
            val nx = normals[a * 3] + normals[b * 3] + normals[c * 3]
            val ny = normals[a * 3 + 1] + normals[b * 3 + 1] + normals[c * 3 + 1]
            val nz = normals[a * 3 + 2] + normals[b * 3 + 2] + normals[c * 3 + 2]
            if (faceNormal(rest, a, b, c) dot v(nx, ny, nz) < 0) { t[k + 1] = c; t[k + 2] = b }
        }
        tris = t
        smooth(rest, t, size)
        vertexNormals(rest, t, size, normals)

        val perBone = DoubleArray(Bone.COUNT)
        for (i in 0 until size) {
            val x = rest[i * 3]; val y = rest[i * 3 + 1]; val z = rest[i * 3 + 2]
            perBone.fill(1e9)
            var hairDist = 1e9
            for (b in blobs) {
                val d = b.dist(x, y, z)
                if (d < perBone[b.bone]) perBone[b.bone] = d
                if (b.hair && d < hairDist) hairDist = d
            }
            // Hair is the hair shapes, plus the scalp above a hairline that runs from the
            // forehead down to the nape, so no bald patch shows where the skull peeks through.
            val neckY = Sculpt.rest.neck.y
            val line = neckY + lerp(0.12, 0.245, ((z + 0.05) / 0.09).coerceIn(0.0, 1.0))
            val hair = hairDist < 0.002 || (hairDist < 0.014 && y > line)
            val order = perBone.indices.sortedBy { perBone[it] }.take(3)
            val best = perBone[order[0]]
            val w = order.map { exp(-(perBone[it] - best) / SOFT) }.map { if (it < 0.03) 0.0 else it }
            val sum = w.sum()
            for (k in 0 until 3) { boneOf[i * 3 + k] = order[k]; weight[i * 3 + k] = w[k] / sum }
            main[i] = order[0]
            color[i] = Wardrobe.colour(avatar, v(x, y, z), order[0], hair)
        }
    }

    /** Which points light up for a step's [Focus]: 0 none, 1 full accent. */
    private val focusCache = HashMap<Focus, FloatArray>()

    fun focusMask(focus: Focus): FloatArray? {
        if (focus == Focus.NONE) return null
        return focusCache.getOrPut(focus) {
            val s = Sculpt.rest
            FloatArray(size) { i ->
                val p = v(rest[i * 3], rest[i * 3 + 1], rest[i * 3 + 2])
                val b = main[i]
                fun near(vararg js: Vec3, r: Double) = js.any { (it - p).length < r }
                val lit = when (focus) {
                    Focus.HANDS -> Bone.isForearm(b) && near(s.handL, s.handR, r = 0.085)
                    Focus.ELBOWS -> near(s.elbowL, s.elbowR, r = 0.085)
                    Focus.KNEES -> near(s.kneeL, s.kneeR, r = 0.095)
                    Focus.FEET -> Bone.isFoot(b) || (Bone.isShin(b) && p.y < 0.13)
                    Focus.SHOULDERS -> near(s.shoulderL, s.shoulderR, r = 0.11)
                    Focus.HIPS -> b == Bone.PELVIS || (Bone.isThigh(b) && near(s.hipL, s.hipR, r = 0.13))
                    Focus.BACK -> (b == Bone.CHEST || b == Bone.PELVIS) && p.z < -0.02 && p.y > s.pelvis.y - 0.05
                    Focus.CORE -> (b == Bone.CHEST || b == Bone.PELVIS) && p.z > 0.0 && p.y in (s.pelvis.y - 0.02)..(s.pelvis.y + 0.27)
                    Focus.NONE -> false
                }
                if (lit) 1f else 0f
            }
        }
    }

    /**
     * The skin bent to [s]: positions and normals, x y z per point, written into [pos]
     * and [nrm]. Each point follows its bones: where a bone has moved and turned
     * since the rest pose, the point moves and turns with it, blended by weight.
     */
    fun skin(s: Skeleton, pos: DoubleArray, nrm: DoubleArray) {
        val frames = Frames.of(s)
        val r = frames.restHead; val h = frames.head; val d = frames.turn
        for (i in 0 until size) {
            var px = 0.0; var py = 0.0; var pz = 0.0
            var nx = 0.0; var ny = 0.0; var nz = 0.0
            val rx = rest[i * 3]; val ry = rest[i * 3 + 1]; val rz = rest[i * 3 + 2]
            val ax = normals[i * 3]; val ay = normals[i * 3 + 1]; val az = normals[i * 3 + 2]
            for (k in 0 until 3) {
                val w = weight[i * 3 + k]
                if (w == 0.0) continue
                val b = boneOf[i * 3 + k]
                val t = d[b]
                val ox = rx - r[b].x; val oy = ry - r[b].y; val oz = rz - r[b].z
                px += w * (t.side.x * ox + t.up.x * oy + t.fwd.x * oz + h[b].x)
                py += w * (t.side.y * ox + t.up.y * oy + t.fwd.y * oz + h[b].y)
                pz += w * (t.side.z * ox + t.up.z * oy + t.fwd.z * oz + h[b].z)
                nx += w * (t.side.x * ax + t.up.x * ay + t.fwd.x * az)
                ny += w * (t.side.y * ax + t.up.y * ay + t.fwd.y * az)
                nz += w * (t.side.z * ax + t.up.z * ay + t.fwd.z * az)
            }
            val l = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(1e-9)
            pos[i * 3] = px; pos[i * 3 + 1] = py; pos[i * 3 + 2] = pz
            nrm[i * 3] = nx / l; nrm[i * 3 + 1] = ny / l; nrm[i * 3 + 2] = nz / l
        }
    }

    companion object {
        private fun faceNormal(p: DoubleArray, a: Int, b: Int, c: Int): Vec3 {
            val e1 = v(p[b * 3] - p[a * 3], p[b * 3 + 1] - p[a * 3 + 1], p[b * 3 + 2] - p[a * 3 + 2])
            val e2 = v(p[c * 3] - p[a * 3], p[c * 3 + 1] - p[a * 3 + 1], p[c * 3 + 2] - p[a * 3 + 2])
            return e1 cross e2
        }

        /**
         * Taubin smoothing: each point pulled towards its neighbours' average, then pushed
         * back a little further, so the grid's ripples go and the body does not shrink.
         */
        private fun smooth(p: DoubleArray, t: IntArray, n: Int) {
            val sum = DoubleArray(n * 3); val count = IntArray(n)
            fun pass(f: Double) {
                sum.fill(0.0); count.fill(0)
                for (k in t.indices step 3) for (e in 0 until 3) {
                    val a = t[k + e]; val b = t[k + (e + 1) % 3]
                    for (j in 0 until 3) { sum[a * 3 + j] += p[b * 3 + j]; sum[b * 3 + j] += p[a * 3 + j] }
                    count[a]++; count[b]++
                }
                for (i in 0 until n) if (count[i] > 0) for (j in 0 until 3) {
                    p[i * 3 + j] += f * (sum[i * 3 + j] / count[i] - p[i * 3 + j])
                }
            }
            repeat(6) { pass(0.5); pass(-0.53) }
        }

        private fun vertexNormals(p: DoubleArray, t: IntArray, n: Int, out: DoubleArray) {
            out.fill(0.0)
            for (k in t.indices step 3) {
                val f = faceNormal(p, t[k], t[k + 1], t[k + 2])
                for (e in 0 until 3) { val i = t[k + e]; out[i * 3] += f.x; out[i * 3 + 1] += f.y; out[i * 3 + 2] += f.z }
            }
            for (i in 0 until n) {
                val l = sqrt(out[i * 3] * out[i * 3] + out[i * 3 + 1] * out[i * 3 + 1] + out[i * 3 + 2] * out[i * 3 + 2]).coerceAtLeast(1e-12)
                for (j in 0 until 3) out[i * 3 + j] /= l
            }
        }

        /** Grid spacing the skin is grown on: fine enough for fingers' worth of detail. */
        const val CELL = 0.0155
        /** How softly a point is shared between neighbouring bones, metres. */
        const val SOFT = 0.011

        /** How many different avatars' meshes are kept; the least recently built go first. */
        private const val CACHE_SIZE = 6

        /**
         * Replaced whole, never changed in place, so a reader on another thread always
         * sees a complete map. Two threads building the same avatar at once is harmless:
         * both get a correct mesh and one of them is kept.
         */
        @kotlin.concurrent.Volatile
        private var cache: Map<Avatar, BodyMesh> = emptyMap()

        /** Already built and ready to draw this frame, or null. */
        fun ready(avatar: Avatar): BodyMesh? = cache[avatar]

        /** The mesh for [avatar], built now if needed. Call off the main thread the first time. */
        fun of(avatar: Avatar): BodyMesh {
            cache[avatar]?.let { return it }
            val built = BodyMesh(avatar)
            val next = LinkedHashMap(cache)
            next[avatar] = built
            while (next.size > CACHE_SIZE) next.remove(next.keys.first())
            cache = next
            return built
        }
    }
}

/** The sculpt as one smooth distance field. */
internal class Field(private val blobs: List<Blob>) {

    val min = v(blobs.minOf { it.cx - it.reach }, blobs.minOf { it.cy - it.reach }, blobs.minOf { it.cz - it.reach })
    val max = v(blobs.maxOf { it.cx + it.reach }, blobs.maxOf { it.cy + it.reach }, blobs.maxOf { it.cz + it.reach })

    fun at(x: Double, y: Double, z: Double): Double {
        var d = 1.0
        for (b in blobs) {
            val dx = x - b.cx; val dy = y - b.cy; val dz = z - b.cz
            // A shape whose bounding sphere is further than the blend cannot change the answer.
            if (sqrt(dx * dx + dy * dy + dz * dz) - b.reach > d + Sculpt.BLEND) continue
            d = Sculpt.smin(d, b.dist(x, y, z), Sculpt.BLEND)
        }
        return d
    }

    fun gradient(x: Double, y: Double, z: Double): Vec3 {
        val e = 0.002
        return v(
            at(x + e, y, z) - at(x - e, y, z),
            at(x, y + e, z) - at(x, y - e, z),
            at(x, y, z + e) - at(x, y, z - e)
        ).normalized()
    }
}

/**
 * Naive surface nets: one point per grid cell the surface passes through, one quad
 * per grid edge it crosses. Smoother than marching cubes for the same grid and much
 * shorter to write.
 */
internal object SurfaceNets {

    class Out(val points: DoubleArray, val tris: IntArray)

    fun mesh(f: Field, cell: Double): Out {
        val o = f.min - v(cell * 2, cell * 2, cell * 2)
        val nx = ((f.max.x - o.x) / cell).toInt() + 3
        val ny = ((f.max.y - o.y) / cell).toInt() + 3
        val nz = ((f.max.z - o.z) / cell).toInt() + 3
        fun idx(i: Int, j: Int, k: Int) = (k * ny + j) * nx + i
        val value = FloatArray(nx * ny * nz)
        for (k in 0 until nz) for (j in 0 until ny) for (i in 0 until nx) {
            value[idx(i, j, k)] = f.at(o.x + i * cell, o.y + j * cell, o.z + k * cell).toFloat()
        }
        val vertexOf = IntArray(nx * ny * nz) { -1 }
        val pts = ArrayList<Double>()
        val corner = FloatArray(8)
        for (k in 0 until nz - 1) for (j in 0 until ny - 1) for (i in 0 until nx - 1) {
            var inside = 0
            for (c in 0 until 8) {
                corner[c] = value[idx(i + (c and 1), j + ((c shr 1) and 1), k + ((c shr 2) and 1))]
                if (corner[c] < 0) inside++
            }
            if (inside == 0 || inside == 8) continue
            var sx = 0.0; var sy = 0.0; var sz = 0.0; var n = 0
            for (a in 0 until 8) for (bit in 0 until 3) {
                val b = a or (1 shl bit)
                if (b == a) continue
                val va = corner[a]; val vb = corner[b]
                if ((va < 0) == (vb < 0)) continue
                val t = va / (va - vb)
                val ax = (a and 1).toDouble(); val ay = ((a shr 1) and 1).toDouble(); val az = ((a shr 2) and 1).toDouble()
                val bx = (b and 1).toDouble(); val by = ((b shr 1) and 1).toDouble(); val bz = ((b shr 2) and 1).toDouble()
                sx += ax + (bx - ax) * t; sy += ay + (by - ay) * t; sz += az + (bz - az) * t; n++
            }
            vertexOf[idx(i, j, k)] = pts.size / 3
            pts += o.x + (i + sx / n) * cell; pts += o.y + (j + sy / n) * cell; pts += o.z + (k + sz / n) * cell
        }
        val tris = ArrayList<Int>()
        fun quad(a: Int, b: Int, c: Int, d: Int) {
            if (a < 0 || b < 0 || c < 0 || d < 0) return
            tris += a; tris += b; tris += c
            tris += a; tris += c; tris += d
        }
        for (k in 1 until nz - 1) for (j in 1 until ny - 1) for (i in 1 until nx - 1) {
            val here = value[idx(i, j, k)] < 0
            if (here != (value[idx(i + 1, j, k)] < 0)) {
                quad(vertexOf[idx(i, j - 1, k - 1)], vertexOf[idx(i, j, k - 1)], vertexOf[idx(i, j, k)], vertexOf[idx(i, j - 1, k)])
            }
            if (here != (value[idx(i, j + 1, k)] < 0)) {
                quad(vertexOf[idx(i - 1, j, k - 1)], vertexOf[idx(i, j, k - 1)], vertexOf[idx(i, j, k)], vertexOf[idx(i - 1, j, k)])
            }
            if (here != (value[idx(i, j, k + 1)] < 0)) {
                quad(vertexOf[idx(i - 1, j - 1, k)], vertexOf[idx(i, j - 1, k)], vertexOf[idx(i, j, k)], vertexOf[idx(i - 1, j, k)])
            }
        }
        return Out(pts.toDoubleArray(), tris.toIntArray())
    }
}

/**
 * Where each bone is and how it has turned since the rest pose.
 *
 * The torso and head carry their own frames. A limb segment's turn is the turn of the
 * segment it hangs from, followed by the smallest further turn that lines it up with
 * where it now points, so an arm never spins about its own length for no reason.
 */
internal class Frames(val restHead: Array<Vec3>, val head: Array<Vec3>, val turn: Array<Basis>) {
    companion object {
        fun of(s: Skeleton): Frames {
            val r = Sculpt.rest
            val rh = heads(r); val h = heads(s)
            val t = Array(Bone.COUNT) { Basis.IDENTITY }
            t[Bone.PELVIS] = s.hips; t[Bone.CHEST] = s.torso; t[Bone.HEAD] = s.headFrame
            fun chain(bone: Int, parent: Int, restA: Vec3, restB: Vec3, a: Vec3, b: Vec3) {
                val from = t[parent].apply((restB - restA).normalized())
                t[bone] = arc(from, (b - a).normalized()) * t[parent]
            }
            chain(Bone.UPPER_ARM_L, Bone.CHEST, r.shoulderL, r.elbowL, s.shoulderL, s.elbowL)
            chain(Bone.FOREARM_L, Bone.UPPER_ARM_L, r.elbowL, r.handL, s.elbowL, s.handL)
            chain(Bone.UPPER_ARM_R, Bone.CHEST, r.shoulderR, r.elbowR, s.shoulderR, s.elbowR)
            chain(Bone.FOREARM_R, Bone.UPPER_ARM_R, r.elbowR, r.handR, s.elbowR, s.handR)
            chain(Bone.THIGH_L, Bone.PELVIS, r.hipL, r.kneeL, s.hipL, s.kneeL)
            chain(Bone.SHIN_L, Bone.THIGH_L, r.kneeL, r.ankleL, s.kneeL, s.ankleL)
            chain(Bone.FOOT_L, Bone.SHIN_L, r.heelL, r.toeL, s.heelL, s.toeL)
            chain(Bone.THIGH_R, Bone.PELVIS, r.hipR, r.kneeR, s.hipR, s.kneeR)
            chain(Bone.SHIN_R, Bone.THIGH_R, r.kneeR, r.ankleR, s.kneeR, s.ankleR)
            chain(Bone.FOOT_R, Bone.SHIN_R, r.heelR, r.toeR, s.heelR, s.toeR)
            return Frames(rh, h, t)
        }

        private fun heads(s: Skeleton) = arrayOf(
            s.pelvis, s.chest, s.neck,
            s.shoulderL, s.elbowL, s.shoulderR, s.elbowR,
            s.hipL, s.kneeL, s.ankleL, s.hipR, s.kneeR, s.ankleR
        )

        /** The smallest turn taking direction [a] to direction [b]. */
        fun arc(a: Vec3, b: Vec3): Basis {
            val axis = a cross b
            val c = (a dot b).coerceIn(-1.0, 1.0)
            if (axis.length < 1e-9) {
                if (c > 0) return Basis.IDENTITY
                val perp = (if (abs(a.x) < 0.9) Vec3.X else Vec3.Y).let { (it - a * (it dot a)).normalized() }
                return rotation(perp, kotlin.math.PI)
            }
            return rotation(axis.normalized(), acos(c))
        }

        private fun rotation(k: Vec3, angle: Double): Basis {
            val c = cos(angle); val s = sin(angle)
            fun rot(p: Vec3) = p * c + (k cross p) * s + k * ((k dot p) * (1 - c))
            return Basis(rot(Vec3.X), rot(Vec3.Y), rot(Vec3.Z))
        }
    }
}

/** One turn after another: this frame applied to each axis of [o]. */
operator fun Basis.times(o: Basis) = Basis(apply(o.side), apply(o.up), apply(o.fwd))

/** What each part of the body is wearing, from the avatar's [Outfit] and [Hair]. */
object Wardrobe {

    fun colour(avatar: Avatar, p: Vec3, bone: Int, hair: Boolean): Int {
        val s = Sculpt.rest
        val o = avatar.outfit
        val waist = s.pelvis.y + 0.06
        if (Bone.isFoot(bone) || p.y < 0.115) return if (p.y < 0.022) o.soleColour else o.shoeColour
        if (hair && avatar.hair.style != HairStyle.NONE) return avatar.hair.colour
        val top = when (o.top) {
            TopStyle.TEE -> bone == Bone.CHEST || (Bone.isUpperArm(bone) && (p - nearestShoulder(p)).length < 0.15)
            TopStyle.TANK -> bone == Bone.CHEST &&
                (p.y < s.pelvis.y + (if (p.z > 0) 0.41 else 0.45) || abs(p.x) in 0.06..0.1)
        }
        if (top || (bone == Bone.PELVIS && p.y > waist)) return o.topColour
        val bottom = when (o.bottom) {
            BottomStyle.SHORTS -> bone == Bone.PELVIS || (Bone.isThigh(bone) && p.y > s.kneeL.y + 0.17)
            BottomStyle.LEGGINGS -> bone == Bone.PELVIS || Bone.isThigh(bone) || Bone.isShin(bone)
        }
        return if (bottom) o.bottomColour else avatar.skin
    }

    private fun nearestShoulder(p: Vec3): Vec3 {
        val s = Sculpt.rest
        return if ((p - s.shoulderL).length < (p - s.shoulderR).length) s.shoulderL else s.shoulderR
    }
}
