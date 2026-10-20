package dev.avles.gym.domain

import kotlin.math.roundToLong

/**
 * What to put on each side of the bar.
 *
 * Every serious lifting app has this because the arithmetic is exactly the kind you
 * get wrong between sets: 87.5 on a 20 kg bar is 33.75 a side, which is a 25, a 5,
 * a 2.5 and a 1.25, and nobody wants to work that out with their heart at 150.
 *
 * Worked in grams as whole numbers, because 1.25 kg plates in floating point
 * accumulate exactly the rounding error that makes a calculator say "0.0000001 kg
 * left over". Greedy is correct here: each standard plate is at least twice the
 * next, so the largest-first choice is also the fewest plates.
 */
object Plates {

    /** The plates a commercial gym in India stocks. Largest first. */
    val STANDARD_KG = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)

    const val OLYMPIC_BAR_KG = 20.0

    /** Bars people actually use: Olympic, women's Olympic, and the fixed EZ/straight bars. */
    val BARS_KG = listOf(20.0, 15.0, 10.0, 7.5)

    /**
     * [perSide] is largest first. [loadedKg] is what the bar will really weigh, which
     * differs from what was asked when the plates cannot make it exactly; the
     * [shortKg] is that difference, never negative.
     */
    data class Loading(
        val perSide: List<Double>,
        val loadedKg: Double,
        val shortKg: Double,
        val belowBar: Boolean = false
    ) {
        val isExact: Boolean get() = shortKg == 0.0 && !belowBar
    }

    fun load(totalKg: Double, barKg: Double = OLYMPIC_BAR_KG, plates: List<Double> = STANDARD_KG): Loading {
        if (totalKg < barKg) return Loading(emptyList(), barKg, 0.0, belowBar = true)
        val sizes = plates.filter { it > 0.0 }.map { grams(it) }.sortedDescending()
        var remainingPerSide = (grams(totalKg) - grams(barKg)) / 2
        val chosen = mutableListOf<Long>()
        for (size in sizes) {
            while (remainingPerSide >= size) {
                chosen += size
                remainingPerSide -= size
            }
        }
        val loaded = grams(barKg) + chosen.sum() * 2
        return Loading(
            perSide = chosen.map { kg(it) },
            loadedKg = kg(loaded),
            shortKg = kg(grams(totalKg) - loaded)
        )
    }

    /** "25 + 2 × 10 + 1.25", or "just the bar". */
    fun describe(perSide: List<Double>): String {
        if (perSide.isEmpty()) return "just the bar"
        val counts = LinkedHashMap<Double, Int>()
        for (p in perSide) counts[p] = (counts[p] ?: 0) + 1
        return counts.entries.joinToString(" + ") { (plate, n) ->
            if (n == 1) label(plate) else "$n × ${label(plate)}"
        }
    }

    /** 1.25 stays 1.25. The app's usual one-decimal weight format would print it as 1.3. */
    fun label(kg: Double): String =
        Numbers.fixed(kg, 2).trimEnd('0').trimEnd('.')

    private fun grams(kg: Double): Long = (kg * 1000).roundToLong()
    private fun kg(grams: Long): Double = grams / 1000.0
}
