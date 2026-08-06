package com.atriidev.warp_widget

import com.atriidev.warp_widget.api.PlatformContext

/**
 * Platform persistence for widget prefs + timeline / Glance reload.
 *
 * | Platform | Read / write | Reload UI |
 * |----------|--------------|-----------|
 * | Android  | Glance `PreferencesGlanceStateDefinition` via `getAppWidgetState` / `updateAppWidgetState` | `GlanceAppWidget.update` |
 * | iOS      | App Group `UserDefaults(suiteName:)` | `WidgetCenter.reloadTimelines(ofKind:)` |
 *
 * Hosts and apps must not talk to Glance / UserDefaults directly — use [read],
 * [updateWarpWidgetState], [reloadWarpWidget].
 *
 * **Android:** register each widget id with [WarpWidgetAndroidRegistry] before update/reload.
 */
expect object WarpWidgetStateStore {
    /**
     * Load prefs for [widgetId].
     *
     * - **Android:** first Glance id’s preferences (register via [WarpWidgetAndroidRegistry])
     * - **iOS:** App Group UserDefaults keys `"$widgetId.*"`
     */
    suspend fun read(context: PlatformContext, widgetId: String): WarpWidgetPreferences

    /**
     * Apply [transform] and persist.
     *
     * - **Android:** `updateAppWidgetState` on each Glance id for this widget kind
     * - **iOS:** write UserDefaults suite
     */
    suspend fun update(
        context: PlatformContext,
        widgetId: String,
        transform: MutableWarpWidgetPreferences.() -> Unit,
    )

    /**
     * Ask the host to re-render after prefs change.
     *
     * - **Android:** `GlanceAppWidget.update` for matching ids
     * - **iOS:** `WidgetCenter.reloadTimelines(ofKind: widgetId)`
     */
    suspend fun reload(context: PlatformContext, widgetId: String)
}

/**
 * Update widget prefs from the **app** or a click handler, then reload the surface.
 *
 * ```
 * updateWarpWidgetState(context, CounterWarpWidget) {
 *     this[CounterKeys.Count] = 42
 * }
 * ```
 *
 * Android: requires [WarpWidgetAndroidRegistry] registration for [widgetId].
 */
suspend fun updateWarpWidgetState(
    context: PlatformContext,
    widgetId: String,
    transform: MutableWarpWidgetPreferences.() -> Unit,
) {
    WarpWidgetStateStore.update(context, widgetId, transform)
    WarpWidgetStateStore.reload(context, widgetId)
}

/** [updateWarpWidgetState] using [WarpWidget.id]. */
suspend fun updateWarpWidgetState(
    context: PlatformContext,
    widget: WarpWidget,
    transform: MutableWarpWidgetPreferences.() -> Unit,
) = updateWarpWidgetState(context, widget.id, transform)

/** Reload home-screen UI without changing prefs. */
suspend fun reloadWarpWidget(
    context: PlatformContext,
    widgetId: String,
) = WarpWidgetStateStore.reload(context, widgetId)

/** [reloadWarpWidget] using [WarpWidget.id]. */
suspend fun reloadWarpWidget(
    context: PlatformContext,
    widget: WarpWidget,
) = reloadWarpWidget(context, widget.id)
