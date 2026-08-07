package com.atriidev.warp_ui.glance.internal

import androidx.glance.ButtonColors
import androidx.glance.ButtonDefaults
import androidx.glance.unit.ColorProvider
import com.atriidev.warp_runtime.nodes.style.WarpButtonColors

@androidx.compose.runtime.Composable
internal fun WarpButtonColors?.toGlanceButtonColors(): ButtonColors {
    val defaults = ButtonDefaults.buttonColors()
    if (this == null) return defaults

    val background = backgroundColor?.let { ColorProvider(it.toComposeColor()) }
        ?: defaults.backgroundColor
    val content = contentColor?.let { ColorProvider(it.toComposeColor()) }
        ?: defaults.contentColor
    return ButtonDefaults.buttonColors(
        backgroundColor = background,
        contentColor = content,
    )
}
