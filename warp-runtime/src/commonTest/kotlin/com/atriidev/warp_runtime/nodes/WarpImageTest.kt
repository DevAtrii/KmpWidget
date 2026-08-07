package com.atriidev.warp_runtime.nodes

import com.atriidev.warp_runtime.compose.WarpImage
import com.atriidev.warp_runtime.compose.composeWarp
import com.atriidev.warp_runtime.compose.composeWarpToJson
import com.atriidev.warp_runtime.compose.toJson
import com.atriidev.warp_runtime.nodes.assets.WarpAsset
import com.atriidev.warp_runtime.nodes.assets.WarpAssetId
import com.atriidev.warp_runtime.nodes.assets.WarpAssets
import com.atriidev.warp_runtime.nodes.modifiers.WarpColor
import com.atriidev.warp_runtime.nodes.style.WarpContentScale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private object TestAssets {
    val NumberCircle = WarpAssetId("number.circle.fill")
    val Sun = WarpAssetId("weather/sun")
    val Plus = WarpAssetId("plus.circle.fill")
}

class WarpImageTest {

    @Test
    fun systemAsset_serializesForSfSymbols() {
        val json = composeWarpToJson {
            WarpImage(
                asset = TestAssets.NumberCircle.asSystem(),
                contentDescription = "Count",
                contentScale = WarpContentScale.Fit,
                tint = WarpColor("#B0BEC5"),
            )
        }

        assertTrue(json.contains("\"type\": \"image\""))
        assertTrue(json.contains("\"type\": \"system\""))
        assertTrue(json.contains("number.circle.fill"))
        assertTrue(json.contains("\"hex\": \"#B0BEC5\""))
    }

    @Test
    fun idAndUri_roundTripThroughCompose() {
        val tree = composeWarp {
            WarpImage(asset = TestAssets.Sun.asId())
        }
        val image = assertIs<WarpImage>(tree)
        assertEquals(WarpAsset.Id(TestAssets.Sun), image.asset)

        val uriTree = composeWarp {
            WarpImage(asset = WarpAssets.Android.Uri("file:///tmp/avatar.png"))
        }
        assertEquals(
            WarpAssets.Android.Uri("file:///tmp/avatar.png"),
            assertIs<WarpImage>(uriTree).asset,
        )
    }

    @Test
    fun warpImageNode_toJson_includesContentScale() {
        val json = com.atriidev.warp_runtime.nodes.WarpImage(
            asset = TestAssets.Plus.asSystem(),
            contentScale = WarpContentScale.Crop,
        ).toJson()

        assertTrue(json.contains("\"contentScale\": \"crop\""))
    }
}
