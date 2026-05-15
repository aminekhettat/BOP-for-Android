plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.kotlinx.kover")
    id("org.jetbrains.dokka")
}

android {
    namespace = "org.blindsystems.bop"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.blindsystems.bop"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
        unitTests.isIncludeAndroidResources = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    }
    // composeOptions block removed — Kotlin 2.0+ uses the compose compiler plugin directly
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core KTX
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("androidx.activity:activity-compose:1.10.1")

    // Compose BOM (2025-05)
    implementation(platform("androidx.compose:compose-bom:2025.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // ViewModel for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")

    // Media3 (ExoPlayer) — audio playback, speed & pitch control
    implementation("androidx.media3:media3-exoplayer:1.6.1")
    implementation("androidx.media3:media3-session:1.6.1")
    implementation("androidx.media3:media3-ui:1.6.1")

    // DataStore Preferences — persisted settings
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    // Kotlin Serialization — JSON segment/history persistence
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Tests - JUnit 5 & MockK
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    // Robolectric — test Android components without device
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.test.ext:junit:1.2.1")

    // UI Tests & Accessibility
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-accessibility:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.05.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("io.mockk:mockk-android:1.13.16")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kover {
    reports {
        total {
            filters {
                excludes {
                    // Exclude packages
                    packages("org.blindsystems.bop.ui")
                    
                    // Exclude specific classes
                    classes(
                        "org.blindsystems.bop.MainActivity",
                        "org.blindsystems.bop.MainActivity\$*",
                        "org.blindsystems.bop.BopPlaybackService",
                        "org.blindsystems.bop.BopPlaybackService\$*",
                        "org.blindsystems.bop.AudioPlayerManager",
                        "org.blindsystems.bop.AudioPlayerManager\$*",
                        "org.blindsystems.bop.infra.SettingsRepository",
                        "org.blindsystems.bop.infra.SettingsRepository\$*",
                        "org.blindsystems.bop.infra.SettingsRepositoryKt",
                        "*.ComposableSingletons*",
                        "*\$\$serializer",
                        "*.BuildConfig"
                    )
                }
            }
        }
    }
}

detekt {
    toolVersion = "1.23.8"
    config.setFrom(files("$projectDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    source.setFrom(files("src/main/java", "src/test/java"))
}
