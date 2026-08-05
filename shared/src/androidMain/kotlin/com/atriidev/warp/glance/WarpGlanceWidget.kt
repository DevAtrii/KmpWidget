package com.atriidev.warp.glance

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.atriidev.kmpwidget.KmpDataStore
import com.atriidev.warp.dsl.WarpWidgetDefinition
import com.atriidev.warp.ir.WarpState

class WarpGlanceWidget(
    private val definition: WarpWidgetDefinition,
    private val stateKey: String,
) : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        seedStateFromStoreIfNeeded(context, id)

        provideContent {
            val glanceState = currentState<Preferences>()
            val preferenceKey = stringPreferencesKey(stateKey)
            val warpState = WarpState(
                values = mapOf(
                    stateKey to (glanceState[preferenceKey] ?: "0"),
                ),
            )
            val document = definition.build(warpState)
            GlanceRenderer.RenderNode(document.root, warpState)
        }
    }

    private suspend fun seedStateFromStoreIfNeeded(
        context: Context,
        id: GlanceId,
    ) {
        val store = KmpDataStore(context)
        val storedValue = store.get(stateKey, "0")
        val preferenceKey = stringPreferencesKey(stateKey)
        updateAppWidgetState(context, id) { prefs ->
            if (prefs[preferenceKey] == null) {
                prefs[preferenceKey] = storedValue
            }
        }
    }
}
