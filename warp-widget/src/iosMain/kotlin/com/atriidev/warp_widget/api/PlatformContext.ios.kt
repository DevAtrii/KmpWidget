package com.atriidev.warp_widget.api

import com.atriidev.warp_widget.WarpWidget

/**
 * iOS [PlatformContext].
 *
 * [appGroupId] must match Xcode App Groups. Prefer building via
 * [com.atriidev.warp_widget.platformContext] / [com.atriidev.warp_widget.WarpWidgetHost.iosSession]
 * so [WarpWidget.iosGroupId] stays the single source of truth.
 */
actual class PlatformContext(
    val appGroupId: String,
)

/** [PlatformContext] using [WarpWidget.iosGroupId]. */
fun WarpWidget.platformContext(): PlatformContext =
    PlatformContext(appGroupId = iosGroupId)
