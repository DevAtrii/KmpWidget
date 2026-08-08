This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/sharedLogic](./sharedLogic/src) is for the code that will be shared between app targets in the project.
  The most important subfolder is [commonMain](./sharedLogic/src/commonMain/kotlin). If preferred, you
  can add code to the platform-specific folders here too.

* [/sharedUI](./sharedUI/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./sharedUI/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./sharedUI/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./sharedUI/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

## iOS Build Size Report

Below is the size report for the iOS Application (`TodoWidget.app`), Widget Extension (`TodoWidgetExtension.appex`), and the KMP Shared Framework (`SharedLogic.framework`) built for **Debug** and **Release** configurations on `arm64-apple-ios-simulator`.

### Summary Comparison Table

| Artifact | Component / Path | Debug Size | Release Size | Reduction / Note |
| :--- | :--- | :--- | :--- | :--- |
| **Xcode App Store Archive (`.ipa`)** | `TodoWidget.ipa` *(Xcode App Thinning Export)* | — | **1.8 MB** (1.7 MB - 1.8 MB) | **Official App Store Download Size** |
| **Installed App Size (Thinned)** | On-Device Installed Footprint | — | **5.8 MB** | **Official App Store Install Size** |
| **Shared KMP Framework** | `SharedLogic.framework/SharedLogic` | 66.8 MB (69.99 MB) | 18.0 MB (18.92 MB) | **~73.0%** |
| **Widget Extension Bundle** | `TodoWidgetExtension.appex` | 13.9 MB (14.25 MB) | 8.50 MB (8.70 MB) | **~38.8%** |
| **Widget Extension Binary** | `TodoWidgetExtension.appex/TodoWidgetExtension` | 39.6 KB (39,648 B) | 8.49 MB (8,901,616 B) | *Linked statically in Release* |
| **Main Host App Bundle** | `TodoWidget.app` | 14.1 MB (14.46 MB) | 8.60 MB (8.80 MB) | **~39.0%** |
| **Main Host App Binary** | `TodoWidget.app/TodoWidget` | 39.6 KB (39,640 B) | 87.8 KB (89,928 B) | — |

### Breakdown & Key Insights

1. **Official Xcode Archive & App Thinning Report**:
   - According to Xcode's App Thinning Size Report (`App Thinning Size Report.txt` generated during Xcode export):
     - **App Store Download Size**: **1.8 MB** compressed (`TodoWidget.ipa`)
     - **On-Device Installed Footprint**: **5.8 MB** uncompressed
   - App Thinning optimizes asset catalogs and bitcode/binary slicing for each device variant, keeping the final download under 2 MB.

2. **Static Framework Stripping**:
   - In **Debug**, `SharedLogic.framework` contains unstripped debug symbols and full non-optimized IR, weighing in at **66.8 MB**.
   - In **Release**, dead-code stripping, LTO, and Kotlin/Native release optimizations reduce `SharedLogic.framework` down to **18.0 MB** (~73% reduction).

3. **Widget Extension Bundle**:
   - In **Release**, the uncompressed `TodoWidgetExtension.appex` bundle on-disk is **8.50 MB**, which includes the statically linked Kotlin Multiplatform runtime (`warp-runtime`, `warp-ui`, `warp-widget`), `SharedLogic`, kotlinx.serialization, and Compose Runtime.
   - The uncompressed host app bundle (`TodoWidget.app`) containing the embedded `TodoWidgetExtension.appex` comes out to **8.60 MB** total on-disk before App Thinning.

### Reproducing the Size Report

To generate and measure the builds yourself via `xcodebuild`:

```bash
# 1. Build Debug
xcodebuild -project examples/todo-widget/iosApp/iosApp.xcodeproj \
  -scheme iosApp -configuration Debug -destination 'generic/platform=iOS Simulator' ARCHS=arm64 \
  CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO CODE_SIGNING_ALLOWED=NO \
  SYMROOT=$(pwd)/examples/todo-widget/iosApp/build

# 2. Build Release
xcodebuild -project examples/todo-widget/iosApp/iosApp.xcodeproj \
  -scheme iosApp -configuration Release -destination 'generic/platform=iOS Simulator' ARCHS=arm64 \
  CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO CODE_SIGNING_ALLOWED=NO \
  SYMROOT=$(pwd)/examples/todo-widget/iosApp/build

# 3. Inspect Bundle Sizes
du -sk examples/todo-widget/iosApp/build/Release-iphonesimulator/TodoWidget.app
ls -lh examples/todo-widget/iosApp/build/Release-iphonesimulator/TodoWidget.app/PlugIns/TodoWidgetExtension.appex/TodoWidgetExtension
```

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…