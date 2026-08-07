package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Expand width and height to parent. JSON `"type": "fillMaxSize"`. */
@Serializable
@SerialName("fillMaxSize")
data object WarpFillMaxSizeElement : WarpModifierElement
