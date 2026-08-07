package com.atriidev.kmpwidget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.atriidev.warp_ui.WarpRender
import com.atriidev.warp_widget.WarpWidgetAndroidRegistry
import com.atriidev.warp_widget.WarpWidgetHost
import com.atriidev.warp_widget.rememberGlanceWidgetSession

class CounterWidgetReceiver : GlanceAppWidgetReceiver() {
    init {
        // Cold-start taps wake this receiver → register runs → handlers re-bound in warp-widget.
        WarpWidgetAndroidRegistry.register(
            CounterWarpWidget.id,
            CounterWarpWidget,
        ) { glanceAppWidget }
    }

    override val glanceAppWidget: GlanceAppWidget
        get() = CounterGlanceAppWidget()
}

/** Glance host for [CounterWarpWidget]. */
class CounterGlanceAppWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*>
        get() = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            val session = rememberGlanceWidgetSession(context)
            WarpRender(
                node = WarpWidgetHost.compose(CounterWarpWidget, session),
                handlers = WarpWidgetHost.handlers(CounterWarpWidget, session),
            )
        }
    }
}
