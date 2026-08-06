package com.atriidev.kmpwidget

import kotlinx.cinterop.ExperimentalForeignApi
import warpWidgetKit.WarpWidgetBridge

actual class WidgetUpdater {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun update(counter: Int) {
        WarpWidgetBridge.shared().reloadTimelines()
    }
}
