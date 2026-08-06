package com.atriidev.kmpwidget

/**
 * Tiny key/value store shared by the demo app and widgets.
 *
 * - **Android:** DataStore / preferences
 * - **iOS:** App Group `UserDefaults` ([APP_GROUP_ID])
 */
expect class KmpDataStore {
    fun get(key: String, defaultValue: String): String
    fun set(key: String, value: String): Boolean
}
