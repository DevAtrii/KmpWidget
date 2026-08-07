package com.atriidev.warp_runtime.nodes.style

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Horizontal text alignment for [WarpTextStyle]. */
@Serializable
enum class WarpTextAlign {
    @SerialName("start")
    Start,

    @SerialName("center")
    Center,

    @SerialName("end")
    End,
}
