package com.atriidev.warp.ir

import kotlinx.serialization.Serializable

@Serializable
data class WarpPadding(
    val all: Int,
)

@Serializable
data class WarpModifier(
    val padding: WarpPadding? = null,
    val background: WarpColor? = null,
    val weight: Float? = null,
) {
    fun padding(all: Int): WarpModifier = copy(padding = WarpPadding(all))

    fun background(color: WarpColor): WarpModifier = copy(background = color)

    fun defaultWeight(): WarpModifier = copy(weight = 1f)
}
