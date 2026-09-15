# Gym — requirements

Version 0.1.0 · single user · Android · offline

---

## 1. What this is

A training and food log for one person, built around one specific pattern of use:
a gym session that starts around 8:50 PM after a commute, a South Indian
non-vegetarian diet, and a body recomposition goal that follows a long weight
loss. It is a sibling to Notify, not a replacement for it — Notify owns
reminders and daily planning; Gym owns the two things that need a number
attached to them.

**The one job:** log a set between two sets without breaking rest, and log a
meal in under ten seconds. Everything else in this document exists to serve
that, and anything that competes with it gets cut.

## 2. Users and context

| | |
|---|---|
| Users | One. No accounts, no sharing, no sync. |
| Device | Phone, held in one hand, often mid-set. |
| Light | Gym floor at night, or in bed. The app is dark-only by design. |
| Network | None required, ever. The app must be fully functional in airplane mode. |

## 3. Scope

### In scope for 0.1

1. Log resistance training: session, exercise, set, weight, reps, warm-up flag.
2. Rest timer between working sets, with a notification when it ends.
3. Log food against a catalogue, by meal, with fractional quantities.
4. A food catalogue seeded with South Indian staples, editable in place.
5. Daily protein and calorie totals against user-set targets.
6. Body weight log with a trend line.
7. Training volume and consistency streaks.
8. One nightly reminder notification.
9. A week calendar strip for moving between days and seeing gaps.

### Explicitly out of scope for 0.1

- Any AI, LLM or coaching feature. That belongs elsewhere.
- Barcode scanning, photo logging, packaged-food databases.
- Cardio, steps, heart rate, sleep, water, supplements-as-a-module.
- Cloud sync, export to sheets, sharing, social.
- Programme templates and autoregulation.
- Light theme.

Cutting these is the point. Each one would add a surface to maintain and none of
them move the one job.

## 4. Functional requirements

### 4.1 Today

- **F1.1** Shows the selected day's protein total as the dominant number, with
  the calorie total subordinate to it. Protein is the number that decides a
  recomposition; calories are context.
- **F1.2** Shows a seven-day strip centred three days behind the selected day.
  Each day carries up to two dots: brass if trained, sage if food was logged.
- **F1.3** Future days are visible but not selectable.
- **F1.4** Shows whether the day has a training session, and whether one is open.
- **F1.5** Lists the day's food entries, newest first.
- **F1.6** Both section buttons route to the relevant tab. Today is a summary,
  not a data entry surface.

### 4.2 Train

- **F2.1** With no session open: choose a label (Push, Pull, Legs, Full) and
  start. Shows the last 20 sessions below.
- **F2.2** With a session open: choose a lift, enter kg and reps, mark warm-up
  or working, log. The reps field clears after each set; weight and lift persist,
  because the next set is usually the same weight.
- **F2.3** Logging a working set starts the rest timer automatically. Warm-ups
  do not.
- **F2.4** Rest can be skipped. When it expires the app posts a notification
  naming the lift.
- **F2.5** A working set that beats every previous working set on the same lift
  by estimated 1RM is marked in the session list.
- **F2.6** Tapping a logged set deletes it. No confirmation — a mis-logged set
  is cheap to re-enter and a dialog costs more than the mistake.
- **F2.7** New lifts can be added to the catalogue without leaving the session.
- **F2.8** A session stays open until explicitly finished. An open session
  survives app restart.

### 4.3 Fuel

- **F3.1** Meal defaults from the clock: breakfast 04–10, lunch 11–15,
  snack 16–19, dinner otherwise.
- **F3.2** Search filters the catalogue by substring, case-insensitive, capped
  at 12 results.
- **F3.3** With no search term, shows the most-logged foods as quick picks.
  This list builds from actual use and is empty on first launch.
- **F3.4** Quantity dialog offers 0.5 / 1 / 1.5 / 2 presets and free entry, and
  previews the protein and calories the entry will add before it is committed.
- **F3.5** Any food can be edited in place. Any new food can be added.
- **F3.6** Entries group by meal, each meal showing its own protein and calorie
  subtotal.
- **F3.7** Tapping an entry removes it.

### 4.4 Progress

- **F4.1** Body weight trend since the first recorded entry, with the change
  stated in words rather than left to the reader.
- **F4.2** Training volume for the last 14 days as bars. Untrained days render
  as a hairline, not as nothing — the gap is the information.
- **F4.3** Current and longest streaks for both training and food logging.
- **F4.4** A streak break is stated as a restart, never as a loss. The copy is
  deliberate: a missed day is not a reason to stop.

### 4.5 Settings

- **F5.1** Protein and calorie targets are user-set integers. The app never
  computes a target from height, weight, age or activity, and never suggests a
  deficit. These are the user's numbers to choose and change.
- **F5.2** Rest duration, clamped 15–900 seconds.
- **F5.3** Nightly reminder time and on/off switch.
- **F5.4** States plainly that all data is local.

## 5. Data model

Dates are stored as epoch days and instants as epoch millis, so no Room type
converter is needed anywhere in the schema.

```
exercise         id, name, muscleGroup, isBodyweight, isCustom
workout_session  id, dateEpochDay, label, startedAt, endedAt?, notes
set_entry        id, sessionId→, exerciseId→, setIndex, weightKg, reps, isWarmup, loggedAt
food_item        id, name, unitLabel, kcal, proteinG, carbsG, fatG, category, isCustom
food_log         id, dateEpochDay, foodId→, meal, quantity, loggedAt
body_weight      id, dateEpochDay (unique), weightKg, note
```

Settings live in DataStore, not Room: they are six scalars, not a table.

**Macros are stored per unit, not per 100 g.** A unit is whatever the user
actually eats — one idli, one cup, one 30 g scoop. Per-100 g would be more
"correct" and would require arithmetic at the moment of logging, which is
exactly when the user has the least patience.

## 6. Business rules

| ID | Rule |
|---|---|
| R1 | Warm-up sets never count toward volume and can never set a record. |
| R2 | Estimated 1RM uses Epley: `w × (1 + reps/30)`. A single returns the weight itself. |
| R3 | Invalid set input (zero or negative weight or reps) returns zero rather than throwing. |
| R4 | A streak ending yesterday still counts today. Logging before bed is not a deadline. |
| R5 | Calorie overshoot past 105% of target is shown in the error colour rather than silently capped. |
| R6 | A quantity of zero or less cannot be logged. |
| R7 | Dates after today cannot be selected. |
| R8 | The catalogue seeds once, on first launch, and never overwrites user edits. |

## 7. Non-functional requirements

- **N1** Fully offline. `INTERNET` and `ACCESS_NETWORK_STATE` are actively removed
  from the merged manifest with `tools:node="remove"`, so no dependency can
  reintroduce network access without the build showing it.
- **N2** No analytics, no crash reporting, no third-party SDK of any kind.
- **N3** Cold start to an interactive Today screen in under one second on
  mid-range hardware.
- **N4** minSdk 26, targetSdk 35.
- **N5** Notification permission is requested once and declining degrades only
  the timer buzz and the nightly nudge.
- **N6** Data survives reinstall via Android auto-backup where the device
  supports it — conditionally. `disableIfNoEncryptionCapabilities="true"` means a
  device that cannot encrypt the backup end to end uploads nothing rather than
  uploading something readable.
- **N7** The exported boot receiver is restricted to
  `android.permission.RECEIVE_BOOT_COMPLETED`, so only the system can reach it.
- **N8** The release build is non-debuggable and signed from CI secrets, never
  from a keystore committed to the repository.

## 8. Design direction

Recorded here so later changes are deliberate rather than accidental.

**Palette.** Blue-black base (`#12141C`) rather than tinted grey, because at
night a slight indigo cast reads as evening instead of as a dead screen. Two
accents that carry meaning and never swap roles: brass `#F2A93B` is always
training, sage `#6FBF8B` is always food. A number's colour tells you which half
of the app you are in before you read its label.

**Type.** One family, the platform's own. The scale spends all its contrast on
numerals — large, light, tight tracking — and keeps everything around them
quiet. No bundled typeface: several hundred kilobytes to render "92.5" is a bad
trade.

**Structure.** A vertical column separated by hairlines and space, not a grid of
identical rounded cards with identical shadows. Boxes appear only where
something is a discrete object — a logged set, a food entry — and those get a
three-pixel accent edge instead of a full radius and a drop shadow.

**Motion.** One animated element in the whole app: the meter fill. Everything
else is static. Entrance animations on every section are the house style of
generated UI and they slow down the one job.

**Bottom bar.** Accent dots instead of icons. The colour system already
distinguishes the sections, so adding a second visual language of glyphs would
be redundant — and it keeps the APK free of an icon pack.

**Copy.** Empty states point at the next action. Streak copy never frames a
missed day as failure.

## 9. Test and audit plan

### Automated, runs on every push

- **Unit tests** over the pure domain layer: macro arithmetic, ratio clamping,
  estimated 1RM, personal record detection, set numbering, streak maths, weight
  formatting, and the seed catalogue itself. 32 cases covering the boundaries
  that matter — empty days, zero targets, fractional servings, warm-up
  exclusion, per-exercise record isolation, streaks broken by a two-day gap,
  decimal separators under a comma locale, and a seeded food whose stated
  calories drift from its own macros.
- **Android Lint** across the app and its dependencies, publishing an HTML
  report as a build artifact. Lint does not gate the APK: a style warning must
  never stand between the user and a working build.
- **Assemble**, publishing the debug APK as a downloadable artifact.

### Manual, before trusting a build

1. First launch seeds 27 exercises and 35 foods.
2. Log a set, confirm the rest timer starts and the notification fires.
3. Log a warm-up, confirm no timer and no volume change.
4. Beat a previous best, confirm the record marker.
5. Kill and reopen mid-session, confirm the session is still open.
6. Log food, change day on the strip, confirm totals follow the day.
7. Edit a seeded food, confirm past entries recompute.
8. Turn the nightly reminder off and on, reboot, confirm it survives.
9. Decline the notification permission, confirm nothing else breaks.

## 10. Build and delivery

There is no development machine in this setup — the phone is the whole
environment. So the build runs on GitHub Actions and the APK comes back as a
downloadable artifact.

```
push to any branch
  → JDK 17, Android SDK, Gradle 8.9
  → gradle testDebugUnitTest  (gates)
  → gradle lintDebug          (reports, does not gate)
  → gradle assembleDebug
  → upload: gym-debug-apk, unit-test-report, lint-report
```

No branch filter, deliberately. The APK is the deliverable and it is wanted from
whichever branch the work is on; a filter on `main` means pushing a fix and
getting nothing back until you merge.

The debug build is signed with the standard debug key, which is enough to
install on your own phone and not enough to publish. A release build would need
a keystore in repository secrets; that is a later problem.

## 11. Known risks

| Risk | Handling |
|---|---|
| Toolchain versions in `libs.versions.toml` may be behind current | Pinned as a matched set; if CI fails on the first run it is almost certainly this, and the fix is bumping AGP, Kotlin, KSP and the Compose BOM together rather than one at a time |
| Seeded macro values are estimates for home-style portions | Every food is editable; the app says so in Settings and in the food editor |
| Restaurant oil and ghee vary enormously | Same handling — correct the five foods you eat most and the rest stops mattering |
| Database has no migrations yet | Schema is exported to `app/schemas` from v1 so migrations are possible later; until then a schema change means a reinstall |
| The Compose and Android layers have never been compiled | Statically audited only — package and resource resolution, manifest class references, property-delegate operator imports, and every cross-file call site against its declaration. The first CI run is the first real compile of that half |
| A phone in a comma-decimal locale | `Training.formatWeight` is pinned to a dot, because the weight dialog parses its own output back through a digits-and-dot filter and would otherwise read 62.5 kg as 625. Regression test in `TrainingTest` |
| Auto-backup silently dropping settings | Targets, rest length and the reminder live in DataStore under `files/datastore`, not `sharedpref`; both backup rule files name it explicitly, alongside the database and its `-wal` |

## 12. What version 0.2 would earn

Only after 0.1 has been used daily for a month, and only what the month proves
necessary:

- Repeat-last-session, to remove the retyping that will become the main friction.
- Per-lift history on the picker, so the last working weight is visible when
  choosing.
- CSV export, once there is enough data to be worth losing.
