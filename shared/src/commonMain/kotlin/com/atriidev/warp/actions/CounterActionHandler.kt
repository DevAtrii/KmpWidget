package com.atriidev.warp.actions

import com.atriidev.warp.ir.WarpState

object CounterActions {
    const val INCREMENT = "increment"
    const val DECREMENT = "decrement"
}

object CounterActionHandler {
    fun handle(
        actionId: String,
        counterKey: String,
        state: WarpState,
    ): WarpState {
        val current = state.get(counterKey, "0").toIntOrNull() ?: 0
        val updated = when (actionId) {
            CounterActions.INCREMENT -> current + 1
            CounterActions.DECREMENT -> current - 1
            else -> current
        }
        return state.with(counterKey, updated.toString())
    }
}
