package com.atriidev.warp_runtime.example.counter

import androidx.compose.runtime.Composable
import com.atriidev.warp_runtime.compose.WarpButton
import com.atriidev.warp_runtime.compose.WarpColumn
import com.atriidev.warp_runtime.compose.WarpRow
import com.atriidev.warp_runtime.compose.WarpText
import com.atriidev.warp_runtime.compose.composeWarpToJson
import com.atriidev.warp_runtime.nodes.actions.asClickAction
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import kotlinx.serialization.Serializable

/**
 * Example counter widget — demonstrates state, typed actions, and click handlers together.
 *
 * Not part of the core WARP API. Copy or adapt this package when building your own widgets.
 *
 * Exported to Swift as `CounterWidget` — keep WidgetKit hosts named differently
 * (e.g. `CounterHomeWidget`) to avoid clashes.
 */
object CounterWidget {

    /** Serializable state for the sample counter widget. */
    @Serializable
    data class State(
        val count: Int = 0,
    )

    /** Counter UI driven by [State]. Layout: column with title and a row of [-] [count] [+]. */
    val ui: @Composable (State) -> Unit = { state ->
        WarpColumn(
            modifier = WarpModifier.padding(16),
        ) {
            WarpText("Counter")
            WarpRow {
                WarpButton(text = "-", onClick = CounterActions.Decrement.asClickAction())
                WarpText(state.count.toString())
                WarpButton(text = "+", onClick = CounterActions.Increment.asClickAction())
            }
        }
    }

    /** Composes the sample counter widget and returns its JSON representation. */
    fun toJson(count: Int = 42): String =
        composeWarpToJson(State(count = count), ui)

}

/** @see CounterWidget.State */
typealias CounterState = CounterWidget.State

/** @see CounterWidget.ui */
internal val sampleCounterWidgetUi: @Composable (CounterState) -> Unit = CounterWidget.ui

/** @see CounterWidget.toJson */
fun sampleCounterWidgetJson(count: Int = 42): String = CounterWidget.toJson(count)

