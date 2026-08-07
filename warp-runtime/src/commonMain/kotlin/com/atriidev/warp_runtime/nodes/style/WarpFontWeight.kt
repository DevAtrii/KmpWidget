package com.atriidev.warp_runtime.nodes.style

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Font weight for [WarpTextStyle] (Compose/Glance-aligned). */
@Serializable
enum class WarpFontWeight {
    @SerialName("normal")
    Normal,

    @SerialName("medium")
    Medium,

    @SerialName("semibold")
    Semibold,

    @SerialName("bold")
    Bold,
}
