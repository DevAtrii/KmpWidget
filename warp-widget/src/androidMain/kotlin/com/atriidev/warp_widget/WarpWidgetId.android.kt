package com.atriidev.warp_widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager

/** Encode [appWidgetId] as [WarpWidgetId] (`"aw:$appWidgetId"`). */
fun WarpWidgetId.Companion.fromAppWidgetId(appWidgetId: Int): WarpWidgetId =
    WarpWidgetId.android(appWidgetId)

/** Resolve [GlanceId] → [WarpWidgetId] via [GlanceAppWidgetManager.getAppWidgetId]. */
suspend fun WarpWidgetId.Companion.fromGlanceId(context: Context, glanceId: GlanceId): WarpWidgetId {
    val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
    return fromAppWidgetId(appWidgetId)
}

/** Parse `"aw:$id"` → app widget id; null for non-Android ids. */
fun WarpWidgetId.toAppWidgetIdOrNull(): Int? {
    if (!value.startsWith("aw:")) return null
    return value.removePrefix("aw:").toIntOrNull()
}
