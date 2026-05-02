import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(project(":shared"))
}

compose.desktop {
    application {
        mainClass = "com.colux.libretune.desktop.MainKt"

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
