package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.style.WarpHorizontalAlignment
import com.atriidev.warp_runtime.nodes.style.WarpVerticalAlignment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A horizontal scrollable/lazy layout container.
 *
 * On iOS WidgetKit, this renders as a `LazyHStack` (just like [WarpRow]). On Android Glance app widgets,
 * this renders as a horizontal container.
 *
 * JSON `"type"` value: `"lazy_row"`.
 *
 * @property modifier Layout styling applied to this lazy row.
 * @property horizontalAlignment Pack children when narrower than the row (default [WarpHorizontalAlignment.Start]).
 * @property verticalAlignment Align children across the height (default [WarpVerticalAlignment.Top]).
 * @property children Nested [WarpNode] instances inside this lazy row.
 */
@Serializable
@SerialName("lazy_row")
data class WarpLazyRow(
    val modifier: WarpModifier = WarpModifier(),
    val horizontalAlignment: WarpHorizontalAlignment = WarpHorizontalAlignment.Start,
    val verticalAlignment: WarpVerticalAlignment = WarpVerticalAlignment.Top,
    val children: List<WarpNode> = emptyList(),
) : WarpNode
