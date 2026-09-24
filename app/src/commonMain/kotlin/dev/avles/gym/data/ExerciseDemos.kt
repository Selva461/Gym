package dev.avles.gym.data

/**
 * Which lifts have a bundled demonstration, and the asset each one uses.
 *
 * The pictures come from the Free Exercise DB (github.com/yuhonas/free-exercise-db),
 * which is released into the public domain under the Unlicense. That licence is why
 * they are here: the commercial exercise animations people usually mean are somebody
 * else's product, and this app will not ship those. Public domain work it can.
 *
 * Two frames per lift — the start and the end of the movement — alternated on a
 * timer. That is what almost all exercise GIFs actually are, and two WebP stills at
 * 420px cost about 25 KB where an animated GIF of the same thing costs ten times
 * that. All 140 lifts together come to roughly 3.6 MB in the APK, which is the whole
 * reason this could be bundled at all: the app has no network permission and never
 * will, so anything it shows has to already be inside it.
 *
 * Only 140 of the 394 catalogue lifts are here. That is deliberate. Matching was done
 * on name, and a match was only accepted when the equipment, angle, grip and the
 * movement noun all agreed — "Barbell row" scores well against "Upright Barbell Row"
 * and is a completely different exercise. A lift with no confident match shows the
 * muscle map and the written guide instead, which is a smaller loss than showing
 * somebody the wrong movement and letting them learn it.
 *
 * Generated, not hand-written. Do not edit by hand.
 */
internal object ExerciseDemos {

    /** Exercise name to asset stem. Frames are `demo/<stem>-0.webp` and `-1.webp`. */
    val BY_NAME: Map<String, String> = mapOf(
        "Ab crunch machine" to "ab-crunch-machine",
        "Back extension" to "back-extension",
        "Barbell bench press" to "barbell-bench-press",
        "Barbell curl" to "barbell-curl",
        "Barbell glute bridge" to "barbell-glute-bridge",
        "Barbell lunge" to "barbell-lunge",
        "Barbell shrug" to "barbell-shrug",
        "Barbell step-up" to "barbell-step-up",
        "Barbell upright row" to "barbell-upright-row",
        "Barbell walking lunge" to "barbell-walking-lunge",
        "Behind-the-back barbell shrug" to "behind-the-back-barbell-shrug",
        "Bench dip" to "bench-dip",
        "Board press" to "board-press",
        "Box squat" to "box-squat",
        "Cable crossover" to "cable-crossover",
        "Cable crunch" to "cable-crunch",
        "Cable external rotation" to "cable-external-rotation",
        "Cable front raise" to "cable-front-raise",
        "Cable overhead triceps extension" to "cable-overhead-triceps-extension",
        "Cable preacher curl" to "cable-preacher-curl",
        "Cable rear delt fly" to "cable-rear-delt-fly",
        "Cable rope overhead triceps extension" to "cable-rope-overhead-triceps-extension",
        "Cable shrug" to "cable-shrug",
        "Cable upright row" to "cable-upright-row",
        "Cable wrist curl" to "cable-wrist-curl",
        "Chest dip" to "chest-dip",
        "Chin-up" to "chin-up",
        "Clean and jerk" to "clean-and-jerk",
        "Clean pull" to "clean-pull",
        "Close-grip barbell bench press" to "close-grip-barbell-bench-press",
        "Close-grip dumbbell floor press" to "close-grip-dumbbell-floor-press",
        "Close-grip dumbbell press" to "close-grip-dumbbell-press",
        "Concentration curl" to "concentration-curl",
        "Converging chest press machine" to "converging-chest-press-machine",
        "Cross-body hammer curl" to "cross-body-hammer-curl",
        "Crunch" to "crunch",
        "Cuban press" to "cuban-press",
        "Dead bug" to "dead-bug",
        "Decline barbell bench press" to "decline-barbell-bench-press",
        "Decline dumbbell fly" to "decline-dumbbell-fly",
        "Decline machine chest press" to "decline-machine-chest-press",
        "Decline push-up" to "decline-push-up",
        "Deficit deadlift" to "deficit-deadlift",
        "Donkey calf raise" to "donkey-calf-raise",
        "Drag curl" to "drag-curl",
        "Dumbbell calf raise" to "dumbbell-calf-raise",
        "Dumbbell floor press" to "dumbbell-floor-press",
        "Dumbbell front raise" to "dumbbell-front-raise",
        "Dumbbell lunge" to "dumbbell-lunge",
        "Dumbbell shoulder press" to "dumbbell-shoulder-press",
        "Dumbbell shrug" to "dumbbell-shrug",
        "Dumbbell side bend" to "dumbbell-side-bend",
        "Dumbbell split squat" to "dumbbell-split-squat",
        "Dumbbell step-up" to "dumbbell-step-up",
        "EZ-bar curl" to "ez-bar-curl",
        "Face pull" to "face-pull",
        "Flat bench cable fly" to "flat-bench-cable-fly",
        "Glute-ham raise" to "glute-ham-raise",
        "Goblet squat" to "goblet-squat",
        "Good morning" to "good-morning",
        "Hack squat" to "hack-squat",
        "Hammer curl" to "hammer-curl",
        "Handstand push-up" to "handstand-push-up",
        "Hang clean" to "hang-clean",
        "Hang snatch" to "hang-snatch",
        "Hanging leg raise" to "hanging-leg-raise",
        "High cable curl" to "high-cable-curl",
        "Incline barbell bench press" to "incline-barbell-bench-press",
        "Incline bench cable fly" to "incline-bench-cable-fly",
        "Incline dumbbell curl" to "incline-dumbbell-curl",
        "Incline dumbbell fly" to "incline-dumbbell-fly",
        "Incline dumbbell press" to "incline-dumbbell-press",
        "Incline dumbbell row" to "incline-dumbbell-row",
        "Incline machine chest press" to "incline-machine-chest-press",
        "Incline push-up" to "incline-push-up",
        "Inverted row" to "inverted-row",
        "JM press" to "jm-press",
        "Leg extension" to "leg-extension",
        "Leg press" to "leg-press",
        "Lying leg curl" to "lying-leg-curl",
        "Machine chest press" to "machine-chest-press",
        "Machine preacher curl" to "machine-preacher-curl",
        "Machine shoulder press" to "machine-shoulder-press",
        "Machine shrug" to "machine-shrug",
        "Machine triceps extension" to "machine-triceps-extension",
        "Muscle-up" to "muscle-up",
        "Overhead cable curl" to "overhead-cable-curl",
        "Overhead squat" to "overhead-squat",
        "Pallof press" to "pallof-press",
        "Paused barbell bench press" to "paused-barbell-bench-press",
        "Plank" to "plank",
        "Plate front raise" to "plate-front-raise",
        "Power clean" to "power-clean",
        "Power snatch" to "power-snatch",
        "Push press" to "push-press",
        "Reverse cable curl" to "reverse-cable-curl",
        "Reverse crunch" to "reverse-crunch",
        "Reverse hyperextension" to "reverse-hyperextension",
        "Reverse-grip triceps pushdown" to "reverse-grip-triceps-pushdown",
        "Ring dip" to "ring-dip",
        "Romanian deadlift" to "romanian-deadlift",
        "Russian twist" to "russian-twist",
        "Scissor kick" to "scissor-kick",
        "Seated cable row" to "seated-cable-row",
        "Seated calf raise" to "seated-calf-raise",
        "Seated dumbbell curl" to "seated-dumbbell-curl",
        "Seated dumbbell shoulder press" to "seated-dumbbell-shoulder-press",
        "Seated leg curl" to "seated-leg-curl",
        "Single-leg extension" to "single-leg-extension",
        "Sit-up" to "sit-up",
        "Sled push" to "sled-push",
        "Smith machine bench press" to "smith-machine-bench-press",
        "Smith machine calf raise" to "smith-machine-calf-raise",
        "Smith machine decline press" to "smith-machine-decline-press",
        "Smith machine incline press" to "smith-machine-incline-press",
        "Smith machine squat" to "smith-machine-squat",
        "Snatch" to "snatch",
        "Snatch pull" to "snatch-pull",
        "Snatch-grip deadlift" to "snatch-grip-deadlift",
        "Spider curl" to "spider-curl",
        "Split jerk" to "split-jerk",
        "Stability ball leg curl" to "stability-ball-leg-curl",
        "Standing cable chest press" to "standing-cable-chest-press",
        "Standing calf raise" to "standing-calf-raise",
        "Straight-arm pulldown" to "straight-arm-pulldown",
        "Sumo deadlift" to "sumo-deadlift",
        "Svend press" to "svend-press",
        "Tate press" to "tate-press",
        "Trap bar deadlift" to "trap-bar-deadlift",
        "Triceps dip" to "triceps-dip",
        "Triceps pushdown" to "triceps-pushdown",
        "V-bar triceps pushdown" to "v-bar-triceps-pushdown",
        "Weighted hanging leg raise" to "weighted-hanging-leg-raise",
        "Weighted pull-up" to "weighted-pull-up",
        "Wide-grip barbell bench press" to "wide-grip-barbell-bench-press",
        "Wide-grip lat pulldown" to "wide-grip-lat-pulldown",
        "Wide-grip push-up" to "wide-grip-push-up",
        "Wrist roller" to "wrist-roller",
        "Zercher squat" to "zercher-squat",
        "Zottman curl" to "zottman-curl"
    )

    /** Null when this lift has no bundled demonstration, which most do not. */
    fun assetStemFor(exerciseName: String): String? = BY_NAME[exerciseName]
}
