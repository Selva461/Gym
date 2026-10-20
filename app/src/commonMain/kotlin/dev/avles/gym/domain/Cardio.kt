package dev.avles.gym.domain

import kotlin.math.roundToInt

/**
 * The cardio a lifting app lets you log beside the weights: what, how long, how far.
 * Each activity carries a typical MET from the Compendium of Physical Activities, so
 * the calories line up with how [Burn] estimates a lifting session.
 */
object Cardio {

    data class Activity(val name: String, val met: Double, val hasDistance: Boolean)

    val ACTIVITIES = listOf(
        Activity("Walking", 3.5, true),
        Activity("Incline walk", 6.0, true),
        Activity("Running", 9.8, true),
        Activity("Cycling", 7.5, true),
        Activity("Rowing", 7.0, true),
        Activity("Elliptical", 5.0, false),
        Activity("Stair climber", 9.0, false),
        Activity("Skipping", 11.0, false),
        Activity("Swimming", 7.0, true),
        Activity("Badminton", 5.5, false),
        Activity("Cricket", 4.8, false),
        Activity("Football", 7.0, false)
    )

    fun activity(name: String): Activity = ACTIVITIES.firstOrNull { it.name == name } ?: Activity(name, 5.0, false)

    fun kcal(activity: String, minutes: Int, bodyKg: Double): Int = Burn.kcal(minutes, bodyKg, activity(activity).met)

    /** "6:12 /km", or null without a distance. */
    fun pace(minutes: Int, distanceKm: Double): String? {
        if (distanceKm <= 0 || minutes <= 0) return null
        val secondsPerKm = (minutes * 60 / distanceKm).roundToInt()
        return "${secondsPerKm / 60}:${(secondsPerKm % 60).toString().padStart(2, '0')} /km"
    }
}
