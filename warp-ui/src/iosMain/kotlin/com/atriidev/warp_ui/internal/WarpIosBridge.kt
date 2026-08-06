package com.atriidev.warp_ui.internal

import com.atriidev.warp_ui.WarpClicksRegistry
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import warpWidgetKit.WarpClickBridge
import warpWidgetKit.WarpWidgetBridge

/**
 * Kotlin ↔ Swift glue for iOS WARP.
 *
 * | Swift type | Role |
 * |------------|------|
 * | [WarpClickBridge] | ObjC singleton; Kotlin installs the click handler here |
 * | [WarpWidgetBridge] | `WidgetCenter.reloadAllTimelines()` |
 *
 * Installed by [com.atriidev.warp_ui.registerWarpClicks] /
 * [com.atriidev.warp_ui.warpRender].
 *
 * **Important:** There must be only one [WarpClickBridge.shared] in the process.
 * Do not compile `warpWidgetKit` Swift sources into the widget extension
 * (that creates a second singleton and breaks clicks).
 */
@OptIn(ExperimentalForeignApi::class)
internal object WarpIosBridge {
    private val json = Json { ignoreUnknownKeys = true }

    /** Asks WidgetKit to redraw all timelines (app or extension process). */
    fun reloadTimelines() {
        WarpWidgetBridge.shared().reloadTimelines()
    }

    /**
     * Installs Kotlin dispatch on [WarpClickBridge.shared].
     *
     * Swift `Button` / preview path → `perform(actionId:parametersJson:)` → here →
     * [WarpClicksRegistry.dispatch].
     */
    fun installClickHandler() {
        WarpClickBridge.shared().setHandler { actionId, parametersJson ->
            runBlocking {
                val parameters = decodeParameters(parametersJson.orEmpty())
                WarpClicksRegistry.dispatch(actionId.orEmpty(), parameters)
            }
        }
    }

    private fun decodeParameters(raw: String): Map<String, String> {
        if (raw.isBlank() || raw == "{}") return emptyMap()
        return json.decodeFromString(raw)
    }
}
