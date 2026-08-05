package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.modifier.WarpModifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A read-only text leaf node.
 *
 * JSON `"type"` value: `"text"`.
 *
 * @property text The string displayed in the widget.
 * @property modifier Layout styling applied to this text (padding, etc.).
 */
@Serializable
@SerialName("text")
data class WarpText(
    val text: String,
    val modifier: WarpModifier = WarpModifier(),
) : WarpNode
