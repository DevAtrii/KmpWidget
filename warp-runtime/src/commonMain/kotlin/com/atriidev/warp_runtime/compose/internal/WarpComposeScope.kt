package com.atriidev.warp_runtime.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.atriidev.warp_runtime.nodes.modifier.WarpModifier

internal object WarpCompositionRoot {
    lateinit var holder: RootHolder
}

internal val LocalWarpContainer = staticCompositionLocalOf<WarpContainerHolder?> { null }

@Composable
internal fun currentContainer(): WarpContainerHolder =
    LocalWarpContainer.current ?: WarpCompositionRoot.holder

@Composable
internal fun WarpContainer(
    holder: WarpContainerNodeHolder,
    content: @Composable () -> Unit,
) {
    currentContainer().children.add(holder)
    CompositionLocalProvider(LocalWarpContainer provides holder) {
        content()
    }
}

@Composable
internal fun WarpLeaf(holder: WarpNodeHolder) {
    currentContainer().children.add(holder)
}

@Composable
internal fun WarpColumnComposable(
    modifier: WarpModifier,
    content: @Composable () -> Unit,
) {
    WarpContainer(WarpColumnHolder(modifier = modifier), content)
}

@Composable
internal fun WarpRowComposable(
    modifier: WarpModifier,
    content: @Composable () -> Unit,
) {
    WarpContainer(WarpRowHolder(modifier = modifier), content)
}

@Composable
internal fun WarpTextComposable(
    text: String,
    modifier: WarpModifier,
) {
    WarpLeaf(WarpTextHolder(text = text, modifier = modifier))
}

@Composable
internal fun WarpButtonComposable(
    text: String,
    actionId: String,
    modifier: WarpModifier,
) {
    WarpLeaf(
        WarpButtonHolder(
            text = text,
            actionId = actionId,
            modifier = modifier,
        ),
    )
}
