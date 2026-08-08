package com.atriidev.warp_ui.glance.internal

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.ProgressIndicatorDefaults
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import com.atriidev.warp_runtime.nodes.WarpBox
import com.atriidev.warp_runtime.nodes.WarpButton
import com.atriidev.warp_runtime.nodes.WarpColumn
import com.atriidev.warp_runtime.nodes.WarpDivider
import com.atriidev.warp_runtime.nodes.WarpImage
import com.atriidev.warp_runtime.nodes.WarpNode
import com.atriidev.warp_runtime.nodes.WarpProgressIndicator
import com.atriidev.warp_runtime.nodes.WarpRow
import com.atriidev.warp_runtime.nodes.WarpSpacer
import com.atriidev.warp_runtime.nodes.WarpText
import com.atriidev.warp_runtime.nodes.actions.ClickAction
import com.atriidev.warp_runtime.nodes.style.WarpProgressIndicatorStyle
import com.atriidev.warp_runtime.nodes.style.WarpTextAlign
import com.atriidev.warp_ui.glance.WarpAndroidAssets

/** Glance / RemoteViews: max direct children per Column or Row. */
private const val GlanceMaxChildrenPerContainer = 10

@PublishedApi
@Composable
internal fun RenderWarpNode(
    node: WarpNode,
    clickAction: (ClickAction) -> Action,
) {
    when (node) {
        is WarpColumn -> RenderColumn(node, clickAction)
        is WarpRow -> RenderRow(node, clickAction)

        is WarpBox -> Box(
            modifier = node.modifier.toGlanceModifier(clickAction),
            contentAlignment = node.contentAlignment.toGlance(),
        ) {
            node.children.forEach { child ->
                RenderWarpNode(child, clickAction)
            }
        }

        is WarpText -> RenderText(node, clickAction)
        is WarpButton -> RenderButton(node, clickAction)
        is WarpSpacer -> Spacer(modifier = node.modifier.toGlanceModifier(clickAction))
        is WarpDivider -> RenderDivider(node, clickAction)
        is WarpProgressIndicator -> RenderProgressIndicator(node, clickAction)
        is WarpImage -> RenderImage(node, clickAction)
    }
}

@Composable
private fun RenderColumn(
    node: WarpColumn,
    clickAction: (ClickAction) -> Action,
    extraModifier: GlanceModifier = GlanceModifier,
) {
    val modifier = node.modifier.toGlanceModifier(clickAction).then(extraModifier)
    val vAlign = node.verticalAlignment.toGlance()
    val hAlign = node.horizontalAlignment.toGlance()
    val chunks = node.children.chunked(GlanceMaxChildrenPerContainer)
    Column(
        modifier = modifier,
        verticalAlignment = vAlign,
        horizontalAlignment = hAlign,
    ) {
        if (chunks.size <= 1) {
            node.children.forEach { child ->
                RenderScopedChild(child, clickAction)
            }
        } else {
            // Nest chunks so each Glance Column stays ≤ 10 children.
            chunks.forEach { chunk ->
                Column(
                    verticalAlignment = vAlign,
                    horizontalAlignment = hAlign,
                ) {
                    chunk.forEach { child ->
                        RenderScopedChild(child, clickAction)
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderRow(
    node: WarpRow,
    clickAction: (ClickAction) -> Action,
    extraModifier: GlanceModifier = GlanceModifier,
) {
    val modifier = node.modifier.toGlanceModifier(clickAction).then(extraModifier)
    val hAlign = node.horizontalAlignment.toGlance()
    val vAlign = node.verticalAlignment.toGlance()
    val chunks = node.children.chunked(GlanceMaxChildrenPerContainer)
    Row(
        modifier = modifier,
        horizontalAlignment = hAlign,
        verticalAlignment = vAlign,
    ) {
        if (chunks.size <= 1) {
            node.children.forEach { child ->
                RenderScopedChild(child, clickAction)
            }
        } else {
            chunks.forEach { chunk ->
                Row(
                    horizontalAlignment = hAlign,
                    verticalAlignment = vAlign,
                ) {
                    chunk.forEach { child ->
                        RenderScopedChild(child, clickAction)
                    }
                }
            }
        }
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
    if (node.children.isNotEmpty()) {
        val baseModifier = node.modifier
            .toGlanceModifier(clickAction, applyClickable = false)
            .then(extraModifier)
        val buttonModifier = if (node.enabled) {
            baseModifier.clickable(clickAction(action))
        } else {
            baseModifier
        }

        Box(
            modifier = buttonModifier,
            contentAlignment = Alignment.Center,
        ) {
            node.children.forEach { child ->
                RenderWarpNode(child, clickAction)
            }
        }
    } else {
        Button(
            text = node.text ?: "",
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
}

@Composable
private fun RenderDivider(
    node: WarpDivider,
    clickAction: (ClickAction) -> Action,
    extraModifier: GlanceModifier = GlanceModifier,
) {
    val color = node.color?.toComposeColor() ?: Color.Gray
    Spacer(
        modifier = node.modifier
            .toGlanceModifier(clickAction)
            .then(extraModifier)
            .fillMaxWidth()
            .height(node.thickness.value.dp)
            .background(color),
    )
}

@SuppressLint("RestrictedApi")
@Composable
private fun RenderProgressIndicator(
    node: WarpProgressIndicator,
    clickAction: (ClickAction) -> Action,
    extraModifier: GlanceModifier = GlanceModifier,
) {
    val modifier = node.modifier.toGlanceModifier(clickAction).then(extraModifier)
    val color = node.color?.let { ColorProvider(it.toComposeColor()) }
        ?: ProgressIndicatorDefaults.IndicatorColorProvider
    when (node.style) {
        WarpProgressIndicatorStyle.Circular -> CircularProgressIndicator(
            modifier = modifier,
            color = color,
        )

        WarpProgressIndicatorStyle.Linear -> {
            val background = node.backgroundColor?.let { ColorProvider(it.toComposeColor()) }
                ?: ProgressIndicatorDefaults.BackgroundColorProvider
            val progress = node.progress
            if (progress != null) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = modifier,
                    color = color,
                    backgroundColor = background,
                )
            } else {
                LinearProgressIndicator(
                    modifier = modifier,
                    color = color,
                    backgroundColor = background,
                )
            }
        }
    }
}

@Composable
private fun RenderImage(
    node: WarpImage,
    clickAction: (ClickAction) -> Action,
    extraModifier: GlanceModifier = GlanceModifier,
) {
    val modifier = node.modifier.toGlanceModifier(clickAction).then(extraModifier)
    val provider = WarpAndroidAssets.resolve(node.asset, LocalContext.current)
    if (provider == null) {
        Spacer(modifier = modifier)
        return
    }
    val tintFilter = node.tint?.let { ColorFilter.tint(ColorProvider(it.toComposeColor())) }
    Image(
        provider = provider,
        contentDescription = node.contentDescription,
        modifier = modifier,
        contentScale = node.contentScale.toGlance(),
        colorFilter = tintFilter,
    )
}

@Composable
private fun RowScope.RenderScopedChild(
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
    is WarpBox -> modifier
    is WarpText -> modifier
    is WarpButton -> modifier
    is WarpSpacer -> modifier
    is WarpDivider -> modifier
    is WarpProgressIndicator -> modifier
    is WarpImage -> modifier
}

@Composable
private fun RenderNodeWithExtra(
    node: WarpNode,
    clickAction: (ClickAction) -> Action,
    extra: GlanceModifier,
) {
    when (node) {
        is WarpColumn -> RenderColumn(node, clickAction, extra)
        is WarpRow -> RenderRow(node, clickAction, extra)

        is WarpBox -> Box(
            modifier = node.modifier.toGlanceModifier(clickAction).then(extra),
            contentAlignment = node.contentAlignment.toGlance(),
        ) {
            node.children.forEach { child ->
                RenderWarpNode(child, clickAction)
            }
        }

        is WarpText -> RenderText(node, clickAction, extra)
        is WarpButton -> RenderButton(node, clickAction, extra)
        is WarpSpacer -> Spacer(modifier = node.modifier.toGlanceModifier(clickAction).then(extra))
        is WarpDivider -> RenderDivider(node, clickAction, extra)
        is WarpProgressIndicator -> RenderProgressIndicator(node, clickAction, extra)
        is WarpImage -> RenderImage(node, clickAction, extra)
    }
}
