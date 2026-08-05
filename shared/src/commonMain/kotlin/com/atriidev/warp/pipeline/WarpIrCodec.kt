package com.atriidev.warp.pipeline

import com.atriidev.warp.ir.WarpDocument
import com.atriidev.warp.ir.WarpState
import com.atriidev.warp.dsl.WarpWidgetDefinition
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object WarpIrCodec {
    private val json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(document: WarpDocument): String = json.encodeToString(document)

    fun decode(raw: String): WarpDocument = json.decodeFromString(raw)
}

object WarpPipeline {
    fun compile(
        definition: WarpWidgetDefinition,
        state: WarpState,
    ): WarpDocument = definition.build(state)
}
