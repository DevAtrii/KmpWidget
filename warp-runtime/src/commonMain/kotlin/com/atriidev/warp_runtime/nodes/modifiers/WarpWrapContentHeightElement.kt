package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Wrap content height. JSON `"type": "wrapContentHeight"`. */
@Serializable
@SerialName("wrapContentHeight")
data object WarpWrapContentHeightElement : WarpModifierElement
