package com.atriidev.warp_ui.glance.internal

import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.layout.padding
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier

internal fun WarpModifier.toGlanceModifier(): GlanceModifier {
    val p = resolvedPadding()
    return GlanceModifier.padding(
        start = p.start.dp,
        end = p.end.dp,
        top = p.top.dp,
        bottom = p.bottom.dp,
    )
}
