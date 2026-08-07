package com.atriidev.warp_runtime.nodes.style

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * How an image fills its bounds — Glance `ContentScale`-shaped.
 */
@Serializable
enum class WarpContentScale {
    @SerialName("fit")
    Fit,

    @SerialName("crop")
    Crop,

    @SerialName("fillBounds")
    FillBounds,
}
