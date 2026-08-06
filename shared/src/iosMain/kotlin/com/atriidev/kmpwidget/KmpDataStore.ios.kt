package com.atriidev.kmpwidget

import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDefaults

/**
 * iOS [KmpDataStore]: `UserDefaults(suiteName:)` for [APP_GROUP_ID].
 *
 * Shared by the main app and the widget extension so both see the same counter.
 *
 * ### Xcode
 * Enable **App Groups** → `group.com.atriidev.kmpwidget` on **both** targets
 * (iosApp + CounterWidgetExtension). If the container URL is missing, app and
 * widget will not share state (a warning is logged).
 *
 * ### Swift equivalent
 * `UserDefaults(suiteName: "group.com.atriidev.kmpwidget")`
 */
actual class KmpDataStore {

    private val defaults: NSUserDefaults

    init {
        val container = NSFileManager.defaultManager
            .containerURLForSecurityApplicationGroupIdentifier(APP_GROUP_ID)
        if (container == null) {
            println(
                "KmpDataStore: App Group '$APP_GROUP_ID' unavailable — " +
                    "app/widget will not share state. " +
                    "Enable App Groups for both targets in Xcode (Signing & Capabilities).",
            )
        }
        defaults = NSUserDefaults(suiteName = APP_GROUP_ID)
    }

    actual fun get(key: String, defaultValue: String): String {
        return defaults.stringForKey(key) ?: defaultValue
    }

    actual fun set(key: String, value: String): Boolean {
        defaults.setObject(value, forKey = key)
        defaults.synchronize()
        return true
    }
}
