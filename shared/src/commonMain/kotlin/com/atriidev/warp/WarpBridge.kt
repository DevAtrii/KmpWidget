package com.atriidev.warp

import com.atriidev.warp.ir.WarpState
import com.atriidev.warp.pipeline.WarpIrCodec
import com.atriidev.warp.widgets.CounterWarpWidget

object WarpBridge {
    fun buildCounterWidgetJson(stateValues: Map<String, String>): String {
        val document = CounterWarpWidget.build(WarpState(stateValues))
        return WarpIrCodec.encode(document)
    }

    fun counterWidgetKind(): String = CounterWarpWidget.KIND
}
