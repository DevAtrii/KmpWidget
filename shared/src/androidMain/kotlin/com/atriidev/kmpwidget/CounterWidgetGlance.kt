package com.atriidev.kmpwidget

import com.atriidev.kmpwidget.shared.R
import com.atriidev.warp_ui.glance.WarpDrawableAsset
import com.atriidev.warp_widget.WarpGlanceWidget
import com.atriidev.warp_widget.WarpGlanceWidgetReceiver

class CounterWidgetReceiver : WarpGlanceWidgetReceiver() {
    init {
        ensureRegistered()
    }

    override val widget get() = CounterWarpWidget
    override fun createGlanceWidget() = CounterGlanceAppWidget()
}

/** Glance host for [CounterWarpWidget]. */
class CounterGlanceAppWidget : WarpGlanceWidget() {
    override val widget get() = CounterWarpWidget

    override fun assets(): List<WarpDrawableAsset> = listOf(
        WarpDrawableAsset(CounterAssets.NumberCircle, R.drawable.ic_number_circle),
        WarpDrawableAsset(CounterAssets.Checklist, R.drawable.ic_checklist),
        WarpDrawableAsset(CounterAssets.Circle, R.drawable.ic_circle),
        WarpDrawableAsset(CounterAssets.CheckCircle, R.drawable.ic_check_circle),
    )
}
