package com.atriidev.warp_runtime.example.counter

import com.atriidev.warp_runtime.nodes.actions.WarpActionId

/**
 * Counter widget click action ids (test fixture).
 */
enum class CounterActions(
    override val actionId: String,
) : WarpActionId {
    Increment("increment"),
    Decrement("decrement"),
    Reset("reset"),
}
