package com.atriidev.kmpwidget

import com.atriidev.warp_runtime.compose.composeWarp
import com.atriidev.warp_runtime.compose.toJson
import com.atriidev.warp_runtime.example.counter.CounterWidget
import com.atriidev.warp_ui.dispatchWarpClick
import com.atriidev.warp_ui.warpRender
import kotlinx.cinterop.ExperimentalForeignApi
import warpWidgetKit.WarpClickBridge

/**
 * Publishes and returns counter widget JSON for WidgetKit (.systemSmall).
 * Mirrors [com.atriidev.kmpwidget.CounterWidget] on Android Glance.
 */
@OptIn(ExperimentalForeignApi::class)
fun renderCounterWidget(): String {
    installWidgetPrepareHandler()
    val dataStore = KmpDataStore()
    val widgetUpdater = WidgetUpdater()
    val count = dataStore.get(COUNTER_KEY, "0").toIntOrNull() ?: 0
    val node = composeWarp(CounterWidget.State(count = count), CounterWidget.ui)
    warpRender(node, counterWidgetClickHandlers(dataStore, widgetUpdater))
    return node.toJson()
}

/** Registers handlers before widget AppIntent dispatch (extension cold start). */
@OptIn(ExperimentalForeignApi::class)
fun prepareCounterWidgetHandlers() {
    installWidgetPrepareHandler()
    renderCounterWidget()
}

/** Widget extension click entry — callable from Swift AppIntent. */
fun dispatchCounterWidgetClick(actionId: String, parametersJson: String) {
    prepareCounterWidgetHandlers()
    dispatchWarpClick(actionId, parametersJson)
}

@OptIn(ExperimentalForeignApi::class)
private fun installWidgetPrepareHandler() {
    WarpClickBridge.shared().setPrepareHandler {
        prepareCounterWidgetHandlers()
    }
}
