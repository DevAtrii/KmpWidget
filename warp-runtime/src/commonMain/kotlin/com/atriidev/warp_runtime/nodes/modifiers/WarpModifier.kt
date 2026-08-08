/**
 * Serializable styling modifiers attached to [com.atriidev.warp_runtime.nodes.WarpNode] instances.
 *
 * Modifiers describe **what** to apply; platform renderers interpret them.
 * Native code must not invent styles — only apply elements present in this chain.
 *
 * Text/typography is **not** a modifier (Compose-style) — pass as args on text composables later.
 */
package com.atriidev.warp_runtime.nodes.modifiers

import androidx.compose.runtime.Stable
import com.atriidev.warp_runtime.nodes.actions.ClickAction
import com.atriidev.warp_runtime.nodes.actions.WarpAction
import com.atriidev.warp_runtime.nodes.actions.WarpActionId
import com.atriidev.warp_runtime.nodes.actions.asClickAction
import com.atriidev.warp_runtime.unit.Dp
import com.atriidev.warp_runtime.unit.dp
import kotlinx.serialization.Serializable

/**
 * Sequential modifier chain — like Compose `Modifier.padding().background()`.
 *
 * JSON preserves order in `elements[]`.
 */
@Serializable
@Stable
data class WarpModifier(
    val elements: List<WarpModifierElement> = emptyList(),
) {
    companion object {
        val Default: WarpModifier = WarpModifier()

        fun padding(all: Dp): WarpModifier = Default.padding(all)
        fun padding(all: Number): WarpModifier = Default.padding(all.toFloat().dp)

        fun padding(
            start: Dp = 0.dp,
            end: Dp = 0.dp,
            top: Dp = 0.dp,
            bottom: Dp = 0.dp,
        ): WarpModifier = Default.padding(start, end, top, bottom)

        fun padding(
            start: Number,
            end: Number,
            top: Number,
            bottom: Number,
        ): WarpModifier = Default.padding(start.toFloat().dp, end.toFloat().dp, top.toFloat().dp, bottom.toFloat().dp)

        fun padding(
            horizontal: Dp = 0.dp,
            vertical: Dp = 0.dp,
        ): WarpModifier = Default.padding(horizontal, vertical)

        fun padding(
            horizontal: Number,
            vertical: Number,
        ): WarpModifier = Default.padding(horizontal.toFloat().dp, vertical.toFloat().dp)

        fun padding(paddingValues: WarpPadding): WarpModifier =
            Default.padding(paddingValues)

        fun background(color: WarpColor): WarpModifier = Default.background(color)

        fun background(hex: String): WarpModifier = Default.background(hex)

        fun cornerRadius(radius: Dp): WarpModifier = Default.cornerRadius(radius)
        fun cornerRadius(radius: Number): WarpModifier = Default.cornerRadius(radius.toFloat().dp)

        fun alpha(alpha: Float): WarpModifier = Default.alpha(alpha)

        fun border(width: Dp, color: WarpColor): WarpModifier =
            Default.border(width, color)
        fun border(width: Number, color: WarpColor): WarpModifier =
            Default.border(width.toFloat().dp, color)

        fun border(width: Dp, hex: String): WarpModifier =
            Default.border(width, hex)
        fun border(width: Number, hex: String): WarpModifier =
            Default.border(width.toFloat().dp, hex)

        fun clickable(action: Any): WarpModifier = Default.clickable(action)

        fun clickable(actionId: WarpActionId): WarpModifier =
            Default.clickable(actionId.asClickAction())

        inline fun <reified A : WarpAction> clickable(action: A): WarpModifier =
            Default.clickable(action)

        fun visibility(visibility: WarpVisibility): WarpModifier =
            Default.visibility(visibility)

        fun fillMaxWidth(): WarpModifier = Default.fillMaxWidth()

        fun fillMaxHeight(): WarpModifier = Default.fillMaxHeight()

        fun fillMaxSize(): WarpModifier = Default.fillMaxSize()

        fun width(width: Dp): WarpModifier = Default.width(width)
        fun width(width: Number): WarpModifier = Default.width(width.toFloat().dp)

        fun height(height: Dp): WarpModifier = Default.height(height)
        fun height(height: Number): WarpModifier = Default.height(height.toFloat().dp)

        fun size(size: Dp): WarpModifier = Default.size(size)
        fun size(size: Number): WarpModifier = Default.size(size.toFloat().dp)

        fun size(width: Dp, height: Dp): WarpModifier = Default.size(width, height)
        fun size(width: Number, height: Number): WarpModifier = Default.size(width.toFloat().dp, height.toFloat().dp)

        fun weight(weight: Float = 1f): WarpModifier = Default.weight(weight)

        fun wrapContentWidth(): WarpModifier = Default.wrapContentWidth()

        fun wrapContentHeight(): WarpModifier = Default.wrapContentHeight()

        fun wrapContentSize(): WarpModifier = Default.wrapContentSize()
    }

    fun then(other: WarpModifier): WarpModifier =
        WarpModifier(elements = elements + other.elements)

    fun then(element: WarpModifierElement): WarpModifier =
        copy(elements = elements + element)

    // region Spacing / appearance

    fun padding(paddingValues: WarpPadding): WarpModifier =
        then(WarpPaddingElement(paddingValues))

    fun padding(all: Dp): WarpModifier =
        padding(WarpPadding(all, all, all, all))

    fun padding(all: Number): WarpModifier =
        padding(all.toFloat().dp)

    fun padding(
        start: Dp = 0.dp,
        end: Dp = 0.dp,
        top: Dp = 0.dp,
        bottom: Dp = 0.dp,
    ): WarpModifier = then(WarpPaddingElement(start, end, top, bottom))

    fun padding(
        start: Number,
        end: Number,
        top: Number,
        bottom: Number,
    ): WarpModifier = padding(start.toFloat().dp, end.toFloat().dp, top.toFloat().dp, bottom.toFloat().dp)

    fun padding(
        horizontal: Dp = 0.dp,
        vertical: Dp = 0.dp,
    ): WarpModifier = padding(
        start = horizontal,
        end = horizontal,
        top = vertical,
        bottom = vertical,
    )

    fun padding(
        horizontal: Number,
        vertical: Number,
    ): WarpModifier = padding(horizontal.toFloat().dp, vertical.toFloat().dp)

    fun background(color: WarpColor): WarpModifier =
        then(WarpBackgroundElement(color))

    fun background(hex: String): WarpModifier =
        background(WarpColor(hex))

    fun cornerRadius(radius: Dp): WarpModifier =
        then(WarpCornerRadiusElement(radius))

    fun cornerRadius(radius: Number): WarpModifier =
        cornerRadius(radius.toFloat().dp)

    fun alpha(alpha: Float): WarpModifier =
        then(WarpAlphaElement(alpha))

    fun border(width: Dp, color: WarpColor): WarpModifier =
        then(WarpBorderElement(width, color))

    fun border(width: Number, color: WarpColor): WarpModifier =
        border(width.toFloat().dp, color)

    fun border(width: Dp, hex: String): WarpModifier =
        border(width, WarpColor(hex))

    fun border(width: Number, hex: String): WarpModifier =
        border(width.toFloat().dp, hex)

    fun clickable(action: Any): WarpModifier =
        then(WarpClickableElement(action.asClickAction()))

    @Deprecated(
        message = "Use clickable(action: Any) instead.",
        replaceWith = ReplaceWith("clickable(actionId.asClickAction())"),
        level = DeprecationLevel.ERROR,
    )
    fun clickable(actionId: WarpActionId): WarpModifier =
        clickable(actionId.asClickAction())

    inline fun <reified A : WarpAction> clickable(action: A): WarpModifier =
        then(WarpClickableElement(action.asClickAction()))

    fun visibility(visibility: WarpVisibility): WarpModifier =
        then(WarpVisibilityElement(visibility))

    // endregion

    // region Layout size

    fun fillMaxWidth(): WarpModifier = then(WarpFillMaxWidthElement)

    fun fillMaxHeight(): WarpModifier = then(WarpFillMaxHeightElement)

    fun fillMaxSize(): WarpModifier = then(WarpFillMaxSizeElement)

    fun width(width: Dp): WarpModifier = then(WarpWidthElement(width))
    fun width(width: Number): WarpModifier = width(width.toFloat().dp)

    fun height(height: Dp): WarpModifier = then(WarpHeightElement(height))
    fun height(height: Number): WarpModifier = height(height.toFloat().dp)

    fun size(size: Dp): WarpModifier = size(size, size)
    fun size(size: Number): WarpModifier = size(size.toFloat().dp)

    fun size(width: Dp, height: Dp): WarpModifier =
        then(WarpSizeElement(width, height))

    fun size(width: Number, height: Number): WarpModifier =
        size(width.toFloat().dp, height.toFloat().dp)

    fun weight(weight: Float = 1f): WarpModifier =
        then(WarpWeightElement(weight))

    fun wrapContentWidth(): WarpModifier = then(WarpWrapContentWidthElement)

    fun wrapContentHeight(): WarpModifier = then(WarpWrapContentHeightElement)

    fun wrapContentSize(): WarpModifier = then(WarpWrapContentSizeElement)

    // endregion

    // region Resolved (for renderers)

   internal fun resolvedPadding(): WarpPadding =
        elements.filterIsInstance<WarpPaddingElement>()
            .fold(WarpPadding.Zero) { acc, pad ->
                acc + WarpPadding(pad.start, pad.end, pad.top, pad.bottom)
            }

    /**
     * Last clickable in the chain, if any.
     *
     * Renderers must prefer this over node-level `onClick` (e.g. button).
     */
    fun resolvedClickable(): ClickAction? =
        elements.filterIsInstance<WarpClickableElement>().lastOrNull()?.action

    /**
     * Effective click: modifier [resolvedClickable] wins over [nodeOnClick].
     */
    fun resolveClickAction(nodeOnClick: WarpAction?): ClickAction? =
        resolvedClickable() ?: (nodeOnClick as? ClickAction)

    fun resolvedVisibility(): WarpVisibility? =
        elements.filterIsInstance<WarpVisibilityElement>().lastOrNull()?.visibility

    fun resolvedBackground(): WarpColor? =
        elements.filterIsInstance<WarpBackgroundElement>().lastOrNull()?.color

    fun resolvedCornerRadius(): Dp? =
        elements.filterIsInstance<WarpCornerRadiusElement>().lastOrNull()?.radius

    fun resolvedAlpha(): Float? =
        elements.filterIsInstance<WarpAlphaElement>().lastOrNull()?.alpha

    fun resolvedBorder(): WarpBorderElement? =
        elements.filterIsInstance<WarpBorderElement>().lastOrNull()

    fun resolvedWeight(): Float? =
        elements.filterIsInstance<WarpWeightElement>().lastOrNull()?.weight

    // endregion
}
