package com.atriidev.warp_runtime.nodes.assets

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Type-safe asset key shared by common UI and Android [WarpDrawableAsset] registration.
 *
 * Define once in commonMain — use the same constant in `Content` and `WarpGlanceWidget.assets()`:
 *
 * ```
 * object WeatherAssets {
 *     val Sun = WarpAssetId("weather/sun")
 *     val SfCloud = WarpAssetId("cloud.fill") // SF Symbol name on iOS
 * }
 *
 * WarpImage(asset = WeatherAssets.Sun.asId())
 * WarpImage(asset = WeatherAssets.SfCloud.asSystem())
 *
 * // androidMain:
 * WarpDrawableAsset(WeatherAssets.Sun, R.drawable.weather_sun)
 * WarpDrawableAsset(WeatherAssets.SfCloud, R.drawable.ic_cloud)
 * ```
 */
@JvmInline
@Serializable
value class WarpAssetId(val value: String) {
    /** Bundled catalog / drawable id. */
    fun asId(): WarpAsset = WarpAsset.Id(this)

    /** SF Symbol on iOS; Android looks up [value] in the same id→drawable map. */
    fun asSystem(): WarpAsset = WarpAsset.System(this)

    override fun toString(): String = value
}
