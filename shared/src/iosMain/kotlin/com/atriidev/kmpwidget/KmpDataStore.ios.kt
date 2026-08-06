package com.atriidev.kmpwidget

import platform.Foundation.NSUserDefaults

actual class KmpDataStore {

    private val defaults =
        NSUserDefaults(suiteName = APP_GROUP_ID)

    actual fun get(key: String, defaultValue: String): String {
        return defaults.stringForKey(key) ?: defaultValue
    }

    actual fun set(key: String, value: String): Boolean {
        defaults.setObject(value, forKey = key)
        return defaults.synchronize()
    }
}