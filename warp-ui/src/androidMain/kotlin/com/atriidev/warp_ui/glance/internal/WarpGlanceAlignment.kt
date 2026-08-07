package com.atriidev.warp_ui.glance.internal

import androidx.glance.layout.Alignment
import com.atriidev.warp_runtime.nodes.style.WarpContentAlignment
import com.atriidev.warp_runtime.nodes.style.WarpHorizontalAlignment
import com.atriidev.warp_runtime.nodes.style.WarpVerticalAlignment

internal fun WarpHorizontalAlignment.toGlance(): Alignment.Horizontal = when (this) {
    WarpHorizontalAlignment.Start -> Alignment.Start
    WarpHorizontalAlignment.Center -> Alignment.CenterHorizontally
    WarpHorizontalAlignment.End -> Alignment.End
}

internal fun WarpVerticalAlignment.toGlance(): Alignment.Vertical = when (this) {
    WarpVerticalAlignment.Top -> Alignment.Top
    WarpVerticalAlignment.Center -> Alignment.CenterVertically
    WarpVerticalAlignment.Bottom -> Alignment.Bottom
}

internal fun WarpContentAlignment.toGlance(): Alignment = when (this) {
    WarpContentAlignment.TopStart -> Alignment.TopStart
    WarpContentAlignment.TopCenter -> Alignment.TopCenter
    WarpContentAlignment.TopEnd -> Alignment.TopEnd
    WarpContentAlignment.CenterStart -> Alignment.CenterStart
    WarpContentAlignment.Center -> Alignment.Center
    WarpContentAlignment.CenterEnd -> Alignment.CenterEnd
    WarpContentAlignment.BottomStart -> Alignment.BottomStart
    WarpContentAlignment.BottomCenter -> Alignment.BottomCenter
    WarpContentAlignment.BottomEnd -> Alignment.BottomEnd
}
