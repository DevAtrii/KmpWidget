package com.atriidev.warp_widget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import com.atriidev.warp_widget.api.WarpWidgetSize

/**
 * Resolve the **current** home-screen widget size from Glance [LocalAppWidgetOptions].
 *
 * Android 12+: [AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH] and
 * [AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT] are the widget's **current** dp size.
 * [AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH] / [AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT]
 * are resize **limits** — do not use max height as current height (inflates narrow widgets).
 *
 * Prefer layout prefs written by [GlanceInternalState.touchLayout] on resize when the Glance
 * session's options bundle is stale ([SizeMode.Single]).
 */
fun Preferences.resolveGlanceWidgetSize(
    options: Bundle,
    fallback: DpSize,
): DpSize {
    val prefW = this[GlanceInternalState.layoutWidthKey]
    val prefH = this[GlanceInternalState.layoutHeightKey]
    if (prefW != null && prefH != null && prefW > 0 && prefH > 0) {
        return DpSize(prefW.toFloat().dp, prefH.toFloat().dp)
    }
    return options.resolveGlanceWidgetSize(fallback)
}

fun Bundle.resolveGlanceWidgetSize(fallback: DpSize): DpSize {
    val minWidth = getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
    val minHeight = getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
    if (minWidth > 0 && minHeight > 0) {
        return DpSize(minWidth.dp, minHeight.dp)
    }

    // Pre-Android-12 / sparse bundles: portrait ≈ minW×maxH, landscape ≈ maxW×minH.
    val maxHeight = getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0)
    if (minWidth > 0 && maxHeight > 0) {
        return DpSize(minWidth.dp, maxHeight.dp)
    }
    val maxWidth = getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0)
    if (maxWidth > 0 && minHeight > 0) {
        return DpSize(maxWidth.dp, minHeight.dp)
    }

    return fallback
}

fun Bundle.resolveGlanceWidgetSizeOrNull(): WarpWidgetSize? {
    val resolved = resolveGlanceWidgetSize(DpSize.Unspecified)
    if (resolved == DpSize.Unspecified) return null
    return WarpWidgetSize(
        widthDp = resolved.width.value,
        heightDp = resolved.height.value,
    )
}

fun DpSize.toWarpWidgetSize(): WarpWidgetSize = WarpWidgetSize(
    widthDp = width.value,
    heightDp = height.value,
)
