package com.atriidev.kmpwidget

import com.atriidev.warp_runtime.compose.WarpComposition
import com.atriidev.warp_runtime.compose.WarpText


fun renderWarpWidget(count: Int) = WarpComposition(count) { count ->
    WarpText(
        text = "Hello $count"
    )
}