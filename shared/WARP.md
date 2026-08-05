# WARP (Widget Abstraction Rendering Pipeline)

WARP lets you define a home-screen widget once in Kotlin and render it natively on:

- **Android** via Jetpack Glance
- **iOS** via WidgetKit + SwiftUI

## Architecture

```text
commonMain DSL  ->  WarpNode IR  ->  Android Glance renderer
                                 ->  iOS JSON bridge + SwiftUI renderer
```

### Layers

| Layer | Package | Responsibility |
|-------|---------|----------------|
| **W** Widget | `com.atriidev.warp.dsl` | Glance-like widget definitions |
| **A** Abstraction | `com.atriidev.warp.ir` | Platform-neutral IR nodes, modifiers, actions, state |
| **R** Rendering | `com.atriidev.warp.glance` (Android), `CounterWidgetExtension` (iOS) | Native UI translation |
| **P** Pipeline | `com.atriidev.warp.pipeline` | Compile DSL to IR and serialize for iOS |

## Quick start

Define a widget once:

```kotlin
object CounterWarpWidget : WarpWidgetDefinition(kind = "CounterWidget") {
    override fun provideContent(scope: WarpWidgetScope) {
        scope.row(
            modifier = WarpModifier()
                .background(WarpColor(argb = 0xFF00FF00))
                .padding(all = 16),
        ) {
            button("-", actionRunCallback(CounterActions.DECREMENT))
            text(stateKey = COUNTER_KEY, modifier = WarpModifier().defaultWeight())
            button("+", actionRunCallback(CounterActions.INCREMENT))
        }
    }
}
```

Android hosts it through `WarpGlanceWidget(CounterWarpWidget)`.

iOS builds the same IR through `WarpBridge.buildCounterWidgetJson(...)` and renders it in the widget extension.

## V1 API surface

Supported in V1:

- Layout: `row`, `column`
- Components: `text`, `button`
- Modifiers: `padding`, `background`, `defaultWeight`
- Alignment: vertical + horizontal on containers
- State: string key bindings (`stateKey`)
- Actions: `actionRunCallback(id)`

Not supported in V1 (will differ or fail explicitly later):

- Lazy lists / grids
- Images
- Compose `remember` inside widgets
- Full GlanceModifier parity
- 1:1 Material theming across platforms

## State model

- **Android app + widget**: `KmpDataStore` + Glance `PreferencesGlanceStateDefinition`
- **iOS app + extension**: App Group `group.com.atriidev.kmpwidget`
- **Shared action IDs**: `increment`, `decrement`

When updating from the app, call `WidgetUpdater.update(count)`.

## iOS setup requirements

1. Enable App Group `group.com.atriidev.kmpwidget` for app + extension in Apple Developer Portal
2. Set `TEAM_ID` in `iosApp/Configuration/Config.xcconfig`
3. Build `CounterWidgetExtension` target from Xcode
4. `ContentView` registers `WidgetCenterBridge` to reload timelines from Kotlin

## Android setup

Widget receiver remains in `shared/src/androidMain/AndroidManifest.xml`.

The counter widget entry point is:

- `CounterWidgetReceiver`
- `CounterWarpGlanceHost.instance`

## Testing

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Common tests cover IR shape, JSON round-trip, and shared action handlers.

## Adding a new widget

1. Create a `WarpWidgetDefinition` in `commonMain`
2. Register Android host with `WarpGlanceWidget(YourWidget)`
3. Expose a bridge method in `WarpBridge` if iOS needs JSON
4. Extend `SwiftUIRenderer` only if new node types are introduced
5. Map new actions in `WarpActionCallback` (Android) and App Intents (iOS)
