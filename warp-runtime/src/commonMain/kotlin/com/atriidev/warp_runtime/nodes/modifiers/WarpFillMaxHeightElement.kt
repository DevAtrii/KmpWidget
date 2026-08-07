package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Expand height to parent. JSON `"type": "fillMaxHeight"`. */
@Serializable
@SerialName("fillMaxHeight")
data object WarpFillMaxHeightElement : WarpModifierElement
