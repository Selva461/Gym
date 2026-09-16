# Gym — architecture

Written for the person who opens this repository in a year wanting to change one
thing. The organising idea is that **adding should never mean rewriting**: every
part of the app that is expected to grow is a list you append to, not a function
you edit.

---

## Layers

```
  ui/        Compose screens, components, theme, navigation, one view model
     ↓
  data/      Room entities, DAO, database, repository, settings, seed catalogue
     ↓
  domain/    Pure Kotlin. Progression, macro maths, streaks, the exercise guide
```

**Dependencies point down and never up**, and `ArchitectureTest` fails the build
if they do. The rules and the reason for each are in that file — if a rule is
genuinely wrong, change it there and say why in the commit rather than adding an
exception.

The one that matters most: **`domain/` contains no Android imports.** That is not
tidiness. It is why the entire progression, macro, streak and catalogue suite runs
in a plain JVM with no emulator and no SDK — which is how every one of these 90
tests was written and verified. One `import android.` ends that, and the failure
mode is not a broken test, it is a test suite that quietly needs a device.

`core/` sits outside the stack and holds the one piece of infrastructure both
sides use: `GymLog`.

---

## Where to add things

This is the section worth keeping current.

| To add… | Edit | Notes |
|---|---|---|
| A bundled demonstration | `data/ExerciseDemos.kt` + `assets/demo/` | Generated; see CREDITS.md for the source and the matching rules |
| An exercise | `data/SeedExercises.kt` | One line. Pick the right per-group builder — `tri("…")`, `chest("…")` — and put the equipment **in the name**, which is what makes the picker's search double as an equipment filter |
| A muscle group | `SeedExercises.Groups` | Add the constant and put it in `ALL`. The picker, the new-lift dialog and the tests all read that one list |
| A food | `data/SeedData.kt` | One line in the relevant function, then add the function to `foods()` if it is new |
| Reference text for a movement | `domain/ExerciseGuide.kt` | Edit the `when (pattern)` branch. Every lift with that pattern picks it up |
| A new movement pattern | `domain/ExerciseGuide.kt` | Add to the `Pattern` enum; the compiler then tells you every `when` that needs a branch. This is deliberate — an exhaustive `when` over an enum is a to-do list the compiler maintains |
| A variation axis | `ExerciseGuide.variationsFor` | Axes are declared once as vals and referenced per pattern |
| A screen | `ui/screens/`, then `ui/GymNav.kt` | |
| A migration | `data/GymDatabase.kt` + `data/SchemaSql.kt` | Bump the version, write the `Migration`, put the DDL in `SchemaSql` so `MigrationSqlTest` can compare it against Room's exported schema |

Nothing in that table says "rewrite" or "replace". That is the test of whether
this holds: if adding a thing means changing logic rather than adding data, the
structure has drifted.

---

## Why the exercise guide is derived rather than stored

Four hundred hand-written references would be four hundred things to get wrong,
and a lift you invent tomorrow would still have none. Instead `ExerciseGuide.of()`
computes everything from the exercise row — equipment from the name, pattern from
the name and muscle group, muscles and text from the pattern.

The cost is that one careless keyword can mis-describe a hundred lifts at once.
That is what `ExerciseGuideTest` is for, and every case in it is one the derivation
got wrong at some point: close-grip bench described as an extension, a rack pull
described as a row, hip adduction claiming the glutes.

---

## Debugging

**`GymLog`** is the only place the app logs. It keeps the last 200 events in
memory so a debug build can show what just happened, and compiles itself out of
release entirely — a health log must not write body weights into logcat. Call it
with a lambda (`GymLog.d(Tag.TRAIN) { "…" }`) so the string is never built in
release.

**`tools/check-imports.py`** answers the one question this project cannot otherwise
ask without a full Android SDK: is every capitalised reference in a Kotlin file
either imported, declared in the same package, or fully qualified? It is not a type
checker and knows nothing about signatures — it catches exactly one mistake, the
unresolvable name, which is the mistake that otherwise costs a full CI round trip
to discover. It found `Slate` and `Training` missing from a file that had just been
split out of another, which is precisely when it happens.

**Off-device test harness.** Everything in `domain/` plus the entities and seed
catalogue compiles and runs in a plain JVM. That is the fast loop: a change to
progression or the catalogue is verified in about a second without an emulator.
Compose and Room code needs CI.

**`MigrationSqlTest`** compares the hand-written migration against the schema Room
exports. It *skips* when the exported schema is absent rather than failing —
because a test that blocks the APK over a build-layout detail is a test that will
be deleted. It still fails loudly when the schema is present and disagrees.

### Reading a release crash

Release builds are obfuscated, so a stack trace is `a.b.c(SourceFile:42)` until it
is mapped. `app/build/outputs/mapping/release/mapping.txt` from **that exact
build** turns it back into real names. Archive it with every release; do not
publish it.

---

## Testing

Ninety tests, none of which need a device.

| Suite | Guards |
|---|---|
| `ProgressionTest`, `MacroMathTest`, `TrainingTest`, `ConsistencyTest`, `NutrientsTest` | The maths |
| `SeedDataTest` | Catalogue size, duplicates, and food data that cannot be eyeballed — fibre under carbs, saturated under total fat, calories agreeing with macros |
| `ExerciseGuideTest` | The derivation, case by case |
| `ArchitectureTest` | Layering, file size, no SQL built by interpolation |
| `SecurityPostureTest` | Permissions, exported components, obfuscation, signing material |

The last two are the ones that make the qualities in this document hold. A
convention that lives only in a document decays; these fail.
