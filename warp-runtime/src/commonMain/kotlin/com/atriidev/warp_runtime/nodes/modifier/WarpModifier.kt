/**
 * Serializable styling modifiers attached to [com.atriidev.warp_runtime.nodes.WarpNode] instances.
 *
 * Modifiers describe **what** to apply (padding values, etc.), not how a specific platform draws them.
 * Platform renderers interpret these values when converting a [WarpNode] tree to native widget UI.
 */
package com.atriidev.warp_runtime.nodes.modifier

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
)

/**
 * Styling and layout modifiers for a WARP node.
 *
 * Attach via composable parameters (`WarpText(..., modifier = ...)`) or extension functions
 * like [padding].
 *
 * @property padding Inner padding applied around the node's content.
 */
@Serializable
data class WarpModifier(
    val padding: WarpPadding = WarpPadding(0, 0, 0, 0),
)
