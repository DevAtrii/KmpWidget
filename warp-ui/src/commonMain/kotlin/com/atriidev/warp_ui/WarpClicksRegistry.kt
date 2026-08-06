package com.atriidev.warp_ui

/**
 * Registry of wire `actionId` → [WarpClickHandler] dispatch targets.
 *
 * Populated by [WarpRender] (or iOS `registerWarpClicks`). Platform callbacks call [dispatch]:
 * - **Android:** Glance `ActionCallback`
 * - **iOS:** `dispatchWarpClick` / `WarpClickBridge` → Kotlin
 */
object WarpClicksRegistry {
    private val handlers = mutableMapOf<String, suspend (Map<String, String>) -> Unit>()

    /** Replaces all handlers (clears previous widget’s actions). */
    fun register(handlers: List<WarpClickHandler<*>>) {
        this.handlers.clear()
        handlers.forEach(::registerOne)
    }

    /**
     * Invokes the handler for [actionId], if registered.
     *
     * @param actionId WARP JSON `onClick.actionId` (e.g. `"increment"`)
     */
    suspend fun dispatch(actionId: String, parameters: Map<String, String>) {
        handlers[actionId]?.invoke(parameters)
    }

    private fun registerOne(handler: WarpClickHandler<*>) {
        handler.registerEntries { wireId, entryHandler ->
            handlers[wireId] = entryHandler
        }
    }
}
