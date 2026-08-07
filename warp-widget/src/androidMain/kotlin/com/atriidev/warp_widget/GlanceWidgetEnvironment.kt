package com.atriidev.warp_widget

import android.content.Context
import android.content.res.Configuration
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.datastore.preferences.core.Preferences
import androidx.glance.LocalSize
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
): WidgetEnvironment {
    val config = context.resources.configuration
    val metrics = context.resources.displayMetrics
    val night = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
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
        family = size.toWarpWidgetFamily(),
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
    val size = LocalSize.current
    val prefs = currentState<Preferences>()
    val optionsBundle = LocalAppWidgetOptions.current
    val optionsConfig = remember(optionsBundle) {
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

    val environment = remember(size, context, appWidgetId, isPreview, optionsConfig) {
        glanceWidgetEnvironment(
            context = context,
            size = size,
            isPreview = isPreview,
            appWidgetId = appWidgetId,
            configuration = optionsConfig,
        )
    }
    return remember(environment, prefs) {
        WarpWidgetSession(
            context = PlatformContext(context),
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
