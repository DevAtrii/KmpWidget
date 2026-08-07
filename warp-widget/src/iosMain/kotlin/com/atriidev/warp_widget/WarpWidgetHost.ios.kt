package com.atriidev.warp_widget

import com.atriidev.warp_widget.api.WidgetEnvironment
import com.atriidev.warp_widget.api.platformContext

/**
 * Preferred Swift entry: Kit field bag → [WarpWidgetSession].
 *
 * Pass `WarpWidgetKitEnv.asKitFields(...)` as a Swift `[AnyHashable: Any]` (Kotlin [Map]).
 * Auto-installs the Kit↔Kotlin bridge.
 *
 * ```swift
 * let session = WarpWidgetHost.shared.iosSession(
 *     widget: CounterWarpWidget.shared,
 *     kitFields: WarpWidgetKitEnv.from(context: context).asKitFields(
 *         appGroupId: CounterWarpWidget.shared.iosGroupId
 *     )
 * )
 * ```
 */
fun WarpWidgetHost.iosSession(
    widget: WarpWidget,
    kitFields: Map<Any?, *>,
): WarpWidgetSession = iosSession(widget, kitFields, preferences = null)

fun WarpWidgetHost.iosSession(
    widget: WarpWidget,
    kitFields: Map<Any?, *>,
    preferences: WarpWidgetPreferences?,
): WarpWidgetSession {
    ensureWarpWidgetKitSharedInstalled()
    return WarpWidgetSession(
        context = widget.platformContext(),
        environment = WarpWidgetKitMapping.makeEnvironmentFromMap(kitFields),
        preferences = preferences,
    )
}

/**
 * Build a session when you already hold a [WidgetEnvironment] (e.g. Kotlin tests).
 *
 * Swift hosts should prefer [iosSession] with `kitFields` so the bridge installs itself.
 */
fun WarpWidgetHost.iosSession(
    widget: WarpWidget,
    environment: WidgetEnvironment,
): WarpWidgetSession = iosSession(widget, environment, preferences = null)

fun WarpWidgetHost.iosSession(
    widget: WarpWidget,
    environment: WidgetEnvironment,
    preferences: WarpWidgetPreferences?,
): WarpWidgetSession {
    ensureWarpWidgetKitSharedInstalled()
    return WarpWidgetSession(
        context = widget.platformContext(),
        environment = environment,
        preferences = preferences,
    )
}
