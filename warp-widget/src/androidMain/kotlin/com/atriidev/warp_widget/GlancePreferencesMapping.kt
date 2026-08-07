package com.atriidev.warp_widget

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Glance [Preferences] → WARP string bag (all values via `toString()`).
 *
 * Use when building [WarpWidgetSession.preferences] inside Glance `provideContent`,
 * or rely on [rememberGlanceWidgetSession] which does this for you.
 */
fun Preferences.toWarpPreferences(): WarpWidgetPreferences =
    WarpWidgetPreferences(
        values = asMap().entries
            .filterNot { (key, _) -> GlanceInternalState.isInternalKey(key.name) }
            .associate { (key, value) ->
                key.name to value.toString()
            },
    )

/**
 * Apply [warp] onto Glance [MutablePreferences].
 *
 * Sets string keys from [warp]; removes Glance keys that disappeared vs [beforeKeys].
 */
internal fun MutablePreferences.applyWarpPreferences(
    beforeKeys: Set<String>,
    warp: WarpWidgetPreferences,
) {
    val after = warp.values
    (beforeKeys - after.keys).forEach { name ->
        if (!GlanceInternalState.isInternalKey(name)) {
            remove(stringPreferencesKey(name))
        }
    }
    after.forEach { (name, value) ->
        this[stringPreferencesKey(name)] = value
    }
}
