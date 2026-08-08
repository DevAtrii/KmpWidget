package com.atriidev.todowidget.widgets


import com.atriidev.todowidget.R
import com.atriidev.warp_ui.glance.WarpDrawableAsset
import com.atriidev.warp_widget.WarpGlanceWidget
import com.atriidev.warp_widget.WarpGlanceWidgetReceiver
import com.atriidev.warp_widget.WarpWidgetHostApi

class TodoWarpGlanceWidgetReceiver : WarpGlanceWidgetReceiver() {


    override fun createGlanceWidget(): WarpGlanceWidget = TodoWarpGlanceWidget(createWarpWidget())
    override fun createWarpWidget(): WarpWidgetHostApi = TodoWarpWidget

}

class TodoWarpGlanceWidget(
    private val widget: WarpWidgetHostApi,
) : WarpGlanceWidget() {
    override fun createWarpWidget(): WarpWidgetHostApi = widget

    override fun assets(): List<WarpDrawableAsset> = listOf(
        WarpDrawableAsset(TodoAssets.Trash, R.drawable.ic_trash),
        WarpDrawableAsset(TodoAssets.Plus, R.drawable.ic_add),
        WarpDrawableAsset(TodoAssets.Circle, R.drawable.ic_circle),
        WarpDrawableAsset(TodoAssets.CheckCircle, R.drawable.ic_check_circle),
    )
}