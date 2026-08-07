package com.atriidev.warp_widget

import com.atriidev.warp_widget.api.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDefaults
import warpWidgetKit.WarpWidgetBridge

/**
 * iOS: App Group [NSUserDefaults] + [WarpWidgetBridge] → WidgetCenter reload.
 *
 * Storage keys: `"$widgetId.$keyName"`.
 */
actual object WarpWidgetStateStore {
    actual suspend fun read(
        context: PlatformContext,
        widgetId: String,
    ): WarpWidgetPreferences {
        val defaults = userDefaults(context)
        val prefix = keyPrefix(widgetId)
        val values = buildMap {
            defaults.dictionaryRepresentation().forEach { (rawKey, rawValue) ->
                val key = rawKey as? String ?: return@forEach
                if (!key.startsWith(prefix)) return@forEach
                val shortKey = key.removePrefix(prefix)
                if (shortKey.isEmpty()) return@forEach
                put(shortKey, rawValue?.toString() ?: return@forEach)
            }
        }
        return WarpWidgetPreferences(values)
    }

    actual suspend fun update(
        context: PlatformContext,
        widgetId: String,
        transform: MutableWarpWidgetPreferences.() -> Unit,
    ) {
        val defaults = userDefaults(context)
        val prefix = keyPrefix(widgetId)
        val mutable = MutableWarpWidgetPreferences(read(context, widgetId).values)
        val beforeKeys = mutable.asMap().keys.toSet()
        mutable.transform()
        val after = mutable.asMap()

        (beforeKeys - after.keys).forEach { shortKey ->
            defaults.removeObjectForKey(prefix + shortKey)
        }
        after.forEach { (shortKey, value) ->
            defaults.setObject(value, forKey = prefix + shortKey)
        }
        defaults.synchronize()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun refreshAfterUpdate(
        context: PlatformContext,
        widgetId: String,
    ) {
        WarpWidgetBridge.shared().reloadTimelinesOfKind(widgetId)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun reload(
        context: PlatformContext,
        widgetId: String,
    ) {
        WarpWidgetBridge.shared().reloadTimelinesOfKind(widgetId)
    }

    private fun keyPrefix(widgetId: String): String = "$widgetId."

    private fun userDefaults(context: PlatformContext): NSUserDefaults {
        val suite = context.appGroupId
        val container = NSFileManager.defaultManager
            .containerURLForSecurityApplicationGroupIdentifier(suite)
        if (container == null) {
            println(
                "WarpWidgetStateStore: App Group '$suite' unavailable — " +
                    "enable App Groups on app + extension (Signing & Capabilities).",
            )
        }
        return NSUserDefaults(suiteName = suite)
    }
}
