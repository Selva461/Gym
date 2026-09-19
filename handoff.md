# Gym — handoff

Last updated after the phase 2 work (tiers 0–5), the bundled demonstrations, and
the Room schema-export fix. Supersedes the version written at the 394-lift
catalogue commit, which is 15 commits stale.

## Goal

A personal Android training-and-food log for one phone, offline, no account.
Repo `Selva461/Gym`, branch `claude/gym-app-audit-982uq6`.

Standing constraints, all deliberate and all enforced by tests:

- **No network, ever.** `INTERNET` and `ACCESS_NETWORK_STATE` are stripped from
  the merged manifest with `tools:node="remove"`. Anything the app shows must
  already be inside the APK.
- **Nothing licensed from anyone else.** No scraped exercise GIFs.
- **Additive, not destructive.** Every schema change is a hand-written migration;
  no `fallbackToDestructiveMigration`.

## Current state

**Everything builds and ships. 116 tests green. CI run 25 succeeded.**

| | |
|---|---|
| Exercises | 394, across 13 real muscle groups, equipment in the name |
| Demonstrations | 140 lifts, 2 frames each (start/finish), 3.6 MB, public domain |
| Foods | 439 in 24 categories, heavily South Indian |
| Database | v5, migrations 1→2→3→4→5, all hand-written |
| Tests | 116, none needing a device or an emulator |

Phase 2 tiers, all done: **0** foundation (logging, enforced layering, security
posture), **1** derived exercise reference, **2** reference UI with a muscle map
drawn in Compose, **3** per-set grip/stance with split progression and records,
**4** progress photos, **5** your own picture per lift.

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

**None.** Working tree is clean and everything is pushed. `app/schemas/` is now
committed and CI writes to it on each successful build.

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

## Next step

**Get the app onto the phone**, which is the only thing actually blocking the
user: uninstall the existing `dev.avles.gym.debug`, then install from
https://github.com/Selva461/Gym/releases/download/latest-debug/gym-debug.apk

If that still fails, check for a second user or work profile holding the package.

After that, nothing is outstanding. Two open questions the user may want answered:

- Whether to narrow the 800-line cap in `ArchitectureTest` to `ui/` only, since
  data-heavy files like `ExerciseGuide.kt` (789 lines of tables) are where it is
  least useful and where it caused the import churn above.
- Whether to add either of the two security gaps named in `SECURITY.md`: a
  biometric lock on app launch, or SQLCipher at rest. Both are real trade-offs,
  and the second can lose the database permanently if the Keystore key is lost.
