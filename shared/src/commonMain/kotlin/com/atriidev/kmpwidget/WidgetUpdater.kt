package com.atriidev.kmpwidget

/**
 * Platform hook to refresh the home-screen widget after state changes.
 *
 * - **Android:** Glance / AppWidget update
 * - **iOS:** `WarpWidgetBridge.reloadTimelines()` → `WidgetCenter`
 */
expect class WidgetUpdater {
    /**
     * @param counter latest counter value (platforms may ignore and just reload)
     */
    suspend fun update(counter: Int)
}
