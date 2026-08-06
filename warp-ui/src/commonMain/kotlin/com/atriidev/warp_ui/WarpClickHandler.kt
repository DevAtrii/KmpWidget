package com.atriidev.warp_ui

import com.atriidev.warp_runtime.nodes.actions.WarpActionId
import kotlin.reflect.KClass

/**
 * Pure click handler for a widget action enum [T].
 *
 * Register instances with [WarpClicksRegistry] via [WarpRender]. Platform renderers
 * look up handlers by wire `actionId` and invoke [onClick] with the decoded enum value.
 *
 * ```
 * class CounterClickHandler(
 *     dataStore: KmpDataStore,
 *     widgetUpdater: WidgetUpdater,
 * ) : WarpClickHandler<CounterActions>(CounterActions::class) {
 *     override suspend fun onClick(actionId: CounterActions, parameters) {
 *         when (actionId) {
 *             CounterActions.Increment -> update(+1)
 *             CounterActions.Decrement -> update(-1)
 *         }
 *     }
 * }
 * ```
 */
abstract class WarpClickHandler<T>(
    val actionIdType: KClass<T>,
) where T : Enum<T>, T : WarpActionId {
    abstract suspend fun onClick(actionId: T, parameters: Map<String, String>)

    internal suspend fun dispatch(actionId: Enum<*>, parameters: Map<String, String>) {
        @Suppress("UNCHECKED_CAST")
        onClick(actionId as T, parameters)
    }
}
