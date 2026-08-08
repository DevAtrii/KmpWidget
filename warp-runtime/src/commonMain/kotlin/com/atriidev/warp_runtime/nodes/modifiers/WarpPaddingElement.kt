package com.atriidev.warp_runtime.nodes.modifiers

import com.atriidev.warp_runtime.unit.Dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Padding link in a [WarpModifier] chain. JSON `"type": "padding"`. */
@Serializable
@SerialName("padding")
data class WarpPaddingElement(
    val start: Dp,
    val end: Dp,
    val top: Dp,
    val bottom: Dp,
) : WarpModifierElement {
    constructor(values: WarpPadding) : this(
        start = values.start,
        end = values.end,
        top = values.top,
        bottom = values.bottom,
    )
}
