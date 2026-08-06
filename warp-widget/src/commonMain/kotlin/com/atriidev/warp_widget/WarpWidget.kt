package com.atriidev.warp_widget

import androidx.compose.runtime.Composable
import com.atriidev.warp_runtime.compose.composeWarp
import com.atriidev.warp_runtime.compose.toJson
import com.atriidev.warp_runtime.nodes.WarpNode
import com.atriidev.warp_ui.WarpClickHandler
import com.atriidev.warp_widget.api.PlatformContext
import com.atriidev.warp_widget.api.WidgetEnvironment
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

/**
 * One render / click pass from a platform host.
 *
 * Hosts **must** supply [environment] and [context] (never invented by [WarpWidgetHost]):
 *
 * - **Android:** [rememberGlanceWidgetSession] / [glanceWidgetEnvironment]
 * - **iOS:** `WarpWidgetKitEnv.from(context:).makeSession()` after [installWarpWidgetKitBridge]
 *
 * @property context Platform I/O handle ([PlatformContext])
 * @property environment Glance ∩ WidgetKit snapshot for this render
 * @property preferences Optional preloaded prefs; when null, [WarpWidgetHost] reads
 *   [WarpWidgetStateStore]
 */
data class WarpWidgetSession(
    val context: PlatformContext,
    val environment: WidgetEnvironment,
    val preferences: WarpWidgetPreferences? = null,
)

/**
 * Shared widget definition for Glance and WidgetKit.
 *
 * Implement once in `commonMain`. Platforms only build a [WarpWidgetSession] and call
 * [WarpWidgetHost].
 *
 * ### Content
 * Use warp-runtime composables (`WarpColumn`, `WarpText`, `WarpButton`, …).
 * Read state with [currentState] / [currentPreferences]. Branch on [WidgetEnvironment]
 * (family, theme, size, preview) — not on platform APIs.
 *
 * ### Size classes
 * Declared on the **host** (WidgetKit `.supportedFamilies`, Glance provider sizes).
 * [WidgetEnvironment.family] is the family for **this** render only.
 *
 * ### iOS (Swift)
 * ```swift
 * WarpWidgetKitMappingKt.installWarpWidgetKitBridge()
 * let session: WarpWidgetSession = WarpWidgetKitEnv.from(context: context).makeSession()
 * WarpWidgetHost.shared.prepare(widget: CounterWarpWidget.shared, session: session)
 * let json = WarpWidgetHost.shared.composeJson(widget: CounterWarpWidget.shared, session: session)
 * ```
 *
 * ### Android (Glance)
 * ```kotlin
 * val session = rememberGlanceWidgetSession(context)
 * WarpRender(
 *     node = WarpWidgetHost.compose(CounterWarpWidget, session),
 *     handlers = WarpWidgetHost.handlers(CounterWarpWidget, session),
 * )
 * ```
 */
interface WarpWidget {
    /**
     * Stable widget kind id.
     *
     * Used for timeline kind / AppIntent routing, Glance registry lookup, and iOS
     * `WidgetCenter.reloadTimelines(ofKind:)` — keep in sync with WidgetKit `kind`.
     */
    val id: String

    /**
     * Declarative UI for the current [env] and prefs from [currentState].
     */
    @Composable
    fun Content(env: WidgetEnvironment)

    /**
     * Click handlers for wire `actionId`s used in [Content].
     *
     * Prefer [updateWarpWidgetState] inside handlers so prefs + reload stay portable.
     */
    fun clickHandlers(session: WarpWidgetSession): List<WarpClickHandler<*>>
}

/**
 * Serializable payload for timeline entries or debugging.
 *
 * Produced by [WarpWidgetHost.snapshot].
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
 *
 * Hosts should not call `composeWarp` / [com.atriidev.warp_ui.WarpClicksRegistry] directly.
 * Every entry point requires an explicit [WarpWidgetSession].
 */
object WarpWidgetHost {
    private var lastWidget: WarpWidget? = null
    private var lastSession: WarpWidgetSession? = null

    /**
     * Resolve prefs for a compose pass: [WarpWidgetSession.preferences] if set, else
     * [WarpWidgetStateStore.read].
     *
     * Glance hosts should pass `currentState<Preferences>().toWarpPreferences()` on the
     * session so composition matches the Glance datastore.
     */
    fun preferences(widget: WarpWidget, session: WarpWidgetSession): WarpWidgetPreferences =
        session.preferences
            ?: runBlocking { WarpWidgetStateStore.read(session.context, widget.id) }

    /**
     * Run [WarpWidget.Content] under [ProvideWarpWidgetPreferences] → [WarpNode].
     *
     * Use as input to Glance [com.atriidev.warp_ui.WarpRender].
     */
    fun compose(widget: WarpWidget, session: WarpWidgetSession): WarpNode {
        val prefs = preferences(widget, session)
        return composeWarp {
            ProvideWarpWidgetPreferences(prefs) {
                widget.Content(session.environment)
            }
        }
    }

    /**
     * [compose] then serialize to JSON for WidgetKit (`WarpSwiftUIRootView`).
     */
    fun composeJson(widget: WarpWidget, session: WarpWidgetSession): String =
        compose(widget, session).toJson()

    /** [WarpWidget.clickHandlers] for [session] (Glance `WarpRender` / iOS prepare). */
    fun handlers(
        widget: WarpWidget,
        session: WarpWidgetSession,
    ): List<WarpClickHandler<*>> = widget.clickHandlers(session)

    /**
     * Register click handlers and remember [widget] + [session] for AppIntent cold start.
     *
     * - **iOS:** [com.atriidev.warp_ui.registerWarpClicks] + [WarpClickBridge] prepare → [reprepare];
     *   also installs [installWarpWidgetKitBridge]
     * - **Android:** [com.atriidev.warp_ui.WarpClicksRegistry] (Glance also registers via `WarpRender`)
     */
    fun prepare(widget: WarpWidget, session: WarpWidgetSession) {
        lastWidget = widget
        lastSession = session
        platformRegisterClickHandlers(handlers(widget, session))
        platformInstallPrepareHandler { reprepare() }
    }

    /**
     * Re-register handlers from the last [prepare] (WidgetKit AppIntent process start).
     */
    fun reprepare() {
        val widget = lastWidget ?: return
        val session = lastSession ?: return
        platformRegisterClickHandlers(handlers(widget, session))
    }

    /**
     * AppIntent / bridge entry: [prepare] then dispatch wire [actionId].
     *
     * @param parametersJson JSON object of string params, or `"{}"`
     */
    fun dispatchClick(
        widget: WarpWidget,
        session: WarpWidgetSession,
        actionId: String,
        parametersJson: String,
    ) {
        prepare(widget, session)
        platformDispatchClick(actionId, parametersJson)
    }

    /**
     * Dispatch using the last [prepare]d widget/session (after [reprepare]).
     */
    fun dispatchClick(actionId: String, parametersJson: String) {
        reprepare()
        platformDispatchClick(actionId, parametersJson)
    }

    /** Compose + pack [WarpWidgetSnapshot] for persistence / debug. */
    fun snapshot(widget: WarpWidget, session: WarpWidgetSession): WarpWidgetSnapshot {
        val prefs = preferences(widget, session)
        return WarpWidgetSnapshot(
            widgetId = widget.id,
            nodeJson = composeJson(widget, session),
            environment = session.environment,
            preferences = prefs,
        )
    }
}
