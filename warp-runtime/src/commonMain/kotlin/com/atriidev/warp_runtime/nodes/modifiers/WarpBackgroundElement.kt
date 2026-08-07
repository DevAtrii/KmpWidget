package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Solid background color. JSON `"type": "background"`. */
@Serializable
@SerialName("background")
data class WarpBackgroundElement(
    val color: WarpColor,
) : WarpModifierElement
