package com.atriidev.warp_widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.atriidev.warp_ui.WarpClicksRegistry
import com.atriidev.warp_ui.setWarpGlanceClickPrepareHandler
import com.atriidev.warp_widget.api.PlatformContext
import com.atriidev.warp_widget.api.WarpWidgetFamily
import com.atriidev.warp_widget.api.makeWidgetEnvironment

private const val TAG = "WarpWidgetAndroidRegistry"

/**
 * Phone book: WARP [WarpWidget.id] → shared [WarpWidget] + Glance `GlanceAppWidget` factory.
 *
 * Shared code updates widgets by string id. Glance needs a concrete
 * [androidx.glance.appwidget.GlanceAppWidget] to read/write preferences and call `update`.
 *
 * ### Cold-start clicks
 * When the app process is dead, Glance delivers taps into a fresh process where
 * [com.atriidev.warp_ui.WarpRender] never ran — [com.atriidev.warp_ui.WarpClicksRegistry]
 * is empty. [WarpWidgetAndroidInitProvider] (merged from this library) installs a prepare
 * hook that:
 * 1. Instantiates the Glance `AppWidgetProvider` for this [GlanceId] (runs receiver `init`
 *    → your [register] call)
 * 2. Re-binds click handlers before dispatch
 *
 * Prefer [WarpGlanceWidgetReceiver] + [WarpGlanceWidget] — they call [register] for you.
 *
 * No app ContentProvider / Application hook required.
 */
object WarpWidgetAndroidRegistry {
    private data class Entry(
        val widget: WarpWidget?,
        val factory: () -> GlanceAppWidget,
    )

    private val entries = mutableMapOf<String, Entry>()

    @Volatile
    private var prepareInstalled = false

    /**
     * Register [widget] + Glance factory for [widgetId].
     *
     * Call from [androidx.glance.appwidget.GlanceAppWidgetReceiver] `init` (preferred)
     * so cold-start prepare can wake the receiver and pick up this registration.
     */
    fun register(
        widgetId: String,
        widget: WarpWidget,
        factory: () -> GlanceAppWidget,
    ) {
        entries[widgetId] = Entry(widget = widget, factory = factory)
        installColdStartPrepare()
    }

    /**
     * Glance-only registration (state update/reload). Prefer [register] with [WarpWidget]
     * so cold-start clicks can re-bind handlers.
     */
    @Deprecated(
        message = "Pass WarpWidget so cold-start Glance clicks can re-register handlers",
        replaceWith = ReplaceWith("register(widgetId, widget, factory)"),
    )
    fun register(widgetId: String, factory: () -> GlanceAppWidget) {
        val previous = entries[widgetId]
        entries[widgetId] = Entry(widget = previous?.widget, factory = factory)
        if (previous?.widget == null) {
            Log.w(
                TAG,
                "register($widgetId) without WarpWidget — cold-start clicks will no-op. " +
                    "Use register(widgetId, widget) { … }",
            )
        }
        installColdStartPrepare()
    }

    fun unregister(widgetId: String) {
        entries.remove(widgetId)
    }

    fun clear() {
        entries.clear()
    }

    /**
     * Wire Glance [androidx.glance.appwidget.action.ActionCallback] → cold-start reprepare.
     * Idempotent; also invoked by [WarpWidgetAndroidInitProvider].
     */
    fun installColdStartPrepare() {
        if (prepareInstalled) return
        prepareInstalled = true
        setWarpGlanceClickPrepareHandler { context, glanceId ->
            reprepareForGlanceId(context, glanceId)
        }
    }

    internal fun create(widgetId: String): GlanceAppWidget? =
        entries[widgetId]?.factory?.invoke()

    internal fun isRegistered(widgetId: String): Boolean =
        entries.containsKey(widgetId)

    /**
     * Re-bind [WarpClicksRegistry] for the WARP widget that owns [glanceId].
     */
    private suspend fun reprepareForGlanceId(context: Context, glanceId: GlanceId) {
        // ActionCallback does not construct GlanceAppWidgetReceiver — wake it so init { register } runs.
        wakeGlanceReceiver(context, glanceId)

        val entry = findEntryForGlanceId(context, glanceId)
        val widget = entry?.widget
        if (entry == null || widget == null) {
            Log.e(
                TAG,
                "Cold-start click: no WarpWidget registered for glanceId=$glanceId. " +
                    "Call WarpWidgetAndroidRegistry.register(id, widget) { GlanceAppWidget() } " +
                    "from GlanceAppWidgetReceiver.init.",
            )
            return
        }
        val platformContext = PlatformContext(context.applicationContext)
        val session = WarpWidgetSession(
            context = platformContext,
            environment = makeWidgetEnvironment(
                platformContext = platformContext,
                family = WarpWidgetFamily.SYSTEM_SMALL,
                isPreview = false,
            ),
        )
        WarpClicksRegistry.register(widget.clickHandlers(session))
        Log.d(TAG, "Cold-start prepare: handlers for widgetId=${widget.id}")
    }

    /**
     * Construct the manifest [android.appwidget.AppWidgetProvider] for [glanceId].
     * Receiver `init` blocks typically call [register].
     */
    private fun wakeGlanceReceiver(context: Context, glanceId: GlanceId) {
        try {
            val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
            val info = AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId)
            if (info == null) {
                Log.w(TAG, "wakeGlanceReceiver: no AppWidgetInfo for id=$appWidgetId")
                return
            }
            val className = info.provider.className
            val clazz = Class.forName(className, true, context.classLoader)
            val instance = clazz.getDeclaredConstructor().newInstance()
            (instance as? WarpGlanceWidgetReceiver)?.ensureRegistered()
            Log.d(TAG, "Woke Glance receiver $className")
        } catch (e: Exception) {
            Log.w(TAG, "wakeGlanceReceiver failed", e)
        }
    }

    private suspend fun findEntryForGlanceId(context: Context, glanceId: GlanceId): Entry? {
        val manager = GlanceAppWidgetManager(context)
        for (entry in entries.values) {
            val glanceWidget = entry.factory()
            val ids = manager.getGlanceIds(glanceWidget.javaClass)
            if (ids.any { it == glanceId }) return entry
        }
        // Single registered widget → still prepare (GlanceId match can fail on some paths).
        if (entries.size == 1) return entries.values.first()
        return null
    }
}
