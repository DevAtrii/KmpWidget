package com.atriidev.kmpwidget

import com.atriidev.warp.widgets.CounterWarpWidget

actual class WidgetUpdater {
    private val dataStore = KmpDataStore()

    actual suspend fun update(counter: Int) {
        dataStore.set(COUNTER_KEY, counter.toString())
        reloadWidgetTimelines(CounterWarpWidget.KIND)
    }
}
