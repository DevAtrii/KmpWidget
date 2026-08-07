package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.modifiers.WarpColor
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.style.WarpProgressIndicatorStyle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Progress indicator — Glance `CircularProgressIndicator` / `LinearProgressIndicator`.
 *
 * JSON `"type"` value: `"progress_indicator"`.
 *
 * @property modifier Layout styling.
 * @property style Circular or linear (default [WarpProgressIndicatorStyle.Circular]).
 * @property progress `0f..1f` for determinate; `null` = indeterminate
 *   (circular is always indeterminate on Glance).
 * @property color Indicator color; platform default when null.
 * @property backgroundColor Track color for linear; ignored for circular when null.
 */
@Serializable
@SerialName("progress_indicator")
data class WarpProgressIndicator(
    val modifier: WarpModifier = WarpModifier(),
    val style: WarpProgressIndicatorStyle = WarpProgressIndicatorStyle.Circular,
    val progress: Float? = null,
    val color: WarpColor? = null,
    val backgroundColor: WarpColor? = null,
) : WarpNode
