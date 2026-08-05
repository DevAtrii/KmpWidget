package com.atriidev.warp_runtime.compose

import com.atriidev.warp_runtime.nodes.modifier.WarpModifier
import com.atriidev.warp_runtime.nodes.modifier.WarpPadding
import com.atriidev.warp_runtime.nodes.modifier.padding
import androidx.compose.runtime.Composable

internal val sampleCounterWidgetUi: @Composable () -> Unit = {
    WarpColumn(
        modifier = WarpModifier().padding(WarpPadding(16, 16, 16, 16)),
    ) {
        WarpText("Counter")
        WarpRow {
            WarpButton(text = "-", actionId = "decrement")
            WarpText("42")
            WarpButton(text = "+", actionId = "increment")
        }
    }
}

/**
 * Example widget UI for manual PoC runs.
 *
 * Usage:
 * ```
 * val json = sampleCounterWidgetJson()
 * println(json)
 * ```
 */
fun sampleCounterWidgetJson(): String = composeWarpToJson(sampleCounterWidgetUi)
