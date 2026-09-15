# Gym

A training and food log for one person, one phone, no network.
Kotlin, Jetpack Compose, Room. Built on GitHub Actions because the phone is the
whole development environment.

Full spec: [REQUIREMENTS.md](REQUIREMENTS.md)

---

## Read this first

**It builds.** CI is green: the whole app compiles — Kotlin, Compose, Room's
generated code — lint passes, the 32 unit tests pass, and `gym-debug-apk` comes
back as an artifact. The pinned toolchain below is a working set, not a guess.

What has *not* happened is anyone running it on a phone. Compiling is not the
same as behaving, so the manual pass in section 9 of
[REQUIREMENTS.md](REQUIREMENTS.md) is still worth walking through once — in
particular that the rest timer fires, an open session survives a restart, and
editing a seeded food recomputes past entries.

**If a later build fails, look at versions before you look at code.** The
toolchain in `gradle/libs.versions.toml` is pinned as a matched set that is
known to work together, but it may be behind what the Android plugins expect
now. The fix is to bump AGP, Kotlin, KSP and the Compose BOM **together** —
bumping one alone will break the other three.

```toml
agp        = "8.7.3"
kotlin     = "2.0.21"
ksp        = "2.0.21-1.0.28"   # must track the Kotlin version exactly
composeBom = "2024.10.01"
room       = "2.6.1"
```

## Getting the APK

1. Push to any branch. `.github/workflows/android.yml` has no branch filter, so
   the build runs wherever the work is — you do not have to merge to `main` to
   get an APK.
2. Open the **Actions** tab, click the run, scroll to **Artifacts**, download
   `gym-debug-apk`.
3. Unzip on the phone and install. Android will ask you to allow installs from
   that source once.

The APK is signed with the standard debug key — fine for your own phone,
not publishable. The app id is `dev.avles.gym.debug`, so it installs
alongside anything else without conflict.

Two other artifacts come back from every run: `unit-test-report` and
`lint-report`. The tests gate the build; lint does not, deliberately — a style
warning should never stand between you and a working APK.

## What's in it

```
Today      protein first, calories second, a week strip with a dot per logged day
Train      session → lift → kg × reps, rest timer, records, per-session volume
Fuel       meal-aware logging over an editable catalogue, fractional quantities
Progress   weight trend, 14-day volume, training and logging streaks
Settings   your targets, rest length, nightly reminder
```

Seeded on first launch: 27 lifts across push, pull, legs and core, and 35 foods
built around South Indian home cooking — idli, dosa, parotta, sambar, Chettinad
chicken, mutton kuzhambu, vanjaram and nethili, egg podimas, curd rice, plus
plain oats and unflavoured raw whey.

**Those macro numbers are estimates.** Home-style portions vary enormously with
oil, coconut and ghee. Every food is editable in place. Correcting the five or
six you actually eat every week is worth more than any number of extra entries.

## Structure

```
data/      Room entities, one DAO, repository, seed catalogue, DataStore settings
domain/    pure Kotlin — macro maths, 1RM and records, streaks. All of it unit tested.
ui/        one ViewModel, five screens, a small set of shared components
notify/    notification channels, rest timer alert, nightly reminder, boot re-schedule
```

`domain/` holds no Android types, and neither do the row classes it works on.
That is not tidiness for its own sake — it is what lets the maths be compiled and
tested without an emulator, or without an SDK at all.

The domain layer holds no Android types, which is why it can be tested without
an emulator. Anything that can move into it, should.

There is no dependency injection framework. One app, one user, five screens —
a graph builder would be more machinery than the problem deserves. `GymApp`
constructs the repository and hands it to a single ViewModel.

## Design

Dark only, on purpose. This app is opened at 8:50 PM in a gym and again at 10 PM
in bed; it never meets daylight, and an app that is honestly dark beats one that
flashes white on launch.

Two accents that never swap roles: brass is always training, sage is always
food. A number's colour tells you which half of the app you are in before you
read its label. One animated element in the whole app — the meter fill.

The reasoning behind the palette, type scale and layout is in section 8 of
REQUIREMENTS.md, so that later changes are deliberate.

## Security and privacy

This app holds a health log — body weight over time, what you ate, what you
lifted. What that deserves, and what has been done about it:

**It cannot reach the network.** No networking code, no analytics, no SDKs. The
manifest goes further than simply not asking for `INTERNET`: it removes the
permission with `tools:node="remove"`, so if a dependency ever declares it, the
merge fails to grant it rather than quietly handing the app network access. You
can confirm this on any build by reading the merged manifest under
`app/build/outputs/logs/`.

**Backup is encrypted or it does not happen.** Android's auto-backup is the one
route by which data leaves the phone, and it is genuinely useful — it is what
makes the log survive a reinstall. `data_extraction_rules.xml` sets
`disableIfNoEncryptionCapabilities="true"`, so on Android 12+ the upload only
proceeds when the backup is end-to-end encrypted with your screen lock, which
Google does not hold. On a device that cannot do that, nothing is uploaded at
all. Android 11 and below have no equivalent switch; set
`android:allowBackup="false"` if that matters to you. The Settings screen says
all of this in the app rather than claiming more privacy than it delivers.

**The debug APK is debuggable.** That is what debug means, and it is the one
real exposure in the artifact you install: anyone with your unlocked phone and
USB debugging enabled can read the database out of a debuggable app with
`adb run-as`. If that is in your threat model, add four repository secrets
(`GYM_KEYSTORE_BASE64`, `GYM_KEYSTORE_PASSWORD`, `GYM_KEY_ALIAS`,
`GYM_KEY_PASSWORD` — the workflow comments carry the `keytool` command) and CI
also produces `gym-release-apk`, which is minified and not debuggable. Nothing
changes if you skip it. **The release build has not been run yet**, so expect to
iterate on R8 rules the first time.

**The boot receiver is reachable only by the system.** It has to be exported so
Android can deliver `BOOT_COMPLETED`, so it also carries
`android:permission="android.permission.RECEIVE_BOOT_COMPLETED"` — without that,
any installed app could invoke it directly.

**No injection surface.** Every query is a Room `@Query` with bound parameters;
nothing builds SQL by concatenation, and food search filters in Kotlin rather
than in SQL. There is no WebView, no reflection, no dynamic code loading, and
nothing is written to the log.

## Licence

Personal project. Do what you like with it.
