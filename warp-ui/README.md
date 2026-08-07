# warp-ui

Platform renderers for WARP `WarpNode` trees.

**Status:** Android (Jetpack Glance) ✓ · iOS (WidgetKit + SwiftUI via [spm4Kmp](https://github.com/frankois944/spm4Kmp)) ✓

## Architecture

```
commonMain                          androidMain                         iosMain
──────────                          ───────────                         ───────
WarpRender(node, handlers)   →      Glance composable                   SideEffect → warpRender
warpRender(node, handlers)   →      throws (iOS-only)                   register + WarpSwiftUIView
warpWidgetView(node)         →      —                                   WarpSwiftUIView (view only)
WarpClickHandler<T>                 WarpRegistryActionCallback            SwiftUI + WidgetKit
WarpClicksRegistry
```

| API | Android | iOS |
|-----|---------|-----|
| `WarpRender` | `@Composable` Glance tree | Registers handlers via `warpRender` side-effect |
| `warpRender` | throws | Registers handlers + returns `WarpSwiftUIView` |
| `warpWidgetView` | — | `WarpNode` → `WarpSwiftUIView` (no handler register) |
| `registerWarpClicks` | — | Registers handlers + installs click bridge |

## Click dispatch

```
Button tap
  → Android: WarpRegistryActionCallback (Glance)
  → iOS widget: extension AppIntent → dispatchCounterWidgetClick → WarpClicksRegistry
  → iOS preview: WarpClickBridge
  → WarpClicksRegistry.dispatch
  → WarpClickHandler.onClick
```

---

## Android

```kotlin
provideContent {
    val node = composeWarp(WarpCounterWidget.State(count = counter), WarpCounterWidget.ui)
    val handlers = remember(context) {
        counterWidgetClickHandlers(KmpDataStore(context), WidgetUpdater(context))
    }
    WarpRender(node, handlers)
}
```

See [`CounterWidgetGlance.kt`](../shared/src/androidMain/kotlin/com/atriidev/kmpwidget/CounterWidgetGlance.kt).

---

## iOS

Swift bridge: `src/swift/warpWidgetKit/` → compiled by [spm4Kmp](https://spmforkmp.eu/setup/) into the KMP framework. **Do not** copy those sources into the widget target (duplicate `WarpClickBridge` → broken clicks).

Requires `kotlin.mpp.enableCInteropCommonization=true` in root `gradle.properties`.

### Library consumer setup

```kotlin
// shared/build.gradle.kts
kotlin {
    iosTarget.binaries.framework {
        export(project(":warp-ui"))
        export(project(":warp-runtime"))
    }
    sourceSets.commonMain.dependencies {
        api(project(":warp-runtime")) // or maven coords when published
        api(project(":warp-ui"))
    }
}
```

`api` = Gradle/metadata. `export` = types + top-level fns appear in `Shared.h` for Swift.

### WidgetKit flow

```
renderXWidget(): WarpNode              // your iosMain — compose + registerWarpClicks
        ↓
warpWidgetJson(node)                   // warp-ui iosMain
        ↓
WarpSwiftUIView(...).widgetRootView()  // warpWidgetKit (spm4Kmp, prebuilt module)
```

**Kotlin** — compose + register only:

```kotlin
fun renderCounterWidget(): WarpNode {
    val node = composeWarp(CounterWidget.State(count = count), CounterWidget.ui)
    registerWarpClicks(counterWidgetClickHandlers(dataStore, widgetUpdater))
    return node
}
```

**Swift** — display only:

```swift
import Shared
import SwiftUI
import warpWidgetKit

func counterWidgetRootView() -> WarpSwiftUIRootView {
    let node = CounterWidgetIosKt.renderCounterWidget()
    return WarpSwiftUIView(
        json: WarpWidgetView_iosKt.warpWidgetJson(node: node),
        useIntents: true
    ).widgetRootView()
}
```

**WidgetBundle + AppIntent** — intents must live in the **extension** target (not Shared).
Extension defines intent only; `warpWidgetKit` owns button styling:

```swift
struct CounterWidgetClickIntent: WarpClickAppIntent { /* perform → WarpWidgetHost.dispatchClick */ }

@main
struct CounterWidgetBundle: WidgetBundle {
    init() {
        WarpClickIntentRegistry.install(
            CounterWidgetClickIntent.self,
            for: CounterWarpWidget.shared.id
        )
        // WarpClickIntentRegistry.install(OtherClickIntent.self, for: OtherWidget.shared.id)
        // WarpWidgetHost.prepare(…)
    }
    var body: some Widget { CounterHomeWidget() }
}

// Entry view:
WarpSwiftUIRootView(json: json, useIntents: true, widgetId: CounterWarpWidget.shared.id)
```

App Intents inside a static Shared library are **not** discovered by WidgetKit.

### `import warpWidgetKit` (SPM package)

SwiftUI types live in the **`warpWidgetKit` Swift package** (repo root: [`../warpWidgetKit`](../warpWidgetKit/)):

| Consumer | How |
|----------|-----|
| **Kotlin (`:warp-ui`)** | spm4Kmp `localPackage` + `exportToKotlin` (bridge cinterop `warpBridge`) |
| **Xcode widget / app** | Local SPM dependency → `import warpWidgetKit` |

**Local (this repo):** Xcode → package `../warpWidgetKit` (already wired in `iosApp.xcodeproj`).

**Remote (when published):**

```text
# Xcode: File → Add Package Dependencies
https://github.com/<org>/warpWidgetKit.git

# :warp-ui build.gradle.kts — swap localPackage for:
remotePackageVersion(
    url = uri("https://github.com/<org>/warpWidgetKit.git"),
    version = "1.0.0",
    products = { add("warpWidgetKit", exportToKotlin = true) },
)
```

> **Never** copy package sources into the widget target. Depend on the SPM product only (same as any other Swift package).

### `useIntents`

| Value | Use | Button |
|-------|-----|--------|
| `true` | Home-screen widget | `install(MyIntent.self, for: widgetId)` + `WarpSwiftUIRootView(widgetId:)` |
| `false` | In-app preview | `Button { WarpClickBridge.perform }` |

### In-app preview

```kotlin
val holder = warpRender(node, handlers)
holder.previewView() // UIKitView
```

### App Group

Same App Group on **app + widget extension**. Extension needs **iOS 17+** for interactive buttons.

### Swift package layout

```
warpWidgetKit/                         # SPM root (publish this)
  Package.swift
  Sources/warpWidgetKit/
    WarpSwiftUIView.swift
    WarpSwiftUIRenderer.swift
    WarpWidgetBridge.swift
    WarpClickBridge.swift
    WarpUiHostView.swift

warp-ui/src/swift/warpBridge/          # empty-ish spm4Kmp bridge (imports package)
  WarpBridgeAnchor.swift
```

---

## Examples

| File | Platform | What it shows |
|------|----------|---------------|
| [`CounterWidgetGlance.kt`](../shared/src/androidMain/kotlin/com/atriidev/kmpwidget/CounterWidgetGlance.kt) | Android | `WarpRender` in Glance |
| [`CounterClickHandler.kt`](../shared/src/commonMain/kotlin/com/atriidev/kmpwidget/CounterClickHandler.kt) | common | Shared click handlers |
| [`CounterWidgetIos.kt`](../shared/src/iosMain/kotlin/com/atriidev/kmpwidget/CounterWidgetIos.kt) | iOS | `renderCounterWidget(): WarpNode` |
| [`CounterWidgetView.swift`](../iosApp/CounterWidget/CounterWidgetView.swift) | iOS | Node → SwiftUI |
| [`CounterWidgetBundle.swift`](../iosApp/CounterWidget/CounterWidgetBundle.swift) | iOS | Cold-start prepare |
| [`MainViewController.kt`](../shared/src/iosMain/kotlin/com/atriidev/kmpwidget/MainViewController.kt) | iOS | In-app preview |

## Build verification

```bash
./gradlew :warp-ui:compileKotlinIosSimulatorArm64
./gradlew :androidApp:assembleDebug
```

## Related docs

- [warp-runtime README](../warp-runtime/README.md)
- [Click dispatch guide](../warp-runtime/README_CLICK.md)
