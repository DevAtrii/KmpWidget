package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Empty space — Glance `Spacer`-shaped API.
 *
 * Size via [modifier] (`width` / `height` / `size` / `weight`).
 *
 * JSON `"type"` value: `"spacer"`.
 */
@Serializable
@SerialName("spacer")
data class WarpSpacer(
    val modifier: WarpModifier = WarpModifier(),
) : WarpNode
