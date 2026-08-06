package com.atriidev.warp_runtime.compose

import com.atriidev.warp_runtime.nodes.WarpText
import com.atriidev.warp_runtime.nodes.WarpColumn
import com.atriidev.warp_runtime.nodes.WarpButton
import com.atriidev.warp_runtime.nodes.WarpRow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Cross-platform tests for [composeWarp], [composeWarpToJson], [WarpComposition], and samples.
 *
 * Uses composable lambdas from `commonMain` because test source sets are not processed
 * by the Compose Compiler.
 */
class ComposeWarpTest {

    /** Verifies the sample counter UI produces the expected [WarpNode] tree shape. */
    @Test
    fun composeWarp_buildsSerializableTree() {
        val tree = composeWarp(CounterState(count = 42), sampleCounterWidgetUi)

        val column = assertIs<WarpColumn>(tree)
        assertEquals(2, column.children.size)
        assertIs<WarpText>(column.children[0])
        assertIs<WarpRow>(column.children[1])

        val row = column.children[1] as WarpRow
        assertEquals(3, row.children.size)
        assertEquals("-", (row.children[0] as WarpButton).text)
        assertEquals("increment", (row.children[2] as WarpButton).actionId)
    }

    /** Verifies JSON output includes type discriminators and expected content. */
    @Test
    fun composeWarpToJson_emitsTypeDiscriminator() {
        val json = composeWarpToJson(CounterState(count = 42), sampleCounterWidgetUi)

        assertTrue(json.contains("\"type\""))
        assertTrue(json.contains("\"text\""))
        assertTrue(json.contains("Counter"))
    }

    /** Smoke test for the public [sampleCounterWidgetJson] helper. */
    @Test
    fun sampleCounterWidgetJson_isReadyToPrint() {
        val json = sampleCounterWidgetJson()
        assertTrue(json.contains("increment"))
    }

    /** Verifies [composeWarp] with different [CounterState] values produces different text nodes. */
    @Test
    fun composeWarp_recomposesWhenStateParameterChanges() {
        val treeZero = composeWarp(CounterState(count = 0), sampleCounterWidgetUi)
        val rowZero = (treeZero as WarpColumn).children[1] as WarpRow
        assertEquals("0", (rowZero.children[1] as WarpText).text)

        val treeTen = composeWarp(CounterState(count = 10), sampleCounterWidgetUi)
        val rowTen = (treeTen as WarpColumn).children[1] as WarpRow
        assertEquals("10", (rowTen.children[1] as WarpText).text)
    }

    /** Verifies [WarpComposition] recomposes and returns an updated tree when [WarpComposition.updateState] is called. */
    @Test
    fun warpComposition_updateState_recomposesTree() {
        val composition = WarpComposition(CounterState(count = 0), sampleCounterWidgetUi)

        val initialRow = ((composition.currentNode() as WarpColumn).children[1] as WarpRow)
        assertEquals("0", (initialRow.children[1] as WarpText).text)

        val updated = composition.updateState(CounterState(count = 7))
        val updatedRow = ((updated as WarpColumn).children[1] as WarpRow)
        assertEquals("7", (updatedRow.children[1] as WarpText).text)
    }

    /** Verifies [androidx.compose.runtime.mutableStateOf] changes trigger recomposition inside [composeWarp]. */
    @Test
    fun composeWarp_recomposesOnMutableStateChange() {
        val tree = composeWarp(mutableStateCounterUi)
        assertEquals("5", (tree as WarpText).text)
    }
}
