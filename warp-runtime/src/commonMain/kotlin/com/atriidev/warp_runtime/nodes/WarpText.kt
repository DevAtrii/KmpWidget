package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.style.WarpTextStyle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A read-only text leaf node — Glance `Text`-shaped API.
 *
 * JSON `"type"` value: `"text"`.
 *
 * @property text The string displayed in the widget.
 * @property modifier Layout styling (padding, weight, …).
 * @property style Optional [WarpTextStyle] (not a modifier).
 * @property maxLines Max lines (`Int.MAX_VALUE` = unlimited).
 */
@Serializable
@SerialName("text")
data class WarpText(
    val text: String,
    val modifier: WarpModifier = WarpModifier(),
    val style: WarpTextStyle? = null,
    val maxLines: Int = Int.MAX_VALUE,
) : WarpNode
