package com.atriidev.warp_ui

import com.atriidev.warp_runtime.compose.toJson
import com.atriidev.warp_runtime.nodes.WarpNode
import com.atriidev.warp_ui.internal.WarpIosBridge
import kotlinx.cinterop.ExperimentalForeignApi
import warpWidgetKit.WarpSwiftUIView as NativeWarpSwiftUIView

/**
 * Registers [handlers] in [WarpClicksRegistry] and wires [WarpClickBridge] → Kotlin dispatch.
 *
 * Call from your `renderXWidget()` **before** returning the [WarpNode] so WidgetKit
 * AppIntents (and in-app previews) can resolve `actionId`s.
 *
 * ### Swift side
 * Does not produce UI. Swift still needs [warpWidgetJson] + `WarpSwiftUIView`.
 *
 * ### Flow
 * ```
 * Kotlin registerWarpClicks(handlers)
 *   → WarpClicksRegistry
 *   → WarpClickBridge.setHandler { dispatch(...) }
 *
 * Swift AppIntent / Button
 *   → dispatchWarpClick / WarpClickBridge.perform
 *   → WarpClicksRegistry.dispatch
 *   → WarpClickHandler.onClick
 * ```
 */
@OptIn(ExperimentalForeignApi::class)
fun registerWarpClicks(handlers: List<WarpClickHandler<*>>) {
    WarpClicksRegistry.register(handlers)
    WarpIosBridge.installClickHandler()
}

/**
 * Serializes [node] to WARP JSON for SwiftUI hosting.
 *
 * Prefer this over returning [WarpSwiftUIView] across the Shared framework boundary:
 * that Swift class is only **forward-declared** in `Shared.h`, so Swift cannot call
 * methods like `widgetRootView()` on a Kotlin-returned instance.
 *
 * ### Swift (WidgetKit)
 * ```swift
 * import Shared
 * import warpWidgetKit
 *
 * let node = CounterWidgetIosKt.renderCounterWidget() // registers clicks
 * let json = WarpWidgetView_iosKt.warpWidgetJson(node: node)
 * return WarpSwiftUIRootView(json: json, useIntents: true, widgetId: MyWidget.id)
 * ```
 *
 * @see warpWidgetView for Kotlin / in-app use when you already hold the Swift type
 */
fun warpWidgetJson(node: WarpNode): String = node.toJson()

/**
 * Builds a [WarpSwiftUIView] holder for [node] (Kotlin / same-process use).
 *
 * - Does **not** register handlers — call [registerWarpClicks] or [warpRender] first.
 * - [useIntents] `true` → intent buttons (needs [WarpClickIntentRegistry] when hosted
 *   with a `widgetId`; in-app preview usually forces bridge via `makePreviewView`).
 * - [useIntents] `false` → buttons call [WarpClickBridge] (in-app preview).
 *
 * ### WidgetKit Swift
 * Prefer [warpWidgetJson] + `WarpSwiftUIRootView(json:useIntents:widgetId:)` so each
 * widget kind maps to its installed intent.
 */
@OptIn(ExperimentalForeignApi::class)
fun warpWidgetView(node: WarpNode, useIntents: Boolean = true): WarpSwiftUIView =
    // ObjC/cinterop exposes the 2-arg init; pass widgetId from Swift WidgetKit hosts.
    NativeWarpSwiftUIView(json = warpWidgetJson(node), useIntents = useIntents)
