package com.atriidev.warp_runtime.nodes.style

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Vertical child alignment — Glance `Alignment.Vertical`.
 *
 * Default for Row/Column: [Top].
 */
@Serializable
enum class WarpVerticalAlignment {
    @SerialName("top")
    Top,

    @SerialName("center")
    Center,

    @SerialName("bottom")
    Bottom,
}
