package com.atriidev.warp_widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.serialization.Serializable

/**
 * Low-level typed preference key (advanced).
 *
 * Prefer [WarpWidget] with a `@Serializable` state class + [updateWarpWidgetState].
 * Keep [WarpStateKey] for ad-hoc string bags alongside the typed JSON blob.
 *
 * Values live in [WarpWidgetPreferences]; iOS keys are `"$widgetId.$name"`.
 */
class WarpStateKey<T> internal constructor(
    /** Wire / datastore key name (not namespaced; iOS store adds widget id prefix). */
    val name: String,
    internal val encode: (T) -> String,
    internal val decode: (String) -> T,
) {
    companion object {
        /** String preference key. */
        fun string(name: String): WarpStateKey<String> =
            WarpStateKey(name, encode = { it }, decode = { it })

        /** [Int] preference key (decimal string). */
        fun int(name: String): WarpStateKey<Int> =
            WarpStateKey(name, encode = { it.toString() }, decode = { it.toInt() })

        /** [Long] preference key (decimal string). */
        fun long(name: String): WarpStateKey<Long> =
            WarpStateKey(name, encode = { it.toString() }, decode = { it.toLong() })

        /** [Boolean] preference key (`true` / `false`). */
        fun boolean(name: String): WarpStateKey<Boolean> =
            WarpStateKey(name, encode = { it.toString() }, decode = { it.toBooleanStrict() })

        /** [Float] preference key. */
        fun float(name: String): WarpStateKey<Float> =
            WarpStateKey(name, encode = { it.toString() }, decode = { it.toFloat() })
    }
}

/**
 * Read-only preference bag available during [WarpWidget.Content] via [currentState] /
 * [currentPreferences].
 */
@Serializable
data class WarpWidgetPreferences(
    val values: Map<String, String> = emptyMap(),
) {
    operator fun <T> get(key: WarpStateKey<T>): T? =
        values[key.name]?.let(key.decode)

    fun <T> getOrDefault(key: WarpStateKey<T>, default: T): T =
        get(key) ?: default
}

/**
 * Mutable prefs for [updateWarpWidgetState] transforms (app or click handlers).
 */
class MutableWarpWidgetPreferences(
    initial: Map<String, String> = emptyMap(),
) {
    private val map = initial.toMutableMap()

    operator fun <T> get(key: WarpStateKey<T>): T? =
        map[key.name]?.let(key.decode)

    operator fun <T> set(key: WarpStateKey<T>, value: T) {
        map[key.name] = key.encode(value)
    }

    fun <T> remove(key: WarpStateKey<T>) {
        map.remove(key.name)
    }

    /** Raw string put (used for typed [WarpWidget] JSON blobs keyed by widget id). */
    fun setRaw(name: String, value: String) {
        map[name] = value
    }

    fun removeRaw(name: String) {
        map.remove(name)
    }

    fun getRaw(name: String): String? = map[name]

    fun toPreferences(): WarpWidgetPreferences =
        WarpWidgetPreferences(map.toMap())

    internal fun asMap(): Map<String, String> = map.toMap()
}

internal val LocalWarpWidgetPreferences =
    staticCompositionLocalOf<WarpWidgetPreferences> {
        error(
            "No WarpWidgetPreferences. Call WarpWidgetHost.compose / provide Content " +
                "only inside a host session (Glance provideContent or WidgetKit timeline).",
        )
    }

/**
 * Full preference bag for this render — analogous to Glance `currentState<Preferences>()`.
 *
 * Only valid inside [WarpWidget.Content] while [WarpWidgetHost] is composing.
 */
@Composable
@ReadOnlyComposable
fun currentPreferences(): WarpWidgetPreferences = LocalWarpWidgetPreferences.current

/**
 * Single key — analogous to Glance `currentState(key)`.
 *
 * ```
 * @Composable
 * override fun Content(env: WidgetEnvironment) {
 *     val count = currentState(CounterKeys.Count) ?: 0
 *     WarpText("$count")
 * }
 * ```
 */
@Composable
@ReadOnlyComposable
fun <T> currentState(key: WarpStateKey<T>): T? = currentPreferences()[key]

/**
 * Typed widget state for the current [WarpWidget.Content] render.
 *
 * Prefer the `state` parameter on [WarpWidget.Content]; use this when nesting
 * composables that need [S] without threading it through every call.
 */
@Composable
@ReadOnlyComposable
fun <S : Any> currentWidgetState(widget: WarpWidget<S>): S =
    widget.decodeState(currentPreferences())

@Composable
internal fun ProvideWarpWidgetPreferences(
    preferences: WarpWidgetPreferences,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalWarpWidgetPreferences provides preferences, content = content)
}
