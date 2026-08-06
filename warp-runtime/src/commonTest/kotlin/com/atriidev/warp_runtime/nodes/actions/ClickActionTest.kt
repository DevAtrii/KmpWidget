package com.atriidev.warp_runtime.nodes.actions

import com.atriidev.warp_runtime.compose.WarpButton
import com.atriidev.warp_runtime.compose.composeWarpToJson
import com.atriidev.warp_runtime.compose.toJson
import com.atriidev.warp_runtime.example.counter.CounterActions
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
                onClick = actionClick(CounterActions.Increment, "step" to "1"),
            )
        }

        assertTrue(json.contains("\"onClick\""))
        assertTrue(json.contains("\"type\": \"click\""))
        assertTrue(json.contains("\"actionId\": \"increment\""))
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
        assertTrue(json.contains("\"actionId\": \"increment\""))
    }

    @Test
    fun warpActionId_producesExpectedClickAction() {
        assertEquals(
            ClickAction(actionId = "decrement"),
            CounterActions.Decrement.asClickAction(),
        )
    }

    @Test
    fun decodeActionId_decodesWidgetEnumForExhaustiveWhen() {
        val result = when (decodeActionId("increment", CounterActions::class)) {
            CounterActions.Increment -> "incremented"
            CounterActions.Decrement -> "decremented"
        }

        assertEquals("incremented", result)
    }

    @Test
    fun actionIdAs_decodesWidgetEnumForExhaustiveWhen() {
        val action = CounterActions.Increment.asClickAction()

        val result = when (action.actionIdAs<CounterActions>()) {
            CounterActions.Increment -> "incremented"
            CounterActions.Decrement -> "decremented"
        }

        assertEquals("incremented", result)
    }
}
