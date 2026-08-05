package com.atriidev.warp.ir

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface WarpAction {
    val id: String

    @Serializable
    @SerialName("callback")
    data class Callback(
        override val id: String,
        val payload: Map<String, String> = emptyMap(),
    ) : WarpAction

    @Serializable
    @SerialName("open_url")
    data class OpenUrl(
        val url: String,
    ) : WarpAction {
        override val id: String = "open_url"
    }
}
