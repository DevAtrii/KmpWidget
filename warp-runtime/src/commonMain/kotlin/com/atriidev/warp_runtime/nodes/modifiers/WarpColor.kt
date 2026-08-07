package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.Serializable

/**
 * Serializable ARGB/RGB color for WARP modifiers.
 *
 * Accepts `#RRGGBB` or `#AARRGGBB` (leading `#` optional).
 */
@Serializable
data class WarpColor(val hex: String) {
    /** Packed ARGB int (`0xAARRGGBB`). Alpha defaults to `FF` for 6-digit RGB. */
    fun toArgbInt(): Int {
        val raw = hex.removePrefix("#")
        val value = when (raw.length) {
            6 -> ("FF$raw").toLong(16)
            8 -> raw.toLong(16)
            else -> error("WarpColor expects #RRGGBB or #AARRGGBB, got \"$hex\"")
        }
        return value.toInt()
    }
}
