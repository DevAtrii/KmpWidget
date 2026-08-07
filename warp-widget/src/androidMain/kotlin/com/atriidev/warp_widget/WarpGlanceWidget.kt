package com.atriidev.warp_widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.atriidev.warp_ui.WarpRender
import com.atriidev.warp_ui.glance.WarpAndroidAssets
import com.atriidev.warp_ui.glance.WarpDrawableAsset

/**
 * Glance host for a shared [WarpWidget].
 *
 * Uses [PreferencesGlanceStateDefinition] and wires [WarpWidgetHost] + [WarpRender]
 * in [provideGlance]. Override [provideGlance] only if you need a custom Glance tree.
 *
 * ```
 * class CounterGlanceAppWidget : WarpGlanceWidget() {
 *     override val widget get() = CounterWarpWidget
 *     override fun assets() = listOf(
 *         WarpDrawableAsset("number.circle.fill", R.drawable.ic_number_circle),
 *     )
 * }
 * ```
 *
 * [assets] are registered into [WarpAndroidAssets] automatically (id map).
 * [com.atriidev.warp_runtime.nodes.assets.WarpAsset.System] resolves via the same ids.
 */
abstract class WarpGlanceWidget : GlanceAppWidget() {
    /** Shared WARP definition composed into this Glance surface. */
    abstract val widget: WarpWidget<*>

    /**
     * Optional bundled drawables for this widget.
     *
     * Keys = [com.atriidev.warp_runtime.nodes.assets.WarpAsset.Id] / SF Symbol name used with
     * [com.atriidev.warp_runtime.nodes.assets.WarpAsset.System]. Registered under the hood.
     */
    open fun assets(): List<WarpDrawableAsset> = emptyList()

    override val stateDefinition: GlanceStateDefinition<*>
        get() = PreferencesGlanceStateDefinition

    /** Idempotent [WarpAndroidAssets.registerAll] for [assets]. */
    fun ensureAssetsRegistered() {
        WarpAndroidAssets.registerAll(assets())
    }

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        ensureAssetsRegistered()
        val warp = widget
        provideContent {
            val session = rememberGlanceWidgetSession(context)
            WarpRender(
                node = WarpWidgetHost.compose(warp, session),
                handlers = WarpWidgetHost.handlers(warp, session),
            )
        }
    }
}
