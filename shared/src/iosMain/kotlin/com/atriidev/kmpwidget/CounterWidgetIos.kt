package com.atriidev.kmpwidget

import com.atriidev.warp_runtime.compose.composeWarp
import com.atriidev.warp_runtime.example.counter.CounterWidget
import com.atriidev.warp_runtime.nodes.WarpNode
import com.atriidev.warp_ui.dispatchWarpClick
import com.atriidev.warp_ui.registerWarpClicks
import kotlinx.cinterop.ExperimentalForeignApi
import warpWidgetKit.WarpClickBridge

/**
 * WidgetKit **display** entry: read App Group count → compose WARP tree → register clicks.
 *
 * Returns a [WarpNode]. Swift turns it into SwiftUI via `warpWidgetJson` + `WarpSwiftUIView`.
 *
 * ### Swift timeline / body
 * ```swift
 * let node = CounterWidgetIosKt.renderCounterWidget()
 * let json = WarpWidgetView_iosKt.warpWidgetJson(node: node)
 * // store json on TimelineEntry, then:
 * WarpSwiftUIRootView(json: json, useIntents: true)
 * ```
 *
 * ### Also installs
 * [WarpClickBridge] prepare handler so a cold-start AppIntent can re-run this before dispatch.
 *
 * @see prepareCounterWidgetHandlers
 * @see dispatchCounterWidgetClick
 */
@OptIn(ExperimentalForeignApi::class)
fun renderCounterWidget(): WarpNode {
    installWidgetPrepareHandler()
    val dataStore = KmpDataStore()
    val widgetUpdater = WidgetUpdater()
    val count = dataStore.get(COUNTER_KEY, "0").toIntOrNull() ?: 0
    val node = composeWarp(CounterWidget.State(count = count), CounterWidget.ui)
    registerWarpClicks(counterWidgetClickHandlers(dataStore, widgetUpdater))
    return node
}

/**
 * WidgetKit **cold-start** entry: ensure handlers exist before an AppIntent runs.
 *
 * Call from `WidgetBundle.init` — AppIntent may launch the extension without
 * first rendering the widget body (so [renderCounterWidget] may not have run yet).
 *
 * Swift: `CounterWidgetIosKt.prepareCounterWidgetHandlers()`
 */
@OptIn(ExperimentalForeignApi::class)
fun prepareCounterWidgetHandlers() {
    installWidgetPrepareHandler()
    renderCounterWidget()
}

/**
 * WidgetKit **click** entry: Swift `AppIntent.perform` → Kotlin handlers.
 *
 * Swift (intent in the **extension** target, not Shared):
 * ```swift
 * CounterWidgetIosKt.dispatchCounterWidgetClick(
 *     actionId: actionId,
 *     parametersJson: parametersJson
 * )
 * ```
 *
 * Re-prepares handlers, then [dispatchWarpClick] → [com.atriidev.warp_ui.WarpClicksRegistry]
 * → [CounterClickHandler] → App Group + timeline reload.
 */
fun dispatchCounterWidgetClick(actionId: String, parametersJson: String) {
    prepareCounterWidgetHandlers()
    dispatchWarpClick(actionId, parametersJson)
}

/**
 * Tells [WarpClickBridge] how to warm handlers on first `perform` after process start.
 */
@OptIn(ExperimentalForeignApi::class)
private fun installWidgetPrepareHandler() {
    WarpClickBridge.shared().setPrepareHandler {
        prepareCounterWidgetHandlers()
    }
}
