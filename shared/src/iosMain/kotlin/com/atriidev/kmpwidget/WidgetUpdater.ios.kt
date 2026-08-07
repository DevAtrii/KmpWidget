package com.atriidev.kmpwidget

import com.atriidev.warp_widget.api.platformContext

/**
 * iOS [WidgetUpdater]: writes [CounterState] to every instance + reloads WidgetKit timeline.
 */
actual class WidgetUpdater {
    actual suspend fun update(counter: Int) {
        updateAllCounterWidgetInstances(CounterWarpWidget.platformContext()) { state ->
            state.copy(count = counter)
        }
    }
}
