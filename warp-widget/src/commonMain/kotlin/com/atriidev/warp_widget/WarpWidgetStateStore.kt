package com.atriidev.warp_widget

import com.atriidev.warp_widget.api.PlatformContext

/**
 * Platform persistence for widget prefs + timeline / Glance reload.
 *
 * Typed [WarpWidget] state is stored as JSON under prefs key = [WarpWidget.id].
 */
expect object WarpWidgetStateStore {
    suspend fun read(context: PlatformContext, widgetId: String): WarpWidgetPreferences

    suspend fun update(
        context: PlatformContext,
        widgetId: String,
        transform: MutableWarpWidgetPreferences.() -> Unit,
    )

    /** Refresh UI after prefs were already mutated (no theme bump). */
    suspend fun refreshAfterUpdate(context: PlatformContext, widgetId: String)

    suspend fun reload(context: PlatformContext, widgetId: String)
}

/**
 * Update typed widget state, persist JSON under [WarpWidget.id], then reload UI.
 *
 * ```
 * updateWarpWidgetState(context, CounterWarpWidget) { state ->
 *     state.copy(count = state.count + 1)
 * }
 * ```
 */
suspend fun <S : Any> updateWarpWidgetState(
    context: PlatformContext,
    widget: WarpWidget<S>,
    transform: (S) -> S,
) {
    WarpWidgetStateStore.update(context, widget.id) {
        val current = widget.decodeState(toPreferences())
        setRaw(widget.id, widget.encodeState(transform(current)))
    }
    WarpWidgetStateStore.refreshAfterUpdate(context, widget.id)
}

/** Read typed state (or [WarpWidget.defaultState] when missing). */
suspend fun <S : Any> readWarpWidgetState(
    context: PlatformContext,
    widget: WarpWidget<S>,
): S = widget.decodeState(WarpWidgetStateStore.read(context, widget.id))

/**
 * Low-level string-key prefs update (advanced). Prefer typed [updateWarpWidgetState].
 */
suspend fun updateWarpWidgetPreferences(
    context: PlatformContext,
    widgetId: String,
    transform: MutableWarpWidgetPreferences.() -> Unit,
) {
    WarpWidgetStateStore.update(context, widgetId, transform)
    WarpWidgetStateStore.refreshAfterUpdate(context, widgetId)
}

/** [updateWarpWidgetPreferences] using [WarpWidgetHostApi.id]. */
suspend fun updateWarpWidgetPreferences(
    context: PlatformContext,
    widget: WarpWidgetHostApi,
    transform: MutableWarpWidgetPreferences.() -> Unit,
) = updateWarpWidgetPreferences(context, widget.id, transform)

/** Reload home-screen UI without changing prefs. */
suspend fun reloadWarpWidget(
    context: PlatformContext,
    widgetId: String,
) = WarpWidgetStateStore.reload(context, widgetId)

/** [reloadWarpWidget] using [WarpWidgetHostApi.id]. */
suspend fun reloadWarpWidget(
    context: PlatformContext,
    widget: WarpWidgetHostApi,
) = reloadWarpWidget(context, widget.id)
