package com.atriidev.kmpwidget

import kotlinx.cinterop.ExperimentalForeignApi
import warpWidgetKit.WarpWidgetBridge

/**
 * iOS [WidgetUpdater]: asks WidgetKit to reload timelines after counter changes.
 *
 * Swift: `WarpWidgetBridge.shared.reloadTimelines()` → `WidgetCenter.reloadAllTimelines()`.
 * Works from the **app** process (after UI +/-) and from the **extension** (after AppIntent).
 */
actual class WidgetUpdater {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun update(counter: Int) {
        WarpWidgetBridge.shared().reloadTimelines()
    }
}
