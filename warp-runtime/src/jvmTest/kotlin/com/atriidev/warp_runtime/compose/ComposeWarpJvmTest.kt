package com.atriidev.warp_runtime.compose

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * JVM-only smoke test for faster local verification of [sampleCounterWidgetJson].
 *
 * Mirrors part of [ComposeWarpTest] without requiring the iOS simulator link step.
 */
class ComposeWarpJvmTest {

    /** Ensures the sample counter widget JSON contains expected button labels and action IDs. */
    @Test
    fun sampleCounterWidgetJson_containsButtons() {
        val json = sampleCounterWidgetJson()
        assertTrue(json.contains("increment"), "json was: $json")
        assertTrue(json.contains("Counter"))
    }
}
