package com.atriidev.warp_widget

import androidx.compose.runtime.Composable
import com.atriidev.warp_runtime.compose.composeWarp
import com.atriidev.warp_runtime.compose.toJson
import com.atriidev.warp_runtime.nodes.WarpNode
import com.atriidev.warp_ui.WarpClickHandler
import com.atriidev.warp_widget.api.PlatformContext
import com.atriidev.warp_widget.api.WidgetEnvironment
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * One render / click pass from a platform host.
 *
 * Hosts **must** supply [environment] and [context] (never invented by [WarpWidgetHost]):
 *
 * - **Android:** [rememberGlanceWidgetSession] / [glanceWidgetEnvironment]
 * - **iOS:** [WarpWidgetHost.iosSession] with Kit `asKitFields`
 */
data class WarpWidgetSession(
    val context: PlatformContext,
    val environment: WidgetEnvironment,
    val preferences: WarpWidgetPreferences? = null,
)

/**
 * Shared widget definition for Glance and WidgetKit — typed serializable [S] state.
 *
 * State is JSON-encoded and stored under prefs key = [id].
 *
 * ```
 * @Serializable
 * data class CounterState(val count: Int = 0)
 *
 * object CounterWarpWidget : WarpWidget<CounterState>(CounterState.serializer()) {
 *     override val id = "CounterWidget"
 *     override val iosGroupId = APP_GROUP_ID
 *     override val defaultState = CounterState()
 *
 *     @Composable
 *     override fun Content(env: WidgetEnvironment, state: CounterState) {
 *         WarpText("${state.count}")
 *     }
 * }
 *
 * // update:
 * updateWarpWidgetState(context, CounterWarpWidget) { it.copy(count = it.count + 1) }
 * ```
 */
abstract class WarpWidget<S : Any>(
    private val stateSerializer: KSerializer<S>,
) {
    /**
     * Stable widget kind id.
     *
     * Prefs JSON key, timeline kind, Glance registry, WidgetKit `kind`.
     */
    abstract val id: String

    /**
     * iOS App Group suite id (`group.*`).
     * Ignored on Android.
     */
    abstract val iosGroupId: String

    /** Used when prefs are empty or decode fails. */
    abstract val defaultState: S

    /**
     * Declarative UI for [env] + decoded [state].
     */
    @Composable
    abstract fun Content(env: WidgetEnvironment, state: S)

    /**
     * Click handlers for wire `actionId`s used in [Content].
     *
     * Prefer [updateWarpWidgetState] with a `(S) -> S` transform.
     */
    open fun clickHandlers(session: WarpWidgetSession): List<WarpClickHandler<*>> = emptyList()

    /** Decode [S] from prefs (key = [id]); falls back to [defaultState]. */
    fun decodeState(preferences: WarpWidgetPreferences): S {
        val json = preferences.values[id] ?: return defaultState
        return runCatching { stateJson.decodeFromString(stateSerializer, json) }
            .getOrDefault(defaultState)
    }

    /** Encode [state] for prefs storage. */
    fun encodeState(state: S): String =
        stateJson.encodeToString(stateSerializer, state)

    /**
     * Host entry: resolve [state] from [preferences] and call [Content].
     */
    @Composable
    fun ComposeContent(env: WidgetEnvironment, preferences: WarpWidgetPreferences) {
        Content(env, decodeState(preferences))
    }

    companion object {
        internal val stateJson = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}

/**
 * Serializable payload for timeline entries or debugging.
 */
@Serializable
data class WarpWidgetSnapshot(
    val widgetId: String,
    /** Pretty-printed [WarpNode] JSON for SwiftUI / debug. */
    val nodeJson: String,
    val environment: WidgetEnvironment,
    val preferences: WarpWidgetPreferences = WarpWidgetPreferences(),
)

/**
 * Platform consumption surface for a [WarpWidget].
 */
object WarpWidgetHost {
    private var lastWidget: WarpWidget<*>? = null
    private var lastSession: WarpWidgetSession? = null

    /**
     * Resolve prefs for a compose pass: [WarpWidgetSession.preferences] if set, else
     * [WarpWidgetStateStore.read].
     */
    fun preferences(widget: WarpWidget<*>, session: WarpWidgetSession): WarpWidgetPreferences =
        session.preferences
            ?: runBlocking { WarpWidgetStateStore.read(session.context, widget.id) }

    /**
     * Run [WarpWidget.ComposeContent] under [ProvideWarpWidgetPreferences] → [WarpNode].
     */
    fun compose(widget: WarpWidget<*>, session: WarpWidgetSession): WarpNode {
        val prefs = preferences(widget, session)
        return composeWarp {
            ProvideWarpWidgetPreferences(prefs) {
                widget.ComposeContent(session.environment, prefs)
            }
        }
    }

    /** [compose] then serialize to JSON for WidgetKit. */
    fun composeJson(widget: WarpWidget<*>, session: WarpWidgetSession): String =
        compose(widget, session).toJson()

    fun handlers(
        widget: WarpWidget<*>,
        session: WarpWidgetSession,
    ): List<WarpClickHandler<*>> = widget.clickHandlers(session)

    fun prepare(widget: WarpWidget<*>, session: WarpWidgetSession) {
        lastWidget = widget
        lastSession = session
        platformRegisterClickHandlers(handlers(widget, session))
        platformInstallPrepareHandler { reprepare() }
    }

    fun reprepare() {
        val widget = lastWidget ?: return
        val session = lastSession ?: return
        platformRegisterClickHandlers(handlers(widget, session))
    }

    fun dispatchClick(
        widget: WarpWidget<*>,
        session: WarpWidgetSession,
        actionId: String,
        parametersJson: String,
    ) {
        prepare(widget, session)
        platformDispatchClick(actionId, parametersJson)
    }

    fun dispatchClick(actionId: String, parametersJson: String) {
        reprepare()
        platformDispatchClick(actionId, parametersJson)
    }

    fun snapshot(widget: WarpWidget<*>, session: WarpWidgetSession): WarpWidgetSnapshot {
        val prefs = preferences(widget, session)
        return WarpWidgetSnapshot(
            widgetId = widget.id,
            nodeJson = composeJson(widget, session),
            environment = session.environment,
            preferences = prefs,
        )
    }
}
