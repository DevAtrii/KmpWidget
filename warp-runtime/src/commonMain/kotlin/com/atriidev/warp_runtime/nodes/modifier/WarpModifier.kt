package com.atriidev.warp_runtime.nodes.modifier

import kotlinx.serialization.Serializable

@Serializable
data class WarpPadding(
    val start: Int,
    val end: Int,
    val top: Int,
    val bottom: Int,
)


@Serializable
data class WarpModifier(
    internal val padding: WarpPadding = WarpPadding(0,0,0,0,),
)






















