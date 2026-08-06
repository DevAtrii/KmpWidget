package com.atriidev.warp_ui

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import warpWidgetKit.WarpClickBridge

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
