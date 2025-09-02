plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    kotlin("plugin.serialization") version "1.9.24"
}

android {
    namespace = "com.coluzziandrea.libretune_extractor"
    compileSdk = 36

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform() // This tells Gradle to use the JUnit 5 runner
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)


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


    // Core Kotlin/Java
    implementation(libs.androidx.core.ktx.v1131)

    // Hilt (for dependency injection in the data layer)
    implementation(libs.hilt.android.v2511)
    kapt(libs.hilt.compiler.v2511)


    // OkHttp & Jsoup
    implementation(libs.okhttp.v4120)
    implementation(libs.jsoup.v1172)


    implementation(libs.kotlinx.serialization.json)



    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)


    testImplementation(kotlin("test"))
}