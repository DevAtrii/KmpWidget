package com.atriidev.warp.ir

import androidx.compose.runtime.Composable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface WarpNode {
    val modifier: WarpModifier

    @Serializable
    @SerialName("row")
    data class Row(
        override val modifier: WarpModifier = WarpModifier(),
        val verticalAlignment: WarpVerticalAlignment = WarpVerticalAlignment.CenterVertically,
        val horizontalAlignment: WarpHorizontalAlignment = WarpHorizontalAlignment.CenterHorizontally,
        val children: List<WarpNode> = emptyList(),
    ) : WarpNode

    @Serializable
    @SerialName("column")
    data class Column(
        override val modifier: WarpModifier = WarpModifier(),
        val verticalAlignment: WarpVerticalAlignment = WarpVerticalAlignment.CenterVertically,
        val horizontalAlignment: WarpHorizontalAlignment = WarpHorizontalAlignment.CenterHorizontally,
        val children: List<WarpNode> = emptyList(),
    ) : WarpNode

    @Serializable
    @SerialName("text")
    data class Text(
        override val modifier: WarpModifier = WarpModifier(),
        val text: String? = null,
        val stateKey: String? = null,
    ) : WarpNode

    @Serializable
    @SerialName("button")
    data class Button(
        override val modifier: WarpModifier = WarpModifier(),
        val label: String,
        val action: WarpAction,
    ) : WarpNode
}
