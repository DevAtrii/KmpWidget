package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.style.WarpHorizontalAlignment
import com.atriidev.warp_runtime.nodes.style.WarpVerticalAlignment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A horizontal layout container — Glance `Row`-shaped API.
 *
 * JSON `"type"` value: `"row"`.
 *
 * @property modifier Layout styling applied to this row.
 * @property horizontalAlignment Pack children when narrower than the row (default [WarpHorizontalAlignment.Start]).
 * @property verticalAlignment Align children across the height (default [WarpVerticalAlignment.Top]).
 * @property children Nested [WarpNode] instances inside this row.
 */
@Serializable
@SerialName("row")
data class WarpRow(
    val modifier: WarpModifier = WarpModifier(),
    val horizontalAlignment: WarpHorizontalAlignment = WarpHorizontalAlignment.Start,
    val verticalAlignment: WarpVerticalAlignment = WarpVerticalAlignment.Top,
    val children: List<WarpNode> = emptyList(),
) : WarpNode
