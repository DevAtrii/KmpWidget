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
import com.atriidev.warp_runtime.nodes.assets.WarpAsset
import com.atriidev.warp_runtime.nodes.modifiers.WarpColor
import com.atriidev.warp_runtime.nodes.modifiers.WarpModifier
import com.atriidev.warp_runtime.nodes.actions.WarpAction
import com.atriidev.warp_runtime.nodes.style.WarpButtonColors
import com.atriidev.warp_runtime.nodes.style.WarpContentAlignment
import com.atriidev.warp_runtime.nodes.style.WarpContentScale
import com.atriidev.warp_runtime.nodes.style.WarpHorizontalAlignment
import com.atriidev.warp_runtime.nodes.style.WarpProgressIndicatorStyle
import com.atriidev.warp_runtime.nodes.style.WarpTextStyle
import com.atriidev.warp_runtime.nodes.style.WarpVerticalAlignment
import com.atriidev.warp_runtime.unit.Dp

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
    verticalAlignment: WarpVerticalAlignment,
    horizontalAlignment: WarpHorizontalAlignment,
    content: @Composable () -> Unit,
) {
    WarpContainer(
        WarpColumnHolder(
            modifier = modifier,
            verticalAlignment = verticalAlignment,
            horizontalAlignment = horizontalAlignment,
        ),
        content,
    )
}

/** Internal implementation backing public [com.atriidev.warp_runtime.compose.WarpRow]. */
@Composable
internal fun WarpRowComposable(
    modifier: WarpModifier,
    horizontalAlignment: WarpHorizontalAlignment,
    verticalAlignment: WarpVerticalAlignment,
    content: @Composable () -> Unit,
) {
    WarpContainer(
        WarpRowHolder(
            modifier = modifier,
            horizontalAlignment = horizontalAlignment,
            verticalAlignment = verticalAlignment,
        ),
        content,
    )
}

/** Internal implementation backing public [com.atriidev.warp_runtime.compose.WarpText]. */
@Composable
internal fun WarpTextComposable(
    text: String,
    modifier: WarpModifier,
    style: WarpTextStyle?,
    maxLines: Int,
) {
    WarpLeaf(
        WarpTextHolder(
            text = text,
            modifier = modifier,
            style = style,
            maxLines = maxLines,
        ),
    )
}

/** Internal implementation backing public [com.atriidev.warp_runtime.compose.WarpButton]. */
@Composable
internal fun WarpButtonComposable(
    text: String,
    onClick: WarpAction,
    modifier: WarpModifier,
    enabled: Boolean,
    style: WarpTextStyle?,
    colors: WarpButtonColors?,
    maxLines: Int,
) {
    WarpLeaf(
        WarpButtonHolder(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            style = style,
            colors = colors,
            maxLines = maxLines,
        ),
    )
}

@Composable
internal fun WarpBoxComposable(
    modifier: WarpModifier,
    contentAlignment: WarpContentAlignment,
    content: @Composable () -> Unit,
) {
    WarpContainer(
        WarpBoxHolder(
            modifier = modifier,
            contentAlignment = contentAlignment,
        ),
        content,
    )
}

@Composable
internal fun WarpSpacerComposable(modifier: WarpModifier) {
    WarpLeaf(WarpSpacerHolder(modifier = modifier))
}

@Composable
internal fun WarpDividerComposable(
    modifier: WarpModifier,
    thickness: Dp,
    color: WarpColor?,
) {
    WarpLeaf(
        WarpDividerHolder(
            modifier = modifier,
            thickness = thickness,
            color = color,
        ),
    )
}

@Composable
internal fun WarpProgressIndicatorComposable(
    modifier: WarpModifier,
    style: WarpProgressIndicatorStyle,
    progress: Float?,
    color: WarpColor?,
    backgroundColor: WarpColor?,
) {
    WarpLeaf(
        WarpProgressIndicatorHolder(
            modifier = modifier,
            style = style,
            progress = progress,
            color = color,
            backgroundColor = backgroundColor,
        ),
    )
}

@Composable
internal fun WarpImageComposable(
    asset: WarpAsset,
    contentDescription: String?,
    modifier: WarpModifier,
    contentScale: WarpContentScale,
    tint: WarpColor?,
) {
    WarpLeaf(
        WarpImageHolder(
            asset = asset,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            tint = tint,
        ),
    )
}
