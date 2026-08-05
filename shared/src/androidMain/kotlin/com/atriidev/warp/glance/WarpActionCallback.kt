package com.atriidev.warp.glance

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import com.atriidev.kmpwidget.COUNTER_KEY
import com.atriidev.kmpwidget.KmpDataStore
import com.atriidev.warp.actions.CounterActionHandler
import com.atriidev.warp.ir.WarpState
import com.atriidev.warp.widgets.CounterWarpWidget
import com.atriidev.warp.widgets.WaterIntakeWarpWidget

class WarpActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val actionId = parameters[ActionIdKey] ?: return
        val stateKey = parameters[StateKeyKey] ?: COUNTER_KEY
        val widgetKind = parameters[WidgetKindKey] ?: CounterWarpWidget.KIND

        val store = KmpDataStore(context)
        val current = WarpState(mapOf(stateKey to store.get(stateKey, "0")))
        val updated = CounterActionHandler.handle(actionId, stateKey, current)
        val value = updated.get(stateKey, "0")

        store.set(stateKey, value)
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[stringPreferencesKey(stateKey)] = value
        }

        hostFor(widgetKind).update(context, glanceId)
    }

    private fun hostFor(widgetKind: String): WarpGlanceWidget = when (widgetKind) {
        WaterIntakeWarpWidget.KIND -> WaterIntakeWarpGlanceHost.instance
        else -> CounterWarpGlanceHost.instance
    }

    companion object {
        val ActionIdKey = ActionParameters.Key<String>("warp_action_id")
        val ActionUrlKey = ActionParameters.Key<String>("warp_action_url")
        val StateKeyKey = ActionParameters.Key<String>("warp_state_key")
        val WidgetKindKey = ActionParameters.Key<String>("warp_widget_kind")
    }
}

object CounterWarpGlanceHost {
    val instance = WarpGlanceWidget(CounterWarpWidget, COUNTER_KEY)
}

object WaterIntakeWarpGlanceHost {
    val instance = WarpGlanceWidget(WaterIntakeWarpWidget, com.atriidev.kmpwidget.WATER_INTAKE_KEY)
}
