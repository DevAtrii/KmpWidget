package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.style.WarpContentAlignment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Stacks children on top of each other — Glance `Box`-shaped API.
 *
 * JSON `"type"` value: `"box"`.
 *
 * @property modifier Layout styling.
 * @property contentAlignment Alignment of children within the box (default [WarpContentAlignment.TopStart]).
 * @property children Nested nodes (stacked in composition order).
 */
@Serializable
@SerialName("box")
data class WarpBox(
    val modifier: WarpModifier = WarpModifier(),
    val contentAlignment: WarpContentAlignment = WarpContentAlignment.TopStart,
    val children: List<WarpNode> = emptyList(),
) : WarpNode
