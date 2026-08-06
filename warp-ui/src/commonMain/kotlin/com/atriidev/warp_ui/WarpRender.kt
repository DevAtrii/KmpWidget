package com.atriidev.warp_ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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
