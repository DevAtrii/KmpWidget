package com.atriidev.warp_runtime.nodes.actions

/**
 * Shared click action ids for the sample counter widget.
 *
 * Platform modules register handlers for these ids (for example `IncrementAction` on Android).
 */
object CounterActions {
    object Increment : WarpActionKey {
        override val id: String = "increment"
    }

    object Decrement : WarpActionKey {
        override val id: String = "decrement"
    }
}
