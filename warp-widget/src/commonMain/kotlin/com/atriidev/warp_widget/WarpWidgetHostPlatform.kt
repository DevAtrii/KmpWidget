package com.atriidev.warp_widget

import com.atriidev.warp_ui.WarpClickHandler

/**
 * Platform hooks for [WarpWidgetHost.prepare] / [WarpWidgetHost.dispatchClick].
 *
 * - **iOS:** [registerWarpClicks] + [WarpClickBridge] prepare; AppIntent → [dispatchWarpClick]
 * - **Android:** [WarpClicksRegistry] only (Glance uses [com.atriidev.warp_ui.WarpRender])
 */
internal expect fun platformRegisterClickHandlers(handlers: List<WarpClickHandler<*>>)

internal expect fun platformInstallPrepareHandler(reprepare: () -> Unit)

internal expect fun platformDispatchClick(actionId: String, parametersJson: String)
