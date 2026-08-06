package com.atriidev.kmpwidget

import com.atriidev.warp_widget.api.DEFAULT_IOS_APP_GROUP_ID
import com.atriidev.warp_widget.api.PlatformContext
import com.atriidev.warp_widget.updateWarpWidgetState

/**
 * iOS [WidgetUpdater]: writes [CounterWarpWidget] prefs + reloads WidgetKit timeline.
 */
actual class WidgetUpdater {
    actual suspend fun update(counter: Int) {
        updateWarpWidgetState(
            PlatformContext(appGroupId = DEFAULT_IOS_APP_GROUP_ID),
            CounterWarpWidget,
        ) {
            this[CounterKeys.Count] = counter
        }
    }
}
