package com.atriidev.warp.glance

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.atriidev.warp.ir.WarpAction
import com.atriidev.warp.ir.WarpHorizontalAlignment
import com.atriidev.warp.ir.WarpNode
import com.atriidev.warp.ir.WarpState
import com.atriidev.warp.ir.WarpVerticalAlignment
import androidx.glance.Button as GlanceButton

object GlanceRenderer {
    @Composable
    fun RenderNode(
        node: WarpNode,
        state: WarpState,
        modifier: GlanceModifier = GlanceModifier,
    ) {
        when (node) {
            is WarpNode.Row -> RenderRow(node, state, modifier)
            is WarpNode.Column -> RenderColumn(node, state, modifier)
            is WarpNode.Text -> RenderText(node, state, modifier)
            is WarpNode.Button -> RenderButton(node, modifier)
        }
    }

    @Composable
    private fun RenderRow(
        node: WarpNode.Row,
        state: WarpState,
        modifier: GlanceModifier,
    ) {
        Row(
            modifier = modifier.then(node.modifier.toGlance()),
            verticalAlignment = node.verticalAlignment.toGlance(),
            horizontalAlignment = node.horizontalAlignment.toGlance(),
        ) {
            node.children.forEach { child ->
                RenderRowChild(child, state)
            }
        }
    }

    @Composable
    private fun RenderColumn(
        node: WarpNode.Column,
        state: WarpState,
        modifier: GlanceModifier,
    ) {
        Column(
            modifier = modifier.then(node.modifier.toGlance()),
            verticalAlignment = node.verticalAlignment.toGlance(),
            horizontalAlignment = node.horizontalAlignment.toGlance(),
        ) {
            node.children.forEach { child ->
                RenderColumnChild(child, state)
            }
        }
    }

    @Composable
    private fun RowScope.RenderRowChild(
        child: WarpNode,
        state: WarpState,
    ) {
        when (child) {
            is WarpNode.Row -> RenderRow(child, state, child.modifier.toGlance())
            is WarpNode.Column -> RenderColumn(child, state, child.modifier.toGlance())
            is WarpNode.Text -> {
                val resolved = resolveText(child, state)
                val textModifier = if (child.modifier.weight != null) {
                    GlanceModifier.defaultWeight()
                } else {
                    child.modifier.toGlance()
                }
                Text(text = resolved, modifier = textModifier)
            }
            is WarpNode.Button -> RenderButton(child, child.modifier.toGlance())
        }
    }

    @Composable
    private fun RenderColumnChild(
        child: WarpNode,
        state: WarpState,
    ) {
        when (child) {
            is WarpNode.Row -> RenderRow(child, state, child.modifier.toGlance())
            is WarpNode.Column -> RenderColumn(child, state, child.modifier.toGlance())
            is WarpNode.Text -> RenderText(child, state, child.modifier.toGlance())
            is WarpNode.Button -> RenderButton(child, child.modifier.toGlance())
        }
    }

    @Composable
    private fun RenderText(
        node: WarpNode.Text,
        state: WarpState,
        modifier: GlanceModifier,
    ) {
        Text(
            text = resolveText(node, state),
            modifier = modifier,
        )
    }

    @Composable
    private fun resolveText(node: WarpNode.Text, state: WarpState): String {
        val glanceState = currentState<Preferences>()
        return node.stateKey?.let { key ->
            glanceState[stringPreferencesKey(key)] ?: state.get(key, "0")
        } ?: node.text.orEmpty()
    }

    @Composable
    private fun RenderButton(
        node: WarpNode.Button,
        modifier: GlanceModifier,
    ) {
        GlanceButton(
            text = node.label,
            modifier = modifier,
            onClick = when (val action = node.action) {
                is WarpAction.Callback -> {
                    val params = buildList {
                        add(WarpActionCallback.ActionIdKey to action.id)
                        action.payload.forEach { (key, value) ->
                            when (key) {
                                "stateKey" -> add(WarpActionCallback.StateKeyKey to value)
                                "widgetKind" -> add(WarpActionCallback.WidgetKindKey to value)
                            }
                        }
                    }
                    actionRunCallback<WarpActionCallback>(actionParametersOf(*params.toTypedArray()))
                }
                is WarpAction.OpenUrl -> actionRunCallback<WarpActionCallback>(
                    actionParametersOf(
                        WarpActionCallback.ActionIdKey to action.id,
                        WarpActionCallback.ActionUrlKey to action.url,
                    ),
                )
            },
        )
    }
}

private fun com.atriidev.warp.ir.WarpModifier.toGlance(includeWeight: Boolean = false): GlanceModifier {
    var result: GlanceModifier = GlanceModifier
    padding?.let { value -> result = result.padding(value.all.dp) }
    background?.let { color -> result = result.background(Color(color.argb)) }
    return result
}

private fun WarpVerticalAlignment.toGlance(): Alignment.Vertical = when (this) {
    WarpVerticalAlignment.Top -> Alignment.Top
    WarpVerticalAlignment.CenterVertically -> Alignment.CenterVertically
    WarpVerticalAlignment.Bottom -> Alignment.Bottom
}

private fun WarpHorizontalAlignment.toGlance(): Alignment.Horizontal = when (this) {
    WarpHorizontalAlignment.Start -> Alignment.Start
    WarpHorizontalAlignment.CenterHorizontally -> Alignment.CenterHorizontally
    WarpHorizontalAlignment.End -> Alignment.End
}
