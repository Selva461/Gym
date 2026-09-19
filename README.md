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
Train      split → lift → kg × reps, a target for every lift, rest timer, records
           tap "How to do it" for a muscle map and how the movement works
Fuel       search or browse 538 foods by category, macros and 15 micronutrients
Progress   weight trend, 14-day volume, streaks, and progress photos
Settings   your targets, rest length, nightly reminder
```

**486 lifts**, each with its own rep range and increment, filed under thirteen
real muscle groups rather than three buckets:

```
Chest 59   Back 61   Shoulders 51   Traps 22   Triceps 43   Biceps 34
Forearms 24   Quads 46   Hamstrings 26   Glutes 28   Calves 19   Core 46
Full body 27
```

**Equipment is part of the lift's name**, which makes the picker's search box an
equipment filter without needing one. Type `cable` and you get the 28 cable
movements, `dumbbell` the 43, `machine` the 37, `smith` the 12. Triceps alone
carries 37: thirteen on a cable (rope, bar, single-arm, overhead, kickback,
cross-body), eight with dumbbells, seven with a barbell or EZ bar, three on a
machine, six bodyweight. Whatever is free, there is a way to train the muscle you
came for.

**A demonstration on 140 of them.** The reference sheet animates between the start
and the finish of the movement — which is what almost every exercise GIF is once you
strip the tweening, at a tenth of the size. The pictures are public domain (Free
Exercise DB, Unlicense); the commercial animations people usually mean are somebody's
product and this app will not ship those. Only lifts with a confident name match get
one: "Barbell row" scores 0.82 against "Upright Barbell Row" and they are different
exercises, so anything less than certain shows the muscle map instead of the wrong
movement. Bundled in the APK at about 3.6 MB, because the app has no network
permission and never will.

**A reference on every lift.** Tap "How to do it" under the lift for a body
diagram with the worked muscles shaded, how to set up, how the movement goes, the
two words to hold in your head mid-set, and what usually goes wrong. It is derived
from the lift itself rather than written out four hundred times, so a lift you
invent tomorrow has one the moment you name it. There are no downloaded
animations: the app has no network permission, those GIFs are somebody's licensed
product, and four hundred of them would be several hundred megabytes. The map is
drawn in code instead — vectors, kilobytes, sharp at any size.

**Grip and stance are logged per set.** A close-grip bench and a wide-grip bench
keep separate histories and separate records, because sharing one means twelve
reps on the easier grip tells you to add weight to the harder one. Volume does not
split — that is a fatigue number, and fatigue does not care how you held the bar.
Leave it on "Any" and nothing changes.

**Progress photos, kept out of your gallery.** Add one from the Progress tab. They
are copied into the app's private storage — not MediaStore, so no gallery app,
cloud sync or lock-screen slideshow ever sees them — re-encoded so the GPS
coordinates and device serial in the original do not come along, and deliberately
left out of cloud backup. The oldest and newest sit side by side with the days
between them, because adjacent weeks look identical and the distance travelled is
the point. The trade is stated on the screen: reinstalling loses them.

**Your own picture on any lift.** The reference sheet takes one from your phone
and shows it at the top. This is the honest way to get the demo image you wanted:
the app has no network permission and will not ship somebody else's licensed
animation, but it will happily hold the picture you put there. Picking uses the
system photo picker, so the app never asks for access to your photos.

**Ten starter splits** — Push, Pull, Legs, Upper, Lower, Full body, Chest and
triceps, Back and biceps, Shoulders and arms, Arms — and you can build your own.
Starting a session from a split names it and puts that split's lifts on the
screen, in order, one tap each. That is the whole point: not picking from a list
of 486 between two sets. Splits are editable, reorderable and deletable, and
deleting one leaves every session already run from it untouched.

**538 foods in 25 categories.** South Indian cooking gets the detail it deserves —
twenty-four dosa varieties (masala, Mysore masala, ghee roast, paper, rava, set,
neer, kal, karam, ragi, kambu, thinai, oats, pesarattu, adai and the rest), seven
kinds of idli, forty-three tiffin items (pongal, upma, uttapam, kuzhi paniyaram,
sevai, ragi mudde, kambu koozh, bonda, bajji, vada curry), twenty-eight rice
dishes (biryani, kuska, puliyodarai, thakkali sadam, ellu sadam, vangi bath) and
forty-one sides from sambar to ennai kathirikai and paruppu usili. Sixty-two
protein entries cover chicken chukka, nattu kozhi kuzhambu, mutton pepper fry,
kola urundai, karuvadu kuzhambu, eral thokku and the rest of the coast. Then
North Indian, Italian, Chinese and Indo-Chinese, Middle Eastern, Japanese,
Mexican and Continental, plus dairy, legumes, vegetables, fruit, nuts, sweets,
drinks and oils.

**Juices are their own category**, not filed under Drinks. A glass of sugarcane
juice is 180 kcal of almost pure sugar; a tender coconut is 46. Among the teas and
coffees that difference disappears, and in a list of its own it is the first thing
you see. Twenty-three of them — sugarcane, mosambi, amla, ash gourd, beetroot,
carrot, the milkshakes and the lassis.

**33 supplements** — whey, casein, creatine, fish oil, cod liver oil, magnesium
glycinate and citrate, D3, B12, zinc, calcium, iron, multivitamin, electrolytes,
psyllium and the rest. They file themselves under their own slot rather than
landing in dinner, and their numbers come off a label so they are the one honest
corner of the catalogue.

### On the numbers

**Macros are estimates.** Home-style portions vary enormously with oil, coconut
and ghee. Every food is editable in place, micronutrients included. Correcting the
five or six you actually eat every week is worth more than any number of entries.

**Micronutrients are rougher still**, and the app says so where you read them.
Whole foods — eggs, milk, fish, spinach, nuts — are close to published values.
Composite dishes are built up from their main ingredients and should be read as
"that was a decent source of iron", never as a measurement.

**A zero means not estimated, not proven absent.**

## Fixing what you logged

Tapping a logged set opens it — weight, reps, warm-up — with delete inside.
Tapping a food entry opens its quantity and meal the same way. Both used to delete
on a single tap, on the theory that a mis-logged entry is cheap to re-enter. That
was wrong in practice: the usual mistake is one wrong digit, where re-entering the
whole thing is worse than a dialog, and an accidental tap destroyed work.

Past sessions are editable too. Tap one in the recent list to rename it, re-open it
to add more sets, fix any set on it, or delete it. Deleting a session asks twice and
tells you how many sets go with it — unlike a single set, that can be an hour of
work and there is no undo.

## Targets, and where they come from

Pick a lift and the Train screen shows what to try next, taken from your own logged
history and nothing else:

```
Next target
82.5 kg × 6                                    [Use]
Last time 7 at 82.5 kg. One more rep, same weight — at 8 it goes up.
────────────────────────────────────────────────
estimated max 96 kg                   next 100 kg
```

It is double progression, the least clever scheme that works. Every lift has a rep
range; you stay at one weight until you hit the top of it, then the weight goes up
and the reps reset to the bottom. Ranges and increments are per lift, because a
squat absorbs a 5 kg jump and a lateral raise does not. Three sessions at one weight
without the reps moving gets called a stall, and the panel says so.

Nothing is computed from your age, weight or "level", and the app will not invent a
starting weight for a lift you have never done — it says so and waits. Tapping
**Use** fills the weight and reps fields, because a target that needs typing is no
faster than remembering.

The nutrient panel works the same way: the reference intakes are the published adult
figures, shown so a day's log reads as "low on iron" rather than as a bare number.
They are not a prescription, the app never tells you to supplement anything, and
anything you mean to act on — especially doses — belongs with a doctor or dietitian
rather than a phone app.

## Structure

```
data/      Room entities, one DAO, repository, seed catalogue, DataStore settings
domain/    pure Kotlin — macro and micronutrient maths, 1RM and records, streaks,
           progression. All of it unit tested.
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
