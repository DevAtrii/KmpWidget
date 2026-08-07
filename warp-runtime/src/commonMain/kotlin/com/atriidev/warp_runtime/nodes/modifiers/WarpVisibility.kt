package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Visibility of a node in the native layout. */
@Serializable
enum class WarpVisibility {
    @SerialName("visible")
    Visible,

    @SerialName("invisible")
    Invisible,

    @SerialName("gone")
    Gone,
}
