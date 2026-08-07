package com.atriidev.warp_widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.atriidev.warp_ui.WarpRender

/**
 * Glance host for a shared [WarpWidget].
 *
 * Uses [PreferencesGlanceStateDefinition] and wires [WarpWidgetHost] + [WarpRender]
 * in [provideGlance]. Override [provideGlance] only if you need a custom Glance tree.
 *
 * ```
 * class CounterGlanceAppWidget : WarpGlanceWidget() {
 *     override val widget get() = CounterWarpWidget
 * }
 * ```
 */
abstract class WarpGlanceWidget : GlanceAppWidget() {
    /** Shared WARP definition composed into this Glance surface. */
    abstract val widget: WarpWidget<*>

    override val stateDefinition: GlanceStateDefinition<*>
        get() = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
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
