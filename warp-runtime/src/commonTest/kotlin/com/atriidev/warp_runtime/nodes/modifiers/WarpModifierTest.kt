package com.atriidev.warp_runtime.nodes.modifiers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WarpModifierTest {

    @Test
    fun padding_chainsSequentiallyAndAddsInsets() {
        val modifier = WarpModifier
            .padding(8)
            .padding(horizontal = 4, vertical = 2)

        assertEquals(2, modifier.elements.size)

        val resolved = modifier.resolvedPadding()
        assertEquals(12, resolved.start)
        assertEquals(12, resolved.end)
        assertEquals(10, resolved.top)
        assertEquals(10, resolved.bottom)
    }

    @Test
    fun then_appendsElementsInOrder() {
        val a = WarpModifier.padding(1)
        val b = WarpModifier.padding(2)
        val combined = a.then(b)

        assertEquals(2, combined.elements.size)
        assertEquals(3, combined.resolvedPadding().start)
    }

    @Test
    fun json_preservesSequentialElements() {
        val json = com.atriidev.warp_runtime.compose.ComposeWarpInternals.warpJson.encodeToString(
            WarpModifier.padding(8).padding(4),
        )
        assertTrue(json.contains("\"elements\""))
        assertTrue(json.contains("\"type\": \"padding\""))
    }
}
