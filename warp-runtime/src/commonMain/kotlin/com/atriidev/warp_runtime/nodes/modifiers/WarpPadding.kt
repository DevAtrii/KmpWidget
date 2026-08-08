package com.atriidev.warp_runtime.nodes.modifiers

import com.atriidev.warp_runtime.unit.Dp
import com.atriidev.warp_runtime.unit.dp
import kotlinx.serialization.Serializable

/**
 * Padding in density-independent pixels applied to a node's edges.
 *
 * @property start Padding on the start edge (left in LTR layouts).
 * @property end Padding on the end edge (right in LTR layouts).
 * @property top Padding on the top edge.
 * @property bottom Padding on the bottom edge.
 */
@Serializable
data class WarpPadding(
    val start: Dp = 0.dp,
    val end: Dp = 0.dp,
    val top: Dp = 0.dp,
    val bottom: Dp = 0.dp,
) {
    companion object {
        val Zero: WarpPadding = WarpPadding(0.dp, 0.dp, 0.dp, 0.dp)
    }

    operator fun plus(other: WarpPadding): WarpPadding = WarpPadding(
        start = (start.value + other.start.value).dp,
        end = (end.value + other.end.value).dp,
        top = (top.value + other.top.value).dp,
        bottom = (bottom.value + other.bottom.value).dp,
    )
}
