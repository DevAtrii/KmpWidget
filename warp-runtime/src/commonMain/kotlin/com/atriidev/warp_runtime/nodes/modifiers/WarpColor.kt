package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.Serializable

/**
 * Serializable ARGB/RGB color for WARP modifiers.
 *
 * Accepts `#RRGGBB` or `#AARRGGBB` (leading `#` optional).
 */
@Serializable
data class WarpColor(val hex: String) {

    /**
     * Creates color from packed ARGB int (`0xAARRGGBB`).
     */
    constructor(argb: Long) : this(
        argb.toULong().toString(16).uppercase().padStart(8, '0')
    )

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


    companion object {
        // Basic
        val Transparent = WarpColor(0x00000000)
        val Black = WarpColor(0xFF000000)
        val White = WarpColor(0xFFFFFFFF)

        val Red = WarpColor(0xFFFF0000)
        val Green = WarpColor(0xFF00FF00)
        val Blue = WarpColor(0xFF0000FF)

        val Yellow = WarpColor(0xFFFFFF00)
        val Cyan = WarpColor(0xFF00FFFF)
        val Magenta = WarpColor(0xFFFF00FF)

        val Gray = WarpColor(0xFF808080)
        val LightGray = WarpColor(0xFFD3D3D3)
        val DarkGray = WarpColor(0xFF404040)

        // Tailwind Blue
        val Blue50 = WarpColor(0xFFEFF6FF)
        val Blue100 = WarpColor(0xFFDBEAFE)
        val Blue200 = WarpColor(0xFFBFDBFE)
        val Blue300 = WarpColor(0xFF93C5FD)
        val Blue400 = WarpColor(0xFF60A5FA)
        val Blue500 = WarpColor(0xFF3B82F6)
        val Blue600 = WarpColor(0xFF2563EB)
        val Blue700 = WarpColor(0xFF1D4ED8)
        val Blue800 = WarpColor(0xFF1E40AF)
        val Blue900 = WarpColor(0xFF1E3A8A)
        val Blue950 = WarpColor(0xFF172554)

        // Tailwind Red
        val Red50 = WarpColor(0xFFFEF2F2)
        val Red100 = WarpColor(0xFFFEE2E2)
        val Red200 = WarpColor(0xFFFECACA)
        val Red300 = WarpColor(0xFFFCA5A5)
        val Red400 = WarpColor(0xFFF87171)
        val Red500 = WarpColor(0xFFEF4444)
        val Red600 = WarpColor(0xFFDC2626)
        val Red700 = WarpColor(0xFFB91C1C)
        val Red800 = WarpColor(0xFF991B1B)
        val Red900 = WarpColor(0xFF7F1D1D)
        val Red950 = WarpColor(0xFF450A0A)

        // Tailwind Green
        val Green50 = WarpColor(0xFFF0FDF4)
        val Green100 = WarpColor(0xFFDCFCE7)
        val Green200 = WarpColor(0xFFBBF7D0)
        val Green300 = WarpColor(0xFF86EFAC)
        val Green400 = WarpColor(0xFF4ADE80)
        val Green500 = WarpColor(0xFF22C55E)
        val Green600 = WarpColor(0xFF16A34A)
        val Green700 = WarpColor(0xFF15803D)
        val Green800 = WarpColor(0xFF166534)
        val Green900 = WarpColor(0xFF14532D)
        val Green950 = WarpColor(0xFF052E16)
    }
}
