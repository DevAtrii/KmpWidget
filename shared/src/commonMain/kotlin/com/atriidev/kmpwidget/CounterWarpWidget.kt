package com.atriidev.kmpwidget

import androidx.compose.runtime.Composable
import com.atriidev.kmpwidget.CounterWarpWidget.id
import com.atriidev.warp_runtime.compose.WarpButton
import com.atriidev.warp_runtime.compose.WarpColumn
import com.atriidev.warp_runtime.compose.WarpRow
import com.atriidev.warp_runtime.compose.WarpText
import com.atriidev.warp_runtime.example.counter.CounterActions
import com.atriidev.warp_runtime.nodes.actions.asClickAction
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
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
 * let session = WarpWidgetHost.shared.iosSession(
 *     widget: CounterWarpWidget.shared,
 *     kitFields: WarpWidgetKitEnv.from(context: context).asKitFields(
 *         appGroupId: CounterWarpWidget.shared.iosGroupId
 *     )
 * )
 * ```
 */
object CounterWarpWidget : WarpWidget {
    override val id: String = "CounterWidget"

    /** App Group suite — keep in sync with Xcode entitlements / [APP_GROUP_ID]. */
    override val iosGroupId: String = APP_GROUP_ID

    @Composable
    override fun Content(env: WidgetEnvironment) {
        println("WIDGET_ENV $env")
        val count = currentState(CounterKeys.Count) ?: 0
        WarpColumn(
            modifier = WarpModifier
                .padding(16),
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
        println("WARP_CLICK: id=$actionId, params=$parameters")
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
