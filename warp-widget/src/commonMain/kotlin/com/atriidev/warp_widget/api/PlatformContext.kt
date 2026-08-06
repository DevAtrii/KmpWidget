package com.atriidev.warp_widget.api

/**
 * Opaque host handle for I/O (prefs, reload).
 *
 * - **Android:** wraps [android.content.Context]
 * - **iOS:** holds App Group id for `UserDefaults(suiteName:)`
 *
 * Built by the platform host and stored on [com.atriidev.warp_widget.WarpWidgetSession].
 */
expect class PlatformContext
