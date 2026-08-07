package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Wrap content width and height. JSON `"type": "wrapContentSize"`. */
@Serializable
@SerialName("wrapContentSize")
data object WarpWrapContentSizeElement : WarpModifierElement
