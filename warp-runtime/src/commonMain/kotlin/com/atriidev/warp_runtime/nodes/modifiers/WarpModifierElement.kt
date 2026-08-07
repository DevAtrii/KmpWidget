package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.Serializable

/**
 * One link in a sequential [WarpModifier] chain (Compose-style).
 *
 * Add a new `@Serializable` `@SerialName` implementation in its own file
 * under this package (e.g. [WarpPaddingElement]).
 */
@Serializable
sealed interface WarpModifierElement
