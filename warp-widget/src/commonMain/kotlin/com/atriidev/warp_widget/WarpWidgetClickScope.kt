package com.atriidev.warp_widget

/**
 * Per-click [WarpWidgetId] override.
 *
 * Glance / AppIntent registry is process-global — last [WarpRender] would otherwise leave
 * handlers bound to the wrong instance. Platforms set this around dispatch so
 * [updateWarpWidgetState] (session) targets the tapped widget.
 */
object WarpWidgetClickScope {
    private var current: WarpWidgetId? = null

    fun current(): WarpWidgetId? = current

    fun <R> withWidgetId(id: WarpWidgetId, block: () -> R): R {
        val previous = current
        current = id
        return try {
            block()
        } finally {
            current = previous
        }
    }
}
