package com.atriidev.kmpwidget

import com.atriidev.kmpwidget.shared.R
import com.atriidev.warp_ui.glance.WarpDrawableAsset
import com.atriidev.warp_widget.WarpGlanceWidget
import com.atriidev.warp_widget.WarpGlanceWidgetReceiver

class CounterWidgetReceiver : WarpGlanceWidgetReceiver() {
    override val widget get() = CounterWarpWidget
    override fun createGlanceWidget() = CounterGlanceAppWidget()
}

/** Glance host for [CounterWarpWidget]. */
class CounterGlanceAppWidget : WarpGlanceWidget() {
    override val widget get() = CounterWarpWidget

    override fun assets(): List<WarpDrawableAsset> = listOf(
        // Same string as WarpAsset.System("number.circle.fill") / SF Symbol on iOS.
        WarpDrawableAsset("number.circle.fill", R.drawable.ic_number_circle),
    )
}
