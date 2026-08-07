package com.atriidev.warp_widget.api

/**
 * Build a [WidgetEnvironment] from host-supplied values.
 *
 * Callers should pass real Glance / WidgetKit measurements (size, theme, locale, …).
 * Defaults are only for unspecified optional fields — not a substitute for a real host snapshot.
 *
 * WidgetKit [WarpWidgetFamily] belongs on [WidgetPlatformEnvironment.Ios.family], not here —
 * Glance size buckets are unreliable.
 *
 * @param isPreview Gallery / editor preview vs home-screen instance
 * @param platformEnvironment Android- or iOS-only extras; inferred from [platform] when null
 */
fun makeWidgetEnvironment(
    platformContext: PlatformContext,
    isPreview: Boolean,
    size: WarpWidgetSize? = null,
    theme: WarpWidgetTheme = WarpWidgetTheme.UNSPECIFIED,
    locale: String = "en",
    layoutDirection: WarpLayoutDirection = WarpLayoutDirection.LTR,
    timeZone: String = "UTC",
    displayScale: Float = 1f,
    fontScale: Float = 1f,
    platformEnvironment: WidgetPlatformEnvironment? = null,
    platform: WidgetPlatform = currentWidgetPlatform(platformContext),
): WidgetEnvironment = WidgetEnvironment(
    platform = platform,
    theme = theme,
    locale = locale,
    layoutDirection = layoutDirection,
    timeZone = timeZone,
    size = size,
    isPreview = isPreview,
    displayScale = displayScale,
    fontScale = fontScale,
    platformEnvironment = platformEnvironment
        ?: when (platform) {
            is WidgetPlatform.Android -> WidgetPlatformEnvironment.Android()
            is WidgetPlatform.Ios -> WidgetPlatformEnvironment.Ios(
                family = WarpWidgetFamily.SYSTEM_SMALL,
            )
        },
)
