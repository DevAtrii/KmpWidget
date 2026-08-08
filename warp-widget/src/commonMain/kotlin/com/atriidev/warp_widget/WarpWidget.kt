package com.atriidev.warp_widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
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
import com.atriidev.warp_runtime.log.WarpLogger

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
    /** This instance (or [WarpWidgetId.ofKind] when [WarpWidget.stateScope] is [WarpWidgetStateScope.Shared]). */
    val widgetId: WarpWidgetId,
)

/**
 * Type-erased widget surface for platform hosts.
 *
 * Swift cannot accept Kotlin `WarpWidget<*>` (`WarpWidget<AnyObject>` is invariant).
 * Host APIs take this interface; app widgets still extend [WarpWidget].
 */
interface WarpWidgetHostApi {
    val id: String
    val iosGroupId: String

    fun clickHandlers(session: WarpWidgetSession): List<WarpClickHandler<*>>

    @Composable
    fun ComposeContent(env: WidgetEnvironment, preferences: WarpWidgetPreferences)
}

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
 *     override suspend fun defaultState() = CounterState()
 *
 *     @Composable
 *     override fun Content(env: WidgetEnvironment, state: CounterState) {
 *         WarpText("${state.count}")
 *     }
 * }
 *
 * // update (Glance-style — always pass WarpWidgetId):
 * updateWarpWidgetState(session, CounterWarpWidget) { it.copy(count = it.count + 1) }
 * ```
 */
@Stable
abstract class WarpWidget<S : Any>(
    private val stateSerializer: KSerializer<S>,
) : WarpWidgetHostApi {
    /**
     * Stable widget kind id.
     *
     * Prefs JSON key, timeline kind, Glance registry, WidgetKit `kind`.
     */
    abstract override val id: String

    /**
     * iOS App Group suite id (`group.*`).
     * Ignored on Android.
     */
    abstract override val iosGroupId: String

    /**
     * Used when prefs are empty or decode fails.
     *
     * This will be used for the first render of the widget.
     */
    abstract suspend fun defaultState(): S

    /**
     * Shared vs per-instance state.
     *
     * - [WarpWidgetStateScope.Shared] — all home-screen instances mirror the same JSON (default).
     * - [WarpWidgetStateScope.Instance] — each [WarpWidgetId] has its own prefs blob.
     */
    open val stateScope: WarpWidgetStateScope get() = WarpWidgetStateScope.Shared

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
    override fun clickHandlers(session: WarpWidgetSession): List<WarpClickHandler<*>> = emptyList()

    /** Decode [S] from prefs (key = [id]); falls back to [defaultState]. */
    suspend fun decodeState(preferences: WarpWidgetPreferences): S {
        val json = preferences.values[id] ?: return defaultState()
        return runCatching { stateJson.decodeFromString(stateSerializer, json) }
            .getOrElse { defaultState() }
    }

    /** Encode [state] for prefs storage. */
    fun encodeState(state: S): String =
        stateJson.encodeToString(stateSerializer, state)

    /**
     * Host entry: resolve [state] from [preferences] and call [Content].
     */
    @Composable
    final override fun ComposeContent(env: WidgetEnvironment, preferences: WarpWidgetPreferences) {
        val state = runBlocking { decodeState(preferences) }
        Content(env, state)
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
 * Platform consumption surface for a [WarpWidgetHostApi].
 */
object WarpWidgetHost {
    private var lastWidget: WarpWidgetHostApi? = null
    private var lastSession: WarpWidgetSession? = null

    /**
     * Resolve prefs for a compose pass: [WarpWidgetSession.preferences] if set, else
     * [WarpWidgetStateStore.read].
     */
    fun preferences(widget: WarpWidgetHostApi, session: WarpWidgetSession): WarpWidgetPreferences =
        session.preferences
            ?: runBlocking {
                WarpWidgetStateStore.read(
                    context = session.context,
                    widget = widget,
                    id = session.widgetId,
                )
            }

    /**
     * Run [WarpWidgetHostApi.ComposeContent] under [ProvideWarpWidgetPreferences] → [WarpNode].
     */
    fun compose(widget: WarpWidgetHostApi, session: WarpWidgetSession): WarpNode {
        WarpLogger.d("WarpWidgetHost", "compose: starting render pass for widget ${widget.id}")
        val prefs = preferences(widget, session)
        return composeWarp {
            ProvideWarpWidgetPreferences(prefs) {
                widget.ComposeContent(session.environment, prefs)
            }
        }
    }

    /**
     * [compose] then serialize to JSON for WidgetKit.
     *
     * Embeds [session.widgetId] at the JSON root (`__warpWidgetId`) so warpWidgetKit
     * can attach it to AppIntent parameters — no per-click Swift plumbing in the app.
     */
    fun composeJson(widget: WarpWidgetHostApi, session: WarpWidgetSession): String =
        embedWarpWidgetIdInRootJson(compose(widget, session).toJson(), session.widgetId)

    fun handlers(
        widget: WarpWidgetHostApi,
        session: WarpWidgetSession,
    ): List<WarpClickHandler<*>> = widget.clickHandlers(session)

    fun prepare(widget: WarpWidgetHostApi, session: WarpWidgetSession) {
        WarpLogger.d("WarpWidgetHost", "prepare: registering handlers for widget ${widget.id}")
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
        widget: WarpWidgetHostApi,
        session: WarpWidgetSession,
        actionId: String,
        parametersJson: String,
    ) {
        val resolvedId = extractWarpWidgetIdFromParametersJson(parametersJson) ?: session.widgetId
        val resolved = if (resolvedId == session.widgetId) {
            session
        } else {
            session.copy(widgetId = resolvedId)
        }
        WarpLogger.d(
            "WarpWidgetHost",
            "WARP_CLICK: dispatch kind=${widget.id} actionId=$actionId " +
                "widgetId=$resolvedId params=$parametersJson"
        )
        WarpWidgetClickScope.withWidgetId(resolvedId) {
            prepare(widget, resolved)
            platformDispatchClick(actionId, parametersJson)
        }
    }

    fun dispatchClick(actionId: String, parametersJson: String) {
        val widget = lastWidget
        val session = lastSession
        if (widget != null && session != null) {
            dispatchClick(widget, session, actionId, parametersJson)
        } else {
            val fromParams = extractWarpWidgetIdFromParametersJson(parametersJson)
            if (fromParams != null) {
                WarpWidgetClickScope.withWidgetId(fromParams) {
                    reprepare()
                    platformDispatchClick(actionId, parametersJson)
                }
            } else {
                reprepare()
                platformDispatchClick(actionId, parametersJson)
            }
        }
    }

    fun snapshot(widget: WarpWidgetHostApi, session: WarpWidgetSession): WarpWidgetSnapshot {
        val prefs = preferences(widget, session)
        return WarpWidgetSnapshot(
            widgetId = widget.id,
            nodeJson = composeJson(widget, session),
            environment = session.environment,
            preferences = prefs,
        )
    }
}
