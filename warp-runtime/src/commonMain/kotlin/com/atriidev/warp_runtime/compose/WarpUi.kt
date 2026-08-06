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
import com.atriidev.warp_runtime.compose.internal.WarpButtonComposable
import com.atriidev.warp_runtime.compose.internal.WarpColumnComposable
import com.atriidev.warp_runtime.compose.internal.WarpRowComposable
import com.atriidev.warp_runtime.compose.internal.WarpTextComposable
import com.atriidev.warp_runtime.nodes.modifier.WarpModifier

/**
 * Arranges child nodes vertically (top to bottom).
 *
 * Maps to [com.atriidev.warp_runtime.nodes.WarpColumn] in the output tree.
 *
 * @param modifier Optional layout styling such as padding.
 * @param content Nested composables placed inside this column.
 */
@Composable
fun WarpColumn(
    modifier: WarpModifier = WarpModifier(),
    content: @Composable () -> Unit,
) {
    WarpColumnComposable(modifier = modifier, content = content)
}

/**
 * Arranges child nodes horizontally (left to right).
 *
 * Maps to [com.atriidev.warp_runtime.nodes.WarpRow] in the output tree.
 *
 * @param modifier Optional layout styling such as padding.
 * @param content Nested composables placed inside this row.
 */
@Composable
fun WarpRow(
    modifier: WarpModifier = WarpModifier(),
    content: @Composable () -> Unit,
) {
    WarpRowComposable(modifier = modifier, content = content)
}

/**
 * Displays read-only text in the widget.
 *
 * Maps to [com.atriidev.warp_runtime.nodes.WarpText] in the output tree.
 *
 * @param text The string shown in the widget.
 * @param modifier Optional styling such as padding.
 */
@Composable
fun WarpText(
    text: String,
    modifier: WarpModifier = WarpModifier(),
) {
    WarpTextComposable(text = text, modifier = modifier)
}

/**
 * Displays a clickable button in the widget.
 *
 * Maps to [com.atriidev.warp_runtime.nodes.WarpButton] in the output tree.
 *
 * [onClick] is stored in JSON as a [WarpAction] — use [actionClick] or [ClickAction] directly.
 * Platform code forwards [ClickAction.actionId] and [ClickAction.parameters] to native handlers.
 *
 * @param text Label shown on the button.
 * @param onClick Serializable action for the tap (typically [actionClick] or a [ClickAction]).
 * @param modifier Optional styling such as padding.
 */
@Composable
fun WarpButton(
    text: String,
    onClick: WarpAction,
    modifier: WarpModifier = WarpModifier(),
) {
    WarpButtonComposable(text = text, onClick = onClick, modifier = modifier)
}

/**
 * Convenience overload for a typed widget [actionId].
 */
@Composable
fun WarpButton(
    text: String,
    actionId: WarpActionId,
    modifier: WarpModifier = WarpModifier(),
) {
    WarpButton(text = text, onClick = actionClick(actionId), modifier = modifier)
}
