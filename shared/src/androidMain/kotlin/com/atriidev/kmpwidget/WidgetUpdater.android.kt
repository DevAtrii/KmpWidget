package com.atriidev.kmpwidget

import android.content.Context
import com.atriidev.warp_widget.api.PlatformContext
import com.atriidev.warp_widget.updateWarpWidgetState

actual class WidgetUpdater(
    private val context: Context,
) {
    actual suspend fun update(counter: Int) {
        updateWarpWidgetState(PlatformContext(context), CounterWarpWidget) { state ->
            state.copy(count = counter)
        }
    }
}
