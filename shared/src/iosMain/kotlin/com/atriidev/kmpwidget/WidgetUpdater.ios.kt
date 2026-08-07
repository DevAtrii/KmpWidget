package com.atriidev.kmpwidget

import com.atriidev.warp_widget.api.platformContext
import com.atriidev.warp_widget.updateWarpWidgetState

/**
 * iOS [WidgetUpdater]: writes [CounterWarpWidget] prefs + reloads WidgetKit timeline.
 */
actual class WidgetUpdater {
    actual suspend fun update(counter: Int) {
        updateWarpWidgetState(
            CounterWarpWidget.platformContext(),
            CounterWarpWidget,
        ) {
            this[CounterKeys.Count] = counter
        }
    }
}
