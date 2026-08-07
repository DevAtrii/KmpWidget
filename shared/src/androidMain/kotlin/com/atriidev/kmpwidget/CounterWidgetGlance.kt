package com.atriidev.kmpwidget

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import com.atriidev.warp_widget.WarpGlanceWidget
import com.atriidev.warp_widget.WarpGlanceWidgetReceiver

class CounterWidgetReceiver : WarpGlanceWidgetReceiver() {
    override val widget get() = CounterWarpWidget
    override fun createGlanceWidget() = CounterGlanceAppWidget()
}

/** Glance host for [CounterWarpWidget]. */
class CounterGlanceAppWidget : WarpGlanceWidget() {
    override val widget get() = CounterWarpWidget
}


@Composable
fun GlanceTester(modifier: GlanceModifier = GlanceModifier) {

    Column(
        verticalAlignment = Alignment.CenterVertically,

    ) {

    }

}







