# warp-ui

Platform renderers for WARP `WarpNode` trees.

**Status:** Android (Jetpack Glance) ✓ · iOS (WidgetKit) — not implemented yet

## Architecture

```
commonMain
──────────
WarpRender(node, handlers)     → registers handlers, renders tree
WarpClickHandler<T>            → typed onClick(actionId: T, parameters)
WarpClicksRegistry             → wire actionId → handler dispatch

androidMain
───────────
WarpRegistryActionCallback     → Glance ActionCallback → registry.dispatch
```

No per-widget Glance callback subclasses. Pass shared [WarpClickHandler] instances from your app module.

## Quick start (Android)

```kotlin
import com.atriidev.kmpwidget.counterWidgetClickHandlers
import com.atriidev.warp_ui.WarpRender

provideContent {
    val node = composeWarp(CounterWidget.State(count = 0), CounterWidget.ui)
    WarpRender(
        node = node,
        handlers = counterWidgetClickHandlers(
            dataStore = KmpDataStore(context),
            widgetUpdater = WidgetUpdater(context),
        ),
    )
}
```

```kotlin
// commonMain — shared handler
class CounterClickHandler(
    dataStore: KmpDataStore,
    widgetUpdater: WidgetUpdater,
) : WarpClickHandler<CounterActions>(CounterActions::class) {
    override suspend fun onClick(actionId: CounterActions, parameters: Map<String, String>) {
        when (actionId) {
            CounterActions.Increment -> update(+1)
            CounterActions.Decrement -> update(-1)
        }
    }
}
```

## Public API

| API | Role |
|-----|------|
| `WarpRender(node, handlers)` | Register handlers + render `WarpNode` tree |
| `WarpClickHandler<T>` | Typed click contract; pass `actionIdType` to base |
| `WarpClicksRegistry` | Wire `actionId` → handler map; filled by `WarpRender` |

## Click flow

```
WarpButton.onClick (ClickAction)
    → WarpRender encodes actionId + parameters into Glance ActionParameters
    → user taps
    → WarpRegistryActionCallback → WarpClicksRegistry.dispatch(actionId)
    → handler.onClick(typedActionId, parameters)
```

## Example in this repo

[`shared/.../CounterWidgetGlance.kt`](../shared/src/androidMain/kotlin/com/atriidev/kmpwidget/CounterWidgetGlance.kt) + [`CounterClickHandler.kt`](../shared/src/commonMain/kotlin/com/atriidev/kmpwidget/CounterClickHandler.kt)

## Related docs

- [warp-runtime README](../warp-runtime/README.md)
- [Click dispatch guide](../warp-runtime/README_CLICK.md)
