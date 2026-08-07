package com.atriidev.warp_runtime.nodes.modifiers

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
    val start: Int,
    val end: Int,
    val top: Int,
    val bottom: Int,
) {
    companion object {
        val Zero: WarpPadding = WarpPadding(0, 0, 0, 0)
    }

    operator fun plus(other: WarpPadding): WarpPadding = WarpPadding(
        start = start + other.start,
        end = end + other.end,
        top = top + other.top,
        bottom = bottom + other.bottom,
    )
}
