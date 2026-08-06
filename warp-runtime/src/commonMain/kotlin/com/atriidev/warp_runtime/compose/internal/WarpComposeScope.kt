/**
 * Internal machinery that builds the widget tree while [@Composable][androidx.compose.runtime.Composable]
 * functions run.
 *
 * Uses [LocalWarpContainer] (a [androidx.compose.runtime.CompositionLocal]) to track which
 * parent column/row is currently open, similar to how Compose tracks layout scope.
 */
package com.atriidev.warp_runtime.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.atriidev.warp_runtime.nodes.modifier.WarpModifier

/**
 * Holds the root [RootHolder] for the active [composeWarp][com.atriidev.warp_runtime.compose.composeWarp] call.
 *
 * Used as a fallback when no [LocalWarpContainer] is set (top-level nodes attach here).
 */
internal object WarpCompositionRoot {
    /** The root bucket collecting nodes for the current composition. */
    lateinit var holder: RootHolder
}

/**
 * CompositionLocal pointing to the container (column/row/root) that should receive new child nodes.
 *
 * `null` outside of an active WARP composition. Top-level composables fall back to [WarpCompositionRoot.holder].
 */
internal val LocalWarpContainer = staticCompositionLocalOf<WarpContainerHolder?> { null }

/**
 * Clears and re-builds the holder tree on every recomposition pass.
 *
 * Must wrap all WARP composition content so [RootHolder] does not accumulate duplicate
 * nodes when [androidx.compose.runtime.mutableStateOf] state changes trigger recomposition.
 */
@Composable
internal fun WarpRootContent(content: @Composable () -> Unit) {
    val root = WarpCompositionRoot.holder
    root.children.clear()
    CompositionLocalProvider(LocalWarpContainer provides root) {
        content()
    }
}

/**
 * Returns the container that should receive the next child node.
 *
 * Prefers [LocalWarpContainer] when inside a nested column/row; otherwise uses [WarpCompositionRoot.holder].
 */
@Composable
internal fun currentContainer(): WarpContainerHolder =
    LocalWarpContainer.current ?: WarpCompositionRoot.holder

/**
 * Registers a container holder and runs [content] with that holder as the active parent.
 *
 * @param holder Internal column/row holder being added to the tree.
 * @param content Nested composables that become children of [holder].
 */
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

/**
 * Registers a leaf node (text, button) under the current container.
 *
 * @param holder Internal holder for a node with no children.
 */
@Composable
internal fun WarpLeaf(holder: WarpNodeHolder) {
    currentContainer().children.add(holder)
}

/** Internal implementation backing public [com.atriidev.warp_runtime.compose.WarpColumn]. */
@Composable
internal fun WarpColumnComposable(
    modifier: WarpModifier,
    content: @Composable () -> Unit,
) {
    WarpContainer(WarpColumnHolder(modifier = modifier), content)
}

/** Internal implementation backing public [com.atriidev.warp_runtime.compose.WarpRow]. */
@Composable
internal fun WarpRowComposable(
    modifier: WarpModifier,
    content: @Composable () -> Unit,
) {
    WarpContainer(WarpRowHolder(modifier = modifier), content)
}

/** Internal implementation backing public [com.atriidev.warp_runtime.compose.WarpText]. */
@Composable
internal fun WarpTextComposable(
    text: String,
    modifier: WarpModifier,
) {
    WarpLeaf(WarpTextHolder(text = text, modifier = modifier))
}

/** Internal implementation backing public [com.atriidev.warp_runtime.compose.WarpButton]. */
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
