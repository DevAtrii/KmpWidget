package com.atriidev.warp_runtime.nodes.actions

import kotlin.test.Test
import kotlin.test.assertEquals

private enum class LegacyCounterActions(
    override val actionId: String,
) : WarpActionId {
    Increment("increment"),
    Decrement("decrement"),
    Reset("reset"),
}

class ClickActionJvmTest {

    @Test
    fun decodeActionId_decodesLegacyEnumForExhaustiveWhen() {
        val result = when (decodeActionId("increment", LegacyCounterActions::class)) {
            LegacyCounterActions.Increment -> "incremented"
            LegacyCounterActions.Decrement -> "decremented"
            LegacyCounterActions.Reset -> "reset"
        }

        assertEquals("incremented", result)
    }

    @Test
    fun actionIdAs_decodesLegacyEnumForExhaustiveWhen() {
        val action = actionClick(LegacyCounterActions.Increment)

        val result = when (action.actionIdAs<LegacyCounterActions>()) {
            LegacyCounterActions.Increment -> "incremented"
            LegacyCounterActions.Decrement -> "decremented"
            LegacyCounterActions.Reset -> "reset"
        }

        assertEquals("incremented", result)
    }
}
