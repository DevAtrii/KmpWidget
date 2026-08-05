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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val warpJson = Json {
    prettyPrint = true
    classDiscriminator = "type"
}

private object NoOpApplier : AbstractApplier<Any>(root = Unit) {
    override fun onClear() = Unit
    override fun insertTopDown(index: Int, instance: Any) = Unit
    override fun insertBottomUp(index: Int, instance: Any) = Unit
    override fun move(from: Int, to: Int, count: Int) = Unit
    override fun remove(index: Int, count: Int) = Unit
}

/**
 * Runs a single-pass composition and returns the resulting [WarpNode] tree.
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
 * Convenience helper for the PoC: compose UI and return pretty JSON.
 */
fun composeWarpToJson(content: @Composable () -> Unit): String {
    return composeWarp(content).toJson()
}

fun WarpNode.toJson(): String = warpJson.encodeToString(this)
