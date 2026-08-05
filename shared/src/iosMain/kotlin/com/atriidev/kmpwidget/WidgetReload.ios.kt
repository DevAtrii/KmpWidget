package com.atriidev.kmpwidget

actual fun reloadWidgetTimelines(kind: String) {
    WidgetCenterBridge.reloadHandler?.invoke(kind)
}

object WidgetCenterBridge {
    var reloadHandler: ((String) -> Unit)? = null
}
