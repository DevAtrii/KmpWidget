package com.atriidev.kmpwidget

import android.content.Context
import androidx.compose.runtime.remember
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.atriidev.warp_runtime.compose.composeWarp
import com.atriidev.warp_runtime.example.counter.CounterWidget as WarpCounterWidget
import com.atriidev.warp_ui.WarpRender

class CounterWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = counterWidget()
}

fun counterWidget() = CounterWidget()

class CounterWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*>
        get() = PreferencesGlanceStateDefinition

    private val counterKey = stringPreferencesKey(COUNTER_KEY)

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            val counter = currentState(counterKey)?.toIntOrNull() ?: 0
            val node = composeWarp(WarpCounterWidget.State(count = counter), WarpCounterWidget.ui)
            val handlers = remember(context) {
                counterWidgetClickHandlers(
                    dataStore = KmpDataStore(context),
                    widgetUpdater = WidgetUpdater(context),
                )
            }
            WarpRender(node, handlers)
        }
    }
}
