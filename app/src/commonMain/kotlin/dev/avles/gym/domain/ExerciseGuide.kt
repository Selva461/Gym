package dev.avles.gym.domain

import dev.avles.gym.data.Exercise

/**
 * What a lift is, how it moves, and what it works — for every exercise in the
 * catalogue and every one you add later.
 *
 * The whole thing is **derived, never stored**. A guide is computed from the
 * exercise row itself: its name already carries the equipment (the catalogue is
 * written that way on purpose), its muscle group and the words in its name give
 * the movement pattern, and everything else follows from those two.
 *
 * That design is the point. Three hundred and ninety-four hand-written references
 * would be three hundred and ninety-four things to get wrong, and a lift you
 * invent tomorrow would have none. This way the reference exists the moment the
 * lift is named, for seeded and custom lifts alike, and it costs no database
 * column and no migration.
 *
 * It also means the reference is honest about its own resolution. It tells you
 * which way the thing moves, what it is working and what usually goes wrong. It
 * is not a coaching cue tailored to your body, and the app does not pretend it is.
 */

/** What you are holding or sitting in. Read off the lift's name. */
enum class Equipment(val label: String) {
    BARBELL("Barbell"),
    DUMBBELL("Dumbbell"),
    CABLE("Cable"),
    MACHINE("Machine"),
    SMITH("Smith machine"),
    EZ_BAR("EZ bar"),
    TRAP_BAR("Trap bar"),
    KETTLEBELL("Kettlebell"),
    BAND("Band"),
    PLATE("Plate"),
    SLED("Sled"),
    BODYWEIGHT("Bodyweight"),
    OTHER("Other")
}

/**
 * The shape of the movement. This is what the written guide and the offered
 * variations are keyed on — a cable row and a barbell row want the same words
 * about your back and different words about the load.
 */
enum class Pattern {
    HORIZONTAL_PRESS, VERTICAL_PRESS, HORIZONTAL_PULL, VERTICAL_PULL,
    SQUAT, LUNGE, HINGE, HIP_EXTENSION, LEG_CURL, LEG_EXTENSION,
    CURL, TRICEPS_EXTENSION, LATERAL_RAISE, FRONT_RAISE, FLY,
    SHRUG, CALF_RAISE, WRIST, GRIP, CARRY,
    CORE_FLEXION, CORE_ANTI_EXTENSION, CORE_ROTATION,
    EXPLOSIVE, OTHER
}

/**
 * Muscles at the resolution a body diagram can actually shade. Finer than the
 * thirteen catalogue groups — "Back" is one drawer in the picker but four
 * different regions on a drawing — and no finer than that, because a map that
 * distinguishes the two heads of the pec is a map nobody can read.
 */
enum class Muscle(val label: String) {
    CHEST_UPPER("Upper chest"), CHEST_MID("Mid chest"), CHEST_LOWER("Lower chest"),
    LATS("Lats"), RHOMBOIDS("Rhomboids"), TRAPS_UPPER("Upper traps"), TRAPS_MID("Mid traps"),
    ERECTORS("Lower back"),
    DELT_FRONT("Front delt"), DELT_SIDE("Side delt"), DELT_REAR("Rear delt"),
    TRICEPS("Triceps"), BICEPS("Biceps"), BRACHIALIS("Brachialis"),
    FOREARM_FLEXORS("Forearm flexors"), FOREARM_EXTENSORS("Forearm extensors"), GRIP("Grip"),
    QUADS("Quads"), HAMSTRINGS("Hamstrings"), GLUTES("Glutes"),
    ADDUCTORS("Adductors"), ABDUCTORS("Abductors"), HIP_FLEXORS("Hip flexors"),
    CALVES_GASTROC("Calves"), CALVES_SOLEUS("Soleus"),
    ABS("Abs"), OBLIQUES("Obliques"), SERRATUS("Serratus")
}

/**
 * One thing you can change about how a lift is performed without it becoming a
 * different lift — grip, stance, bench angle.
 *
 * Offered per movement pattern, because the question only makes sense for some:
 * a row has grips, a leg press has stances, a calf raise has neither worth
 * logging. [options] is ordered with the most common first; the empty choice is
 * always available and is what every set logged so far already is.
 */
data class VariationAxis(val name: String, val options: List<String>)

/**
 * The assembled reference for one lift.
 *
 * [primary] is what the lift is for; [secondary] is what it also works and what
 * will be sore tomorrow if you overdo it. Both feed the muscle map as well as the
 * text, which is why they are structured rather than prose.
 */
data class ExerciseGuide(
    val equipment: Equipment,
    val pattern: Pattern,
    val primary: List<Muscle>,
    val secondary: List<Muscle>,
    val setup: List<String>,
    val execution: List<String>,
    val cues: List<String>,
    val mistakes: List<String>,
    val variations: List<VariationAxis>
) {
    /** One line for a list row: "Barbell · horizontal press · chest, triceps". */
    val summary: String
        get() = listOf(
            equipment.label,
            primary.joinToString(", ") { it.label.lowercase() }
        ).joinToString(" · ")

    companion object {

        fun of(exercise: Exercise): ExerciseGuide {
            val name = exercise.name.lowercase()
            val equipment = equipmentOf(name, exercise.isBodyweight)
            val pattern = patternOf(name, exercise.muscleGroup)
            val (primary, secondary) = musclesOf(exercise.muscleGroup, pattern, name)
            return ExerciseGuide(
                equipment = equipment,
                pattern = pattern,
                primary = primary,
                secondary = secondary,
                setup = setupFor(pattern, equipment),
                execution = executionFor(pattern, equipment),
                cues = cuesFor(pattern),
                mistakes = mistakesFor(pattern),
                variations = variationsFor(pattern, equipment)
            )
        }

        // ---- equipment -------------------------------------------------------
        //
        // Order matters and is not alphabetical. "Smith machine bench press"
        // contains "machine"; "Assisted dip machine" contains "dip". The most
        // specific claim wins, which means the sequence below is the logic.

        private fun equipmentOf(name: String, bodyweightFlag: Boolean): Equipment = when {
            name.contains("smith") -> Equipment.SMITH
            // Rope, pushdown, pulldown, crossover, face pull and Pallof are cable
            // words even when the name never says "cable" — a triceps pushdown is
            // on a cable stack whatever it is called.
            name.containsAny(
                "cable", "rope", "pushdown", "pulldown", "crossover", "cross-over",
                "pull-through", "face pull", "pallof"
            ) -> Equipment.CABLE
            // Selectorised lifts. Named individually rather than by keyword because
            // "calf raise" is a machine standing and seated but bodyweight on one
            // leg on a stair, and a rule broad enough to catch the first would be
            // wrong about the second.
            name.containsAny(
                "machine", "pec deck", "leg press", "hack squat", "hammer strength",
                "assisted", "leg extension", "leg curl", "hip abduction", "hip adduction",
                "standing calf raise", "seated calf raise", "donkey calf raise"
            ) -> Equipment.MACHINE
            name.containsAny("ez-bar", "ez bar") -> Equipment.EZ_BAR
            name.containsAny("trap-bar", "trap bar", "hex bar") -> Equipment.TRAP_BAR
            name.containsAny("kettlebell", "turkish get-up") -> Equipment.KETTLEBELL
            name.contains("band") -> Equipment.BAND
            name.containsAny("plate", "svend") -> Equipment.PLATE
            name.contains("sled") -> Equipment.SLED
            // An explicit word always beats a default. "Barbell front raise" is a
            // barbell lift even though a bare front raise is a dumbbell one.
            name.contains("dumbbell") -> Equipment.DUMBBELL
            name.contains("barbell") -> Equipment.BARBELL
            // Lifts nobody qualifies because the default is understood.
            name.containsAny(
                "hammer curl", "zottman", "concentration curl", "spider curl", "cross-body",
                "lateral raise", "front raise", "rear delt fly", "reverse fly", "arnold",
                "cuban press", "powell", "scaption", "six-way", "tate press",
                "suitcase", "farmer", "goblet", "overhead triceps extension"
            ) -> Equipment.DUMBBELL
            name.containsAny(
                "clean", "snatch", "jerk", "thruster", "high pull", "rack pull",
                "good morning", "hip thrust", "skull crusher", "landmine", "spoto",
                "board press", "guillotine", "z press", "push press", "behind-the-neck",
                "viking", "drag curl", "reverse curl", "jm press", "french press", "wrist curl"
            ) -> Equipment.BARBELL
            name.containsAny(
                "push-up", "pushup", "pull-up", "pullup", "chin-up", "chinup", "dip",
                "bodyweight", "plank", "sit-up", "crunch", "ring", "nordic", "burpee",
                "hanging", "muscle-up", "handstand", "pistol", "glute bridge", "lunge",
                "tibialis raise", "russian twist", "step-up", "step up",
                "single-leg calf raise", "dead hang", "towel hang"
            ) -> Equipment.BODYWEIGHT
            bodyweightFlag -> Equipment.BODYWEIGHT
            // Bench press, squat and deadlift are barbell lifts unless the name said
            // otherwise, which by this point it has not. Deliberately not extended to
            // bare "press" or "curl": a wrist roller is neither, and claiming a
            // barbell would be worse than admitting the name does not say.
            name.containsAny("bench press", "squat", "deadlift", "overhead press", "row", "shrug") ->
                Equipment.BARBELL
            else -> Equipment.OTHER
        }

        // ---- movement pattern ------------------------------------------------

        private fun patternOf(name: String, group: String): Pattern = when {
            name.containsAny("clean", "snatch", "thruster", "jerk", "swing", "burpee", "slam", "jump") ->
                Pattern.EXPLOSIVE
            name.containsAny("carry", "farmer", "suitcase", "yoke") -> Pattern.CARRY
            name.containsAny("wrist curl", "wrist extension", "reverse wrist", "wrist roller") -> Pattern.WRIST
            // Static grip work. Matched on precise names on purpose: a bare "hang"
            // would swallow the hanging leg raises, which are core work, and the
            // hang cleans, which are caught above.
            name.containsAny("pinch", "gripper", "farmer's hold", "dead hang", "towel hang") -> Pattern.GRIP
            name.contains("shrug") -> Pattern.SHRUG
            name.containsAny("calf raise", "calf press", "donkey calf", "seated calf") -> Pattern.CALF_RAISE
            name.containsAny("leg extension", "knee extension") -> Pattern.LEG_EXTENSION
            name.containsAny("leg curl", "nordic", "ham curl") -> Pattern.LEG_CURL
            name.containsAny("hip thrust", "glute bridge", "glute kickback", "hip extension", "pull-through") ->
                Pattern.HIP_EXTENSION
            name.containsAny("lunge", "split squat", "step-up", "step up", "bulgarian") -> Pattern.LUNGE
            name.containsAny("squat", "leg press", "hack") -> Pattern.SQUAT
            name.containsAny("deadlift", "romanian", "rdl", "good morning", "back extension", "hyperextension", "rack pull") ->
                Pattern.HINGE
            name.containsAny("twist", "woodchop", "wood chop", "side bend", "oblique", "windmill", "landmine 180") ->
                Pattern.CORE_ROTATION
            name.containsAny("plank", "ab wheel", "rollout", "dead bug", "hollow", "pallof") ->
                Pattern.CORE_ANTI_EXTENSION
            name.containsAny("crunch", "sit-up", "situp", "leg raise", "knee raise", "v-up", "toes to bar") ->
                Pattern.CORE_FLEXION
            name.containsAny("lateral raise", "side raise", "side lateral", "upright row") -> Pattern.LATERAL_RAISE
            name.containsAny("front raise", "plate raise") -> Pattern.FRONT_RAISE
            // Any other shoulder raise — Powell, scaption, six-way. Described as a
            // lateral raise because that is the plane they mostly live in, and it is
            // far closer than the overhead press the group default would have given.
            name.contains("raise") && group == "Shoulders" -> Pattern.LATERAL_RAISE
            name.containsAny("fly", "flye", "pec deck", "crossover", "cross-over") -> Pattern.FLY
            // A triceps kickback and a glute kickback share a word and nothing else,
            // so the group breaks the tie. Note this tests the movement's words and
            // not the group: close-grip bench, dips and diamond push-ups are filed
            // under Triceps but they are presses, and telling someone only their
            // forearm should move during a dip would be nonsense. They fall through
            // to the press branch below, and the group only decides at the very end.
            name.containsAny("pushdown", "skull crusher", "triceps extension") ||
                (name.contains("kickback") && group != "Glutes") -> Pattern.TRICEPS_EXTENSION
            name.containsAny("curl") -> Pattern.CURL
            name.containsAny("pulldown", "pull-up", "pullup", "chin-up", "chinup", "pullover", "muscle-up") ->
                Pattern.VERTICAL_PULL
            name.containsAny("row", "face pull", "rear delt", "reverse fly") -> Pattern.HORIZONTAL_PULL
            name.containsAny("overhead press", "shoulder press", "military", "arnold", "handstand", "z press") ->
                Pattern.VERTICAL_PRESS
            name.containsAny("bench press", "chest press", "push-up", "pushup", "dip", "floor press") ->
                Pattern.HORIZONTAL_PRESS
            // Nothing matched a word, so fall back on what the lift is filed under.
            else -> when (group) {
                "Chest" -> Pattern.HORIZONTAL_PRESS
                "Back" -> Pattern.HORIZONTAL_PULL
                "Shoulders" -> Pattern.VERTICAL_PRESS
                "Traps" -> Pattern.SHRUG
                "Triceps" -> Pattern.TRICEPS_EXTENSION
                "Biceps", "Forearms" -> Pattern.CURL
                "Quads" -> Pattern.SQUAT
                "Hamstrings" -> Pattern.HINGE
                "Glutes" -> Pattern.HIP_EXTENSION
                "Calves" -> Pattern.CALF_RAISE
                "Core" -> Pattern.CORE_FLEXION
                "Full body" -> Pattern.EXPLOSIVE
                else -> Pattern.OTHER
            }
        }

        // ---- muscles ---------------------------------------------------------

        private fun musclesOf(
            group: String,
            pattern: Pattern,
            name: String
        ): Pair<List<Muscle>, List<Muscle>> {
            // An incline press works the upper chest; a decline works the lower.
            // The word is in the name, so use it rather than shading the whole pec.
            val chest = when {
                name.containsAny("incline") -> listOf(Muscle.CHEST_UPPER, Muscle.CHEST_MID)
                name.containsAny("decline", "dip") -> listOf(Muscle.CHEST_LOWER, Muscle.CHEST_MID)
                else -> listOf(Muscle.CHEST_MID, Muscle.CHEST_UPPER, Muscle.CHEST_LOWER)
            }

            val primary = when (group) {
                "Chest" -> chest
                "Back" -> when (pattern) {
                    Pattern.VERTICAL_PULL -> listOf(Muscle.LATS, Muscle.TRAPS_MID)
                    Pattern.HINGE -> listOf(Muscle.ERECTORS, Muscle.LATS)
                    else -> listOf(Muscle.LATS, Muscle.RHOMBOIDS, Muscle.TRAPS_MID)
                }
                "Shoulders" -> when (pattern) {
                    Pattern.LATERAL_RAISE -> listOf(Muscle.DELT_SIDE)
                    Pattern.FRONT_RAISE -> listOf(Muscle.DELT_FRONT)
                    Pattern.HORIZONTAL_PULL -> listOf(Muscle.DELT_REAR)
                    else -> listOf(Muscle.DELT_FRONT, Muscle.DELT_SIDE)
                }
                "Traps" -> listOf(Muscle.TRAPS_UPPER, Muscle.TRAPS_MID)
                "Triceps" -> listOf(Muscle.TRICEPS)
                "Biceps" -> listOf(Muscle.BICEPS, Muscle.BRACHIALIS)
                "Forearms" -> if (name.contains("reverse") || name.contains("extension"))
                    listOf(Muscle.FOREARM_EXTENSORS) else listOf(Muscle.FOREARM_FLEXORS, Muscle.GRIP)
                "Quads" -> listOf(Muscle.QUADS)
                "Hamstrings" -> listOf(Muscle.HAMSTRINGS)
                "Glutes" -> when {
                    name.contains("adduction") -> listOf(Muscle.ADDUCTORS)
                    name.contains("abduction") -> listOf(Muscle.ABDUCTORS, Muscle.GLUTES)
                    else -> listOf(Muscle.GLUTES)
                }
                "Calves" -> if (name.contains("seated")) listOf(Muscle.CALVES_SOLEUS)
                    else listOf(Muscle.CALVES_GASTROC, Muscle.CALVES_SOLEUS)
                "Core" -> when (pattern) {
                    Pattern.CORE_ROTATION -> listOf(Muscle.OBLIQUES, Muscle.ABS)
                    else -> listOf(Muscle.ABS, Muscle.OBLIQUES)
                }
                else -> listOf(Muscle.QUADS, Muscle.GLUTES, Muscle.ERECTORS)
            }

            // These list every muscle the movement works, including the one it is
            // usually filed under. The overlap filter at the end removes whichever
            // is primary, which means a lift filed somewhere unexpected still gets
            // the full picture: a close-grip bench lives under Triceps, and without
            // the chest here its muscle map would show no chest at all.
            val secondary = when (pattern) {
                Pattern.HORIZONTAL_PRESS -> listOf(Muscle.CHEST_MID, Muscle.TRICEPS, Muscle.DELT_FRONT, Muscle.SERRATUS)
                Pattern.VERTICAL_PRESS -> listOf(Muscle.DELT_FRONT, Muscle.TRICEPS, Muscle.TRAPS_UPPER, Muscle.ABS)
                Pattern.HORIZONTAL_PULL -> listOf(Muscle.LATS, Muscle.BICEPS, Muscle.DELT_REAR, Muscle.ERECTORS, Muscle.GRIP)
                Pattern.VERTICAL_PULL -> listOf(Muscle.LATS, Muscle.BICEPS, Muscle.BRACHIALIS, Muscle.RHOMBOIDS, Muscle.GRIP)
                Pattern.SQUAT -> listOf(Muscle.GLUTES, Muscle.ADDUCTORS, Muscle.ERECTORS, Muscle.ABS)
                Pattern.LUNGE -> listOf(Muscle.GLUTES, Muscle.HAMSTRINGS, Muscle.ADDUCTORS, Muscle.ABS)
                Pattern.HINGE -> listOf(Muscle.GLUTES, Muscle.HAMSTRINGS, Muscle.TRAPS_MID, Muscle.GRIP)
                Pattern.HIP_EXTENSION -> listOf(Muscle.HAMSTRINGS, Muscle.ERECTORS, Muscle.ABS)
                Pattern.LEG_CURL -> listOf(Muscle.CALVES_GASTROC, Muscle.GLUTES)
                Pattern.LEG_EXTENSION -> listOf(Muscle.HIP_FLEXORS)
                Pattern.CURL -> listOf(Muscle.BICEPS, Muscle.BRACHIALIS, Muscle.FOREARM_FLEXORS, Muscle.GRIP)
                Pattern.TRICEPS_EXTENSION -> listOf(Muscle.TRICEPS, Muscle.DELT_FRONT, Muscle.ABS)
                Pattern.LATERAL_RAISE -> listOf(Muscle.TRAPS_UPPER, Muscle.DELT_FRONT)
                Pattern.FRONT_RAISE -> listOf(Muscle.DELT_SIDE, Muscle.ABS)
                Pattern.FLY -> listOf(Muscle.DELT_FRONT, Muscle.BICEPS)
                Pattern.SHRUG -> listOf(Muscle.TRAPS_MID, Muscle.GRIP, Muscle.FOREARM_FLEXORS)
                Pattern.CALF_RAISE -> listOf(Muscle.HAMSTRINGS)
                Pattern.WRIST -> listOf(Muscle.GRIP)
                Pattern.GRIP -> listOf(Muscle.FOREARM_FLEXORS, Muscle.FOREARM_EXTENSORS, Muscle.TRAPS_UPPER)
                Pattern.CARRY -> listOf(Muscle.GRIP, Muscle.TRAPS_UPPER, Muscle.ABS, Muscle.OBLIQUES)
                Pattern.CORE_FLEXION -> listOf(Muscle.HIP_FLEXORS, Muscle.OBLIQUES)
                Pattern.CORE_ANTI_EXTENSION -> listOf(Muscle.OBLIQUES, Muscle.DELT_FRONT, Muscle.GLUTES)
                Pattern.CORE_ROTATION -> listOf(Muscle.ABS, Muscle.HIP_FLEXORS)
                Pattern.EXPLOSIVE -> listOf(Muscle.QUADS, Muscle.GLUTES, Muscle.TRAPS_UPPER, Muscle.ERECTORS)
                Pattern.OTHER -> emptyList()
            }

            // A muscle cannot be both the point of the lift and a bystander.
            return primary to secondary.filterNot { it in primary }
        }

        // ---- written reference -----------------------------------------------

        private fun loadWord(e: Equipment) = when (e) {
            Equipment.CABLE -> "the handle"
            Equipment.MACHINE -> "the handles"
            Equipment.BODYWEIGHT -> "yourself"
            Equipment.BAND -> "the band"
            else -> "the weight"
        }

        private fun setupFor(pattern: Pattern, e: Equipment): List<String> {
            val base = when (pattern) {
                Pattern.HORIZONTAL_PRESS -> listOf(
                    "Lie back with your eyes under the bar, or sit into the machine so the handles line up with mid-chest.",
                    "Feet flat and driving into the floor. Shoulder blades pulled back and down, and kept there.",
                    "Grip a little wider than your shoulders unless you are deliberately going close."
                )
                Pattern.VERTICAL_PRESS -> listOf(
                    "Stand or sit tall with the load at shoulder height, elbows just in front of the body.",
                    "Ribs down and glutes tight — the press should not turn into a standing back bend.",
                    "Grip just outside shoulder width, wrists stacked over the elbows."
                )
                Pattern.HORIZONTAL_PULL -> listOf(
                    "Hinge forward or sit with a tall chest, whichever the lift asks for.",
                    "Arms long at the start, shoulder blades allowed to stretch forward.",
                    "Set your lower back before the first rep and hold that position throughout."
                )
                Pattern.VERTICAL_PULL -> listOf(
                    "Take a grip roughly a hand's width outside your shoulders.",
                    "Chest up, ribs down, a slight lean back — not a swing.",
                    "Start from a full hang or full stretch with the shoulders pulled away from the ears."
                )
                Pattern.SQUAT -> listOf(
                    "Feet about shoulder width, toes turned slightly out.",
                    "Whole foot on the floor — spread the weight between heel and the base of the big toe.",
                    "Brace as if about to be shoved sideways, then start."
                )
                Pattern.LUNGE -> listOf(
                    "Take a stride long enough that the front shin stays near vertical at the bottom.",
                    "Torso upright, weight through the front heel.",
                    "Find your balance before adding load, not after."
                )
                Pattern.HINGE -> listOf(
                    "Feet hip width, load close to the shins.",
                    "Soft knees, chest proud, weight in the middle of the foot.",
                    "Take the slack out and set your back before anything moves."
                )
                Pattern.HIP_EXTENSION -> listOf(
                    "Shoulders or upper back supported, feet planted so the shins finish vertical at the top.",
                    "Ribs down and chin tucked — this is a hip movement, not a back movement."
                )
                Pattern.LEG_CURL, Pattern.LEG_EXTENSION -> listOf(
                    "Line the machine's pivot up with your knee joint. This is the one setting that matters.",
                    "Pad sitting on the soft part of the leg, not on bone.",
                    "Hold the handles and keep your hips down on the seat."
                )
                Pattern.CURL -> listOf(
                    "Stand or sit tall, elbows pinned at your sides.",
                    "Shoulders back and still — they are not part of this.",
                    "Full extension at the bottom before the first rep."
                )
                Pattern.TRICEPS_EXTENSION -> listOf(
                    "Elbows tucked and fixed — wherever you set them is where they stay.",
                    "Brace the midsection so the weight does not pull you forward or arch you back.",
                    "Start from a full stretch on the triceps."
                )
                Pattern.LATERAL_RAISE, Pattern.FRONT_RAISE -> listOf(
                    "Stand tall with a very slight forward lean and a soft elbow.",
                    "Start with the load hanging, shoulders down away from the ears.",
                    "Pick a weight you can raise without a hitch. This one is not a strength lift."
                )
                Pattern.FLY -> listOf(
                    "Set the handles or your arms level with mid-chest.",
                    "Shoulder blades back and down, chest open.",
                    "Fix a slight bend in the elbow and do not change it during the set."
                )
                Pattern.SHRUG -> listOf(
                    "Stand tall, arms hanging, load in front of or beside the thighs.",
                    "Chin level and neck neutral."
                )
                Pattern.CALF_RAISE -> listOf(
                    "Balls of the feet on the edge, heels free to drop below.",
                    "Knees straight for the calf, bent for the soleus — the position picks the muscle."
                )
                Pattern.WRIST -> listOf(
                    "Forearms resting on a bench or your thighs, wrists just past the edge.",
                    "Light load. This is a small muscle with a short range."
                )
                Pattern.CARRY -> listOf(
                    "Pick the load up with a proper hinge, not a rounded back.",
                    "Stand tall, shoulders back, ribs down."
                )
                Pattern.GRIP -> listOf(
                    "Take the grip you intend to hold and set it before the clock starts.",
                    "Shoulders pulled down away from the ears, not hanging loose in the sockets.",
                    "Time is the load here, so have a way to see the seconds."
                )
                Pattern.CORE_FLEXION -> listOf(
                    "Lower back in contact with the floor or the pad at the start.",
                    "Hands where they will not pull on your neck."
                )
                Pattern.CORE_ANTI_EXTENSION -> listOf(
                    "Get into a straight line from ear to heel before the clock starts.",
                    "Ribs down, glutes tight, hips level."
                )
                Pattern.CORE_ROTATION -> listOf(
                    "Feet planted, hips facing forward.",
                    "The rotation comes from the ribs, not the lower back."
                )
                Pattern.EXPLOSIVE -> listOf(
                    "Learn the positions light before you load them.",
                    "Feet about hip width, load close to the body.",
                    "Full breath and brace before every single rep."
                )
                Pattern.OTHER -> listOf(
                    "Set your position and brace before the first rep.",
                    "Pick a load you can control through the whole range."
                )
            }
            val kit = when (e) {
                Equipment.CABLE -> "Set the pulley height first — it decides where the tension is hardest, which is most of what a cable lift is for."
                Equipment.MACHINE -> "Set the seat so the pivot matches your joint. Every machine is adjustable and most people never touch it."
                Equipment.SMITH -> "The bar's path is fixed, so your feet have to be where that path suits you. Adjust your stance, not the bar."
                Equipment.BODYWEIGHT -> "Load comes from leverage here — change the angle or the tempo, not the plates."
                Equipment.BAND -> "Anchor the band to something that will not move, and check the tension at the start of the range."
                Equipment.DUMBBELL -> "Two independent weights means the weaker side sets the honest number. Do not let the strong arm cover for it."
                Equipment.KETTLEBELL -> "Let the bell sit where it wants to sit in the rack or the hang; fighting it costs more than it gains."
                else -> null
            }
            return base + listOfNotNull(kit)
        }

        private fun executionFor(pattern: Pattern, e: Equipment): List<String> {
            val load = loadWord(e)
            return when (pattern) {
                Pattern.HORIZONTAL_PRESS -> listOf(
                    "Lower $load under control to the chest or to a deep stretch — two to three seconds down.",
                    "Elbows around 45 degrees from the body, not flared to 90.",
                    "Press back up and slightly back towards your face, not straight out over the shoulders."
                )
                Pattern.VERTICAL_PRESS -> listOf(
                    "Press straight up, moving your head back out of the way and then through as the load passes.",
                    "Finish with the arms locked and the load stacked over the middle of the foot.",
                    "Lower under control to the start — do not let it drop onto your shoulders."
                )
                Pattern.HORIZONTAL_PULL -> listOf(
                    "Lead with the elbows and pull $load to your lower ribs or upper stomach.",
                    "Squeeze the shoulder blades together at the end of the pull, then hold for a beat.",
                    "Let the arms extend fully on the way back, keeping the torso still."
                )
                Pattern.VERTICAL_PULL -> listOf(
                    "Pull the elbows down and into your sides until the bar reaches your upper chest.",
                    "Think about driving the elbows into your back pockets rather than pulling with the hands.",
                    "Control the way back to a full stretch — that is half the work."
                )
                Pattern.SQUAT -> listOf(
                    "Sit down and back, knees tracking out over the toes.",
                    "Go to a depth you can reach without the lower back rounding — for most people that is hips level with or just below the knees.",
                    "Drive the floor away, hips and shoulders rising together."
                )
                Pattern.LUNGE -> listOf(
                    "Lower until the back knee is just off the floor.",
                    "Drive up through the front heel; the back leg is for balance, not for power.",
                    "Finish each rep tall before starting the next."
                )
                Pattern.HINGE -> listOf(
                    "Push the hips back, letting the knees bend only as much as the movement needs.",
                    "Lower until you feel a hard stretch in the hamstrings with a flat back — depth past that gains nothing.",
                    "Drive the hips forward to stand, finishing with glutes tight and ribs down."
                )
                Pattern.HIP_EXTENSION -> listOf(
                    "Drive through the heels and push the hips up until the body is flat from knee to shoulder.",
                    "Hold the top for a second and squeeze.",
                    "Lower under control without letting the load rest at the bottom."
                )
                Pattern.LEG_CURL -> listOf(
                    "Curl the heels towards your backside, all the way.",
                    "Pause at the top where it is hardest.",
                    "Lower slowly — hamstrings respond to the lowering more than most muscles."
                )
                Pattern.LEG_EXTENSION -> listOf(
                    "Extend until the legs are straight and hold for a beat.",
                    "Lower under control; do not let the stack slam."
                )
                Pattern.CURL -> listOf(
                    "Curl $load up by bending the elbow only — the upper arm stays still.",
                    "Squeeze at the top without swinging the elbow forward.",
                    "Lower all the way to a straight arm, slowly."
                )
                Pattern.TRICEPS_EXTENSION -> listOf(
                    "Straighten the elbow against $load, keeping the upper arm fixed.",
                    "Lock out and hold for a count.",
                    "Return to a full stretch under control."
                )
                Pattern.LATERAL_RAISE -> listOf(
                    "Raise out to the side to about shoulder height, leading with the elbow.",
                    "Little fingers slightly higher than thumbs at the top.",
                    "Lower slowly — three seconds down is not too slow here."
                )
                Pattern.FRONT_RAISE -> listOf(
                    "Raise to shoulder height in front of you, arm nearly straight.",
                    "Stop at shoulder height; higher is the traps taking over.",
                    "Lower under control."
                )
                Pattern.FLY -> listOf(
                    "Open the arms wide until you feel a stretch across the chest.",
                    "Bring them together as if hugging, squeezing at the end.",
                    "Keep the elbow angle fixed the whole way."
                )
                Pattern.SHRUG -> listOf(
                    "Lift the shoulders straight up towards the ears.",
                    "Hold the top for a second — this lift has a short range and the pause is where the work is.",
                    "Lower all the way down. No rolling."
                )
                Pattern.CALF_RAISE -> listOf(
                    "Drop the heels as far below the step as they will go.",
                    "Push all the way up onto the toes and pause.",
                    "Lower slowly. The stretch is the point."
                )
                Pattern.WRIST -> listOf(
                    "Curl the hand up through its full range.",
                    "Pause at the top, lower slowly."
                )
                Pattern.CARRY -> listOf(
                    "Walk a set distance or time with short, controlled steps.",
                    "Do not let the shoulders round or the load swing."
                )
                Pattern.GRIP -> listOf(
                    "Hold until the grip genuinely fails rather than until it gets uncomfortable.",
                    "Keep the body still — swinging makes the hold easier and the set pointless.",
                    "Log the seconds, not a rep count."
                )
                Pattern.CORE_FLEXION -> listOf(
                    "Curl the ribs towards the hips rather than lifting with a straight back.",
                    "Exhale at the top and hold for a beat.",
                    "Lower slowly under control."
                )
                Pattern.CORE_ANTI_EXTENSION -> listOf(
                    "Hold the position, resisting the pull that wants to arch your back.",
                    "Breathe normally throughout — holding your breath is not bracing.",
                    "End the set when the position breaks, not when the clock says so."
                )
                Pattern.CORE_ROTATION -> listOf(
                    "Rotate through the ribs against the resistance, hips staying square.",
                    "Control the way back — the return is the useful half."
                )
                Pattern.EXPLOSIVE -> listOf(
                    "Move fast on the way up and catch or land soft.",
                    "Reset fully between reps; these are not meant to be rushed together.",
                    "Stop the set when speed drops, not when you are exhausted."
                )
                Pattern.OTHER -> listOf(
                    "Move through the full range under control.",
                    "Pause where the muscle is working hardest.",
                    "Lower more slowly than you lift."
                )
            }
        }

        private fun cuesFor(pattern: Pattern): List<String> = when (pattern) {
            Pattern.HORIZONTAL_PRESS -> listOf("Bend the bar apart", "Shoulder blades in your back pockets", "Press yourself away from the weight")
            Pattern.VERTICAL_PRESS -> listOf("Ribs down", "Head through at the top", "Squeeze the glutes")
            Pattern.HORIZONTAL_PULL -> listOf("Elbows to the wall behind you", "Chest to the bar, not bar to the chest", "Pause at the squeeze")
            Pattern.VERTICAL_PULL -> listOf("Elbows to the hips", "Chest up to meet it", "Long arms at the bottom")
            Pattern.SQUAT -> listOf("Spread the floor", "Knees out", "Big breath, hold it down and up")
            Pattern.LUNGE -> listOf("Tall chest", "Push the floor away with the front heel")
            Pattern.HINGE -> listOf("Hips back, not down", "Long spine", "Squeeze a walnut with the glutes at the top")
            Pattern.HIP_EXTENSION -> listOf("Chin tucked", "Finish with the hips, not the back")
            Pattern.LEG_CURL -> listOf("Heels to the backside", "Slow on the way out")
            Pattern.LEG_EXTENSION -> listOf("Squeeze at the top", "No bouncing off the stack")
            Pattern.CURL -> listOf("Elbows glued", "No swing", "All the way down")
            Pattern.TRICEPS_EXTENSION -> listOf("Only the forearm moves", "Lock and hold", "Stretch at the bottom")
            Pattern.LATERAL_RAISE -> listOf("Lead with the elbow", "Pour the jug", "Slow down on the way back")
            Pattern.FRONT_RAISE -> listOf("Stop at shoulder height", "No leaning back")
            Pattern.FLY -> listOf("Hug a barrel", "Stretch, then squeeze", "Fixed elbow")
            Pattern.SHRUG -> listOf("Shoulders to ears", "Pause at the top")
            Pattern.CALF_RAISE -> listOf("All the way down, all the way up", "Pause at both ends")
            Pattern.WRIST -> listOf("Small range, slow tempo")
            Pattern.CARRY -> listOf("Tall and tight", "Short steps")
            Pattern.GRIP -> listOf("Crush it", "Shoulders down", "Hold to failure, not to discomfort")
            Pattern.CORE_FLEXION -> listOf("Ribs to hips", "Exhale at the top")
            Pattern.CORE_ANTI_EXTENSION -> listOf("Straight line", "Ribs down, glutes on")
            Pattern.CORE_ROTATION -> listOf("Hips square", "Turn from the ribs")
            Pattern.EXPLOSIVE -> listOf("Fast up, soft down", "Reset every rep")
            Pattern.OTHER -> listOf("Control it", "Full range")
        }

        private fun mistakesFor(pattern: Pattern): List<String> = when (pattern) {
            Pattern.HORIZONTAL_PRESS -> listOf(
                "Flaring the elbows straight out to the sides, which is where shoulders get hurt.",
                "Bouncing the bar off the chest instead of controlling it down.",
                "Letting the shoulder blades come loose at the top and pressing off an unstable base."
            )
            Pattern.VERTICAL_PRESS -> listOf(
                "Leaning back until it is an inclined bench press.",
                "Pressing around the head rather than moving the head out of the way.",
                "Going heavier than the midsection can brace for."
            )
            Pattern.HORIZONTAL_PULL -> listOf(
                "Heaving with the lower back so the torso rises with every rep.",
                "Pulling with the hands and biceps while the shoulder blades never move.",
                "Cutting the return short and losing the stretch."
            )
            Pattern.VERTICAL_PULL -> listOf(
                "Swinging the body to get the last few reps.",
                "Stopping short of a full hang, which removes the hardest part.",
                "Pulling behind the neck, which buys nothing and costs shoulder room."
            )
            Pattern.SQUAT -> listOf(
                "Hips shooting up first so it becomes a good morning.",
                "Heels lifting, or the weight rolling onto the toes.",
                "Chasing depth past what the lower back can hold flat."
            )
            Pattern.LUNGE -> listOf(
                "Stride too short, so the front knee travels far over the toes.",
                "Pushing off the back foot rather than driving through the front heel.",
                "Letting the torso fold forward under load."
            )
            Pattern.HINGE -> listOf(
                "Rounding the lower back, which is the one thing that actually hurts people here.",
                "Turning it into a squat by bending the knees too early.",
                "Hyperextending at the top and leaning back."
            )
            Pattern.HIP_EXTENSION -> listOf(
                "Arching the lower back instead of finishing with the hips.",
                "Pushing through the toes rather than the heels.",
                "A range so short the glutes never fully shorten."
            )
            Pattern.LEG_CURL -> listOf(
                "Hips lifting off the pad to help.",
                "Letting the weight drop back rather than controlling it."
            )
            Pattern.LEG_EXTENSION -> listOf(
                "Pivot not lined up with the knee, which puts the load in the wrong place.",
                "Slamming the stack at the bottom of every rep."
            )
            Pattern.CURL -> listOf(
                "Swinging with the hips to start the rep.",
                "Elbows drifting forward, which hands the work to the front delt.",
                "Stopping short of a straight arm at the bottom."
            )
            Pattern.TRICEPS_EXTENSION -> listOf(
                "Elbows flaring and travelling, turning it into a press.",
                "Leaning into the weight to push it down.",
                "Too much load for a single-joint movement on a small muscle."
            )
            Pattern.LATERAL_RAISE -> listOf(
                "Using so much weight that it becomes a shrug and a swing.",
                "Raising well above shoulder height, which hands the work to the traps.",
                "Dropping the weight back down with no control."
            )
            Pattern.FRONT_RAISE -> listOf(
                "Leaning back to launch it.",
                "Going above shoulder height."
            )
            Pattern.FLY -> listOf(
                "Bending and straightening the elbow, which makes it a clumsy press.",
                "Stretching so far that the shoulder takes the load instead of the chest."
            )
            Pattern.SHRUG -> listOf(
                "Rolling the shoulders, which adds wear and no muscle.",
                "No pause at the top on a movement whose range is already tiny."
            )
            Pattern.CALF_RAISE -> listOf(
                "Bouncing on the tendon rather than working the muscle.",
                "A half range at both ends."
            )
            Pattern.WRIST -> listOf("Loading it like a curl on a joint that cannot take it.")
            Pattern.CARRY -> listOf(
                "Rounding forward as the grip fades.",
                "Long strides that let the load swing."
            )
            Pattern.GRIP -> listOf(
                "Hanging dead from loose shoulders rather than holding them down.",
                "Stopping on a round number instead of when the hands actually open.",
                "Chalk or straps, which train the strap rather than the grip."
            )
            Pattern.CORE_FLEXION -> listOf(
                "Pulling on the back of the neck with the hands.",
                "Lifting with a straight back so the hip flexors do the work."
            )
            Pattern.CORE_ANTI_EXTENSION -> listOf(
                "Hips sagging or piking, which is the set ending whether you stop or not.",
                "Holding the breath instead of breathing behind the brace."
            )
            Pattern.CORE_ROTATION -> listOf(
                "Twisting from the lower back rather than the ribs.",
                "Letting the hips rotate with the torso, which removes the resistance."
            )
            Pattern.EXPLOSIVE -> listOf(
                "Grinding slow reps, which defeats the point of the movement.",
                "Adding load before the positions are solid.",
                "Running the set to failure — speed work stops when speed stops."
            )
            Pattern.OTHER -> listOf("Rushing the lowering phase.", "Using a range shorter than the muscle's.")
        }

        // ---- variations ------------------------------------------------------
        //
        // Offered only where the choice changes the lift enough to be worth its own
        // history. A calf raise has no grip worth logging and does not get an axis
        // just to look complete.

        private val GRIP_WIDTH = VariationAxis("Grip width", listOf("Standard", "Close", "Wide", "Shoulder width"))
        private val GRIP_STYLE = VariationAxis("Grip", listOf("Overhand", "Underhand", "Neutral", "Mixed", "Rope", "Fat grip"))
        private val STANCE = VariationAxis("Stance", listOf("Standard", "Narrow", "Wide", "Sumo", "Staggered"))
        private val FOOT_PLACEMENT = VariationAxis("Foot placement", listOf("Mid", "High", "Low", "Narrow", "Wide"))
        private val BENCH_ANGLE = VariationAxis("Bench angle", listOf("Flat", "Incline 30°", "Incline 45°", "Decline"))
        private val TEMPO = VariationAxis("Tempo", listOf("Normal", "Paused", "Slow eccentric", "1½ reps"))
        private val ARM = VariationAxis("Arms", listOf("Both", "Single — left", "Single — right", "Alternating"))
        private val PULLEY = VariationAxis("Pulley height", listOf("Low", "Mid", "High"))
        private val KNEE = VariationAxis("Knee", listOf("Straight", "Bent"))

        private fun variationsFor(pattern: Pattern, e: Equipment): List<VariationAxis> {
            val axes = when (pattern) {
                Pattern.HORIZONTAL_PRESS -> listOf(GRIP_WIDTH, BENCH_ANGLE, TEMPO)
                Pattern.VERTICAL_PRESS -> listOf(GRIP_WIDTH, GRIP_STYLE, TEMPO)
                Pattern.HORIZONTAL_PULL -> listOf(GRIP_STYLE, GRIP_WIDTH, TEMPO)
                Pattern.VERTICAL_PULL -> listOf(GRIP_STYLE, GRIP_WIDTH, TEMPO)
                Pattern.SQUAT -> listOf(STANCE, TEMPO)
                Pattern.LUNGE -> listOf(STANCE, TEMPO)
                Pattern.HINGE -> listOf(STANCE, GRIP_STYLE, TEMPO)
                Pattern.HIP_EXTENSION -> listOf(FOOT_PLACEMENT, TEMPO)
                Pattern.LEG_CURL, Pattern.LEG_EXTENSION -> listOf(FOOT_PLACEMENT, TEMPO)
                Pattern.CURL -> listOf(GRIP_STYLE, ARM, TEMPO)
                Pattern.TRICEPS_EXTENSION -> listOf(GRIP_STYLE, ARM, TEMPO)
                Pattern.LATERAL_RAISE, Pattern.FRONT_RAISE -> listOf(ARM, TEMPO)
                Pattern.FLY -> listOf(PULLEY, TEMPO)
                Pattern.SHRUG -> listOf(GRIP_STYLE, TEMPO)
                Pattern.CALF_RAISE -> listOf(KNEE, FOOT_PLACEMENT)
                Pattern.CORE_ROTATION, Pattern.CORE_FLEXION -> listOf(TEMPO)
                else -> emptyList()
            }
            // A pulley height axis is meaningless on a dumbbell, and a bench angle
            // is meaningless on a machine whose seat is fixed.
            return axes.filterNot { it === PULLEY && e != Equipment.CABLE }
        }

        private fun String.containsAny(vararg needles: String) = needles.any { this.contains(it) }
    }
}
