package com.atriidev.warp_runtime.nodes.assets

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Logical image reference for [com.atriidev.warp_runtime.nodes.WarpImage].
 *
 * Wire format is a small JSON ref — never raw pixels. Hosts resolve at paint time:
 *
 * - [Id] → bundled drawable / asset catalog
 * - [System] → **SF Symbol** on iOS; Android via system→drawable map
 * - [WarpAssets.Android.Uri] → local `file://` / `content://` / `android.resource://` only
 *
 * ```
 * WarpImage(asset = WarpAsset.System("plus.circle.fill"))
 * WarpImage(asset = WarpAsset.Id("weather/sun"))
 * WarpImage(asset = WarpAssets.Android.Uri("file:///…/photo.jpg"))
 * ```
 */
@Serializable
sealed interface WarpAsset {
    /** App-bundled asset id (Android drawable registry / iOS Asset Catalog name). */
    @Serializable
    @SerialName("id")
    data class Id(val id: String) : WarpAsset

    /**
     * Platform system symbol.
     *
     * iOS: SF Symbol name (e.g. `"number.circle.fill"`).
     * Android: same string looked up in the id→drawable map ([WarpAsset.Id] registry).
     */
    @Serializable
    @SerialName("system")
    data class System(val name: String) : WarpAsset
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
