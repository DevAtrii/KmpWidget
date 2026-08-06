package com.atriidev.warp_ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.atriidev.warp_runtime.nodes.WarpNode

@Composable
actual fun WarpRender(node: WarpNode, handlers: List<WarpClickHandler<*>>) {
    SideEffect {
        WarpClicksRegistry.register(handlers)
    }
    error("WarpRender is not implemented on iOS yet")
}
