package com.atriidev.warp_ui.glance.internal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.unit.ColorProvider
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.atriidev.warp_runtime.nodes.modifiers.WarpColor
import com.atriidev.warp_runtime.nodes.style.WarpFontWeight
import com.atriidev.warp_runtime.nodes.style.WarpTextAlign
import com.atriidev.warp_runtime.nodes.style.WarpTextStyle

internal fun WarpTextStyle.toGlanceTextStyle(): TextStyle {
    var textStyle = TextStyle()
    color?.let {
        textStyle = textStyle.copy(color = ColorProvider(it.toComposeColor()))
    }
    fontSize?.let {
        textStyle = textStyle.copy(fontSize = it.value.sp)
    }
    fontWeight?.let {
        textStyle = textStyle.copy(fontWeight = it.toGlanceFontWeight())
    }
    textAlign?.let {
        textStyle = textStyle.copy(textAlign = it.toGlanceTextAlign())
    }
    return textStyle
}

internal fun WarpColor.toComposeColor(): Color = Color(toArgbInt())

private fun WarpFontWeight.toGlanceFontWeight(): FontWeight = when (this) {
    WarpFontWeight.Normal -> FontWeight.Normal
    WarpFontWeight.Medium -> FontWeight.Medium
    WarpFontWeight.Semibold -> FontWeight.Medium
    WarpFontWeight.Bold -> FontWeight.Bold
}

private fun WarpTextAlign.toGlanceTextAlign(): TextAlign = when (this) {
    WarpTextAlign.Start -> TextAlign.Start
    WarpTextAlign.Center -> TextAlign.Center
    WarpTextAlign.End -> TextAlign.End
}
