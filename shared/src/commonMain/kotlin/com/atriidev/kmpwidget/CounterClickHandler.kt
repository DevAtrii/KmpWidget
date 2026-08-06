package com.atriidev.kmpwidget

import com.atriidev.warp_runtime.example.counter.CounterActions
import com.atriidev.warp_ui.WarpClickHandler

/**
 * Shared counter click handling for app + widget.
 */
class CounterClickHandler(
    private val dataStore: KmpDataStore,
    private val widgetUpdater: WidgetUpdater,
) : WarpClickHandler<CounterActions>(CounterActions::class, CounterActions.entries) {

    override suspend fun onClick(actionId: CounterActions, parameters: Map<String, String>) {
        println("CounterClickHandler, $actionId, $parameters")
        when (actionId) {
            CounterActions.Increment -> updateCount(delta = +1)
            CounterActions.Decrement -> updateCount(delta = -1)
        }
    }

    private suspend fun updateCount(delta: Int) {
        val value = dataStore.get(COUNTER_KEY, "0").toIntOrNull() ?: 0
        val newValue = value + delta
        dataStore.set(COUNTER_KEY, newValue.toString())
        widgetUpdater.update(newValue)
    }
}

fun counterWidgetClickHandlers(
    dataStore: KmpDataStore,
    widgetUpdater: WidgetUpdater,
): List<WarpClickHandler<*>> = listOf(
    CounterClickHandler(dataStore, widgetUpdater),
)
