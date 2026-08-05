/**
 * Public [@Composable][androidx.compose.runtime.Composable] DSL for describing widget layouts.
 *
 * These functions look like Jetpack Compose / Glance APIs but produce no pixels.
 * During [composeWarp], each call registers an internal holder that is later converted
 * to a serializable [com.atriidev.warp_runtime.nodes.WarpNode].
 */
package com.atriidev.warp_runtime.compose

import androidx.compose.runtime.Composable
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
 * The [actionId] is stored in JSON — not a Kotlin lambda — so platform code can map
 * `"increment"` → a real callback on Android (Glance) or iOS (WidgetKit).
 *
 * @param text Label shown on the button.
 * @param actionId Stable identifier for the click action (must be serializable).
 * @param modifier Optional styling such as padding.
 */
@Composable
fun WarpButton(
    text: String,
    actionId: String,
    modifier: WarpModifier = WarpModifier(),
) {
    WarpButtonComposable(text = text, actionId = actionId, modifier = modifier)
}
