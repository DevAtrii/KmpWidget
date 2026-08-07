package com.atriidev.warp_runtime.nodes.style

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 2D content alignment — Glance `Alignment` for [com.atriidev.warp_runtime.nodes.WarpBox].
 *
 * Default: [TopStart].
 */
@Serializable
enum class WarpContentAlignment {
    @SerialName("topStart")
    TopStart,

    @SerialName("topCenter")
    TopCenter,

    @SerialName("topEnd")
    TopEnd,

    @SerialName("centerStart")
    CenterStart,

    @SerialName("center")
    Center,

    @SerialName("centerEnd")
    CenterEnd,

    @SerialName("bottomStart")
    BottomStart,

    @SerialName("bottomCenter")
    BottomCenter,

    @SerialName("bottomEnd")
    BottomEnd,
}
