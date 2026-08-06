import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.composeCompiler)
    id("io.github.frankois944.spmForKmp") version "1.9.4"
}

kotlin {
    android {
        namespace = "com.atriidev.warp_ui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    val xcfName = "warp-uiKit"

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = xcfName
        }
        target.compilations.getByName("main") {
            cinterops.create("warpWidgetKit")
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
    create("warpWidgetKit") {
        minIos = "17.0"
    }
}
