# warp-ui

Platform renderers for WARP `WarpNode` trees.

**Status:** Android (Jetpack Glance) ✓ · iOS (WidgetKit + SwiftUI via [spm4Kmp](https://github.com/frankois944/spm4Kmp)) ✓

## Architecture

```
commonMain                          androidMain                         iosMain
──────────                          ───────────                         ───────
WarpRender(node, handlers)   →      Glance composable                   SideEffect → warpRender
warpRender(node, handlers)   →      throws (iOS-only)                   WarpSwiftUIView holder
WarpClickHandler<T>                 WarpRegistryActionCallback            SwiftUI + WidgetKit
WarpClicksRegistry
```

| API | Android | iOS |
|-----|---------|-----|
| `WarpRender` | `@Composable` Glance tree | Publishes JSON + registers handlers (Compose side-effect) |
| `warpRender` | throws | Returns `WarpSwiftUIView` for SwiftUI / WidgetKit embedding |

## Click dispatch

Handlers are registered at render time. Platform code only invokes `WarpClicksRegistry.dispatch(actionId, parameters)`.

```
Button tap
  → Android: WarpRegistryActionCallback (Glance)
  → iOS widget: WarpClickIntent (iOS 17+)
  → iOS preview: WarpClickBridge
  → WarpClicksRegistry.dispatch
  → WarpClickHandler.onClick(typedActionId, parameters)
```

Register shared handlers once per render:

```kotlin
val handlers = listOf(CounterClickHandler(dataStore, widgetUpdater))

WarpRender(node, handlers)           // Android Glance widget
val view = warpRender(node, handlers) // iOS native view holder
```

---

## Android

Use `WarpRender` inside a Glance `provideContent` block:

```kotlin
provideContent {
    val node = composeWarp(WarpCounterWidget.State(count = counter), WarpCounterWidget.ui)
    val handlers = remember(context) {
        counterWidgetClickHandlers(KmpDataStore(context), WidgetUpdater(context))
    }
    WarpRender(node, handlers)
}
```

`warpRender` is **not** available on Android — it throws. Glance requires the composable renderer.

Flow: `RenderWarpNode` → `WarpRegistryActionCallback` → `WarpClicksRegistry`.

See [`CounterWidgetGlance.kt`](../shared/src/androidMain/kotlin/com/atriidev/kmpwidget/CounterWidgetGlance.kt).

---

## iOS

Swift bridge lives in `src/swift/warpWidgetKit/` and is exported to Kotlin via [spm4Kmp](https://spmforkmp.eu/setup/).

Requires `kotlin.mpp.enableCInteropCommonization=true` in the root `gradle.properties`.

### `warpRender` — native view holder

Primary iOS entry point when you need a SwiftUI view to embed in a widget or UIKit host:

```kotlin
import com.atriidev.warp_ui.warpRender

val holder = warpRender(
    node = composeWarp(CounterWidget.State(count = count), CounterWidget.ui),
    handlers = counterWidgetClickHandlers(dataStore, widgetUpdater),
)
```

What it does:

1. `WarpClicksRegistry.register(handlers)`
2. Install `WarpClickBridge` handler → Kotlin dispatch
3. Publish `WarpNode` JSON to `WarpWidgetBridge` (App Group / UserDefaults for extensions)
4. Return `WarpSwiftUIView(json, useIntents = true)`

**Swift — embed in WidgetKit:**

```swift
let holder = WarpUiKt.warpRender(node: node, handlers: handlers)
holder.makeView()  // AnyView
    .containerBackground(.fill.tertiary, for: .widget)
```

**Swift — `StaticConfiguration` body:**

```swift
StaticConfiguration(kind: kind, provider: provider) { entry in
    let holder = SharedKt.warpRender(node: entry.node, handlers: handlers)
    holder.makeView()
}
```

### `WarpRender` — Compose side-effect (iOS)

When already inside Compose, call the composable variant — it delegates to `warpRender` via `SideEffect`:

```kotlin
@Composable
fun MyScreen(node: WarpNode, handlers: List<WarpClickHandler<*>>) {
    WarpRender(node, handlers)  // publishes JSON, registers handlers
}
```

### In-app preview (UIKit host)

Three ways to preview the SwiftUI tree inside the app:

| Method | Use case |
|--------|----------|
| `holder.previewView()` | Embed in Compose via `UIKitView` |
| `holder.previewViewController()` | Full-screen UIKit push / sheet |
| `warpWidgetPreviewViewController()` | Reads last published JSON from `WarpWidgetBridge` |

**Compose + UIKitView** (see [`MainViewController.kt`](../shared/src/iosMain/kotlin/com/atriidev/kmpwidget/MainViewController.kt)):

```kotlin
import com.atriidev.warp_ui.previewView
import com.atriidev.warp_ui.warpRender

val holder = warpRender(node, handlers)

UIKitView(
    factory = {
        @Suppress("UNCHECKED_CAST")
        holder.previewView() as UIView
    },
    modifier = Modifier.fillMaxWidth().height(160.dp),
)
```

Preview uses `useIntents: false` so taps go through `WarpClickBridge` (same process). WidgetKit uses `WarpClickIntent` (`useIntents: true`).

**Legacy preview helper** (JSON already published):

```kotlin
import com.atriidev.warp_ui.warpWidgetPreviewViewController

val controller = warpWidgetPreviewViewController()
```

### Widget extension setup

Add a Widget Extension target in Xcode:

```swift
import warpWidgetKit
import WidgetKit
import SwiftUI

@main
struct MyWidgetBundle: WidgetBundle {
    var body: some Widget {
        WarpWidgetKitWidget()  // reads JSON from WarpWidgetBridge
    }
}
```

For app ↔ extension JSON sharing, configure an **App Group** and point `WarpWidgetBridge` storage at the shared `UserDefaults` suite.

Alternatively, call `warpRender` from Kotlin in the extension's timeline provider and use `holder.makeView()` directly.

### Swift package layout

```
src/swift/warpWidgetKit/
  WarpSwiftUIView.swift      # View holder (makeView / makePreviewView)
  WarpSwiftUIRenderer.swift  # JSON → SwiftUI
  WarpWidgetBridge.swift     # JSON storage + timeline reload
  WarpClickBridge.swift      # In-app click callback
  WarpWidgetKitWidget.swift  # WidgetKit entry point
```

---

## Examples

| File | Platform | What it shows |
|------|----------|---------------|
| [`CounterWidgetGlance.kt`](../shared/src/androidMain/kotlin/com/atriidev/kmpwidget/CounterWidgetGlance.kt) | Android | `WarpRender` in Glance widget |
| [`CounterClickHandler.kt`](../shared/src/commonMain/kotlin/com/atriidev/kmpwidget/CounterClickHandler.kt) | common | Shared click handlers |
| [`MainViewController.kt`](../shared/src/iosMain/kotlin/com/atriidev/kmpwidget/MainViewController.kt) | iOS | `warpRender` + `UIKitView` preview |

## Build verification

```bash
./gradlew :warp-ui:compileKotlinIosSimulatorArm64   # iOS + spm4Kmp
./gradlew :androidApp:assembleDebug                  # Android Glance
```

## Related docs

- [warp-runtime README](../warp-runtime/README.md)
- [Click dispatch guide](../warp-runtime/README_CLICK.md)
