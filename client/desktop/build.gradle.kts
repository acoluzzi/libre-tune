import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(project(":shared"))
    implementation(project(":libretune-extractor"))
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation("io.ktor:ktor-client-core:3.2.3")
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
}

compose.desktop {
    application {
        mainClass = "com.colux.libretune.desktop.MainKt"

        // BundledSQLiteDriver's JNI crashes in std::ctype<wchar_t> locale code when loaded
        // under JBR on some Linux configurations. Force the C locale to avoid it.
        jvmArgs += listOf("-Duser.language=en", "-Duser.country=US")

        nativeDistributions {
            targetFormats(
                TargetFormat.Deb,
                TargetFormat.AppImage,
                TargetFormat.Rpm,
            )
            packageName = "LibreTune"
            packageVersion = "1.0.0"
            description = "LibreTune Desktop"
            vendor = "LibreTune"

            linux {
                packageName = "libretune"
            }
        }
    }
}
