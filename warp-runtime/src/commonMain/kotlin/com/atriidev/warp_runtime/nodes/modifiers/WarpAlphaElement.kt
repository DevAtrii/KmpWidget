package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Opacity `0f..1f`. JSON `"type": "alpha"`. */
@Serializable
@SerialName("alpha")
data class WarpAlphaElement(
    val alpha: Float,
) : WarpModifierElement
