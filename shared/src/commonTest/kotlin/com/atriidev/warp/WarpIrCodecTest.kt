package com.atriidev.warp

import com.atriidev.kmpwidget.COUNTER_KEY
import com.atriidev.warp.actions.CounterActionHandler
import com.atriidev.warp.actions.CounterActions
import com.atriidev.warp.ir.WarpDocument
import com.atriidev.warp.ir.WarpNode
import com.atriidev.warp.ir.WarpState
import com.atriidev.warp.pipeline.WarpIrCodec
import com.atriidev.warp.pipeline.WarpPipeline
import com.atriidev.warp.widgets.CounterWarpWidget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WarpIrCodecTest {
    @Test
    fun counterWidgetProducesExpectedRowTree() {
        val document = CounterWarpWidget.build(WarpState(mapOf(COUNTER_KEY to "7")))
        val root = assertIs<WarpNode.Row>(document.root)

        assertEquals(3, root.children.size)
        assertIs<WarpNode.Button>(root.children[0])
        assertIs<WarpNode.Text>(root.children[1])
        assertIs<WarpNode.Button>(root.children[2])

        val textNode = root.children[1] as WarpNode.Text
        assertEquals(COUNTER_KEY, textNode.stateKey)
        assertEquals(16, root.modifier.padding?.all)
        assertEquals(0xFF00FF00, root.modifier.background?.argb)
    }

    @Test
    fun jsonRoundTripPreservesDocument() {
        val original = CounterWarpWidget.build(WarpState(mapOf(COUNTER_KEY to "42")))
        val encoded = WarpIrCodec.encode(original)
        val decoded = WarpIrCodec.decode(encoded)

        assertEquals(original, decoded)
        assertTrue(encoded.contains("\"CounterWidget\""))
    }

    @Test
    fun pipelineCompilesCounterWidget() {
        val document = WarpPipeline.compile(
            CounterWarpWidget,
            WarpState(mapOf(COUNTER_KEY to "3")),
        )

        assertEquals(CounterWarpWidget.KIND, document.widgetKind)
        assertEquals(WARP_SCHEMA_VERSION, document.schemaVersion)
    }

    @Test
    fun counterActionHandlerIncrementsAndDecrements() {
        val initial = WarpState(mapOf(COUNTER_KEY to "5"))

        val incremented = CounterActionHandler.handle(
            CounterActions.INCREMENT,
            COUNTER_KEY,
            initial,
        )
        assertEquals("6", incremented.get(COUNTER_KEY))

        val decremented = CounterActionHandler.handle(
            CounterActions.DECREMENT,
            COUNTER_KEY,
            incremented,
        )
        assertEquals("5", decremented.get(COUNTER_KEY))
    }
}
