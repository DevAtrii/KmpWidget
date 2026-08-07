package com.atriidev.warp_ui.glance

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.glance.ImageProvider
import com.atriidev.warp_runtime.nodes.assets.WarpAsset
import com.atriidev.warp_runtime.nodes.assets.WarpAssets
import java.util.concurrent.ConcurrentHashMap

/**
 * One bundled drawable for [WarpAndroidAssets.register] / [com.atriidev.warp_widget.WarpGlanceWidget.assets].
 *
 * Use the same [id] as [WarpAsset.Id] or as [WarpAsset.System.name] (SF Symbol name on iOS).
 */
data class WarpDrawableAsset(
    val id: String,
    @DrawableRes val resId: Int,
)

/**
 * Resolves [WarpAsset] → Glance [ImageProvider] for Android widgets.
 *
 * - [WarpAsset.Id] / [WarpAsset.System] → [register] by id (system name looks up the same map)
 * - [WarpAssets.Android.Uri] → local bitmap (`content://` / `file://` / `android.resource://` only)
 *
 * Prefer declaring assets on [com.atriidev.warp_widget.WarpGlanceWidget.assets] — registration is automatic.
 * Unresolved → `null` (empty space). Remote http(s) not supported.
 */
object WarpAndroidAssets {
    private val ids = ConcurrentHashMap<String, Int>()

    /** Map logical id → `@DrawableRes` (also used when resolving [WarpAsset.System]). */
    fun register(id: String, @DrawableRes resId: Int) {
        ids[id] = resId
    }

    fun registerAll(assets: Iterable<WarpDrawableAsset>) {
        assets.forEach { register(it.id, it.resId) }
    }

    fun unregister(id: String) {
        ids.remove(id)
    }

    fun clear() {
        ids.clear()
    }

    fun resolve(asset: WarpAsset, context: Context): ImageProvider? = when (asset) {
        is WarpAsset.Id -> ids[asset.id]?.let { ImageProvider(it) }
        is WarpAsset.System -> ids[asset.name]?.let { ImageProvider(it) }
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
