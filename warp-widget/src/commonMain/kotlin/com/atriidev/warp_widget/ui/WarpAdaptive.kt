package com.atriidev.warp_widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.atriidev.warp_widget.api.WarpWidgetFamily
import com.atriidev.warp_widget.api.WarpWidgetSize
import com.atriidev.warp_widget.api.WidgetEnvironment
import com.atriidev.warp_widget.api.isAndroid
import com.atriidev.warp_widget.api.isIos
import com.atriidev.warp_widget.api.widgetFamily

/**
 * Cross-platform size bucket aligned with WidgetKit `systemSmall` / `systemMedium` / `systemLarge`.
 *
 * - **iOS:** from [WidgetEnvironment.widgetFamily]
 * - **Android:** inferred from [WidgetEnvironment.size] (dp from AppWidget options — not Glance [androidx.glance.LocalSize])
 */
enum class WarpAdaptiveSize {
    Small,
    Medium,
    Large,
}

/** Default dp breakpoints for [adaptiveSizeFrom] (Android launcher columns). */
object WarpAdaptiveThresholds {
    /** ~2 columns (e.g. 179dp) → [WarpAdaptiveSize.Small]. */
    const val SMALL_MAX_WIDTH_DP = 250f

    /** ~5+ columns (e.g. 734dp) → [WarpAdaptiveSize.Large]. */
    const val LARGE_MIN_WIDTH_DP = 550f

    /** Multi-row height (e.g. 373×311) promotes to [WarpAdaptiveSize.Large]. */
    const val LARGE_MIN_HEIGHT_DP = 170f
}

/** `(widthDp, heightDp) → bucket` — pass to [rememberWarpAdaptiveSize] / [WarpAdaptiveContent]. */
typealias WarpAdaptiveCalc = (widthDp: Float, heightDp: Float) -> WarpAdaptiveSize

/**
 * Classify [widthDp] × [heightDp] into a [WarpAdaptiveSize] bucket.
 *
 * Width is the primary signal (~179 small, ~373 medium, ~734 large). Tall layouts
 * ([heightDp] ≥ [WarpAdaptiveThresholds.LARGE_MIN_HEIGHT_DP]) promote to large at the same width.
 *
 * ```
 * val bucket = adaptiveSizeFrom(env.size?.widthDp ?: 0f, env.size?.heightDp ?: 0f)
 * ```
 */
fun adaptiveSizeFrom(widthDp: Float, heightDp: Float): WarpAdaptiveSize = when {
    widthDp < WarpAdaptiveThresholds.SMALL_MAX_WIDTH_DP -> WarpAdaptiveSize.Small
    widthDp >= WarpAdaptiveThresholds.LARGE_MIN_WIDTH_DP ||
        heightDp >= WarpAdaptiveThresholds.LARGE_MIN_HEIGHT_DP -> WarpAdaptiveSize.Large
    else -> WarpAdaptiveSize.Medium
}

/**
 * Run [calc] against [environment] size (0×0 dp when unknown). Recomputes when width/height change.
 *
 * ```
 * val size = rememberWarpAdaptiveSize(env) { w, h -> adaptiveSizeFrom(w, h) }
 * ```
 */
@Composable
fun rememberWarpAdaptiveSize(
    environment: WidgetEnvironment,
    calc: WarpAdaptiveCalc,
): WarpAdaptiveSize {
    val widthDp = environment.size?.widthDp ?: 0f
    val heightDp = environment.size?.heightDp ?: 0f
    return remember(widthDp, heightDp) { calc(widthDp, heightDp) }
}

/**
 * Current adaptive bucket for this render.
 *
 * Prefer [WarpAdaptiveContent] in composables; use this for non-UI branching.
 */
fun WidgetEnvironment.adaptiveSize(): WarpAdaptiveSize = when {
    platform.isIos -> when (widgetFamily) {
        WarpWidgetFamily.SYSTEM_SMALL -> WarpAdaptiveSize.Small
        WarpWidgetFamily.SYSTEM_MEDIUM -> WarpAdaptiveSize.Medium
        WarpWidgetFamily.SYSTEM_LARGE,
        WarpWidgetFamily.SYSTEM_EXTRA_LARGE,
        -> WarpAdaptiveSize.Large
        null -> WarpAdaptiveSize.Small
    }
    platform.isAndroid -> size?.adaptiveSize() ?: WarpAdaptiveSize.Small
    else -> WarpAdaptiveSize.Small
}

/**
 * Map logical dp size to [WarpAdaptiveSize] (Android / fallback).
 *
 * **Width is the primary bucket** (~launcher columns): ~179dp → small, ~373dp → medium,
 * ~734dp → large. Vertical growth (height ≥ 170dp) promotes to large at the same width
 * (e.g. 373×99 medium vs 373×311 large).
 */
fun WarpWidgetSize.adaptiveSize(): WarpAdaptiveSize =
    adaptiveSizeFrom(widthDp = widthDp, heightDp = heightDp)

/** Pick a value per [WarpAdaptiveSize] without a composable wrapper. */
fun <T> WidgetEnvironment.adaptiveValue(
    small: T,
    medium: T = small,
    large: T = medium,
): T = when (adaptiveSize()) {
    WarpAdaptiveSize.Small -> small
    WarpAdaptiveSize.Medium -> medium
    WarpAdaptiveSize.Large -> large
}

/**
 * Compose one of three adaptive layouts (WidgetKit-style).
 *
 * Uses [WidgetEnvironment.adaptiveSize] — iOS family, Android default [adaptiveSizeFrom].
 */
@Composable
fun WarpAdaptiveContent(
    environment: WidgetEnvironment,
    small: @Composable () -> Unit,
    medium: @Composable () -> Unit = small,
    large: @Composable () -> Unit = medium,
) {
    when (environment.adaptiveSize()) {
        WarpAdaptiveSize.Small -> small()
        WarpAdaptiveSize.Medium -> medium()
        WarpAdaptiveSize.Large -> large()
    }
}

/**
 * [WarpAdaptiveContent] with a custom [calc] over widget dp size.
 *
 * ```
 * WarpAdaptiveContent(env, calc = { w, h -> adaptiveSizeFrom(w, h) }) {
 *     small { CompactCounter(state) }
 *     medium { WideCounter(state) }
 *     large { TallTodoList(state) }
 * }
 * ```
 */
@Composable
fun WarpAdaptiveContent(
    environment: WidgetEnvironment,
    calc: WarpAdaptiveCalc,
    small: @Composable () -> Unit,
    medium: @Composable () -> Unit = small,
    large: @Composable () -> Unit = medium,
) {
    when (rememberWarpAdaptiveSize(environment, calc)) {
        WarpAdaptiveSize.Small -> small()
        WarpAdaptiveSize.Medium -> medium()
        WarpAdaptiveSize.Large -> large()
    }
}

/** True when [adaptiveSize] is [WarpAdaptiveSize.Small]. */
fun WidgetEnvironment.isSmallAdaptive(): Boolean = adaptiveSize() == WarpAdaptiveSize.Small

/** True when [adaptiveSize] is [WarpAdaptiveSize.Medium]. */
fun WidgetEnvironment.isMediumAdaptive(): Boolean = adaptiveSize() == WarpAdaptiveSize.Medium

/** True when [adaptiveSize] is [WarpAdaptiveSize.Large]. */
fun WidgetEnvironment.isLargeAdaptive(): Boolean = adaptiveSize() == WarpAdaptiveSize.Large
