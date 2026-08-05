package com.atriidev.kmpwidget

expect class WidgetUpdater {

    suspend fun update(counter:Int)

}