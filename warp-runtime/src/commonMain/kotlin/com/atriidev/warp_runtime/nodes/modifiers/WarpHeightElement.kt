package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Fixed height in dp. JSON `"type": "height"`. */
@Serializable
@SerialName("height")
data class WarpHeightElement(
    val height: Int,
) : WarpModifierElement
