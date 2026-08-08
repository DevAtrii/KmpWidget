package com.atriidev.warp_runtime.nodes.modifiers

import com.atriidev.warp_runtime.unit.Dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Corner radius in dp. JSON `"type": "cornerRadius"`. */
@Serializable
@SerialName("cornerRadius")
data class WarpCornerRadiusElement(
    val radius: Dp,
) : WarpModifierElement
