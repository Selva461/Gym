# Gym — phase 2

Phase 1 built the log: sessions, sets, food, targets, 394 lifts and 439 foods.
Phase 2 is about **knowing what you are doing and seeing that it worked** — a
reference on every lift, variations tracked properly, and a photo record.

---

## The GIF question, answered first

The request was for the app to show what each exercise is, the way the GymVisual
animation does. Three facts decide how that gets built:

1. **The app has no network access, deliberately.** `INTERNET` and
   `ACCESS_NETWORK_STATE` are stripped from the merged manifest with
   `tools:node="remove"`, so no dependency can quietly reintroduce them. Nothing
   can be downloaded at runtime. Reversing that would trade the single strongest
   security property this app has for some pictures, and it is not worth it.
2. **Those GIFs are somebody's licensed product.** GymVisual sells them. Scraping
   them into an APK is not something this project will do.
3. **394 animated GIFs is 200–400 MB.** That is not an app you can install, let
   alone update over mobile data.

So the reference gets **built rather than borrowed**, in three layers that
between them do the job the GIF was doing:

| Layer | What it gives you | Covers |
|---|---|---|
| **Written reference** | Setup, execution, cues, the mistakes that actually happen | All 394, automatically |
| **Muscle map** | A body diagram with the worked muscles shaded — the same information the red shading in the GIF carries | All 394, automatically |
| **Your own picture** | Attach any image or GIF from your phone to any lift | The ones you care about |

The first two are **derived from the lift itself** — its name already encodes the
equipment, its muscle group and movement pattern give the rest. That is the part
that matters for "and more that gets added need an exercise reference": a lift you
invent tomorrow gets a reference the moment you name it, because nothing is
hand-written per row. The third layer is the legal way to get the actual GymVisual
image in for the twenty lifts you actually do.

---

## Variations, and how the reps are calculated

The question was how reps get calculated if you change grip. The answer decides
the data model, so it is worth stating plainly.

**A variation is an attribute of the set, not a new exercise.** You pick "Barbell
bench press", then optionally a grip chip — close, wide, standard. The catalogue
stays 394 rows instead of exploding to two thousand.

Then the rules:

- **Progression is keyed on the lift *and* the variation.** Close-grip bench at
  60 kg and wide-grip bench at 60 kg are different lifts as far as "what do I do
  next" is concerned. If they shared one history, doing twelve reps on the easier
  grip would tell you to add weight to the harder one. That is the bug this whole
  design exists to avoid.
- **Records are keyed the same way.** A PR is a PR on that variation. A close-grip
  best does not silently erase a wide-grip best.
- **Volume is not keyed on variation.** Session and daily volume sum across every
  grip, because volume is a fatigue number and fatigue does not care how you held
  the bar.
- **No variation is the default**, and it behaves exactly like today. Every set
  already logged keeps its meaning — none of them retroactively become
  "standard grip", they stay unspecified, because that is what they were.

Which axes a lift offers depends on the movement: a row offers grips, a leg press
offers stance, a bench offers grip and bench angle, a curl offers grip and
supination. Derived from the movement pattern, same mechanism as the reference.

---

## Tiers

Ordered by dependency. Each one is shippable on its own.

### Tier 1 — Reference data — **done**
Every exercise resolves to a structured guide: equipment, movement pattern,
primary and secondary muscles, setup, execution, cues, common mistakes, and which
variation axes it offers. **Derived, not stored** — computed from the exercise
row, so there is no migration, no per-row data entry, and a custom lift is covered
the moment it is created. Pure Kotlin, fully testable off-device.

### Tier 2 — Reference UI — **done**
"How to do it" under the lift opens a sheet with the Tier 1 guide and a **muscle
map drawn in Compose** — front and back views, primary shaded solidly, secondary
at a third of the intensity, laid out in a normalised box and scaled at draw
time. Vectors, so kilobytes and sharp at any size.

### Tier 3 — Variations — **done**
`variation` on the set (schema v4). Grip and stance chips under the lift, driven
by Tier 1's axes, with an explicit "Any" for unspecified. Progression and records
re-keyed to (exercise, variation); volume deliberately not. Nine tests, each
naming the failure it prevents.

### Tier 0 — Foundation — **done**
Not in the original plan; added when the request came to make the app debuggable,
extendable and secure. `GymLog`, `ArchitectureTest`, `SecurityPostureTest`,
`SECURITY.md`, `ARCHITECTURE.md`, and a ProGuard file that no longer exempts the
data layer from obfuscation.

### Tier 4 — Daily photos
A progress photo per day, stored in app-private storage — not the shared gallery,
so it does not land in Google Photos or any backup. Timeline by date, and a
side-by-side compare of any two days. Includes the storage, permission and
lifecycle work that Tier 5 then reuses.

### Tier 5 — Your own image per lift
Attach a picture or GIF from the phone to any exercise, shown at the top of its
reference sheet. Reuses Tier 4's storage pipeline entirely.

---

## Not in phase 2

- **Downloading anything.** See above. The no-network guarantee stays.
- **Video.** Storage and playback cost far more than they return here; a still
  image plus written cues covers the actual question, which is "which way does
  this thing move".
- **Form checking from a photo.** Requires a model, a camera pipeline and a
  network round trip, and would be wrong often enough to be dangerous.
