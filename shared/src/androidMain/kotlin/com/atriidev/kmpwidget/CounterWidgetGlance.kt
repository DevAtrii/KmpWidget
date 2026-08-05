package com.atriidev.kmpwidget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text


class CounterWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = counterWidget()

}

fun counterWidget() = CounterWidget()

class CounterWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*>
        get() = PreferencesGlanceStateDefinition

    val counterKey = stringPreferencesKey(COUNTER_KEY)
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            val counter = currentState(counterKey)?.toIntOrNull() ?: 0

            Row(
                modifier = GlanceModifier
                    .background(Color(0xFF00FF00))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Button(
                    text = "-",
                    onClick = actionRunCallback<DecrementAction>()
                )


                Text(
                    text = counter.toString(),
                    modifier = GlanceModifier.defaultWeight()
                )

                Button(
                    text = "+",
                    onClick = actionRunCallback<IncrementAction>()
                )
            }
        }
    }


}


class IncrementAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val store = KmpDataStore(context)
        val value = store.get(COUNTER_KEY, "0").toIntOrNull() ?: 0
        val newValue = value + 1
        store.set(COUNTER_KEY, (newValue).toString())
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[stringPreferencesKey(COUNTER_KEY)] = "$newValue"
        }

        CounterWidget().update(context, glanceId)
    }
}

class DecrementAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val store = KmpDataStore(context)
        val value = store.get(COUNTER_KEY, "0").toIntOrNull() ?: 0
        val newValue = value - 1
        store.set(COUNTER_KEY, (newValue).toString())
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[stringPreferencesKey(COUNTER_KEY)] = "$newValue"
        }
        CounterWidget().update(context, glanceId)
    }
}

















