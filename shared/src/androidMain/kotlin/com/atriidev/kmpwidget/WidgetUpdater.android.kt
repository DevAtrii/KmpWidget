package com.atriidev.kmpwidget

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState

actual class WidgetUpdater(
    private val context: Context,
) {
    companion object {
        private const val TAG = "WidgetUpdater"
    }

    actual suspend fun update(counter: Int) {
        val manager = GlanceAppWidgetManager(context)
        val widget = CounterWidget()

        val glanceIds = manager.getGlanceIds(widget.javaClass)

        Log.d(TAG, "Found ${glanceIds.size} widget(s)")

        glanceIds.forEachIndexed { index, glanceId ->
            try {
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[stringPreferencesKey(COUNTER_KEY)] = "$counter"
                }
                widget.update(context, glanceId)
                Log.d(TAG, "Successfully updated widget #$index")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update widget #$index", e)
            }
        }
    }
}