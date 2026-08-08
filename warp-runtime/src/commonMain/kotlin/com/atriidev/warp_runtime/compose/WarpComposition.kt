/**
 * Stateful, re-usable WARP composition that recomposes when [updateState] is called.
 */
package com.atriidev.warp_runtime.compose

import androidx.compose.runtime.Composable
import com.atriidev.warp_runtime.log.WarpLogger
import com.atriidev.warp_runtime.nodes.WarpNode

/**
 * Holds widget [state] and produces a fresh [WarpNode] tree whenever [updateState] is called.
 *
 * Each update runs a full [composeWarp] pass with the new state — suitable for widget refresh
 * cycles where you load state, compose once, serialize to JSON, and push to the platform renderer.
 *
 * ```
 * val warp = WarpComposition(CounterWidget.State(0), CounterWidget.ui)
 *
 * warp.updateState(CounterState(5)) // recomposes → new WarpNode
 * warp.currentNode().toJson()
 * ```
 *
 * @param S Widget state type (typically a `@Serializable` data class).
 * @param initialState Starting state for the first composition.
 * @param content UI lambda that receives the current state each time composition runs.
 */
class WarpComposition<S>(
    initialState: S,
    private val content: @Composable (S) -> Unit,
) {
    private var state: S = initialState
    private var lastNode: WarpNode = composeWarp(state, content)

    /**
     * Replaces the held state, runs composition again, and returns the new tree.
     *
     * @param newState Updated widget state.
     */
    fun updateState(newState: S): WarpNode {
        WarpLogger.d("WarpComposition", "updateState: recomposing with new state")
        state = newState
        lastNode = composeWarp(state, content)
        return lastNode
    }

    /** Returns the [WarpNode] from the most recent [updateState] or initial composition. */
    fun currentNode(): WarpNode = lastNode

    /** No-op for now; exists for API symmetry with future live-composition backends. */
    fun dispose() = Unit
}
