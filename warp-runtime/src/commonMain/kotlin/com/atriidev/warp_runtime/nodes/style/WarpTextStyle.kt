package com.atriidev.warp_runtime.nodes.style

import com.atriidev.warp_runtime.nodes.modifiers.WarpColor
import com.atriidev.warp_runtime.unit.Sp
import com.atriidev.warp_runtime.unit.sp
import kotlinx.serialization.Serializable

/**
 * Serializable text style — Compose/Glance `TextStyle`-shaped args, not a modifier.
 *
 * Only non-null fields should be applied by native renderers.
 */
@Serializable
data class WarpTextStyle(
    val color: WarpColor? = null,
    /** Font size in sp. */
    val fontSize: Sp? = null,
    val fontWeight: WarpFontWeight? = null,
    val textAlign: WarpTextAlign? = null,
) {
    companion object {
        fun color(hex: String): WarpTextStyle =
            WarpTextStyle(color = WarpColor(hex))

        fun fontSize(size: Sp): WarpTextStyle =
            WarpTextStyle(fontSize = size)

        fun fontSize(size: Number): WarpTextStyle =
            WarpTextStyle(fontSize = size.toFloat().sp)
    }
}
