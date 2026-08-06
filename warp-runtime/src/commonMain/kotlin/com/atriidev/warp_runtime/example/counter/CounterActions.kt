package com.atriidev.warp_runtime.example.counter

import com.atriidev.warp_runtime.nodes.actions.WarpActionId

/**
 * Counter widget click action ids.
 *
 * Native hosts decode a [com.atriidev.warp_runtime.nodes.actions.ClickAction] with
 * `action.actionIdAs<CounterActions>()`, then use an exhaustive `when`.
 */
enum class CounterActions(
    override val actionId: String,
) : WarpActionId {
    Increment("increment"),
    Decrement("decrement"),
}
