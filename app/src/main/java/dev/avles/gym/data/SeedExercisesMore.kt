package dev.avles.gym.data

import dev.avles.gym.data.SeedExercises.Groups

/**
 * The second half of the exercise catalogue.
 *
 * Split from [SeedExercises] for the same reason the foods were split: that file
 * reached the 800-line ceiling `ArchitectureTest` enforces, and a catalogue grows
 * forever while a file should not.
 *
 * Weighted towards what the first pass under-served. Traps, forearms, calves and
 * glutes had a dozen lifts each against fifty for chest, which is roughly the
 * ratio of how much attention those muscles get rather than how many ways there
 * are to train them — and the coach's weekly targets make those the exact gaps it
 * will ask you to fill.
 *
 * Same convention as everywhere: **equipment lives in the name**, so the picker's
 * search box doubles as an equipment filter and `ExerciseGuide` can read the
 * equipment back out without a column to store it in.
 */
internal object SeedExercisesMore {

    private fun ex(name: String, group: String, low: Int, high: Int, inc: Double, bw: Boolean = false) =
        Exercise(
            name = name, muscleGroup = group, isBodyweight = bw,
            repLow = low, repHigh = high, incrementKg = inc
        )

    fun all(): List<Exercise> =
        traps() + forearms() + calves() + glutes() + hamstrings() +
            shoulders() + back() + chest() + arms() + core() + fullBody()

    // ---- traps -------------------------------------------------------------

    private fun traps() = listOf(
        ex("Single-arm cable shrug", Groups.TRAPS, 12, 20, 1.0),
        ex("Cable shrug behind the back", Groups.TRAPS, 10, 15, 2.5),
        ex("Cable high pull to neck", Groups.TRAPS, 10, 15, 2.5),
        ex("Dumbbell shrug, leaning forward", Groups.TRAPS, 10, 15, 2.0),
        ex("Overhead barbell shrug", Groups.TRAPS, 8, 12, 2.5),
        ex("Trap-bar shrug", Groups.TRAPS, 8, 15, 5.0),
        ex("Kettlebell high pull", Groups.TRAPS, 8, 12, 4.0),
        ex("Prone incline dumbbell shrug", Groups.TRAPS, 12, 20, 1.0),
        ex("Band pull-apart", Groups.TRAPS, 15, 25, 1.0),
        ex("Farmer's walk shrug", Groups.TRAPS, 8, 15, 5.0)
    )

    // ---- forearms ----------------------------------------------------------

    private fun forearms() = listOf(
        ex("Cable reverse wrist curl", Groups.FOREARMS, 12, 20, 1.0),
        ex("Dumbbell reverse wrist curl", Groups.FOREARMS, 12, 20, 1.0),
        ex("Machine forearm curl", Groups.FOREARMS, 12, 20, 2.5),
        ex("Barbell reverse curl", Groups.FOREARMS, 10, 15, 1.0),
        ex("EZ-bar reverse curl", Groups.FOREARMS, 10, 15, 1.0),
        ex("Cable reverse curl", Groups.FOREARMS, 12, 20, 1.0),
        ex("Hammer curl over bench", Groups.FOREARMS, 10, 15, 1.0),
        ex("Radial deviation with dumbbell", Groups.FOREARMS, 12, 20, 1.0),
        ex("Pronation and supination with dumbbell", Groups.FOREARMS, 12, 20, 1.0),
        ex("Fat-grip dumbbell hold", Groups.FOREARMS, 1, 1, 2.5),
        ex("Rice bucket dig", Groups.FOREARMS, 1, 1, 1.0, bw = true),
        ex("Barbell finger roll", Groups.FOREARMS, 15, 25, 2.5)
    )

    // ---- calves ------------------------------------------------------------

    private fun calves() = listOf(
        ex("Machine seated calf raise", Groups.CALVES, 12, 20, 2.5),
        ex("Single-leg dumbbell calf raise", Groups.CALVES, 12, 20, 2.0),
        ex("Machine bent-knee calf raise", Groups.CALVES, 12, 20, 2.5),
        ex("Jump rope calf bounce", Groups.CALVES, 20, 40, 1.0, bw = true),
        ex("Farmer's walk on toes", Groups.CALVES, 8, 15, 5.0),
        ex("Tibialis raise with plate", Groups.CALVES, 15, 25, 1.0),
        ex("Cable tibialis raise", Groups.CALVES, 15, 25, 1.0)
    )

    // ---- glutes ------------------------------------------------------------

    private fun glutes() = listOf(
        ex("Dumbbell hip thrust", Groups.GLUTES, 10, 15, 2.5),
        ex("Banded glute bridge", Groups.GLUTES, 15, 25, 1.0, bw = true),
        ex("Single-leg glute bridge", Groups.GLUTES, 12, 20, 1.0, bw = true),
        ex("Cable hip abduction", Groups.GLUTES, 12, 20, 1.0),
        ex("Cable standing glute squeeze", Groups.GLUTES, 12, 20, 1.0),
        ex("Cable step-back lunge", Groups.GLUTES, 10, 15, 2.5),
        ex("Single-arm cable Romanian deadlift", Groups.GLUTES, 10, 15, 2.5),
        ex("Standing machine hip abduction", Groups.GLUTES, 12, 20, 2.5),
        ex("Step-up to knee drive", Groups.GLUTES, 10, 15, 2.0),
        ex("Kettlebell swing, hip dominant", Groups.GLUTES, 12, 20, 4.0)
    )

    // ---- hamstrings --------------------------------------------------------

    private fun hamstrings() = listOf(
        ex("Single-leg dumbbell Romanian deadlift", Groups.HAMSTRINGS, 10, 15, 2.0),
        ex("Machine glute ham raise", Groups.HAMSTRINGS, 8, 12, 2.5),
        ex("Banded leg curl", Groups.HAMSTRINGS, 12, 20, 1.0),
        ex("Kettlebell single-leg deadlift", Groups.HAMSTRINGS, 10, 15, 4.0),
        ex("Machine 45-degree back extension", Groups.HAMSTRINGS, 12, 20, 2.5),
        ex("Cable single-leg curl", Groups.HAMSTRINGS, 12, 20, 1.0)
    )

    // ---- shoulders ---------------------------------------------------------

    private fun shoulders() = listOf(
        ex("Single-arm cable lateral raise", Groups.SHOULDERS, 12, 20, 1.0),
        ex("Dumbbell lateral raise", Groups.SHOULDERS, 12, 20, 1.0),
        ex("Seated dumbbell lateral raise", Groups.SHOULDERS, 12, 20, 1.0),
        ex("Machine reverse fly", Groups.SHOULDERS, 12, 20, 2.5),
        ex("Cable reverse fly", Groups.SHOULDERS, 12, 20, 1.0),
        ex("Cable face pull", Groups.SHOULDERS, 12, 20, 1.0),
        ex("Band lateral raise", Groups.SHOULDERS, 15, 25, 1.0),
        ex("Dumbbell W-raise", Groups.SHOULDERS, 12, 20, 1.0)
    )

    // ---- back --------------------------------------------------------------

    private fun back() = listOf(
        ex("Machine lat pulldown", Groups.BACK, 8, 15, 2.5),
        ex("Single-arm cable lat pulldown", Groups.BACK, 10, 15, 2.5),
        ex("Cable straight-arm pulldown", Groups.BACK, 12, 20, 2.5),
        ex("Machine seated row", Groups.BACK, 8, 15, 2.5),
        ex("Cable wide-grip row", Groups.BACK, 10, 15, 2.5),
        ex("Smith machine bent-over row", Groups.BACK, 8, 12, 5.0),
        ex("Kettlebell single-arm row", Groups.BACK, 10, 15, 4.0),
        ex("Band lat pulldown", Groups.BACK, 15, 25, 1.0)
    )

    // ---- chest -------------------------------------------------------------

    private fun chest() = listOf(
        ex("Machine incline press", Groups.CHEST, 8, 12, 2.5),
        ex("Machine decline press", Groups.CHEST, 8, 12, 2.5),
        ex("Cable incline fly", Groups.CHEST, 10, 15, 1.0),
        ex("Cable decline fly", Groups.CHEST, 10, 15, 1.0),
        ex("Single-arm cable fly", Groups.CHEST, 10, 15, 1.0),
        ex("Single-arm machine chest press", Groups.CHEST, 8, 12, 2.5),
        ex("Band chest press", Groups.CHEST, 12, 20, 1.0)
    )

    // ---- arms --------------------------------------------------------------

    private fun arms() = listOf(
        ex("Machine biceps curl", Groups.BICEPS, 10, 15, 2.5),
        ex("Cable bayesian curl", Groups.BICEPS, 10, 15, 1.0),
        ex("Incline dumbbell hammer curl", Groups.BICEPS, 10, 15, 1.0),
        ex("Band biceps curl", Groups.BICEPS, 15, 25, 1.0),
        ex("Machine overhead triceps extension", Groups.TRICEPS, 10, 15, 2.5),
        ex("Single-arm cable pushdown, underhand", Groups.TRICEPS, 12, 20, 1.0),
        ex("Cable rope pushdown, wide finish", Groups.TRICEPS, 12, 20, 1.0),
        ex("Band triceps pushdown", Groups.TRICEPS, 15, 25, 1.0),
        ex("Dumbbell crush press", Groups.TRICEPS, 10, 15, 2.0),
        ex("Bodyweight triceps extension on bar", Groups.TRICEPS, 8, 15, 1.0, bw = true)
    )

    // ---- core --------------------------------------------------------------

    private fun core() = listOf(
        ex("Machine crunch", Groups.CORE, 12, 20, 2.5),
        ex("Machine rotary torso", Groups.CORE, 12, 20, 2.5),
        ex("Weighted decline sit-up", Groups.CORE, 10, 20, 2.5),
        ex("Hanging windshield wiper", Groups.CORE, 6, 12, 1.0, bw = true),
        ex("Stir the pot on a ball", Groups.CORE, 10, 20, 1.0, bw = true),
        ex("Kettlebell suitcase march", Groups.CORE, 12, 20, 4.0)
    )

    // ---- full body ---------------------------------------------------------

    private fun fullBody() = listOf(
        ex("Dumbbell thruster", Groups.FULL_BODY, 8, 15, 2.0),
        ex("Kettlebell snatch", Groups.FULL_BODY, 6, 12, 4.0),
        ex("Dumbbell devil press", Groups.FULL_BODY, 6, 12, 2.0),
        ex("Dumbbell man maker", Groups.FULL_BODY, 5, 10, 2.0),
        ex("Battle rope wave", Groups.FULL_BODY, 20, 40, 1.0, bw = true),
        ex("Box jump", Groups.FULL_BODY, 5, 10, 1.0, bw = true),
        ex("Broad jump", Groups.FULL_BODY, 3, 8, 1.0, bw = true),
        ex("Sandbag over shoulder", Groups.FULL_BODY, 5, 10, 5.0)
    )
}
