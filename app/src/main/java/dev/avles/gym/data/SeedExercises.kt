package dev.avles.gym.data

/**
 * The lift catalogue.
 *
 * Split out of [SeedData] because it is long enough to deserve its own file, and
 * because it is pure data — a list of names with a rep range and a sensible jump.
 *
 * **Naming carries the equipment.** "Cable rope overhead triceps extension" rather
 * than "Overhead extension (cable, rope)". That is not a style preference: the lift
 * picker searches on the name, so typing `cable`, `dumbbell`, `machine` or `smith`
 * filters the whole catalogue down to that equipment in one word. Without the
 * equipment in the name there is no way to ask that question.
 *
 * **Muscle groups are real muscle groups**, not Push/Pull/Legs. With this many
 * lifts, three buckets of a hundred each is a scroll, not a menu. Chest, Back,
 * Shoulders, Traps, Triceps, Biceps, Forearms, Quads, Hamstrings, Glutes, Calves,
 * Core, Full body.
 *
 * **Rep ranges and increments are per lift**, because they have to be. A squat
 * absorbs a 5 kg jump; a lateral raise takes 1 kg and a cable pushdown takes
 * whatever the stack's smallest plate is. Getting this wrong is how a progression
 * suggestion becomes something you learn to ignore.
 */
internal object SeedExercises {

    private fun ex(
        name: String,
        group: String,
        low: Int,
        high: Int,
        inc: Double,
        bw: Boolean = false
    ) = Exercise(
        name = name,
        muscleGroup = group,
        isBodyweight = bw,
        repLow = low,
        repHigh = high,
        incrementKg = inc
    )

    // Per-group shorthands, so each lift is one readable line and the defaults for
    // that muscle are stated once instead of eighty times.
    private fun chest(n: String, low: Int = 6, high: Int = 12, inc: Double = 2.5, bw: Boolean = false) =
        ex(n, Groups.CHEST, low, high, inc, bw)

    private fun back(n: String, low: Int = 6, high: Int = 12, inc: Double = 2.5, bw: Boolean = false) =
        ex(n, Groups.BACK, low, high, inc, bw)

    private fun delt(n: String, low: Int = 10, high: Int = 15, inc: Double = 1.0, bw: Boolean = false) =
        ex(n, Groups.SHOULDERS, low, high, inc, bw)

    private fun trap(n: String, low: Int = 10, high: Int = 15, inc: Double = 2.5, bw: Boolean = false) =
        ex(n, Groups.TRAPS, low, high, inc, bw)

    private fun tri(n: String, low: Int = 8, high: Int = 15, inc: Double = 2.0, bw: Boolean = false) =
        ex(n, Groups.TRICEPS, low, high, inc, bw)

    private fun bi(n: String, low: Int = 8, high: Int = 12, inc: Double = 1.0, bw: Boolean = false) =
        ex(n, Groups.BICEPS, low, high, inc, bw)

    private fun fore(n: String, low: Int = 12, high: Int = 20, inc: Double = 1.0, bw: Boolean = false) =
        ex(n, Groups.FOREARMS, low, high, inc, bw)

    private fun quad(n: String, low: Int = 6, high: Int = 12, inc: Double = 5.0, bw: Boolean = false) =
        ex(n, Groups.QUADS, low, high, inc, bw)

    private fun ham(n: String, low: Int = 8, high: Int = 12, inc: Double = 2.5, bw: Boolean = false) =
        ex(n, Groups.HAMSTRINGS, low, high, inc, bw)

    private fun glute(n: String, low: Int = 8, high: Int = 15, inc: Double = 5.0, bw: Boolean = false) =
        ex(n, Groups.GLUTES, low, high, inc, bw)

    private fun calf(n: String, low: Int = 10, high: Int = 20, inc: Double = 2.5, bw: Boolean = false) =
        ex(n, Groups.CALVES, low, high, inc, bw)

    private fun core(n: String, low: Int = 10, high: Int = 20, inc: Double = 2.5, bw: Boolean = false) =
        ex(n, Groups.CORE, low, high, inc, bw)

    private fun full(n: String, low: Int = 3, high: Int = 6, inc: Double = 5.0, bw: Boolean = false) =
        ex(n, Groups.FULL_BODY, low, high, inc, bw)

    /** The muscle groups the picker organises by, and the new-lift dialog offers. */
    object Groups {
        const val CHEST = "Chest"
        const val BACK = "Back"
        const val SHOULDERS = "Shoulders"
        const val TRAPS = "Traps"
        const val TRICEPS = "Triceps"
        const val BICEPS = "Biceps"
        const val FOREARMS = "Forearms"
        const val QUADS = "Quads"
        const val HAMSTRINGS = "Hamstrings"
        const val GLUTES = "Glutes"
        const val CALVES = "Calves"
        const val CORE = "Core"
        const val FULL_BODY = "Full body"

        val ALL = listOf(
            CHEST, BACK, SHOULDERS, TRAPS, TRICEPS, BICEPS, FOREARMS,
            QUADS, HAMSTRINGS, GLUTES, CALVES, CORE, FULL_BODY
        )
    }

    fun all(): List<Exercise> =
        chest() + back() + shoulders() + traps() + triceps() + biceps() + forearms() +
            quads() + hamstrings() + glutes() + calves() + core() + fullBody() +
            SeedExercisesMore.all()

    // ------------------------------------------------------------------ chest

    private fun chest(): List<Exercise> = listOf(
        // Barbell
        chest("Barbell bench press", 4, 8),
        chest("Incline barbell bench press", 5, 8),
        chest("Decline barbell bench press", 6, 10),
        chest("Close-grip barbell bench press", 6, 10),
        chest("Wide-grip barbell bench press", 6, 10),
        chest("Barbell floor press", 5, 8),
        chest("Spoto press", 5, 8),
        chest("Paused barbell bench press", 3, 6),
        chest("Board press", 3, 6),
        chest("Reverse-grip barbell bench press", 8, 12),
        chest("Guillotine press", 8, 12),
        // Dumbbell
        chest("Flat dumbbell press", 6, 12, 2.0),
        chest("Incline dumbbell press", 6, 12, 2.0),
        chest("Decline dumbbell press", 8, 12, 2.0),
        chest("Neutral-grip dumbbell press", 8, 12, 2.0),
        chest("Dumbbell floor press", 8, 12, 2.0),
        chest("Dumbbell squeeze press", 10, 15, 2.0),
        chest("Flat dumbbell fly", 10, 15, 2.0),
        chest("Incline dumbbell fly", 10, 15, 2.0),
        chest("Decline dumbbell fly", 10, 15, 2.0),
        chest("Dumbbell pullover", 10, 15, 2.0),
        // Machine
        chest("Machine chest press", 8, 12),
        chest("Incline machine chest press", 8, 12),
        chest("Decline machine chest press", 8, 12),
        chest("Hammer strength chest press", 8, 12),
        chest("Iso-lateral machine press", 8, 12),
        chest("Converging chest press machine", 8, 12),
        chest("Pec deck", 10, 15),
        chest("Machine chest fly", 10, 15),
        // Cable
        chest("Cable fly", 10, 15),
        chest("Cable crossover", 10, 15),
        chest("Low-to-high cable crossover", 12, 15),
        chest("High-to-low cable crossover", 12, 15),
        chest("Flat bench cable fly", 10, 15),
        chest("Incline bench cable fly", 10, 15),
        chest("Standing cable chest press", 10, 15),
        chest("Single-arm cable chest press", 10, 15),
        chest("Cable pullover", 12, 15),
        // Smith
        chest("Smith machine bench press", 6, 10),
        chest("Smith machine incline press", 6, 10),
        chest("Smith machine decline press", 8, 12),
        // Bodyweight
        chest("Push-up", 10, 25, 2.5, bw = true),
        chest("Incline push-up", 12, 25, 2.5, bw = true),
        chest("Decline push-up", 10, 20, 2.5, bw = true),
        chest("Wide-grip push-up", 10, 20, 2.5, bw = true),
        chest("Archer push-up", 6, 12, 2.5, bw = true),
        chest("Deficit push-up", 8, 15, 2.5, bw = true),
        chest("Clap push-up", 5, 12, 2.5, bw = true),
        chest("Ring push-up", 8, 15, 2.5, bw = true),
        chest("Chest dip", 6, 12, 2.5, bw = true),
        chest("Weighted chest dip", 5, 10, 2.5, bw = true),
        chest("Svend press", 12, 20, 1.0)
    )

    // ------------------------------------------------------------------- back

    private fun back(): List<Exercise> = listOf(
        // Deadlift family
        back("Deadlift", 3, 6, 5.0),
        back("Sumo deadlift", 3, 6, 5.0),
        back("Trap bar deadlift", 4, 8, 5.0),
        back("Deficit deadlift", 3, 6, 5.0),
        back("Snatch-grip deadlift", 3, 6, 5.0),
        back("Rack pull", 4, 8, 5.0),
        back("Paused deadlift", 3, 5, 5.0),
        // Barbell rows
        back("Barbell row", 5, 8),
        back("Pendlay row", 4, 8),
        back("Yates row", 6, 10),
        back("Underhand barbell row", 6, 10),
        back("T-bar row", 6, 10),
        back("Chest-supported T-bar row", 8, 12),
        back("Seal row", 8, 12),
        back("Landmine row", 8, 12),
        back("Meadows row", 8, 12),
        // Dumbbell
        back("Dumbbell row", 8, 12, 2.0),
        back("Chest-supported dumbbell row", 8, 12, 2.0),
        back("Two-arm dumbbell row", 8, 12, 2.0),
        back("Kroc row", 12, 20, 2.0),
        back("Dumbbell seal row", 10, 15, 2.0),
        back("Incline dumbbell row", 8, 12, 2.0),
        // Pulldowns
        back("Lat pulldown", 8, 12),
        back("Wide-grip lat pulldown", 8, 12),
        back("Close-grip lat pulldown", 8, 12),
        back("Neutral-grip lat pulldown", 8, 12),
        back("Reverse-grip lat pulldown", 8, 12),
        back("Single-arm lat pulldown", 10, 15),
        back("Kneeling cable pulldown", 10, 15),
        back("Behind-the-neck pulldown", 10, 15),
        // Cable rows
        back("Seated cable row", 8, 12),
        back("Wide-grip seated cable row", 8, 12),
        back("Single-arm cable row", 10, 15),
        back("Cable high row", 10, 15),
        back("Cable low row", 8, 12),
        back("Straight-arm pulldown", 12, 15),
        back("Cable rope pullover", 12, 15),
        // Machine
        back("Chest-supported machine row", 8, 12),
        back("Hammer strength row", 8, 12),
        back("Iso-lateral pulldown", 8, 12),
        back("Machine pullover", 10, 15),
        back("Assisted pull-up machine", 8, 12),
        back("Low row machine", 8, 12),
        // Bodyweight
        back("Pull-up", 5, 12, 2.5, bw = true),
        back("Chin-up", 5, 12, 2.5, bw = true),
        back("Neutral-grip pull-up", 5, 12, 2.5, bw = true),
        back("Wide-grip pull-up", 4, 10, 2.5, bw = true),
        back("Weighted pull-up", 3, 8, 2.5, bw = true),
        back("Inverted row", 8, 15, 2.5, bw = true),
        back("Ring row", 8, 15, 2.5, bw = true),
        back("Muscle-up", 2, 6, 2.5, bw = true),
        back("Back extension", 10, 20, 2.5, bw = true),
        back("Reverse hyperextension", 10, 20, 2.5, bw = true)
    )

    // -------------------------------------------------------------- shoulders

    private fun shoulders(): List<Exercise> = listOf(
        // Presses
        delt("Overhead press", 4, 8, 2.5),
        delt("Seated barbell overhead press", 5, 8, 2.5),
        delt("Push press", 3, 6, 2.5),
        delt("Behind-the-neck press", 8, 12, 2.5),
        delt("Z press", 6, 10, 2.5),
        delt("Dumbbell shoulder press", 6, 12, 2.0),
        delt("Seated dumbbell shoulder press", 8, 12, 2.0),
        delt("Arnold press", 8, 12, 2.0),
        delt("Neutral-grip dumbbell shoulder press", 8, 12, 2.0),
        delt("Machine shoulder press", 8, 12, 2.5),
        delt("Smith machine shoulder press", 6, 10, 2.5),
        delt("Landmine shoulder press", 8, 12, 2.5),
        delt("Viking press", 6, 10, 2.5),
        // Lateral
        delt("Lateral raise"),
        delt("Seated lateral raise"),
        delt("Cable lateral raise", 12, 18),
        delt("Machine lateral raise", 12, 18, 2.5),
        delt("Leaning cable lateral raise", 12, 18),
        delt("Lying incline lateral raise", 12, 18),
        delt("Powell raise", 12, 18),
        delt("Dumbbell upright row", 10, 15, 2.0),
        delt("Cable upright row", 12, 15),
        delt("Barbell upright row", 10, 15, 2.5),
        // Front
        delt("Dumbbell front raise"),
        delt("Barbell front raise", 10, 15, 2.5),
        delt("Cable front raise", 12, 15),
        delt("Plate front raise", 10, 15, 2.5),
        // Rear
        delt("Rear delt fly", 12, 18),
        delt("Seated rear delt fly", 12, 18),
        delt("Cable rear delt fly", 12, 18),
        delt("Reverse pec deck", 12, 18, 2.5),
        delt("Face pull", 12, 20),
        delt("Rope face pull to external rotation", 12, 20),
        delt("Bent-over reverse fly", 12, 18),
        delt("Cable Y-raise", 12, 18),
        // Rotator and accessory
        delt("Cable external rotation", 15, 20),
        delt("Dumbbell external rotation", 15, 20),
        delt("Cuban press", 10, 15),
        delt("Scaption raise", 12, 18),
        delt("Six-way shoulder raise", 8, 12),
        // Bodyweight
        delt("Pike push-up", 8, 15, 2.5, bw = true),
        delt("Handstand push-up", 3, 10, 2.5, bw = true),
        delt("Wall walk", 3, 8, 2.5, bw = true)
    )

    // ------------------------------------------------------------------ traps

    private fun traps(): List<Exercise> = listOf(
        trap("Barbell shrug"),
        trap("Dumbbell shrug", 10, 15, 2.0),
        trap("Cable shrug"),
        trap("Smith machine shrug"),
        trap("Trap bar shrug", 10, 15, 5.0),
        trap("Behind-the-back barbell shrug"),
        trap("Machine shrug"),
        trap("Incline dumbbell shrug", 12, 20, 2.0),
        trap("Power shrug", 6, 10, 5.0),
        trap("Kirk shrug", 8, 12),
        trap("Snatch-grip high pull", 5, 8, 5.0),
        trap("Barbell high pull", 6, 10, 5.0)
    )

    // ---------------------------------------------------------------- triceps

    private fun triceps(): List<Exercise> = listOf(
        // Cable — the biggest family, and the one most people want variations of
        tri("Triceps pushdown", 10, 15, 2.5),
        tri("Rope triceps pushdown", 10, 15, 2.5),
        tri("Straight-bar triceps pushdown", 10, 15, 2.5),
        tri("V-bar triceps pushdown", 10, 15, 2.5),
        tri("Reverse-grip triceps pushdown", 12, 15, 2.5),
        tri("Single-arm triceps pushdown", 12, 15, 2.5),
        tri("Cable overhead triceps extension", 10, 15, 2.5),
        tri("Cable rope overhead triceps extension", 10, 15, 2.5),
        tri("Single-arm overhead cable extension", 12, 15, 2.5),
        tri("Cable triceps kickback", 12, 20, 2.5),
        tri("Cross-body cable extension", 12, 15, 2.5),
        tri("Cable skull crusher", 10, 15, 2.5),
        tri("Bent-over cable extension", 12, 15, 2.5),
        // Dumbbell
        tri("Overhead triceps extension", 10, 15, 2.0),
        tri("Single-arm overhead dumbbell extension", 10, 15, 1.0),
        tri("Seated overhead dumbbell extension", 10, 15, 2.0),
        tri("Lying dumbbell skull crusher", 8, 12, 2.0),
        tri("Dumbbell triceps kickback", 12, 20, 1.0),
        tri("Tate press", 10, 15, 2.0),
        tri("Close-grip dumbbell floor press", 8, 12, 2.0),
        tri("Close-grip dumbbell press", 8, 12, 2.0),
        // Barbell / EZ bar
        tri("Skull crusher", 8, 12, 2.0),
        tri("EZ-bar skull crusher", 8, 12, 2.0),
        tri("Incline skull crusher", 10, 15, 2.0),
        tri("Close-grip bench press", 6, 10, 2.5),
        tri("JM press", 6, 10, 2.5),
        tri("French press", 10, 15, 2.0),
        tri("Reverse-grip bench press", 8, 12, 2.5),
        // Machine
        tri("Machine triceps extension", 10, 15, 2.5),
        tri("Machine triceps dip", 8, 12, 2.5),
        tri("Assisted dip machine", 8, 12, 2.5),
        // Bodyweight
        tri("Triceps dip", 6, 12, 2.5, bw = true),
        tri("Bench dip", 10, 20, 2.5, bw = true),
        tri("Ring dip", 6, 12, 2.5, bw = true),
        tri("Diamond push-up", 8, 20, 2.5, bw = true),
        tri("Close-grip push-up", 10, 20, 2.5, bw = true),
        tri("Bodyweight skull crusher", 8, 15, 2.5, bw = true)
    )

    // ----------------------------------------------------------------- biceps

    private fun biceps(): List<Exercise> = listOf(
        // Barbell / EZ
        bi("Barbell curl", 8, 12, 2.0),
        bi("EZ-bar curl", 8, 12, 2.0),
        bi("Wide-grip barbell curl", 8, 12, 2.0),
        bi("Close-grip barbell curl", 8, 12, 2.0),
        bi("Reverse curl", 10, 15),
        bi("Drag curl", 10, 15, 2.0),
        bi("Barbell preacher curl", 10, 15, 2.0),
        bi("Barbell 21s", 21, 21, 2.0),
        // Dumbbell
        bi("Dumbbell curl"),
        bi("Alternating dumbbell curl"),
        bi("Hammer curl"),
        bi("Incline dumbbell curl"),
        bi("Concentration curl", 10, 15),
        bi("Dumbbell preacher curl", 10, 15),
        bi("Zottman curl", 10, 15),
        bi("Cross-body hammer curl"),
        bi("Spider curl", 10, 15),
        bi("Seated dumbbell curl"),
        bi("Dumbbell 21s", 21, 21),
        // Cable
        bi("Cable curl", 10, 15),
        bi("Rope hammer curl", 10, 15),
        bi("High cable curl", 12, 15),
        bi("Single-arm cable curl", 12, 15),
        bi("Cable preacher curl", 10, 15),
        bi("Bayesian cable curl", 10, 15),
        bi("Reverse cable curl", 12, 15),
        bi("Overhead cable curl", 12, 15),
        // Machine and bodyweight
        bi("Machine preacher curl", 10, 15, 2.5),
        bi("Biceps curl machine", 10, 15, 2.5),
        bi("Ring biceps curl", 8, 15, 2.5, bw = true)
    )

    // --------------------------------------------------------------- forearms

    private fun forearms(): List<Exercise> = listOf(
        fore("Barbell wrist curl"),
        fore("Dumbbell wrist curl"),
        fore("Cable wrist curl"),
        fore("Behind-the-back wrist curl"),
        fore("Reverse wrist curl"),
        fore("Reverse barbell wrist curl"),
        fore("Wrist roller", 3, 6),
        fore("Plate pinch hold", 3, 6, 2.5),
        fore("Farmer's hold", 3, 6, 5.0),
        fore("Dead hang", 3, 6, 2.5, bw = true),
        fore("Towel hang", 3, 6, 2.5, bw = true),
        fore("Gripper squeeze", 10, 20, 2.5)
    )

    // ------------------------------------------------------------------ quads

    private fun quads(): List<Exercise> = listOf(
        // Barbell squats
        quad("Back squat", 4, 8),
        quad("High-bar back squat", 5, 8),
        quad("Low-bar back squat", 4, 8),
        quad("Front squat", 4, 8, 2.5),
        quad("Box squat", 4, 8),
        quad("Paused squat", 3, 6),
        quad("Zercher squat", 5, 8),
        quad("Overhead squat", 5, 8, 2.5),
        quad("Safety bar squat", 5, 8),
        quad("Barbell split squat", 8, 12, 2.5),
        quad("Barbell lunge", 8, 12, 2.5),
        quad("Barbell walking lunge", 8, 12, 2.5),
        quad("Barbell reverse lunge", 8, 12, 2.5),
        quad("Barbell step-up", 8, 12, 2.5),
        // Dumbbell
        quad("Goblet squat", 8, 15, 2.0),
        quad("Dumbbell front squat", 8, 12, 2.0),
        quad("Dumbbell lunge", 8, 12, 2.0),
        quad("Walking lunge", 8, 12, 2.0),
        quad("Reverse lunge", 8, 12, 2.0),
        quad("Bulgarian split squat", 8, 12, 2.0),
        quad("Dumbbell step-up", 8, 12, 2.0),
        quad("Dumbbell split squat", 8, 12, 2.0),
        quad("Curtsy lunge", 10, 15, 2.0),
        // Machine
        quad("Leg press", 8, 15),
        quad("Horizontal leg press", 10, 15),
        quad("Single-leg press", 10, 15),
        quad("Hack squat", 8, 12),
        quad("Reverse hack squat", 8, 12),
        quad("Pendulum squat", 8, 12),
        quad("V-squat machine", 8, 12),
        quad("Belt squat", 8, 15),
        quad("Leg extension", 10, 15, 2.5),
        quad("Single-leg extension", 12, 15, 2.5),
        quad("Sissy squat machine", 10, 15, 2.5),
        // Smith
        quad("Smith machine squat", 6, 10),
        quad("Smith machine front squat", 8, 12),
        quad("Smith machine split squat", 8, 12, 2.5),
        quad("Smith machine lunge", 8, 12, 2.5),
        // Bodyweight
        quad("Air squat", 15, 30, 2.5, bw = true),
        quad("Jump squat", 8, 15, 2.5, bw = true),
        quad("Pistol squat", 3, 10, 2.5, bw = true),
        quad("Sissy squat", 8, 15, 2.5, bw = true),
        quad("Wall sit", 30, 90, 2.5, bw = true),
        quad("Cossack squat", 8, 12, 2.5, bw = true),
        quad("Skater squat", 6, 12, 2.5, bw = true),
        quad("Step-up", 10, 15, 2.5, bw = true)
    )

    // ------------------------------------------------------------- hamstrings

    private fun hamstrings(): List<Exercise> = listOf(
        ham("Romanian deadlift", 6, 10),
        ham("Barbell stiff-leg deadlift", 6, 10),
        ham("Deficit Romanian deadlift", 8, 12),
        ham("Good morning", 8, 12),
        ham("Seated good morning", 10, 15),
        ham("Dumbbell Romanian deadlift", 8, 12, 2.0),
        ham("Single-leg Romanian deadlift", 8, 12, 2.0),
        ham("B-stance Romanian deadlift", 8, 12, 2.0),
        ham("Smith machine Romanian deadlift", 8, 12),
        ham("Leg curl", 10, 15),
        ham("Lying leg curl", 10, 15),
        ham("Seated leg curl", 10, 15),
        ham("Standing single-leg curl", 12, 15),
        ham("Glute-ham raise", 6, 12, 2.5, bw = true),
        ham("Nordic curl", 5, 10, 2.5, bw = true),
        ham("Slider leg curl", 8, 15, 2.5, bw = true),
        ham("Stability ball leg curl", 10, 20, 2.5, bw = true),
        ham("Cable pull-through", 10, 15),
        ham("Cable Romanian deadlift", 10, 15),
        ham("Kettlebell swing", 12, 20, 4.0)
    )

    // ----------------------------------------------------------------- glutes

    private fun glutes(): List<Exercise> = listOf(
        glute("Hip thrust", 8, 12),
        glute("Barbell glute bridge", 8, 12),
        glute("Single-leg hip thrust", 10, 15, 2.5),
        glute("Machine hip thrust", 8, 12),
        glute("Smith machine hip thrust", 8, 12),
        glute("B-stance hip thrust", 10, 15),
        glute("Cable glute kickback", 12, 20, 2.5),
        glute("Machine glute kickback", 12, 20, 2.5),
        glute("Hip abduction", 12, 20, 2.5),
        glute("Hip adduction", 12, 20, 2.5),
        glute("Cable standing abduction", 15, 20, 2.5),
        glute("Standing glute kickback", 15, 20, 2.5, bw = true),
        glute("Glute bridge", 15, 25, 2.5, bw = true),
        glute("Frog pump", 15, 25, 2.5, bw = true),
        glute("Banded side walk", 15, 25, 2.5, bw = true),
        glute("Fire hydrant", 15, 20, 2.5, bw = true),
        glute("Donkey kick", 15, 20, 2.5, bw = true),
        glute("Bulgarian split squat, glute bias", 8, 12, 2.0)
    )

    // ----------------------------------------------------------------- calves

    private fun calves(): List<Exercise> = listOf(
        calf("Standing calf raise"),
        calf("Seated calf raise", 12, 20),
        calf("Machine standing calf raise"),
        calf("Smith machine calf raise"),
        calf("Barbell calf raise"),
        calf("Dumbbell calf raise", 12, 20, 2.0),
        calf("Leg press calf press", 12, 20, 5.0),
        calf("Donkey calf raise", 12, 20),
        calf("Single-leg calf raise", 12, 20, 2.0),
        calf("Bodyweight calf raise", 15, 30, 2.5, bw = true),
        calf("Tibialis raise", 15, 25, 2.5),
        calf("Seated tibialis raise", 15, 25, 2.5)
    )

    // ------------------------------------------------------------------- core

    private fun core(): List<Exercise> = listOf(
        // Cable and machine
        core("Cable crunch", 10, 20),
        core("Kneeling cable crunch", 10, 20),
        core("Standing cable crunch", 12, 20),
        core("Cable woodchop, high to low", 12, 15),
        core("Cable woodchop, low to high", 12, 15),
        core("Pallof press", 10, 15),
        core("Cable side bend", 12, 20),
        core("Ab crunch machine", 12, 20),
        core("Rotary torso machine", 12, 20),
        // Weighted
        core("Weighted sit-up", 10, 20),
        core("Decline weighted sit-up", 10, 20),
        core("Russian twist", 12, 20, 2.0),
        core("Landmine 180", 10, 16, 2.5),
        core("Suitcase carry", 3, 6, 5.0),
        core("Weighted plank", 30, 90),
        core("Dumbbell side bend", 12, 20, 2.0),
        core("Weighted hanging leg raise", 8, 15),
        // Hanging and bar
        core("Hanging leg raise", 8, 15, 2.5, bw = true),
        core("Hanging knee raise", 10, 20, 2.5, bw = true),
        core("Toes to bar", 6, 15, 2.5, bw = true),
        core("Captain's chair leg raise", 10, 20, 2.5, bw = true),
        core("Windshield wiper", 6, 12, 2.5, bw = true),
        // Bodyweight
        core("Plank", 30, 90, 2.5, bw = true),
        core("Side plank", 30, 90, 2.5, bw = true),
        core("RKC plank", 10, 30, 2.5, bw = true),
        core("Hollow body hold", 20, 60, 2.5, bw = true),
        core("Dead bug", 10, 20, 2.5, bw = true),
        core("Bird dog", 10, 20, 2.5, bw = true),
        core("Crunch", 15, 30, 2.5, bw = true),
        core("Sit-up", 12, 25, 2.5, bw = true),
        core("Bicycle crunch", 15, 30, 2.5, bw = true),
        core("Reverse crunch", 12, 20, 2.5, bw = true),
        core("V-up", 10, 20, 2.5, bw = true),
        core("Ab wheel rollout", 8, 15, 2.5, bw = true),
        core("Dragon flag", 3, 10, 2.5, bw = true),
        core("L-sit", 10, 40, 2.5, bw = true),
        core("Mountain climber", 20, 40, 2.5, bw = true),
        core("Flutter kick", 20, 40, 2.5, bw = true),
        core("Scissor kick", 20, 40, 2.5, bw = true),
        core("Copenhagen plank", 15, 45, 2.5, bw = true)
    )

    // -------------------------------------------------------------- full body

    private fun fullBody(): List<Exercise> = listOf(
        full("Clean and jerk"),
        full("Power clean"),
        full("Hang clean"),
        full("Clean pull", 3, 5),
        full("Snatch"),
        full("Power snatch"),
        full("Hang snatch"),
        full("Snatch pull", 3, 5),
        full("Push jerk"),
        full("Split jerk"),
        full("Thruster", 6, 12, 2.5),
        full("Barbell complex", 6, 10, 2.5),
        full("Kettlebell clean and press", 6, 12, 4.0),
        full("Turkish get-up", 3, 6, 4.0),
        full("Farmer's carry", 3, 6),
        full("Sled push", 3, 6, 10.0),
        full("Sled drag", 3, 6, 10.0),
        full("Burpee", 10, 20, 2.5, bw = true),
        full("Bear crawl", 10, 20, 2.5, bw = true)
    )

    /**
     * Starter splits, as name and the lifts on them in order.
     *
     * Referenced by exercise *name* rather than id because ids are assigned by the
     * database on insert and do not exist when this list is written. The repository
     * resolves them once, after the lifts are in. SeedDataTest checks every name
     * here against the catalogue, because a typo would otherwise silently drop the
     * lift from the split with nothing to complain.
     */
    fun splits(): List<Pair<String, List<String>>> = listOf(
        "Push" to listOf(
            "Barbell bench press", "Incline dumbbell press", "Overhead press",
            "Cable fly", "Lateral raise", "Triceps pushdown"
        ),
        "Pull" to listOf(
            "Barbell row", "Lat pulldown", "Seated cable row",
            "Face pull", "Barbell curl", "Hammer curl"
        ),
        "Legs" to listOf(
            "Back squat", "Romanian deadlift", "Leg press",
            "Leg curl", "Leg extension", "Standing calf raise"
        ),
        "Upper" to listOf(
            "Barbell bench press", "Barbell row", "Overhead press", "Lat pulldown",
            "Lateral raise", "Barbell curl", "Triceps pushdown"
        ),
        "Lower" to listOf(
            "Back squat", "Romanian deadlift", "Leg press", "Leg curl",
            "Hip thrust", "Standing calf raise", "Hanging leg raise"
        ),
        "Full body" to listOf(
            "Back squat", "Barbell bench press", "Barbell row",
            "Overhead press", "Romanian deadlift", "Plank"
        ),
        "Chest and triceps" to listOf(
            "Barbell bench press", "Incline dumbbell press", "Cable crossover",
            "Chest dip", "Skull crusher", "Rope triceps pushdown"
        ),
        "Back and biceps" to listOf(
            "Deadlift", "Pull-up", "T-bar row", "Seated cable row",
            "Barbell curl", "Incline dumbbell curl"
        ),
        "Shoulders and arms" to listOf(
            "Overhead press", "Lateral raise", "Rear delt fly", "Face pull",
            "Hammer curl", "Overhead triceps extension"
        ),
        "Arms" to listOf(
            "Barbell curl", "Incline dumbbell curl", "Hammer curl",
            "Close-grip bench press", "Skull crusher", "Rope triceps pushdown"
        )
    )
}
