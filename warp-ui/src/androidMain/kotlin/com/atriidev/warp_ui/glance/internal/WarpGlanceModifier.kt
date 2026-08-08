package com.atriidev.warp_ui.glance.internal

import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Visibility
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.layout.wrapContentSize
import androidx.glance.layout.wrapContentWidth
import androidx.glance.visibility
import com.atriidev.warp_runtime.nodes.actions.ClickAction
import com.atriidev.warp_runtime.nodes.modifiers.WarpAlphaElement
import com.atriidev.warp_runtime.nodes.modifiers.WarpBackgroundElement
import com.atriidev.warp_runtime.nodes.modifiers.WarpBorderElement
import com.atriidev.warp_runtime.nodes.modifiers.WarpClickableElement
import com.atriidev.warp_runtime.nodes.modifiers.WarpCornerRadiusElement
import com.atriidev.warp_runtime.nodes.modifiers.WarpFillMaxHeightElement
import com.atriidev.warp_runtime.nodes.modifiers.WarpFillMaxSizeElement
import com.atriidev.warp_runtime.nodes.modifiers.WarpFillMaxWidthElement
import com.atriidev.warp_runtime.nodes.modifiers.WarpHeightElement
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.modifiers.WarpPaddingElement
import com.atriidev.warp_runtime.nodes.modifiers.WarpSizeElement
import com.atriidev.warp_runtime.nodes.modifiers.WarpVisibility
import com.atriidev.warp_runtime.nodes.modifiers.WarpVisibilityElement
import com.atriidev.warp_runtime.nodes.modifiers.WarpWeightElement
import com.atriidev.warp_runtime.nodes.modifiers.WarpWidthElement
import com.atriidev.warp_runtime.nodes.modifiers.WarpWrapContentHeightElement
import com.atriidev.warp_runtime.nodes.modifiers.WarpWrapContentSizeElement
import com.atriidev.warp_runtime.nodes.modifiers.WarpWrapContentWidthElement

/**
 * Maps sequential [WarpModifier] elements to [GlanceModifier].
 *
 * Only WARP-present elements are applied. Weight applies in Row/Column scope via [hasWeight].
 *
 * @param applyClickable When false, skip clickable elements (e.g. [androidx.glance.Button]
 *   already consumes resolved click via `onClick`).
 */
internal fun WarpModifier.toGlanceModifier(
    clickAction: ((ClickAction) -> Action)? = null,
    applyClickable: Boolean = true,
): GlanceModifier {
    var result: GlanceModifier = GlanceModifier
    for (element in elements) {
        result = when (element) {
            is WarpPaddingElement -> result.padding(
                start = element.start.value.dp,
                end = element.end.value.dp,
                top = element.top.value.dp,
                bottom = element.bottom.value.dp,
            )

            is WarpBackgroundElement ->
                result.background(element.color.toComposeColor())

            is WarpCornerRadiusElement ->
                result.cornerRadius(element.radius.value.dp)

            is WarpClickableElement ->
                if (applyClickable && clickAction != null) {
                    result.clickable(clickAction(element.action))
                } else {
                    result
                }

            is WarpVisibilityElement ->
                result.visibility(element.visibility.toGlanceVisibility())

            is WarpFillMaxWidthElement -> result.fillMaxWidth()
            is WarpFillMaxHeightElement -> result.fillMaxHeight()
            is WarpFillMaxSizeElement -> result.fillMaxSize()
            is WarpWidthElement -> result.width(element.width.value.dp)
            is WarpHeightElement -> result.height(element.height.value.dp)
            is WarpSizeElement -> result.size(element.width.value.dp, element.height.value.dp)
            is WarpWrapContentWidthElement -> result.wrapContentWidth()
            is WarpWrapContentHeightElement -> result.wrapContentHeight()
            is WarpWrapContentSizeElement -> result.wrapContentSize()

            // RowScope weight / unsupported on GlanceModifier today.
            is WarpAlphaElement,
            is WarpBorderElement,
            is WarpWeightElement,
            -> result
        }
    }
    return result
}

internal fun WarpModifier.hasWeight(): Boolean = resolvedWeight() != null

private fun WarpVisibility.toGlanceVisibility(): Visibility = when (this) {
    WarpVisibility.Visible -> Visibility.Visible
    WarpVisibility.Invisible -> Visibility.Invisible
    WarpVisibility.Gone -> Visibility.Gone
}
