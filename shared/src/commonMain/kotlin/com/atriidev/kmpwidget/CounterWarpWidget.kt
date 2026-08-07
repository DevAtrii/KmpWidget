package com.atriidev.kmpwidget

import androidx.compose.runtime.Composable
import com.atriidev.kmpwidget.CounterWarpWidget.id
import com.atriidev.warp_runtime.compose.WarpButton
import com.atriidev.warp_runtime.compose.WarpColumn
import com.atriidev.warp_runtime.compose.WarpRow
import com.atriidev.warp_runtime.compose.WarpText
import com.atriidev.warp_runtime.example.counter.CounterActions
import com.atriidev.warp_runtime.nodes.actions.asClickAction
import com.atriidev.warp_runtime.nodes.modifiers.WarpColor
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.style.WarpButtonColors
import com.atriidev.warp_runtime.nodes.style.WarpFontWeight
import com.atriidev.warp_runtime.nodes.style.WarpTextStyle
import com.atriidev.warp_runtime.nodes.style.WarpVerticalAlignment
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
 * Alignments / textAlign use Glance defaults (Start / Top) unless set explicitly.
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
                .fillMaxSize()
                .background("#1B2838")
                .cornerRadius(16)
                .padding(12),
        ) {
            WarpText(
                text = "Count",
                modifier = WarpModifier
                    .fillMaxWidth()
                    .padding(start = 0, end = 0, top = 0, bottom = 8)
                    .clickable(CounterActions.Reset.asClickAction()),
                style = WarpTextStyle(
                    color = WarpColor("#B0BEC5"),
                    fontSize = 14f,
                    fontWeight = WarpFontWeight.Medium,
                ),
                maxLines = 1,
            )
            WarpRow(
                modifier = WarpModifier
                    .fillMaxWidth()
                    .background("#243447")
                    .cornerRadius(12)
                    .padding(8),
                verticalAlignment = WarpVerticalAlignment.Center,
            ) {
                WarpButton(
                    text = "−",
                    onClick = CounterActions.Decrement.asClickAction(),
                    modifier = WarpModifier
                        .size(40)
                        .cornerRadius(20),
                    style = WarpTextStyle(
                        fontSize = 18f,
                        fontWeight = WarpFontWeight.Bold,
                    ),
                    colors = WarpButtonColors.of(
                        backgroundColor = "#E74C3C",
                        contentColor = "#FFFFFF",
                    ),
                )
                // weight = take leftover space; text stays Start (Glance default) — no textAlign.
                WarpText(
                    text = count.toString(),
                    modifier = WarpModifier
                        .weight()
                        .padding(horizontal = 8, vertical = 0),
                    style = WarpTextStyle(
                        color = WarpColor("#FFFFFF"),
                        fontSize = 22f,
                        fontWeight = WarpFontWeight.Bold,
                    ),
                    maxLines = 1,
                )
                WarpButton(
                    text = "+",
                    onClick = CounterActions.Increment.asClickAction(),
                    modifier = WarpModifier
                        .size(40)
                        .cornerRadius(20),
                    style = WarpTextStyle(
                        fontSize = 18f,
                        fontWeight = WarpFontWeight.Bold,
                    ),
                    colors = WarpButtonColors.of(
                        backgroundColor = "#27AE60",
                        contentColor = "#FFFFFF",
                    ),
                )
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
        updateWarpWidgetState(session.context, CounterWarpWidget) {
            val cur = this[CounterKeys.Count] ?: 0
            this[CounterKeys.Count] = when (actionId) {
                CounterActions.Increment -> cur + 1
                CounterActions.Decrement -> cur - 1
                CounterActions.Reset -> 0
            }
        }
    }
}
