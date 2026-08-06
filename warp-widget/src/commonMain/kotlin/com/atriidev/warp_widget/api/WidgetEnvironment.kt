package com.atriidev.warp_widget.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Snapshot of the widget host environment at render time.
 *
 * Fields here are intentionally limited to values that can be read from **both**:
 * - Jetpack Glance (`LocalSize`, `LocalContext` / `Configuration`, preview APIs, …)
 * - WidgetKit (`TimelineProvider.Context`, `EnvironmentValues`, …)
 *
 * Platform-only extras live on [platformEnvironment].
 */
@Serializable
data class WidgetEnvironment(
    /** Process / OS identity (Android vs iOS). */
    val platform: WidgetPlatform,
    /**
     * Size class of the widget.
     *
     * - **WidgetKit:** `context.family`
     * - **Glance:** mapped from [size] / `AppWidgetProviderInfo` min size
     */
    val family: WidgetFamily,
    /**
     * Light / dark appearance.
     *
     * - **WidgetKit:** `EnvironmentValues.colorScheme` / widget rendering
     * - **Glance:** `Configuration.uiMode` night mask via `LocalContext`
     */
    val theme: WidgetTheme,
    /**
     * BCP 47 locale tag (e.g. `"en-US"`).
     *
     * - **WidgetKit:** `Locale.current` / environment
     * - **Glance:** `Configuration.locales` via `LocalContext`
     */
    val locale: String,
    /**
     * LTR / RTL.
     *
     * - **WidgetKit:** `EnvironmentValues.layoutDirection`
     * - **Glance:** `Configuration.layoutDirection` via `LocalContext`
     */
    val layoutDirection: LayoutDirection,
    /**
     * IANA time zone id (e.g. `"America/New_York"`).
     *
     * Available in both hosts via the process time zone when the widget renders.
     */
    val timeZone: String,
    /**
     * Logical size of the widget content area in dp (points ≈ dp).
     *
     * - **WidgetKit:** `context.displaySize` (points)
     * - **Glance:** `LocalSize.current` (`DpSize`)
     */
    val size: WidgetSize?,
    /**
     * Whether this render is a gallery / editor preview.
     *
     * - **WidgetKit:** `context.isPreview`
     * - **Glance:** preview / `providePreview` path
     */
    val isPreview: Boolean,
    /**
     * Display scale (density).
     *
     * - **WidgetKit:** `EnvironmentValues.displayScale`
     * - **Glance:** `DisplayMetrics.density` via `LocalContext`
     */
    val displayScale: Float,
    /**
     * User font scale.
     *
     * - **WidgetKit:** derived from Dynamic Type / size category
     * - **Glance:** `Configuration.fontScale` via `LocalContext`
     */
    val fontScale: Float,
    /** Values that exist on only one host. */
    val platformEnvironment: WidgetPlatformEnvironment,
)

/**
 * Host-specific extras. Prefer [WidgetEnvironment] shared fields when possible.
 */
@Serializable
sealed interface WidgetPlatformEnvironment {
    @Serializable
    @SerialName("android")
    data class Android(
        /**
         * Bound app-widget / Glance id when available.
         *
         * Glance: `LocalGlanceId` / `AppWidgetId`; not present on WidgetKit.
         */
        val appWidgetId: Int? = null,
        /**
         * Optional configuration / options bag (Glance `LocalAppWidgetOptions` keys).
         */
        val configuration: WidgetConfiguration? = null,
    ) : WidgetPlatformEnvironment

    @Serializable
    @SerialName("ios")
    data class Ios(
        /**
         * WidgetKit rendering mode (fullColor / accented / vibrant).
         * No Glance equivalent.
         */
        val renderingMode: WidgetRenderingMode = WidgetRenderingMode.FULL_COLOR,
        /**
         * WidgetKit content margins (`EnvironmentValues.widgetContentMargins`).
         * No Glance equivalent.
         */
        val contentMargins: WidgetPadding = WidgetPadding.Zero,
        /**
         * Whether the system draws a container background behind the widget.
         * WidgetKit (`showsWidgetContainerBackground`); no Glance equivalent.
         */
        val showsContainerBackground: Boolean = true,
        /**
         * App Intent / intent-configuration parameters when using configurable widgets.
         */
        val configuration: WidgetConfiguration? = null,
    ) : WidgetPlatformEnvironment
}

/**
 * Widget size class shared across hosts.
 *
 * WidgetKit exposes these as `WidgetFamily`. On Glance they are inferred from
 * [WidgetSize] (and provider min width/height), not an OS enum.
 */
@Serializable
enum class WidgetFamily {
    SYSTEM_SMALL,
    SYSTEM_MEDIUM,
    SYSTEM_LARGE,
    SYSTEM_EXTRA_LARGE,
}

/**
 * Light / dark appearance for the widget surface.
 */
@Serializable
enum class WidgetTheme {
    LIGHT,
    DARK,
    UNSPECIFIED,
}

/**
 * Layout direction for text and mirroring.
 */
@Serializable
enum class LayoutDirection {
    LTR,
    RTL,
}

/**
 * Logical width/height of the widget (dp on Android, points on iOS).
 */
@Serializable
data class WidgetSize(
    val widthDp: Float,
    val heightDp: Float,
)

/**
 * Edge insets in dp / points (used for iOS content margins).
 */
@Serializable
data class WidgetPadding(
    val start: Float = 0f,
    val top: Float = 0f,
    val end: Float = 0f,
    val bottom: Float = 0f,
) {
    companion object {
        val Zero: WidgetPadding = WidgetPadding()
    }
}

/**
 * WidgetKit `WidgetRenderingMode`. Ignored on Glance.
 */
@Serializable
enum class WidgetRenderingMode {
    FULL_COLOR,
    ACCENTED,
    VIBRANT,
}

/**
 * Opaque string key/value configuration shared by both hosts when present.
 *
 * - **Glance:** flattened `LocalAppWidgetOptions` / intent extras
 * - **WidgetKit:** App Intent configuration parameters
 */
@Serializable
data class WidgetConfiguration(
    val parameters: Map<String, String> = emptyMap(),
)
