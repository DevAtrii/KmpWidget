package com.atriidev.warp_runtime.example.counter

import com.atriidev.warp_runtime.nodes.actions.WarpActionId

/**
 * Counter widget click action ids.
 *
 * Native hosts receive typed [CounterActions] from [com.atriidev.warp_ui.WarpClickHandler].
 */
enum class CounterActions(
    override val actionId: String,
) : WarpActionId {
    Increment("increment"),
    Decrement("decrement"),
}
