import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

/**
 * Publishes Warp KMP library modules (:warp-runtime, :warp-ui, :warp-widget)
 * to Maven Central / `.maven-libs`.
 */
class KmpLibraryPublishPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val path = target.path
        require(path in PUBLISHABLE_MODULE_PATHS) {
            "Module $path is not configured for Maven publishing. Add it to PUBLISHABLE_MODULE_PATHS."
        }

        val libs = target.extensions.getByType<VersionCatalogsExtension>().named("libs")
        val major = libs.findVersion("lib-version-major").get().requiredVersion
        val minor = libs.findVersion("lib-version-minor").get().requiredVersion
        val patch = libs.findVersion("lib-version-patch").get().requiredVersion
        val libVersion = "$major.$minor.$patch"
        val groupId = "io.github.devatrii"
        val artifactId = mavenArtifactId(path)

        target.plugins.apply("com.vanniktech.maven.publish")

        val publishToCentral =
            target.mavenCentralCredentialsPresent() && target.signingCredentialsPresent()

        target.extensions.configure<MavenPublishBaseExtension>("mavenPublishing") {
            coordinates(groupId, artifactId, libVersion)
            pom {
                name.set(artifactId)
                description.set(
                    "Warp KMP library ($artifactId). See https://github.com/DevAtrii/Warp",
                )
                inceptionYear.set("2025")
                url.set("https://github.com/DevAtrii/Warp")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("DevAtrii")
                        name.set("Athar Zaman")
                        url.set("https://github.com/DevAtrii/")
                    }
                }
                scm {
                    url.set("https://github.com/DevAtrii/Warp/")
                    connection.set("scm:git:git://github.com/DevAtrii/Warp.git")
                    developerConnection.set("scm:git:ssh://git@github.com/DevAtrii/Warp.git")
                }

            }
            if (publishToCentral) {
                publishToMavenCentral(automaticRelease = true)
                signAllPublications()
            }
        }

        target.extensions.configure<PublishingExtension> {
            repositories {
                maven {
                    name = "warpLocal"
                    url = target.rootProject.layout.projectDirectory.dir(".maven-libs").asFile.toURI()
                }
            }
        }
    }

    companion object {
        val PUBLISHABLE_MODULE_PATHS: Set<String> =
            setOf(
                ":warp-runtime",
                ":warp-ui",
                ":warp-widget",
            )

        fun mavenArtifactId(path: String): String =
            when (path) {
                ":warp-runtime" -> "warp-runtime"
                ":warp-ui" -> "warp-ui"
                ":warp-widget" -> "warp-widget"
                else -> error("No Maven artifactId configured for project path: $path")
            }
    }

    private fun Project.mavenCentralCredentialsPresent(): Boolean {
        val u = rootProject.findProperty("mavenCentralUsername") as? String
        val p = rootProject.findProperty("mavenCentralPassword") as? String
        return !u.isNullOrBlank() && !p.isNullOrBlank()
    }

    private fun Project.signingCredentialsPresent(): Boolean {
        val inMemory = rootProject.findProperty("signingInMemoryKey") as? String
        if (!inMemory.isNullOrBlank()) return true
        val ring = rootProject.findProperty("signing.secretKeyRingFile") as? String
        return !ring.isNullOrBlank()
    }
}
