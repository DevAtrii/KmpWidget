package com.atriidev.warp_runtime.nodes.modifiers

import com.atriidev.warp_runtime.nodes.actions.ClickAction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Makes any node tappable. JSON `"type": "clickable"`.
 *
 * When both this and a node `onClick` (e.g. [com.atriidev.warp_runtime.nodes.WarpButton])
 * are present, renderers use **this modifier first**.
 */
@Serializable
@SerialName("clickable")
data class WarpClickableElement(
    val action: ClickAction,
) : WarpModifierElement
