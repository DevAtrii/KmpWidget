package com.atriidev.warp_ui.glance.internal

import android.content.Context
import androidx.glance.GlanceId

/**
 * Cold-start hook before [WarpRegistryActionCallback] dispatches.
 *
 * [WarpClicksRegistry] is process-local. When the app is dead, Glance still
 * delivers the tap into a fresh process where [com.atriidev.warp_ui.WarpRender]
 * has never run — so the registry is empty unless something re-registers handlers.
 *
 * [com.atriidev.warp_widget.WarpWidgetAndroidRegistry] installs the prepare
 * callback; [prepareIfNeeded] runs it on every action.
 */
internal object WarpGlanceClickPrepare {
    @Volatile
    private var prepare: (suspend (Context, GlanceId) -> Unit)? = null

    fun setPrepareHandler(handler: suspend (Context, GlanceId) -> Unit) {
        prepare = handler
    }

    suspend fun prepareIfNeeded(context: Context, glanceId: GlanceId) {
        prepare?.invoke(context, glanceId)
    }
}
