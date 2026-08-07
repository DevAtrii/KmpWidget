package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Row/column weight (Glance `defaultWeight` / Swift layout priority).
 *
 * JSON `"type": "weight"`.
 */
@Serializable
@SerialName("weight")
data class WarpWeightElement(
    val weight: Float = 1f,
) : WarpModifierElement
