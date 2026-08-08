# Creating Your First Widget

Welcome to **Warp**! Warp allows you to build cross-platform home screen widgets for **Android (Glance)** and **iOS (WidgetKit)** using a single, unified Kotlin Multiplatform codebase.

This guide walks you step-by-step through creating an interactive widget, using the reference implementation from `CounterWarpWidget`.

---

## Architecture Overview

Building a Warp widget involves four core building blocks:

```
┌────────────────────────────────────────────────────────┐
│                   1. State Model                       │
│    @Serializable data class CounterState(...)          │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                  2. Actions & Clicks                   │
│    @Serializable sealed class CounterActions           │
│    class CounterWarpClickHandler : WarpClickHandler    │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                  3. Widget UI & Theme                  │
│    object CounterWarpWidget : WarpWidget<CounterState> │
│    @Composable fun Content(...)                        │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│               4. App & Host Integration                │
│    Android (GlanceAppWidget) & iOS (SwiftUI / Intent)  │
└────────────────────────────────────────────────────────┘
```

---

## Step 1: Define the Widget State

Your widget needs a serializable data model to represent its state. Warp persists this state automatically as JSON in `SharedPreferences` (Android) or `UserDefaults` / App Group (iOS).

```kotlin
// CounterWarpWidget.kt
@Serializable
enum class WidgetMode {
    @SerialName("counter") Counter,
    @SerialName("todo") Todo,
}

@Serializable
data class TodoItem(
    val id: String,
    val title: String,
    val done: Boolean = false,
)

@Serializable
@Stable
data class CounterState(
    val mode: WidgetMode = WidgetMode.Counter,
    val count: Int = 0,
    val todos: List<TodoItem> = SampleTodos,
)
```

- `@Serializable`: Enables automatic JSON serialization across platforms.
- `@Stable`: Advises Compose compiler for recomposition optimizations.

---

## Step 2: Define Actions & Event Handlers

Actions represent user interactions on the widget (e.g., button taps or chip clicks).

### 2.1 Declare Type-Safe Actions
```kotlin
@Serializable
sealed class CounterActions {
    @Serializable
    data object Increment : CounterActions()

    @Serializable
    data object Decrement : CounterActions()

    @Serializable
    data object Reset : CounterActions()

    @Serializable
    data class SwitchMode(val mode: WidgetMode) : CounterActions()

    @Serializable
    data class ToggleTodo(val todoId: String) : CounterActions()
}
```

### 2.2 Implement Click Handler
Handle incoming user actions and mutate persistent state via `updateWarpWidgetState`:

```kotlin
class CounterWarpClickHandler(
    private val session: WarpWidgetSession,
) : WarpClickHandler<CounterActions>(CounterActions.serializer()) {

    override suspend fun onClick(action: CounterActions) {
        updateWarpWidgetState(session, CounterWarpWidget) { state ->
            when (action) {
                CounterActions.Increment -> state.copy(count = state.count + 1)
                CounterActions.Decrement -> state.copy(count = state.count - 1)
                CounterActions.Reset -> state.copy(count = 0)
                is CounterActions.SwitchMode -> state.copy(mode = action.mode)
                is CounterActions.ToggleTodo -> state.copy(
                    todos = state.todos.map { todo ->
                        if (todo.id == action.todoId) todo.copy(done = !todo.done) else todo
                    },
                )
            }
        }
    }
}
```

---

## Step 3: Define the Widget Object & Composables

Create a singleton `object` extending `WarpWidget<T>` and specify your layout in `@Composable override fun Content(...)`.

```kotlin
object CounterWarpWidget : WarpWidget<CounterState>(CounterState.serializer()) {
    override val id: String = "CounterWidget"
    override val iosGroupId: String = APP_GROUP_ID
    override val stateScope: WarpWidgetStateScope = WarpWidgetStateScope.Instance
    override suspend fun defaultState(): CounterState = CounterState()

    @Composable
    override fun Content(env: WidgetEnvironment, state: CounterState) {
        WarpTheme(environment = env) {
            WarpAdaptiveContent(
                environment = env,
                small = { CounterWidgetContent(state, env, compact = true) },
                medium = { CounterWidgetContent(state, env) },
                large = { CounterWidgetContent(state, env, spacious = true) },
            )
        }
    }

    override fun clickHandlers(session: WarpWidgetSession): List<WarpClickHandler<*>> =
        listOf(CounterWarpClickHandler(session))
}
```

### Layout Components & Adaptive UI
Warp provides declarative primitives like `WarpBox`, `WarpColumn`, `WarpRow`, `WarpText`, `WarpButton`, and `WarpImage`.

Use `WarpAdaptiveContent` or `env.adaptiveValue()` to dynamically scale font sizes, padding, and visible rows for **Small**, **Medium**, and **Large** widget sizes.

```kotlin
val buttonSize = env.adaptiveValue(small = 36, medium = 40, large = 48)
val countFontSize = env.adaptiveValue(small = 22f, medium = 26f, large = 32f)

WarpButton(
    text = "+",
    onClick = CounterActions.Increment.asClickAction(),
    modifier = WarpModifier
        .size(buttonSize)
        .cornerRadius(buttonSize / 2),
    style = WarpTextStyle(fontSize = 18f, fontWeight = WarpFontWeight.Bold),
    colors = WarpButtonColors.of(backgroundColor = "#27AE60", contentColor = "#FFFFFF"),
)
```

---

## Step 4: Interact with Widget State from App UI

Your main application (`App.kt`) can read or update widget state directly:

### 4.1 State Helper Functions (`CounterWidgetState.kt`)
```kotlin
suspend fun readCounterWidgetState(context: PlatformContext): CounterState {
    val ids = listWarpWidgetIds(context, CounterWarpWidget)
    if (ids.isEmpty()) return CounterWarpWidget.defaultState()
    return readWarpWidgetState(context, CounterWarpWidget, ids.first())
}

suspend fun updateAllCounterWidgetInstances(
    context: PlatformContext,
    transform: (CounterState) -> CounterState,
) {
    listWarpWidgetIds(context, CounterWarpWidget).forEach { id ->
        updateWarpWidgetState(context, CounterWarpWidget, id, transform)
    }
}
```

### 4.2 Updating State from App Composable
```kotlin
// App.kt
val scope = rememberCoroutineScope()

Button(
    onClick = {
        scope.launch {
            updateAllCounterWidgetInstances(platformContext) { currentState ->
                currentState.copy(count = currentState.count + 1)
            }
        }
    }
) {
    Text("Increment Widget Count")
}
```

---

## Step 5: iOS Host Integration (SwiftUI, AppIntents & WidgetKit)

On iOS (`iosApp/CounterWidget`), your WidgetKit extension target links the `Shared` KMP framework and `warpWidgetKit`.

### 5.1 App Group Entitlements (`CounterWidget.entitlements`)

Both the main iOS app and the WidgetKit extension must share the same **App Group ID** so Kotlin's `UserDefaults` storage syncs between app and widget.

```xml
<!-- CounterWidget.entitlements -->
<dict>
    <key>com.apple.security.application-groups</key>
    <array>
        <string>group.com.atriidev.kmpwidget</string>
    </array>
</dict>
```

---

### 5.2 Widget Bundle & Extension Entry Point (`CounterWidgetBundle.swift`)

The `@main` `WidgetBundle` registers your click intent handler with `warpWidgetKit` and prepares `WarpWidgetHost`:

```swift
// CounterWidgetBundle.swift
import Shared
import SwiftUI
import WidgetKit
import warpWidgetKit

@main
struct CounterWidgetBundle: WidgetBundle {
    init() {
        if #available(iOS 17.0, *) {
            // Register Swift AppIntent handler for CounterWarpWidget.id
            WarpClickIntentRegistry.install(
                CounterWidgetClickIntent.self,
                for: CounterWarpWidget.shared.id
            )
        }
        
        // Prepare initial host session
        let session = WarpWidgetHost.shared.iosSession(
            widget: CounterWarpWidget.shared,
            kitFields: WarpWidgetKitEnv.placeholder().asKitFields(
                appGroupId: CounterWarpWidget.shared.iosGroupId
            )
        )
        WarpWidgetHost.shared.prepare(widget: CounterWarpWidget.shared, session: session)
    }

    var body: some Widget {
        CounterHomeWidget()
    }
}
```

---

### 5.3 Interactive Tap Actions (`CounterWidgetClickIntent.swift`)

For iOS 17+ interactive buttons and list rows, implement `WarpClickAppIntent`:

```swift
// CounterWidgetClickIntent.swift
import AppIntents
import Shared
import warpWidgetKit

@available(iOS 17.0, *)
struct CounterWidgetClickIntent: WarpClickAppIntent {
    static var title: LocalizedStringResource = "Counter Widget Click"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Action ID")
    var actionId: String

    @Parameter(title: "Parameters JSON")
    var parametersJson: String

    init() {
        actionId = ""
        parametersJson = "{}"
    }

    init(actionId: String, parametersJson: String) {
        self.actionId = actionId
        self.parametersJson = parametersJson
    }

    func perform() async throws -> some IntentResult {
        let session = WarpWidgetHost.shared.iosSession(
            widget: CounterWarpWidget.shared,
            kitFields: WarpWidgetKitEnv.placeholder().asKitFields(
                appGroupId: CounterWarpWidget.shared.iosGroupId
            )
        )
        
        // Dispatch tap event to Kotlin CounterWarpClickHandler
        WarpWidgetHost.shared.dispatchClick(
            widget: CounterWarpWidget.shared,
            session: session,
            actionId: actionId,
            parametersJson: parametersJson
        )
        
        // Refresh WidgetKit UI
        WarpWidgetBridge.shared.reloadTimelinesOfKind(CounterWarpWidget.shared.id)
        return .result()
    }
}
```

---

### 5.4 WidgetKit Provider & Definition (`CounterWidget.swift`)

Define `CounterHomeWidget` using WidgetKit's `StaticConfiguration`:

```swift
// CounterWidget.swift
struct CounterHomeWidget: Widget {
    let kind: String = "CounterWidget" // Matches CounterWarpWidget.id

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: CounterWidgetProvider()) { entry in
            if #available(iOS 17.0, *) {
                CounterWidgetEntryView(entry: entry)
                    .containerBackground(.fill.tertiary, for: .widget)
            } else {
                CounterWidgetEntryView(entry: entry)
                    .padding()
            }
        }
        .contentMarginsDisabled()
        .configurationDisplayName("Counter")
        .description("WARP counter widget")
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge])
    }
}
```

---

### 5.5 Rendering JSON via `WarpSwiftUIRootView` (`CounterWidgetView.swift`)

`CounterWidgetEntryView` uses SwiftUI's `@Environment` variables to dynamically pass `colorScheme`, `widgetFamily`, `widgetRenderingMode`, and `displaySize` into Kotlin's JSON renderer:

```swift
// CounterWidget.swift
struct CounterWidgetEntryView: View {
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.widgetFamily) private var widgetFamily
    @Environment(\.widgetRenderingMode) private var widgetRenderingMode

    var entry: CounterWidgetProvider.Entry

    var body: some View {
        WarpSwiftUIRootView(
            json: composeWidgetJson(
                colorScheme: colorScheme,
                widgetFamily: widgetFamily,
                widgetRenderingMode: widgetRenderingMode,
                displaySize: CGSize(width: entry.displayWidth, height: entry.displayHeight)
            ),
            useIntents: true,
            widgetId: CounterWarpWidget.shared.id
        )
    }
}

// CounterWidgetView.swift
func composeWidgetJson(
    colorScheme: ColorScheme,
    widgetFamily: WidgetFamily,
    widgetRenderingMode: WidgetRenderingMode? = nil,
    displaySize: CGSize? = nil,
    isPreview: Bool = false
) -> String {
    let kitEnv = WarpWidgetKitEnv.from(
        colorScheme: colorScheme,
        family: WarpWidgetKitEnv.Family(widgetFamily: widgetFamily),
        width: displaySize?.width,
        height: displaySize?.height,
        isPreview: isPreview,
        widgetRenderingMode: widgetRenderingMode
    )
    
    let session = WarpWidgetHost.shared.iosSession(
        widget: CounterWarpWidget.shared,
        kitFields: kitEnv.asKitFields(appGroupId: CounterWarpWidget.shared.iosGroupId)
    )
    WarpWidgetHost.shared.prepare(widget: CounterWarpWidget.shared, session: session)
    return WarpWidgetHost.shared.composeJson(widget: CounterWarpWidget.shared, session: session)
}
```

---


## Step 6: Android Host Integration (Glance AppWidget)

On Android, Warp integrates with **Jetpack Glance**. You set up a `WarpGlanceWidget` and a `WarpGlanceWidgetReceiver` inside `shared/src/androidMain`.

### 6.1 Implement Glance AppWidget & Receiver (`CounterWidgetGlance.kt`)

```kotlin
// shared/src/androidMain/kotlin/com/atriidev/kmpwidget/CounterWidgetGlance.kt

/** Receiver registered in AndroidManifest.xml */
class CounterWidgetReceiver : WarpGlanceWidgetReceiver() {
    init {
        ensureRegistered()
    }

    override val widget get() = CounterWarpWidget
    override fun createGlanceWidget() = CounterGlanceAppWidget()
}

/** Glance host mapping Warp assets to Android drawable resources */
class CounterGlanceAppWidget : WarpGlanceWidget() {
    override val widget get() = CounterWarpWidget

    override fun assets(): List<WarpDrawableAsset> = listOf(
        WarpDrawableAsset(CounterAssets.NumberCircle, R.drawable.ic_number_circle),
        WarpDrawableAsset(CounterAssets.Checklist, R.drawable.ic_checklist),
        WarpDrawableAsset(CounterAssets.Circle, R.drawable.ic_circle),
        WarpDrawableAsset(CounterAssets.CheckCircle, R.drawable.ic_check_circle),
    )
}
```

### 6.2 Widget Provider XML Metadata (`my_app_widget_info.xml`)

Create `res/xml/my_app_widget_info.xml` in `shared/src/androidMain/res/xml/`:

```xml
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:minWidth="180dp"
    android:minHeight="90dp"
    android:minResizeWidth="90dp"
    android:minResizeHeight="90dp"
    android:resizeMode="horizontal|vertical" />
```

### 6.3 Register Receiver in AndroidManifest.xml

Declare `CounterWidgetReceiver` inside `<application>` in `shared/src/androidMain/AndroidManifest.xml` (or `androidApp/src/main/AndroidManifest.xml`):

```xml
<application>
    <receiver
        android:name="com.atriidev.kmpwidget.CounterWidgetReceiver"
        android:exported="true">
        <intent-filter>
            <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            <action android:name="android.intent.action.CONFIGURATION_CHANGED" />
            <action android:name="android.intent.action.UI_MODE_CHANGED" />
        </intent-filter>
        <meta-data
            android:name="android.appwidget.provider"
            android:resource="@xml/my_app_widget_info" />
    </receiver>
</application>
```

---

## Summary Checklist

- [x] **State Model**: `@Serializable` data class.
- [x] **Actions & Clicks**: `@Serializable` sealed class + `WarpClickHandler`.
- [x] **Widget Definition**: Extends `WarpWidget<T>`, uses `WarpTheme` & `WarpAdaptiveContent`.
- [x] **App Integration**: `updateWarpWidgetState` from main App Compose UI.
- [x] **iOS Host**: `WarpSwiftUIRootView` in SwiftUI WidgetKit target.
- [x] **Android Host**: `WarpGlanceWidgetReceiver`, `WarpGlanceWidget`, `@xml/my_app_widget_info`, and `AndroidManifest.xml` declaration.

