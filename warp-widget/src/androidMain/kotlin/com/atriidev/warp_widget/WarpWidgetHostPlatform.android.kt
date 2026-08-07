package com.atriidev.warp_widget

import com.atriidev.warp_ui.WarpClickHandler
import com.atriidev.warp_ui.WarpClicksRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

internal actual fun platformRegisterClickHandlers(handlers: List<WarpClickHandler<*>>) {
    WarpClicksRegistry.register(handlers)
}

internal actual fun platformInstallPrepareHandler(reprepare: () -> Unit) {
    // Glance cold-start uses WarpWidgetAndroidRegistry → setWarpGlanceClickPrepareHandler.
}

internal actual fun platformDispatchClick(actionId: String, parametersJson: String) {
    runBlocking {
        WarpClicksRegistry.dispatch(actionId, decodeClickParameters(parametersJson))
    }
}

private fun decodeClickParameters(raw: String): Map<String, String> {
    if (raw.isBlank() || raw == "{}") return emptyMap()
    return Json { ignoreUnknownKeys = true }.decodeFromString(raw)
}
