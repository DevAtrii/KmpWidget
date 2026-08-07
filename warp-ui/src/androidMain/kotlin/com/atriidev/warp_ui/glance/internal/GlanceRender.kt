package com.atriidev.warp_ui.glance.internal

import androidx.compose.runtime.Composable
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.text.Text
import com.atriidev.warp_runtime.nodes.WarpButton
import com.atriidev.warp_runtime.nodes.WarpColumn
import com.atriidev.warp_runtime.nodes.WarpNode
import com.atriidev.warp_runtime.nodes.WarpRow
import com.atriidev.warp_runtime.nodes.WarpText
import com.atriidev.warp_runtime.nodes.actions.ClickAction
import com.atriidev.warp_runtime.nodes.style.WarpTextAlign

@PublishedApi
@Composable
internal fun RenderWarpNode(
    node: WarpNode,
    clickAction: (ClickAction) -> Action,
) {
    when (node) {
        is WarpColumn -> Column(
            modifier = node.modifier.toGlanceModifier(clickAction),
            verticalAlignment = node.verticalAlignment.toGlance(),
            horizontalAlignment = node.horizontalAlignment.toGlance(),
        ) {
            node.children.forEach { child ->
                RenderScopedChild(child, clickAction)
            }
        }

        is WarpRow -> Row(
            modifier = node.modifier.toGlanceModifier(clickAction),
            horizontalAlignment = node.horizontalAlignment.toGlance(),
            verticalAlignment = node.verticalAlignment.toGlance(),
        ) {
            node.children.forEach { child ->
                RenderScopedChild(child, clickAction)
            }
        }

        is WarpText -> RenderText(node, clickAction)

        is WarpButton -> RenderButton(node, clickAction)
    }
}

@Composable
private fun RenderText(
    node: WarpText,
    clickAction: (ClickAction) -> Action,
    extraModifier: GlanceModifier = GlanceModifier,
) {
    val modifier = node.modifier.toGlanceModifier(clickAction).then(extraModifier)
    val style = node.style?.toGlanceTextStyle()
    if (style != null) {
        Text(
            text = node.text,
            modifier = modifier,
            style = style,
            maxLines = node.maxLines,
        )
    } else {
        Text(
            text = node.text,
            modifier = modifier,
            maxLines = node.maxLines,
        )
    }
}

@Composable
private fun RenderButton(
    node: WarpButton,
    clickAction: (ClickAction) -> Action,
    extraModifier: GlanceModifier = GlanceModifier,
) {
    val action = node.modifier.resolveClickAction(node.onClick)
        ?: return
    Button(
        text = node.text,
        onClick = clickAction(action),
        modifier = node.modifier
            .toGlanceModifier(clickAction, applyClickable = false)
            .then(extraModifier),
        enabled = node.enabled,
        style = node.style?.toGlanceTextStyle(),
        colors = node.colors.toGlanceButtonColors(),
        maxLines = node.maxLines,
    )
}

@Composable
private fun RowScope.RenderScopedChild(
    child: WarpNode,
    clickAction: (ClickAction) -> Action,
) {
    // Glance: defaultWeight expands the slot; textAlign (only if set) places content in it.
    // Never fillMaxWidth on the weighted child — that clips fixed-size siblings.
    if (child is WarpText && child.modifier.hasWeight()) {
        RenderWeightedText(child, clickAction, GlanceModifier.defaultWeight())
        return
    }

    val extra = if (child.warpModifier().hasWeight()) {
        GlanceModifier.defaultWeight()
    } else {
        GlanceModifier
    }
    RenderNodeWithExtra(child, clickAction, extra)
}

@Composable
private fun ColumnScope.RenderScopedChild(
    child: WarpNode,
    clickAction: (ClickAction) -> Action,
) {
    if (child is WarpText && child.modifier.hasWeight()) {
        RenderWeightedText(child, clickAction, GlanceModifier.defaultWeight())
        return
    }

    val extra = if (child.warpModifier().hasWeight()) {
        GlanceModifier.defaultWeight()
    } else {
        GlanceModifier
    }
    RenderNodeWithExtra(child, clickAction, extra)
}

/**
 * Weighted Text — Glance semantics:
 * - [weightModifier] expands the slot
 * - [WarpTextStyle.textAlign] only if set; otherwise content stays at Start
 */
@Composable
private fun RenderWeightedText(
    node: WarpText,
    clickAction: (ClickAction) -> Action,
    weightModifier: GlanceModifier,
) {
    val textAlign = node.style?.textAlign
    if (textAlign != null) {
        Box(
            modifier = weightModifier,
            contentAlignment = textAlign.toBoxAlignment(),
        ) {
            RenderText(node, clickAction)
        }
    } else {
        RenderText(node, clickAction, weightModifier)
    }
}

private fun WarpTextAlign.toBoxAlignment(): Alignment = when (this) {
    WarpTextAlign.Center -> Alignment.Center
    WarpTextAlign.End -> Alignment.CenterEnd
    WarpTextAlign.Start -> Alignment.CenterStart
}

private fun WarpNode.warpModifier() = when (this) {
    is WarpColumn -> modifier
    is WarpRow -> modifier
    is WarpText -> modifier
    is WarpButton -> modifier
}

@Composable
private fun RenderNodeWithExtra(
    node: WarpNode,
    clickAction: (ClickAction) -> Action,
    extra: GlanceModifier,
) {
    when (node) {
        is WarpColumn -> Column(
            modifier = node.modifier.toGlanceModifier(clickAction).then(extra),
            verticalAlignment = node.verticalAlignment.toGlance(),
            horizontalAlignment = node.horizontalAlignment.toGlance(),
        ) {
            node.children.forEach { child ->
                RenderScopedChild(child, clickAction)
            }
        }

        is WarpRow -> Row(
            modifier = node.modifier.toGlanceModifier(clickAction).then(extra),
            horizontalAlignment = node.horizontalAlignment.toGlance(),
            verticalAlignment = node.verticalAlignment.toGlance(),
        ) {
            node.children.forEach { child ->
                RenderScopedChild(child, clickAction)
            }
        }

        is WarpText -> RenderText(node, clickAction, extra)
        is WarpButton -> RenderButton(node, clickAction, extra)
    }
}
