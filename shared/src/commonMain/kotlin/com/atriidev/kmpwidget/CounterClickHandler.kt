package com.atriidev.kmpwidget

import com.atriidev.warp_ui.WarpClickHandler
import com.atriidev.warp_widget.api.PlatformContext
import com.atriidev.warp_widget.api.WarpWidgetFamily
import com.atriidev.warp_widget.api.WidgetEnvironment
import com.atriidev.warp_widget.api.makeWidgetEnvironment

/**
 * Legacy factory kept for in-app previews that still take a [PlatformContext].
 *
 * Prefer [CounterWarpWidget.clickHandlers] / [WarpWidgetHost.handlers].
 */
fun counterWidgetClickHandlers(
    context: PlatformContext,
    environment: WidgetEnvironment = makeWidgetEnvironment(
        family = WarpWidgetFamily.SYSTEM_SMALL,
        isPreview = true,
    ),
): List<WarpClickHandler<*>> =
    CounterWarpWidget.clickHandlers(
        counterWidgetSession(context = context, environment = environment),
    )
