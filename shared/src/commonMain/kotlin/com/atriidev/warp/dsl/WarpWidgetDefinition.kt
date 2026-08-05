package com.atriidev.warp.dsl

import com.atriidev.warp.ir.WarpAction
import com.atriidev.warp.ir.WarpHorizontalAlignment
import com.atriidev.warp.ir.WarpModifier
import com.atriidev.warp.ir.WarpNode
import com.atriidev.warp.ir.WarpState
import com.atriidev.warp.ir.WarpVerticalAlignment

fun actionRunCallback(
    actionId: String,
    payload: Map<String, String> = emptyMap(),
): WarpAction.Callback = WarpAction.Callback(actionId, payload)

@WarpDsl
class WarpWidgetScope(
    private val state: WarpState,
) {
    private val nodes = mutableListOf<WarpNode>()

    fun row(
        modifier: WarpModifier = WarpModifier(),
        verticalAlignment: WarpVerticalAlignment = WarpVerticalAlignment.CenterVertically,
        horizontalAlignment: WarpHorizontalAlignment = WarpHorizontalAlignment.CenterHorizontally,
        block: WarpWidgetScope.() -> Unit,
    ) {
        val childScope = WarpWidgetScope(state)
        childScope.block()
        nodes += WarpNode.Row(
            modifier = modifier,
            verticalAlignment = verticalAlignment,
            horizontalAlignment = horizontalAlignment,
            children = childScope.nodes,
        )
    }

    fun column(
        modifier: WarpModifier = WarpModifier(),
        verticalAlignment: WarpVerticalAlignment = WarpVerticalAlignment.CenterVertically,
        horizontalAlignment: WarpHorizontalAlignment = WarpHorizontalAlignment.CenterHorizontally,
        block: WarpWidgetScope.() -> Unit,
    ) {
        val childScope = WarpWidgetScope(state)
        childScope.block()
        nodes += WarpNode.Column(
            modifier = modifier,
            verticalAlignment = verticalAlignment,
            horizontalAlignment = horizontalAlignment,
            children = childScope.nodes,
        )
    }

    fun text(
        text: String? = null,
        stateKey: String? = null,
        modifier: WarpModifier = WarpModifier(),
    ) {
        require(text != null || stateKey != null) {
            "Warp Text requires either text or stateKey"
        }
        nodes += WarpNode.Text(
            modifier = modifier,
            text = text,
            stateKey = stateKey,
        )
    }

    fun button(
        label: String,
        action: WarpAction,
        modifier: WarpModifier = WarpModifier(),
    ) {
        nodes += WarpNode.Button(
            modifier = modifier,
            label = label,
            action = action,
        )
    }

    internal fun buildSingleRoot(): WarpNode {
        require(nodes.size == 1) {
            "Warp widget content must produce exactly one root node, found ${nodes.size}"
        }
        return nodes.single()
    }
}

abstract class WarpWidgetDefinition(
    val kind: String,
) {
    protected abstract fun provideContent(scope: WarpWidgetScope)

    fun build(state: WarpState): com.atriidev.warp.ir.WarpDocument {
        val scope = WarpWidgetScope(state)
        provideContent(scope)
        return com.atriidev.warp.ir.WarpDocument(
            widgetKind = kind,
            root = scope.buildSingleRoot(),
        )
    }
}
