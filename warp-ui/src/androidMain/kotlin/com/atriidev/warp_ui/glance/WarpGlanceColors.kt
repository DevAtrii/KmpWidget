package com.atriidev.warp_ui.glance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import com.atriidev.warp_runtime.nodes.modifiers.WarpColor
import com.atriidev.warp_ui.glance.internal.toComposeColor

/** Maps [WarpColor] → Glance [ColorProvider], including day/night pairs when configured. */
fun interface WarpGlanceColorResolver {
    fun resolve(color: WarpColor): ColorProvider
}

val LocalWarpGlanceColorResolver = staticCompositionLocalOf<WarpGlanceColorResolver?> { null }

@Composable
@ReadOnlyComposable
internal fun WarpColor.toGlanceColorProvider(): ColorProvider =
    LocalWarpGlanceColorResolver.current?.resolve(this) ?: ColorProvider(toComposeColor())

@Composable
@ReadOnlyComposable
internal fun WarpColor.toGlanceColorProviderOrNull(): ColorProvider? =
    this.let { color ->
        LocalWarpGlanceColorResolver.current?.resolve(color)
            ?: ColorProvider(color.toComposeColor())
    }

/** Fixed color — skips day/night resolver (e.g. semantic red/green buttons). */
internal fun WarpColor.toFixedGlanceColorProvider(): ColorProvider =
    ColorProvider(toComposeColor())

internal fun dayNightColorProvider(day: WarpColor, night: WarpColor): ColorProvider =
    androidx.glance.color.ColorProvider(day.toComposeColor(), night.toComposeColor())

internal fun fixedColorProvider(color: Color): ColorProvider = ColorProvider(color)
