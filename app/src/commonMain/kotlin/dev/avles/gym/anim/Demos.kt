package dev.avles.gym.anim

import dev.avles.gym.data.Exercise
import dev.avles.gym.domain.ExerciseGuide

/**
 * A lift from the catalogue (seeded or made on the phone) to its demonstration.
 *
 * The motion is read from the same pattern and equipment as the written guide, so
 * the figure and the text can never describe two different lifts. A name the
 * movement library has no special case for still gets its pattern's standard
 * movement; and if anything in building it fails, the answer is null and the
 * screen shows the written guide alone rather than crashing.
 */
object Demos {

    fun spec(exercise: Exercise, guide: ExerciseGuide = ExerciseGuide.of(exercise)): Spec =
        Spec(exercise.name, guide.pattern.name, guide.equipment.name, exercise.muscleGroup)

    fun of(exercise: Exercise, guide: ExerciseGuide = ExerciseGuide.of(exercise)): Motion? =
        runCatching { Moves.of(spec(exercise, guide)) }.getOrNull()?.takeIf { valid(it) }

    /** Every step names only poses the motion has, and there is a rep to play. */
    fun valid(m: Motion): Boolean =
        m.steps.isNotEmpty() && m.steps.all { s -> s.poses.isNotEmpty() && s.poses.all { it in m.poses } }
}
