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
 * | [WarpWidgetStateScope] | update | refresh / reload |
 * |---|---|---|
 * | Shared | transform once → mirror WARP keys to all active GlanceIds | all active ids |
 * | Instance | read/write single resolved GlanceId | that id only |
 *
 * Register widgets with [WarpWidgetAndroidRegistry] before update/reload.
 */
actual object WarpWidgetStateStore {
    actual suspend fun read(
        context: PlatformContext,
        widget: WarpWidgetHostApi,
        id: WarpWidgetId,
    ): WarpWidgetPreferences {
        val android = context.context
        val glanceWidget = requireWidget(widget.id) ?: return WarpWidgetPreferences()
        return when (widget.stateScopeOrShared()) {
            WarpWidgetStateScope.Shared -> readShared(android, glanceWidget)
            WarpWidgetStateScope.Instance ->
                readGlanceId(android, resolveGlanceId(android, glanceWidget, id))
        }
    }

    actual suspend fun read(
        context: PlatformContext,
        widgetId: String,
    ): WarpWidgetPreferences {
        val android = context.context
        val widget = requireWidget(widgetId) ?: return WarpWidgetPreferences()
        return readShared(android, widget)
    }

    actual suspend fun update(
        context: PlatformContext,
        widget: WarpWidgetHostApi,
        id: WarpWidgetId,
        transform: MutableWarpWidgetPreferences.() -> Unit,
    ) {
        val android = context.context
        val glanceWidget = requireWidget(widget.id) ?: return
        when (widget.stateScopeOrShared()) {
            WarpWidgetStateScope.Shared -> updateShared(android, widget.id, glanceWidget, transform)
            WarpWidgetStateScope.Instance ->
                updateSingle(android, widget.id, resolveGlanceId(android, glanceWidget, id), transform)
        }
    }

    actual suspend fun update(
        context: PlatformContext,
        widgetId: String,
        transform: MutableWarpWidgetPreferences.() -> Unit,
    ) {
        val android = context.context
        val glanceWidget = requireWidget(widgetId) ?: return
        updateShared(android, widgetId, glanceWidget, transform)
    }

    actual suspend fun refreshAfterUpdate(
        context: PlatformContext,
        widget: WarpWidgetHostApi,
        id: WarpWidgetId,
    ) {
        val android = context.context
        val glanceWidget = requireWidget(widget.id) ?: return
        val glanceIds = targetGlanceIds(android, glanceWidget, widget, id)
        if (glanceIds.isEmpty()) {
            Log.w(TAG, "refreshAfterUpdate(${widget.id}): no Glance ids")
            return
        }
        glanceIds.forEach { glanceId ->
            try {
                glanceWidget.update(android, glanceId)
            } catch (e: Exception) {
                Log.e(TAG, "refreshAfterUpdate(${widget.id}) failed for $glanceId", e)
            }
        }
        Log.d(TAG, "refreshAfterUpdate(${widget.id}): ${glanceIds.size} instance(s)")
    }

    actual suspend fun refreshAfterUpdate(
        context: PlatformContext,
        widgetId: String,
    ) {
        val android = context.context
        val glanceWidget = requireWidget(widgetId) ?: return
        val glanceIds = activeGlanceIds(android, glanceWidget)
        refreshGlanceIds(android, glanceWidget, widgetId, glanceIds, touchTheme = false)
    }

    actual suspend fun reload(
        context: PlatformContext,
        widget: WarpWidgetHostApi,
        id: WarpWidgetId,
    ) {
        val android = context.context
        val glanceWidget = requireWidget(widget.id) ?: return
        val glanceIds = targetGlanceIds(android, glanceWidget, widget, id)
        refreshGlanceIds(android, glanceWidget, widget.id, glanceIds, touchTheme = true)
    }

    actual suspend fun reload(
        context: PlatformContext,
        widgetId: String,
    ) {
        val android = context.context
        val glanceWidget = requireWidget(widgetId) ?: return
        val glanceIds = activeGlanceIds(android, glanceWidget)
        refreshGlanceIds(android, glanceWidget, widgetId, glanceIds, touchTheme = true)
    }

    actual suspend fun listWarpWidgetIds(
        context: PlatformContext,
        widget: WarpWidgetHostApi,
    ): List<WarpWidgetId> {
        val android = context.context
        val glanceWidget = requireWidget(widget.id) ?: return emptyList()
        return activeGlanceIds(android, glanceWidget).map { glanceId ->
            WarpWidgetId.fromGlanceId(android, glanceId)
        }
    }

    private suspend fun readShared(context: Context, widget: GlanceAppWidget): WarpWidgetPreferences {
        val glanceIds = activeGlanceIds(context, widget)
        if (glanceIds.isEmpty()) {
            Log.d(TAG, "read(shared): no Glance ids")
            return WarpWidgetPreferences()
        }
        return readGlanceId(context, glanceIds.first())
    }

    private suspend fun readGlanceId(context: Context, glanceId: GlanceId): WarpWidgetPreferences {
        val prefs = getAppWidgetState(
            context,
            PreferencesGlanceStateDefinition,
            glanceId,
        )
        return prefs.toWarpPreferences()
    }

    private suspend fun updateShared(
        context: Context,
        widgetId: String,
        widget: GlanceAppWidget,
        transform: MutableWarpWidgetPreferences.() -> Unit,
    ) {
        val glanceIds = activeGlanceIds(context, widget)
        if (glanceIds.isEmpty()) {
            Log.w(TAG, "update($widgetId): no Glance ids — register receiver / add widget")
            return
        }
        val canonical = readGlanceId(context, glanceIds.first())
        val mutable = MutableWarpWidgetPreferences(canonical.values)
        mutable.transform()
        val next = mutable.toPreferences()
        glanceIds.forEach { glanceId ->
            try {
                updateAppWidgetState(context, glanceId) { prefs ->
                    val beforeKeys = prefs.asMap().keys.map { it.name }.toSet()
                    prefs.applyWarpPreferences(beforeKeys, next)
                }
            } catch (e: Exception) {
                Log.e(TAG, "update($widgetId) failed for $glanceId", e)
            }
        }
        Log.d(TAG, "update($widgetId): mirrored to ${glanceIds.size} instance(s)")
    }

    private suspend fun updateSingle(
        context: Context,
        widgetId: String,
        glanceId: GlanceId,
        transform: MutableWarpWidgetPreferences.() -> Unit,
    ) {
        try {
            updateAppWidgetState(context, glanceId) { prefs ->
                val beforeKeys = prefs.asMap().keys.map { it.name }.toSet()
                val mutable = MutableWarpWidgetPreferences(prefs.toWarpPreferences().values)
                mutable.transform()
                val next = mutable.toPreferences()
                prefs.applyWarpPreferences(beforeKeys, next)
            }
            Log.d(TAG, "update($widgetId): single instance $glanceId")
        } catch (e: Exception) {
            Log.e(TAG, "update($widgetId) failed for $glanceId", e)
        }
    }

    private suspend fun refreshGlanceIds(
        context: Context,
        widget: GlanceAppWidget,
        widgetId: String,
        glanceIds: List<GlanceId>,
        touchTheme: Boolean,
    ) {
        if (glanceIds.isEmpty()) {
            Log.w(TAG, "reload($widgetId): no Glance ids")
            return
        }
        glanceIds.forEach { glanceId ->
            try {
                if (touchTheme) {
                    GlanceInternalState.touchTheme(context, glanceId)
                }
                widget.update(context, glanceId)
            } catch (e: Exception) {
                Log.e(TAG, "reload($widgetId) failed for $glanceId", e)
            }
        }
        Log.d(TAG, "reload($widgetId): ${glanceIds.size} instance(s)")
    }

    private suspend fun targetGlanceIds(
        context: Context,
        widget: GlanceAppWidget,
        host: WarpWidgetHostApi,
        id: WarpWidgetId,
    ): List<GlanceId> = when (host.stateScopeOrShared()) {
        WarpWidgetStateScope.Shared -> activeGlanceIds(context, widget)
        WarpWidgetStateScope.Instance -> listOf(resolveGlanceId(context, widget, id))
    }

    private suspend fun resolveGlanceId(
        context: Context,
        widget: GlanceAppWidget,
        id: WarpWidgetId,
    ): GlanceId {
        val appWidgetId = id.toAppWidgetIdOrNull()
            ?: throw IllegalArgumentException("Android instance WarpWidgetId must be aw:…, got '$id'")
        val manager = GlanceAppWidgetManager(context)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        return activeGlanceIds(context, widget).firstOrNull { glanceId ->
            manager.getAppWidgetId(glanceId) == appWidgetId
        } ?: throw IllegalArgumentException(
            "No active Glance instance for appWidgetId=$appWidgetId (WarpWidgetId=$id)",
        )
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
