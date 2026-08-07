package com.atriidev.warp_runtime.example.counter

import kotlinx.serialization.Serializable

/** Test fixture — param-less sealed actions for compose tests. */
@Serializable
sealed class CounterActions {
    @Serializable
    data object Increment : CounterActions()

    @Serializable
    data object Decrement : CounterActions()

    @Serializable
    data object Reset : CounterActions()
}
