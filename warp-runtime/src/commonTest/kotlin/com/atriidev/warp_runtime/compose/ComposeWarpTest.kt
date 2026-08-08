package com.atriidev.warp_runtime.compose

import com.atriidev.warp_runtime.nodes.WarpText as WarpTextNode
import com.atriidev.warp_runtime.nodes.WarpColumn as WarpColumnNode
import com.atriidev.warp_runtime.nodes.WarpButton as WarpButtonNode
import com.atriidev.warp_runtime.nodes.WarpRow as WarpRowNode
import com.atriidev.warp_runtime.nodes.actions.ClickAction
import com.atriidev.warp_runtime.nodes.actions.clickActionIdOrNull
import com.atriidev.warp_runtime.example.counter.CounterState
import com.atriidev.warp_runtime.example.counter.sampleCounterWidgetJson
import com.atriidev.warp_runtime.example.counter.sampleCounterWidgetUi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Cross-platform tests for [composeWarp], [composeWarpToJson], [WarpComposition], and samples.
 *
 * Counter fixture: [com.atriidev.warp_runtime.example.counter].
 * [mutableStateCounterUi] lives in `commonMain` ([WarpSamples]) for Compose Compiler coverage.
 */
class ComposeWarpTest {

    /** Verifies the sample counter UI produces the expected [WarpNode] tree shape. */
    @Test
    fun composeWarp_buildsSerializableTree() {
        val tree = composeWarp(CounterState(count = 42), sampleCounterWidgetUi)

        val column = assertIs<WarpColumnNode>(tree)
        assertEquals(2, column.children.size)
        assertIs<WarpTextNode>(column.children[0])
        assertIs<WarpRowNode>(column.children[1])

        val row = column.children[1] as WarpRowNode
        assertEquals(3, row.children.size)
        assertEquals("-", (row.children[0] as WarpButtonNode).text)
        val incrementButton = row.children[2] as WarpButtonNode
        assertIs<ClickAction>(incrementButton.onClick)
        assertEquals("increment", incrementButton.onClick.clickActionIdOrNull())
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
        val rowZero = (treeZero as WarpColumnNode).children[1] as WarpRowNode
        assertEquals("0", (rowZero.children[1] as WarpTextNode).text)

        val treeTen = composeWarp(CounterState(count = 10), sampleCounterWidgetUi)
        val rowTen = (treeTen as WarpColumnNode).children[1] as WarpRowNode
        assertEquals("10", (rowTen.children[1] as WarpTextNode).text)
    }

    /** Verifies [WarpComposition] recomposes and returns an updated tree when [WarpComposition.updateState] is called. */
    @Test
    fun warpComposition_updateState_recomposesTree() {
        val composition = WarpComposition(CounterState(count = 0), sampleCounterWidgetUi)

        val initialRow = ((composition.currentNode() as WarpColumnNode).children[1] as WarpRowNode)
        assertEquals("0", (initialRow.children[1] as WarpTextNode).text)

        val updated = composition.updateState(CounterState(count = 7))
        val updatedRow = ((updated as WarpColumnNode).children[1] as WarpRowNode)
        assertEquals("7", (updatedRow.children[1] as WarpTextNode).text)
    }

    /** Verifies [androidx.compose.runtime.mutableStateOf] changes trigger recomposition inside [composeWarp]. */
    @Test
    fun composeWarp_recomposesOnMutableStateChange() {
        val tree = composeWarp(mutableStateCounterUi)
        assertEquals("5", (tree as WarpTextNode).text)
    }

    /** Verifies [WarpButton] with children composables correctly builds and serializes child nodes. */
    @Test
    fun composeWarp_warpButtonWithChildren_serializesChildren() {
        val tree = composeWarp {
            WarpButton(onClick = ClickAction("test_action")) {
                WarpRow {
                    WarpText(text = "Icon")
                    WarpText(text = "Label")
                }
            }
        }

        val button = assertIs<WarpButtonNode>(tree)
        assertEquals(1, button.children.size)
        val row = assertIs<WarpRowNode>(button.children[0])
        assertEquals(2, row.children.size)

        val json = composeWarpToJson {
            WarpButton(onClick = ClickAction("test_action")) {
                WarpText(text = "Label")
            }
        }
        assertTrue(json.contains("\"children\""))
        assertTrue(json.contains("\"test_action\""))
    }
}
