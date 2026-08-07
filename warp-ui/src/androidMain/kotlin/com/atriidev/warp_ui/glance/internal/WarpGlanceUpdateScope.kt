package com.atriidev.warp_ui.glance.internal

import androidx.glance.GlanceId

/**
 * Scopes Glance prefs update + refresh to the widget instance that received the tap.
 *
 * Set in [WarpRegistryActionCallback] before dispatch; cleared after.
 * [com.atriidev.warp_widget.WarpWidgetStateStore] reads this on Android.
 */
object WarpGlanceUpdateScope {
    var targetGlanceId: GlanceId? = null
}
