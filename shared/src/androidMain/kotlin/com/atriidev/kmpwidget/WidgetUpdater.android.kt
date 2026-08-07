package com.atriidev.kmpwidget

import android.content.Context
import com.atriidev.warp_widget.api.PlatformContext

actual class WidgetUpdater(
    private val context: Context,
) {
    actual suspend fun update(counter: Int) {
        updateAllCounterWidgetInstances(PlatformContext(context)) { state ->
            state.copy(count = counter)
        }
    }
}
