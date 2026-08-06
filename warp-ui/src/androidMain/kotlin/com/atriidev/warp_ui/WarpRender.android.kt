package com.atriidev.warp_ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.atriidev.warp_runtime.nodes.WarpNode
import com.atriidev.warp_ui.glance.internal.RenderWarpNode
import com.atriidev.warp_ui.glance.internal.WarpRegistryActionCallback
import com.atriidev.warp_ui.glance.internal.clickActionFor

@Composable
actual fun WarpRender(node: WarpNode, handlers: List<WarpClickHandler<*>>) {
    SideEffect {
        WarpClicksRegistry.register(handlers)
    }
    RenderWarpNode(node) { action ->
        clickActionFor(WarpRegistryActionCallback::class.java, action)
    }
}

actual fun warpRender(node: WarpNode, handlers: List<WarpClickHandler<*>>): WarpSwiftUIView {
    error("warpRender returns a SwiftUI view and is iOS-only. Use WarpRender() for Jetpack Glance on Android.")
}

