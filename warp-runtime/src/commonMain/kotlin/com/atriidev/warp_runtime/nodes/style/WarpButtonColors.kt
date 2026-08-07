package com.atriidev.warp_runtime.nodes.style

import com.atriidev.warp_runtime.nodes.modifiers.WarpColor
import kotlinx.serialization.Serializable

/**
 * Button chrome colors — mirrors Glance `ButtonColors`.
 *
 * Null fields → leave platform default.
 */
@Serializable
data class WarpButtonColors(
    val backgroundColor: WarpColor? = null,
    val contentColor: WarpColor? = null,
) {
    companion object {
        fun of(
            backgroundColor: String? = null,
            contentColor: String? = null,
        ): WarpButtonColors = WarpButtonColors(
            backgroundColor = backgroundColor?.let(::WarpColor),
            contentColor = contentColor?.let(::WarpColor),
        )
    }
}
