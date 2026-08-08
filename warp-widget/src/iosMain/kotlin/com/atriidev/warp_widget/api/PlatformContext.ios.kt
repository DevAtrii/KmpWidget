package com.atriidev.warp_widget.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.atriidev.warp_widget.WarpWidget
import com.atriidev.warp_widget.WarpWidgetHostApi

/**
 * iOS [PlatformContext].
 *
 * [appGroupId] must match Xcode App Groups. Prefer building via
 * [com.atriidev.warp_widget.platformContext] / [com.atriidev.warp_widget.WarpWidgetHost.iosSession]
 * so [WarpWidgetHostApi.iosGroupId] stays the single source of truth.
 */
@Stable
actual class PlatformContext(
    val appGroupId: String,
)

/** [PlatformContext] using [WarpWidgetHostApi.iosGroupId]. */
fun WarpWidgetHostApi.platformContext(): PlatformContext =
    PlatformContext(appGroupId = iosGroupId)

@Composable
actual fun <T : Any> rememberPlatformContext(widget: WarpWidget<T>): PlatformContext {
    return remember(widget, widget.iosGroupId) {
        widget.platformContext()
    }
}