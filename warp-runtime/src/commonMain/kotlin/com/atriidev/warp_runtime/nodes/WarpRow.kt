package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.modifier.WarpModifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("row")
data class WarpRow(
    val modifier: WarpModifier = WarpModifier(),
    val children: List<WarpNode> = emptyList(),
) : WarpNode
