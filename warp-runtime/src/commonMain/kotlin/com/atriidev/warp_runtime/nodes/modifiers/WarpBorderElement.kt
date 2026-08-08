package com.atriidev.warp_runtime.nodes.modifiers

import com.atriidev.warp_runtime.unit.Dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Stroke border. JSON `"type": "border"`. */
@Serializable
@SerialName("border")
data class WarpBorderElement(
    val width: Dp,
    val color: WarpColor,
) : WarpModifierElement
