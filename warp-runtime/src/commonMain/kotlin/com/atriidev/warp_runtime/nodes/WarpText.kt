package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.modifier.WarpModifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("text")
data class WarpText(
    val text: String,
    val modifier: WarpModifier = WarpModifier(),
) : WarpNode
