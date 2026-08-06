package com.atriidev.warp_widget

import androidx.glance.appwidget.GlanceAppWidget

/**
 * Maps WARP [WarpWidget.id] → Glance widget instance factory.
 *
 * Call once at app / receiver startup before [WarpWidgetStateStore] update/reload:
 * ```
 * WarpWidgetAndroidRegistry.register("counter") { CounterGlanceWidget() }
 * ```
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
