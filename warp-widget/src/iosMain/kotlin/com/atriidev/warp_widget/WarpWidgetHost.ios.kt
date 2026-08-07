package com.atriidev.warp_widget

import com.atriidev.warp_widget.api.WidgetEnvironment
import com.atriidev.warp_widget.api.platformContext

/**
 * Build an iOS [WarpWidgetSession] from [widget] + host [environment].
 *
 * Uses [WarpWidget.iosGroupId] for App Group [com.atriidev.warp_widget.api.PlatformContext]
 * (single source of truth — do not hardcode the suite id in Swift/Kotlin hosts).
 *
 * Two overloads so Swift can omit preferences (Kotlin defaults are not exported to Swift).
 *
 * ```swift
 * WarpWidgetKitMappingKt.installWarpWidgetKitBridge()
 * let env: WidgetEnvironment = WarpWidgetKitEnv.from(context: context).makeEnvironment()
 * let session = WarpWidgetHost.shared.iosSession(widget: CounterWarpWidget.shared, environment: env)
 * ```
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
