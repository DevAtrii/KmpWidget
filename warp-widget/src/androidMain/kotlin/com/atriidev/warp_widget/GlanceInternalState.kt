package com.atriidev.warp_widget

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.ui.unit.DpSize
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.updateAppWidgetState

/** Glance-only keys — excluded from [Preferences.toWarpPreferences]. */
internal object GlanceInternalState {
    private const val PREFIX = "__warp_"

    val uiModeKey = longPreferencesKey("${PREFIX}ui_mode")

    val themeEpochKey = longPreferencesKey("${PREFIX}theme_epoch")

    val layoutWidthKey = longPreferencesKey("${PREFIX}layout_w")

    val layoutHeightKey = longPreferencesKey("${PREFIX}layout_h")

    val layoutEpochKey = longPreferencesKey("${PREFIX}layout_epoch")

    fun isInternalKey(name: String): Boolean = name.startsWith(PREFIX)

    fun nightModeMask(config: Configuration): Int =
        config.uiMode and Configuration.UI_MODE_NIGHT_MASK

    fun nightModeMask(context: Context): Int =
        nightModeMask(context.resources.configuration)

    fun readNightModeMask(prefs: Preferences, context: Context): Int =
        prefs[uiModeKey]?.toInt() ?: nightModeMask(context)

    /** Write uiMode + epoch so an active Glance session re-reads state and recomposes. */
    suspend fun touchTheme(context: Context, glanceId: GlanceId) {
        val night = nightModeMask(context)
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[uiModeKey] = night.toLong()
            prefs[themeEpochKey] = System.currentTimeMillis()
        }
    }

    /**
     * Persist layout from [options] + bump epoch.
     *
     * Required because [androidx.glance.appwidget.SizeMode.Single] ignores
     * [androidx.glance.appwidget.GlanceAppWidget.resize] — resize must force [update].
     */
    suspend fun touchLayout(context: Context, glanceId: GlanceId, options: Bundle) {
        val size = options.resolveGlanceWidgetSize(DpSize.Zero)
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[layoutWidthKey] = size.width.value.toLong()
            prefs[layoutHeightKey] = size.height.value.toLong()
            prefs[layoutEpochKey] = System.currentTimeMillis()
        }
    }
}
