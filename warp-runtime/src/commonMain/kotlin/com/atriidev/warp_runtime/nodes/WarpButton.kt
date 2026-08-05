package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.modifier.WarpModifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A clickable button leaf node.
 *
 * JSON `"type"` value: `"button"`.
 *
 * [actionId] is a stable string stored in JSON — not a Kotlin lambda. Platform renderers
 * map it to native click handlers (for example Glance `ActionCallback` on Android).
 *
 * @property text Label displayed on the button.
 * @property actionId Serializable identifier for the button's click action.
 * @property modifier Layout styling applied to this button (padding, etc.).
 */
@Serializable
@SerialName("button")
data class WarpButton(
    val text: String,
    val actionId: String,
    val modifier: WarpModifier = WarpModifier(),
) : WarpNode
