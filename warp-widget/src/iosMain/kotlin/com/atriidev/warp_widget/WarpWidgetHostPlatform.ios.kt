package com.atriidev.warp_widget

import com.atriidev.warp_ui.WarpClickHandler
import com.atriidev.warp_ui.WarpClicksRegistry
import com.atriidev.warp_ui.registerWarpClicks
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import com.atriidev.warp_runtime.log.WarpLogger
import kotlinx.serialization.json.Json
import warpWidgetKit.WarpClickBridge

@OptIn(ExperimentalForeignApi::class)
internal actual fun platformRegisterClickHandlers(handlers: List<WarpClickHandler<*>>) {
    ensureWarpWidgetKitSharedInstalled()
    registerWarpClicks(handlers)
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun platformInstallPrepareHandler(reprepare: () -> Unit) {
    WarpClickBridge.shared().setPrepareHandler {
        reprepare()
    }
}

/**
 * Dispatch after [WarpWidgetHost] already prepared handlers.
 *
 * Do **not** call [com.atriidev.warp_ui.dispatchWarpClick] here — that path runs
 * `prepareIfNeeded` again and is for WarpClickBridge.perform (non-intent buttons).
 * AppIntent → Host.dispatchClick already prepared the correct instance session.
 */
internal actual fun platformDispatchClick(actionId: String, parametersJson: String) {
    val params = decodeClickParametersSafe(parametersJson)
    runBlocking {
        try {
            val ok = WarpClicksRegistry.dispatch(actionId, params)
            if (!ok) {
                WarpLogger.w(
                    "WarpWidgetHostPlatform",
                    "WARP_CLICK iOS: no handler for actionId='$actionId' " +
                        "params=$params (registry empty?)"
                )
            }
        } catch (t: Throwable) {
            WarpLogger.e("WarpWidgetHostPlatform", "WARP_CLICK iOS: handler threw actionId='$actionId': $t", t)
        }
    }
}

private fun decodeClickParametersSafe(raw: String): Map<String, String> {
    if (raw.isBlank() || raw == "{}") return emptyMap()
    return runCatching {
        Json { ignoreUnknownKeys = true }.decodeFromString<Map<String, String>>(raw)
    }.getOrElse {
        WarpLogger.w("WarpWidgetHostPlatform", "WARP_CLICK iOS: bad parametersJson='$raw' (${it.message})")
        emptyMap()
    }
}
