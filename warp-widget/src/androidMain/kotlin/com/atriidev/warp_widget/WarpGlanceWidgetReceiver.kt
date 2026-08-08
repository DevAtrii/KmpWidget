package com.atriidev.warp_widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.atriidev.warp_runtime.log.WarpLogger

private const val ACTION_UI_MODE_CHANGED = "android.intent.action.UI_MODE_CHANGED"

/**
 * Glance [GlanceAppWidgetReceiver] that auto-registers with [WarpWidgetAndroidRegistry].
 *
 * Call [register] from subclass property access / [onReceive] / cold-start wake — not from
 * this class’s `init` (subclass [widget] is not ready yet).
 *
 * ```
 * class CounterWidgetReceiver : WarpGlanceWidgetReceiver() {
 *     override val widget get() = CounterWarpWidget
 *     override fun createGlanceWidget() = CounterGlanceAppWidget()
 * }
 * ```
 *
 * Resizes: default [androidx.glance.appwidget.SizeMode.Single] ignores Glance [resize];
 * [WarpGlanceWidgetReceiver.onAppWidgetOptionsChanged] forces layout reload instead.
 */
abstract class WarpGlanceWidgetReceiver : GlanceAppWidgetReceiver() {

    /** Fresh Glance host instance (do not cache a single instance across updates). */
    protected abstract fun createGlanceWidget(): WarpGlanceWidget

    /** Shared WARP definition (same instance as [WarpGlanceWidget.createWarpWidget]). */
    protected abstract fun createWarpWidget(): WarpWidgetHostApi

    @Volatile
    private var registered = false

    final override val glanceAppWidget: GlanceAppWidget
        get() {
            ensureRegistered()
            return createGlanceWidget().also { it.ensureAssetsRegistered() }
        }

    override fun onReceive(context: Context, intent: Intent) {
        ensureRegistered()
        WarpLogger.d("WarpGlanceWidgetReceiver", "onReceive: intent action = ${intent.action}")
        when (intent.action) {
            Intent.ACTION_CONFIGURATION_CHANGED,
            ACTION_UI_MODE_CHANGED,
                -> WarpWidgetAndroidReload.scheduleReloadAll(context, "receiver:${intent.action}")
        }
        super.onReceive(context, intent)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        ensureRegistered()
        WarpLogger.d(
            "WarpGlanceWidgetReceiver",
            "onAppWidgetOptionsChanged: reloading layout for appWidgetId = $appWidgetId"
        )
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        WarpWidgetAndroidReload.scheduleLayoutReload(
            context = context,
            appWidgetId = appWidgetId,
            options = newOptions,
            widgetFactory = { createGlanceWidget() },
        )
    }

    /**
     * Idempotent [WarpWidgetAndroidRegistry.register] for [widget] + [createGlanceWidget].
     *
     * Safe after full construction (property accessors ready). Invoked by Glance and by
     * cold-start [WarpWidgetAndroidRegistry] wake.
     */
    fun ensureRegistered() {
        if (registered) return
        synchronized(this) {
            if (registered) return
            val warp = createWarpWidget()
            WarpWidgetAndroidRegistry.register(warp.id, warp) { createGlanceWidget() }
            registered = true
        }
    }
}
