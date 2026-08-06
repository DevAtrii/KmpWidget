package com.atriidev.warp_ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.atriidev.warp_runtime.compose.toJson
import com.atriidev.warp_runtime.nodes.WarpNode
import com.atriidev.warp_ui.internal.WarpIosBridge
import kotlinx.cinterop.ExperimentalForeignApi
import warpWidgetKit.WarpSwiftUIView as NativeWarpSwiftUIView

@Composable
actual fun WarpRender(node: WarpNode, handlers: List<WarpClickHandler<*>>) {
    SideEffect {
        warpRender(node, handlers)
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun warpRender(node: WarpNode, handlers: List<WarpClickHandler<*>>): WarpSwiftUIView {
    WarpClicksRegistry.register(handlers)
    WarpIosBridge.installClickHandler()
    val json = node.toJson()
    WarpIosBridge.publishNode(json)
    return NativeWarpSwiftUIView(json = json, useIntents = true)
}
