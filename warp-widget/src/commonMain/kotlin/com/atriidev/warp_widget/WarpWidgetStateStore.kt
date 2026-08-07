package com.atriidev.warp_widget

import com.atriidev.warp_widget.api.PlatformContext

/**
 * Platform persistence for widget prefs + timeline / Glance reload.
 *
 * Typed [WarpWidget] state is stored as JSON under prefs key = [WarpWidget.id].
 * Scope ([WarpWidget.stateScope]) controls shared vs per-[WarpWidgetId] storage.
 *
 * Public typed APIs always take [WarpWidgetId] (Glance-style). Scope only changes
 * fan-out / key layout — never whether an id is required.
 */
expect object WarpWidgetStateStore {
    suspend fun read(
        context: PlatformContext,
        widget: WarpWidgetHostApi,
        id: WarpWidgetId,
    ): WarpWidgetPreferences

    suspend fun read(context: PlatformContext, widgetId: String): WarpWidgetPreferences

    suspend fun update(
        context: PlatformContext,
        widget: WarpWidgetHostApi,
        id: WarpWidgetId,
        transform: MutableWarpWidgetPreferences.() -> Unit,
    )

    suspend fun update(
        context: PlatformContext,
        widgetId: String,
        transform: MutableWarpWidgetPreferences.() -> Unit,
    )

    suspend fun refreshAfterUpdate(
        context: PlatformContext,
        widget: WarpWidgetHostApi,
        id: WarpWidgetId,
    )

    suspend fun refreshAfterUpdate(context: PlatformContext, widgetId: String)

    suspend fun reload(
        context: PlatformContext,
        widget: WarpWidgetHostApi,
        id: WarpWidgetId,
    )

    suspend fun reload(context: PlatformContext, widgetId: String)

    suspend fun listWarpWidgetIds(
        context: PlatformContext,
        widget: WarpWidgetHostApi,
    ): List<WarpWidgetId>
}

/**
 * Update typed widget state for [id], then reload UI.
 *
 * Always pass [WarpWidgetId] (Glance-style) — from [WarpWidgetSession.widgetId] or
 * [listWarpWidgetIds]. [WarpWidget.stateScope] only controls fan-out:
 * - **Shared** — transform once, mirror / reload all instances ([id] still required)
 * - **Instance** — only [id]
 *
 * ```
 * updateWarpWidgetState(session, CounterWarpWidget) { it.copy(count = it.count + 1) }
 *
 * val ids = listWarpWidgetIds(context, StocksWarpWidget)
 * updateWarpWidgetState(context, StocksWarpWidget, ids.first()) {
 *     it.copy(symbols = listOf("AAPL"))
 * }
 * ```
 */
suspend fun <S : Any> updateWarpWidgetState(
    context: PlatformContext,
    widget: WarpWidget<S>,
    id: WarpWidgetId,
    transform: (S) -> S,
) {
    WarpWidgetStateStore.update(context, widget, id) {
        val current = widget.decodeState(toPreferences())
        setRaw(widget.id, widget.encodeState(transform(current)))
    }
    WarpWidgetStateStore.refreshAfterUpdate(context, widget, id)
}

suspend fun <S : Any> updateWarpWidgetState(
    session: WarpWidgetSession,
    widget: WarpWidget<S>,
    transform: (S) -> S,
) {
    // Prefer click-scoped id (tapped GlanceId / AppIntent params) over session captured
    // when handlers were last registered (global registry — last render would win).
    val id = WarpWidgetClickScope.current() ?: session.widgetId
    updateWarpWidgetState(session.context, widget, id, transform)
}

/** Read typed state for [id] (or [WarpWidget.defaultState] when missing). */
suspend fun <S : Any> readWarpWidgetState(
    context: PlatformContext,
    widget: WarpWidget<S>,
    id: WarpWidgetId,
): S = widget.decodeState(
    WarpWidgetStateStore.read(context, widget, id),
)

/**
 * Low-level string-key prefs update (advanced). Prefer typed [updateWarpWidgetState].
 */
suspend fun updateWarpWidgetPreferences(
    context: PlatformContext,
    widget: WarpWidgetHostApi,
    id: WarpWidgetId,
    transform: MutableWarpWidgetPreferences.() -> Unit,
) {
    WarpWidgetStateStore.update(context, widget, id, transform)
    WarpWidgetStateStore.refreshAfterUpdate(context, widget, id)
}

suspend fun updateWarpWidgetPreferences(
    context: PlatformContext,
    widgetId: String,
    transform: MutableWarpWidgetPreferences.() -> Unit,
) {
    WarpWidgetStateStore.update(context, widgetId, transform)
    WarpWidgetStateStore.refreshAfterUpdate(context, widgetId)
}

/** Reload home-screen UI without changing prefs. */
suspend fun reloadWarpWidget(
    context: PlatformContext,
    widget: WarpWidgetHostApi,
    id: WarpWidgetId,
) = WarpWidgetStateStore.reload(context, widget, id)

suspend fun reloadWarpWidget(
    context: PlatformContext,
    widgetId: String,
) = WarpWidgetStateStore.reload(context, widgetId)

/** Active instance ids for [widget] (Glance placements / iOS instance keys). */
suspend fun listWarpWidgetIds(
    context: PlatformContext,
    widget: WarpWidgetHostApi,
): List<WarpWidgetId> = WarpWidgetStateStore.listWarpWidgetIds(context, widget)
