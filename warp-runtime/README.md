# warp-runtime

Shared Kotlin Multiplatform library for describing widget UI in code and turning it into a **plain data tree** that can be serialized to JSON.

This is an early proof of concept. There is **no platform renderer yet** (no Glance, no SwiftUI). The output today is:

1. A `WarpNode` object tree in memory
2. A JSON string you can log, save, or send to a future Android/iOS renderer

---

## What problem does it solve?

Widgets on Android and iOS are built with different UI toolkits. WARP wants one shared way to describe a widget in Kotlin, then render it separately on each platform.

`warp-runtime` handles the **shared description** part:

```
You write UI with @Composable functions
        ↓
warp-runtime builds a WarpNode tree
        ↓
You get JSON (or the tree directly)
        ↓
(later) Android/iOS renderers read that data
```

Think of it like writing HTML as a data structure instead of drawing pixels immediately.

---

## Quick start

```kotlin
import com.atriidev.warp_runtime.compose.*
import com.atriidev.warp_runtime.nodes.modifier.*

// Build UI and get JSON in one step
val json = composeWarpToJson {
    WarpColumn {
        WarpText("Hello WARP")
        WarpButton(text = "Tap me", actionId = "tap")
    }
}

println(json)
```

Or use the built-in sample:

```kotlin
val json = sampleCounterWidgetJson()
println(json)
```

To get the tree object instead of JSON:

```kotlin
val tree: WarpNode = composeWarp {
    WarpRow {
        WarpText("42")
    }
}

val json = tree.toJson()
```

---

## Available UI components (PoC)

| Composable | What it becomes in JSON |
|------------|-------------------------|
| `WarpColumn { ... }` | `{ "type": "column", "children": [...] }` |
| `WarpRow { ... }` | `{ "type": "row", "children": [...] }` |
| `WarpText("Hello")` | `{ "type": "text", "text": "Hello" }` |
| `WarpButton(text, actionId)` | `{ "type": "button", "text": "...", "actionId": "..." }` |

All nodes can optionally take a `WarpModifier` (padding is supported today).

---

## Example JSON output

For the sample counter widget:

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
                    "actionId": "decrement"
                },
                {
                    "type": "text",
                    "text": "42"
                },
                {
                    "type": "button",
                    "text": "+",
                    "actionId": "increment"
                }
            ]
        }
    ]
}
```

Each node has a `"type"` field so JSON readers know which kind of node it is.

---

## How it works (step by step)

There are **two layers** inside warp-runtime. This is the most important idea to understand.

### Layer 1 — What you write (Compose-style API)

You write UI using `@Composable` functions that look similar to Jetpack Compose or Glance:

```kotlin
composeWarpToJson {
    WarpColumn {
        WarpText("Counter")
        WarpRow {
            WarpButton(text = "-", actionId = "decrement")
            WarpText("42")
        }
    }
}
```

Under the hood, the **Compose Compiler** plugin transforms these functions. You get a nice nested DSL, but nothing is drawn on screen.

### Layer 2 — What warp-runtime produces (data classes)

While your composables run, warp-runtime quietly builds a tree of **serializable data classes**:

- `WarpColumn`
- `WarpRow`
- `WarpText`
- `WarpButton`

All of them implement `WarpNode`, a sealed interface marked with `@Serializable`.

These are plain Kotlin objects — no Compose runtime UI, no Android views, no SwiftUI. Just data.

---

## The full pipeline

Here is exactly what happens when you call `composeWarpToJson { ... }`:

### Step 1 — Create an empty root

```kotlin
val root = RootHolder()
```

`RootHolder` is an internal bucket that will collect top-level nodes while composition runs.

### Step 2 — Start a one-time Compose composition

warp-runtime uses a small piece of **Compose Runtime** (not Compose UI):

- `Recomposer` — runs composition once
- `Composition` — executes your `@Composable` lambda
- `BroadcastFrameClock` — gives the recomposer a clock so it can finish

This is **not** a live UI that keeps recomposing at 60fps. It runs **once**, builds the tree, and stops. That is enough for widgets, which refresh only when data changes.

### Step 3 — Your composables register nodes

When `WarpColumn`, `WarpRow`, `WarpText`, or `WarpButton` run:

1. An internal **holder** object is created (for example `WarpColumnHolder`)
2. That holder is added to the current parent's `children` list
3. For containers (`Column`, `Row`), nested composables run inside that holder

Parent/child tracking uses `CompositionLocal`, similar to how Compose passes `LocalContext` or `LocalDensity` — but here it passes "which container am I inside right now?"

```
RootHolder
 └── WarpColumnHolder
      ├── WarpTextHolder("Counter")
      └── WarpRowHolder
           ├── WarpButtonHolder("-", "decrement")
           ├── WarpTextHolder("42")
           └── WarpButtonHolder("+", "increment")
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
WarpButtonHolder  →  WarpButton(text, actionId, modifier)
```

You now hold a `WarpNode` tree made of regular data classes.

### Step 5 — Serialize to JSON

```kotlin
fun composeWarpToJson(content) = composeWarp(content).toJson()

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
│  composeWarpToJson {                        │
│      WarpColumn {                           │
│          WarpText("Hello")                  │
│      }                                      │
│  }                                          │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│  Compose Compiler + Compose Runtime         │
│  (runs @Composable functions once)          │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│  Internal holders (WarpColumnHolder, etc.)  │
│  Built during composition                   │
└──────────────────┬──────────────────────────┘
                   │  toWarpNode()
                   ▼
┌─────────────────────────────────────────────┐
│  WarpNode data tree                         │
│  WarpColumn → WarpText                      │
│  (@Serializable data classes)               │
└──────────────────┬──────────────────────────┘
                   │  toJson()
                   ▼
┌─────────────────────────────────────────────┐
│  JSON string                                │
└─────────────────────────────────────────────┘
```

---

## Project structure

```
warp-runtime/
├── compose/
│   ├── WarpUi.kt           # Public @Composable API (WarpColumn, WarpRow, …)
│   ├── ComposeWarp.kt      # composeWarp(), composeWarpToJson(), toJson()
│   ├── WarpSamples.kt      # sampleCounterWidgetJson() example
│   └── internal/
│       ├── WarpComposeScope.kt   # CompositionLocal tree building
│       └── WarpNodeHolders.kt    # Internal holders + toWarpNode()
└── nodes/
    ├── WarpNode.kt         # Sealed serializable interface
    ├── WarpColumn.kt       # Public data classes
    ├── WarpRow.kt
    ├── WarpText.kt
    ├── WarpButton.kt
    └── modifier/
        ├── WarpModifier.kt
        └── WarpModifierExt.kt
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

If you define the UI in `commonMain` as a stored composable lambda (like `sampleCounterWidgetUi` in `WarpSamples.kt`), that also works.

### Buttons use action IDs, not click lambdas

```kotlin
WarpButton(text = "+", actionId = "increment")
```

Click handlers **cannot** go into JSON. Only the string `"increment"` is stored. A future platform layer will map that ID to a real callback on Android or iOS.

### No platform renderer yet

This module stops at the data/JSON layer. Rendering to Glance (Android) or SwiftUI (iOS) will be a separate step that reads `WarpNode` or JSON and draws the native widget.

### Dependencies (intentionally small)

- `compose-runtime` — powers `@Composable` and one-shot composition
- `kotlinx-serialization-json` — JSON output

No Compose UI, Material, Foundation, or platform widget libraries.

---

## Running tests

```bash
./gradlew :warp-runtime:iosSimulatorArm64Test
```

Tests live in `commonTest` and verify both the node tree shape and JSON output.

---

## What comes next

Planned direction for WARP (not implemented here yet):

- `WarpWidget<State>` — widget class with state and `update()`
- Gradle/KSP plugin — generate Android receiver + iOS widget boilerplate
- Platform renderers — `WarpNode` → Glance on Android, SwiftUI on iOS
- More nodes — `Image`, `Spacer`, `LazyColumn`, etc.

For now, `warp-runtime` proves the core idea: **write widget UI like Compose, get serializable data out.**
