package com.atriidev.kmpwidget

import com.atriidev.warp_widget.WarpWidgetPreferences
import com.atriidev.warp_widget.WarpWidgetSession
import com.atriidev.warp_widget.api.PlatformContext
import com.atriidev.warp_widget.api.WidgetEnvironment
/** Build a [WarpWidgetSession] for [CounterWarpWidget] — host supplies [environment]. */
fun counterWidgetSession(
    context: PlatformContext,
    environment: WidgetEnvironment,
    preferences: WarpWidgetPreferences? = null,
): WarpWidgetSession = WarpWidgetSession(
    context = context,
    environment = environment,
    preferences = preferences,
)
