plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

/**
 * Release signing is opt-in and driven entirely by environment variables, so no
 * keystore and no password ever lives in this repository. With none of them set a
 * release build is simply left unsigned; the debug build is untouched either way.
 */
val releaseKeystore: String? = System.getenv("GYM_KEYSTORE_PATH")
val canSignRelease = !releaseKeystore.isNullOrBlank() && file(releaseKeystore).exists()

android {
    namespace = "dev.avles.gym"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.avles.gym"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        /**
         * A fixed debug key, committed to the repository on purpose.
         *
         * Without this, AGP generates ~/.android/debug.keystore on whatever machine
         * is building — and a fresh CI runner has no such file, so it makes a new
         * random one every run. Android refuses to install an APK over an existing
         * app signed by a different key, so every build was un-installable over the
         * last one: the only way to take an update was to uninstall first, losing
         * every set and meal logged.
         *
         * Committing it costs nothing that matters. These are Android's own
         * documented debug defaults, password and all, and a debug APK is debuggable
         * regardless — the key protects nothing. It is not, and must never become,
         * the release key: that one stays in repository secrets and never lands here.
         */
        getByName("debug") {
            storeFile = file("keystore/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }

        if (canSignRelease) {
            create("release") {
                storeFile = file(releaseKeystore!!)
                storePassword = System.getenv("GYM_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("GYM_KEY_ALIAS")
                keyPassword = System.getenv("GYM_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // A debug APK is debuggable by definition, which means `adb run-as` can read
            // the database straight out of it on an unlocked phone with USB debugging on.
            // For a health log that is the one exposure worth engineering around, and a
            // signed release build is the answer.
            isDebuggable = false
            if (canSignRelease) signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        // GymLog compiles itself out of release builds by branching on
        // BuildConfig.DEBUG, which R8 then folds away entirely. That needs
        // BuildConfig generated, which AGP 8 no longer does by default.
        buildConfig = true
    }

    // Audit pass: lint runs in CI and publishes an HTML report, but a style
    // warning must never block a working APK from being produced.
    lint {
        abortOnError = false
        warningsAsErrors = false
        checkDependencies = true
        htmlReport = true
        xmlReport = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

// MigrationSqlTest compares the hand-written v3 migration against the schema Room
// exports here. Handing it the directory as a system property removes any guess
// about the test task's working directory — the one assumption that is invisible
// until it is wrong, and which reads as "the file is missing" when it is not.
tasks.withType<Test>().configureEach {
    systemProperty("gym.schemaDir", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
}
