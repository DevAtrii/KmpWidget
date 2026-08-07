package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.assets.WarpAsset
import com.atriidev.warp_runtime.nodes.modifiers.WarpColor
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.style.WarpContentScale
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Image leaf — Glance `Image`-shaped API.
 *
 * [asset] is a logical ref ([WarpAsset]); hosts resolve to native images.
 * Use [WarpAsset.System] for SF Symbols on iOS.
 *
 * JSON `"type"` value: `"image"`.
 *
 * @property asset Logical image source.
 * @property contentDescription Accessibility label (`null` = decorative).
 * @property modifier Layout/behavior styling.
 * @property contentScale Scale mode inside bounds.
 * @property tint Optional template tint (SF Symbols / vector icons).
 */
@Serializable
@SerialName("image")
data class WarpImage(
    val asset: WarpAsset,
    val contentDescription: String? = null,
    val modifier: WarpModifier = WarpModifier(),
    val contentScale: WarpContentScale = WarpContentScale.Fit,
    val tint: WarpColor? = null,
) : WarpNode
