package com.atriidev.warp_widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.datastore.preferences.core.Preferences
import androidx.glance.LocalContext
import androidx.glance.LocalGlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.LocalAppWidgetOptions
import androidx.glance.currentState
import com.atriidev.warp_widget.api.PlatformContext
import com.atriidev.warp_widget.api.WarpLayoutDirection
import com.atriidev.warp_widget.api.WarpWidgetConfiguration
import com.atriidev.warp_widget.api.WarpWidgetFamily
import com.atriidev.warp_widget.api.WarpWidgetSize
import com.atriidev.warp_widget.api.WarpWidgetTheme
import com.atriidev.warp_widget.api.WidgetEnvironment
import com.atriidev.warp_widget.api.WidgetPlatformEnvironment
import com.atriidev.warp_widget.api.makeWidgetEnvironment
import java.util.Locale
import java.util.TimeZone

/**
 * Map Glance content [size] + [Context] configuration → WARP [WidgetEnvironment].
 *
 * Call from `provideContent` / [rememberGlanceWidgetSession].
 */
fun glanceWidgetEnvironment(
    context: Context,
    size: DpSize,
    isPreview: Boolean = false,
    appWidgetId: Int? = null,
    configuration: WarpWidgetConfiguration? = null,
    nightModeMask: Int? = null,
): WidgetEnvironment {
    val config = context.resources.configuration
    val metrics = context.resources.displayMetrics
    val mask = nightModeMask ?: (config.uiMode and Configuration.UI_MODE_NIGHT_MASK)
    val night = mask == Configuration.UI_MODE_NIGHT_YES
    val locale: Locale =
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            config.locales[0] ?: Locale.getDefault()
        } else {
            @Suppress("DEPRECATION")
            config.locale ?: Locale.getDefault()
        }
    val rtl = config.layoutDirection == View.LAYOUT_DIRECTION_RTL
    return makeWidgetEnvironment(
        platformContext = PlatformContext(context),
        isPreview = isPreview,
        size = WarpWidgetSize(
            widthDp = size.width.value,
            heightDp = size.height.value,
        ),
        theme = if (night) WarpWidgetTheme.DARK else WarpWidgetTheme.LIGHT,
        locale = locale.toLanguageTag(),
        layoutDirection = if (rtl) WarpLayoutDirection.RTL else WarpLayoutDirection.LTR,
        timeZone = TimeZone.getDefault().id,
        displayScale = metrics.density,
        fontScale = config.fontScale,
        platformEnvironment = WidgetPlatformEnvironment.Android(
            appWidgetId = appWidgetId,
            configuration = configuration,
        ),
    )
}

/**
 * Glance `provideContent` helper: [LocalSize] + [currentState] prefs → [WarpWidgetSession].
 *
 * ```
 * provideContent {
 *   val session = rememberGlanceWidgetSession(context)
 *   WarpRender(
 *     node = WarpWidgetHost.compose(MyWidget, session),
 *     handlers = WarpWidgetHost.handlers(MyWidget, session),
 *   )
 * }
 * ```
 */
@Composable
fun rememberGlanceWidgetSession(
    context: Context,
    appWidgetId: Int? = null,
    isPreview: Boolean = false,
): WarpWidgetSession {
    val glanceContext = LocalContext.current
    val glanceFallbackSize = LocalSize.current
    val optionsBundle = LocalAppWidgetOptions.current
    val optionMinWidth = optionsBundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
    val optionMaxHeight = optionsBundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0)
    val optionMaxWidth = optionsBundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0)
    val optionMinHeight = optionsBundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
    val boundAppWidgetId = (LocalGlanceId.current as? AppWidgetId)?.appWidgetId
    val prefs = currentState<Preferences>()
    val prefLayoutW = prefs[GlanceInternalState.layoutWidthKey]
    val prefLayoutH = prefs[GlanceInternalState.layoutHeightKey]
    val layoutEpoch = prefs[GlanceInternalState.layoutEpochKey] ?: 0L
    val widgetSize = remember(
        optionMinWidth,
        optionMaxHeight,
        optionMaxWidth,
        optionMinHeight,
        glanceFallbackSize.width.value,
        glanceFallbackSize.height.value,
        prefLayoutW,
        prefLayoutH,
        layoutEpoch,
    ) {
        prefs.resolveGlanceWidgetSize(optionsBundle, glanceFallbackSize)
    }
    val optionsConfig = remember(
        optionMinWidth,
        optionMaxHeight,
        optionMaxWidth,
        optionMinHeight,
    ) {
        val keys = optionsBundle.keySet().orEmpty()
        if (keys.isEmpty()) {
            null
        } else {
            WarpWidgetConfiguration(
                parameters = keys.associateWith { key ->
                    optionsBundle.get(key)?.toString().orEmpty()
                },
            )
        }
    }

    val config = glanceContext.resources.configuration
    val nightMode = GlanceInternalState.readNightModeMask(prefs, glanceContext)
    val themeEpoch = prefs[GlanceInternalState.themeEpochKey] ?: 0L
    val localeTag = config.let { cfg ->
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            cfg.locales[0]?.toLanguageTag()
        } else {
            @Suppress("DEPRECATION")
            cfg.locale?.toLanguageTag()
        }
    }
    val layoutDirection = config.layoutDirection
    val fontScale = config.fontScale

    val environment = remember(
        optionMinWidth,
        optionMaxHeight,
        optionMaxWidth,
        optionMinHeight,
        widgetSize.width.value,
        widgetSize.height.value,
        nightMode,
        themeEpoch,
        layoutEpoch,
        localeTag,
        layoutDirection,
        fontScale,
        boundAppWidgetId,
        isPreview,
        optionsConfig,
    ) {
        glanceWidgetEnvironment(
            context = glanceContext,
            size = widgetSize,
            isPreview = isPreview,
            appWidgetId = boundAppWidgetId ?: appWidgetId,
            configuration = optionsConfig,
            nightModeMask = nightMode,
        )
    }
    return remember(environment, prefs) {
        WarpWidgetSession(
            context = PlatformContext(glanceContext),
            environment = environment,
            preferences = prefs.toWarpPreferences(),
        )
    }
}

/** Rough Glance size → [WarpWidgetFamily] (WidgetKit-compatible buckets). */
fun DpSize.toWarpWidgetFamily(): WarpWidgetFamily {
    val w = width.value
    val h = height.value
    val minSide = minOf(w, h)
    val area = w * h
    return when {
        minSide >= 300f || area >= 90_000f -> WarpWidgetFamily.SYSTEM_EXTRA_LARGE
        minSide >= 200f || area >= 40_000f -> WarpWidgetFamily.SYSTEM_LARGE
        w >= 180f || area >= 18_000f -> WarpWidgetFamily.SYSTEM_MEDIUM
        else -> WarpWidgetFamily.SYSTEM_SMALL
    }
}
