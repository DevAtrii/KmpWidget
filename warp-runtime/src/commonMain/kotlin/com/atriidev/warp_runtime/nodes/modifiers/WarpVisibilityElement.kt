package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Visibility link. JSON `"type": "visibility"`. */
@Serializable
@SerialName("visibility")
data class WarpVisibilityElement(
    val visibility: WarpVisibility,
) : WarpModifierElement
