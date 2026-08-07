package com.atriidev.warp_widget

import com.atriidev.warp_widget.api.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDefaults
import warpWidgetKit.WarpWidgetBridge

/**
 * iOS: App Group [NSUserDefaults] + [WarpWidgetBridge] → WidgetCenter reload.
 *
 * | [WarpWidgetStateScope] | keys | reload |
 * |---|---|---|
 * | Shared | `"$kind.$key"` | `reloadTimelinesOfKind(kind)` |
 * | Instance | `"$kind.$instanceId.$key"` | same kind reload (WidgetKit has no per-instance API) |
 */
actual object WarpWidgetStateStore {
    actual suspend fun read(
        context: PlatformContext,
        widget: WarpWidgetHostApi,
        id: WarpWidgetId,
    ): WarpWidgetPreferences {
        val defaults = userDefaults(context)
        val prefix = storagePrefix(widget, id)
        return readWithPrefix(defaults, prefix)
    }

    actual suspend fun read(
        context: PlatformContext,
        widgetId: String,
    ): WarpWidgetPreferences {
        val defaults = userDefaults(context)
        return readWithPrefix(defaults, keyPrefix(widgetId, WarpWidgetStateScope.Shared, null))
    }

    actual suspend fun update(
        context: PlatformContext,
        widget: WarpWidgetHostApi,
        id: WarpWidgetId,
        transform: MutableWarpWidgetPreferences.() -> Unit,
    ) {
        val defaults = userDefaults(context)
        val prefix = storagePrefix(widget, id)
        val mutable = MutableWarpWidgetPreferences(read(context, widget, id).values)
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

    actual suspend fun update(
        context: PlatformContext,
        widgetId: String,
        transform: MutableWarpWidgetPreferences.() -> Unit,
    ) {
        val defaults = userDefaults(context)
        val prefix = keyPrefix(widgetId, WarpWidgetStateScope.Shared, null)
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
        widget: WarpWidgetHostApi,
        id: WarpWidgetId,
    ) {
        WarpWidgetBridge.shared().reloadTimelinesOfKind(widget.id)
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
        widget: WarpWidgetHostApi,
        id: WarpWidgetId,
    ) {
        WarpWidgetBridge.shared().reloadTimelinesOfKind(widget.id)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun reload(
        context: PlatformContext,
        widgetId: String,
    ) {
        WarpWidgetBridge.shared().reloadTimelinesOfKind(widgetId)
    }

    actual suspend fun listWarpWidgetIds(
        context: PlatformContext,
        widget: WarpWidgetHostApi,
    ): List<WarpWidgetId> {
        return when (widget.stateScopeOrShared()) {
            WarpWidgetStateScope.Shared -> listOf(WarpWidgetId.ofKind(widget.id))
            WarpWidgetStateScope.Instance -> listInstanceIds(context, widget.id)
        }
    }

    private fun storagePrefix(widget: WarpWidgetHostApi, id: WarpWidgetId): String =
        keyPrefix(widget.id, widget.stateScopeOrShared(), id)

    private fun keyPrefix(
        kindId: String,
        scope: WarpWidgetStateScope,
        id: WarpWidgetId?,
    ): String = when (scope) {
        WarpWidgetStateScope.Shared -> "$kindId."
        WarpWidgetStateScope.Instance -> {
            val resolved = id
                ?: throw IllegalArgumentException("Instance-scoped '$kindId' requires WarpWidgetId.")
            // Prefer ios:… segment; fall back to raw value so ofKind / aw: never hard-crash clicks.
            val segment = resolved.iosInstanceSegmentOrNull()
                ?: resolved.value.substringAfterLast(':').ifBlank { resolved.value }
            "$kindId.$segment."
        }
    }

    private fun readWithPrefix(defaults: NSUserDefaults, prefix: String): WarpWidgetPreferences {
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

    private fun listInstanceIds(context: PlatformContext, kindId: String): List<WarpWidgetId> {
        val defaults = userDefaults(context)
        val kindPrefix = "$kindId."
        val segments = mutableSetOf<String>()
        defaults.dictionaryRepresentation().forEach { (rawKey, _) ->
            val key = rawKey as? String ?: return@forEach
            if (!key.startsWith(kindPrefix)) return@forEach
            val remainder = key.removePrefix(kindPrefix)
            val dot = remainder.indexOf('.')
            if (dot <= 0) return@forEach
            segments += remainder.substring(0, dot)
        }
        return segments.map { WarpWidgetId.ios(it) }.sortedBy { it.value }
    }

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
