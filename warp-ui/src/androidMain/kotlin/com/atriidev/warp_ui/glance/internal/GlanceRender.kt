package com.atriidev.warp_ui.glance.internal

import androidx.compose.runtime.Composable
import androidx.glance.Button
import androidx.glance.action.Action
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.text.Text
import com.atriidev.warp_runtime.nodes.WarpButton
import com.atriidev.warp_runtime.nodes.WarpColumn
import com.atriidev.warp_runtime.nodes.WarpNode
import com.atriidev.warp_runtime.nodes.WarpRow
import com.atriidev.warp_runtime.nodes.WarpText
import com.atriidev.warp_runtime.nodes.actions.ClickAction

@PublishedApi
@Composable
internal fun RenderWarpNode(
    node: WarpNode,
    clickAction: (ClickAction) -> Action,
) {
    when (node) {
        is WarpColumn -> Column(modifier = node.modifier.toGlanceModifier()) {
            node.children.forEach { child ->
                RenderWarpNode(child, clickAction)
            }
        }

        is WarpRow -> Row(modifier = node.modifier.toGlanceModifier()) {
            node.children.forEachIndexed { index, child ->
                val useDefaultWeight = shouldApplyDefaultWeight(node.children, index)
                if (useDefaultWeight && child is WarpText) {
                    Text(
                        text = child.text,
                        modifier = child.modifier.toGlanceModifier().defaultWeight(),
                    )
                } else {
                    RenderWarpNode(child, clickAction)
                }
            }
        }

        is WarpText -> Text(
            text = node.text,
            modifier = node.modifier.toGlanceModifier(),
        )

        is WarpButton -> Button(
            text = node.text,
            modifier = node.modifier.toGlanceModifier(),
            onClick = when (val action = node.onClick) {
                is ClickAction -> clickAction(action)
            },
        )
    }
}

private fun shouldApplyDefaultWeight(children: List<WarpNode>, index: Int): Boolean =
    children.size == 3 && index == 1 && children[index] is WarpText
