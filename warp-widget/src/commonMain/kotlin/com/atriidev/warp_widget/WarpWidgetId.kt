package com.atriidev.warp_widget

import kotlin.jvm.JvmInline

/**
 * Stable id for a widget **instance** (home-screen placement) or kind-level logical id.
 *
 * - **Android:** `"aw:$appWidgetId"` via [android] / [fromAppWidgetId] (androidMain).
 * - **iOS:** `"ios:$segment"` — derived in Kotlin from WidgetKit family (or optional App Intent id).
 * - **Shared kind:** [ofKind] — same string as [WarpWidget.id].
 */
@JvmInline
value class WarpWidgetId(val value: String) {
    override fun toString(): String = value

    companion object {
        /** Kind-level id for [WarpWidgetStateScope.Shared] widgets. */
        fun ofKind(kindId: String): WarpWidgetId = WarpWidgetId(kindId)

        fun ios(instanceId: String): WarpWidgetId = WarpWidgetId("ios:$instanceId")

        fun android(appWidgetId: Int): WarpWidgetId = WarpWidgetId("aw:$appWidgetId")
    }
}

/** Whether widget state is shared across instances or isolated per [WarpWidgetId]. */
enum class WarpWidgetStateScope {
    /** All home-screen instances share one state (default). */
    Shared,

    /** Each instance has its own state (personal stocks, etc.). */
    Instance,
}

/** Wire key injected into click [parameters] so iOS AppIntent can restore [WarpWidgetId]. */
internal const val WARP_WIDGET_ID_PARAM = "__warpWidgetId"

internal fun WarpWidgetHostApi.stateScopeOrShared(): WarpWidgetStateScope =
    (this as? WarpWidget<*>)?.stateScope ?: WarpWidgetStateScope.Shared

internal fun WarpWidgetId.iosInstanceSegmentOrNull(): String? =
    value.removePrefix("ios:").takeIf { value.startsWith("ios:") && it.isNotEmpty() }

/**
 * Resolve [WarpWidgetSession.widgetId] from host context.
 *
 * Swift never passes instance ids — Kotlin derives them:
 * - **Shared:** [ofKind]
 * - **Android Instance:** `"aw:$appWidgetId"`
 * - **iOS Instance:** `"ios:$family"` from kit fields (optional explicit `instanceId` for App Intent)
 */
fun resolveSessionWidgetId(
    widget: WarpWidgetHostApi,
    kitInstanceId: String? = null,
    kitFamily: String? = null,
    androidAppWidgetId: Int? = null,
): WarpWidgetId = when (widget.stateScopeOrShared()) {
    WarpWidgetStateScope.Shared -> WarpWidgetId.ofKind(widget.id)
    WarpWidgetStateScope.Instance -> when {
        androidAppWidgetId != null -> WarpWidgetId.android(androidAppWidgetId)
        !kitInstanceId.isNullOrBlank() -> WarpWidgetId.ios(kitInstanceId)
        !kitFamily.isNullOrBlank() -> WarpWidgetId.ios(kitFamily)
        else -> WarpWidgetId.ios("unknown")
    }
}
