# Gym — handoff

Last updated after the Indian food dataset import and the port to Kotlin
Multiplatform with an iPhone build. Supersedes every earlier version.

## Goal

A personal training-and-food log for one phone, offline, no account — now on
**Android and iPhone from one codebase**. Repo `Selva461/Gym` (private), branch
`claude/gym-app-audit-982uq6`, merged to `main` once both platforms build.

Standing constraints, all deliberate and all enforced by tests:

- **No network, ever.** `INTERNET` and `ACCESS_NETWORK_STATE` are stripped from
  the merged manifest with `tools:node="remove"`. Anything the app shows must
  already be inside the app.
- **Nothing licensed from anyone else.** No scraped exercise GIFs.
- **Additive, not destructive.** Every schema change is a hand-written migration;
  no `fallbackToDestructiveMigration`.
- **commonMain is portable.** No `android.*` or `java.*` imports there —
  ArchitectureTest and `tools/check-imports.py` both refuse them.

## Current state

| | |
|---|---|
| Platforms | Android (APK, every push) and iOS (unsigned IPA, on `main` or on demand) — both green |
| Exercises | 486, across 13 muscle groups; 140 with bundled demonstrations |
| Foods | 706 built in (76 Tamil curries in `SeedFoodsTamil.kt`, 92 staples in `SeedFoodsStaples.kt`) + 61 from the Indian Food Nutrition Dataset v2 |
| Database | v13 (v7 food `aliases`/`source`; v8 `exercise.isAssisted`; v9 `body_measurement`; v10 `set_entry.kind`/`rpe`; v11 `exercise.notes`; v12 `saved_meal`, `saved_meal_item`; v13 `cardio_entry`), migrations 1→…→13, Room 2.7 KMP |
| Tests | 234, none needing a device |
| Toolchain | Kotlin 2.1.21, KSP 2.1.21-2.0.2, Compose Multiplatform 1.8.2, AGP 8.10.1, Gradle 8.11.1 |

This phase, in order:

1. **Dataset import** (`data/FoodDataset.kt`, `domain/FoodChecks.kt`). One food
   per dish, not per row. 61 added, 38 already present (some via `SAME_AS`),
   23 declined with reasons (blank macros, self-contradicting energy, and
   deep-fried INDB recipes that count all the frying oil — `dishProblems`).
2. **Features it enabled:** `domain/FoodSearch.kt` (aliases, ranking),
   `domain/FoodEdits.kt` (provenance turns to "Edited from …" when numbers change).
3. **Portability:** `core/Dates.kt` (kotlinx-datetime), `domain/Numbers.kt`
   (String.format replacement, checked digit-for-digit against it).
4. **KMP restructure:** `app/src/{commonMain,androidMain,iosMain}`, seams listed
   in README → Structure. Room with the bundled SQLite driver against the same
   `gym.db`; settings keep their DataStore file; demos and the CSV are Compose
   resources.
5. **iOS:** `iosMain`, `iosApp/` (XcodeGen spec + Swift host), `ios.yml`.

### The one open problem

**The APK will not install over the existing app:** *"App not installed as
package conflicts with an existing package."*

That is a signature mismatch, not a bug in the build. The currently-installed app
was signed by an ephemeral key AGP generated on a CI runner that no longer
exists; every build since `dd89f2c` is signed by the fixed debug key committed at
`app/keystore/debug.keystore`. Android refuses to replace an app with one signed
by a different certificate, and some OEM skins word that refusal exactly this way.

**Fix: uninstall the existing app first, then install.** This loses any data
logged in it — there is no way around that, because the old key is gone. Every
update after this one installs over the top without uninstalling.

Worth ruling out if uninstall-and-reinstall still fails: the app installed under
a second user profile or work profile (Settings → Apps → show system → look for
`dev.avles.gym.debug`), which holds the package name even after the primary
profile uninstalls it.

## Files in flight

**None.** Everything is pushed. `app/schemas/` is committed and CI writes to it.

## Changed

Since the stale handoff, in dependency order:

- **Tier 0 — foundation.** `core/GymLog.kt` (lambda-valued, compiled out of
  release). `ArchitectureTest` enforcing the layering, an 800-line file cap and
  no SQL built by interpolation. `SecurityPostureTest` pinning permissions,
  exported components, obfuscation and signing material. `SECURITY.md`,
  `ARCHITECTURE.md`. Removed `-keepclassmembers class dev.avles.gym.data.** { *; }`,
  which had been exempting every table and column from obfuscation.
- **Tier 1 — `domain/ExerciseGuide.kt`.** Equipment, movement pattern, muscles,
  setup, execution, cues, mistakes and variation axes, all *derived* from the
  exercise row. No storage, no migration, and a lift invented on the phone gets a
  reference the moment it is named.
- **Tier 2 — `ui/components/MuscleMap.kt`** (vector body, front and back, primary
  and secondary shading) and `ui/screens/ExerciseReference.kt`.
- **Tier 3 — schema v4.** `variation` on `set_entry`. Progression and records key
  on (exercise, variation); volume deliberately does not.
- **Tier 4/5 — schema v5.** `progress_photo` and `exercise_photo`, `PhotoStore`,
  `PhotosScreen`. Photos are copied into private storage, re-encoded (which drops
  EXIF GPS), excluded from cloud backup, and need no permission because the
  system photo picker runs out of process.
- **140 bundled demonstrations** from the Free Exercise DB (Unlicense, public
  domain), re-encoded to WebP at 420px. See `CREDITS.md`.
- **`tools/check-imports.py`** — the compiler this sandbox does not have.
- **Room Gradle plugin** replacing `ksp { arg("room.schemaLocation", ...) }`,
  which had been exporting nothing.

## Failed attempts

Read this section before touching CI. Most of it is me.

- **No Android SDK in this container.** `dl.google.com` is proxy-blocked, so is
  Google's Maven and the Azure host serving raw CI logs. Compose and Room can
  only be compiled by CI. The workaround is `scratchpad/verify.sh`, which
  compiles the pure-Kotlin layers with `kotlin-compiler-embeddable` from
  `repo1.maven.org` (reachable) and runs JUnit. **Do not try to install the SDK.**
- **`ksp { arg("room.schemaLocation", ...) }` exports nothing.** This was the
  root cause of a seven-push detour. Use the Room Gradle plugin.
- **`MigrationSqlTest` had never run.** It looked under
  `dev.avles.gym.GymDatabase/` while the class is `dev.avles.gym.data.GymDatabase`,
  and the file did not exist at either path anyway. `assumeTrue` turned that into
  a skip, and a skipped test is a green build. It reported success for weeks while
  checking nothing.
- **Three CI failures in a row were missing imports from moving code between
  files** — `Slate`, `Training`, `clickable`, `getValue`. That is the 800-line
  cap's second-order cost. `tools/check-imports.py` now covers it, including the
  lowercase cases (delegate operators, Modifier extensions) it was originally
  blind to.
- **Scripted multi-file edits need the same care as the code they produce.** Three
  consecutive CI failures in phase 3 were mechanical: a property named
  `settingsStore` referenced as `settings`, and a `str.replace` with no count
  anchored on `...domain.Physique`, which is a prefix of `...domain.Physiques` and
  so inserted an import twice. Neither was a design error; both cost a full cycle.
- **Instrumentation I added caused six consecutive red builds while the app was
  green throughout**: a watcher on `if: always()` that masked the real compile
  error; a `bash -e` command substitution that killed a diagnostic before it
  printed; a commit-back step ordered *after* the watcher, so the one run whose
  evidence mattered was the one that skipped it. The lesson, learned expensively:
  when you cannot see, put the artifact somewhere readable rather than building
  more machinery to peer at it.
- **`--stacktrace` on the test step hides compile errors.** It prints 200 lines
  of Gradle internals that push the `e: file:...` lines out of tail range, and the
  tail is all the API returns. Removed; a failure-only step now repeats the real
  errors as the last thing in the log.
- **The APK cannot be handed over as a file.** Artifact and release downloads are
  both proxy-blocked. The release-asset link is the only delivery route.

- **AGP 8.7's lint crashes on Compose 1.8 / lifecycle 2.9** ("Found class
  KaCallableMemberCall, but interface was expected"). The libraries ship lint
  checks built for a newer lint API. AGP 8.10.1 (Gradle 8.11.1) fixed it.
- **The first four iOS builds failed on setup, not on the app.** KSP (Room's code
  generator) runs before Gradle downloads the Kotlin/Native toolchain, so on a
  fresh Mac runner it analysed without a native standard library: KSP2 said
  "cannot find required type kotlin.coroutines.Continuation", KSP1 said "Can't
  find env_blacklist file". Switching KSP versions and reshaping the DAO were
  both wrong turns. The fix is `ios.yml`'s "Provision the Kotlin/Native
  toolchain" step. Keep it.
- **The only real iOS compile error was `MutableMap.merge`** — java.util's,
  reachable on Android. The checker and ArchitectureTest now reject it.
- **No iOS toolchain here, and none reachable.** download.jetbrains.com and
  Google's Maven are proxy-blocked, so iOS code is checked only by `ios.yml`
  on a Mac runner. Kept deliberately to members of ObjC classes rather than
  category methods (which K/N imports as extensions needing their own imports)
  to cut down on blind guesses.

## Phase 3 additions worth knowing

- **`domain/Variations.kt`** — grip, angle and tempo are independent axes encoded
  into one column, sorted by axis name. Progression keys on that string, so the
  ordering is load-bearing rather than tidy.
- **`domain/Physique.kt` / `domain/Coach.kt`** — goals are *data*. Adding one is
  an entry in `Physiques.ALL`; no logic, no screen. A set counts fully for what a
  lift is for and half for what it also works. Only the muscles a lift is *for*
  can be suggested for it.
- **`domain/Patterns.kt`** — an unlogged day is a gap, never a zero, all the way
  down to the SQL. Adherence divides by days logged rather than days elapsed.
- **`SeedFoodsMore.kt` / `SeedExercisesMore.kt`** — the catalogue outgrew its
  files. Third files when these fill; `SeedData` and `SeedExercises` concatenate.

## Next step

**Install on both phones.**

- Android: uninstall any old copy signed before `dd89f2c` (signature mismatch),
  then install https://github.com/Selva461/Gym/releases/download/latest-debug/gym-debug.apk
- iPhone: https://github.com/Selva461/Gym/releases/download/latest-ios/gym-unsigned.ipa
  through Sideloadly or AltStore with the user's Apple ID (README → Installing on
  an iPhone). Free Apple ID: re-sign every 7 days.

Then, in order of value:

- Run the manual pass (REQUIREMENTS.md §9) on the iPhone, which has never been
  run on a device: photo picking, the rest alert with the app backgrounded, the
  evening reminder, and that the Indian dataset seeded (search "aloo").
- If the user gets a paid Apple Developer account: sign in CI (certificate +
  provisioning profile as secrets, `xcodebuild archive` + `-exportArchive`).
- The two security gaps in `SECURITY.md` (biometric lock, SQLCipher) are still
  open questions for the user.

## Backlog: parity with the big fitness apps

The user asked for the app to match Strong, Hevy, MyFitnessPal and HealthifyMe
"in everything possible", and for the work to keep going across sessions. Done
so far: motion and haptics, previous-session chips, plate calculator, session
clock, rest ±15, lift history and rep-max table, workout summary card, macro
split bars, water tracker, copy a meal from the day before, target calculator.

Take these top to bottom. Each needs domain logic with tests first, then UI;
verify with the local harness and `gradle :app:compileDebugKotlinAndroid`
(Android SDK at /opt/android-sdk; Maven Central rate-limits the sandbox, so
`~/.gradle/init.d/mirror.gradle.kts` points it at Google's Central mirror)
before pushing. Strike an item when it lands.

1. ~~**Warm-up sets**~~ (done) from the working weight (bar × 10, 40% × 5, 60% × 3, 80% × 2),
   rounded to plates, offered under the target card. Pure; no schema.
2. ~~**Month calendar**~~ (done) on Progress with trained/fuelled marks and rest days. No schema.
3. ~~**All records** page~~ (done) on Progress: each lift's best set, newest records first.
   Needs one DAO query over all working sets (read-only, no schema change).
4. ~~**This week** card~~ (done) on Today: sessions, volume, average protein and calories
   against target, versus last week. No schema.
5. ~~**Recent foods**~~ (done) next to the usual suspects: the last distinct foods logged.
   A DAO query; no schema.
6. ~~**Fasting timer**~~ (done) (16:8 and the like) on Fuel, start time in DataStore.
7. ~~**Export to CSV**~~ (done) of sets, food log and weights through the platform share
   sheet (a new expect/actual seam; no network involved).
8. ~~**Body measurements**~~ (done) (waist, chest, arms, hips, thighs) with trends, and a
   US Navy body-fat estimate. Room v9: new table, migration, MigrationSqlTest.
9. ~~**Set types and RPE**~~ (done) (warm-up exists; add drop set, to failure, RPE 6–10).
   Room v9/v10 column with a default, migration and test.
10. ~~**Notes on a lift**~~ (done) that show every time it is picked. Schema column.

Left out on purpose, because they need the network or another app's permission:
barcode lookup, step counts, Health Connect / HealthKit sync, social feeds.

### Backlog, second wave

11. ~~**Achievements**~~ (done) on Progress: workouts logged (1/10/50/100/250), training
    runs (7/30 days), total kilos moved (10k/100k/1M), records set, food logged
    days. Computed from data already stored; no schema.
12. ~~**Smart rest defaults**~~ (done): a heavy compound (top of range ≤ 6) rests 3 min, an
    isolation lift (≥ 12) 90 s, the rest the Settings value. No schema.
13. ~~**1RM calculator**~~ (done) on Progress: weight × reps → estimated max and table.
14. ~~**Weight goal**~~ (done) in Settings with the trend's weekly rate and a projected date.
15. ~~**Saved meals**~~ (done): name a meal and log all of it in one tap. Room v12 table.

### Backlog, third wave

16. ~~**Alternatives**~~ (done) for a lift: same muscle, different equipment, for when the
    machine is taken. From the catalogue; no schema.
17. ~~**Save a past session as a split**~~ (done) from the session dialog.
18. ~~**First-run welcome**~~ (done) on Today: set targets (with the calculator) before the
    first log, instead of silently using the defaults.
19. ~~**Supersets**~~ (done): pair two lifts so the rest timer waits for the second.

### Backlog, fourth wave

20. ~~**Calories burned**~~ (done) for a session (MET, by duration and body weight) on the
    workout card and Today, as the food apps add exercise.
21. ~~**Cardio**~~ (done): log minutes and distance for cardio work (Room v13).
22. ~~**Quick weigh-in**~~ (done) from Today.
23. ~~**Water reminders**~~ (done) through the existing notifier, during waking hours.

### Backlog, fifth wave

24. ~~**Quick add**~~ (done): calories and macros without a food.
25. **Backup and restore**: the whole database to a file and back (share sheet
    out; a document picker in, which needs a new platform seam).
26. **Tamil labels** for the main screens, as the Indian diet apps offer.
