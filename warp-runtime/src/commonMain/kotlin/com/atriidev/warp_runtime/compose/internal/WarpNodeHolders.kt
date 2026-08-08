/**
 * Mutable holder types used **during** composition.
 *
 * Holders are internal and never serialized. After [composeWarp][com.atriidev.warp_runtime.compose.composeWarp]
 * finishes, each holder is converted to a public, immutable [com.atriidev.warp_runtime.nodes.WarpNode] data class.
 */
package com.atriidev.warp_runtime.compose.internal

import com.atriidev.warp_runtime.nodes.WarpBox
import com.atriidev.warp_runtime.nodes.WarpButton
import com.atriidev.warp_runtime.nodes.actions.WarpAction
import com.atriidev.warp_runtime.nodes.WarpColumn
import com.atriidev.warp_runtime.nodes.WarpDivider
import com.atriidev.warp_runtime.nodes.WarpImage
import com.atriidev.warp_runtime.nodes.WarpNode
import com.atriidev.warp_runtime.nodes.WarpProgressIndicator
import com.atriidev.warp_runtime.nodes.WarpRow
import com.atriidev.warp_runtime.nodes.WarpSpacer
import com.atriidev.warp_runtime.nodes.WarpText
import com.atriidev.warp_runtime.nodes.assets.WarpAsset
import com.atriidev.warp_runtime.nodes.modifiers.WarpColor
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.style.WarpButtonColors
import com.atriidev.warp_runtime.nodes.style.WarpContentAlignment
import com.atriidev.warp_runtime.nodes.style.WarpContentScale
import com.atriidev.warp_runtime.nodes.style.WarpHorizontalAlignment
import com.atriidev.warp_runtime.nodes.style.WarpProgressIndicatorStyle
import com.atriidev.warp_runtime.nodes.style.WarpTextStyle
import com.atriidev.warp_runtime.nodes.style.WarpVerticalAlignment
import com.atriidev.warp_runtime.unit.Dp
import com.atriidev.warp_runtime.unit.dp

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
 */
internal class WarpColumnHolder(
    var modifier: WarpModifier = WarpModifier(),
    var verticalAlignment: WarpVerticalAlignment = WarpVerticalAlignment.Top,
    var horizontalAlignment: WarpHorizontalAlignment = WarpHorizontalAlignment.Start,
    override val children: MutableList<Any> = mutableListOf(),
) : WarpContainerNodeHolder {
    override fun toWarpNode(): WarpNode = WarpColumn(
        modifier = modifier,
        verticalAlignment = verticalAlignment,
        horizontalAlignment = horizontalAlignment,
        children = children.map { (it as WarpNodeHolder).toWarpNode() },
    )
}

/**
 * Mutable holder for a [WarpRow] while composables run.
 */
internal class WarpRowHolder(
    var modifier: WarpModifier = WarpModifier(),
    var horizontalAlignment: WarpHorizontalAlignment = WarpHorizontalAlignment.Start,
    var verticalAlignment: WarpVerticalAlignment = WarpVerticalAlignment.Top,
    override val children: MutableList<Any> = mutableListOf(),
) : WarpContainerNodeHolder {
    override fun toWarpNode(): WarpNode = WarpRow(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = verticalAlignment,
        children = children.map { (it as WarpNodeHolder).toWarpNode() },
    )
}

/**
 * Mutable holder for a [WarpText] leaf node.
 */
internal class WarpTextHolder(
    var text: String,
    var modifier: WarpModifier = WarpModifier(),
    var style: WarpTextStyle? = null,
    var maxLines: Int = Int.MAX_VALUE,
) : WarpNodeHolder {
    override fun toWarpNode(): WarpNode = WarpText(
        text = text,
        modifier = modifier,
        style = style,
        maxLines = maxLines,
    )
}

/**
 * Mutable holder for a [WarpButton] leaf node.
 */
internal class WarpButtonHolder(
    var text: String,
    var onClick: WarpAction,
    var modifier: WarpModifier = WarpModifier(),
    var enabled: Boolean = true,
    var style: WarpTextStyle? = null,
    var colors: WarpButtonColors? = null,
    var maxLines: Int = Int.MAX_VALUE,
) : WarpNodeHolder {
    override fun toWarpNode(): WarpNode = WarpButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        style = style,
        colors = colors,
        maxLines = maxLines,
    )
}

internal class WarpBoxHolder(
    var modifier: WarpModifier = WarpModifier(),
    var contentAlignment: WarpContentAlignment = WarpContentAlignment.TopStart,
    override val children: MutableList<Any> = mutableListOf(),
) : WarpContainerNodeHolder {
    override fun toWarpNode(): WarpNode = WarpBox(
        modifier = modifier,
        contentAlignment = contentAlignment,
        children = children.map { (it as WarpNodeHolder).toWarpNode() },
    )
}

internal class WarpSpacerHolder(
    var modifier: WarpModifier = WarpModifier(),
) : WarpNodeHolder {
    override fun toWarpNode(): WarpNode = WarpSpacer(modifier = modifier)
}

internal class WarpDividerHolder(
    var modifier: WarpModifier = WarpModifier(),
    var thickness: Dp = 1.dp,
    var color: WarpColor? = null,
) : WarpNodeHolder {
    override fun toWarpNode(): WarpNode = WarpDivider(
        modifier = modifier,
        thickness = thickness,
        color = color,
    )
}

internal class WarpProgressIndicatorHolder(
    var modifier: WarpModifier = WarpModifier(),
    var style: WarpProgressIndicatorStyle = WarpProgressIndicatorStyle.Circular,
    var progress: Float? = null,
    var color: WarpColor? = null,
    var backgroundColor: WarpColor? = null,
) : WarpNodeHolder {
    override fun toWarpNode(): WarpNode = WarpProgressIndicator(
        modifier = modifier,
        style = style,
        progress = progress,
        color = color,
        backgroundColor = backgroundColor,
    )
}

internal class WarpImageHolder(
    var asset: WarpAsset,
    var contentDescription: String? = null,
    var modifier: WarpModifier = WarpModifier(),
    var contentScale: WarpContentScale = WarpContentScale.Fit,
    var tint: WarpColor? = null,
) : WarpNodeHolder {
    override fun toWarpNode(): WarpNode = WarpImage(
        asset = asset,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        tint = tint,
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
