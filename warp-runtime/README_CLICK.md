# Native click dispatch

Guide for future Android Glance and iOS WidgetKit renderer developers.

WARP does not execute click handlers in common code. It serializes an action, then the
native renderer forwards that action to the platform callback.

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

Native callback:

```kotlin
onClick(
    actionId = action.actionId,
    parameters = action.parameters,
)
```

No registry, shared handler, or `WarpActionContext` exists. Native code owns behavior.

## Define an action ID once

```kotlin
enum class CounterActions(
    override val actionId: String,
) : WarpActionId {
    Increment("increment"),
    Decrement("decrement"),
}
```

Use the key in shared UI:

```kotlin
WarpButton("-", onClick = CounterActions.Decrement.asClickAction())
WarpButton("+", onClick = CounterActions.Increment.asClickAction())
```

For dynamic metadata:

```kotlin
enum class ItemActions(override val actionId: String) : WarpActionId {
    Open("open_item"),
}

WarpButton(
    text = "Open",
    onClick = actionClick(ItemActions.Open, "itemId" to "42"),
)
```

`parameters` remains `Map<String, String>` so it round-trips through JSON and native widget APIs.

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
2. `when (action.actionIdAs<CounterActions>())` covers every ID in one widget's enum.

## Android Glance sketch

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

        CounterWidgetHost.onClick(
            context = context,
            glanceId = glanceId,
            actionId = actionId,
            parameters = actionParameters,
        )
    }
}
```

Native host decodes the string wire value into its widget enum, then handles it exhaustively:

```kotlin
object CounterWidgetHost {
    suspend fun onClick(
        context: Context,
        glanceId: GlanceId,
        actionId: String,
        parameters: Map<String, String>,
    ) {
        val clickAction = ClickAction(actionId, parameters)
        when (clickAction.actionIdAs<CounterActions>()) {
            CounterActions.Increment -> updateCount(context, glanceId) { it + 1 }
            CounterActions.Decrement -> updateCount(context, glanceId) { it - 1 }
        }
    }
}
```

After changing state, refresh the widget with Glance APIs.

## iOS WidgetKit sketch

Same flow:

1. Renderer reads `WarpButton.onClick`.
2. Exhaustive `when (action)` maps each `WarpAction` subtype to an `AppIntent` or URL.
3. Intent carries `actionId` and serialized parameters.
4. Native callback invokes the host's `onClick(actionId, parameters)`.
5. Host updates state and reloads the widget timeline.

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

This is the intended extension mechanism.

## Checklist

- Read `WarpButton.onClick`.
- Use exhaustive `when (action)` with no `else`.
- For `ClickAction`, forward `actionId` and all `parameters`.
- Use one native dispatcher callback when possible.
- Decode action IDs with `actionIdAs<WidgetActions>()`.
- Use exhaustive `when` over the widget enum.
- Update persisted state.
- Refresh the widget after state changes.
- Reject or log unknown IDs.
- Never try to serialize Kotlin lambdas.

## Relevant files

- `nodes/actions/WarpAction.kt` — sealed action root
- `nodes/actions/ClickAction.kt` — `actionId`, parameters, factories
- `nodes/WarpButton.kt` — stores `onClick: WarpAction`
- `compose/WarpUi.kt` — public `WarpButton` API
- `example/counter/CounterActions.kt` — action IDs defined once
- `example/counter/CounterWidget.kt` — shared UI example
