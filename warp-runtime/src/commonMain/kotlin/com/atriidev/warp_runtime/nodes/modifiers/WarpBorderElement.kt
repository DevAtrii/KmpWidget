package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Stroke border. JSON `"type": "border"`. */
@Serializable
@SerialName("border")
data class WarpBorderElement(
    val width: Int,
    val color: WarpColor,
) : WarpModifierElement
