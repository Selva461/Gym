# ─── Gym release shrinking, obfuscation and optimisation ──────────────────────
#
# Read the first block before changing anything here. A keep rule is a hole in the
# obfuscation, and the most common way an Android app ends up readable after R8 is
# somebody adding a broad -keep to fix a crash they never diagnosed.
#
# What this file is actually protecting: the shape of the code. There are no
# secrets in this APK — no API keys, no tokens, no server, no credentials — because
# the app has no network access at all. Your *data* is protected by the Android
# sandbox and device encryption, not by anything in this file. See SECURITY.md.


# ─── Reflection Room genuinely needs ─────────────────────────────────────────
#
# Room looks its generated implementation up by name at runtime —
# Class.forName("dev.avles.gym.data.GymDatabase_Impl") — so the @Database class
# has to keep its name and its constructor. Everything else Room generates is
# ordinary compiled code with no reflection, which is why this rule is one line
# and not a package wildcard.
#
# This replaced `-keepclassmembers class dev.avles.gym.data.** { *; }`, which kept
# every field of every entity under its original name. That rule was never needed
# — Room resolves columns at compile time through KSP — and it handed a decompiler
# the entire data model, table by table and column by column, in a build whose
# whole point was to be hard to read.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**

# Coroutines ships internals R8 cannot see through. Warnings only, no keeps.
-dontwarn kotlinx.coroutines.**


# ─── Readable crash reports without readable source ──────────────────────────
#
# Line numbers are kept so a release crash can still be traced, and the source
# file name is renamed so the trace does not name the original files. The mapping
# file that turns one back into the other is written to
# app/build/outputs/mapping/release/mapping.txt and must be archived with every
# release build — without it a release stack trace is unreadable to you too.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile


# ─── Logging cannot survive into a release build ─────────────────────────────
#
# Belt and braces alongside GymLog, which already compiles logging out in release.
# If a stray android.util.Log call is ever added by hand, R8 removes it here rather
# than letting a health log write body weights into logcat, where any app holding
# READ_LOGS on a rooted phone could read them.
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
}


# ─── Optimisation ────────────────────────────────────────────────────────────
#
# R8 in full mode already runs these; stating the passes explicitly makes the
# intent visible and survives a default changing underneath us.
-optimizationpasses 5
-allowaccessmodification
-repackageclasses
