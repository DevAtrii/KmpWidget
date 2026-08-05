package com.atriidev.warp_runtime.compose

import androidx.compose.runtime.Composable
import com.atriidev.warp_runtime.compose.internal.WarpButtonComposable
import com.atriidev.warp_runtime.compose.internal.WarpColumnComposable
import com.atriidev.warp_runtime.compose.internal.WarpRowComposable
import com.atriidev.warp_runtime.compose.internal.WarpTextComposable
import com.atriidev.warp_runtime.nodes.modifier.WarpModifier

@Composable
fun WarpColumn(
    modifier: WarpModifier = WarpModifier(),
    content: @Composable () -> Unit,
) {
    WarpColumnComposable(modifier = modifier, content = content)
}

@Composable
fun WarpRow(
    modifier: WarpModifier = WarpModifier(),
    content: @Composable () -> Unit,
) {
    WarpRowComposable(modifier = modifier, content = content)
}

@Composable
fun WarpText(
    text: String,
    modifier: WarpModifier = WarpModifier(),
) {
    WarpTextComposable(text = text, modifier = modifier)
}

@Composable
fun WarpButton(
    text: String,
    actionId: String,
    modifier: WarpModifier = WarpModifier(),
) {
    WarpButtonComposable(text = text, actionId = actionId, modifier = modifier)
}
