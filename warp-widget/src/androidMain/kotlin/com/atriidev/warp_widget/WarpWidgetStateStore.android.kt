package com.atriidev.warp_widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.atriidev.warp_ui.glance.internal.WarpGlanceUpdateScope
import com.atriidev.warp_widget.api.PlatformContext

private const val TAG = "WarpWidgetStateStore"

/**
 * Android: Glance `PreferencesGlanceStateDefinition` via
 * `getAppWidgetState` / `updateAppWidgetState` + `GlanceAppWidget.update`.
 *
 * Register widgets with [WarpWidgetAndroidRegistry] before update/reload.
 * Inside Glance `provideContent`, prefer passing `currentState<Preferences>().toWarpPreferences()`
 * into [WarpWidgetSession.preferences] instead of [read].
 */
actual object WarpWidgetStateStore {
    actual suspend fun read(
        context: PlatformContext,
        widgetId: String,
    ): WarpWidgetPreferences {
        val android = context.context
        val widget = requireWidget(widgetId) ?: return WarpWidgetPreferences()
        val glanceIds = resolveGlanceIds(android, widget)
        if (glanceIds.isEmpty()) {
            Log.d(TAG, "read($widgetId): no Glance ids")
            return WarpWidgetPreferences()
        }
        // Per-instance state; first id is the app-wide “current” snapshot.
        val prefs = getAppWidgetState(
            android,
            PreferencesGlanceStateDefinition,
            glanceIds.first(),
        )
        return prefs.toWarpPreferences()
    }

    actual suspend fun update(
        context: PlatformContext,
        widgetId: String,
        transform: MutableWarpWidgetPreferences.() -> Unit,
    ) {
        val android = context.context
        val widget = requireWidget(widgetId) ?: return
        val glanceIds = resolveGlanceIds(android, widget)
        if (glanceIds.isEmpty()) {
            Log.w(TAG, "update($widgetId): no Glance ids — register receiver / add widget")
            return
        }
        glanceIds.forEach { glanceId ->
            try {
                updateAppWidgetState(android, glanceId) { prefs ->
                    val beforeKeys = prefs.asMap().keys.map { it.name }.toSet()
                    val mutable = MutableWarpWidgetPreferences(prefs.toWarpPreferences().values)
                    mutable.transform()
                    prefs.applyWarpPreferences(beforeKeys, mutable.toPreferences())
                }
            } catch (e: Exception) {
                Log.e(TAG, "update($widgetId) failed for $glanceId", e)
            }
        }
    }

    actual suspend fun refreshAfterUpdate(
        context: PlatformContext,
        widgetId: String,
    ) {
        val android = context.context
        val widget = requireWidget(widgetId) ?: return
        val glanceIds = resolveGlanceIds(android, widget)
        if (glanceIds.isEmpty()) {
            Log.w(TAG, "refreshAfterUpdate($widgetId): no Glance ids")
            return
        }
        glanceIds.forEach { glanceId ->
            try {
                widget.update(android, glanceId)
            } catch (e: Exception) {
                Log.e(TAG, "refreshAfterUpdate($widgetId) failed for $glanceId", e)
            }
        }
        Log.d(TAG, "refreshAfterUpdate($widgetId): ${glanceIds.size} instance(s)")
    }

    actual suspend fun reload(
        context: PlatformContext,
        widgetId: String,
    ) {
        val android = context.context
        val widget = requireWidget(widgetId) ?: return
        val glanceIds = resolveGlanceIds(android, widget)
        if (glanceIds.isEmpty()) {
            Log.w(TAG, "reload($widgetId): no Glance ids")
            return
        }
        glanceIds.forEach { glanceId ->
            try {
                // Glance keeps a long-lived session with a stale Context; bare update() does not
                // recompose when prefs are unchanged. Touch internal theme keys first (same effect
                // as a user click that mutates state).
                GlanceInternalState.touchTheme(android, glanceId)
                widget.update(android, glanceId)
            } catch (e: Exception) {
                Log.e(TAG, "reload($widgetId) failed for $glanceId", e)
            }
        }
        Log.d(TAG, "reload($widgetId): ${glanceIds.size} instance(s)")
    }

    private fun requireWidget(widgetId: String): GlanceAppWidget? {
        val widget = WarpWidgetAndroidRegistry.create(widgetId)
        if (widget == null) {
            Log.e(
                TAG,
                "No GlanceAppWidget registered for id='$widgetId'. " +
                    "Call WarpWidgetAndroidRegistry.register(\"$widgetId\") { YourGlanceWidget() }",
            )
        }
        return widget
    }

    /**
     * Tap path → single [GlanceId] from [WarpGlanceUpdateScope].
     * App path → only Glance ids still bound in [AppWidgetManager] (drops stale orphans).
     */
    private suspend fun resolveGlanceIds(context: Context, widget: GlanceAppWidget): List<GlanceId> {
        WarpGlanceUpdateScope.targetGlanceId?.let { return listOf(it) }
        val manager = GlanceAppWidgetManager(context)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        return manager.getGlanceIds(widget.javaClass).filter { glanceId ->
            val appWidgetId = manager.getAppWidgetId(glanceId)
            appWidgetManager.getAppWidgetInfo(appWidgetId) != null
        }
    }
}
