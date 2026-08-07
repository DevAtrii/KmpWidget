package com.atriidev.warp_widget

import com.atriidev.warp_widget.api.WarpWidgetFamily
import com.atriidev.warp_widget.api.WidgetEnvironment
import com.atriidev.warp_widget.api.WidgetPlatformEnvironment
import com.atriidev.warp_widget.api.platformContext

/**
 * Preferred Swift entry: Kit field bag → [WarpWidgetSession].
 *
 * Pass `WarpWidgetKitEnv.asKitFields(appGroupId:)` — Kotlin derives [WarpWidgetSession.widgetId]
 * from kit `family` (Instance) or kind id (Shared). No Swift instance-id plumbing.
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
    widget: WarpWidgetHostApi,
    kitFields: Map<Any?, *>,
): WarpWidgetSession = iosSession(widget, kitFields, preferences = null)

fun WarpWidgetHost.iosSession(
    widget: WarpWidgetHostApi,
    kitFields: Map<Any?, *>,
    preferences: WarpWidgetPreferences?,
): WarpWidgetSession {
    ensureWarpWidgetKitSharedInstalled()
    return WarpWidgetSession(
        context = widget.platformContext(),
        environment = WarpWidgetKitMapping.makeEnvironmentFromMap(kitFields),
        preferences = preferences,
        widgetId = resolveSessionWidgetId(
            widget = widget,
            kitInstanceId = kitFields["instanceId"]?.toString()?.takeIf { it.isNotBlank() },
            kitFamily = kitFields["family"]?.toString()?.takeIf { it.isNotBlank() },
        ),
    )
}

/**
 * Build a session when you already hold a [WidgetEnvironment] (e.g. Kotlin tests).
 *
 * Swift hosts should prefer [iosSession] with `kitFields` so the bridge installs itself.
 */
fun WarpWidgetHost.iosSession(
    widget: WarpWidgetHostApi,
    environment: WidgetEnvironment,
): WarpWidgetSession = iosSession(widget, environment, preferences = null)

fun WarpWidgetHost.iosSession(
    widget: WarpWidgetHostApi,
    environment: WidgetEnvironment,
    preferences: WarpWidgetPreferences?,
): WarpWidgetSession {
    ensureWarpWidgetKitSharedInstalled()
    val iosEnv = environment.platformEnvironment as? WidgetPlatformEnvironment.Ios
    return WarpWidgetSession(
        context = widget.platformContext(),
        environment = environment,
        preferences = preferences,
        widgetId = resolveSessionWidgetId(
            widget = widget,
            kitInstanceId = iosEnv?.instanceId,
            kitFamily = iosEnv?.family?.toKitFamilyString(),
        ),
    )
}

private fun WarpWidgetFamily.toKitFamilyString(): String = when (this) {
    WarpWidgetFamily.SYSTEM_SMALL -> "systemSmall"
    WarpWidgetFamily.SYSTEM_MEDIUM -> "systemMedium"
    WarpWidgetFamily.SYSTEM_LARGE -> "systemLarge"
    WarpWidgetFamily.SYSTEM_EXTRA_LARGE -> "systemExtraLarge"
}
