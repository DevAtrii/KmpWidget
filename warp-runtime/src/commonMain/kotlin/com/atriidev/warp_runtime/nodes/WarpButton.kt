package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.actions.WarpAction
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.style.WarpButtonColors
import com.atriidev.warp_runtime.nodes.style.WarpTextStyle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A clickable button leaf node — Glance `Button`-shaped API.
 *
 * JSON `"type"` value: `"button"`.
 *
 * [onClick] is a serializable [WarpAction] — not a Kotlin lambda. Platform renderers
 * forward [com.atriidev.warp_runtime.nodes.actions.ClickAction.actionId] and parameters
 * to native handlers (Glance `ActionCallback` / WidgetKit intents).
 *
 * When [com.atriidev.warp_runtime.nodes.modifiers.WarpModifier.clickable] is also set,
 * renderers prefer the **modifier** action over [onClick].
 *
 * @property text Label displayed on the button.
 * @property onClick Action executed when the user taps the button.
 * @property modifier Layout/behavior styling (padding, size, clickable, …).
 * @property enabled When false, taps are ignored.
 * @property style Optional label [WarpTextStyle] (not a modifier).
 * @property colors Optional [WarpButtonColors] chrome.
 * @property maxLines Max lines for the label (`Int.MAX_VALUE` = unlimited).
 */
@Serializable
@SerialName("button")
data class WarpButton(
    val text: String,
    val onClick: WarpAction,
    val modifier: WarpModifier = WarpModifier(),
    val enabled: Boolean = true,
    val style: WarpTextStyle? = null,
    val colors: WarpButtonColors? = null,
    val maxLines: Int = Int.MAX_VALUE,
) : WarpNode
