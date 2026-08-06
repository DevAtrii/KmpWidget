package com.atriidev.warp_widget

import com.atriidev.warp_ui.WarpClickHandler
import com.atriidev.warp_ui.dispatchWarpClick
import com.atriidev.warp_ui.registerWarpClicks
import kotlinx.cinterop.ExperimentalForeignApi
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

@OptIn(ExperimentalForeignApi::class)
internal actual fun platformDispatchClick(actionId: String, parametersJson: String) {
    dispatchWarpClick(actionId, parametersJson)
}
