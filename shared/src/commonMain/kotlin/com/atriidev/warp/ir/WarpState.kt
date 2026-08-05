package com.atriidev.warp.ir

import kotlinx.serialization.Serializable

@Serializable
data class WarpState(
    val values: Map<String, String> = emptyMap(),
) {
    fun get(key: String, defaultValue: String = ""): String = values[key] ?: defaultValue

    fun with(key: String, value: String): WarpState = copy(values = values + (key to value))

    fun resolveText(text: String?, stateKey: String?): String {
        if (text != null) return text
        if (stateKey != null) return get(stateKey, "0")
        return ""
    }
}
