import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.composeCompiler)
    id("io.github.frankois944.spmForKmp") version "1.9.4"
    id("com.warp.plugins.publish")
}


/** Local SPM package (repo root). Swap for remotePackageVersion when published. */
val warpWidgetKitPackageDir: File =
    rootProject.layout.projectDirectory.dir("warpWidgetKit").asFile

kotlin {
    android {
        namespace = "com.atriidev.warp_widget"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }

    }

    val xcfName = "warp-WidgetKit"
    val bridgeCinterop = "warpBridge"

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = xcfName
        }
        target.compilations.getByName("main") {
            cinterops.create(bridgeCinterop)
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.compose.runtime)
                implementation(libs.kotlinx.serialization.json)
                api(project(":warp-runtime"))
                api(project(":warp-ui"))
            }
        }
        androidMain {
            dependencies {
                implementation(libs.compose.ui)
                implementation(libs.androidx.glance.appwidget)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

swiftPackageConfig {
    create("warpBridge") {
        minIos = "17.0"
        dependency {
            localPackage(
                path = warpWidgetKitPackageDir.absolutePath,
                packageName = "warpWidgetKit",
                products = {
                    add("warpWidgetKit", exportToKotlin = true)
                },
            )
        }
        exportedPackageSettings {
            includeProduct = listOf("warpWidgetKit")
        }
    }
}
