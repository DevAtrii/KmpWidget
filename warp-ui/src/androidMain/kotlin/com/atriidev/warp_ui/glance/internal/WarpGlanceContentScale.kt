package com.atriidev.warp_ui.glance.internal

import androidx.glance.layout.ContentScale
import com.atriidev.warp_runtime.nodes.style.WarpContentScale

internal fun WarpContentScale.toGlance(): ContentScale = when (this) {
    WarpContentScale.Fit -> ContentScale.Fit
    WarpContentScale.Crop -> ContentScale.Crop
    WarpContentScale.FillBounds -> ContentScale.FillBounds
}
