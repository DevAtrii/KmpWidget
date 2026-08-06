package com.atriidev.warp_widget

import androidx.glance.appwidget.GlanceAppWidget

/**
 * Phone book: WARP [WarpWidget.id] → Glance `GlanceAppWidget` factory.
 *
 * Shared code updates widgets by string id. Glance needs a concrete
 * [androidx.glance.appwidget.GlanceAppWidget] to read/write
 * [PreferencesGlanceStateDefinition](androidx.glance.state.PreferencesGlanceStateDefinition)
 * and call `update`. Register once at app / receiver startup:
 *
 * ```
 * WarpWidgetAndroidRegistry.register(CounterWarpWidget.id) { CounterGlanceAppWidget() }
 * ```
 *
 * Without a registration, [WarpWidgetStateStore] update/reload on Android no-ops
 * (and logs an error).
 */
object WarpWidgetAndroidRegistry {
    private val factories = mutableMapOf<String, () -> GlanceAppWidget>()

    fun register(widgetId: String, factory: () -> GlanceAppWidget) {
        factories[widgetId] = factory
    }

    fun unregister(widgetId: String) {
        factories.remove(widgetId)
    }

    fun clear() {
        factories.clear()
    }

    internal fun create(widgetId: String): GlanceAppWidget? =
        factories[widgetId]?.invoke()

    internal fun isRegistered(widgetId: String): Boolean =
        factories.containsKey(widgetId)
}
