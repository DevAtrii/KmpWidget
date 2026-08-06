package com.atriidev.warp_widget

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey

/** Glance [Preferences] → WARP string bag (all values `toString()`). */
fun Preferences.toWarpPreferences(): WarpWidgetPreferences =
    WarpWidgetPreferences(
        values = asMap().entries.associate { (key, value) ->
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
        remove(stringPreferencesKey(name))
    }
    after.forEach { (name, value) ->
        this[stringPreferencesKey(name)] = value
    }
}
