package com.atriidev.kmpwidget

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.atriidev.warp.glance.CounterWarpGlanceHost

actual class WidgetUpdater(
    private val context: Context,
) {
    companion object {
        private const val TAG = "WidgetUpdater"
    }

    actual suspend fun update(counter: Int) {
        KmpDataStore(context.applicationContext).set(COUNTER_KEY, counter.toString())

        val manager = GlanceAppWidgetManager(context.applicationContext)
        val widget = CounterWarpGlanceHost.instance
        val glanceIds = manager.getGlanceIds(widget.javaClass)

        Log.d(TAG, "Found ${glanceIds.size} widget(s)")

        glanceIds.forEachIndexed { index, glanceId ->
            try {
                updateAppWidgetState(context.applicationContext, glanceId) { prefs ->
                    prefs[stringPreferencesKey(COUNTER_KEY)] = counter.toString()
                }
                widget.update(context.applicationContext, glanceId)
                Log.d(TAG, "Successfully updated widget #$index")
            } catch (exception: Exception) {
                Log.e(TAG, "Failed to update widget #$index", exception)
            }
        }
    }
}
