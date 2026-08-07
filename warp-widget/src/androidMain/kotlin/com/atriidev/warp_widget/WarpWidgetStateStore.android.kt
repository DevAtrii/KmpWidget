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
import com.atriidev.warp_widget.api.PlatformContext

private const val TAG = "WarpWidgetStateStore"

/**
 * Android: Glance `PreferencesGlanceStateDefinition` via
 * `getAppWidgetState` / `updateAppWidgetState` + `GlanceAppWidget.update`.
 *
 * **Shared state:** WARP prefs are mirrored to every active Glance instance of a widget
 * kind (same model as iOS App Group). Per-instance Glance keys (`__warp_*` layout/theme)
 * stay local.
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
        val glanceIds = activeGlanceIds(android, widget)
        if (glanceIds.isEmpty()) {
            Log.d(TAG, "read($widgetId): no Glance ids")
            return WarpWidgetPreferences()
        }
        // Shared snapshot — any active instance; all should hold the same WARP keys.
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
        val glanceIds = activeGlanceIds(android, widget)
        if (glanceIds.isEmpty()) {
            Log.w(TAG, "update($widgetId): no Glance ids — register receiver / add widget")
            return
        }
        // Transform once from a canonical snapshot, then mirror to every instance.
        // Per-id transform would diverge (e.g. increment on stale copies).
        val canonical = getAppWidgetState(
            android,
            PreferencesGlanceStateDefinition,
            glanceIds.first(),
        ).toWarpPreferences()
        val mutable = MutableWarpWidgetPreferences(canonical.values)
        mutable.transform()
        val next = mutable.toPreferences()

        glanceIds.forEach { glanceId ->
            try {
                updateAppWidgetState(android, glanceId) { prefs ->
                    val beforeKeys = prefs.asMap().keys.map { it.name }.toSet()
                    prefs.applyWarpPreferences(beforeKeys, next)
                }
            } catch (e: Exception) {
                Log.e(TAG, "update($widgetId) failed for $glanceId", e)
            }
        }
        Log.d(TAG, "update($widgetId): mirrored to ${glanceIds.size} instance(s)")
    }

    actual suspend fun refreshAfterUpdate(
        context: PlatformContext,
        widgetId: String,
    ) {
        val android = context.context
        val widget = requireWidget(widgetId) ?: return
        val glanceIds = activeGlanceIds(android, widget)
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
        val glanceIds = activeGlanceIds(android, widget)
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

    /** Active launcher instances only (drops orphan Glance ids). */
    private suspend fun activeGlanceIds(context: Context, widget: GlanceAppWidget): List<GlanceId> {
        val manager = GlanceAppWidgetManager(context)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        return manager.getGlanceIds(widget.javaClass).filter { glanceId ->
            val appWidgetId = manager.getAppWidgetId(glanceId)
            appWidgetManager.getAppWidgetInfo(appWidgetId) != null
        }
    }
}
