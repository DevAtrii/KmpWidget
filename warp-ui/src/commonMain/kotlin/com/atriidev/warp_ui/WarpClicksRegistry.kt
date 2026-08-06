package com.atriidev.warp_ui

import com.atriidev.warp_runtime.nodes.actions.WarpActionId

/**
 * Registry of wire `actionId` → [WarpClickHandler] dispatch targets.
 *
 * Populated by [WarpRender] on each composition. Platform callbacks call [dispatch].
 */
object WarpClicksRegistry {
    private val handlers = mutableMapOf<String, suspend (Map<String, String>) -> Unit>()

    fun register(handlers: List<WarpClickHandler<*>>) {
        this.handlers.clear()
        handlers.forEach(::registerOne)
    }

    internal suspend fun dispatch(actionId: String, parameters: Map<String, String>) {
        handlers[actionId]?.invoke(parameters)
    }

    private fun registerOne(handler: WarpClickHandler<*>) {
        handler.actionIdType.java.enumConstants?.forEach { constant ->
            val action = constant as Enum<*>
            val wireId = (constant as WarpActionId).actionId
            handlers[wireId] = { parameters -> handler.dispatch(action, parameters) }
        }
    }
}
