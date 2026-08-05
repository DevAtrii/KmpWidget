package com.atriidev.warp.widgets

import com.atriidev.kmpwidget.WATER_INTAKE_KEY
import com.atriidev.warp.actions.CounterActions
import com.atriidev.warp.dsl.WarpWidgetDefinition
import com.atriidev.warp.dsl.WarpWidgetScope
import com.atriidev.warp.dsl.actionRunCallback
import com.atriidev.warp.ir.WarpColor
import com.atriidev.warp.ir.WarpHorizontalAlignment
import com.atriidev.warp.ir.WarpModifier
import com.atriidev.warp.ir.WarpVerticalAlignment

object WaterIntakeWarpWidget : WarpWidgetDefinition(kind = "WaterIntakeWidget") {
    const val KIND = "WaterIntakeWidget"

    private val actionPayload = mapOf(
        "stateKey" to WATER_INTAKE_KEY,
        "widgetKind" to KIND,
    )

    override fun provideContent(scope: WarpWidgetScope) {
        scope.row(
            modifier = WarpModifier()
                .background(WarpColor(argb = 0xFF2196F3))
                .padding(all = 16),
            verticalAlignment = WarpVerticalAlignment.CenterVertically,
            horizontalAlignment = WarpHorizontalAlignment.CenterHorizontally,
        ) {
            button("-", actionRunCallback(CounterActions.DECREMENT, actionPayload))
            text(stateKey = WATER_INTAKE_KEY, modifier = WarpModifier().defaultWeight())
            button("+", actionRunCallback(CounterActions.INCREMENT, actionPayload))
        }
    }
}
