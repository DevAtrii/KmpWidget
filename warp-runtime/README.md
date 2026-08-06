# warp-runtime

Shared Kotlin Multiplatform library for describing widget UI in code and turning it into a **plain data tree** that can be serialized to JSON.

This is an early proof of concept. There is **no platform renderer yet** (no Glance, no SwiftUI). The output today is:

1. A `WarpNode` object tree in memory
2. A JSON string you can log, save, or send to a future Android/iOS renderer

---

## What problem does it solve?

Widgets on Android and iOS are built with different UI toolkits. WARP wants one shared way to describe a widget in Kotlin, then render it separately on each platform.

`warp-runtime` handles the **shared description** and **shared action contracts**:

```
You write UI with @Composable functions
        ↓
warp-runtime builds a WarpNode tree (recomposing when state changes)
        ↓
You get JSON (or the tree directly)
        ↓
(later) Android/iOS renderers read that data and forward clicks
```

Think of it like writing HTML as a data structure instead of drawing pixels immediately. Clicks are also data — not Kotlin lambdas in the tree.

---

## Quick start

### Static UI (no external state)

```kotlin
import com.atriidev.warp_runtime.compose.*
import com.atriidev.warp_runtime.example.counter.*
import com.atriidev.warp_runtime.nodes.actions.*
import com.atriidev.warp_runtime.nodes.modifier.*

val json = composeWarpToJson {
    WarpColumn {
        WarpText("Hello WARP")
        WarpButton(text = "Tap me", onClick = actionClick(CounterActions.Increment))
    }
}

println(json)
```

### State-driven UI (recommended for widgets)

Use the built-in counter example, or write your own:

```kotlin
import com.atriidev.warp_runtime.compose.*
import com.atriidev.warp_runtime.example.counter.*

val json = CounterWidget.toJson(count = 42)
// or: sampleCounterWidgetJson(count = 42)
```

Or compose manually with the same typed actions:

```kotlin
import com.atriidev.warp_runtime.compose.*
import com.atriidev.warp_runtime.example.counter.*
import com.atriidev.warp_runtime.nodes.actions.*

val json = composeWarpToJson(CounterWidget.State(count = 42)) { state ->
    WarpColumn {
        WarpText("Counter")
        WarpRow {
            WarpButton(text = "-", onClick = CounterActions.Decrement.asClickAction())
            WarpText(state.count.toString())
            WarpButton(text = "+", onClick = CounterActions.Increment.asClickAction())
        }
    }
}
```

When `state.count` changes and you call `composeWarp` again, you get a new `WarpNode` tree reflecting the updated value.

### Built-in sample

```kotlin
val json = sampleCounterWidgetJson(count = 42)
println(json)
```

### Get the tree object instead of JSON

```kotlin
val tree: WarpNode = composeWarp(CounterState(0)) { state ->
    WarpText("Count: ${state.count}")
}

val json = tree.toJson()
```

---

## Working with state

WARP supports three ways to drive UI from state. Pick the one that fits your widget refresh flow.

| Approach | Best for | API |
|----------|----------|-----|
| **Explicit state parameter** | Widget refresh — load state, compose, serialize, push to platform | `composeWarp(state) { s -> ... }` |
| **WarpComposition** | Same widget UI composed multiple times in one session | `WarpComposition(state) { ... }.updateState(newState)` |
| **mutableStateOf inside composables** | Local/derived UI state during a single composition | `remember { mutableStateOf(...) }` inside `composeWarp { }` |

### 1. Explicit state parameter

The simplest pattern for widget updates. Pass your widget state each time the platform asks for a new tree:

```kotlin
fun renderWidget(state: CounterState): WarpNode =
    composeWarp(state) { s ->
        WarpText("Count: ${s.count}")
    }

// Widget refresh cycle:
val tree = renderWidget(CounterState(count = 10))
val json = tree.toJson()
```

Each call with a new `CounterState` runs composition again and produces a fresh tree.

### 2. WarpComposition

Holds state for you and re-composes when you call `updateState`:

```kotlin
val warp = WarpComposition(CounterState(0), sampleCounterWidgetUi)

warp.updateState(CounterState(5))   // → new WarpNode
warp.currentNode().toJson()

warp.dispose()  // no-op today; reserved for future live-composition backends
```

Under the hood, each `updateState` calls `composeWarp` with the new state.

### 3. mutableStateOf inside composeWarp

If you use Compose's `mutableStateOf` inside your composables, WARP runs multiple recomposition passes until state settles:

```kotlin
composeWarp {
    val count = remember { mutableStateOf(0) }
    if (count.value == 0) count.value = 5
    WarpText(count.value.toString())  // final tree shows "5"
}
```

The internal tree is **cleared and rebuilt** on every recomposition pass so holders never accumulate duplicates.

> **Note:** `LaunchedEffect` + `delay` inside `composeWarp` does not work — composition finishes and disposes before async effects complete. Drive widget updates from outside via explicit state or platform refresh cycles.

---

## Actions and native clicks

Widget buttons cannot store Kotlin lambdas — taps must survive JSON and run on the native host later:

| Side | Where | What you write |
|------|-------|----------------|
| **Author** (common) | `WarpButton(onClick = …)` | Serializable `WarpAction` data |
| **Handle** (native) | Glance / WidgetKit callback | `onClick(actionId, parameters)` |

### Author side — declaring clicks

All actions implement the sealed `WarpAction` interface. Today the main type is `ClickAction`:

```kotlin
import com.atriidev.warp_runtime.nodes.actions.*

// Typed widget action
WarpButton(text = "+", onClick = actionClick(CounterActions.Increment))

// Typed ids
WarpButton(text = "+", onClick = CounterActions.Increment.asClickAction())

// With string parameters (JSON-safe metadata for the handler)
WarpButton(
    text = "+5",
    onClick = actionClick(CounterActions.Increment, "step" to "5"),
)
```

Define each action ID once:

```kotlin
enum class CounterActions(
    override val actionId: String,
) : WarpActionId {
    Increment("increment"),
    Decrement("decrement"),
}

WarpButton("+", onClick = CounterActions.Increment.asClickAction())
```

See `example/counter/` for the full sample widget.

`ClickAction` JSON shape:

```json
{
  "type": "click",
  "actionId": "increment",
  "parameters": {}
}
```

Future action types (`StartActivityAction`, deep links, etc.) add new `@SerialName` implementations of `WarpAction` without changing `WarpButton`.

### Native side — exhaustive action mapping

Native renderers receive `WarpButton.onClick: WarpAction`. Map it to native UI with `when`:

```kotlin
fun renderAction(action: WarpAction): NativeAction =
    when (action) {
        is ClickAction -> nativeClick(
            actionId = action.actionId,
            parameters = action.parameters,
        )
    }
```

Because `WarpAction` is sealed, Kotlin reports this `when` when a future action subtype needs
a new branch. The native callback receives `actionId` and `parameters`, then owns behavior,
state updates, and widget refresh.

Decode the wire ID into the widget enum for another exhaustive `when`:

```kotlin
when (clickAction.actionIdAs<CounterActions>()) {
    CounterActions.Increment -> increment()
    CounterActions.Decrement -> decrement()
}
```

### End-to-end click flow

```
Common UI                          Host (Android / iOS)
─────────────────────────────────────────────────────────
WarpButton(                        Renderer reads WarpButton.onClick
  onClick = CounterActions         → wires native onClick with action id
    .Increment.asClickAction()     User taps
)                                       ↓
     ↓                             ActionCallback / AppIntent
JSON: { onClick: {                      ↓
  type: "click",                   native onClick(
  actionId: "increment"              actionId,
}}                                   parameters
                                   )
                                        ↓
                                   update state + refresh widget
```

---

## Available UI components (PoC)

| Composable | What it becomes in JSON |
|------------|-------------------------|
| `WarpColumn { ... }` | `{ "type": "column", "children": [...] }` |
| `WarpRow { ... }` | `{ "type": "row", "children": [...] }` |
| `WarpText("Hello")` | `{ "type": "text", "text": "Hello" }` |
| `WarpButton(text, onClick)` | `{ "type": "button", "text": "...", "onClick": { "type": "click", "actionId": "..." } }` |

All nodes can optionally take a `WarpModifier` (padding is supported today).

---

## Example JSON output

For the sample counter widget with `CounterState(count = 42)`:

```json
{
    "type": "column",
    "modifier": {
        "padding": {
            "start": 16,
            "end": 16,
            "top": 16,
            "bottom": 16
        }
    },
    "children": [
        {
            "type": "text",
            "text": "Counter"
        },
        {
            "type": "row",
            "children": [
                {
                    "type": "button",
                    "text": "-",
                    "onClick": {
                        "type": "click",
                        "actionId": "decrement",
                        "parameters": {}
                    }
                },
                {
                    "type": "text",
                    "text": "42"
                },
                {
                    "type": "button",
                    "text": "+",
                    "onClick": {
                        "type": "click",
                        "actionId": "increment",
                        "parameters": {}
                    }
                }
            ]
        }
    ]
}
```

Each node and nested action has a `"type"` field for polymorphic decoding.

---

## How it works (step by step)

There are **two layers** inside warp-runtime:

1. **Compose DSL** — what you write (`WarpColumn`, `WarpButton`, …)
2. **Node tree** — serializable data classes (`WarpNode`, `WarpAction`)

### Layer 1 — What you write (Compose-style API)

You write UI using `@Composable` functions that look similar to Jetpack Compose or Glance:

```kotlin
composeWarpToJson(CounterState(count = 42)) { state ->
    WarpColumn {
        WarpText("Counter")
        WarpRow {
            WarpButton(text = "-", onClick = CounterActions.Decrement.asClickAction())
            WarpText(state.count.toString())
            WarpButton(text = "+", onClick = CounterActions.Increment.asClickAction())
        }
    }
}
```

Under the hood, the **Compose Compiler** plugin transforms these functions. You get a nice nested DSL, but nothing is drawn on screen.

### Layer 2 — What warp-runtime produces (data classes)

While your composables run, warp-runtime quietly builds a tree of **serializable data classes**:

- `WarpColumn`, `WarpRow`, `WarpText`, `WarpButton` — all implement `WarpNode`
- `ClickAction` (and future types) — implement `WarpAction` on `WarpButton.onClick`

These are plain Kotlin objects — no Compose runtime UI, no Android views, no SwiftUI. Just data.

---

## The full pipeline

Here is exactly what happens when you call `composeWarpToJson { ... }` or `composeWarp(state) { ... }`:

### Step 1 — Create an empty root

```kotlin
val root = RootHolder()
```

`RootHolder` is an internal bucket that will collect top-level nodes while composition runs.

### Step 2 — Run Compose Runtime (with recomposition support)

warp-runtime uses a small piece of **Compose Runtime** (not Compose UI):

- `Recomposer` — drives composition and recomposition
- `Composition` — executes your `@Composable` lambda
- `BroadcastFrameClock` — sends frames so pending recompositions can finish

This is **not** a live 60fps UI. It runs until all state-driven recompositions settle, builds the tree, and returns. That matches how widgets work: compose when data changes, then hand off JSON to the platform.

If `mutableStateOf` changes during composition, WARP sends additional frames (up to 3) and waits for the recomposer to become idle before converting the result.

### Step 3 — WarpRootContent clears and rebuilds the tree

Every recomposition pass starts by clearing `RootHolder.children`. Then your composables register fresh holders:

1. An internal **holder** object is created (for example `WarpColumnHolder`)
2. That holder is added to the current parent's `children` list
3. For containers (`Column`, `Row`), nested composables run inside that holder

Parent/child tracking uses `CompositionLocal`, similar to how Compose passes `LocalContext` or `LocalDensity` — but here it passes "which container am I inside right now?"

```
RootHolder
 └── WarpColumnHolder
      ├── WarpTextHolder("Counter")
      └── WarpRowHolder
           ├── WarpButtonHolder("-", ClickAction("decrement"))
           ├── WarpTextHolder("42")
           └── WarpButtonHolder("+", ClickAction("increment"))
```

Holders are **internal** and **not** serialized. They exist only during composition.

### Step 4 — Convert holders → WarpNode tree

After composition finishes:

```kotlin
return root.toWarpNode()
```

Each holder has a `toWarpNode()` function that creates the public, immutable, serializable data class:

```
WarpColumnHolder  →  WarpColumn(modifier, children)
WarpTextHolder    →  WarpText(text, modifier)
WarpButtonHolder  →  WarpButton(text, onClick, modifier)
```

You now hold a `WarpNode` tree made of regular data classes.

### Step 5 — Serialize to JSON

```kotlin
fun composeWarpToJson(state, content) = composeWarp(state, content).toJson()

fun WarpNode.toJson() = Json.encodeToString(this)
```

`kotlinx.serialization` walks the `WarpNode` tree and writes JSON.

Settings used today:

- `prettyPrint = true` — readable indented output
- `classDiscriminator = "type"` — adds `"type": "column"` etc. so polymorphic types are clear in JSON

---

## Visual summary

```
┌─────────────────────────────────────────────┐
│  Your code                                  │
│  composeWarp(CounterState(42)) { state ->   │
│      WarpColumn {                           │
│          WarpButton("+", onClick = …)       │
│          WarpText("${state.count}")         │
│      }                                      │
│  }                                          │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│  Compose Compiler + Compose Runtime         │
│  (compose + recompose until state settles)  │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│  WarpRootContent clears root each pass      │
│  Internal holders rebuilt (ColumnHolder…)   │
└──────────────────┬──────────────────────────┘
                   │  toWarpNode()
                   ▼
┌─────────────────────────────────────────────┐
│  WarpNode data tree                         │
│  WarpColumn → WarpButton(onClick: ClickAction) │
│  (@Serializable data classes)               │
└──────────────────┬──────────────────────────┘
                   │  toJson()
                   ▼
┌─────────────────────────────────────────────┐
│  JSON string  →  platform renderer          │
└──────────────────┬──────────────────────────┘
                   │  user tap
                   ▼
┌─────────────────────────────────────────────┐
│  Native onClick(actionId, parameters)       │
│  → update state + refresh widget            │
└─────────────────────────────────────────────┘
```

---

## Project structure

```
warp-runtime/
├── compose/
│   ├── WarpUi.kt           # Public @Composable API (WarpColumn, WarpRow, …)
│   ├── ComposeWarp.kt      # composeWarp(), composeWarpToJson(), toJson()
│   ├── WarpComposition.kt  # Stateful WarpComposition<S> with updateState()
│   ├── WarpSamples.kt      # Internal test-only composables
│   └── internal/
│       ├── WarpComposeScope.kt   # WarpRootContent, CompositionLocal tree building
│       └── WarpNodeHolders.kt    # Internal holders + toWarpNode()
├── example/
│   └── counter/            # Sample counter widget (not core API)
│       ├── CounterWidget.kt        # State, UI, toJson()
│       └── CounterActions.kt       # Widget-specific WarpActionId enum
├── nodes/
│   ├── WarpNode.kt         # Sealed serializable interface
│   ├── WarpColumn.kt       # Public node data classes
│   ├── WarpRow.kt
│   ├── WarpText.kt
│   ├── WarpButton.kt       # onClick: WarpAction
│   ├── modifier/
│   │   ├── WarpModifier.kt
│   │   └── WarpModifierExt.kt
│   └── actions/
│       ├── WarpAction.kt       # Sealed action interface
│       └── ClickAction.kt      # actionClick(), WarpActionId, typed decoding
```

---

## Important things to know

### Compose Compiler is required

Any module that **calls** `composeWarp { }` with an inline lambda must apply the Compose Compiler Gradle plugin.

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.composeCompiler)
}
```

This applies to `shared`, app modules, etc. The lambda `{ WarpText("Hi") }` must be compiled as a composable lambda.

If you define the UI in `commonMain` as a stored composable lambda (like `CounterWidget.ui` in `example/counter/`), that also works.

### Widget state should be serializable data classes

Use `@Serializable` data classes for state you persist or pass across platforms:

```kotlin
@Serializable
data class CounterState(val count: Int = 0)
```

Pass them to `composeWarp(state) { ... }` or `WarpComposition`. This mirrors how Glance uses `GlanceStateDefinition` — state lives outside the UI tree, composition reads it.

### No platform renderer yet

This module stops at the data/JSON layer. Rendering `WarpNode` → Glance (Android) or SwiftUI
(iOS), then forwarding `ClickAction.actionId` and parameters, belongs to native host modules.

### Dependencies (intentionally small)

- `compose-runtime` — powers `@Composable`, composition, and recomposition
- `kotlinx-serialization-json` — JSON output

No Compose UI, Material, Foundation, or platform widget libraries.

---

## Running tests

```bash
./gradlew :warp-runtime:jvmTest
./gradlew :warp-runtime:iosSimulatorArm64Test
```

Tests in `commonTest` verify:

- Node tree shape and JSON output
- `ClickAction` serialization on buttons
- State parameter changes produce different trees
- `WarpComposition.updateState()` returns updated trees
- `mutableStateOf` triggers recomposition inside `composeWarp`

---

## What comes next

Planned direction for WARP (not implemented here yet):

- **Platform renderers** — `WarpNode` → Glance on Android, SwiftUI on iOS
- **Glance dispatcher** — single `ActionCallback` forwards `actionId` and parameters
- **New action types** — `StartActivityAction`, deep links
- **`WarpWidget<State>`** — widget class with `update()` and platform integration
- **Gradle/KSP plugin** — generate Android receiver + iOS widget boilerplate
- **More nodes** — `Image`, `Spacer`, `LazyColumn`, etc.

For now, `warp-runtime` proves the core idea: **write widget UI like Compose, declare clicks
as data, and let native hosts handle `actionId` plus parameters.**

---

## Click dispatch (native UI)

See **[README_CLICK.md](./README_CLICK.md)** for native click callbacks and **[warp-ui/README.md](../warp-ui/README.md)** for the Glance renderer (`WarpRender`, `WarpClicksRegistry`).
