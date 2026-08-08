import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
}

subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<KotlinMultiplatformExtension>{
            compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
}

private val publishableWarpModules =
    listOf(
        ":warp-runtime",
        ":warp-ui",
        ":warp-widget",
    )

tasks.register("publishWarpLibrariesToLocalRepository") {
    group = "publishing"
    description =
        "Publishes all Warp libraries (:warp-runtime, :warp-ui, :warp-widget) to .maven-libs."
    dependsOn(
        publishableWarpModules.map { "$it:publishAllPublicationsToWarpLocalRepository" },
    )
}

tasks.register("publishWarpLibrariesToMavenCentral") {
    group = "publishing"
    description = "Publishes all Warp libraries (:warp-runtime, :warp-ui, :warp-widget) to Maven Central."
    dependsOn(
        publishableWarpModules.map { "$it:publishAllPublicationsToMavenCentralRepository" },
    )
}