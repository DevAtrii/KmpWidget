package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.actions.WarpAction
import com.atriidev.warp_runtime.nodes.modifier.WarpModifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A clickable button leaf node.
 *
 * JSON `"type"` value: `"button"`.
 *
 * [onClick] is a serializable [WarpAction] — not a Kotlin lambda. Platform renderers
 * map [com.atriidev.warp_runtime.nodes.actions.ClickAction.id] to native handlers
 * (for example Glance `ActionCallback` on Android).
 *
 * @property text Label displayed on the button.
 * @property onClick Action executed when the user taps the button.
 * @property modifier Layout styling applied to this button (padding, etc.).
 */
@Serializable
@SerialName("button")
data class WarpButton(
    val text: String,
    val onClick: WarpAction,
    val modifier: WarpModifier = WarpModifier(),
) : WarpNode
