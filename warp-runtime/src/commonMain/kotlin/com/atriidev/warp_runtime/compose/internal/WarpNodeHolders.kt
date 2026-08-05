package com.atriidev.warp_runtime.compose.internal

import com.atriidev.warp_runtime.nodes.WarpButton
import com.atriidev.warp_runtime.nodes.WarpColumn
import com.atriidev.warp_runtime.nodes.WarpNode
import com.atriidev.warp_runtime.nodes.WarpRow
import com.atriidev.warp_runtime.nodes.WarpText
import com.atriidev.warp_runtime.nodes.modifier.WarpModifier

internal interface WarpNodeHolder {
    fun toWarpNode(): WarpNode
}

internal interface WarpContainerHolder {
    val children: MutableList<Any>
}

internal interface WarpContainerNodeHolder : WarpNodeHolder, WarpContainerHolder

internal class RootHolder(
    override val children: MutableList<Any> = mutableListOf(),
) : WarpContainerHolder

internal class WarpColumnHolder(
    var modifier: WarpModifier = WarpModifier(),
    override val children: MutableList<Any> = mutableListOf(),
) : WarpContainerNodeHolder {
    override fun toWarpNode(): WarpNode = WarpColumn(
        modifier = modifier,
        children = children.map { (it as WarpNodeHolder).toWarpNode() },
    )
}

internal class WarpRowHolder(
    var modifier: WarpModifier = WarpModifier(),
    override val children: MutableList<Any> = mutableListOf(),
) : WarpContainerNodeHolder {
    override fun toWarpNode(): WarpNode = WarpRow(
        modifier = modifier,
        children = children.map { (it as WarpNodeHolder).toWarpNode() },
    )
}

internal class WarpTextHolder(
    var text: String,
    var modifier: WarpModifier = WarpModifier(),
) : WarpNodeHolder {
    override fun toWarpNode(): WarpNode = WarpText(text = text, modifier = modifier)
}

internal class WarpButtonHolder(
    var text: String,
    var actionId: String,
    var modifier: WarpModifier = WarpModifier(),
) : WarpNodeHolder {
    override fun toWarpNode(): WarpNode = WarpButton(
        text = text,
        actionId = actionId,
        modifier = modifier,
    )
}

internal fun RootHolder.toWarpNode(): WarpNode = when (children.size) {
    0 -> WarpColumn()
    1 -> (children.first() as WarpNodeHolder).toWarpNode()
    else -> WarpColumn(children = children.map { (it as WarpNodeHolder).toWarpNode() })
}
