package com.atriidev.warp_runtime.nodes.style

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Progress indicator shape — Glance `CircularProgressIndicator` / `LinearProgressIndicator`.
 */
@Serializable
enum class WarpProgressIndicatorStyle {
    @SerialName("circular")
    Circular,

    @SerialName("linear")
    Linear,
}
