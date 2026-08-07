package com.atriidev.kmpwidget

import androidx.compose.runtime.Composable
import com.atriidev.warp_runtime.compose.WarpBox
import com.atriidev.warp_runtime.compose.WarpButton
import com.atriidev.warp_runtime.compose.WarpColumn
import com.atriidev.warp_runtime.compose.WarpDivider
import com.atriidev.warp_runtime.compose.WarpProgressIndicator
import com.atriidev.warp_runtime.compose.WarpRow
import com.atriidev.warp_runtime.compose.WarpSpacer
import com.atriidev.warp_runtime.compose.WarpText
import com.atriidev.warp_runtime.example.counter.CounterActions
import com.atriidev.warp_runtime.nodes.actions.asClickAction
import com.atriidev.warp_runtime.nodes.modifiers.WarpColor
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.style.WarpButtonColors
import com.atriidev.warp_runtime.nodes.style.WarpContentAlignment
import com.atriidev.warp_runtime.nodes.style.WarpFontWeight
import com.atriidev.warp_runtime.nodes.style.WarpProgressIndicatorStyle
import com.atriidev.warp_runtime.nodes.style.WarpTextStyle
import com.atriidev.warp_runtime.nodes.style.WarpVerticalAlignment
import com.atriidev.warp_ui.WarpClickHandler
import com.atriidev.warp_widget.WarpWidget
import com.atriidev.warp_widget.WarpWidgetSession
import com.atriidev.warp_widget.api.WidgetEnvironment
import com.atriidev.warp_widget.updateWarpWidgetState
import kotlin.math.abs
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

/** Serializable state for [CounterWarpWidget] — persisted as JSON under prefs key = widget id. */
@Serializable
data class CounterState(
    val count: Int = 0,
)

/**
 * Shared counter [WarpWidget] — one definition for Glance + WidgetKit.
 *
 * Demo layout also uses [WarpBox], [WarpSpacer], [WarpDivider], [WarpProgressIndicator].
 */
object CounterWarpWidget : WarpWidget<CounterState>(CounterState.serializer()) {
    override val id: String = "CounterWidget"

    /** App Group suite — keep in sync with Xcode entitlements / [APP_GROUP_ID]. */
    override val iosGroupId: String = APP_GROUP_ID

    override val defaultState: CounterState = CounterState()

    @Composable
    override fun Content(env: WidgetEnvironment, state: CounterState) {
        println("WIDGET_ENV $env")
        val count = state.count
        // 0..99 cycle for the linear bar (Glance determinate progress is 0f..1f).
        val progress = (abs(count) % 100) / 100f

        WarpBox(
            modifier = WarpModifier
                .fillMaxSize()
                .background("#1B2838")
                .cornerRadius(16)
                .padding(12),
            contentAlignment = WarpContentAlignment.TopStart,
        ) {
            WarpColumn(
                modifier = WarpModifier.fillMaxWidth(),
            ) {
                WarpText(
                    text = "Count",
                    modifier = WarpModifier
                        .fillMaxWidth()
                        .clickable(CounterActions.Reset.asClickAction()),
                    style = WarpTextStyle(
                        color = WarpColor("#B0BEC5"),
                        fontSize = 14f,
                        fontWeight = WarpFontWeight.Medium,
                    ),
                    maxLines = 1,
                )

                WarpSpacer(modifier = WarpModifier.height(6))

                WarpDivider(
                    modifier = WarpModifier.fillMaxWidth(),
                    thickness = 1,
                    color = WarpColor("#33FFFFFF"),
                )

                WarpSpacer(modifier = WarpModifier.height(8))

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

                WarpSpacer(modifier = WarpModifier.height(10))

                WarpProgressIndicator(
                    modifier = WarpModifier.fillMaxWidth(),
                    style = WarpProgressIndicatorStyle.Linear,
                    progress = progress,
                    color = WarpColor("#4FC3F7"),
                    backgroundColor = WarpColor("#243447"),
                )
            }
        }
    }

    override fun clickHandlers(session: WarpWidgetSession): List<WarpClickHandler<*>> =
        listOf(CounterWarpClickHandler(session))
}

/**
 * Persists [CounterState] via [updateWarpWidgetState] (Glance prefs / UserDefaults + reload).
 */
class CounterWarpClickHandler(
    private val session: WarpWidgetSession,
) : WarpClickHandler<CounterActions>(CounterActions::class, CounterActions.entries) {

    override suspend fun onClick(actionId: CounterActions, parameters: Map<String, String>) {
        println("WARP_CLICK: id=$actionId, params=$parameters")
        updateWarpWidgetState(session.context, CounterWarpWidget) { state ->
            state.copy(
                count = when (actionId) {
                    CounterActions.Increment -> state.count + 1
                    CounterActions.Decrement -> state.count - 1
                    CounterActions.Reset -> 0
                },
            )
        }
    }
}
