/**
 * Entry point for turning [@Composable][androidx.compose.runtime.Composable] widget UI
 * into a serializable [com.atriidev.warp_runtime.nodes.WarpNode] tree or JSON string.
 *
 * Typical flow:
 * 1. Developer writes UI with [WarpColumn], [WarpText], etc.
 * 2. [composeWarp] runs Compose Runtime and collects nodes (recomposing when state changes).
 * 3. [WarpNode.toJson] (or [composeWarpToJson]) produces JSON for platform renderers.
 *
 * For state that changes over time without recreating the composition, use [WarpComposition].
 */
package com.atriidev.warp_runtime.compose

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import com.atriidev.warp_runtime.compose.internal.RootHolder
import com.atriidev.warp_runtime.compose.internal.WarpCompositionRoot
import com.atriidev.warp_runtime.compose.internal.WarpRootContent
import com.atriidev.warp_runtime.compose.internal.toWarpNode
import com.atriidev.warp_runtime.nodes.WarpNode
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/** Shared internals for [composeWarp] and [WarpComposition]. */
internal object ComposeWarpInternals {
    /** JSON encoder used by [WarpNode.toJson]. Pretty-printed with a `"type"` discriminator per node. */
    val warpJson = Json {
        prettyPrint = true
        classDiscriminator = "type"
    }

    /**
     * Minimal [AbstractApplier] required by Compose Runtime.
     *
     * WARP builds its tree via [com.atriidev.warp_runtime.compose.internal.LocalWarpContainer],
     * not via this applier. All applier callbacks are intentionally no-ops.
     */
    val noOpApplier = object : AbstractApplier<Any>(root = Unit) {
        override fun onClear() = Unit
        override fun insertTopDown(index: Int, instance: Any) = Unit
        override fun insertBottomUp(index: Int, instance: Any) = Unit
        override fun move(from: Int, to: Int, count: Int) = Unit
        override fun remove(index: Int, count: Int) = Unit
    }

    /**
     * Sends frames to [clock] until [recomposer] has applied all pending changes.
     *
     * Multiple frames handle cascading invalidations from [androidx.compose.runtime.mutableStateOf].
     */
    suspend fun advanceRecomposition(
        clock: BroadcastFrameClock,
        recomposer: Recomposer,
        nextFrame: () -> Long,
    ) {
        repeat(3) {
            clock.sendFrame(nextFrame())
            recomposer.awaitIdle()
            if (!recomposer.hasPendingWork) return
        }
    }
}

/**
 * Runs [@Composable][androidx.compose.runtime.Composable] composition and returns the resulting [WarpNode] tree.
 *
 * Supports [androidx.compose.runtime.mutableStateOf] inside [content] — the tree is cleared and rebuilt
 * on each recomposition pass before being converted to [WarpNode].
 *
 * For widget state that you pass in from outside, prefer [composeWarp] with an explicit [state] parameter
 * or a long-lived [WarpComposition].
 *
 * @param content The widget UI described with [WarpColumn], [WarpRow], [WarpText], [WarpButton], etc.
 * @return The root [WarpNode] after all pending recompositions have settled.
 */
fun composeWarp(content: @Composable () -> Unit): WarpNode {
    val root = RootHolder()
    val clock = BroadcastFrameClock()
    var frame = 0L

    runBlocking(clock) {
        WarpCompositionRoot.holder = root
        val recomposer = Recomposer(coroutineContext)
        val composition = Composition(ComposeWarpInternals.noOpApplier, recomposer)

        launch {
            recomposer.runRecomposeAndApplyChanges()
        }

        composition.setContent {
            WarpRootContent(content = content)
        }

        ComposeWarpInternals.advanceRecomposition(clock, recomposer) { frame++ }
        recomposer.close()
        composition.dispose()
    }

    return root.toWarpNode()
}

/**
 * Composes widget UI from explicit [state], recomposing whenever you call again with a new [state].
 *
 * This is the simplest state-driven API for widget refresh cycles:
 * ```
 * val tree = composeWarp(widgetState) { state ->
 *     WarpText("Count: ${state.count}")
 * }
 * ```
 *
 * @param state Current widget state read by [content] during composition.
 * @param content UI lambda that receives [state] each time this function is called.
 * @return [WarpNode] tree reflecting the given [state].
 */
fun <S> composeWarp(
    state: S,
    content: @Composable (S) -> Unit,
): WarpNode = composeWarp { content(state) }

/**
 * Convenience wrapper around [composeWarp] that returns pretty-printed JSON.
 *
 * @param content The widget UI described with WARP composables.
 * @return A JSON string representing the composed [WarpNode] tree.
 */
fun composeWarpToJson(content: @Composable () -> Unit): String {
    return composeWarp(content).toJson()
}

/**
 * State-aware variant of [composeWarpToJson].
 *
 * @param state Current widget state passed to [content].
 * @param content UI lambda that receives [state].
 */
fun <S> composeWarpToJson(
    state: S,
    content: @Composable (S) -> Unit,
): String = composeWarp(state, content).toJson()

/**
 * Serializes this [WarpNode] tree to JSON.
 *
 * Each node includes a `"type"` field (for example `"column"`, `"text"`, `"button"`)
 * so polymorphic deserialization is possible on Android or iOS renderers.
 *
 * @return Pretty-printed JSON string.
 */
fun WarpNode.toJson(): String = ComposeWarpInternals.warpJson.encodeToString(this)
