package com.atriidev.warp_widget

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.updateAppWidgetState

/** Glance-only keys — excluded from [Preferences.toWarpPreferences]. */
internal object GlanceInternalState {
    private const val PREFIX = "__warp_"

    val uiModeKey = longPreferencesKey("${PREFIX}ui_mode")

    val themeEpochKey = longPreferencesKey("${PREFIX}theme_epoch")

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
}
