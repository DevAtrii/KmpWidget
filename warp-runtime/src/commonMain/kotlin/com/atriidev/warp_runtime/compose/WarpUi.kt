/**
 * Public [@Composable][androidx.compose.runtime.Composable] DSL for describing widget layouts.
 *
 * These functions look like Jetpack Compose / Glance APIs but produce no pixels.
 * During [composeWarp], each call registers an internal holder that is later converted
 * to a serializable [com.atriidev.warp_runtime.nodes.WarpNode].
 */
package com.atriidev.warp_runtime.compose

import androidx.compose.runtime.Composable
import com.atriidev.warp_runtime.nodes.actions.ClickAction
import com.atriidev.warp_runtime.nodes.actions.WarpAction
import com.atriidev.warp_runtime.nodes.actions.WarpActionId
import com.atriidev.warp_runtime.nodes.actions.actionClick
import com.atriidev.warp_runtime.compose.internal.WarpBoxComposable
import com.atriidev.warp_runtime.compose.internal.WarpButtonComposable
import com.atriidev.warp_runtime.compose.internal.WarpColumnComposable
import com.atriidev.warp_runtime.compose.internal.WarpDividerComposable
import com.atriidev.warp_runtime.compose.internal.WarpProgressIndicatorComposable
import com.atriidev.warp_runtime.compose.internal.WarpRowComposable
import com.atriidev.warp_runtime.compose.internal.WarpSpacerComposable
import com.atriidev.warp_runtime.compose.internal.WarpTextComposable
import com.atriidev.warp_runtime.nodes.modifiers.WarpColor
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.style.WarpButtonColors
import com.atriidev.warp_runtime.nodes.style.WarpContentAlignment
import com.atriidev.warp_runtime.nodes.style.WarpHorizontalAlignment
import com.atriidev.warp_runtime.nodes.style.WarpProgressIndicatorStyle
import com.atriidev.warp_runtime.nodes.style.WarpTextStyle
import com.atriidev.warp_runtime.nodes.style.WarpVerticalAlignment

/**
 * Arranges child nodes vertically — Glance `Column`-shaped API.
 *
 * Maps to [com.atriidev.warp_runtime.nodes.WarpColumn] in the output tree.
 *
 * @param modifier Layout styling.
 * @param verticalAlignment Pack children when shorter than the column.
 * @param horizontalAlignment Align children across the width.
 * @param content Nested composables placed inside this column.
 */
@Composable
fun WarpColumn(
    modifier: WarpModifier = WarpModifier(),
    verticalAlignment: WarpVerticalAlignment = WarpVerticalAlignment.Top,
    horizontalAlignment: WarpHorizontalAlignment = WarpHorizontalAlignment.Start,
    content: @Composable () -> Unit,
) {
    WarpColumnComposable(
        modifier = modifier,
        verticalAlignment = verticalAlignment,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

/**
 * Arranges child nodes horizontally — Glance `Row`-shaped API.
 *
 * Maps to [com.atriidev.warp_runtime.nodes.WarpRow] in the output tree.
 *
 * @param modifier Layout styling.
 * @param horizontalAlignment Pack children when narrower than the row.
 * @param verticalAlignment Align children across the height.
 * @param content Nested composables placed inside this row.
 */
@Composable
fun WarpRow(
    modifier: WarpModifier = WarpModifier(),
    horizontalAlignment: WarpHorizontalAlignment = WarpHorizontalAlignment.Start,
    verticalAlignment: WarpVerticalAlignment = WarpVerticalAlignment.Top,
    content: @Composable () -> Unit,
) {
    WarpRowComposable(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = verticalAlignment,
        content = content,
    )
}

/**
 * Displays read-only text — Glance `Text`-shaped API.
 *
 * Maps to [com.atriidev.warp_runtime.nodes.WarpText] in the output tree.
 *
 * @param text The string shown in the widget.
 * @param modifier Layout styling (padding, weight, …).
 * @param style Optional [WarpTextStyle].
 * @param maxLines Max lines for the text.
 */
@Composable
fun WarpText(
    text: String,
    modifier: WarpModifier = WarpModifier(),
    style: WarpTextStyle? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    WarpTextComposable(
        text = text,
        modifier = modifier,
        style = style,
        maxLines = maxLines,
    )
}

/**
 * Displays a clickable button — Glance `Button`-shaped API.
 *
 * Maps to [com.atriidev.warp_runtime.nodes.WarpButton] in the output tree.
 *
 * [onClick] is stored in JSON as a [WarpAction]. When [modifier] includes
 * [com.atriidev.warp_runtime.nodes.modifiers.WarpModifier.clickable], that wins.
 *
 * @param text Label shown on the button.
 * @param onClick Serializable action for the tap.
 * @param modifier Layout/behavior styling.
 * @param enabled When false, taps are ignored.
 * @param style Optional label [WarpTextStyle].
 * @param colors Optional [WarpButtonColors] chrome.
 * @param maxLines Max lines for the label.
 */
@Composable
fun WarpButton(
    text: String,
    onClick: WarpAction,
    modifier: WarpModifier = WarpModifier(),
    enabled: Boolean = true,
    style: WarpTextStyle? = null,
    colors: WarpButtonColors? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    WarpButtonComposable(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        style = style,
        colors = colors,
        maxLines = maxLines,
    )
}

/**
 * Convenience overload for a typed widget [actionId].
 */
@Composable
fun WarpButton(
    text: String,
    actionId: WarpActionId,
    modifier: WarpModifier = WarpModifier(),
    enabled: Boolean = true,
    style: WarpTextStyle? = null,
    colors: WarpButtonColors? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    WarpButton(
        text = text,
        onClick = actionClick(actionId),
        modifier = modifier,
        enabled = enabled,
        style = style,
        colors = colors,
        maxLines = maxLines,
    )
}

/**
 * Stacks children — Glance `Box`-shaped API.
 *
 * Maps to [com.atriidev.warp_runtime.nodes.WarpBox].
 */
@Composable
fun WarpBox(
    modifier: WarpModifier = WarpModifier(),
    contentAlignment: WarpContentAlignment = WarpContentAlignment.TopStart,
    content: @Composable () -> Unit,
) {
    WarpBoxComposable(
        modifier = modifier,
        contentAlignment = contentAlignment,
        content = content,
    )
}

/**
 * Empty space — Glance `Spacer`-shaped API.
 *
 * Size via [modifier] (`width` / `height` / `size` / `weight`).
 *
 * Maps to [com.atriidev.warp_runtime.nodes.WarpSpacer].
 */
@Composable
fun WarpSpacer(
    modifier: WarpModifier = WarpModifier(),
) {
    WarpSpacerComposable(modifier = modifier)
}

/**
 * Horizontal separator (Material `Divider`-shaped).
 *
 * Maps to [com.atriidev.warp_runtime.nodes.WarpDivider].
 */
@Composable
fun WarpDivider(
    modifier: WarpModifier = WarpModifier(),
    thickness: Int = 1,
    color: WarpColor? = null,
) {
    WarpDividerComposable(
        modifier = modifier,
        thickness = thickness,
        color = color,
    )
}

/**
 * Progress indicator — Glance circular / linear API.
 *
 * Maps to [com.atriidev.warp_runtime.nodes.WarpProgressIndicator].
 *
 * @param progress `0f..1f` determinate; `null` indeterminate.
 */
@Composable
fun WarpProgressIndicator(
    modifier: WarpModifier = WarpModifier(),
    style: WarpProgressIndicatorStyle = WarpProgressIndicatorStyle.Circular,
    progress: Float? = null,
    color: WarpColor? = null,
    backgroundColor: WarpColor? = null,
) {
    WarpProgressIndicatorComposable(
        modifier = modifier,
        style = style,
        progress = progress,
        color = color,
        backgroundColor = backgroundColor,
    )
}
