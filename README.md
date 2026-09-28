# Gym

A training and food log for one person, one phone, no network — on Android and
on iPhone, from one codebase. Kotlin Multiplatform, Compose Multiplatform, Room.
Built on GitHub Actions because the phone is the whole development environment.

Full spec: [REQUIREMENTS.md](REQUIREMENTS.md)

---

## Read this first

**It builds.** CI is green: the whole app compiles — Kotlin, Compose, Room's
generated code — lint passes, the 180 unit tests pass, and `gym-debug-apk` comes
back as an artifact. The pinned toolchain below is a working set, not a guess.

What has *not* happened is anyone running it on a phone. Compiling is not the
same as behaving, so the manual pass in section 9 of
[REQUIREMENTS.md](REQUIREMENTS.md) is still worth walking through once — in
particular that the rest timer fires, an open session survives a restart, and
editing a seeded food recomputes past entries.

**If a later build fails, look at versions before you look at code.** The
toolchain in `gradle/libs.versions.toml` is pinned as a matched set that is
known to work together. Kotlin, KSP, Compose Multiplatform and the JetBrains
lifecycle/navigation ports move **together**: Kotlin/Native reads a library only
if it was built by the same or an older compiler, so bumping one alone breaks
the iOS build even when Android still compiles.

```toml
agp                  = "8.10.1"
kotlin               = "2.1.21"
ksp                  = "2.1.21-2.0.2"   # must track the Kotlin version exactly
composeMultiplatform = "1.8.2"
jetbrainsLifecycle   = "2.9.1"
jetbrainsNavigation  = "2.9.0-beta03"
room                 = "2.7.1"
```

## Getting the APK

1. Push to any branch. `.github/workflows/android.yml` has no branch filter, so
   the build runs wherever the work is — you do not have to merge to `main` to
   get an APK.
2. Download it on the phone from the fixed address every green build updates:
   `https://github.com/Selva461/Gym/releases/download/latest-debug/gym-debug.apk`
   (or: **Actions** tab → the run → **Artifacts** → `gym-debug-apk`).
3. Unzip on the phone and install. Android will ask you to allow installs from
   that source once.

The APK is signed with the standard debug key — fine for your own phone,
not publishable. The app id is `dev.avles.gym.debug`, so it installs
alongside anything else without conflict.

Two other artifacts come back from every run: `unit-test-report` and
`lint-report`. The tests gate the build; lint does not, deliberately — a style
warning should never stand between you and a working APK.

## Installing on an iPhone

The iOS build is `.github/workflows/ios.yml`. It runs when `main` moves, or on
demand from **Actions → Build iOS app → Run workflow** (it is not run on every
push: this repository is private, and GitHub bills a Mac minute as ten Linux
ones). Each run publishes the app at a fixed address:

    https://github.com/Selva461/Gym/releases/download/latest-ios/gym-unsigned.ipa

**It is unsigned, and that is not something a build can fix on its own.** iOS
installs only apps signed for the device, by an Apple ID. So the last step
happens on your side, once:

1. On a Windows PC or a Mac, install **Sideloadly** (or **AltStore**).
2. Plug the iPhone in, drop `gym-unsigned.ipa` onto Sideloadly, sign in with your
   Apple ID, press Start. It signs the app for your phone and installs it.
3. On the iPhone: Settings → General → VPN & Device Management → trust your
   Apple ID. On iOS 16 and later, also turn on Settings → Privacy & Security →
   Developer Mode (the phone restarts once).

With a **free** Apple ID the signature lasts **7 days**; re-run step 2 to renew
it (your data stays). With a paid Apple Developer account ($99/year) it lasts a
year, and CI can sign the IPA itself if you add the certificate and
provisioning profile as repository secrets — say so and that is a small change.

The iPhone app is the same app as the Android one, compiled from the same code:
same screens, same catalogue, same Indian dataset, same exercise demonstrations.
Its data lives in the app's private Application Support directory; photos are
excluded from iCloud backup, as they are left out of Android's cloud backup.

## What's in it

```
Today      protein first, calories second, a week strip with a dot per logged day
Train      split → lift → kg × reps, a target for every lift, rest timer, records
           tap "How to do it" for a muscle map and how the movement works
Fuel       search or browse 675 foods by name or local name, macros and 15 micronutrients
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

**614 built-in foods in 25 categories.** South Indian cooking gets the detail it deserves —
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

**Tamil curries, properly.** Beyond the everyday sambar, rasam and kuzhambus, 76
more: every kuzhambu named for what goes in it (vendakkai, kathirikai, poondu,
vengaya, manathakkali, kollu, karamani, paruppu urundai), sambar as it actually
varies (vengaya, murungakkai, mullangi, arachuvitta, tiffin, keerai), kootus,
poriyals and roasts, the herbal rasams (mudakathan, thuthuvalai, kollu), tiffin
gravies (poori masala, Bombay chutney, kosamalli, kothsu, empty salna, vellai
kurma) and the Chettinad, Kongu and coastal non-vegetarian dishes (salna, uppu
kari, paya, kudal, Pallipalayam chicken, nandu and eral kuzhambu, karuvadu).
Each carries its English names too, so "brinjal", "drumstick" or "horse gram"
finds it. Their calories are computed from their macros rather than typed, so
none can disagree with itself.

**Juices are their own category**, not filed under Drinks. A glass of sugarcane
juice is 180 kcal of almost pure sugar; a tender coconut is 46. Among the teas and
coffees that difference disappears, and in a list of its own it is the first thing
you see. Twenty-three of them — sugarcane, mosambi, amla, ash gourd, beetroot,
carrot, the milkshakes and the lassis.

**61 more from the Indian Food Nutrition Dataset (v2)**, bundled in the app and
added on first launch: the paratha family (aloo, mooli, gobhi, matar, keema, dal,
besan-palak), makki ki roti, kheers, halwas, phirni, dhokla, sandwiches, rolls,
Hyderabadi biryani, khichdi and a shelf of drinks. The file holds 724 rows but only
about 120 dishes — each dish repeated at a dozen weights — so each dish becomes one
food. A countable thing keeps its own unit ("1 paratha (56 g)"); anything served in a
cup, glass or bowl is per 100 g or 100 ml, because a vessel is whatever size yours is.

Of the rest, 38 were already here, some under another name, and 23 were **declined
rather than imported**, each for a stated reason: six had calories but no macros, three
declared calories their own macros contradict, and the deep-fried recipes (pakoras,
samosas, cutlets, stuffed pooris) count every drop of the frying oil, arriving at up to
485 g of fat on a plate. Those numbers agree with each other and are still not food.
The rules live in `FoodChecks` — the same gate every built-in food passes — and the
file's own README is beside it in `assets/foods/`.

Every food can carry the other names it goes by. Search matches them, so "aloo" finds
the potato paratha and "chawal kheer" finds rice kheer, and the editor shows where a
food's numbers came from — until you change them, when they become yours.

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

**Assisted machines run the other way.** On an assisted pull-up or dip machine the
weight you pin is help — more kilos is easier. Those lifts are flagged (by name, so a
custom "Assisted chin-up" is too) and everything turns round: at the top of the range
the *help* comes down, a record is doing as many reps with less help than ever
before, the milestone counts down towards the unassisted lift, and the help is never
added to your volume. The weight field says "kg of help" so the number is never read
the wrong way.

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

One Gradle module, `app`, compiled for two platforms.

```
app/src/commonMain/   everything that is the same on both phones — nearly all of it
  data/               Room entities, one DAO, repository, seed catalogue, the
                      Indian dataset importer, DataStore settings
  domain/             pure Kotlin — macro and micronutrient maths, 1RM and records,
                      streaks, progression, food checks and search. All unit tested.
  ui/                 one ViewModel, the screens, a small set of shared components
  core/               the log, the clock and calendar
  composeResources/   exercise demonstrations and the food dataset, bundled
app/src/androidMain/  Android: GymApp, MainActivity, notifications, photo store
app/src/iosMain/      iOS: MainViewController, notifications, photo store
iosApp/               the Xcode project (XcodeGen spec) and a few lines of Swift
```

What differs between the platforms is kept to a handful of seams, each small
enough to read in one sitting:

| Seam | Android | iOS |
| --- | --- | --- |
| `PhotoStore` | copies from a content URI, applies EXIF | redraws a UIImage upright |
| `Notifier` | AlarmManager + broadcast | UNUserNotificationCenter triggers |
| `rememberImagePicker` | system photo picker | PHPickerViewController |
| `decodeImageFile` | BitmapFactory, sampled | Skia, scaled |
| `writePlatformLog` | logcat | NSLog |
| database / settings location | the files every earlier version wrote | Application Support |

`commonMain` may not import `android.*` or `java.*` — ArchitectureTest and
`tools/check-imports.py` both refuse it — so a change that would break the iOS
build is caught on the Linux build first.

There is no dependency injection framework. One app, one user, a handful of
screens — a graph builder would be more machinery than the problem deserves.
Each platform's entry point makes the four things only it can make and hands
them to `AppContainer`, which assembles the rest identically on both.

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
