# warp-widget

Shared **widget definition + host API** for WARP. Write one [`WarpWidget`](src/commonMain/kotlin/com/atriidev/warp_widget/WarpWidget.kt) in `commonMain`; Android Glance and iOS WidgetKit consume it the same way.

**Status:** Early / experimental · depends on [`warp-runtime`](../warp-runtime/) + [`warp-ui`](../warp-ui/)

## Role in the stack

```
┌─────────────────────────────────────────────────────────────┐
│  App defines WarpWidget<S> (Content + state + id)           │
└────────────────────────────┬────────────────────────────────┘
                             ▼
┌─────────────────────────────────────────────────────────────┐
│  warp-widget                                                │
│  · WarpWidgetSession (PlatformContext + WidgetEnvironment)│
│  · WarpWidgetHost (compose / JSON / prepare / dispatch)     │
│  · typed state JSON (key = id) + WarpWidgetStateStore       │
│  · Glance helpers (Android) · Kit bridge (iOS)              │
└───────────────┬─────────────────────────────┬───────────────┘
                ▼                             ▼
         warp-runtime                   warp-ui
         (Compose → WarpNode)           (Glance / SwiftUI)
                │                             │
                └──────────┬──────────────────┘
                           ▼
              Jetpack Glance  ·  WidgetKit + warpWidgetKit SPM
```

| Concern | Where it lives |
|---------|----------------|
| UI tree (`WarpColumn`, `WarpButton`, …) | `warp-runtime` |
| Paint nodes on screen | `warp-ui` |
| Define widget + session + state + host entry points | **`warp-widget`** (this module) |
| SwiftUI / WidgetKit helpers | [`warpWidgetKit`](../warpWidgetKit/) SPM |

## Core concepts

### `WarpWidget<S>`

One shared definition with `@Serializable` state `S`:

- `id` — stable kind (`"CounterWidget"`); prefs JSON key + iOS `Widget.kind`
- `iosGroupId` — iOS App Group suite (`group.*`); ignored on Android
- `defaultState` — used when prefs empty / decode fails
- `Content(env, state)` — WARP composables with decoded `S`
- `clickHandlers(session)` — persist via `updateWarpWidgetState { (S) -> S }`

Supported size classes are **not** on `WarpWidget` — the host is the source of truth (WidgetKit `.supportedFamilies` / Glance sizes). Use `env.family` for the **current** family at render time.

### `WarpWidgetSession`

Every host call needs an explicit session:

```kotlin
WarpWidgetSession(
    context = PlatformContext(/* Android Context / iOS app group */),
    environment = /* from Glance or WarpWidgetKitEnv */,
    preferences = /* optional preloaded prefs */,
)
```

### `WarpWidgetHost`

| API | Use |
|-----|-----|
| `compose(widget, session)` | `WarpNode` for Glance `WarpRender` |
| `composeJson(widget, session)` | JSON string for WidgetKit SwiftUI |
| `prepare(widget, session)` | Register clicks (WidgetKit cold start) |
| `dispatchClick(widget, session, actionId, parametersJson)` | AppIntent → handlers |
| `handlers(widget, session)` | Pass into Glance `WarpRender` |
| `snapshot(widget, session)` | Serializable timeline / debug payload |

## Define a widget

```kotlin
@Serializable
data class CounterState(val count: Int = 0)

object CounterWarpWidget : WarpWidget<CounterState>(CounterState.serializer()) {
    override val id = "CounterWidget"
    override val iosGroupId = "group.com.example.app"
    override val defaultState = CounterState()

    @Composable
    override fun Content(env: WidgetEnvironment, state: CounterState) {
        WarpColumn {
            WarpText("Counter")
            WarpRow {
                WarpButton("-", CounterActions.Decrement.asClickAction())
                WarpText("${state.count}")
                WarpButton("+", CounterActions.Increment.asClickAction())
            }
        }
    }

    override fun clickHandlers(session: WarpWidgetSession) = listOf(
        object : WarpClickHandler<CounterActions>(…) {
            override suspend fun onClick(actionId: CounterActions, parameters: Map<String, String>) {
                updateWarpWidgetState(session.context, CounterWarpWidget) { state ->
                    state.copy(
                        count = when (actionId) {
                            CounterActions.Increment -> state.count + 1
                            CounterActions.Decrement -> state.count - 1
                        },
                    )
                }
            }
        },
    )
}
```

State JSON is stored under prefs key = [WarpWidget.id].

See demo: [`CounterWarpWidget.kt`](../shared/src/commonMain/kotlin/com/atriidev/kmpwidget/CounterWarpWidget.kt).

## Android (Jetpack Glance)

1. Subclass [WarpGlanceWidgetReceiver](src/androidMain/kotlin/com/atriidev/warp_widget/WarpGlanceWidgetReceiver.kt) + [WarpGlanceWidget](src/androidMain/kotlin/com/atriidev/warp_widget/WarpGlanceWidget.kt) — registry + `PreferencesGlanceStateDefinition` + `WarpRender` are automatic:

```kotlin
class CounterWidgetReceiver : WarpGlanceWidgetReceiver() {
    override val widget get() = CounterWarpWidget
    override fun createGlanceWidget() = CounterGlanceAppWidget()
}

class CounterGlanceAppWidget : WarpGlanceWidget() {
    override val widget get() = CounterWarpWidget
}
```

| Helper | Role |
|--------|------|
| `WarpGlanceWidgetReceiver` | Auto [WarpWidgetAndroidRegistry.register]; cold-start wake → `ensureRegistered` |
| `WarpGlanceWidget` | `PreferencesGlanceStateDefinition` + `WarpWidgetHost` / `WarpRender` |
| `rememberGlanceWidgetSession(context)` | `LocalSize` + Glance prefs → `WarpWidgetSession` (used inside `WarpGlanceWidget`) |
| `glanceWidgetEnvironment(context, size)` | Map config / density / theme → `WidgetEnvironment` |
| `Preferences.toWarpPreferences()` | Glance prefs → WARP bag |

**State:** Glance `PreferencesGlanceStateDefinition` via `WarpWidgetStateStore`.  
**Update from app:** `updateWarpWidgetState(PlatformContext(context), widget) { … }`.

## iOS (WidgetKit)

Hosts supply env from [`warpWidgetKit`](../warpWidgetKit/) (`WarpWidgetKitEnv`), then map to Shared types via Kotlin (package must not `import Shared` — that would cycle with spm4Kmp).

```swift
let session = WarpWidgetHost.shared.iosSession(
    widget: CounterWarpWidget.shared,
    kitFields: WarpWidgetKitEnv.from(context: context).asKitFields(
        appGroupId: CounterWarpWidget.shared.iosGroupId
    )
)
WarpWidgetHost.shared.prepare(widget: CounterWarpWidget.shared, session: session)
let json = WarpWidgetHost.shared.composeJson(widget: CounterWarpWidget.shared, session: session)
// SwiftUI: WarpSwiftUIRootView(json: json, useIntents: true, widgetId: CounterWarpWidget.shared.id)
```

No `installWarpWidgetKitBridge()` — `iosSession(widget:kitFields:)` installs the bridge.

| Piece | Role |
|-------|------|
| `WarpWidget.iosGroupId` | App Group suite (source of truth) |
| `WarpWidgetKitEnv.from(context:).asKitFields` | WidgetKit → field bag (SPM, no Shared) |
| `WarpWidgetHost.iosSession(widget:kitFields:)` | Map fields → session; auto-install bridge |
| `WarpWidgetStateStore` | App Group UserDefaults `"$widgetId.$key"` + `reloadTimelinesOfKind` |

**Do not** copy `warpWidgetKit` sources into the extension (duplicate `WarpClickBridge` → broken clicks). Link the SPM product once.

## State API

| API | Role |
|-----|------|
| `WarpWidget<S>(serializer)` + `defaultState` | Typed serializable widget state |
| `updateWarpWidgetState(context, widget) { S -> S }` | Encode JSON under `id` + reload |
| `readWarpWidgetState(context, widget)` | Decode typed state |
| `WarpStateKey` / `currentState` | Low-level string-key bag (optional) |
| `reloadWarpWidget(context, widget)` | Reload only |

## Gradle

```kotlin
// consumer commonMain
api(project(":warp-widget"))

// iOS framework export (for Swift)
kotlin {
    iosTarget.binaries.framework {
        export(project(":warp-widget"))
        export(project(":warp-ui"))
        export(project(":warp-runtime"))
    }
}
```

This module uses [spm4Kmp](https://spmforkmp.eu/) `localPackage` → repo-root [`warpWidgetKit`](../warpWidgetKit/). Swap to `remotePackageVersion` when published.

Requires `kotlin.mpp.enableCInteropCommonization=true` in root `gradle.properties`.

## Package layout

```
warp-widget/
  src/commonMain/…/WarpWidget.kt          # WarpWidget, session, host
  src/commonMain/…/WarpWidgetState*.kt    # prefs, currentState, store expect
  src/commonMain/…/api/                   # WidgetEnvironment, PlatformContext, …
  src/androidMain/…/WarpGlance*.kt        # GlanceAppWidget / Receiver bases
  src/androidMain/…/Glance*.kt            # Glance env/session helpers, registry
  src/iosMain/…/WarpWidgetKitMapping.kt   # Kit env dict → Shared types
  src/iosMain/…/WarpWidgetHost.ios.kt     # iosSession()
  src/swift/warpBridge/                   # spm4Kmp thin bridge
```

## Related docs

- [warp-runtime](../warp-runtime/README.md) — compose DSL, nodes, actions
- [warp-ui](../warp-ui/README.md) — `WarpRender` / `warpWidgetJson` / clicks
- [warpWidgetKit](../warpWidgetKit/README.md) — SPM SwiftUI package
- Demo: [`shared/…/CounterWarpWidget.kt`](../shared/src/commonMain/kotlin/com/atriidev/kmpwidget/CounterWarpWidget.kt)
