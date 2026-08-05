package com.atriidev.kmpwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val dataStore = remember(context) {
                KmpDataStore(context)
            }
            val widgetUpdater = remember(context) {
                WidgetUpdater(context)
            }
            App(
                dataStore = dataStore,
                widgetUpdater = widgetUpdater
            )
        }
    }
}
