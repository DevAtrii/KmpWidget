package com.atriidev.warp_widget.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.atriidev.warp_widget.WarpWidget

/**
 * Opaque host handle for I/O (prefs, reload).
 *
 * - **Android:** wraps [android.content.Context]
 * - **iOS:** holds App Group id for `UserDefaults(suiteName:)`
 *
 * Built by the platform host and stored on [com.atriidev.warp_widget.WarpWidgetSession].
 */
@Stable
expect class PlatformContext



/**
 * use this function inside screens to get current platform context,
 * **/
@Composable
expect fun <T : Any> rememberPlatformContext(widget: WarpWidget<T>): PlatformContext
