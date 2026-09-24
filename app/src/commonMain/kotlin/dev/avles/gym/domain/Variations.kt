package dev.avles.gym.domain

/**
 * How a set was performed, across every axis the lift offers at once.
 *
 * A bench press offers a grip width, a bench angle and a tempo. They are
 * independent questions — close grip *and* paused is an ordinary thing to do —
 * so a set has to be able to answer all of them. The first version of this stored
 * one string and let every row write to it, which meant choosing a tempo silently
 * erased the grip and blanked that row. That is the bug this file exists to fix.
 *
 * Encoded into the single `variation` column rather than a table of its own, so
 * there is no migration: `Grip width=Close|Tempo=Paused`. The axes are **sorted by
 * name** when encoding, which is the part that matters. Progression and records
 * key on this string, so the same two choices made in either order have to produce
 * the same bytes or a lifter would end up with two histories for one thing.
 *
 * A value written before this existed has no `=` in it. It round-trips unchanged
 * rather than being discarded or guessed at — the app does not know which axis it
 * belonged to, and inventing one would put made-up data in a training log.
 */
object Variations {

    private const val PAIR = '|'
    private const val KV = '='

    /** Where a legacy bare value is kept so it survives a parse/format round trip. */
    private const val LEGACY = ""

    /**
     * Axis name to chosen value. Empty when nothing is specified, which is what
     * every set logged before variations existed is, and a real choice in itself.
     */
    fun parse(encoded: String): Map<String, String> {
        val text = encoded.trim()
        if (text.isEmpty()) return emptyMap()
        // A legacy value is a plain word someone chose from a chip row. Anything
        // carrying the separators is malformed rather than old, and treating it as
        // legacy would hand back an entry with a blank axis name.
        if (KV !in text) return if (PAIR in text) emptyMap() else mapOf(LEGACY to text)
        return text.split(PAIR)
            .mapNotNull { pair ->
                val at = pair.indexOf(KV)
                if (at <= 0) return@mapNotNull null
                val axis = pair.substring(0, at).trim()
                val value = pair.substring(at + 1).trim()
                if (axis.isEmpty() || value.isEmpty()) null else axis to value
            }
            .toMap()
    }

    /**
     * Canonical encoding. Sorted by axis name so that two sets performed the same
     * way compare equal however the chips were tapped.
     */
    fun format(values: Map<String, String>): String {
        val clean = values.filterValues { it.isNotBlank() }
        if (clean.isEmpty()) return ""
        clean[LEGACY]?.let { if (clean.size == 1) return it }
        return clean.entries
            .filter { it.key.isNotEmpty() }
            .sortedBy { it.key }
            .joinToString(PAIR.toString()) { "${it.key}$KV${it.value}" }
    }

    /** What is chosen on one axis, or empty for unspecified. */
    fun valueFor(encoded: String, axis: String): String = parse(encoded)[axis].orEmpty()

    /**
     * The same selection with one axis changed. A blank [value] clears that axis
     * and leaves the others alone — which is the whole point, and what the previous
     * version could not do.
     */
    fun with(encoded: String, axis: String, value: String): String {
        val values = parse(encoded).toMutableMap()
        if (value.isBlank()) values.remove(axis) else values[axis] = value.trim()
        return format(values)
    }

    /**
     * For a set row: "Close · Paused", values only.
     *
     * The axis names are omitted deliberately. Beside a lift you already know what
     * "Close" refers to, and "Grip width: Close · Tempo: Paused" is twice the width
     * for no information on a line that has a weight and a rep count to fit as well.
     */
    fun label(encoded: String): String =
        parse(encoded).entries
            .sortedBy { it.key }
            .joinToString(" · ") { it.value }

    /** Whether anything at all is specified. */
    fun isUnspecified(encoded: String): Boolean = parse(encoded).isEmpty()
}
