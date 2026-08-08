package com.atriidev.kmpwidget

import com.atriidev.warp_widget.api.PlatformContext
import com.atriidev.warp_widget.listWarpWidgetIds
import com.atriidev.warp_widget.readWarpWidgetState
import com.atriidev.warp_widget.updateWarpWidgetState

/** First installed instance, or [CounterWarpWidget.defaultState] when none on screen. */
suspend fun readCounterWidgetState(context: PlatformContext): CounterState {
    val ids = listWarpWidgetIds(context, CounterWarpWidget)
    if (ids.isEmpty()) return CounterWarpWidget.defaultState()
    return readWarpWidgetState(context, CounterWarpWidget, ids.first())
}

/** Apply [transform] to every home-screen [CounterWarpWidget] instance. */
suspend fun updateAllCounterWidgetInstances(
    context: PlatformContext,
    transform: (CounterState) -> CounterState,
) {
    listWarpWidgetIds(context, CounterWarpWidget).forEach { id ->
        updateWarpWidgetState(context, CounterWarpWidget, id, transform)
    }
}
