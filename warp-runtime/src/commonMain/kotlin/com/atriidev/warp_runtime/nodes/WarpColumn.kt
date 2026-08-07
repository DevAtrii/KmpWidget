package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A vertical layout container — children are stacked top to bottom.
 *
 * JSON `"type"` value: `"column"`.
 *
 * @property modifier Layout styling applied to this column (padding, etc.).
 * @property children Nested [WarpNode] instances inside this column.
 */
@Serializable
@SerialName("column")
data class WarpColumn(
    val modifier: WarpModifier = WarpModifier(),
    val children: List<WarpNode> = emptyList(),
) : WarpNode
