package com.atriidev.warp_ui

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView

/** UIKit preview host for [warpRender] output. */
@OptIn(ExperimentalForeignApi::class)
fun WarpSwiftUIView.previewViewController() = makePreviewViewController()

/** UIView for Compose [UIKitView] interop. */
@OptIn(ExperimentalForeignApi::class)
fun WarpSwiftUIView.previewView()  = makePreviewView()
