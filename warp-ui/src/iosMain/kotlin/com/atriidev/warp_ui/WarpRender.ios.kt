package com.atriidev.warp_ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.atriidev.warp_runtime.nodes.WarpNode
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS [WarpRender]: registers handlers via [warpRender] on each composition (side-effect).
 *
 * Prefer [warpRender] / [registerWarpClicks] + [warpWidgetJson] for WidgetKit hosts.
 */
@Composable
actual fun WarpRender(node: WarpNode, handlers: List<WarpClickHandler<*>>) {
    SideEffect {
        warpRender(node, handlers)
    }
}

/**
 * iOS [warpRender]: [registerWarpClicks] + [warpWidgetView] (`useIntents = true`).
 *
 * Returns a [WarpSwiftUIView] for in-app preview (`previewView()` / `UIKitView`).
 * For home-screen WidgetKit, Swift should call [warpWidgetJson] and host
 * `WarpSwiftUIRootView` directly (pure SwiftUI — no `UIViewControllerRepresentable`).
 */
@OptIn(ExperimentalForeignApi::class)
actual fun warpRender(node: WarpNode, handlers: List<WarpClickHandler<*>>): WarpSwiftUIView {
    registerWarpClicks(handlers)
    return warpWidgetView(node, useIntents = true)
}
