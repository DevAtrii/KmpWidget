package com.atriidev.warp.ir

import com.atriidev.warp.WARP_SCHEMA_VERSION
import kotlinx.serialization.Serializable

@Serializable
data class WarpDocument(
    val schemaVersion: Int = WARP_SCHEMA_VERSION,
    val widgetKind: String,
    val root: WarpNode,
)
