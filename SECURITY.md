# Gym — security

## The short version

Your data is well protected. Your *code* cannot be made secret, and this document
explains why that is fine.

---

## "Nobody should be able to reverse engineer the app"

**This is not achievable on Android, by anyone, ever.** Not by this app, not by
your bank's app, not by Google's. It is worth understanding exactly why, because
the belief that it is achievable leads people to spend effort in the wrong place.

An APK is a file on a device you do not control. To run, every instruction in it
must be readable by the CPU — which means it is readable, full stop. `apktool` and
`jadx` are free, take about thirty seconds, and produce something close to source.
Obfuscation does not change this. It makes the output tedious to read: classes
become `a.b.c`, methods become `a()`, and following a code path takes hours
instead of minutes. That is a real cost imposed on an attacker. It is not a wall.

Commercial anti-tamper products (DexGuard, and similar) add packing, string
encryption and integrity checks. They raise the cost further, they are expensive,
they make crashes harder for *you* to diagnose, and they are also routinely
defeated. For a personal training log they would be a bad trade.

**So the right question is not "can they read my code" but "what would they get
if they did".** For this app, the answer is: nothing.

| What attackers usually extract from an APK | What is in this one |
|---|---|
| API keys, tokens | None — the app has no network access |
| Server endpoints | None — there is no server |
| Login credentials, OAuth secrets | None — there is no account |
| Encryption keys | None — see below |
| Proprietary algorithms | Double progression, which is public knowledge |
| Your personal data | **Not in the APK.** It is on your phone, in the app sandbox |

Your weights, meals and sessions are not in the APK and never were. Somebody
decompiling the app learns how a rest timer works. They learn nothing about you.

### What is done anyway

Obfuscation is still worth its (zero) cost, so:

- **R8 in full mode** — shrinking, optimisation and obfuscation on every release.
- **One keep rule**, for the one place Room genuinely uses reflection. A previous
  build carried `-keepclassmembers class dev.avles.gym.data.** { *; }`, which
  exempted every table and column from obfuscation and was never needed. A test
  now fails if a wildcard keep over app code reappears.
- **Resource shrinking**, so unused assets are not shipped.
- **`-repackageclasses` and `-allowaccessmodification`**, flattening the package
  structure in the output so the layering is not legible from the APK.
- **Source file names renamed**, line numbers kept. A release crash is still
  traceable *by you*, through `mapping.txt` — which is why that file must be
  archived with every release build, and must not be published.

---

## Photos

Progress photos are the most sensitive thing this app holds, and the first thing
in it that arrives from outside. Four decisions follow from that.

**They are copied in, not referenced.** The picker returns a `content://` URI
into another app's storage under a grant that dies with the process. Storing one
would work today and show nothing next week, so the bytes are copied into this
app's private directory and the URI is discarded.

**Private storage, never the gallery.** `filesDir` is readable only by this app's
UID. Writing them to MediaStore would put them in the phone's gallery beside
holiday pictures and hand them to every gallery app, cloud sync and lock-screen
slideshow on the device.

**They are re-encoded, which strips the metadata.** A 12 MB phone photo becomes a
few hundred KB, the orientation tag is baked into the pixels, and whatever else
the original carried — GPS coordinates, device serial, timestamps — does not
survive the round trip. A progress photo with the gym's location in it is a worse
thing to keep than one without.

**They are excluded from cloud backup, on purpose.** The rest of the data is
numbers; encrypted on a server with a key Google does not hold, a weight history
is a reasonable trade for surviving a lost phone. Photographs of a body are a
different question, and not one to answer on the user's behalf by defaulting to
upload. Device-to-device transfer does include them — that is a local link
between two phones the person is holding. The cost is real and the photos screen
states it: reinstalling loses them.

**No permission was added.** Picking uses the system photo picker, which runs
outside this process and returns exactly the one image chosen. `READ_MEDIA_IMAGES`
would grant access to every picture on the phone, forever, in order to import one.
The permission allowlist in `SecurityPostureTest` is unchanged.

### The filename is untrusted input

Everything else in this database was typed into it. A picked image carries a name
and a MIME type chosen by another app, and a filename is one string join away from
being a path. So the name is generated here, the extension comes from a fixed
list, and names read back out of the database are validated before they are
resolved — because a restored or corrupted database is exactly the case where "it
cannot happen" stops being true. `PhotosTest` checks this against inputs like
`../../databases/gym.db`.

---

## What actually protects your data

In rough order of how much work each one does:

1. **The Android application sandbox.** `/data/data/dev.avles.gym/` is readable
   only by this app's UID. No other installed app can open the database. This is
   kernel-enforced and it is doing most of the work.
2. **Full-disk encryption**, tied to your lock screen. A phone that is off, or
   locked and never unlocked since boot, does not give the files up even to
   someone who takes it apart.
3. **No network permission**, enforced at build time. Data cannot leave the device
   over the network because the process has no way to open a socket. This also
   means no analytics, no crash reporting, no telemetry — not as a policy, as a
   capability that does not exist.
4. **A non-debuggable release build.** A debuggable APK can have its database
   pulled out with `adb run-as` on an unlocked phone with USB debugging enabled.
5. **Backup that refuses to run unencrypted.** `disableIfNoEncryptionCapabilities="true"`
   means a phone with no screen lock uploads nothing rather than shipping a
   readable copy of your weight history to a server.

---

## Threat model

Stated plainly, because a security claim without a threat model is decoration.

| Threat | Protected? | By what |
|---|---|---|
| Another app on your phone reading your log | **Yes** | Kernel-enforced sandbox |
| The app leaking data over the network | **Yes** | No INTERNET permission exists |
| Data in logcat | **Yes** | GymLog is compiled out of release; R8 strips `android.util.Log` |
| Someone picking up your unlocked phone | **No** | Nothing stops them opening the app. See below |
| Phone stolen while locked | **Mostly** | Full-disk encryption, as strong as your lock screen |
| Forensic extraction from an unlocked, rooted phone | **No** | Neither the database nor the photos are separately encrypted. See below |
| Progress photos reaching Google's servers | **Yes** | Excluded from cloud backup; no network permission exists |
| Progress photos appearing in your gallery | **Yes** | Private storage, never MediaStore |
| Location or device data hidden in a photo | **Yes** | Re-encoding drops every EXIF tag but orientation |
| Malicious app with root | **No** | Root defeats the sandbox; nothing in userspace survives it |
| Someone decompiling the APK | **No, and it does not matter** | See the top of this document |
| Supply chain — a dependency turning hostile | **Partly** | No network to exfiltrate through; new permissions fail a test |

### The two real gaps, and what would close them

Neither is currently implemented. Both are honest options rather than
recommendations — they cost something, and for a personal log on a phone with a
lock screen the current posture is reasonable.

**An unlocked phone in someone else's hands.** A biometric or PIN gate on app
launch would close this. It is genuinely useful if you share a phone or leave it
on a desk. Cost: a prompt every time you open the app between sets.

**Database encryption at rest.** SQLCipher with a key held in the Android Keystore
(hardware-backed, non-exportable, optionally requiring user authentication) would
mean the database file is ciphertext even to an attacker who has defeated the
sandbox. Cost: a native dependency, a slower cold start, and a real risk of
permanent data loss — if the Keystore key is lost, which happens on some devices
after a lock-screen change or a factory reset, **the data is gone, with no
recovery**. That tradeoff deserves a decision rather than a default.

---

## What is enforced automatically

`SecurityPostureTest` fails the build if any of these is undone:

- INTERNET or ACCESS_NETWORK_STATE stops being actively removed
- a permission appears that is not on the allowlist with a stated reason
- a component becomes exported without being the launcher or permission-guarded
- the release build stops being minified, shrunk or non-debuggable
- a wildcard keep rule exempts app code from obfuscation
- `android.util.Log` stops being stripped
- signing material other than the published debug key appears in the repository

`ArchitectureTest` additionally fails if `android.util.Log` is called anywhere
outside `GymLog`, which is what makes the logging guarantee hold in the first
place.

These are the controls most likely to be reversed by accident — by a dependency,
or by someone silencing a crash in a hurry. A document alone would not have
caught any of them.

---

## Reporting

This is a personal app with one user and no server. If you find something, open an
issue on the repository.
