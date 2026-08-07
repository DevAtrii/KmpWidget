package com.atriidev.warp_widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

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
 * Pair with [WarpGlanceWidget]. Cold-start taps wake this receiver via
 * [WarpWidgetAndroidRegistry] → [ensureRegistered].
 *
 * Manifest intent-filter should include [Intent.ACTION_CONFIGURATION_CHANGED] so
 * light/dark toggles trigger [WarpWidgetAndroidReload] (also installed via init provider).
 */
abstract class WarpGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    /** Shared WARP definition (same instance as [WarpGlanceWidget.widget]). */
    abstract val widget: WarpWidgetHostApi

    /** Fresh Glance host instance (do not cache a single instance across updates). */
    protected abstract fun createGlanceWidget(): WarpGlanceWidget

    @Volatile
    private var registered = false

    final override val glanceAppWidget: GlanceAppWidget
        get() {
            ensureRegistered()
            return createGlanceWidget().also { it.ensureAssetsRegistered() }
        }

    override fun onReceive(context: Context, intent: Intent) {
        ensureRegistered()
        when (intent.action) {
            Intent.ACTION_CONFIGURATION_CHANGED,
            ACTION_UI_MODE_CHANGED,
            -> WarpWidgetAndroidReload.scheduleReloadAll(context, "receiver:${intent.action}")
        }
        super.onReceive(context, intent)
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
            val warp = widget
            WarpWidgetAndroidRegistry.register(warp.id, warp) { createGlanceWidget() }
            registered = true
        }
    }
}
