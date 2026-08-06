/**
 * Internal composable samples used by warp-runtime tests (not example widgets).
 */
package com.atriidev.warp_runtime.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * Demo composable that mutates [androidx.compose.runtime.mutableStateOf] during composition.
 *
 * Used to verify [composeWarp] runs multiple recomposition passes before returning.
 */
internal val mutableStateCounterUi: @Composable () -> Unit = {
    val count = remember { mutableStateOf(0) }
    if (count.value == 0) {
        count.value = 5
    }
    WarpText(count.value.toString())
}
