package com.atriidev.warp_ui.glance.internal

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.atriidev.warp_ui.WarpClicksRegistry

internal class WarpRegistryActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val actionId = parameters[WarpGlanceActionKeys.ActionId] ?: return
        val actionParameters = WarpGlanceActionKeys.decodeParameters(
            parameters[WarpGlanceActionKeys.ParametersJson],
        )
        if (!WarpClicksRegistry.hasHandler(actionId)) {
            // Cold start: process may never have run WarpRender → registry empty.
            WarpGlanceClickPrepare.prepareIfNeeded(context.applicationContext, glanceId)
        }
        try {
            WarpGlanceUpdateScope.targetGlanceId = glanceId
            WarpClicksRegistry.dispatch(actionId, actionParameters)
        } finally {
            WarpGlanceUpdateScope.targetGlanceId = null
        }
    }
}
