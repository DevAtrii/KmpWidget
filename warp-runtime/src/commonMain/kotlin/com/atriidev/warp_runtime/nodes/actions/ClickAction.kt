package com.atriidev.warp_runtime.nodes.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A tap/click action identified by [id], with optional [parameters].
 *
 * This is the foundation action type for WARP. Most widget buttons use [ClickAction] today.
 * Future action types ([StartActivityAction], deep links, etc.) also implement [WarpAction]
 * but carry different payloads.
 *
 * JSON shape:
 * ```json
 * {
 *   "type": "click",
 *   "id": "increment",
 *   "parameters": { "step": "1" }
 * }
 * ```
 *
 * @property id Stable action identifier. Platform registries map this to a handler class
 *   (for example Glance `ActionCallback` on Android).
 * @property parameters Optional key/value metadata passed to the handler. Values are strings
 *   so the action stays JSON-safe across platforms.
 */
@Serializable
@SerialName("click")
data class ClickAction(
    val id: String,
    val parameters: WarpActionParameters = emptyMap(),
) : WarpAction

/**
 * Creates a [ClickAction] with only an [id].
 *
 * ```
 * WarpButton(text = "+", onClick = actionClick("increment"))
 * ```
 */
fun actionClick(id: String): ClickAction = ClickAction(id = id)

/**
 * Creates a [ClickAction] with an [id] and string [parameters].
 *
 * ```
 * WarpButton(
 *     text = "Open",
 *     onClick = actionClick("open_item", "itemId" to "42"),
 * )
 * ```
 */
fun actionClick(id: String, vararg parameters: Pair<String, String>): ClickAction =
    ClickAction(id = id, parameters = parameters.toMap())

/**
 * Creates a [ClickAction] from a pre-built parameter map.
 */
fun actionClick(id: String, parameters: WarpActionParameters): ClickAction =
    ClickAction(id = id, parameters = parameters)

/**
 * Marker for typed action ids defined in common code.
 *
 * ```
 * object CounterActions {
 *     object Increment : WarpActionKey { override val id = "increment" }
 * }
 *
 * WarpButton("+", onClick = CounterActions.Increment.asClickAction())
 * ```
 */
interface WarpActionKey {
    val id: String
}

/** Converts this key to a [ClickAction] with no parameters. */
fun WarpActionKey.asClickAction(): ClickAction = actionClick(id)

/** Converts this key to a [ClickAction] with [parameters]. */
fun WarpActionKey.asClickAction(vararg parameters: Pair<String, String>): ClickAction =
    actionClick(id, *parameters)

/**
 * Returns the click [id] when this [WarpAction] is a [ClickAction], otherwise null.
 *
 * Useful for platform renderers that only need the handler id during migration.
 */
fun WarpAction.clickIdOrNull(): String? = (this as? ClickAction)?.id

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
