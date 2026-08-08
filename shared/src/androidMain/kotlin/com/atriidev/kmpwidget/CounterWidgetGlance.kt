package com.atriidev.kmpwidget

import com.atriidev.kmpwidget.shared.R
import com.atriidev.warp_ui.glance.WarpDrawableAsset
import com.atriidev.warp_widget.WarpGlanceWidget
import com.atriidev.warp_widget.WarpGlanceWidgetReceiver
import com.atriidev.warp_widget.WarpWidgetHostApi

class CounterWidgetReceiver : WarpGlanceWidgetReceiver() {
    override fun createGlanceWidget() = CounterGlanceAppWidget(createWarpWidget())
    override fun createWarpWidget(): WarpWidgetHostApi {
        return CounterWarpWidget
    }
}

/** Glance host for [CounterWarpWidget]. */
class CounterGlanceAppWidget(
    private val widget: WarpWidgetHostApi,
) : WarpGlanceWidget() {
    override fun createWarpWidget(): WarpWidgetHostApi = widget

    override fun assets(): List<WarpDrawableAsset> = listOf(
        WarpDrawableAsset(CounterAssets.NumberCircle, R.drawable.ic_number_circle),
        WarpDrawableAsset(CounterAssets.Checklist, R.drawable.ic_checklist),
        WarpDrawableAsset(CounterAssets.Circle, R.drawable.ic_circle),
        WarpDrawableAsset(CounterAssets.CheckCircle, R.drawable.ic_check_circle),
    )
}
