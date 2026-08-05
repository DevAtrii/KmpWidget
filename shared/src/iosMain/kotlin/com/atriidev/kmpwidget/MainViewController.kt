package com.atriidev.kmpwidget

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    val dataStore = remember() {
        KmpDataStore()
    }
    val widgetUpdater = remember() {
        WidgetUpdater()
    }

    App(
        dataStore = dataStore,
        widgetUpdater = widgetUpdater
    )
}