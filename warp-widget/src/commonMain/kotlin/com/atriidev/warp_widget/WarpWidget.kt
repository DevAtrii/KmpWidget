package com.atriidev.warp_widget

import androidx.compose.runtime.Composable
import com.atriidev.warp_runtime.compose.composeWarp
import com.atriidev.warp_runtime.compose.toJson
import com.atriidev.warp_runtime.nodes.WarpNode
import com.atriidev.warp_ui.WarpClickHandler
import com.atriidev.warp_ui.WarpClicksRegistry
import com.atriidev.warp_widget.api.PlatformContext
import com.atriidev.warp_widget.api.WidgetEnvironment
import com.atriidev.warp_widget.api.WidgetFamily
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

/**
 * One render / click pass from a platform host.
 *
 * - [context] — Android `Context` / iOS host handle
 * - [environment] — Glance ∩ WidgetKit snapshot
 * - [preferences] — optional preloaded prefs (Glance hosts pass `currentState<Preferences>()`
 *   mapped here; if null, [WarpWidgetHost] loads via [WarpWidgetStateStore])
 */
data class WarpWidgetSession(
    val context: PlatformContext,
    val environment: WidgetEnvironment,
    val preferences: WarpWidgetPreferences? = null,
)

/**
 * Shared widget definition (no generic state type — use [currentState] like Glance).
 *
 * ```
 * object CounterKeys {
 *     val Count = WarpStateKey.int("count")
 * }
 *
 * object CounterWarpWidget : WarpWidget {
 *     override val id = "counter"
 *
 *     @Composable
 *     override fun Content(env: WidgetEnvironment) {
 *         val count = currentState(CounterKeys.Count) ?: 0
 *         WarpColumn {
 *             WarpText("$count")
 *             WarpButton("+", CounterActions.Increment.asClickAction())
 *         }
 *     }
 *
 *     override fun clickHandlers(session: WarpWidgetSession) = listOf(
 *         object : WarpClickHandler<CounterActions>(…) {
 *             override suspend fun onClick(actionId: CounterActions, parameters: Map<String, String>) {
 *                 updateWarpWidgetState(session.context, this@CounterWarpWidget) {
 *                     val cur = this[CounterKeys.Count] ?: 0
 *                     this[CounterKeys.Count] = when (actionId) {
 *                         CounterActions.Increment -> cur + 1
 *                         CounterActions.Decrement -> cur - 1
 *                     }
 *                 }
 *             }
 *         },
 *     )
 * }
 * ```
 *
 * ### Persist
 * - **Android:** Glance preferences (`PreferencesGlanceStateDefinition`)
 * - **iOS:** App Group `UserDefaults`
 *
 * ### Update from app (outside widget)
 * ```
 * updateWarpWidgetState(platformContext, CounterWarpWidget) {
 *     this[CounterKeys.Count] = 0
 * }
 * ```
 *
 * ### Android Glance host
 * ```
 * provideContent {
 *     val glancePrefs = currentState<Preferences>()
 *     val session = WarpWidgetSession(
 *         context = PlatformContext(context),
 *         environment = env,
 *         preferences = glancePrefs.toWarpPreferences(), // host mapper
 *     )
 *     WarpRender(
 *         node = WarpWidgetHost.compose(CounterWarpWidget, session),
 *         handlers = WarpWidgetHost.handlers(CounterWarpWidget, session),
 *     )
 * }
 * ```
 *
 * ### iOS WidgetKit host
 * ```
 * WarpWidgetHost.prepare(CounterWarpWidget, session)
 * val json = WarpWidgetHost.composeJson(CounterWarpWidget, session)
 * // Swift: WarpSwiftUIRootView(json:)
 * ```
 */
interface WarpWidget {
    /** Stable widget kind id (timeline kind, Glance registry, AppIntent routing). */
    val id: String

    val supportedFamilies: Set<WidgetFamily>
        get() = setOf(
            WidgetFamily.SYSTEM_SMALL,
            WidgetFamily.SYSTEM_MEDIUM,
            WidgetFamily.SYSTEM_LARGE,
        )

    /**
     * Declarative UI. Read prefs with [currentState] / [currentPreferences] (Glance-style).
     *
     * Use warp-runtime composables only. Branch on [env], not platform APIs.
     */
    @Composable
    fun Content(env: WidgetEnvironment)

    /**
     * Click handlers. Persist via [updateWarpWidgetState] (Glance prefs / UserDefaults + reload).
     */
    fun clickHandlers(session: WarpWidgetSession): List<WarpClickHandler<*>>
}

/**
 * Serializable timeline / debug payload from [WarpWidgetHost.snapshot].
 */
@Serializable
data class WarpWidgetSnapshot(
    val widgetId: String,
    val nodeJson: String,
    val environment: WidgetEnvironment,
    val preferences: WarpWidgetPreferences = WarpWidgetPreferences(),
)

/**
 * Platform consumption surface for a [WarpWidget].
 */
object WarpWidgetHost {
    /**
     * Resolve prefs: session override → [WarpWidgetStateStore.read].
     *
     * Prefer setting [WarpWidgetSession.preferences] inside Glance `provideContent`
     * (`currentState<Preferences>().toWarpPreferences()`).
     */
    fun preferences(widget: WarpWidget, session: WarpWidgetSession): WarpWidgetPreferences =
        session.preferences
            ?: runBlocking { WarpWidgetStateStore.read(session.context, widget.id) }

    /**
     * Compose [WarpWidget.Content] → [WarpNode].
     *
     * Provides [currentState] / [currentPreferences] for the duration of composition.
     */
    fun compose(widget: WarpWidget, session: WarpWidgetSession): WarpNode {
        val prefs = preferences(widget, session)
        return composeWarp {
            ProvideWarpWidgetPreferences(prefs) {
                widget.Content(session.environment)
            }
        }
    }

    /** Compose → JSON (WidgetKit Swift bridge). */
    fun composeJson(widget: WarpWidget, session: WarpWidgetSession): String =
        compose(widget, session).toJson()

    fun handlers(
        widget: WarpWidget,
        session: WarpWidgetSession,
    ): List<WarpClickHandler<*>> = widget.clickHandlers(session)

    /** Register clicks for WidgetKit / AppIntent cold start. */
    fun prepare(widget: WarpWidget, session: WarpWidgetSession) {
        WarpClicksRegistry.register(handlers(widget, session))
    }

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
