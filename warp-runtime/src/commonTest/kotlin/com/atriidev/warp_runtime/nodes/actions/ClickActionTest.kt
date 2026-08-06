package com.atriidev.warp_runtime.nodes.actions

import com.atriidev.warp_runtime.compose.WarpButton
import com.atriidev.warp_runtime.compose.composeWarpToJson
import com.atriidev.warp_runtime.compose.toJson
import com.atriidev.warp_runtime.nodes.WarpButton as WarpButtonNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClickActionTest {

    @Test
    fun actionClick_serializesWithTypeDiscriminator() {
        val json = composeWarpToJson {
            WarpButton(
                text = "+",
                onClick = actionClick("increment", "step" to "1"),
            )
        }

        assertTrue(json.contains("\"onClick\""))
        assertTrue(json.contains("\"type\": \"click\""))
        assertTrue(json.contains("\"id\": \"increment\""))
        assertTrue(json.contains("\"step\": \"1\""))
    }

    @Test
    fun warpButton_onClick_serializesNestedAction() {
        val json = WarpButtonNode(
            text = "+",
            onClick = CounterActions.Increment.asClickAction(),
        ).toJson()

        assertTrue(json.contains("\"onClick\""))
        assertTrue(json.contains("\"type\": \"click\""))
        assertTrue(json.contains("\"id\": \"increment\""))
    }

    @Test
    fun warpActionKey_producesExpectedClickAction() {
        assertEquals(
            ClickAction(id = "decrement"),
            CounterActions.Decrement.asClickAction(),
        )
    }
}
