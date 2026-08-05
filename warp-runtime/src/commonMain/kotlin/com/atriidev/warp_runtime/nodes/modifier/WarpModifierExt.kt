package com.atriidev.warp_runtime.nodes.modifier

/**
 * Sets the [WarpPadding] on this [WarpModifier].
 *
 * Example:
 * ```
 * WarpColumn(
 *     modifier = WarpModifier().padding(WarpPadding(16, 16, 16, 16))
 * ) { ... }
 * ```
 *
 * @param paddingValues Padding to apply on all edges of the node.
 * @return A new [WarpModifier] with the given padding (modifiers are immutable data classes).
 */
fun WarpModifier.padding(
    paddingValues: WarpPadding,
): WarpModifier = this.copy(
    padding = paddingValues,
)
