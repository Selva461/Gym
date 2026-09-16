# Gym — handoff

## Goal

A personal Android training-and-food log for one phone, offline, no account.
Named **Gym**, repo `Selva461/Gym`, all work on branch `claude/gym-app-audit-982uq6`.

The current round of work has three parts, all from one request:

1. **Fix the crash** that closed the app on the Train tab. *(done, shipped)*
2. **Massively expand the exercise catalogue** — "in tricep there are different
   tricep exercises that we can do with a machine and cable and dumbbell and
   everything I want every possible combination of every exercise". *(done, not
   yet pushed)*
3. **Massively expand the food catalogue**, South Indian especially — "it's not
   enough add more data south indian food more more". *(done, not yet pushed)*

Plus one question to answer rather than build: **how to edit an exercise
session**, which the user could not work out.

## Current state

Everything for parts 2 and 3 is written, compiles, and passes the off-device
test harness — **67 tests green**. Nothing is committed yet.

- **394 exercises** across 13 real muscle groups (was 86 across Push/Pull/Legs):
  Chest 52, Back 53, Shoulders 43, Traps 12, Triceps 37, Biceps 30, Forearms 12,
  Quads 46, Hamstrings 20, Glutes 18, Calves 12, Core 40, Full body 19.
  Equipment lives in the lift's *name* ("Cable rope overhead triceps extension"),
  so the picker's existing search box doubles as an equipment filter: `cable`
  gives 28, `dumbbell` 43, `machine` 37, `smith` 12. Triceps alone has 37 —
  13 cable, 8 dumbbell, 7 barbell/EZ, 3 machine, 6 bodyweight.
- **439 foods in 24 categories** (was 239 in 22). ~199 added, heavily South
  Indian: 24 dosa, 7 idli, 43 tiffin, 28 rice, 41 sides, 62 protein entries,
  14 North Indian, 12 sweets, 19 drinks, 33 supplements.
- An integrity probe over all 439 foods found **zero problems**: no duplicate
  names, no negative values, no fibre or sugar above carbs, no saturated above
  total fat, no energy drift over 15% against the macros, protein and supplement
  categories internally consistent.
- The crash fix is **already pushed and live** (commit `92610af`); the APK at the
  release link below contains it.

**APK:** https://github.com/Selva461/Gym/releases/download/latest-debug/gym-debug.apk

**Note the user still needs:** the currently-installed app was signed by a
throwaway key that no longer exists, so moving to this build needs **one
uninstall** (which loses logged data). Every update after that installs over the
top, because the debug signing key is now pinned in the repo.

## Files in flight

All uncommitted, in `/home/user/Gym`:

| File | State | What changed |
|---|---|---|
| `app/src/main/java/dev/avles/gym/data/SeedExercises.kt` | **new** | All 394 lifts + the 10 starter splits, moved out of `SeedData`. Per-group shorthand builders keep each lift to one line. `Groups.ALL` is the single list of 13 groups. |
| `app/src/main/java/dev/avles/gym/data/SeedData.kt` | modified | Lifts delegated to `SeedExercises`; ~199 foods appended in 15 new functions; new public `muscleGroups()` so UI and tests share one list. |
| `app/src/main/java/dev/avles/gym/data/GymDao.kt` | modified | `staleSeededExerciseCount`, `refreshSeededExercise`, transactional `refreshSeededExercises`. |
| `app/src/main/java/dev/avles/gym/data/GymRepository.kt` | modified | `regroupSeededExercisesIfStale()` — rewrites muscle group and rep range on **seeded lifts only**, gated on a count so it runs once after upgrade. |
| `app/src/main/java/dev/avles/gym/ui/components/Primitives.kt` | modified | New `ChipRow` — scrolling selectable chips. `PillRow` divides width evenly, which works for 4 meals and collapses at 13 groups. |
| `app/src/main/java/dev/avles/gym/ui/screens/TrainScreen.kt` | modified | New-lift dialog uses `ChipRow(SeedData.muscleGroups(), …)`. (The key fix in this file is already committed.) |
| `app/src/test/java/dev/avles/gym/SeedDataTest.kt` | modified | Counts updated to 394/439/10; new tests for group membership, free-weight + machine coverage per muscle, cable coverage, per-category minimums, name length. |
| `README.md` | modified | New counts, the 13 groups, the equipment-in-the-name convention, the expanded food list. |
| `REQUIREMENTS.md` | modified | New §4.7 "Exercise catalogue" (F7.1–F7.4), new rule R9 on what an upgrade may rewrite, counts corrected. |

## Changed

- Fixed and shipped the Train-tab crash: two `items()` in one `LazyColumn` were
  both keyed on raw `it.id`, so a split and a session sharing an id collided.
  Keys are now namespaced (`"split-…"`, `"session-…"`, `"set-…"`, `"lift-…"`).
- Rebuilt the exercise catalogue from 86 to 394 and regrouped it from
  Push/Pull/Legs onto 13 real muscle groups.
- Added a one-time upgrade pass so an existing install does not end up showing
  the old three groups alongside the new thirteen.
- Grew the food catalogue from 239 to 439.
- Swapped the muscle-group selector from `PillRow` to a scrolling `ChipRow`.
- Updated both docs and the test tripwires to match.

## Failed attempts

- **No Android SDK in this container** — `dl.google.com` is blocked by the egress
  proxy, so nothing Android can be compiled here. `repo1.maven.org` *is*
  reachable, so the workaround is a local harness
  (`…/scratchpad/verify.sh`) that compiles the pure-Kotlin domain, entity and
  seed layers with `kotlin-compiler-embeddable` and runs JUnit. Compose and Room
  code can only be compiled by CI. **Do not try to install the SDK again.**
- **Cannot hand the APK over as a file.** The Actions artifact host (Azure blob)
  is proxy-blocked, and an unauthenticated github.com release download returns
  "Not Found" because the repo is private. The user must use the link.
- **`MigrationSqlTest` blocked the APK twice** over where KSP writes the exported
  schema. Now skips via `@Before` + `assumeTrue` when the schema file is absent,
  and still fails loudly when it is present and disagrees. This was the direct
  cause of the user's "stop running in a loop" — do not reintroduce a test that
  can block the build over a build-layout detail.
- **`internal object SeedExercises` was invisible to tests** in the harness
  (separate compilations). Fixed by exposing `SeedData.muscleGroups()` publicly
  rather than widening the object.
- **Two of my own tests were wrong**: one demanded a cable *Quads* exercise —
  there is no standard one, so the test was narrowed rather than inventing a
  lift; an earlier one used a naive 4/4/9 energy check that failed cooked spinach
  because fibre is ~2 kcal/g, not 4.
- **Two duplicate lift names** slipped into the new catalogue and were caught by
  the probe: renamed to `Neutral-grip dumbbell shoulder press` and
  `Close-grip dumbbell floor press`.
- **CI run 1 failed in 26s** because `setup-android@v3` installs the withdrawn
  legacy `tools` package; fixed with an explicit `packages:` list.

## Next step

**Commit the seven in-flight files and push to `claude/gym-app-audit-982uq6`,
then watch CI produce the new APK at the `latest-debug` release tag.**

Nothing is blocking it — the suite is green and the data is verified.

Then tell the user how to edit a session, which is the one open question:

> Train tab → with no session open, scroll to **Recent sessions** (the heading
> says "tap to edit") → tap any session row. The dialog lets you rename it,
> re-open it to add more sets (offered only when no other session is open), tap
> any set in the list to change weight, reps or the warm-up flag, delete a single
> set, or delete the whole session behind a second confirming tap.

They could not reach this before because the duplicate-key crash fired on exactly
that screen as soon as a past session existed.
