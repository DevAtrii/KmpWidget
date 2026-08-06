package com.atriidev.kmpwidget

import androidx.compose.runtime.Composable
import com.atriidev.warp_runtime.compose.WarpButton
import com.atriidev.warp_runtime.compose.WarpColumn
import com.atriidev.warp_runtime.compose.WarpRow
import com.atriidev.warp_runtime.compose.WarpText
import com.atriidev.warp_runtime.example.counter.CounterActions
import com.atriidev.warp_runtime.nodes.actions.asClickAction
import com.atriidev.warp_runtime.nodes.modifier.WarpModifier
import com.atriidev.warp_runtime.nodes.modifier.WarpPadding
import com.atriidev.warp_runtime.nodes.modifier.padding
import com.atriidev.warp_ui.WarpClickHandler
import com.atriidev.warp_widget.WarpStateKey
import com.atriidev.warp_widget.WarpWidget
import com.atriidev.warp_widget.WarpWidgetSession
import com.atriidev.warp_widget.api.WidgetEnvironment
import com.atriidev.warp_widget.currentState
import com.atriidev.warp_widget.updateWarpWidgetState

/** Preference keys for [CounterWarpWidget] (Glance prefs / App Group UserDefaults). */
object CounterKeys {
    val Count: WarpStateKey<Int> = WarpStateKey.int(COUNTER_KEY)
}

/**
 * Shared counter [WarpWidget] — one definition for Glance + WidgetKit.
 *
 * [id] matches iOS `Widget.kind` (`"CounterWidget"`) for timeline reload.
 *
 * ### Swift (WidgetKit)
 * ```swift
 * let session = WarpWidgetKitEnv.from(context: context).makeSession()
 * WarpWidgetHost.shared.prepare(widget: CounterWarpWidget.shared, session: session)
 * let json = WarpWidgetHost.shared.composeJson(widget: CounterWarpWidget.shared, session: session)
 * ```
 */
object CounterWarpWidget : WarpWidget {
    override val id: String = "CounterWidget"

    @Composable
    override fun Content(env: WidgetEnvironment) {
        val count = currentState(CounterKeys.Count) ?: 0
        WarpColumn(
            modifier = WarpModifier().padding(WarpPadding(16, 16, 16, 16)),
        ) {
            WarpText("Count")
            WarpRow {
                WarpButton(text = "-", onClick = CounterActions.Decrement.asClickAction())
                WarpText(count.toString())
                WarpButton(text = "+", onClick = CounterActions.Increment.asClickAction())
            }
        }
    }

    override fun clickHandlers(session: WarpWidgetSession): List<WarpClickHandler<*>> =
        listOf(CounterWarpClickHandler(session))
}

/**
 * Persists count via [updateWarpWidgetState] (Glance prefs / UserDefaults + reload).
 */
class CounterWarpClickHandler(
    private val session: WarpWidgetSession,
) : WarpClickHandler<CounterActions>(CounterActions::class, CounterActions.entries) {

    override suspend fun onClick(actionId: CounterActions, parameters: Map<String, String>) {
        val delta = when (actionId) {
            CounterActions.Increment -> +1
            CounterActions.Decrement -> -1
        }
        updateWarpWidgetState(session.context, CounterWarpWidget) {
            val cur = this[CounterKeys.Count] ?: 0
            this[CounterKeys.Count] = cur + delta
        }
    }
}
