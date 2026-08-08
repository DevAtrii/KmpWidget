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
- `clickHandlers(session)` — persist via `updateWarpWidgetState(session, widget) { (S) -> S }`
- `stateScope` — [WarpWidgetStateScope.Shared] (default) or [WarpWidgetStateScope.Instance] for per-placement state

Supported size classes are **not** on `WarpWidget` — the host is the source of truth (WidgetKit `.supportedFamilies` / Glance sizes). On **iOS** use `env.widgetFamily` ([WidgetPlatformEnvironment.Ios.family]). On **Android** use `env.size` (dp) — inferred Glance buckets are not exposed on [WidgetEnvironment].

### `WarpWidgetSession`

Every host call needs an explicit session:

```kotlin
WarpWidgetSession(
    context = PlatformContext(/* Android Context / iOS app group */),
    environment = /* from Glance or WarpWidgetKitEnv */,
    preferences = /* optional preloaded prefs */,
    widgetId = /* this instance — aw:… / ios:… / kind id when Shared */,
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
    override suspend fun defaultState() = CounterState()

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
                updateWarpWidgetState(session, CounterWarpWidget) { state ->
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

## Theming (`WarpTheme`)

Material-style color roles for widget UI — cross-platform `WarpColor` values for Glance + WidgetKit.

Wrap `Content` in `WarpTheme(environment = env)` and read colors via `WarpTheme.colors`:

```kotlin
@Composable
override fun Content(env: WidgetEnvironment, state: CounterState) {
    WarpTheme(environment = env) {
        val colors = WarpTheme.colors
        WarpBox(
            modifier = WarpModifier
                .fillMaxSize()
                .background(colors.widgetBackground),
        ) {
            WarpText(
                text = "${state.count}",
                style = WarpTextStyle(color = colors.onSurface),
            )
        }
    }
}
```

| API | Role |
|-----|------|
| `WarpTheme(environment = env) { … }` | Pick light/dark from `env.theme` |
| `WarpTheme.colors` | Current `WarpColors` inside the theme subtree |
| `WarpColors.defaultLight(platform)` / `defaultDark(platform)` | Platform defaults (Material 3 on Android, system blue on iOS) |
| `WarpColors.Material3Light` / `Material3Dark` | Explicit Material 3 baseline |
| `WarpColors.IosLight` / `IosDark` | Explicit iOS-style blue baseline |
| `env.warpColors()` | Resolve colors outside compose (no `WarpTheme` wrapper) |

On **Android**, `env.theme` comes from Glance `Configuration.uiMode` (stored in internal Glance prefs on reload — see below).  
On **iOS**, compose at view time with live `@Environment(\.colorScheme)` so WidgetKit pre-render passes get the correct scheme.

## Adaptive layout (`WarpAdaptive`)

WidgetKit has `systemSmall` / `systemMedium` / `systemLarge`. [WarpAdaptive.kt](src/commonMain/kotlin/com/atriidev/warp_widget/ui/WarpAdaptive.kt) maps both hosts to [WarpAdaptiveSize]:

| Host | Source |
|------|--------|
| iOS | `env.widgetFamily` |
| Android | `env.size` (dp) — from `AppWidgetManager` options, **not** Glance `LocalSize` |

### Default platform bucketing

```kotlin
WarpAdaptiveContent(env) {
    small { CompactLayout(state) }
    medium { WideLayout(state) }
    large { TallLayout(state) }
}

// non-compose branching:
val columns = env.adaptiveValue(small = 1, medium = 2, large = 3)
val bucket = env.adaptiveSize()  // iOS: family · Android: adaptiveSizeFrom(w, h)
```

On **Android**, default buckets use **width** as the primary signal (launcher columns). Tall layouts promote to large via height:

| Bucket | Typical size (dp) | Rule ([WarpAdaptiveThresholds]) |
|--------|-------------------|-----------------------------------|
| Small | ~179×99 | `width < 250` |
| Medium | ~373×99 | else, and not large |
| Large | ~373×311, ~734×154 | `width ≥ 550` **or** `height ≥ 170` |

Use [adaptiveSizeFrom] for manual classification outside compose:

```kotlin
val bucket = adaptiveSizeFrom(widthDp = 373f, heightDp = 99f)  // Medium
```

### Custom calc (Compose)

Pass your own `(widthDp, heightDp) → WarpAdaptiveSize` when defaults do not fit your widget:

```kotlin
// memoized bucket — recomposes when env.size changes
val bucket = rememberWarpAdaptiveSize(env) { w, h ->
    when {
        w < 200f -> WarpAdaptiveSize.Small
        w < 450f -> WarpAdaptiveSize.Medium
        else -> WarpAdaptiveSize.Large
    }
}

// three layout branches with custom calc
WarpAdaptiveContent(
    environment = env,
    calc = { w, h -> adaptiveSizeFrom(w, h) },  // or your own lambda
    small = { CompactLayout(state) },
    medium = { WideLayout(state) },
    large = { TallLayout(state) },
)
```

| API | Role |
|-----|------|
| [WarpAdaptiveCalc] | `(widthDp, heightDp) → WarpAdaptiveSize` type alias |
| [adaptiveSizeFrom] | Library default width/height buckets |
| [WarpAdaptiveThresholds] | Tunable default breakpoints (`SMALL_MAX_WIDTH_DP`, …) |
| [rememberWarpAdaptiveSize] | Run [calc] against `env.size` in compose |
| [WarpAdaptiveContent] (no calc) | Platform default via [WidgetEnvironment.adaptiveSize] |
| [WarpAdaptiveContent] (with calc) | Custom bucket + small / medium / large slots |
| [WarpWidgetSize.adaptiveSize] | Same as [adaptiveSizeFrom] on a [WarpWidgetSize] |

### Android sizing & resize

[GlanceAppWidgetSize.kt](src/androidMain/kotlin/com/atriidev/warp_widget/GlanceAppWidgetSize.kt) resolves **current** dp size for `env.size`:

1. **Layout prefs first** — `__warp_layout_w/h` written on resize (see below) when Glance `LocalAppWidgetOptions` is stale under default `SizeMode.Single`.
2. **Options bundle** — `OPTION_APPWIDGET_MIN_WIDTH` × `OPTION_APPWIDGET_MIN_HEIGHT` (Android 12+ current size). Do **not** use `MAX_HEIGHT` as current height — that is the resize ceiling and mis-buckets narrow widgets.

[rememberGlanceWidgetSession](src/androidMain/kotlin/com/atriidev/warp_widget/GlanceWidgetEnvironment.kt) feeds this into `WidgetEnvironment.size` so resize updates adaptive UI (e.g. `179×99` small, `373×99` medium — not the ~90dp Glance `LocalSize` minimum).

**Requirements:** subclass [WarpGlanceWidgetReceiver] — `onAppWidgetOptionsChanged` calls [WarpWidgetAndroidReload.scheduleLayoutReload], which persists layout via [GlanceInternalState.touchLayout] and calls `GlanceAppWidget.update()`. After changing sizing logic, remove/re-add the widget or resize once to refresh stale layout prefs.

## Android (Jetpack Glance)

1. Subclass [WarpGlanceWidgetReceiver](src/androidMain/kotlin/com/atriidev/warp_widget/WarpGlanceWidgetReceiver.kt) + [WarpGlanceWidget](src/androidMain/kotlin/com/atriidev/warp_widget/WarpGlanceWidget.kt) — registry + `PreferencesGlanceStateDefinition` + `WarpRender` are automatic:

```kotlin
class CounterWidgetReceiver : WarpGlanceWidgetReceiver() {
    init { ensureRegistered() }  // eager registry when receiver class loads

    override val widget get() = CounterWarpWidget
    override fun createGlanceWidget() = CounterGlanceAppWidget()
}

class CounterGlanceAppWidget : WarpGlanceWidget() {
    override val widget get() = CounterWarpWidget
}
```

2. **Manifest** — add ui-mode / config actions so system light/dark toggles reload widgets (subclassing `WarpGlanceWidgetReceiver` handles the broadcast; the intent filter must be declared in the app manifest):

```xml
<receiver android:name=".CounterWidgetReceiver" android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
        <action android:name="android.intent.action.CONFIGURATION_CHANGED" />
        <action android:name="android.intent.action.UI_MODE_CHANGED" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/my_app_widget_info" />
</receiver>
```

No custom `Application` or ContentProvider is required — [WarpWidgetAndroidInitProvider](src/androidMain/kotlin/com/atriidev/warp_widget/WarpWidgetAndroidInitProvider.kt) installs cold-start click prepare and ui-mode reload listeners at process start.

| Helper | Role |
|--------|------|
| `WarpGlanceWidgetReceiver` | Auto [WarpWidgetAndroidRegistry.register]; `UI_MODE_CHANGED` / resize layout reload |
| `WarpGlanceWidget` | `PreferencesGlanceStateDefinition` + `WarpWidgetHost` / `WarpRender` |
| `rememberGlanceWidgetSession(context)` | Options + layout prefs → `WarpWidgetSession` (inside `WarpGlanceWidget`) |
| `glanceWidgetEnvironment(context, size)` | Map config / density / theme → `WidgetEnvironment` |
| `Bundle.resolveGlanceWidgetSize` | Current dp from AppWidget options (see [Adaptive layout](#adaptive-layout-warpadaptive)) |
| `Preferences.toWarpPreferences()` | Glance prefs → WARP bag (filters internal `__warp_*` keys) |
| `WarpTheme(environment = env)` | Material-style colors from `env.theme` — see [Theming](#theming-warptheme) |

**State:** Glance `PreferencesGlanceStateDefinition` via `WarpWidgetStateStore`.  
**Update from app:** `updateWarpWidgetState(PlatformContext(context), widget) { … }`.

### System light / dark reload

Glance keeps a long-lived composition session whose `Context` can retain a stale `uiMode`. A bare `GlanceAppWidget.update()` with unchanged user prefs may not recompose — which is why widgets often stayed on the old theme until a click changed state.

`warp-widget` handles this automatically:

1. **[WarpWidgetAndroidReload](src/androidMain/kotlin/com/atriidev/warp_widget/WarpWidgetAndroidReload.kt)** listens for `UI_MODE_CHANGED` / `CONFIGURATION_CHANGED` (dynamic receiver at process start + manifest actions on `WarpGlanceWidgetReceiver`).
2. **[WarpWidgetAndroidRegistry.reloadAll](src/androidMain/kotlin/com/atriidev/warp_widget/WarpWidgetAndroidRegistry.kt)** wakes every installed widget receiver, then calls `WarpWidgetStateStore.reload` per registered widget.
3. **`reload`** writes internal Glance prefs (`__warp_ui_mode`, `__warp_theme_epoch`) via [GlanceInternalState](src/androidMain/kotlin/com/atriidev/warp_widget/GlanceInternalState.kt), then calls `update()` — same “prefs changed → recompose” path as a user click.
4. **`rememberGlanceWidgetSession`** reads theme from those internal prefs (fallback: live `Configuration`) so `WarpTheme(environment = env)` picks up the new scheme.

**Requirements:** manifest intent filter above; open the app once after install so the init provider registers receivers (or interact with the widget once). Reload only runs while the app process is alive (standard Android widget limitation).

**Debug logcat tags:** `WarpWidgetAndroidReload`, `WarpWidgetStateStore`, `WarpWidgetAndroidRegistry`.

### Resize / adaptive reload

Default Glance `SizeMode.Single` ignores `GlanceAppWidget.resize()`. Without an explicit reload, `LocalAppWidgetOptions` and `LocalSize` can stay at the last session dimensions while the user resizes on the home screen.

`warp-widget` handles this in [WarpGlanceWidgetReceiver.onAppWidgetOptionsChanged](src/androidMain/kotlin/com/atriidev/warp_widget/WarpGlanceWidgetReceiver.kt):

1. **`scheduleLayoutReload`** — resolve size from the new options bundle, write `__warp_layout_w/h` + `__warp_layout_epoch` via [GlanceInternalState.touchLayout](src/androidMain/kotlin/com/atriidev/warp_widget/GlanceInternalState.kt), then `GlanceAppWidget.update()`.
2. **`rememberGlanceWidgetSession`** — prefers layout prefs when present so [WarpAdaptiveContent](#adaptive-layout-warpadaptive) and `env.size` track the resized widget.

Same “internal prefs changed → recompose” pattern as [system light / dark reload](#system-light--dark-reload) above.

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

Glance-style: **always pass [WarpWidgetId]** for update / read / reload. Changing [WarpWidget.stateScope] only changes fan-out — never whether an id is required (no runtime “missing id” when flipping Shared ↔ Instance).

| API | Role |
|-----|------|
| `WarpWidget<S>(serializer)` + `defaultState` | Typed serializable widget state |
| `WarpWidget.stateScope` | [Shared] (mirror all) or [Instance] (one id) |
| `WarpWidgetId` | `"aw:$appWidgetId"` (Android), `"ios:$family"` (iOS, Kotlin-derived), or kind id when Shared |
| `updateWarpWidgetState(context, widget, id) { S -> S }` | Required [id] — Shared still fans out |
| `updateWarpWidgetState(session, widget) { S -> S }` | Uses [WarpWidgetSession.widgetId] |
| `readWarpWidgetState(context, widget, id)` | Decode typed state for [id] |
| `listWarpWidgetIds(context, widget)` | Active instance ids |
| `reloadWarpWidget(context, widget, id)` | Reload for scope rules |
| `WarpStateKey` / `currentState` | Low-level string-key bag (optional) |

### Shared vs instance state

**Shared:** all instances share one JSON blob. Android mirrors WARP prefs to every active `GlanceId`; iOS App Group `"$kind.$key"`. Still pass an id (`WarpWidgetId.ofKind(widget.id)` or any from `listWarpWidgetIds`).

**Instance** — personal widgets:

```kotlin
object StocksWarpWidget : WarpWidget<StocksState>(StocksState.serializer()) {
    override val stateScope = WarpWidgetStateScope.Instance
    …
}

val ids = listWarpWidgetIds(context, StocksWarpWidget)
updateWarpWidgetState(context, StocksWarpWidget, ids.first()) {
    it.copy(symbols = listOf("AAPL"))
}

// Click handler:
updateWarpWidgetState(session, StocksWarpWidget) { it.copy(symbols = …) }
```

**iOS Swift stays normal** — no instance id in kit fields. Kotlin derives `WarpWidgetId` from kit `family` and injects it into click JSON for AppIntent cold starts.

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
  src/commonMain/…/ui/WarpTheme.kt        # WarpTheme, WarpColors
  src/commonMain/…/ui/WarpAdaptive.kt     # WarpAdaptiveSize, rememberWarpAdaptiveSize, WarpAdaptiveContent
  src/androidMain/…/WarpGlance*.kt        # GlanceAppWidget / Receiver bases
  src/androidMain/…/Glance*.kt            # Glance env/session helpers, registry
  src/androidMain/…/GlanceAppWidgetSize.kt  # minW×minH size resolve + layout pref override
  src/androidMain/…/GlanceInternalState.kt  # internal uiMode + layout prefs for reload
  src/androidMain/…/WarpWidgetAndroidReload.kt  # UI_MODE_CHANGED + resize → reload
  src/iosMain/…/WarpWidgetKitMapping.kt   # Kit env dict → Shared types
  src/iosMain/…/WarpWidgetHost.ios.kt     # iosSession()
  src/swift/warpBridge/                   # spm4Kmp thin bridge
```

## Related docs

- [warp-runtime](../warp-runtime/README.md) — compose DSL, nodes, actions
- [warp-ui](../warp-ui/README.md) — `WarpRender` / `warpWidgetJson` / clicks
- [warpWidgetKit](../warpWidgetKit/README.md) — SPM SwiftUI package
- Demo: [`shared/…/CounterWarpWidget.kt`](../shared/src/commonMain/kotlin/com/atriidev/kmpwidget/CounterWarpWidget.kt)
