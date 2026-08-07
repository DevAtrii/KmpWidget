package com.atriidev.warp_widget

import android.content.Context
import androidx.compose.runtime.remember
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
 *         WarpDrawableAsset(CounterAssets.NumberCircle, R.drawable.ic_number_circle),
 *     )
 * }
 * ```
 *
 * [assets] are registered into [WarpAndroidAssets] automatically.
 * Use the same [com.atriidev.warp_runtime.nodes.assets.WarpAssetId] constants as in common `Content`.
 */
abstract class WarpGlanceWidget : GlanceAppWidget() {
    /** Shared WARP definition composed into this Glance surface. */
    abstract val widget: WarpWidgetHostApi

    /**
     * Optional bundled drawables for this widget.
     *
     * Pass [com.atriidev.warp_runtime.nodes.assets.WarpAssetId] keys shared with common UI.
     * Registered under the hood into [WarpAndroidAssets].
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
            val node = remember(session.preferences, session.environment) {
                WarpWidgetHost.compose(warp, session)
            }
            val handlers = remember(warp.id, session.context) {
                WarpWidgetHost.handlers(warp, session)
            }
            WarpRender(
                node = node,
                handlers = handlers,
            )
        }
    }
}
