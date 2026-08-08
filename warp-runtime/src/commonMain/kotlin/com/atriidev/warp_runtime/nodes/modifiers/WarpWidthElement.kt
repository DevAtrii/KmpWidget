package com.atriidev.warp_runtime.nodes.modifiers

import com.atriidev.warp_runtime.unit.Dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Fixed width in dp. JSON `"type": "width"`. */
@Serializable
@SerialName("width")
data class WarpWidthElement(
    val width: Dp,
) : WarpModifierElement
