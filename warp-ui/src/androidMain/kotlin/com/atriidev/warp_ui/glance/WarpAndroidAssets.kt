package com.atriidev.warp_ui.glance

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.glance.ImageProvider
import com.atriidev.warp_runtime.nodes.assets.WarpAsset
import com.atriidev.warp_runtime.nodes.assets.WarpAssetId
import com.atriidev.warp_runtime.nodes.assets.WarpAssets
import java.util.concurrent.ConcurrentHashMap

/**
 * One bundled drawable for [WarpAndroidAssets.register] / [com.atriidev.warp_widget.WarpGlanceWidget.assets].
 *
 * [id] must be the same [WarpAssetId] used in common `Content` (`asId()` / `asSystem()`).
 */
data class WarpDrawableAsset(
    val id: WarpAssetId,
    @DrawableRes val resId: Int,
)

/**
 * Resolves [WarpAsset] → Glance [ImageProvider] for Android widgets.
 *
 * - [WarpAsset.Id] / [WarpAsset.System] → [register] by [WarpAssetId]
 * - [WarpAssets.Android.Uri] → local bitmap only
 *
 * Prefer [com.atriidev.warp_widget.WarpGlanceWidget.assets] — registration is automatic.
 */
object WarpAndroidAssets {
    private val ids = ConcurrentHashMap<String, Int>()

    fun register(id: WarpAssetId, @DrawableRes resId: Int) {
        ids[id.value] = resId
    }

    fun registerAll(assets: Iterable<WarpDrawableAsset>) {
        assets.forEach { register(it.id, it.resId) }
    }

    fun unregister(id: WarpAssetId) {
        ids.remove(id.value)
    }

    fun clear() {
        ids.clear()
    }

    fun resolve(asset: WarpAsset, context: Context): ImageProvider? = when (asset) {
        is WarpAsset.Id -> ids[asset.id.value]?.let { ImageProvider(it) }
        is WarpAsset.System -> ids[asset.name.value]?.let { ImageProvider(it) }
        is WarpAssets.Android.Uri -> resolveUri(asset.uri, context)
    }

    private fun resolveUri(uriString: String, context: Context): ImageProvider? {
        return runCatching {
            val uri = Uri.parse(uriString)
            when (uri.scheme?.lowercase()) {
                "content", "file", "android.resource" -> {
                    val stream = context.contentResolver.openInputStream(uri) ?: return null
                    stream.use { input ->
                        val bitmap = BitmapFactory.decodeStream(input) ?: return null
                        ImageProvider(bitmap)
                    }
                }
                else -> null
            }
        }.getOrNull()
    }
}
