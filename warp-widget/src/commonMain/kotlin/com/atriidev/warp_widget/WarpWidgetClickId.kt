package com.atriidev.warp_widget

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val wireJson = Json { ignoreUnknownKeys = true }

/**
 * Embed [WARP_WIDGET_ID_PARAM] on the root WARP JSON object **without** rewriting the tree.
 *
 * String insert after the opening `{` — preserves kotlinx pretty-print / number shapes so
 * Swift `JSONSerialization` keeps working. warpWidgetKit merges this into AppIntent params.
 */
internal fun embedWarpWidgetIdInRootJson(json: String, widgetId: WarpWidgetId): String {
    val trimmed = json.trimStart()
    if (!trimmed.startsWith("{")) return json
    if (trimmed.contains("\"$WARP_WIDGET_ID_PARAM\"")) return json
    val escaped = widgetId.value.replace("\\", "\\\\").replace("\"", "\\\"")
    val insertion = "\"$WARP_WIDGET_ID_PARAM\":\"$escaped\","
    return trimmed.replaceFirst("{", "{$insertion")
}

/** Read [WARP_WIDGET_ID_PARAM] from click parameters JSON, if present. */
internal fun extractWarpWidgetIdFromParametersJson(parametersJson: String): WarpWidgetId? {
    val root = runCatching { wireJson.parseToJsonElement(parametersJson) }.getOrNull()
        as? JsonObject ?: return null
    val value = root[WARP_WIDGET_ID_PARAM]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
        ?: return null
    return WarpWidgetId(value)
}
