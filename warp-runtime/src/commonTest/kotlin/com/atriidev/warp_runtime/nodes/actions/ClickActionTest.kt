package com.atriidev.warp_runtime.nodes.actions

import com.atriidev.warp_runtime.compose.WarpButton
import com.atriidev.warp_runtime.compose.composeWarpToJson
import com.atriidev.warp_runtime.compose.toJson
import com.atriidev.warp_runtime.example.counter.CounterActions
import com.atriidev.warp_runtime.nodes.WarpButton as WarpButtonNode
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Parameterized fixture for auto-codec round-trip tests. */
@Serializable
private sealed class ParamCounterActions {
    @Serializable
    data class SetStep(val step: Int) : ParamCounterActions()
}

private val counterActionsFamily = warpActionFamily(CounterActions.serializer())
private val paramCounterActionsFamily = warpActionFamily(ParamCounterActions.serializer())

/** String-id fixture — numeric todo ids must round-trip as strings on the wire. */
@Serializable
private sealed class TodoIdActions {
    @Serializable
    data class Toggle(val todoId: String) : TodoIdActions()
}

private val todoIdActionsFamily = warpActionFamily(TodoIdActions.serializer())

class ClickActionTest {

    @Test
    fun typedAction_asClickAction_serializesWithTypeDiscriminator() {
        val json = composeWarpToJson {
            WarpButton(
                text = "+",
                onClick = CounterActions.Increment.asClickAction(),
            )
        }

        assertTrue(json.contains("\"onClick\""))
        assertTrue(json.contains("\"type\": \"click\""))
        assertTrue(json.contains("\"actionId\": \"increment\""))
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
    fun typedAction_producesExpectedClickAction() {
        assertEquals(
            ClickAction(actionId = "decrement"),
            CounterActions.Decrement.asClickAction(),
        )
    }

    @Test
    fun actionFamily_decodesWirePayload() {
        assertEquals(
            CounterActions.Increment,
            counterActionsFamily.decode("increment", emptyMap()),
        )
        assertEquals(
            CounterActions.Decrement,
            counterActionsFamily.decode("decrement", emptyMap()),
        )
        assertNull(counterActionsFamily.decode("unknown", emptyMap()))
    }

    @Test
    fun actionFamily_roundTripsThroughClickAction() {
        val wire = CounterActions.Increment.asClickAction()
        assertEquals(
            CounterActions.Increment,
            counterActionsFamily.decode(wire.actionId, wire.parameters),
        )
    }

    @Test
    fun paramFamily_registersActionIds() {
        assertTrue(paramCounterActionsFamily.actionIds.contains("set_step"))
    }

    @Test
    fun actionFamily_roundTripsStringIdParameterAction() {
        val action = TodoIdActions.Toggle(todoId = "1")
        val wire = action.asClickAction()
        assertEquals("toggle", wire.actionId)
        assertEquals("1", wire.parameters["todoId"])
        assertEquals(action, todoIdActionsFamily.decode(wire.actionId, wire.parameters))
    }

    @Test
    fun actionFamily_roundTripsParameterizedAction() {
        val action = ParamCounterActions.SetStep(step = 3)
        val wire = action.asClickAction()
        assertEquals("set_step", wire.actionId)
        assertEquals("3", wire.parameters["step"])
        assertEquals(action, paramCounterActionsFamily.decode(wire.actionId, wire.parameters))
    }
}
