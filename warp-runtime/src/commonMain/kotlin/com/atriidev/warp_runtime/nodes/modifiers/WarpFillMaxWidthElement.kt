package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Expand width to parent. JSON `"type": "fillMaxWidth"`. */
@Serializable
@SerialName("fillMaxWidth")
data object WarpFillMaxWidthElement : WarpModifierElement
