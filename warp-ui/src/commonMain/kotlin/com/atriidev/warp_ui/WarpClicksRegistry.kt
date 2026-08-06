package com.atriidev.warp_ui

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

    suspend fun dispatch(actionId: String, parameters: Map<String, String>) {
        handlers[actionId]?.invoke(parameters)
    }

    private fun registerOne(handler: WarpClickHandler<*>) {
        handler.registerEntries { wireId, entryHandler ->
            handlers[wireId] = entryHandler
        }
    }
}
