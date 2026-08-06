package com.atriidev.warp_ui

import androidx.compose.runtime.Composable
import com.atriidev.warp_runtime.nodes.WarpNode

/**
 * Renders a [WarpNode] tree on the current platform.
 *
 * [handlers] are registered in [WarpClicksRegistry] for the platform click callback.
 *
 * ```
 * WarpRender(
 *     node = composeWarp(state, MyWidget.ui),
 *     handlers = listOf(CounterClickHandler(dataStore, widgetUpdater)),
 * )
 * ```
 */
@Composable
expect fun WarpRender(node: WarpNode, handlers: List<WarpClickHandler<*>>)

/**
 * Builds a native widget view from a [WarpNode] tree.
 *
 * - **iOS:** returns [WarpSwiftUIView] — call `makeView()` in Swift for the SwiftUI tree.
 * - **Android:** throws — use [WarpRender] with Jetpack Glance instead.
 *
 * ```
 * val view = warpRender(
 *     node = composeWarp(state, MyWidget.ui),
 *     handlers = counterWidgetClickHandlers(dataStore, widgetUpdater),
 * )
 * ```
 */
expect fun warpRender(node: WarpNode, handlers: List<WarpClickHandler<*>>): WarpSwiftUIView






