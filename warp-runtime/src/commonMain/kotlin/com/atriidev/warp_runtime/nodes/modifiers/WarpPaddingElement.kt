package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Padding link in a [WarpModifier] chain. JSON `"type": "padding"`. */
@Serializable
@SerialName("padding")
data class WarpPaddingElement(
    val start: Int,
    val end: Int,
    val top: Int,
    val bottom: Int,
) : WarpModifierElement {
    constructor(values: WarpPadding) : this(
        start = values.start,
        end = values.end,
        top = values.top,
        bottom = values.bottom,
    )
}
