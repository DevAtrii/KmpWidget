package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Fixed width and height in dp. JSON `"type": "size"`. */
@Serializable
@SerialName("size")
data class WarpSizeElement(
    val width: Int,
    val height: Int,
) : WarpModifierElement
