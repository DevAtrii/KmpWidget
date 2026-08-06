/**
 * Mutable holder types used **during** composition.
 *
 * Holders are internal and never serialized. After [composeWarp][com.atriidev.warp_runtime.compose.composeWarp]
 * finishes, each holder is converted to a public, immutable [com.atriidev.warp_runtime.nodes.WarpNode] data class.
 */
package com.atriidev.warp_runtime.compose.internal

import com.atriidev.warp_runtime.nodes.WarpButton
import com.atriidev.warp_runtime.nodes.actions.WarpAction
import com.atriidev.warp_runtime.nodes.WarpColumn
import com.atriidev.warp_runtime.nodes.WarpNode
import com.atriidev.warp_runtime.nodes.WarpRow
import com.atriidev.warp_runtime.nodes.WarpText
import com.atriidev.warp_runtime.nodes.modifier.WarpModifier

/**
 * Something that can be converted to a serializable [WarpNode] after composition ends.
 */
internal interface WarpNodeHolder {
    /** Produces the immutable, serializable node for this holder. */
    fun toWarpNode(): WarpNode
}

/**
 * A node that can contain child holders while the tree is being built.
 */
internal interface WarpContainerHolder {
    /** Mutable list of child holders or nested container holders. */
    val children: MutableList<Any>
}

/** A holder that is both a convertible node and a parent container (column or row). */
internal interface WarpContainerNodeHolder : WarpNodeHolder, WarpContainerHolder

/**
 * Top-level bucket for a single [composeWarp][com.atriidev.warp_runtime.compose.composeWarp] invocation.
 *
 * All root-level composables append their holders here before conversion.
 */
internal class RootHolder(
    override val children: MutableList<Any> = mutableListOf(),
) : WarpContainerHolder

/**
 * Mutable holder for a [WarpColumn] while composables run.
 *
 * @property modifier Column modifier captured from the composable call.
 * @property children Child holders added by nested composables.
 */
internal class WarpColumnHolder(
    var modifier: WarpModifier = WarpModifier(),
    override val children: MutableList<Any> = mutableListOf(),
) : WarpContainerNodeHolder {
    override fun toWarpNode(): WarpNode = WarpColumn(
        modifier = modifier,
        children = children.map { (it as WarpNodeHolder).toWarpNode() },
    )
}

/**
 * Mutable holder for a [WarpRow] while composables run.
 *
 * @property modifier Row modifier captured from the composable call.
 * @property children Child holders added by nested composables.
 */
internal class WarpRowHolder(
    var modifier: WarpModifier = WarpModifier(),
    override val children: MutableList<Any> = mutableListOf(),
) : WarpContainerNodeHolder {
    override fun toWarpNode(): WarpNode = WarpRow(
        modifier = modifier,
        children = children.map { (it as WarpNodeHolder).toWarpNode() },
    )
}

/**
 * Mutable holder for a [WarpText] leaf node.
 *
 * @property text Text content from the composable call.
 * @property modifier Text modifier from the composable call.
 */
internal class WarpTextHolder(
    var text: String,
    var modifier: WarpModifier = WarpModifier(),
) : WarpNodeHolder {
    override fun toWarpNode(): WarpNode = WarpText(text = text, modifier = modifier)
}

/**
 * Mutable holder for a [WarpButton] leaf node.
 *
 * @property text Button label from the composable call.
 * @property onClick Serializable action from the composable call.
 * @property modifier Button modifier from the composable call.
 */
internal class WarpButtonHolder(
    var text: String,
    var onClick: WarpAction,
    var modifier: WarpModifier = WarpModifier(),
) : WarpNodeHolder {
    override fun toWarpNode(): WarpNode = WarpButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
    )
}

/**
 * Converts the [RootHolder] into the final public [WarpNode] returned by [composeWarp][com.atriidev.warp_runtime.compose.composeWarp].
 *
 * - 0 children → empty [WarpColumn]
 * - 1 child → that child directly (no extra wrapper)
 * - 2+ children → wrapped in a [WarpColumn]
 */
internal fun RootHolder.toWarpNode(): WarpNode = when (children.size) {
    0 -> WarpColumn()
    1 -> (children.first() as WarpNodeHolder).toWarpNode()
    else -> WarpColumn(children = children.map { (it as WarpNodeHolder).toWarpNode() })
}
