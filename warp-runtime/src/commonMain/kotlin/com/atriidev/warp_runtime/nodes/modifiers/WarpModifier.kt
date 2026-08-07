/**
 * Serializable styling modifiers attached to [com.atriidev.warp_runtime.nodes.WarpNode] instances.
 *
 * Modifiers describe **what** to apply (padding values, etc.), not how a specific platform draws them.
 * Platform renderers interpret these values when converting a [WarpNode] tree to native widget UI.
 */
package com.atriidev.warp_runtime.nodes.modifiers

import kotlinx.serialization.Serializable

/**
 * Sequential modifier chain — like Compose `Modifier.padding().padding()`.
 *
 * Each factory appends an element; renderers fold them (padding **adds** per edge).
 *
 * ```
 * WarpColumn(
 *     modifier = WarpModifier
 *         .padding(8)
 *         .padding(horizontal = 4, vertical = 12),
 * ) { … }
 * ```
 *
 * JSON preserves order:
 * ```json
 * "modifier": {
 *   "elements": [
 *     { "type": "padding", "start": 8, "end": 8, "top": 8, "bottom": 8 },
 *     { "type": "padding", "start": 4, "end": 4, "top": 12, "bottom": 12 }
 *   ]
 * }
 * ```
 */
@Serializable
data class WarpModifier(
    val elements: List<WarpModifierElement> = emptyList(),
) {
    companion object {
        /** Empty chain — same as `WarpModifier()`. */
        val Default: WarpModifier = WarpModifier()

        fun padding(all: Int): WarpModifier = Default.padding(all)

        fun padding(
            start: Int,
            end: Int,
            top: Int,
            bottom: Int,
        ): WarpModifier = Default.padding(start, end, top, bottom)

        fun padding(
            horizontal: Int,
            vertical: Int,
        ): WarpModifier = Default.padding(horizontal, vertical)

        fun padding(paddingValues: WarpPadding): WarpModifier =
            Default.padding(paddingValues)
    }

    /** Append another chain (Compose `then`). */
    fun then(other: WarpModifier): WarpModifier =
        WarpModifier(elements = elements + other.elements)

    /** Append a single element. */
    fun then(element: WarpModifierElement): WarpModifier =
        copy(elements = elements + element)

    fun padding(paddingValues: WarpPadding): WarpModifier =
        then(WarpPaddingElement(paddingValues))

    fun padding(all: Int): WarpModifier =
        padding(WarpPadding(all, all, all, all))

    fun padding(
        start: Int,
        end: Int,
        top: Int,
        bottom: Int,
    ): WarpModifier = then(WarpPaddingElement(start, end, top, bottom))

    fun padding(
        horizontal: Int,
        vertical: Int,
    ): WarpModifier = padding(
        start = horizontal,
        end = horizontal,
        top = vertical,
        bottom = vertical,
    )

    /**
     * Fold sequential padding elements (Compose stacks insets).
     *
     * Renderers should use this rather than reading a single padding field.
     */
    fun resolvedPadding(): WarpPadding =
        elements.filterIsInstance<WarpPaddingElement>()
            .fold(WarpPadding.Zero) { acc, pad ->
                acc + WarpPadding(pad.start, pad.end, pad.top, pad.bottom)
            }
}
