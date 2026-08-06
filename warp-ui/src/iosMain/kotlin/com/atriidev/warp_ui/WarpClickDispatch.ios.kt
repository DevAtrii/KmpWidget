package com.atriidev.warp_ui

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import warpWidgetKit.WarpClickBridge

/**
 * Entry point for Swift AppIntents → Kotlin [WarpClicksRegistry].
 *
 * ### Typical Swift call (widget extension)
 * ```swift
 * CounterWidgetIosKt.dispatchCounterWidgetClick(actionId:actionId, parametersJson:parametersJson)
 * // which eventually calls:
 * WarpClickDispatch_iosKt.dispatchWarpClick(actionId:parametersJson:)
 * ```
 *
 * Runs [WarpClickBridge.prepareIfNeeded] first so cold-start AppIntent launches
 * can re-register handlers before [WarpClicksRegistry.dispatch].
 *
 * @param actionId wire id from WARP JSON (`"increment"`, `"decrement"`, …)
 * @param parametersJson JSON object of string params, or `"{}"`
 */
@OptIn(ExperimentalForeignApi::class)
fun dispatchWarpClick(actionId: String, parametersJson: String) {
    WarpClickBridge.shared().prepareIfNeeded()
    runBlocking {
        WarpClicksRegistry.dispatch(actionId, decodeClickParameters(parametersJson))
    }
}

private fun decodeClickParameters(raw: String): Map<String, String> {
    if (raw.isBlank() || raw == "{}") return emptyMap()
    return Json { ignoreUnknownKeys = true }.decodeFromString(raw)
}
