package com.atriidev.warp_ui.internal

import com.atriidev.warp_ui.WarpClicksRegistry
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import warpWidgetKit.WarpClickBridge
import warpWidgetKit.WarpWidgetBridge

@OptIn(ExperimentalForeignApi::class)
internal object WarpIosBridge {
    private val json = Json { ignoreUnknownKeys = true }

    fun publishNode(nodeJson: String) {
        WarpWidgetBridge.shared().publishNodeJson(nodeJson)
    }

    fun reloadTimelines() {
        WarpWidgetBridge.shared().reloadTimelines()
    }

    fun installClickHandler() {
        WarpClickBridge.shared().setHandler { actionId, parametersJson ->
            runBlocking {
                val parameters = decodeParameters(parametersJson.orEmpty())
                WarpClicksRegistry.dispatch(actionId.orEmpty(), parameters)
            }
        }
    }

    private fun decodeParameters(raw: String): Map<String, String> {
        if (raw.isBlank() || raw == "{}") return emptyMap()
        return json.decodeFromString(raw)
    }
}
