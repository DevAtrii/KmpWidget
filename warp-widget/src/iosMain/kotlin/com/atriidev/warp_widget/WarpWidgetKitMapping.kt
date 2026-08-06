package com.atriidev.warp_widget

import com.atriidev.warp_widget.api.DEFAULT_IOS_APP_GROUP_ID
import com.atriidev.warp_widget.api.PlatformContext
import com.atriidev.warp_widget.api.WarpLayoutDirection
import com.atriidev.warp_widget.api.WarpWidgetFamily
import com.atriidev.warp_widget.api.WarpWidgetPadding
import com.atriidev.warp_widget.api.WarpWidgetRenderingMode
import com.atriidev.warp_widget.api.WarpWidgetSize
import com.atriidev.warp_widget.api.WarpWidgetTheme
import com.atriidev.warp_widget.api.WidgetEnvironment
import com.atriidev.warp_widget.api.WidgetPlatformEnvironment
import com.atriidev.warp_widget.api.makeWidgetEnvironment
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSNumber
import warpWidgetKit.WarpWidgetKitShared

/**
 * Maps `WarpWidgetKitEnv` field bags (from Swift) → [WidgetEnvironment] / [WarpWidgetSession].
 *
 * Lives in Shared/Kotlin — not in the SPM package — because Shared already depends on
 * `warpWidgetKit` via spm4Kmp (`import Shared` inside that package would cycle).
 *
 * Swift hosts should call [installWarpWidgetKitBridge] once, then
 * `WarpWidgetKitEnv.from(context:).makeSession()` (preferred) rather than invoking these
 * methods with dozens of scalars.
 */
object WarpWidgetKitMapping {
    /** Flat WidgetKit fields → [WidgetEnvironment] (iOS platform extras included). */
    fun makeEnvironment(
        family: String,
        widthDp: Float,
        heightDp: Float,
        isPreview: Boolean,
        locale: String,
        layoutDirection: String,
        timeZone: String,
        displayScale: Float,
        fontScale: Float,
        theme: String,
        renderingMode: String,
        marginLeading: Float,
        marginTop: Float,
        marginTrailing: Float,
        marginBottom: Float,
        showsContainerBackground: Boolean,
    ): WidgetEnvironment = makeWidgetEnvironment(
        family = family.toWarpFamily(),
        isPreview = isPreview,
        size = WarpWidgetSize(widthDp = widthDp, heightDp = heightDp),
        theme = theme.toWarpTheme(),
        locale = locale,
        layoutDirection = layoutDirection.toWarpLayoutDirection(),
        timeZone = timeZone,
        displayScale = displayScale,
        fontScale = fontScale,
        platformEnvironment = WidgetPlatformEnvironment.Ios(
            renderingMode = renderingMode.toWarpRenderingMode(),
            contentMargins = WarpWidgetPadding(
                start = marginLeading,
                top = marginTop,
                end = marginTrailing,
                bottom = marginBottom,
            ),
            showsContainerBackground = showsContainerBackground,
            configuration = null,
        ),
    )

    /**
     * Flat WidgetKit fields → [WarpWidgetSession] with App Group [PlatformContext].
     *
     * Prefer Swift `WarpWidgetKitEnv.makeSession()` after [installWarpWidgetKitBridge].
     */
    fun makeSession(
        family: String,
        widthDp: Float,
        heightDp: Float,
        isPreview: Boolean,
        locale: String,
        layoutDirection: String,
        timeZone: String,
        displayScale: Float,
        fontScale: Float,
        theme: String,
        renderingMode: String,
        marginLeading: Float,
        marginTop: Float,
        marginTrailing: Float,
        marginBottom: Float,
        showsContainerBackground: Boolean,
        appGroupId: String = DEFAULT_IOS_APP_GROUP_ID,
    ): WarpWidgetSession = WarpWidgetSession(
        context = PlatformContext(appGroupId = appGroupId),
        environment = makeEnvironment(
            family = family,
            widthDp = widthDp,
            heightDp = heightDp,
            isPreview = isPreview,
            locale = locale,
            layoutDirection = layoutDirection,
            timeZone = timeZone,
            displayScale = displayScale,
            fontScale = fontScale,
            theme = theme,
            renderingMode = renderingMode,
            marginLeading = marginLeading,
            marginTop = marginTop,
            marginTrailing = marginTrailing,
            marginBottom = marginBottom,
            showsContainerBackground = showsContainerBackground,
        ),
    )

    internal fun makeSessionFromMap(fields: Map<*, *>): WarpWidgetSession =
        makeSession(
            family = fields.str("family", "systemSmall"),
            widthDp = fields.float("widthDp"),
            heightDp = fields.float("heightDp"),
            isPreview = fields.bool("isPreview"),
            locale = fields.str("locale", "en"),
            layoutDirection = fields.str("layoutDirection", "ltr"),
            timeZone = fields.str("timeZone", "UTC"),
            displayScale = fields.float("displayScale", 1f),
            fontScale = fields.float("fontScale", 1f),
            theme = fields.str("theme", "unspecified"),
            renderingMode = fields.str("renderingMode", "fullColor"),
            marginLeading = fields.float("marginLeading"),
            marginTop = fields.float("marginTop"),
            marginTrailing = fields.float("marginTrailing"),
            marginBottom = fields.float("marginBottom"),
            showsContainerBackground = fields.bool("showsContainerBackground", true),
            appGroupId = fields.str("appGroupId", DEFAULT_IOS_APP_GROUP_ID),
        )

    internal fun makeEnvironmentFromMap(fields: Map<*, *>): WidgetEnvironment =
        makeEnvironment(
            family = fields.str("family", "systemSmall"),
            widthDp = fields.float("widthDp"),
            heightDp = fields.float("heightDp"),
            isPreview = fields.bool("isPreview"),
            locale = fields.str("locale", "en"),
            layoutDirection = fields.str("layoutDirection", "ltr"),
            timeZone = fields.str("timeZone", "UTC"),
            displayScale = fields.float("displayScale", 1f),
            fontScale = fields.float("fontScale", 1f),
            theme = fields.str("theme", "unspecified"),
            renderingMode = fields.str("renderingMode", "fullColor"),
            marginLeading = fields.float("marginLeading"),
            marginTop = fields.float("marginTop"),
            marginTrailing = fields.float("marginTrailing"),
            marginBottom = fields.float("marginBottom"),
            showsContainerBackground = fields.bool("showsContainerBackground", true),
        )
}

/**
 * Wire Swift `WarpWidgetKitEnv.makeSession()` → Kotlin mapping.
 * Safe to call repeatedly. Call before the first `makeSession()`.
 */
@OptIn(ExperimentalForeignApi::class)
fun installWarpWidgetKitBridge() {
    ensureWarpWidgetKitSharedInstalled()
}

@OptIn(ExperimentalForeignApi::class)
internal fun ensureWarpWidgetKitSharedInstalled() {
    if (WarpWidgetKitShared.isInstalled()) return
    WarpWidgetKitShared.installMakeSession { fields ->
        @Suppress("UNCHECKED_CAST")
        WarpWidgetKitMapping.makeSessionFromMap(fields as Map<*, *>)
    }
    WarpWidgetKitShared.installMakeEnvironment { fields ->
        @Suppress("UNCHECKED_CAST")
        WarpWidgetKitMapping.makeEnvironmentFromMap(fields as Map<*, *>)
    }
}

private fun Map<*, *>.str(key: String, default: String = ""): String =
    this[key]?.toString() ?: default

private fun Map<*, *>.bool(key: String, default: Boolean = false): Boolean {
    val value = this[key] ?: return default
    return when (value) {
        is Boolean -> value
        is NSNumber -> value.boolValue
        else -> value.toString().toBooleanStrictOrNull() ?: default
    }
}

private fun Map<*, *>.float(key: String, default: Float = 0f): Float {
    val value = this[key] ?: return default
    return when (value) {
        is Float -> value
        is Double -> value.toFloat()
        is Int -> value.toFloat()
        is Long -> value.toFloat()
        is NSNumber -> value.floatValue
        else -> value.toString().toFloatOrNull() ?: default
    }
}

private fun String.toWarpFamily(): WarpWidgetFamily = when (this) {
    "systemSmall" -> WarpWidgetFamily.SYSTEM_SMALL
    "systemMedium" -> WarpWidgetFamily.SYSTEM_MEDIUM
    "systemLarge" -> WarpWidgetFamily.SYSTEM_LARGE
    "systemExtraLarge" -> WarpWidgetFamily.SYSTEM_EXTRA_LARGE
    else -> WarpWidgetFamily.SYSTEM_SMALL
}

private fun String.toWarpTheme(): WarpWidgetTheme = when (this) {
    "light" -> WarpWidgetTheme.LIGHT
    "dark" -> WarpWidgetTheme.DARK
    else -> WarpWidgetTheme.UNSPECIFIED
}

private fun String.toWarpLayoutDirection(): WarpLayoutDirection = when (this) {
    "rtl" -> WarpLayoutDirection.RTL
    else -> WarpLayoutDirection.LTR
}

private fun String.toWarpRenderingMode(): WarpWidgetRenderingMode = when (this) {
    "accented" -> WarpWidgetRenderingMode.ACCENTED
    "vibrant" -> WarpWidgetRenderingMode.VIBRANT
    else -> WarpWidgetRenderingMode.FULL_COLOR
}
