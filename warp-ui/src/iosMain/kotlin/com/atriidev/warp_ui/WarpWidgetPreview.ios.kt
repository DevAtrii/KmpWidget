package com.atriidev.warp_ui

import kotlinx.cinterop.ExperimentalForeignApi
import warpWidgetKit.WarpWidgetBridge

/**
 * Native SwiftUI widget preview in a UIKit host.
 *
 * Use from the iOS app while the shared widget-api layer is being built.
 */
@OptIn(ExperimentalForeignApi::class)
fun warpWidgetPreviewViewController() =
    WarpWidgetBridge.shared().makePreviewViewController()
