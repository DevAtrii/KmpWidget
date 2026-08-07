package com.atriidev.warp_ui

import android.content.Context
import androidx.glance.GlanceId
import com.atriidev.warp_ui.glance.internal.WarpGlanceClickPrepare

/**
 * Install cold-start re-registration for Glance [androidx.glance.appwidget.action.ActionCallback].
 *
 * Call from app / [com.atriidev.warp_widget.WarpWidgetAndroidRegistry] once at process start.
 * Handler should register [WarpClickHandler]s into [WarpClicksRegistry] for the given
 * [GlanceId] before dispatch runs.
 */
fun setWarpGlanceClickPrepareHandler(handler: suspend (Context, GlanceId) -> Unit) {
    WarpGlanceClickPrepare.setPrepareHandler(handler)
}
