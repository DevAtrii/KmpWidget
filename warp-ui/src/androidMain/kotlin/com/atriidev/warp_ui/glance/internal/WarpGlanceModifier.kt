package com.atriidev.warp_ui.glance.internal

import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.layout.padding
import com.atriidev.warp_runtime.nodes.modifier.WarpModifier

internal fun WarpModifier.toGlanceModifier(): GlanceModifier {
    val p = padding
    return GlanceModifier.padding(
        start = p.start.dp,
        end = p.end.dp,
        top = p.top.dp,
        bottom = p.bottom.dp,
    )
}
