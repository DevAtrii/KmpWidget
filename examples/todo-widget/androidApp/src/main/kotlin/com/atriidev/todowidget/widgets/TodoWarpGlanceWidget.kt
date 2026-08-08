package com.atriidev.todowidget.widgets


import com.atriidev.todowidget.R
import com.atriidev.warp_ui.glance.WarpDrawableAsset
import com.atriidev.warp_widget.WarpGlanceWidget
import com.atriidev.warp_widget.WarpGlanceWidgetReceiver
import com.atriidev.warp_widget.WarpWidgetHostApi

class TodoWarpGlanceWidgetReceiver : WarpGlanceWidgetReceiver() {

    override val widget: WarpWidgetHostApi get() = TodoWarpWidget

    override fun createGlanceWidget(): WarpGlanceWidget = TodoWarpGlanceWidget()

}

class TodoWarpGlanceWidget : WarpGlanceWidget() {
    override val widget: WarpWidgetHostApi
        get() = TodoWarpWidget

    override fun assets(): List<WarpDrawableAsset> = listOf(
        WarpDrawableAsset(TodoAssets.Circle, R.drawable.ic_circle),
        WarpDrawableAsset(TodoAssets.CheckCircle, R.drawable.ic_check_circle),
    )
}