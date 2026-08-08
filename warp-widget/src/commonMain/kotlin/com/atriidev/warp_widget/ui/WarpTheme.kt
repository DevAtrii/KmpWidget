package com.atriidev.warp_widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import com.atriidev.warp_runtime.nodes.modifiers.WarpColor
import com.atriidev.warp_widget.api.WarpWidgetTheme
import com.atriidev.warp_widget.api.WidgetEnvironment
import com.atriidev.warp_widget.api.WidgetPlatform
import com.atriidev.warp_widget.ui.WarpColors.Companion.Material3Dark
import com.atriidev.warp_widget.ui.WarpColors.Companion.Material3Light
import com.atriidev.warp_widget.ui.WarpColors.Companion.defaultDark
import com.atriidev.warp_widget.ui.WarpColors.Companion.defaultLight
import com.atriidev.warp_widget.ui.WarpColors.Companion.light

/**
 * Material-style color roles for WARP widget UI.
 *
 * Mirrors Glance Material 3 [androidx.glance.color.ColorProviders] roles, using
 * serializable [WarpColor] for cross-platform renderers (Glance + WidgetKit).
 */
@Stable
data class WarpColors(
    val primary: WarpColor,
    val onPrimary: WarpColor,
    val primaryContainer: WarpColor,
    val onPrimaryContainer: WarpColor,
    val secondary: WarpColor,
    val onSecondary: WarpColor,
    val secondaryContainer: WarpColor,
    val onSecondaryContainer: WarpColor,
    val tertiary: WarpColor,
    val onTertiary: WarpColor,
    val tertiaryContainer: WarpColor,
    val onTertiaryContainer: WarpColor,
    val error: WarpColor,
    val onError: WarpColor,
    val errorContainer: WarpColor,
    val onErrorContainer: WarpColor,
    val background: WarpColor,
    val onBackground: WarpColor,
    val surface: WarpColor,
    val onSurface: WarpColor,
    val surfaceVariant: WarpColor,
    val onSurfaceVariant: WarpColor,
    val outline: WarpColor,
    val inverseOnSurface: WarpColor,
    val inverseSurface: WarpColor,
    val inversePrimary: WarpColor,
    /** Widget surface behind content (Glance `widgetBackground`). */
    val widgetBackground: WarpColor,
) {
    companion object {
        /** Material 3 baseline — default on Android. */
        val Material3Light: WarpColors = light(
            primary = "#6750A4",
            onPrimary = "#FFFFFF",
            primaryContainer = "#EADDFF",
            onPrimaryContainer = "#21005D",
            secondary = "#625B71",
            onSecondary = "#FFFFFF",
            secondaryContainer = "#E8DEF8",
            onSecondaryContainer = "#1D192B",
            tertiary = "#7D5260",
            onTertiary = "#FFFFFF",
            tertiaryContainer = "#FFD8E4",
            onTertiaryContainer = "#31111D",
            error = "#B3261E",
            onError = "#FFFFFF",
            errorContainer = "#F9DEDC",
            onErrorContainer = "#410E0B",
            background = "#FFFBFE",
            onBackground = "#1C1B1F",
            surface = "#FFFBFE",
            onSurface = "#1C1B1F",
            surfaceVariant = "#E7E0EC",
            onSurfaceVariant = "#49454F",
            outline = "#79747E",
            inverseSurface = "#313033",
            inverseOnSurface = "#F4EFF4",
            inversePrimary = "#D0BCFF",
            widgetBackground = "#F4F6F8",
        )

        /** Material 3 baseline — default on Android. */
        val Material3Dark: WarpColors = dark(
            primary = "#D0BCFF",
            onPrimary = "#381E72",
            primaryContainer = "#4F378B",
            onPrimaryContainer = "#EADDFF",
            secondary = "#CCC2DC",
            onSecondary = "#332D41",
            secondaryContainer = "#4A4458",
            onSecondaryContainer = "#E8DEF8",
            tertiary = "#EFB8C8",
            onTertiary = "#492532",
            tertiaryContainer = "#633B48",
            onTertiaryContainer = "#FFD8E4",
            error = "#F2B8B5",
            onError = "#601410",
            errorContainer = "#8C1D18",
            onErrorContainer = "#F9DEDC",
            background = "#1C1B1F",
            onBackground = "#E6E1E5",
            surface = "#1C1B1F",
            onSurface = "#E6E1E5",
            surfaceVariant = "#49454F",
            onSurfaceVariant = "#CAC4D0",
            outline = "#938F99",
            inverseSurface = "#E6E1E5",
            inverseOnSurface = "#313033",
            inversePrimary = "#6750A4",
            widgetBackground = "#1B2838",
        )

        /** iOS system-blue light scheme — default on iOS / WidgetKit. */
        val IosLight: WarpColors = light(
            primary = "#007AFF",
            onPrimary = "#FFFFFF",
            primaryContainer = "#D6EBFF",
            onPrimaryContainer = "#004080",
            secondary = "#8E8E93",
            onSecondary = "#FFFFFF",
            secondaryContainer = "#E5E5EA",
            onSecondaryContainer = "#3A3A3C",
            tertiary = "#5856D6",
            onTertiary = "#FFFFFF",
            tertiaryContainer = "#E4E3FA",
            onTertiaryContainer = "#262457",
            error = "#FF3B30",
            onError = "#FFFFFF",
            errorContainer = "#FFE5E3",
            onErrorContainer = "#8A0A00",
            background = "#F2F2F7",
            onBackground = "#000000",
            surface = "#FFFFFF",
            onSurface = "#000000",
            surfaceVariant = "#E5E5EA",
            onSurfaceVariant = "#3C3C43",
            outline = "#C6C6C8",
            inverseSurface = "#1C1C1E",
            inverseOnSurface = "#FFFFFF",
            inversePrimary = "#5AC8FA",
            widgetBackground = "#F2F2F7",
        )

        /** iOS system-blue dark scheme — default on iOS / WidgetKit. */
        val IosDark: WarpColors = dark(
            primary = "#0A84FF",
            onPrimary = "#FFFFFF",
            primaryContainer = "#1C3F5E",
            onPrimaryContainer = "#B3D7FF",
            secondary = "#98989D",
            onSecondary = "#1C1C1E",
            secondaryContainer = "#3A3A3C",
            onSecondaryContainer = "#E5E5EA",
            tertiary = "#5E5CE6",
            onTertiary = "#FFFFFF",
            tertiaryContainer = "#3D3B80",
            onTertiaryContainer = "#E4E3FA",
            error = "#FF453A",
            onError = "#FFFFFF",
            errorContainer = "#8A0A00",
            onErrorContainer = "#FFE5E3",
            background = "#000000",
            onBackground = "#FFFFFF",
            surface = "#1C1C1E",
            onSurface = "#FFFFFF",
            surfaceVariant = "#2C2C2E",
            onSurfaceVariant = "#EBEBF5",
            outline = "#636366",
            inverseSurface = "#FFFFFF",
            inverseOnSurface = "#1C1C1E",
            inversePrimary = "#007AFF",
            widgetBackground = "#1C1C1E",
        )

        /** Platform default light — Material 3 on Android, system blue on iOS. */
        fun defaultLight(platform: WidgetPlatform): WarpColors = when (platform) {
            is WidgetPlatform.Android -> Material3Light
            is WidgetPlatform.Ios -> IosLight
        }

        /** Platform default dark — Material 3 on Android, system blue on iOS. */
        fun defaultDark(platform: WidgetPlatform): WarpColors = when (platform) {
            is WidgetPlatform.Android -> Material3Dark
            is WidgetPlatform.Ios -> IosDark
        }

        /** @see [Material3Light] — use [defaultLight] when [WidgetPlatform] is known. */
        val DefaultLight: WarpColors get() = Material3Light

        /** @see [Material3Dark] — use [defaultDark] when [WidgetPlatform] is known. */
        val DefaultDark: WarpColors get() = Material3Dark

        /** Build a light [WarpColors] from hex strings. */
        fun light(
            primary: String,
            onPrimary: String,
            primaryContainer: String,
            onPrimaryContainer: String,
            secondary: String,
            onSecondary: String,
            secondaryContainer: String,
            onSecondaryContainer: String,
            tertiary: String,
            onTertiary: String,
            tertiaryContainer: String,
            onTertiaryContainer: String,
            error: String,
            onError: String,
            errorContainer: String,
            onErrorContainer: String,
            background: String,
            onBackground: String,
            surface: String,
            onSurface: String,
            surfaceVariant: String,
            onSurfaceVariant: String,
            outline: String,
            inverseSurface: String,
            inverseOnSurface: String,
            inversePrimary: String,
            widgetBackground: String,
        ): WarpColors = scheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            widgetBackground = widgetBackground,
        )

        /** Build a dark [WarpColors] from hex strings — same roles as [light]. */
        fun dark(
            primary: String,
            onPrimary: String,
            primaryContainer: String,
            onPrimaryContainer: String,
            secondary: String,
            onSecondary: String,
            secondaryContainer: String,
            onSecondaryContainer: String,
            tertiary: String,
            onTertiary: String,
            tertiaryContainer: String,
            onTertiaryContainer: String,
            error: String,
            onError: String,
            errorContainer: String,
            onErrorContainer: String,
            background: String,
            onBackground: String,
            surface: String,
            onSurface: String,
            surfaceVariant: String,
            onSurfaceVariant: String,
            outline: String,
            inverseSurface: String,
            inverseOnSurface: String,
            inversePrimary: String,
            widgetBackground: String,
        ): WarpColors = light(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            widgetBackground = widgetBackground,
        )

        private fun scheme(
            primary: String,
            onPrimary: String,
            primaryContainer: String,
            onPrimaryContainer: String,
            secondary: String,
            onSecondary: String,
            secondaryContainer: String,
            onSecondaryContainer: String,
            tertiary: String,
            onTertiary: String,
            tertiaryContainer: String,
            onTertiaryContainer: String,
            error: String,
            onError: String,
            errorContainer: String,
            onErrorContainer: String,
            background: String,
            onBackground: String,
            surface: String,
            onSurface: String,
            surfaceVariant: String,
            onSurfaceVariant: String,
            outline: String,
            inverseSurface: String,
            inverseOnSurface: String,
            inversePrimary: String,
            widgetBackground: String,
        ): WarpColors = WarpColors(
            primary = WarpColor(primary),
            onPrimary = WarpColor(onPrimary),
            primaryContainer = WarpColor(primaryContainer),
            onPrimaryContainer = WarpColor(onPrimaryContainer),
            secondary = WarpColor(secondary),
            onSecondary = WarpColor(onSecondary),
            secondaryContainer = WarpColor(secondaryContainer),
            onSecondaryContainer = WarpColor(onSecondaryContainer),
            tertiary = WarpColor(tertiary),
            onTertiary = WarpColor(onTertiary),
            tertiaryContainer = WarpColor(tertiaryContainer),
            onTertiaryContainer = WarpColor(onTertiaryContainer),
            error = WarpColor(error),
            onError = WarpColor(onError),
            errorContainer = WarpColor(errorContainer),
            onErrorContainer = WarpColor(onErrorContainer),
            background = WarpColor(background),
            onBackground = WarpColor(onBackground),
            surface = WarpColor(surface),
            onSurface = WarpColor(onSurface),
            surfaceVariant = WarpColor(surfaceVariant),
            onSurfaceVariant = WarpColor(onSurfaceVariant),
            outline = WarpColor(outline),
            inverseSurface = WarpColor(inverseSurface),
            inverseOnSurface = WarpColor(inverseOnSurface),
            inversePrimary = WarpColor(inversePrimary),
            widgetBackground = WarpColor(widgetBackground),
        )
    }
}


object WarpTheme {
    /**
     * Access to the current [WarpColors] inside a [WarpTheme] subtree.
     *
     * ```
     * WarpTheme.colors.primary
     * WarpTheme.colors.widgetBackground
     * ```
     */
    val colors: WarpColors
        @Composable
        @ReadOnlyComposable
        get() = LocalWarpColors.current
}

/** Defaults to [WarpColors.DefaultLight] outside [WarpTheme]. */
private val LocalWarpColors = staticCompositionLocalOf { WarpColors.DefaultLight }

/**
 * Top-level theme for WARP widget compose — provides [WarpColors] only (like [androidx.glance.GlanceTheme]).
 *
 * ```
 * WarpTheme(colors = myScheme) {
 *     WarpText("Hello", style = WarpTextStyle(color = WarpTheme.colors.onSurface))
 * }
 * ```
 */
@Composable
private fun WarpTheme(
    colors: WarpColors = LocalWarpColors.current,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalWarpColors provides colors) {
        content()
    }
}

/**
 * Picks [lightColors] or [darkColors] from [darkTheme] (Glance `isDarkMode` style).
 */
@Composable
private fun WarpTheme(
    darkTheme: Boolean,
    lightColors: WarpColors = WarpColors.DefaultLight,
    darkColors: WarpColors = WarpColors.DefaultDark,
    content: @Composable () -> Unit,
) {
    WarpTheme(
        colors = if (darkTheme) darkColors else lightColors,
        content = content,
    )
}

/**
 * Resolves colors from [WidgetEnvironment.theme] — light vs dark/unspecified.
 */
@Composable
fun WarpTheme(
    environment: WidgetEnvironment,
    lightColors: WarpColors = WarpColors.defaultLight(environment.platform),
    darkColors: WarpColors = WarpColors.defaultDark(environment.platform),
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit,
) {
    WarpTheme(
        darkTheme = darkTheme ?: (environment.theme != WarpWidgetTheme.LIGHT),
        lightColors = lightColors,
        darkColors = darkColors,
        content = content,
    )
}



