# Native click dispatch

Guide for Android Glance and iOS WidgetKit renderer developers.

WARP does not execute click handlers in common code. It serializes an action, then the
native renderer forwards that action to the platform callback. Common code encodes typed
actions to wire `ClickAction`; [`warp-ui`](../warp-ui/README.md) decodes them back to
typed sealed instances in [`WarpClickHandler`](../warp-ui/src/commonMain/kotlin/com/atriidev/warp_ui/WarpClickHandler.kt).

## Contract

Common UI:

```kotlin
WarpButton(
    text = "+",
    onClick = CounterActions.Increment.asClickAction(),
)
```

Serialized node:

```json
{
  "type": "button",
  "text": "+",
  "onClick": {
    "type": "click",
    "actionId": "increment",
    "parameters": {}
  }
}
```

Parameterized action:

```json
{
  "type": "click",
  "actionId": "toggle_todo",
  "parameters": { "todoId": "1" }
}
```

Native callback (unchanged — still string wire id + string map):

```kotlin
onClick(
    actionId = action.actionId,
    parameters = action.parameters,
)
```

Platform bridges do not need to know about sealed classes. [`WarpClicksRegistry`](../warp-ui/src/commonMain/kotlin/com/atriidev/warp_ui/WarpClicksRegistry.kt)
maps wire ids to handlers; the handler family decodes wire → typed action.

## Define typed actions (recommended)

Define a `@Serializable sealed class` with variants and parameters only. Wire ids and
encode/decode are automatic via kotlinx.serialization — no manual `actionId`, `parameters()`,
or `WarpActionFamily` implementation.

```kotlin
@Serializable
sealed class CounterActions {
    @Serializable
    data object Increment : CounterActions()

    @Serializable
    data object Decrement : CounterActions()

    @Serializable
    data class SwitchMode(val mode: WidgetMode) : CounterActions()

    @Serializable
    data class ToggleTodo(val todoId: String) : CounterActions()
}
```

Use typed instances in shared UI:

```kotlin
WarpButton("-", onClick = CounterActions.Decrement.asClickAction())
WarpButton("+", onClick = CounterActions.Increment.asClickAction())
WarpButton("Todo", onClick = CounterActions.SwitchMode(WidgetMode.Todo).asClickAction())

// Modifier overload — same encode path
WarpText(
    "Buy milk",
    modifier = WarpModifier.clickable(CounterActions.ToggleTodo("1")),
)
```

### Wire id derivation

| Variant | Wire `actionId` |
|---------|-----------------|
| `Increment` | `increment` |
| `SwitchMode` | `switch_mode` |
| `ToggleTodo` | `toggle_todo` |

Rules:

- Default: snake_case of the variant class name (`SwitchMode` → `switch_mode`).
- Override with `@SerialName("custom_id")` on a variant when needed.
- Enum parameters use their `@SerialName` values on the wire (e.g. `"mode": "todo"`).
- `parameters` values are strings in JSON; ints/bools round-trip via the codec.

Implementation: [`WarpTypedAction.kt`](src/commonMain/kotlin/com/atriidev/warp_runtime/nodes/actions/WarpTypedAction.kt).

### Encode / decode API

```kotlin
// UI → wire
CounterActions.ToggleTodo("1").asClickAction()
// → ClickAction(actionId = "toggle_todo", parameters = mapOf("todoId" to "1"))

// Wire → typed (used internally by WarpClickHandler)
val family = warpActionFamily(CounterActions.serializer())
family.decode("toggle_todo", mapOf("todoId" to "1"))
// → CounterActions.ToggleTodo(todoId = "1")
```

## Legacy enum actions

Param-less widgets can still use `WarpActionId` enums and `actionClick()`:

```kotlin
enum class CounterActions(override val actionId: String) : WarpActionId {
    Increment("increment"),
    Decrement("decrement"),
}

WarpButton("-", onClick = CounterActions.Decrement.asClickAction())

// With dynamic metadata
WarpButton(
    text = "Open",
    onClick = actionClick(ItemActions.Open, "itemId" to "42"),
)
```

Decode on the native side with `ClickAction.actionIdAs<CounterActions>()`. Prefer the
sealed-class approach for new widgets — it gives typed parameters and exhaustive `when`
without string lookups.

## Renderer responsibility

The renderer receives `WarpButton.onClick: WarpAction`. Handle it with an exhaustive `when`:

```kotlin
fun renderAction(action: WarpAction): NativeAction =
    when (action) {
        is ClickAction -> nativeClick(
            actionId = action.actionId,
            parameters = action.parameters,
        )
    }
```

`WarpAction` is sealed. When WARP later adds `StartActivityAction`, `DeepLinkAction`, etc.,
Kotlin reports every non-exhaustive `when (action)` and asks the renderer developer to add
the missing branch.

There are two exhaustive checks:

1. `when (action)` covers WARP action types (`ClickAction`, future `StartActivityAction`, etc.).
2. `when (action)` in [`WarpClickHandler.onClick`](../warp-ui/src/commonMain/kotlin/com/atriidev/warp_ui/WarpClickHandler.kt) covers every variant in the widget's sealed hierarchy.

## warp-ui (Android Glance / iOS WidgetKit)

For a concrete app widget, use [`warp-ui`](../warp-ui/README.md):

```kotlin
provideContent {
    val node = composeWarp(state, CounterWidget.ui)
    WarpRender(
        node = node,
        handlers = counterWidgetClickHandlers(session),
    )
}

class CounterClickHandler(
    session: WarpWidgetSession,
) : WarpClickHandler<CounterActions>(CounterActions.serializer()) {
    override suspend fun onClick(action: CounterActions) {
        when (action) {
            CounterActions.Increment -> updateCount(+1)
            CounterActions.Decrement -> updateCount(-1)
            is CounterActions.SwitchMode -> switchMode(action.mode)
            is CounterActions.ToggleTodo -> toggleTodo(action.todoId)
        }
    }
}
```

Pass the generated sealed serializer — `CounterActions.serializer()`. Registration of all
wire ids and wire → typed decode is automatic.

`WarpRender` registers handlers in `WarpClicksRegistry`; one Glance callback / one Swift
dispatch path forwards all clicks.

Flow:

```
UI: CounterActions.ToggleTodo(id).asClickAction()
  → ClickAction JSON in WARP tree
  → Glance / AppIntent (actionId + parameters)
  → WarpClicksRegistry.dispatch
  → family.decode(wireId, parameters)
  → WarpClickHandler.onClick typed sealed action
```

## Android Glance sketch (manual wiring)

Pass the serialized data through one Glance `ActionCallback`:

```kotlin
private val ActionIdKey = ActionParameters.Key<String>("warp_action_id")
private val ActionParametersJsonKey =
    ActionParameters.Key<String>("warp_action_parameters")

fun ClickAction.toGlanceAction(): Action =
    actionRunCallback<WarpClickCallback>(
        actionParametersOf(
            ActionIdKey to actionId,
            ActionParametersJsonKey to encodeParameters(parameters),
        ),
    )
```

Renderer:

```kotlin
@Composable
fun RenderButton(node: WarpButton) {
    Button(
        text = node.text,
        onClick = when (val action = node.onClick) {
            is ClickAction -> action.toGlanceAction()
        },
    )
}
```

Single native callback:

```kotlin
class WarpClickCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val actionId = parameters[ActionIdKey] ?: return
        val actionParameters =
            decodeParameters(parameters[ActionParametersJsonKey])

        WarpClicksRegistry.dispatch(actionId, actionParameters)
    }
}
```

With `WarpClickHandler` registered, dispatch decodes wire → typed action — no manual
`actionIdAs` in the Glance callback.

## iOS WidgetKit sketch

Same flow:

1. Renderer reads `WarpButton.onClick`.
2. Exhaustive `when (action)` maps each `WarpAction` subtype to an `AppIntent` or URL.
3. Intent carries `actionId` and serialized parameters.
4. Native callback calls `dispatchWarpClick(actionId, parameters)`.
5. `WarpClicksRegistry` → `WarpClickHandler.onClick(action)` with typed sealed action.
6. Host updates state and reloads the widget timeline.

## Adding a new `WarpAction` subtype

Example:

```kotlin
@Serializable
@SerialName("start_activity")
data class StartActivityAction(
    val component: String,
    val parameters: WarpActionParameters = emptyMap(),
) : WarpAction
```

Every native renderer now gets a compiler error until it handles the new branch:

```kotlin
when (action) {
    is ClickAction -> nativeClick(action.actionId, action.parameters)
    is StartActivityAction -> nativeStartActivity(action.component, action.parameters)
}
```

This is the intended extension mechanism for **renderer-level** action types. Widget-specific
tap actions stay in a `@Serializable sealed class` per widget.

## Checklist

- Define widget actions as a `@Serializable sealed class` (variants + params only).
- Use `.asClickAction()` or `WarpModifier.clickable(action)` in shared UI.
- Subclass `WarpClickHandler<YourActions>(YourActions.serializer())`.
- Exhaustive `when (action)` in `onClick` — compiler checks all variants.
- Read `WarpButton.onClick` in renderers.
- Use exhaustive `when (action)` with no `else` for `WarpAction` subtypes.
- For `ClickAction`, forward `actionId` and all `parameters` to the platform.
- Use one native dispatcher callback when possible (`WarpClicksRegistry`).
- Update persisted state after handling.
- Refresh the widget after state changes.
- Reject or log unknown wire ids (decode returns null).
- Never try to serialize Kotlin lambdas.

## Relevant files

- `nodes/actions/WarpTypedAction.kt` — auto codec (`asClickAction`, `warpActionFamily`)
- `nodes/actions/WarpAction.kt` — sealed action root, `WarpActionParameters`
- `nodes/actions/ClickAction.kt` — wire `actionId` + parameters, legacy `WarpActionId`
- `nodes/modifiers/WarpModifier.kt` — `clickable(action)` overload
- `nodes/WarpButton.kt` — stores `onClick: WarpAction`
- `compose/WarpUi.kt` — public `WarpButton` API
- `commonTest/.../example/counter/` — counter fixture (sealed actions + UI) for tests
- `warp-ui/.../WarpClickHandler.kt` — typed handler + registry wiring
- `shared/.../CounterWarpWidget.kt` — full demo (CounterActions + handler)
