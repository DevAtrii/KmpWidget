package com.atriidev.kmpwidget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.atriidev.warp.glance.WaterIntakeWarpGlanceHost

class WaterIntakeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WaterIntakeWarpGlanceHost.instance
}
