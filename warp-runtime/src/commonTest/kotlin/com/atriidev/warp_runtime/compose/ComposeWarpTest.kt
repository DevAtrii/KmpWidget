package com.atriidev.warp_runtime.compose

import com.atriidev.warp_runtime.nodes.WarpButton
import com.atriidev.warp_runtime.nodes.WarpColumn
import com.atriidev.warp_runtime.nodes.WarpRow
import com.atriidev.warp_runtime.nodes.WarpText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ComposeWarpTest {

    @Test
    fun composeWarp_buildsSerializableTree() {
        val tree = composeWarp(sampleCounterWidgetUi)

        val column = assertIs<WarpColumn>(tree)
        assertEquals(2, column.children.size)
        assertIs<WarpText>(column.children[0])
        assertIs<WarpRow>(column.children[1])

        val row = column.children[1] as WarpRow
        assertEquals(3, row.children.size)
        assertEquals("-", (row.children[0] as WarpButton).text)
        assertEquals("increment", (row.children[2] as WarpButton).actionId)
    }

    @Test
    fun composeWarpToJson_emitsTypeDiscriminator() {
        val json = composeWarpToJson(sampleCounterWidgetUi)

        assertTrue(json.contains("\"type\""))
        assertTrue(json.contains("\"text\""))
        assertTrue(json.contains("Counter"))
    }

    @Test
    fun sampleCounterWidgetJson_isReadyToPrint() {
        val json = sampleCounterWidgetJson()
        assertTrue(json.contains("increment"))
    }
}
