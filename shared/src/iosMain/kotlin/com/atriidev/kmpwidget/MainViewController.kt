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
import com.atriidev.warp_ui.warpRender
import com.atriidev.warp_widget.WarpWidgetHost
import com.atriidev.warp_widget.WarpWidgetPreferences
import com.atriidev.warp_widget.WarpWidgetSession
import com.atriidev.warp_widget.api.PlatformContext
import com.atriidev.warp_widget.api.WarpWidgetFamily
import com.atriidev.warp_widget.api.WidgetPlatformEnvironment
import com.atriidev.warp_widget.api.makeWidgetEnvironment
import com.atriidev.warp_widget.api.platformContext
import com.atriidev.warp_widget.readWarpWidgetState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import platform.UIKit.UIView

fun MainViewController() = ComposeUIViewController {
    val platformContext = remember {
        CounterWarpWidget.platformContext()
    }
    var count by remember { mutableIntStateOf(0) }

    fun refreshCount() {
        count = runBlocking {
            readWarpWidgetState(platformContext, CounterWarpWidget).count
        }
    }

    LifecycleResumeEffect(Unit) {
        refreshCount()
        onPauseOrDispose { }
    }

    LaunchedEffect(platformContext) {
        while (isActive) {
            val latest = runBlocking {
                readWarpWidgetState(platformContext, CounterWarpWidget).count
            }
            if (latest != count) count = latest
            delay(200)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(),
    ) {
        WarpUiKitPreview(
            count = count,
            platformContext = platformContext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        App(platformContext = platformContext)
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun WarpUiKitPreview(
    count: Int,
    platformContext: PlatformContext,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Widget preview (SwiftUI / UIKit)",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        key(count) {
            val session = remember(count, platformContext) {
                WarpWidgetSession(
                    context = platformContext,
                    environment = makeWidgetEnvironment(
                        platformContext,
                        isPreview = true,
                        platformEnvironment = WidgetPlatformEnvironment.Ios(
                            family = WarpWidgetFamily.SYSTEM_SMALL,
                        ),
                    ),
                    preferences = WarpWidgetPreferences(
                        mapOf(
                            CounterWarpWidget.id to
                                CounterWarpWidget.encodeState(CounterState(count = count)),
                        ),
                    ),
                )
            }
            val holder = remember(session) {
                warpRender(
                    node = WarpWidgetHost.compose(CounterWarpWidget, session),
                    handlers = WarpWidgetHost.handlers(CounterWarpWidget, session),
                )
            }
            UIKitView(
                factory = {
                    // cinterop maps Swift UIView → objcnames; Compose wants platform.UIKit.UIView.
                    @Suppress("CAST_NEVER_SUCCEEDS", "USELESS_CAST")
                    holder.makePreviewView() as UIView
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            )
        }
    }
}
