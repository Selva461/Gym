package dev.avles.gym

import dev.avles.gym.domain.Variations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The reported bug, pinned: choosing a tempo used to erase the grip.
 *
 * Every axis a lift offers is an independent question, and close grip *and*
 * paused is an ordinary thing to do. The first version stored one string that
 * every row wrote to, so the second choice overwrote the first and blanked the
 * row it came from.
 */
class VariationsTest {

    @Test
    fun `choosing on one axis leaves the others alone`() {
        var v = ""
        v = Variations.with(v, "Grip width", "Close")
        v = Variations.with(v, "Tempo", "Paused")
        v = Variations.with(v, "Bench angle", "Incline 30°")

        assertEquals("Close", Variations.valueFor(v, "Grip width"))
        assertEquals("Paused", Variations.valueFor(v, "Tempo"))
        assertEquals("Incline 30°", Variations.valueFor(v, "Bench angle"))
    }

    /**
     * The property progression depends on. Records and next-set targets key on this
     * string, so two sets performed identically must encode identically however the
     * chips were tapped — otherwise one lift quietly grows two histories.
     */
    @Test
    fun `the encoding does not depend on the order things were chosen`() {
        val a = Variations.with(Variations.with("", "Tempo", "Paused"), "Grip width", "Close")
        val b = Variations.with(Variations.with("", "Grip width", "Close"), "Tempo", "Paused")
        assertEquals(a, b)
        assertEquals("Grip width=Close|Tempo=Paused", a)
    }

    @Test
    fun `clearing one axis keeps the rest`() {
        val both = Variations.with(Variations.with("", "Grip width", "Close"), "Tempo", "Paused")
        val cleared = Variations.with(both, "Grip width", "")
        assertEquals("", Variations.valueFor(cleared, "Grip width"))
        assertEquals("Paused", Variations.valueFor(cleared, "Tempo"))
        assertEquals("Tempo=Paused", cleared)
    }

    @Test
    fun `clearing the last axis returns to unspecified`() {
        val one = Variations.with("", "Tempo", "Paused")
        assertTrue(Variations.isUnspecified(Variations.with(one, "Tempo", "")))
        assertTrue(Variations.isUnspecified(""))
        assertFalse(Variations.isUnspecified(one))
    }

    /**
     * A value written by the broken version has no `=` in it. It survives unchanged
     * rather than being dropped or attributed to a guessed axis — the app does not
     * know which row it came from, and inventing one would put made-up data into a
     * training history.
     */
    @Test
    fun `a value from before this existed round-trips untouched`() {
        assertEquals("Close", Variations.format(Variations.parse("Close")))
        assertEquals("Close", Variations.label("Close"))
        assertFalse(Variations.isUnspecified("Close"))
    }

    @Test
    fun `the label reads as values, without the axis names`() {
        val v = Variations.with(Variations.with("", "Grip width", "Close"), "Tempo", "Paused")
        // Sorted by axis, so: Grip width then Tempo.
        assertEquals("Close · Paused", Variations.label(v))
        assertEquals("", Variations.label(""))
    }

    /** Values carry degree signs, fractions and dashes. None of them may break the encoding. */
    @Test
    fun `awkward values survive the encoding`() {
        listOf("Incline 30°", "1½ reps", "Single — left", "Slow eccentric").forEach { value ->
            val v = Variations.with("", "Axis", value)
            assertEquals(value, Variations.valueFor(v, "Axis"))
        }
    }

    @Test
    fun `malformed input does not throw or invent axes`() {
        listOf("=", "|", "=x", "a=", "||", "a=b|", "|a=b").forEach { junk ->
            val parsed = Variations.parse(junk)
            parsed.forEach { (axis, value) ->
                assertTrue("blank value from \"$junk\"", value.isNotBlank())
                // The one legal blank key is the legacy slot, and it only holds a
                // plain value — never something carrying the separators.
                if (axis.isBlank()) {
                    assertFalse("\"$junk\" was taken for a legacy value", '|' in value || '=' in value)
                }
            }
        }
        assertEquals(mapOf("a" to "b"), Variations.parse("a=b|"))
    }
}
