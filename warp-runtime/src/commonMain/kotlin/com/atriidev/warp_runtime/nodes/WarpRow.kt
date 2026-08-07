package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A horizontal layout container — children are placed left to right.
 *
 * JSON `"type"` value: `"row"`.
 *
 * @property modifier Layout styling applied to this row (padding, etc.).
 * @property children Nested [WarpNode] instances inside this row.
 */
@Serializable
@SerialName("row")
data class WarpRow(
    val modifier: WarpModifier = WarpModifier(),
    val children: List<WarpNode> = emptyList(),
) : WarpNode
