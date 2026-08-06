package com.atriidev.warp_runtime.nodes.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * A tap/click action identified by [actionId], with optional [parameters].
 *
 * This is the foundation action type for WARP. Most widget buttons use [ClickAction] today.
 * Future action types ([StartActivityAction], deep links, etc.) also implement [WarpAction]
 * but carry different payloads.
 *
 * JSON shape:
 * ```json
 * {
 *   "type": "click",
 *   "actionId": "increment",
 *   "parameters": { "step": "1" }
 * }
 * ```
 *
 * @property actionId Stable identifier passed to the native click callback.
 * @property parameters Optional key/value metadata passed to the handler. Values are strings
 *   so the action stays JSON-safe across platforms.
 */
@Serializable
@SerialName("click")
data class ClickAction(
    val actionId: String,
    val parameters: WarpActionParameters = emptyMap(),
) : WarpAction

/**
 * Creates a [ClickAction] from a typed [actionId].
 *
 * ```
 * WarpButton(text = "+", onClick = actionClick(CounterActions.Increment))
 * ```
 */
fun actionClick(actionId: WarpActionId): ClickAction =
    ClickAction(actionId = actionId.actionId)

/**
 * Creates a [ClickAction] with a typed [actionId] and string [parameters].
 *
 * ```
 * WarpButton(
 *     text = "Open",
 *     onClick = actionClick(ItemActions.Open, "itemId" to "42"),
 * )
 * ```
 */
fun actionClick(
    actionId: WarpActionId,
    vararg parameters: Pair<String, String>,
): ClickAction = ClickAction(
    actionId = actionId.actionId,
    parameters = parameters.toMap(),
)

/**
 * Creates a [ClickAction] from a pre-built parameter map.
 */
fun actionClick(actionId: WarpActionId, parameters: WarpActionParameters): ClickAction =
    ClickAction(actionId = actionId.actionId, parameters = parameters)

/**
 * Stable string representation implemented by a widget-specific enum.
 *
 * ```
 * enum class CounterActions(override val actionId: String) : WarpActionId {
 *     Increment("increment"),
 *     Decrement("decrement"),
 * }
 *
 * WarpButton("+", onClick = CounterActions.Increment.asClickAction())
 * ```
 *
 * Native renderers receive the resulting [ClickAction] and forward [ClickAction.actionId]
 * plus [ClickAction.parameters] to their platform callback.
 */
interface WarpActionId {
    val actionId: String
}

/** Converts this key to a [ClickAction] with no parameters. */
fun WarpActionId.asClickAction(): ClickAction = actionClick(this)

/** Converts this key to a [ClickAction] with [parameters]. */
fun WarpActionId.asClickAction(vararg parameters: Pair<String, String>): ClickAction =
    actionClick(this, *parameters)

/**
 * Decodes a wire [actionId] string into widget-specific enum [A].
 *
 * @throws IllegalArgumentException when [actionId] is unknown to [A].
 */
fun <A> decodeActionId(actionId: String, idClass: KClass<A>): A
    where A : Enum<A>, A : WarpActionId {
    @Suppress("UNCHECKED_CAST")
    val constants = idClass.java.enumConstants as Array<A>
    return constants.firstOrNull { it.actionId == actionId }
        ?: throw IllegalArgumentException(
            "Unknown ${idClass.simpleName} action id \"$actionId\"",
        )
}

/**
 * Decodes this wire [actionId] into widget-specific enum [A].
 *
 * The returned enum enables an exhaustive native `when`:
 * ```
 * when (action.actionIdAs<CounterActions>()) {
 *     CounterActions.Increment -> increment()
 *     CounterActions.Decrement -> decrement()
 * }
 * ```
 *
 * @throws IllegalArgumentException when [actionId] is unknown to [A].
 */
inline fun <reified A> ClickAction.actionIdAs(): A
    where A : Enum<A>, A : WarpActionId =
    decodeActionId(actionId, A::class)

/**
 * Returns the click [ClickAction.actionId] when this is a [ClickAction], otherwise null.
 *
 * Useful for platform renderers that only need the handler id during migration.
 */
fun WarpAction.clickActionIdOrNull(): String? = (this as? ClickAction)?.actionId

/**
 * Placeholder for a future [WarpAction] type — not wired into renderers yet.
 *
 * When implemented, add `@Serializable`, a factory `actionStartActivity(...)`, and platform
 * mapping to Glance `actionStartActivity` / iOS equivalent.
 */
// @Serializable
// @SerialName("start_activity")
// data class StartActivityAction(
//     val component: String,
//     val parameters: WarpActionParameters = emptyMap(),
// ) : WarpAction
