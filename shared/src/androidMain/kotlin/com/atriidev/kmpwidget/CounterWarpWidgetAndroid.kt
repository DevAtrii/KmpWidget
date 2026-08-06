package com.atriidev.kmpwidget

import com.atriidev.warp_widget.WarpWidgetAndroidRegistry

/** Register Glance host for [CounterWarpWidget] (call from Application / Activity). */
fun installCounterWarpWidget() {
    WarpWidgetAndroidRegistry.register(CounterWarpWidget.id) { CounterGlanceAppWidget() }
}
