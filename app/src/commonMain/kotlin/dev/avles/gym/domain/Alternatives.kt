package dev.avles.gym.domain

import dev.avles.gym.data.Exercise

/**
 * What to do instead when the machine is taken: lifts for the same muscle, the
 * same movement first, on different equipment first. The swap button of the
 * workout apps, worked out from the catalogue's own muscles and patterns.
 */
object Alternatives {

    fun forLift(target: Exercise, catalogue: List<Exercise>, limit: Int = 8): List<Exercise> {
        val guide = ExerciseGuide.of(target)
        val candidates = catalogue
            .filter { it.id != target.id && it.muscleGroup == target.muscleGroup }
            .map { it to ExerciseGuide.of(it) }
        // Same movement on other kit first, then same movement same kit, then the rest
        // of the muscle's lifts. Within a rank, one per piece of equipment before any
        // repeats, so the list offers a dumbbell, a cable and a machine rather than
        // five cable variations.
        val ranked = candidates.sortedWith(
            compareByDescending<Pair<Exercise, ExerciseGuide>> { (_, g) ->
                when {
                    g.pattern == guide.pattern && g.equipment != guide.equipment -> 3
                    g.pattern == guide.pattern -> 2
                    else -> 1
                }
            }.thenBy { it.first.name }
        )
        val seenKit = mutableSetOf<Equipment>()
        val first = ranked.filter { seenKit.add(it.second.equipment) }
        val rest = ranked - first.toSet()
        return (first + rest).map { it.first }.take(limit)
    }
}
