package com.atriidev.warp_ui

/**
 * Registry of wire `actionId` → [WarpClickHandler] dispatch targets.
 *
 * Populated by [WarpRender] (or iOS `registerWarpClicks`). Platform callbacks call [dispatch]:
 * - **Android:** Glance `ActionCallback`
 * - **iOS:** `dispatchWarpClick` / `WarpClickBridge` → Kotlin
 */
import com.atriidev.warp_runtime.log.WarpLogger

object WarpClicksRegistry {
    private const val TAG = "WarpClicksRegistry"
    private val handlers = mutableMapOf<String, suspend (Map<String, String>) -> Unit>()

    /** Replaces all handlers (clears previous widget’s actions). */
    fun register(handlers: List<WarpClickHandler<*>>) {
        this.handlers.clear()
        handlers.forEach(::registerOne)
        WarpLogger.d(TAG, "Registered ${this.handlers.size} click action handler(s): ${this.handlers.keys}")
    }

    /** True when [actionId] was registered (warm process / after [WarpRender]). */
    fun hasHandler(actionId: String): Boolean = handlers.containsKey(actionId)

    /**
     * Invokes the handler for [actionId], if registered.
     *
     * @param actionId WARP JSON `onClick.actionId` (e.g. `"increment"`)
     * @return true if a handler ran
     */
    suspend fun dispatch(actionId: String, parameters: Map<String, String>): Boolean {
        val handler = handlers[actionId]
        if (handler == null) {
            WarpLogger.w(
                TAG,
                "No handler for actionId=$actionId (registered keys: ${handlers.keys})",
            )
            return false
        }
        WarpLogger.i(TAG, "Dispatching click actionId=$actionId params=$parameters")
        handler.invoke(parameters)
        return true
    }

    private fun registerOne(handler: WarpClickHandler<*>) {
        handler.registerEntries { wireId, entryHandler ->
            handlers[wireId] = entryHandler
        }
    }
}
