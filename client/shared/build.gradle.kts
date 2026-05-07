import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            freeCompilerArgs.addAll("-P", "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=com.colux.libretune.shared.parcelable.Parcelize")
        }
    }
    jvm("desktop") {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":libretune-extractor"))
                api(libs.ktor.client.core)
                api(libs.ktor.client.content.negotiation)
                api(libs.ktor.serialization.kotlinx.json)
                api(libs.ktor.client.logging)
                implementation(libs.kotlinx.serialization.json)
                api(libs.androidx.room.runtime)
                api(libs.androidx.paging.common)
                api(libs.koin.core)
                api(libs.koin.core.viewmodel)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmCommonMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(libs.github.teamnewpipe.newpipeextractor)
                implementation(libs.okhttp)
                api(libs.ktor.client.cio)
            }
        }
        val androidMain by getting {
            dependsOn(jvmCommonMain)
            dependencies {
                // BundledSQLiteDriver works on Android; on desktop it crashes with a JNI/glibc conflict.
                implementation(libs.androidx.sqlite.bundled)
                api(libs.koin.android)
            }
        }
        val desktopMain by getting {
            dependsOn(jvmCommonMain)
            dependencies {
                // Custom JDBC-backed SQLite driver using org.xerial:sqlite-jdbc.
                // BundledSQLiteDriver crashes on Linux JBR due to JNI/glibc C++ ABI conflict.
                implementation(libs.xerial.sqlite.jdbc)
            }
        }
    }
}

android {
    namespace = "com.colux.libretune.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 29
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspDesktop", libs.androidx.room.compiler)
}

ksp {
    arg("room.schemaLocation", "${projectDir}/schemas")
}
