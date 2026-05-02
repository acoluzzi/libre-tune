plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")

    kotlin("plugin.serialization") version "1.9.24"
}

android {
    namespace = "com.colux.libretune"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.colux.libretune"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "BACKEND_BASE_URL",
            "\"https://libretune.coluzziandrea.com\""
        )
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        isCoreLibraryDesugaringEnabled = true

    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    // Ktor Client Core
    implementation(libs.ktor.client.core)
    // CIO is a good default engine for Android
    implementation(libs.ktor.client.cio)

    // This plugin handles automatic JSON serialization/deserialization
    implementation(libs.ktor.client.content.negotiation)
    // This tells the plugin to use kotlinx.serialization
    implementation(libs.ktor.serialization.kotlinx.json)

    // Optional: for logging network requests, very useful for debugging
    implementation(libs.ktor.client.logging)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.palette.ktx)

    implementation(libs.androidx.core.splashscreen)


    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)
    coreLibraryDesugaring(libs.desugar.jdk.libs)


    implementation(libs.androidx.material.icons.extended.android)

    implementation(libs.hilt.android)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.foundation)
    implementation(libs.material3)
    implementation(libs.androidx.animation)
    kapt(libs.hilt.compiler)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)


    implementation(libs.github.teamnewpipe.newpipeextractor)
    implementation(libs.retrofit)

    implementation(libs.kotlinx.serialization.json.v170)


    // Moshi for parsing JSON
    implementation(libs.converter.moshi)
    implementation(libs.moshi.kotlin)


    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // For fetching the web page
    implementation(libs.okhttp)

    // For parsing the HTML
    implementation(libs.jsoup)

    implementation(libs.androidx.media3.session)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    kapt(libs.androidx.hilt.compiler)


    implementation(project(":libretune-extractor"))
    implementation(project(":shared"))


    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}