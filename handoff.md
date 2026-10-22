# Gym — handoff

**Start here.** A new session should be able to continue from this file alone.
It is kept current by rule: every commit that changes code, data, the build or
the plan also updates this file (see `CLAUDE.md`). Last updated with the
protein powder brands and the list of further additions.

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
| Working branch | `claude/gym-app-audit-982uq6` (kept merged with `main`). Another session used `claude/project-thread-dhxbg7`, merged as PR #1. |
| Android CI | `.github/workflows/android.yml` on **every push and PR, any branch**: unit tests, lint, `assembleDebug`, then **replaces the APK on the `latest-debug` release**. So the APK link always holds the last green push, from whichever branch. Also commits exported Room schemas back with `[skip ci]`. |
| APK link (never changes) | https://github.com/Selva461/Gym/releases/download/latest-debug/gym-debug.apk |
| iOS CI | `.github/workflows/ios.yml` on push to `main` or manual dispatch only (a Mac minute costs 10×). Unsigned IPA to the `latest-ios` release: https://github.com/Selva461/Gym/releases/download/latest-ios/gym-unsigned.ipa, sideloaded with Sideloadly or AltStore (re-sign every 7 days on a free Apple ID). |
| **iOS status** | **Blocked by GitHub billing**: "recent account payments have failed or your spending limit needs to be increased". Last good IPA is from `bf28299`. Every iOS change since (share sheet, water reminders, Tamil, …) has **never been compiled**. The user must fix Settings → Billing & plans. |
| Android status | Green on `main` `1948ef9`; APK published 2026-10-22. |

To ship: commit on the branch, push (Android CI publishes the APK), merge to
`main` (a fast-forward, or a squash if the branch history is messy), push `main`.
This session cannot edit releases itself (`gh` REST is allowed for reading, but
release writes return 403); CI does the publishing.

## Current state

| | |
|---|---|
| Exercises | 486 across 13 muscle groups; 140 with bundled demonstrations |
| Foods | **1,062 built in** + 61 imported from the Indian Food Nutrition Dataset v2 (bundled CSV) = 1,123. 26 categories (newest: Meals). Newest file: `SeedFoodsSupplements.kt` (protein powders and gainers by brand, flagged as supplements). |
| Database | **v13**. v7 food `aliases`/`source`; v8 `exercise.isAssisted`; v9 `body_measurement`; v10 `set_entry.kind`/`rpe`; v11 `exercise.notes`; v12 `saved_meal`, `saved_meal_item`; v13 `cardio_entry`. Exported schemas in `app/schemas/`. |
| Tests | 243 (`gradle testDebugUnitTest`), none needing a device |
| Toolchain | Kotlin 2.1.21, KSP 2.1.21-2.0.2, Compose Multiplatform 1.8.2, AGP 8.10.1, Gradle 8.11.1 (CI) |

Features, by screen (README → "What's in it" has the user-facing list):

- **Today**: protein/calorie meters, protein-carbs-fat bars, water card,
  weigh-in, this-week vs last-week card, first-run welcome (target calculator).
- **Train**: splits, target per lift with warm-up ramp, last session's sets as
  chips, plate calculator (plate-loaded bars only), set types (working, warm-up,
  drop, failure) and RPE, notes per lift, swap (alternatives), supersets, rest
  timer that fits the lift (±15 s), session clock, lift history with rep-max
  table, workout summary card with records and calories, cardio log.
- **Fuel**: search/browse with Tamil aliases, recent and usual foods, saved
  meals, quick add, copy a meal from the day before, water, fasting timer.
- **Progress**: weight trend with goal date, achievements, month calendar,
  personal records, 1RM calculator, tape measurements with Navy body fat,
  progress photos, coach and patterns.
- **Settings**: targets (with a Mifflin-St Jeor calculator), water and weight
  goals, rest rules, reminders (nightly, water), CSV export (sets, food,
  weight), language (English / தமிழ் for tabs, headings, main buttons).
- **Other session's addition** (`e512b0d`): `share/TrainingProvider.kt`, a
  read-only content provider letting the user's "SelfUp" app read working sets,
  cardio and weigh-ins behind a dangerous `READ_TRAINING` permission.

Code map: `app/src/commonMain/kotlin/dev/avles/gym/` → `data/` (Room, seeds,
dataset importer, settings), `domain/` (pure logic, all unit tested),
`ui/` (one ViewModel `GymViewModel.kt` + `UiState.kt`, `GymNav.kt`, `screens/`,
`components/` with `Motion.kt` for every animation and haptic, `i18n/`),
`core/` (dates, log). Platform seams in `androidMain` / `iosMain`.

## Working in a fresh session

The container starts without an Android SDK. These worked on 2026-10-22:

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
2. **Install**: Android from the APK link above. An install over a copy signed
   before `dd89f2c` needs an uninstall first (old CI key is gone).
3. Deferred by choice: full database backup/restore (needs a FileProvider, a
   document picker on both platforms and a live DB swap; Android Auto Backup
   and the CSV export cover most of it). Open security questions in
   `SECURITY.md` (biometric lock, SQLCipher).
4. Food requests come in batches by place: done so far are Tamil curries,
   staples, Indo-Chinese and grill counter, meals and restaurant chains,
   bakery, tea stall and juice shop, street and fast food, Kerala/Andhra/
   Hyderabadi/Arabian, packaged oats and protein foods, protein powders.

### Candidate additions (offered to the user; take in this order unless asked)

Foods:
1. **Aavin and Tamil Nadu dairy**: Aavin milk variants (toned, standardised,
   full cream), Aavin curd, ghee, paneer, milkshakes, ice creams; Hatsun/Arun.
2. **Packaged snack and biscuit brands**: Parle-G, Good Day, Marie Gold, Hide &
   Seek, Bourbon, Britannia bread, Lay's, Kurkure, Bingo, Haldiram's bhujia
   and namkeens, Too Yumm, Act II popcorn.
3. **Cool drinks and energy drinks**: Coca-Cola, Pepsi, Thums Up, Sprite, 7Up,
   Fanta, Mirinda, Bovonto (Tamil Nadu), Red Bull, Monster, Sting, Gatorade,
   Paper Boat, Real/Tropicana juices.
4. **Alcohol** (energy mostly from ethanol, so add to `FoodChecks.ENERGY_EXEMPT`):
   beer variants, whisky, rum, vodka, gin, wine, per peg and per bottle.
5. **Fish and seafood by species**: seer, mackerel (ayala), sardine, pomfret,
   tuna, salmon, prawns, crab, squid, each as fry, curry and grill.
6. **Millet dishes**: thinai pongal, samai upma, varagu and kuthiraivali rice,
   kambu and ragi variants.
7. **Home-style breakfast variants**: kara and pidi kozhukattai, adai
   varieties, idiyappam with kurma or coconut milk, paniyaram varieties.
8. **Festival foods**: Diwali savouries, Karthigai pori and appam, Ramzan nombu
   kanji, Christmas plum cake variants, Pongal set.
9. **More supplements by brand**: pre-workouts, BCAA/EAA, creatine,
   electrolytes, fish oil, multivitamins (MuscleBlaze, ON, HealthKart, Wellbeing).
10. **Per-100 g home dishes** for people who weigh cooked food: chicken curry,
    dal, sambar, rice, chapati dough, egg curry.
11. **Chain menus with published nutrition**: McDonald's India, KFC, Domino's,
    Subway, Burger King, Starbucks, A2B (Adyar Ananda Bhavan) sweets.

Features:
12. Recipe builder: combine foods into a home recipe with servings.
13. Favourite foods pinned to the top of Fuel.
14. Grams toggle: log any food by weight as well as by its unit.
15. Meal reminders (breakfast, lunch, dinner).
16. Offline barcode scan for the seeded branded foods only (needs EAN codes and
    a camera seam on both platforms).
17. Backup and restore (deferred; see item 3 above).

The fitness-parity backlog (items 1–26 from earlier sessions) is complete except
item 25 (backup/restore, deferred as above).
