# Gym — handoff

**Start here.** A new session should be able to continue from this file alone.
It is kept current by rule: every commit that changes code, data, the build or
the plan also updates this file (see `CLAUDE.md`). Last updated with the
protein powder brands and the list of further additions, then the "Export everything for AI" file,
then the **week plan** (Excel template → import → Today card and calendar), then
**training-only plans that create splits, with "Start today"** (branch
`claude/plan-splits-start-today`, built on the week-plan branch).

## What this is

A personal training-and-food log, offline, no account, on **Android and iPhone
from one Kotlin Multiplatform codebase**. Repo `Selva461/Gym` (private). The
user eats mostly Tamil Nadu food and trains in a commercial gym; feature
requests have been "match Strong / Hevy / MyFitnessPal / HealthifyMe" and "add
the foods I actually eat".

Standing constraints, all enforced by tests:

- **No network, ever.** `INTERNET` / `ACCESS_NETWORK_STATE` are stripped with
  `tools:node="remove"`. Everything the app shows ships inside it.
- **Nothing licensed from anyone else.** No scraped exercise GIFs.
- **Additive schema changes only.** Hand-written migrations in `SchemaSql.kt`,
  checked against Room's exported JSON by `MigrationSqlTest`; never
  `fallbackToDestructiveMigration`.
- **commonMain is portable.** No `android.*` / `java.*` there (ArchitectureTest,
  `tools/check-imports.py`). Files capped at 800 lines (ArchitectureTest).
- Never commit a release keystore. `app/keystore/debug.keystore` (storepass and
  keypass `android`, alias `androiddebugkey`) is committed on purpose so every
  build installs over the last.

## How code reaches the phone (the pipelines)

| | |
|---|---|
| Source of truth | `main`. Work happens on a `claude/...` branch, then merges to `main`. |
| Working branch | `claude/gym-app-audit-982uq6` (kept merged with `main`). The AI export was built on `claude/project-thread-gzz3wl`; the week plan on `claude/project-thread-kr2wuy`. Another session used `claude/project-thread-dhxbg7`, merged as PR #1. |
| Android CI | `.github/workflows/android.yml` on **every push and PR, any branch**: unit tests, lint, `assembleDebug`, then **replaces the APK on the `latest-debug` release**. So the APK link always holds the last green push, from whichever branch. Also commits exported Room schemas back with `[skip ci]`. |
| APK link (never changes) | https://github.com/Selva461/Gym/releases/download/latest-debug/gym-debug.apk |
| iOS CI | `.github/workflows/ios.yml` on push to `main` or manual dispatch only (a Mac minute costs 10×). Unsigned IPA to the `latest-ios` release: https://github.com/Selva461/Gym/releases/download/latest-ios/gym-unsigned.ipa, sideloaded with Sideloadly or AltStore (re-sign every 7 days on a free Apple ID). |
| **iOS status** | **Blocked by GitHub billing**: "recent account payments have failed or your spending limit needs to be increased". Last good IPA is from `bf28299`. Every iOS change since (share sheet, water reminders, Tamil, …) has **never been compiled**. The user must fix Settings → Billing & plans. |
| Android status | Green on `main` `1948ef9`; APK published 2026-10-22. Week plan branch `claude/project-thread-kr2wuy` green in CI (run 42148048298, 14.json committed back); its APK published 2026-11-06. Merge to `main` once the user has tried it on the phone. |

To ship: commit on the branch, push (Android CI publishes the APK), merge to
`main` (a fast-forward, or a squash if the branch history is messy), push `main`.
This session cannot edit releases itself (`gh` REST is allowed for reading, but
release writes return 403); CI does the publishing.

## Current state

| | |
|---|---|
| Exercises | 486 across 13 muscle groups; 140 with bundled demonstrations |
| Foods | **1,248 built in** + 61 imported from the Indian Food Nutrition Dataset v2 (bundled CSV) = 1,309. 27 categories (newest: Alcohol). Newest file: `SeedFoodsWeek.kt` (186: Aavin/TN dairy, biscuit/chip/chocolate brands, cool and energy drinks, alcohol, fish by species, millets, breakfast and festival foods, supplement brands, per-100 g home dishes, chain menus). |
| Database | **v14**. v7 food `aliases`/`source`; v8 `exercise.isAssisted`; v9 `body_measurement`; v10 `set_entry.kind`/`rpe`; v11 `exercise.notes`; v12 `saved_meal`, `saved_meal_item`; v13 `cardio_entry`; v14 `plan_day`, `plan_lift`, `plan_food` (week plan, `data/PlanEntities.kt`, own `PlanDao`). Exported schemas in `app/schemas/` (CI commits `14.json` back after the first green run). |
| Tests | 277 (`gradle testDebugUnitTest`), none needing a device. New: `XlsxTest` (zip/deflate/XML vs the JDK and hostile input), `PlanImportTest` (incl. a fixture filled by openpyxl, `androidUnitTest/resources/`; splits, fill-forward, start today), `PlanCalendarTest`. |
| Toolchain | Kotlin 2.1.21, KSP 2.1.21-2.0.2, Compose Multiplatform 1.8.2, AGP 8.10.1, Gradle 8.11.1 (CI) |

Features, by screen (README → "What's in it" has the user-facing list):

- **Today**: protein/calorie meters, protein-carbs-fat bars, water card,
  weigh-in, this-week vs last-week card, first-run welcome (target calculator),
  **plan card** for the shown day (`screens/PlanCards.kt`): focus, every planned lift
  with sets × reps · kg · rest, "Start <focus> session", meals with "Log breakfast"
  (logs the foods that matched the catalogue), "Open week plan".
- **Week plan** (route `plan`, from Today and Settings → Your data;
  `screens/WeekPlanScreen.kt`): Teams-style week (Mon–Sun columns, today circled,
  each day's focus as a coloured all-day tile, colour by `FocusKind`), selected day
  as an hour timeline (workout at its planned time, meals at the plan's time or
  `PlanCalendar.SLOT_TIMES`, overlaps side by side), full lists, clear week,
  **Import plan** and **Get the Excel template** (Android file pickers,
  `PlatformPlanFiles.android.kt`; iOS returns null so the buttons hide).
  **Plans are training only** (the user asked for food to be removed and the import
  kept simple): `domain/plan/PlanTemplate.kt` writes the .xlsx (sheet "Read me" =
  instructions for an AI; "Week" prefilled with 30 dates for a month of split days;
  "Workout" with each split's lifts written once; "Exercises" = this phone's
  catalogue). A Meals sheet in an older file is ignored. On import every Focus
  that has lifts becomes a `PlanSplit` (first date wins) and later days with the
  same Focus and no Workout rows get its lifts. Confirming saves the plan and
  creates or updates (by name, case-insensitive) one split per `PlanSplit` under
  Train > Your splits (catalogue lifts only). The preview offers **Start today**
  (`PlanImportResult.startingOn`: every date shifted so the first is today, the
  rotation and rest days kept) or keeping the file's dates. Today's "Start
  <focus> session" starts from the split of that name, so its lifts come with it.
  The user's filled month file (4-day rotation) is in their project files, not the repo.
  Plan food tables (`plan_food`) and their display stay for old imports; nothing writes them now. `PlanSheets.kt` is the single source of column names (matched by header
  text, aliases allowed). `PlanImport.kt` validates strictly (`PlanValues.kt`
  parsers: ISO dates or Excel serials, HH:MM or Excel fractions, reps 8 / 8-12 / 45s /
  AMRAP, ranges and length caps), skips bad rows with sheet + row number, notes
  unknown exercises/foods (kept as written; unknown foods take kcal/protein from the
  sheet; food is no longer read), shows a preview, then `PlanRepository.replace` swaps exactly the dates in
  the file in one transaction. Reading is hand-written and portable:
  `core/zip/` (inflate, CRC, zip reader with size/entry caps, zip-bomb and duplicate
  name refusal; stored-zip writer) and `core/xlsx/` (`MiniXml` refuses DOCTYPE/
  entities, `Xlsx` reads cell values only, never formulas; writes inline strings so
  text never becomes a formula). The plan is in the AI export too.
- **Train**: splits, target per lift with warm-up ramp, last session's sets as
  chips, plate calculator (plate-loaded bars only), set types (working, warm-up,
  drop, failure) and RPE, notes per lift, swap (alternatives), supersets, rest
  timer that fits the lift (±15 s), session clock, lift history with rep-max
  table, workout summary card with records and calories, cardio log.
- **Fuel**: search/browse with Tamil aliases, favourites (star in the quantity
  dialog, stored as ids in settings), recent and usual foods, saved meals, quick
  add, recipe builder (`RecipeBuilder.kt` + `domain/Recipes.kt`: a custom food in
  "My recipes", per serving), log by grams/ml when the unit states a weight
  (`domain/Portions.kt`, `FoodDialogs.kt`), copy a meal from the day before,
  water, fasting timer.
- **Progress**: weight trend with goal date, achievements, month calendar,
  personal records, 1RM calculator, tape measurements with Navy body fat,
  progress photos, coach and patterns.
- **Settings**: targets (with a Mifflin-St Jeor calculator), water and weight
  goals, rest rules, reminders (nightly, water, meals at
  `Notifier.MEAL_TIMES`), backup/restore file (Android), CSV export (sets, food,
  weight), **Export everything for AI** (Settings → Your data, first button): one
  Markdown file `gym-log-for-ai-<date>.md` with a prompt explaining every field,
  summaries (daily nutrition vs target, sessions, strength per lift) and every table
  as CSV (`domain/AiExport.kt`, read via `GymRepository.fullLog` into `data/FullLog.kt`,
  formatted off-main in `ui/Exporter.kt`). Android shares it as a file through a
  non-exported `FileProvider` limited to `cache/exports/` (`res/xml/file_paths.xml`);
  iOS shares the text. **Any new table the user can enter must be added to `FullLog`
  and `AiExport.build`** (the user asked that everything they enter is in the export). Language (English / தமிழ் for tabs, headings, main buttons).
- **Other session's addition** (`e512b0d`): `share/TrainingProvider.kt`, a
  read-only content provider letting the user's "SelfUp" app read working sets,
  cardio and weigh-ins behind a dangerous `READ_TRAINING` permission.

Code map: `app/src/commonMain/kotlin/dev/avles/gym/` → `data/` (Room, seeds,
dataset importer, settings), `domain/` (pure logic, all unit tested),
`ui/` (`GymViewModel.kt` (at the 800-line cap: move pure logic to `domain/` before adding), `PlanViewModel.kt` for the week plan, + `UiState.kt`, `GymNav.kt`, `screens/`,
`components/` with `Motion.kt` for every animation and haptic, `i18n/`),
`core/` (dates, log, `zip/`, `xlsx/`), `domain/plan/` (template, import, calendar
layout). Platform seams in `androidMain` / `iosMain`.

## Working in a fresh session

The container starts without an Android SDK. These worked on 2026-10-22 (on
2026-11-05 `dl.google.com` returned 403 through the proxy, so that session could not
build locally and relied on Android CI to compile and test; same on 2026-11-06. That
session tested the pure-Kotlin parts (`core/zip`, `core/xlsx`, `domain/`, the plan tests,
`AiExportTest`, `ArchitectureTest`) in a scratch Kotlin/JVM Gradle project outside the
repo: plugin `kotlin("jvm") version "2.1.21"` from the Gradle plugin portal, deps from
the googleapis Maven mirror, the repo's source dirs added as `srcDirs`, tiny stub
`androidx.room` annotations and a copy of `GymSettings` in place of the Room/DataStore
artifacts that only Google Maven serves. Compose/Room code was left to CI):

```sh
# Android SDK (dl.google.com is reachable now)
mkdir -p /opt/android-sdk && cd /opt/android-sdk
curl -sSLfo clt.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip -q clt.zip && mkdir -p cmdline-tools/latest && mv cmdline-tools/{bin,lib,NOTICE.txt,source.properties} cmdline-tools/latest/
yes | JAVA_TOOL_OPTIONS= cmdline-tools/latest/bin/sdkmanager --sdk_root=/opt/android-sdk \
  --proxy=http --proxy_host=127.0.0.1 --proxy_port=44191 "platforms;android-35" "build-tools;35.0.0"
echo "sdk.dir=/opt/android-sdk" > /home/user/Gym/local.properties   # gitignored
```

Maven Central rate-limits the sandbox (HTTP 429), so point Gradle at Google's
Central mirror with a **local** init script, `~/.gradle/init.d/mirror.gradle.kts`
(never commit it):

```kotlin
val mirror = "https://maven-central.storage-download.googleapis.com/maven2/"
fun org.gradle.api.artifacts.dsl.RepositoryHandler.useMirror() {
    all { if (this is MavenArtifactRepository && url.toString().let { it.contains("repo.maven.apache.org") || it.contains("repo1.maven.org") }) setUrl(mirror) }
}
settingsEvaluated { pluginManagement.repositories.useMirror(); dependencyResolutionManagement.repositories.useMirror() }
allprojects { buildscript.repositories.useMirror(); repositories.useMirror() }
```

Then, from the repo (`gradle` 8.14 is preinstalled and works):

```sh
export ANDROID_HOME=/opt/android-sdk
gradle :app:compileDebugKotlinAndroid --no-daemon -q   # compile check, ~1 min warm
gradle testDebugUnitTest --no-daemon -q                # all tests; also exports Room schemas
gradle lintDebug assembleDebug --no-daemon -q          # APK at app/build/outputs/apk/debug/
python3 tools/check-imports.py                         # missing-import checker, seconds
```

The first Gradle run downloads ~1 GB. Lint has two **pre-existing** errors in
`androidMain/.../notify/Notifications.kt` (MissingPermission), non-fatal by
config. iOS cannot be compiled here (no macOS); keep iOS code to plain UIKit
members and let `ios.yml` judge it.

## Adding foods (the commonest request)

Foods live in `data/SeedFoods*.kt`, one `internal object` per batch, each with a
private `f(name, unit, kcal, p, c, fat, cat, aka = "", fib, sug, sat, na, k, ca,
fe, mg, zn, va, vc, vd, b12, fol, o3, supp)` and an `all()` that
`SeedData.foods()` concatenates. Rules the tests hold:

- **kcal is derived**, never typed: `round5(4P + 4(C − fibre) + 2·fibre + 9F)`.
  `FoodChecks` rejects >20% drift; fibre and sugar ≤ carbs; sat fat ≤ fat.
- **Names unique** across seeds; check against the dataset CSV too, because a
  seed name equal to a dataset dish moves that dish from imported/declined to
  "already present" and changes `FoodDatasetTest`'s counts (61 / 22 / 39 now).
- **Alcohol** (category `Alcohol`, `FoodChecks.ALCOHOL`) is the one exception:
  its energy is mostly ethanol, so kcal is given directly and the drift check
  skips the category. The generator takes `kc=` for this.
- Category `Protein` needs ≥ 5 g protein or ≥ 25% of kcal from protein.
- Category `Supplement` must have `supp = true` and the reverse; supplements
  file under the Supplements meal slot whatever meal is picked.
- Put local and English names in `aka` (comma separated) — search uses them —
  and add a line to `FoodSearchTest` for how the user would type it.
- Update the count in `SeedDataTest` ("the catalogue is the size the README
  claims"), README's two food counts, and this file.
- Existing installs pick up new seeds on next launch; nothing logged is touched.

## Conventions

- Commit trailer: `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>` and
  `Claude-Session: <session url>`. No model names anywhere else.
- Do not open PRs unless asked. Commit, push the branch, merge to `main` when the
  user asks or the work is verified.
- Never `git add -A` without checking `git status`: build output once got
  committed (`949ebcd`, now ignored via `build/` in `.gitignore`).
- Room schema change = bump `version`, add `MIGRATION_n_m` using `SchemaSql`
  constants, add a `MigrationSqlTest` case, run the Gradle tests so the new
  `app/schemas/.../n.json` is generated, and commit that JSON.

## Failed attempts worth not repeating

- `ksp { arg("room.schemaLocation", ...) }` exports nothing; the Room Gradle
  plugin does.
- `--stacktrace` on the CI test step hides the `e:` compile lines from the log
  tail; the workflow repeats real errors last instead.
- CI logs: `gh api repos/Selva461/Gym/actions/jobs/<id>/logs` works when the
  job ran; for jobs that never started, read
  `repos/.../check-runs/<id>/annotations` (that is how the billing block shows).
- KSP on a fresh Mac runner needs the Kotlin/Native toolchain provisioned first
  (`ios.yml` step "Provision the Kotlin/Native toolchain"). Keep it.
- `MutableMap.merge` and `computeIfAbsent` compile on Android and break iOS.
- History rewriting (filter-branch, force-push) is refused by the session's
  safety rules; untrack and ignore instead.
- Compose: `using` in `AnimatedContent` transitions is a member of the scope,
  not an import.

## Open items and next steps

1. **iOS billing** (user action): fix GitHub billing, then re-run `ios.yml` and
   fix whatever the never-compiled iOS code turns up.
2. **Week plan on iOS**: the screens and Today card work, but import and template
   download need a `UIDocumentPickerViewController` in `PlatformPlanFiles.ios.kt`
   (returns null now, so the buttons hide). Ideas the user may want next: a "next
   in rotation" pick on Train (from the last session's split, so a missed day does
   not shift the plan; today the fix is re-import with Start today), sets/reps/kg
   from the plan as targets inside the session, and notifications at planned times.
   Local builds: `dl.google.com` was blocked on 2026-11-06 (no Android SDK), so that
   session compiled and ran the pure-Kotlin plan tests with a downloaded `kotlinc`
   plus stub Room annotations, and left the UI compile to CI.
3. **Install**: Android from the APK link above. An install over a copy signed
   before `dd89f2c` needs an uninstall first (old CI key is gone).
4. Backup/restore: **Android done** (Settings › Your data › Back up / Restore).
   `data/Backup.kt` snapshots with `VACUUM INTO` and validates a file
   (schema version ≤ newest migration, core tables present);
   `PlatformBackup.android.kt` uses the system file pickers, keeps the replaced
   log as `gym-before-restore.db`, swaps the file and restarts the process.
   Photos and settings are not in the file. **iOS returns null** (buttons
   hidden): needs a `UIDocumentPickerViewController` and a relaunch prompt.
   Open security questions in `SECURITY.md` (biometric lock, SQLCipher).
5. Food requests come in batches by place: done so far are Tamil curries,
   staples, Indo-Chinese and grill counter, meals and restaurant chains,
   bakery, tea stall and juice shop, street and fast food, Kerala/Andhra/
   Hyderabadi/Arabian, packaged oats and protein foods, protein powders, and
   candidates 1–11 below (`SeedFoodsWeek.kt`). The generator scripts are not in
   the repo; write a seed file directly or with a throwaway script.

### Candidate additions (the user asked for all 17)

Foods 1–11: **done** in `SeedFoodsWeek.kt`.
2. **Aavin and Tamil Nadu dairy**: Aavin milk variants (toned, standardised,
   full cream), Aavin curd, ghee, paneer, milkshakes, ice creams; Hatsun/Arun.
3. **Packaged snack and biscuit brands**: Parle-G, Good Day, Marie Gold, Hide &
   Seek, Bourbon, Britannia bread, Lay's, Kurkure, Bingo, Haldiram's bhujia
   and namkeens, Too Yumm, Act II popcorn.
4. **Cool drinks and energy drinks**: Coca-Cola, Pepsi, Thums Up, Sprite, 7Up,
   Fanta, Mirinda, Bovonto (Tamil Nadu), Red Bull, Monster, Sting, Gatorade,
   Paper Boat, Real/Tropicana juices.
5. **Alcohol** (energy mostly from ethanol, so add to `FoodChecks.ENERGY_EXEMPT`):
   beer variants, whisky, rum, vodka, gin, wine, per peg and per bottle.
6. **Fish and seafood by species**: seer, mackerel (ayala), sardine, pomfret,
   tuna, salmon, prawns, crab, squid, each as fry, curry and grill.
7. **Millet dishes**: thinai pongal, samai upma, varagu and kuthiraivali rice,
   kambu and ragi variants.
8. **Home-style breakfast variants**: kara and pidi kozhukattai, adai
   varieties, idiyappam with kurma or coconut milk, paniyaram varieties.
9. **Festival foods**: Diwali savouries, Karthigai pori and appam, Ramzan nombu
   kanji, Christmas plum cake variants, Pongal set.
10. **More supplements by brand**: pre-workouts, BCAA/EAA, creatine,
   electrolytes, fish oil, multivitamins (MuscleBlaze, ON, HealthKart, Wellbeing).
11. **Per-100 g home dishes** for people who weigh cooked food: chicken curry,
    dal, sambar, rice, chapati dough, egg curry.
12. **Chain menus with published nutrition**: McDonald's India, KFC, Domino's,
    Subway, Burger King, Starbucks, A2B (Adyar Ananda Bhavan) sweets.

Features 12–15: **done** (recipe builder, favourites, grams toggle, meal reminders).
17 done on Android (see open item 4). 16 **declined for now**: the app has no
network or camera permission and no verified EAN codes for the seeded brands;
adding a camera, a barcode library and guessed codes would cost more trust than
it saves taps. Revisit if the user supplies codes from their own packs.

13. Recipe builder: combine foods into a home recipe with servings.
14. Favourite foods pinned to the top of Fuel.
15. Grams toggle: log any food by weight as well as by its unit.
16. Meal reminders (breakfast, lunch, dinner).
17. Offline barcode scan for the seeded branded foods only (needs EAN codes and
    a camera seam on both platforms).
18. Backup and restore (deferred; see item 4 above).

The fitness-parity backlog (items 1–26 from earlier sessions) is complete; item
25 (backup/restore) is done on Android only.
