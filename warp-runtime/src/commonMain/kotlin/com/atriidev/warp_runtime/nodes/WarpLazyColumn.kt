package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.style.WarpHorizontalAlignment
import com.atriidev.warp_runtime.nodes.style.WarpVerticalAlignment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A vertical scrollable/lazy layout container.
 *
 * On iOS WidgetKit, this renders as a `LazyVStack` (just like [WarpColumn]). On Android Glance app widgets,
 * this renders as a scrollable Glance `LazyColumn`.
 *
 * JSON `"type"` value: `"lazy_column"`.
 *
 * @property modifier Layout styling applied to this lazy column.
 * @property verticalAlignment Pack children when shorter than the column (default [WarpVerticalAlignment.Top]).
 * @property horizontalAlignment Align children across the width (default [WarpHorizontalAlignment.Start]).
 * @property children Nested [WarpNode] instances inside this lazy column.
 */
@Serializable
@SerialName("lazy_column")
data class WarpLazyColumn(
    val modifier: WarpModifier = WarpModifier(),
    val verticalAlignment: WarpVerticalAlignment = WarpVerticalAlignment.Top,
    val horizontalAlignment: WarpHorizontalAlignment = WarpHorizontalAlignment.Start,
    val children: List<WarpNode> = emptyList(),
) : WarpNode
