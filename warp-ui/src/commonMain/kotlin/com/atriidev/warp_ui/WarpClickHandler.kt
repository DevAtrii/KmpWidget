package com.atriidev.warp_ui

import com.atriidev.warp_runtime.nodes.actions.WarpActionId
import kotlin.reflect.KClass

/**
 * Pure click handler for a widget action enum [T].
 *
 * Register instances with [WarpClicksRegistry] via [WarpRender]. Platform renderers
 * look up handlers by wire `actionId` and invoke [onClick] with the decoded enum value.
 */
abstract class WarpClickHandler<T>(
    val actionIdType: KClass<T>,
    private val actionEntries: List<T>,
) where T : Enum<T>, T : WarpActionId {
    abstract suspend fun onClick(actionId: T, parameters: Map<String, String>)

    internal fun registerEntries(
        register: (wireId: String, handler: suspend (Map<String, String>) -> Unit) -> Unit,
    ) {
        actionEntries.forEach { action ->
            register(action.actionId) { parameters -> onClick(action, parameters) }
        }
    }

    internal suspend fun dispatch(actionId: Enum<*>, parameters: Map<String, String>) {
        @Suppress("UNCHECKED_CAST")
        onClick(actionId as T, parameters)
    }
}
