package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.modifiers.WarpColor
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Horizontal rule / separator (Compose Material `Divider`-shaped; not a Glance primitive).
 *
 * Rendered as a thin filled bar. JSON `"type"` value: `"divider"`.
 *
 * @property modifier Layout styling (typically [WarpModifier.fillMaxWidth]).
 * @property thickness Stroke thickness in dp (default `1`).
 * @property color Optional fill color; platform default when null.
 */
@Serializable
@SerialName("divider")
data class WarpDivider(
    val modifier: WarpModifier = WarpModifier(),
    val thickness: Int = 1,
    val color: WarpColor? = null,
) : WarpNode
