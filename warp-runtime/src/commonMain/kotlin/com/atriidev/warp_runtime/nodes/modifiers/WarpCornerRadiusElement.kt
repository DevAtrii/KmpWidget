package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Corner radius in dp. JSON `"type": "cornerRadius"`. */
@Serializable
@SerialName("cornerRadius")
data class WarpCornerRadiusElement(
    val radius: Int,
) : WarpModifierElement
