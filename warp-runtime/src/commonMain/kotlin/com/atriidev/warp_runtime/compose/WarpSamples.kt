/**
 * Sample widget definitions for trying WARP without writing your own UI first.
 */
package com.atriidev.warp_runtime.compose

import androidx.compose.runtime.Composable
import com.atriidev.warp_runtime.nodes.modifier.WarpModifier
import com.atriidev.warp_runtime.nodes.modifier.WarpPadding
import com.atriidev.warp_runtime.nodes.modifier.padding

/**
 * A pre-built counter widget UI used by [sampleCounterWidgetJson] and tests.
 *
 * Layout: column with title text and a row of [-] [count] [+] buttons.
 * Stored as a composable lambda in `commonMain` so the Compose Compiler processes it correctly.
 */
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
 * Composes the sample counter widget and returns its JSON representation.
 *
 * Useful for quick manual checks:
 * ```
 * val json = sampleCounterWidgetJson()
 * println(json)
 * ```
 *
 * @return Pretty-printed JSON of the sample counter [com.atriidev.warp_runtime.nodes.WarpNode] tree.
 */
fun sampleCounterWidgetJson(): String = composeWarpToJson(sampleCounterWidgetUi)
