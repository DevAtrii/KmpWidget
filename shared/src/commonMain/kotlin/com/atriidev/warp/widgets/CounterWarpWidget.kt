package com.atriidev.warp.widgets

import com.atriidev.kmpwidget.COUNTER_KEY
import com.atriidev.warp.actions.CounterActions
import com.atriidev.warp.dsl.WarpWidgetDefinition
import com.atriidev.warp.dsl.WarpWidgetScope
import com.atriidev.warp.dsl.actionRunCallback
import com.atriidev.warp.ir.WarpColor
import com.atriidev.warp.ir.WarpHorizontalAlignment
import com.atriidev.warp.ir.WarpModifier
import com.atriidev.warp.ir.WarpVerticalAlignment

object CounterWarpWidget : WarpWidgetDefinition(kind = "CounterWidget") {
    const val KIND = "CounterWidget"

    override fun provideContent(scope: WarpWidgetScope) {
        scope.row(
            modifier = WarpModifier()
                .background(WarpColor(argb = 0xFF00FF00))
                .padding(all = 16),
            verticalAlignment = WarpVerticalAlignment.CenterVertically,
            horizontalAlignment = WarpHorizontalAlignment.CenterHorizontally,
        ) {
            button("-", actionRunCallback(CounterActions.DECREMENT, counterActionPayload))
            text(stateKey = COUNTER_KEY, modifier = WarpModifier().defaultWeight())
            button("+", actionRunCallback(CounterActions.INCREMENT, counterActionPayload))
        }
    }

    private val counterActionPayload = mapOf(
        "stateKey" to COUNTER_KEY,
        "widgetKind" to KIND,
    )
}
