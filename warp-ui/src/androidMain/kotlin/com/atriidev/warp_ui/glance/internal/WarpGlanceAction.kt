package com.atriidev.warp_ui.glance.internal

import androidx.glance.action.Action
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import com.atriidev.warp_runtime.nodes.actions.ClickAction

@PublishedApi
internal fun clickActionFor(
    handlerClass: Class<out ActionCallback>,
    action: ClickAction,
): Action = actionRunCallback(
    handlerClass,
    WarpGlanceActionKeys.actionParametersOf(
        actionId = action.actionId,
        parameters = action.parameters,
    ),
)
