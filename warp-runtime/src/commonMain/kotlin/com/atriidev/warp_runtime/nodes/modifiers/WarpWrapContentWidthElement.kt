package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Wrap content width. JSON `"type": "wrapContentWidth"`. */
@Serializable
@SerialName("wrapContentWidth")
data object WarpWrapContentWidthElement : WarpModifierElement
