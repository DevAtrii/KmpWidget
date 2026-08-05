package com.atriidev.kmpwidget

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

actual class KmpDataStore(
    private val context: Context,
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("kmp_widgets", Context.MODE_PRIVATE)

    actual fun get(key: String, defaultValue: String): String {
        val value = prefs.getString(key, defaultValue) ?: defaultValue
        return value
    }

    actual fun set(key: String, value: String): Boolean {
        prefs.edit {
            putString(key, value)
        }
        return true
    }
}