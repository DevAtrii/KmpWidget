package com.atriidev.warp_widget

import com.atriidev.warp_widget.api.DEFAULT_IOS_APP_GROUP_ID
import com.atriidev.warp_widget.api.PlatformContext
import com.atriidev.warp_widget.api.WidgetEnvironment

/**
 * Build an iOS [WarpWidgetSession] with App Group [PlatformContext].
 *
 * Host supplies [environment] (typically from Swift
 * `WarpWidgetKitEnv.makeEnvironment()` after [installWarpWidgetKitBridge]).
 *
 * ```swift
 * WarpWidgetKitMappingKt.installWarpWidgetKitBridge()
 * let env: WidgetEnvironment = WarpWidgetKitEnv.from(context: context).makeEnvironment()
 * let session = WarpWidgetHost.shared.iosSession(environment: env)
 * ```
 *
 * Prefer `WarpWidgetKitEnv.makeSession()` which builds env + session in one step.
 */
fun WarpWidgetHost.iosSession(
    environment: WidgetEnvironment,
    appGroupId: String = DEFAULT_IOS_APP_GROUP_ID,
    preferences: WarpWidgetPreferences? = null,
): WarpWidgetSession {
    ensureWarpWidgetKitSharedInstalled()
    return WarpWidgetSession(
        context = PlatformContext(appGroupId = appGroupId),
        environment = environment,
        preferences = preferences,
    )
}
