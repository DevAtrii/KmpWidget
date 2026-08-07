package com.atriidev.warp_runtime.nodes.assets

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Logical image reference for [com.atriidev.warp_runtime.nodes.WarpImage].
 *
 * Wire format is a small JSON ref — never raw pixels. Hosts resolve at paint time:
 *
 * - [Id] → bundled drawable / asset catalog ([WarpAssetId])
 * - [System] → **SF Symbol** on iOS; Android via same [WarpAssetId] registry
 * - [WarpAssets.Android.Uri] → local `file://` / `content://` / `android.resource://` only
 *
 * Prefer defining keys as [WarpAssetId] constants — avoid raw strings in UI.
 *
 * ```
 * object Icons {
 *     val Plus = WarpAssetId("plus.circle.fill")
 * }
 * WarpImage(asset = Icons.Plus.asSystem())
 * WarpImage(asset = Icons.Plus.asId())
 * WarpImage(asset = WarpAssets.Android.Uri("file:///…/photo.jpg"))
 * ```
 */
@Serializable
sealed interface WarpAsset {
    /** App-bundled asset ([WarpAssetId] → Android drawable registry / iOS Asset Catalog). */
    @Serializable
    @SerialName("id")
    data class Id(val id: WarpAssetId) : WarpAsset

    /**
     * Platform system symbol.
     *
     * iOS: SF Symbol [WarpAssetId.value].
     * Android: same key looked up in the id→drawable map.
     */
    @Serializable
    @SerialName("system")
    data class System(val name: WarpAssetId) : WarpAsset
}

/**
 * Platform-scoped asset variants.
 */
object WarpAssets {
    /**
     * Android-oriented local URI asset (also used for iOS `file://` App Group paths).
     *
     * Remote `http` / `https` are not supported.
     */
    object Android {
        @Serializable
        @SerialName("uri")
        data class Uri(val uri: String) : WarpAsset
    }
}
