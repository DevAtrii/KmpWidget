package com.atriidev.kmpwidget

actual class WidgetUpdater {
    actual suspend fun update(counter: Int) {
        println("Widget is not updated")
    }
}