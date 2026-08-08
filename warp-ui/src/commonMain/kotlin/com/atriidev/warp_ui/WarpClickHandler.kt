package com.atriidev.warp_ui

import com.atriidev.warp_runtime.nodes.actions.WarpActionFamily
import com.atriidev.warp_runtime.nodes.actions.warpActionFamily
import kotlinx.serialization.KSerializer

/**
 * Pure click handler for a widget `@Serializable` sealed click-action hierarchy.
 *
 * Pass the generated serializer — e.g. `CounterActions.serializer()` — wire codec is automatic.
 *
 * ### iOS
 * Swift `AppIntent` → `dispatchWarpClick` → registry → [onAction].
 *
 * ### Android
 * Glance `ActionCallback` → registry → [onAction].
 */
abstract class WarpClickHandler<A : Any>(
    serializer: KSerializer<A>,
) {
    private val family: WarpActionFamily<A> = warpActionFamily(serializer)

    /** Handle a typed action after the platform forwarded and decoded the wire payload. */
    abstract suspend fun onAction(action: A)

    internal fun registerEntries(
        register: (wireId: String, handler: suspend (Map<String, String>) -> Unit) -> Unit,
    ) {
        family.actionIds.forEach { wireId ->
            register(wireId) { parameters ->
                val action = family.decode(wireId, parameters) ?: return@register
                onAction(action)
            }
        }
    }
}
