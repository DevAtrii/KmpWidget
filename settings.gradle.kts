rootProject.name = "KmpWidget"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}



dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        val mavenLibsDir = rootDir.resolve(".maven-libs")
        if (mavenLibsDir.exists()) {
            maven(mavenLibsDir.toURI()) {
                name = "mavenLibsLocal"
                content {
                    includeGroup("io.github.devatrii")
                }
            }
        }
    }
}

includeBuild("build-logic")
include(":androidApp")

include(":shared")

include(":warp-runtime")
include(":warp-ui")
include(":warp-widget")
