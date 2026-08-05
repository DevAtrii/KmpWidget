/**
 * Entry point for turning [@Composable][androidx.compose.runtime.Composable] widget UI
 * into a serializable [com.atriidev.warp_runtime.nodes.WarpNode] tree or JSON string.
 *
 * Typical flow:
 * 1. Developer writes UI with [WarpColumn], [WarpText], etc.
 * 2. [composeWarp] runs Compose Runtime once and collects nodes.
 * 3. [WarpNode.toJson] (or [composeWarpToJson]) produces JSON for platform renderers.
 */
package com.atriidev.warp_runtime.compose

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import com.atriidev.warp_runtime.compose.internal.RootHolder
import com.atriidev.warp_runtime.compose.internal.WarpCompositionRoot
import com.atriidev.warp_runtime.compose.internal.toWarpNode
import com.atriidev.warp_runtime.nodes.WarpNode
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/** JSON encoder used by [WarpNode.toJson]. Pretty-printed with a `"type"` discriminator per node. */
private val warpJson = Json {
    prettyPrint = true
    classDiscriminator = "type"
}

/**
 * Minimal [AbstractApplier] required by Compose Runtime.
 *
 * WARP builds its tree via [com.atriidev.warp_runtime.compose.internal.LocalWarpContainer],
 * not via this applier. All applier callbacks are intentionally no-ops.
 */
private object NoOpApplier : AbstractApplier<Any>(root = Unit) {
    override fun onClear() = Unit
    override fun insertTopDown(index: Int, instance: Any) = Unit
    override fun insertBottomUp(index: Int, instance: Any) = Unit
    override fun move(from: Int, to: Int, count: Int) = Unit
    override fun remove(index: Int, count: Int) = Unit
}

/**
 * Runs a single-pass [@Composable][androidx.compose.runtime.Composable] composition and
 * returns the resulting [WarpNode] tree.
 *
 * This is **not** a live UI — composition runs once, holders are converted to data classes,
 * and the result is returned. Suitable for widget refresh cycles where state changes
 * trigger a new tree build.
 *
 * @param content The widget UI described with [WarpColumn], [WarpRow], [WarpText], [WarpButton], etc.
 * @return The root [WarpNode], usually a [com.atriidev.warp_runtime.nodes.WarpColumn] or single leaf node.
 *
 * @see composeWarpToJson
 * @see WarpNode.toJson
 */
fun composeWarp(content: @Composable () -> Unit): WarpNode {
    val root = RootHolder()
    WarpCompositionRoot.holder = root
    val clock = BroadcastFrameClock()

    runBlocking(clock) {
        val recomposer = Recomposer(coroutineContext)
        val composition = Composition(NoOpApplier, recomposer)

        launch {
            recomposer.runRecomposeAndApplyChanges()
        }

        composition.setContent(content)

        clock.sendFrame(0)
        recomposer.awaitIdle()
        recomposer.close()
        composition.dispose()
    }

    return root.toWarpNode()
}

/**
 * Convenience wrapper around [composeWarp] that returns pretty-printed JSON.
 *
 * @param content The widget UI described with WARP composables.
 * @return A JSON string representing the composed [WarpNode] tree.
 *
 * @sample com.atriidev.warp_runtime.compose.sampleCounterWidgetJson
 */
fun composeWarpToJson(content: @Composable () -> Unit): String {
    return composeWarp(content).toJson()
}

/**
 * Serializes this [WarpNode] tree to JSON.
 *
 * Each node includes a `"type"` field (for example `"column"`, `"text"`, `"button"`)
 * so polymorphic deserialization is possible on Android or iOS renderers.
 *
 * @return Pretty-printed JSON string.
 */
fun WarpNode.toJson(): String = warpJson.encodeToString(this)
