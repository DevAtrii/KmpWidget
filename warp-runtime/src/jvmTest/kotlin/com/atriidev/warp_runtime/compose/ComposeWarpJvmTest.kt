package com.atriidev.warp_runtime.compose

import kotlin.test.Test
import kotlin.test.assertTrue

class ComposeWarpJvmTest {

    @Test
    fun sampleCounterWidgetJson_containsButtons() {
        val json = sampleCounterWidgetJson()
        assertTrue(json.contains("increment"), "json was: $json")
        assertTrue(json.contains("Counter"))
    }
}
