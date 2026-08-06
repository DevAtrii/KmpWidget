https://github.com/user-attachments/assets/6bcdd802-41fc-4ac7-8629-e9711f0fb1f8

# KmpWidget

> **Active development** — APIs and architecture are changing. Follow [@dev_atrii on X](https://x.com/dev_atrii) for updates.

## WARP — Coming Soon

**WARP** (**W**idget **A**bstraction, **R**endering **P**ipeline) is a unified API for creating home-screen widgets in **Kotlin Multiplatform**.

Write widget UI once in Kotlin → render on **Android** (Jetpack Glance) and **iOS** (WidgetKit + SwiftUI).

```
Compose-like Kotlin UI  →  WarpNode tree  →  JSON  →  platform renderer
                              ↑
                         shared state & click handlers
```

### Example

Describe widget UI in `commonMain` with composable primitives — same counter on Android Glance and iOS WidgetKit:

```kotlin
@Composable
fun CounterWidgetUi(state: CounterState) {
    WarpColumn(
        modifier = WarpModifier().padding(WarpPadding(16, 16, 16, 16)),
    ) {
        WarpText("Counter")
        WarpRow {
            WarpButton(text = "-", onClick = CounterActions.Decrement.asClickAction())
            WarpText(state.count.toString())
            WarpButton(text = "+", onClick = CounterActions.Increment.asClickAction())
        }
    }
}

// Compose state → tree → platform render
val node = composeWarp(CounterState(count = count), CounterWidgetUi)
WarpRender(node, counterWidgetClickHandlers(dataStore, widgetUpdater))  // Android Glance
// iOS: renderCounterWidget() → WidgetKit (.systemSmall)
```

See [example/counter/CounterWidget.kt](./warp-runtime/src/commonMain/kotlin/com/atriidev/warp_runtime/example/counter/CounterWidget.kt) for the full demo.

### Modules

| Module | Role |
|--------|------|
| [warp-runtime](./warp-runtime/) | Author widget UI (`WarpColumn`, `WarpText`, `WarpButton`), compose to tree/JSON, click wire format |
| [warp-ui](./warp-ui/) | Platform renderers — Glance (Android), iOS via [spm4Kmp](https://github.com/frankois944/spm4Kmp) |
| [warp-widget](./warp-widget/) | Shared `WarpWidget` definition, session/env, prefs store, host API (`WarpWidgetHost`) |
| [warpWidgetKit](./warpWidgetKit/) | **SPM** SwiftUI / WidgetKit package (`import warpWidgetKit`) — local now, remote later |
| [shared](./shared/) | App + demo widgets (counter), shared click handlers, DataStore |
| [androidApp](./androidApp/) | Android host app + Glance widget |
| [iosApp](./iosApp/) | iOS host app + Counter Widget extension (`.systemSmall`) |

### Docs

- [warp-runtime README](./warp-runtime/README.md) — composing widgets, JSON, click actions
- [warp-runtime click guide](./warp-runtime/README_CLICK.md) — handler registry & dispatch
- [warp-ui README](./warp-ui/README.md) — `WarpRender`, `warpRender`, iOS WidgetKit setup
- [warp-widget README](./warp-widget/README.md) — `WarpWidget`, session, state, Glance / WidgetKit hosts

### Status

| Platform | Renderer | Demo |
|----------|----------|------|
| Android | Jetpack Glance ✓ | Counter widget ✓ |
| iOS | WidgetKit + SwiftUI ✓ | Counter widget (`.systemSmall`) ✓ |
| API stability | Early / experimental | — |

---

## Running the apps

- **Android:** `./gradlew :androidApp:assembleDebug` — install app, add Counter widget from launcher
- **iOS:** open [iosApp](./iosApp) in Xcode, run **iosApp**, add **Counter** widget (requires App Group + iOS 17+)

### Verify builds

```bash
./gradlew :warp-runtime:jvmTest
./gradlew :warp-ui:compileKotlinIosSimulatorArm64
./gradlew :androidApp:assembleDebug
```

---

## Project layout

* [/iosApp](./iosApp) — iOS application and Widget Extension entry points
* [/shared](./shared/src) — shared Kotlin (commonMain, androidMain, iosMain)
* [/androidApp](./androidApp) — Android application

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html).
