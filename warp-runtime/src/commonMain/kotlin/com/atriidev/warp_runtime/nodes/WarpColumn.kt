package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.style.WarpHorizontalAlignment
import com.atriidev.warp_runtime.nodes.style.WarpVerticalAlignment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A vertical layout container — Glance `Column`-shaped API.
 *
 * JSON `"type"` value: `"column"`.
 *
 * @property modifier Layout styling applied to this column.
 * @property verticalAlignment Pack children when shorter than the column (default [WarpVerticalAlignment.Top]).
 * @property horizontalAlignment Align children across the width (default [WarpHorizontalAlignment.Start]).
 * @property children Nested [WarpNode] instances inside this column.
 */
@Serializable
@SerialName("column")
data class WarpColumn(
    val modifier: WarpModifier = WarpModifier(),
    val verticalAlignment: WarpVerticalAlignment = WarpVerticalAlignment.Top,
    val horizontalAlignment: WarpHorizontalAlignment = WarpHorizontalAlignment.Start,
    val children: List<WarpNode> = emptyList(),
) : WarpNode
