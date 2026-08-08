# Contributing to Warp

> [!NOTE]
> External contributions are currently **not accepted**. This repository is maintained primarily for single-owner development.

---

## Publishing Guide

### Local Publishing (`.maven-libs`)

To publish libraries locally for testing in other projects:

```bash
./gradlew publishWarpLibrariesToLocalRepository
```

Artifacts publish to `.maven-libs/io/github/devatrii` (gitignored).

To consume published local libraries in another project, add `.maven-libs` repository in `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
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
```

---

### Maven Central Publishing

Publishing to Sonatype / Maven Central requires:

1. **Gradle Properties / Secrets**:
   - `MAVEN_CENTRAL_USERNAME` — Sonatype token username
   - `MAVEN_CENTRAL_PASSWORD` — Sonatype token password
   - `SIGNING_KEY` — ASCII-armored GPG private key
   - `SIGNING_PASSWORD` — Key passphrase (if any)

2. **Publish Command**:
   ```bash
   ./gradlew publishWarpLibrariesToMavenCentral
   ```

3. **CI/CD Automation**:
   - Trigger `.github/workflows/publish-warp-libraries.yml` manually via **workflow_dispatch** on GitHub Actions.
