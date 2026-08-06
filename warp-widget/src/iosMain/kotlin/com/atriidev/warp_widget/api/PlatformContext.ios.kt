package com.atriidev.warp_widget.api

/**
 * iOS [PlatformContext].
 *
 * [appGroupId] must match Xcode App Groups + Swift `warpAppGroupId`
 * (`warpWidgetKit`). Used by [com.atriidev.warp_widget.WarpWidgetStateStore]
 * (`UserDefaults` suite).
 */
actual class PlatformContext(
    val appGroupId: String = DEFAULT_IOS_APP_GROUP_ID,
)

/** Default suite shared with the demo app / `warpWidgetKit.warpAppGroupId`. */
const val DEFAULT_IOS_APP_GROUP_ID: String = "group.com.atriidev.kmpwidget"
