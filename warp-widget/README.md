# warp-widget

Shared **widget definition + host API** for WARP. Write one [`WarpWidget`](src/commonMain/kotlin/com/atriidev/warp_widget/WarpWidget.kt) in `commonMain`; Android Glance and iOS WidgetKit consume it the same way.

**Status:** Early / experimental · depends on [`warp-runtime`](../warp-runtime/) + [`warp-ui`](../warp-ui/)

## Role in the stack

```
┌─────────────────────────────────────────────────────────────┐
│  App defines WarpWidget (Content + clickHandlers + id)      │
└────────────────────────────┬────────────────────────────────┘
                             ▼
┌─────────────────────────────────────────────────────────────┐
│  warp-widget                                                │
│  · WarpWidgetSession (PlatformContext + WidgetEnvironment)│
│  · WarpWidgetHost (compose / JSON / prepare / dispatch)     │
│  · currentState / prefs + WarpWidgetStateStore              │
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

### `WarpWidget`

One shared definition:

- `id` — stable kind (`"CounterWidget"`); matches iOS `Widget.kind` for timeline reload
- `Content(env)` — WARP composables; read prefs with `currentState(key)` (Glance-style)
- `clickHandlers(session)` — persist via `updateWarpWidgetState`

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
object CounterKeys {
    val Count = WarpStateKey.int("counter")
}

object CounterWarpWidget : WarpWidget {
    override val id = "CounterWidget"

    @Composable
    override fun Content(env: WidgetEnvironment) {
        val count = currentState(CounterKeys.Count) ?: 0
        WarpColumn {
            WarpText("Counter")
            WarpRow {
                WarpButton("-", CounterActions.Decrement.asClickAction())
                WarpText("$count")
                WarpButton("+", CounterActions.Increment.asClickAction())
            }
        }
    }

    override fun clickHandlers(session: WarpWidgetSession) = listOf(
        object : WarpClickHandler<CounterActions>(…) {
            override suspend fun onClick(actionId: CounterActions, parameters: Map<String, String>) {
                updateWarpWidgetState(session.context, CounterWarpWidget) {
                    val cur = this[CounterKeys.Count] ?: 0
                    this[CounterKeys.Count] = when (actionId) {
                        CounterActions.Increment -> cur + 1
                        CounterActions.Decrement -> cur - 1
                    }
                }
            }
        },
    )
}
```

See demo: [`CounterWarpWidget.kt`](../shared/src/commonMain/kotlin/com/atriidev/kmpwidget/CounterWarpWidget.kt).

## Android (Jetpack Glance)

1. Register WARP widget + Glance class in the **receiver `init`** (library handles cold-start taps — no app ContentProvider):

```kotlin
class CounterWidgetReceiver : GlanceAppWidgetReceiver() {
    init {
        WarpWidgetAndroidRegistry.register(
            CounterWarpWidget.id,
            CounterWarpWidget,
        ) { CounterGlanceAppWidget() }
    }
    override val glanceAppWidget get() = CounterGlanceAppWidget()
}
```

2. In `provideContent`, build a session with Glance helpers:

```kotlin
provideContent {
    val session = rememberGlanceWidgetSession(context)
    WarpRender(
        node = WarpWidgetHost.compose(CounterWarpWidget, session),
        handlers = WarpWidgetHost.handlers(CounterWarpWidget, session),
    )
}
```

| Helper | Role |
|--------|------|
| `rememberGlanceWidgetSession(context)` | `LocalSize` + Glance prefs → `WarpWidgetSession` |
| `glanceWidgetEnvironment(context, size)` | Map config / density / theme → `WidgetEnvironment` |
| `Preferences.toWarpPreferences()` | Glance prefs → WARP bag |
| `WarpWidgetAndroidRegistry` | `widgetId` → `GlanceAppWidget` for state update / reload |

**State:** Glance `PreferencesGlanceStateDefinition` via `WarpWidgetStateStore`.  
**Update from app:** `updateWarpWidgetState(PlatformContext(context), widget) { … }`.

## iOS (WidgetKit)

Hosts supply env from [`warpWidgetKit`](../warpWidgetKit/) (`WarpWidgetKitEnv`), then map to Shared types via Kotlin (package must not `import Shared` — that would cycle with spm4Kmp).

```swift
WarpWidgetKitMappingKt.installWarpWidgetKitBridge()
let session: WarpWidgetSession = WarpWidgetKitEnv.from(context: context).makeSession()
WarpWidgetHost.shared.prepare(widget: CounterWarpWidget.shared, session: session)
let json = WarpWidgetHost.shared.composeJson(widget: CounterWarpWidget.shared, session: session)
// SwiftUI: WarpSwiftUIRootView(json: json, useIntents: true)
```

AppIntent:

```swift
WarpWidgetKitMappingKt.installWarpWidgetKitBridge()
let session: WarpWidgetSession = WarpWidgetKitEnv.placeholder().makeSession()
WarpWidgetHost.shared.dispatchClick(
    widget: CounterWarpWidget.shared,
    session: session,
    actionId: actionId,
    parametersJson: parametersJson
)
```

| Piece | Role |
|-------|------|
| `WarpWidgetKitEnv.from(context:)` | WidgetKit → field snapshot (SPM, no Shared) |
| `makeSession()` / `makeEnvironment()` | Generics → Shared types via `WarpWidgetKitShared` bridge |
| `installWarpWidgetKitBridge()` | Kotlin installs dict → `WarpWidgetKitMapping` |
| `WarpWidgetHost.iosSession(environment:)` | App Group `PlatformContext` + env |
| `WarpWidgetStateStore` | App Group UserDefaults `"$widgetId.$key"` + `reloadTimelinesOfKind` |

**Do not** copy `warpWidgetKit` sources into the extension (duplicate `WarpClickBridge` → broken clicks). Link the SPM product once.

## State API

| API | Role |
|-----|------|
| `WarpStateKey.int/string/…` | Typed preference keys |
| `currentState(key)` / `currentPreferences()` | Inside `Content` (Glance-style) |
| `updateWarpWidgetState(context, widget) { … }` | Write + reload (app or click handler) |
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
