package com.atriidev.warp_widget.api

import androidx.compose.runtime.Stable
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
 *
 * Type names use a `Warp` prefix where needed so they do not collide with
 * WidgetKit / SwiftUI symbols when exported to Swift (`WidgetConfiguration`,
 * `WidgetFamily`, `LayoutDirection`, …).
 */
@Serializable
@Stable
data class WidgetEnvironment(
    /** Process / OS identity (Android vs iOS). */
    val platform: WidgetPlatform,
    /**
     * Light / dark appearance.
     *
     * - **WidgetKit:** `EnvironmentValues.colorScheme` / widget rendering
     * - **Glance:** `Configuration.uiMode` night mask via `LocalContext`
     */
    val theme: WarpWidgetTheme,
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
    val layoutDirection: WarpLayoutDirection,
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
     * - **Glance:** `AppWidgetManager` options (`minWidth` × `maxHeight`), not [androidx.glance.LocalSize]
     */
    val size: WarpWidgetSize?,
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
        val configuration: WarpWidgetConfiguration? = null,
    ) : WidgetPlatformEnvironment

    @Serializable
    @SerialName("ios")
    data class Ios(
        /**
         * WidgetKit size class (`context.family`).
         *
         * Not stored on Android — Glance resize buckets from [WidgetEnvironment.size] are
         * unreliable; use [WidgetEnvironment.size] on Android instead.
         */
        val family: WarpWidgetFamily,
        /**
         * WidgetKit rendering mode (fullColor / accented / vibrant).
         * No Glance equivalent.
         */
        val renderingMode: WarpWidgetRenderingMode = WarpWidgetRenderingMode.FULL_COLOR,
        /**
         * WidgetKit content margins (`EnvironmentValues.widgetContentMargins`).
         * No Glance equivalent.
         */
        val contentMargins: WarpWidgetPadding = WarpWidgetPadding.Zero,
        /**
         * Whether the system draws a container background behind the widget.
         * WidgetKit (`showsWidgetContainerBackground`); no Glance equivalent.
         */
        val showsContainerBackground: Boolean = true,
        /**
         * App Intent / intent-configuration parameters when using configurable widgets.
         */
        val configuration: WarpWidgetConfiguration? = null,
    ) : WidgetPlatformEnvironment
}

/**
 * Widget size class (WidgetKit only).
 *
 * Maps from WidgetKit `WidgetFamily`. On Android use [WidgetEnvironment.size] (dp) instead.
 */
@Serializable
enum class WarpWidgetFamily {
    SYSTEM_SMALL,
    SYSTEM_MEDIUM,
    SYSTEM_LARGE,
    SYSTEM_EXTRA_LARGE,
}

/**
 * Light / dark appearance for the widget surface.
 */
@Serializable
enum class WarpWidgetTheme {
    LIGHT,
    DARK,
    UNSPECIFIED,
}

/**
 * Layout direction for text and mirroring (not SwiftUI `LayoutDirection`).
 */
@Serializable
enum class WarpLayoutDirection {
    LTR,
    RTL,
}

/**
 * Logical width/height of the widget (dp on Android, points on iOS).
 */
@Serializable
data class WarpWidgetSize(
    val widthDp: Float,
    val heightDp: Float,
)

/**
 * Edge insets in dp / points (used for iOS content margins).
 */
@Serializable
data class WarpWidgetPadding(
    val start: Float = 0f,
    val top: Float = 0f,
    val end: Float = 0f,
    val bottom: Float = 0f,
) {
    companion object {
        val Zero: WarpWidgetPadding = WarpWidgetPadding()
    }
}

/**
 * Maps WidgetKit `WidgetRenderingMode`. Ignored on Glance.
 */
@Serializable
enum class WarpWidgetRenderingMode {
    FULL_COLOR,
    ACCENTED,
    VIBRANT,
}

/**
 * Opaque string key/value configuration shared by both hosts when present.
 *
 * Named [WarpWidgetConfiguration] so it does not collide with WidgetKit’s
 * `WidgetConfiguration` when Shared is imported from Swift.
 *
 * - **Glance:** flattened `LocalAppWidgetOptions` / intent extras
 * - **WidgetKit:** App Intent configuration parameters
 */
@Serializable
data class WarpWidgetConfiguration(
    val parameters: Map<String, String> = emptyMap(),
)

/** WidgetKit [WarpWidgetFamily] — null on Android ([WidgetEnvironment.size] instead). */
val WidgetEnvironment.widgetFamily: WarpWidgetFamily?
    get() = (platformEnvironment as? WidgetPlatformEnvironment.Ios)?.family
