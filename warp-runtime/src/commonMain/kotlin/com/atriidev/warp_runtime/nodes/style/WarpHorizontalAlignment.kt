package com.atriidev.warp_runtime.nodes.style

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Horizontal child alignment — Glance `Alignment.Horizontal`.
 *
 * Default for Row/Column: [Start].
 */
@Serializable
enum class WarpHorizontalAlignment {
    @SerialName("start")
    Start,

    @SerialName("center")
    Center,

    @SerialName("end")
    End,
}
