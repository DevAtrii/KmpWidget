package com.atriidev.kmpwidget

actual class KmpDataStore {

    companion object {
        const val APP_GROUP_ID = "group.com.atriidev.kmpwidget"
    }

    private val defaults = platform.Foundation.NSUserDefaults(suiteName = APP_GROUP_ID)
        ?: platform.Foundation.NSUserDefaults.standardUserDefaults()

    actual fun get(key: String, defaultValue: String): String {
        return defaults.stringForKey(key) ?: defaultValue
    }

    actual fun set(key: String, value: String): Boolean {
        defaults.setObject(value, forKey = key)
        return defaults.synchronize()
    }
}
