package com.atriidev.warp_widget.api

import com.atriidev.warp_widget.WarpWidgetHostApi

/**
 * iOS [PlatformContext].
 *
 * [appGroupId] must match Xcode App Groups. Prefer building via
 * [com.atriidev.warp_widget.platformContext] / [com.atriidev.warp_widget.WarpWidgetHost.iosSession]
 * so [WarpWidgetHostApi.iosGroupId] stays the single source of truth.
 */
actual class PlatformContext(
    val appGroupId: String,
)

/** [PlatformContext] using [WarpWidgetHostApi.iosGroupId]. */
fun WarpWidgetHostApi.platformContext(): PlatformContext =
    PlatformContext(appGroupId = iosGroupId)
