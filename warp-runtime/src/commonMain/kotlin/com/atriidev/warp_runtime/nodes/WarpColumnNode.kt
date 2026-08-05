package com.atriidev.warp_runtime.nodes

import androidx.compose.runtime.Composable
import com.atriidev.warp_runtime.nodes.modifier.WarpModifier
import kotlinx.serialization.Serializable


@Serializable
data class WarpColumnNode(
    val modifier: WarpModifier,
    val children: List<WarpNode>,
) : WarpNode

