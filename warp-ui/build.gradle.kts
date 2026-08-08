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
        namespace = "com.atriidev.warp_ui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }

    }

    val xcfName = "warp-uiKit"
    // Bridge cinterop name must differ from the SPM product name (avoids package cycle).
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
                implementation(project(":warp-runtime"))
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
        // Empty bridge (src/swift/warpBridge) — sources live in the SPM package.
        dependency {
            localPackage(
                path = warpWidgetKitPackageDir.absolutePath,
                packageName = "warpWidgetKit",
                products = {
                    // ObjC (@objcMembers) API → Kotlin `import warpWidgetKit.*`
                    add("warpWidgetKit", exportToKotlin = true)
                },
            )
        }
        // Xcode links the same product (local package now; remote URL later)
        exportedPackageSettings {
            includeProduct = listOf("warpWidgetKit")
        }
    }
}
