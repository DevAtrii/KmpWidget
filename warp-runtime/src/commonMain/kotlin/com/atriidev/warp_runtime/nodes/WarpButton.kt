package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.modifier.WarpModifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("button")
data class WarpButton(
    val text: String,
    val actionId: String,
    val modifier: WarpModifier = WarpModifier(),
) : WarpNode
