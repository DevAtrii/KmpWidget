package com.atriidev.kmpwidget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.atriidev.warp_runtime.compose.composeWarp
import com.atriidev.warp_runtime.example.counter.CounterWidget
import com.atriidev.warp_ui.WarpClickHandler
import com.atriidev.warp_ui.previewView
import com.atriidev.warp_ui.warpRender
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import platform.UIKit.UIView

fun MainViewController() = ComposeUIViewController {
    val dataStore = remember { KmpDataStore() }
    val widgetUpdater = remember { WidgetUpdater() }
    var count by remember { mutableIntStateOf(0) }

    LifecycleResumeEffect(Unit) {
        count = dataStore.get(COUNTER_KEY, "0").toIntOrNull() ?: 0
        onPauseOrDispose { }
    }

    LaunchedEffect(dataStore) {
        while (isActive) {
            val latest = dataStore.get(COUNTER_KEY, "0").toIntOrNull() ?: 0
            if (latest != count) count = latest
            delay(200)
        }
    }

    val handlers = remember(dataStore, widgetUpdater) {
        counterWidgetClickHandlers(dataStore, widgetUpdater)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
    ) {
        WarpUiKitPreview(
            count = count,
            handlers = handlers,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        App(
            dataStore = dataStore,
            widgetUpdater = widgetUpdater,
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun WarpUiKitPreview(
    count: Int,
    handlers: List<WarpClickHandler<*>>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Widget preview (SwiftUI / UIKit)",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        key(count) {
            val holder = remember(handlers) {
                warpRender(
                    node = composeWarp(CounterWidget.State(count = count), CounterWidget.ui),
                    handlers = handlers,
                )
            }
            UIKitView(
                factory = {
                    @Suppress("UNCHECKED_CAST")
                    holder.previewView() as UIView
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            )
        }
    }
}
