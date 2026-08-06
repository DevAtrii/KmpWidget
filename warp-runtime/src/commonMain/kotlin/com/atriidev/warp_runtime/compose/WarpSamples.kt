/**
 * Sample widget definitions for trying WARP without writing your own UI first.
 */
package com.atriidev.warp_runtime.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.atriidev.warp_runtime.nodes.modifier.WarpModifier
import com.atriidev.warp_runtime.nodes.modifier.WarpPadding
import com.atriidev.warp_runtime.nodes.modifier.padding
import kotlinx.serialization.Serializable

/**
 * Serializable state for the sample counter widget.
 *
 * @property count The number displayed between the increment and decrement buttons.
 */
@Serializable
data class CounterState(
    val count: Int = 0,
)

/**
 * Counter widget UI driven by [CounterState].
 *
 * Layout: column with title text and a row of [-] [count] [+] buttons.
 */
internal val sampleCounterWidgetUi: @Composable (CounterState) -> Unit = { state ->
    WarpColumn(
        modifier = WarpModifier().padding(WarpPadding(16, 16, 16, 16)),
    ) {
        WarpText("Counter")
        WarpRow {
            WarpButton(text = "-", actionId = "decrement")
            WarpText(state.count.toString())
            WarpButton(text = "+", actionId = "increment")
        }
    }
}

/**
 * Composes the sample counter widget and returns its JSON representation.
 *
 * @param count Counter value shown in the widget. Defaults to `42`.
 */
fun sampleCounterWidgetJson(count: Int = 42): String =
    composeWarpToJson(CounterState(count = count), sampleCounterWidgetUi)

/**
 * Demo composable that mutates [androidx.compose.runtime.mutableStateOf] during composition.
 *
 * Used to verify [composeWarp] runs multiple recomposition passes before returning.
 */
internal val mutableStateCounterUi: @Composable () -> Unit = {
    val count = remember { mutableStateOf(0) }
    if (count.value == 0) {
        count.value = 5
    }
    WarpText(count.value.toString())
}
